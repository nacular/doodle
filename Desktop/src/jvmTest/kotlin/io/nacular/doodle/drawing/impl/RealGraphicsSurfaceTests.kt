package io.nacular.doodle.drawing.impl

import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.jetbrains.skia.Font
import org.jetbrains.skia.paragraph.FontCollection
import kotlin.test.Test
import kotlin.test.expect

class RealGraphicsSurfaceTests {

    @Test fun `default sort order correct`() {
        val parent = rootSurface()

        val surfaces = (0 .. 3).map {
            createSurface(parent = parent)
        }

        expect(surfaces) { parent.children }

        verify(exactly = surfaces.size) {
            parent.needsRerender()
        }
    }

    @Test fun `setting index in order correct`() {
        val parent = rootSurface()

        val surfaces = (0 .. 3).map {
            createSurface(parent = parent)
        }

        surfaces.forEachIndexed { index, surface -> surface.index = index }

        expect(surfaces) { parent.children }

        verify(exactly = surfaces.size * 2) {
            parent.needsRerender()
        }
    }

    @Test fun `setting index with gaps correct`() {
        val parent = rootSurface()

        val second = createSurface(parent = parent).apply { index = 1 }
        val fourth = createSurface(parent = parent).apply { index = 3 }

        expect(listOf(second, fourth)) { parent.children }

        val third = createSurface(parent = parent).apply { index = 2 }

        verify(exactly = 6) { parent.needsRerender() }

        expect(listOf(second, third, fourth)) { parent.children }

        val first = createSurface(parent = parent).apply { index = 0 }

        verify(exactly = 8) { parent.needsRerender() }

        expect(listOf(first, second, third, fourth)) { parent.children }
    }

    @Test fun `changing index is correct`() {
        val parent = rootSurface()

        val surfaces = (0 .. 3).map {
            createSurface(parent = parent)
        }

        surfaces.forEachIndexed { index, surface -> surface.index = index }

        expect(surfaces) { parent.children }

        surfaces[1].index = surfaces.size - 1

        expect(listOf(
            surfaces[0],
            surfaces[2],
            surfaces[3],
            surfaces[1],
        )) { parent.children }
    }

    @Test fun `setting zOrder correct`() {
        val parent = rootSurface()

        val surfaces = (0 .. 3).map {
            createSurface(parent = parent)
        }

        surfaces.forEachIndexed { index, surface -> surface.index = index }

        surfaces[0].zOrder = 2
        surfaces[3].zOrder = 3

        expect(listOf(
            surfaces[1],
            surfaces[2],
            surfaces[0],
            surfaces[3],
        )) { parent.children }
    }

    @Test fun `setting zOrder correct (2)`() {
        val parent = rootSurface()

        val surfaces = (0 .. 1).map {
            createSurface(parent = parent)
        }

        surfaces.forEachIndexed { index, surface -> surface.index = index }

        surfaces[0].zOrder = 1
        surfaces[1].zOrder = 0

        expect(listOf(surfaces[1], surfaces[0])) { parent.children }
    }

    @Test fun `adding zOrder 0 works if previous updated first`() {
        val parent = rootSurface()

        val firstChild = createSurface(parent = parent).also {
            it.index = 0
        }

        firstChild.zOrder = 1

        val secondChild = createSurface(parent = parent).also {
            it.index = 1
        }

        secondChild.zOrder = 0

        expect(listOf(secondChild, firstChild)) { parent.children }
    }

    @Test fun `adding zOrder 0 works if previous updated last`() {
        val parent = rootSurface()

        val firstChild = createSurface(parent = parent).also {
            it.index = 0
        }

        val secondChild = createSurface(parent = parent).also {
            it.index = 1
        }

        secondChild.zOrder = 0
        firstChild.zOrder  = 1

        expect(listOf(secondChild, firstChild)) { parent.children }
    }

    private fun createSurface(
        parent               : RealGraphicsSurface,
        defaultFont          : Font                 = mockk(),
        fontCollection       : FontCollection       = mockk(),
    ) = RealGraphicsSurface(parent, defaultFont, fontCollection)

    private fun rootSurface() = spyk(RealGraphicsSurface(mockk(), mockk(), mockk()))
}
