@file:Suppress("FunctionName")
@file:OptIn(Internal::class)

package io.nacular.doodle.core

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import io.nacular.doodle.accessibility.AccessibilityManager
import io.nacular.doodle.accessibility.AccessibilityRole
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.core.ContentDirection.LeftRight
import io.nacular.doodle.core.ContentDirection.RightLeft
import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.core.LookupResult.Found
import io.nacular.doodle.core.LookupResult.Ignored
import io.nacular.doodle.core.View.Companion.fixed
import io.nacular.doodle.core.View.Companion.proposedSize
import io.nacular.doodle.core.View.EllipseClipPath
import io.nacular.doodle.core.View.PolyClipPath
import io.nacular.doodle.core.View.SizeAuditor
import io.nacular.doodle.datatransport.dragdrop.DragRecognizer
import io.nacular.doodle.datatransport.dragdrop.DropReceiver
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.AffineTransform2D
import io.nacular.doodle.drawing.Color.Companion.Green
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.RenderManager
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.KeyState
import io.nacular.doodle.event.KeyState.Type
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.focus.FocusTraversalPolicy
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Backward
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Downward
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Forward
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Upward
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.at
import io.nacular.doodle.geometry.coerceIn
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.Insets.Companion.None
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.Cursor.Companion.Crosshair
import io.nacular.doodle.system.SystemPointerEvent.Type.Click
import io.nacular.doodle.system.SystemPointerEvent.Type.Down
import io.nacular.doodle.system.SystemPointerEvent.Type.Drag
import io.nacular.doodle.system.SystemPointerEvent.Type.Enter
import io.nacular.doodle.system.SystemPointerEvent.Type.Exit
import io.nacular.doodle.system.SystemPointerEvent.Type.Move
import io.nacular.doodle.system.SystemPointerEvent.Type.Up
import io.nacular.doodle.utils.ChangeObserver
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.PropertyObserver
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.times
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 2/23/18.
 */
class ViewTests {
    @Test fun `defaults valid`() {
        expect("", "View::toolTipText") { object: View() {}.toolTipText }

        expect(true) { object: View() {}.shouldYieldFocus() }

        mapOf(
            View::x                                to 0.0,
            View::y                                to 0.0,
            View::font                             to null,
            View::size                             to Size.Empty,
            View::width                            to 0.0,
            View::parent                           to null,
            View::height                           to 0.0,
            View::bounds                           to Empty,
            View::cursor                           to null,
            View::zOrder                           to 0,
            View::enabled                          to true,
            View::visible                          to true,
            View::opacity                          to 1f,
            View::insets_                          to None,
            View::layout_                          to null,
            View::position                         to Origin,
            View::hasFocus                         to false,
            View::display                          to null,
            View::displayed                        to false,
            View::focusable                        to true,
            View::idealSize                        to Size.Empty,
            View::displayRect                      to Empty,
            View::sizeAuditor                      to null,
            View::dropReceiver                     to null,
            View::acceptsThemes                    to true,
            View::dragRecognizer                   to null,
            View::focusCycleRoot_                  to null,
            View::foregroundColor                  to null,
            View::backgroundColor                  to null,
            View::contentDirection                 to LeftRight,
            View::isFocusCycleRoot_                to false,
            View::childrenClipPath_                to null,
            View::accessibilityLabel               to null,
            View::clipCanvasToBounds_              to true,
            View::mirrorWhenRightLeft              to true,
            View::monitorsDisplayRect              to false,
            View::needsMirrorTransform             to false,
            View::focusTraversalPolicy_            to null,
            View::localContentDirection            to null,
            View::nextInAccessibleReadOrder        to null,
            View::accessibilityLabelProvider       to null,
            View::accessibilityDescriptionProvider to null,
        ).forEach { validateDefault(it.key, it.value) }
    }

    @Test fun `default traversal keys valid`() {
        val view = object: View() {}

        listOf(Forward, Backward, Upward, Downward).forEach {
            expect(null) { view[it] }
        }
    }

    @Test fun `setters work`() {
        object: View() {}.also {
            val value = "foo"
            it.toolTipText = value

            expect(value, "toolTipText set to $value") { it.toolTipText }
        }

        validateSetter(View::font,                             mockk()                        )
        validateSetter(View::zOrder,                           56                             )
        validateSetter(View::cursor,                           Crosshair                      )
        validateSetter(View::enabled,                          false                          )
        validateSetter(View::visible,                          false                          )
        validateSetter(View::opacity,                          0.3f                           )
        validateSetter(View::focusable,                        false                          )
        validateSetter(View::sizeAuditor,                      mockk()                        )
        validateSetter(View::preferredSize,                    { _,_ -> Size(40, 67) }        )
        validateSetter(View::foregroundColor,                  Red                            )
        validateSetter(View::backgroundColor,                  Green                          )
        validateSetter(View::isFocusCycleRoot_,                true                           )
        validateSetter(View::accessibilityLabel,               "foo bar"                      )
        validateSetter(View::clipCanvasToBounds_,              false, shouldRerender = true   )
        validateSetter(View::monitorsDisplayRect,              true                           )
        validateSetter(View::localContentDirection,            RightLeft                      )
        validateSetter(View::nextInAccessibleReadOrder,        mockk()                        )
        validateSetter(View::accessibilityLabelProvider,       mockk()                        )
        validateSetter(View::accessibilityDescriptionProvider, mockk()                        )
    }

    @Test fun `traversal key setters work`() {
        val view = object: View() {}

        val key1 = mockk<KeyState>()
        val key2 = mockk<KeyState>()
        val key3 = mockk<KeyState>()
        val key4 = mockk<KeyState>()

        listOf(
            Forward  to setOf(key1, key2      ),
            Backward to setOf(key4            ),
            Upward   to setOf(key2, key4      ),
            Downward to setOf(key1, key2, key3)
        ).forEach { (type, keys) ->
            view[type] = keys

            expect(keys) { view[type] }
        }
    }

    @Test fun `rerender work`() {
        val display       = mockk<InternalDisplay>()
        val renderManager = mockk<RenderManager>()
        val view          = object: View() {}

        view.addedToDisplay_(display, renderManager, null)

        view.rerender()

        verify(exactly = 1) { renderManager.render(view) }
    }

    @Test fun `rerenderNow work`() {
        val display       = mockk<InternalDisplay>()
        val renderManager = mockk<RenderManager>()
        val view          = object: View() {}

        view.rerenderNow()

        view.addedToDisplay_(display, renderManager, null)

        view.rerenderNow()

        verify(exactly = 1) { renderManager.renderNow(view) }
    }

    @Test fun `most recent ancestor`() {
        val child       = object: View() {}
        val parent      = container {}
        val grandParent = container {}

        parent.children      += child
        grandParent.children += parent

        expect(parent     ) { child.mostRecentAncestor       { true                              } }
        expect(grandParent) { child.mostRecentAncestor       { it != parent                      } }
        expect(null       ) { child.mostRecentAncestor       { it != parent && it != grandParent } }
        expect(null       ) { grandParent.mostRecentAncestor { true                              } }
    }

    @Test fun `rerenders on clipCanvasToBounds change`() {
        val display       = mockk<InternalDisplay>()
        val renderManager = mockk<RenderManager>()
        val view          = view {}

        view.addedToDisplay_(display, renderManager, null)

        view.clipCanvasToBounds_ = false

        verify(exactly = 1) { renderManager.render(view) }
    }

    @Test fun `parent change works`() {
        val view   = object: View() {}
        val parent = container {}

        val observer = mockk<PropertyObserver<View, View?>>()

        view.parentChanged += observer

        parent.children += view

        verify(exactly = 1) { observer(view, null, parent) }

        parent.children -= view

        verify(exactly = 1) { observer(view, parent, null) }
    }

    @Test fun `display change works`() {
        val view = object: View() {}

        val observer = mockk<PropertyObserver<View, Boolean>>()

        view.displayChanged += observer

        view.addedToDisplay_(mockk(), mockk(), null)

        verify(exactly = 1) { observer(view, false, true) }

        view.removedFromDisplay_()

        verify(exactly = 1) { observer(view, true, false) }
    }

    @Test fun `displayed works`() {
        val view = object: View() {}

        val renderManager = mockk<RenderManager>()

        expect(false) { view.displayed }

        view.addedToDisplay_(mockk(), renderManager, null)

        expect(true) { view.displayed }

        view.removedFromDisplay_()

        expect(false) { view.displayed }
    }

    @Test fun `registers accessibility role when displayed`() {
        val role = mockk<AccessibilityRole>()
        val view = object: View(accessibilityRole = role) {}

        val accessibilityManager = mockk<AccessibilityManager>()

        view.addedToDisplay_(mockk(), mockk(), accessibilityManager)

        verify(exactly = 1) { accessibilityManager.roleAdopted(view) }

        view.removedFromDisplay_()

        verify(exactly = 1) { accessibilityManager.roleAbandoned(view) }
    }

    @Test fun `child at point inside clip poly`() {
        val child  = view().apply { forcePosition(5.0, 5.0) }
        val parent = object: View() {
            init {
                children_        += child
                childrenClipPath  = PolyClipPath(Rectangle(6, 6))
            }
        }

        expect(child) { parent.child_(Point(5, 5)) }
    }

    @Test fun `child at point layout returns Ignored`() {
        val child  = view().apply { forcePosition(5.0, 5.0) }
        val parent = object: View() {
            init {
                children_ += child
                layout     = mockk<Layout>().apply {
                    every { item(any(), any()) } returns Ignored
                }
            }
        }

        expect(child) { parent.child_(Point(5, 5)) }
        expect(null ) { parent.child_(Point(4, 4)) }
    }

    @Test fun `child at point layout returns Empty`() {
        val child  = view().apply { suggestPosition(5.0, 5.0) }
        val parent = object: View() {
            init {
                children_ += child
                layout    = mockk<Layout>().apply {
                    every { item(any(), any()) } returns LookupResult.Empty
                }
            }
        }

        expect(null) { parent.child_(Point(5, 5)) }
    }

    @Test fun `child at point layout returns value`() {
        val found  = view()
        val child  = view().apply { suggestPosition(5.0, 5.0) }
        val parent = object: View() {
            init {
                children_ += child
                layout    = mockk<Layout>().apply {
                    every { item(any(), any()) } returns Found(found.positionable)
                }
            }
        }

        expect(found) { parent.child_(Point(5, 5)) }
    }

