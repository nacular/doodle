package io.dongxi.natty.menu

import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.AnimatorImpl
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.impl.PathMetricsImpl
import org.kodein.di.DI.Module
import org.kodein.di.bindSingleton
import org.kodein.di.instance


/**
 * Creates a Natty [MenuApp]
 */
fun main() {
    application(modules = listOf(PointerModule, Module(name = "AppModule") {
        bindSingleton<Animator> { AnimatorImpl(instance(), instance()) }
        bindSingleton<PathMetrics> { PathMetricsImpl(instance()) }
    })) {
        // load app
        MenuApp(instance(), instance(), instance())
    }
}
