@file:Suppress("FunctionName")

package io.nacular.doodle.drawing.impl

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import io.nacular.doodle.accessibility.AccessibilityManager
import io.nacular.doodle.core.ChildObserver
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.view
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.GraphicsSurface
import io.nacular.doodle.drawing.RenderManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.times
import io.nacular.doodle.scheduler.AnimationScheduler
import io.nacular.doodle.scheduler.Task
import io.nacular.doodle.theme.InternalThemeManager
import io.nacular.doodle.utils.ChangeObserver
import io.nacular.doodle.utils.ListObserver
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.PropertyObserver
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.diff.Differences
import io.nacular.doodle.utils.diff.Equal
import io.nacular.doodle.utils.diff.Insert
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.times
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 11/6/17.
 */
class RenderManagerImplTests {
    // TODO: Add tests to make sure things in cleanup list never get rendered

    @Test fun `renders are batched`() {
        val view      = spyk<View> { bounds = Rectangle(size = Size(100, 100)) }
        val scheduler = ManualAnimationScheduler()
        val display   = display(view)

        view.visible = false

        val renderManager = renderManager(display, scheduler = scheduler)

        view.visible  = true
        view.size    *= 2.0

        verify(exactly = 0) { view.render(any()) }

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, view)
    }

    @Test fun `lays out display on size change`() {

        val display = display(view())

        val slot = slot<PropertyObserver<Display, Size>>()

        every { display.sizeChanged.plusAssign(capture(slot)) } just Runs

        renderManager(display)

        verify(exactly = 0) { display.relayout() }

        slot.captured(display, display.size, display.size * 2.0)

        verify(exactly = 1) { display.relayout() }
    }

    @Test fun `lays out display on new child`() {

        val display = display(view())

        val slot = slot<ListObserver<Display, View>>()

        every { display.childrenChanged.plusAssign(capture(slot)) } just Runs

        renderManager(display)

        verify(exactly = 0) { display.relayout() }

        slot.captured(display, Differences(listOf(Equal(display.children), Insert(listOf(view())))))

        verify(exactly = 1) { display.relayout() }
    }

    @Ignore @Test fun `lays out display on child bounds change`() {
        val child = spyk<View> { bounds = Rectangle(size = Size(100.0, 100.0)) }

        val display = display(child)

        renderManager(display)

        verify(exactly = 0) { display.relayout() }

        child.width += 10

        verify(exactly = 1) { display.relayout() }
    }

    @Test fun `render ignores unknown views`() {
        val view = spyk(view())

        renderManager().first.render(view)

        verify(exactly = 0) { view.render(any()) }
    }

    @Test fun `renders displayed views`() {
        val views         = (0 until 2).mapTo(mutableListOf()) { spyk(view()) }
        val display       = display(*views.toTypedArray())
        val renderManager = renderManager(display)

        views.forEach {
            verifyChildAddedProperly(renderManager, display, it)
        }
    }

    @Test fun `renders new views`() {
        val child = spyk(view())

        val display        = display()
        val surface        = mockk<GraphicsSurface>()
        val graphicsDevice = graphicsDevice(mapOf(child to surface))

        val renderManager = renderManager(display, graphicsDevice = graphicsDevice)

        verify(exactly = 0) { child.render(any()) }

        display += child

        verifyChildAddedProperly(renderManager, display, child)

        val bounds    = child.bounds
        val transform = child.transform

        verify(exactly = 1) { surface setProperty "bounds"    value bounds    }
        verify(exactly = 1) { surface setProperty "transform" value transform }
    }

    @Test fun `renders views moved to new top-level parent`() {
        val parent1  = container()
        val parent2  = container()
        val children = (0 .. 3).map { spyk(view()).apply { bounds = Rectangle(10, 15) } }

        val display        = display()
        val surfaces       = children.map { mockk<GraphicsSurface>() }
        val graphicsDevice = graphicsDevice(children.mapIndexed { index, view -> view to surfaces[index] }.toMap())

        val renderManager = renderManager(display, graphicsDevice = graphicsDevice)

        parent1.children += children

        display += parent1

        children.forEachIndexed { index, it ->
            verifyChildAddedProperly(renderManager, display, it)
            verify(exactly = 1) { surfaces[index] setProperty "bounds" value Rectangle(10, 15) }
        }

        parent2.children += children

        children.forEach {
            verifyChildRemovedProperly(it)
        }

        display.children[0] = parent2

        children.forEachIndexed { index, it ->
            verifyChildAddedProperly(renderManager, display, it, times = 2)
            verify(exactly = 2) { surfaces[index] setProperty "bounds" value Rectangle(10, 15) }
        }
    }

    @Test fun `renders views moved to new nested parent`() {
        val grandParent = container()
        val parent1  = container()
        val parent2  = container()
        val children = (0 .. 3).map { spyk(view()).apply { bounds = Rectangle(10, 15) } }

        val display        = display(grandParent)
        val surfaces       = children.map { mockk<GraphicsSurface>() }
        val graphicsDevice = graphicsDevice(children.mapIndexed { index, view -> view to surfaces[index] }.toMap())

        val scheduler     = ManualAnimationScheduler()
        val renderManager = renderManager(display, graphicsDevice = graphicsDevice, scheduler = scheduler)

        parent1.children += children

        grandParent.children += parent1

        scheduler.runJobs()

        children.forEachIndexed { index, it ->
            verifyChildAddedProperly(renderManager, display, it)
            verify(exactly = 1) { surfaces[index] setProperty "bounds" value Rectangle(10, 15) }
        }

        parent2.children += children
        grandParent.children[0] = parent2

        scheduler.runJobs()

        children.forEach {
            verifyChildRemovedProperly(it)
        }

        children.forEachIndexed { index, it ->
            verifyChildAddedProperly(renderManager, display, it, times = 2)
            verify(exactly = 2) { surfaces[index] setProperty "bounds" value Rectangle(10, 15) }
        }

        parent1.children += children
        grandParent.children[0] = parent1

        scheduler.runJobs()

        children.forEach {
            verifyChildRemovedProperly(it, times = 2)
        }

        children.forEachIndexed { index, it ->
            verifyChildAddedProperly(renderManager, display, it, times = 3)
            verify(exactly = 3) { surfaces[index] setProperty "bounds" value Rectangle(10, 15) }
        }
    }

    @Test fun `renders new popups`() {
        val child = spyk(view())

        val display        = display()
        val surface        = mockk<GraphicsSurface>()
        val graphicsDevice = graphicsDevice(mapOf(child to surface))

        val renderManager = renderManager(display, graphicsDevice = graphicsDevice)

        verify(exactly = 0) { child.render(any()) }

        renderManager.first.popupShown(child)

        verifyChildAddedProperly(renderManager, display, child)

        val bounds    = child.bounds
        val transform = child.transform

        verify(exactly = 1) { surface setProperty "bounds"    value bounds    }
        verify(exactly = 1) { surface setProperty "transform" value transform }
    }

    @Test fun `handles becoming invisible before first render`() {
        val view      = spyk<View> { bounds = Rectangle(size = Size(100, 100)) }
        val scheduler = ManualAnimationScheduler()
        val display   = display(view)

        view.visible = true

        val renderManager = renderManager(display, scheduler = scheduler)

        view.visible = false

        verify(exactly = 0) { view.render(any()) }

        scheduler.runJobs()

        verify(exactly = 0) { view.render(any()) }

        scheduler.runJobs()

        verify(exactly = 0) { view.render(any()) }

        view.visible = true

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, view)

        scheduler.runJobs()

        verify(exactly = 1) { view.render(any()) }
    }

    @Test fun `removes top-level views`() {
        val container = spyk<Container> { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(view()).apply { children += spyk(view()) } }
        val display   = display(container)
        val scheduler = ManualAnimationScheduler()

        val renderManager = renderManager(display, scheduler = scheduler)

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, container)

        display -= container

        scheduler.runJobs()

        verifyChildRemovedProperly(container)
    }

    @Test fun `removes popups`() {
        val container = spyk<Container> { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(view()).apply { children += spyk(view()) } }
        val display   = display()
        val scheduler = ManualAnimationScheduler()

        val renderManager = renderManager(display, scheduler = scheduler)

        renderManager.first.popupShown(container)

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, container)

        renderManager.first.popupHidden(container)

        scheduler.runJobs()

        verifyChildRemovedProperly(container)
    }

    @Test fun `only updates index view converted to popup`() {
        val container = spyk<Container> { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(view()).apply { children += spyk(view()) } }
        val display   = display(container)
        val scheduler = ManualAnimationScheduler()
        val surface   = mockk<GraphicsSurface>()

        val renderManager = renderManager(display, scheduler = scheduler, graphicsDevice = graphicsDevice(mapOf(container to surface)))

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, container)

        renderManager.first.popupHidden(container)

        // show popup as PopupManager would
        display.hidePopup(container)
        display.showPopup(container)
        renderManager.first.popupShown (container)

        scheduler.runJobs()

        verifyChildRemovedProperly(container)
        verifyChildAddedProperly(renderManager, display, container, times = 2)

        verifyOrder {
            surface.index =  0
            surface.index = -1
        }
    }

    @Test fun `no-op remove, add top-level views`() {
        val container1 = spyk<Container> { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(view()).apply { children += spyk(view()) } }
        val container2 = spyk<Container> { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(view()).apply { children += spyk(view()) } }
        val display    = display(container1, container2)
        val surface1   = mockk<GraphicsSurface>()
        val surface2   = mockk<GraphicsSurface>()

        val renderManager = renderManager(display = display, graphicsDevice = graphicsDevice(mapOf(container1 to surface1, container2 to surface2)))

        listOf(container1, container2).forEach {
            verifyChildAddedProperly(renderManager, display, it)
        }

        display.children.move(container2, 0)

        verify(exactly = 1) { surface2.index = 0 }

        listOf(container1, container2).forEach {
            verify(exactly = 0) { it.removedFromDisplay_(     ) }
            verify(exactly = 1) { it.render             (any()) }
            verify(exactly = 1) { it.doLayout_          (     ) }
        }
    }

    @Test fun `handles child swapped`() {
        val child1   = spyk(view())
        val child2   = spyk(view())
        val child3   = spyk(view())
        val child4   = spyk(view())
        val parent   = spyk<Container> { bounds = Rectangle(size = Size(10.0, 10.0)); children += listOf(child1, child2, child3) }
        val display  = display(parent)
        val surface1 = mockk<GraphicsSurface>()
        val surface2 = mockk<GraphicsSurface>()
        val surface3 = mockk<GraphicsSurface>()
        val surface4 = mockk<GraphicsSurface>()

        val renderManager = renderManager(
            display        = display,
            graphicsDevice = graphicsDevice(mapOf(child1 to surface1, child2 to surface2, child3 to surface3, child4 to surface4))
        )

        verifyChildAddedProperly(renderManager, display, parent)

        parent.children[1] = child4

        verify(exactly = 1) { surface4.index = 1 }

        listOf(child1, child3).forEach {
            verify(exactly = 0) { it.removedFromDisplay_(     ) }
            verify(exactly = 1) { it.render             (any()) }
        }

        verify(exactly = 1) { child2.removedFromDisplay_() }
    }

    @Test fun `handles child swapped top-level`() {
        val child1   = spyk(view())
        val child2   = spyk(view())
        val child3   = spyk(view())
        val child4   = spyk(view())
        val display  = display(child1, child2, child3)
        val surface1 = mockk<GraphicsSurface>()
        val surface2 = mockk<GraphicsSurface>()
        val surface3 = mockk<GraphicsSurface>()
        val surface4 = mockk<GraphicsSurface>()

        val renderManager = renderManager(
            display        = display,
            graphicsDevice = graphicsDevice(mapOf(child1 to surface1, child2 to surface2, child3 to surface3, child4 to surface4))
        )

        listOf(child1, child2, child3).forEach {
            verifyChildAddedProperly(renderManager, display, it)
        }

        display.children[1] = child4

        verify(exactly = 1) { surface4.index = 1 }

        listOf(child1, child3).forEach {
            verify(exactly = 0) { it.removedFromDisplay_(     ) }
            verify(exactly = 1) { it.render             (any()) }
        }

        verify(exactly = 1) { child2.removedFromDisplay_() }
    }

    @Test fun `handles index change`() {
        val child1   = spyk(view())
        val child2   = spyk(view())
        val parent   = spyk<Container> { bounds = Rectangle(size = Size(10.0, 10.0)); children += listOf(child1, child2) }
        val display  = display(parent)
        val surface1 = mockk<GraphicsSurface>()
        val surface2 = mockk<GraphicsSurface>()

        val renderManager = renderManager(display = display, graphicsDevice = graphicsDevice(mapOf(child1 to surface1, child2 to surface2)))

        verifyChildAddedProperly(renderManager, display, parent)

        parent.children.move(child2, 0)

        verify(exactly = 1) { surface2.index = 0 }

        listOf(child1, child2).forEach {
            verify(exactly = 0) { it.removedFromDisplay_(     ) }
            verify(exactly = 1) { it.render             (any()) }
        }
    }

    @Test fun `z-order change top-level views`() {
        val container1 = spyk<Container> { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(view()).apply { children += spyk(view()) } }
        val container2 = spyk<Container> { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(view()).apply { children += spyk(view()) } }
        val display    = display(container1, container2)
        val surface1   = mockk<GraphicsSurface>()
        val surface2   = mockk<GraphicsSurface>()

        val renderManager = renderManager(display = display, graphicsDevice = graphicsDevice(mapOf(container1 to surface1, container2 to surface2)))

        listOf(container1, container2).forEach {
            verifyChildAddedProperly(renderManager, display, it)
        }

        container2.zOrder = 3

        verify(exactly = 1) { surface2.zOrder = 3 }

        listOf(container1, container2).forEach {
            verify(exactly = 0) { it.removedFromDisplay_(     ) }
            verify(exactly = 1) { it.render             (any()) }
            verify(exactly = 1) { it.doLayout_          (     ) }
        }
    }

    @Test fun `opacity change handled`() {
        val view    = spyk<View> { bounds = Rectangle(size = Size(10.0, 10.0)) }
        val display = display(view)
        val surface = mockk<GraphicsSurface>()

        val renderManager = renderManager(display = display, graphicsDevice = graphicsDevice(mapOf(view to surface)))

        verifyChildAddedProperly(renderManager, display, view)

        view.opacity = 0.3f

        verify(exactly = 1) { surface.opacity = 0.3f }
    }

    @Test fun `initializes graphics surface`() {
        val view    = spyk<View> { bounds = Rectangle(size = Size(10.0, 10.0)); opacity = 0.56f; zOrder = 4 }
        val display = display(view)
        val surface = mockk<GraphicsSurface>()

        val renderManager = renderManager(display = display, graphicsDevice = graphicsDevice(mapOf(view to surface)))

        verifyChildAddedProperly(renderManager, display, view)

        verify(exactly = 1) { surface.index     = 0        }
        verify(exactly = 1) { surface.zOrder    = 4        }
        verify(exactly = 1) { surface.opacity   = 0.56f    }
        verify(exactly = 1) { surface.transform = Identity }
    }

    @Test fun `initializes popup graphics surface`() {
        val view    = spyk<View> { bounds = Rectangle(size = Size(10.0, 10.0)); opacity = 0.56f; zOrder = 4 }
        val display = display()
        val surface = mockk<GraphicsSurface>()

        val renderManager = renderManager(display = display, graphicsDevice = graphicsDevice(mapOf(view to surface)))

        renderManager.first.popupShown(view)

        verifyChildAddedProperly(renderManager, display, view)

        verify(exactly = 1) { surface.index     = -1       }
        verify(exactly = 0) { surface.zOrder    = any()    }
        verify(exactly = 1) { surface.opacity   = 0.56f    }
        verify(exactly = 1) { surface.transform = Identity }
    }

    @Test fun `removes nested views`() {
        val container = spyk<Container> { bounds = Rectangle(size = Size(10.0, 10.0)); children += spyk(view()).apply { children += spyk(view()) } }

        val display   = display(container)
        val scheduler = ManualAnimationScheduler()

        val renderManager = renderManager(display, scheduler = scheduler)

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, container)

        val firstChild = container.children.first()
        container -= firstChild

        scheduler.runJobs()

        verifyChildRemovedProperly(firstChild)
    }

    @Test fun `moves nested views to parent`() {
        val parent = spyk<Container> {
            bounds    = Rectangle(size = Size(10.0, 10.0))
            children += spyk(container()).apply {
                children += spyk(view())
            }
        }

        val display       = display(parent)
        val scheduler     = ManualAnimationScheduler()
        val renderManager = renderManager(display, scheduler = scheduler)

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, parent)

        val child      = parent.children.first()
        val grandChild = child.children_.first()

        parent -= child
        parent += grandChild

        scheduler.runJobs()

        verifyChildRemovedProperly(child)

        expect(1   ) { parent.children.size          }
        expect(true) { grandChild in parent.children }
    }

    @Test fun `rerenders on bounds changed`() {
        val view          = spyk<View> { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val display       = display(view)
        val renderManager = renderManager(display)

        verifyChildAddedProperly(renderManager, display, view)

        view.size *= 2.0

        verify(exactly = 2) { view.render(any()) }
    }

    @Test fun `rerenders on becoming visible`() {
        val view    = spyk<View> { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val display = display(view)

        view.visible = false

        val renderManager = renderManager(display)

        verifyChildAddedProperly(renderManager, display, view, 0)

        view.visible = true

        verifyChildAddedProperly(renderManager, display, view)
    }

    @Test fun `rerenders on added becoming visible`() {
        val parent  = container()
        val view    = spyk<View> { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val display = display(parent)

        view.visible = false

        val renderManager = renderManager(display)

        verify(exactly = 0) { view.addedToDisplay(display, renderManager.first, any()) }
        verify(exactly = 0) { view.render        (any()                              ) }

        parent.children_ += view

        view.visible = true

        verifyChildAddedProperly(renderManager, display, view)
    }

    @Test fun `addedToDisplay called in correct order`() {
        val child       = spyk<View>      { bounds = Rectangle(size = Size(100, 100))                     }
        val parent      = spyk<Container> { bounds = Rectangle(size = Size(100, 100)); children += child  }
        val grandParent = spyk<Container> { bounds = Rectangle(size = Size( 10,  10)); children += parent }
        val display     = display(grandParent)

        val renderManager = renderManager(display)

        verifyOrder {
            grandParent.addedToDisplay(display, renderManager.first, any())
            parent.addedToDisplay     (display, renderManager.first, any())
            child.addedToDisplay      (display, renderManager.first, any())

            grandParent.render(any())
            parent.render(any())
            child.render(any())
        }
    }

    @Test fun `does not rerender on bounds zeroed`() {
        val view          = spyk<View> { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val display       = display(view)
        val renderManager = renderManager(display)

        verifyChildAddedProperly(renderManager, display, view)

        view.size *= 0.0

        verify(exactly = 1) { view.render(any()) }
    }

    @Test fun `does not rerender on position changed`() {
        val view          = spyk<View> { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val display       = display(view)
        val renderManager = renderManager(display)

        verifyChildAddedProperly(renderManager, display, view)

        view.x *= 2.0

        verify(exactly = 1) { view.render(any()) }
    }

    @Test fun `does not re-render on size zeroed`() {
        val view          = spyk<View> { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val display       = display(view)
        val renderManager = renderManager(display)

        verifyChildAddedProperly(renderManager, display, view)

        view.size = Size.Empty

        verify(exactly = 1) { view.render(any()) }
    }

    @Test fun `renders pre-existing nested views`() {
        val container = container()
        val child     = spyk(view())

        container += child

        val display = display(container)

        val renderManager = renderManager(display)

        verifyChildAddedProperly(renderManager, display, child)
    }

    @Test fun `renders re-added nested views`() {
        val container = container()
        val child     = spyk(view())

        container += child

        val display = display(container)

        val renderManager = renderManager(display)

        container -= child
        container += child

        verifyChildAddedProperly(renderManager, display, child, 2)
    }

    @Test fun `renders new nested views`() {
        val container = container()
        val child     = spyk(view())

        val display = display(container)

        val renderManager = renderManager(display)

        container += view()
        container += view()

        verify(exactly = 0) { child.render(any()) }

        container += child

        verifyChildAddedProperly(renderManager, display, child)
    }

    @Test fun `renders new nested view inserted`() {
        val container = container()
        val child     = spyk(view())
        val display   = display(container)
        val surface   = mockk<GraphicsSurface>()
        val device    = graphicsDevice(mapOf(child to surface))

        val renderManager = renderManager(display, graphicsDevice = device)

        container += view()
        container += view()

        verify(exactly = 0) { child.render(any()) }

        container.children.add(1, child)

        verifyChildAddedProperly(renderManager, display, child)
    }

    @Test fun `does not render invisible views`() = doesNotRender(spyk(view()).apply { visible = false })

    @Test fun `does not render invisible children`() = doesNotRenderChild(spyk(view()).apply { visible = false })

    @Test fun `does not render zero bounds views`() = doesNotRender(spyk(view()).apply { bounds = Rectangle.Empty })

    @Test fun `renderNow ignores unknown views`() {
        val view = spyk(view())

        renderManager().first.renderNow(view)

        verify(exactly = 0) { view.render(any()) }
    }

    @Test fun `renderNow ignores views that are not displayed`() {
        val view = spyk(view()) {
            every { displayed } returns false
        }

        renderManager(display(view)).first.renderNow(view)

        verify(exactly = 0) { view.render(any()) }
    }

    @Test fun `renderNow ignores views with zero size`() {
        val view = spyk(view()) {
            every { size } returns Size.Empty
        }

        renderManager(display(view)).first.renderNow(view)

        verify(exactly = 0) { view.render(any()) }
    }

    @Test fun `renderNow works`() {
        val view = spyk(view())

        renderManager(display(view)).also { (renderManager, _) ->
            verify(exactly = 1) { view.render(any()) }

            renderManager.renderNow(view)

            verify(exactly = 2) { view.render(any()) }
        }
    }

    @Test fun `revalidates parent out when new views`() {
        val container = spyk<Container> { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val child     = view()

        val display = display(container)

        renderManager(display)

        verify(exactly = 0) { container.revalidate_() }

        container += child

        verify(exactly = 1) { container.revalidate_() }
    }

    @Ignore @Test fun `lays out parent on size changed`() = verifyLayout { it.size *= 2.0 }

    @Test fun `does not lay out parent on size changed when ignored`() = verifyLayout(layout(ignoreChildBounds = true), count = 1) { it.size *= 2.0 }

    @Ignore @Test fun `lays out parent on position changed`() = verifyLayout { it.x += 2.0 }

    @Test fun `does not lay out parent on position changed when ignored`() = verifyLayout(layout(ignoreChildBounds = true), count = 1) { it.x += 2.0 }

    @Test fun `lays out parent on visibility changed`() = verifyLayout { it.visible = false }

    @Test fun `does not lay out parent on ideal-size changed`() = verifyLayout(count = 1) { it.idealSize = Size(100) }

    @Test fun `lays out parent on ideal-size changed when not ignored`() = verifyLayout(layout(ignoreChildIdealSize = false)) { it.idealSize = Size(100) }

    @Test fun `does not lay out parent on min-size changed`() = verifyLayout(count = 1) { it.minimumSize = Size(100) }

    @Test fun `lays out parent on min-size changed when not ignored`() = verifyLayout(layout(ignoreChildMinSize = false)) { it.minimumSize = Size(100) }

    @Test fun `reflects visibility change`() {
        val container = spyk<Container> ("xyz").apply { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val child     = spyk<View>(     ).apply { bounds = Rectangle(size = Size( 10.0,  10.0)) }

        container += child

        val parentSurface  = mockk<GraphicsSurface>  ()
        val childSurface   = mockk<GraphicsSurface>  ()
        val graphicsDevice = mockk<GraphicsDevice<*>> {
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

    @Test fun `reflects transform change`() {
        val child = spyk<View> { bounds = Rectangle(size = Size(10.0, 10.0)) }

        val childSurface   = mockk<GraphicsSurface>()
        val graphicsDevice = graphicsDevice(mapOf(child to childSurface))

        renderManager(display(child), graphicsDevice = graphicsDevice)

        child.transform = Identity.rotate(45 * degrees)

        verify(exactly = 1) { childSurface.transform = Identity.rotate(45 * degrees) }

        child.transform = Identity.rotate(57 * degrees)

        verify(exactly = 1) { childSurface.transform = Identity.rotate(57 * degrees) }
    }

    @Test fun `installs theme for displayed views`() {
        val container = spyk<Container> { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val child     = view()

        container += child

        val display      = display(container)
        val themeManager = mockk<InternalThemeManager>()

        renderManager(display, themeManager = themeManager)

        verify(exactly = 1) { themeManager.update(container) }
        verify(exactly = 1) { themeManager.update(child    ) }
    }

    @Test fun `installs theme for re-added views`() {
        val container = spyk<Container> { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val child     = view()

        container += child

        val display      = display(container)
        val themeManager = mockk<InternalThemeManager>()

        renderManager(display, themeManager = themeManager)

        verify(exactly = 1) { themeManager.update(container) }
        verify(exactly = 1) { themeManager.update(child    ) }

        container -= child
        container += child

        verify(exactly = 2) { themeManager.update(child    ) }
    }

    @Test fun `renders removed when re-added (top-level)`() {
        val child     = spyk(view())
        val display   = display(child)
        val scheduler = ManualAnimationScheduler()

        val renderManager = renderManager(display, scheduler = scheduler)

        scheduler.runJobs()

        display -= child

        // Add happens before next render

        display += child

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, child, 2)
    }

    @Test fun `removes view from old container when moved to display`() {
        val child     = spyk(view())
        val container = container()
        val display   = display(container)
        val scheduler = ManualAnimationScheduler()

        container += child

        val renderManager = renderManager(display, scheduler = scheduler)

        scheduler.runJobs()

        display += child

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, child, 2)

        expect(true) { container.children.isEmpty() }
    }

    @Test fun `removes view from display when moved to container`() {
        val child     = spyk(view())
        val container = container()
        val display   = display(child, container)
        val scheduler = ManualAnimationScheduler()

        val renderManager = renderManager(display, scheduler = scheduler)

        scheduler.runJobs()

        container += child

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, child, 2)

        expect(1) { display.children.size }
    }

    @Test fun `renders moved from parent to display`() {
        val child     = spyk(view())
        val container = container()

        container += child

        val display   = display(container)
        val scheduler = ManualAnimationScheduler()

        val renderManager = renderManager(display, scheduler = scheduler)

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, child)

        // Add happens before next render

        display += child

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, child, 2)
    }

    @Test fun `no-op removed re-added same parent`() {
        val child     = spyk(view())
        val container = container()

        container += child

        val display   = display(container)
        val scheduler = ManualAnimationScheduler()

        val renderManager = renderManager(display, scheduler = scheduler)

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, child)

        container -= child

        // Add happens before next render

        container += child

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, child)
    }

    @Test fun `renders removed re-added different parent`() {
        val child      = spyk(view())
        val container1 = container()
        val container2 = container()

        container1.children += child

        val display   = display()
        val scheduler = ManualAnimationScheduler()

        display += listOf<View>(container1, container2)

        val renderManager = renderManager(display, scheduler = scheduler)

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, child)

        // Add happens before next render

        container2.children += child

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, child, 2)
    }

    @Test fun `renders removed re-added nested different parent`() {
        val child      = spyk(view())
        val container1 = container()
        val container2 = container()

        container1.children += child

        val display   = display(container1)
        val scheduler = ManualAnimationScheduler()

        val renderManager = renderManager(display, scheduler = scheduler)

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, child)

        container2.children += child

        // Add happens before next render

        display -= container1
        display += container2

        scheduler.runJobs()

        verifyChildAddedProperly(renderManager, display, child, 2)
    }

    @Test fun `updates top-level on content direction change`() {
        val child1  = spyk(view())
        val child2  = spyk(view())
        val observer = slot<ChangeObserver<Display>>()

        val display = display(child1, child2).apply {
            every { contentDirectionChanged += capture(observer) } just Runs
        }

        renderManager(display)

        observer.captured(display)

        verify { child1.contentDirectionChanged_() }
        verify { child2.contentDirectionChanged_() }
    }

    @Test fun `updates top-level on mirror change`() {
        val child1  = spyk(view())
        val child2  = spyk(view())
        val observer = slot<ChangeObserver<Display>>()

        val display = display(child1, child2).apply {
            every { mirroringChanged += capture(observer) } just Runs
        }

        renderManager(display)

        observer.captured(display)

        verify { child1.updateNeedsMirror() }
        verify { child2.updateNeedsMirror() }
    }

    @Test fun `notifies top level of display rect change`() {
        val handleDisplayRectEvent = mockk<(Rectangle, Rectangle) -> Unit>()

        // Cannot use spyk since it has issues https://github.com/mockk/mockk/issues/342
        val child = object: View() {
            override fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) {
                handleDisplayRectEvent(old, new)
            }
        }.apply {
            size                = Size(10, 10)
            monitorsDisplayRect = true
        }

        val display   = display(child)
        val scheduler = ManualAnimationScheduler()

        renderManager(display, scheduler = scheduler)

        val oldRect = child.bounds.atOrigin

        child.width = 100.0

        val newRect = child.bounds.atOrigin

        scheduler.runJobs()

        verify { handleDisplayRectEvent(oldRect, newRect) }
    }

    @Test fun `display rect change works when enabled late`() {
        val handleDisplayRectEvent = mockk<(Rectangle, Rectangle) -> Unit>()

        // Cannot use spyk since it has issues https://github.com/mockk/mockk/issues/342
        val child = object: View() {
            override fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) {
                handleDisplayRectEvent(old, new)
            }
        }.apply { size = Size(10, 10) }

        val display   = display(child)
        val scheduler = ManualAnimationScheduler()

        renderManager(display, scheduler = scheduler)

        expect(false) { child.monitorsDisplayRect }

        child.monitorsDisplayRect = true

        expect(true) { child.monitorsDisplayRect }

        val oldRect = child.bounds.atOrigin

        child.width = 100.0

        val newRect = child.bounds.atOrigin

        scheduler.runJobs()

        verify { handleDisplayRectEvent(oldRect, newRect) }
    }

    @Test fun `display rect notification works`() {
        data class Data(
                val grandParent: Rectangle,
                val parent     : Rectangle,
                val child      : Rectangle,
                val event      : Pair<Rectangle, Rectangle>,
                val operation  : (View, View, View) -> Unit
        )

        listOf(
            Data(Rectangle(100, 100), Rectangle(50, 50), Rectangle(10, 10), Rectangle(10, 10) to Rectangle(10, 0, 0, 10)) { _,parent,_ ->
                parent.x = -10.0
            },
            Data(Rectangle(100, 100), Rectangle( 0, 50), Rectangle(10, 10), Rectangle( 0, 10) to Rectangle(10, 10)) { _,parent,_ ->
                parent.width = 1000.0
            }
        ).forEach {
            val handleDisplayRectEvent = mockk<(Rectangle, Rectangle) -> Unit>()

            val child = object: View() {
                override fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) {
                    handleDisplayRectEvent(old, new)
                }
            }.apply {
                size                = Size(10, 10)
                monitorsDisplayRect = true
            }
            val parent        = container().apply { children += child;  bounds = it.parent      }
            val grandParent   = container().apply { children += parent; bounds = it.grandParent }
            val display       = display(grandParent)
            val scheduler     = ManualAnimationScheduler()
            val renderManager = renderManager(display, scheduler = scheduler)

            it.operation(grandParent, parent, child)

            scheduler.runJobs()

            verify { handleDisplayRectEvent(it.event.first, it.event.second) }

            expect(it.event.second) {
                renderManager.first.displayRect(child)
            }
        }
    }

    @Test fun `cold display rect call works`() {
        data class Data(
                val grandParent: Rectangle,
                val parent     : Rectangle,
                val child      : Rectangle,
                val event      : Pair<Rectangle, Rectangle>,
                val operation  : (View, View, View) -> Unit
        )

        listOf(
                Data(Rectangle(100, 100), Rectangle(50, 50), Rectangle(10, 10), Rectangle(10, 10) to Rectangle(10, 0, 0, 10)) { _,parent,_ ->
                    parent.x = -10.0
                },
                Data(Rectangle(100, 100), Rectangle( 0, 50), Rectangle(10, 10), Rectangle( 0, 10) to Rectangle(10, 10)) { _,parent,_ ->
                    parent.width = 1000.0
                }
        ).forEach {
            val child         = view()
            val parent        = container().apply { children += child;  bounds = it.parent      }
            val grandParent   = container().apply { children += parent; bounds = it.grandParent }
            val display       = display(grandParent)
            val renderManager = renderManager(display)

            it.operation(grandParent, parent, child)

            expect(it.event.second) {
                renderManager.first.displayRect(child)
            }
        }
    }

    @Test fun `stops monitoring display rect when disabled`() {
        val child = spyk(view()).apply {
            monitorsDisplayRect = true
        }

        val display   = display(child)
        val scheduler = ManualAnimationScheduler()

        val (renderManager, _) = renderManager(display, scheduler = scheduler)

        renderManager.displayRectHandlingChanged(child, old = true, new = false)

        child.bounds = Rectangle(100, 100)

        scheduler.runJobs()

        verify(exactly = 1) {
            child.handleDisplayRectEvent_(Rectangle.Empty, Rectangle( 10,  10))
        }
    }

    private fun verifyLayout(layout: Layout = layout(), count: Int = 2, block: (View) -> Unit) {
        val container = spyk<Container>("xyz").apply { bounds = Rectangle(size = Size(100.0, 100.0)) }
        val child     = view()

        container.layout = layout
        container        += child

        renderManager(display(container))

        verify(exactly = 1) { container.doLayout_() }

        block(child)

        verify(exactly = count) { container.doLayout_() }
    }

    private fun verifyChildAddedProperly(renderManager: Pair<RenderManager, AccessibilityManager>, display: InternalDisplay, view: View, times: Int = 1) {
        verify(exactly = times) { view.addedToDisplay(display, renderManager.first, renderManager.second) }
        verify(exactly = times) { view.render        (any()                                             ) }
    }

    private fun verifyChildRemovedProperly(view: View, times: Int = 1) {
        verify(exactly = times) { view.removedFromDisplay_() }

        view.children_.forEach { verifyChildRemovedProperly(it) }
    }

    private fun view     (): View = view { size = Size(10, 10) }
    private fun container(): Container = io.nacular.doodle.core.container { size = Size(10, 10) }

    private fun doesNotRender(view: View) {
        renderManager(display(view))

        verify(exactly = 0) { view.render(any()) }
    }

    private fun doesNotRenderChild(view: View) {
        val container = spyk<Container>("xyz").apply { bounds = Rectangle(size = Size(100.0, 100.0)) }

        container += view

        renderManager(display(container))

        verify(exactly = 0) { view.render(any()) }
    }

    private fun renderManager(
            display             : InternalDisplay      = mockk(),
            themeManager        : InternalThemeManager = mockk(),
            scheduler           : AnimationScheduler   = instantScheduler,
            accessibilityManager: AccessibilityManager = mockk(),
            graphicsDevice      : GraphicsDevice<*>    = defaultGraphicsDevice): Pair<RenderManagerImpl, AccessibilityManager> = RenderManagerImpl(display, scheduler, themeManager, accessibilityManager, graphicsDevice) to accessibilityManager

    private val defaultGraphicsDevice by lazy {
        graphicsDevice()
    }

    private fun graphicsDevice(mapping: Map<View, GraphicsSurface> = mapOf()): GraphicsDevice<*> {
        val result         = mockk<GraphicsDevice<*>>()
        val defaultSurface = mockk<GraphicsSurface>()
        val canvas         = mockk<Canvas>()

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

    private fun layout(
        ignoreChildBounds   : Boolean = false,
        ignoreParentBounds  : Boolean = false,
        ignoreChildIdealSize: Boolean = true,
        ignoreChildMinSize  : Boolean = true,
    ): Layout = mockk {
        every { requiresLayout(any(), any(), any<Rectangle>(), any())            } returns !ignoreChildBounds
        every { requiresLayout(any(), any(), any())                              } returns !ignoreParentBounds
        every { requiresLayout(any(), any(), any<View.SizePreferences>(), any()) } returns !(ignoreChildIdealSize && ignoreChildMinSize)
    }

    private fun display(vararg children: View, layout: Layout = layout()): InternalDisplay = mockk<InternalDisplay> {
        val displayChildren = ObservableList<View>()

        displayChildren.addAll(children)

        val observers = SetPool<ChildObserver<Display>>()

        displayChildren.changed += { _, differences ->
            observers.forEach { it(this, differences) }
        }

        val view  = slot<View>()
        val views = slot<Collection<View>>()

        every { size                      } returns Size(100, 100)
        every { this@mockk.children       } returns displayChildren
        every { iterator()                } answers { displayChildren.iterator() }
        every { childrenChanged           } returns observers
        every { sizeChanged               } returns mockk()
        every { ancestorOf(capture(view)) } answers {
            var result = false

            if (this@mockk.children.isNotEmpty()) {
                var parent: View? = view.captured

                do {
                    if (parent in this@mockk.children) {
                        result = true
                        break
                    }

                    parent = parent?.parent
                } while (parent != null)
            }

            result
        }
        every { this@mockk.layout            } returns layout
        every { this@mockk += capture(view ) } answers { displayChildren += view.captured  }
        every { this@mockk -= capture(view ) } answers { displayChildren -= view.captured  }
        every { this@mockk += capture(views) } answers { displayChildren += views.captured }
        every { this@mockk -= capture(views) } answers { displayChildren -= views.captured }
        every { showPopup(capture(view))     } answers { displayChildren -= view.captured  }
        every { boundsChanged(capture(view)) } answers {
            view.captured.apply {
                size = preferredSize_(Size.Empty, Size.Infinite)
            }
        }
    }

    private val instantScheduler by lazy { mockk<AnimationScheduler> {
        every { this@mockk.onNextFrame(captureLambda()) } answers {
            lambda<(Measure<Time>) -> Unit>().captured(0 * milliseconds)

            val task = mockk<Task>()

            every { task.completed } returns true

            task
        }
    }}

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
}