package io.nacular.doodle.utils.diff

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 8/26/22.
 */
class DiffTests {
    @Test fun `common prefix`() {
        expect(0) { getCommonPrefix("abc".toList(), "xyz".toList()) }
        expect(4) { getCommonPrefix("1234abcdef".toList(), "1234xyz".toList()) }
        expect(4) { getCommonPrefix("1234".toList(), "1234xyz".toList()) }
    }

    @Test fun `common suffix`() {
        expect(0) { getCommonSuffix("abc".toList(), "xyz".toList()) }
        expect(4) { getCommonSuffix("abcdef1234".toList(), "xyz1234".toList()) }
        expect(4) { getCommonSuffix("1234".toList(), "xyz1234".toList()) }
    }

    @Test fun `half match`() {
        val equality: (Char, Char) -> Boolean = { a, b -> a == b }

        expect(null) { getHalfMatch("1234567890".toList(), "abcdef".toList(), equality) }

        val test: (String, String) -> Array<String>? = { a, b -> getHalfMatch(a.toList(), b.toList(), equality)?.map {
            it.joinToString(separator = "")
        }?.toTypedArray() }

        assertContentEquals(arrayOf("12", "90", "a", "z", "345678"), test("1234567890", "a345678z"))
        assertContentEquals(arrayOf("a", "z", "12", "90", "345678"), test("a345678z", "1234567890"))
        assertContentEquals(
            arrayOf("12123", "123121", "a", "z", "1234123451234"),
            test("121231234123451234123121", "a1234123451234z")
        )
        assertContentEquals(
            arrayOf("", "-=-=-=-=-=", "x", "", "x-=-=-=-=-=-=-="),
            test("x-=-=-=-=-=-=-=-=-=-=-=-=", "xx-=-=-=-=-=-=-=")
        )
        assertContentEquals(
            arrayOf("-=-=-=-=-=", "", "", "y", "-=-=-=-=-=-=-=y"),
            test("-=-=-=-=-=-=-=-=-=-=-=-=y", "-=-=-=-=-=-=-=yy")
        )
    }

    @Test fun `clean up merge`() {
        // Cleanup a messy diff.
        var diffs = mutableListOf<Difference<Char>>()

        cleanupMerge(diffs)

        expect(emptyList()) { diffs }

        diffs = mutableListOf(makeDiff(Operation.Equal, "a"), makeDiff(Operation.Delete, "b"), makeDiff(Operation.Insert, "c"))
        cleanupMerge(diffs)
        expect(
            listOf(
                makeDiff(Operation.Equal, "a"),
                makeDiff(Operation.Delete, "b"),
                makeDiff(Operation.Insert, "c")
            )
        ) { diffs }

        diffs = mutableListOf(makeDiff(Operation.Equal, "a"), makeDiff(Operation.Equal, "b"), makeDiff(Operation.Equal, "c"))
        cleanupMerge(diffs)
        expect(listOf(makeDiff(Operation.Equal, "abc"))) { diffs }

        diffs = mutableListOf(makeDiff(Operation.Delete, "a"), makeDiff(Operation.Delete, "b"), makeDiff(Operation.Delete, "c"))
        cleanupMerge(diffs)
        expect(listOf(makeDiff(Operation.Delete, "abc"))) { diffs }

        diffs = mutableListOf(makeDiff(Operation.Insert, "a"), makeDiff(Operation.Insert, "b"), makeDiff(Operation.Insert, "c"))
        cleanupMerge(diffs)
        expect(listOf(makeDiff(Operation.Insert, "abc"))) { diffs }

        diffs = mutableListOf(makeDiff(Operation.Delete, "0"), makeDiff(Operation.Equal, "1"), makeDiff(Operation.Insert, "11"), makeDiff(
            Operation.Delete, "0"))
        cleanupMerge(diffs)
        expect(
            listOf(
                makeDiff(Operation.Delete, "0"),
                makeDiff(Operation.Equal, "1"),
                makeDiff(Operation.Delete, "0"),
                makeDiff(Operation.Insert, "11")
            )
        ) { diffs }

        diffs = mutableListOf(makeDiff(Operation.Delete, "a"), makeDiff(Operation.Insert, "b"), makeDiff(Operation.Delete, "c"), makeDiff(
            Operation.Insert, "d"), makeDiff(Operation.Equal, "e"), makeDiff(Operation.Equal, "f"))
        cleanupMerge(diffs)
        expect(
            listOf(
                makeDiff(Operation.Delete, "ac"),
                makeDiff(Operation.Insert, "bd"),
                makeDiff(Operation.Equal, "ef")
            )
        ) { diffs }

        diffs = mutableListOf(makeDiff(Operation.Delete, "a"), makeDiff(Operation.Insert, "abc"), makeDiff(Operation.Delete, "dc"))
        cleanupMerge(diffs)
        expect(
            listOf(
                makeDiff(Operation.Equal, "a"),
                makeDiff(Operation.Delete, "dc"),
                makeDiff(Operation.Insert, "bc")
            )
        ) { diffs }

        diffs = mutableListOf(makeDiff(Operation.Equal, "a"), makeDiff(Operation.Insert, "ba"), makeDiff(Operation.Equal, "c"))
        cleanupMerge(diffs)
        expect(listOf(makeDiff(Operation.Insert, "ab"), makeDiff(Operation.Equal, "ac"))) { diffs }

        diffs = mutableListOf(makeDiff(Operation.Equal, "c"), makeDiff(Operation.Insert, "ab"), makeDiff(Operation.Equal, "a"))
        cleanupMerge(diffs)
        expect(listOf(makeDiff(Operation.Equal, "ca"), makeDiff(Operation.Insert, "ba"))) { diffs }

        diffs = mutableListOf(makeDiff(Operation.Equal, "a"), makeDiff(Operation.Delete, "b"), makeDiff(Operation.Equal, "c"), makeDiff(
            Operation.Delete, "ac"), makeDiff(Operation.Equal, "x"))
        cleanupMerge(diffs)
        expect(listOf(makeDiff(Operation.Delete, "abc"), makeDiff(Operation.Equal, "acx"))) { diffs }

        diffs = mutableListOf(makeDiff(Operation.Equal, "x"), makeDiff(Operation.Delete, "ca"), makeDiff(Operation.Equal, "c"), makeDiff(
            Operation.Delete, "b"), makeDiff(Operation.Equal, "a"))
        cleanupMerge(diffs)
        expect(listOf(makeDiff(Operation.Equal, "xca"), makeDiff(Operation.Delete, "cba"))) { diffs }
    }

