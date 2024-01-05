package io.nacular.doodle.utils

import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 7/18/20.
 */
class PathTests {
    @Test fun `top works`() {
        val paths = listOf(
                listOf(0, 0, 2, 5),
                listOf(0),
                listOf()
        )

        paths.forEach {
            expect(it.firstOrNull()) { Path(it).top }
        }
    }

    @Test fun `bottom works`() {
        val paths = listOf(
                listOf(0, 0, 2, 5),
                listOf(0),
                listOf()
        )

        paths.forEach {
            expect(it.lastOrNull()) { Path(it).bottom }
        }
    }

    @Test fun `parent works`() {
        val paths = listOf(
                listOf(0, 0, 2, 5),
                listOf(0),
                listOf()
        )

        paths.forEach {
            val parent = Path(it).parent

            if (it.isEmpty()) {
                expect(null) { parent }
            } else {
                expect(Path(it.dropLast(1))) { parent }
            }
        }
    }

    @Test fun `depth works`() {
        val paths = listOf(
                listOf(0, 0, 2, 5),
                listOf(0),
                listOf()
        )

        paths.forEach {
            expect(it.size) { Path(it).depth }
        }
    }

    @Test fun `overlapping root works`() {
        data class Inputs<T>(val first: List<T>, val second: List<T>, val expect: List<T>)

        val paths = listOf(
                Inputs(listOf(0, 1),       listOf(0, 1, 2, 3, 4, 5), expect = listOf(0, 1)),
                Inputs(listOf(0, 1, 9, 8), listOf(0, 1, 2, 3, 4, 5), expect = listOf(0, 1))
        )

        paths.forEach {
            expect(Path(it.expect)) { Path(it.second).overlappingRoot(Path(it.first )) }
            expect(Path(it.expect)) { Path(it.first ).overlappingRoot(Path(it.second)) }
        }
    }

    @Test fun `non-overlapping stem works`() {
        data class Inputs<T>(val first: List<T>, val second: List<T>, val expect: List<T>)

        val paths = listOf(
                Inputs(listOf(0, 1),       listOf(0, 1, 2, 3, 4, 5), expect = listOf(2, 3, 4, 5)),
                Inputs(listOf(0, 1, 9, 8), listOf(0, 1, 2, 3, 4, 5), expect = listOf(2, 3, 4, 5))
        )

        paths.forEach {
            expect(it.expect) { Path(it.second).nonOverlappingStem(Path(it.first )) }
        }
    }

    @Test fun `root is always ancestor`() {
        val paths = listOf(
            listOf(0, 0, 2, 5),
            listOf(0)
        )

        paths.forEach {
            expect(true) { Path<Int>() ancestorOf Path(it) }
        }
    }
}