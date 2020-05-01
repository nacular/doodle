package com.nectar.doodle.themes.material

import com.nectar.doodle.animation.Animation
import com.nectar.doodle.animation.Animator
import com.nectar.doodle.animation.Animator.Listener
import com.nectar.doodle.animation.fixedTimeLinear
import com.nectar.doodle.animation.speedUpSlowDown
import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.buttons.ButtonModel
import com.nectar.doodle.controls.theme.AbstractTextButtonBehavior
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.black
import com.nectar.doodle.drawing.Color.Companion.white
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Font.Companion.Thick
import com.nectar.doodle.drawing.FontDetector
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.layout.Insets
import com.nectar.measured.units.Time.Companion.milliseconds
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
fun drawRipple(on: Canvas, at: Point, opacity: Float, progress: Float) {
    val x = max(at.x, on.size.width  - at.x)
    val y = max(at.y, on.size.height - at.y)

    val maxRadius = sqrt(x.pow(2) + y.pow(2))

    on.circle(Circle(radius = maxRadius * progress, center = at), ColorBrush(white opacity opacity))
}

class MaterialButtonBehavior(
                    textMetrics    : TextMetrics,
        private val animate        : Animator,
        private val fontDetector   : FontDetector,
        private val textColor      : Color,
        private val backgroundColor: Color,
        private val cornerRadius   : Double = 0.0): AbstractTextButtonBehavior<Button>(textMetrics), MouseListener {

    private var fontLoadJob        = null as Job?; set(new) { field?.cancel(); field = new }
    private var shadow1Blur        = 1.0
    private var rippleOpacity      = 0.24f
    private var rippleProgress     = 0f
    private var overlayOpacity     = 0f
    private var animationListener  = null as Listener?
    private var mousePressed       = false
    private var mousePressLocation = null as Point?

    private var shadowAnimation  = null as Animation?;   set(new) { field?.cancel(); field = new }
    private var rippleAnimation  = null as Animation?;   set(new) { field?.cancel(); field = new }
    private var overlayAnimation = null as Animation?;   set(new) { field?.cancel(); field = new }

    private val hoverAnimationTime = 180 * milliseconds
    private val pressAnimationTime = hoverAnimationTime / 2

    private val styleChanged: (View) -> Unit =  { it.rerender() }

    private val mouseOverChanged: (ButtonModel, Boolean, Boolean) -> Unit = { _,_,new ->
        val overlayEnd     = if (new) 0.2f else 0f
        val shadow1BlurEnd = if (new) 4.0  else 1.0

        shadowAnimation  = (animate (shadow1Blur    to shadow1BlurEnd) using speedUpSlowDown(hoverAnimationTime)) { shadow1Blur    = it }
        overlayAnimation = (animate (overlayOpacity to overlayEnd    ) using fixedTimeLinear(hoverAnimationTime)) { overlayOpacity = it }
    }

    override val clipCanvasToBounds = false

    override fun install(view: Button) {
        super.install(view)

        // FIXME: Centralize
        fontLoadJob = GlobalScope.launch {
            view.font = fontDetector {
                family = "Roboto"
                size   = 14
                weight = Thick
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
                    view.rerenderNow()
                }
            }
        }.also { animationListener = it }
    }

    override fun uninstall(view: Button) {
        super.uninstall(view)

        fontLoadJob?.cancel()

        view.mouseChanged           -= this
        view.styleChanged           -= styleChanged
        view.model.mouseOverChanged -= mouseOverChanged

        animationListener?.let { animate.listeners -= it }
    }

    override fun mousePressed(event: MouseEvent) {
        super<AbstractTextButtonBehavior>.mousePressed(event)

        mousePressed       = true
        mousePressLocation = event.location

        rippleAnimation = (animate (0.4f to 1f) using fixedTimeLinear(pressAnimationTime)) { rippleProgress = it }.apply {
            completed += {
                if (!mousePressed) {
                    fadeRipple()
                } else {
                    rippleAnimation = null
                }
            }
        }
        shadowAnimation = (animate (shadow1Blur to 10.0) using speedUpSlowDown(pressAnimationTime)) { shadow1Blur = it }
    }

    override fun mouseReleased(event: MouseEvent) {
        super<AbstractTextButtonBehavior>.mouseReleased(event)

        mousePressed = false

        if (rippleAnimation == null) {
            fadeRipple()
        }

        shadowAnimation = (animate (shadow1Blur to 4.0) using speedUpSlowDown(pressAnimationTime)) { shadow1Blur = it }
    }

    override fun render(view: Button, canvas: Canvas) {
        val bounds = view.bounds.atOrigin

        canvas.outerShadow(color = black opacity 0.2f, horizontal = 0.0, vertical = shadow1Blur, blurRadius = shadow1Blur) {
            canvas.rect(bounds.inset(Insets(left = 2.0, right = 3.0)), radius = cornerRadius, brush = ColorBrush(backgroundColor))
        }

        canvas.outerShadow(color = black opacity 0.22f, horizontal = 0.0, vertical = 2.0, blurRadius = 2.0) {
            canvas.outerShadow(color = black opacity 0.2f, vertical = 1.0, blurRadius = 5.0) {
                canvas.rect(bounds, cornerRadius, ColorBrush(backgroundColor))
            }
        }

        val text = view.text.toUpperCase()

        canvas.clip(bounds, cornerRadius) {
            canvas.text(text, view.font, textPosition(view, text, bounds = bounds), ColorBrush(textColor))

            canvas.rect(bounds, cornerRadius, ColorBrush(white opacity overlayOpacity))

            mousePressLocation?.let { drawRipple(canvas, it, rippleOpacity, rippleProgress) }
        }
    }

    private fun fadeRipple() {
        rippleAnimation = (animate(0.24f to 0f) using fixedTimeLinear(pressAnimationTime * 2)) { rippleOpacity = it }.apply {
            completed += {
                if (!mousePressed) {
                    mousePressLocation = null
                }

                rippleOpacity   = 0.24f
                rippleAnimation = null
            }
        }
    }
}