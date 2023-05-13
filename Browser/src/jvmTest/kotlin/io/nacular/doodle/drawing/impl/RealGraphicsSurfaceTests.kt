package io.nacular.doodle.drawing.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.nacular.doodle.Element
import io.nacular.doodle.HTMLCollection
import io.nacular.doodle.HTMLElement
import io.nacular.doodle.Node
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.childAt
import io.nacular.doodle.dom.index
import io.nacular.doodle.dom.remove
import io.nacular.doodle.drawing.CanvasFactory
import kotlin.test.Test
import kotlin.test.expect

class RealGraphicsSurfaceTests {
    private val nonPopupTopLevelSurfaces = mutableListOf<RealGraphicsSurface>()

    init {
        mockkStatic(HTMLElement::childAt)
        mockkStatic(Node::removeChild)
    }

    @Test fun `default sort order correct (top level)`() {
        val root = createHtmlElement()

        val data = (0 .. 3).map {
            createSurface(root)
        }

        val surfaceRoots = data.map { it.root }

        expect(surfaceRoots) { root.children.toList() }
    }

    @Test fun `default sort order correct`() {
        val root   = createHtmlElement()
        val parent = createSurface(root).surface

        val data = (0 .. 3).map {
            createSurface(root, parent)
        }

        val surfaceRoots = data.map { it.root }

        expect(surfaceRoots) { parent.rootElement.children.toList().drop(1) }
    }

    @Test fun `setting index in order correct (top level)`() {
        val root = createHtmlElement()

        val data = (0 .. 3).map {
            createSurface(root)
        }

        val surfaces     = data.map { it.surface }
        val surfaceRoots = data.map { it.root    }

        surfaces.forEachIndexed { index, surface -> surface.index = index }

        expect(surfaceRoots) { root.children.toList() }
    }

    @Test fun `setting index in order correct`() {
        val root   = createHtmlElement()
        val parent = createSurface(root).surface

        val data = (0 .. 3).map {
            createSurface(root, parent)
        }

        val surfaces     = data.map { it.surface }
        val surfaceRoots = data.map { it.root    }

        surfaces.forEachIndexed { index, surface -> surface.index = index }

        expect(surfaceRoots) { parent.rootElement.children.toList().drop(1) }
    }

    @Test fun `setting zOrder correct (top level)`() {
        val root = createHtmlElement()

        val data = (0 .. 3).map {
            createSurface(root)
        }

        val surfaces     = data.map { it.surface }
        val surfaceRoots = data.map { it.root    }

        surfaces.forEachIndexed { index, surface -> surface.index = index }

        surfaces[0].zOrder = 2
        surfaces[3].zOrder = 3

        expect(listOf(
            surfaceRoots[1],
            surfaceRoots[2],
            surfaceRoots[0],
            surfaceRoots[3],
        )) { root.children.toList() }
    }

    @Test fun `setting zOrder correct`() {
        val root   = createHtmlElement()
        val parent = createSurface(root).surface

        val data = (0 .. 3).map {
            createSurface(root, parent)
        }

        val surfaces     = data.map { it.surface }
        val surfaceRoots = data.map { it.root    }

        surfaces.forEachIndexed { index, surface -> surface.index = index }

        surfaces[0].zOrder = 2
        surfaces[3].zOrder = 3

        expect(listOf(
            surfaceRoots[1],
            surfaceRoots[2],
            surfaceRoots[0],
            surfaceRoots[3],
        )) { parent.rootElement.children.toList().drop(1) }
    }

    @Test fun `popups always on top`() {
        val root = createHtmlElement()

        val data = (0 .. 3).map {
            createSurface(root)
        }

        val surfaces     = data.map { it.surface }
        val surfaceRoots = data.map { it.root    }

        surfaces.forEachIndexed { index, surface -> surface.index = index }

        // this effectively makes this a popup
        surfaces[2].index = -1

        expect(listOf(
            surfaceRoots[0],
            surfaceRoots[1],
            surfaceRoots[3],
            surfaceRoots[2],
        )) { root.children.toList() }
    }

    @Test fun `zOrder cannot take over popup`() {
        val root = createHtmlElement()

        val data = (0 .. 3).map {
            createSurface(root)
        }

        val surfaces     = data.map { it.surface }
        val surfaceRoots = data.map { it.root    }

        surfaces.forEachIndexed { index, surface -> surface.index = index }

        // this effectively makes this a popup
        surfaces.last().index = -1

        surfaces[1].zOrder = 100

        expect(listOf(
            surfaceRoots[0],
            surfaceRoots[2],
            surfaceRoots[1],
            surfaceRoots[3],
        )) { root.children.toList() }
    }

    private fun HTMLCollection.toList(): List<Element> = mutableListOf<Element>().also { elements ->
        repeat(length) {
            elements += item(it)!!
        }
    }

    private data class Surface(val surface: RealGraphicsSurface, val root: HTMLElement)

    private fun createHtmlElement(): HTMLElement {
        val indexSlot   = slot<Int>()
        val elementSlot = slot<Element>()
        val siblingSlot = mutableListOf<Element?>() // FIXME: work-around for slot not supporting nullables https://github.com/mockk/mockk/issues/293
        val childList   = mutableListOf<Element>()

        return mockk<HTMLElement>().apply {
            every { children } returns mockk<HTMLCollection>().apply {
                every { length } answers { childList.size }
                every { item(capture(indexSlot)) } answers {
                    childList[indexSlot.captured]
                }
            }

            every { index(capture(elementSlot)) } answers {
                childList.indexOf(elementSlot.captured)
            }

            every { childAt(capture(indexSlot)) } answers {
                childList.getOrNull(indexSlot.captured)
            }

            every { add(capture(elementSlot)) } answers {
                elementSlot.captured.also {
                    childList += it
                    every { it.parentNode } returns this@apply
                }
            }

            every { remove(capture(elementSlot)) } answers {
                elementSlot.captured.also {
                    childList -= it
                    every { it.parentNode } returns null
                }
            }

            every { insertBefore(capture(elementSlot), captureNullable(siblingSlot)) } answers {
                when (val sibling = siblingSlot.captured()) {
                    null -> childList.add(elementSlot.captured)
                    else -> childList.add(childList.indexOf(sibling), elementSlot.captured)
                }
                elementSlot.captured
            }
        }
    }

    private fun createSurface(root: HTMLElement, parent: RealGraphicsSurface? = null): Surface {
        val surfaceRoot = createHtmlElement()

        val htmlFactory = mockk<HtmlFactory>().apply {
            every { this@apply.root } returns root
            every { create<HTMLElement>() } returns surfaceRoot andThen mockk<HTMLElement>()
        }

        return Surface(createSurface(htmlFactory, parent), surfaceRoot)
    }

    private fun createSurface(htmlFactory: HtmlFactory = mockk(), parent: RealGraphicsSurface? = null, canvasFactory: CanvasFactory = mockk()): RealGraphicsSurface {
        val view = mockk< View>()

        return RealGraphicsSurface(htmlFactory, canvasFactory, nonPopupTopLevelSurfaces, parent = parent, view)
    }
}