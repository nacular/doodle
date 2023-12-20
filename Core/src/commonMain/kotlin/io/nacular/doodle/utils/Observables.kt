package io.nacular.doodle.utils

import io.nacular.doodle.utils.diff.Delete
import io.nacular.doodle.utils.diff.Difference
import io.nacular.doodle.utils.diff.Differences
import io.nacular.doodle.utils.diff.Equal
import io.nacular.doodle.utils.diff.Insert
import io.nacular.doodle.utils.diff.compare
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Handler for arbitrary changes to some source.
 * @param source  the item that changed
 * @param removed items removed from the source
 * @param added   items added from the source
 */
public typealias SetObserver<S, T> = (source: S, removed: Set<T>, added: Set<T>) -> Unit

/**
 * Handler for arbitrary changes to some source.
 * @param source  the item that changed
 * @param changes made to the source
 */
public typealias ListObserver<S, T> = (source: S, changes: Differences<T>) -> Unit

/**
 * Handler for arbitrary changes to some source.
 * @param source the item that changed
 */
public typealias ChangeObserver<S> = (source: S) -> Unit

/**
 * Handler for changes to some property of a source.
 *
 * @param source the item that changed
 * @param old    the previous value of the property
 * @param new    the updated value of the property
 */
public typealias PropertyObserver<S, T> = (source: S, old: T, new: T) -> Unit

public typealias SetObservers     <S, T> = Pool<SetObserver     <S, T>>
public typealias ListObservers    <S, T> = Pool<ListObserver    <S, T>>
public typealias ChangeObservers  <S   > = Pool<ChangeObserver  <S   >>
public typealias PropertyObservers<S, T> = Pool<PropertyObserver<S, T>>

/**
 * A collection of items that only exposes add/remove semantics that is used for observers.
 */
public interface Pool<in T> {
    /** Add [item] to the collection */
    public operator fun plusAssign (item: T)

    /** Remove [item] from the collection */
    public operator fun minusAssign(item: T)
}

/**
 * A [Pool] that notifies of changes to its contents.
 */
public interface ObservablePool<T>: Pool<T> {
    /** Notified when any item is added/removed from the Pool */
    public val changed: Pool<PoolObserver<T>>
}

/**
 * Observer of changes to [ObservablePool].
 */
public interface PoolObserver<T> {
    /** Notified when [item] is added to the [source] */
    public fun added(source: ObservablePool<T>, item: T) {}

    /** Notified when [item] is removed from the [source] */
    public fun removed(source: ObservablePool<T>, item: T) {}
}

/**
 * Implementation of [Pool] based on a [Set].
 */
public expect open class SetPool<T> private constructor(delegate: MutableSet<T>): Pool<T>, Set<T> {
    public constructor()
}

/**
 * Implementation of [ObservablePool] based on a [Set]. Contents are stored in the
 * provided delegate.
 *
 * @param delegate that underlies this pool
 */
public expect open class ObservableSetPool<T>(delegate: SetPool<T>): ObservablePool<T>, Set<T>

/**
 * [ChangeObservers] implemented using a [SetPool].
 *
 * @property source being monitored for changes
 */
public class ChangeObserversImpl<S>(private val source: S): SetPool<ChangeObserver<S>>() {
    /** Notifies observers of changes to [source]. */
    public operator fun invoke() {
        forEach { it(source) }
    }
}

/**
 * [PropertyObservers] implemented using a [SetPool].
 *
 * @property source being monitored for changes
 */
public class PropertyObserversImpl<S, T>(private val source: S): SetPool<PropertyObserver<S, T>>() {
    /**
     * Notifies observers of changes to [source].
     *
     * @param old value of property that changed
     * @param new value of property that changed
     */
    public operator fun invoke(old: T, new: T) {
        forEach { it(source, old, new) }
    }
}

/**
 * A [List] that notifies observers of changes to its contents.
 */
public interface ObservableList<E>: MutableList<E> {
    /** Notifies of changes to the list */
    public val changed: ListObservers<ObservableList<E>, E>

    /**
     * Moves [element] to a new index.
     *
     * @param   element to move
     * @to      this index
     * @return `true` if the element was moved
     */
    public fun move(element: E, to: Int): Boolean

    /** Adds [element] to the list */
    public operator fun plusAssign(element: E)

