package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.theme.Modules
import io.nacular.doodle.theme.Modules.Companion.ThemeModule
import io.nacular.doodle.theme.Modules.Companion.bindBehavior
import io.nacular.doodle.theme.adhoc.DynamicTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import org.kodein.di.DI.Module
import org.kodein.di.bindSingleton
import org.kodein.di.erasedSet
import org.kodein.di.instance
import org.kodein.di.instanceOrNull

private typealias NTheme = NativeTheme

public class NativeTheme(behaviors: Iterable<Modules.BehaviorResolver>): DynamicTheme(behaviors.filter { it.theme == NTheme::class }) {
    override fun toString(): String = this::class.simpleName ?: ""

    public companion object {
        public val NativeTheme: Module = Module(name = "NativeTheme") {
            importOnce(ThemeModule, allowOverride = true)

            bindSingleton { NativeTheme(Instance(erasedSet())) }
        }

        private val CommonNativeModule = Module(allowSilentOverride = true, name = "CommonNativeModule") {

        }

//        private val NativeCheckBoxRadioButtonBehavior = Module(name = "NativeCheckBoxRadioButtonBehavior") {
//            importOnce(CommonNativeModule, allowOverride = true)
//
//            bind<NativeCheckBoxRadioButtonFactory>() with singleton { NativeCheckBoxRadioButtonFactoryImpl(instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }
//        }

        public fun nativeButtonBehavior(): Module = Module(name = "NativeButtonBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

//            bindSingleton<NativeButtonFactory> { NativeButtonFactoryImpl(instance(), instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }

            bindBehavior<Button>(NTheme::class) { it.behavior = NativeButtonBehavior(instance(), Dispatchers.Swing, instance(), instance(), instanceOrNull(), it) }
        }

//        public fun nativeScrollPanelBehavior(smoothScrolling: Boolean = false): Module = Module(name = "NativeScrollPanelBehavior") {
//            importOnce(CommonNativeModule, allowOverride = true)
//
//            bind<NativeScrollPanelFactory>() with singleton { NativeScrollPanelFactoryImpl(smoothScrolling, instance(), instance(), instance()) }
//
//            bindBehavior<ScrollPanel>(NTheme::class) { it.behavior = NativeScrollPanelBehavior(instance(), it) }
//        }
//
//        public fun nativeSliderBehavior(): Module = Module(name = "NativeSliderBehavior") {
//            importOnce(CommonNativeModule, allowOverride = true)
//
//            bind<NativeSliderFactory>() with singleton { NativeSliderFactoryImpl(instance(), instance(), instance(), instanceOrNull()) }
//
//            bindBehavior<Slider>(NTheme::class) { it.behavior = NativeSliderBehavior(instance(), it) }
//        }
//
//        public fun nativeTextFieldBehavior(spellCheck: Boolean = false): Module = Module(name = "NativeTextFieldBehavior") {
//            importOnce(CommonNativeModule, allowOverride = true)
//
//            bind<NativeTextFieldFactory>() with singleton {
//                NativeTextFieldFactoryImpl(
//                    instance(),
//                    instance(),
//                    instance(),
//                    instance(),
//                    instance(),
//                    instance(),
//                    instanceOrNull(),
//                    instance(),
//                    spellCheck)
//            }
//
//            bindBehavior<TextField>(NTheme::class) { it.behavior = NativeTextFieldBehavior(instance(), it) }
//        }
//
//        public fun nativeHyperLinkBehavior(): Module = Module(name = "NativeHyperLinkBehavior") {
//            importOnce(CommonNativeModule, allowOverride = true)
//
//            bind<NativeHyperLinkFactory        >() with singleton { NativeHyperLinkFactoryImpl(instance(), instance(), instance(), instance(), instanceOrNull()) }
//            bind<NativeHyperLinkBehaviorBuilder>() with singleton { NativeHyperLinkBehaviorBuilderImpl(instance()) }
//
//            bindBehavior<HyperLink>(NTheme::class) { it.behavior = NativeHyperLinkBehavior(instance(), instance(), it) as Behavior<Button> }
//        }
//
//        public fun nativeCheckBoxBehavior(): Module = Module(name = "NativeCheckBoxBehavior") {
//            importOnce(NativeCheckBoxRadioButtonBehavior, allowOverride = true)
//
//            bindBehavior<CheckBox>(NTheme::class) { it.behavior = NativeCheckBoxBehavior(instance(), instance(), it) as Behavior<Button> }
//        }
//
//        public fun nativeRadioButtonBehavior(): Module = Module(name = "NativeRadioButtonBehavior") {
//            importOnce(NativeCheckBoxRadioButtonBehavior, allowOverride = true)
//
//            bindBehavior<RadioButton>(NTheme::class) { it.behavior = NativeRadioButtonBehavior(instance(), instance(), it) as Behavior<Button> }
//        }
//
//        public fun nativeSwitchBehavior(): Module = Module(name = "NativeSwitchBehavior") {
//            importOnce(NativeCheckBoxRadioButtonBehavior, allowOverride = true)
//
//            bindBehavior<Switch>(NTheme::class) { it.behavior = NativeCheckBoxBehavior(instance(), instance(), it) as Behavior<Button> }
//        }

        public val nativeThemeBehaviors: List<Module> = listOf(
            nativeButtonBehavior     (),
//            nativeSliderBehavior     (),
//            nativeSwitchBehavior     (),
//            nativeCheckBoxBehavior   (),
//            nativeTextFieldBehavior  (),
//            nativeHyperLinkBehavior  (),
//            nativeScrollPanelBehavior(),
//            nativeRadioButtonBehavior()
        )
    }
}