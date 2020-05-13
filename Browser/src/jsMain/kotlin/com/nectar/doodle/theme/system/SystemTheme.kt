package com.nectar.doodle.theme.system

import com.nectar.doodle.controls.Slider
import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.buttons.CheckBox
import com.nectar.doodle.controls.buttons.HyperLink
import com.nectar.doodle.controls.buttons.RadioButton
import com.nectar.doodle.controls.panels.ScrollPanel
import com.nectar.doodle.controls.text.TextField
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
import com.nectar.doodle.themes.Modules
import com.nectar.doodle.themes.Modules.Companion.bindBehavior
import com.nectar.doodle.themes.adhoc.AdhocTheme
import org.kodein.di.Kodein.Module
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.instanceOrNull
import org.kodein.di.erased.singleton
import org.kodein.di.erasedSet
import org.w3c.dom.HTMLElement

/**
 * Created by Nicholas Eddy on 1/28/18.
 */
class SystemTheme(behaviors: Iterable<Modules.BehaviorResolver>): AdhocTheme(behaviors.filter { it.theme == SystemTheme::class }) {
    override fun toString() = this::class.simpleName ?: ""

    companion object {
        val systemTheme = Module(name = "SystemTheme") {
            bind<SystemTheme>() with singleton { SystemTheme(Instance(erasedSet())) }
        }

        private val commonSystemModule = Module(allowSilentOverride = true, name = "CommonSystemModule") {

            // TODO: Can this be handled better?
            bind<RealGraphicsSurfaceFactory>() with singleton { instance<GraphicsSurfaceFactory<*>>() as RealGraphicsSurfaceFactory }

            bind<NativeEventHandlerFactory>() with singleton { { element: HTMLElement, listener: NativeEventListener -> NativeEventHandlerImpl(element, listener) } }
        }

        private val nativeCheckBoxRadioButtonBehavior = Module(name = "NativeCheckBoxRadioButtonBehavior") {
            importOnce(commonSystemModule, allowOverride = true)

            bind<NativeCheckBoxRadioButtonFactory>() with singleton { NativeCheckBoxRadioButtonFactoryImpl(instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }
        }

        val systemThemeBehaviors = Module(name = "SystemThemeBehaviors") {
            importOnce(systemButtonBehavior,      allowOverride = true)
            importOnce(systemSliderBehavior,      allowOverride = true)
            importOnce(systemCheckBoxBehavior,    allowOverride = true)
            importOnce(systemTextFieldBehavior,   allowOverride = true)
            importOnce(systemHyperLinkBehavior,   allowOverride = true)
            importOnce(systemScrollPanelBehavior, allowOverride = true)
            importOnce(systemRadioButtonBehavior, allowOverride = true)
        }

        val systemButtonBehavior = Module(name = "SystemButtonBehavior") {
            importOnce(commonSystemModule, allowOverride = true)

            bind<NativeButtonFactory>() with singleton { NativeButtonFactoryImpl(instance(), instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }

            bindBehavior<Button>(SystemTheme::class) { it.behavior = SystemButtonBehavior(instance(), instance(), it) }
        }

        val systemScrollPanelBehavior = Module(name = "SystemScrollPanelBehavior") {
            importOnce(commonSystemModule, allowOverride = true)

            bind<NativeScrollPanelFactory>() with singleton { NativeScrollPanelFactoryImpl(instance(), instance()) }

            bindBehavior<ScrollPanel>(SystemTheme::class) { it.behavior = SystemScrollPanelBehavior(instance(), it) }
        }

        val systemSliderBehavior = Module(name = "SystemSliderBehavior") {
            importOnce(commonSystemModule, allowOverride = true)

            bind<NativeSliderFactory>() with singleton { NativeSliderFactoryImpl(instance(), instance(), instance(), instanceOrNull()) }

            bindBehavior<Slider>(SystemTheme::class) { it.behavior = SystemSliderBehavior(instance(), it) }
        }

        val systemTextFieldBehavior = Module(name = "SystemTextFieldBehavior") {
            importOnce(commonSystemModule, allowOverride = true)

            bind<NativeTextFieldFactory>() with singleton { NativeTextFieldFactoryImpl(instance(), instance(), instance(), instanceOrNull(), instance()) }

            bindBehavior<TextField>(SystemTheme::class) { it.behavior = SystemTextFieldBehavior(instance(), it) }
        }

        val systemHyperLinkBehavior = Module(name = "SystemHyperLinkBehavior") {
            importOnce(commonSystemModule, allowOverride = true)

            bind<NativeHyperLinkFactory>() with singleton { NativeHyperLinkFactoryImpl(instance(), instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }

            bindBehavior<HyperLink>(SystemTheme::class) { it.behavior = SystemHyperLinkBehavior(instance(), instance(), it) as Behavior<Button> }
        }

        val systemCheckBoxBehavior = Module(name = "SystemCheckBoxBehavior") {
            importOnce(nativeCheckBoxRadioButtonBehavior, allowOverride = true)

            bindBehavior<CheckBox>(SystemTheme::class) { it.behavior = SystemCheckBoxBehavior(instance(), instance(), it) as Behavior<Button> }
        }

        val systemRadioButtonBehavior = Module(name = "SystemRadioButtonBehavior") {
            importOnce(nativeCheckBoxRadioButtonBehavior, allowOverride = true)

            bindBehavior<RadioButton>(SystemTheme::class) { it.behavior = SystemRadioButtonBehavior(instance(), instance(), it) as Behavior<Button> }
        }
    }
}