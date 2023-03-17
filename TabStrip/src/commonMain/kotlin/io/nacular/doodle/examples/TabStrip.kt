package io.nacular.doodle.examples

import io.nacular.doodle.animation.Animation
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.Animator.Listener
import io.nacular.doodle.animation.transition.easeInOutCubic
import io.nacular.doodle.animation.transition.linear
import io.nacular.doodle.animation.tweenDouble
import io.nacular.doodle.animation.tweenFloat
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.event.PointerListener.Companion.clicked
import io.nacular.doodle.event.PointerMotionListener.Companion.moved
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.path
import io.nacular.doodle.system.Cursor.Companion.Default
import io.nacular.doodle.system.Cursor.Companion.Pointer
import io.nacular.doodle.utils.autoCanceling
import io.nacular.doodle.utils.observable
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.times
import kotlin.math.max

/**
 * Simple tab selector control that animates selection.
 *
 * @param animate used for animations
 * @param pathMetrics used to measure [Path]s
 */
class TabStrip(private val animate: Animator, private val pathMetrics: PathMetrics): View() {

    // Holds information about an item and it's current state
    private inner class ItemState(val selected: Path, val deselected: Path, var selectionProgress: Float = 0f) {
        val x          get() = position.x
        val y          get() = position.y
        val width      get() = size.width
        val height     get() = size.height
        val centerX    get() = x + width / 2
        val size             = pathMetrics.size(selected)
        val atDefaults get() = selectionProgress == 0f && moveProgress == 0f

        lateinit var position     : Point
                 var moveProgress = 0f
    }

    private val cornerRadius              = 31.0

