package com.nectar.doodle.theme.system

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.erased.bind
import com.github.salomonbrys.kodein.erased.instance
import com.github.salomonbrys.kodein.erased.provider
import com.github.salomonbrys.kodein.erased.singleton
import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.impl.GraphicsSurfaceFactory
import com.nectar.doodle.drawing.impl.NativeButtonFactory
import com.nectar.doodle.drawing.impl.NativeButtonFactoryImpl
import com.nectar.doodle.drawing.impl.NativeEventHandler
import com.nectar.doodle.drawing.impl.NativeEventHandlerImpl
import com.nectar.doodle.drawing.impl.NativeTextFieldFactory
import com.nectar.doodle.drawing.impl.NativeTextFieldFactoryImpl
import com.nectar.doodle.drawing.impl.RealGraphicsSurfaceFactory
import com.nectar.doodle.theme.Theme
import com.zinoti.jaz.controls.text.TextField

/**
 * Created by Nicholas Eddy on 1/28/18.
 */
class SystemTheme internal constructor(
        private val nativeButtonFactory   : NativeButtonFactory,
        private val nativeTextFieldFactory: NativeTextFieldFactory): Theme {

    override fun install(display: Display, all: Sequence<Gizmo>) = all.forEach {
        when (it) {
            is Button    -> it.renderer = SystemButtonUI   (nativeButtonFactory,    it)
            is TextField -> it.renderer = SystemTextFieldUI(nativeTextFieldFactory, it)
        }
    }

    override fun uninstall(display: Display, all: Sequence<Gizmo>) = all.forEach {
        when (it) {
            is Button    -> it.renderer?.uninstall(it)
            is TextField -> it.renderer?.uninstall(it)
        }
    }
}

val systemThemeModule = Kodein.Module {
    // TODO: Can this be handled better?
    bind<RealGraphicsSurfaceFactory>() with singleton { instance<GraphicsSurfaceFactory<*>>() as RealGraphicsSurfaceFactory }

    bind<NativeButtonFactory>   () with singleton { NativeButtonFactoryImpl(instance(), instance(), instance(), instance(), instance(), { instance() }) }
    bind<NativeTextFieldFactory>() with singleton { NativeTextFieldFactoryImpl(instance(), { instance() }) }
    bind<NativeEventHandler>    () with provider  { NativeEventHandlerImpl() }
    bind<SystemTheme>           () with singleton { SystemTheme(instance(), instance())  }
}