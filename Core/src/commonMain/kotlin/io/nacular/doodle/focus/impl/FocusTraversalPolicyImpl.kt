package io.nacular.doodle.focus.impl

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.focus.FocusTraversalPolicy
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 5/11/18.
 */
public class FocusTraversalPolicyImpl(private val focusabilityChecker: FocusabilityChecker): FocusTraversalPolicy {

    override fun default(within: View): View? = first(within)
    override fun first  (within: View): View? = within.children_.firstOrNull { focusabilityChecker(it) }
    override fun last   (within: View): View? = within.children_.lastOrNull  { focusabilityChecker(it) }

    override fun next    (within: View, from: View?): View? = from?.takeIf { within ancestorOf_ it }?.let { first(it) ?: next    (within, it.parent, it) }
    override fun previous(within: View, from: View?): View? = from?.takeIf { within ancestorOf_ it }?.let {              previous(within, it.parent, it) }

    override fun default(display: Display): View? = first(display)
    override fun first  (display: Display): View? = display.children.firstOrNull { focusabilityChecker(it) }
    override fun last   (display: Display): View? = display.children.lastOrNull  { focusabilityChecker(it) }

    override fun next    (display: Display, from: View?): View? = from?.takeIf { display ancestorOf it }?.let { first(it) ?: next    (DisplayView(display), it.parent, it) }
    override fun previous(display: Display, from: View?): View? = from?.takeIf { display ancestorOf it }?.let {              previous(DisplayView(display), it.parent, it) }

    private class DisplayView(val display_: Display): View() {
        override val children get() = display_.children
    }

    private fun next(cycleRoot: View, parentView: View?, current: View): View? {
        val parent = when {
            parentView != null       -> parentView
            cycleRoot is DisplayView -> cycleRoot
            else                     -> current.display?.let { DisplayView(it) }
        }

        if (parent != null) {
            var i           = max(0, parent.children_.indexOfFirst { current === it })
            val numChildren = parent.children_.size

            if (parent === cycleRoot) {
                var j = i

                do {
                    j = ++j % numChildren

                    parent.children_.getOrNull(j)?.also { child ->
                        when {
                            child.isContainer && !focusabilityChecker(child) -> return first(child)
                            focusabilityChecker(child)                       -> return child
                        }
                    }
                } while (j != i)
            } else {
                while (++i < numChildren) {
                    parent.children_.getOrNull(i)?.also { child ->
                        when {
                            child.isContainer && !focusabilityChecker(child) -> return first(child)
                            focusabilityChecker(child)                       -> return child
                        }
                    }
                }
            }

            return next(cycleRoot, parent.parent, parent)
        }

        return null
    }

    private fun previous(cycleRoot: View, parentView: View?, current: View): View? {
        val parent = when {
            parentView != null       -> parentView
            cycleRoot is DisplayView -> cycleRoot
            else                     -> current.display?.let { DisplayView(it) }
        }

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

                        if (focusabilityChecker(child)) {
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

                        if (focusabilityChecker(child)) {
                            return child
                        }
                    }
                }

                return when {
                    focusabilityChecker(parent) -> parent
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
                focusabilityChecker(it) -> return it
            }
        }

        return within
    }

    private val View.isContainer: Boolean get() = children_.isNotEmpty()
}