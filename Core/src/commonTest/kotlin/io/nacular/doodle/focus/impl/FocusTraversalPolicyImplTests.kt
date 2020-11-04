package io.nacular.doodle.focus.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.ObservableList
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 9/17/20.
 */
class FocusTraversalPolicyImplTests {
    @Test @JsName("defaultContainerWorks")
    fun `default in container works`() {
        FocusTraversalPolicyImpl(alwaysFocusable).apply {
            container(0).also { expect(null      ) { default(within = it) } }
            container(5).also { expect(it.first()) { default(within = it) } }
        }
        FocusTraversalPolicyImpl(neverFocusable).apply {
            container(5).also { expect(null) { default(within = it) } }
        }
    }

    @Test @JsName("firstContainerWorks")
    fun `first in container works`() {
        FocusTraversalPolicyImpl(alwaysFocusable).apply {
            container(0).also { expect(null      ) { first(within = it) } }
            container(5).also { expect(it.first()) { first(within = it) } }
        }
        FocusTraversalPolicyImpl(neverFocusable).apply {
            container(5).also { expect(null) { first(within = it) } }
        }
    }

    @Test @JsName("lastContainerWorks")
    fun `last in container works`() {
        FocusTraversalPolicyImpl(alwaysFocusable).apply {
            container(0).also { expect(null     ) { last(within = it) } }
            container(5).also { expect(it.last()) { last(within = it) } }
        }
        FocusTraversalPolicyImpl(neverFocusable).apply {
            container(5).also { expect(null) { last(within = it) } }
        }
    }

    @Test @JsName("nextContainerNotAncestorNoOps")
    fun `next in container not ancestor no-ops`() {
        FocusTraversalPolicyImpl(alwaysFocusable).apply {
            container(0).also { expect(null) { next(within = it, from = mockk()) } }
            container(5).also { expect(null) { next(within = it, from = mockk()) } }
        }
    }

    @Test @JsName("nextSimpleContainerWorks")
    fun `next in simple container works`() {
        container(5).also { container ->
            listOf(
                    0 to 1,
                    1 to 2,
                    2 to 4, // 3 is not focusable, see below
                    4 to 0
            ).forEach { (from, to) ->
                val view = slot<View>()
                val focusabilityChecker = mockk<FocusabilityChecker>().apply {
                    every { this@apply(capture(view)) } answers  { view.captured != container.children[3] }

                }
                FocusTraversalPolicyImpl(focusabilityChecker).apply {
                    expect(container.children[to], "$from -> $to") {
                        next(within = container, from = container.children[from])
                    }
                }
            }
        }
    }

    @Test @JsName("previousSimpleContainerWorks")
    fun `previous in simple container works`() {
        container(5).also { container ->
            listOf(
                    0 to 4,
                    1 to 0,
                    2 to 1,
                    4 to 2 // 3 is not focusable, see below
            ).forEach { (from, to) ->
                val view = slot<View>()
                val focusabilityChecker = mockk<FocusabilityChecker>().apply {
                    every { this@apply(capture(view)) } answers  { view.captured != container.children[3] }
                }

                FocusTraversalPolicyImpl(focusabilityChecker).apply {
                    expect(container.children[to], "$from -> $to") { previous(within = container, from = container.children[from]) }
                }
            }
        }
    }

    // =====

    @Test @JsName("defaultDisplayWorks")
    fun `default in display works`() {
        FocusTraversalPolicyImpl(alwaysFocusable).apply {
            display(0).also { expect(null      ) { default(display = it) } }
            display(5).also { expect(it.first()) { default(display = it) } }
        }
    }

    @Test @JsName("firstDisplayWorks")
    fun `first in display works`() {
        FocusTraversalPolicyImpl(alwaysFocusable).apply {
            display(0).also { expect(null      ) { first(display = it) } }
            display(5).also { expect(it.first()) { first(display = it) } }
        }
    }

    @Test @JsName("lastDisplayWorks")
    fun `last in display works`() {
        FocusTraversalPolicyImpl(alwaysFocusable).apply {
            display(0).also { expect(null     ) { last(display = it) } }
            display(5).also { expect(it.last()) { last(display = it) } }
        }
    }

    @Test @JsName("nextSimpleDisplayWorks")
    fun `next in simple display works`() {
        display(5).also { display ->
            listOf(
                0 to 1,
                1 to 2,
                2 to 4, // 3 is not focusable, see below
                4 to 0
            ).forEach { (from, to) ->
                val view = slot<View>()
                val focusabilityChecker = mockk<FocusabilityChecker>().apply {
                    every { this@apply(capture(view)) } answers  { view.captured != display.children[3] }
                }

                FocusTraversalPolicyImpl(focusabilityChecker).apply {
                    expect(display.children[to], "$from -> $to") { next(display = display, from = display.children[from]) }
                }
            }
        }
    }

    @Test @JsName("previousSimpleDisplayWorks")
    fun `previous in simple display works`() {
        display(5).also { display ->
            listOf(
                0 to 4,
                1 to 0,
                2 to 1,
                4 to 2 // 3 is not focusable, see below
            ).forEach { (from, to) ->
                val view = slot<View>()
                val focusabilityChecker = mockk<FocusabilityChecker>().apply {
                    every { this@apply(capture(view)) } answers  { view.captured != display.children[3] }
                }

                FocusTraversalPolicyImpl(focusabilityChecker).apply {
                    expect(display.children[to], "$from -> $to") { previous(display = display, from = display.children[from]) }
                }
            }
        }
    }

    private val alwaysFocusable = mockk<FocusabilityChecker>().apply {
        every { this@apply(any()) } returns true
    }
    private val neverFocusable  = mockk<FocusabilityChecker>().apply {
        every { this@apply(any()) } returns false
    }

    private fun display(numChildren: Int = 5) = mockk<Display>().apply {
        val children = ObservableList<View>()

        repeat(numChildren) {
            children += view(display = this)
        }

        val view = slot<View>()

        every { this@apply.ancestorOf(capture(view)) } answers { view.captured in children }
        every { this@apply.children                  } returns children
        every { this@apply.iterator()                } returns children.iterator()
    }

    private fun container(numChildren: Int = 5) = spyk(Container()).apply {
        val display = mockk<Display>()

        repeat(numChildren) {
            children += view(display = display, parent = this).also {
                // need to mock this since it.parent will be null when View.ancestor is call
                // because of: https://github.com/mockk/mockk/issues/104
                every { ancestorOf_(it) } returns true
            }
        }
    }

    private fun view(display: Display, parent: View? = null) = mockk<View>().apply {
        every { display.ancestorOf(this@apply) } returns true
        every { this@apply.display             } returns display
        every { this@apply.parent              } returns parent
        every { this@apply.children_           } returns ObservableList()
    }
}
