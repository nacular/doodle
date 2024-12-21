package io.nacular.doodle.focus.impl

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Internal
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

/** @suppress */
@Internal
public interface FocusabilityChecker {
    public operator fun invoke(view: View): Boolean
}

/** @suppress */
@Internal
public class DefaultFocusabilityChecker: FocusabilityChecker {
    override fun invoke(view: View): Boolean = view.run { focusable && enabled && visible }
}

/** @suppress */
@Internal
public class FocusManagerImpl(
        private val display                    : Display,
        private val defaultFocusTraversalPolicy: FocusTraversalPolicy,
        private val focusabilityChecker        : FocusabilityChecker
): FocusManager {

    override var focusOwner: View? = null; private set

    override var focusCycleRoot: View? = null; private set

    override val focusChanged: PropertyObservers<FocusManager, View?> = PropertyObserversImpl<FocusManager, View?>(this)

    override fun focusable(view: View): Boolean = focusabilityChecker(view)

    override fun requestFocus(view: View): Unit = requestFocusInternal(view)

    override fun clearFocus        (          ): Unit = requestFocusInternal(null)
    override fun moveFocusForward  (          ): Unit = moveFocus(null, Forward )
    override fun moveFocusForward  (from: View): Unit = moveFocus(from, Forward )
    override fun moveFocusBackward (          ): Unit = moveFocus(null, Backward)
    override fun moveFocusBackward (from: View): Unit = moveFocus(from, Backward)
    override fun moveFocusUpward   (          ): Unit = moveFocus(null, Upward  )
    override fun moveFocusUpward   (from: View): Unit = moveFocus(from, Upward  )
    override fun moveFocusDownward (          ): Unit = moveFocus(null, Downward)
    override fun moveFocusDownward (from: View): Unit = moveFocus(from, Downward)
    override fun moveFocusToDefault(          ): Unit = requestFocusInternal(getTraversalPolicy(null).default(display))

    // used to track focus owner before disabled
    private var finalFocusOwner: View? = null

    public var enabled: Boolean = true
        set(new) {
            if (field == new) {
                return
            }

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
                focusView = when {
                    it.isFocusCycleRoot_ -> policy.default(it)
                    else                 -> policy.first  (it)
                }
            }
        }

        requestFocusInternal(focusView)
    }

    private fun requestFocusInternal(view: View?) {
        when {
            focusOwner == view               -> return
            view != null && !focusable(view) -> moveFocusDownward(view)
            else -> {
                val oldFocusOwner = focusOwner

                if (oldFocusOwner != null) {
                    when {
                        oldFocusOwner.shouldYieldFocus() -> {
                            stopMonitorProperties(oldFocusOwner)
                            oldFocusOwner.focusLost(view)
                        }

                        else -> return
                    }
                }

                if (finalFocusOwner == null || !enabled) {
                    finalFocusOwner = view
                }

                // short-circuit (and record intended view) if disabled
                if (!enabled) {
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

    private val displayChanged: (View, Boolean, Boolean) -> Unit = { _,_, displayed ->
        // View ancestor removed from Display, since the view still has a parent.
        if (!displayed) {
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