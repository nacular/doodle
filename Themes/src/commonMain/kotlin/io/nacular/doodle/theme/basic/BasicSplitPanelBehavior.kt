package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.panels.SplitPanel
import io.nacular.doodle.controls.theme.CommonSplitPanelBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Paint

/**
 * Created by Nicholas Eddy on 2/16/18.
 */
public class BasicSplitPanelBehavior(
    private val background       : Paint? = null,
    private val dividerBackground: Paint? = background
): CommonSplitPanelBehavior(divider = object: View() {
    override fun render(canvas: Canvas) {
        dividerBackground?.let {
            canvas.rect(bounds.atOrigin, fill = it)
        }
    }
}, size = 7.0, dividerVisible = dividerBackground != null) {
    override fun render(view: SplitPanel, canvas: Canvas) {
        background?.let {
            canvas.rect(view.bounds.atOrigin, fill = it)
        }
    }
}