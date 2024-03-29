package io.nacular.doodle.application

import io.nacular.doodle.HtmlElementViewFactory
import io.nacular.doodle.HtmlElementViewFactoryImpl
import org.kodein.di.DI.Module
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

/** Enable use of [HtmlElementViewFactory]. */
public actual val Modules.Companion.HtmlElementViewModule: Module get() = Module(allowSilentOverride = true, name = "ForeignView") {
    bind<HtmlElementViewFactory>() with singleton {
        HtmlElementViewFactoryImpl(instance(), instance(), instance())
    }
}