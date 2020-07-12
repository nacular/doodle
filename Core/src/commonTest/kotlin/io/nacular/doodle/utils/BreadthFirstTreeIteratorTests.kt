package io.nacular.doodle.utils

import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 3/17/18.
 */
private data class N<T>(override val value: T, override var children: Sequence<Node<T>> = emptySequence()): Node<T>

class BreadthFirstTreeIteratorTests {
    @Test @JsName("singleNodeWorks")
    fun `single node works`() {
        val iter = BreadthFirstTreeIterator(N(234))

        expect(true ) { iter.hasNext() }
        expect(234  ) { iter.next   () }
        expect(false) { iter.hasNext() }
    }

    @Test @JsName("singleLevelWorks")
    fun `single level works`() {
        val iter = BreadthFirstTreeIterator(N(1, sequenceOf(N(2), N(3), N(4))))

        expect(true ) { iter.hasNext() }
        expect(1    ) { iter.next   () }
        expect(2    ) { iter.next   () }
        expect(3    ) { iter.next   () }
        expect(4    ) { iter.next   () }
        expect(false) { iter.hasNext() }
    }

    @Test @JsName("multiLevelWorks")
    fun `multi-level works`() {
        val iter = BreadthFirstTreeIterator(N(1, sequenceOf(N(2, sequenceOf(N(5))), N(3), N(4, sequenceOf(N(6))))))

        expect(true ) { iter.hasNext() }
        expect(1    ) { iter.next   () }
        expect(2    ) { iter.next   () }
        expect(3    ) { iter.next   () }
        expect(4    ) { iter.next   () }
        expect(5    ) { iter.next   () }
        expect(6    ) { iter.next   () }
        expect(false) { iter.hasNext() }
    }
}