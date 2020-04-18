package com.nectar.doodle.theme.system

import com.nectar.doodle.controls.Slider
import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.buttons.CheckBox
import com.nectar.doodle.controls.buttons.HyperLink
import com.nectar.doodle.controls.buttons.RadioButton
import com.nectar.doodle.controls.panels.ScrollPanel
import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.drawing.impl.GraphicsSurfaceFactory
import com.nectar.doodle.drawing.impl.NativeButtonFactory
import com.nectar.doodle.drawing.impl.NativeButtonFactoryImpl
import com.nectar.doodle.drawing.impl.NativeCheckBoxRadioButtonFactory
import com.nectar.doodle.drawing.impl.NativeCheckBoxRadioButtonFactoryImpl
import com.nectar.doodle.drawing.impl.NativeEventHandlerFactory
import com.nectar.doodle.drawing.impl.NativeEventHandlerImpl
import com.nectar.doodle.drawing.impl.NativeEventListener
import com.nectar.doodle.drawing.impl.NativeHyperLinkFactory
import com.nectar.doodle.drawing.impl.NativeHyperLinkFactoryImpl
import com.nectar.doodle.drawing.impl.NativeScrollPanelFactory
import com.nectar.doodle.drawing.impl.NativeScrollPanelFactoryImpl
import com.nectar.doodle.drawing.impl.NativeSliderFactory
import com.nectar.doodle.drawing.impl.NativeSliderFactoryImpl
import com.nectar.doodle.drawing.impl.NativeTextFieldFactory
import com.nectar.doodle.drawing.impl.NativeTextFieldFactoryImpl
import com.nectar.doodle.drawing.impl.RealGraphicsSurfaceFactory
import com.nectar.doodle.theme.Behavior
import com.nectar.doodle.themes.Modules.Companion.themeModule
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
        private val textMetrics                     : TextMetrics,
        private val nativeButtonFactory             : NativeButtonFactory,
        private val nativeHyperLinkFactory          : NativeHyperLinkFactory,
        private val nativeSliderFactory             : NativeSliderFactory,
        private val nativeTextFieldFactory          : NativeTextFieldFactory,
        private val nativeScrollPanelFactory        : NativeScrollPanelFactory,
        private val nativeCheckBoxRadioButtonFactory: NativeCheckBoxRadioButtonFactory): Theme {

    override fun install(display: Display, all: Sequence<View>) = all.forEach {
        when (it) {
            is RadioButton -> it.behavior = SystemRadioButtonBehavior(nativeCheckBoxRadioButtonFactory, textMetrics, it) as Behavior<Button>
            is CheckBox    -> it.behavior = SystemCheckBoxBehavior   (nativeCheckBoxRadioButtonFactory, textMetrics, it) as Behavior<Button>
            is HyperLink   -> it.behavior = SystemHyperLinkBehavior  (nativeHyperLinkFactory,           textMetrics, it) as Behavior<Button>
            is Button      -> it.behavior = SystemButtonBehavior     (nativeButtonFactory,              textMetrics, it)
            is Slider      -> it.behavior = SystemSliderBehavior     (nativeSliderFactory,              it             )
            is TextField   -> it.behavior = SystemTextFieldBehavior  (nativeTextFieldFactory,           it             )
            is ScrollPanel -> it.behavior = SystemScrollPanelBehavior(nativeScrollPanelFactory,         it             )
        }
    }

    override fun toString() = this::class.simpleName ?: ""

    companion object {
        val systemThemeModule = Module(allowSilentOverride = true, name = "SystemTheme") {
            // TODO: Can this be handled better?
            bind<RealGraphicsSurfaceFactory>      () with singleton { instance<GraphicsSurfaceFactory<*>>() as RealGraphicsSurfaceFactory }

            bind<NativeScrollPanelFactory>        () with singleton { NativeScrollPanelFactoryImpl(instance(), instance()) }
            bind<NativeButtonFactory>             () with singleton { NativeButtonFactoryImpl(instance(), instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }
            bind<NativeSliderFactory>             () with singleton { NativeSliderFactoryImpl(instance(), instance(), instance(), instanceOrNull()) }
            bind<NativeTextFieldFactory>          () with singleton { NativeTextFieldFactoryImpl  (instance(), instance(), instance(), instanceOrNull(), instance()                        ) }
            bind<SystemTheme>                     () with singleton { SystemTheme                 (instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
            bind<NativeEventHandlerFactory>       () with singleton { { element: HTMLElement, listener: NativeEventListener -> NativeEventHandlerImpl(element, listener) } }
            bind<NativeCheckBoxRadioButtonFactory>() with singleton { NativeCheckBoxRadioButtonFactoryImpl(instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }
            bind<NativeHyperLinkFactory>          () with singleton { NativeHyperLinkFactoryImpl(instance(), instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }

            importOnce(themeModule, allowOverride = true)
        }
    }
}