package io.nacular.doodle.utils

import kotlin.math.max
import kotlin.math.min
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by Nicholas Eddy on 10/21/17.
 */
public typealias SetObserver     <T>    = (source: ObservableSet<T>,  removed: Set<T>,      added: Set<T>                                    ) -> Unit
public typealias ListObserver    <T>    = (source: ObservableList<T>, removed: Map<Int, T>, added: Map<Int, T>, moved: Map<Int, Pair<Int, T>>) -> Unit
public typealias ChangeObserver  <S>    = (source: S                                                                                         ) -> Unit
public typealias PropertyObserver<S, T> = (source: S, old: T, new: T                                                                         ) -> Unit

public interface Pool<in T> {
    public operator fun plusAssign (item: T)
    public operator fun minusAssign(item: T)
}

public typealias ChangeObservers<S>      = Pool<ChangeObserver<S>>
public typealias PropertyObservers<S, T> = Pool<PropertyObserver<S, T>>

public open class SetPool<T>(protected val delegate: MutableSet<T> = mutableSetOf()): Pool<T>, Set<T> by delegate {
    override fun plusAssign (item: T) { delegate += item }
    override fun minusAssign(item: T) { delegate -= item }
}

public class ChangeObserversImpl<S>(private val source: S, mutableSet: MutableSet<ChangeObserver<S>> = mutableSetOf()): SetPool<ChangeObserver<S>>(mutableSet) {
    public operator fun invoke(): Unit = delegate.forEach { it(source) }
}

public class PropertyObserversImpl<S, T>(private val source: S, mutableSet: MutableSet<PropertyObserver<S, T>> = mutableSetOf()): SetPool<PropertyObserver<S, T>>(mutableSet) {
    public operator fun invoke(old: T, new: T): Unit = delegate.forEach { it(source, old, new) }
}

public class ObservableList<E> private constructor(private val list: MutableList<E>): MutableList<E> by list {

    private val changed_ = SetPool<ListObserver<E>>()
    public val changed: Pool<ListObserver<E>> = changed_

    public fun move(element: E, to: Int): Boolean {
        val oldIndex = indexOf(element)

        if (to !in 0 until size || oldIndex < 0 || oldIndex == to) return false

        list.removeAt(oldIndex)
        list.add     (to, element)

        changed_.forEach {
            it(this, mapOf(), mapOf(), mapOf(to to (oldIndex to element)))
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
                changed_.forEach {
                    it(this@ObservableList, mapOf(index to element), mapOf(), mapOf())
                }
            }
        }
    }

    public operator fun plusAssign(element: E) {
        add(element)
    }

    public operator fun minusAssign(element: E) {
        remove(element)
    }

    override fun add(element: E): Boolean = list.add(element).ifTrue {
        changed_.forEach {
            it(this, mapOf(), mapOf(list.size - 1 to element), mapOf())
        }
    }

    override fun remove(element: E): Boolean {
        val index = list.indexOf(element)

        return when {
            index < 0 -> false
            else      -> list.remove(element).ifTrue { changed_.forEach { it(this, mapOf(index to element), mapOf(), mapOf()) } }
        }
    }

    public override fun addAll(elements: Collection<E>): Boolean = batch { addAll(elements) }

    public override fun addAll(index: Int, elements: Collection<E>): Boolean = batch { addAll(index, elements) }

    public override fun removeAll(elements: Collection<E>): Boolean = batch { removeAll(elements) }
    public override fun retainAll(elements: Collection<E>): Boolean = batch { retainAll(elements) }

    public fun replaceAll(elements: Collection<E>): Boolean = batch { clear(); addAll(elements) }

    private class Move<T>(val from: Int, val to:Int, val value: T) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Move<*>) return false

