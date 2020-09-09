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
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import kotlin.math.max

class FocusManagerImpl(private val display: Display, defaultFocusTraversalPolicy: FocusTraversalPolicy? = null): FocusManager {

    private val defaultFocusTraversalPolicy = defaultFocusTraversalPolicy ?: FocusTraversalPolicyImpl(display, this)

    private val ancestors = mutableListOf<View>()

    override var focusOwner: View? = null
        private set

    override var focusCycleRoot: View? = null
        private set

    override val focusChanged: PropertyObservers<FocusManager, View?> by lazy { PropertyObserversImpl<FocusManager, View?>(this) }

    override fun focusable(view: View) = view.run { focusable && enabled && visible }

    override fun requestFocus(view: View) = requestFocusInternal(view)

    override fun clearFocus        (          ) = requestFocusInternal(null)
    override fun moveFocusForward  (          ) = moveFocus(null, Forward )
    override fun moveFocusForward  (from: View) = moveFocus(from, Forward )
    override fun moveFocusBackward (from: View) = moveFocus(from, Backward)
    override fun moveFocusUpward   (from: View) = moveFocus(from, Upward  )
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

    private fun requestFocusInternal(view: View?) {
        if (focusOwner != view && (view == null || focusable(view))) {
            val oldFocusOwner = focusOwner

            if (oldFocusOwner != null) {
                if (oldFocusOwner.shouldYieldFocus()) {
                    clearAncestorListeners()

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

                // Listen for removal of this item or any of its ancestors
                registerAncestorListeners()
            }

            (focusChanged as PropertyObserversImpl<FocusManager, View?>)(oldFocusOwner, focusOwner)
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

                    requestFocusInternal(view)
                }
            }
        }

        requestFocusInternal(focusView)
    }

    private fun registerAncestorListeners() {
        var parent = focusOwner?.parent

        while (parent != null) {
            parent.children_.changed += childrenChanged

            ancestors += parent

            parent = parent.parent
        }
    }

    private fun clearAncestorListeners() {
        for (ancestor in ancestors) {
            ancestor.children_.changed -= childrenChanged
        }

        ancestors.clear()
    }

    private fun startMonitorProperties(view: View) {
        view.enabledChanged      += focusabilityChanged
        view.visibilityChanged   += focusabilityChanged
        view.focusabilityChanged += focusabilityChanged
    }

    private fun stopMonitorProperties(view: View) {
        view.enabledChanged      -= focusabilityChanged
        view.visibilityChanged   -= focusabilityChanged
        view.focusabilityChanged -= focusabilityChanged
    }

    private val childrenChanged: (ObservableList<View>, Map<Int, View>, Map<Int, View>, Map<Int, Pair<Int, View>>) -> Unit = { list,removed,added,_ ->
        added.values.forEach {
            if (it === focusOwner || it in ancestors) {
                val owner = focusOwner

                if (owner != null) moveFocusForward(owner) else moveFocusForward()

                return@forEach
            }
        }

        removed.forEach { (index, value) ->
            if (value === focusOwner || value in ancestors) {
                val owner = focusOwner

                if (owner != null) {
                    val sibling = max(0, index - 1)

                    if (sibling < list.size) {
                        moveFocusForward(list[sibling])
                    }
                }

                return@forEach
            }
        }
    }

    private val focusabilityChanged: (View, Boolean, Boolean) -> Unit = { view, _, _ ->
        moveFocusForward(view)
    }
}