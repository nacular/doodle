package io.nacular.doodle.controls.tree

import io.nacular.doodle.controls.SingleItemSelectionModel
import io.nacular.doodle.controls.theme.TreeBehavior
import io.nacular.doodle.controls.theme.TreeBehavior.RowGenerator
import io.nacular.doodle.utils.Path
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 3/26/18.
 */
class TreeTests {
    @Test @JsName("rootDefaultsToHidden")
    fun `root defaults to hidden`() {
        val tree = Tree<Int, TreeModel<Int>>(model())

        expect(false) { tree.rootVisible     }
        expect(false) { tree.visible(Path()) }
    }

    @Test @JsName("hasRightNumberOfRows")
    fun `has right number of rows`() {
        val root = rootNode(0) { node(1); node(2); node(3) }

        var tree = tree(root)

        expect(3) { tree.numRows }

        expect(1) { tree[Path(0)] }
        expect(2) { tree[Path(1)] }
        expect(3) { tree[Path(2)] }

        tree = tree(root).apply { rootVisible = true }

        expect(4) { tree.numRows }

        expect(0) { tree[Path( )] }
        expect(1) { tree[Path(0)] }
        expect(2) { tree[Path(1)] }
        expect(3) { tree[Path(2)] }
    }

    @Test @JsName("hasRightChildren")
    fun `has right children`() {
        val root = rootNode("root") {
            node("child1") {
                node("child1_1")
                node("child1_2") }
            node("child2") {
                node("child2_1")
            }
            node("child3")
        }

        val generator = rowGenerator<String>()

        val tree = tree(root, behavior(generator)).apply { expand(0) }

        expect(5) { tree.numRows }

        expect(true ) { tree.visible(Path(0)    ) }
        expect(true ) { tree.visible(Path(0) + 0) }
        expect(true ) { tree.visible(Path(0) + 1) }
        expect(true ) { tree.visible(Path(1)    ) }
        expect(false) { tree.visible(Path(1) + 0) }
        expect(true ) { tree.visible(Path(2)    ) }

        expect(true ) { tree.visible(0) }
        expect(true ) { tree.visible(1) }
        expect(true ) { tree.visible(2) }
        expect(true ) { tree.visible(3) }
        expect(true ) { tree.visible(4) }

        expect("child1"  ) { tree[Path(0)    ] }
        expect("child1_1") { tree[Path(0) + 0] }
        expect("child1_2") { tree[Path(0) + 1] }
        expect("child2"  ) { tree[Path(1)    ] }
        expect("child2_1") { tree[Path(1) + 0] }
        expect("child3"  ) { tree[Path(2)    ] }
    }

    @Test @JsName("getWorks")
    fun `get path`() {
        validateGetPath(rootNode(11) { node(105); node(-24) { node(33) }; node(0) }, mapOf(
                Path<Int>( )     to  11,
                Path     (0)     to 105,
                Path     (1)     to -24,
                Path     (1) + 0 to  33,
                Path     (2)     to   0))

        validateGetPath(rootNode(11) { node(105); node(-24) { node(33) }; node(0) }, mapOf(
                Path<Int>( )     to  11,
                Path     (0)     to 105,
                Path     (1)     to -24,
                Path     (1) + 0 to  33,
                Path     (2)     to   0)) { expandAll() }
    }

    @Test @JsName("getRow")
    fun `get row`() {
        validateGetRow(rootNode(11) { node(105); node(-24) { node(33) }; node(0) }, listOf(105, -24, 0    ))
        validateGetRow(rootNode(11) { node(105); node(-24) { node(33) }; node(0) }, listOf(105, -24, 33, 0)) {
            expandAll()
        }
        validateGetRow(rootNode(11) { node(105); node(-24) { node(33) }; node(0) }, listOf(11, 105, -24, 33, 0)) {
            rootVisible = true
            expandAll()
        }
    }

