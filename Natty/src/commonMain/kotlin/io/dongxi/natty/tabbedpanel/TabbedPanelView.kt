package io.dongxi.natty.tabbedpanel

import io.dongxi.natty.storage.DataStore
import io.dongxi.natty.tabbedpanel.TabAttributes.*
import io.dongxi.natty.util.ClassUtils.simpleClassName
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ScrollPanelVisualizer
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


    // Natty:  Categories: aneis, colares, escapulários, pulseiras, brincos e sobre.

    private val homeView = PlainView(
        display,
        config,
        uiDispatcher,
        animator,
        pathMetrics,
        textMetrics,
        dataStore,
        images,
        linkStyler,
        focusManager,
        CASA
    ).apply { }
    private val ringsView = PlainView(
        display,
        config,
        uiDispatcher,
        animator,
        pathMetrics,
        textMetrics,
        dataStore,
        images,
        linkStyler,
        focusManager, ANEIS
    ).apply { }
    private val necklacesView = PlainView(
        display,
        config,
        uiDispatcher,
        animator,
        pathMetrics,
        textMetrics,
        dataStore,
        images,
        linkStyler,
        focusManager, COLARES
    ).apply { }
    private val scapularsView = PlainView(
        display,
        config,
        uiDispatcher,
        animator,
        pathMetrics,
        textMetrics,
        dataStore,
        images,
        linkStyler,
        focusManager, ESCAPULARIOS
    ).apply { }
    private val braceletsView = PlainView(
        display,
        config,
        uiDispatcher,
        animator,
        pathMetrics,
        textMetrics,
        dataStore,
        images,
        linkStyler,
        focusManager, PULSEIRAS
    ).apply { }
    private val earRingsView = PlainView(
        display,
        config,
        uiDispatcher,
        animator,
        pathMetrics,
        textMetrics,
        dataStore,
        images,
        linkStyler,
        focusManager, BRINCOS
    ).apply { }
    private val aboutView = PlainView(
        display,
        config,
        uiDispatcher,
        animator,
        pathMetrics,
        textMetrics,
        dataStore,
        images,
        linkStyler,
        focusManager, SOBRE
    ).apply { }
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
        tabbedPanel.render(canvas)
    }
}
