package com.nectar.doodle.controls.list

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.ListObserver
import com.nectar.doodle.utils.SetObserver

/**
 * Created by Nicholas Eddy on 3/19/18.
 */
interface ItemUIGenerator<T> {
    operator fun invoke(list: List<T, *>, row: T, index: Int, selected: Boolean, hasFocus: Boolean): Gizmo
}

interface ItemPositioner {
    operator fun invoke(list: List<*, *>, index: Int): Rectangle
}

interface ListRenderer<T>: Renderer<List<T, *>> {
    val positioner : ItemPositioner
    val uiGenerator: ItemUIGenerator<T>
}

open class List<T, out M: Model<T>>(protected open val model: M, selectionModel: SelectionModel): Gizmo() {

    private val selectionChanged: SetObserver<SelectionModel, Int> = { _,added,removed ->

    }

    var selectionModel = selectionModel
        set(new) {
            field.onChanged -= selectionChanged
            field = new
            field.onChanged += selectionChanged
        }

    private var itemUIGenerator: ItemUIGenerator<T>? = null

    var renderer: ListRenderer<T>? = null
        set(new) {
            if (new == renderer) { return }

            field = new?.also {
                itemUIGenerator = it.uiGenerator

                children.clear()

                updateVisibleItems()
            }
        }

    init {
        selectionModel.onChanged += selectionChanged
    }

    private fun updateVisibleItems() {
        itemUIGenerator?.let {
            model.forEachIndexed { index, row ->
                children.add(it(this, row, index, false, false))
            }
        }
    }

    override fun render(canvas: Canvas) {
        renderer?.render(this, canvas)
    }

    override fun removedFromDisplay() {
        selectionModel.onChanged -= selectionChanged

        super.removedFromDisplay()
    }

    fun addSelection   (rows: Set<Int>) { selectionModel.addAll    (rows) }
    fun setSelection   (rows: Set<Int>) { selectionModel.replaceAll(rows) }
    fun removeSelection(rows: Set<Int>) { selectionModel.removeAll (rows) }
    fun clearSelection (              ) = selectionModel.clear     (    )

//    public ItemUIGenerator getItemUIGenerator(                                  ) { return mItemUIGenerator; }
//    public void            setItemUIGenerator( ItemUIGenerator aItemUIGenerator )
//    {
//        setProperty( new AbstractNamedProperty<ItemUIGenerator>( ITEM_UI_GENERATOR )
//                     {
//                         @Override public void            setValue( ItemUIGenerator aValue ) { mItemUIGenerator = aValue; }
//                         @Override public ItemUIGenerator getValue(                        ) { return mItemUIGenerator;   }
//                     },
//                     aItemUIGenerator );
//    }
}

class MutableList<T>(model: MutableModel<T>, selectionModel: SelectionModel): List<T, MutableModel<T>>(model, selectionModel) {
    private val modelChanged: ListObserver<MutableModel<T>, T> = { _,removed,added,moved ->
        itemsRemoved(removed)
        itemsAdded  (added  )
    }

    override var model = model
        set(new) {
            field.onChanged -= modelChanged
            field = new
            field.onChanged += modelChanged
        }

    init {
        model.onChanged += modelChanged
    }

    override fun removedFromDisplay() {
        model.onChanged -= modelChanged

        super.removedFromDisplay()
    }

    private fun itemsAdded(values: Map<Int, T>) {
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

    private fun itemsRemoved(values: Map<Int, T>) {
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