    @Test fun `path test`() {
        // First, check footprints are different.
        assertTrue("diff_footprint:") { getFootprint(1, 10) != getFootprint(10, 1) }

        var vMap = mutableListOf(
            setOf(getFootprint(0, 0)),
            setOf(getFootprint(0, 1), getFootprint(1, 0)),
            setOf(getFootprint(0, 2), getFootprint(2, 0), getFootprint(2, 2)),
            setOf(getFootprint(0, 3), getFootprint(2, 3), getFootprint(3, 0), getFootprint(4, 3)),
            setOf(getFootprint(0, 4), getFootprint(2, 4), getFootprint(4, 0), getFootprint(4, 4), getFootprint(5, 3)),
            setOf(
                getFootprint(0, 5),
                getFootprint(2, 5),
                getFootprint(4, 5),
                getFootprint(5, 0),
                getFootprint(6, 3),
                getFootprint(6, 5)
            ),
            setOf(getFootprint(0, 6), getFootprint(2, 6), getFootprint(4, 6), getFootprint(6, 6), getFootprint(7, 5)),
        )

        var diffs = listOf(
            makeDiff(Operation.Insert, "W"),
            makeDiff(Operation.Delete, "A"),
            makeDiff(Operation.Equal,  "1"),
            makeDiff(Operation.Delete, "B"),
            makeDiff(Operation.Equal,  "2"),
            makeDiff(Operation.Insert, "X"),
            makeDiff(Operation.Delete, "C"),
            makeDiff(Operation.Equal,  "3"),
            makeDiff(Operation.Delete, "D")
        )

        expect(diffs, "diffPath1: Single letters.") { diffPath1(vMap, "A1B2C3D".toList(), "W12X3".toList()) }

        // Trace a path from front to back.
        vMap.removeAt(vMap.size - 1)
        diffs = listOf(
            makeDiff(Operation.Equal,  "4"),
            makeDiff(Operation.Delete, "E"),
            makeDiff(Operation.Insert, "Y"),
            makeDiff(Operation.Equal,  "5"),
            makeDiff(Operation.Delete, "F"),
            makeDiff(Operation.Equal,  "6"),
            makeDiff(Operation.Delete, "G"),
            makeDiff(Operation.Insert, "Z")
        )
        expect(diffs, "diffPath2: Single letters.") { diffPath2(vMap, "4E5F6G".toList(), "4Y56Z".toList()) }

        // Double letters.
        // Trace a path from back to front.
        vMap = mutableListOf(
            setOf(getFootprint(0, 0)),
            setOf(getFootprint(0, 1), getFootprint(1, 0)),
            setOf(getFootprint(0, 2), getFootprint(1, 1), getFootprint(2, 0)),
            setOf(getFootprint(0, 3), getFootprint(1, 2), getFootprint(2, 1), getFootprint(3, 0)),
            setOf(getFootprint(0, 4), getFootprint(1, 3), getFootprint(3, 1), getFootprint(4, 0), getFootprint(4, 4))
        )

        diffs = listOf(
            makeDiff(Operation.Insert, "WX"),
            makeDiff(Operation.Delete, "AB"),
            makeDiff(Operation.Equal,  "12")
        )

        expect(diffs, "diff_path1: Double letters.") { diffPath1(vMap, "AB12".toList(), "WX12".toList()) }

        // Trace a path from front to back.
        vMap = mutableListOf(
            setOf(getFootprint(0, 0)),
            setOf(getFootprint(0, 1), getFootprint(1, 0)),
            setOf(getFootprint(1, 1), getFootprint(2, 0), getFootprint(2, 4)),
            setOf(getFootprint(2, 1), getFootprint(2, 5), getFootprint(3, 0), getFootprint(3, 4)),
            setOf(getFootprint(2, 6), getFootprint(3, 5), getFootprint(4, 4))
        )

        diffs = listOf(
            makeDiff(Operation.Delete, "CD"),
            makeDiff(Operation.Equal,  "34"),
            makeDiff(Operation.Insert, "YZ")
        )

        expect(diffs, "diffPath2: Double letters.") { diffPath2(vMap, "CD34".toList(), "34YZ".toList()) }
    }

