package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.table.MetaRowVisibility.Always
import io.nacular.doodle.controls.table.MetaRowVisibility.HasContents
import io.nacular.doodle.controls.table.MetaRowVisibility.Never
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.utils.observable
import kotlin.math.max

/**
 * Indicate when a meta row in a [Table] (i.e. header, footer, etc.) should be visible.
 *
 * @property Always Row is always visible
 * @property HasContents Only visible if it has contents
 * @property Never Row is never visible
 */
public enum class MetaRowVisibility {
    /** Row is always visible */
    Always,

    /** Only visible if it has contents */
    HasContents,

    /** Row is never visible */
    Never
}

internal class TableMetaRow(columns: List<InternalColumn<*,*,*,*>>, private val renderBlock: (Canvas) -> Unit): Container() {
    var hasContent   = false
    var scrollOffset by observable(0.0) { _,_ -> relayout() }

    init {
        focusable = false
        layout = simpleLayout { views,_,current,_,_ ->
            var x = scrollOffset

            views.forEachIndexed { index, view ->
                val size = Size(columns[index].width, current.height)

                view.updateBounds(x, 0.0, size, size).let {
                    x += it.width
                }
            }

            Size(x, current.height)
        }
    }

    override fun render(canvas: Canvas) {
        renderBlock(canvas)
    }
}

internal class TablePanel(
    columns: List<InternalColumn<*, *, *, *>>,
    private val renderBlock: (Canvas) -> Unit
): Container() {
    init {
        focusable  = false
        children  += columns.map { it.view }
        layout     = simpleLayout { views,_,current,_,_ ->
            var x          = 0.0
            var height     = max(current.height, views.first().idealSize.height)
            var totalWidth = 0.0

            views.forEachIndexed { index, view ->
                val col   = columns[index]
                val width = col.width

                view.updateBounds(x, 0.0, Size(width, 0.0), Size(width, height)).also {
                    x          += it.width
                    totalWidth += it.width
                }
            }

            preferredSize = fixed(Size(totalWidth, height))

            idealSize
        }
    }

    override fun render(canvas: Canvas) {
        renderBlock(canvas)
    }
}

@Suppress("LocalVariableName")
internal fun <T: View> tableLayout(
    table           : T,
    header          : TableMetaRow,
    panel           : View,
    footer          : TableMetaRow,
    behavior        : AbstractTableBehavior<T>,
    headerVisibility: () -> MetaRowVisibility,
    headerSticky    : () -> Boolean,
    footerVisibility: () -> MetaRowVisibility,
    footerSticky    : () -> Boolean
) = constrain(header, panel, footer) { header_, panel_, footer_ ->
    val headerHeight   : Double
    val headerPadding  : Double
    val footerPadding  : Double
    val isHeaderSticky = headerSticky()
    val isFooterSticky = footerSticky()

    val displayRect = if ((isHeaderSticky || isFooterSticky) && table.monitorsDisplayRect) table.displayRect else table.prospectiveBounds.atOrigin

    behavior.headerPositioner(table).apply {
        headerHeight  = metaRowHeight(header, headerVisibility(), height)
        headerPadding = if (headerHeight > 0) insetBottom else 0.0

        header_.top    eq insetTop + if (isHeaderSticky) displayRect.y else 0.0
        header_.width  eq parent.width
        header_.height eq headerHeight
    }

    behavior.footerPositioner(table).apply {
        val footerHeight = metaRowHeight(footer, footerVisibility(), height)
        footerPadding    = if (footerHeight > 0) insetTop else 0.0

        footer_.width  eq parent.width
        footer_.height eq footerHeight

        when {
            isFooterSticky -> footer_.bottom eq parent.bottom - insetBottom - parent.height + displayRect.bottom strength Strong
            else           -> footer_.bottom eq parent.bottom - insetBottom                                      strength Strong
        }
    }

    panel_.top   eq header_.bottom - if (isHeaderSticky) displayRect.y else 0.0 + headerPadding
    panel_.left  eq 0
    panel_.right eq parent.right

    when {
        (isHeaderSticky || isFooterSticky) && table.monitorsDisplayRect -> {
            panel_.height greaterEq panel_.preferredHeight
            panel_.height eq        parent.height - (header_.height + footer_.height + headerPadding + footerPadding) strength Strong
            parent.height eq        panel_.bottom + footer_.height + footerPadding                                    strength Strong
        }
        else -> panel_.bottom eq parent.bottom - (footer_.height + footerPadding)
    }
}

internal fun metaRowHeight(row: TableMetaRow, visibility: MetaRowVisibility, targetHeight: Double): Double = when {
    visibility == Always || (visibility == HasContents && row.hasContent) -> targetHeight
    else                                                                  -> 0.0
}