package io.nacular.doodle.drawing

/**
 * Brushes are used to fill regions when drawing shapes on a [Canvas].
 *
 * @author Nicholas Eddy
 */
abstract class Brush internal constructor() {
    /**
     * `true` if the brush is visible.  This could be be false if the Brush's attributes (i.e. color) make it
     * invisible if used for rendering.  Returning `false` in such a case allows the rendering system to avoid
     * unnecessary operations that won't be visible to the user.
     */
    abstract val visible: Boolean
}