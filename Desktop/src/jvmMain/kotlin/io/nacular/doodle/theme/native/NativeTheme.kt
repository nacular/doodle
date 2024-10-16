package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.controls.buttons.RadioButton
import io.nacular.doodle.controls.buttons.Switch
import io.nacular.doodle.controls.files.FileSelector
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.core.WindowGroupImpl
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.impl.RealGraphicsSurface
import io.nacular.doodle.system.impl.NativeScrollHandlerFinder
import io.nacular.doodle.theme.Modules.BehaviorResolver
import io.nacular.doodle.theme.Modules.Companion.ThemeModule
import io.nacular.doodle.theme.Modules.Companion.bindBehavior
import io.nacular.doodle.theme.Scene
import io.nacular.doodle.theme.adhoc.DynamicTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.FontMgr
import org.kodein.di.DI.Module
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.erasedSet
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import java.awt.GraphicsEnvironment
import javax.swing.FocusManager
import javax.swing.JPanel

private typealias NTheme = NativeTheme

/**
 * Theme incorporates [Behavior]s that style Views using the underlying platform so they look as close to native as
 * possible.
 */
public class NativeTheme internal constructor(behaviors: Iterable<BehaviorResolver>): DynamicTheme(behaviors.filter { it.theme == NTheme::class }) {
    private lateinit var scene: Scene

    override fun toString(): String = this::class.simpleName ?: ""

    override fun install(scene: Scene) {
        super.install(scene)
        this.scene = scene
    }

    internal class WindowDiscovery(private val windowGroup: WindowGroupImpl) {
        fun frameFor (view: View): JPanel?                              = windowGroup.owner(view)?.skiaLayer
        fun deviceFor(view: View): GraphicsDevice<RealGraphicsSurface>? = windowGroup.owner(view)?.graphicsDevice
    }