    @Test @JsName("expandAll")
    fun `expand all`() {
        val tree = tree(rootNode("root") {
            node("child1") {
                node("child1_1")
                node("child1_2") {
                    node("child1_2_1")
                }
            }
            node("child2") {
                node("child2_1")
            }
            node("child3")
        })

        val observer = mockk<ExpansionObserver<String>>(relaxed = true)

        tree.expanded += observer

        tree.expandAll()

        verify { observer(tree, setOf(Path(0), Path(0) + 0, Path(0) + 1, Path(0) + 1 + 0, Path(1), Path(1) + 0, Path(2))) }
    }

    @Test @JsName("collapseAll")
    fun `collapse all`() {
        val tree = tree(rootNode("root") {
            node("child1") {
                node("child1_1")
                node("child1_2") {
                    node("child1_2_1")
                }
            }
            node("child2") {
                node("child2_1")
            }
            node("child3")
        })

        val observer = mockk<ExpansionObserver<String>>(relaxed = true)

        tree.collapsed += observer

        tree.expandAll  ()
        tree.collapseAll()

        verify { observer(tree, setOf(Path(0), Path(0) + 0, Path(0) + 1, Path(0) + 1 + 0, Path(1), Path(1) + 0, Path(2))) }
    }

    @Test @JsName("expandNonVisible")
    fun `expand non-visible`() {
        val tree = tree(rootNode("root") {
            node("child1") {
                node("child1_1")
                node("child1_2") {
                    node("child1_2_1")
                }
            }
            node("child2") {
                node("child2_1")
            }
            node("child3")
        })

        val observer = mockk<ExpansionObserver<String>>(relaxed = true)

        tree.expanded += observer

        tree.expand(Path(0) + 0 + 1)

        verify { observer(tree, setOf(Path(0) + 0 + 1)) }
    }

    @Test @JsName("cannotSelectInvisible")
    fun `cannot select invisible`() {
        val tree = Tree(SimpleTreeModel(rootNode("root") {
            node("child1") {
                node("child1_1")
            }
            node("child2")
        }), selectionModel = SingleItemSelectionModel())

        tree.setSelection(setOf(Path(0) + 0))

        expect(true) { tree.selection.isEmpty() }
    }

    private fun <T> rowGenerator(): RowGenerator<T> = mockk(relaxed = true)

    private fun <T> behavior(uiGenerator: RowGenerator<T> = rowGenerator()): TreeBehavior<T> = mockk<TreeBehavior<T>>(relaxed = true).apply {
        every { generator } returns uiGenerator
    }

    private fun <T> tree(root: TreeNode<T>, behavior: TreeBehavior<T> = behavior()) = Tree(SimpleTreeModel(root)).apply { this.behavior = behavior }

    private fun <T> validateGetRow(root: TreeNode<T>, expected: List<T>, block: Tree<T, *>.() -> Unit = {}) {
        val tree = tree(root).also { block(it) }

        expected.forEachIndexed { index, value ->
            expect(value) { tree[index] }
        }
    }

    private fun <T> validateGetPath(root: TreeNode<T>, expected: Map<Path<Int>, T>, block: Tree<T, *>.() -> Unit = {}) {
        val tree = tree(root).also{ block(it) }

        expected.forEach { (path, value) ->
            expect(value) { tree[path] }
        }
    }

    private fun <T> model(): TreeModel<T> {
        val result = mockk<TreeModel<T>>(relaxed = true)

        every { result.isEmpty() } returns true

        return result
    }
}

//private fun addChildren(rootNode: NodeBuilder<String>, config: kotlin.collections.List<Int>) {
//    if (config.isNotEmpty()) {
//        (0 until config[0]).forEach { i ->
//            val child = NodeBuilder("${rootNode.value}[$i]") //getRandomText( 20, 100 ) );
//
//            rootNode.children += child
//
//            addChildren(child, config.drop(1))
//        }
//    }
//}
