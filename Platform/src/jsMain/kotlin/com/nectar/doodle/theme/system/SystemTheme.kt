package com.nectar.doodle.theme.system

import com.nectar.doodle.application.themeModule
import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.panels.ScrollPanel
import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.dom.ElementRuler
import com.nectar.doodle.dom.ElementRulerImpl
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.drawing.impl.GraphicsSurfaceFactory
import com.nectar.doodle.drawing.impl.NativeButtonFactory
import com.nectar.doodle.drawing.impl.NativeButtonFactoryImpl
import com.nectar.doodle.drawing.impl.NativeEventHandlerFactory
import com.nectar.doodle.drawing.impl.NativeEventHandlerImpl
import com.nectar.doodle.drawing.impl.NativeEventListener
import com.nectar.doodle.drawing.impl.NativeScrollPanelFactory
import com.nectar.doodle.drawing.impl.NativeScrollPanelFactoryImpl
import com.nectar.doodle.drawing.impl.NativeTextFieldFactory
import com.nectar.doodle.drawing.impl.NativeTextFieldFactoryImpl
import com.nectar.doodle.drawing.impl.RealGraphicsSurfaceFactory
import com.nectar.doodle.theme.Theme
import org.kodein.di.Kodein.Module
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.instanceOrNull
import org.kodein.di.erased.singleton
import org.w3c.dom.HTMLElement

/**
 * Created by Nicholas Eddy on 1/28/18.
 */
class SystemTheme internal constructor(
        private val textMetrics             : TextMetrics,
        private val nativeButtonFactory     : NativeButtonFactory,
        private val nativeTextFieldFactory  : NativeTextFieldFactory,
        private val nativeScrollPanelFactory: NativeScrollPanelFactory): Theme {

    override fun install(display: Display, all: Sequence<View>) = all.forEach {
        when (it) {
            is Button      -> { it.renderer?.uninstall(it); it.renderer = SystemButtonUI     (nativeButtonFactory,      textMetrics, it).apply { install(it) } }
            is TextField   -> { it.renderer?.uninstall(it); it.renderer = SystemTextFieldUI  (nativeTextFieldFactory,   it             ).apply { install(it) } }
            is ScrollPanel -> { it.renderer?.uninstall(it); it.renderer = SystemScrollPanelUI(nativeScrollPanelFactory, it             ).apply { install(it) } }
        }
    }

    override fun toString() = this::class.simpleName ?: ""
}

val systemThemeModule = Module {
    // TODO: Can this be handled better?
    bind<RealGraphicsSurfaceFactory>() with singleton { instance<GraphicsSurfaceFactory<*>>() as RealGraphicsSurfaceFactory }

    bind<ElementRuler>             () with singleton { ElementRulerImpl            (instance()                                                                              ) }
    bind<NativeScrollPanelFactory> () with singleton { NativeScrollPanelFactoryImpl(instance(), instance()                                                                  ) }
    bind<NativeButtonFactory>      () with singleton { NativeButtonFactoryImpl     (instance(), instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }
    bind<NativeTextFieldFactory>   () with singleton { NativeTextFieldFactoryImpl  (instance(), instance(), instanceOrNull()                                                ) }
    bind<SystemTheme>              () with singleton { SystemTheme                 (instance(), instance(), instance(), instance()                                          ) }
    bind<NativeEventHandlerFactory>() with singleton { { element: HTMLElement, listener: NativeEventListener -> NativeEventHandlerImpl(element, listener) } }

    import(themeModule, allowOverride = true)
}