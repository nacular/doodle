package io.nacular.doodle.theme

import JsName
import io.mockk.MockKMatcherScope
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.nacular.doodle.core.ChildObserver
import io.nacular.doodle.core.ContentDirection
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.view
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.focus.FocusTraversalPolicy
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.utils.ChangeObserver
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.PropertyObserver
import io.nacular.doodle.utils.PropertyObservers
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 9/14/20.
 */
class ThemeTests {
    @Test @JsName("selectingNewThemeWorks")
    fun `selecting new theme works`() {
        val child1      = viewAcceptingTheme()
        val popupChild1 = viewAcceptingTheme()
        val popup1      = viewAcceptingTheme()
        val popup2      = viewAcceptingTheme().apply {
            every { children_ } returns ObservableList(listOf(popupChild1))
        }
        val parent1     = viewAcceptingTheme()
        val parent2     = viewAcceptingTheme().apply {
            every { children_ } returns ObservableList(listOf(child1))
        }

        val display = mockk<InternalDisplay>().apply {
            every { popups   } returns listOf(popup1, popup2)
            every { children } returns ObservableList(listOf(parent1, parent2))
        }
        val manager  = ThemeManagerImpl(display)
        val sequence = slot<Sequence<View>>()

        val newTheme = mockk<Theme>().apply {
            every { install(any(), capture(sequence)) } answers {}
        }
        val observer = mockk<PropertyObserver<ThemeManager, Theme?>>()

        manager.selectionChanged += observer

        listOf(
            null to newTheme,
            newTheme to null
        ).forEach { (old, new) ->

            manager.selected = new

            expect(new) { manager.selected }

            when {
                new != null -> {
                    expect(true) { new in manager.themes }
                    // FIXME: Not sure why this fails for mockk
//                    verify(exactly = 1) { new.install(display, seqEq(sequenceOf(popup1, popup2, parent1, parent2, popupChild1, child1))) }
                    verify(exactly = 1) { new.install(display, any()) }
                    assertContentEquals(sequence.captured.toList(), listOf(popup1, popup2, parent1, parent2, popupChild1, child1))
                }
            }

            verify(exactly = 1) { observer(manager, old, new) }
        }
    }

    private val dummyDisplay = object: InternalDisplay {
        override var cursor = null as Cursor?
        override val size                       = Size.Empty
        override var layout                     = null as Layout?
        override var insets                     = Insets.None
        override var transform: AffineTransform = Identity
        override val children                   = ObservableList<View>()
        override val popups                     = emptyList<View>()
        override val cursorChanged              = mockk<PropertyObservers<Display, Cursor?>>()
        override val sizeChanged                = mockk<PropertyObservers<Display, Size>>()
        override var focusTraversalPolicy       = null as FocusTraversalPolicy?
        override val contentDirectionChanged    = mockk<Pool<ChangeObserver<Display>>>()
        override var contentDirection           = ContentDirection.LeftRight
        override var mirrorWhenRightLeft        = true
        override val mirroringChanged           = mockk<Pool<ChangeObserver<Display>>>()
        override val childrenChanged            = mockk<Pool<ChildObserver<Display>>>()

        override fun toAbsolute(point: Point) = point

        override fun fromAbsolute(point: Point) = point

        override fun fill(fill: Paint) {}

        override fun child(at: Point) = null

        override fun ancestorOf(view: View) = false

        override fun relayout() {}

        override fun repaint() {}

        override fun hidePopup(view: View) {}

        override fun showPopup(view: View) {}
    }

    @Test @JsName("installsThemeOnUpdate")
    fun `installs theme on update`() {
        val manager  = ThemeManagerImpl(dummyDisplay)
        val newTheme = mockk<Theme>()
        val view     = view {}

        manager.selected = newTheme

        manager.update(view)

        verify(exactly = 1) { newTheme.install(dummyDisplay, seqEq(sequenceOf(view))) }
    }

    @Test @JsName("respectsViewAcceptsThemes")
    fun `respects View acceptsThemes`() {
        val manager  = ThemeManagerImpl(dummyDisplay)
        val newTheme = mockk<Theme>()
        val view     = mockk<View>().apply {
            every { acceptsThemes } returns false
        }

        manager.selected = newTheme

        manager.update(view)

        verify(exactly = 0) { newTheme.install(dummyDisplay, seqEq(sequenceOf(view))) }
    }

    private fun <T> MockKMatcherScope.seqEq(seq: Sequence<T>) = match<Sequence<T>> {
        it.toList() == seq.toList()
    }
    private fun viewAcceptingTheme() = mockk<View>().apply {
        every { acceptsThemes } returns true
    }
}