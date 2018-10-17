package com.nectar.doodle.focus.impl

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.focus.FocusTraversalPolicy
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 5/11/18.
 */
class FocusTraversalPolicyImpl(private val focusManager: FocusManager): FocusTraversalPolicy {

    override fun default(within: Gizmo) = first(within)
    override fun first  (within: Gizmo) = within.children_.firstOrNull { focusManager.focusable(it) }
    override fun last   (within: Gizmo) = within.children_.lastOrNull  { focusManager.focusable(it) }

    override fun next    (within: Gizmo, from: Gizmo?) = from?.let { first(it) ?: next    (within, it.parent, it) }
    override fun previous(within: Gizmo, from: Gizmo?) = from?.let {              previous(within, it.parent, it) }

    private fun next(cycleRoot: Gizmo, parent: Gizmo?, current: Gizmo): Gizmo? {
        if (parent != null) {
            var child: Gizmo?
            var i           = max(0, parent.children_.indexOfFirst { current === it })
            val numChildren = parent.children_.size

            if (parent === cycleRoot) {
                var j = i

                do {
                    j = ++j % numChildren

                    child = parent.children_[j]

                    if (isContainer(child) && !focusManager.focusable(child)) {
                        first(child)?.let { return it }
                    }

                    if (focusManager.focusable(child)) {
                        return child
                    }
                } while (j != i)
            } else {
                while (++i < numChildren) {
                    child = parent.children_[i]

                    if (isContainer(child) && !focusManager.focusable(child)) {
                        first(child)?.let { return it }
                    }

                    if (focusManager.focusable(child)) {
                        return child
                    }
                }
            }

            return next(cycleRoot, parent.parent, parent)
        }

        return null
    }

    private fun previous(cycleRoot: Gizmo, parent: Gizmo?, current: Gizmo): Gizmo? {
        if (parent != null) {
            var i = max(0, parent.children_.indexOfFirst { current === it })
            var child: Gizmo?
            val numChildren = parent.children_.size

            if (parent === cycleRoot) {
                var j = (numChildren - i) % numChildren
                var k: Int

                do {
                    k = numChildren - j - 1

                    child = parent.children_[k]

                    if (isContainer(child)) {
                        val aLastGizmo = lastGizmoInTree(child)

                        if (aLastGizmo != null) {
                            child = aLastGizmo
                        }
                    }

                    if (focusManager.focusable(child)) {
                        return child
                    }

                    j = ++j % numChildren
                } while (k != i)
            } else {
                while (--i >= 0) {
                    child = parent.children_[i]

                    if (isContainer(child)) {
                        val aLastGizmo = lastGizmoInTree(child)

                        if (aLastGizmo != null) {
                            child = aLastGizmo
                        }
                    }

                    if (focusManager.focusable(child)) {
                        return child
                    }
                }

                return if (focusManager.focusable(parent)) {
                    parent
                } else previous(cycleRoot, parent.parent, parent)

            }
        }

        return null
    }

    private fun lastGizmoInTree(within: Gizmo): Gizmo? {
        within.children_.reversed().forEach {
            if (it.children_.isNotEmpty()) {
                return lastGizmoInTree(it)
            }

            if (focusManager.focusable(it)) {
                return it
            }
        }

        return within
    }

    private fun isContainer(gizmo: Gizmo) = gizmo.children_.isEmpty()
}
