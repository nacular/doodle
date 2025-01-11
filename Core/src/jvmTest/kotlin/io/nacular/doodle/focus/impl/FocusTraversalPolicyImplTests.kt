package io.nacular.doodle.focus.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.Path
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 9/17/20.
 */

private class TreeNode<T>(val value: T,
        val children        : List<TreeNode<T>> = emptyList(),
        val focusable       : Boolean,
        val isFocusCycleRoot: Boolean?) {
    operator fun get(index: Int): TreeNode<T> = children[index]
}

private class NodeBuilder<T>(var value: T, val children: MutableList<NodeBuilder<T>> = mutableListOf()) {
    var focusable:        Boolean  = true
    var isFocusCycleRoot: Boolean? = null

    fun build(): TreeNode<T> = TreeNode(value, children.map { it.build() }, focusable, isFocusCycleRoot)

    fun child(value: T, block: NodeBuilder<T>.() -> Unit = {}): NodeBuilder<T> {
        val node = NodeBuilder(value).also { block(it) }

        children.add(node)

        return node
    }
}

private fun <T> rootNode(value: T, block: NodeBuilder<T>.() -> Unit = {}): TreeNode<T> = NodeBuilder(value).also { block(it) }.build()

class FocusTraversalPolicyImplTests {
    @Test fun `default in container works`() {
        FocusTraversalPolicyImpl(alwaysFocusable).apply {
            container(0).also { expect(null      ) { default(within = it) } }
            container(5).also { expect(it.first()) { default(within = it) } }
        }
        FocusTraversalPolicyImpl(neverFocusable).apply {
            container(5).also { expect(null) { default(within = it) } }
        }
    }

    @Test fun `first in container works`() {
        FocusTraversalPolicyImpl(alwaysFocusable).apply {
            container(0).also { expect(null      ) { first(within = it) } }
            container(5).also { expect(it.first()) { first(within = it) } }
        }
        FocusTraversalPolicyImpl(neverFocusable).apply {
            container(5).also { expect(null) { first(within = it) } }
        }
    }

    @Test fun `last in container works`() {
        FocusTraversalPolicyImpl(alwaysFocusable).apply {
            container(0).also { expect(null     ) { last(within = it) } }
            container(5).also { expect(it.last()) { last(within = it) } }
        }
        FocusTraversalPolicyImpl(neverFocusable).apply {
            container(5).also { expect(null) { last(within = it) } }
        }
    }

    @Test fun `next in container not ancestor no-ops`() {
        FocusTraversalPolicyImpl(alwaysFocusable).apply {
            container(0).also { expect(null) { next(within = it, from = mockk()) } }
            container(5).also { expect(null) { next(within = it, from = mockk()) } }
        }
    }

    @Test fun `next in simple container works`() {
        val root = rootNode("container") {
            child("0")
            child("1")
            child("2")
            child("3") { focusable = false }
            child("4")
        }

        validate(root) {
            val container = Path("container")

            listOf(
                    0 to 1,
                    1 to 2,
                    2 to 4, // 3 is not focusable, see above
                    4 to 0
            ).forEach {
                val from = container + "${it.first}"
                val to   = container + "${it.second}"
                expect(to, "next($container, $from)") { next(container, from) }
            }
        }
    }

    @Test fun `next goes to first in child container`() {
        val root = rootNode("container") {
            child("0")
            child("1") {
                child("0")
                child("1")
            }
        }

        validate(root) {
            val container = Path("container")
            val from      = container + "1"
            val to        = container + "1" + "0"
            expect(to, "next($container, $from)") { next(container, from) }
        }
    }

    @Test fun `next goes from last in container to first in parent`() {
        val root = rootNode("container") {
            child("0")
            child("1") {
                isFocusCycleRoot = false
                child("0")
                child("1")
            }
        }

        validate(root) {
            val parent    = Path("container")
            val container = parent    + "1"
            val from      = container + "1"
            val to        = parent    + "0"
            expect(to, "next($parent, $from)") { next(parent, from) }
        }
    }

