@file:Suppress("FunctionName")

package com.nectar.doodle.drawing.impl

import com.nectar.doodle.JsName
import com.nectar.doodle.core.Box
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
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
import com.nectar.doodle.utils.ListObserver
import com.nectar.doodle.utils.ObservableList
import com.nectar.doodle.utils.Pool
import com.nectar.doodle.utils.PropertyObserver
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.milliseconds
import com.nectar.measured.units.times
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
        val view      = spyk(view())
        val scheduler = ManualAnimationScheduler()

        view.visible = false

        val renderManager = renderManager(display(view), scheduler = scheduler)

        view.visible  = true
        view.size    *= 2.0

        verify(exactly = 0) { view.render(any()) }

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, view)
    }

    @Test @JsName("laysOutDisplayOnSizeChange")
    fun `lays out display on size change`() {

        val display = display(view())

        val slot = slot<PropertyObserver<Display, Size>>()

        every { display.sizeChanged.plusAssign(capture(slot)) } just Runs

        renderManager(display)

        verify(exactly = 0) { display.doLayout() }

        slot.captured(display, display.size, display.size * 2.0)

        verify(exactly = 1) { display.doLayout() }
    }

    @Test @JsName("laysOutDisplayOnNewChild")
    fun `lays out display on new child`() {

        val display = display(view())

        val slot = slot<ListObserver<View>>()

        every { display.children.changed.plusAssign(capture(slot)) } just Runs

        renderManager(display)

        verify(exactly = 0) { display.doLayout() }

        slot.captured(display.children, emptyMap(), mapOf(1 to view()), emptyMap())

        verify(exactly = 1) { display.doLayout() }
    }

    @Test @JsName("laysOutDisplayOnChildBoundsChange")
    fun `lays out display on child bounds change`() {
        val child = spyk<View>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        val display = display(child)

        renderManager(display)

        verify(exactly = 0) { display.doLayout() }

        child.width += 10

        verify(exactly = 1) { display.doLayout() }
    }

    @Test @JsName("renderIgnoresUnknownViews")
    fun `render ignores unknown views`() {
        val view = spyk(view())

        renderManager().render(view)

        verify(exactly = 0) { view.render(any()) }
    }

    @Test @JsName("rendersDisplayedViews")
    fun `renders displayed views`() {
        val views = (0 until 2).mapTo(mutableListOf()) { spyk(view()) }

        val renderManager = renderManager(display(*views.toTypedArray()))

        views.forEach {
            verifyChildAddedProperly(renderManager, it)
        }
    }

    @Test @JsName("rendersNewViews")
    fun `renders new views`() {
        val child = spyk(view())

        val display = display()

        val renderManager = renderManager(display)

        verify(exactly = 0) { child.render(any()) }

        display.children += child

        verifyChildAddedProperly(renderManager, child)
    }

    @Test @JsName("removesTopLevelViews")
    fun `removes top-level views`() {
        val container = spyk<Box>().apply { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(view()).apply { children += spyk(view()) } }

        val display   = display(container)
        val scheduler = ManualAnimationScheduler()

        val renderManager = renderManager(display, scheduler = scheduler)

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, container)

        display.children.remove(container)

        scheduler.runJobs()

        verifyChildRemovedProperly(container)
    }

    @Test @JsName("noopRemoveAddTopLevelViews")
    fun `no-op remove, add top-level views`() {
        val container1 = spyk<Box>().apply { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(view()).apply { children += spyk(view()) } }
        val container2 = spyk<Box>().apply { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(view()).apply { children += spyk(view()) } }
        val display    = display(container1, container2)
        val surface1   = mockk<GraphicsSurface>(relaxed = true)
        val surface2   = mockk<GraphicsSurface>(relaxed = true)

        val renderManager = renderManager(display = display, graphicsDevice = graphicsDevice(mapOf(container1 to surface1, container2 to surface2)))

        listOf(container1, container2).forEach {
            verifyChildAddedProperly(renderManager, it)
        }

        display.children.move(container2, 0)

        verify(exactly = 1) { surface2.index = 0 }

        listOf(container1, container2).forEach {
            verify(exactly = 0) { it.removedFromDisplay_(     ) }
            verify(exactly = 1) { it.render             (any()) }
            verify(exactly = 1) { it.doLayout_          (     ) }
        }
    }

    @Test @JsName("zOrderChangeTopLevelViews")
    fun `z-order change top-level views`() {
        val container1 = spyk<Box>().apply { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(view()).apply { children += spyk(view()) } }
        val container2 = spyk<Box>().apply { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(view()).apply { children += spyk(view()) } }
        val display    = display(container1, container2)
        val surface1   = mockk<GraphicsSurface>(relaxed = true)
        val surface2   = mockk<GraphicsSurface>(relaxed = true)

        val renderManager = renderManager(display = display, graphicsDevice = graphicsDevice(mapOf(container1 to surface1, container2 to surface2)))

        listOf(container1, container2).forEach {
            verifyChildAddedProperly(renderManager, it)
        }

        container2.zOrder = 3

        verify(exactly = 1) { surface2.zOrder = 3 }

        listOf(container1, container2).forEach {
            verify(exactly = 0) { it.removedFromDisplay_(     ) }
            verify(exactly = 1) { it.render             (any()) }
            verify(exactly = 1) { it.doLayout_          (     ) }
        }
    }

    @Test @JsName("removesNestedViews")
    fun `removes nested views`() {
        val container = spyk<Box>().apply { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(view()).apply { children += spyk(view()) } }

        val display   = display(container)
        val scheduler = ManualAnimationScheduler()

        val renderManager = renderManager(display, scheduler = scheduler)

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, container)

        val firstChild = container.children.first()
        container.children -= firstChild

        scheduler.runJobs()

        verifyChildRemovedProperly(firstChild)
    }

    @Test @JsName("rerendersOnBoundsChanged")
    fun `rerenders on bounds changed`() {
        val view = spyk<View>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        val renderManager = renderManager(display(view))

        verifyChildAddedProperly(renderManager, view)

        view.size *= 2.0

        verify(exactly = 2) { view.render(any()) }
    }

    @Test @JsName("rerendersOnBecomingVisible")
    fun `rerenders on becoming visible`() {
        val view = spyk<View>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        view.visible = false

        val renderManager = renderManager(display(view))

        verify(exactly = 1) { view.addedToDisplay(renderManager) }
        verify(exactly = 0) { view.render        (any()        ) }

        view.visible = true

        verifyChildAddedProperly(renderManager, view)
    }

    @Test @JsName("rerendersOnAddedBecomingVisible")
    fun `rerenders on added becoming visible`() {
        val parent = container()
        val view  = spyk<View>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        view.visible = false

        val renderManager = renderManager(display(parent))

        verify(exactly = 0) { view.addedToDisplay(renderManager) }
        verify(exactly = 0) { view.render        (any()        ) }

        parent.children_ += view

        view.visible = true

        verifyChildAddedProperly(renderManager, view)
    }

    @Test @JsName("doesNotRerenderOnBoundsZeroed")
    fun `does not rerender on bounds zeroed`() {
        val view = spyk<View>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        val renderManager = renderManager(display(view))

        verifyChildAddedProperly(renderManager, view)

        view.size *= 0.0

        verify(exactly = 1) { view.render(any()) }
    }

    @Test @JsName("doesNotRerenderOnPositionChanged")
    fun `does not rerender on position changed`() {
        val view = spyk<View>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        val renderManager = renderManager(display(view))

        verifyChildAddedProperly(renderManager, view)

        view.x *= 2.0

        verify(exactly = 1) { view.render(any()) }
    }

    @Test @JsName("doesNotRerenderOnSizeZeroed")
    fun `does not rerender on size zeroed`() {
        val view = spyk<View>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        val renderManager = renderManager(display(view))

        verifyChildAddedProperly(renderManager, view)

        view.size = Size.Empty

        verify(exactly = 1) { view.render(any()) }
    }

    @Test @JsName("rendersNewNestedViews")
    fun `renders new nested views`() {
        val container = container()
        val child = spyk(view())

        val display = display(container)

        val renderManager = renderManager(display)

        container.children += view()
        container.children += view()

        verify(exactly = 0) { child.render(any()) }

        container.children += child

        verifyChildAddedProperly(renderManager, child)
    }

    @Test @JsName("rendersNewNestedViewInserted")
    fun `renders new nested view inserted`() {
        val container = container()
        val child     = spyk(view())
        val display   = display(container)
        val surface   = mockk<GraphicsSurface>(relaxed = true)
        val device    = graphicsDevice(mapOf(child to surface))

        val renderManager = renderManager(display, graphicsDevice = device)

        container.children += view()
        container.children += view()

        verify(exactly = 0) { child.render(any()) }

        container.children.add(1, child)

        verifyChildAddedProperly(renderManager, child)
    }

    @Test @JsName("doesNotRenderInvisibleViews")
    fun `does not render invisible views`() = doesNotRender(spyk(view()).apply { visible = false })

    @Test @JsName("doesNotRenderInvisibleChildren")
    fun `does not render invisible children`() = doesNotRenderChild(spyk(view()).apply { visible = false })

    @Test @JsName("doesNotRenderZeroBoundsViews")
    fun `does not render zero bounds views`() = doesNotRender(spyk(view()).apply { bounds = Rectangle.Empty })

    @Test @JsName("renderNowIgnoresUnknownViews")
    fun `renderNow ignores unknown views`() {
        val view = spyk(view())

        renderManager().renderNow(view)

        verify(exactly = 0) { view.render(any()) }
    }

    @Test @JsName("renderNowWorks")
    fun `renderNow works`() {
        val view = spyk(view())

        renderManager(display(view)).also {
            verify(exactly = 1) { view.render(any()) }

            it.renderNow(view)

            verify(exactly = 2) { view.render(any()) }
        }
    }

    @Test @JsName("revalidatesParentWhenNewViews")
    fun `revalidates parent out when new views`() {
        val container = spyk<Box>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val child     = view()

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

    @Test @JsName("reflectsVisibilityChange")
    fun `reflects visibility change`() {
        val container = spyk<Box>("xyz").apply { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val child     = spyk<View>().apply { bounds = Rectangle(size = Size(10.0, 10.0)) }

        container.children += child

        val parentSurface  = mockk<GraphicsSurface>  (relaxed = true)
        val childSurface   = mockk<GraphicsSurface>  (relaxed = true)
        val graphicsDevice = mockk<GraphicsDevice<*>>(relaxed = true).apply {
            every { get(container) } returns parentSurface
            every { get(child    ) } returns childSurface
        }

        renderManager(display(container), graphicsDevice = graphicsDevice)

        child.visible = false

        verify(exactly = 1) { childSurface.visible = false }
        verify(exactly = 0) { childSurface.visible = true  }

        child.visible = true

        verify(exactly = 1) { childSurface.visible = true }
    }

    @Test @JsName("installsThemeForDisplayedViews")
    fun `installs theme for displayed views`() {
        val container = spyk<Box>().apply { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val child     = view()

        container.children += child

        val display      = display(container)
        val themeManager = mockk<InternalThemeManager>(relaxed = true)

        renderManager(display, themeManager = themeManager)

        verify(exactly = 1) { themeManager.update(container) }
        verify(exactly = 1) { themeManager.update(child    ) }
    }

    private fun verifyLayout(block: (View) -> Unit) {
        val container = spyk<Box>("xyz").apply { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val child     = view()

        container.layout    = mockk(relaxed = true)
        container.children += child

        renderManager(display(container))

        verify(exactly = 1) { container.doLayout_() }

        block(child)

        verify(exactly = 2) { container.doLayout_() }
    }

    private fun verifyChildAddedProperly(renderManager: RenderManager, view: View) {
        verify(exactly = 1) { view.addedToDisplay(renderManager) }
        verify(exactly = 1) { view.render        (any()        ) }
    }

    private fun verifyChildRemovedProperly(view: View) {
        verify(exactly = 1) { view.removedFromDisplay_() }

        view.children_.forEach { verifyChildRemovedProperly(it) }
    }

    private fun view(): View = object: View() {}.apply { bounds = Rectangle(size = Size(10.0, 10.0)) }
    private fun container(): Box = Box().apply { bounds = Rectangle(size = Size(10.0, 10.0)) }

    private fun doesNotRender(view: View) {
        renderManager(display(view))

        verify(exactly = 0) { view.render(any()) }
    }

    private fun doesNotRenderChild(view: View) {
        val container = spyk<Box>("xyz").apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        container.children += view

        renderManager(display(container))

        verify(exactly = 0) { view.render(any()) }
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
        every { now } returns 0 * milliseconds
    }

    private fun graphicsDevice(mapping: Map<View, GraphicsSurface> = mapOf()): GraphicsDevice<*> {
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

    private fun display(vararg children: View): Display = mockk<Display>(relaxed = true).apply {
        val displayChildren = ObservableList<View>()

        displayChildren.addAll(children)

        val view = slot<View>()

        every { this@apply.children   } returns displayChildren
        every { this@apply.iterator() } answers { displayChildren.iterator() }

        // FIXME: compiler fails to build w/o hint
        every { sizeChanged as Pool<PropertyObserver<Display, Size>> } returns mockk(relaxed = true)
        every { this@apply.ancestorOf(capture(view)) } answers {
            var result = false

            if (this@apply.children.isNotEmpty()) {
                var parent: View? = view.captured

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
    }

    private val instantScheduler by lazy { mockk<AnimationScheduler>(relaxed = true).apply {
        every { this@apply.onNextFrame(captureLambda()) } answers {
            lambda<(Measure<Time>) -> Unit>().captured(0 * milliseconds)

            val task = mockk<Task>()

            every { task.completed } returns true

            task
        }
    }}

//    private val instantStrand by lazy { mockk<Strand>(relaxed = true).apply {
//        val iterable = slot<Iterable<() -> Unit>>()
//
//        every { this@apply.invoke(capture(iterable)) } answers {
//            iterable.captured.forEach {
//                it()
//            }
//
//            val task = mockk<Task>()
//
//            every { task.completed } returns true
//
//            task
//        }
//
//        val sequence = slot<Sequence<() -> Unit>>()
//
//        every { this@apply.invoke(capture(sequence)) } answers { this@apply(sequence.captured.asIterable()) }
//    } }

    private class SimpleTask(override var completed: Boolean = false) : Task {
        override fun cancel() {
            completed = true
        }
    }

    private class ManualAnimationScheduler: AnimationScheduler {
        val tasks = mutableListOf<Pair<SimpleTask, (Measure<Time>) -> Unit>>()

        fun runJobs() {
            tasks.forEach {
                it.first.completed = true
                it.second(0 * milliseconds)
            }
        }

        override fun onNextFrame(job: (Measure<Time>) -> Unit): Task {
            val task = SimpleTask()

            tasks += task to job

            return task
        }
    }

//    private class ManualStrand: Strand {
//        val jobs = mutableListOf<Pair<SimpleTask, Iterable<() -> Unit>>>()
//
//        fun runJobs() {
//            jobs.forEach {
//                it.first.completed = true
//                it.second.forEach { it() }
//            }
//        }
//
//        override operator fun invoke(jobs: Sequence<() -> Unit>): Task = invoke(jobs.asIterable())
//
//        override operator fun invoke(jobs: Iterable<() -> Unit>): Task {
//            val task = SimpleTask()
//
//            this.jobs += task to jobs
//
//            return task
//        }
//    }
}