package io.nacular.doodle.focus

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View


/**
 * This interface defines the way focus moves over the children of a [View].
 */
public interface FocusTraversalPolicy {
    public enum class TraversalType {
        Forward,
        Backward,
        Upward,
        Downward
    }

    /**
     * Returns the next item in a [View] based on the given [View].
     *
     * @param  within The View
     * @param  from  The current View
     * @return       The next item to gain focus
     */
    public fun next(within: View, from: View?): View?

    /**
     * Returns the previous item in a [View] based on the given [View].
     *
     * @param  within The View
     * @param  from  The current View
     * @return       The previous item to gain focus
     */
    public fun previous(within: View, from: View?): View?

    /**
     * Returns the last item in a [View] that should receive focus.
     *
     * @param  within The View
     * @return        The last item to gain focus
     */
    public fun last(within: View): View?

    /**
     * Returns the first item in a [View] that should receive focus.
     *
     * @param  within The View
     * @return        The first item to gain focus
     */
    public fun first(within: View): View?

    /**
     * Returns the item in a [View] that should receive focus by default.
     *
     * @param  within The View
     * @return        The item to gain focus by default
     */
    public fun default(within: View): View?

    /**
     * Returns the next item in a [View] based on the given [View].
     *
     * @param  display this Display
     * @param  from    The current View
     * @return         The next item to gain focus
     */
    public fun next(display: Display, from: View?): View?

    /**
     * Returns the previous item in a [View] based on the given [View].
     *
     * @param  display this Display
     * @param  from    The current View
     * @return         The previous item to gain focus
     */
    public fun previous(display: Display, from: View?): View?

    /**
     * Returns the last item in a [View] that should receive focus.
     *
     * @param  display this Display
     * @return         The last item to gain focus
     */
    public fun last(display: Display): View?

    /**
     * Returns the first item in a [View] that should receive focus.
     *
     * @param  display this Display
     * @return         The first item to gain focus
     */
    public fun first(display: Display): View?

    /**
     * Returns the item in a [View] that should receive focus by default.
     *
     * @param  display this Display
     * @return         The item to gain focus by default
     */
    public fun default(display: Display): View?
}
