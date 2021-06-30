package io.nacular.doodle.application

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
import io.nacular.doodle.system.KeyInputService
import io.nacular.doodle.system.PointerInputService
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift
import io.nacular.doodle.system.impl.KeyInputServiceImpl
import io.nacular.doodle.system.impl.PointerInputServiceImpl
import org.kodein.di.DI.Module
import org.kodein.di.bind
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.singleton

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
            bindSingleton<ViewFinder>          { ViewFinderImpl         (instance()                        ) }
            bindSingleton<PointerInputService> { PointerInputServiceImpl(instance()                        ) }
            bindSingleton<PointerInputManager> { PointerInputManagerImpl(instance(), instance(), instance()) }
        }

        public val KeyboardModule: Module = Module(allowSilentOverride = true, name = "Keyboard") {
            importOnce(FocusModule)

            // TODO: Make this pluggable
            val keys = mapOf(
                FocusTraversalPolicy.TraversalType.Forward to setOf(KeyState (KeyCode.Tab, Tab, emptySet(     ), Down)),
                FocusTraversalPolicy.TraversalType.Backward to setOf(KeyState(KeyCode.Tab, Tab, setOf   (Shift), Down))
            )

            bindSingleton<KeyInputService>     { KeyInputServiceImpl     (instance()                  ) }
            bindSingleton<KeyboardFocusManager>{ KeyboardFocusManagerImpl(instance(), instance(), keys) }
            bindSingleton<KeyInputService>     { KeyInputServiceImpl     (instance()                  ) }
        }

        public val FontModule: Module = Module(allowSilentOverride = true, name = "Font") {
            bind<FontLoader>() with singleton { FontLoaderImpl() }
        }
    }
}