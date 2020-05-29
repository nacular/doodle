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
import com.nectar.doodle.core.Behavior
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

private typealias NTheme = NativeTheme

class NativeTheme(behaviors: Iterable<BehaviorResolver>): AdhocTheme(behaviors.filter { it.theme == NTheme::class }) {
    override fun toString() = this::class.simpleName ?: ""

    companion object {
        val NativeTheme = Module(name = "NativeTheme") {
            bind<NativeTheme>() with singleton { NativeTheme(Instance(erasedSet())) }
        }

        private val CommonNativeModule = Module(allowSilentOverride = true, name = "CommonNativeModule") {

            // TODO: Can this be handled better?
            bind<RealGraphicsSurfaceFactory>() with singleton { instance<GraphicsSurfaceFactory<*>>() as RealGraphicsSurfaceFactory }

            bind<NativeEventHandlerFactory>() with singleton { { element: HTMLElement, listener: NativeEventListener -> NativeEventHandlerImpl(element, listener) } }
        }

        private val NativeCheckBoxRadioButtonBehavior = Module(name = "NativeCheckBoxRadioButtonBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeCheckBoxRadioButtonFactory>() with singleton { NativeCheckBoxRadioButtonFactoryImpl(instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }
        }

        val NativeThemeBehaviors = Module(name = "NativeThemeBehaviors") {
            importOnce(NativeButtonBehavior,      allowOverride = true)
            importOnce(NativeSliderBehavior,      allowOverride = true)
            importOnce(NativeCheckBoxBehavior,    allowOverride = true)
            importOnce(NativeTextFieldBehavior,   allowOverride = true)
            importOnce(NativeHyperLinkBehavior,   allowOverride = true)
            importOnce(NativeScrollPanelBehavior, allowOverride = true)
            importOnce(NativeRadioButtonBehavior, allowOverride = true)
        }

        val NativeButtonBehavior = Module(name = "NativeButtonBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeButtonFactory>() with singleton { NativeButtonFactoryImpl(instance(), instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }

            bindBehavior<Button>(NTheme::class) { it.behavior = NativeButtonBehavior(instance(), instance(), it) }
        }

        val NativeScrollPanelBehavior = Module(name = "NativeScrollPanelBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeScrollPanelFactory>() with singleton { NativeScrollPanelFactoryImpl(instance(), instance()) }

            bindBehavior<ScrollPanel>(NTheme::class) { it.behavior = NativeScrollPanelBehavior(instance(), it) }
        }

        val NativeSliderBehavior = Module(name = "NativeSliderBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeSliderFactory>() with singleton { NativeSliderFactoryImpl(instance(), instance(), instance(), instanceOrNull()) }

            bindBehavior<Slider>(NTheme::class) { it.behavior = NativeSliderBehavior(instance(), it) }
        }

        val NativeTextFieldBehavior = Module(name = "NativeTextFieldBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeTextFieldFactory>() with singleton { NativeTextFieldFactoryImpl(instance(), instance(), instance(), instanceOrNull(), instance()) }

            bindBehavior<TextField>(NTheme::class) { it.behavior = NativeTextFieldBehavior(instance(), it) }
        }

        val NativeHyperLinkBehavior = Module(name = "NativeHyperLinkBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeHyperLinkFactory>() with singleton { NativeHyperLinkFactoryImpl(instance(), instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }

            bindBehavior<HyperLink>(NTheme::class) { it.behavior = NativeHyperLinkBehavior(instance(), instance(), it) as Behavior<Button> }
        }

        val NativeCheckBoxBehavior = Module(name = "NativeCheckBoxBehavior") {
            importOnce(NativeCheckBoxRadioButtonBehavior, allowOverride = true)

            bindBehavior<CheckBox>(NTheme::class) { it.behavior = NativeCheckBoxBehavior(instance(), instance(), it) as Behavior<Button> }
        }

        val NativeRadioButtonBehavior = Module(name = "NativeRadioButtonBehavior") {
            importOnce(NativeCheckBoxRadioButtonBehavior, allowOverride = true)

            bindBehavior<RadioButton>(NTheme::class) { it.behavior = NativeRadioButtonBehavior(instance(), instance(), it) as Behavior<Button> }
        }
    }
}