package com.nectar.doodle.theme.system

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.erased.bind
import com.github.salomonbrys.kodein.erased.instance
import com.github.salomonbrys.kodein.erased.singleton
import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.panels.ScrollPanel
import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Gizmo
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
import org.w3c.dom.HTMLElement

/**
 * Created by Nicholas Eddy on 1/28/18.
 */
class SystemTheme internal constructor(
        private val nativeButtonFactory     : NativeButtonFactory,
        private val nativeTextFieldFactory  : NativeTextFieldFactory,
        private val nativeScrollPanelFactory: NativeScrollPanelFactory): Theme {

    override fun install(display: Display, all: Sequence<Gizmo>) = all.forEach {
        when (it) {
            is Button      -> it.renderer = SystemButtonUI     (nativeButtonFactory,      it)
            is TextField   -> it.renderer = SystemTextFieldUI  (nativeTextFieldFactory,   it)
            is ScrollPanel -> it.renderer = SystemScrollPanelUI(nativeScrollPanelFactory, it)
        }
    }

    override fun uninstall(display: Display, all: Sequence<Gizmo>) = all.forEach {
        when (it) {
            is Button      -> it.renderer?.uninstall(it)
            is TextField   -> it.renderer?.uninstall(it)
            is ScrollPanel -> it.renderer?.uninstall(it)
        }
    }
}

val systemThemeModule = Kodein.Module {
    // TODO: Can this be handled better?
    bind<RealGraphicsSurfaceFactory>() with singleton { instance<GraphicsSurfaceFactory<*>>() as RealGraphicsSurfaceFactory }

    bind<NativeScrollPanelFactory> () with singleton { NativeScrollPanelFactoryImpl(instance(), instance()) }
    bind<NativeButtonFactory>      () with singleton { NativeButtonFactoryImpl(instance(), instance(), instance(), instance(), instance(), instance()) }
    bind<NativeTextFieldFactory>   () with singleton { NativeTextFieldFactoryImpl(instance(), instance()) }
    bind<NativeEventHandlerFactory>() with singleton { { element: HTMLElement, listener: NativeEventListener -> NativeEventHandlerImpl(element, listener) } }
    bind<SystemTheme>              () with singleton { SystemTheme(instance(), instance(), instance())  }
}