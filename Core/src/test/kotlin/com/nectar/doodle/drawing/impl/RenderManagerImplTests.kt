package com.nectar.doodle.drawing.impl

import com.nectar.doodle.JsName
import com.nectar.doodle.core.Container
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.GraphicsSurface
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.theme.InternalThemeManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlin.test.Test

/**
 * Created by Nicholas Eddy on 11/6/17.
 */
class RenderManagerImplTests {
    @Test
    @JsName("renderIgnoresUnknownGizmos")
    fun `render ignores unknown Gizmos`() {
        val gizmo = spyk(gizmo())

        renderManager().render(gizmo)

        verify(exactly = 0) { gizmo.render(any()) }
    }

    @Test
    @JsName("rendersDisplayedGizmos")
    fun `renders displayed Gizmos`() {
        val gizmos = (0 until 2).mapTo(mutableListOf()) { spyk(gizmo()) }

        val display = display(*gizmos.toTypedArray())

        renderManager(display)

        gizmos.forEach {
            verify(exactly = 1) { it.render(any()) }
        }
    }

    @Test
    @JsName("rendersNewGizmos")
    fun `renders new Gizmos`() {
        val container = container()
        val child = spyk(gizmo())

        val display = display(container)

        renderManager(display)

        verify(exactly = 0) { child.render(any()) }

        display.children += child

        verify(exactly = 1) { child.render(any()) }
    }

    @Test
    @JsName("rerendersOnBoundsChanged")
    fun `rerenders on bounds changed`() {
        val gizmo = spyk<Gizmo>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        renderManager(display(gizmo))

        verify(exactly = 1) { gizmo.render(any()) }

        gizmo.size *= 2.0

        verify(exactly = 2) { gizmo.render(any()) }
    }

    @Test
    @JsName("doesNotRerenderOnBoundsZeroed")
    fun `does not rerender on bounds zeroed`() {
        val gizmo = spyk<Gizmo>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        renderManager(display(gizmo))

        verify(exactly = 1) { gizmo.render(any()) }

        gizmo.size *= 0.0

        verify(exactly = 1) { gizmo.render(any()) }
    }

    @Test
    @JsName("doesNotRerenderOnPositionChanged")
    fun `does not rerender on position changed`() {
        val gizmo = spyk<Gizmo>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        renderManager(display(gizmo))

        verify(exactly = 1) { gizmo.render(any()) }

        gizmo.x *= 2.0

        verify(exactly = 1) { gizmo.render(any()) }
    }

    @Test
    @JsName("rendersNewNestedGizmos")
    fun `renders new nested Gizmos`() {
        val container = container()
        val child = spyk(gizmo())

        val display = display(container)

        renderManager(display)

        verify(exactly = 0) { child.render(any()) }

        container.children += child

        verify(exactly = 1) { child.render(any()) }
    }

    @Test
    @JsName("doesNotRenderInvisibleGizmos")
    fun `does not render invisible Gizmos`() = doesNotRender(spyk(gizmo()).apply { visible = false })

    @Test
    @JsName("doesNotRenderZeroBoundsGizmos")
    fun `does not render zero bounds Gizmos`() = doesNotRender(spyk(gizmo()).apply { bounds = Rectangle.Empty })

    @Test
    @JsName("renderNowIgnoresUnknownGizmos")
    fun `renderNow ignores unknown Gizmos`() {
        val gizmo = spyk(gizmo())

        renderManager().renderNow(gizmo)

        verify(exactly = 0) { gizmo.render(any()) }
    }

    @Test
    @JsName("revalidatesParentWhenNewGizmos")
    fun `revalidates parent out when new Gizmos`() {
        val container = spyk<Container>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val child     = gizmo()

        val display = display(container)

        renderManager(display)

        verify(exactly = 0) { container.revalidate_() }

        container.children += child

        verify(exactly = 1) { container.revalidate_() }
    }

    @Test
    @JsName("laysOutParentOnBoundsChanged")
    fun `lays out parent on bounds changed`() {
        val container = spyk<Container>("xyz").apply { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val child     = gizmo()

        container.children += child

        renderManager(display(container))

        verify(exactly = 1) { container.doLayout_() }

        child.size *= 2.0

        verify(exactly = 2) { container.doLayout_() }
    }

//    @Test
//    @JsName("doesNotRerenderOnBoundsZeroed")
//    fun `does not rerender on bounds zeroed`() {
//        val gizmo = spyk<Gizmo>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }
//
//        renderManager(display(gizmo))
//
//        verify(exactly = 1) { gizmo.render(any()) }
//
//        gizmo.size *= 0.0
//
//        verify(exactly = 1) { gizmo.render(any()) }
//    }
//
//    @Test
//    @JsName("doesNotRerenderOnPositionChanged")
//    fun `does not rerender on position changed`() {
//        val gizmo = spyk<Gizmo>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }
//
//        renderManager(display(gizmo))
//
//        verify(exactly = 1) { gizmo.render(any()) }
//
//        gizmo.x *= 2.0
//
//        verify(exactly = 1) { gizmo.render(any()) }
//    }


    private fun gizmo(): Gizmo = object: Gizmo() {}.apply { bounds = Rectangle(size = Size(10.0, 10.0)) }
    private fun container(): Container = Container().apply { bounds = Rectangle(size = Size(10.0, 10.0)) }

    private fun doesNotRender(gizmo: Gizmo) {
        renderManager(display(gizmo))

        verify(exactly = 0) { gizmo.render(any()) }
    }

    private fun renderManager(
            display       : Display              = mockk(relaxed = true),
            themeManager  : InternalThemeManager = mockk(relaxed = true),
            scheduler     : Scheduler            = instantScheduler,
            graphicsDevice: GraphicsDevice<*>    = defaultGraphicsDevice) = RenderManagerImpl(display, scheduler, themeManager, graphicsDevice)

    private val defaultGraphicsDevice by lazy {
        val result = mockk<GraphicsDevice<*>>(relaxed = true)

        val surface = mockk<GraphicsSurface>(relaxed = true)

        every { surface.render(captureLambda()) } answers {
            lambda<(Canvas) -> Unit>().captured(mockk(relaxed = true))
        }

        every { result[any()] } answers { surface }

        result
    }

    private fun display(vararg children: Gizmo): Display = mockk<Display>(relaxed = true).apply {
        val container = Container()

        container.children.addAll(children)

        val gizmo = slot<Gizmo>()

        every { this@apply.children                   } returns container.children
        every { this@apply.iterator()                 } answers { container.children.iterator() }
        every { sizeChanged                           } returns mockk(relaxed = true)
        every { this@apply.isAncestor(capture(gizmo)) } answers { container.isAncestor(gizmo.captured) }
    }

    private val instantScheduler by lazy { mockk<Scheduler>(relaxed = true).apply {
        every { this@apply.after(any(), captureLambda()) } answers {
            lambda<() -> Unit>().captured()

            val task = mockk<Task>()

            every { task.completed } returns true

            task
        }
    }}
}