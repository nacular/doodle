package com.nectar.doodle.controls.table

import com.nectar.doodle.controls.panels.ScrollPanel
import com.nectar.doodle.core.Box
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.AffineTransform.Companion.Identity
import com.nectar.doodle.layout.Constraints
import com.nectar.doodle.utils.ChangeObserversImpl
import com.nectar.doodle.utils.Completable
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 9/15/19.
 */
internal interface TableLikeBehavior<T: TableLike> {
    fun <B: TableLikeBehavior<T>, R> columnMoveStart(table: T, internalColumn: InternalColumn<T, B, R>)
    fun <B: TableLikeBehavior<T>, R> columnMoveEnd(table: T, internalColumn: InternalColumn<T, B, R>)
    fun <B: TableLikeBehavior<T>, R> columnMoved    (table: T, internalColumn: InternalColumn<T, B, R>)
    fun moveColumn(table: T, function: (Float) -> Unit): Completable?
}

internal interface TableLike {
    val width           : Double
    val columns         : List<Column<*>>
    var resizingCol     : Int?
    val internalColumns : MutableList<InternalColumn<*, *, *>>
    val columnSizePolicy: ColumnSizePolicy
    val header          : Box

    val panel: ScrollPanel

    fun relayout()
}

internal abstract class InternalColumn<T: TableLike, B: TableLikeBehavior<T>, R>(
        private   val table          : T,
        private   val behavior       : B?,
        override  val header         : View?,
                      headerAlignment: (Constraints.() -> Unit)? = null,
        protected val cellGenerator  : CellVisualizer<R>,
                      cellAlignment  : (Constraints.() -> Unit)? = null,
                      preferredWidth : Double? = null,
                      minWidth       : Double  = 0.0,
                      maxWidth       : Double? = null,
        private   val numFixedColumns: Int     = 0): Column<R>, ColumnSizePolicy.Column {

    override val alignmentChanged = ChangeObserversImpl(this)

    override  var headerAlignment: (Constraints.() -> Unit)? = headerAlignment
        set(value) {
            field = value
            alignmentChanged.forEach { it(this) }
        }

    override  var cellAlignment  : (Constraints.() -> Unit)? = cellAlignment
        set(value) {
            field = value
            alignmentChanged.forEach { it(this) }
        }

    override var preferredWidth = preferredWidth
        set(new) {
            field = new

            field?.let {
                table.resizingCol = index
                table.columnSizePolicy.widthChanged(table.width, table.internalColumns, index, it)
                table.relayout()
            }
        }

    override var width = preferredWidth ?: minWidth
        set(new) {
            field = max(minWidth, new).let {
                maxWidth?.let { maxWidth -> min(maxWidth, it) } ?: it
            }
        }

    override var minWidth = minWidth
        set(new) {
            field = maxWidth?.let { max(new, it) } ?: new
        }

    override var maxWidth = maxWidth
        set(new) {
            field = new?.let { min(it, minWidth) }
        }

    private val x get() = view.x

    private var index get() = table.columns.indexOf(this)
        set(new) {
            val index = this.index

            table.header.children.batch {
                if (new < size) {
                    add(new, removeAt(index))
                } else {
                    add(removeAt(index))
                }
            }

            (table.panel.content as Box).children.batch {
                if (new < size) {
                    add(new, removeAt(index))
                } else {
                    add(removeAt(index))
                }
            }

            table.internalColumns.add(new, table.internalColumns.removeAt(index))
        }

    private var transform get() = view.transform
        set(new) {
            table.header.children.getOrNull(index)?.transform = new
            view.transform                                             = new
        }

    private var zOrder get() = view.zOrder
        set(new) {
            table.header.children.getOrNull(index)?.zOrder = new
            view.zOrder                                             = new
        }

    private var animation: Completable? = null
        set(new) {
            field?.cancel()
            field = new
        }

    /** FIXME: Refactor and join w/ impl in [[Table]] and move to Behavior */
    override fun moveBy(x: Double) {
        zOrder         = 1
        val translateX = transform.translateX
        val delta      = min(max(x, 0 - (view.x + translateX)), table.width - width - (view.x + translateX))

        if (translateX == 0.0) {
            behavior?.columnMoveStart(table, this)
        }

        transform *= Identity.translate(delta)

        behavior?.columnMoved(table, this)

        table.internalColumns.dropLast(1).forEachIndexed { index, column ->
            if (column != this && index > numFixedColumns - 1) {
                val targetMiddle = column.x + column.transform.translateX + column.width / 2

                val value = when (targetMiddle) {
                    in view.x + translateX + delta            .. view.x + translateX                    ->  width
                    in view.x + translateX                    .. view.x + translateX + delta            -> -width
                    in view.bounds.right + translateX         .. view.bounds.right + translateX + delta -> -width
                    in view.bounds.right + translateX + delta .. view.bounds.right + translateX         ->  width
                    else                                                                                ->  null
                }

                value?.let {
                    val oldTransform = column.transform
                    val minViewX     = if (index > this.index) column.x - width else column.x
                    val maxViewX     = minViewX + width
                    val offset       = column.x + column.transform.translateX
                    val translate    = min(max(value, minViewX - offset), maxViewX - offset)

                    column.animation = behavior?.moveColumn(table) {
                        column.transform = oldTransform.translate(translate * it)
                    }
                }
            }
        }
    }

    override fun resetPosition() {
        behavior?.columnMoveEnd(table, this)

        zOrder           = 0
        val myOffset     = view.x + transform.translateX
        var myNewIndex   = if (myOffset >= table.internalColumns.last().view.x ) table.internalColumns.size - 2 else index
        var targetBounds = view.bounds
        val numColumns   = table.internalColumns.size

        run loop@ {
            table.internalColumns.forEachIndexed { index, column ->
                val targetMiddle = column.x + column.transform.translateX + column.width / 2

                if (index > numFixedColumns - 1 &&
                        (transform.translateX < 0 && myOffset < targetMiddle) ||
                        (transform.translateX > 0 && ((myOffset + view.width < targetMiddle) || index == numColumns - 1))) {
                    myNewIndex   = index - if (this.index < index) 1 else 0 // Since column will be removed and added to index
                    targetBounds = table.header.children[myNewIndex].bounds
                    return@loop
                }
            }
        }

        val oldTransform = transform

        animation = behavior?.moveColumn(table) {
            transform = when {
                index < myNewIndex -> oldTransform.translate((targetBounds.right - width - myOffset) * it)
                else               -> oldTransform.translate((targetBounds.x             - myOffset) * it)
            }
        }?.apply {
            completed += {
                if (index != myNewIndex) {
                    index = myNewIndex

                    // Force refresh here to avoid jitter since transform takes affect right away, while layout is deferred
                    // TODO: Can this refresh be more efficient?
                    table.header.children.forEach { it.rerenderNow() }
                    (table.panel.content as Box).children.forEach { it.rerenderNow() }
                }

                table.internalColumns.forEach { it.transform = Identity }
            }
        }
    }

    abstract val view: View

    abstract fun behavior(behavior: B?)
}