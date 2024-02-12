package io.nacular.doodle.deviceinput

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.View
import io.nacular.doodle.event.KeyCode.Companion.Space
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyState
import io.nacular.doodle.event.KeyState.Type.Down
import io.nacular.doodle.event.KeyText
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Backward
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Downward
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Forward
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Upward
import io.nacular.doodle.system.KeyInputService
import kotlin.test.Test

/**
 * Created by Nicholas Eddy on 9/15/20.
 */
class KeyboardFocusManagerTests {
    @Test fun `stops listening to key input service on shutdown`() {
        val keyInputService = mockk<KeyInputService>()

        val manager = KeyboardFocusManagerImpl(keyInputService, mockk(), mockk())

        manager.shutdown()

        verifyOrder {
            keyInputService += manager
            keyInputService -= manager
        }
    }

    @Test  fun `notifies of event in correct order`() {
        val keyInputService = mockk<KeyInputService>()
        val focusedView     = view()
        val focusManager    = focusManager(focusedView)
        val preprocessor    = mockk<Preprocessor>()
        val postprocessor   = mockk<Postprocessor>()

        val manager = KeyboardFocusManagerImpl(keyInputService, focusManager, mockk())

        manager += preprocessor
        manager += postprocessor

        val keyState = KeyState(Space, KeyText(" "), emptySet(), Down)

        manager(keyState)

        val keyEvent = KeyEvent(focusedView, keyState)

        verifyOrder {
            preprocessor(keyEvent)
            focusedView.filterKeyEvent_(keyEvent)
            focusedView.handleKeyEvent_(keyEvent)
            postprocessor(keyEvent)
        }
    }

    @Test  fun `event sinks and bubbles as expected`() {
        val grandParent  = spyk(Container(), name = "grand-parent")
        val parent       = spyk(Container(), name = "parent"      )
        val child        = view()

        every { parent.parent } returns grandParent
        every { child.parent  } returns parent

        val keyInputService = mockk<KeyInputService>()
        val focusManager    = focusManager(child)
        val preprocessor    = mockk<Preprocessor>()
        val postprocessor   = mockk<Postprocessor>()

        val manager = KeyboardFocusManagerImpl(keyInputService, focusManager, defaultKeys())

        manager += preprocessor
        manager += postprocessor

        val keyState = KeyState(Space, KeyText(" "), emptySet(), Down)

        manager(keyState)

        val keyEvent = KeyEvent(child, keyState)

        verifyOrder {
            preprocessor(keyEvent)
            grandParent.filterKeyEvent_(keyEvent)
            parent.filterKeyEvent_     (keyEvent)
            child.filterKeyEvent_      (keyEvent)
            child.handleKeyEvent_      (keyEvent)
            parent.handleKeyEvent_     (keyEvent)
            grandParent.handleKeyEvent_(keyEvent)
            postprocessor              (keyEvent)
        }
    }

    @Test  fun `consumed event does not sinks or bubble`() {
        val grandParent  = spyk(Container(), name = "grand-parent")
        val parent       = spyk(Container(), name = "parent"      )
        val child        = view()

        every { parent.parent } returns grandParent
        every { child.parent  } returns parent

        val event = slot<KeyEvent>()
        every { parent.filterKeyEvent_(capture(event)) } answers {
            event.captured.consume()
        }

        val keyInputService = mockk<KeyInputService>()
        val focusManager    = focusManager(child)
        val preprocessor    = mockk<Preprocessor>()
        val postprocessor   = mockk<Postprocessor>()

        val manager = KeyboardFocusManagerImpl(keyInputService, focusManager, defaultKeys())

        manager += preprocessor
        manager += postprocessor

        val keyState = KeyState(Space, KeyText(" "), emptySet(), Down)

        manager(keyState)

        val keyEvent = KeyEvent(child, keyState).also {
            // there seems to be a bug in Mockk where the data provided in the call is live
            // and changes to it affect verification. so need to have the comparison contain
            // the consumed = true property as well for now
            it.consume()
        }

        verifyOrder {
            preprocessor               (keyEvent)
            grandParent.filterKeyEvent_(keyEvent)
            parent.filterKeyEvent_     (keyEvent)
        }

        verify(exactly = 0) {
            child.filterKeyEvent_      (any())
            child.handleKeyEvent_      (any())
            parent.handleKeyEvent_     (any())
            grandParent.handleKeyEvent_(any())
            postprocessor              (any())
        }
    }

    @Test  fun `preprocessor suppresses event when consumed`() {
        val keyInputService = mockk<KeyInputService>()
        val focusedView     = view()
        val focusManager    = focusManager(focusedView)

        val event           = slot<KeyEvent>()
        val preprocessor    = mockk<Preprocessor>().apply {
            every { this@apply(capture(event)) } answers {
                event.captured.consume()
            }
        }

        val postprocessor = mockk<Postprocessor>()

        val manager = KeyboardFocusManagerImpl(keyInputService, focusManager, mockk())

        manager += preprocessor
        manager += postprocessor

        val keyState = KeyState(Space, KeyText(" "), emptySet(), Down)

        manager(keyState)

        verify(exactly = 1) { preprocessor(event.captured)       }
        verify(exactly = 0) { focusedView.filterKeyEvent_(any()) }
        verify(exactly = 0) { focusedView.handleKeyEvent_(any()) }
        verify(exactly = 0) { postprocessor(any())               }
    }

