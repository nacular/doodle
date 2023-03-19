package io.dongxi.natty.application

// import io.nacular.doodle.examples.DataStore.DataStoreListModel
// import io.nacular.doodle.examples.DataStore.Filter
// import io.nacular.doodle.examples.DataStore.Filter.Active
// import io.nacular.doodle.examples.DataStore.Filter.Completed
import io.dongxi.natty.menu.MenuView
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.application.Application
import io.nacular.doodle.core.Display
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image
import io.nacular.doodle.layout.constraints.constrain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


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
    animator: Animator,
    pathMetrics: PathMetrics
) : Application {

    init {
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

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
    }

    override fun shutdown() {
    }
}