    /** Removes [element] from the list */
    public operator fun minusAssign(element: E)

    /**
     * Replaces to list's contents with [elements].
     *
     * @param elements to replace the contents with
     * @return `true` if any changes made to the list
     */
    public fun replaceAll(elements: Collection<E>): Boolean

    /**
     * Runs the operations within [block] and then applies them
     * all to the list as a single operation. This reduces the
     * set of events that would normally trigger if these operations
     * were done sequentially.
     *
     * NOTE: This results in a copy of the existing data.
     *
     * @param block to be run
     * @return the result of [block]
     */
    public fun <T> batch(block: MutableList<E>.() -> T): T

    /**
     * Explicitly trigger a diff that shows the element at [index] being
     * deleted and re-added. This is useful for cases where the elements
     * in the list are mutable and a change has happened to one that would
     * normally not result in a call to [changed] (which only notifies of
     * membership changes.
     *
     * @param index to raise change for
     */
    public fun notifyChanged(index: Int)

    public companion object {
        public operator fun <E> invoke(             ): ObservableList<E> = ObservableListImpl(mutableListOf     ())
        public operator fun <E> invoke(list: List<E>): ObservableList<E> = ObservableListImpl(list.toMutableList())

        internal operator fun <E> invoke(               equality: (E, E) -> Boolean = { a, b -> a == b }): ObservableList<E> = ObservableListImpl(mutableListOf     (), equality)
        internal operator fun <E> invoke(list: List<E>, equality: (E, E) -> Boolean = { a, b -> a == b }): ObservableList<E> = ObservableListImpl(list.toMutableList(), equality)

        internal fun <E> wrap(list: MutableList<E>): ObservableList<E> = ObservableListImpl(list)
    }
}

public fun <T> ObservableList<T>.sortWith(comparator: Comparator<in T>) {
    batch { sortWith(comparator) }
}

public fun <T> ObservableList<T>.sortWithDescending(comparator: Comparator<in T>) {
    batch { sortWith(comparator.reversed()) }
}

public open class ObservableListImpl<E> internal constructor(private val list: MutableList<E>, private val equality: (E, E) -> Boolean = { a, b -> a == b }): ObservableList<E>, MutableList<E> by list {

    private val changed_ = SetPool<ListObserver<ObservableList<E>, E>>()
    public override val changed: ListObservers<ObservableList<E>, E> = changed_

    public override fun move(element: E, to: Int): Boolean {
        val oldIndex = indexOf(element)

        if (to !in 0 until size || oldIndex < 0 || oldIndex == to) return false

        val firstIndex = min(oldIndex, to)
        val lastIndex  = max(oldIndex, to)

        val diffs = mutableListOf<Difference<E>>()

        if (firstIndex > 0) {
            diffs += Equal(list.subList(fromIndex = 0, toIndex = firstIndex).toList())
        }

        val delta       = if (to > oldIndex) 1 else 0
        val elementList = listOf(element)

        diffs += if (oldIndex > to) Insert(elementList) else Delete(elementList)
        diffs += Equal (list.subList(fromIndex = firstIndex + delta, toIndex = lastIndex + delta).toList())
        diffs += if (oldIndex > to) Delete(elementList) else Insert(elementList)

        if (lastIndex < size - 1) {
            diffs += Equal(list.subList(fromIndex = lastIndex + 1, toIndex = size).toList())
        }

        list.removeAt(oldIndex)
        list.add     (to, element)

        val differences = Differences(diffs)

        changed_.forEach {
            it(this, differences)
        }

        return true
    }

    override fun iterator(): MutableIterator<E> = list.iterator().let {
        object: MutableIterator<E> by it {
            private var index = -1

            override fun next() = it.next().also { index++ }

            override fun remove() {
                val element = list[index--]
                it.remove()

                val diffs = mutableListOf<Difference<E>>()

                if (index > 0) {
                    diffs += Equal(list.subList(fromIndex = 0, toIndex = index).toList())
                }

                diffs += Delete(listOf(element))

                if (index < size - 1) {
                    diffs += Equal(list.subList(fromIndex = index + 1, toIndex = size).toList())
                }

                val differences = Differences(diffs)

                changed_.forEach {
                    it(this@ObservableListImpl, differences)
                }
            }
        }
    }

