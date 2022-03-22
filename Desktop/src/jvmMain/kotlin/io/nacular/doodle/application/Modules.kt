package io.nacular.doodle.application

import io.nacular.doodle.datatransport.dragdrop.DragManager
import io.nacular.doodle.datatransport.dragdrop.impl.DragManagerImpl
import io.nacular.doodle.deviceinput.KeyboardFocusManager
import io.nacular.doodle.deviceinput.KeyboardFocusManagerImpl
import io.nacular.doodle.deviceinput.PointerInputManager
import io.nacular.doodle.deviceinput.PointerInputManagerImpl
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
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.image.impl.Base64Decoder
import io.nacular.doodle.image.impl.ImageLoaderImpl
import io.nacular.doodle.image.impl.UrlDecoder
import io.nacular.doodle.system.KeyInputService
import io.nacular.doodle.system.PointerInputService
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift
import io.nacular.doodle.system.impl.KeyInputServiceImpl
import io.nacular.doodle.system.impl.PointerInputServiceImpl
import io.nacular.doodle.theme.native.NativePointerPreprocessor
import org.kodein.di.DI.Module
import org.kodein.di.bind
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.singleton
import java.net.URLDecoder
import java.util.Base64

/**
 * Created by Nicholas Eddy on 5/20/21.
 */
public class Modules {
    public companion object {
        public val FocusModule: Module = Module(allowSilentOverride = true, name = "Focus") {
            bindSingleton<FocusabilityChecker>  { DefaultFocusabilityChecker(                                  ) }
            bindSingleton<FocusTraversalPolicy> { FocusTraversalPolicyImpl  (instance()                        ) }
            bindSingleton<FocusManager>         { FocusManagerImpl          (instance(), instance(), instance()) }
        }

        public val PointerModule: Module = Module(allowSilentOverride = true, name = "Pointer") {
            bindInstance { NativePointerPreprocessor() }

            bindSingleton<ViewFinder>          { ViewFinderImpl         (instance()                                                               ) }
            bindSingleton<PointerInputService> { PointerInputServiceImpl(instance(), instance(), instanceOrNull()                                 ) }
            bindSingleton<PointerInputManager> { PointerInputManagerImpl(instance(), instance(), instance(), instance<NativePointerPreprocessor>()) }
        }

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
    }
}