    @Test fun `child at point outside clip poly`() {
        val child  = view().apply { suggestPosition(5.0, 5.0) }
        val parent = object: View() {
            init {
                children_        += child
                childrenClipPath  = PolyClipPath(Rectangle(3, 3))
            }
        }

        expect(null) { parent.child_(Point(5, 5)) }
    }

    @Test fun `child at point invisible`() {
        val child  = view().apply { suggestPosition(5.0, 5.0); visible = false }
        val parent = object: View() {
            init {
                children_ += child
            }
        }

        expect(null) { parent.child_(Point(5, 5)) }
    }

    @Test fun `display rect delegates to RenderManager`() {
        val view = object: View() {}

        val renderManager = mockk<RenderManager>()

        view.addedToDisplay_(mockk(), renderManager, null)

        view.displayRect

        verify(exactly = 1) { renderManager.displayRect(view) }
    }

    @Test fun `forwards display-rect to subclass`() {
        val view = spyk(SubView())
        val old  = Rectangle(100)
        val new  = Rectangle( 30)

        view.handleDisplayRectEvent_(old, new)

        verify(exactly = 1) { view.handleDisplayRectEvent(old, new) }
    }

    @Test fun `forwards pointer event to subclass`() {
        val view  = spyk(SubView())
        val event = mockk<PointerEvent>()

        view.handlePointerEvent_(event)

        verify(exactly = 1) { view.handlePointerEvent(event) }
    }

    @Test fun `forwards key event to subclass`() {
        val view  = spyk(SubView())
        val event = mockk<KeyEvent>()

        view.handleKeyEvent_(event)

        verify(exactly = 1) { view.handleKeyEvent(event) }
    }

    @Test fun `forwards pointer motion event to subclass`() {
        val view  = spyk(SubView())
        val event = mockk<PointerEvent>()

        view.handlePointerMotionEvent_(event)

        verify(exactly = 1) { view.handlePointerMotionEvent(event) }
    }

    @Test fun `center works`() {
        val view = view()

        listOf(
            Rectangle(        100, 100),
            Rectangle(12, 38,  10, 100)
        ).forEach {
            view.forceBounds(it)
            expect(view.center) { it.center }
        }
    }

    @Test fun `change events work`() {
        listOf(
            View::enabled             to View::enabledChanged,
            View::visible             to View::visibilityChanged,
            View::focusable           to View::focusabilityChanged,
            View::focusable           to View::focusabilityChanged,
            View::monitorsDisplayRect to View::displayRectHandlingChanged
        ).forEach {
            validateChanged(it.first, it.second)
        }
    }

    @Test fun `ideal size delegates to layout`() {
        val size = Size(10, 10)
        val view = object: View() {
            init {
                layout = mockk<Layout>().apply {
                    every { preferredSize(any(), Size.Empty, any(), Size.Infinite) } returns size
                }
            }
        }

        expect(size) { view.idealSize }
    }

    @Test fun `ideal size relies on preferredSize`() {
        val size = Size(10.6, 1.0)
        val prefSize = mockk<View.(Size, Size) -> Size> {
            every { this@mockk.invoke(any(), any(), any()) } returns size
        }
        val view = object: View() {
            init {
                preferredSize = prefSize

                layout = mockk<Layout>().apply {
                    every { layout(any(), Size.Empty, any(), Size.Infinite) } returns Size(56)
                }
            }
        }

        expect(size) { view.idealSize }
    }

    @Test fun `suggested size audited`() {
        val auditor = mockk<SizeAuditor>()
        val view = view {}.apply { sizeAuditor = auditor }
        val size = Size(34.5, 47.9)

        view.suggestSize(size)

        verify(exactly = 1) { auditor.invoke(view, Size.Empty, size, Size.Empty, Size.Infinite) }
    }

    @Test fun `size audit result used`() {
        val auditedSize = Size(10)
        val auditor = mockk<SizeAuditor> {
            every { this@mockk(any(), any(), any(), any(), any()) } returns auditedSize
        }

        val child = view {}.apply { sizeAuditor = auditor }
        val parent = container {
            + child
            layout = simpleLayout { views,_,current,_,_ ->
                views.forEach {
                    it.position = Origin
                    it.size     = it.idealSize
                }

                current
            }
        }

        child.suggestSize(Size(34.5, 47.9))
        parent.doLayout_()

        expect(auditedSize) { child.size }
    }

    @Test fun `audited size clipped to min and max`() {
        val min         = Size( 20)
        val max         = Size(100)
        val auditedSize = Size(1000, 10)
        val auditor = mockk<SizeAuditor> {
            every { this@mockk(any(), any(), any(), any(), any()) } returns auditedSize
        }

        val child = view {}.apply { sizeAuditor = auditor }
        val parent = container {
            + child
            layout = simpleLayout { views,_,current,_,_ ->
                views.forEach {
                    it.position = Origin
                    it.size     = it.preferredSize(min, max)
                }

                current
            }
        }

        child.suggestSize(Size(34.5, 47.9))
        parent.doLayout_()

        expect(auditedSize.coerceIn(min, max)) { child.size }
    }

    @Test fun `style change events work`() {
        validateStyleChanged(View::font,            mockk())
        validateStyleChanged(View::foregroundColor, mockk())
        validateStyleChanged(View::backgroundColor, mockk())
    }

    @Test fun `font falls back to parent`() {
        val font  = mockk<Font>()
        val child = object: View() {}

        object: View() {}.apply {
            this.font = font
            children_ += child
        }

        expect(font) { child.font }
    }

    @Test fun `content direction falls back to parent`() {
        val direction = mockk<ContentDirection>()
        val child     = object: View() {}

        object: View() {}.apply {
            localContentDirection = direction
            children_ += child
        }

        expect(direction) { child.contentDirection }
    }

    @Test fun `content direction falls back to display`() {
        val direction = mockk<ContentDirection>()
        val child     = object: View() {}
        val display   = mockk<InternalDisplay>().apply {
            every { contentDirection } returns direction
        }

        child.addedToDisplay_(display, mockk(), mockk())

        expect(direction) { child.contentDirection }
    }

    @Test fun `needs mirror transform sinks to descendants`() {
        val child       = object: View() {}
        val parent      = object: View() {}.apply { children_ += child  }
        val grandParent = object: View() {}.apply { children_ += parent  }

        child.localContentDirection       = LeftRight
        parent.localContentDirection      = RightLeft
        grandParent.localContentDirection = RightLeft

        expect(true) { child.needsMirrorTransform }
    }

    @Test fun `key down events works`() = validateKeyChanged(mockk<KeyEvent>().apply { every { type } returns Type.Down }) { listener, event ->
        verify(exactly = 1) { listener.pressed(event) }
    }

    @Test fun `key up events works`() = validateKeyChanged(mockk<KeyEvent>().apply { every { type } returns Type.Up }) { listener, event ->
        verify(exactly = 1) { listener.released(event) }
    }

    @Test fun `pointer events works`() = validatePointerChanged(mockk<PointerEvent>().apply { every { type } returns Enter }) { listener, event ->
        verify(exactly = 1) { listener.entered(event) }
    }

    @Test fun `pointer exit works`() = validatePointerChanged(mockk<PointerEvent>().apply { every { type } returns Exit }) { listener, event ->
        verify(exactly = 1) { listener.exited(event) }
    }

    @Test fun `pointer pressed works`() = validatePointerChanged(mockk<PointerEvent>().apply { every { type } returns Down }) { listener, event ->
        verify(exactly = 1) { listener.pressed(event) }
    }

    @Test fun `pointer released works`() = validatePointerChanged(mockk<PointerEvent>().apply { every { type } returns Up }) { listener, event ->
        verify(exactly = 1) { listener.released(event) }
    }

    @Test fun `pointer clicked works`() = validatePointerChanged(mockk<PointerEvent>().apply { every { type } returns Click }) { listener, event ->
        verify(exactly = 1) { listener.clicked(event) }
    }

    @Test fun `pointer move works`() = validatePointerMotionChanged(mockk<PointerEvent>().apply { every { type } returns Move }) { listener, event ->
        verify(exactly = 1) { listener.moved(event) }
    }

    @Test fun `pointer drag works`() = validatePointerMotionChanged(mockk<PointerEvent>().apply { every { type } returns Drag }) { listener, event ->
        verify(exactly = 1) { listener.dragged(event) }
    }

    @Test fun `filter pointer released works`() = validatePointerFilter(mockk<PointerEvent>().apply { every { type } returns Up }) { listener, event ->
        verify(exactly = 1) { listener.released(event) }
    }

    @Test fun `filter pointer move works`() = validatePointerMotionFilter(mockk<PointerEvent>().apply { every { type } returns Move }) { listener, event ->
        verify(exactly = 1) { listener.moved(event) }
    }

    @Test fun `filter pointer events works`() = validatePointerFilter(mockk<PointerEvent>().apply { every { type } returns Enter }) { listener, event ->
        verify(exactly = 1) { listener.entered(event) }
    }

    @Test fun `filter pointer exit works`() = validatePointerFilter(mockk<PointerEvent>().apply { every { type } returns Exit }) { listener, event ->
        verify(exactly = 1) { listener.exited(event) }
    }

    @Test fun `filter pointer pressed works`() = validatePointerFilter(mockk<PointerEvent>().apply { every { type } returns Down }) { listener, event ->
        verify(exactly = 1) { listener.pressed(event) }
    }

    @Test fun `filter pointer clicked works`() = validatePointerFilter(mockk<PointerEvent>().apply { every { type } returns Click }) { listener, event ->
        verify(exactly = 1) { listener.clicked(event) }
    }

    @Test fun `filter pointer drag works`() = validatePointerMotionFilter(mockk<PointerEvent>().apply { every { type } returns Drag }) { listener, event ->
        verify(exactly = 1) { listener.dragged(event) }
    }

    @Test fun `focus gained works`() = validateFocusChanged(true) { view, observer ->
        verify(exactly = 1) { observer(view, false, true) }
    }

    @Test fun `focus lost works`() = validateFocusChanged(false) { view, observer ->
        verify(exactly = 1) { observer(view, true, false) }
    }

