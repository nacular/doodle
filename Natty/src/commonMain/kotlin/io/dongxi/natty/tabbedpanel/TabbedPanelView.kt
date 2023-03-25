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
import io.nacular.doodle.text.StyledText
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

    // Natty:  Categories: aneis, colares, escapulários, pulseiras, braceletes (=pulseiras), brincos e sobre.
    private val homeView = PlainView(config, textMetrics, tabName = "Casa").apply { size = Size(50, 50) }
    private val ringsView = PlainView(config, textMetrics, tabName = "Aneis").apply { size = Size(50, 50) }
    private val necklacesView = PlainView(config, textMetrics, tabName = "Colares").apply { size = Size(50, 50) }
    private val scapularsView = PlainView(config, textMetrics, tabName = "Escapulários").apply { size = Size(50, 50) }
    private val braceletsView = PlainView(config, textMetrics, tabName = "Pulseiras").apply { size = Size(50, 50) }
    private val earRingsView = PlainView(config, textMetrics, tabName = "Brincos").apply { size = Size(50, 50) }
    private val aboutView = PlainView(config, textMetrics, tabName = "Sobre").apply { size = Size(50, 50) }
    // Natty:  Maybe Sub Categories, or ways the person could find big earrings, small earrings, for instance.

    // Each tab preview shows names as StyledText.
    private val styledTextTabVisualizer = object : ItemVisualizer<View, Any> {
        private val textVisualizer = StyledTextVisualizer()
        private val mapping = mapOf(
            homeView to homeView.styledTabName,
            ringsView to ringsView.styledTabName,
            necklacesView to necklacesView.styledTabName,
            scapularsView to scapularsView.styledTabName,
            braceletsView to braceletsView.styledTabName,
            earRingsView to earRingsView.styledTabName,
            aboutView to aboutView.styledTabName
        )

        override fun invoke(item: View, previous: View?, context: Any): View {
            return textVisualizer(
                mapping[item] ?: StyledText("Desconhecido", config.tabPanelFont, Color.Red.paint)
            )
        }
    }


    // Each tab preview shows names as Strings.
    private val defaultTabVisualizer = object : ItemVisualizer<View, Any> {
        private val textVisualizer = TextVisualizer()
        private val mapping = mapOf(
            homeView to homeView.tabName,
            ringsView to ringsView.tabName,
            necklacesView to necklacesView.tabName,
            scapularsView to scapularsView.tabName,
            braceletsView to braceletsView.tabName,
            earRingsView to earRingsView.tabName,
            aboutView to aboutView.tabName
        )

        override fun invoke(item: View, previous: View?, context: Any): View {
            return textVisualizer(mapping[item] ?: "Desconhecido")
        }
    }


    // Each object is displayed within a ScrollPanel
    private val tabbedPanel = TabbedPanel(
        ScrollPanelVisualizer(),
        styledTextTabVisualizer,
        homeView,
        ringsView,
        necklacesView,
        scapularsView,
        braceletsView,
        earRingsView,
        aboutView
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
