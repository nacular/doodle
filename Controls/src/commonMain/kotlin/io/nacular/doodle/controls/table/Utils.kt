package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.table.MetaRowVisibility.Always
import io.nacular.doodle.controls.table.MetaRowVisibility.HasContents
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.then
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.layout.constraints.constrain
import kotlin.math.max

public enum class MetaRowVisibility {
    Always, HasContents, Never
}

internal class TableMetaRow(columns: List<InternalColumn<*,*,*,*>>, private val renderBlock: (Canvas) -> Unit): Container() {
    var hasContent: Boolean = false

    init {
        focusable = false
        layout    = Layout.simpleLayout { container ->
            var x          = 0.0
            var totalWidth = 0.0

            container.children.forEachIndexed { index, view ->
                view.bounds = Rectangle(Point(x, 0.0), Size(columns[index].width, container.height))

                x          += view.width
                totalWidth += view.width
            }

//                width = totalWidth + (columns.getOrNull(columns.size - 1)?.width ?: 0.0)
        }
    }

    override fun render(canvas: Canvas) {
        renderBlock(canvas)
    }

    public override fun doLayout() = super.doLayout()
}

internal class TablePanel(columns: List<InternalColumn<*,*,*,*>>, private val renderBlock: (Canvas) -> Unit): Container() {
    init {
        focusable = false
        children += columns.map { it.view }

        layout = object: Layout {
            override fun layout(container: PositionableContainer) {
                var x          = 0.0
                var height     = 0.0
                var totalWidth = 0.0

                container.children.forEachIndexed { index, view ->
                    view.bounds = Rectangle(Point(x, 0.0), Size(columns[index].width, view.minimumSize.height))

                    x          += view.width
                    height      = max(height, view.height)
                    totalWidth += view.width
                }

                container.idealSize = Size(totalWidth, height)

                container.children.forEach {
                    it.height = container.height
                }
            }
        }
    }

    override fun render(canvas: Canvas) {
        renderBlock(canvas)
    }

    public override fun doLayout() = super.doLayout()
}

internal fun <T: View> tableLayout(
    table           : T,
    header          : TableMetaRow,
    panel           : View,
    footer          : TableMetaRow,
    behavior        : AbstractTableBehavior<T>,
    headerVisibility: () -> MetaRowVisibility,
    headerSticky    : () -> Boolean,
    footerVisibility: () -> MetaRowVisibility,
    footerSticky    : () -> Boolean) = constrain(header, panel, footer) { header_, panel_, footer_ ->
    val headerHeight : Double
    val headerPadding: Double
    val headerSticky = headerSticky()
    val footerSticky = footerSticky()

    val displayRect = if ((headerSticky || footerSticky) && table.monitorsDisplayRect) table.displayRect else table.bounds.atOrigin

    behavior.headerPositioner(table).apply {
        headerHeight  = metaRowHeight(header, headerVisibility(), height)
        headerPadding = if (headerHeight > 0) insetBottom else 0.0

        header_.top    eq insetTop + if (headerSticky) displayRect.y else 0.0
        header_.width  eq parent.width
        header_.height eq headerHeight
    }

    val footerHeight : Double
    val footerPadding: Double
    behavior.footerPositioner(table).apply {
        footerHeight  = metaRowHeight(footer, footerVisibility(), height)
        footerPadding = if (footerHeight > 0) insetTop else 0.0

        footer_.width  eq parent.width
        footer_.height eq footerHeight
        (footer_.bottom eq parent.bottom - insetBottom - if (footerSticky) parent.height - displayRect.bottom else 0.0) .. Strong
    }

    panel_.top    eq header_.bottom.readOnly - if (headerSticky) displayRect.y else 0.0 + headerPadding
    panel_.left   eq 0
    panel_.right  eq parent.right

    if ((headerSticky || footerSticky) && table.monitorsDisplayRect && panel.idealSize != null) {
        panel_.height greaterEq panel.idealSize!!.height
        (panel_.height eq parent.height - (header_.height + footer_.height + headerPadding + footerPadding)) .. Strong
        (parent.height.writable eq panel_.bottom.readOnly + footer_.height.readOnly + footerPadding) .. Strong
    } else {
        panel_.bottom eq parent.bottom - (footer_.height.readOnly + footerPadding)
    }
}.then {
    table.idealSize = Size(table.idealSize?.width ?: 0.0, max(footer.bounds.bottom, panel.bounds.bottom))
}

private fun metaRowHeight(row: TableMetaRow, visibility: MetaRowVisibility, targetHeight: Double): Double = when {
    visibility == Always || (visibility == HasContents && row.hasContent) -> targetHeight
    else                                                                  -> 0.0
}