    @Test
    fun compare() {
        var threshold = 32

        val compare: (String, String) -> Iterable<Difference<Char>> = { x, y ->
            compare(
                x.toList(),
                y.toList(),
                threshold
            )
        }

        expect(Differences(listOf()), "Compare: Null case.") { compare("abc", "abc") }
        expect(
            Differences(
                listOf(
                    makeDiff(Operation.Equal, "ab"),
                    makeDiff(Operation.Insert, "123"),
                    makeDiff(Operation.Equal, "c")
                )
            ), "Compare: Simple Insertion."
        ) { compare("abc", "ab123c") }
        expect(
            Differences(
                listOf(
                    makeDiff(Operation.Equal, "a"),
                    makeDiff(Operation.Delete, "123"),
                    makeDiff(Operation.Equal, "bc")
                )
            ), "Compare: Simple deletion."
        ) { compare("a123bc", "abc") }
        expect(
            Differences(
                listOf(
                    makeDiff(Operation.Equal, "a"),
                    makeDiff(Operation.Insert, "123"),
                    makeDiff(Operation.Equal, "b"),
                    makeDiff(Operation.Insert, "456"),
                    makeDiff(Operation.Equal, "c")
                )
            ), "Compare: Two insertions."
        ) { compare("abc", "a123b456c") }
        expect(
            Differences(
                listOf(
                    makeDiff(Operation.Equal, "a"),
                    makeDiff(Operation.Delete, "123"),
                    makeDiff(Operation.Equal, "b"),
                    makeDiff(Operation.Delete, "456"),
                    makeDiff(Operation.Equal, "c")
                )
            ), "Compare: Two deletions."
        ) { compare("a123b456c", "abc") }
        expect(
            Differences(listOf(makeDiff(Operation.Delete, "a"), makeDiff(Operation.Insert, "b"))),
            "Compare: Simple case #1."
        ) { compare("a", "b") }
        expect(
            Differences(
                listOf(
                    makeDiff(Operation.Delete, "Apple"),
                    makeDiff(Operation.Insert, "Banana"),
                    makeDiff(Operation.Equal, "s are a"),
                    makeDiff(Operation.Insert, "lso"),
                    makeDiff(Operation.Equal, " fruit.")
                )
            ), "Compare: Simple case #2."
        ) { compare("Apples are a fruit.", "Bananas are also fruit.") }
        expect(
            Differences(
                listOf(
                    makeDiff(Operation.Delete, "a"),
                    makeDiff(Operation.Insert, "\u0680"),
                    makeDiff(Operation.Equal, "x"),
                    makeDiff(Operation.Delete, "\t"),
                    makeDiff(Operation.Insert, Char(0).toString())
                )
            ), "Compare: Simple case #3."
        ) { compare("ax\t", "\u0680x" + Char(0).toString()) }
        expect(
            Differences(
                listOf(
                    makeDiff(Operation.Delete, "1"),
                    makeDiff(Operation.Equal, "a"),
                    makeDiff(Operation.Delete, "y"),
                    makeDiff(Operation.Equal, "b"),
                    makeDiff(Operation.Delete, "2"),
                    makeDiff(Operation.Insert, "xab")
                )
            ), "Compare: Overlap #1."
        ) { compare("1ayb2", "abxab") }
        expect(
            Differences(
                listOf(
                    makeDiff(Operation.Insert, "xaxcx"),
                    makeDiff(Operation.Equal, "abc"),
                    makeDiff(Operation.Delete, "y")
                )
            ), "Compare: Overlap #2."
        ) { compare("abcy", "xaxcxabc") }
        expect(
            Differences(
                listOf(
                    makeDiff(Operation.Equal, "a"),
                    makeDiff(Operation.Insert, "c"),
                    makeDiff(Operation.Equal, "b"),
                    makeDiff(Operation.Delete, "c")
                )
            ), "Compare: Simple Move."
        ) { compare("abc", "acb") }

        // Sub-optimal double-ended diff.
//        threshold = 2
//        expect(listOf(makeDiff(Insert, "x"), makeDiff(Equal, "a"), makeDiff(Delete, "b"), makeDiff(Insert, "x"), makeDiff(Equal, "c"), makeDiff(Delete, "y"), makeDiff(Insert, "xabc")), "Compare: Overlap #3.") { compare("abcy", "xaxcxabc") }
    }

