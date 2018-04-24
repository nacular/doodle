package com.nectar.doodle.drawing.impl

import com.nectar.doodle.JsName
import com.nectar.doodle.core.Box
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.GraphicsSurface
import com.nectar.doodle.drawing.RenderManager
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.scheduler.AnimationScheduler
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.theme.InternalThemeManager
import com.nectar.doodle.time.Timer
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.milliseconds
import com.nectar.doodle.utils.ObservableList
import com.nectar.doodle.utils.Pool
import com.nectar.doodle.utils.PropertyObserver
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlin.test.Test

/**
 * Created by Nicholas Eddy on 11/6/17.
 */
class RenderManagerImplTests {
    // TODO: Add layout tests
    // TODO: Add tests to make sure things in cleanup list never get rendered
    // TODO: Add tests for display rect handling

    @Test @JsName("rendersAreBatched")
    fun `renders are batched`() {
        val gizmo     = spyk(gizmo())
        val scheduler = ManualAnimationScheduler()

        gizmo.visible = false

        val renderManager = renderManager(display(gizmo), scheduler = scheduler)

        gizmo.visible  = true
        gizmo.size    *= 2.0

        verify(exactly = 0) { gizmo.render(any()) }

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, gizmo)
    }

    @Test @JsName("laysOutDisplayOnSizeChange")
    fun `lays out display on size change`() {

        val display = display(gizmo())

        val slot = slot<PropertyObserver<Display, Size>>()

        every { display.sizeChanged.plusAssign(capture(slot)) } just Runs

        renderManager(display)

        verify(exactly = 0) { display.doLayout() }

        slot.captured(display, display.size, display.size * 2.0)

        verify(exactly = 1) { display.doLayout() }
    }

    @Test @JsName("renderIgnoresUnknownGizmos")
    fun `render ignores unknown gizmos`() {
        val gizmo = spyk(gizmo())

        renderManager().render(gizmo)

        verify(exactly = 0) { gizmo.render(any()) }
    }

    @Test @JsName("rendersDisplayedGizmos")
    fun `renders displayed gizmos`() {
        val gizmos = (0 until 2).mapTo(mutableListOf()) { spyk(gizmo()) }

        val renderManager = renderManager(display(*gizmos.toTypedArray()))

        gizmos.forEach {
            verifyChildAddedProperly(renderManager, it)
        }
    }

    @Test @JsName("rendersNewGizmos")
    fun `renders new gizmos`() {
        val child = spyk(gizmo())

        val display = display()

        val renderManager = renderManager(display)

        verify(exactly = 0) { child.render(any()) }

        display.children += child

        verifyChildAddedProperly(renderManager, child)
    }

    @Test @JsName("removesTopLevelGizmos")
    fun `removes top-level gizmos`() {
        val container = spyk<Box>().apply { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(gizmo()).apply { children += spyk(gizmo()) } }

        val display = display(container)

        val renderManager = renderManager(display)

        verifyChildAddedProperly(renderManager, container)

        display.children.remove(container)

        verifyChildRemovedProperly(container)
    }

    @Test @JsName("noopRemoveAddTopLevelGizmos")
    fun `no-op remove, add top-level gizmos`() = testDisplayZIndex { display, gizmo -> display.children.move(gizmo, 0) }

    @Test @JsName("noopZIdexChangeTopLevelGizmos")
    fun `no-op z-index change top-level gizmos`() = testDisplayZIndex { display, gizmo -> display.setZIndex(gizmo, 0) }

    @Test @JsName("removesNestedGizmos")
    fun `removes nested gizmos`() {
        val container = spyk<Box>().apply { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(gizmo()).apply { children += spyk(gizmo()) } }

        val display = display(container)

        val renderManager = renderManager(display)

        verifyChildAddedProperly(renderManager, container)

        val firstChild = container.children.first()
        container.children -= firstChild

        verifyChildRemovedProperly(firstChild)
    }

    @Test @JsName("rerendersOnBoundsChanged")
    fun `rerenders on bounds changed`() {
        val gizmo = spyk<Gizmo>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        val renderManager = renderManager(display(gizmo))

        verifyChildAddedProperly(renderManager, gizmo)

        gizmo.size *= 2.0

        verify(exactly = 2) { gizmo.render(any()) }
    }

    @Test @JsName("rerendersOnBecomingVisible")
    fun `rerenders on becoming visible`() {
        val gizmo = spyk<Gizmo>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        gizmo.visible = false

        val renderManager = renderManager(display(gizmo))

        verify(exactly = 1) { gizmo.addedToDisplay(renderManager) }
        verify(exactly = 0) { gizmo.render        (any()        ) }

        gizmo.visible = true

        verifyChildAddedProperly(renderManager, gizmo)
    }

    @Test @JsName("rerendersOnAddedBecomingVisible")
    fun `rerenders on added becoming visible`() {
        val parent = container()
        val gizmo  = spyk<Gizmo>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        gizmo.visible = false

        val renderManager = renderManager(display(parent))

        verify(exactly = 0) { gizmo.addedToDisplay(renderManager) }
        verify(exactly = 0) { gizmo.render        (any()        ) }

        parent.children_ += gizmo

        gizmo.visible = true

        verifyChildAddedProperly(renderManager, gizmo)
    }

    @Test @JsName("doesNotRerenderOnBoundsZeroed")
    fun `does not rerender on bounds zeroed`() {
        val gizmo = spyk<Gizmo>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        val renderManager = renderManager(display(gizmo))

        verifyChildAddedProperly(renderManager, gizmo)

        gizmo.size *= 0.0

        verify(exactly = 1) { gizmo.render(any()) }
    }

    @Test @JsName("doesNotRerenderOnPositionChanged")
    fun `does not rerender on position changed`() {
        val gizmo = spyk<Gizmo>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        val renderManager = renderManager(display(gizmo))

        verifyChildAddedProperly(renderManager, gizmo)

        gizmo.x *= 2.0

        verify(exactly = 1) { gizmo.render(any()) }
    }

    @Test @JsName("doesNotRerenderOnSizeZeroed")
    fun `does not rerender on size zeroed`() {
        val gizmo = spyk<Gizmo>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        val renderManager = renderManager(display(gizmo))

        verifyChildAddedProperly(renderManager, gizmo)

        gizmo.size = Size.Empty

        verify(exactly = 1) { gizmo.render(any()) }
    }

    @Test @JsName("rendersNewNestedGizmos")
    fun `renders new nested gizmos`() {
        val container = container()
        val child = spyk(gizmo())

        val display = display(container)

        val renderManager = renderManager(display)

        container.children += gizmo()
        container.children += gizmo()

        verify(exactly = 0) { child.render(any()) }

        container.children += child

        verifyChildAddedProperly(renderManager, child)
    }

    @Test @JsName("rendersNewNestedGizmoInserted")
    fun `renders new nested gizmo inserted`() {
        val container = container()
        val child     = spyk(gizmo())
        val display   = display(container)
        val surface   = mockk<GraphicsSurface>(relaxed = true)
        val device    = graphicsDevice(mapOf(child to surface))

        val renderManager = renderManager(display, graphicsDevice = device)

        container.children += gizmo()
        container.children += gizmo()

        verify(exactly = 0) { child.render(any()) }

        container.children.add(1, child)

        verifyChildAddedProperly(renderManager, child)
    }

    @Test @JsName("doesNotRenderInvisibleGizmos")
    fun `does not render invisible gizmos`() = doesNotRender(spyk(gizmo()).apply { visible = false })

    @Test @JsName("doesNotRenderZeroBoundsGizmos")
    fun `does not render zero bounds gizmos`() = doesNotRender(spyk(gizmo()).apply { bounds = Rectangle.Empty })

    @Test @JsName("renderNowIgnoresUnknownGizmos")
    fun `renderNow ignores unknown gizmos`() {
        val gizmo = spyk(gizmo())

        renderManager().renderNow(gizmo)

        verify(exactly = 0) { gizmo.render(any()) }
    }

    @Test @JsName("renderNowWorks")
    fun `renderNow works`() {
        val gizmo = spyk(gizmo())

        renderManager(display(gizmo)).also {
            verify(exactly = 1) { gizmo.render(any()) }

            it.renderNow(gizmo)

            verify(exactly = 2) { gizmo.render(any()) }
        }
    }

    @Test @JsName("revalidatesParentWhenNewGizmos")
    fun `revalidates parent out when new gizmos`() {
        val container = spyk<Box>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val child     = gizmo()

        val display = display(container)

        renderManager(display)

        verify(exactly = 0) { container.revalidate_() }

        container.children += child

        verify(exactly = 1) { container.revalidate_() }
    }

    @Test @JsName("laysOutParentOnSizeChanged")
    fun `lays out parent on size changed`() = verifyLayout { it.size *= 2.0 }

    @Test @JsName("laysOutParentOnPositionChanged")
    fun `lays out parent on position changed`() = verifyLayout { it.x += 2.0 }

    @Test @JsName("laysOutParentOnVisibilityChanged")
    fun `lays out parent on visibility changed`() = verifyLayout { it.visible = false }

    private fun testDisplayZIndex(block: (Display, Gizmo) -> Unit) {
        val container1 = spyk<Box>().apply { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(gizmo()).apply { children += spyk(gizmo()) } }
        val container2 = spyk<Box>().apply { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(gizmo()).apply { children += spyk(gizmo()) } }
        val display    = display(container1, container2)
        val surface1   = mockk<GraphicsSurface>(relaxed = true)
        val surface2   = mockk<GraphicsSurface>(relaxed = true)

        val renderManager = renderManager(display = display, graphicsDevice = graphicsDevice(mapOf(container1 to surface1, container2 to surface2)))

        listOf(container1, container2).forEach {
            verifyChildAddedProperly(renderManager, it)
        }

        block(display, container2)

        verify(exactly = 1) { surface2.zIndex = 0 }

        listOf(container1, container2).forEach {
            verify(exactly = 0) { it.removedFromDisplay_(     ) }
            verify(exactly = 1) { it.render             (any()) }
            verify(exactly = 1) { it.doLayout_          (     ) }
        }
    }

    private fun verifyLayout(block: (Gizmo) -> Unit) {
        val container = spyk<Box>("xyz").apply { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val child     = gizmo()

        container.layout    = mockk(relaxed = true)
        container.children += child

        renderManager(display(container))

        verify(exactly = 1) { container.doLayout_() }

        block(child)

        verify(exactly = 2) { container.doLayout_() }
    }

    private fun verifyChildAddedProperly(renderManager: RenderManager, gizmo: Gizmo) {
        verify(exactly = 1) { gizmo.addedToDisplay(renderManager) }
        verify(exactly = 1) { gizmo.render        (any()        ) }
    }

    private fun verifyChildRemovedProperly(gizmo: Gizmo) {
        verify(exactly = 1) { gizmo.removedFromDisplay_() }

        gizmo.children_.forEach { verifyChildRemovedProperly(it) }
    }

    private fun gizmo(): Gizmo = object: Gizmo() {}.apply { bounds = Rectangle(size = Size(10.0, 10.0)) }
    private fun container(): Box = Box().apply { bounds = Rectangle(size = Size(10.0, 10.0)) }

    private fun doesNotRender(gizmo: Gizmo) {
        renderManager(display(gizmo))

        verify(exactly = 0) { gizmo.render(any()) }
    }

    private fun renderManager(
            display       : Display              = mockk(relaxed = true),
            timer         : Timer                = timer(),
            themeManager  : InternalThemeManager = mockk(relaxed = true),
            scheduler     : AnimationScheduler   = instantScheduler,
            graphicsDevice: GraphicsDevice<*>    = defaultGraphicsDevice) = RenderManagerImpl(timer, display, scheduler, themeManager, graphicsDevice)

    private val defaultGraphicsDevice by lazy {
        graphicsDevice()
    }

    private fun timer(): Timer = mockk<Timer>(relaxed = true).apply {
        every { now } returns 0.milliseconds
    }

    private fun graphicsDevice(mapping: Map<Gizmo, GraphicsSurface> = mapOf()): GraphicsDevice<*> {
        val result         = mockk<GraphicsDevice<*>>(relaxed = true)
        val defaultSurface = mockk<GraphicsSurface>(relaxed = true)
        val canvas         = mockk<Canvas>(relaxed = true)

        every { defaultSurface.render(captureLambda()) } answers {
            lambda<(Canvas) -> Unit>().captured(canvas)
        }

        mapping.forEach {
            every { it.value.render(captureLambda()) } answers {
                lambda<(Canvas) -> Unit>().captured(canvas)
            }
        }

        every { result[any()] } answers { mapping[firstArg()] ?: defaultSurface }

        return result
    }

    private fun display(vararg children: Gizmo): Display = mockk<Display>(relaxed = true).apply {
        val displayChildren = ObservableList<Display, Gizmo>(this)

        displayChildren.addAll(children)

        val gizmo = slot<Gizmo>()
        val to    = slot<Int>  ()

        every { this@apply.children                   } returns displayChildren
        every { this@apply.iterator()                 } answers { displayChildren.iterator() }

        // FIXME: compiler fails to build w/o hint
        every { sizeChanged as Pool<PropertyObserver<Display, Size>>                          } returns mockk(relaxed = true)
        every { this@apply.ancestorOf(capture(gizmo)) } answers {
            var result = false

            if (this@apply.children.isNotEmpty()) {
                var parent: Gizmo? = gizmo.captured

                do {
                    if (parent in this@apply.children) {
                        result = true
                        break
                    }

                    parent = parent?.parent
                } while (parent != null)
            }

            result
        }
        every { this@apply.setZIndex(capture(gizmo), capture(to)) } answers { displayChildren.move(gizmo.captured, to.captured) }
    }

    private val instantScheduler by lazy { mockk<AnimationScheduler>(relaxed = true).apply {
        every { this@apply.onNextFrame(captureLambda()) } answers {
            lambda<(Measure<Time>) -> Unit>().captured(0.milliseconds)

            val task = mockk<Task>()

            every { task.completed } returns true

            task
        }
    }}

    private class ManualAnimationScheduler: AnimationScheduler {
        private class SimpleTask(override var completed: Boolean = false) : Task {
            override fun cancel() {
                completed = true
            }
        }

        val tasks = mutableListOf<Pair<SimpleTask, (Measure<Time>) -> Unit>>()

        fun runJobs() = tasks.forEach {
            it.first.completed = true
            it.second(0.milliseconds)
        }

        override fun onNextFrame(job: (Measure<Time>) -> Unit): Task {
            val task = SimpleTask()

            tasks += task to job

            return task
        }
    }

}