package io.dongxi.natty.view


import io.dongxi.natty.application.NattyAppConfig
import io.dongxi.natty.storage.DataStore
import io.dongxi.natty.util.ClassUtils.simpleClassName
import io.dongxi.natty.util.PointUtils.textCenterXPoint
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.core.*
import io.nacular.doodle.drawing.*
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.theme.native.NativeHyperLinkStyler
import io.nacular.doodle.utils.HorizontalAlignment.*
import io.nacular.doodle.utils.Resizer
import io.nacular.doodle.utils.VerticalAlignment.*
import kotlinx.coroutines.CoroutineDispatcher

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
    private val uiDispatcher: CoroutineDispatcher,
    private val animator: Animator,
    private val pathMetrics: PathMetrics,
    private val textMetrics: TextMetrics,
    private val dataStore: DataStore,
    private val images: ImageLoader,
    private val linkStyler: NativeHyperLinkStyler,
    private val focusManager: FocusManager
) : View() {

    private var title by renderProperty(simpleClassName(this))  // var is not final (is mutable)
    private val titleWidth = textMetrics.width(title)     // val is final (immutable)

    private val menu = Menu(animator, pathMetrics).apply {
        size = Size(500, 75)
    }

    private val leftView = LeftView(
        config,
        uiDispatcher,
        animator,
        pathMetrics,
        textMetrics,
        dataStore,
        images,
        linkStyler,
        focusManager
    ).apply {
        size = Size(display.width / 3, 700.00)
    }
    private val centerView = CenterView(
        config,
        uiDispatcher,
        animator,
        pathMetrics,
        textMetrics,
        dataStore,
        images,
        linkStyler,
        focusManager
    ).apply {
        size = Size(display.width / 3, 700.00)
    }
    private val rightView = RightView(
        config,
        uiDispatcher,
        animator,
        pathMetrics,
        textMetrics,
        dataStore,
        images,
        linkStyler,
        focusManager
    ).apply {
        size = Size(display.width / 3, 700.00)
    }
    private val footerView = FooterView(
        config,
        uiDispatcher,
        animator,
        pathMetrics,
        textMetrics,
        dataStore,
        images,
        linkStyler,
        focusManager
    ).apply {
        size = Size(1000, 100)
    }

    private val contentContainer = object : Container() {
        init {
            clipCanvasToBounds = false
            size = Size(display.width, 800.00)
            children += listOf(leftView, centerView, rightView)
            layout = constrain(children[0], children[1], children[2]) { left, center, right ->
                left.top eq 5
                left.left eq 5
                left.width eq display.width / 4
                left.bottom eq display.height - 200

                center.top eq left.top
                center.left eq left.right + 5
                center.width eq display.width / 2
                center.bottom eq left.bottom

                right.top eq left.top
                right.left eq center.right + 5
                right.right eq display.width - 5
                right.bottom eq left.bottom
            }
            Resizer(this)
        }
    }

    private val mainContainer = object : Container() {
        init {
            clipCanvasToBounds = false
            size = display.size
            children += listOf(menu, contentContainer, footerView)
            layout = constrain(children[0], children[1], children[2]) { menu, content, footer ->
                menu.top eq 50
                menu.centerX eq parent.centerX
                menu.height eq 75
                menu.width eq parent.width / 2

                content.top eq 50 + menu.height
                content.centerX eq parent.centerX
                content.width eq parent.width
                content.height eq display.height - 200

                footer.top eq content.bottom + 5
                footer.centerX eq content.centerX
                footer.width eq content.width - 5
                footer.bottom eq display.height - 5

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
            at = textCenterXPoint(this.width, this.titleWidth, 10),
            color = Color.Black
        )
    }
}
