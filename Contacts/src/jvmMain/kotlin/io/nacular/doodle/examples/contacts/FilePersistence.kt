package io.nacular.doodle.examples.contacts

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Simple [PersistentStore] based on a file
 */
class FilePersistence: PersistentStore<Contact> {
    private val file       = File("doodle-contacts.json")
    private val serializer = ListSerializer(Contact.serializer())

    override fun load(): List<Contact> {
        try {
            return Json.decodeFromString(serializer, file.readText())
        } catch (ignored: Exception) {
        }

        return emptyList()
    }

    override fun save(tasks: List<Contact>) {
        file.writeText(Json.encodeToString(serializer, tasks))
    }
}