    @Test fun `bounds changed same value ignored`() {
        val bounds   = Rectangle(5.6, 3.7, 900.0, 1.2)
        val view     = object: View() {}.apply { this.suggestBounds(bounds) }
        val observer = mockk<PropertyObserver<View, Rectangle>>()

        val renderManager = mockk<RenderManager>(relaxed = true)

        view.addedToDisplay_(mockk(relaxed = true), renderManager, mockk(relaxed = true))
        view.boundsChanged += observer
        view.suggestBounds(bounds)

        verify (exactly = 0) {
            observer(any(), any(), any())
        }

        verify (exactly = 0) {
            renderManager.boundsChanged(any(), any(), any())
        }
    }

    @Test fun `bounds changed works`() {
        val view     = object: View() {}
        val new      = Rectangle(5.6, 3.7, 900.0, 1.2)
        val old      = view.bounds
        val newValue = slot<Rectangle>()
        val observer = mockk<PropertyObserver<View, Rectangle>> {
            every { this@mockk.invoke(view, any<Rectangle>(), capture(newValue)) } answers {
                expect(newValue.captured) { view.boundingBox }
            }
        }

        val display       = mockk<Display>      (relaxed = true)
        val renderManager = mockk<RenderManager>(relaxed = true)

        view.addedToDisplay_(display, renderManager, mockk(relaxed = true))
        view.boundsChanged += observer
        view.forceBounds(new             )
        view.forceBounds(new.at(x = 67.0))

        verifyOrder {
            renderManager.boundsSuggested(view, old, new             )
            renderManager.boundsSuggested(view, new, new.at(x = 67.0))
        }
    }

    @Test fun `opacity changed works`() {
        val view     = object: View() {}
        val old      = 1f
        val new      = 0.5f
        val newValue = slot<Float>()
        val observer = mockk<PropertyObserver<View, Float>>().apply {
            every { this@apply.invoke(view, any<Float>(), capture(newValue)) } answers {
                expect(newValue.captured) { view.opacity }
            }
        }

        val renderManager = mockk<RenderManager>(relaxed = true)

        view.addedToDisplay_(mockk(relaxed = true), renderManager, mockk(relaxed = true))
        view.opacityChanged += observer
        view.opacity         = new

        verifyOrder {
            renderManager.opacityChanged(view, old, new)
            observer(view, old, new)
        }
    }

    @Test fun `transform changed works`() {
        val view     = object: View() {}
        val old      = Identity
        val new      = Identity.rotate(90 * degrees)
        val newValue = slot<AffineTransform2D>()
        val observer = mockk<PropertyObserver<View, AffineTransform>>().apply {
            every { this@apply.invoke(view, any<AffineTransform2D>(), capture(newValue)) } answers {
                expect(newValue.captured) { view.transform }
            }
        }

        val renderManager = mockk<RenderManager>(relaxed = true)

        view.addedToDisplay_(mockk(relaxed = true), renderManager, mockk(relaxed = true))
        view.transformChanged += observer
        view.transform         = new

        verifyOrder {
            renderManager.transformChanged(view, old, new)
            observer(view, old, new)
        }
    }

    @Test fun `visibility changed works`() {
        val view     = object: View() {}
        val old      = true
        val new      = false
        val newValue = slot<Boolean>()
        val observer = mockk<PropertyObserver<View, Boolean>>().apply {
            every { this@apply.invoke(view, any<Boolean>(), capture(newValue)) } answers {
                expect(newValue.captured) { view.visible }
            }
        }

        val renderManager = mockk<RenderManager>(relaxed = true)

        view.addedToDisplay_(mockk(relaxed = true), renderManager, mockk(relaxed = true))
        view.visibilityChanged += observer
        view.visible            = new

        verifyOrder {
            renderManager.visibilityChanged(view, old, new)
            observer(view, old, new)
        }
    }

    @Test fun `cursor changed works`() {
        val child       = object: View() {}
        val parent      = object: View() {}.apply { children_ += child  }
        val grandParent = object: View() {}.apply { children_ += parent }
        val observer    = mockk<PropertyObserver<View, Cursor?>>()
        val new         = Crosshair
        val old         = grandParent.cursor

        child.cursorChanged       += observer
        parent.cursorChanged      += observer
        grandParent.cursorChanged += observer

        grandParent.cursor         = new

        verify(exactly = 1) { observer(child,       old, new) }
        verify(exactly = 1) { observer(parent,      old, new) }
        verify(exactly = 1) { observer(grandParent, old, new) }
    }

    @Test fun `disable change works`() {
        val grandChild  = object: View() {}
        val child       = object: View() {}.apply { children_ += grandChild }
        val parent      = object: View() {}.apply { children_ += child      }
        val grandParent = object: View() {}.apply { children_ += parent     }
        val observer    = mockk<PropertyObserver<View, Boolean>>()
        val new         = false
        val old         = grandParent.enabled

        grandChild.enabled = false

        grandChild.enabledChanged  += observer
        child.enabledChanged       += observer
        parent.enabledChanged      += observer
        grandParent.enabledChanged += observer

        grandParent.enabled = new

        verify(exactly = 0) { observer(grandChild,  old, new) }
        verify(exactly = 1) { observer(child,       old, new) }
        verify(exactly = 1) { observer(parent,      old, new) }
        verify(exactly = 1) { observer(grandParent, old, new) }
    }

    @Test fun `enabled change works`() {
        val grandChild  = object: View() {}
        val child       = object: View() {}.apply { children_ += grandChild }
        val parent      = object: View() {}.apply { children_ += child      }
        val grandParent = object: View() {}.apply { children_ += parent     }
        val observer    = mockk<PropertyObserver<View, Boolean>>()
        grandParent.enabled = false
        val new         = true
        val old         = grandParent.enabled

        child.enabled = false

        grandChild.enabledChanged  += observer
        child.enabledChanged       += observer
        parent.enabledChanged      += observer
        grandParent.enabledChanged += observer

        grandParent.enabled = new

        verify(exactly = 0) { observer(grandChild,  old, new) }
        verify(exactly = 0) { observer(child,       old, new) }
        verify(exactly = 1) { observer(parent,      old, new) }
        verify(exactly = 1) { observer(grandParent, old, new) }
    }

    @Test fun `enabled change accessible`() {
        val accessibilityManager = mockk<AccessibilityManager>()

        val view = view {}

        view.addedToDisplay_(mockk(), mockk(), accessibilityManager)

        view.enabled = false

        verify(exactly = 2) { accessibilityManager.syncEnabled(view) }
    }

    @Test fun `accessibility role change works`() {
        val accessibilityManager = mockk<AccessibilityManager>()

        val view = view {}

        view.addedToDisplay_(mockk(), mockk(), accessibilityManager)

        view.accessibilityRole = mockk()

        verify(exactly = 1) { accessibilityManager.roleAbandoned(view) }
        verify(exactly = 1) { accessibilityManager.roleAdopted  (view) }
    }

    @Test fun `accessible label change works`() {
        val accessibilityManager = mockk<AccessibilityManager>()

        val view = view {}

        view.addedToDisplay_(mockk(), mockk(), accessibilityManager)

        view.accessibilityLabel = "hello there"

        verify(exactly = 2) { accessibilityManager.syncLabel(view) }
    }

    @Test fun `accessible label provider change works`() {
        val accessibilityManager = mockk<AccessibilityManager>()

        val view = view {}

        view.addedToDisplay_(mockk(), mockk(), accessibilityManager)

        view.accessibilityLabelProvider = mockk()

        verify(exactly = 2) { accessibilityManager.syncLabel(view) }
    }

    @Test fun `accessible description provider change works`() {
        val accessibilityManager = mockk<AccessibilityManager>()

        val view = view {}

        view.addedToDisplay_(mockk(), mockk(), accessibilityManager)

        view.accessibilityDescriptionProvider = mockk()

        verify(exactly = 2) { accessibilityManager.syncDescription(view) }
    }

    @Test fun `next in accessibility read order change works`() {
        val accessibilityManager = mockk<AccessibilityManager>()

        val view = view {}

        view.addedToDisplay_(mockk(), mockk(), accessibilityManager)

        view.nextInAccessibleReadOrder = mockk()

        verify(exactly = 2) { accessibilityManager.syncNextReadOrder(view) }
    }

    @Test fun `z-order change works`() {
        val view          = object: View() {}
        val observer      = mockk<PropertyObserver<View, Int>>()
        val new           = 35
        val old           = view.zOrder
        val renderManager = mockk<RenderManager>(relaxed = true)

        view.addedToDisplay_(mockk(relaxed = true), renderManager, mockk(relaxed = true))

        view.zOrderChanged += observer
        view.zOrder         = new
        view.zOrder         = new

        verifyOrder {
            renderManager.zOrderChanged(view, old, new)
            observer(view, old, new)
        }
    }

    @Test fun `contains child works`() {
        val view1 = object: View() {}
        val view2 = object: View() {}
        val view  = object: View() {
            init {
                children += view1
            }

            fun has(child: View): Boolean = child in this
        }

        expect(true,  "$view contains $view1") { view.has(view1) }
        expect(false, "$view contains $view2") { view.has(view2) }
    }

    @Test fun `contains point works`() {
        val view   = object: View() {}
        val bounds = Rectangle(10.0, 10.0, 25.0, 25.0)

        expect(false, "$view contains ${bounds.position}") { bounds.position in view }

        view.forceBounds(bounds)

        expect(true, "$view contains ${bounds.position}") { bounds.position in view }

        view.forceSize(Size.Empty)

        expect(false, "$view contains ${bounds.position}") { bounds.position in view }
    }

    @Test fun `tool-top text works`() {
        val view = object: View() {}
        val event = mockk<PointerEvent>()

        expect("", "${view.toolTipText} == \"\"") { view.toolTipText(event) }

        view.toolTipText = "foo"

        expect("foo", "${view.toolTipText} == \"\"") { view.toolTipText(event) }
    }

    @Test fun `is-ancestor works`() {
        val root   = object: View() {}
        val parent = object: View() {}
        val child  = object: View() {}

        root.addedToDisplay_(mockk(), mockk(), mockk())

        expect(false) { root ancestorOf_ root  }
        expect(false) { root ancestorOf_ child }

        root.children_   += parent
        parent.children_ += child

        parent.addedToDisplay_(mockk(), mockk(), mockk())
        child.addedToDisplay_(mockk(), mockk(), mockk())

        expect(true) { root ancestorOf_ parent }
        expect(true) { root ancestorOf_ child  }
    }