    @Suppress("MemberVisibilityCanBePrivate")
    public companion object {
        private val CommonNativeModule = Module(allowSilentOverride = true, name = "CommonNativeModule") {
            bindInstance { GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration }
            bindSingleton {
                object: SwingGraphicsFactory {
                    override fun invoke(fontManager: FontMgr, skiaCanvas: Canvas): SkiaGraphics2D {
                        return SkiaGraphics2D(
                            canvas         = skiaCanvas,
                            defaultFont    = instance(),
                            textMetrics    = instance(),
                            fontManager    = fontManager,
                            fontCollection = instance()
                        )
                    }
                }
            }

            bindSingleton { WindowDiscovery(instance()) }
        }

        /**
         * Module for injecting the [NativeTheme].
         */
        public val NativeTheme: Module = Module(name = "NativeTheme") {
            importOnce(ThemeModule, allowOverride = true)

            bindSingleton { NativeTheme(Instance(erasedSet<BehaviorResolver>()).toList()) }
        }

        /**
         * Module that provides [Behavior]s for [Button]s that are styled by the underlying platform to look as close to native as
         * possible.
         */
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
                nativePointerPreprocessor = instanceOrNull(),
                fontManager               = instance(),
            ) }
        }

        /**
         * Module that provides [Behavior]s for [ScrollPanel]s that are styled by the underlying platform to look as close to native as
         * possible.
         */
        public fun nativeScrollPanelBehavior(): Module = Module(name = "NativeScrollPanelBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindSingleton { NativeScrollHandlerFinder() }

            bindBehavior<ScrollPanel>(NTheme::class) {
                it.behavior = NativeScrollPanelBehavior(
                    window                    = instance(),
                    appScope                  = instance(),
                    uiDispatcher              = Dispatchers.Swing,
                    swingGraphicsFactory      = instance(),
                    nativeScrollHandlerFinder = instanceOrNull(),
                    nativePointerPreprocessor = instanceOrNull(),
                    fontManager               = instance(),
                )
            }
        }

        /**
         * Module that provides [Behavior]s for [Slider]s that are styled by the underlying platform to look as close to native as
         * possible.
         */
        public fun nativeSliderBehavior(): Module = Module(name = "NativeSliderBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindBehavior<Slider<Double>>(NTheme::class) { it.behavior = NativeSliderBehavior(
                window                    = instance(),
                appScope                  = instance(),
                fontManager              = instance(),
                uiDispatcher              = Dispatchers.Swing,
                focusManager              = instanceOrNull(),
                swingGraphicsFactory      = instance(),
                nativePointerPreprocessor = instanceOrNull(),
            ) }
        }

        /**
         * Module that provides [Behavior]s for [TextField]s that are styled by the underlying platform to look as close to native as
         * possible.
         */
        public fun nativeTextFieldBehavior(): Module = Module(name = "NativeTextFieldBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindBehavior<TextField>(NTheme::class) { it.behavior = NativeTextFieldBehavior(
                window               = instance(),
                appScope             = instance(),
                defaultFont          = instance(),
                uiDispatcher         = Dispatchers.Swing,
                focusManager         = instanceOrNull(),
                swingFocusManager    = FocusManager.getCurrentManager(),
                swingGraphicsFactory = instance(),
                textMetrics          = instance(),
                fontManager          = instance(),
            ) }

            bindSingleton<NativeTextFieldStyler> { NativeTextFieldStylerImpl(
                window               = instance(),
                appScope             = instance(),
                defaultFont          = instance(),
                uiDispatcher         = Dispatchers.Swing,
                focusManager         = instanceOrNull(),
                swingFocusManager    = FocusManager.getCurrentManager(),
                swingGraphicsFactory = instance(),
                textMetrics          = instance(),
                fontManager          = instance(),
            ) }
        }

        /**
         * Module that provides [Behavior]s for [HyperLink]s that are styled by the underlying platform to look as close to native as
         * possible.
         */
        public fun nativeHyperLinkBehavior(): Module = Module(name = "NativeHyperLinkBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindSingleton<NativeHyperLinkStyler> { NativeHyperLinkStylerImpl() }

            bindBehavior<HyperLink>(NTheme::class) {
                @Suppress("UNCHECKED_CAST")
                it.behavior = NativeHyperLinkBehavior(
                    window                    = instance(),
                    appScope                  = instance(),
                    textMetrics               = instance(),
                    uiDispatcher              = Dispatchers.Swing,
                    focusManager              = instanceOrNull(),
                    swingFocusManager         = FocusManager.getCurrentManager(),
                    swingGraphicsFactory      = instance(),
                    nativePointerPreprocessor = instanceOrNull(),
                    fontManager               = instance(),
                ) as Behavior<Button>
            }
        }

        /**
         * Module that provides [Behavior]s for [CheckBox]es that are styled by the underlying platform to look as close to native as
         * possible.
         */
        public fun nativeCheckBoxBehavior(): Module = Module(name = "NativeCheckBoxBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindBehavior<CheckBox>(NTheme::class) {
                @Suppress("UNCHECKED_CAST")
                it.behavior = NativeCheckBoxBehavior(
                    window                    = instance(),
                    appScope                  = instance(),
                    textMetrics               = instance(),
                    uiDispatcher              = Dispatchers.Swing,
                    focusManager              = instanceOrNull(),
                    swingFocusManager         = FocusManager.getCurrentManager(),
                    swingGraphicsFactory      = instance(),
                    nativePointerPreprocessor = instanceOrNull(),
                    fontManager               = instance(),
                ) as Behavior<Button>
            }
        }

        /**
         * Module that provides [Behavior]s for [RadioButton]s that are styled by the underlying platform to look as close to native as
         * possible.
         */
        public fun nativeRadioButtonBehavior(): Module = Module(name = "NativeRadioButtonBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindBehavior<RadioButton>(NTheme::class) {
                @Suppress("UNCHECKED_CAST")
                it.behavior = NativeRadioButtonBehavior(
                    window                    = instance(),
                    appScope                  = instance(),
                    textMetrics               = instance(),
                    uiDispatcher              = Dispatchers.Swing,
                    focusManager              = instanceOrNull(),
                    swingFocusManager         = FocusManager.getCurrentManager(),
                    swingGraphicsFactory      = instance(),
                    nativePointerPreprocessor = instanceOrNull(),
                    fontManager               = instance(),
                ) as Behavior<Button>
            }
        }

        /**
         * Module that provides [Behavior]s for [Switch]es that are styled by the underlying platform to look as close to native as
         * possible.
         */
        public fun nativeSwitchBehavior(): Module = Module(name = "NativeSwitchBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindBehavior<Switch>(NTheme::class) {
                @Suppress("UNCHECKED_CAST")
                it.behavior = NativeCheckBoxBehavior(
                    window                    = instance(),
                    appScope                  = instance(),
                    textMetrics               = instance(),
                    uiDispatcher              = Dispatchers.Swing,
                    focusManager              = instanceOrNull(),
                    swingFocusManager         = FocusManager.getCurrentManager(),
                    swingGraphicsFactory      = instance(),
                    nativePointerPreprocessor = instanceOrNull(),
                    fontManager               = instance(),
                ) as Behavior<Button>
            }
        }

        /**
         * Module that provides [Behavior]s for [FileSelector]s that are styled by the underlying platform to look as close to native as
         * possible.
         */
        public fun nativeFileSelectorBehavior(prompt: String): Module = Module(name = "NativeFileSelectorBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindSingleton<NativeFileSelectorStyler> { NativeFileSelectorStylerImpl(window = instance(), fontManager = instance()) }

            bindBehavior<FileSelector>(NTheme::class) {
                it.behavior = NativeFileSelectorBehavior(
                    window                    = instance(),
                    appScope                  = instance(),
                    uiDispatcher              = Dispatchers.Swing,
                    focusManager              = instanceOrNull(),
                    swingGraphicsFactory      = instance(),
                    nativePointerPreprocessor = instanceOrNull(),
                    prompt                    = prompt,
                    fontManager               = instance(),
                )
            }
        }

        /**
         * @return list of common modules for native styles.
         */
        public fun nativeThemeBehaviors(fileSelectorPrompt: String): List<Module> = listOf(
            nativeButtonBehavior      (),
            nativeSliderBehavior      (),
            nativeSwitchBehavior      (),
            nativeCheckBoxBehavior    (),
            nativeTextFieldBehavior   (),
            nativeHyperLinkBehavior   (),
            nativeScrollPanelBehavior (),
            nativeRadioButtonBehavior (),
            nativeFileSelectorBehavior(fileSelectorPrompt),
        )
    }
}