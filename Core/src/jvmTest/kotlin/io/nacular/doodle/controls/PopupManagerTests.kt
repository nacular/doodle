package io.nacular.doodle.controls

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.View
import io.nacular.doodle.core.height
import io.nacular.doodle.core.width
import io.nacular.doodle.drawing.RenderManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.utils.PropertyObserver
import io.nacular.doodle.utils.RelativePositionMonitor
import io.nacular.doodle.utils.observable
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 5/15/23.
 */
class PopupManagerTests {
    @Test fun `shows popup`() {
        val view          = mockk<View>()
        val display       = mockk<InternalDisplay>()
        val renderManager = mockk<RenderManager>()

        val manager = createManager(display, renderManager)

        manager.show(view) {}

        verify(exactly = 1) { display.showPopup       (view) }
        verify(exactly = 1) { renderManager.popupShown(view) }
    }

    @Test fun `shows relative popup`() {
        val view          = mockk<View>()
        val anchor        = mockk<View>()
        val display       = mockk<InternalDisplay>()
        val renderManager = mockk<RenderManager>()

        val manager = createManager(display, renderManager)

        manager.show(view, relativeTo = anchor) { _,_ -> }

        verify(exactly = 1) { display.showPopup       (view) }
        verify(exactly = 1) { renderManager.popupShown(view) }
    }

    @Test fun `popup is active`() {
        val view          = mockk<View>()
        val display       = mockk<InternalDisplay>()
        val renderManager = mockk<RenderManager>()

        val manager = createManager(display, renderManager)

        manager.show(view) {}

        expect(true) { manager.active(view) }
    }

    @Test fun `relative popup is active`() {
        val view          = mockk<View>()
        val anchor        = mockk<View>()
        val display       = mockk<InternalDisplay>()
        val renderManager = mockk<RenderManager>()

        val manager = createManager(display, renderManager)

        manager.show(view, anchor) { _,_ ->}

        expect(true) { manager.active(view) }
    }

    @Test fun `non popup is not active`() {
        val view          = mockk<View>()
        val display       = mockk<InternalDisplay>()
        val renderManager = mockk<RenderManager>()

        val manager = createManager(display, renderManager)

        expect(false) { manager.active(view) }
    }

    @Test fun `hides popup`() {
        val view          = mockk<View>()
        val display       = mockk<InternalDisplay>()
        val renderManager = mockk<RenderManager>()

        val manager = createManager(display, renderManager)

        manager.show(view) {}

        verify(exactly = 1) { display.showPopup       (view) }
        verify(exactly = 1) { renderManager.popupShown(view) }

        manager.hide(view)

        verify(exactly = 1) { display.hidePopup        (view) }
        verify(exactly = 1) { renderManager.popupHidden(view) }
    }

    @Test fun `ignores hide for non-popup`() {
        val view          = mockk<View>()
        val display       = mockk<InternalDisplay>()
        val renderManager = mockk<RenderManager>()

        val manager = createManager(display, renderManager)

        manager.hide(view)

        verify(exactly = 0) { display.showPopup       (any()) }
        verify(exactly = 0) { renderManager.popupShown(any()) }
    }

    @Test fun `hidden popup is not active`() {
        val view          = mockk<View>()
        val display       = mockk<InternalDisplay>()
        val renderManager = mockk<RenderManager>()

        val manager = createManager(display, renderManager)

        manager.show(view) {}
        manager.hide(view)

        expect(false) { manager.active(view) }
    }

    @Test fun `popup positioned on first show`() {
        val view          = mockk<View>()
        val display       = createSizeableDisplay().apply { size_ = Size(100) }
        val renderManager = mockk<RenderManager>()
        val constraints   = mockk<ConstraintDslContext.(Bounds) -> Unit>()

        val manager = createManager(display, renderManager)

        manager.show(view, constraints)

        val viewSize    = Size(view.width,    view.height   )
        val displaySize = Size(display.width, display.height)

        verify(exactly = 1) { constraints(match {
            it.parent.edges.readOnly.size == displaySize
        }, match {
            it.width.readOnly == viewSize.width && it.height.readOnly == viewSize.height
        }) }
    }

    @Test fun `popup re-positioned on display size change`() {
        val view          = mockk<View>()
        val display       = createSizeableDisplay().apply { size_ = Size(100) }
        val renderManager = mockk<RenderManager>()
        val constraints   = mockk<ConstraintDslContext.(Bounds) -> Unit>()

        val manager = createManager(display, renderManager)

        manager.show(view, constraints)

        var viewSize    = Size(view.width,    view.height   )
        var displaySize = Size(display.width, display.height)

        verify(exactly = 1) { constraints(match {
            it.parent.edges.readOnly.size == displaySize
        }, match {
            it.width.readOnly == viewSize.width && it.height.readOnly == viewSize.height
        }) }

        display.size_ = Size(200)

        viewSize    = Size(view.width,    view.height   )
        displaySize = Size(display.width, display.height)

        verify(exactly = 1) { constraints(match {
            it.parent.edges.readOnly.size == displaySize
        }, match {
            it.width.readOnly == viewSize.width && it.height.readOnly == viewSize.height
        }) }
    }

    @Test fun `popup re-positioned on view bounds change`() {
        val view              = mockk<View>()
        val display           = createSizeableDisplay().apply { size_ = Size(100) }
        val constraints       = mockk<ConstraintDslContext.(Bounds) -> Unit>()
        val positionable      = mockk<View.PositionableView>()
        val renderManager     = mockk<RenderManager>()
        var viewBoundsChanged = slot<PropertyObserver<View, Rectangle>>()

        every { view.boundsChanged += captureLambda() } answers {
            viewBoundsChanged = lambda()
        }

        every { view.positionable } returns positionable

        val manager = createManager(display, renderManager)

        manager.show(view, constraints)

        val oldBounds = view.bounds
        val newBounds = Rectangle(10, 10, 10, 10)

        every { positionable.bounds } answers { newBounds }

        viewBoundsChanged.captured(view, oldBounds, newBounds)

        val displaySize = display.size

        verify(exactly = 1) {
            constraints(
                match {
                    it.parent.edges.readOnly.size == displaySize
                },
                match {
                    it.edges.readOnly.also { println("edges: $it == $newBounds") } == newBounds
                }
            )
        }
    }

    private fun createManager(
        display      : InternalDisplay         = mockk(),
        renderManager: RenderManager           = mockk(),
        boundsMonitor: RelativePositionMonitor = mockk(),
    ) = PopupManagerImpl(display, renderManager, boundsMonitor)

    private interface SizeableDisplay: InternalDisplay {
        var size_: Size
    }

    private fun createSizeableDisplay() = mockk<SizeableDisplay>().apply {
        var sizeChangedSlot = slot<PropertyObserver<InternalDisplay, Size>>()
        val sizeSlot        = slot<Size>()
        var localSize       by observable(Empty) { old, new ->
            if (sizeChangedSlot.isCaptured) {
                sizeChangedSlot.captured(this@apply, old, new)
            }
        }

        every { this@apply.sizeChanged += captureLambda() } answers {
            sizeChangedSlot = lambda()
        }

        every { size_ = capture(sizeSlot) } answers {
            localSize = sizeSlot.captured
        }

        every { size } answers { localSize }
    }
}