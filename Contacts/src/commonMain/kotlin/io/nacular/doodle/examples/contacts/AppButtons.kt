package io.nacular.doodle.examples.contacts

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.theme.simpleTextButtonRenderer
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.grayScale
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.system.Cursor.Companion.Pointer

interface AppButtons {
    fun back  (path: String): Button
    fun edit  (background: Color, foreground: Color): Button
    fun delete(background: Color, foreground: Color): Button
    fun create(background: Color, foreground: Color): Button
}

class AppButtonsImpl(
    private val navigator  : Navigator,
    private val textMetrics: TextMetrics,
    private val pathMetrics: PathMetrics
): AppButtons {
    override fun back(path: String): Button {
        return PathIconButton(
            pathData    = path,
            pathMetrics = pathMetrics
        ).apply {
            size            = Size(28)
            cursor          = Pointer
            foregroundColor = Color.Black
            fired += {
                navigator.goBack()
            }
        }
    }

    override fun edit  (background: Color, foreground: Color) = simpleButton("Edit",   background, text = foreground)
    override fun create(background: Color, foreground: Color) = simpleButton("Create", background, text = foreground)
    override fun delete(background: Color, foreground: Color) = simpleButton("Delete", background, text = foreground)

    private fun simpleButton(name: String, background: Color, text: Color) = PushButton(name).apply {
        size          = Size(113, 40)
        cursor        = Pointer
        acceptsThemes = false
        behavior      = simpleTextButtonRenderer(textMetrics) { button, canvas ->
            val color = background.let { if (enabled) it else it.grayScale() }

            canvas.rect(bounds.atOrigin, radius = 4.0, fill = color.paint)
            canvas.text(button.text, at = textPosition(button, button.text), fill = text.paint, font = font)
        }
    }
}