    @Test fun `view suppresses event when consumed`() {
        val event           = slot<KeyEvent>()
        val keyInputService = mockk<KeyInputService>()
        val focusedView     = view().apply {
            every { handleKeyEvent_(capture(event)) } answers {
                event.captured.consume()
            }
        }
        val focusManager  = focusManager(focusedView)
        val preprocessor  = mockk<Preprocessor>()
        val postprocessor = mockk<Postprocessor>()

        val manager = KeyboardFocusManagerImpl(keyInputService, focusManager, mockk())

        manager += preprocessor
        manager += postprocessor

        val keyState = KeyState(Space, KeyText(" "), emptySet(), Down)

        manager(keyState)

        verifyOrder {
            preprocessor(event.captured)
            focusedView.filterKeyEvent_(event.captured)
            focusedView.handleKeyEvent_(event.captured)
        }

        verify(exactly = 0) { postprocessor(any()) }
    }

    @Test fun `no notifications when listener removed`() {
        val keyInputService = mockk<KeyInputService>()
        val focusedView     = view()
        val focusManager    = focusManager(focusedView)
        val preprocessor    = mockk<Preprocessor>()
        val postprocessor   = mockk<Postprocessor>()

        val manager = KeyboardFocusManagerImpl(keyInputService, focusManager, mockk())

        manager += preprocessor
        manager += postprocessor

        manager -= preprocessor
        manager -= postprocessor

        val keyState = KeyState(Space, KeyText(" "), emptySet(), Down)

        manager(keyState)

        val keyEvent = KeyEvent(focusedView, keyState)

        verify(exactly = 1) { focusedView.filterKeyEvent_(keyEvent) }
        verify(exactly = 1) { focusedView.handleKeyEvent_(keyEvent) }
        verify(exactly = 0) { preprocessor (any()) }
        verify(exactly = 0) { postprocessor(any()) }
    }

    @Test fun `no notifications when no focus owner`() {
        val keyInputService = mockk<KeyInputService>()
        val focusManager    = focusManager()
        val preprocessor    = mockk<Preprocessor>()
        val postprocessor   = mockk<Postprocessor>()

        val manager = KeyboardFocusManagerImpl(keyInputService, focusManager, mockk())

        manager += preprocessor
        manager += postprocessor

        val keyState = KeyState(Space, KeyText(" "), emptySet(), Down)

        manager(keyState)

        verify(exactly = 0) { preprocessor (any()) }
        verify(exactly = 0) { postprocessor(any()) }
    }

    @Test fun `handles focus movement`() {
        val key = mockk<KeyState>()

        verifyFocusTraversal(Forward,  key) { view, _ -> every { view[Forward ] } returns setOf(key) }
        verifyFocusTraversal(Forward,  key) { _, keys ->         keys[Forward ]         = setOf(key) }
        verifyFocusTraversal(Backward, key) { view, _ -> every { view[Backward] } returns setOf(key) }
        verifyFocusTraversal(Backward, key) { _, keys ->         keys[Backward]         = setOf(key) }
        verifyFocusTraversal(Upward,   key) { view, _ -> every { view[Upward  ] } returns setOf(key) }
        verifyFocusTraversal(Upward,   key) { _, keys ->         keys[Upward  ]         = setOf(key) }
        verifyFocusTraversal(Downward, key) { view, _ -> every { view[Downward] } returns setOf(key) }
        verifyFocusTraversal(Downward, key) { _, keys ->         keys[Downward]         = setOf(key) }
    }

    @Test fun `no focus downward on non-focus cycle root`() {
        val key = mockk<KeyState>()
        val keyInputService = mockk<KeyInputService>()
        val focusedView     = view().apply {
            every { this@apply[any()   ] } returns null
            every { this@apply[Downward] } returns setOf(key)
            every { isFocusCycleRoot_    } returns false
        }
        val focusManager    = focusManager(focusedView)
        val traversalKeys   = mutableMapOf<TraversalType, Set<KeyState>>()

        val manager = KeyboardFocusManagerImpl(keyInputService, focusManager, traversalKeys)

        manager(key)

        verify(exactly = 0) {
            focusManager.moveFocusDownward(from = focusedView)
        }
    }

    private fun view() = mockk<View>().apply {
        every { parent } returns null
    }

    private fun defaultKeys() = mockk<Map<TraversalType, Set<KeyState>>> {
        every { this@mockk[any()] } returns emptySet<KeyState>()
    }

    private fun verifyFocusTraversal(type: TraversalType, keyState: KeyState, config: (View, MutableMap<TraversalType, Set<KeyState>>) -> Unit) {
        val keyInputService = mockk<KeyInputService>()
        val focusedView     = view().apply {
            every { this@apply[any()] } returns null
            every { isFocusCycleRoot_ } returns true
        }
        val focusManager    = focusManager(focusedView)
        val traversalKeys   = mutableMapOf<TraversalType, Set<KeyState>>()

        config(focusedView, traversalKeys)

        val manager = KeyboardFocusManagerImpl(keyInputService, focusManager, traversalKeys)

        manager(keyState)

        verify(exactly = 1) {
            when (type) {
                Forward  -> focusManager.moveFocusForward (from = focusedView)
                Backward -> focusManager.moveFocusBackward(from = focusedView)
                Upward   -> focusManager.moveFocusUpward  (from = focusedView)
                Downward -> focusManager.moveFocusDownward(from = focusedView)
            }
        }
    }

    private fun focusManager(focusedView: View? = null) = mockk<FocusManager>().apply {
        every { focusOwner } returns focusedView
    }
}