    public override operator fun plusAssign(element: E) {
        add(element)
    }

    public override operator fun minusAssign(element: E) {
        remove(element)
    }

    override fun add(element: E): Boolean = list.add(element).ifTrue {
        val diffs = mutableListOf<Difference<E>>()

        if (size > 1) {
            diffs += Equal(this.subList(fromIndex = 0, toIndex = size - 1).toList())
        }

        diffs += Insert(listOf(element))

        val differences = Differences(diffs)

        changed_.forEach {
            it(this, differences)
        }
    }

    override fun remove(element: E): Boolean {
        val index = list.indexOf(element)

        return when {
            index < 0 -> false
            else      -> list.remove(element).ifTrue {
                val diffs = mutableListOf<Difference<E>>()

                if (index > 0) {
                    diffs += Equal(this.subList(fromIndex = 0, toIndex = index).toList())
                }

                diffs += Delete(listOf(element))

                if (index < size - 1) {
                    diffs += Equal(this.subList(fromIndex = index, toIndex = size).toList())
                }

                val differences = Differences(diffs)

                changed_.forEach { it(this, differences) }
            }
        }
    }

    public override fun addAll(elements: Collection<E>): Boolean = batch { addAll(elements) }

    public override fun addAll(index: Int, elements: Collection<E>): Boolean = batch { addAll(index, elements) }

    public override fun removeAll(elements: Collection<E>): Boolean = batch { removeAll(elements) }
    public override fun retainAll(elements: Collection<E>): Boolean = batch { retainAll(elements) }

    public override fun replaceAll(elements: Collection<E>): Boolean = batch { clear(); addAll(elements) }

    public override fun <T> batch(block: MutableList<E>.() -> T): T = if (changed_.isEmpty()) {
        list.run(block)
    } else {
        // TODO: Can this be optimized?
        val old = ArrayList(list)

        list.run(block).also {
            compare(old, this, by = equality).takeIf { it.iterator().hasNext() }?.let { diffs ->
                changed_.forEach {
                    it(this, diffs)
                }
            }
        }
    }

    override fun clear() {
        val oldList = ArrayList(list)
        val diffs   = listOf(Delete(oldList))

        list.clear()

        val differences = Differences(diffs)

        changed_.forEach {
            it(this, differences)
        }
    }

    override operator fun set(index: Int, element: E): E = list.set(index, element).also { old ->
        if (old != element) {
            val diffs = mutableListOf<Difference<E>>()

            if (index > 0) {
                diffs += Equal(this.subList(fromIndex = 0, toIndex = index))
            }

            diffs += Delete(listOf(old))
            diffs += Insert(listOf(element))

            if (index < size - 1) {
                diffs += Equal(this.subList(fromIndex = index + 1, toIndex = size))
            }

            val differences = Differences(diffs)

            changed_.forEach {
                it(this, differences)
            }
        }
    }

    public override fun notifyChanged(index: Int) {
        val diffs = mutableListOf<Difference<E>>()

        if (index > 0) {
            diffs += Equal(this.subList(fromIndex = 0, toIndex = index))
        }

        diffs += Delete(listOf(this[index]))
        diffs += Insert(listOf(this[index]))

        if (index < size - 1) {
            diffs += Equal(this.subList(fromIndex = index + 1, toIndex = size))
        }

        val differences = Differences(diffs)

        changed_.forEach {
            it(this, differences)
        }
    }

    override fun add(index: Int, element: E) {
        list.add(index, element)

        val diffs = mutableListOf<Difference<E>>()

        if (index > 0) {
            diffs += Equal(this.subList(fromIndex = 0, toIndex = index))
        }

        diffs += Insert(listOf(element))

        if (index < size - 1) {
            diffs += Equal(this.subList(fromIndex = index + 1, toIndex = size))
        }

        val differences = Differences(diffs)

        changed_.forEach {
            it(this, differences)
        }
    }

    override fun removeAt(index: Int): E = list.removeAt(index).also { removed ->
        val diffs = mutableListOf<Difference<E>>()

        if (index > 0) {
            diffs += Equal(this.subList(fromIndex = 0, toIndex = index).toList())
        }

        diffs += Delete(listOf(removed))

        if (index < size - 1) {
            diffs += Equal(this.subList(fromIndex = index, toIndex = size).toList())
        }

        val differences = Differences(diffs)

        changed_.forEach {
            it(this, differences)
        }
    }
}