//            if (value != other.value) return false

            if (from !in listOf(other.from, other.to  )) return false
            if (to   !in listOf(other.to,   other.from)) return false

            return true
        }

        override fun hashCode(): Int {
            val first = min(from, to)
            val last  = max(from, to)

            var result = first
            result = 31 * result + last
            return result
        }
    }

    public fun <T> batch(block: MutableList<E>.() -> T): T = if (changed_.isEmpty()) {
        list.run(block)
    } else {
        // TODO: Can this be optimized?
        val old = ArrayList(list)

        list.run(block).also {
            if (old != this) {
                val removed       = mutableMapOf<Int, E>()
                val added         = mutableMapOf<Int, E>()
                val uniqueMoved   = mutableSetOf<Move<E>>()
                val unusedIndexes = (0 until this.size).toMutableSet()

                old.forEachIndexed { index, item ->
                    if (index >= this.size || this[index] != item) {
                        when (val newIndex = unusedIndexes.firstOrNull { this.getOrNull(it) == item }) {
                            null -> removed[index] = item
                            else -> {
                                uniqueMoved    += Move(from = index, to = newIndex, value = item)
                                unusedIndexes.remove(newIndex)
                            }
                        }
                    }
                }

                removed.forEach { (removedIndex, _) ->
                    uniqueMoved.filter { it.from >= removedIndex }.sortedBy { it.to }.forEach { element ->
                        if (element.from - 1 == element.to) {
                            uniqueMoved.remove(element)
                        } else {
                            uniqueMoved.remove(element)
                            uniqueMoved.add(Move(element.from - 1, element.to, element.value))
                        }
                    }
                }

                unusedIndexes.forEach {
                    val item = this[it]

                    if (it >= old.size || old[it] != item) {
                        added[it] = item

                        // Adjust all the moves
                        uniqueMoved.filter { element -> element.from >= it }.sortedByDescending { it.to }.forEach { element ->
                            if (element.from + 1 == element.to) {
                                uniqueMoved.remove(element)
                            } else {
                                uniqueMoved.remove(element)
                                uniqueMoved.add(Move(element.from + 1, element.to, element.value))
                            }
                        }
                    }
                }

                changed_.forEach {
                    it(this, removed, added, uniqueMoved.associate { it.from to (it.to to it.value) })
                }
            }
        }
    }

    override fun clear() {
        val size    = list.size
        val oldList = ArrayList(list)

        list.clear()

        changed_.forEach {
            it(this, (0 until size).associate { it to oldList[it] }, mapOf(), mapOf())
        }
    }

    override operator fun set(index: Int, element: E): E = list.set(index, element).also { old ->
        if (old != element) {
            changed_.forEach {
                it(this, mapOf(index to old), mapOf(index to element), mapOf())
            }
        }
    }

    public fun notifyChanged(index: Int) {
        changed_.forEach {
            it(this, mapOf(index to this[index]), mapOf(index to this[index]), mapOf())
        }
    }

    override fun add(index: Int, element: E) {
        list.add(index, element)

        changed_.forEach {
            it(this, mapOf(), mapOf(index to element), mapOf())
        }
    }

    override fun removeAt(index: Int): E = list.removeAt(index).also { removed ->
        changed_.forEach {
            it(this, mapOf(index to removed), mapOf(), mapOf())
        }
    }

    public companion object {
        public operator fun <E> invoke(             ): ObservableList<E> = ObservableList(mutableListOf     ())
        public operator fun <E> invoke(list: List<E>): ObservableList<E> = ObservableList(list.toMutableList())
    }
}

public fun <T> ObservableList<T>.sortWith(comparator: Comparator<in T>) {
    batch { sortWith(comparator) }
}

public fun <T> ObservableList<T>.sortWithDescending(comparator: Comparator<in T>) {
    batch { sortWith(comparator.reversed()) }
}

public open class ObservableSet<E> private constructor(protected val set: MutableSet<E>): MutableSet<E> by set {
    private val changed_ = SetPool<SetObserver<E>>()
    public val changed: Pool<SetObserver<E>> = changed_

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

public fun <S, T> observable(initial: T, observers: Iterable<PropertyObserver<S, T>>): ReadWriteProperty<S, T> = ObservableProperty(initial) { thisRef, old, new ->
    observers.forEach { it(thisRef, old, new) }
}
public fun <S, T> observable(initial: T, observers: Iterable<PropertyObserver<S, T>>, onChange: (old: T, new: T) -> Unit): ReadWriteProperty<S, T> = ObservableProperty(initial) { thisRef, old, new ->
    onChange(old, new)
    observers.forEach { it(thisRef, old, new) }
}

private class ObservableProperty<S, T>(initial: T, private val onChange: (thisRef: S, old: T, new: T) -> Unit): ReadWriteProperty<S, T> {
    private var value: T = initial

    override operator fun getValue(thisRef: S, property: KProperty<*>): T = value

    override operator fun setValue(thisRef: S, property: KProperty<*>, value: T) {
        if (value != this.value) {
            val old = this.value

            this.value = value

            onChange(thisRef, old, this.value)
        }
    }
}
