package io.nacular.doodle.controls.modal

import io.nacular.doodle.controls.PopupManager
import io.nacular.doodle.controls.modal.ModalManager.Modal
import io.nacular.doodle.controls.modal.ModalManager.ModalContext
import io.nacular.doodle.controls.modal.ModalManager.ModalContext.BackgroundMode
import io.nacular.doodle.controls.modal.ModalManager.ModalContext.BackgroundMode.Overlay
import io.nacular.doodle.controls.modal.ModalManager.ModalContext.BackgroundMode.Replace
import io.nacular.doodle.controls.modal.ModalManager.ModalType
import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.observable
import kotlinx.coroutines.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Simple factory to create modals.
 *
 * @suppress
 */
@Internal
@Suppress("PrivatePropertyName")
public class ModalManagerImpl(private val popupManager: PopupManager, private val focusManager: FocusManager?): ModalManager {
    private val overlayConstraints: ConstraintDslContext.(Bounds) -> Unit = { it.edges eq parent.edges }

    private val modalStack = mutableListOf<ModalContextImpl<*>>()

    @Suppress("PropertyName")
    private inner class ModalContextImpl<T>(background: Paint = Transparent.paint, private val completed_: (T) -> Unit): ModalContext<T> {
        lateinit var modalType: ModalType

        val focusOwner = focusManager?.focusOwner

        val pointerChanged_       by lazy { SetPool<PointerListener>      () }
        val pointerMotionChanged_ by lazy { SetPool<PointerMotionListener>() }

        var overlay = object: View() {
            override fun shouldHandlePointerEvent      (event: PointerEvent) = !allowPointerThrough
            override fun shouldHandlePointerMotionEvent(event: PointerEvent) = !allowPointerThrough

            override fun render(canvas: Canvas) {
                canvas.rect(bounds.atOrigin, fill = this@ModalContextImpl.background)
            }
        }

        override val pointerOutsideModalChanged      : Pool<PointerListener>       get() = pointerChanged_
        override val pointerMotionOutsideModalChanged: Pool<PointerMotionListener> get() = pointerMotionChanged_

        override var allowPointerThrough = false

        override var background: Paint by observable(background) { _,_ ->
            overlay.rerenderNow()
        }

        override var backgroundMode: BackgroundMode by observable(Replace) { _,_ -> updateBackground() }

        override fun reLayout() {
            if (::modalType.isInitialized) {
                popupManager.relayout(modalType.view)
            }
        }

        override fun completed(result: T) {
            completed_(result)
        }

        init {
            updateBackground()
        }

        fun updateBackground() {
            modalStack.takeWhile { it != this }.forEach {
                it.overlay.visible = backgroundMode == Overlay
            }
        }

        fun registerListeners() {
            pointerChanged_.forEach {
                overlay.pointerChanged       += it
                overlay.pointerPassedThrough += it
            }
            pointerMotionChanged_.forEach {
                overlay.pointerMotionChanged       += it
                overlay.pointerMotionPassedThrough += it
            }
        }

        fun unregisterListeners() {
            pointerChanged_.forEach {
                overlay.pointerChanged       -= it
                overlay.pointerPassedThrough -= it
            }
            pointerMotionChanged_.forEach {
                overlay.pointerMotionChanged       -= it
                overlay.pointerMotionPassedThrough -= it
            }
        }
    }

    override suspend fun <T> invoke(contents: ModalContext<T>.() -> ModalType): T = suspendCoroutine { coroutine ->
        try {
            var modalClosed = false

            val modal = ModalContextImpl<T> { result ->
                if (!modalClosed) {
                    modalClosed = true

                    coroutine.resume(result)

                    var previousFocusOwner: View?

                    modalStack.removeLast().let {
                        popupManager.hide(it.modalType.view)
                        popupManager.hide(it.overlay)
                        it.unregisterListeners()
                        previousFocusOwner = it.focusOwner
                    }

                    modalStack.forEach {
                        it.overlay.visible = true
                        it.updateBackground()
                    }

                    modalStack.lastOrNull()?.let { modal ->
                        modal.registerListeners()
                        previousFocusOwner?.let { focusManager?.requestFocus(it) }
                    }
                }
            }

            with(contents(modal)) {
                modal.modalType = this
            }

            modalStack.lastOrNull()?.unregisterListeners()

            modalStack += modal

            modal.registerListeners()

            focusManager?.clearFocus()

            popupManager.show(modal.overlay, overlayConstraints)

            when (val type = modal.modalType) {
                is Modal                      -> popupManager.show(type.view, type.layout)
                is ModalManager.RelativeModal -> popupManager.show(type.view, type.relativeTo, type.layout)
            }
        } catch (e: CancellationException) {
            coroutine.resumeWithException(e)
        }
    }
}