package io.nacular.doodle.application

import io.nacular.doodle.UrlView
import io.nacular.doodle.accessibility.AccessibilityManager
import io.nacular.doodle.accessibility.AccessibilityManagerImpl
import io.nacular.doodle.controls.PopupManager
import io.nacular.doodle.controls.PopupManagerImpl
import io.nacular.doodle.controls.document.Document
import io.nacular.doodle.controls.modal.ModalManager
import io.nacular.doodle.controls.modal.ModalManagerImpl
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
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.image.impl.ImageLoaderImpl
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
import io.nacular.doodle.utils.RelativePositionMonitor
import io.nacular.doodle.utils.RelativePositionMonitorImpl
import kotlinx.browser.document
import org.kodein.di.DI.Module
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.w3c.dom.HTMLElement

public class Modules {
    public companion object {
        /** Enables focus management by providing access to [FocusManager]. */
        public val FocusModule: Module = Module(allowSilentOverride = true, name = "Focus") {
            bindSingleton<FocusabilityChecker>  { DefaultFocusabilityChecker(                                            ) }
            bindSingleton<FocusTraversalPolicy> { FocusTraversalPolicyImpl  (instance()                                  ) }
            bindSingleton<FocusManager>         { FocusManagerImpl          (instance(), instance(), instance()          ) }
            bindSingleton<NativeFocusManager>   { NativeFocusManagerImpl    (instance<FocusManager>() as FocusManagerImpl) }
        }

        /** Enables pointer use. */
        public val PointerModule: Module = Module(allowSilentOverride = true, name = "Pointer") {
            bindSingleton<ViewFinder>                  { ViewFinderImpl                   (instance()                        ) }
            bindSingleton<PointerLocationResolver>     { PointerLocationResolverImpl      (document,   instance()            ) }
            bindSingleton<PointerInputService>         { PointerInputServiceImpl          (instance()                        ) }
            bindSingleton<PointerInputManager>         { PointerInputManagerImpl          (instance(), instance(), instance()) }
            bindSingleton<PointerInputServiceStrategy> { PointerInputServiceStrategyWebkit(document,   instance(), instance()) }
        }

        /** Enables keyboard use. Includes [FocusModule]. */
        public val KeyboardModule: Module = Module(allowSilentOverride = true, name = "Keyboard") {
            importOnce(FocusModule)

            // TODO: Make this pluggable
            val keys = mapOf(
                Forward  to setOf(KeyState(KeyCode.Tab, KeyText.Tab, emptySet(     ), Down)),
                Backward to setOf(KeyState(KeyCode.Tab, KeyText.Tab, setOf   (Shift), Down))
            )

            bindSingleton<KeyInputService>         { KeyInputServiceImpl     (instance()                  ) }
            bindSingleton<KeyboardFocusManager>    { KeyboardFocusManagerImpl(instance(), instance(), keys) }
            bindSingleton<KeyInputServiceStrategy> { KeyInputStrategyWebkit  (instance()                  ) }
        }

        /**
         * Enables drag-drop data transfer (not simple moving of Views) that allows data to be shared in and outside the
         * application. Includes [PointerModule].
         */
        public val DragDropModule: Module = Module(allowSilentOverride = true, name = "DragDrop") {
            importOnce(PointerModule)

            bindSingleton<DragManager> { DragManagerImpl(instance(), instance(), instance(), instance(), instance(), instance()) }
        }

        /** Enables use of [Document]s */
        public val DocumentModule: Module = Module(allowSilentOverride = true, name = "Document") {
            bindProvider<Document> { DocumentImpl(instance(), instance(), instance(), instance()) }
        }

        /** Enables accessibility features */
        public val AccessibilityModule: Module = Module(allowSilentOverride = true, name = "Accessibility") {
            importOnce(KeyboardModule)

            // TODO: Can this be handled better?
            bindSingleton { instance<KeyInputService>() as KeyInputServiceImpl }

            // FIXME: Centralize
            bindSingleton<NativeEventHandlerFactory> { { element: HTMLElement, listener: NativeEventListener -> NativeEventHandlerImpl(instanceOrNull(), element, listener) } }

            bindSingleton<AccessibilityManager> { AccessibilityManagerImpl(instance(), instance(), instance(), instance(), instance(), instance()) }

            // TODO: Can this be handled better?
            bindSingleton { instance<AccessibilityManager>() as AccessibilityManagerImpl }
        }

        /** Enables use of [UrlView]s */
        public val UrlViewModule: Module = Module(allowSilentOverride = true, name = "UrlView") {
            bindProvider { UrlView(instance()) }
        }

        public val FontModule: Module = Module(allowSilentOverride = true, name = "Font") {
            bindSingleton<FontLoader> { FontLoaderLegacy(instance(), instance(), instance(), instance()) }
        }

        public val ImageModule: Module = Module(allowSilentOverride = true, name = "Image") {
            bindSingleton<ImageLoader>{ ImageLoaderImpl(instance(), instance()) }
        }

        public val PopupModule: Module = Module(allowSilentOverride = true, name = "Popup") {
            bindSingleton<RelativePositionMonitor> { RelativePositionMonitorImpl() }
            bindSingleton<PopupManager>            { PopupManagerImpl(instance(), instance(), instance()) }
        }

        public val ModalModule: Module = Module(allowSilentOverride = true, name = "Modal") {
            importOnce(PopupModule)

            bindSingleton<ModalManager>{ ModalManagerImpl(instance(), instanceOrNull()) }
        }
    }
}