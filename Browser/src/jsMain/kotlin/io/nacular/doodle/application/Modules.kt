package io.nacular.doodle.application

import io.nacular.doodle.UrlView
import io.nacular.doodle.accessibility.AccessibilityManager
import io.nacular.doodle.accessibility.AccessibilityManagerImpl
import io.nacular.doodle.controls.document.Document
import io.nacular.doodle.datatransport.dragdrop.DragManager
import io.nacular.doodle.datatransport.dragdrop.impl.DragManagerImpl
import io.nacular.doodle.deviceinput.KeyboardFocusManager
import io.nacular.doodle.deviceinput.KeyboardFocusManagerImpl
import io.nacular.doodle.deviceinput.PointerInputManager
import io.nacular.doodle.deviceinput.PointerInputManagerImpl
import io.nacular.doodle.deviceinput.ViewFinder
import io.nacular.doodle.deviceinput.ViewFinderImpl
import io.nacular.doodle.document.impl.DocumentImpl
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.impl.FontLoaderLegacy
import io.nacular.doodle.drawing.impl.NativeEventHandlerFactory
import io.nacular.doodle.drawing.impl.NativeEventHandlerImpl
import io.nacular.doodle.drawing.impl.NativeEventListener
import io.nacular.doodle.event.KeyCode
import io.nacular.doodle.event.KeyState
import io.nacular.doodle.event.KeyState.Type.Down
import io.nacular.doodle.event.KeyText
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.focus.FocusTraversalPolicy
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Backward
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Forward
import io.nacular.doodle.focus.NativeFocusManager
import io.nacular.doodle.focus.NativeFocusManagerImpl
import io.nacular.doodle.focus.impl.DefaultFocusabilityChecker
import io.nacular.doodle.focus.impl.FocusManagerImpl
import io.nacular.doodle.focus.impl.FocusTraversalPolicyImpl
import io.nacular.doodle.focus.impl.FocusabilityChecker
import io.nacular.doodle.system.KeyInputService
import io.nacular.doodle.system.PointerInputService
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift
import io.nacular.doodle.system.impl.KeyInputServiceImpl
import io.nacular.doodle.system.impl.KeyInputServiceStrategy
import io.nacular.doodle.system.impl.KeyInputStrategyWebkit
import io.nacular.doodle.system.impl.PointerInputServiceImpl
import io.nacular.doodle.system.impl.PointerInputServiceStrategy
import io.nacular.doodle.system.impl.PointerInputServiceStrategyWebkit
import io.nacular.doodle.system.impl.PointerLocationResolver
import io.nacular.doodle.system.impl.PointerLocationResolverImpl
import kotlinx.browser.document
import org.kodein.di.DI.Module
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.provider
import org.kodein.di.singleton
import org.w3c.dom.HTMLElement

public class Modules {
    public companion object {
        /** Enables focus management by providing access to [FocusManager]. */
        public val FocusModule: Module = Module(allowSilentOverride = true, name = "Focus") {
            bind<FocusabilityChecker> () with singleton { DefaultFocusabilityChecker(                                            ) }
            bind<FocusTraversalPolicy>() with singleton { FocusTraversalPolicyImpl  (instance()                                  ) }
            bind<FocusManager>        () with singleton { FocusManagerImpl          (instance(), instance(), instance()          ) }
            bind<NativeFocusManager>  () with singleton { NativeFocusManagerImpl    (instance<FocusManager>() as FocusManagerImpl) }
        }

        /** Enables pointer use. */
        public val PointerModule: Module = Module(allowSilentOverride = true, name = "Pointer") {
            bind<ViewFinder>                 () with singleton { ViewFinderImpl                   (instance()                        ) }
            bind<PointerLocationResolver>    () with singleton { PointerLocationResolverImpl      (document,   instance()            ) }
            bind<PointerInputService>        () with singleton { PointerInputServiceImpl          (instance()                        ) }
            bind<PointerInputManager>        () with singleton { PointerInputManagerImpl          (instance(), instance(), instance()) }
            bind<PointerInputServiceStrategy>() with singleton { PointerInputServiceStrategyWebkit(document,   instance(), instance()) }
        }

        /** Enables keyboard use. Includes [FocusModule]. */
        public val KeyboardModule: Module = Module(allowSilentOverride = true, name = "Keyboard") {
            importOnce(FocusModule)

            // TODO: Make this pluggable
            val keys = mapOf(
                Forward  to setOf(KeyState(KeyCode.Tab, KeyText.Tab, emptySet(     ), Down)),
                Backward to setOf(KeyState(KeyCode.Tab, KeyText.Tab, setOf   (Shift), Down))
            )

            bind<KeyInputService>        () with singleton { KeyInputServiceImpl     (instance()                  ) }
            bind<KeyboardFocusManager>   () with singleton { KeyboardFocusManagerImpl(instance(), instance(), keys) }
            bind<KeyInputServiceStrategy>() with singleton { KeyInputStrategyWebkit  (instance()                  ) }
        }

        /**
         * Enables drag-drop data transfer (not simple moving of Views) that allows data to be shared in and outside the
         * application. Includes [PointerModule].
         */
        public val DragDropModule: Module = Module(allowSilentOverride = true, name = "DragDrop") {
            importOnce(PointerModule)

            bind<DragManager>() with singleton { DragManagerImpl(instance(), instance(), instance(), instance(), instance(), instance()) }
        }

        /** Enables use of [Document]s */
        public val DocumentModule: Module = Module(allowSilentOverride = true, name = "Document") {
            bind<Document>() with provider { DocumentImpl(instance(), instance(), instance(), instance()) }
        }

        /** Enables accessibility features */
        public val AccessibilityModule: Module = Module(allowSilentOverride = true, name = "Accessibility") {
            importOnce(KeyboardModule)

            // TODO: Can this be handled better?
            bind<KeyInputServiceImpl>() with singleton { instance<KeyInputService>() as KeyInputServiceImpl }

            // FIXME: Centralize
            bind<NativeEventHandlerFactory>() with singleton { { element: HTMLElement, listener: NativeEventListener -> NativeEventHandlerImpl(instanceOrNull(), element, listener) } }

            bind<AccessibilityManager>() with singleton { AccessibilityManagerImpl(instance(), instance(), instance(), instance(), instance(), instance()) }

            // TODO: Can this be handled better?
            bind<AccessibilityManagerImpl>() with singleton { instance<AccessibilityManager>() as AccessibilityManagerImpl }
        }

        /** Enables use of [UrlView]s */
        public val UrlViewModule: Module = Module(allowSilentOverride = true, name = "UrlView") {
            bind<UrlView>() with provider { UrlView(instance()) }
        }

        public val FontModule: Module = Module(allowSilentOverride = true, name = "Font") {
            bind<FontLoader>() with singleton { FontLoaderLegacy(instance(), instance(), instance(), instance()) }
        }
    }
}