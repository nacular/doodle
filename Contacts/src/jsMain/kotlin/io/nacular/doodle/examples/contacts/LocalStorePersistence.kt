package io.nacular.doodle.examples.contacts

import kotlinx.browser.localStorage
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * Simple [PersistentStore] based on LocalStore
 */
class LocalStorePersistence: PersistentStore<Contact> {
    private val name       = "doodle-contacts"
    private val serializer = ListSerializer(Contact.serializer())

    override fun load() = when (val stored = localStorage[name]) {
        null -> emptyList()
        else -> Json.decodeFromString(serializer, stored)
    }

    override fun save(tasks: List<Contact>) {
        localStorage[name] = Json.encodeToString(serializer, tasks)
    }
}