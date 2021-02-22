package io.nacular.doodle.controls

import io.nacular.doodle.utils.ObservableSet
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetObserver
import io.nacular.doodle.utils.SetPool

/**
 * Created by Nicholas Eddy on 3/19/18.
 */
public interface Selectable<T> {
    public fun selected       (item : T     ): Boolean
    public fun selectAll      (             )
    public fun addSelection   (items: Set<T>)
    public fun setSelection   (items: Set<T>)
    public fun removeSelection(items: Set<T>)
    public fun toggleSelection(items: Set<T>)
    public fun clearSelection (             )

    public fun next    (after : T): T?
    public fun previous(before: T): T?

    public val firstSelection : T?
    public val lastSelection  : T?
    public val selectionAnchor: T?
    public val selection      : Set<T>
}

public interface SelectionModel<T>: Iterable<T> {
    public val first  : T?
    public val last   : T?
    public val anchor : T?
    public val size   : Int
    public val isEmpty: Boolean

    public fun add        (item : T            ): Boolean
    public fun clear      (                    )
    public fun addAll     (items: Collection<T>): Boolean
    public fun remove     (item : T            ): Boolean
    public fun contains   (item : T            ): Boolean
    public fun removeAll  (items: Collection<T>): Boolean
    public fun retainAll  (items: Collection<T>): Boolean
    public fun replaceAll (items: Collection<T>): Boolean
    public fun containsAll(items: Collection<T>): Boolean
    public fun toggle     (items: Collection<T>): Boolean

    public val changed: Pool<SetObserver<SelectionModel<T>, T>>
}

public open class MultiSelectionModel<T>: SelectionModel<T> {

    private val set = LinkedHashSet<T>()
    private var anchor_: T? = null
    protected val observableSet: ObservableSet<T> by lazy { ObservableSet(set) }

    init {
        observableSet.changed += { set, removed, added ->
            if (set.size <= 1 || (anchor_ in removed && anchor_ !in added)) {
                anchor_ = null
            }

            (changed as SetPool).forEach {
                it(this, removed, added)
            }
        }
    }

    override val size  : Int get() = observableSet.size
    override val first : T?  get() = observableSet.firstOrNull()
    override val last  : T?  get() = observableSet.lastOrNull ()
    override val anchor: T?  get() = anchor_ ?: first

    override val isEmpty: Boolean get() = observableSet.isEmpty()

    override fun add        (item  : T           ): Boolean            = observableSet.add        (item )
    override fun clear      (                    ): Unit               = observableSet.clear      (     )
    override fun addAll     (items: Collection<T>): Boolean            = observableSet.addAll     (items)
    override fun remove     (item  : T           ): Boolean            { moveAnchorToDeleteSite(item); return observableSet.remove     (item ) }
    override fun iterator   (                    ): MutableIterator<T> = observableSet.iterator   (     )
    override fun contains   (item  : T           ): Boolean            = observableSet.contains   (item )
    override fun removeAll  (items: Collection<T>): Boolean            = observableSet.removeAll  (items)
    override fun retainAll  (items: Collection<T>): Boolean            = observableSet.retainAll  (items)
    override fun containsAll(items: Collection<T>): Boolean            = observableSet.containsAll(items)
    override fun replaceAll (items: Collection<T>): Boolean            = observableSet.replaceAll (items)
    override fun toggle     (items: Collection<T>): Boolean            {
        var result = false

        observableSet.batch {
            items.forEach {
                result = remove(it)
                if (!result) {
                    result = add(it)
                }
            }
        }

        return result
    }

    override val changed: Pool<SetObserver<SelectionModel<T>, T>> = SetPool()

    private fun moveAnchorToDeleteSite(item: T) {
        if (anchor_ == item) {
            anchor_ = null
        } else {
            observableSet.indexOf(item).let {
                if (it >= 0) {
                    anchor_ = set.elementAtOrNull(it + 1)
                }
            }
        }
    }
}

