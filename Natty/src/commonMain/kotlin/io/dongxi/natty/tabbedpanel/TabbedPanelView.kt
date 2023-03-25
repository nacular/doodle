package io.dongxi.natty.tabbedpanel

import io.dongxi.natty.storage.DataStore
import io.dongxi.natty.util.ClassUtils.simpleClassName
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ScrollPanelVisualizer
import io.nacular.doodle.controls.TextVisualizer
import io.nacular.doodle.controls.invoke
import io.nacular.doodle.controls.panels.TabbedPanel
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.core.renderProperty
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
import io.nacular.doodle.theme.native.NativeHyperLinkStyler
import io.nacular.doodle.utils.Resizer
import kotlinx.coroutines.CoroutineDispatcher

// https://nacular.github.io/doodle/docs/ui_components/overview#tabbedpanel
// https://nacular.github.io/doodle/docs/rendering/behaviors#specialized-behaviors

class TabbedPanelView(
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

    private val object1 = PlainView(config, textMetrics, tabName = "Tab A").apply { size = Size(50, 50) }
    private val object2 = PlainView(config, textMetrics, tabName = "Tab B").apply { size = Size(50, 50) }
    private val object3 = PlainView(config, textMetrics, tabName = "Tab C").apply { size = Size(50, 50) }
    private val object4 = PlainView(config, textMetrics, tabName = "Tab D").apply { size = Size(50, 50) }


    // Each tab preview shows hardcoded names
    private val defaultTabVisualizer = object : ItemVisualizer<View, Any> {
        private val textVisualizer = TextVisualizer()
        private val mapping = mapOf(
            object1 to object1.tabName,
            object2 to object2.tabName,
            object3 to object3.tabName,
            object4 to object4.tabName
        )

        override fun invoke(item: View, previous: View?, context: Any): View {
            return textVisualizer(mapping[item] ?: "Unknown")
        }
    }


    // Each object is displayed within a ScrollPanel
    private val tabbedPanel = TabbedPanel(
        ScrollPanelVisualizer(),
        defaultTabVisualizer,
        object1,
        object2,
        object3,
        object4
    ).apply {
        size = Size(500, 300)
        Resizer(this).apply { movable = false }
    }

    init {
        clipCanvasToBounds = false

        children += tabbedPanel

        layout = constrain(tabbedPanel, fill)

        Resizer(this)
    }

    override fun render(canvas: Canvas) {
        val foreGround = (foregroundColor ?: Color.White).paint
        val backGround = (backgroundColor ?: Color.Orange).paint

        /*
        canvas.rect(bounds.atOrigin, backGround)

        canvas.text(
            StyledText(title, config.titleFont, Color.Blue.paint),
            textCenterXPoint(this.width, this.titleWidth, 10)
        )
         */
    }
}
