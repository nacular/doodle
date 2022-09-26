package io.nacular.doodle.utils.diff

import io.nacular.doodle.utils.diff.Operation.*
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 8/26/22.
 */
class DiffTests {
    @Test @JsName("commonPrefix") fun `common prefix`() {
        expect(0) { getCommonPrefix("abc".toList       (), "xyz".toList    ()) }
        expect(4) { getCommonPrefix("1234abcdef".toList(), "1234xyz".toList()) }
        expect(4) { getCommonPrefix("1234".toList      (), "1234xyz".toList()) }
    }

    @Test @JsName("commonSuffix") fun `common suffix`() {
        expect(0) { getCommonSuffix("abc".toList       (), "xyz".toList    ()) }
        expect(4) { getCommonSuffix("abcdef1234".toList(), "xyz1234".toList()) }
        expect(4) { getCommonSuffix("1234".toList      (), "xyz1234".toList()) }
    }

    @Test @JsName("halfMatch") fun `half match`() {
        val equality: (Char, Char) -> Boolean = { a, b -> a == b }

        expect(null) { getHalfMatch("1234567890".toList(), "abcdef".toList(), equality) }

        val test: (String, String) -> Array<String>? = { a, b -> getHalfMatch(a.toList(), b.toList(), equality)?.map {
            it.joinToString(separator = "")
        }?.toTypedArray() }

        assertContentEquals(arrayOf("12", "90", "a", "z", "345678"), test("1234567890", "a345678z"))
        assertContentEquals(arrayOf("a", "z", "12", "90", "345678"), test("a345678z", "1234567890"))
        assertContentEquals(arrayOf("12123", "123121", "a", "z", "1234123451234"), test("121231234123451234123121", "a1234123451234z"))
        assertContentEquals(arrayOf("", "-=-=-=-=-=", "x", "", "x-=-=-=-=-=-=-="), test("x-=-=-=-=-=-=-=-=-=-=-=-=", "xx-=-=-=-=-=-=-="))
        assertContentEquals(arrayOf("-=-=-=-=-=", "", "", "y", "-=-=-=-=-=-=-=y"), test("-=-=-=-=-=-=-=-=-=-=-=-=y", "-=-=-=-=-=-=-=yy"))
    }

    @Test @JsName("cleanUpMerge") fun `clean up merge`() {
        // Cleanup a messy diff.
        var diffs = mutableListOf<Difference<Char>>()

        cleanupMerge(diffs)

        expect(emptyList()) { diffs }

        diffs = mutableListOf(makeDiff(Equal, "a"), makeDiff(Delete, "b"), makeDiff(Insert, "c"))
        cleanupMerge(diffs)
        expect(listOf(makeDiff(Equal, "a"), makeDiff(Delete, "b"), makeDiff(Insert, "c"))) { diffs }

        diffs = mutableListOf(makeDiff(Equal, "a"), makeDiff(Equal, "b"), makeDiff(Equal, "c"))
        cleanupMerge(diffs)
        expect(listOf(makeDiff(Equal, "abc"))) { diffs }

        diffs = mutableListOf(makeDiff(Delete, "a"), makeDiff(Delete, "b"), makeDiff(Delete, "c"))
        cleanupMerge(diffs)
        expect(listOf(makeDiff(Delete, "abc"))) { diffs }

        diffs = mutableListOf(makeDiff(Insert, "a"), makeDiff(Insert, "b"), makeDiff(Insert, "c"))
        cleanupMerge(diffs)
        expect(listOf(makeDiff(Insert, "abc"))) { diffs }

        diffs = mutableListOf(makeDiff(Delete, "a"), makeDiff(Insert, "b"), makeDiff(Delete, "c"), makeDiff(Insert, "d"), makeDiff(Equal, "e"), makeDiff(Equal, "f"))
        cleanupMerge(diffs)
        expect(listOf(makeDiff(Delete, "ac"), makeDiff(Insert, "bd"), makeDiff(Equal, "ef"))) { diffs }

        diffs = mutableListOf(makeDiff(Delete, "a"), makeDiff(Insert, "abc"), makeDiff(Delete, "dc"))
        cleanupMerge(diffs)
        expect(listOf(makeDiff(Equal, "a"), makeDiff(Delete, "d"), makeDiff(Insert, "b"), makeDiff(Equal, "c"))) { diffs }

        diffs = mutableListOf(makeDiff(Equal, "a"), makeDiff(Insert, "ba"), makeDiff(Equal, "c"))
        cleanupMerge(diffs)
        expect(listOf(makeDiff(Insert, "ab"), makeDiff(Equal, "ac"))) { diffs }

        diffs = mutableListOf(makeDiff(Equal, "c"), makeDiff(Insert, "ab"), makeDiff(Equal, "a"))
        cleanupMerge(diffs)
        expect(listOf(makeDiff(Equal, "ca"), makeDiff(Insert, "ba"))) { diffs }

        diffs = mutableListOf(makeDiff(Equal, "a"), makeDiff(Delete, "b"), makeDiff(Equal, "c"), makeDiff(Delete, "ac"), makeDiff(Equal, "x"))
        cleanupMerge(diffs)
        expect(listOf(makeDiff(Delete, "abc"), makeDiff(Equal, "acx"))) { diffs }

        diffs = mutableListOf(makeDiff(Equal, "x"), makeDiff(Delete, "ca"), makeDiff(Equal, "c"), makeDiff(Delete, "b"), makeDiff(Equal, "a"))
        cleanupMerge(diffs)
        expect(listOf(makeDiff(Equal, "xca"), makeDiff(Delete, "cba"))) { diffs }
    }

