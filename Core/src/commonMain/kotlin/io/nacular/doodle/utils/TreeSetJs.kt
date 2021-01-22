package io.nacular.doodle.utils

/**
 * Created by Nicholas Eddy on 4/11/18.
 */
public class BstNode<E>(public var value: E) {
    public var left : BstNode<E>? = null
    public var right: BstNode<E>? = null
}

@Suppress("PrivatePropertyName", "FunctionName")
public open class TreeSetJs<E> constructor(private val comparator: Comparator<E>, elements: Collection<E>): Set<E> {
    public constructor(comparator: Comparator<E>): this(comparator, emptyList<E>())

    private var root = null as BstNode<E>?

    init {
        elements.forEach { add(it) }
    }

    override val size: Int get() = size_

    private var size_ = 0

    override fun isEmpty(): Boolean = root == null

    override fun contains(element: E): Boolean = if (isEmpty()) false else contains(root, element)

    override fun containsAll(elements: Collection<E>): Boolean = if (isEmpty()) false else elements.all { contains(it) }

    override fun iterator(): Iterator<E> = BstIterator()

    override fun toString(): String {
        return "[${ iterator().asSequence().joinToString(", ")}]"
    }

    protected open fun add(element: E): Boolean = when (root) {
        null -> {
            root = BstNode(element)
            ++size_
            true
        }
        else -> add(root!!, element).ifTrue { ++size_ }
    }

    protected open fun remove_(element: E): Boolean = (root?.let {
        when (element) {
            it.value -> {

                val auxRoot = BstNode(it.value)

                auxRoot.left = root

                val result = remove(it, auxRoot, element)

                root = auxRoot.left

                result

            }
            else     -> remove(it, null, element)
        }

    } ?: false).ifTrue { --size_; if (size < 0) { throw Exception("BROKEN!!!!") } }

    protected fun clear_() {
        root  = null
        size_ = 0
    }

    private fun add(node: BstNode<E>, element: E): Boolean = when {
        node.value == element -> false
        comparator.compare(node.value, element) > 0 -> when (node.left) {
            null -> { node.left = BstNode(element); true }
            else -> add(node.left!!, element)
        }
        else -> when (node.right) {
            null -> { node.right = BstNode(element); true }
            else -> add(node.right!!, element)
        }
    }

    private fun remove(from: BstNode<E>, parent: BstNode<E>?, element: E): Boolean {
        when {
            comparator.compare(element, from.value) < 0 -> return from.left?.let  { remove(it, from, element) } ?: false
            comparator.compare(element, from.value) > 0 -> return from.right?.let { remove(it, from, element) } ?: false
            else                                        -> {
                if (from.left != null && from.right != null) {
                    from.right?.let {
                        from.value = minValue(it)

                        return remove(it, from, from.value)
                    }
                } else if (parent?.left == from) {
                    parent.left = from.left ?: from.right
                    return true

                } else if (parent?.right == from) {
                    parent.right = from.left ?: from.right
                    return true
                }
            }
        }

        return false
    }

    private fun minValue(from: BstNode<E>): E = from.left?.let {
        minValue(it)
    } ?: from.value


    private fun contains(node: BstNode<E>?, element: E): Boolean = when {
        node == null                  -> false
        node.value == element         -> true
        contains(node.left,  element) -> true
        contains(node.right, element) -> true
        else                          -> false
    }

    protected inner class BstIterator: MutableIterator<E> {
        private val stack by lazy {
            mutableListOf<BstNode<E>>().also {
                populateStack(root, it)
            }
        }

        override fun remove() {
            if (hasNext()) {
                val node = pop()

                remove_(node.value)
            }
        }

        override fun hasNext(): Boolean = stack.isNotEmpty()

        override fun next(): E {
            if (!hasNext()) {
                throw NoSuchElementException("The tree has no more elements")
            }

            return pop().value
        }

        private fun pop(): BstNode<E> {
            val node = stack.removeAt(stack.lastIndex)

            populateStack(node.right, stack)

            return node
        }

        private fun populateStack(from: BstNode<E>?, stack: MutableList<BstNode<E>>) {
            var node = from

            while (node != null) {
                stack.add(node)
                node = node.left
            }
        }
    }

    public companion object {
        public operator fun <T: Comparable<T>> invoke(                       ): TreeSetJs<T> = TreeSetJs(Comparator { a, b -> a.compareTo(b) }          )
        public operator fun <T: Comparable<T>> invoke(elements: Collection<T>): TreeSetJs<T> = TreeSetJs(Comparator { a, b -> a.compareTo(b) }, elements)
    }
}