package io.nacular.doodle.drawing

import io.nacular.doodle.core.Camera
import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.utils.diff.Differences


/**
 * An internal manger that handles all render operations within an app.
 */
@Internal
public abstract class RenderManager {
    /**
     * Renders the given [View] during the next render cycle.  This has no effect
     * if the given [View] is not currently added to the [Display][io.nacular.doodle.core.Display].
     *
     * @param view
     */
    public abstract fun render(view: View)

    /**
     * Renders the given [View] immediately, without waiting until the next render cycle.  This has no effect
     * if the given [View] is not currently added to the [Display][io.nacular.doodle.core.Display].
     *
     * @param view
     */
    public abstract fun renderNow(view: View)

    /**
     * Requests layout for the given view.
     *
     * @param view to be laid out
     */
    public abstract fun layout(view: View)

    /**
     * Requests immediate layout for the given view.
     *
     * @param view to be laid out
     */
    public abstract fun layoutNow(view: View)

    /**
     * @param of view in question
     * @return the [View]'s current display rectangle (in its coordinate system) based on clipping with ancestor display rectangles.
     */
    public abstract fun displayRect(of: View): Rectangle

    /**
     * Notifies whenever a View's [bounds][View.bounds] changes
     *
     * @param view that is changing
     * @param old bounds
     * @param new bounds
     */
    internal abstract fun boundsChanged(view: View, old: Rectangle, new: Rectangle)

    /**
     * Notifies whenever a View's [z-order][View.zOrder]z-order changes
     *
     * @param view that is changing
     * @param old value
     * @param new value
     */
    internal abstract fun zOrderChanged(view: View, old: Int, new: Int)

    /**
     * Notifies whenever a View's [transform][View.transform] changes
     *
     * @param view that is changing
     * @param old value
     * @param new value
     */
    internal abstract fun transformChanged(view: View, old: AffineTransform, new: AffineTransform)

    /**
     * Notifies whenever a View's [camera][View.camera] changes
     *
     * @param view that is changing
     * @param old value
     * @param new value
     */
    internal abstract fun cameraChanged(view: View, old: Camera, new: Camera)

    /**
     * Notifies whenever a View's [opacity][View.opacity] changes
     *
     * @param view that is changing
     * @param old value
     * @param new value
     */
    internal abstract fun opacityChanged(view: View, old: Float, new: Float)

    /**
     * Notifies whenever a View's [visibility][View.visible] changes
     *
     * @param view that is changing
     * @param old value
     * @param new value
     */
    internal abstract fun visibilityChanged(view: View, old: Boolean, new: Boolean)

    /**
     * Notifies whenever a View's [children][View.children] changes
     *
     * @param view that is changing
     * @param differences representing changes
     */
    internal abstract fun childrenChanged(view: View, differences: Differences<View>)

    /**
     * Notifies whenever a View's [ideal size][View.idealSize] or [minimum size][View.minimumSize] change.
     *
     * @param view that is changing
     * @param old value
     * @param new value
     */
    internal abstract fun sizePreferencesChanged(view: View, old: View.SizePreferences, new: View.SizePreferences)

    /**
     * Notifies whenever a View's interest in [display rect events][View.monitorsDisplayRect] changes
     *
     * @param view that is changing
     * @param old value
     * @param new value
     */
    internal abstract fun displayRectHandlingChanged(view: View, old: Boolean, new: Boolean)
}
