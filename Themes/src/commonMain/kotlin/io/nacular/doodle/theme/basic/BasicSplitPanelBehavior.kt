package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.panels.SplitPanel
import io.nacular.doodle.controls.theme.CommonSplitPanelBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas

/**
 * Created by Nicholas Eddy on 2/16/18.
 */
public class BasicSplitPanelBehavior: CommonSplitPanelBehavior(divider = object: View() {}.apply { width = 7.0 }) {
    override fun render(view: SplitPanel, canvas: Canvas) {}
}