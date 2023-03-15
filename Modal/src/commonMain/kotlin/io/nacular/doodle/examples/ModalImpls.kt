package io.nacular.doodle.examples

import io.nacular.doodle.animation.Animation
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.invoke
import io.nacular.doodle.animation.transition.easeInOutCubic
import io.nacular.doodle.animation.tweenFloat
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.height
import io.nacular.doodle.core.width
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.toPath
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.autoCanceling
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.times
import kotlinx.coroutines.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Simple factory to create modals.
 */
class ModalFactoryImpl(private val display: Display, private val animate: Animator): ModalFactory {
    override fun     invoke(insets: Insets, contents: (Modal      ) -> View) = ModalImpl          (display, animate, insets, contents)
    override fun     invoke(insets: Insets, contents: (Modeless   ) -> View) = ModelessImpl       (display, animate, insets, contents)
    override fun <T> invoke(insets: Insets, contents: ((T) -> Unit) -> View) = SuspendingModalImpl(display, animate, insets, contents)
}

/**
 * Helper class to render the window within the modal. These modals will render full screen
 * to obscure the content behind them. This dialog will be a child of the modal that contains
 * the contents provided to the [ModalFactory] when the modal was created.
 *
 * @param contents to display in the dialog
 * @param insets applied to contents
 */
private class Dialog(contents: View, insets: Insets): View() {
    private val clipPath get() = object: ClipPath(bounds.atOrigin.toPath(10.0)) {
        override fun contains(point: Point) = point in bounds.atOrigin
    }

    init {
        clipCanvasToBounds = false

        children += contents

        childrenClipPath = clipPath

        boundsChanged += { _,_,_ ->
            childrenClipPath = clipPath
        }

        layout = object: Layout {
            override fun requiresLayout(child: Positionable, of: PositionableContainer, old: SizePreferences, new: SizePreferences): Boolean {
                return old.idealSize != new.idealSize
            }

            override fun layout(container: PositionableContainer) {
                container.children.forEach {
                    val size = it.idealSize ?: it.size

                    it.bounds = Rectangle(insets.left, insets.top, size.width, size.height)
                    idealSize = Size(size.width + insets.run { left + right }, size.height + insets.run { top + bottom })
                }
            }
        }
    }

    override fun render(canvas: Canvas) {
        canvas.outerShadow(vertical = 4.0, blurRadius = 12.0, color = Black.opacity(0.15f)) {
            canvas.rect(bounds.atOrigin, radius = 10.0, color = White)
        }
    }
}

/**
 * Base class for our modals that enables logic reuse.
 *
 * @param display where the modal will be shown
 * @param animate used to animate the modal
 */
abstract class AbstractModal(display: Display, private val animate: Animator): View() {
    private var showing      = false
    private var showProgress = 0f
    private var animation: Animation<Float>? by autoCanceling()

    private val display_ = display

    private val sizeChanged: (Display, Size, Size) -> Unit = { _,_,_ ->
        size = display.size
    }

    fun show(contents: View, insets: Insets) {
        if (children.isEmpty()) {
            children += Dialog(contents, insets).apply { opacity = 0f }
            layout = object: Layout {
                override fun requiresLayout(child: Positionable, of: PositionableContainer, old: SizePreferences, new: SizePreferences): Boolean {
                    return old.idealSize != new.idealSize
                }

                override fun layout(container: PositionableContainer) {
                    container.children.forEach {
                        val size = it.idealSize ?: it.size

                        it.bounds = Rectangle((width - size.width) / 2, (height - size.height) / 2, size.width, size.height)
                    }
                }
            }
        }

        if (!showing) {
            showing = true
            size    = display_.size

            display_ += this
            display_.sizeChanged += sizeChanged

            animation = animate(0f to 1f, tweenFloat(easeInOutCubic, 250 * milliseconds)) {
                showProgress = it
                children[0].opacity = it
                rerenderNow()
            }
        }
    }

    fun hide() {
        animation?.cancel()

        children.clear()
        display_ -= this
        display_.sizeChanged -= sizeChanged

        showing  = false
    }

    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, color = Black.opacity(0.25f * showProgress))
    }
}

/**
 * Simple modal that displays some content.
 *
 * @param display where the modal will be shown.
 * @param contents to show within the modal.
 */
class ModalImpl(display: Display, animate: Animator, private val modalInsets: Insets, private val contents: (Modal) -> View): AbstractModal(display, animate), Modal {
    override fun show() {
        super.show(contents(this), modalInsets)
    }
}

/**
 * Modal that suspends when shown, until it has a result.
 *
 * @param display where the modal will be shown.
 * @param config defining what the modal shows and when it is complete.
 */
class SuspendingModalImpl<T>(display: Display, animate: Animator, private val modalInsets: Insets, private val config: (completed: (T) -> Unit) -> View): AbstractModal(display, animate), SuspendingModal<T> {
    override suspend fun show(): T = suspendCoroutine { coroutine ->
        try {
            super.show(config {
                super.hide()
                coroutine.resume(it)
            }, modalInsets)
        } catch (e: CancellationException) {
            coroutine.resumeWithException(e)
        }
    }
}

class ModelessImpl(display: Display, private val animate: Animator, modalInsets: Insets, contents: (Modeless) -> View): Modeless {
    private var showing      = false
    private var showProgress = 0f
    private var animation: Animation<Float>? by autoCanceling()

    private val dialog = Dialog(contents(this), modalInsets).apply {
        boundsChanged += { _,_,_ ->
            position = Point((display.width - width) / 2, (display.height - height) / 2)
        }

        sizePreferencesChanged += { _,_,_ ->
            size = idealSize ?: size
        }
    }
    private val display_ = display

    private val sizeChanged: (Display, Size, Size) -> Unit = { _,_,_ ->
        dialog.position = Point((display.width - dialog.width) / 2, (display.height - dialog.height) / 2)
    }

    override fun show() {
        if (!showing) {
            showing = true
            display_ += dialog

            display_.sizeChanged += sizeChanged

            animation = animate(0f to 1f, tweenFloat(easeInOutCubic, 250 * milliseconds)) {
                showProgress   = it
                dialog.opacity = it
            }
        }
    }

    override fun hide() {
        animation?.cancel()

        display_ -= dialog
        display_.sizeChanged -= sizeChanged

        showing  = false
    }
}
