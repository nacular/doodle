package com.nectar.doodle.theme.native

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
import com.nectar.doodle.theme.Modules.BehaviorResolver
import com.nectar.doodle.theme.Modules.Companion.bindBehavior
import com.nectar.doodle.theme.adhoc.AdhocTheme
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
class NativeTheme(behaviors: Iterable<BehaviorResolver>): AdhocTheme(behaviors.filter { it.theme == NativeTheme::class }) {
    override fun toString() = this::class.simpleName ?: ""

    companion object {
        val nativeTheme = Module(name = "NativeTheme") {
            bind<NativeTheme>() with singleton { NativeTheme(Instance(erasedSet())) }
        }

        private val commonNativeModule = Module(allowSilentOverride = true, name = "CommonNativeModule") {

            // TODO: Can this be handled better?
            bind<RealGraphicsSurfaceFactory>() with singleton { instance<GraphicsSurfaceFactory<*>>() as RealGraphicsSurfaceFactory }

            bind<NativeEventHandlerFactory>() with singleton { { element: HTMLElement, listener: NativeEventListener -> NativeEventHandlerImpl(element, listener) } }
        }

        private val nativeCheckBoxRadioButtonBehavior = Module(name = "NativeCheckBoxRadioButtonBehavior") {
            importOnce(commonNativeModule, allowOverride = true)

            bind<NativeCheckBoxRadioButtonFactory>() with singleton { NativeCheckBoxRadioButtonFactoryImpl(instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }
        }

        val nativeThemeBehaviors = Module(name = "NativeThemeBehaviors") {
            importOnce(nativeButtonBehavior,      allowOverride = true)
            importOnce(nativeSliderBehavior,      allowOverride = true)
            importOnce(nativeCheckBoxBehavior,    allowOverride = true)
            importOnce(nativeTextFieldBehavior,   allowOverride = true)
            importOnce(nativeHyperLinkBehavior,   allowOverride = true)
            importOnce(nativeScrollPanelBehavior, allowOverride = true)
            importOnce(nativeRadioButtonBehavior, allowOverride = true)
        }

        val nativeButtonBehavior = Module(name = "NativeButtonBehavior") {
            importOnce(commonNativeModule, allowOverride = true)

            bind<NativeButtonFactory>() with singleton { NativeButtonFactoryImpl(instance(), instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }

            bindBehavior<Button>(NativeTheme::class) { it.behavior = NativeButtonBehavior(instance(), instance(), it) }
        }

        val nativeScrollPanelBehavior = Module(name = "NativeScrollPanelBehavior") {
            importOnce(commonNativeModule, allowOverride = true)

            bind<NativeScrollPanelFactory>() with singleton { NativeScrollPanelFactoryImpl(instance(), instance()) }

            bindBehavior<ScrollPanel>(NativeTheme::class) { it.behavior = NativeScrollPanelBehavior(instance(), it) }
        }

        val nativeSliderBehavior = Module(name = "NativeSliderBehavior") {
            importOnce(commonNativeModule, allowOverride = true)

            bind<NativeSliderFactory>() with singleton { NativeSliderFactoryImpl(instance(), instance(), instance(), instanceOrNull()) }

            bindBehavior<Slider>(NativeTheme::class) { it.behavior = NativeSliderBehavior(instance(), it) }
        }

        val nativeTextFieldBehavior = Module(name = "NativeTextFieldBehavior") {
            importOnce(commonNativeModule, allowOverride = true)

            bind<NativeTextFieldFactory>() with singleton { NativeTextFieldFactoryImpl(instance(), instance(), instance(), instanceOrNull(), instance()) }

            bindBehavior<TextField>(NativeTheme::class) { it.behavior = NativeTextFieldBehavior(instance(), it) }
        }

        val nativeHyperLinkBehavior = Module(name = "NativeHyperLinkBehavior") {
            importOnce(commonNativeModule, allowOverride = true)

            bind<NativeHyperLinkFactory>() with singleton { NativeHyperLinkFactoryImpl(instance(), instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }

            bindBehavior<HyperLink>(NativeTheme::class) { it.behavior = NativeHyperLinkBehavior(instance(), instance(), it) as Behavior<Button> }
        }

        val nativeCheckBoxBehavior = Module(name = "NativeCheckBoxBehavior") {
            importOnce(nativeCheckBoxRadioButtonBehavior, allowOverride = true)

            bindBehavior<CheckBox>(NativeTheme::class) { it.behavior = NativeCheckBoxBehavior(instance(), instance(), it) as Behavior<Button> }
        }

        val nativeRadioButtonBehavior = Module(name = "NativeRadioButtonBehavior") {
            importOnce(nativeCheckBoxRadioButtonBehavior, allowOverride = true)

            bindBehavior<RadioButton>(NativeTheme::class) { it.behavior = NativeRadioButtonBehavior(instance(), instance(), it) as Behavior<Button> }
        }
    }
}