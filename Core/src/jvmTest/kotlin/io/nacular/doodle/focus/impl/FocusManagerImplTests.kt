package io.nacular.doodle.focus.impl

import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.JvmMockKGateway
import io.mockk.impl.instantiation.JvmAnyValueGenerator
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.focus.FocusTraversalPolicy
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Backward
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Downward
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Forward
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Upward
import io.nacular.doodle.utils.PropertyObserver
import org.junit.jupiter.api.BeforeAll
import kotlin.reflect.KClass
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 1/27/19.
 */
class FocusManagerImplTests {
    private val focusabilityChecker = DefaultFocusabilityChecker()

    private data class Focusability(val focusable: Boolean, val enabled: Boolean, val visible: Boolean)

    @Test fun `no default focus owner`() {
        expect(null) { FocusManagerImpl(mockk(), mockk(), focusabilityChecker).focusOwner }
    }

    @Test fun `no default focus-cycle-root`() {
        expect(null) { FocusManagerImpl(mockk(), mockk(), focusabilityChecker).focusCycleRoot }
    }

    @Test fun `obeys focusability checker`() {
        listOf(true, false).forEach { expected ->
            expect(expected) {
                val focusabilityChecker = mockk<FocusabilityChecker>().apply { every { this@apply(any()) } returns expected }

                FocusManagerImpl(mockk(), mockk(), focusabilityChecker).focusable(mockk())
            }
        }
    }

    @Test fun `validate focusability`() {
        createFocusablePermutations().forEach { (view, expected) ->
            expect(expected) { DefaultFocusabilityChecker()(view) }
        }
    }

    @Test fun `request focus no-ops if not focusable`() {
        listOf(true, false).forEach { expected ->
            val focusabilityChecker = mockk<FocusabilityChecker>().apply { every { this@apply(any()) } returns expected }

            FocusManagerImpl(mockk(), mockk(), focusabilityChecker).apply {
                val view     = mockk<View>()
                val listener = mockk<(FocusManager, View?, View?) -> Unit>()

                focusChanged += listener

                requestFocus(view)

                verify(exactly = if (expected) 1 else 0) {
                    view.focusGained_(null)
                    listener(this@apply, null, view)
                }

                expect(expected) { focusOwner == view }
            }
        }
    }

    @Test fun `request focus with previous`() {
        val previous = focusableView()
        val view     = focusableView()

        FocusManagerImpl(mockk(), mockk(), focusabilityChecker).apply {
            val listener = mockk<(FocusManager, View?, View?) -> Unit>()

            focusChanged += listener

            requestFocus(previous) // move focus to previous
            requestFocus(view    )

            verify {
                previous.focusLost_(view    )
                view.focusGained_  (previous)
                listener(this@apply, previous, view)
            }

            expect(view) { focusOwner }
        }
    }

    @Test fun `request focus to focus owner no-ops`() {
        val view = focusableView()

        FocusManagerImpl(mockk(), mockk(), focusabilityChecker).apply {
            val listener = mockk<(FocusManager, View?, View?) -> Unit>()

            focusChanged += listener

            requestFocus(view) // give focus already
            requestFocus(view)

            verify(exactly = 1) {
                view.focusGained_(null)
                listener(this@apply, null, view)
            }

            expect(view) { focusOwner }
        }
    }

    @Test fun `request focus when disabled no-ops`() {
        val view = focusableView()

        FocusManagerImpl(mockk(), mockk(), focusabilityChecker).apply {
            val listener = mockk<(FocusManager, View?, View?) -> Unit>()

            focusChanged += listener

            enabled = false

            requestFocus(view) // should no-op

            verify(exactly = 0) {
                view.focusGained_(any())
                listener(this@apply, null, view)
            }

            expect(null) { focusOwner }
        }
    }

    @Test fun `focus moved when owner disabled`() {
        verifyFocusMoves {
            val propertyChanged = slot<PropertyObserver<View, Boolean>>()

            every { it.enabledChanged += capture(propertyChanged) } just Runs

            {
                every { it.enabled } returns false
                propertyChanged.captured(it, true, false)
            }
        }
    }

    @Test fun `focus moved when owner no longer focusable`() {
        verifyFocusMoves {
            val propertyChanged = slot<PropertyObserver<View, Boolean>>()

            every { it.focusabilityChanged += capture(propertyChanged) } just Runs

            {
                every { it.focusable } returns false
                propertyChanged.captured(it, true, false)
            }
        }
    }

    @Test fun `focus moved when owner no longer visible`() {
        verifyFocusMoves {
            val propertyChanged = slot<PropertyObserver<View, Boolean>>()

            every { it.visibilityChanged += capture(propertyChanged) } just Runs

            {
                every { it.visible } returns false
                propertyChanged.captured(it, true, false)
            }
        }
    }

