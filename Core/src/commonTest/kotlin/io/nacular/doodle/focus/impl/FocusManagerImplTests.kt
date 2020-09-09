package io.nacular.doodle.focus.impl

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.focus.FocusTraversalPolicy
import io.nacular.doodle.utils.PropertyObserver
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 1/27/19.
 */
class FocusManagerImplTests {
    data class Focusability(val focusable: Boolean, val enabled: Boolean, val visible: Boolean)

    @Test @JsName("noDefaultFocusOwner")
    fun `no default focus owner`() {
        expect(null) { FocusManagerImpl(mockk()).focusOwner }
    }

    @Test @JsName("noDefaultFocusCycleRoot")
    fun `no default focus-cycle-root`() {
        expect(null) { FocusManagerImpl(mockk()).focusCycleRoot }
    }

    @Test @JsName("validateFocusability")
    fun `validate focusability`() {
        createFocusablePermutations().forEach { (view, expected) ->
            expect(expected) { FocusManagerImpl(mockk()).focusable(view) }
        }
    }

    @Test @JsName("requestFocusNoOpsIfNotFocusable")
    fun `request focus no-ops if not focusable`() {
        createFocusablePermutations().forEach { (view, expected) ->
            FocusManagerImpl(mockk()).apply {
                val listener = mockk<(FocusManager, View?, View?) -> Unit>()

                focusChanged += listener

                every { view.parent } returns null

                requestFocus(view)

                verify(exactly = if (expected) 1 else 0) {
                    view.focusGained(null)
                    listener(this@apply, null, view)
                }

                expect(expected) { focusOwner == view }
            }
        }
    }

    @Test @JsName("requestFocusWithPrevious")
    fun `request focus with previous`() {
        val previous = createFocusableView()
        val view     = createFocusableView()

        FocusManagerImpl(mockk()).apply {
            val listener = mockk<(FocusManager, View?, View?) -> Unit>()

            focusChanged += listener

            requestFocus(previous) // move focus to previous
            requestFocus(view    )

            verify {
                previous.focusLost(view    )
                view.focusGained  (previous)
                listener(this@apply, previous, view)
            }

            expect(view) { focusOwner }
        }
    }

    @Test @JsName("requestFocusToFocusOwnerNoOps")
    fun `request focus to focus owner no-ops`() {
        val view = createFocusableView()

        FocusManagerImpl(mockk()).apply {
            val listener = mockk<(FocusManager, View?, View?) -> Unit>()

            focusChanged += listener

            requestFocus(view) // give focus already
            requestFocus(view)

            verify(exactly = 1) {
                view.focusGained(null)
                listener(this@apply, null, view)
            }

            expect(view) { focusOwner }
        }
    }

    @Test @JsName("requestFocusWhenDisabledNoOps")
    fun `request focus when disabled no-ops`() {
        val view = createFocusableView()

        FocusManagerImpl(mockk()).apply {
            val listener = mockk<(FocusManager, View?, View?) -> Unit>()

            focusChanged += listener

            enabled = false

            requestFocus(view) // should no-op

            verify(exactly = 0) {
                view.focusGained(any())
                listener(this@apply, null, view)
            }

            expect(null) { focusOwner }
        }
    }

    @Test @JsName("focusClearedWhenOwnerDisabled")
    fun `focus cleared when owner disabled`() {
        verifyFocusLost {
            val propertyChanged = slot<PropertyObserver<View, Boolean>>()

            every { it.enabledChanged += capture(propertyChanged) } just Runs

            { propertyChanged.captured }
        }
    }

    @Test @JsName("focusClearedWhenOwnerNoLongerFocusable")
    fun `focus cleared when owner no longer focusable`() {
        verifyFocusLost {
            val propertyChanged = slot<PropertyObserver<View, Boolean>>()

            every { it.focusabilityChanged += capture(propertyChanged) } just Runs

            { propertyChanged.captured }
        }
    }

    @Test @JsName("focusClearedWhenOwnerNoLongerVisible")
    fun `focus cleared when owner no longer visible`() {
        verifyFocusLost {
            val propertyChanged = slot<PropertyObserver<View, Boolean>>()

            every { it.visibilityChanged += capture(propertyChanged) } just Runs

            { propertyChanged.captured }
        }
    }

    @Test @JsName("focusClearedWhenDisabled")
    fun `focus cleared when disabled`() {
        val view = createFocusableView()

        FocusManagerImpl(createDisplayWithSingleView()).apply {
            val listener = mockk<(FocusManager, View?, View?) -> Unit>()

            focusChanged += listener

            requestFocus(view)

            enabled = false

            verify(exactly = 1) {
                view.focusLost(null)
                listener(this@apply, view, null)
            }

            expect(null) { focusOwner }
        }
    }

    @Test @JsName("focusReturnedToPreviousOnwerWhenEnabled")
    fun `focus returned to previous owner when enabled`() {
        val view = createFocusableView()

        FocusManagerImpl(createDisplayWithSingleView()).apply {
            val listener = mockk<(FocusManager, View?, View?) -> Unit>()

            focusChanged += listener

            requestFocus(view)

            enabled = false
            enabled = true

            verify(exactly = 2) {
                view.focusGained(null)
                listener(this@apply, null, view)
            }

            expect(view) { focusOwner }
        }
    }

    private fun verifyFocusLost(setup: (View) -> () -> PropertyObserver<View, Boolean>) {
        val view = createFocusableView()

        FocusManagerImpl(createDisplayWithSingleView()).apply {
            val listener = mockk<(FocusManager, View?, View?) -> Unit>()

            focusChanged += listener

            val propertyChange = setup(view)

            requestFocus(view) // give focus already

            propertyChange()(view, true, false)

            verify(exactly = 1) {
                view.focusLost(null)
                listener(this@apply, view, null)
            }

            expect(null) { focusOwner }
        }
    }

    private fun createFocusableView() = mockk<View>().apply {
        every { parent                } returns null
        every { enabled               } returns true
        every { visible               } returns true
        every { focusable             } returns true
        every { focusCycleRoot_       } returns null
        every { shouldYieldFocus()    } returns true
        every { focusTraversalPolicy_ } returns null
    }

    private fun createDisplayWithSingleView() = mockk<Display>().apply {
        every { focusTraversalPolicy } returns mockk<FocusTraversalPolicy>().apply {
            every { next(any<Display>(), any()) } returns null
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
}