    // List of items
    private val items = listOf(
        ItemState(
            selected   = path("M17.9992 9.0984 18 17.9681C18 18.4138 17.9536 18.5755 17.8664 18.7384 17.7793 18.9014 17.6514 19.0293 17.4884 19.1164 17.3255 19.2036 17.1638 19.25 16.7181 19.25H12.2819C11.8362 19.25 11.6745 19.2036 11.5116 19.1164 11.3486 19.0293 11.2207 18.9014 11.1336 18.7384 11.0464 18.5755 11 18.4138 11 17.9681V13.5319C11 13.0862 10.9536 12.9245 10.8664 12.7616 10.7793 12.5986 10.6514 12.4707 10.4884 12.3836 10.3255 12.2964 10.1638 12.25 9.7181 12.25H8.2819C7.8362 12.25 7.6745 12.2964 7.5116 12.3836 7.3486 12.4707 7.2207 12.5986 7.1336 12.7616 7.0464 12.9245 7 13.0862 7 13.5319V17.9681C7 18.4138 6.9536 18.5755 6.8664 18.7384 6.7793 18.9014 6.6514 19.0293 6.4884 19.1164 6.3255 19.2036 6.1638 19.25 5.7181 19.25H1.2819C.8362 19.25.6745 19.2036.5116 19.1164.3486 19.0293.2207 18.9014.1336 18.7384.0464 18.5755 0 18.4138 0 17.9681V9.2965C0 8.4601.0363 8.1664.121 7.8432.2058 7.5199.3391 7.2353.5332 6.9633L.6329 6.8302C.8069 6.6104 1.0367 6.3861 1.5722 5.9398L8.1575.4521C8.465.1958 8.5992.1296 8.7594.086 8.9195.0424 9.0805.0424 9.2406.086 9.4008.1296 9.535.1958 9.8425.4521L16.4278 5.9398C17.0704 6.4753 17.2728 6.6912 17.4668 6.9633 17.6609 7.2353 17.7942 7.5199 17.879 7.8432 17.9567 8.1395 17.9936 8.4109 17.9992 9.0984Z")!!,
            deselected = path("M8.5358.1609C8.8081-.0536 9.1919-.0536 9.4642.1609L16.5708 5.76C17.4733 6.4711 18 7.5567 18 8.7056V18C18 18.6904 17.4404 19.25 16.75 19.25H11.75C11.0596 19.25 10.5 18.6904 10.5 18V13C10.5 12.8619 10.3881 12.75 10.25 12.75H7.75C7.6119 12.75 7.5 12.8619 7.5 13V18C7.5 18.6904 6.9404 19.25 6.25 19.25H1.25C.5596 19.25 0 18.6904 0 18V8.7056C0 7.5567.5267 6.4711 1.4292 5.76L8.5358.1609ZM9 1.7048 2.3575 6.9383C1.816 7.3649 1.5 8.0162 1.5 8.7056V17.75H6V13C6 12.0335 6.7835 11.25 7.75 11.25H10.25C11.2165 11.25 12 12.0335 12 13V17.75H16.5V8.7056C16.5 8.0162 16.184 7.3649 15.6425 6.9383L9 1.7048Z")!!,
            selectionProgress = 1f
        ), // Home

        ItemState(
            selected   = path("M10 0C15.5228 0 20 4.4771 20 10 20 15.5228 15.5228 20 10 20 8.3596 20 6.7752 19.6039 5.3558 18.8583L1.0654 19.9753C.6111 20.0937.1469 19.8213.0286 19.367-.008 19.2266-.008 19.0791.0286 18.9386L1.1449 14.6502C.3972 13.2294 0 11.6428 0 10 0 4.4771 4.4771 0 10 0ZM11.2517 11H6.75L6.6482 11.0068C6.2821 11.0565 6 11.3703 6 11.75 6 12.1297 6.2821 12.4435 6.6482 12.4932L6.75 12.5H11.2517L11.3535 12.4932C11.7196 12.4435 12.0017 12.1297 12.0017 11.75 12.0017 11.3703 11.7196 11.0565 11.3535 11.0068L11.2517 11ZM13.25 7.5H6.75L6.6482 7.5069C6.2821 7.5565 6 7.8703 6 8.25 6 8.6297 6.2821 8.9435 6.6482 8.9932L6.75 9H13.25L13.3518 8.9932C13.7178 8.9435 14 8.6297 14 8.25 14 7.8703 13.7178 7.5565 13.3518 7.5069L13.25 7.5Z")!!,
            deselected = path("M10 0C15.5228 0 20 4.4771 20 10 20 15.5228 15.5228 20 10 20 8.3817 20 6.8178 19.6146 5.4129 18.888L1.587 19.9553C.9221 20.141.2326 19.7525.0469 19.0876-.0145 18.8676-.0145 18.6349.0469 18.4151L1.1146 14.5922C.3864 13.186 0 11.6203 0 10 0 4.4771 4.4771 0 10 0ZM10 1.5C5.3056 1.5 1.5 5.3056 1.5 10 1.5 11.4696 1.8728 12.8834 2.573 14.1375L2.7237 14.4072 1.611 18.3914 5.5976 17.2792 5.8671 17.4295C7.1201 18.1281 8.5322 18.5 10 18.5 14.6944 18.5 18.5 14.6944 18.5 10 18.5 5.3056 14.6944 1.5 10 1.5ZM6.75 11H11.2483C11.6625 11 11.9983 11.3358 11.9983 11.75 11.9983 12.1297 11.7161 12.4435 11.35 12.4932L11.2483 12.5H6.75C6.3358 12.5 6 12.1642 6 11.75 6 11.3703 6.2821 11.0565 6.6482 11.0068L6.75 11H11.2483 6.75ZM6.75 7.5H13.2545C13.6687 7.5 14.0045 7.8358 14.0045 8.25 14.0045 8.6297 13.7223 8.9435 13.3563 8.9932L13.2545 9H6.75C6.3358 9 6 8.6642 6 8.25 6 7.8703 6.2821 7.5565 6.6482 7.5069L6.75 7.5H13.2545 6.75Z")!!,
        ), // Chat

        ItemState(
            selected   = path("M11.821 2.5H17.75C18.8867 2.5 19.8266 3.343 19.9785 4.4379L19.9948 4.596 20 4.75V13.75C20 14.9409 19.0748 15.9156 17.904 15.9948L17.75 16H2.25C1.0591 16 .0844 15.0748.0052 13.904L0 13.75V6.499L6.2069 6.5 6.4033 6.4914C6.794 6.4572 7.169 6.3214 7.4909 6.0977L7.6473 5.9785 11.821 2.5ZM6.2069 0C6.6675 0 7.1153.1413 7.4909.4024L7.6473.5215 9.75 2.273 6.6871 4.8262 6.6022 4.8874C6.5136 4.9423 6.4145 4.9782 6.3113 4.9927L6.2069 5 0 4.999V2.25C0 1.0591.9252.0844 2.096.0052L2.25 0H6.2069Z")!!,
            deselected = path("M6.2069 0C6.6675 0 7.1153.1413 7.4909.4024L7.6473.5215 10.022 2.5H17.75C18.8867 2.5 19.8266 3.343 19.9785 4.4379L19.9948 4.596 20 4.75V13.75C20 14.9409 19.0748 15.9156 17.904 15.9948L17.75 16H2.25C1.0591 16 .0844 15.0748.0052 13.904L0 13.75V2.25C0 1.0591.9252.0844 2.096.0052L2.25 0H6.2069ZM7.6473 5.9785C7.2935 6.2733 6.8591 6.4515 6.4033 6.4914L6.2069 6.5 1.5 6.499V13.75C1.5 14.1297 1.7822 14.4435 2.1482 14.4932L2.25 14.5H17.75C18.1297 14.5 18.4435 14.2178 18.4932 13.8518L18.5 13.75V4.75C18.5 4.3703 18.2178 4.0565 17.8518 4.0069L17.75 4H10.021L7.6473 5.9785ZM6.2069 1.5H2.25C1.8703 1.5 1.5565 1.7821 1.5069 2.1482L1.5 2.25V4.999L6.2069 5C6.3473 5 6.4841 4.9606 6.6022 4.8874L6.6871 4.8262 8.578 3.249 6.6871 1.6738C6.5792 1.584 6.4489 1.5266 6.3113 1.5073L6.2069 1.5Z")!!,
        ), // Folder

        ItemState(
            selected   = path("M13.7545 11.9999C14.9966 11.9999 16.0034 13.0068 16.0034 14.2488V15.1673C16.0034 15.7406 15.8242 16.2996 15.4908 16.7661 13.9449 18.9294 11.4206 20.0011 8.0004 20.0011 4.5794 20.0011 2.0564 18.9289.5143 16.7646.1823 16.2987.0039 15.7408.0039 15.1688V14.2488C.0039 13.0068 1.0108 11.9999 2.2528 11.9999H13.7545ZM8.0004.0046C10.7618.0046 13.0004 2.2432 13.0004 5.0046 13.0004 7.766 10.7618 10.0046 8.0004 10.0046 5.2389 10.0046 3.0004 7.766 3.0004 5.0046 3.0004 2.2432 5.2389.0046 8.0004.0046Z")!!,
            deselected = path("M13.7545 11.9999C14.9966 11.9999 16.0034 13.0068 16.0034 14.2488V14.8242C16.0034 15.7185 15.6838 16.5833 15.1023 17.2627 13.5329 19.0962 11.1457 20.0011 8.0004 20.0011 4.8545 20.0011 2.4685 19.0959.9022 17.2617.3224 16.5827.0039 15.7193.0039 14.8265V14.2488C.0039 13.0068 1.0108 11.9999 2.2528 11.9999H13.7545ZM13.7545 13.4999H2.2528C1.8392 13.4999 1.5039 13.8352 1.5039 14.2488V14.8265C1.5039 15.3621 1.695 15.8802 2.0429 16.2876 3.2962 17.7553 5.2621 18.5011 8.0004 18.5011 10.7387 18.5011 12.7063 17.7552 13.9627 16.2873 14.3117 15.8797 14.5034 15.3608 14.5034 14.8242V14.2488C14.5034 13.8352 14.1681 13.4999 13.7545 13.4999ZM8.0004.0046C10.7618.0046 13.0004 2.2432 13.0004 5.0046 13.0004 7.766 10.7618 10.0046 8.0004 10.0046 5.2389 10.0046 3.0004 7.766 3.0004 5.0046 3.0004 2.2432 5.2389.0046 8.0004.0046ZM8.0004 1.5046C6.0674 1.5046 4.5004 3.0716 4.5004 5.0046 4.5004 6.9376 6.0674 8.5046 8.0004 8.5046 9.9334 8.5046 11.5004 6.9376 11.5004 5.0046 11.5004 3.0716 9.9334 1.5046 8.0004 1.5046Z")!!,
        ) // User
    )

