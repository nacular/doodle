package io.nacular.doodle.focus.impl

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.View
import io.nacular.doodle.focus.FocusTraversalPolicy
import io.nacular.doodle.utils.ObservableList
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 5/11/18.
 */
public class FocusTraversalPolicyImpl(private val focusabilityChecker: FocusabilityChecker): FocusTraversalPolicy {

    override fun default(within: View): View? = first         (within          )
    override fun first  (within: View): View? = firstRecursive(within.children_)
    override fun last   (within: View): View? = lastRecursive (within.children_)

    override fun next    (within: View, from: View?): View? = from?.takeIf { within ancestorOf_ it }?.let { first(it) ?: next(within, it.parent, it) }
    override fun previous(within: View, from: View?): View? = from?.takeIf { within ancestorOf_ it }?.let { previous(within, it.parent, it) }

    override fun default(display: Display): View? = first         (display         )
    override fun first  (display: Display): View? = firstRecursive(display.children)
    override fun last   (display: Display): View? = lastRecursive (display.children)

    override fun next    (display: Display, from: View?): View? = from?.takeIf { it.displayed }?.let { first(it) ?: next(DisplayView(display), it.parent, it) }
    override fun previous(display: Display, from: View?): View? = from?.takeIf { it.displayed }?.let { previous(DisplayView(display), it.parent, it) }

    private class DisplayView(val display_: Display): View() {
        override val children get() = when (display_) {
            is InternalDisplay -> ObservableList(display_.popups + display_.children)
            else               -> display_.children
        }
    }

    private fun firstRecursive(children: List<View>): View? {
        children.forEach {
            if (focusabilityChecker(it)) {
                return it
            }
            firstRecursive(it.children_)?.let {
                return it
            }
        }

        return null
    }

    private fun lastRecursive(children: List<View>): View? {
        children.asReversed().forEach {
            if (focusabilityChecker(it)) {
                return it
            }
            lastRecursive(it.children_)?.let{
                return it
            }
        }

        return null
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

            if (parent !is DisplayView) {
                return next(cycleRoot, parent.parent, parent)
            }
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
                            val lastView = lastRecursive(child.children_)

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
                            val lastView = lastRecursive(child.children_)

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
                    else                        -> previous(cycleRoot, parent.parent, parent)
                }
            }
        }

        return null
    }

    private val View.isContainer: Boolean get() = children_.isNotEmpty()
}