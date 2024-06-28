package io.nacular.doodle.application

import io.nacular.doodle.accessibility.AccessibilityManager
import io.nacular.doodle.accessibility.AccessibilityManagerImpl
import io.nacular.doodle.accessibility.AccessibilityManagerSkiko
import io.nacular.doodle.controls.PopupManager
import io.nacular.doodle.controls.PopupManagerImpl
import io.nacular.doodle.controls.modal.ModalManager
import io.nacular.doodle.controls.modal.ModalManagerImpl
import io.nacular.doodle.controls.popupmenu.MenuFactory
import io.nacular.doodle.controls.popupmenu.MenuFactoryImpl
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.Window
import io.nacular.doodle.core.WindowGroup
import io.nacular.doodle.core.WindowImpl
import io.nacular.doodle.datatransport.dragdrop.DragManager
import io.nacular.doodle.datatransport.dragdrop.impl.DragManagerImpl
import io.nacular.doodle.deviceinput.KeyboardFocusManager
import io.nacular.doodle.deviceinput.KeyboardFocusManagerImpl
import io.nacular.doodle.deviceinput.ViewFinder
import io.nacular.doodle.deviceinput.ViewFinderImpl
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.impl.FontLoaderImpl
import io.nacular.doodle.event.KeyCode
import io.nacular.doodle.event.KeyState
import io.nacular.doodle.event.KeyState.Type.Down
import io.nacular.doodle.event.KeyText.Companion.Tab
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.focus.FocusTraversalPolicy
import io.nacular.doodle.focus.impl.DefaultFocusabilityChecker
import io.nacular.doodle.focus.impl.FocusManagerImpl
import io.nacular.doodle.focus.impl.FocusTraversalPolicyImpl
import io.nacular.doodle.focus.impl.FocusabilityChecker
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.impl.PathMetricsImpl
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.image.impl.Base64Decoder
import io.nacular.doodle.image.impl.ImageLoaderImpl
import io.nacular.doodle.image.impl.UrlDecoder
import io.nacular.doodle.system.KeyInputService
import io.nacular.doodle.system.PointerInputService
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift
import io.nacular.doodle.system.impl.DesktopPointerInputManagers
import io.nacular.doodle.system.impl.KeyInputServiceImpl
import io.nacular.doodle.system.impl.PointerInputServiceImpl
import io.nacular.doodle.theme.native.NativePointerPreprocessor
import io.nacular.doodle.user.UserPreferences
import io.nacular.doodle.user.impl.UserPreferencesImpl
import io.nacular.doodle.utils.RelativePositionMonitor
import io.nacular.doodle.utils.RelativePositionMonitorImpl
import org.kodein.di.DI.Module
import org.kodein.di.bind
import org.kodein.di.bindFactory
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.singleton
import java.net.URLDecoder
import java.util.*

/**
 * Created by Nicholas Eddy on 5/20/21.
 */
