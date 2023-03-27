package io.dongxi.natty.tabbedpanel

import io.dongxi.natty.storage.DataStore
import io.dongxi.natty.util.ClassUtils
import io.dongxi.natty.util.ViewUtils
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.core.*
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.theme.native.NativeHyperLinkStyler
import io.nacular.doodle.utils.Resizer
import kotlinx.coroutines.CoroutineDispatcher

/**
 * The main view for each tab is the same, containing four nested views:  left, center, right and footer.
 */
class TabView(
    private val display: Display,
    private val config: NattyAppConfig,
    private val uiDispatcher: CoroutineDispatcher,
    private val animator: Animator,
    private val pathMetrics: PathMetrics,
    private val textMetrics: TextMetrics,
    private val dataStore: DataStore,
    private val images: ImageLoader,
    private val linkStyler: NativeHyperLinkStyler,
    private val focusManager: FocusManager,
    private val tabAttributes: TabAttributes
) : View() {

    private var title by renderProperty(ClassUtils.simpleClassName(this))
    private val titleWidth = textMetrics.width(title)

    val styledTabName: StyledText = StyledText(
        tabAttributes.tabName,
        config.tabPanelFont,
        Color(0x733236u).paint
    )

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
        size = Size(display!!.width / 3, display!!.height - 105)
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
        size = Size(display!!.width / 3, display!!.height - 105)
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
        size = Size(display!!.width / 3, display!!.height - 105)
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
        size = Size(display!!.width, 100.00)
    }

    private val contentContainer = object : Container() {
        init {
            clipCanvasToBounds = false

            size = Size(display.width, display.height - 200)

            children += listOf(leftView, centerView, rightView) // footerView excluded

            layout = constrain(children[0], children[1], children[2]) { left, center, right ->

                val contentViewBoundariesMap = ViewUtils.getContentViewBoundaries(display)
                val leftViewBoundaries = contentViewBoundariesMap.get(ClassUtils.simpleClassName(children[0]))
                val centerViewBoundaries = contentViewBoundariesMap.get(ClassUtils.simpleClassName(children[1]))
                val rightViewBoundaries = contentViewBoundariesMap.get(ClassUtils.simpleClassName(children[2]))
                // val footerViewBoundaries = contentViewBoundariesMap.get(ClassUtils.simpleClassName(children[0]))


                left.top eq 5
                left.left eq 5
                left.width eq display.width / 4
                left.bottom eq display.height - 200
                /*
                left.top eq leftViewBoundaries!!.top
                left.left eq leftViewBoundaries!!.left
                left.width eq leftViewBoundaries!!.width
                left.bottom eq leftViewBoundaries!!.bottom
                 */

                center.top eq left.top
                center.left eq left.right + 5
                center.width eq display.width / 2
                center.bottom eq left.bottom
                /*
                center.top eq centerViewBoundaries!!.top
                center.left eq centerViewBoundaries!!.left
                center.width eq centerViewBoundaries!!.width
                center.bottom eq centerViewBoundaries!!.bottom
                 */

                right.top eq left.top
                right.left eq center.right + 5
                right.right eq display.width - 5
                right.bottom eq left.bottom
                /*
                right.top eq rightViewBoundaries!!.top
                right.left eq rightViewBoundaries!!.left
                right.width eq rightViewBoundaries!!.width
                right.bottom eq rightViewBoundaries!!.bottom
                 */

                //footer.top eq left.bottom + 5
                //footer.left eq left.left
                //footer.right eq display.width - 5
                //footer.bottom eq display.height - 5
            }
            Resizer(this)
        }
    }


    init {
        clipCanvasToBounds = false

        size = Size(display.width, display.height)

        display.layout = constrain(this) {
            it.edges eq parent.edges
        }


        children += contentContainer

        // Never constrain `this` !!!
        // Constrain the child, not this.
        layout = constrain(contentContainer, fill)

        Resizer(this).apply { movable = false }
    }

    override fun render(canvas: Canvas) {
        val backGround = (backgroundColor ?: tabAttributes.color).paint
        canvas.rect(bounds.atOrigin, backGround)
    }
}