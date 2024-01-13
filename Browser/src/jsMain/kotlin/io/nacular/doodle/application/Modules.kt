package io.nacular.doodle.application

import io.nacular.doodle.HtmlElementViewFactory
import io.nacular.doodle.HtmlElementViewFactoryImpl
import org.kodein.di.DI.Module
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

public actual val Modules.Companion.HtmlElementViewFactory: Module get() = Module(allowSilentOverride = true, name = "ForeignView") {
    bind<HtmlElementViewFactory>() with singleton {
        HtmlElementViewFactoryImpl(instance(), instance(), instance())
    }
}