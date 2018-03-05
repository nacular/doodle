package com.nectar.doodle.focus

import com.nectar.doodle.core.Gizmo
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
    val focusOwner    : Gizmo?
    val focusCycleRoot: Gizmo?

    fun focusable(gizmo: Gizmo): Boolean

    fun requestFocus     (gizmo: Gizmo)
    fun clearFocus       (            )
    fun moveFocusForward (            )
    fun moveFocusForward (from : Gizmo)
    fun moveFocusBackward(from : Gizmo)
    fun moveFocusUpward  (from : Gizmo)
    fun moveFocusDownward(from : Gizmo)

    val focusChanged: PropertyObservers<FocusManager, Gizmo?>
}