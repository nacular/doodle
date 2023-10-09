package io.nacular.doodle.controls.modal

import io.nacular.doodle.controls.PopupManager
import io.nacular.doodle.controls.modal.ModalManager.Modal
import io.nacular.doodle.controls.modal.ModalManager.ModalContext
import io.nacular.doodle.controls.modal.ModalManager.ModalType
import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.paint
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
    private var background: Paint? by observable(null) { _,_ ->
        overlay.rerenderNow()
    }

    private val overlay = object: View() {
        override fun render(canvas: Canvas) {
            background?.let { canvas.rect(bounds.atOrigin, fill = it) }
        }
    }

    private val overlayConstraints: ConstraintDslContext.(Bounds) -> Unit = { it.edges eq parent.edges }

    private val modalStack = mutableListOf<ModalContextImpl<*>>()

    @Suppress("PropertyName")
    private inner class ModalContextImpl<T>(background: Paint = Transparent.paint, private val completed_: (T) -> Unit): ModalContext<T> {
        lateinit var modalType: ModalType

        val focusOwner = focusManager?.focusOwner

        val pointerChanged_       by lazy { SetPool<PointerListener>      () }
        val pointerMotionChanged_ by lazy { SetPool<PointerMotionListener>() }

        override val pointerOutsideModalChanged      : Pool<PointerListener>       get() = pointerChanged_
        override val pointerMotionOutsideModalChanged: Pool<PointerMotionListener> get() = pointerMotionChanged_

        override var background: Paint by observable(background) { _,new ->
            this@ModalManagerImpl.background = new
        }

        override fun reLayout() {
            if (::modalType.isInitialized) {
                popupManager.relayout(modalType.view)
            }
        }

        override fun completed(result: T) {
            completed_(result)
        }
    }

    override suspend fun <T> invoke(contents: ModalContext<T>.() -> ModalType): T = suspendCoroutine { coroutine ->
        try {
            val modal = ModalContextImpl<T> { result ->
                coroutine.resume(result)

                var previousFocusOwner: View?

                modalStack.removeLast().let {
                    popupManager.hide(it.modalType.view)
                    it.unregisterListeners()
                    previousFocusOwner = it.focusOwner
                }

                popupManager.hide(overlay)

                modalStack.lastOrNull()?.let { modal ->
                    popupManager.hide(modal.modalType.view)
                    this.background = modal.background

                    modal.registerListeners()

                    popupManager.show(overlay, overlayConstraints)

                    when (val type = modal.modalType) {
                        is Modal                      -> popupManager.show(type.view, type.layout)
                        is ModalManager.RelativeModal -> popupManager.show(type.view, type.relativeTo, type.layout)
                    }

                    previousFocusOwner?.let { focusManager?.requestFocus(it) }
                }
            }

            with(contents(modal)) {
                modal.modalType = this
            }

            popupManager.hide(overlay)

            modalStack.lastOrNull()?.unregisterListeners()

            modalStack += modal

            modal.registerListeners()

            focusManager?.clearFocus()

            background = modal.background
            popupManager.show(overlay, overlayConstraints)
            when (val type = modal.modalType) {
                is Modal                      -> popupManager.show(type.view, type.layout)
                is ModalManager.RelativeModal -> popupManager.show(type.view, type.relativeTo, type.layout)
            }
        } catch (e: CancellationException) {
            coroutine.resumeWithException(e)
        }
    }

    private fun ModalContextImpl<*>.registerListeners() {
        pointerChanged_.forEach {
            overlay.pointerChanged += it
        }
        pointerMotionChanged_.forEach {
            overlay.pointerMotionChanged += it
        }
    }

    private fun ModalContextImpl<*>.unregisterListeners() {
        pointerChanged_.forEach {
            overlay.pointerChanged -= it
        }
        pointerMotionChanged_.forEach {
            overlay.pointerMotionChanged -= it
        }
    }
}