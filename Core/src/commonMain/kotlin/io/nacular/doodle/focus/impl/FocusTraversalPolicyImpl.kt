package io.nacular.doodle.focus.impl

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.focus.FocusTraversalPolicy
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 5/11/18.
 */
class FocusTraversalPolicyImpl(private val display: Display, private val focusManager: FocusManager): FocusTraversalPolicy {

    override fun default(within: View) = first(within)
    override fun first  (within: View) = within.children_.firstOrNull { focusManager.focusable(it) }
    override fun last   (within: View) = within.children_.lastOrNull  { focusManager.focusable(it) }

    override fun next    (within: View, from: View?) = from?.let { first(it) ?: next    (within, it.parent, it) }
    override fun previous(within: View, from: View?) = from?.let {              previous(within, it.parent, it) }

    override fun default(display: Display) = first(display)
    override fun first  (display: Display) = display.children.firstOrNull { focusManager.focusable(it) }
    override fun last   (display: Display) = display.children.lastOrNull  { focusManager.focusable(it) }

    override fun next    (display: Display, from: View?) = from?.let { first(it) ?: next    (displayView, it.parent, it) }
    override fun previous(display: Display, from: View?) = from?.let {              previous(displayView, it.parent, it) }

    private val displayView: View = object: View() {
        override val children get() = this@FocusTraversalPolicyImpl.display.children
    }

    private fun next(cycleRoot: View, parentView: View?, current: View): View? {
        val parent = parentView ?: if (current.displayed) displayView else null

        if (parent != null) {
            var i           = max(0, parent.children_.indexOfFirst { current === it })
            val numChildren = parent.children_.size

            if (parent === cycleRoot) {
                var j = i

                do {
                    j = ++j % numChildren

                    parent.children_.getOrNull(j)?.also { child ->
                        when {
                            child.isContainer && !focusManager.focusable(child) -> return first(child)
                            focusManager.focusable(child)                       -> return child
                        }
                    }
                } while (j != i)
            } else {
                while (++i < numChildren) {
                    parent.children_.getOrNull(i)?.also { child ->
                        when {
                            child.isContainer && !focusManager.focusable(child) -> return first(child)
                            focusManager.focusable(child)                       -> return child
                        }
                    }
                }
            }

            return next(cycleRoot, parent.parent, parent)
        }

        return null
    }

    private fun previous(cycleRoot: View, parentView: View?, current: View): View? {
        val parent = parentView ?: if (current.displayed) displayView else null

        if (parent != null) {
            var i = max(0, parent.children_.indexOfFirst { current === it })
            val numChildren = parent.children_.size

            if (parent === cycleRoot) {
                var j = (numChildren - i) % numChildren
                var k: Int

                do {
                    k = numChildren - j - 1

                    parent.children_.getOrNull(k)?.let {
                        var child = it

                        if (child.isContainer) {
                            val lastView = lastViewInTree(child)

                            if (lastView != null) {
                                child = lastView
                            }
                        }

                        if (focusManager.focusable(child)) {
                            return child
                        }
                    }

                    j = ++j % numChildren
                } while (k != i)
            } else {
                while (--i >= 0) {
                    parent.children_.getOrNull(i)?.let {
                        var child = it

                        if (child.isContainer) {
                            val lastView = lastViewInTree(child)

                            if (lastView != null) {
                                child = lastView
                            }
                        }

                        if (focusManager.focusable(child)) {
                            return child
                        }
                    }
                }

                return when {
                    focusManager.focusable(parent) -> parent
                    else                           -> previous(cycleRoot, parent.parent, parent)
                }
            }
        }

        return null
    }

    private fun lastViewInTree(within: View): View? {
        within.children_.reversed().forEach {
            when {
                it.children_.isNotEmpty()  -> return lastViewInTree(it)
                focusManager.focusable(it) -> return it
            }
        }

        return within
    }

    private val View.isContainer: Boolean get() = children_.isNotEmpty()
}