    @Test @JsName("pathTest") fun `path test`() {
        // First, check footprints are different.
        assertTrue("diff_footprint:") { getFootprint(1, 10) != getFootprint(10, 1) }

        var vMap = mutableListOf(
            setOf(getFootprint(0, 0)),
            setOf(getFootprint(0, 1), getFootprint(1, 0)),
            setOf(getFootprint(0, 2), getFootprint(2, 0), getFootprint(2, 2)),
            setOf(getFootprint(0, 3), getFootprint(2, 3), getFootprint(3, 0), getFootprint(4, 3)),
            setOf(getFootprint(0, 4), getFootprint(2, 4), getFootprint(4, 0), getFootprint(4, 4), getFootprint(5, 3)),
            setOf(getFootprint(0, 5), getFootprint(2, 5), getFootprint(4, 5), getFootprint(5, 0), getFootprint(6, 3), getFootprint(6, 5)),
            setOf(getFootprint(0, 6), getFootprint(2, 6), getFootprint(4, 6), getFootprint(6, 6), getFootprint(7, 5)),
        )

        var diffs = listOf(
            makeDiff(Insert, "W"),
            makeDiff(Delete, "A"),
            makeDiff(Equal,  "1"),
            makeDiff(Delete, "B"),
            makeDiff(Equal,  "2"),
            makeDiff(Insert, "X"),
            makeDiff(Delete, "C"),
            makeDiff(Equal,  "3"),
            makeDiff(Delete, "D")
        )

        expect(diffs, "diffPath1: Single letters.") { diffPath1(vMap, "A1B2C3D".toList(), "W12X3".toList()) }

        // Trace a path from front to back.
        vMap.removeAt(vMap.size - 1)
        diffs = listOf(
            makeDiff(Equal,  "4"),
            makeDiff(Delete, "E"),
            makeDiff(Insert, "Y"),
            makeDiff(Equal,  "5"),
            makeDiff(Delete, "F"),
            makeDiff(Equal,  "6"),
            makeDiff(Delete, "G"),
            makeDiff(Insert, "Z")
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
            makeDiff(Insert, "WX"),
            makeDiff(Delete, "AB"),
            makeDiff(Equal,  "12")
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
            makeDiff(Delete, "CD"),
            makeDiff(Equal,  "34"),
            makeDiff(Insert, "YZ")
        )

        expect(diffs, "diffPath2: Double letters.") { diffPath2(vMap, "CD34".toList(), "34YZ".toList()) }
    }

