package io.nacular.doodle.examples.contacts

import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.core.View
import io.nacular.doodle.core.then
import io.nacular.doodle.core.view
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.theme.native.NativeTextFieldStyler
import io.nacular.doodle.utils.Dimension.Width
import kotlin.math.max
import kotlin.math.min

/**
 * View used for Contact creation.
 *
 * @param assets containing fonts, colors, etc.
 * @param buttons containing styled buttons
 * @param contacts model
 * @param navigator for going to different parts of the app
 * @param pathMetrics to measure paths
 * @param textMetrics to measure text
 * @param textFieldStyler to apply custom behavior to TextFields
 */
class CreateContactView(
                assets         : AppConfig,
                buttons        : AppButtons,
    private val contacts       : ContactsModel,
                navigator      : Navigator,
    private val pathMetrics    : PathMetrics,
    private val textMetrics    : TextMetrics,
    private val textFieldStyler: NativeTextFieldStyler
): View() {
    private inner class DynamicAvatar(private val image: Image): Avatar(textMetrics, "") {
        override fun render(canvas: Canvas) {
            when {
                name.isBlank() -> canvas.clip(Circle(radius = min(width, height) / 2, center = Point(width / 2, height / 2))) {
                    canvas.image(image, destination = bounds.atOrigin)
                }
                else -> super.render(canvas)
            }
        }
    }

    init {
        lateinit var name       : String
        lateinit var phoneNumber: String

        val label = Label("Create Contact").apply {
            font    = assets.medium
            height  = 28.0
            fitText = setOf(Width)
        }

        val back   = buttons.back  (assets.backIcon)
        val avatar = DynamicAvatar (assets.blankAvatar).apply { size = Size(176); font = assets.medium }
        val button = buttons.create(assets.buttonBackground, assets.buttonForeground).apply {
            font     = assets.small
            enabled  = false
            fired   += {
                contacts += Contact(name, phoneNumber)
                navigator.showContactList()
            }
        }

        val spacer = view {
            height = 64.0
            render = {
                line(Point(0.0, height / 2), Point(width, height / 2), stroke = Stroke(assets.outline))
            }
        }

        val form = editForm(
            assets          = assets,
            button          = button,
            pathMetrics     = pathMetrics,
            nameChanged     = { avatar.name = it },
            textFieldStyler = textFieldStyler
        ) { name_, phone_ ->
            name           = name_
            phoneNumber    = phone_
            button.enabled = true
        }

        children += listOf(label, back, avatar, spacer, form, button)

        layout = constrain(label, back, avatar, spacer, form, button) { (label, back, avatar, spacer, form, button) ->
            label.top    eq 2 * INSET
            label.left   eq     INSET
            label.height eq label.height.readOnly

            back.top  eq label.bottom + 2 * INSET
            back.left eq label.left

            avatar.top  eq label.bottom + 3.0 / 2 * INSET
            avatar.left eq back.right + 2 * INSET

            spacer.top   eq avatar.bottom.readOnly
            spacer.left  eq back.left
            spacer.right eq parent.right - INSET

            form.top   eq spacer.bottom
            form.left  eq back.left
            form.width eq max(0.0, min(parent.width - 2 * INSET, 520.0))

            button.top  eq form.bottom + 2 * INSET
            button.left eq back.left
        }.then {
            idealSize = Size(spacer.width + 2 * INSET, button.bounds.bottom + INSET)
        }
    }

    // Helper to use constrain with 6 items
    private operator fun <T> List<T>.component6() = this[5]
}
