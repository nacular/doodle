@file:Suppress("DuplicatedCode")

package io.dongxi.natty


import io.dongxi.natty.storage.DataStore
import io.dongxi.natty.storage.PersistentStore
import io.dongxi.natty.tabbedpanel.NattyTabProducer
import io.dongxi.natty.tabbedpanel.TabbedPanelApp
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.AnimatorImpl
import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.ImageModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.coroutines.Dispatchers
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.impl.PathMetricsImpl
import io.nacular.doodle.theme.basic.BasicTheme.*
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicTabbedPanelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeHyperLinkBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeScrollPanelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import kotlinx.browser.window
import org.kodein.di.DI.Module
import org.kodein.di.bindSingleton
import org.kodein.di.instance


/**
 * Creates a [TabbedPanelApp]
 */
fun main() {
    application(modules = listOf(
        FontModule,
        PointerModule,
        KeyboardModule,
        ImageModule,
        basicLabelBehavior(),
        nativeTextFieldBehavior(),
        nativeHyperLinkBehavior(),
        nativeScrollPanelBehavior(smoothScrolling = true),
        basicTabbedPanelBehavior(NattyTabProducer(), Color(0xe4dfdeu), null),
        Module(name = "AppModule") {
            bindSingleton<Animator> { AnimatorImpl(timer = instance(), animationScheduler = instance()) }
            bindSingleton<PathMetrics> { PathMetricsImpl(svgFactory = instance()) }
            bindSingleton<PersistentStore> { LocalStorePersistence() }
            bindSingleton { DataStore(persistentStore = instance()) }
            bindSingleton<Router> { TrivialRouter(window) }
        }
    )) {
        TabbedPanelApp(
            display = instance(),
            uiDispatcher = Dispatchers.UI,
            animator = instance(),
            pathMetrics = instance(),
            dataStore = instance(),
            fonts = instance(),
            theme = instance(),
            themes = instance(),
            images = instance(),
            textMetrics = instance(),
            linkStyler = instance(),
            focusManager = instance()
        )
    }
}
