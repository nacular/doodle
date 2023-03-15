import io.nacular.doodle.application.Modules.Companion.FocusModule
import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.ImageModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.application
import io.nacular.doodle.coroutines.Dispatchers
import io.nacular.doodle.examples.contacts.EditContactView
import io.nacular.doodle.examples.contacts.LocalStorePersistence
import io.nacular.doodle.examples.contacts.Router
import io.nacular.doodle.examples.contacts.SimpleContactsModel
import io.nacular.doodle.examples.contacts.TrivialRouter
import io.nacular.doodle.examples.contacts.appModule
import io.nacular.doodle.examples.contacts.AppConfig
import io.nacular.doodle.examples.contacts.AppConfigImpl
import io.nacular.doodle.examples.contacts.Contact
import io.nacular.doodle.examples.contacts.ContactView
import io.nacular.doodle.examples.contacts.ContactsApp
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.impl.PathMetricsImpl
import io.nacular.doodle.theme.basic.BasicTheme.Companion.basicLabelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeHyperLinkBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeScrollPanelBehavior
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.kodein.di.DI.Module
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.factory
import org.kodein.di.instance

fun main() {
    val contacts = SimpleContactsModel(LocalStorePersistence())
    val appScope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)

    application (modules = listOf(
        FontModule,
        ImageModule,
        FocusModule,
        PointerModule,
        KeyboardModule,
        basicLabelBehavior       (),
        nativeTextFieldBehavior  (spellCheck = false),
        nativeHyperLinkBehavior  (),
        nativeScrollPanelBehavior(),
        appModule(appScope = appScope, contacts = contacts, uiDispatcher = Dispatchers.UI),
        Module   (name = "PlatformModule") {
            // Platform-specific bindings
            bindInstance<Router>       { TrivialRouter  (window    ) }
            bindSingleton<PathMetrics> { PathMetricsImpl(instance()) }
        }
    )) {
        // load app
        ContactsApp(
            theme             = instance(),
            assets            = { AppConfigImpl(instance(), instance()) },
            router            = instance(),
            Header            = factory(),
            display           = instance(),
            contacts          = contacts,
            appScope          = appScope,
            navigator         = instance(),
            ContactList       = factory(),
            uiDispatcher      = Dispatchers.UI,
            ContactView       = { assets, contact -> factory<Pair<AppConfig, Contact>, ContactView>()(assets to contact) },
            CreateButton      = factory(),
            themeManager      = instance(),
            EditContactView   = { assets, contact -> factory<Pair<AppConfig, Contact>, EditContactView>()(assets to contact) },
            CreateContactView = factory(),
        )
    }
}