public class Modules private constructor() {
    public companion object {
        /** Enables focus management by providing access to [FocusManager]. */
        public val FocusModule: Module = Module(allowSilentOverride = true, name = "Focus") {
            bindSingleton<FocusabilityChecker>  { DefaultFocusabilityChecker(                                  ) }
            bindSingleton<FocusTraversalPolicy> { FocusTraversalPolicyImpl  (instance()                        ) }
            bindSingleton<FocusManager>         { FocusManagerImpl          (instance(), instance(), instance()) }
        }

        /** Enables pointer use. */
        public val PointerModule: Module = Module(allowSilentOverride = true, name = "Pointer") {
            bindInstance { NativePointerPreprocessor() }

            bindInstance<ViewFinder>           { ViewFinderImpl                                                        }
            bindSingleton<PointerInputService> { PointerInputServiceImpl    (instance(), instance(), instanceOrNull()) }
            bindSingleton                      { DesktopPointerInputManagers(instance(), instance(), instance(), instance<NativePointerPreprocessor>())  }
        }

        /** Enables keyboard use. Includes [FocusModule]. */
        public val KeyboardModule: Module = Module(allowSilentOverride = true, name = "Keyboard") {
            importOnce(FocusModule)

            // TODO: Make this pluggable
            val keys = mapOf(
                FocusTraversalPolicy.TraversalType.Forward to setOf(KeyState (KeyCode.Tab, Tab, emptySet(     ), Down)),
                FocusTraversalPolicy.TraversalType.Backward to setOf(KeyState(KeyCode.Tab, Tab, setOf   (Shift), Down))
            )

            bindSingleton<KeyInputService>     { KeyInputServiceImpl     (java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager()) }
            bindSingleton<KeyboardFocusManager>{ KeyboardFocusManagerImpl(instance(), instance(), keys                                  ) }
        }

        /** Enable use of [FontLoader]. */
        public val FontModule: Module = Module(allowSilentOverride = true, name = "Font") {
            bind<FontLoader>() with singleton { FontLoaderImpl(instance()) }
        }

        /**
         * Enables drag-drop data transfer (not simple moving of Views) that allows data to be shared in and outside the
         * application. Includes [PointerModule].
         */
        public val DragDropModule: Module = Module(allowSilentOverride = true, name = "DragDrop") {
            importOnce(PointerModule)

            bindSingleton<DragManager> { DragManagerImpl(instance(), instance(), instance(), instance()) }
        }

        /** Enable use of [ImageLoader]. */
        public val ImageModule: Module = Module(allowSilentOverride = true, name = "Image") {
            bindSingleton<ImageLoader>{
                ImageLoaderImpl(
                    urlDecoder = object: UrlDecoder {
                        override fun decode(string: String, encoding: String) = URLDecoder.decode(string, encoding)
                    },
                    base64Decoder = object: Base64Decoder {
                        override fun decode(string: String) = Base64.getDecoder().decode(string)
                    }
                )
            }
        }

        /** Enable use of [PopupManager]. */
        public val PopupModule: Module = Module(allowSilentOverride = true, name = "Popup") {
            bindSingleton<RelativePositionMonitor> { RelativePositionMonitorImpl() }
            bindSingleton<PopupManager>            {
                factory<Window, PopupManager>()(instance<WindowGroup>().main)
            }

            bindFactory<Window, PopupManager> {
                PopupManagerImpl(it.display as InternalDisplay, (it as WindowImpl).renderManager, instance())
            }
        }

        /** Enable use of [ModalManager]; includes [PopupModule]. */
        public val ModalModule: Module = Module(allowSilentOverride = true, name = "Modal") {
            importOnce(PopupModule)

            bindSingleton<ModalManager>{
                factory<Window, ModalManager>()(instance<WindowGroup>().main)
            }

            bindFactory<Window, ModalManager> {
                ModalManagerImpl(factory<Window, PopupManager>()(it), instanceOrNull())
            }
        }

        /** Enable use of [PathMetrics]. */
        public val PathModule: Module = Module(allowSilentOverride = true, name = "Path") {
            bindSingleton<PathMetrics>{ PathMetricsImpl(instance()) }
        }

        /** Enable use of [UserPreferences]. */
        public val UserPreferencesModule: Module = Module(allowSilentOverride = true, name = "UserPreferences") {
            bindSingleton<UserPreferences>{ UserPreferencesImpl() }
        }

        /** Enable use of [MenuFactory]. */
        public val MenuFactoryModule: Module = Module(allowSilentOverride = true, name = "MenuFactory") {
            bindSingleton<MenuFactory>{
                factory<Window, MenuFactory>()(instance<WindowGroup>().main)
            }

            bindFactory<Window, MenuFactory> {
                MenuFactoryImpl(factory<Window, PopupManager>()(it), instance(), instanceOrNull())
            }
        }

        /** Enables accessibility features. */
        public val AccessibilityModule: Module = Module(allowSilentOverride = true, name = "Accessibility") {
            importOnce(KeyboardModule)

            // TODO: Can this be handled better?
            bindSingleton { instance<KeyInputService>() as KeyInputServiceImpl }

            bindSingleton<AccessibilityManager> {
                AccessibilityManagerImpl(instance(), instance())
            }

            // TODO: Can this be handled better?
            bindSingleton { instance<AccessibilityManager>() as AccessibilityManagerSkiko }
        }
    }
}