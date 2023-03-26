package io.dongxi.natty.tabbedpanel

import io.nacular.doodle.controls.panels.TabbedPanel
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.theme.basic.ColorMapper
import io.nacular.doodle.theme.basic.tabbedpanel.BasicTab
import io.nacular.doodle.theme.basic.tabbedpanel.TabProducer

open class NattyTabProducer<T>(
    override val tabHeight: Double = 40.0,
    private val tabRadius: Double = 10.0,
    private val tabColor: Color = Color(0xfcfaecu),  // The color #fcfaec when hovering (from natty color tbl)
    private val selectedColorMapper: ColorMapper = { Color(0xe0bdbcu) },  // From natty color tbl.
    private val hoverColorMapper: ColorMapper = { it.darker(0.1f) }
) : TabProducer<T> {
    override val spacing: Double = -2 * tabRadius

    override fun invoke(panel: TabbedPanel<T>, item: T, index: Int): BasicTab<T> = BasicTab(
        panel,
        index,
        panel.tabVisualizer,
        tabRadius,
        tabColor,
        move,
        cancelMove,
        selectedColorMapper,
        hoverColorMapper
    ).apply { size = Size(95.0, tabHeight) } // FIXME: use dynamic width

    protected open val move: (TabbedPanel<T>, Int, Double) -> Unit = { _, _, _ -> }

    protected open val cancelMove: (TabbedPanel<T>, Int) -> Unit = { _, _ -> }
}