    @Test fun `focus moved when owner removed directly`() {
        verifyFocusMoves {
            val propertyChanged = slot<PropertyObserver<View, View?>>()
            val parent = focusableView()

            every { it.parent } returns parent
            every { it.parentChanged += capture(propertyChanged) } just Runs

            {
                propertyChanged.captured(it, parent, null)
            }
        }
    }

    @Test fun `focus moved when ancestor removed`() {
        // Assume focus goes to null since helper does not configure a default
        verifyFocusMoves(to = null) {
            val propertyChanged = slot<PropertyObserver<View, Boolean>>()
            val parent = focusableView()

            every { it.parent                                     } returns parent
            every { it.displayChanged += capture(propertyChanged) } just Runs

            {
                propertyChanged.captured(it, true, false)
            }
        }
    }

    @Test @Ignore
    // This test breaks b/c the functionality was changed in FocusManager, so it does
    // move focus if a top-level view is no longer displayed.
    fun `no-op on display changed path of direct removal`() {
        val displayChangedListener = slot<PropertyObserver<View, Boolean>>()

        val view = focusableView().apply {
            every { displayChanged += capture(displayChangedListener) } just Runs
        }

        FocusManagerImpl(createDisplayWithSingleView(), mockk(), focusabilityChecker).apply {
            val listener = mockk<(FocusManager, View?, View?) -> Unit>()

            focusChanged += listener

            requestFocus(view) // give focus already

            displayChangedListener.captured(view, true, false)

            verify(exactly = 0) {
                view.focusLost_(any())
                listener(this@apply, view, any())
            }

            expect(view) { focusOwner }
        }
    }

    @Test fun `focus cleared when disabled`() {
        val view = focusableView()

        FocusManagerImpl(createDisplayWithSingleView(), mockk(), focusabilityChecker).apply {
            val listener = mockk<(FocusManager, View?, View?) -> Unit>()

            focusChanged += listener

            requestFocus(view)

            enabled = false

            verify(exactly = 1) {
                view.focusLost_(null)
                listener(this@apply, view, null)
            }

            expect(null) { focusOwner }
        }
    }

    @Test fun `focus returned to previous owner when enabled`() {
        val view = focusableView()

        FocusManagerImpl(createDisplayWithSingleView(), mockk(), focusabilityChecker).apply {
            val listener = mockk<(FocusManager, View?, View?) -> Unit>()

            focusChanged += listener

            requestFocus(view)

            enabled = false
            enabled = true

            verify(exactly = 2) {
                view.focusGained_(null)
                listener(this@apply, null, view)
            }

            expect(view) { focusOwner }
        }
    }

    @Test fun `handles focus forward backward and downward`() {
        listOf(Forward, Backward, Downward).forEach { type ->
            listOf(true, false).forEach { explicit ->

                verifyFocusTraversal(type, explicit) { display, focusView, _ ->
                    focusableView().also { view ->
                        every { display.focusTraversalPolicy } returns when (type) {
                            Forward  -> policy(display = display, next = view)
                            Backward -> policy(display = display, previous = view)
                            else     -> {
                                every { focusView.isFocusCycleRoot_ } returns true
                                policy(cycleRoot = focusView, default = view)
                            }
                        }
                    }
                }

                verifyFocusTraversal(type, explicit) { display, focusView, policy ->
                    focusableView().also {
                        every { display.focusTraversalPolicy } returns null

                        when (type) {
                            Forward  -> every { policy.next(display, any()) } returns it
                            Backward -> every { policy.previous(display, any()) } returns it
                            else     -> {
                                every { focusView.isFocusCycleRoot_ } returns true
                                every { policy.default(focusView) } returns it
                            }
                        }
                    }
                }

                verifyFocusTraversal(type, explicit) { _, focusView, _ ->
                    focusableView().also {
                        every { focusView.focusCycleRoot_ } returns focusView
                        every { focusView.focusTraversalPolicy_ } returns when (type) {
                            Forward  -> policy(cycleRoot = focusView, next = it)
                            Backward -> policy(cycleRoot = focusView, previous = it)
                            else     -> {
                                every { focusView.isFocusCycleRoot_ } returns true
                                every { focusView.focusTraversalPolicy_ } returns policy(cycleRoot = focusView, default = it)
                                policy(cycleRoot = focusView, default = it)
                            }
                        }
                    }
                }
            }
        }
    }

    @Test fun `handles focus upward`() {
        listOf(true, false).forEach { explicit ->
            verifyFocusTraversal(Upward, explicit) { _, focusView, policy ->
                focusableView().also {
                    every { focusView.focusCycleRoot_ } returns it
                    every { focusView.focusTraversalPolicy_ } returns policy
                }
            }
        }
    }

    private fun policy(display: Display, next: View? = null, previous: View? = null, default: View? = null) = mockk<FocusTraversalPolicy>().apply {
        every { next    (display, any()) } returns next
        every { default (display       ) } returns default
        every { previous(display, any()) } returns previous
    }

