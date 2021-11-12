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
import io.nacular.doodle.system.impl.NativeScrollHandlerFinder
import io.nacular.doodle.theme.Modules
import io.nacular.doodle.theme.Modules.Companion.ThemeModule
import io.nacular.doodle.theme.Modules.Companion.bindBehavior
import io.nacular.doodle.theme.adhoc.DynamicTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skia.Canvas
import org.kodein.di.DI.Module
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.erasedSet
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import java.awt.GraphicsEnvironment
import javax.swing.FocusManager

private typealias NTheme = NativeTheme

public class NativeTheme(behaviors: Iterable<Modules.BehaviorResolver>): DynamicTheme(behaviors.filter { it.theme == NTheme::class }) {
    override fun toString(): String = this::class.simpleName ?: ""

    public companion object {
        public val NativeTheme: Module = Module(name = "NativeTheme") {
            importOnce(ThemeModule, allowOverride = true)

            bindSingleton { NativeTheme(Instance(erasedSet())) }
        }

        private val CommonNativeModule = Module(allowSilentOverride = true, name = "CommonNativeModule") {
            bindInstance { GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration }
            bindSingleton {
                object: SwingGraphicsFactory {
                    override fun invoke(skiaCanvas: Canvas): SkiaGraphics2D {
                        return SkiaGraphics2D(
                                canvas         = skiaCanvas,
                                defaultFont    = instance(),
                                textMetrics    = instance(),
                                fontCollection = instance()
                        )
                    }
                }
            }
        }

//        private val NativeCheckBoxRadioButtonBehavior = Module(name = "NativeCheckBoxRadioButtonBehavior") {
//            importOnce(CommonNativeModule, allowOverride = true)
//
//            bind<NativeCheckBoxRadioButtonFactory>() with singleton { NativeCheckBoxRadioButtonFactoryImpl(instance(), instance(), instance(), instance(), instance(), instanceOrNull()) }
//        }

        public fun nativeButtonBehavior(): Module = Module(name = "NativeButtonBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindBehavior<Button>(NTheme::class) { it.behavior = NativeButtonBehavior(
                    window                    = instance(),
                    appScope                  = instance(),
                    textMetrics               = instance(),
                    uiDispatcher              = Dispatchers.Swing,
                    focusManager              = instanceOrNull(),
                    swingFocusManager         = FocusManager.getCurrentManager(),
                    swingGraphicsFactory      = instance(),
                    nativePointerPreprocessor = instanceOrNull()
            ) }
        }

        public fun nativeScrollPanelBehavior(): Module = Module(name = "NativeScrollPanelBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindSingleton { NativeScrollHandlerFinder() }

            bindBehavior<ScrollPanel>(NTheme::class) {
                it.behavior = NativeScrollPanelBehavior(
                        window                    = instance(),
                        appScope                  = instance(),
                        uiDispatcher              = Dispatchers.Swing,
                        graphicsDevice            = instance(),
                        swingGraphicsFactory      = instance(),
                        nativeScrollHandlerFinder = instanceOrNull(),
                        nativePointerPreprocessor = instanceOrNull()) }
        }

        public fun nativeSliderBehavior(): Module = Module(name = "NativeSliderBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindBehavior<Slider>(NTheme::class) { it.behavior = NativeSliderBehavior(
                    window                    = instance(),
                    appScope                  = instance(),
                    uiDispatcher              = Dispatchers.Swing,
                    focusManager              = instanceOrNull(),
                    swingGraphicsFactory      = instance(),
                    nativePointerPreprocessor = instanceOrNull()
            ) }
        }

        public fun nativeTextFieldBehavior(spellCheck: Boolean = false): Module = Module(name = "NativeTextFieldBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindBehavior<TextField>(NTheme::class) { it.behavior = NativeTextFieldBehavior(
                    window               = instance(),
                    appScope             = instance(),
                    defaultFont          = instance(),
                    uiDispatcher         = Dispatchers.Swing,
                    focusManager         = instanceOrNull(),
                    swingFocusManager    = FocusManager.getCurrentManager(),
                    swingGraphicsFactory = instance(),
                    textMetrics          = instance()
            ) }

            bindSingleton<NativeTextFieldStyler> { NativeTextFieldStylerImpl(
                    window               = instance(),
                    appScope             = instance(),
                    defaultFont          = instance(),
                    uiDispatcher         = Dispatchers.Swing,
                    focusManager         = instanceOrNull(),
                    swingFocusManager    = FocusManager.getCurrentManager(),
                    swingGraphicsFactory = instance(),
                    textMetrics          = instance()
            ) }
        }

        public fun nativeHyperLinkBehavior(): Module = Module(name = "NativeHyperLinkBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindSingleton<NativeHyperLinkStyler> { NativeHyperLinkStylerImpl() }

            bindBehavior<HyperLink>(NTheme::class) { it.behavior = NativeHyperLinkBehavior(
                    window                    = instance(),
                    appScope                  = instance(),
                    textMetrics               = instance(),
                    uiDispatcher              = Dispatchers.Swing,
                    focusManager              = instanceOrNull(),
                    swingFocusManager         = FocusManager.getCurrentManager(),
                    swingGraphicsFactory      = instance(),
                    nativePointerPreprocessor = instanceOrNull()
            ) as Behavior<Button> }
        }

        public fun nativeCheckBoxBehavior(): Module = Module(name = "NativeCheckBoxBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindBehavior<CheckBox>(NTheme::class) { it.behavior = NativeCheckBoxBehavior(
                    window                    = instance(),
                    appScope                  = instance(),
                    textMetrics               = instance(),
                    uiDispatcher              = Dispatchers.Swing,
                    focusManager              = instanceOrNull(),
                    swingFocusManager         = FocusManager.getCurrentManager(),
                    swingGraphicsFactory      = instance(),
                    nativePointerPreprocessor = instanceOrNull()
            ) as Behavior<Button> }
        }

        public fun nativeRadioButtonBehavior(): Module = Module(name = "NativeRadioButtonBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindBehavior<RadioButton>(NTheme::class) { it.behavior = NativeRadioButtonBehavior(
                    window                    = instance(),
                    appScope                  = instance(),
                    textMetrics               = instance(),
                    uiDispatcher              = Dispatchers.Swing,
                    focusManager              = instanceOrNull(),
                    swingFocusManager         = FocusManager.getCurrentManager(),
                    swingGraphicsFactory      = instance(),
                    nativePointerPreprocessor = instanceOrNull()
            ) as Behavior<Button> }
        }

        public fun nativeSwitchBehavior(): Module = Module(name = "NativeSwitchBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindBehavior<Switch>(NTheme::class) { it.behavior = NativeCheckBoxBehavior(
                    window                    = instance(),
                    appScope                  = instance(),
                    textMetrics               = instance(),
                    uiDispatcher              = Dispatchers.Swing,
                    focusManager              = instanceOrNull(),
                    swingFocusManager         = FocusManager.getCurrentManager(),
                    swingGraphicsFactory      = instance(),
                    nativePointerPreprocessor = instanceOrNull()
            ) as Behavior<Button> }
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