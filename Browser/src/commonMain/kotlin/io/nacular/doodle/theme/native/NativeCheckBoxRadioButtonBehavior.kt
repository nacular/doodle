package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.ToggleButton
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.fixed
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.impl.NativeCheckBoxRadioButtonFactory
import io.nacular.doodle.drawing.impl.Type
import io.nacular.doodle.drawing.impl.Type.Check
import io.nacular.doodle.drawing.impl.Type.Radio
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.Cursor.Companion.Default

/**
 * Created by Nicholas Eddy on 4/26/19.
 */
internal abstract class CommonNativeCheckBoxRadioButtonBehavior(
    private val nativeCheckBoxRadioButtonFactory: NativeCheckBoxRadioButtonFactory,
    textMetrics                     : TextMetrics,
    focusManager                    : FocusManager?,
    private val button                          : Button,
    private val type                            : Type
): CommonTextButtonBehavior<ToggleButton>(textMetrics, focusManager = focusManager) {

    private val nativePeer by lazy { nativeCheckBoxRadioButtonFactory(button, type) }

    private lateinit var oldSize          : Size
    private          var oldCursor        : Cursor? = null
    private          var oldPreferredSize = button.preferredSize

    override fun mirrorWhenRightToLeft(view: ToggleButton) = false

    override fun render(view: ToggleButton, canvas: Canvas) {
        nativePeer.render(canvas)
    }

    override fun install(view: ToggleButton) {
        super.install(view)

        view.apply {
            oldSize          = size
            oldCursor        = cursor
            oldPreferredSize = preferredSize

            cursor        = Default
            preferredSize = fixed(nativePeer.idealSize)

            suggestSize(idealSize)
        }
    }

    override fun uninstall(view: ToggleButton) {
        super.uninstall(view)

        nativePeer.discard()

        view.apply {
            if (::oldSize.isInitialized) {
                suggestSize(oldSize)
            }
            cursor        = oldCursor
            preferredSize = oldPreferredSize
        }
    }

    override fun released(event: PointerEvent) {
        val button = event.source as Button
        val model  = button.model

        if (button.enabled && event.buttons.isEmpty()) {
            model.pressed = false
            model.armed   = false
        }
    }

    override fun pointerChanged(button: ToggleButton) {}

    override fun released(event: KeyEvent) { /* intentional no-op */ }

    override fun pressed(event: KeyEvent) { /* intentional no-op */ }
}

internal class NativeCheckBoxBehavior(
    nativeCheckBoxRadioButtonFactory: NativeCheckBoxRadioButtonFactory,
    textMetrics                     : TextMetrics,
    focusManager                    : FocusManager?,
    button                          : Button): CommonNativeCheckBoxRadioButtonBehavior(nativeCheckBoxRadioButtonFactory, textMetrics, focusManager, button, Check)


internal class NativeRadioButtonBehavior(
    nativeCheckBoxRadioButtonFactory: NativeCheckBoxRadioButtonFactory,
    textMetrics                     : TextMetrics,
    focusManager                    : FocusManager?,
    button                          : Button): CommonNativeCheckBoxRadioButtonBehavior(nativeCheckBoxRadioButtonFactory, textMetrics, focusManager, button, Radio)


internal class NativeSwitchBehavior(
    nativeCheckBoxRadioButtonFactory: NativeCheckBoxRadioButtonFactory,
    textMetrics                     : TextMetrics,
    focusManager                    : FocusManager?,
    button                          : Button): CommonNativeCheckBoxRadioButtonBehavior(nativeCheckBoxRadioButtonFactory, textMetrics, focusManager, button, Check)