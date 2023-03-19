package io.dongxi.natty.storage


import io.nacular.doodle.controls.SimpleMutableListModel
import io.nacular.doodle.utils.*
import kotlinx.serialization.Serializable
import kotlin.properties.Delegates.observable


interface PersistentStore {
    fun loadTasks(): List<Task>
    fun save(tasks: List<Task>)
}


@Serializable
class Task(val text: String, val completed: Boolean = false) {
    override fun toString() = "$text${if (completed) " [completed]" else ""}]"
}

class DataStore private constructor(
    private val tasks: ObservableList<Task>,
    private val filteredTasks: FilteredList<Task>
) {
    class DataStoreListModel(dataStore: DataStore) : SimpleMutableListModel<Task>(dataStore.filteredTasks)

    sealed class Filter(internal val operation: (Task) -> Boolean) {
        object Active : Filter({ !it.completed })
        object Completed : Filter({ it.completed })
    }

    var filter: Filter? by observable(null) { _, _, new ->
        filteredTasks.filter = new?.operation
        (filterChanged as SetPool).forEach { it(this) }
    }

    val isEmpty get() = tasks.isEmpty()
    val active get() = tasks.filter { !it.completed }
    val completed get() = tasks.filter { it.completed }

    private val changed: Pool<ChangeObserver<DataStore>> = SetPool()
    private val filterChanged: Pool<ChangeObserver<DataStore>> = SetPool()

    init {
        tasks.changed += { _, _ ->
            (changed as SetPool).forEach { it(this) }
        }
    }

    fun add(task: Task) {
        tasks.add(task)
    }

    fun remove(task: Task) {
        tasks.remove(task)
    }

    fun set(index: Int, task: Task) = set(tasks, index, task)

    fun mark(task: Task, completed: Boolean) {
        tasks.indexOf(task).takeIf { it > -1 }?.let { setCompleted(tasks, task, it, completed) }
    }

    fun markAll(completed: Boolean) {
        tasks.batch {
            forEachIndexed { index, item -> setCompleted(this, item, index, completed) }
        }
    }

    fun removeCompleted() {
        tasks.batch { removeAll { it.completed } }
    }

    private fun setCompleted(list: MutableList<Task>, task: Task, index: Int, completed: Boolean) {
        if (list[index].completed != completed) {
            set(list, index, Task(task.text, completed = completed))
        }
    }

    private fun set(tasks: MutableList<Task>, index: Int, task: Task) = when {
        task.text.isBlank() -> tasks.removeAt(index)
        else -> tasks.set(index, task)
    }

    companion object {
        operator fun invoke(persistentStore: PersistentStore): DataStore {
            val tasks = ObservableList(persistentStore.loadTasks()).apply {
                changed += { _, _ ->
                    persistentStore.save(this)
                }
            }

            return DataStore(tasks, FilteredList(tasks))
        }
    }
}