public open class ObservableSet<E> private constructor(protected val set: MutableSet<E>): MutableSet<E> by set {
    private val changed_ = SetPool<SetObserver<ObservableSet<E>, E>>()
    public val changed: SetObservers<ObservableSet<E>, E> = changed_

    override fun add(element: E): Boolean = set.add(element).ifTrue {
        changed_.forEach {
            it(this, emptySet(), setOf(element))
        }
    }

    public override fun remove(element: E): Boolean = set.remove(element).ifTrue { changed_.forEach { it(this, setOf(element), emptySet()) } }

    public override fun addAll(elements: Collection<E>): Boolean = batch { addAll(elements) }

    public override fun removeAll(elements: Collection<E>): Boolean = batch { removeAll(elements) }
    public override fun retainAll(elements: Collection<E>): Boolean = batch { retainAll(elements) }

    public fun replaceAll(elements: Collection<E>): Boolean = batch { clear(); addAll(elements) }

    public fun <T> batch(block: MutableSet<E>.() -> T): T = if (changed_.isEmpty()) {
        set.run(block)
    } else {
        // TODO: Can this be optimized?
        val old = HashSet(set)

        set.run(block).also {
            if (old != this) {
                changed_.forEach {
                    it(this, old.asSequence().filter { it !in set }.toSet(), set.asSequence().filter { it !in old }.toSet())
                }
            }
        }
    }

    override fun clear() {
        val oldSet = HashSet(set)

        set.clear()

        changed_.forEach {
            it(this, oldSet, emptySet())
        }
    }

    public companion object {
        public operator fun <E> invoke(           ): ObservableSet<E> = ObservableSet(mutableSetOf())
        public operator fun <E> invoke(set: Set<E>): ObservableSet<E> = ObservableSet(set.toMutableSet())
    }
}

public fun <S, T> observable(initial: T, onChange: S.(old: T, new: T) ->Unit): ReadWriteProperty<S, T> = ObservableProperty(initial) { thisRef, old, new ->
    onChange(thisRef, old, new)
}

internal fun <S, T> observable(initial: T, equality: (T, T) -> Boolean, onChange: S.(old: T, new: T) ->Unit): ReadWriteProperty<S, T> = ObservableProperty(initial, equality) { thisRef, old, new ->
    onChange(thisRef, old, new)
}

public fun <S, T> observable(initial: T, observers: Iterable<PropertyObserver<S, T>>): ReadWriteProperty<S, T> = ObservableProperty(initial) { thisRef, old, new ->
    observers.forEach { it(thisRef, old, new) }
}

internal fun <S, T> observable(initial: T, equality: (T, T) -> Boolean, observers: Iterable<PropertyObserver<S, T>>): ReadWriteProperty<S, T> = ObservableProperty(initial, equality) { thisRef, old, new ->
    observers.forEach { it(thisRef, old, new) }
}

public fun <S, T> observable(initial: T, observers: Iterable<PropertyObserver<S, T>>, onChange: (old: T, new: T) -> Unit): ReadWriteProperty<S, T> = ObservableProperty(initial) { thisRef, old, new ->
    onChange(old, new)
    observers.forEach { it(thisRef, old, new) }
}

internal fun <S, T> observable(initial: T, equality: (T, T) -> Boolean, observers: Iterable<PropertyObserver<S, T>>, onChange: (old: T, new: T) -> Unit): ReadWriteProperty<S, T> = ObservableProperty(initial, equality) { thisRef, old, new ->
    onChange(old, new)
    observers.forEach { it(thisRef, old, new) }
}

private class ObservableProperty<S, T>(initial: T, private val equality: (T, T) -> Boolean = { a, b -> a == b }, private val onChange: (thisRef: S, old: T, new: T) -> Unit): ReadWriteProperty<S, T> {
    private var value: T = initial

    override operator fun getValue(thisRef: S, property: KProperty<*>): T = value

    override operator fun setValue(thisRef: S, property: KProperty<*>, value: T) {
        if (!equality(value, this.value)) {
            val old = this.value

            this.value = value

            onChange(thisRef, old, this.value)
        }
    }
}