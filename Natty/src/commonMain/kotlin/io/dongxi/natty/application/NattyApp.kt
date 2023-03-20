package io.dongxi.natty.application

// import io.nacular.doodle.examples.DataStore.DataStoreListModel
// import io.nacular.doodle.examples.DataStore.Filter
// import io.nacular.doodle.examples.DataStore.Filter.Active
// import io.nacular.doodle.examples.DataStore.Filter.Completed
import io.dongxi.natty.menu.MenuView
import io.dongxi.natty.storage.DataStore
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.application.Application
import io.nacular.doodle.core.Display
import io.nacular.doodle.drawing.*
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import kotlinx.coroutines.*


/**
 * General styling config
 */
data class NattyAppConfig(
    val listFont: Font,
    val titleFont: Font,
    val lineColor: Color = Color(0xEDEDEDu),
    val filterFont: Font,
    val footerFont: Font,
    val headerColor: Color = Color(0xAF2F2Fu) opacity 0.15f,
    val deleteColor: Color = Color(0xCC9A9Au),
    val appBackground: Color = Color(0xF5F5F5u),
    val boldFooterFont: Font,
    val selectAllColor: Color = Color(0x737373u),
    val checkForeground: Image,
    val checkBackground: Image,
    val placeHolderFont: Font,
    val placeHolderText: String = "What needs to be done?",
    val placeHolderColor: Color = Color(0xE6E6E6u),
    val labelForeground: Color = Color(0x4D4D4Du),
    val footerForeground: Color = Color(0xBFBFBFu),
    val deleteHoverColor: Color = Color(0xAF5B5Eu),
    val taskCompletedColor: Color = Color(0xD9D9D9u),
    val clearCompletedText: String = "Clear completed",
    val textFieldBackground: Color = Color.White,
    val filterButtonForeground: Color = Color(0x777777u)
)


/**
 * Natty App should be based on TodoMVC?
 */
class NattyApp(
    display: Display,
    uiDispatcher: CoroutineDispatcher,
    animator: Animator,
    pathMetrics: PathMetrics,
    dataStore: DataStore,
    fonts: FontLoader,
    theme: DynamicTheme,
    themes: ThemeManager,
    images: ImageLoader,
    focusManager: FocusManager
) : Application {

    init {
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        // Launch coroutine to fetch fonts/images
        appScope.launch(uiDispatcher) {
            val titleFont = fonts {
                size = 100; weight = 100; families = listOf("Helvetica Neue", "Helvetica", "Arial", "sans-serif")
            }!!
            val listFont = fonts(titleFont) { size = 24 }!! // !! -> raises NullPointerException ?
            val footerFont = fonts(titleFont) { size = 10 }!!
            val config = NattyAppConfig(
                listFont = listFont,
                titleFont = titleFont,
                footerFont = footerFont,
                filterFont = fonts(titleFont) { size = 14 }!!,
                boldFooterFont = fonts(footerFont) { weight = 400 }!!,
                placeHolderFont = fonts(listFont) { style = Font.Style.Italic }!!,
                checkForeground = images.load("data:image/svg+xml;utf8,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20width%3D%2240%22%20height%3D%2240%22%20viewBox%3D%22-10%20-18%20100%20135%22%3E%3Ccircle%20cx%3D%2250%22%20cy%3D%2250%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23bddad5%22%20stroke-width%3D%223%22/%3E%3Cpath%20fill%3D%22%235dc2af%22%20d%3D%22M72%2025L42%2071%2027%2056l-4%204%2020%2020%2034-52z%22/%3E%3C/svg%3E")!!,
                checkBackground = images.load("data:image/svg+xml;utf8,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20width%3D%2240%22%20height%3D%2240%22%20viewBox%3D%22-10%20-18%20100%20135%22%3E%3Ccircle%20cx%3D%2250%22%20cy%3D%2250%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23ededed%22%20stroke-width%3D%223%22/%3E%3C/svg%3E")!!
            )

            // install theme
            themes.selected = theme

            // display += TodoView(config, dataStore, linkStyler, textMetrics, focusManager, filterButtonProvider)
            display += MenuView(animator, pathMetrics).apply {
                size = Size(500, 100)
            }

            // display.layout = constrain(display.children[0]) { it.edges eq parent.edges }
            display.layout = constrain(display.children[0]) {
                it.top eq 2
                it.centerX eq parent.centerX
            }

            display.fill(config.appBackground.paint)
        }

        /*
        // create and display a single Menu
        with(display) {

            this += MenuView(animator, pathMetrics).apply {
                size = Size(500, 100)
            }

            layout = constrain(first()) {
                it.top eq 2
                it.centerX eq parent.centerX
            }
        }
         */
    }

    override fun shutdown() {
    }
}