    @Test fun compare() {
        var threshold = 32

        val compare: (String, String) -> Iterable<Difference<Char>> = { x, y -> compare(x.toList(), y.toList(), threshold) }

        expect(Differences(listOf(                                                                                                                                                                          )), "Compare: Null case."       ) { compare("abc",                 "abc"                         ) }
        expect(Differences(listOf(makeDiff(Equal,  "ab"   ), makeDiff(Insert, "123"   ), makeDiff(Equal,  "c"      )                                                                                        )), "Compare: Simple Insertion.") { compare("abc",                 "ab123c"                      ) }
        expect(Differences(listOf(makeDiff(Equal,  "a"    ), makeDiff(Delete, "123"   ), makeDiff(Equal,  "bc"     )                                                                                        )), "Compare: Simple deletion." ) { compare("a123bc",              "abc"                         ) }
        expect(Differences(listOf(makeDiff(Equal,  "a"    ), makeDiff(Insert, "123"   ), makeDiff(Equal,  "b"      ), makeDiff(Insert, "456"), makeDiff(Equal,  "c"               )                         )), "Compare: Two insertions."  ) { compare("abc",                 "a123b456c"                   ) }
        expect(Differences(listOf(makeDiff(Equal,  "a"    ), makeDiff(Delete, "123"   ), makeDiff(Equal,  "b"      ), makeDiff(Delete, "456"), makeDiff(Equal,  "c"               )                         )), "Compare: Two deletions."   ) { compare("a123b456c",           "abc"                         ) }
        expect(Differences(listOf(makeDiff(Delete, "a"    ), makeDiff(Insert, "b"     )                                                                                                                     )), "Compare: Simple case #1."  ) { compare("a",                   "b"                           ) }
        expect(Differences(listOf(makeDiff(Delete, "Apple"), makeDiff(Insert, "Banana"), makeDiff(Equal,  "s are a"), makeDiff(Insert, "lso"), makeDiff(Equal,  " fruit."         )                         )), "Compare: Simple case #2."  ) { compare("Apples are a fruit.", "Bananas are also fruit."     ) }
        expect(Differences(listOf(makeDiff(Delete, "a"    ), makeDiff(Insert, "\u0680"), makeDiff(Equal,  "x"      ), makeDiff(Delete, "\t" ), makeDiff(Insert, Char(0).toString())                         )), "Compare: Simple case #3."  ) { compare("ax\t",                "\u0680x" + Char(0).toString()) }
        expect(Differences(listOf(makeDiff(Delete, "1"    ), makeDiff(Equal,  "a"     ), makeDiff(Delete, "y"      ), makeDiff(Equal,  "b"  ), makeDiff(Delete, "2"               ), makeDiff(Insert, "xab"))), "Compare: Overlap #1."      ) { compare("1ayb2",               "abxab"                       ) }
        expect(Differences(listOf(makeDiff(Insert, "xaxcx"), makeDiff(Equal,  "abc"   ), makeDiff(Delete, "y"      )                                                                                        )), "Compare: Overlap #2."      ) { compare("abcy",                "xaxcxabc"                    ) }
        expect(Differences(listOf(makeDiff(Equal,  "a"    ), makeDiff(Insert, "c"     ), makeDiff(Equal,  "b"      ), makeDiff(Delete, "c"  )                                                               )), "Compare: Simple Move."     ) { compare("abc",                 "acb"                      ) }

        // Sub-optimal double-ended diff.
//        threshold = 2
//        expect(listOf(makeDiff(Insert, "x"), makeDiff(Equal, "a"), makeDiff(Delete, "b"), makeDiff(Insert, "x"), makeDiff(Equal, "c"), makeDiff(Delete, "y"), makeDiff(Insert, "xabc")), "Compare: Overlap #3.") { compare("abcy", "xaxcxabc") }
    }

    private fun makeDiff(operation: Operation, string: String) = when(operation) {
        Equal  -> Equal (string.toList())
        Delete -> Delete(string.toList())
        Insert -> Insert(string.toList())
    }
}