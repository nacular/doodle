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
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.window
import io.nacular.doodle.drawing.impl.GraphicsSurfaceFactory
import io.nacular.doodle.drawing.impl.NativeButtonFactory
import io.nacular.doodle.drawing.impl.NativeButtonFactoryImpl
import io.nacular.doodle.drawing.impl.NativeCheckBoxRadioButtonFactory
import io.nacular.doodle.drawing.impl.NativeCheckBoxRadioButtonFactoryImpl
import io.nacular.doodle.drawing.impl.NativeEventHandler
import io.nacular.doodle.drawing.impl.NativeEventHandlerFactory
import io.nacular.doodle.drawing.impl.NativeEventHandlerImpl
import io.nacular.doodle.drawing.impl.NativeEventListener
import io.nacular.doodle.drawing.impl.NativeFileSelectorFactory
import io.nacular.doodle.drawing.impl.NativeFileSelectorFactoryImpl
import io.nacular.doodle.drawing.impl.NativeHyperLinkFactory
import io.nacular.doodle.drawing.impl.NativeHyperLinkFactoryImpl
import io.nacular.doodle.drawing.impl.NativeScrollPanelFactory
import io.nacular.doodle.drawing.impl.NativeScrollPanelFactoryImpl
import io.nacular.doodle.drawing.impl.NativeSliderFactory
import io.nacular.doodle.drawing.impl.NativeSliderFactoryImpl
import io.nacular.doodle.drawing.impl.NativeTextFieldFactory
import io.nacular.doodle.drawing.impl.NativeTextFieldFactoryImpl
import io.nacular.doodle.drawing.impl.RealGraphicsSurfaceFactory
import io.nacular.doodle.theme.Modules
import io.nacular.doodle.theme.Modules.Companion.bindBehavior
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.native.NativeTheme.Companion.NativeTheme
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindSingleton
import org.kodein.di.erasedSet
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.singleton

public class NativeTheme(behaviors: Iterable<Modules.BehaviorResolver>): DynamicTheme(behaviors.filter { it.theme == NTheme::class }) {
    override fun toString(): String = this::class.simpleName ?: ""

    @Suppress("MemberVisibilityCanBePrivate")
    public companion object {
        private val CommonNativeModule = DI.Module(allowSilentOverride = true, name = "CommonNativeModule") {

            // TODO: Can this be handled better?
            bindSingleton { instance<GraphicsSurfaceFactory<*>>() as RealGraphicsSurfaceFactory }

            bindSingleton<NativeEventHandlerFactory> {
                object: NativeEventHandlerFactory {
                    override fun invoke(element: HTMLElement, listener: NativeEventListener): NativeEventHandler {
                        return NativeEventHandlerImpl(
                            instanceOrNull(),
                            element,
                            listener
                        )
                    }
                }
            }
        }

        private val NativeCheckBoxRadioButtonBehavior = DI.Module(name = "NativeCheckBoxRadioButtonBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindSingleton<NativeCheckBoxRadioButtonFactory> {
                NativeCheckBoxRadioButtonFactoryImpl(
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instanceOrNull()
                )
            }
        }

        /**
         * Module for injecting the [NativeTheme].
         */
        public val NativeTheme: DI.Module = DI.Module(name = "NativeTheme") {
            importOnce(Modules.ThemeModule, allowOverride = true)

            bind<NativeTheme>() with singleton { NativeTheme(Instance(erasedSet())) }
        }

        /**
         * Module that provides [Behavior]s for [Button]s that are styled by the underlying platform to look as close to native as
         * possible.
         */
        public fun nativeButtonBehavior(): DI.Module = DI.Module(name = "NativeButtonBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindSingleton<NativeButtonFactory> {
                NativeButtonFactoryImpl(
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instanceOrNull()
                )
            }

