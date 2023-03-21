package io.dongxi.natty.view


import io.dongxi.natty.application.NattyAppConfig
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.core.View
import io.nacular.doodle.core.renderProperty
import io.nacular.doodle.drawing.*
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import kotlin.math.roundToInt

/**
 * The MainView is only direct descendent (child) of the Display.
 * All other views in the application are contained in it.
 * @param animate used for animations
 * @param pathMetrics used to measure [Path]s
 * @param textMetrics used to measure Text
 */
class MainView(
    private val config: NattyAppConfig,
    private val animate: Animator,
    private val pathMetrics: PathMetrics,
    private val textMetrics: TextMetrics
) : View() {

    private var title by renderProperty("MainView") // var is not final (is mutable)
    private val titleWidth = textMetrics.width(title)     // val is final (immutable)

    init {
        clipCanvasToBounds = false // nothing rendered shows beyond its [bounds]

        boundsChanged += { _, old, new ->
            if (old.x != new.x) {
                this@MainView.relayout()
            }
        }
    }
    
    override fun render(canvas: Canvas) {
        val foreGround = (foregroundColor ?: Color.White).paint
        val backGround = (backgroundColor ?: Color.Orange).paint

        canvas.rect(bounds.atOrigin, backGround)

        canvas.text(
            text = title,
            font = config.titleFont,
            at = centeredTitlePoint(),
            color = Color.Black
        )
    }

    private fun centeredTitlePoint(): Point {
        val x = (this.width - this.titleWidth.roundToInt()) / 2
        val y = 10
        return Point(x, y)
    }

}
