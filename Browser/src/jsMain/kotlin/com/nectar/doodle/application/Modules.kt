package com.nectar.doodle.application

import com.nectar.doodle.UrlView
import com.nectar.doodle.accessibility.AccessibilityManager
import com.nectar.doodle.accessibility.AccessibilityManagerImpl
import com.nectar.doodle.controls.document.Document
import com.nectar.doodle.datatransport.dragdrop.DragManager
import com.nectar.doodle.datatransport.dragdrop.impl.DragManagerImpl
import com.nectar.doodle.deviceinput.KeyboardFocusManager
import com.nectar.doodle.deviceinput.KeyboardFocusManagerImpl
import com.nectar.doodle.deviceinput.PointerInputManager
import com.nectar.doodle.deviceinput.PointerInputManagerImpl
import com.nectar.doodle.deviceinput.ViewFinder
import com.nectar.doodle.deviceinput.ViewFinderImpl
import com.nectar.doodle.document.impl.DocumentImpl
import com.nectar.doodle.drawing.impl.NativeEventHandlerFactory
import com.nectar.doodle.drawing.impl.NativeEventHandlerImpl
import com.nectar.doodle.drawing.impl.NativeEventListener
import com.nectar.doodle.event.KeyCode
import com.nectar.doodle.event.KeyState
import com.nectar.doodle.event.KeyState.Type.Down
import com.nectar.doodle.event.KeyText
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType.Backward
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType.Forward
import com.nectar.doodle.focus.impl.FocusManagerImpl
import com.nectar.doodle.system.KeyInputService
import com.nectar.doodle.system.PointerInputService
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift
import com.nectar.doodle.system.impl.KeyInputServiceImpl
import com.nectar.doodle.system.impl.KeyInputServiceStrategy
import com.nectar.doodle.system.impl.KeyInputStrategyWebkit
import com.nectar.doodle.system.impl.PointerInputServiceImpl
import com.nectar.doodle.system.impl.PointerInputServiceStrategy
import com.nectar.doodle.system.impl.PointerInputServiceStrategyWebkit
import org.kodein.di.Kodein.Module
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.provider
import org.kodein.di.erased.singleton
import org.w3c.dom.HTMLElement
import kotlin.browser.document

class Modules {
    companion object {
        /** Enables focus management by providing access to [FocusManager]. */
        val FocusModule = Module(allowSilentOverride = true, name = "Focus") {
            bind<FocusManager>() with singleton { FocusManagerImpl(instance()) }
        }

        /** Enables pointer use. */
        val PointerModule = Module(allowSilentOverride = true, name = "Pointer") {
            bind<ViewFinder>                 () with singleton { ViewFinderImpl                   (instance()                        ) }
            bind<PointerInputService>        () with singleton { PointerInputServiceImpl          (instance()                        ) }
            bind<PointerInputManager>        () with singleton { PointerInputManagerImpl          (instance(), instance(), instance()) }
            bind<PointerInputServiceStrategy>() with singleton { PointerInputServiceStrategyWebkit(document,   instance()            ) }
        }

        /** Enables keyboard use. Includes [FocusModule]. */
        val KeyboardModule = Module(allowSilentOverride = true, name = "Keyboard") {
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
        val DragDropModule = Module(allowSilentOverride = true, name = "DragDrop") {
            importOnce(PointerModule)

            bind<DragManager>() with singleton { DragManagerImpl(instance(), instance(), instance(), instance(), instance()) }
        }

        /** Enables use of [Document]s */
        val DocumentModule = Module(allowSilentOverride = true, name = "Document") {
            bind<Document>() with provider { DocumentImpl(instance(), instance(), instance(), instance()) }
        }

        /** Enables accessibility features */
        val AccessibilityModule = Module(allowSilentOverride = true, name = "Accessibility") {
            importOnce(KeyboardModule)

            // TODO: Can this be handled better?
            bind<KeyInputServiceImpl>() with singleton { instance<KeyInputService>() as KeyInputServiceImpl }

            // FIXME: Centralize
            bind<NativeEventHandlerFactory>() with singleton { { element: HTMLElement, listener: NativeEventListener -> NativeEventHandlerImpl(element, listener) } }

            bind<AccessibilityManager>() with singleton { AccessibilityManagerImpl(instance(), instance(), instance(), instance(), instance()) }

            // TODO: Can this be handled better?
            bind<AccessibilityManagerImpl>() with singleton { instance<AccessibilityManager>() as AccessibilityManagerImpl }
        }

        /** Enables use of [UrlView]s */
        val UrlViewModule = Module(allowSilentOverride = true, name = "UrlView") {
            bind<UrlView>() with provider { UrlView(instance()) }
        }
    }
}