    private val itemIndent                = 60.0
    private val totalItemWidth            = items.foldRight(0.0) { item, sum -> item.width + sum }
    private var itemSpace                 = (width - 2 * itemIndent - totalItemWidth) / (items.size - 1)
    private val itemDipOffset             = 4.0
    private val itemScaleChange           = 0.1

    // Indicator
    private val indicatorWidth            = 68.0
    private val minIndicatorHeight        =  8.0
    private val maxIndicatorHeight        = 23.0
    private val defaultIndicatorHeight    = 18.0

    private var indicatorPath             = path(Point(itemIndent, height)).close()
    private var indicatorCenter           = Point(0, height)
    private var indicatorHeight           by observable(defaultIndicatorHeight) { _,_ -> updateIndicatorPath() }

    // Droplet
    private val dropLetRadius             = 4.0
    private var dropletYAboveIndicator    = 0.0
    private val dropletMaxY: Double get() = height - (selectedItem.y + selectedItem.height + itemDipOffset - 2 * dropLetRadius) - defaultIndicatorHeight

    // Timings
    private val slideDuration         = 250 * milliseconds
    private val primeDuration         = 225 * milliseconds
    private val fireDuration          = 200 * milliseconds
    private val recoilDuration        = 200 * milliseconds
    private val dropletTravelDuration = 100 * milliseconds
    private val itemMoveDownDuration  = 185 * milliseconds
    private val itemMoveUpDuration    = 200 * milliseconds
    private val itemFillDuration      = 250 * milliseconds

