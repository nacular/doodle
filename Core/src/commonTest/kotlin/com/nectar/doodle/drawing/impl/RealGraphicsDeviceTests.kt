@file:Suppress("FunctionName")

package com.nectar.doodle.drawing.impl

import com.nectar.doodle.JsName
import com.nectar.doodle.core.Box
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.GraphicsSurface
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 2/26/18.
 */
class RealGraphicsDeviceTests {
    @Test @JsName("create")
    fun `create works`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>(relaxed = true)
        val surface1       = mockk<GraphicsSurface>(relaxed = true)
        val surface2       = mockk<GraphicsSurface>(relaxed = true)

        every { surfaceFactory() } returns surface1 andThen surface2

        val device = RealGraphicsDevice(surfaceFactory)

        expect(surface1) { device.create() }
        expect(surface2) { device.create() }

        verify(exactly = 1) { surface1.index = 0 }
        verify(exactly = 1) { surface2.index = 0 }
    }

    @Test @JsName("getNoParentNoChildrenWorks")
    fun `get no parent, no children works`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>(relaxed = true)
        val surface        = mockk<GraphicsSurface>(relaxed = true)

        every { surfaceFactory(null, mockk(relaxed = true)) } returns surface

        val device = RealGraphicsDevice(surfaceFactory)

        expect(surface) { device[object: View() {}] }

        verify(exactly = 1) { surface.zOrder = 0 }
    }

    @Test @JsName("getNoParentWithChildrenWorks")
    fun `get no parent, with children works`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>(relaxed = true)
        val surface        = mockk<GraphicsSurface>(relaxed = true)
        val parent         = Box()
        val child          = object: View() {}

        parent.children_ += child

        every { surfaceFactory(null, child) } returns surface

        val device = RealGraphicsDevice(surfaceFactory)

        expect(surface) { device[parent] }

        verify(exactly = 1) { surface.zOrder = 0 }
    }

    @Test @JsName("getWithParentNoChildrenWorks")
    fun `get with parent, no children works`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>(relaxed = true)
        val parentSurface  = mockk<GraphicsSurface>(relaxed = true)
        val childSurface   = mockk<GraphicsSurface>(relaxed = true)
        val parent         = Box()
        val child          = object: View() {}

        parent.children_ += child

        every { surfaceFactory(null,          child                ) } returns parentSurface
        every { surfaceFactory(parentSurface, mockk(relaxed = true)) } returns childSurface

        val device = RealGraphicsDevice(surfaceFactory)

        expect(parentSurface) { device[parent] }
        expect(childSurface ) { device[child ] }

        verify(exactly = 1) { parentSurface.zOrder = 0 }
        verify(exactly = 1) { childSurface.zOrder  = 0 }
    }

    @Test @JsName("getWithParentWithChildrenWorks")
    fun `get with parent, with children works`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>(relaxed = true)
        val parentSurface  = mockk<GraphicsSurface>(relaxed = true)
        val childSurface   = mockk<GraphicsSurface>(relaxed = true)
        val nestedSurface  = mockk<GraphicsSurface>(relaxed = true)
        val parent         = Box()
        val child          = object: View() {}
        val nested         = object: View() {}

        parent.children_ += child
        child.children_  += nested

        every { surfaceFactory(null,          parent               ) } returns parentSurface
        every { surfaceFactory(parentSurface, child                ) } returns childSurface
        every { surfaceFactory(childSurface,  mockk(relaxed = true)) } returns nestedSurface

        val device = RealGraphicsDevice(surfaceFactory)

        expect(parentSurface) { device[parent  ] }
        expect(childSurface ) { device[child   ] }
        expect(nestedSurface ) { device[nested ] }

        verify(exactly = 1) { parentSurface.zOrder = 0 }
        verify(exactly = 1) { childSurface.zOrder  = 0 }
        verify(exactly = 1) { nestedSurface.zOrder = 0 }
    }

    @Test @JsName("resultsCached")
    fun `results cached`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>(relaxed = true)
        val parentSurface  = mockk<GraphicsSurface>(relaxed = true)
        val childSurface   = mockk<GraphicsSurface>(relaxed = true)
        val nestedSurface  = mockk<GraphicsSurface>(relaxed = true)
        val parent         = Box()
        val child          = object: View() {}
        val nested         = object: View() {}

        parent.children_ += child
        child.children_  += nested

        every { surfaceFactory(null,          parent               ) } returns parentSurface andThenThrows Exception()
        every { surfaceFactory(parentSurface, child                ) } returns childSurface  andThenThrows Exception()
        every { surfaceFactory(childSurface,  mockk(relaxed = true)) } returns nestedSurface andThenThrows Exception()

        val device = RealGraphicsDevice(surfaceFactory)

        repeat((0..2).count()) {
            expect(parentSurface) { device[parent] }
            expect(childSurface) { device[child] }
            expect(nestedSurface) { device[nested] }
        }

        verify(exactly = 1) { parentSurface.zOrder = 0 }
        verify(exactly = 1) { childSurface.zOrder  = 0 }
        verify(exactly = 1) { nestedSurface.zOrder = 0 }
    }

    @Test @JsName("releaseView")
    fun `release view`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>(relaxed = true)
        val parentSurface1 = mockk<GraphicsSurface>(relaxed = true)
        val childSurface1  = mockk<GraphicsSurface>(relaxed = true)
        val nestedSurface1 = mockk<GraphicsSurface>(relaxed = true)
        val parentSurface2 = mockk<GraphicsSurface>(relaxed = true)
        val childSurface2  = mockk<GraphicsSurface>(relaxed = true)
        val nestedSurface2 = mockk<GraphicsSurface>(relaxed = true)
        val parent         = Box()
        val child          = object: View() {}
        val nested         = object: View() {}

        parent.children_ += child
        child.children_  += nested

        every { surfaceFactory(null,           parent ) } returns parentSurface1 andThen parentSurface2
        every { surfaceFactory(parentSurface1, child  ) } returns childSurface1  andThenThrows Exception()
        every { surfaceFactory(childSurface1,  nested ) } returns nestedSurface1 andThenThrows Exception()

        every { surfaceFactory(parentSurface2, child ) } returns childSurface2  andThenThrows Exception()
        every { surfaceFactory(childSurface2,  nested) } returns nestedSurface2 andThenThrows Exception()

        val device = RealGraphicsDevice(surfaceFactory)

        repeat((0..2).count()) {
            expect(parentSurface1) { device[parent] }
            expect(childSurface1 ) { device[child ] }
            expect(nestedSurface1) { device[nested] }
        }

        device.release(parent)

        repeat((0..2).count()) {
            expect(parentSurface2) { device[parent] }
            expect(childSurface2 ) { device[child ] }
            expect(nestedSurface2) { device[nested] }
        }
    }

    @Test @JsName("releaseSurface")
    fun `release surface`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>(relaxed = true)
        val parentSurface1 = mockk<GraphicsSurface>(relaxed = true)
        val childSurface1  = mockk<GraphicsSurface>(relaxed = true)
        val nestedSurface1 = mockk<GraphicsSurface>(relaxed = true)
        val parentSurface2 = mockk<GraphicsSurface>(relaxed = true)
        val childSurface2  = mockk<GraphicsSurface>(relaxed = true)
        val nestedSurface2 = mockk<GraphicsSurface>(relaxed = true)
        val parent         = Box()
        val child          = object: View() {}
        val nested         = object: View() {}

        parent.children_ += child
        child.children_  += nested

        every { surfaceFactory(null,           parent) } returns parentSurface1 andThen parentSurface2
        every { surfaceFactory(parentSurface1, child ) } returns childSurface1  andThenThrows Exception()
        every { surfaceFactory(childSurface1,  nested) } returns nestedSurface1 andThenThrows Exception()

        every { surfaceFactory(parentSurface2, child ) } returns childSurface2  andThenThrows Exception()
        every { surfaceFactory(childSurface2,  nested) } returns nestedSurface2 andThenThrows Exception()

        val device = RealGraphicsDevice(surfaceFactory)

        repeat((0..2).count()) {
            expect(parentSurface1) { device[parent] }
            expect(childSurface1 ) { device[child ] }
            expect(nestedSurface1) { device[nested] }
        }

        device.release(parentSurface1)

        repeat((0..2).count()) {
            expect(parentSurface2) { device[parent] }
            expect(childSurface2 ) { device[child ] }
            expect(nestedSurface2) { device[nested] }
        }
    }
}