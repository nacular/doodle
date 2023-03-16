package io.nacular.doodle.examples

import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.AnimatorImpl
import io.nacular.doodle.application.Modules.Companion.DragDropModule
import io.nacular.doodle.application.Modules.Companion.FocusModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.application
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.image.impl.ImageLoaderImpl
import io.nacular.doodle.theme.basic.BasicTheme
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import org.kodein.di.DI.Module
import org.kodein.di.bindSingleton
import org.kodein.di.instance

/**
 * Creates a [PhotosApp]
 */
fun main() {
    application(modules = listOf(
        FocusModule,
        KeyboardModule,
        DragDropModule,
        BasicTheme.basicLabelBehavior(),
        nativeTextFieldBehavior(spellCheck = false),
        BasicTheme.basicMutableSpinnerBehavior(),
        BasicTheme.basicCircularProgressIndicatorBehavior(thickness = 18.0),
        Module(name = "AppModule") {
            bindSingleton<Animator>    { AnimatorImpl   (instance(), instance()) }
            bindSingleton<ImageLoader> { ImageLoaderImpl(instance(), instance()) }
        }
    )) {
        // load app
        PhotosApp(instance(), instance(), instance(), instance(), instance(), instance())
    }
}