    @Test fun `to absolute works`() {
        val root   = view()
        val parent = view().apply { suggestPosition(10.0, 12.0) }
        val child  = view().apply { suggestPosition(10.0, 12.0) }

        root.children_   += parent
        parent.children_ += child

        val point = Point(100.0, 56.0)

        expect(point + parent.position                 ) { parent.toAbsolute(point) }
        expect(point + parent.position + child.position) { child.toAbsolute (point) }
    }

    @Test fun `from absolute works`() {
        val root   = view()
        val parent = view().apply { suggestPosition(10.0, 12.0) }
        val child  = view().apply { suggestPosition(10.0, 12.0) }

        root.children_   += parent
        parent.children_ += child

        val point = Point(100.0, 56.0)

        expect(point -  parent.position                  ) { parent.fromAbsolute(point) }
        expect(point - (parent.position + child.position)) { child.fromAbsolute (point) }

        expect(child.fromAbsolute(point)) { child.toLocal(point, root) }
    }

    @Test fun `to local works`() {
        val root   = view()
        val parent = view().apply { suggestPosition(10.0, 12.0) }
        val child1 = view().apply { suggestPosition(10.0, 12.0) }
        val child2 = view().apply { suggestPosition(20.0, 12.0) }

        root.children_   += parent
        parent.children_ += child1
        parent.children_ += child2

        expect(Origin                           ) { parent.toLocal(parent.position,    root  ) }
        expect(Point(-45.0, 0.89)               ) { root.toLocal  (Point(-45.0, 0.89), root  ) }
        expect(child2.position - child1.position) { child1.toLocal(Origin,             child2) }
    }

    @Test fun `child at works`() {
        val root   = view()
        val child0 = view().apply { forcePosition(10.0, 12.0) }
        val child1 = view().apply { forcePosition(10.0, 12.0) }
        val child2 = view().apply { forcePosition(20.0, 12.0) }
        val child3 = view().apply { forceBounds(Rectangle(x = 10.0, y = 23.0, width = 0.0)) }

        root.children_ += child0
        root.children_ += child1
        root.children_ += child2
        root.children_ += child3

        expect(child1) { root.child_(Point(11.0, 13.0)) }
        expect(child2) { root.child_(Point(20.0, 12.0)) }
        expect(null  ) { root.child_(child3.position  ) }

        child1.visible = false

        expect(child0) { root.child_(Point(11.0, 13.0)) }
    }

    @Test fun `cannot be parent to self`() {
        val view = object: View() {
            public override val children: ObservableList<View>
                get() = super.children
        }

        assertFailsWith<IllegalArgumentException> { view.children += view }
    }

    @Test fun `cannot add ancestor to children`() {
        val grandParent = object: View() {
            public override val children get() = super.children
        }

        val parent = object: View() {
            public override val children get() = super.children
        }

        val child = object: View() {
            public override val children get() = super.children
        }

        grandParent.children += parent
        parent.children      += child

        grandParent.addedToDisplay_(mockk(), mockk(), mockk())
        parent.addedToDisplay_(mockk(), mockk(), mockk())
        child.addedToDisplay_(mockk(), mockk(), mockk())

        assertFailsWith<IllegalArgumentException> { child.children += grandParent }
    }

    @Test fun `scroll to works`() {
        val view  = view { suggestSize(Size(100)) }

        val panel = spyk<ScrollPanel>().apply {
            suggestSize(Size(50))
        }

        panel.content = view

        val rect = Rectangle(0, 55, 10, 10)

        view.scrollTo(rect)

        verify {
            panel.scrollToVisible(rect)
            panel.scrollTo       (any()) // FIXME: This causes mockk to hang, maybe b/c it's a recursive method
        }
    }

    @Test fun `new bounds works`() {
        val child  = view {}
        val parent = container {
            + child

            suggestSize(Size(100))
            layout = simpleLayout { items, min, _, max, _ ->
                var maxRight  = 0.0
                var maxBottom = 0.0

                items.forEach {
                    it.assumeAnySize(min, max)

                    maxRight  = it.right
                    maxBottom = it.bottom
                }

                Size(maxRight, maxBottom)
            }
        }

        child.suggestSize(Size(10))
        parent.doLayout_()

        expect(Size(10)) { child.size }

        child.suggestBounds(Rectangle(23, 56, 110, 110))
        parent.doLayout_()

        expect(Rectangle(23, 56, 110, 110)) { child.bounds }
        expect(Size(child.bounds.right, child.bounds.bottom)) { parent.idealSize }
    }

    // region Suggest-bounds API

    @Test fun `suggest x updates only x`() {
        val view = view().apply { suggestBounds(1.0, 2.0, 30.0, 40.0) }

        view.suggestX(5.0)

        expect(Rectangle(5.0, 2.0, 30.0, 40.0)) { view.prospectiveBounds }
    }

    @Test fun `suggest y updates only y`() {
        val view = view().apply { suggestBounds(1.0, 2.0, 30.0, 40.0) }

        view.suggestY(5.0)

        expect(Rectangle(1.0, 5.0, 30.0, 40.0)) { view.prospectiveBounds }
    }

    @Test fun `suggest width updates only width`() {
        val view = view().apply { suggestBounds(1.0, 2.0, 30.0, 40.0) }

        view.suggestWidth(5.0)

        expect(Rectangle(1.0, 2.0, 5.0, 40.0)) { view.prospectiveBounds }
    }

    @Test fun `suggest height updates only height`() {
        val view = view().apply { suggestBounds(1.0, 2.0, 30.0, 40.0) }

        view.suggestHeight(5.0)

        expect(Rectangle(1.0, 2.0, 30.0, 5.0)) { view.prospectiveBounds }
    }

    @Test fun `suggest position by point updates only position`() {
        val view = view().apply { suggestBounds(1.0, 2.0, 30.0, 40.0) }

        view.suggestPosition(Point(7.0, 8.0))

        expect(Rectangle(7.0, 8.0, 30.0, 40.0)) { view.prospectiveBounds }
    }

    @Test fun `suggest size by size updates only size`() {
        val view = view().apply { suggestBounds(1.0, 2.0, 30.0, 40.0) }

        view.suggestSize(Size(7.0, 8.0))

        expect(Rectangle(1.0, 2.0, 7.0, 8.0)) { view.prospectiveBounds }
    }

    @Test fun `suggest bounds by components works`() {
        val view = view()

        view.suggestBounds(1.0, 2.0, 30.0, 40.0)

        expect(Rectangle(1.0, 2.0, 30.0, 40.0)) { view.prospectiveBounds }
    }

    @Test fun `suggest center moves center and preserves size`() {
        val view = view().apply { suggestBounds(0.0, 0.0, 20.0, 20.0) }

        view.suggestCenter(Point(50.0, 50.0))

        expect(Rectangle(40.0, 40.0, 20.0, 20.0)) { view.prospectiveBounds }
    }

    @Test fun `suggesting current x y width or height is ignored`() {
        val view = object: View() {}.apply { forceBounds(Rectangle(10.0, 20.0, 30.0, 40.0)) }

        view.suggestBounds(50.0, 60.0, 70.0, 80.0)

        view.suggestX     (10.0)
        view.suggestY     (20.0)
        view.suggestWidth (30.0)
        view.suggestHeight(40.0)

        expect(Rectangle(50.0, 60.0, 70.0, 80.0)) { view.prospectiveBounds }
    }

    @Test fun `suggested bounds not applied until synced`() {
        val view = object: View() {}
        val new  = Rectangle(1.0, 2.0, 30.0, 40.0)

        view.suggestBounds(new)

        expect(Empty) { view.bounds           }
        expect(new  ) { view.prospectiveBounds }

        view.syncBounds()

        expect(new) { view.bounds }
    }

    @Test fun `sync bounds clips size to allowed range`() {
        val view   = view {}
        val layout = simpleLayout { views, _, _, _, _ ->
            views.first().assumeAnySize(upTo = Size(20))
        }

        view.suggestSize(100.0, 100.0)

        expect(Size(100)) { view.prospectiveBounds.size }

        layout.layout(sequenceOf(view.positionable), Size.Empty, Size.Empty, Size.Infinite)
        view.positionable.finalize()

        view.syncBounds()

        expect(Size(20)) { view.bounds.size }
    }

    @Test fun `allowed size range clips suggested size until reset`() {
        val view   = view {}
        val layout = simpleLayout { views, _, _, _, _ ->
            views.first().assumeAnySize(upTo = Size(20))
        }

        view.suggestSize(100.0, 100.0)

        expect(Size(100)) { view.prospectiveBounds.size }

        layout.layout(sequenceOf(view.positionable), Size.Empty, Size.Empty, Size.Infinite)
        view.positionable.finalize()

        view.syncBounds()

        expect(Size(20)) { view.bounds.size }

        view.resetConstraints()

        view.syncBounds()

        view.suggestSize(100.0, 100.0)

        expect(Size(100)) { view.prospectiveBounds.size }
    }

    // endregion
    // region Size

    @Test fun `size auditor applied when set`() {
        val view = view {}

        view.sizeAuditor = SizeAuditor { _, _, _, _, _ -> Size(7.0, 9.0) }

        expect(Size(7.0, 9.0)) { view.prospectiveBounds.size }
    }

    @Test fun `fixed preferred size works`() {
        val view = view {}

        view.preferredSize = fixed(Size(5, 6))

        expect(Size(5, 6)) { view.idealSize }
    }

    @Test fun `proposed preferred size uses prospective size`() {
        val view = view {}

        view.preferredSize = proposedSize
        view.suggestSize(Size(7, 8))

        expect(Size(7, 8)) { view.idealSize }
    }

    @Test fun `default preferred size without layout uses prospective size`() {
        val view = view {}

        view.suggestSize(Size(3, 4))

        expect(Size(3, 4)) { view.idealSize }
    }

    @Test fun `preserve aspect keeps ratio when width changes`() {
        val view = view()

        val result = SizeAuditor.preserveAspect(2.0)(view, Size(10.0, 5.0), Size(20.0, 5.0), Size.Empty, Size.Infinite)

        expect(Size(20.0, 10.0)) { result }
    }

    @Test fun `preserve aspect keeps ratio when height changes`() {
        val view = view()

        val result = SizeAuditor.preserveAspect(2.0)(view, Size(10.0, 5.0), Size(10.0, 10.0), Size.Empty, Size.Infinite)

        expect(Size(20.0, 10.0)) { result }
    }

