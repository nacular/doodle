package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.text.TextFieldBehavior
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.drawing.Canvas

/**
 * Behavior that modifies the background and foreground of a [TextField].
 */
public interface NativeTextFieldBehaviorModifier: Behavior<TextField> {
    /**
     * Allows custom rendering for [textField]'s background
     * NOTE: implementations should most likely update [TextField.backgroundColor] to
     * ensure the results of this call are visible.
     *
     * @param textField being rendered
     * @param canvas to render onto
     */
    public fun renderBackground(textField: TextField, canvas: Canvas) {}

    /**
     * Allows custom rendering for [textField]'s foreground.
     *
     * @param textField being rendered
     * @param canvas to render onto
     */
    public fun renderForeground(textField: TextField, canvas: Canvas) {}
}

/**
 * Allows more control over how [TextField]s are styled while still applying native treatments. The given behavior
 * has the ability to render any background or foreground along with the native implementation's layer.
 */
public interface NativeTextFieldStyler {
    /**
     * Wraps [behavior] with other native styling for text fields.
     *
     * @param textField to apply [behavior] to
     * @param behavior to be "wrapped"
     * @return a new Behavior for the text field
     */
    public operator fun invoke(textField: TextField, behavior: NativeTextFieldBehaviorModifier): TextFieldBehavior
}