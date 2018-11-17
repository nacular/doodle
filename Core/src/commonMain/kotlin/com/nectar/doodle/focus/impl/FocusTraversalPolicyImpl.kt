package com.nectar.doodle.focus.impl

import com.nectar.doodle.core.View
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.focus.FocusTraversalPolicy
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 5/11/18.
 */
class FocusTraversalPolicyImpl(private val focusManager: FocusManager): FocusTraversalPolicy {
    override fun default(within: View) = first(within)
    override fun first  (within: View) = within.children_.firstOrNull { focusManager.focusable(it) }
    override fun last   (within: View) = within.children_.lastOrNull  { focusManager.focusable(it) }

    override fun next    (within: View, from: View?) = from?.let { first(it) ?: next    (within, it.parent, it) }
    override fun previous(within: View, from: View?) = from?.let {              previous(within, it.parent, it) }

    private fun next(cycleRoot: View, parent: View?, current: View): View? {
        if (parent != null) {
            var child: View?
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

    private fun previous(cycleRoot: View, parent: View?, current: View): View? {
        if (parent != null) {
            var i = max(0, parent.children_.indexOfFirst { current === it })
            var child: View?
            val numChildren = parent.children_.size

            if (parent === cycleRoot) {
                var j = (numChildren - i) % numChildren
                var k: Int

                do {
                    k = numChildren - j - 1

                    child = parent.children_[k]

                    if (isContainer(child)) {
                        val lastView = lastViewInTree(child)

                        if (lastView != null) {
                            child = lastView
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
                        val lastView = lastViewInTree(child)

                        if (lastView != null) {
                            child = lastView
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

    private fun lastViewInTree(within: View): View? {
        within.children_.reversed().forEach {
            if (it.children_.isNotEmpty()) {
                return lastViewInTree(it)
            }

            if (focusManager.focusable(it)) {
                return it
            }
        }

        return within
    }

    private fun isContainer(view: View) = view.children_.isEmpty()
}
