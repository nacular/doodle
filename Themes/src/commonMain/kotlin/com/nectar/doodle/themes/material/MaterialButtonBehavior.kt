package com.nectar.doodle.themes.material

import com.nectar.doodle.animation.Animation
import com.nectar.doodle.animation.Animator
import com.nectar.doodle.animation.Animator.Listener
import com.nectar.doodle.animation.AnimatorFactory
import com.nectar.doodle.animation.fixedTimeLinear
import com.nectar.doodle.animation.speedUpSlowDown
import com.nectar.doodle.animation.transition.NoChange
import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.buttons.ButtonModel
import com.nectar.doodle.controls.theme.AbstractTextButtonBehavior
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.white
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.FontDetector
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.utils.Completable
import com.nectar.measured.units.milliseconds
import com.nectar.measured.units.times
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by Nicholas Eddy on 12/10/19.
 */
fun drawRipple(on: Canvas, at: Point, progress: Float) {
    val x = max(at.x, on.size.width  - at.x)
    val y = max(at.y, on.size.height - at.y)

    val maxRadius = sqrt(x.pow(2) + y.pow(2))

    on.circle(Circle(radius = maxRadius * progress, center = at), ColorBrush(white opacity 0.24f))
}

class MaterialButtonBehavior(
                    textMetrics    : TextMetrics,
                    animatorFactory: AnimatorFactory,
        private val fontDetector   : FontDetector,
        private val textColor      : Color,
        private val backgroundColor: Color,
        private val cornerRadius   : Double = 0.0): AbstractTextButtonBehavior<Button>(textMetrics), MouseListener {

    private val animate            = animatorFactory()
    private var fontLoadJob        = null as Job?
    private var rippleProgress     = 0f
    private var shadowProgress     = 0f
    private var overlayProgress    = 0f
    private var animationListener  = null as Listener?
    private var mousePressLocation = null as Point?

    private var rippleAnimation    = null as Animation?
    private var mouseOverAnimation = null as Completable?

    private val styleChanged: (View) -> Unit =  { it.rerender() }

    private val mouseOverChanged: (ButtonModel, Boolean, Boolean) -> Unit = { _,_,new ->
        val time = 180 * milliseconds
        val end  = if (new) 1f else 0f

        mouseOverAnimation?.cancel()

        mouseOverAnimation = animate {
            (0f to end using fixedTimeLinear(time)) { overlayProgress = it }
            (0f to end using speedUpSlowDown(time)) { shadowProgress  = it }
        }
    }

    override fun install(view: Button) {
        super.install(view)

        fontLoadJob?.cancel()

        fontLoadJob = GlobalScope.launch {
            view.font = fontDetector {
                family = "Roboto"
                size   = 14
                weight = Font.Weight.Thick
            }
        }

        view.mouseChanged           += this
        view.styleChanged           += styleChanged
        view.model.mouseOverChanged += mouseOverChanged

        animationListener?.let { animate.listeners -= it }

        animate.listeners += object: Listener {
            override fun changed(animator: Animator, animations: Set<Animation>) {
                view.rerenderNow()
            }

            override fun completed(animator: Animator, animations: Set<Animation>) {
                if (rippleAnimation in animations) {
                    rippleProgress = 0f

                    view.rerenderNow()
                }
            }
        }.also { animationListener = it }
    }

    override fun mousePressed(event: MouseEvent) {
        super<AbstractTextButtonBehavior>.mousePressed(event)

        val time = 180 * milliseconds

        mousePressLocation = event.location

        rippleAnimation?.cancel()

        animate { rippleAnimation = ((0f to 1f using fixedTimeLinear(time)).then(NoChange(time))) { rippleProgress = it } }
    }

    override fun uninstall(view: Button) {
        super.uninstall(view)

        fontLoadJob?.cancel()

        view.mouseChanged           -= this
        view.styleChanged           -= styleChanged
        view.model.mouseOverChanged -= mouseOverChanged

        animationListener?.let { animate.listeners -= it }
    }

    override fun render(view: Button, canvas: Canvas) {
        val bounds = view.bounds.atOrigin

        canvas.outerShadow(color = Color.black opacity 0.2f, horizontal = 0.0, vertical = 1.0 + 2 * shadowProgress, blurRadius = 1.0 + 3 * shadowProgress) {
            canvas.rect(bounds.inset(Insets(left = 2.0, right = 3.0)), radius = cornerRadius, brush = ColorBrush(backgroundColor))
        }

        canvas.outerShadow(color = Color.black opacity 0.22f, horizontal = 0.0, vertical = 2.0, blurRadius = 2.0) {
            canvas.outerShadow(color = Color.black opacity 0.2f, vertical = 1.0, blurRadius = 5.0) {
                canvas.rect(bounds, cornerRadius, ColorBrush(backgroundColor))
            }
        }

        val text = view.text.toUpperCase()

        canvas.text(text, view.font, textPosition(view, text, bounds = bounds), ColorBrush(textColor))

        canvas.rect(bounds, cornerRadius, ColorBrush(white opacity 0.075f * overlayProgress))

        mousePressLocation?.let { canvas.clip(bounds) { drawRipple(canvas, it, rippleProgress) } }
    }
}