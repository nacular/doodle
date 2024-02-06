package io.nacular.doodle.controls.modal

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.nacular.doodle.controls.PopupManager
import io.nacular.doodle.controls.modal.ModalManager.Modal
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.focus.FocusManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 2/2/24.
 */
@OptIn(DelicateCoroutinesApi::class)
class ModalManagerImplTests {
    @Test fun `shows modal`() = runBlocking {
        val popups       = mutableListOf<View>()
        val popupManager = popupManager(popups)
        val focusManager = focusManager()

        val modal = ModalManagerImpl(popupManager, focusManager)
        val view  = mockk<View>(relaxed = true)
        var close = {}

        showModal(modal, view) { close = it }
        delayUntil { view in popups }

        expect(2   ) { popups.size   }
        expect(view) { popups.last() }

        close()
    }

    @Test fun `shows background`() = runBlocking {
        val popups       = mutableListOf<View>()
        val popupManager = popupManager(popups)
        val focusManager = focusManager()

        val modal = ModalManagerImpl(popupManager, focusManager)
        val view  = mockk<View>(relaxed = true)
        var close = {}

        showModal(modal, view) {
            background = Red.paint
            close = it
        }

        delayUntil { view in popups }

        expect(2   ) { popups.size   }
        expect(view) { popups.last() }

        close()
    }

    // TODO: Implement
    @Ignore
    @Test fun `fires clicked outside`() = runBlocking {
    }

    @Test fun `closes modal`() = runBlocking {
        val popups       = mutableListOf<View>()
        val popupManager = popupManager(popups)
        val focusManager = focusManager()

        val modal = ModalManagerImpl(popupManager, focusManager)
        val view  = mockk<View>(relaxed = true)
        var close = {}

        showModal(modal, view) { close = it }

        delayUntil { view in popups }

        close()

        expect(emptyList()) { popups }
    }

    @Test fun `closes modal with background`() = runBlocking {
        val popups       = mutableListOf<View>()
        val popupManager = popupManager(popups)
        val focusManager = focusManager()

        val modal = ModalManagerImpl(popupManager, focusManager)
        val view  = mockk<View>(relaxed = true)
        var close = {}

        showModal(modal, view) {
            background = Red.paint
            close = it
        }

        delayUntil { view in popups }

        close()

        expect(emptyList()) { popups }
    }

    @Test fun `shows modal atop existing`() = runBlocking {
        val popups       = mutableListOf<View>()
        val popupManager = popupManager(popups)
        val focusManager = focusManager()

        val modal  = ModalManagerImpl(popupManager, focusManager)
        val view1  = mockk<View>(relaxed = true)
        val view2  = mockk<View>(relaxed = true)
        var close1 = {}
        var close2 = {}

        showModal(modal, view1) { close1 = it }

        delayUntil { view1 in popups }

        showModal(modal, view2) { close2 = it }

        delayUntil { view2 in popups }

        assertContentEquals(listOf(anyView, view1, anyView, view2), popups)

        close2()
        close1()
    }

    @Test fun `shows background atop existing`() = runBlocking {
        val popups       = mutableListOf<View>()
        val popupManager = popupManager(popups)
        val focusManager = focusManager()

        val modal  = ModalManagerImpl(popupManager, focusManager)
        val view1  = mockk<View>(relaxed = true)
        val view2  = mockk<View>(relaxed = true)
        var close1 = {}
        var close2 = {}

        showModal(modal, view1) { close1 = it }

        delayUntil { view1 in popups }

        showModal(modal, view2) {
            background = Red.paint
            close2 = it
        }

        delayUntil { view2 in popups }

        assertContentEquals(listOf(anyView, view1, anyView, view2), popups)

        close2()
        close1()
    }

    @Test fun `only shows top background`() = runBlocking {
        val popups       = mutableListOf<View>()
        val popupManager = popupManager(popups)
        val focusManager = focusManager()

        val modal  = ModalManagerImpl(popupManager, focusManager)
        val view1  = mockk<View>(relaxed = true)
        val view2  = mockk<View>(relaxed = true)
        var close1 = {}
        var close2 = {}

        showModal(modal, view1) {
            background = Red.paint
            close1 = it
        }

        delayUntil { view1 in popups }

        expect(2) { popups.size }
        expect(popups.last ()) { view1 }

        showModal(modal, view2) {
            background = Red.paint
            close2 = it
        }

        delayUntil { view2 in popups }

        assertContentEquals(listOf(matches { !it.visible }, view1, matches { it.visible }, view2), popups)

        close2()
        close1()
    }

    @Test fun `returns previous background on close`() = runBlocking {
        val popups       = mutableListOf<View>()
        val popupManager = popupManager(popups)
        val focusManager = focusManager()

        val modal  = ModalManagerImpl(popupManager, focusManager)
        val view1  = mockk<View>(relaxed = true)
        val view2  = mockk<View>(relaxed = true)
        var close1 = {}
        var close2 = {}

        showModal(modal, view1) {
            background = Blue.paint
            close1 = it
        }

        delayUntil { view1 in popups }

        expect(2) { popups.size }
        expect(popups.last ()) { view1 }

        showModal(modal, view2) {
            background = Red.paint
            close2 = it
        }

        delayUntil { view2 in popups }

        assertContentEquals(listOf(matches { !it.visible }, view1, matches { it.visible }, view2), popups)

        close2()

        assertContentEquals(listOf(matches { it.visible }, view1), popups)

        close1()
    }

    private val anyView = matches { true }

    private fun matches(block: (View) -> Boolean) = object: View() {
        override fun equals(other: Any?) = other is View && block(other)
    }

    private fun showModal(modal: ModalManager, view: View, toClose: ModalManager.ModalContext<Unit>.(() -> Unit) -> Unit) = GlobalScope.launch {
        modal {
            toClose(this) { completed(Unit) }

            Modal(view)
        }
    }

    private suspend fun delayUntil(predicate: () -> Boolean) = suspendCoroutine { coroutine ->
        try {
            while (true) {
                if (predicate()) {
                    coroutine.resume(Unit)
                    break
                }
            }
        } catch (e: CancellationException) {
            coroutine.resumeWithException(e)
        }
    }

    private fun popupManager(popups: MutableList<View> = mutableListOf()) = mockk<PopupManager>().apply {
        val view = slot<View>()

        every { show(capture(view), any()) } answers {
            view.captured.also { popups += it }
        }

        every { hide(capture(view)) } answers {
            popups -= view.captured
        }
    }

    private fun focusManager() = mockk<FocusManager>(relaxed = true)
}