            bindBehavior<Button>(NTheme::class) {
                it.behavior = NativeButtonBehavior(instance(), instance(), instanceOrNull(), it)
            }
        }

        /**
         * Module that provides [Behavior]s for [ScrollPanel]s that are styled by the underlying platform to look as close to native as
         * possible.
         */
        public fun nativeScrollPanelBehavior(smoothScrolling: Boolean = false, managedScrolling: Boolean = true): DI.Module =
            DI.Module(name = "NativeScrollPanelBehavior") {
                importOnce(CommonNativeModule, allowOverride = true)

                bindSingleton<NativeScrollPanelFactory> {
                    NativeScrollPanelFactoryImpl(
                        smoothScrolling,
                        instance(),
                        instance(),
                        instance(),
                        instance()
                    )
                }

                bindBehavior<ScrollPanel>(NTheme::class) {
                    it.behavior = NativeScrollPanelBehavior(instance(), it, managedScrolling)
                }
            }

        /**
         * Module that provides [Behavior]s for [Slider]s that are styled by the underlying platform to look as close to native as
         * possible.
         */
        public fun nativeSliderBehavior(): DI.Module = DI.Module(name = "NativeSliderBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindSingleton<NativeSliderFactory> {
                NativeSliderFactoryImpl(
                    instance(),
                    instance(),
                    instance(),
                    instanceOrNull()
                )
            }

            bindBehavior<Slider<Double>>(NTheme::class) { it.behavior = NativeSliderBehavior(instance(), it) }
        }

        /**
         * Module that provides [Behavior]s for [TextField]s that are styled by the underlying platform to look as close to native as
         * possible.
         */
        public fun nativeTextFieldBehavior(spellCheck: Boolean = false, autoComplete: Boolean = true): DI.Module =
            DI.Module(name = "NativeTextFieldBehavior") {
                importOnce(CommonNativeModule, allowOverride = true)

                bindSingleton<NativeTextFieldFactory> {
                    NativeTextFieldFactoryImpl(
                        instance(),
                        instance(),
                        instance(),
                        instance(),
                        instance(),
                        instance(),
                        instanceOrNull(),
                        instance(),
                        instanceOrNull(),
                        spellCheck,
                        autoComplete
                    )
                }
                bindSingleton<NativeTextFieldStyler> { NativeTextFieldStylerImpl(instance()) }

                bindBehavior<TextField>(NTheme::class) { it.behavior = NativeTextFieldBehavior(instance(), it) }
            }

        /**
         * Module that provides [Behavior]s for [HyperLink]s that are styled by the underlying platform to look as close to native as
         * possible.
         */
        public fun nativeHyperLinkBehavior(): DI.Module = DI.Module(name = "NativeHyperLinkBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindSingleton<NativeHyperLinkFactory> {
                NativeHyperLinkFactoryImpl(
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instanceOrNull(),
                    instanceOrNull(),
                    window.location.host
                )
            }
            bindSingleton<NativeHyperLinkStyler> { NativeHyperLinkStylerImpl(instance()) }

            bindBehavior<HyperLink>(NTheme::class) {
                @Suppress("UNCHECKED_CAST")
                it.behavior = NativeHyperLinkBehavior(instance(), instance(), instanceOrNull(), it) as Behavior<Button>
            }
        }

        /**
         * Module that provides [Behavior]s for [CheckBox]es that are styled by the underlying platform to look as close to native as
         * possible.
         */
        public fun nativeCheckBoxBehavior(): DI.Module = DI.Module(name = "NativeCheckBoxBehavior") {
            importOnce(NativeCheckBoxRadioButtonBehavior, allowOverride = true)

            bindBehavior<CheckBox>(NTheme::class) {
                @Suppress("UNCHECKED_CAST")
                it.behavior = NativeCheckBoxBehavior(instance(), instance(), instanceOrNull(), it) as Behavior<Button>
            }
        }

        /**
         * Module that provides [Behavior]s for [RadioButton]s that are styled by the underlying platform to look as close to native as
         * possible.
         */
        public fun nativeRadioButtonBehavior(): DI.Module = DI.Module(name = "NativeRadioButtonBehavior") {
            importOnce(NativeCheckBoxRadioButtonBehavior, allowOverride = true)

            bindBehavior<RadioButton>(NTheme::class) {
                @Suppress("UNCHECKED_CAST")
                it.behavior = NativeRadioButtonBehavior(instance(), instance(), instanceOrNull(), it) as Behavior<Button>
            }
        }

        /**
         * Module that provides [Behavior]s for [Switch]es that are styled by the underlying platform to look as close to native as
         * possible.
         */
        public fun nativeSwitchBehavior(): DI.Module = DI.Module(name = "NativeSwitchBehavior") {
            importOnce(NativeCheckBoxRadioButtonBehavior, allowOverride = true)

            bindBehavior<Switch>(NTheme::class) {
                @Suppress("UNCHECKED_CAST")
                it.behavior = NativeCheckBoxBehavior(instance(), instance(), instanceOrNull(), it) as Behavior<Button>
            }
        }

        /**
         * Module that provides [Behavior]s for [FileSelector]s that are styled by the underlying platform to look as close to native as
         * possible.
         */
        public fun nativeFileSelectorBehavior(): DI.Module = DI.Module(name = "NativeFileSelectorBehavior") {
            importOnce(CommonNativeModule, allowOverride = true)

            bindSingleton<NativeFileSelectorFactory> {
                NativeFileSelectorFactoryImpl(
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instanceOrNull()
                )
            }
            bindSingleton<NativeFileSelectorStyler> { NativeFileSelectorStylerImpl(instance()) }

            bindBehavior<FileSelector>(NTheme::class) { it.behavior = NativeFileSelectorBehavior(instance(), it) }
        }

        /**
         * @return list of common modules for native styles.
         */
        public val nativeThemeBehaviors: List<DI.Module> = listOf(
            nativeButtonBehavior     (),
            nativeSliderBehavior     (),
            nativeSwitchBehavior     (),
            nativeCheckBoxBehavior   (),
            nativeTextFieldBehavior  (),
            nativeHyperLinkBehavior  (),
            nativeScrollPanelBehavior(),
            nativeRadioButtonBehavior(),
            nativeFileSelectorBehavior(),
        )
    }
}

private typealias NTheme = NativeTheme