    private fun policy(cycleRoot: View, next: View? = null, previous: View? = null, default: View? = null) = mockk<FocusTraversalPolicy>().apply {
        every { next    (cycleRoot, any()) } returns next
        every { default (cycleRoot       ) } returns default
        every { previous(cycleRoot, any()) } returns previous
    }

    private fun verifyFocusMoves(to: View? = focusableView(), setup: (View) -> () -> Unit) {
        val view    = focusableView()
        val display = createDisplayWithSingleView(to)

        FocusManagerImpl(display, mockk(), focusabilityChecker).apply {
            val listener = mockk<(FocusManager, View?, View?) -> Unit>()

            focusChanged += listener

            val propertyChange = setup(view)

            requestFocus(view) // give focus already

            propertyChange()

            verify(exactly = 1) {
                view.focusLost_(to)
                listener(this@apply, view, to)
            }

            expect(to) { focusOwner }
        }
    }

    private fun focusableView() = mockk<View>().apply {
        every { parent                } returns null
        every { enabled               } returns true
        every { visible               } returns true
        every { focusable             } returns true
        every { focusCycleRoot_       } returns null
        every { shouldYieldFocus()    } returns true
        every { focusTraversalPolicy_ } returns null
    }

    private fun createDisplayWithSingleView(next: View? = null, default: View? = null) = mockk<Display>().apply {
        every { focusTraversalPolicy } returns mockk<FocusTraversalPolicy>().apply {
            every { next   (any<Display>(), any()) } returns next
            every { default(any<Display>()       ) } returns default
        }
    }

    private fun createFocusablePermutations() = listOf(
            Focusability(focusable = false, enabled = false, visible = false) to false,
            Focusability(focusable = false, enabled = false, visible = true ) to false,
            Focusability(focusable = false, enabled = true,  visible = false) to false,
            Focusability(focusable = false, enabled = true,  visible = true ) to false,
            Focusability(focusable = true,  enabled = false, visible = false) to false,
            Focusability(focusable = true,  enabled = false, visible = true ) to false,
            Focusability(focusable = true,  enabled = true,  visible = false) to false,
            Focusability(focusable = true,  enabled = true,  visible = true ) to true
    ).map { (row, expected) ->
        mockk<View>().apply {
            every { enabled               } returns row.enabled
            every { visible               } returns row.visible
            every { focusable             } returns row.focusable
            every { focusCycleRoot_       } returns null
            every { shouldYieldFocus()    } returns true
            every { focusTraversalPolicy_ } returns null
        } to expected
    }

    private fun view(isFocusCycleRoot: Boolean = false, shouldYieldFocus: Boolean = true) = mockk<View>().apply {
        every { parent                } returns null
        every { focusCycleRoot_       } returns null
        every { shouldYieldFocus()    } returns shouldYieldFocus
        every { isFocusCycleRoot_     } returns isFocusCycleRoot
        every { focusTraversalPolicy_ } returns null
    }

    private fun verifyFocusTraversal(type: TraversalType, explicit: Boolean = true, config: (Display, View, FocusTraversalPolicy) -> View?) {
        val focusedView         = view()
        val display             = mockk<Display>()
        val defaultPolicy       = mockk<FocusTraversalPolicy>()
        val focusabilityChecker = mockk<FocusabilityChecker>().apply {
            every { this@apply(any()) } returns true
        }

        val newFocusOwner = config(display, focusedView, defaultPolicy)
        val manager       = FocusManagerImpl(display, defaultPolicy, focusabilityChecker)

        manager.requestFocus(focusedView)

        when (type) {
            Forward  -> if (explicit) manager.moveFocusForward (from = focusedView) else manager.moveFocusForward ()
            Backward -> if (explicit) manager.moveFocusBackward(from = focusedView) else manager.moveFocusBackward()
            Upward   -> if (explicit) manager.moveFocusUpward  (from = focusedView) else manager.moveFocusUpward  ()
            Downward -> if (explicit) manager.moveFocusDownward(from = focusedView) else manager.moveFocusDownward()
        }

        verify(exactly = 1) { focusedView.focusLost_(newFocusOwner) }

        newFocusOwner?.let { verify(exactly = 1) { it.focusGained_(focusedView) } }
    }

    private companion object {
        private class NullableValueGenerator(voidInstance: Any): JvmAnyValueGenerator(voidInstance) {
            override fun anyValue(cls: KClass<*>, isNullable: Boolean, orInstantiateVia: () -> Any?): Any? {
                if (isNullable) return null

                return super.anyValue(cls, isNullable, orInstantiateVia)
            }
        }

        @JvmStatic @BeforeAll fun setupGateway() {
            JvmMockKGateway.anyValueGeneratorFactory = { voidInstance ->
                NullableValueGenerator(voidInstance)
            }
        }
    }
}