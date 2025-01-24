package io.nacular.doodle.drawing.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.Element
import io.nacular.doodle.dom.HTMLCollection
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Node
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

    @Test fun `default sort order correct (top level) display canvas`() {
        val root = createHtmlElement().apply { add(mockk()) }

        val data = (0 .. 3).map {
            createSurface(root, displayCanvas = true)
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

    @Test fun `setting index in order correct (top level) display canvas`() {
        val root = createHtmlElement().apply { add(mockk()) }

        val data = (0 .. 3).map {
            createSurface(root, displayCanvas = true)
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

    @Test fun `setting index with gaps correct (top level)`() {
        val root = createHtmlElement()

        val second = createSurface(root).apply { surface.index = 1 }
        val fourth = createSurface(root).apply { surface.index = 3 }

        expect(listOf(second, fourth).map { it.root }) { root.children.toList() }

        val third = createSurface(root).apply { surface.index = 2 }
        val first = createSurface(root).apply { surface.index = 0 }

        expect(listOf(first, second, third, fourth).map { it.root }) { root.children.toList() }
    }

    @Test fun `setting index with gaps correct (top level) display canvas`() {
        val root = createHtmlElement(mockk())

        val second = createSurface(root, displayCanvas = true).apply { surface.index = 1 }
        val fourth = createSurface(root, displayCanvas = true).apply { surface.index = 3 }

        expect(listOf(second, fourth).map { it.root }) { root.children.toList().drop(1) }

        val third = createSurface(root, displayCanvas = true).apply { surface.index = 2 }

        expect(listOf(second, third, fourth).map { it.root }) { root.children.toList().drop(1) }

        val first = createSurface(root, displayCanvas = true).apply { surface.index = 0 }

        expect(listOf(first, second, third, fourth).map { it.root }) { root.children.toList().drop(1) }
    }

    @Test fun `setting index with gaps correct`() {
        val root   = createHtmlElement()
        val parent = createSurface(root).surface

        val second = createSurface(root, parent).apply { surface.index = 1 }
        val fourth = createSurface(root, parent).apply { surface.index = 3 }

        expect(listOf(second, fourth).map { it.root }) { parent.rootElement.children.toList().drop(1) }

        val third = createSurface(root, parent).apply { surface.index = 2 }

        expect(listOf(second, third, fourth).map { it.root }) { parent.rootElement.children.toList().drop(1) }

        val first = createSurface(root, parent).apply { surface.index = 0 }

        expect(listOf(first, second, third, fourth).map { it.root }) { parent.rootElement.children.toList().drop(1) }
    }

    @Test fun `changing index in correct (top level)`() {
        val root = createHtmlElement()

        val data = (0 .. 3).map {
            createSurface(root)
        }

        val surfaces     = data.map { it.surface }
        val surfaceRoots = data.map { it.root    }

        surfaces.forEachIndexed { index, surface -> surface.index = index }

        expect(surfaceRoots) { root.children.toList() }

        surfaces[1].index = surfaces.size - 1

        expect(listOf(
            surfaceRoots[0],
            surfaceRoots[2],
            surfaceRoots[3],
            surfaceRoots[1],
        )) { root.children.toList() }
    }

    @Test fun `changing index in correct (top level) display canvas`() {
        val root = createHtmlElement(mockk())

        val data = (0 .. 3).map {
            createSurface(root, displayCanvas = true)
        }

        val surfaces     = data.map { it.surface }
        val surfaceRoots = data.map { it.root    }

        surfaces.forEachIndexed { index, surface -> surface.index = index }

        expect(surfaceRoots) { root.children.toList().drop(1) }

        surfaces[1].index = surfaces.size - 1

        expect(listOf(
            surfaceRoots[0],
            surfaceRoots[2],
            surfaceRoots[3],
            surfaceRoots[1],
        )) { root.children.toList().drop(1) }
    }

    @Test fun `changing index is correct`() {
        val root   = createHtmlElement()
        val parent = createSurface(root).surface

        val data = (0 .. 3).map {
            createSurface(root, parent)
        }

        val surfaces     = data.map { it.surface }
        val surfaceRoots = data.map { it.root    }

        surfaces.forEachIndexed { index, surface -> surface.index = index }

        expect(surfaceRoots) { parent.rootElement.children.toList().drop(1) }

        surfaces[1].index = surfaces.size - 1

        expect(listOf(
            surfaceRoots[0],
            surfaceRoots[2],
            surfaceRoots[3],
            surfaceRoots[1],
        )) { parent.rootElement.children.toList().drop(1) }
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

    @Test fun `setting zOrder correct (top level) display canvas`() {
        val root = createHtmlElement(mockk())

        val data = (0 .. 3).map {
            createSurface(root, displayCanvas = true)
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
        )) { root.children.toList().drop(1) }
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

    @Test fun `setting zOrder correct (2)`() {
        val root   = createHtmlElement()
        val parent = createSurface(root).surface

        val data = (0 .. 1).map {
            createSurface(root, parent)
        }

        val surfaces     = data.map { it.surface }
        val surfaceRoots = data.map { it.root    }

        surfaces.forEachIndexed { index, surface -> surface.index = index }

        surfaces[0].zOrder = 1
        surfaces[1].zOrder = 0

        expect(listOf(
            surfaceRoots[1],
            surfaceRoots[0],
        )) { parent.rootElement.children.toList().drop(1) }
    }

    @Test fun `adding zOrder 0 works if previous updated first`() {
        val root   = createHtmlElement()
        val parent = createSurface(root).surface

        val firstChild = createSurface(root, parent).also {
            it.surface.index = 0
        }

        firstChild.surface.zOrder = 1

        val secondChild = createSurface(root, parent).also {
            it.surface.index = 1
        }

        secondChild.surface.zOrder = 0

        expect(listOf(
            secondChild.root,
            firstChild.root,
        )) { parent.rootElement.children.toList().drop(1) }
    }

    @Test fun `adding zOrder 0 works if previous updated last`() {
        val root   = createHtmlElement()
        val parent = createSurface(root).surface

        val firstChild = createSurface(root, parent).also {
            it.surface.index = 0
        }

        val secondChild = createSurface(root, parent).also {
            it.surface.index = 1
        }

        secondChild.surface.zOrder = 0
        firstChild.surface.zOrder  = 1

        expect(listOf(
            secondChild.root,
            firstChild.root,
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

    @Test fun `popups always on top display canvas`() {
        val root = createHtmlElement(mockk())

        val data = (0 .. 3).map {
            createSurface(root, displayCanvas = true)
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
        )) { root.children.toList().drop(1) }
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

    @Test fun `zOrder cannot take over popup display canvas`() {
        val root = createHtmlElement(mockk())

        val data = (0 .. 3).map {
            createSurface(root, displayCanvas = true)
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
        )) { root.children.toList().drop(1) }
    }

    private fun HTMLCollection.toList(): List<Element> = mutableListOf<Element>().also { elements ->
        repeat(length) {
            elements += item(it)!!
        }
    }

    private data class Surface(val surface: RealGraphicsSurface, val root: HTMLElement)

    private fun createHtmlElement(child: HTMLElement? = null): HTMLElement {
        val indexSlot   = slot<Int>     ()
        val elementSlot = slot<Element> ()
        val siblingSlot = slot<Element?>()
        val childList   = mutableListOf<Element>()

        if (child != null) { childList.add(child) }

        return mockk<HTMLElement> {
            every { children } returns mockk<HTMLCollection> {
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

            every { appendChild(capture(elementSlot)) } answers {
                elementSlot.captured.also {
                    childList += it
                    every { it.parentNode } returns this@mockk
                }
            }

            every { remove(capture(elementSlot)) } answers {
                elementSlot.captured.also {
                    childList -= it
                    every { it.parentNode } returns null
                }
            }

            every { insertBefore(capture(elementSlot), captureNullable(siblingSlot)) } answers {
                elementSlot.captured.also {
                    when (val sibling = siblingSlot.captured) {
                        null -> childList.add(it)
                        else -> childList.add(childList.indexOf(sibling), it)
                    }
                    every { it.parentNode } returns this@mockk
                }
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

    private fun createSurface(root: HTMLElement, displayCanvas: Boolean): Surface {
        val surfaceRoot = createHtmlElement()

        val htmlFactory = mockk<HtmlFactory>().apply {
            every { this@apply.root } returns root
            every { create<HTMLElement>() } returns surfaceRoot andThen mockk<HTMLElement>()
        }

        return Surface(createSurface(htmlFactory, null, rootElementOffset = if (displayCanvas) 1 else 0), surfaceRoot)
    }

    private fun createSurface(
        htmlFactory      : HtmlFactory          = mockk(),
        parent           : RealGraphicsSurface? = null,
        canvasFactory    : CanvasFactory        = mockk(),
        rootElementOffset: Int                  = 0
    ): RealGraphicsSurface {
        val view = mockk< View>()

        return RealGraphicsSurface(htmlFactory, canvasFactory, nonPopupTopLevelSurfaces, parent = parent, view, rootElementOffset = { rootElementOffset })
    }
}