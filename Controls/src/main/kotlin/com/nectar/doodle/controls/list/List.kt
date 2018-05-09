package com.nectar.doodle.controls.list

import com.nectar.doodle.controls.SelectionModel
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.scheduler.Strand
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.ObservableSet
import com.nectar.doodle.utils.SetObserver
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 3/19/18.
 */
interface ItemUIGenerator<T> {
    operator fun invoke(list: List<T, *>, row: T, index: Int, current: Gizmo? = null): Gizmo
}

interface ItemPositioner<T> {
    operator fun invoke(list: List<T, *>, row: T, index: Int): Rectangle

    fun rowFor(list: List<T, *>, y: Double): Int
}

interface ListRenderer<T>: Renderer<List<T, *>> {
    val positioner : ItemPositioner<T>
    val uiGenerator: ItemUIGenerator<T>
}

open class List<T, out M: Model<T>>(
        private        val strand        : Strand,
        protected open val model         : M,
        protected      val selectionModel: SelectionModel<Int>? = null,
        private        val fitContent    : Boolean              = true): Gizmo() {

    private val observableSet by lazy { ObservableSet<List<T, *>, T>(this) }

    val selectionChanged = observableSet.changed

    @Suppress("PrivatePropertyName")
    private val selectionChanged_: SetObserver<SelectionModel<Int>, Int> = { _,removed,added ->
        children.batch {
            (added + removed).forEach {
                update(this, it)
            }
        }
    }

    private var itemUIGenerator: ItemUIGenerator<T>? = null
    private var itemPositioner : ItemPositioner<T>?  = null

    private var firstVisibleRow =  0
    private var lastVisibleRow  = -1

    var renderer: ListRenderer<T>? = null
        set(new) {
            if (new == renderer) { return }

            field = new?.also {
                itemPositioner  = it.positioner
                itemUIGenerator = it.uiGenerator

                children.batch {
                    clear()

                    height = model.size * (model[0]?.let { itemPositioner?.invoke(this@List, it, 0)?.height } ?: 0.0) + insets.run { top + bottom }
                }
            }
        }

    public override var insets
        get(   ) = super.insets
        set(new) { super.insets = new }

    init {
        monitorsDisplayRect = true

        selectionModel?.let { it.changed += selectionChanged_ }
    }

    operator fun get(index: Int) = model[index]

    override fun render(canvas: Canvas) {
        renderer?.render(this, canvas)
    }

    override fun removedFromDisplay() {
        selectionModel?.let { it.changed -= selectionChanged_ }

        super.removedFromDisplay()
    }

    override fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) {
        val oldFirst = firstVisibleRow
        val oldLast  = lastVisibleRow

        firstVisibleRow = new.y.let { when {
            it != old.y -> findRowAt(it, firstVisibleRow)
            else        -> firstVisibleRow
        }}

        lastVisibleRow = (new.y + new.height).let { when {
            it != old.y + old.height -> findRowAt(it, lastVisibleRow)
            else                     -> lastVisibleRow
        }}

        var jobs = emptySequence<() -> Unit>()

        if (oldFirst > firstVisibleRow) {
            val end = min(oldFirst, lastVisibleRow)

            jobs += (firstVisibleRow until end).asSequence().map { { insert(children, it) } }
        }

        if (oldLast < lastVisibleRow) {
            val start = when {
                oldLast > firstVisibleRow -> oldLast + 1
                else                      -> firstVisibleRow
            }

            jobs += (start .. lastVisibleRow).asSequence().map { { insert(children, it) } }
        }

        strand(jobs)
    }

//    override fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) {
//        val oldFirst = firstVisibleRow
//        val oldLast  = lastVisibleRow
//
//        firstVisibleRow = new.y.let { when {
//            it != old.y -> findRowAt(it, firstVisibleRow)
//            else        -> firstVisibleRow
//        }}
//
//        lastVisibleRow = (new.y + new.height).let { when {
//            it != old.y + old.height -> findRowAt(it, lastVisibleRow)
//            else                     -> lastVisibleRow
//        }}
//
//        if (oldFirst > firstVisibleRow) {
//            val end = min(oldFirst, lastVisibleRow)
//
//            (firstVisibleRow until end).asSequence().forEach {
//                insert(children, it)
//            }
//        }
//
//        if (oldLast < lastVisibleRow) {
//            val start = when {
//                oldLast > firstVisibleRow -> oldLast + 1
//                else                      -> firstVisibleRow
//            }
//
//            (start .. lastVisibleRow).asSequence().forEach {
//                insert(children, it)
//            }
//        }
//    }

    fun selected       (row : Int     ) = selectionModel?.contains  (row ) ?: false
    fun addSelection   (rows: Set<Int>) { selectionModel?.addAll    (rows) }
    fun setSelection   (rows: Set<Int>) { selectionModel?.replaceAll(rows) }
    fun removeSelection(rows: Set<Int>) { selectionModel?.removeAll (rows) }
    fun clearSelection (              ) = selectionModel?.clear     (    )

    private fun layout(gizmo: Gizmo, row: T, index: Int) {
        itemPositioner?.let {
            gizmo.bounds = it(this, row, index)
        }
    }

    private fun insert(children: kotlin.collections.MutableList<Gizmo>, index: Int) {
        itemUIGenerator?.let {
            model[index]?.let { row ->
                if (children.size <= lastVisibleRow - firstVisibleRow) {
                    it(this, row, index).also {
                        when {
                            index > children.lastIndex -> children.add(it)
                            else                       -> children.add(index, it)
                        }

                        layout(it, row, index)
                    }
                } else {
                    update(children, index)
                }
            }
        }
    }

    private fun update(children: kotlin.collections.MutableList<Gizmo>, index: Int) {
        if (index in firstVisibleRow .. lastVisibleRow) {
            itemUIGenerator?.let {
                model[index]?.let { row ->
                    val i = index % children.size

                    it(this, row, index, children.getOrNull(i)).also {
                        children[i] = it

                        layout(it, row, index)
                    }
                }
            }
        }
    }

    private fun findRowAt(y: Double, nearbyRow: Int): Int {
        return min(model.size - 1, itemPositioner?.rowFor(this, y) ?: nearbyRow)
    }

    companion object {
        operator fun invoke(strand: Strand, progression: IntProgression, selectionModel: SelectionModel<Int>? = null, fitContent: Boolean = true) =
                List<Int, Model<Int>>(strand, IntProgressionModel(progression), selectionModel, fitContent)

        operator fun <T> invoke(strand: Strand, values: kotlin.collections.List<T>, selectionModel: SelectionModel<Int>? = null, fitContent: Boolean = true): List<T, Model<T>> =
                List<T, Model<T>>(strand, ListModel(values), selectionModel, fitContent)
    }
}

