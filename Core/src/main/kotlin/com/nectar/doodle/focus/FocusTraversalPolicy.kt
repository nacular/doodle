package com.nectar.doodle.focus

import com.nectar.doodle.core.Gizmo


/**
 * This interface defines the way focus moves over the children of a Gizmo.
 */

interface FocusTraversalPolicy {
    enum class TraversalType {
        Forward,
        Backward,
        Upward,
        Downward
    }

    /**
     * Returns the next item in a Gizmo based on the given Gizmo.
     *
     * @param  within The Gizmo
     * @param  from  The current Gizmo
     * @return       The next item to gain focus
     */
    fun next(within: Gizmo, from: Gizmo?): Gizmo?

    /**
     * Returns the previous item in a Gizmo based on the given Gizmo.
     *
     * @param  within The Gizmo
     * @param  from  The current Gizmo
     * @return       The previous item to gain focus
     */
    fun previous(within: Gizmo, from: Gizmo?): Gizmo?

    /**
     * Returns the last item in a Gizmo that should receive focus.
     *
     * @param  within The Gizmo
     * @return        The last item to gain focus
     */
    fun last(within: Gizmo): Gizmo?

    /**
     * Returns the first item in a Gizmo that should receive focus.
     *
     * @param  within The Gizmo
     * @return        The first item to gain focus
     */
    fun first(within: Gizmo): Gizmo?

    /**
     * Returns the item in a Gizmo that should receive focus by default.
     *
     * @param  within The Gizmo
     * @return        The item to gain focus by default
     */
    fun default(within: Gizmo): Gizmo?
}