    @Test fun `preserve aspect respects max size`() {
        val view = view()

        val widthLimited  = SizeAuditor.preserveAspect(2.0)(view, Size(10.0, 5.0), Size(100.0, 5.0  ), Size.Empty, Size(50.0, 50.0 ))
        val heightLimited = SizeAuditor.preserveAspect(2.0)(view, Size(10.0, 5.0), Size(100.0, 100.0), Size.Empty, Size(200.0, 20.0))

        expect(Size(50.0, 25.0)) { widthLimited  }
        expect(Size(40.0, 20.0)) { heightLimited }
    }

    @Test fun `preserve aspect from width and height matches ratio`() {
        val view     = view()
        val fromSize = SizeAuditor.preserveAspect(100.0, 50.0)
        val fromRate = SizeAuditor.preserveAspect(2.0)

        listOf(
            Triple(Size(10.0,  5.0), Size(20.0,  5.0), Size.Infinite),
            Triple(Size(10.0,  5.0), Size(10.0, 10.0), Size.Infinite),
            Triple(Size(10.0,  5.0), Size(100.0, 5.0), Size(50.0, 50.0))
        ).forEach { (old, new, max) ->
            expect(fromRate(view, old, new, Size.Empty, max)) { fromSize(view, old, new, Size.Empty, max) }
        }
    }

    // endregion
    // region Clip paths

    @Test fun `poly clip path contains works`() {
        val clip = PolyClipPath(Rectangle(10, 10))

        expect(true ) { Point( 5.0,  5.0) in clip }
        expect(false) { Point(20.0, 20.0) in clip }
    }

    @Test fun `ellipse clip path from circle works`() {
        val clip = EllipseClipPath(Point(10.0, 10.0), 5.0)

        expect(true ) { Point(10.0, 10.0) in clip }
        expect(true ) { Point(10.0, 14.0) in clip }
        expect(false) { Point(10.0, 16.0) in clip }
        expect(false) { Point(30.0, 30.0) in clip }
    }

    @Test fun `ellipse clip path from radii works`() {
        val clip = EllipseClipPath(Point(0.0, 0.0), 10.0, 5.0)

        expect(true ) { Point(9.0, 0.0) in clip }
        expect(false) { Point(0.0, 6.0) in clip }
    }

    @Test fun `ellipse clip path from ellipse works`() {
        val clip = EllipseClipPath(Ellipse(Point(0.0, 0.0), 10.0, 5.0))

        expect(true ) { Point(9.0, 0.0) in clip }
        expect(false) { Point(0.0, 6.0) in clip }
    }

    @Test fun `children clipped by ellipse clip path`() {
        val child  = view().apply { forcePosition(0.0, 0.0) }
        val parent = object: View() {
            init {
                children_        += child
                childrenClipPath  = EllipseClipPath(Point(5.0, 5.0), 2.0)
            }
        }

        expect(child) { parent.child_(Point(5.0, 5.0)) }
        expect(null ) { parent.child_(Point(0.5, 0.5)) }
    }

    // endregion
    // region Children

    @Test fun `removing child resets parent z-order and position`() {
        val parent = OpenView()
        val child  = view().apply { zOrder = 3; suggestPosition(5.0, 5.0) }

        parent.children += child

        expect(parent) { child.parent }

        parent.children -= child

        expect(null  ) { child.parent                    }
        expect(0     ) { child.zOrder                    }
        expect(Origin) { child.prospectiveBounds.position }
    }

    @Test fun `moving child to new parent preserves its state`() {
        val first  = OpenView()
        val second = OpenView()
        val child  = view().apply { zOrder = 3; suggestPosition(5.0, 5.0) }

        first.children  += child
        second.children += child

        expect(second     ) { child.parent                      }
        expect(3          ) { child.zOrder                      }
        expect(Point(5, 5)) { child.prospectiveBounds.position  }
        expect(true       ) { first.children.isEmpty()          }
    }

    @Test fun `children changes notify observers and render manager`() {
        val parent   = OpenView()
        val child    = view()
        val observer = mockk<ChildObserver<View>>(relaxed = true)
        val rm       = mockk<RenderManager>(relaxed = true)

        parent.addedToDisplay_(mockk(relaxed = true), rm, null)
        parent.childrenChanged_ += observer

        parent.children += child

        verify(exactly = 1) { observer(parent, any()) }
        verify(exactly = 1) { rm.childrenChanged(parent, any()) }
    }

    @Test fun `child picks up parent font when added`() {
        val font     = mockk<Font>()
        val child    = view()
        val parent   = view().apply { this.font = font }
        val observer = mockk<ChangeObserver<View>>(relaxed = true)

        child.styleChanged += observer

        parent.children_ += child

        expect(font) { child.font }
        verify(exactly = 1) { observer(child) }
    }

    @Test fun `child with own font ignores parent font when added`() {
        val child    = view().apply { font = mockk() }
        val parent   = view().apply { font = mockk() }
        val observer = mockk<ChangeObserver<View>>(relaxed = true)

        child.styleChanged += observer

        parent.children_ += child

        verify(exactly = 0) { observer(child) }
    }

    @Test fun `child at point prefers higher z-order`() {
        val root  = view()
        val back  = view().apply { forcePosition(10.0, 12.0); zOrder = 5 }
        val front = view().apply { forcePosition(10.0, 12.0)              }

        root.children_ += back
        root.children_ += front

        expect(back ) { root.child_(Point(11.0, 13.0)) }

        back.zOrder = 0

        expect(front) { root.child_(Point(11.0, 13.0)) }
    }

    @Test fun `child at point honors predicate`() {
        val root  = view()
        val back  = view().apply { forcePosition(10.0, 12.0) }
        val front = view().apply { forcePosition(10.0, 12.0) }

        root.children_ += back
        root.children_ += front

        expect(back) { root.child_(Point(11.0, 13.0)) { it !== front } }
        expect(null) { root.child_(Point(11.0, 13.0)) { false         } }
    }

    @Test fun `child at point layout returns non-view positionable`() {
        val parent = object: View() {
            init {
                layout = mockk<Layout>().apply {
                    every { item(any(), any()) } returns Found(mockk<Positionable>())
                }
            }
        }

        expect(null) { parent.child_(Point(1.0, 1.0)) }
    }

    @Test fun `focus cycle root is nearest cycle-root ancestor`() {
        val root = view()
        val mid  = view()
        val leaf = view()

        root.children_ += mid
        mid.children_  += leaf

        expect(null) { leaf.focusCycleRoot_ }

        root.isFocusCycleRoot_ = true

        expect(root) { leaf.focusCycleRoot_ }
        expect(root) { mid.focusCycleRoot_  }
        expect(null) { root.focusCycleRoot_ }

        mid.isFocusCycleRoot_ = true

        expect(mid) { leaf.focusCycleRoot_ }
    }

    @Test fun `is-ancestor is false for unrelated and descendant views`() {
        val root      = view()
        val child     = view()
        val unrelated = view()

        root.addedToDisplay_(mockk(), mockk(), null)
        root.children_ += child
        child.addedToDisplay_(mockk(), mockk(), null)
        unrelated.addedToDisplay_(mockk(), mockk(), null)

        expect(true ) { root      ancestorOf_ child     }
        expect(false) { child     ancestorOf_ root      }
        expect(false) { root      ancestorOf_ unrelated }
        expect(false) { unrelated ancestorOf_ child     }
    }

    // endregion
    // region Layout

    @Test fun `setting layout requests relayout`() {
        val rm     = mockk<RenderManager>(relaxed = true)
        val view   = OpenView()
        val layout = simpleLayout { _, _, current, _, _ -> current }

        view.addedToDisplay_(mockk(relaxed = true), rm, null)

        view.layout = layout

        verify(exactly = 1) { rm.layout(view) }

        view.layout = layout

        verify(exactly = 1) { rm.layout(view) }
    }

    @Test fun `setting layout relayouts parent if preferred size is updated`() {
        val renderManager = mockk<RenderManager>(relaxed = true)
        val parent        = container {}
        val child         = OpenView()

        parent.children += child

        parent.addedToDisplay_(mockk(relaxed = true), renderManager, null)
        child.addedToDisplay_( mockk(relaxed = true), renderManager, null)

        child.layout = simpleLayout { _, _, _, _, _ -> Size(10) }
        child.doLayout_()

        verify(exactly = 1) { renderManager.layout(parent) }
    }

    @Test fun `setting layout relayouts display if preferred size is updated`() {
        val display       = mockk<InternalDisplay>()
        val renderManager = mockk<RenderManager>  ()
        val child         = OpenView()

        child.addedToDisplay_( display, renderManager, null)

        child.layout = simpleLayout { _, _, _, _, _ -> Size(10) }
        child.doLayout_()

        verify(exactly = 1) { display.relayout() }
    }

    @Test fun `relayout delegates to render manager`() {
        val rm   = mockk<RenderManager>(relaxed = true)
        val view = OpenView()

        view.relayout()

        view.addedToDisplay_(mockk(relaxed = true), rm, null)

        view.relayout()
        view.relayout_()

        verify(exactly = 2) { rm.layout(view) }
    }

    @Test fun `relayout now delegates to render manager`() {
        val rm   = mockk<RenderManager>(relaxed = true)
        val view = OpenView()

        view.relayoutNow()

        view.addedToDisplay_(mockk(relaxed = true), rm, null)

        view.relayoutNow()

        verify(exactly = 1) { rm.layoutNow(view) }
    }

    @Test fun `revalidate relayouts and rerenders`() {
        val rm   = mockk<RenderManager>(relaxed = true)
        val view = OpenView()

        view.addedToDisplay_(mockk(relaxed = true), rm, null)

        view.callRevalidate()
        view.revalidate_()

        verify(exactly = 2) { rm.layout(view) }
        verify(exactly = 2) { rm.render(view) }
    }

    @Test fun `do layout skipped unless layout is needed`() {
        val renderManager = mockk<RenderManager>(relaxed = true)
        val view          = view { layout = mockk() }

        view.addedToDisplay_(mockk(relaxed = true), renderManager, null)

        view.doLayout_()

        verify(exactly = 1) { renderManager.performedLayout(view) }

        every { renderManager.layoutNeeded(view) } returns false

        // nothing invalidated, so layout is skipped
        view.doLayout_()

        verify(exactly = 1) { renderManager.performedLayout(view) }

        every { renderManager.layoutNeeded(view) } returns true

        view.doLayout_()

        verify(exactly = 2) { renderManager.performedLayout(view) }
    }

