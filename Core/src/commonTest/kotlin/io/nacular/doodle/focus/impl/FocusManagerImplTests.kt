package io.nacular.doodle.focus.impl

import io.nacular.doodle.JsName
import io.nacular.doodle.core.View
import io.nacular.doodle.focus.FocusManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

            expect(true) { focusOwner == view }
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

            expect(true) { focusOwner == view }
        }
    }

//    @Test @JsName("focusClearedWhenOwnerNoLongerFocusable")
//    fun `focus cleared when owner no longer focusable`() {
//        val view = createFocusableView()
//
//        FocusManagerImpl().apply {
//            val listener = mockk<(FocusManager, View?, View?) -> Unit>()
//
//            focusChanged += listener
//
//            val enabledChanged      = slot<PropertyObserver<View, Boolean>>()
//            val visibilityChanged   = slot<PropertyObserver<View, Boolean>>()
//            val focusabilityChanged = slot<PropertyObserver<View, Boolean>>()
//
//            every { view.enabledChanged      += capture(enabledChanged     ) } just Runs
//            every { view.visibilityChanged   += capture(visibilityChanged  ) } just Runs
//            every { view.focusabilityChanged += capture(focusabilityChanged) } just Runs
//
//            requestFocus(view) // give focus already
//
//            enabledChanged.captured(view, true, false)
//
//            verify(exactly = 1) {
//                view.focusLost(null)
//                listener(this@apply, view, null)
//            }
//
//            expect(null) { focusOwner }
//        }
//    }


    private fun createFocusableView() = mockk<View>().apply {
        every { parent                } returns null
        every { enabled               } returns true
        every { visible               } returns true
        every { focusable             } returns true
        every { focusCycleRoot_       } returns null
        every { shouldYieldFocus()    } returns true
        every { focusTraversalPolicy_ } returns null
    }

    private fun createFocusablePermutations() = listOf(
            Focusability(false, false, false) to false,
            Focusability(false, false, true ) to false,
            Focusability(false, true,  false) to false,
            Focusability(false, true,  true ) to false,
            Focusability(true,  false, false) to false,
            Focusability(true,  false, true ) to false,
            Focusability(true,  true,  false) to false,
            Focusability(true,  true,  true ) to true
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