    // Overall animation handle
    private var animation: Animation<*>? by autoCanceling()
    private val secondaryAnimations = mutableSetOf<Animation<*>>()

    // Monitor changes to the selected item to handle animation
    private var selectedItem by observable(items.first()) { _,selected ->
        // cancel any ongoing secondary animations and hide droplet
        dropletYAboveIndicator = 0.0
        secondaryAnimations.forEach { it.cancel() }
        secondaryAnimations.clear()

        // Animation blocks roll all top-level animations (those created while in the block) into a common
        // parent animation. Canceling that animation cancels all the children.
        // However, our code creates additional animations that are created when top-level ones are completed.
        // These animations are NOT tracked as part of the returned animation group. So they need to be tracked
        // separately, so we can cancel them if anything changes mid-flight.
        // We do that using the secondaryAnimations set.
        animation = animate {
            // All deselected items move back to normal
            items.filter { it != selected && !it.atDefaults }.forEach { deselected ->
                deselected.moveProgress      to 0f using (tweenFloat(linear, itemMoveUpDuration)) { deselected.moveProgress      = it }
                deselected.selectionProgress to 0f using (tweenFloat(linear, itemFillDuration  )) { deselected.selectionProgress = it }
            }

            // Indicator moves to selected item
            (indicatorCenter.x to selected.centerX using (tweenDouble(easeInOutCubic, slideDuration)) { indicatorCenter = Point(it, height) }).onCompleted {
                // Selected item moves down
                (selected.moveProgress to 1f using (tweenFloat(linear, itemMoveDownDuration)) { selected.moveProgress = it }).also { secondaryAnimations += it }
            }

            // Indicator primes as it travels to selected item
            (indicatorHeight to minIndicatorHeight using (tweenDouble(linear, primeDuration)) { indicatorHeight = it }).onCompleted {
                // NOTE: All these are secondary animations that won't be attached to the outer animation, since it would have been
                // completed at this point. So they need to be tracked using our secondaryAnimation set.

                // Indicator fires at selected item
                (indicatorHeight to maxIndicatorHeight using (tweenDouble(linear, fireDuration)) { indicatorHeight = it }).onCompleted {
                    // Indicator height returns to normal
                    (indicatorHeight to defaultIndicatorHeight using (tweenDouble(linear, recoilDuration)) { indicatorHeight = it }).also { secondaryAnimations += it }

                    // Droplet moves up to item
                    (dropletYAboveIndicator to dropletMaxY using (tweenDouble(linear, dropletTravelDuration)) { dropletYAboveIndicator = it }).onCompleted {
                        // Droplet is instantly hidden
                        dropletYAboveIndicator = 0.0

                        // Selected item moves up
                        (selected.moveProgress to 0f using (tweenFloat(linear, itemMoveUpDuration)) { selected.moveProgress = it }).also { secondaryAnimations += it }

                        // Selected item animates droplet within it
                        (selected.selectionProgress to 1f using (tweenFloat(linear, itemFillDuration)) { selected.selectionProgress = it }).also { secondaryAnimations += it }
                    }.also { secondaryAnimations += it }
                }.also { secondaryAnimations += it }
            }.also { secondaryAnimations += it }
        }
    }

