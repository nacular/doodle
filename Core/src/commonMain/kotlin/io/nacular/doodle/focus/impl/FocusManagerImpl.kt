package io.nacular.doodle.focus.impl

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.focus.FocusTraversalPolicy
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Backward
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Downward
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Forward
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Upward
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl

interface FocusabilityChecker {
    operator fun invoke(view: View): Boolean
}

class DefaultFocusabilityChecker: FocusabilityChecker {
    override fun invoke(view: View) = view.run { focusable && enabled && visible }
}

class FocusManagerImpl(
        private val display                    : Display,
        private val defaultFocusTraversalPolicy: FocusTraversalPolicy,
        private val focusabilityChecker        : FocusabilityChecker
): FocusManager {

    override var focusOwner: View? = null; private set

    override var focusCycleRoot: View? = null; private set

    override val focusChanged: PropertyObservers<FocusManager, View?> = /*by lazy {*/ PropertyObserversImpl<FocusManager, View?>(this) //}

    override fun focusable(view: View) = focusabilityChecker(view)

    override fun requestFocus(view: View) = requestFocusInternal(view)

    override fun clearFocus        (          ) = requestFocusInternal(null)
    override fun moveFocusForward  (          ) = moveFocus(null, Forward )
    override fun moveFocusForward  (from: View) = moveFocus(from, Forward )
    override fun moveFocusBackward (          ) = moveFocus(null, Backward)
    override fun moveFocusBackward (from: View) = moveFocus(from, Backward)
    override fun moveFocusUpward   (          ) = moveFocus(null, Upward  )
    override fun moveFocusUpward   (from: View) = moveFocus(from, Upward  )
    override fun moveFocusDownward (          ) = moveFocus(null, Downward)
    override fun moveFocusDownward (from: View) = moveFocus(from, Downward)
    override fun moveFocusToDefault(          ) = requestFocusInternal(getTraversalPolicy(null).default(display))

    // used to track focus owner before disabled
    private var finalFocusOwner: View? = null

    var enabled = true
        set(new) {
            if (!new) {
                // do this before updating field since it is checked in requestFocusInternal
                // and won't do the full clear.
                finalFocusOwner = focusOwner
                clearFocus()
            }

            field = new

            if (field) {
                requestFocusInternal(finalFocusOwner)
            }
        }

    private fun getTraversalPolicy(view: View?) = when (val focusCycleRoot = view?.focusCycleRoot_) {
        null -> display.focusTraversalPolicy
        else -> focusCycleRoot.focusTraversalPolicy_
    } ?: defaultFocusTraversalPolicy

    private fun moveFocus(view: View?, traversalType: TraversalType) {
        var focusView      = view ?: focusOwner
        val focusCycleRoot = focusView?.focusCycleRoot_
        val policy         = getTraversalPolicy(focusView)

        when (traversalType) {
            Forward  -> focusView = if (focusCycleRoot != null) policy.next    (focusCycleRoot, focusView) else policy.next    (display, focusView)
            Backward -> focusView = if (focusCycleRoot != null) policy.previous(focusCycleRoot, focusView) else policy.previous(display, focusView)
            Upward   -> focusCycleRoot?.let { requestFocus(focusCycleRoot) }
            Downward -> focusView?.let {
                if (it.isFocusCycleRoot_) {
                    focusView = policy.default(it)
                }
            }
        }

        requestFocusInternal(focusView)
    }

    private fun requestFocusInternal(view: View?) {
        if (focusOwner != view && (view == null || focusable(view))) {
            val oldFocusOwner = focusOwner

            if (oldFocusOwner != null) {
                if (oldFocusOwner.shouldYieldFocus()) {
                    oldFocusOwner.focusLost(view)

                    stopMonitorProperties(oldFocusOwner)
                } else {
                    return
                }
            }

            // short-circuit (and record intended view) if disabled
            if (!enabled) {
                finalFocusOwner = view
                return
            }

            focusOwner = view

            focusOwner?.let { focusOwner ->
                focusOwner.focusGained(oldFocusOwner)

                focusCycleRoot = focusOwner.focusCycleRoot_

                startMonitorProperties(focusOwner)
            }

            (focusChanged as PropertyObserversImpl<FocusManager, View?>)(oldFocusOwner, focusOwner)
        }
    }

    private fun startMonitorProperties(view: View) {
        view.enabledChanged      += focusabilityChanged
        view.visibilityChanged   += focusabilityChanged
        view.focusabilityChanged += focusabilityChanged
        view.displayChange       += displayChanged
        view.parentChange        += parentChanged
    }

    private fun stopMonitorProperties(view: View) {
        view.enabledChanged      -= focusabilityChanged
        view.visibilityChanged   -= focusabilityChanged
        view.focusabilityChanged -= focusabilityChanged
        view.displayChange       -= displayChanged
        view.parentChange        -= parentChanged
    }

    private val displayChanged: (View, Boolean, Boolean) -> Unit = { view, _, displayed ->
        // View ancestor removed from Display, since the view still has a parent.
        // The other case--the view itself is removed--is handled by the parentChanged handler
        if (!displayed && view.parent != null) {
            // move focus to default since it is hard to reconstruct the right place to go
            moveFocusToDefault()
        }
    }

    private val parentChanged: (View, View?, View?) -> Unit = { view, _, new ->
        // The view is being deleted
        if (new == null) {
            // move focus to next item
            moveFocusForward(view)
        }
    }

    private val focusabilityChanged: (View, Boolean, Boolean) -> Unit = { view, _, _ ->
        if (!focusabilityChecker(view)) {
            moveFocusForward(view)
        }
    }
}