    @Test fun `do layout without layout does nothing`() {
        val rm   = mockk<RenderManager>(relaxed = true)
        val view = OpenView()

        view.addedToDisplay_(mockk(relaxed = true), rm, null)

        view.doLayout_()

        verify(exactly = 0) { rm.performedLayout(any()) }
    }

    @Test fun `do layout passes children and insets to layout`() {
        var seen       = emptyList<Positionable>()
        var seenInsets = None
        val child      = view()
        val view       = OpenView().apply {
            insets = Insets(3.0)
            layout = simpleLayout { views, _, current, _, insets ->
                seen       = views.toList()
                seenInsets = insets

                current
            }
        }

        view.children += child

        view.doLayout_()

        expect(listOf(child.positionable)) { seen            }
        expect(3.0                       ) { seenInsets.top  }
    }

    // endregion
    // region Events

    @Test fun `filter key down works`() = validateKeyFilter(mockk<KeyEvent>().apply { every { type } returns Type.Down }) { listener, event ->
        verify(exactly = 1) { listener.pressed(event) }
    }

    @Test fun `filter key up works`() = validateKeyFilter(mockk<KeyEvent>().apply { every { type } returns Type.Up }) { listener, event ->
        verify(exactly = 1) { listener.released(event) }
    }

    @Test fun `forwards key filter event to subclass`() {
        val view  = spyk(SubView())
        val event = mockk<KeyEvent>(relaxed = true)

        view.filterKeyEvent_(event)

        verify(exactly = 1) { view.filterKeyEvent(event) }
    }

    @Test fun `pointer listeners ignore unsupported event types`() {
        val view     = object: View() {}
        val listener = mockk<PointerListener>(relaxed = true)
        val event    = mockk<PointerEvent>().apply { every { type } returns Move }

        view.pointerChanged += listener
        view.pointerFilter  += listener

        view.handlePointerEvent_(event)
        view.filterPointerEvent_(event)

        verify(exactly = 0) { listener.pressed (any()) }
        verify(exactly = 0) { listener.released(any()) }
        verify(exactly = 0) { listener.entered (any()) }
        verify(exactly = 0) { listener.exited  (any()) }
        verify(exactly = 0) { listener.clicked (any()) }
    }

    @Test fun `pointer motion listeners ignore unsupported event types`() {
        val view     = object: View() {}
        val listener = mockk<PointerMotionListener>(relaxed = true)
        val event    = mockk<PointerEvent>().apply { every { type } returns Down }

        view.pointerMotionChanged += listener
        view.pointerMotionFilter  += listener

        view.handlePointerMotionEvent_(event)
        view.filterPointerMotionEvent_(event)

        verify(exactly = 0) { listener.moved  (any()) }
        verify(exactly = 0) { listener.dragged(any()) }
    }

    @Test fun `pointer events handled by default and not passed through`() {
        val view     = object: View() {}
        val listener = mockk<PointerListener>(relaxed = true)
        val event    = mockk<PointerEvent>().apply { every { type } returns Down }

        view.pointerPassedThrough += listener

        expect(true) { view.shouldHandlePointerEvent_(event) }

        verify(exactly = 0) { listener.pressed(any()) }
    }

    @Test fun `pointer events passed through when not handled`() {
        val view     = object: View() {
            override fun shouldHandlePointerEvent(event: PointerEvent): Boolean = false
        }
        val listener = mockk<PointerListener>(relaxed = true)

        view.pointerPassedThrough += listener

        listOf(Up, Down, Exit, Enter, Click).forEach { eventType ->
            expect(false) { view.shouldHandlePointerEvent_(mockk<PointerEvent>().apply { every { type } returns eventType }) }
        }

        verify(exactly = 1) { listener.released(any()) }
        verify(exactly = 1) { listener.pressed (any()) }
        verify(exactly = 1) { listener.exited  (any()) }
        verify(exactly = 1) { listener.entered (any()) }
        verify(exactly = 1) { listener.clicked (any()) }
    }

    @Test fun `pointer motion events handled by default and not passed through`() {
        val view     = object: View() {}
        val listener = mockk<PointerMotionListener>(relaxed = true)
        val event    = mockk<PointerEvent>().apply { every { type } returns Move }

        view.pointerMotionPassedThrough += listener

        expect(true) { view.shouldHandlePointerMotionEvent_(event) }

        verify(exactly = 0) { listener.moved(any()) }
    }

    @Test fun `pointer motion events passed through when not handled`() {
        val view     = object: View() {
            override fun shouldHandlePointerMotionEvent(event: PointerEvent): Boolean = false
        }
        val listener = mockk<PointerMotionListener>(relaxed = true)

        view.pointerMotionPassedThrough += listener

        listOf(Move, Drag).forEach { eventType ->
            expect(false) { view.shouldHandlePointerMotionEvent_(mockk<PointerEvent>().apply { every { type } returns eventType }) }
        }

        verify(exactly = 1) { listener.moved  (any()) }
        verify(exactly = 1) { listener.dragged(any()) }
    }

    // endregion
    // region Enabled, cursor, style, content direction

    @Test fun `enabled is false when parent is disabled`() {
        val child  = view()
        val parent = view().apply { enabled = false }

        parent.children_ += child

        expect(false) { child.enabled }

        parent.enabled = true

        expect(true) { child.enabled }
    }

    @Test fun `enabled same value ignored`() {
        val view     = view()
        val observer = mockk<PropertyObserver<View, Boolean>>(relaxed = true)

        view.enabledChanged += observer

        view.enabled = true

        verify(exactly = 0) { observer(any(), any(), any()) }
    }

    @Test fun `child enabled change under disabled parent not notified`() {
        val child    = view()
        val parent   = view()
        val observer = mockk<PropertyObserver<View, Boolean>>(relaxed = true)

        parent.children_ += child
        parent.enabled    = false

        child.enabledChanged += observer
        child.enabled         = false

        verify(exactly = 0) { observer(any(), any(), any()) }
        expect(false) { child.enabled }
    }

    @Test fun `cursor falls back to parent`() {
        val child  = view()
        val parent = view().apply { cursor = Crosshair }

        parent.children_ += child

        expect(Crosshair) { child.cursor }

        val own = mockk<Cursor>()

        child.cursor = own

        expect(own) { child.cursor }
    }

    @Test fun `cursor same value ignored`() {
        val view     = view()
        val observer = mockk<PropertyObserver<View, Cursor?>>(relaxed = true)

        view.cursorChanged += observer

        view.cursor = Crosshair
        view.cursor = Crosshair

        verify(exactly = 1) { observer(view, null, Crosshair) }
    }

    @Test fun `cursor change skips children with their own cursor`() {
        val own      = mockk<Cursor>()
        val child    = view().apply { cursor = own }
        val parent   = view()
        val observer = mockk<PropertyObserver<View, Cursor?>>(relaxed = true)

        parent.children_ += child

        child.cursorChanged += observer

        parent.cursor = Crosshair

        verify(exactly = 0) { observer(any(), any(), any()) }
    }

    @Test fun `style change same font ignored`() {
        val font     = mockk<Font>()
        val view     = view().apply { this.font = font }
        val observer = mockk<ChangeObserver<View>>(relaxed = true)

        view.styleChanged += observer

        view.font = font

        verify(exactly = 0) { observer(any()) }
    }

    @Test fun `content direction change notifies view and inheriting children`() {
        val inheriting = view()
        val overriding = view().apply { localContentDirection = LeftRight }
        val parent     = view().apply {
            children_ += inheriting
            children_ += overriding
        }
        val observer = mockk<ChangeObserver<View>>(relaxed = true)

        parent.contentDirectionChanged     += observer
        inheriting.contentDirectionChanged += observer
        overriding.contentDirectionChanged += observer

        parent.localContentDirection = RightLeft

        verify(exactly = 1) { observer(parent    ) }
        verify(exactly = 1) { observer(inheriting) }
        verify(exactly = 0) { observer(overriding) }

        parent.localContentDirection = RightLeft

        verify(exactly = 1) { observer(parent) }
    }

    @Test fun `mirrored depends on direction and mirror flag`() {
        val view = OpenView()

        expect(false) { view.mirrored }

        view.localContentDirection = RightLeft

        expect(true) { view.mirrored }

        view.disableMirroring()

        expect(false) { view.mirrored }

        view.localContentDirection = LeftRight

        expect(false) { view.mirrored }
    }

    @Test fun `needs mirror transform only when direction differs from parent`() {
        val child  = view()
        val parent = view()

        parent.children_ += child

        child.localContentDirection = RightLeft

        expect(true) { child.needsMirrorTransform }

        parent.localContentDirection = RightLeft

        expect(true ) { parent.needsMirrorTransform }
        expect(false) { child.needsMirrorTransform  }
    }

    @Test fun `needs mirror transform compares to display when top-level`() {
        val view    = view()
        val display = mockk<InternalDisplay>().apply {
            every { contentDirection } returns RightLeft
            every { mirrored         } returns false
        }

        view.addedToDisplay_(display, mockk(relaxed = true), null)
        view.updateNeedsMirror()

        // view resolves to RightLeft (from display) but display reports it is not mirrored
        expect(true) { view.needsMirrorTransform }
    }

    @Test fun `mirror transform change rerenders`() {
        val rm   = mockk<RenderManager>(relaxed = true)
        val view = view()

        view.addedToDisplay_(mockk(relaxed = true), rm, null)

        view.localContentDirection = RightLeft

        verify(atLeast = 1) { rm.render(view) }
    }

    // endregion
    // region Transform, camera, coordinate mapping

    @Test fun `bounding box accounts for transform`() {
        val view = view()

        view.forceBounds(Rectangle(0.0, 0.0, 10.0, 20.0))
        view.transform = Identity.scale(2.0, 3.0)

        expect(Rectangle(0.0, 0.0, 20.0, 60.0)) { view.boundingBox }

        view.forceBounds(Rectangle(10.0, 10.0, 10.0, 20.0))

        expect(Rectangle(20.0, 30.0, 20.0, 60.0)) { view.boundingBox }
    }

    @Test fun `contains accounts for transform`() {
        val view = object: View() {}

        view.forceBounds(Rectangle(0.0, 0.0, 10.0, 10.0))
        view.transform = Identity.scale(2.0, 2.0)

        expect(true ) { Point(15.0, 15.0) in view }
        expect(false) { Point(25.0, 25.0) in view }
    }

