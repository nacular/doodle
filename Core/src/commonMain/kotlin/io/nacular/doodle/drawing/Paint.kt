package io.nacular.doodle.drawing

/**
 * Used to fill regions when drawing shapes on a [Canvas].
 *
 * @author Nicholas Eddy
 */
public abstract class Paint {
    /**
     * `true` if the Paint is visible. This could be be false if the Paint's attributes (i.e. color) make it
     * invisible if used for rendering. Returning `false` in such a case allows the rendering system to avoid
     * unnecessary operations that won't be visible to the user.
     */
    public abstract val visible: Boolean
}