    @Test fun `previous in simple container works`() {
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

    @Test fun `previous goes to previous sibling from nested container`() {
        val root = rootNode("container") {
            child("0")
            child("1") {
                focusable = false
                child("0")
                child("1")
            }
        }

        validate(root) {
            val container = Path("container")
            val from      = container + "1" + "0"
            val to        = container + "0"
            expect(to, "previous($container, $from)") { previous(container, from) }
        }
    }

    @Test fun `previous goes to last in sibling from nested container`() {
        val root = rootNode("container") {
            child("0") {
                child("0")
                child("1")
            }
            child("1") {
                focusable = false
                child("0")
                child("1")
            }
        }

        validate(root) {
            val container = Path("container")
            val from      = container + "1" + "0"
            val to        = container + "0" + "1"
            expect(to, "previous($container, $from)") { previous(container, from) }
        }
    }

    // =====

    @Test fun `default in display works`() {
        FocusTraversalPolicyImpl(alwaysFocusable).apply {
            display(0).also { expect(null      ) { default(display = it) } }
            display(5).also { expect(it.first()) { default(display = it) } }
        }
    }

    @Test fun `first in display works`() {
        FocusTraversalPolicyImpl(alwaysFocusable).apply {
            display(0).also { expect(null      ) { first(display = it) } }
            display(5).also { expect(it.first()) { first(display = it) } }
        }
    }

    @Test fun `last in display works`() {
        FocusTraversalPolicyImpl(alwaysFocusable).apply {
            display(0).also { expect(null     ) { last(display = it) } }
            display(5).also { expect(it.last()) { last(display = it) } }
        }
    }

    @Test fun `next in simple display works`() {
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

    @Test fun `previous in simple display works`() {
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

    private interface ValidationContext<T> {
        fun next(within: Path<T>, from: Path<T>): Path<T>?
        fun previous(within: Path<T>, from: Path<T>): Path<T>?
    }

    private class ContainerValidationContext<T>(root: TreeNode<T>): ValidationContext<T> {
        private val pathToView = mutableMapOf<Path<T>, View>()
        private val viewToPath = mutableMapOf<View, Path<T>>()

        private val focusabilityChecker = mockk<FocusabilityChecker>().apply {
            val view = slot<View>()
            every { this@apply(capture(view)) } answers {
                view.captured.focusable
            }
        }

        private val policy = FocusTraversalPolicyImpl(focusabilityChecker)

        init {
            createHierarchy(root, Path(root.value))
        }

        override fun next    (within: Path<T>, from: Path<T>): Path<T>? = viewToPath[policy.next(pathToView[within]!!, pathToView[from])]
        override fun previous(within: Path<T>, from: Path<T>): Path<T>? = viewToPath[policy.previous(pathToView[within]!!, pathToView[from])]

        private fun createHierarchy(node: TreeNode<T>, path: Path<T>): Container {
            return io.nacular.doodle.core.container {
                focusable = node.focusable
                node.isFocusCycleRoot?.let { isFocusCycleRoot = it }
                children += node.children.map { createHierarchy(it, path + it.value) }
                children.forEach {
                    it.addedToDisplay_(mockk(), mockk(), mockk())
                }
            }.also {
                pathToView[path] = it
                viewToPath[it  ] = path
            }
        }
    }

    private class DisplayValidationContext<T>(nodes: List<TreeNode<T>>): ValidationContext<T> {
        private val pathToView = mutableMapOf<Path<T>, View>()
        private val viewToPath = mutableMapOf<View, Path<T>>()

        private val focusabilityChecker = mockk<FocusabilityChecker>().apply {
            val view = slot<View>()
            every { this@apply(capture(view)) } answers {
                view.captured.focusable
            }
        }

        private val policy = FocusTraversalPolicyImpl(focusabilityChecker)

        init {
//            createHierarchy(nodes)
        }

        override fun next    (within: Path<T>, from: Path<T>): Path<T>? = viewToPath[policy.next    (pathToView[within]!!, pathToView[from])]
        override fun previous(within: Path<T>, from: Path<T>): Path<T>? = viewToPath[policy.previous(pathToView[within]!!, pathToView[from])]

        private fun createHierarchy(node: TreeNode<T>, path: Path<T>): Container {
            return io.nacular.doodle.core.container {
                focusable = node.focusable
                node.isFocusCycleRoot?.let { isFocusCycleRoot = it }
                children += node.children.map { createHierarchy(it, path + it.value) }
            }.also {
                pathToView[path] = it
                viewToPath[it  ] = path
            }
        }
    }

    private fun <T> validate(root: TreeNode<T>, validation: ValidationContext<T>.() -> Unit) {
        validation(ContainerValidationContext(root))
    }

    private val alwaysFocusable = mockk<FocusabilityChecker>().apply {
        every { this@apply(any()) } returns true
    }
    private val neverFocusable  = mockk<FocusabilityChecker>().apply {
        every { this@apply(any()) } returns false
    }

    private fun display(numChildren: Int = 5) = mockk<InternalDisplay>().apply {
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
        val display = mockk<InternalDisplay>()

        repeat(numChildren) {
            children += view(display = display, parent = this).also {
                // need to mock this since it.parent will be null when View.ancestor is call
                // because of: https://github.com/mockk/mockk/issues/104
                every { ancestorOf_(it) } returns true
            }
        }
    }

    private fun view(display: InternalDisplay, parent: View? = null) = mockk<View>().apply {
        every { display.ancestorOf(this@apply) } returns true
        every { this@apply.display             } returns display
        every { this@apply.parent              } returns parent
        every { this@apply.children_           } returns ObservableList()
        every { this@apply.displayed           } returns true
    }
}
