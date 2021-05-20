package io.nacular.doodle.application

import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.impl.FontLoaderImpl
import org.kodein.di.DI.Module
import org.kodein.di.bind
import org.kodein.di.singleton

/**
 * Created by Nicholas Eddy on 5/20/21.
 */
public class Modules {
    public companion object {
        public val FontModule: Module = Module(allowSilentOverride = true, name = "Font") {
            bind<FontLoader>() with singleton { FontLoaderImpl() }
        }
    }
}