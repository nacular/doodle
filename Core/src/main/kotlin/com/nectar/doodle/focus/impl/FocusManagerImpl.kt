package com.nectar.doodle.focus.impl

import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.event.FocusEvent
import com.nectar.doodle.event.FocusEvent.Type.Gained
import com.nectar.doodle.event.FocusEvent.Type.Lost
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType.Backward
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType.Downward
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType.Forward
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType.Upward
import com.nectar.doodle.utils.ObservableList
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl

/**
 * Created by Nicholas Eddy on 3/2/18.
 */

class FocusManagerImpl(private val display: Display): FocusManager {

    private val ancestors = mutableListOf<Gizmo>()

    override var focusOwner: Gizmo? = null
        private set

    override var focusCycleRoot: Gizmo? = null
        private set

    override fun focusable(gizmo: Gizmo) = gizmo.run { focusable && enabled && visible }

    override fun requestFocus(gizmo: Gizmo) = requestFocusInternal(gizmo)

    override fun clearFocus() = requestFocusInternal(null)

    private fun requestFocusInternal(gizmo: Gizmo?) {
        if (focusOwner != gizmo && (gizmo == null || focusable(gizmo))) {
            val oldFocusOwner = focusOwner

            if (oldFocusOwner != null) {
                if (oldFocusOwner.shouldYieldFocus()) {
                    clearAncestorListeners()

                    oldFocusOwner.handleFocusEvent(FocusEvent(oldFocusOwner, Lost, gizmo))

                    stopMonitorProperties(oldFocusOwner)
                } else {
                    return
                }
            }

            focusOwner = gizmo

            focusOwner?.let { focusOwner ->
                focusOwner.handleFocusEvent(FocusEvent(focusOwner, Gained, oldFocusOwner))

                focusCycleRoot = focusOwner.focusCycleRoot

                startMonitorProperties(focusOwner)

                // Listen for removal of this item or any of its ancestors

                registerAncestorListeners()
            }

            (focusChanged as PropertyObserversImpl<FocusManager, Gizmo?>)(oldFocusOwner, gizmo)
        }
    }

    override fun moveFocusForward (           ) = moveFocus(null, Forward )
    override fun moveFocusForward (from: Gizmo) = moveFocus(from, Forward )
    override fun moveFocusBackward(from: Gizmo) = moveFocus(from, Backward)
    override fun moveFocusUpward  (from: Gizmo) = moveFocus(from, Upward  )
    override fun moveFocusDownward(from: Gizmo) = moveFocus(from, Downward)

    override val focusChanged: PropertyObservers<FocusManager, Gizmo?> by lazy { PropertyObserversImpl<FocusManager, Gizmo?>(this) }

    private fun moveFocus(gizmo: Gizmo?, traversalType: TraversalType) {
        var focusGizmo: Gizmo? = gizmo ?: focusOwner

        val focusCycleRoot = focusGizmo?.focusCycleRoot
        val policy         = focusCycleRoot?.focusTraversalPolicy

        if (policy != null) {
            when (traversalType) {
                Forward  -> focusGizmo = policy.next    (focusCycleRoot, focusGizmo)
                Backward -> focusGizmo = policy.previous(focusCycleRoot, focusGizmo)
                Upward   -> focusCycleRoot.let { requestFocus(it) }
                Downward ->

                    if (focusGizmo?.isFocusCycleRoot == true) {
                        focusGizmo = policy.default(focusGizmo)

                        requestFocusInternal(gizmo)
                    }
            }
        }

        requestFocusInternal(focusGizmo)
    }

    private fun registerAncestorListeners() {
        var parent = focusOwner?.parent

        while (parent != null && parent !in display) {
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

    private val childrenChanged: (ObservableList<Gizmo, Gizmo>, Map<Int, Gizmo>, Map<Int, Gizmo>, Map<Int, Pair<Int, Gizmo>>) -> Unit = { _,_,added,_ ->
        added.values.forEach {
            if (it === focusOwner || it in ancestors) {
                val owner = focusOwner

                if (owner != null) moveFocusForward(owner) else moveFocusForward()

                return@forEach
            }
        }
    }

    private fun startMonitorProperties(gizmo: Gizmo) {
        gizmo.enabledChanged    += focusabilityChanged
        gizmo.focusableChanged  += focusabilityChanged
        gizmo.visibilityChanged += focusabilityChanged
    }

    private fun stopMonitorProperties(gizmo: Gizmo) {
        gizmo.enabledChanged    -= focusabilityChanged
        gizmo.focusableChanged  -= focusabilityChanged
        gizmo.visibilityChanged -= focusabilityChanged
    }

    private val focusabilityChanged: (Gizmo, Boolean, Boolean) -> Unit = { gizmo,_,_ -> moveFocusForward(gizmo) }
}