    @Test fun `contains delegates to intersects`() {
        val view = object: View() {
            override fun intersects(point: Point): Boolean = point.x < 5
        }

        view.forceBounds(Rectangle(0.0, 0.0, 100.0, 100.0))

        expect(true ) { Point(1.0, 50.0) in view }
        expect(false) { Point(6.0, 50.0) in view }
    }

    @Test fun `to parent works`() {
        val view = view().apply { forcePosition(10.0, 12.0) }

        expect(Point(11.0, 14.0)) { view.toParent(Point(1.0, 2.0)) }

        view.transform = Identity.scale(2.0, 2.0)

        expect(Point(22.0, 28.0)) { view.toParent(Point(1.0, 2.0)) }
    }

    @Test fun `to local from parent inverts to parent`() {
        val parent = view()
        val child  = view().apply {
            forcePosition(10.0, 12.0)
            transform = Identity.scale(2.0, 2.0)
        }

        parent.children_ += child

        val point = Point(3.0, 4.0)

        expect(point) { child.toLocal(child.toParent(point), from = parent) }
    }

    @Test fun `to local from null is from absolute`() {
        val root  = view()
        val child = view().apply { forcePosition(10.0, 12.0) }

        root.children_ += child

        val point = Point(100.0, 56.0)

        expect(child.fromAbsolute(point)) { child.toLocal(point, from = null) }
    }

    @Test fun `absolute mapping falls back to display for top-level views`() {
        val display = mockk<InternalDisplay>().apply {
            every { toAbsolute  (any()) } returns Point(1.0, 2.0)
            every { fromAbsolute(any()) } returns Point(100.0, 100.0)
        }
        val view = view().apply { forcePosition(10.0, 12.0) }

        view.addedToDisplay_(display, mockk(relaxed = true), null)

        expect(Point(1.0,  2.0 )) { view.toAbsolute  (Point(5.0, 5.0)) }
        expect(Point(90.0, 88.0)) { view.fromAbsolute(Point(5.0, 5.0)) }
    }

    @Test fun `camera change works`() {
        val view     = object: View() {}
        val old      = view.camera
        val new      = Camera(Origin, 100.0)
        val observer = mockk<PropertyObserver<View, Camera>>(relaxed = true)
        val rm       = mockk<RenderManager>(relaxed = true)

        view.addedToDisplay_(mockk(relaxed = true), rm, null)
        view.cameraChanged += observer

        view.camera = new

        expect(new) { view.camera }

        verifyOrder {
            rm.cameraChanged(view, old, new)
            observer(view, old, new)
        }
    }

    // endregion
    // region Display & accessibility lifecycle

    @Test fun `display lifecycle hooks invoked`() {
        var added   = 0
        var removed = 0
        val view    = object: View() {
            override fun addedToDisplay    () { added++   }
            override fun removedFromDisplay() { removed++ }
        }

        view.addedToDisplay_(mockk(), mockk(), null)

        expect(1) { added   }
        expect(0) { removed }

        view.removedFromDisplay_()

        expect(1) { added   }
        expect(1) { removed }
    }

    @Test fun `generation number follows parent`() {
        val parent = view()
        val child  = view()

        parent.children_ += child

        parent.addedToDisplay_(mockk(), mockk(), null)
        child.addedToDisplay_ (mockk(), mockk(), null)

        expect(0) { parent.generationNumber }
        expect(1) { child.generationNumber  }
    }

    @Test fun `accessibility synced when added to display`() {
        val am   = mockk<AccessibilityManager>(relaxed = true)
        val view = view {}

        view.addedToDisplay_(mockk(), mockk(), am)

        verify(exactly = 1) { am.syncLabel        (view) }
        verify(exactly = 1) { am.syncEnabled      (view) }
        verify(exactly = 1) { am.syncDescription  (view) }
        verify(exactly = 1) { am.syncNextReadOrder(view) }
        verify(exactly = 0) { am.roleAdopted      (view) }
    }

    @Test fun `accessibility role same value ignored`() {
        val role = mockk<AccessibilityRole>()
        val am   = mockk<AccessibilityManager>(relaxed = true)
        val view = object: View(accessibilityRole = role) {}

        view.addedToDisplay_(mockk(), mockk(), am)

        view.accessibilityRole = role

        verify(exactly = 0) { am.roleAbandoned(view) }
        verify(exactly = 1) { am.roleAdopted  (view) }
    }

    @Test fun `visibility change syncs accessibility`() {
        val am   = mockk<AccessibilityManager>(relaxed = true)
        val view = view {}

        view.addedToDisplay_(mockk(relaxed = true), mockk(relaxed = true), am)

        view.visible = false

        verify(exactly = 1) { am.syncVisibility(view) }
    }

    @Test fun `monitoring display rect notifies render manager`() {
        val rm   = mockk<RenderManager>(relaxed = true)
        val view = view {}

        view.addedToDisplay_(mockk(relaxed = true), rm, null)

        view.monitorsDisplayRect = true

        verify(exactly = 1) { rm.displayRectHandlingChanged(view, false, true) }
    }

    @Test fun `first render notified once`() {
        val view     = view {}
        val observer = mockk<ChangeObserver<View>>(relaxed = true)

        expect(false) { view.rendered }

        view.firstRender += observer

        view.rendered = true
        view.rendered = true

        expect(true) { view.rendered }

        verify(exactly = 1) { observer(view) }
    }

    // endregion
    // region Delegates

    @Test fun `behavior delegate installs and uninstalls behaviors`() {
        val log  = mutableListOf<String>()
        val view = BehaviorView(log)

        fun named(name: String) = object: Behavior<BehaviorView> {
            override fun install  (view: BehaviorView) { log += "install $name"   }
            override fun uninstall(view: BehaviorView) { log += "uninstall $name" }
        }

        val first  = named("first" )
        val second = named("second")

        view.current = first

        expect(listOf("before", "install first", "after")) { log.toList() }

        log.clear()

        view.current = second

        expect(listOf("before", "uninstall first", "install second", "after")) { log.toList() }

        log.clear()

        view.current = null

        expect(listOf("before", "uninstall second", "after")) { log.toList() }

        expect(listOf<Pair<Behavior<BehaviorView>?, Behavior<BehaviorView>?>>(null to first, first to second, second to null)) { view.changes }
    }

    @Test fun `behavior delegate with only before callback works`() {
        val calls = mutableListOf<Pair<Behavior<BehaviorView2>?, Behavior<BehaviorView2>?>>()
        val view  = BehaviorView2(calls)
        val b     = object: Behavior<BehaviorView2> {}

        view.current = b

        expect(listOf<Pair<Behavior<BehaviorView2>?, Behavior<BehaviorView2>?>>(null to b)) { calls }
        expect(b) { view.current }
    }

    @Test fun `behavior delegate applies view settings`() {
        val view = BehaviorView(mutableListOf())
        val clip = PolyClipPath(Rectangle(10, 10))

        view.current = object: Behavior<BehaviorView> {
            override fun clipCanvasToBounds   (view: BehaviorView) = false
            override fun childrenClipPath     (view: BehaviorView) = clip
            override fun mirrorWhenRightToLeft(view: BehaviorView) = false
        }

        expect(false) { view.clipCanvasToBounds_  }
        expect(clip ) { view.childrenClipPath_    }
        expect(false) { view.mirrorWhenRightLeft  }
    }

    @Test fun `behavior delegate restores settings from before uninstall when cleared`() {
        val view = BehaviorView(mutableListOf())
        val clip = PolyClipPath(Rectangle(10, 10))

        view.current = object: Behavior<BehaviorView> {
            override fun clipCanvasToBounds   (view: BehaviorView) = false
            override fun childrenClipPath     (view: BehaviorView) = clip
            override fun mirrorWhenRightToLeft(view: BehaviorView) = false

            // uninstall tampers with the view; clearing the behavior should undo that
            override fun uninstall(view: BehaviorView) {
                view.clipCanvasToBounds_ = true
                view.childrenClipPath_   = null
                view.mirrorWhenRightLeft_ = true
            }
        }

        view.current = null

        expect(false) { view.clipCanvasToBounds_  }
        expect(clip ) { view.childrenClipPath_    }
        expect(false) { view.mirrorWhenRightLeft  }
        expect(null ) { view.current              }
    }

    @Test fun `behavior delegate rerenders when installed`() {
        val rm   = mockk<RenderManager>(relaxed = true)
        val view = BehaviorView(mutableListOf())

        view.addedToDisplay_(mockk(relaxed = true), rm, null)

        view.current = object: Behavior<BehaviorView> {}

        verify(atLeast = 1) { rm.render(view) }
    }

    @Test fun `render property delegate rerenders and notifies`() {
        val rm   = mockk<RenderManager>(relaxed = true)
        val view = RenderPropertyView()

        view.addedToDisplay_(mockk(relaxed = true), rm, null)

        view.level = 3

        expect(3) { view.level }
        expect(listOf(0 to 3)) { view.changes }

        verify(exactly = 1) { rm.render(view) }
    }

    @Test fun `style property delegate notifies view and filtered children`() {
        val child    = view()
        val parent   = StyledView()
        val observer = mockk<ChangeObserver<View>>(relaxed = true)

        parent.children_ += child

        parent.styleChanged += observer
        child.styleChanged  += observer

        parent.level = 1

        expect(1) { parent.level }
        expect(listOf(0 to 1)) { parent.changes }

        verify(exactly = 1) { observer(parent) }
        verify(exactly = 1) { observer(child ) }

        parent.local = 2

        expect(listOf(0 to 1, 0 to 2)) { parent.changes }

        verify(exactly = 2) { observer(parent) }
        verify(exactly = 1) { observer(child ) } // filtered out
    }

    // endregion
    // region View builder

    @Test fun `view builder manages children`() {
        val a = view {}
        val b = view {}
        val c = view {}
        val d = view {}

        var kids = emptyList<View>()

        val parent = view {
            +a
            +listOf(b, c, d)
            -d
            -listOf(b)

            kids = children.toList()
        }

        expect(listOf(a, c)) { kids     }
        expect(parent      ) { a.parent }
        expect(null        ) { d.parent }
        expect(null        ) { b.parent }
    }

