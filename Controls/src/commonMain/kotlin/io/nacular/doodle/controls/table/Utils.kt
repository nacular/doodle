package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.table.MetaRowVisibility.Always
import io.nacular.doodle.controls.table.MetaRowVisibility.HasContents
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.with
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
        this.layout = Layout.simpleLayout { views, _, current, _ ->
            var x          = 0.0
            var totalWidth = 0.0

            views.forEachIndexed { index, view ->
                val size = Size(columns[index].width, current.height)

                view.updateBounds(x, 0.0, size, size).let {
                    x          += it.width
                    totalWidth += it.width
                }
            }

            Size(totalWidth, current.height)
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

        this.layout = object: Layout {
            override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size): Size {
                var x          = 0.0
                var height     = 0.0
                var totalWidth = 0.0

                views.forEachIndexed { index, view ->
                    // FIXME Min height handling?
                    val size = Size(columns[index].width, 0.0) //view.minimumSize.height)

                    view.updateBounds(x, 0.0, size, size).also {
                        x          += it.width
                        height      = max(height, it.height)
                        totalWidth += it.width
                    }
                }

                val idealSize = Size(totalWidth, height)

                views.forEach {
                    it.updateBounds(it.bounds.with(height = idealSize.height))
                }

                return idealSize
            }
        }
    }

    override fun render(canvas: Canvas) {
        renderBlock(canvas)
    }

    public override fun doLayout() = super.doLayout()
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
    val headerHeight : Double
    val headerPadding: Double
    val isHeaderSticky = headerSticky()
    val isFooterSticky = footerSticky()

    val displayRect = if ((isHeaderSticky || isFooterSticky) && table.monitorsDisplayRect) table.displayRect else table.bounds.atOrigin

    behavior.headerPositioner(table).apply {
        headerHeight  = metaRowHeight(header, headerVisibility(), height)
        headerPadding = if (headerHeight > 0) insetBottom else 0.0

        header_.top    eq insetTop + if (isHeaderSticky) displayRect.y else 0.0
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
        // FIXME: Remove readOnly
        (footer_.bottom eq parent.bottom - insetBottom - if (isFooterSticky) parent.height.readOnly - displayRect.bottom else 0.0) .. Strong
    }

    panel_.top    eq header_.bottom.readOnly - if (isHeaderSticky) displayRect.y else 0.0 + headerPadding
    panel_.left   eq 0
    panel_.right  eq parent.right

    if ((isHeaderSticky || isFooterSticky) && table.monitorsDisplayRect) {
        panel_.height greaterEq panel.idealSize.height
        (panel_.height eq parent.height - (header_.height + footer_.height + headerPadding + footerPadding)) .. Strong
        (parent.height eq panel_.bottom.readOnly + footer_.height.readOnly + footerPadding) .. Strong
    } else {
        panel_.bottom eq parent.bottom - (footer_.height.readOnly + footerPadding)
    }
}

private fun metaRowHeight(row: TableMetaRow, visibility: MetaRowVisibility, targetHeight: Double): Double = when {
    visibility == Always || (visibility == HasContents && row.hasContent) -> targetHeight
    else                                                                  -> 0.0
}