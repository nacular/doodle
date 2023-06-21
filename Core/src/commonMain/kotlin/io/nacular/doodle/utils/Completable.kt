package io.nacular.doodle.utils

import io.nacular.doodle.utils.CompletableImpl.State.Active
import io.nacular.doodle.utils.CompletableImpl.State.Canceled
import io.nacular.doodle.utils.CompletableImpl.State.Completed
import kotlin.js.JsName
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


/**
 * Defines an activity that can be completed at some point.
 */
public interface Completable: Cancelable {
    /** Notifies when this item is completed */
    public val completed: Pool<(source: Completable) -> Unit>

    // FIXME: Move this to Cancelable interface
    /** Notifies when this item is canceled */
    public val canceled: Pool<(source: Completable) -> Unit>
}

/**
 * Defines an activity that can be paused and resumed.
 */
public interface Pausable: Completable {
    /** Notifies when this item is paused */
    public val paused: Pool<(source: Pausable) -> Unit>

    /** Notifies when this item is resumed */
    public val resumed: Pool<(source: Pausable) -> Unit>

    /** Pauses this item */
    public fun pause()

    /** Resumes this item */
    public fun resume()
}

public fun Collection<Completable>.allCompleted(completed: () -> Unit) {
    var numCompleted = 0

    forEach {
        it.completed += {
            ++numCompleted

            if (numCompleted >= size) {
                completed()
            }
        }
    }
}

internal class NoOpPool<T>: Pool<(source: T) -> Unit> {
    override fun plusAssign(item: (source: T) -> Unit) {}

    override fun minusAssign(item: (source: T) -> Unit) {}
}

internal class InstantPool<T>(private val source: T): Pool<(source: T) -> Unit> {
    override fun plusAssign(item: (source: T) -> Unit) {
        item(source)
    }

    override fun minusAssign(item: (source: T) -> Unit) {}
}

public open class CompletableImpl: Completable {
    public enum class State { Active, Completed, Canceled }

    protected var state: State = Active
        private set(new) {
            if (field != Active) { return }

            field = new

            when (new) {
                Completed -> {
                    completed_.forEach { it(this) }
                    completed_.clear()
                    completed = InstantPool(this)
                    canceled  = NoOpPool()
                }
                Canceled -> {
                    canceled_.forEach { it(this) }
                    canceled_.clear()
                    canceled  = InstantPool(this)
                    completed = NoOpPool()
                }
                else -> {}
            }
        }

    private val completed_ by lazy { ObservableSet<(source: Completable) -> Unit>() }
    private val canceled_  by lazy { ObservableSet<(source: Completable) -> Unit>() }

    override var completed: Pool<(source: Completable) -> Unit> = SetPool(completed_); protected set
    override var canceled : Pool<(source: Completable) -> Unit> = SetPool(canceled_ ); protected set

    init {
        completed_.changed += { _,_,added ->
            if (state == Completed) {
                added.forEach { it(this) }
            }
        }

        canceled_.changed += { _,_,added ->
            if (state == Canceled) {
                added.forEach { it(this) }
            }
        }
    }

    override fun cancel() { state = Canceled }

    @JsName("completedFunc")
    protected open fun completed() { state = Completed }
}

public val NoOpCompletable: Completable = object: CompletableImpl() {
    init {
        completed()
    }
}

public interface Cancelable {
    public fun cancel()
}

/**
 * Creates a [ReadWriteProperty] that will cancel the previous value when changed.
 *
 * @param default value for the property to begin with
 * @param onChanged that notifies when the underlying property changes
 */
public fun <V, T: Cancelable> autoCanceling(
    default  : T? = null,
    onChanged: (old: T?, new: T?) -> Unit = { _,_ -> }
): ReadWriteProperty<V, T?> = object: ReadWriteProperty<V, T?> {
    private var backingField: T? = default

    override operator fun getValue(thisRef: V, property: KProperty<*>): T? = backingField

    override operator fun setValue(thisRef: V, property: KProperty<*>, value: T?) {
        val old = backingField?.apply { cancel() }

        backingField = value

        onChanged(old, backingField)
    }
}