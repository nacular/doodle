package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.controls.buttons.RadioButton
import io.nacular.doodle.controls.buttons.Switch
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
import org.kodein.di.DI.Module
import org.kodein.di.bind
import org.kodein.di.erasedSet
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.singleton
import org.w3c.dom.HTMLElement


/**
 * Created by Nicholas Eddy on 1/28/18.
 */

private typealias NTheme = NativeTheme

public class NativeTheme(behaviors: Iterable<BehaviorResolver>): DynamicTheme(behaviors.filter { it.theme == NTheme::class }) {
    override fun toString(): String = this::class.simpleName ?: ""

    public companion object {
        public val NativeTheme: Module = Module(name = "NativeTheme") {
            importOnce(ThemeModule, allowOverride = true)

            bind<NativeTheme>() with singleton { NativeTheme(Instance(erasedSet())) }
        }

        private val CommonNativeModule = Module(allowSilentOverride = true, name = "CommonNativeModule") {

            // TODO: Can this be handled better?
            bind<RealGraphicsSurfaceFactory>() with singleton { instance<GraphicsSurfaceFactory<*>>() as RealGraphicsSurfaceFactory }

            bind<NativeEventHandlerFactory>() with singleton { { element: HTMLElement, listener: NativeEventListener -> NativeEventHandlerImpl(instanceOrNull(), element, listener) } }
        }

        private val NativeCheckBoxRadioButtonBehavior = Module(name = "NativeCheckBoxRadioButtonBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeCheckBoxRadioButtonFactory>() with singleton { NativeCheckBoxRadioButtonFactoryImpl(instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }
        }

        public fun nativeButtonBehavior(): Module = Module(name = "NativeButtonBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeButtonFactory>() with singleton { NativeButtonFactoryImpl(instance(), instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }

            bindBehavior<Button>(NTheme::class) { it.behavior = NativeButtonBehavior(instance(), instance(), it) }
        }

        public fun nativeScrollPanelBehavior(smoothScrolling: Boolean = false): Module = Module(name = "NativeScrollPanelBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeScrollPanelFactory>() with singleton { NativeScrollPanelFactoryImpl(smoothScrolling, instance(), instance(), instance()) }

            bindBehavior<ScrollPanel>(NTheme::class) { it.behavior = NativeScrollPanelBehavior(instance(), it) }
        }

        public fun nativeSliderBehavior(): Module = Module(name = "NativeSliderBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeSliderFactory>() with singleton { NativeSliderFactoryImpl(instance(), instance(), instance(), instanceOrNull()) }

            bindBehavior<Slider>(NTheme::class) { it.behavior = NativeSliderBehavior(instance(), it) }
        }

        public fun nativeTextFieldBehavior(spellCheck: Boolean = false): Module = Module(name = "NativeTextFieldBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeTextFieldFactory>() with singleton { NativeTextFieldFactoryImpl(instance(), instance(), instance(), instance(), instance(), instance(), instanceOrNull(), instance(), spellCheck) }

            bindBehavior<TextField>(NTheme::class) { it.behavior = NativeTextFieldBehavior(instance(), it) }
        }

        public fun nativeHyperLinkBehavior(): Module = Module(name = "NativeHyperLinkBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bind<NativeHyperLinkFactory        >() with singleton { NativeHyperLinkFactoryImpl(instance(), instance(), instance(), instance(), instanceOrNull()) }
            bind<NativeHyperLinkBehaviorBuilder>() with singleton { NativeHyperLinkBehaviorBuilderImpl(instance()) }

            bindBehavior<HyperLink>(NTheme::class) { it.behavior = NativeHyperLinkBehavior(instance(), instance(), it) as Behavior<Button> }
        }

        public fun nativeCheckBoxBehavior(): Module = Module(name = "NativeCheckBoxBehavior") {
            importOnce(NativeCheckBoxRadioButtonBehavior, allowOverride = true)

            bindBehavior<CheckBox>(NTheme::class) { it.behavior = NativeCheckBoxBehavior(instance(), instance(), it) as Behavior<Button> }
        }

        public fun nativeRadioButtonBehavior(): Module = Module(name = "NativeRadioButtonBehavior") {
            importOnce(NativeCheckBoxRadioButtonBehavior, allowOverride = true)

            bindBehavior<RadioButton>(NTheme::class) { it.behavior = NativeRadioButtonBehavior(instance(), instance(), it) as Behavior<Button> }
        }

        public fun nativeSwitchBehavior(): Module = Module(name = "NativeSwitchBehavior") {
            importOnce(NativeCheckBoxRadioButtonBehavior, allowOverride = true)

            bindBehavior<Switch>(NTheme::class) { it.behavior = NativeCheckBoxBehavior(instance(), instance(), it) as Behavior<Button> }
        }

        public val nativeThemeBehaviors: List<Module> = listOf(
            nativeButtonBehavior     (),
            nativeSliderBehavior     (),
            nativeSwitchBehavior     (),
            nativeCheckBoxBehavior   (),
            nativeTextFieldBehavior  (),
            nativeHyperLinkBehavior  (),
            nativeScrollPanelBehavior(),
            nativeRadioButtonBehavior()
        )
    }
}