private class IntProgressionModel(private val progression: IntProgression): Model<Int> {
    override val size = progression.run { (last - first) / step }

    override fun get(index: Int) = progression.elementAt(index)

    override fun section(range: ClosedRange<Int>) = progression.asSequence().drop(range.start).take(range.endInclusive - range.start).toList()

    override fun contains(value: Int) = progression.contains(value)

    override fun iterator() = progression.iterator()
}

open class MutableList<T>(strand: Strand, model: MutableModel<T>, selectionModel: SelectionModel<Int>): List<T, MutableModel<T>>(strand, model, selectionModel) {
    private val modelChanged: ModelObserver<T> = { _,removed,added,_ ->
        itemsRemoved(removed)
        itemsAdded  (added  )
    }

    override var model = model
        set(new) {
            field.changed -= modelChanged
            field = new
            field.changed += modelChanged
        }

    init {
        model.changed += modelChanged
    }

    fun removeAt(index: Int): T = model.removeAt(index)

    override fun removedFromDisplay() {
        model.changed -= modelChanged

        super.removedFromDisplay()
    }

    private fun itemsAdded(values: Map<Int, T>) {
        if (selectionModel != null) {
            val updatedSelection = mutableSetOf<Int>()

            for (selectionItem in selectionModel) {
                var delta = 0

                for (index in values.keys) {
                    if (selectionItem >= index) {
                        ++delta
                    }
                }

                updatedSelection.add(selectionItem + delta)
            }

            setSelection(updatedSelection)
        }
    }

    private fun itemsRemoved(values: Map<Int, T>) {
        if (selectionModel != null) {

            val updatedSelection = mutableSetOf<Int>()

            for (selectionItem in selectionModel) {
                var delta = 0

                for (aIndex in values.keys) {
                    if (selectionItem > aIndex) {
                        delta--
                    }
                }

                if (delta > 0) {
                    updatedSelection.add(selectionItem + delta)
                } else {
                    updatedSelection.add(selectionItem)
                }
            }

            setSelection(updatedSelection)
        }
    }

//
//    public ItemEditorGenerator getItemEditorGenerator(                                          ) { return mItemEditorGenerator; }
//    public void                setItemEditorGenerator( ItemEditorGenerator aItemEditorGenerator )
//    {
//        setProperty( new AbstractNamedProperty<ItemEditorGenerator>( ITEM_EDITOR_GENERATOR )
//                     {
//                         @Override public void                setValue( ItemEditorGenerator aValue ) { mItemEditorGenerator = aValue; }
//                         @Override public ItemEditorGenerator getValue(                            ) { return mItemEditorGenerator;   }
//                     },
//                     aItemEditorGenerator );
//    }

//
//    public boolean isEditing()
//    {
//        ListUI aUI = (ListUI)getUI();
//
//        return ( aUI != null ) ? aUI.isEditing( this ) : false;
//    }
//
//    public void startEditing( int aRow )
//    {
//        ListUI aUI = (ListUI)getUI();
//
//        if( aUI != null ) { aUI.startEditing( this, aRow ); }
//    }
//
//    public boolean stopEditing()
//    {
//        ListUI aUI = (ListUI)getUI();
//
//        return ( aUI != null ) ? aUI.stopEditing( this ) : false;
//    }
//
//    public boolean cancelEditing()
//    {
//        ListUI aUI = (ListUI)getUI();
//
//        return ( aUI != null ) ? aUI.cancelEditing( this ) : false;
//    }

//    public interface ItemUIGenerator
//    {
//        Gizmo getGizmo( List    aList,
//                        Object  aObject,
//                        int     aIndex,
//                        boolean aIsSelected,
//                        boolean aHasFocus );
//    }
//
//    public interface ItemEditorGenerator extends ItemEditor
//    {
//        Gizmo getGizmo( List    aList,
//                        Object  aObject,
//                        int     aIndex,
//                        boolean aIsSelected,
//                        boolean aHasFocus );
//    }
}