    @Test fun `computes simple forward move`() {
        val differences = compare("ABCDE".toList(), "ACDBE".toList())

        expect(Differences(listOf(Equal('A'), Delete('B'), Equal('C', 'D'), Insert('B'), Equal('E')))) { differences }
        expect(
            Differences(
                listOf(
                    Equal('A'),
                    Delete('B').apply { setDestination('B', 3) },
                    Equal('C', 'D'),
                    Insert('B').apply { setOrigin('B', 1) },
                    Equal('E'),
                )
            )
        ) { differences.computeMoves() }
    }

    @Test fun `computes forward moves`() {
        val differences = compare("ADBEC".toList(), "ABCDE".toList())

        expect(
            Differences(
                listOf(
                    Equal('A'),
                    Insert('B', 'C'),
                    Equal('D'),
                    Delete('B'),
                    Equal('E'),
                    Delete('C')
                )
            )
        ) { differences }
        expect(
            Differences(
                listOf(
                    Equal('A'),
                    Insert('B', 'C').apply { setOrigin('B', 2); setOrigin('C', 4) },
                    Equal('D'),
                    Delete('B').apply { setDestination('B', 1) },
                    Equal('E'),
                    Delete('C').apply { setDestination('C', 2) },
                )
            )
        ) { differences.computeMoves() }
    }

    @Test fun `computes simple backward move`() {
        val differences = compare("ACDBE".toList(), "ABCDE".toList())

        expect(Differences(listOf(Equal('A'), Insert('B'), Equal('C', 'D'), Delete('B'), Equal('E')))) { differences }
        expect(
            Differences(
                listOf(
                    Equal('A'),
                    Insert('B').apply { setOrigin('B', 3) },
                    Equal('C', 'D'),
                    Delete('B').apply { setDestination('B', 1) },
                    Equal('E'),
                )
            )
        ) { differences.computeMoves() }
    }

    @Test fun `computes backward moves`() {
        val differences = compare("ABCDE".toList(), "ADBEC".toList())

        expect(
            Differences(
                listOf(
                    Equal('A'),
                    Insert('D'),
                    Equal('B'),
                    Insert('E'),
                    Equal('C'),
                    Delete('D', 'E')
                )
            )
        ) { differences }
        expect(
            Differences(
                listOf(
                    Equal('A'),
                    Insert('D').apply { setOrigin('D', 3) },
                    Equal('B'),
                    Insert('E').apply { setOrigin('E', 4) },
                    Equal('C'),
                    Delete('D', 'E').apply { setDestination('D', 1); setDestination('E', 3) },
                )
            )
        ) { differences.computeMoves() }
    }

    @Test fun `computes middle unchanged`() {
        val differences = compare("010".toList(), "111".toList())

        expect(Differences(listOf(Delete('0'), Equal('1'), Delete('0'), Insert('1', '1')))) { differences }
    }

    private fun makeDiff(operation: Operation, string: String) = when(operation) {
        Operation.Equal -> Equal(string.toList())
        Operation.Delete -> Delete(string.toList())
        Operation.Insert -> Insert(string.toList())
    }
}