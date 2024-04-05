package io.nacular.doodle.application

import io.nacular.doodle.dom.HTMLElement
import org.kodein.di.DI.Module
import org.kodein.di.bindings.NoArgBindingDI

/**
 * Launches an application nested within [root] with the given [modules]. The set of modules configure which types are
 * available for injection to the app. Modules also control some features like Pointer and Keyboard
 * support.
 *
 * @param root element where the application will be hosted
 * @param allowDefaultDarkMode controls whether the Browser tries to provide dark vs light styles
 * @param modules to use for the application
 * @param creator block that constructs the application
 */
public inline fun <reified T: Application> application(
             root                : org.w3c.dom.HTMLElement,
             allowDefaultDarkMode: Boolean     = false,
             modules             : List<Module> = emptyList(),
    noinline creator             : NoArgBindingDI<*>.() -> T
): Application = application(root.unsafeCast<HTMLElement>(), allowDefaultDarkMode, modules, creator)