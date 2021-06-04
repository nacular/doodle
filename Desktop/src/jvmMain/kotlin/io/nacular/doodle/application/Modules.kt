package io.nacular.doodle.application

import io.nacular.doodle.deviceinput.PointerInputManager
import io.nacular.doodle.deviceinput.PointerInputManagerImpl
import io.nacular.doodle.deviceinput.ViewFinder
import io.nacular.doodle.deviceinput.ViewFinderImpl
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.impl.FontLoaderImpl
import io.nacular.doodle.system.PointerInputService
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
        public val PointerModule: Module = Module(allowSilentOverride = true, name = "Pointer") {
            bindSingleton<ViewFinder>          { ViewFinderImpl         (instance()                        ) }
            bindSingleton<PointerInputService> { PointerInputServiceImpl(instance()                        ) }
            bindSingleton<PointerInputManager> { PointerInputManagerImpl(instance(), instance(), instance()) }
        }

        public val FontModule: Module = Module(allowSilentOverride = true, name = "Font") {
            bind<FontLoader>() with singleton { FontLoaderImpl() }
        }
    }
}