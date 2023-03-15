package io.nacular.doodle.examples.contacts

import io.nacular.doodle.controls.SimpleMutableListModel
import io.nacular.doodle.examples.contacts.ContactsModel.EditContext
import io.nacular.doodle.utils.FilteredList
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.observable
import kotlinx.serialization.Serializable

/**
 * Data representing a contact
 */
@Serializable
data class Contact(val name: String, val phoneNumber: String)

/**
 * Collection of contacts
 */
interface ContactsModel {
    class EditContext {
        var name       : String? = null
        var phoneNumber: String? = null
    }

    /**
     * Changes which contacts are shown
     */
    var filter: ((Contact) -> Boolean)?

    // Adds a Contact
    operator fun plusAssign(contact: Contact)

    // Removes a Contact
    operator fun minusAssign(contact: Contact)

    /**
     * Edits the given Contact in place within the model.
     *
     * @param existing contact to edit (must be present in the model)
     */
    fun edit(existing: Contact, block: EditContext.() -> Unit): Result<Contact>

    fun id  (of: Contact): Int?
    fun find(id: Int    ): Contact?
}

interface PersistentStore<T> {
    fun load(              ): List<T>
    fun save(tasks: List<T>)
}

/**
 * Model based on [FilteredList]
 */
class SimpleContactsModel /*private constructor*/(private val filteredList: FilteredList<Contact>): SimpleMutableListModel<Contact>(filteredList), ContactsModel {
    override var filter: ((Contact) -> Boolean)? by observable(null) { _,new ->
        filteredList.filter = new
    }

    // Contacts are inserted at the front
    override fun plusAssign (contact: Contact) = if (size > 0) super.add(0, contact) else super.add(contact)
    override fun minusAssign(contact: Contact) = super.remove(contact)

    override fun edit(existing: Contact, block: EditContext.() -> Unit): Result<Contact> {
        val index = indexOf(existing)

        // Only edit if the Contact really exists
        if (index >= 0) {
            EditContext().apply(block).also {
                // Apply the properties that have been set
                this[index] = Contact(it.name ?: existing.name, it.phoneNumber ?: existing.phoneNumber)
            }
        }

        return this[index]
    }

    override fun id(of: Contact): Int? = indexOf(of).takeIf { it >= 0 }

    override fun find(id: Int): Contact? = this[id].getOrNull()

    companion object {
        operator fun invoke(persistentStore: PersistentStore<Contact>): SimpleContactsModel {
            val contacts = ObservableList(persistentStore.load()).apply {
                changed += { _,_ ->
                    persistentStore.save(this)
                }
            }

            return SimpleContactsModel(FilteredList(contacts)).apply {
                // Add dummy data if empty
                if (isEmpty) {
                    this += Contact("Joe",             "1234567")
                    this += Contact("Jack",            "1234567")
                    this += Contact("Bob",             "1234567")
                    this += Contact("Jen",             "1234567")
                    this += Contact("Herman",          "1234567")
                    this += Contact("Lisa Fuentes",    "1234567")
                    this += Contact("Langston Hughes", "1234567")
                }
            }
        }
    }
}
