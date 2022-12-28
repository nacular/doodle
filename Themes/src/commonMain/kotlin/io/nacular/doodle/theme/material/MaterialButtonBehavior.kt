package io.nacular.doodle.theme.material

import io.nacular.doodle.animation.Animation
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.Animator.Listener
import io.nacular.doodle.animation.invoke
import io.nacular.doodle.animation.transition.easeInOutCubic
import io.nacular.doodle.animation.transition.linear
import io.nacular.doodle.animation.tweenDouble
import io.nacular.doodle.animation.tweenFloat
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.ButtonModel
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Font.Companion.Thick
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.scheduler.Scheduler
import io.nacular.doodle.theme.material.MaterialTheme.Companion.FontConfig
import io.nacular.doodle.utils.Cancelable
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.times
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by Nicholas Eddy on 12/10/19.
 */
internal fun drawRipple(on: Canvas, at: Point, opacity: Float, progress: Float) {
    val x = max(at.x, on.size.width  - at.x)
    val y = max(at.y, on.size.height - at.y)

    val maxRadius = sqrt(x.pow(2) + y.pow(2))

    on.circle(Circle(radius = maxRadius * progress, center = at), ColorPaint(White opacity opacity))
}

public class MaterialButtonBehavior(
                    textMetrics    : TextMetrics,
        private val animate        : Animator,
        private val scheduler      : Scheduler,
        private val fontConfig     : FontConfig?,
        private val fonts          : FontLoader,
        private val textColor      : Color,
        private val backgroundColor: Color,
        private val cornerRadius   : Double        = 0.0,
                    focusManager   : FocusManager? = null): CommonTextButtonBehavior<Button>(textMetrics, focusManager = focusManager), PointerListener {

    private var fontTimer          = null as Cancelable?; set(new) { field?.cancel(); field = new }
    private var fontLoadJob        = null as Job?; set(new) {
        field?.cancel()
        fontTimer?.cancel()
        field = new?.also { job ->
            fontTimer = scheduler.after(fontConfig!!.timeout) {
                job.cancel()
            }
        }
    }
    private var shadow1Blur          = 1.0
    private var rippleOpacity        = 0.24f
    private var rippleProgress       = 0f
    private var overlayOpacity       = 0f
    private var animationListener    = null as Listener?
    private var pointerPressed       = false
    private var pointerPressLocation = null as Point?

    private var shadowAnimation  = null as Animation<Double>?; set(new) { field?.cancel(); field = new }
    private var rippleAnimation  = null as Animation<Float>?;  set(new) { field?.cancel(); field = new }
    private var overlayAnimation = null as Animation<Float>?;  set(new) { field?.cancel(); field = new }

    private val hoverAnimationTime = 180 * milliseconds
    private val pressAnimationTime = hoverAnimationTime / 2

    private val styleChanged: (View) -> Unit =  { it.rerender() }

    private val pointerOverChanged: (ButtonModel, Boolean, Boolean) -> Unit = { _,_,new ->
        val overlayEnd     = if (new) 0.2f else 0f
        val shadow1BlurEnd = if (new) 4.0  else 1.0

        shadowAnimation  = animate(shadow1Blur    to shadow1BlurEnd, tweenDouble(easeInOutCubic, hoverAnimationTime)) { shadow1Blur    = it }
        overlayAnimation = animate(overlayOpacity to overlayEnd,     tweenFloat (linear,         hoverAnimationTime)) { overlayOpacity = it }
    }

    override fun clipCanvasToBounds(view: Button): Boolean = false

    override fun install(view: Button) {
        super.install(view)

        // FIXME: Centralize, Handle cancellation errors
        fontLoadJob = fontConfig?.source?.let {
            GlobalScope.launch {
                view.font = fonts(it) {
                    family = "Roboto"
                    size   = 14
                    weight = Thick
                }
            }
        }

        view.pointerChanged           += this
        view.styleChanged             += styleChanged
        view.model.pointerOverChanged += pointerOverChanged

        animationListener?.let { animate.listeners -= it }

        animate.listeners += object: Listener {
            override fun changed(animator: Animator, animations: Set<Animation<*>>) {
                view.rerenderNow()
            }

            override fun completed(animator: Animator, animations: Set<Animation<*>>) {
                if (rippleAnimation in animations.filterIsInstance<Animation<Float>>()) {
                    view.rerenderNow()
                }
            }
        }.also { animationListener = it }
    }

    override fun uninstall(view: Button) {
        super.uninstall(view)

        fontLoadJob?.cancel()

        view.pointerChanged           -= this
        view.styleChanged           -= styleChanged
        view.model.pointerOverChanged -= pointerOverChanged

        animationListener?.let { animate.listeners -= it }
    }

    override fun pressed(event: PointerEvent) {
        super<CommonTextButtonBehavior>.pressed(event)

        pointerPressed       = true
        pointerPressLocation = event.location

        rippleAnimation = animate(0.4f to 1f, tweenFloat(linear, pressAnimationTime)) { rippleProgress = it }.apply {
            completed += {
                if (!pointerPressed) {
                    fadeRipple()
                } else {
                    rippleAnimation = null
                }
            }
        }
        shadowAnimation = animate(shadow1Blur to 10.0, tweenDouble(easeInOutCubic, pressAnimationTime)) { shadow1Blur = it }
    }

    override fun released(event: PointerEvent) {
        super<CommonTextButtonBehavior>.released(event)

        pointerPressed = false

        if (rippleAnimation == null) {
            fadeRipple()
        }

        shadowAnimation = animate(shadow1Blur to 4.0, tweenDouble(easeInOutCubic, pressAnimationTime)) { shadow1Blur = it }
    }

    override fun render(view: Button, canvas: Canvas) {
        if (fontLoadJob?.isActive == true) {
            return
        }

        val bounds = view.bounds.atOrigin

        canvas.outerShadow(color = Black opacity 0.2f, horizontal = 0.0, vertical = shadow1Blur, blurRadius = shadow1Blur) {
            canvas.rect(bounds.inset(Insets(left = 2.0, right = 3.0)), radius = cornerRadius, fill = ColorPaint(backgroundColor))
        }

        canvas.outerShadow(color = Black opacity 0.22f, horizontal = 0.0, vertical = 2.0, blurRadius = 2.0) {
            canvas.outerShadow(color = Black opacity 0.2f, vertical = 1.0, blurRadius = 5.0) {
                canvas.rect(bounds, cornerRadius, ColorPaint(backgroundColor))
            }
        }

        val text = view.text.uppercase()

        canvas.clip(bounds, cornerRadius) {
            canvas.text(text, view.font, textPosition(view, text, bounds = bounds), ColorPaint(textColor))

            canvas.rect(bounds, cornerRadius, ColorPaint(White opacity overlayOpacity))

            pointerPressLocation?.let { drawRipple(canvas, it, rippleOpacity, rippleProgress) }
        }
    }

    private fun fadeRipple() {
        rippleAnimation = animate(0.24f to 0f, tweenFloat(linear, pressAnimationTime * 2)) { rippleOpacity = it }.apply {
            completed += {
                if (!pointerPressed) {
                    pointerPressLocation = null
                }

                rippleOpacity   = 0.24f
                rippleAnimation = null
            }
        }
    }
}