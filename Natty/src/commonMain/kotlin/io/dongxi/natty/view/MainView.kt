package io.dongxi.natty.view


import io.dongxi.natty.application.NattyAppConfig
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.core.*
import io.nacular.doodle.drawing.*
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.utils.Resizer
import kotlin.math.roundToInt

/**
 * The MainView is only direct descendent (child) of the Display.
 * All other views in the application are contained in it.
 * @param animate used for animations
 * @param pathMetrics used to measure [Path]s
 * @param textMetrics used to measure Text
 */
class MainView(
    private val display: Display,
    private val config: NattyAppConfig,
    private val animator: Animator,
    private val pathMetrics: PathMetrics,
    private val textMetrics: TextMetrics
) : View() {

    private var title by renderProperty("MainView") // var is not final (is mutable)
    private val titleWidth = textMetrics.width(title)     // val is final (immutable)

    private val menu = Menu(animator, pathMetrics).apply {
        size = Size(500, 100)
    }

    // You can constrain any set of Views, regardless of their hierarchy. But, the Constraint Layout will only
    // update the Views that within the Container it is laying out. All other Views are treated as readOnly.
    // This adjustment happens automatically as the View hierarchy changes. A key consequence is that Views
    // outside the current parent will not conform to any constraints they "participate" in. This avoids the issue
    // of a layout for one container affecting the children of another.
    // See https://nacular.github.io/doodle/docs/layout/constraints#non-siblings-constraints
    private val mainContainer = object : Container() {
        init {
            clipCanvasToBounds = false
            size = display.size
            children += menu
            layout = constrain(menu) { menu ->
                menu.top eq 50
                menu.centerX eq parent.centerX
                menu.width eq parent.width / 2
            }

            Resizer(this)
        }
    }

    init {
        clipCanvasToBounds = false // nothing rendered shows beyond its [bounds]

        layout = constrain(mainContainer) {
            it.edges eq parent.edges
            it.centerX eq parent.centerX
            it.centerY eq parent.centerY
        }

        children += mainContainer

        Resizer(this)
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

        this.menu.boundsChanged
    }

    private fun centeredTitlePoint(): Point {
        val x = (this.width - this.titleWidth.roundToInt()) / 2
        val y = 10
        return Point(x, y)
    }
}
