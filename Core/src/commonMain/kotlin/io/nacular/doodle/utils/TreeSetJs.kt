package io.nacular.doodle.utils

import io.nacular.doodle.core.Internal

/**
 * Created by Nicholas Eddy on 4/11/18.
 */
@Suppress("PrivatePropertyName", "FunctionName")
@Internal
public open class TreeSetJs<E> constructor(private val comparator: Comparator<E>, elements: Collection<E>): Set<E> {
    private class BstNode<E>(var value: E) {
        var left : BstNode<E>? = null
        var right: BstNode<E>? = null
    }

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

    private fun add(node: BstNode<E>, element: E): Boolean {
        @Suppress("NAME_SHADOWING") var node = node

        while (true) {
            when {
                node.value == element                       -> return false
                comparator.compare(node.value, element) > 0 -> when (node.left) {
                    null -> { node.left = BstNode(element); return true }
                    else -> node = node.left!!
                }
                else                                        -> when (node.right) {
                    null -> { node.right = BstNode(element); return true }
                    else -> node = node.right!!
                }
            }
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun remove(from: BstNode<E>, parent: BstNode<E>?, element: E): Boolean {
        var from   = from
        var parent = parent
        var element = element

        while (true) {
            val comparison = comparator.compare(element, from.value)

            when {
                comparison < 0                              -> if (from.left  != null) { parent = from; from = from.left!!  } else return false
                comparison > 0                              -> if (from.right != null) { parent = from; from = from.right!! } else return false
                else                                        -> when {
                    from.left != null && from.right != null -> {
                        from.right!!.let {
                            from.value = minValue(it)

                            parent  = from
                            from    = it
                            element = parent!!.value
                        }
                    }
                    parent?.left  == from                   -> { parent!!.left  = from.left ?: from.right; return true }
                    parent?.right == from                   -> { parent!!.right = from.left ?: from.right; return true }
                    else                                    -> return false
                }
            }
        }
    }

    private fun minValue(from: BstNode<E>): E {
        @Suppress("NAME_SHADOWING") var from = from

        while (true) {
            from.left?.let {
                from = it
            } ?: return from.value
        }
    }

    private fun contains(node: BstNode<E>?, element: E): Boolean {
        @Suppress("NAME_SHADOWING") var node = node

        while (true) {
            when (node) {
                null -> return false
                else -> {
                    val comparison = comparator.compare(element, node.value)

                    node = when {
                        comparison < 0 -> node.left
                        comparison > 0 -> node.right
                        else           -> return node.value == element
                    }
                }
            }
        }
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
        public operator fun <T: Comparable<T>> invoke(                       ): TreeSetJs<T> = TreeSetJs { a, b -> a.compareTo(b) }
        public operator fun <T: Comparable<T>> invoke(elements: Collection<T>): TreeSetJs<T> = TreeSetJs({ a, b -> a.compareTo(b) }, elements)
    }
}