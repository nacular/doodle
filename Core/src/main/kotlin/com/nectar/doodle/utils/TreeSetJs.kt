package com.nectar.doodle.utils

/**
 * Created by Nicholas Eddy on 4/11/18.
 */
class BstNode<E>(var value: E) {
    var left  = null as BstNode<E>?
    var right = null as BstNode<E>?
}

open class TreeSetJs<E: Comparable<E>> constructor(elements: Collection<E>): Set<E> {
    constructor(): this(emptyList<E>())

    protected var root = null as BstNode<E>?

    init {
        elements.forEach { add(it) }
    }

    override val size get() = size_

    private var size_ = 0

    override fun isEmpty() = root == null

    override fun contains(element: E) = contains(root, element)

    override fun containsAll(elements: Collection<E>) = elements.all { contains(it) }

    override fun iterator(): Iterator<E> = BstIterator()

    override fun toString(): String {
        return "[${ iterator().asSequence().joinToString(", ")}]"
    }

    protected open fun add(element: E): Boolean {
        if (root == null) {
            root = BstNode(element)
            ++size_
            return true
        } else {
            return add(root!!, element).ifTrue { ++size_ }
        }
    }

    protected open fun remove_(element: E): Boolean {
        return root?.let {
            if (it.value == element) {

                val auxRoot = BstNode<E>(it.value)

                auxRoot.left = root

                val result = remove(it, auxRoot, element)

                root = auxRoot.left

                return result

            } else {
                return remove(it, null, element)

            }

        } ?: false
    }

    private fun add(node: BstNode<E>, element: E): Boolean = when {
        node.value == element -> false
        node.value  > element -> when (node.left){
            null -> { node.left = BstNode(element); true }
            else -> add(node.left!!, element)
        }
        else -> when (node.right){
            null -> { node.right = BstNode(element); true }
            else -> add(node.right!!, element)
        }
    }

    private fun remove(from: BstNode<E>, parent: BstNode<E>?, element: E): Boolean {
        when {
            element < from.value -> return from.left?.let  { remove(it, from, element) } ?: false
            element > from.value -> return from.right?.let { remove(it, from, element) } ?: false
            else                 -> {
                if (from.left != null && from.right != null) {
                    from.right?.let {
                        from.value = minValue(it)

                        remove(it, from, from.value)
                    }

                } else if (parent?.left == from) {
                    parent.left = from.left ?: from.right

                } else if (parent?.right == from) {
                    parent.right = from.left ?: from.right

                }
                return true
            }
        }
    }

    private fun minValue(from: BstNode<E>): E = from.left?.let {
        minValue(it)
    } ?: from.value

    private fun contains(node: BstNode<E>?, element: E): Boolean = node?.let {
        when {
            node.value == element         -> true
            contains(node.left,  element) -> true
            contains(node.right, element) -> true
            else                          -> false
        }
    } ?: false

    protected inner class BstIterator: kotlin.collections.MutableIterator<E> {
        val stack by lazy {
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

        override fun hasNext() = stack.isNotEmpty()

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
}