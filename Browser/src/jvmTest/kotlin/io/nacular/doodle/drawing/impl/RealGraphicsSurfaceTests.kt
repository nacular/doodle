package io.nacular.doodle.drawing.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.nacular.doodle.Element
import io.nacular.doodle.HTMLElement
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.childAt
import io.nacular.doodle.dom.index
import io.nacular.doodle.drawing.CanvasFactory
import kotlin.test.Test
import kotlin.test.expect

class RealGraphicsSurfaceTests {
    private val nonPopupTopLevelSurfaces = mutableListOf<RealGraphicsSurface>()

    init {
        mockkStatic(HTMLElement::childAt)
    }

    @Test fun `default sort order correct`() {
        val root = createRoot()

        val data = (0 .. 3).map {
            createSurface(root.htmlElement)
        }

        val surfaceRoots = data.map { it.root    }

        expect(surfaceRoots) { root.children }
    }

    @Test fun `setting index in order correct`() {
        val root = createRoot()

        val data = (0 .. 3).map {
            createSurface(root.htmlElement)
        }

        val surfaces     = data.map { it.surface }
        val surfaceRoots = data.map { it.root    }

        surfaces.forEachIndexed { index, surface -> surface.index = index }

        expect(surfaceRoots) { root.children }
    }

    @Test fun `setting zOrder correct`() {
        val root = createRoot()

        val data = (0 .. 3).map {
            createSurface(root.htmlElement)
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
        )) { root.children }
    }

    @Test fun `popups always on top`() {
        val root = createRoot()

        val data = (0 .. 3).map {
            createSurface(root.htmlElement)
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
        )) { root.children }
    }

    @Test fun `zOrder cannot take over popup`() {
        val root = createRoot()

        val data = (0 .. 3).map {
            createSurface(root.htmlElement)
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
        )) { root.children }
    }

    private data class Root(val htmlElement: HTMLElement, val children: List<Element>)
    private data class Surface(val surface: RealGraphicsSurface, val root: HTMLElement)

    private fun createRoot(): Root {
        val indexSlot   = slot<Int>()
        val elementSlot = slot<Element>()
        val siblingSlot = mutableListOf<Element?>() // FIXME: work-around for slot not supporting nullables https://github.com/mockk/mockk/issues/293

        val childrenInRoot = mutableListOf<Element>()

        return Root(mockk<HTMLElement>().apply {
            every { index(capture(elementSlot)) } answers {
                childrenInRoot.indexOf(elementSlot.captured)
            }

            every { removeChild(capture(elementSlot)) } answers {
                childrenInRoot.remove(elementSlot.captured)
                elementSlot.captured
            }

            every { childAt(capture(indexSlot)) } answers {
                childrenInRoot.getOrNull(indexSlot.captured)
            }

            every { add(capture(elementSlot)) } answers {
                elementSlot.captured.also { childrenInRoot.add(it) }
            }

            every { insertBefore(capture(elementSlot), captureNullable(siblingSlot)) } answers {
                when (val sibling = siblingSlot.captured()) {
                    null -> childrenInRoot.add(elementSlot.captured)
                    else -> childrenInRoot.add(childrenInRoot.indexOf(sibling), elementSlot.captured)
                }
                elementSlot.captured
            }
        }, childrenInRoot)
    }

    private fun createSurface(root: HTMLElement): Surface {
        val surfaceRoot = mockk<HTMLElement>().apply { every { parentNode } returns root }

        val htmlFactory = mockk<HtmlFactory>().apply {
            every { this@apply.root } returns root
            every { create<HTMLElement>() } returns surfaceRoot andThen mockk<HTMLElement>()
        }

        return Surface(createSurface(htmlFactory), surfaceRoot)
    }

    private fun createSurface(htmlFactory: HtmlFactory = mockk(), canvasFactory: CanvasFactory = mockk()): RealGraphicsSurface {
        val view = mockk< View>()

        return RealGraphicsSurface(htmlFactory, canvasFactory, nonPopupTopLevelSurfaces, parent = null, view)
    }
}