    init {
        clipCanvasToBounds = false

        // Update paths etc. when bounds changes
        boundsChanged += { _,old,new ->
            if (old.size != new.size) {
                updatePaths()
                indicatorCenter = selectedItem.let { Point(it.x + it.width / 2, height) }
            }
        }

        // Rerender on animation updates
        animate.listeners += object: Listener {
            override fun changed(animator: Animator, animations: Set<Animation<*>>) {
                rerenderNow() // only called once per animation tick
            }
        }

        // Listen for item clicks
        pointerChanged += clicked { event ->
            getItem(at = event.location)?.let {
                selectedItem = it
                cursor       = Default
            }
        }

        // Update cursor as pointer moves
        pointerMotionChanged += moved { event ->
            cursor = when (getItem(event.location)) {
                selectedItem, null -> Default
                else               -> Pointer
            }
        }
    }

    override fun render(canvas: Canvas) {
        val foreGround = (foregroundColor ?: Black).paint
        val backGround = (backgroundColor ?: White).paint

        // draw shadow
        canvas.outerShadow(color = Black opacity 0.1f, blurRadius = 20.0) {
            // draw background rounded rect
            canvas.rect(bounds.atOrigin, radius = cornerRadius, fill = backGround)
        }

        // draw items
        items.forEach { item ->
            val itemScale = 1 - itemScaleChange * item.moveProgress

            // position and scale the item
            canvas.transform(Identity.
                translate(Point(item.x, item.y + itemDipOffset * item.moveProgress)).
                scale(around = Point(item.width / 2, item.height / 2), itemScale, itemScale)) {

                when (item.selectionProgress) {
                    1f   -> path(item.selected, fill = foreGround) // fully selected
                    else -> {
                        path(item.deselected, fill = foreGround)

                        if (item.selectionProgress > 0f) {
                            // overlay transition if partially selected
                            val dropletCircle = Circle(
                                center = Point(item.width / 2, item.height - dropLetRadius),
                                radius = dropLetRadius + (max(item.width, item.height) - dropLetRadius) * item.selectionProgress
                            )

                            // overlay background fill so it seeps through holes in item
                            circle(dropletCircle, fill = backGround)

                            // draw selected item clip to droplet
                            clip(dropletCircle) {
                                path(item.selected, fill = foreGround)
                            }
                        }
                    }
                }
            }
        }

        canvas.translate(indicatorCenter) {
            // draw indicator
            path(indicatorPath, fill = foreGround)

            if (dropletYAboveIndicator != 0.0) {
                // draw droplet so that it's top is at the indicator top when dropletYAboveIndicator == 0
                circle(Circle(
                    radius = dropLetRadius,
                    center = Point(0, -indicatorHeight + dropLetRadius - dropletYAboveIndicator)
                ), fill = foreGround)
            }
        }
    }

    private fun getItem(at: Point) = items.firstOrNull { at in Rectangle(it.position, it.size).inset(-itemSpace / 4) }

    private fun updatePaths() {
        var x     = itemIndent
        itemSpace = (width - 2 * itemIndent - totalItemWidth) / (items.size - 1)

        items.forEach { item ->
            item.position = Point(x, (height - item.height) / 2)
            x += itemSpace + item.width
        }

        updateIndicatorPath()
    }

    private fun updateIndicatorPath() {
        val indicatorHalfWidth = indicatorWidth / 2

        val controlPoint1 = Point(x = -indicatorHalfWidth) + Point(19.6113, -5.2466)
        val controlPoint2 = Point(x = -11.4078, -indicatorHeight)
        val controlPoint3 = controlPoint2 + Point(x = 2 * 11.4078)
        val controlPoint4 = Point(x = indicatorHalfWidth) + Point(-19.6113, -5.2466)

        indicatorPath = path(Point(x = -indicatorHalfWidth)).
            cubicTo(Point(y = -indicatorHeight  ), controlPoint1, controlPoint2).
            cubicTo(Point(x = indicatorHalfWidth), controlPoint3, controlPoint4).
            close()
    }

    private fun <T> Animation<T>.onCompleted(block: () -> Unit): Animation<T> = this.apply {
        completed += {
            block()
        }
    }
}