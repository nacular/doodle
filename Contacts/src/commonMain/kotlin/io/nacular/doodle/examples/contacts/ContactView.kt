package io.nacular.doodle.examples.contacts

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.controls.icons.PathIcon
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.core.container
import io.nacular.doodle.core.then
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.path
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.text.invoke
import io.nacular.doodle.theme.native.NativeHyperLinkStyler
import io.nacular.doodle.utils.Dimension.Width
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlin.math.max

/**
 * Displays a Contact
 */
class ContactView(
    modals      : Modals,
    assets      : AppConfig,
    contact     : Contact,
    contacts    : ContactsModel,
    buttons     : AppButtons,
    appScope    : CoroutineScope,
    navigator   : Navigator,
    linkStyler  : NativeHyperLinkStyler,
    textMetrics : TextMetrics,
    pathMetrics : PathMetrics,
    uiDispatcher: CoroutineDispatcher
): ContactCommon(
    assets       = assets,
    modals       = modals,
    contact      = contact,
    buttons      = buttons,
    contacts     = contacts,
    appScope     = appScope,
    navigator    = navigator,
    textMetrics  = textMetrics,
    uiDispatcher = uiDispatcher
) {
    init {
        edit.apply {
            fired += {
                // Show Contact edit when pressed
                navigator.showContactEdit(super.contact)
            }
        }

        val details = container {
            this += Label("Contact Details").apply {
                font    = assets.small
                height  = 24.0
                fitText = setOf(Width)
            }
            this += HyperLink(
                url  = "tel:${contact.phoneNumber}",
                text = contact.phoneNumber,
                icon = PathIcon(path = path(assets.phoneIcon)!!, pathMetrics = pathMetrics, fill = assets.phoneNumber),
            ).apply {
                font            = assets.small
                acceptsThemes   = false
                iconTextSpacing = INSET
                behavior        = linkStyler(this, object: CommonTextButtonBehavior<HyperLink>(textMetrics) {
                    override fun install(view: HyperLink) {
                        super.install(view)
                        val textSize = textMetrics.size(text, font)
                        val iconSize = icon!!.size(view)

                        // Ensure link's size includes icon and text
                        size = Size(textPosition(view).x + textSize.width, max(iconSize.height, textSize.height))
                    }

                    override fun render(view: HyperLink, canvas: Canvas) {
                        icon!!.render(view, canvas, at = iconPosition(view, icon = icon!!))

                        // Styled text with phoneNumberLink color and link's font
                        canvas.text(assets.phoneNumberLink.invoke { view.font(view.text) }, at = textPosition(view))
                    }
                }) as Behavior<Button>
            }

            render = {
                rect(bounds.atOrigin.inset(0.5), radius = 12.0, stroke = Stroke(assets.outline))
            }

            layout = constrain(children[0], children[1]) { label, link ->
                label.top  eq INSET
                label.left eq INSET
                link.top   eq label.bottom + INSET
                link.left  eq label.left
            }
        }

        setDetail(details)

        layout = simpleLayout { layoutCommonItems() }.then {
            idealSize = Size(spacer.width + 2 * INSET, details.bounds.bottom + INSET)
        }
    }
}