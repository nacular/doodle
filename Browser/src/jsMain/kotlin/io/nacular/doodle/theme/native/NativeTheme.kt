package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.controls.buttons.RadioButton
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.drawing.impl.GraphicsSurfaceFactory
import io.nacular.doodle.drawing.impl.NativeButtonFactory
import io.nacular.doodle.drawing.impl.NativeButtonFactoryImpl
import io.nacular.doodle.drawing.impl.NativeCheckBoxRadioButtonFactory
import io.nacular.doodle.drawing.impl.NativeCheckBoxRadioButtonFactoryImpl
import io.nacular.doodle.drawing.impl.NativeEventHandlerFactory
import io.nacular.doodle.drawing.impl.NativeEventHandlerImpl
import io.nacular.doodle.drawing.impl.NativeEventListener
import io.nacular.doodle.drawing.impl.NativeHyperLinkFactory
import io.nacular.doodle.drawing.impl.NativeHyperLinkFactoryImpl
import io.nacular.doodle.drawing.impl.NativeScrollPanelFactory
import io.nacular.doodle.drawing.impl.NativeScrollPanelFactoryImpl
import io.nacular.doodle.drawing.impl.NativeSliderFactory
import io.nacular.doodle.drawing.impl.NativeSliderFactoryImpl
import io.nacular.doodle.drawing.impl.NativeTextFieldFactory
import io.nacular.doodle.drawing.impl.NativeTextFieldFactoryImpl
import io.nacular.doodle.drawing.impl.RealGraphicsSurfaceFactory
import io.nacular.doodle.theme.Modules.BehaviorResolver
import io.nacular.doodle.theme.Modules.Companion.ThemeModule
import io.nacular.doodle.theme.Modules.Companion.bindBehavior
import io.nacular.doodle.theme.adhoc.DynamicTheme
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

class NativeTheme(behaviors: Iterable<BehaviorResolver>): DynamicTheme(behaviors.filter { it.theme == NTheme::class }) {
    override fun toString() = this::class.simpleName ?: ""

    companion object {
        val NativeTheme = Module(name = "NativeTheme") {
            importOnce(ThemeModule, allowOverride = true)

            bind<NativeTheme>() with singleton { NativeTheme(Instance(erasedSet())) }
        }

        private val CommonNativeModule = Module(allowSilentOverride = true, name = "CommonNativeModule") {

            // TODO: Can this be handled better?
            bind<RealGraphicsSurfaceFactory>() with singleton { instance<GraphicsSurfaceFactory<*>>() as RealGraphicsSurfaceFactory }

            bind<NativeEventHandlerFactory>() with singleton { { element: HTMLElement, listener: NativeEventListener -> NativeEventHandlerImpl(element, listener) } }
        }

        private val NativeCheckBoxRadioButtonBehavior = Module(name = "NativeCheckBoxRadioButtonBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeCheckBoxRadioButtonFactory>() with singleton { NativeCheckBoxRadioButtonFactoryImpl(instance(), instance(), instance(), instance(), instance(), instanceOrNull(), instanceOrNull()) }
        }

        val NativeButtonBehavior = Module(name = "NativeButtonBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeButtonFactory>() with singleton { NativeButtonFactoryImpl(instance(), instance(), instance(), instance(), instance(), instance(), instanceOrNull(), instanceOrNull()) }

            bindBehavior<Button>(NTheme::class) { it.behavior = NativeButtonBehavior(instance(), instance(), it) }
        }

        val NativeScrollPanelBehavior = Module(name = "NativeScrollPanelBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeScrollPanelFactory>() with singleton { NativeScrollPanelFactoryImpl(instance(), instance()) }

            bindBehavior<ScrollPanel>(NTheme::class) { it.behavior = NativeScrollPanelBehavior(instance(), it) }
        }

        val NativeSliderBehavior = Module(name = "NativeSliderBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeSliderFactory>() with singleton { NativeSliderFactoryImpl(instance(), instance(), instance(), instanceOrNull(), instanceOrNull()) }

            bindBehavior<Slider>(NTheme::class) { it.behavior = NativeSliderBehavior(instance(), it) }
        }

        val NativeTextFieldBehavior = Module(name = "NativeTextFieldBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeTextFieldFactory>() with singleton { NativeTextFieldFactoryImpl(instance(), instance(), instance(), instanceOrNull(), instanceOrNull(), instance()) }

            bindBehavior<TextField>(NTheme::class) { it.behavior = NativeTextFieldBehavior(instance(), it) }
        }

        val NativeHyperLinkBehavior = Module(name = "NativeHyperLinkBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeHyperLinkFactory>() with singleton { NativeHyperLinkFactoryImpl(instance(), instance(), instance(), instance(), instance(), instance(), instanceOrNull(), instanceOrNull()) }

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

        val nativeThemeBehaviors = listOf(
                NativeButtonBehavior,
                NativeSliderBehavior,
                NativeCheckBoxBehavior,
                NativeTextFieldBehavior,
                NativeHyperLinkBehavior,
                NativeScrollPanelBehavior,
                NativeRadioButtonBehavior
        )
    }
}