public class SingleItemSelectionModel<T>: MultiSelectionModel<T>() {
    override fun add(item: T): Boolean {
        var result = false

        observableSet.batch {
            clear()
            result = add(item)
        }

        return result
    }

    override fun addAll(items: Collection<T>): Boolean {
        if (observableSet.firstOrNull() in items) {
            return false
        }

        return items.lastOrNull()?.let { add(it) } ?: false
    }

    override fun replaceAll(items: Collection<T>): Boolean = items.lastOrNull()?.let { super.replaceAll(listOf(it)) } ?: false

    override fun toggle(items: Collection<T>): Boolean {
        var result = false

        observableSet.batch {
            items.forEach {
                result = remove(it)
                if (!result) {
                    clear()
                    result = add(it)
                }
            }
        }

        return result
    }
}

public class ListSelectionManager(private val selectionModel: SelectionModel<Int>?, private val numRows: () -> Int): Selectable<Int> {
    override fun selected       (item : Int     ): Boolean = selectionModel?.contains  (item ) ?: false
    override fun selectAll      (               )          { selectionModel?.addAll    ((0 until numRows()).toList()) }
    override fun addSelection   (items: Set<Int>)          { selectionModel?.addAll    (items) }
    override fun setSelection   (items: Set<Int>)          { selectionModel?.replaceAll(items) }
    override fun removeSelection(items: Set<Int>)          { selectionModel?.removeAll (items) }
    override fun toggleSelection(items: Set<Int>)          { selectionModel?.toggle    (items) }
    override fun clearSelection (               )          { selectionModel?.clear     (     ) }

    override fun next    (after : Int): Int? = (after  + 1).takeIf { it <  numRows() }
    override fun previous(before: Int): Int? = (before - 1).takeIf { it >= 0         }

    override val firstSelection : Int?     get() = selectionModel?.first
    override val lastSelection  : Int?     get() = selectionModel?.last
    override val selectionAnchor: Int?     get() = selectionModel?.anchor
    override val selection      : Set<Int> get() = selectionModel?.toSet() ?: emptySet()
}

//class TreeSelectionManager(private val selectionModel: SelectionModel<Path<Int>>?, private val pathFromRow: (Int) -> Path<Int>, private val numChildren: (Path<Int>) -> Int): Selectable<Path<Int>> {
//    override fun selected       (item : Path<Int>     ) = selectionModel?.contains  (item ) ?: false
//    override fun selectAll      (                     ) { selectionModel?.addAll((0 .. numRows).mapNotNull {
//            pathFromRow(it)
//        }.toList())
//    }
//    override fun addSelection   (items: Set<Path<Int>>) { selectionModel?.addAll    (items) }
//    override fun setSelection   (items: Set<Path<Int>>) { selectionModel?.replaceAll(items) }
//    override fun removeSelection(items: Set<Path<Int>>) { selectionModel?.removeAll (items) }
//    override fun toggleSelection(items: Set<Path<Int>>) { selectionModel?.toggle    (items) }
//    override fun clearSelection (                     ) { selectionModel?.clear     (    ) }
//
//    override fun next(after: Path<Int>) = item(after, withOffset = 1)
//
//    override fun previous(before: Path<Int>) = item(before, withOffset = -1)
//
//    override val firstSelection  get() = selectionModel?.first
//    override val lastSelection   get() = selectionModel?.last
//    override val selectionAnchor get() = selectionModel?.anchor
//    override val selection       get() = selectionModel?.toSet() ?: emptySet()
//
//    private fun item(from: Path<Int>, withOffset: Int): Path<Int>? {
//        return from.bottom?.let { index ->
//            from.parent?.let { parent ->
//                val targetIndex = index + withOffset
//                val numChildren = numChildren(parent)
//
//                when {
//                    targetIndex in 0..(numChildren - 1) -> parent + withOffset
//                    targetIndex < 0                     -> item(parent, targetIndex)
//                    else                                -> item(parent, targetIndex - numChildren)
//                }
//            }
//        }
//    }
//}