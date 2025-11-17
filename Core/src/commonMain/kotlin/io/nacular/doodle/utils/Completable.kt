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

    private inner class CompletablePool: SetPool<(source: Completable) -> Unit>()

    protected var state: State = Active; private set(new) {
        if (field != Active) { return }

        field = new

        when (new) {
            Completed -> {
                completed_?.forEach { it(this) }
                completed_ = null
                completed = InstantPool(this)
                canceled  = NoOpPool()
            }
            Canceled -> {
                canceled_?.forEach { it(this) }
                canceled_ = null
                canceled  = InstantPool(this)
                completed = NoOpPool()
            }
            else -> {}
        }
    }

    private val poolObserverImpl = object: PoolObserver<(source: Completable) -> Unit> {
        override fun added(source: ObservablePool<(source: Completable) -> Unit>, item: (source: Completable) -> Unit) {
            when {
                source == completed && state == Completed -> item(this@CompletableImpl)
                source == canceled  && state == Canceled  -> item(this@CompletableImpl)
            }
        }
    }

    private var completed_: CompletablePool? = CompletablePool()
    private var canceled_ : CompletablePool? = CompletablePool()

    override var completed: Pool<(source: Completable) -> Unit> = ObservableSetPool(completed_!!).apply { changed += poolObserverImpl }; protected set
    override var canceled : Pool<(source: Completable) -> Unit> = ObservableSetPool(canceled_!! ).apply { changed += poolObserverImpl }; protected set

    override fun cancel() { state = Canceled }

    @JsName("completedFunc")
    protected open fun completed() { state = Completed }
}

public open class PausableImpl: Pausable, CompletableImpl() {
    private inner class PausablePool: SetPool<(source: Pausable) -> Unit>()

    protected var isPaused: Boolean = false; private set

    private val poolObserverImpl = object: PoolObserver<(source: Pausable) -> Unit> {
        override fun added(source: ObservablePool<(source: Pausable) -> Unit>, item: (source: Pausable) -> Unit) {
            if (isPaused) item(this@PausableImpl)
        }
    }

    override var paused : Pool<(source: Pausable) -> Unit> = ObservableSetPool(PausablePool()).apply  { changed += poolObserverImpl }; protected set
    override var resumed: Pool<(source: Pausable) -> Unit> = SetPool(); protected set

    override fun pause() {
        isPaused = true
    }

    override fun resume() {
        isPaused = false
    }
}

public val NoOpCompletable: Completable = object: CompletableImpl() {
    init {
        completed()
    }
}

public val NoOpPausable: Pausable = object: PausableImpl() {
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