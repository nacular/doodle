package io.nacular.doodle.examples.contacts

/**
 * Provides a way to navigate between various views of the app.
 */
interface Navigator {
    fun showContact      (contact: Contact)
    fun showContactEdit  (contact: Contact)
    fun showContactList  (                )
    fun showCreateContact(                )
    fun goBack           (                )
}

/**
 * Navigator based on [Router].
 *
 * @param router used to update app view
 * @param contacts model
 */
class NavigatorImpl(private val router: Router, private val contacts: ContactsModel): Navigator {
    override fun showContact(contact: Contact) {
        when (val id = contacts.id(contact)) {
             null -> showContactList()
             else -> router.goTo("/contact/$id")
        }
    }

    override fun showContactEdit(contact: Contact) {
        when (val id = contacts.id(contact)) {
            null -> showContactList()
            else -> router.goTo("/contact/$id/edit")
        }
    }

    override fun showContactList() {
        router.goTo("")
    }

    override fun showCreateContact() {
        router.goTo("/add")
    }

    override fun goBack() {
        router.goBack()
    }
}