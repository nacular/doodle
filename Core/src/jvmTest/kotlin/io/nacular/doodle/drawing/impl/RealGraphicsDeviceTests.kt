@file:Suppress("FunctionName")

package io.nacular.doodle.drawing.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.nacular.doodle.core.container
import io.nacular.doodle.core.view
import io.nacular.doodle.drawing.GraphicsSurface
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 2/26/18.
 */
class RealGraphicsDeviceTests {
    @Test fun `create works`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>()
        val surface1       = mockk<GraphicsSurface>()
        val surface2       = mockk<GraphicsSurface>()

        every { surfaceFactory() } returns surface1 andThen surface2

        val device = RealGraphicsDevice(surfaceFactory)

        expect(surface1) { device.create() }
        expect(surface2) { device.create() }

        verify(exactly = 1) { surface1.index = 0 }
        verify(exactly = 1) { surface2.index = 0 }
    }

    @Test fun `get no parent, no children works`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>()
        val surface        = mockk<GraphicsSurface>()

        every { surfaceFactory(null, any(), false) } returns surface

        val device = RealGraphicsDevice(surfaceFactory)

        expect(surface) { device[view {}] }

        verify(exactly = 1) { surface.zOrder = 0 }
    }

    @Test fun `get no parent, with children works`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>()
        val surface        = mockk<GraphicsSurface>()
        val parent         = container {}
        val child          = view {}

        parent.children_ += child

        every { surfaceFactory(null, any(), true) } returns surface

        val device = RealGraphicsDevice(surfaceFactory)

        expect(surface) { device[parent] }

        verify(exactly = 1) { surface.zOrder = 0 }
    }

    @Test fun `get with parent, no children works`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>()
        val parentSurface  = mockk<GraphicsSurface>()
        val childSurface   = mockk<GraphicsSurface>()
        val parent         = container {}
        val child          = view {}

        parent.children_ += child

        every { surfaceFactory(null,          any(), true ) } returns parentSurface
        every { surfaceFactory(parentSurface, any(), false) } returns childSurface

        val device = RealGraphicsDevice(surfaceFactory)

        expect(parentSurface) { device[parent] }
        expect(childSurface ) { device[child ] }

        verify(exactly = 1) { parentSurface.zOrder = 0 }
        verify(exactly = 1) { childSurface.zOrder  = 0 }
    }

    @Test fun `get with parent, with children works`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>()
        val parentSurface  = mockk<GraphicsSurface>()
        val childSurface   = mockk<GraphicsSurface>()
        val nestedSurface  = mockk<GraphicsSurface>()
        val parent         = container {}
        val child          = view {}
        val nested         = view {}

        parent.children_ += child
        child.children_  += nested

        every { surfaceFactory(null,          any(), true ) } returns parentSurface
        every { surfaceFactory(parentSurface, any(), true ) } returns childSurface
        every { surfaceFactory(childSurface,  any(), false) } returns nestedSurface

        val device = RealGraphicsDevice(surfaceFactory)

        expect(parentSurface) { device[parent  ] }
        expect(childSurface ) { device[child   ] }
        expect(nestedSurface ) { device[nested ] }

        verify(exactly = 1) { parentSurface.zOrder = 0 }
        verify(exactly = 1) { childSurface.zOrder  = 0 }
        verify(exactly = 1) { nestedSurface.zOrder = 0 }
    }

    @Test fun `results cached`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>()
        val parentSurface  = mockk<GraphicsSurface>()
        val childSurface   = mockk<GraphicsSurface>()
        val nestedSurface  = mockk<GraphicsSurface>()
        val parent         = container {}
        val child          = view {}
        val nested         = view {}

        parent.children_ += child
        child.children_  += nested

        every { surfaceFactory(null,          any(), true ) } returns parentSurface andThenThrows Exception()
        every { surfaceFactory(parentSurface, any(), true ) } returns childSurface  andThenThrows Exception()
        every { surfaceFactory(childSurface,  any(), false) } returns nestedSurface andThenThrows Exception()

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

    @Test fun `release view`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>()
        val parentSurface1 = mockk<GraphicsSurface>()
        val childSurface1  = mockk<GraphicsSurface>()
        val nestedSurface1 = mockk<GraphicsSurface>()
        val parentSurface2 = mockk<GraphicsSurface>()
        val childSurface2  = mockk<GraphicsSurface>()
        val nestedSurface2 = mockk<GraphicsSurface>()
        val parent         = container {}
        val child          = view {}
        val nested         = view {}

        parent.children_ += child
        child.children_  += nested

        every { surfaceFactory(null,           any(), true ) } returns parentSurface1 andThen parentSurface2
        every { surfaceFactory(parentSurface1, any(), true ) } returns childSurface1  andThenThrows Exception()
        every { surfaceFactory(childSurface1,  any(), false) } returns nestedSurface1 andThenThrows Exception()

        every { surfaceFactory(parentSurface2, any(), true ) } returns childSurface2  andThenThrows Exception()
        every { surfaceFactory(childSurface2,  any(), false) } returns nestedSurface2 andThenThrows Exception()

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

    @Test fun `release surface`() {
        val surfaceFactory = mockk<GraphicsSurfaceFactory<GraphicsSurface>>()
        val parentSurface1 = mockk<GraphicsSurface>()
        val childSurface1  = mockk<GraphicsSurface>()
        val nestedSurface1 = mockk<GraphicsSurface>()
        val parentSurface2 = mockk<GraphicsSurface>()
        val childSurface2  = mockk<GraphicsSurface>()
        val nestedSurface2 = mockk<GraphicsSurface>()
        val parent         = container {}
        val child          = view {}
        val nested         = view {}

        parent.children_ += child
        child.children_  += nested

        every { surfaceFactory(null,           any(), true ) } returns parentSurface1 andThen parentSurface2
        every { surfaceFactory(parentSurface1, any(), true ) } returns childSurface1  andThenThrows Exception()
        every { surfaceFactory(childSurface1,  any(), false) } returns nestedSurface1 andThenThrows Exception()

        every { surfaceFactory(parentSurface2, any(), true ) } returns childSurface2  andThenThrows Exception()
        every { surfaceFactory(childSurface2,  any(), false) } returns nestedSurface2 andThenThrows Exception()

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