    @Test fun `view builder lambdas used`() {
        var renderCalls  = 0
        var addedCalls   = 0
        var removedCalls = 0

        val view = view {
            render             = { renderCalls++  }
            addedToDisplay     = { addedCalls++   }
            removedFromDisplay = { removedCalls++ }
            shouldYieldFocus   = { false          }
            contains           = { true           }
            intersects         = { false          }
        }

        view.render(mockk(relaxed = true))

        expect(1) { renderCalls }

        view.addedToDisplay_(mockk(), mockk(), null)

        expect(1) { addedCalls }

        view.removedFromDisplay_()

        expect(1) { removedCalls }

        expect(false) { view.shouldYieldFocus()           }
        expect(true ) { Point(1000.0, 1000.0) in view     }
        expect(false) { view intersects Point(1.0, 1.0)   }
    }

    @Test fun `view builder defaults delegate to view`() {
        val view = view {}

        view.forceBounds(Rectangle(0.0, 0.0, 10.0, 10.0))

        expect(true ) { view.shouldYieldFocus()             }
        expect(true ) { Point(1.0,  1.0 ) in view           }
        expect(false) { Point(50.0, 50.0) in view           }
        expect(true ) { view intersects Point(1.0, 1.0)     }
        expect(false) { view intersects Point(50.0, 50.0)   }
    }

    @Test fun `view builder exposes container settings`() {
        val expectedLayout = simpleLayout { _, _, current, _, _ -> current }
        val expectedPolicy = mockk<FocusTraversalPolicy>()

        val view = view {
            layout               = expectedLayout
            insets               = Insets(4.0)
            isFocusCycleRoot     = true
            focusTraversalPolicy = expectedPolicy
        }

        expect(expectedLayout) { view.layout_               }
        expect(4.0           ) { view.insets_.top           }
        expect(true          ) { view.isFocusCycleRoot_     }
        expect(expectedPolicy) { view.focusTraversalPolicy_ }
    }

    // endregion
    // region Misc

    @Test fun `traversal keys can be cleared`() {
        val view = object: View() {}

        view[Forward] = setOf(mockk<KeyState>())

        view[Forward] = null

        expect(null) { view[Forward] }
    }

    @Test fun `additional setters work`() {
        validateSetter(View::acceptsThemes,  false          )
        validateSetter(View::dragRecognizer, mockk<DragRecognizer>())
        validateSetter(View::dropReceiver,   mockk<DropReceiver>()  )
    }

    @Test fun `scroll to horizontal works`() {
        val view  = view { suggestSize(Size(100)) }
        val panel = spyk<ScrollPanel>().apply { suggestSize(Size(50)) }

        panel.content = view

        view.scrollToHorizontal(2.0..8.0)

        verify { panel.scrollHorizontallyToVisible(2.0..8.0) }
    }

    @Test fun `scroll to vertical works`() {
        val view  = view { suggestSize(Size(100)) }
        val panel = spyk<ScrollPanel>().apply { suggestSize(Size(50)) }

        panel.content = view

        view.scrollToVertical(2.0..8.0)

        verify { panel.scrollVerticallyToVisible(2.0..8.0) }
    }

    @Test fun `scroll to point works`() {
        val view  = view { suggestSize(Size(100)) }
        val panel = spyk<ScrollPanel>().apply { suggestSize(Size(50)) }

        panel.content = view

        val point = Point(5.0, 60.0)

        view.scrollTo(point)

        verify { panel.scrollToVisible(point) }
    }

    @Test fun `scroll helpers ignore views outside scroll panels`() {
        val orphan = view()
        val nested = view().apply { children_ += view() }

        // None of these should throw
        orphan.scrollTo(Point(1.0, 1.0))
        orphan.scrollTo(Rectangle(1, 1, 2, 2))
        orphan.scrollToHorizontal(0.0..1.0)
        orphan.scrollToVertical  (0.0..1.0)
        nested.scrollTo(Point(1.0, 1.0))
    }

    // endregion

    private class SubView: View() {
        public override fun filterKeyEvent          (event: KeyEvent               ) { super.filterKeyEvent          (event   ) }
        public override fun handleKeyEvent          (event: KeyEvent               ) { super.handleKeyEvent          (event   ) }
        public override fun handlePointerEvent      (event: PointerEvent           ) { super.handlePointerEvent      (event   ) }
        public override fun handleDisplayRectEvent  (old: Rectangle, new: Rectangle) { super.handleDisplayRectEvent  (old, new) }
        public override fun handlePointerMotionEvent(event: PointerEvent           ) { super.handlePointerMotionEvent(event   ) }
    }

    private fun validateFocusChanged(gained: Boolean, block: (View, PropertyObserver<View, Boolean>) -> Unit) {
        val view     = object: View() {}
        val observer = mockk<PropertyObserver<View, Boolean>>()

        view.focusChanged += observer

        // Force the View to have focus if we are testing losing it
        if (!gained) {
            view.focusGained_(null)
        }

        if (gained) {
            view.focusGained_(null)
        } else {
            view.focusLost_(null)
        }

        block(view, observer)
    }

    private fun validateKeyChanged(event: KeyEvent, block: (KeyListener, KeyEvent) -> Unit) {
        val view     = object: View() {}
        val listener = mockk<KeyListener>()

        view.keyChanged += listener

        view.handleKeyEvent_(event)

        block(listener, event)
    }

    private fun validatePointerChanged(event: PointerEvent, block: (PointerListener, PointerEvent) -> Unit) {
        val view     = object: View() {}
        val listener = mockk<PointerListener>()

        view.pointerChanged += listener

        view.handlePointerEvent_(event)

        block(listener, event)
    }

    private fun validatePointerFilter(event: PointerEvent, block: (PointerListener, PointerEvent) -> Unit) {
        val view     = object: View() {}
        val listener = mockk<PointerListener>()

        view.pointerFilter += listener

        view.filterPointerEvent_(event)

        block(listener, event)
    }

    private fun validatePointerMotionChanged(event: PointerEvent, block: (PointerMotionListener, PointerEvent) -> Unit) {
        val view     = object: View() {}
        val listener = mockk<PointerMotionListener>()

        view.pointerMotionChanged += listener

        view.handlePointerMotionEvent_(event)

        block(listener, event)
    }

    private fun validatePointerMotionFilter(event: PointerEvent, block: (PointerMotionListener, PointerEvent) -> Unit) {
        val view     = object: View() {}
        val listener = mockk<PointerMotionListener>()

        view.pointerMotionFilter += listener

        view.filterPointerMotionEvent_(event)

        block(listener, event)
    }

    private fun validateChanged(property: KMutableProperty1<View, Boolean>, changed: KProperty1<View, PropertyObservers<View, Boolean>>) {
        val view     = object: View() {}
        val old      = property.get(view)
        val observer = mockk<PropertyObserver<View, Boolean>>()

        changed.get(view).plusAssign(observer)

        property.set(view, !property.get(view))

        verify(exactly = 1) { observer(view, old, property.get(view)) }
    }

    private fun <T: Any?> validateStyleChanged(property: KMutableProperty1<View, T>, value: T) {
        val grandChild  = object: View() {}
        val child       = object: View() {}.apply { children_ += grandChild }
        val parent      = object: View() {}.apply { children_ += child      }
        val grandParent = object: View() {}.apply { children_ += parent     }
        val observer    = mockk<ChangeObserver<View>>()

        // child and grandChild shouldn't trigger event since it has the same property as grandParent will change to
        property.set(child, value)

        grandChild.styleChanged  += observer
        child.styleChanged       += observer
        parent.styleChanged      += observer
        grandParent.styleChanged += observer

        property.set(grandParent, value)

        verify(exactly = 0) { observer(grandChild ) }
        verify(exactly = 0) { observer(child      ) }
        verify(exactly = 1) { observer(parent     ) }
        verify(exactly = 1) { observer(grandParent) }
    }

    private fun <T> validateDefault(p: KProperty1<View, T>, default: T?) {
        expect(default, "$p defaults to $default") { p.get(object: View() {}) }
    }

    private fun <T> validateSetter(p: KMutableProperty1<View, T>, value: T, shouldRerender: Boolean = false) {
        view {}.also { view ->
            val renderManager = if (shouldRerender) {
                val display       = mockk<InternalDisplay>()
                val renderManager = mockk<RenderManager>()

                view.addedToDisplay_(display, renderManager, null)
                renderManager
            } else null

            p.set(view, value)

            expect(value, "$p set to $value") { p.get(view) }

            renderManager?.let {
                verify(exactly = 1) { it.render(view) }
            }
        }
    }

    private fun view(): View = object: View() {}.apply { suggestSize(10.0, 10.0) }

    private fun validateKeyFilter(event: KeyEvent, block: (KeyListener, KeyEvent) -> Unit) {
        val view     = object: View() {}
        val listener = mockk<KeyListener>(relaxed = true)

        view.keyFilter += listener

        view.filterKeyEvent_(event)

        block(listener, event)
    }

    /** Exposes protected members needed by the gap tests */
    private class OpenView: View() {
        public override var layout: Layout? get() = super.layout; set(new) { super.layout = new }

        public override var insets: Insets get() = super.insets; set(new) { super.insets = new }

        public override val children: ObservableList<View> get() = super.children

        public override fun relayout   () { super.relayout   () }
        public override fun relayoutNow() { super.relayoutNow() }

        fun callRevalidate() { revalidate() }

        fun disableMirroring() { mirrorWhenRightLeft = false }
    }

    private class BehaviorView(private val log: MutableList<String>): View() {
        val changes = mutableListOf<Pair<Behavior<BehaviorView>?, Behavior<BehaviorView>?>>()

        var current: Behavior<BehaviorView>? by behavior(
            beforeChange = { old, new -> log += "before"; changes += old to new },
            afterChange  = { _,   _   -> log += "after"                         }
        )
    }

    private class BehaviorView2(private val calls: MutableList<Pair<Behavior<BehaviorView2>?, Behavior<BehaviorView2>?>>): View() {
        var current: Behavior<BehaviorView2>? by behavior(beforeChange = { old, new -> calls += old to new })
    }

    private class RenderPropertyView: View() {
        val changes = mutableListOf<Pair<Int, Int>>()

        var level: Int by renderProperty(0) { old, new -> changes += old to new }
    }

    private class StyledView: View() {
        val changes = mutableListOf<Pair<Int, Int>>()

        var level: Int by observableStyleProperty(initial = 0, filter = { true  }, onChanged = { old, new -> changes += old to new })
        var local: Int by observableStyleProperty(initial = 0, filter = { false }, onChanged = { old, new -> changes += old to new })
    }
}