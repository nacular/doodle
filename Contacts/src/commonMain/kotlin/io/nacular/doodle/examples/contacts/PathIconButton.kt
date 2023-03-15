package io.nacular.doodle.examples.contacts

import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.icons.PathIcon
import io.nacular.doodle.controls.theme.simpleButtonRenderer
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.path

/**
 * Button with a centered [PathIcon].
 *
 * @param pathMetrics for measuring Path
 * @param pathData to create Path
 */
class PathIconButton(pathMetrics: PathMetrics, pathData: String): PushButton(icon = PathIcon(path(pathData)!!, pathMetrics = pathMetrics)) {
    init {
        acceptsThemes = false
        behavior      = simpleButtonRenderer { button, canvas ->
            button.icon?.apply {
                val iconSize = size(button)
                render(button, canvas, at = Point((button.width - iconSize.width) / 2, (button.height - iconSize.height) / 2))
            }
        }
    }
}