package com.nectar.doodle.focus

import com.nectar.doodle.core.View
import com.nectar.doodle.event.FocusEvent
import com.nectar.doodle.utils.PropertyObservers

interface FocusListener {
    /**
     * Informs listener that the source has gained focus.
     *
     * @param event The event
     */
    fun focusGained(event: FocusEvent)

    /**
     * Informs listener that the source has lost focus.
     *
     * @param event The event
     */
    fun focusLost(event: FocusEvent)
}

/**
 * Created by Nicholas Eddy on 3/2/18.
 */
interface FocusManager {
    val focusOwner    : View?
    val focusCycleRoot: View?

    fun focusable(view: View): Boolean

    fun requestFocus     (view: View)
    fun clearFocus       (          )
    fun moveFocusForward (          )
    fun moveFocusForward (from: View)
    fun moveFocusBackward(from: View)
    fun moveFocusUpward  (from: View)
    fun moveFocusDownward(from: View)

    val focusChanged: PropertyObservers<FocusManager, View?>
}