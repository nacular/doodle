package io.dongxi.natty


import io.dongxi.natty.application.NattyApp
import io.dongxi.natty.storage.DataStore
import io.dongxi.natty.storage.PersistentStore
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.AnimatorImpl
import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.ImageModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.coroutines.Dispatchers
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.impl.PathMetricsImpl
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeHyperLinkBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeScrollPanelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import kotlinx.browser.window
import org.kodein.di.DI.Module
import org.kodein.di.bindSingleton
import org.kodein.di.instance


/**
 * Creates a [NattyApp]
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
        Module(name = "AppModule") {
            bindSingleton<Animator> { AnimatorImpl(instance() /*of Timer*/, instance() /*of AnimationScheduler*/) }
            bindSingleton<PathMetrics> { PathMetricsImpl(instance() /*of SvgFactory*/) }
            bindSingleton<PersistentStore> { LocalStorePersistence() }
            bindSingleton { DataStore(instance() /*of PersistentStore*/) }
            bindSingleton<Router> { TrivialRouter(window) }
        }
    )) {
        NattyApp(
            instance(),
            Dispatchers.UI,
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
}
