package io.nacular.doodle.examples.contacts

import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.AnimatorImpl
import io.nacular.doodle.examples.ModalFactory
import io.nacular.doodle.examples.ModalFactoryImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.kodein.di.DI.Module
import org.kodein.di.bindFactory
import org.kodein.di.bindSingleton
import org.kodein.di.instance

/**
 * Creates a Module with common bindings.
 *
 * @param appScope used by various dependencies for launching coroutines
 * @param uiDispatcher used to ensure coroutines run on the UI thread
 * @param contacts model used by various dependencies
 *
 * @return module with common bindings.
 */
fun appModule(appScope: CoroutineScope, uiDispatcher: CoroutineDispatcher, contacts: SimpleContactsModel) = Module(name = "AppModule") {
    bindSingleton<Animator>     { AnimatorImpl     (instance(), instance())             }
    bindSingleton<ModalFactory> { ModalFactoryImpl (instance(), instance())             }
    bindSingleton<Navigator>    { NavigatorImpl    (instance(), contacts)               }
    bindSingleton<AppButtons>   { AppButtonsImpl   (instance(), instance(), instance()) }
    bindSingleton<Modals>       { ModalsImpl       (instance(), instance())             }

    bindFactory<AppConfig, ContactList> {
        ContactList(
            assets       = it,
            modals       = instance(),
            appScope     = appScope,
            contacts     = contacts,
            navigator    = instance(),
            textMetrics  = instance(),
            pathMetrics  = instance(),
            uiDispatcher = uiDispatcher
        )
    }

    bindFactory<AppConfig, CreateContactView> {
        CreateContactView(
            assets          = it,
            buttons         = instance(),
            contacts        = contacts,
            navigator       = instance(),
            pathMetrics     = instance(),
            textMetrics     = instance(),
            textFieldStyler = instance(),
        )
    }

    bindFactory<Pair<AppConfig, Contact>, ContactView> { (assets, contact) ->
        ContactView(
            assets       = assets,
            modals       = instance(),
            buttons      = instance(),
            contact      = contact,
            contacts     = contacts,
            appScope     = appScope,
            navigator    = instance(),
            linkStyler   = instance(),
            pathMetrics  = instance(),
            textMetrics  = instance(),
            uiDispatcher = uiDispatcher
        )
    }

    bindFactory<Pair<AppConfig, Contact>, EditContactView> { (assets, contact) ->
        EditContactView(
            assets          = assets,
            modals          = instance(),
            contact         = contact,
            contacts        = contacts,
            buttons         = instance(),
            appScope        = appScope,
            navigator       = instance(),
            pathMetrics     = instance(),
            textMetrics     = instance(),
            uiDispatcher    = uiDispatcher,
            textFieldStyler = instance(),
        )
    }

    bindFactory<AppConfig, Header> {
        Header(
            assets       = it,
            animate      = instance(),
            contacts     = contacts,
            navigator    = instance(),
            textMetrics  = instance(),
            pathMetrics  = instance(),
            focusManager = instance(),
        )
    }

    bindFactory<AppConfig, CreateContactButton> {
        CreateContactButton(assets = it, navigator = instance(), textMetrics = instance(), animate = instance())
    }
}