package io.nacular.doodle.utils

import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 8/31/22.
 */
class RangesTests {
    @Test @JsName("overlappingPrefix") fun `overlapping prefix`() {
        expect(5 .. 7) { 5 .. 10 intersect 0 .. 7 }
    }

    @Test @JsName("overlappingMiddle") fun `overlapping middle`() {
        expect(6 .. 7) { 5 .. 10 intersect 6 .. 7 }
    }

    @Test @JsName("overlappingExact") fun `overlapping exact`() {
        expect(5 .. 10) { 5 .. 10 intersect 5 .. 10 }
    }

    @Test @JsName("overlappingPostfix") fun `overlapping postfix`() {
        expect(7 .. 10) { 5 .. 10 intersect 7 .. 77 }
    }

    @Test @JsName("overlappingPrePostfix") fun `overlapping pre and postfix`() {
        expect(5 .. 10) { 5 .. 10 intersect 0 .. 77 }
    }

    @Test @JsName("nonOverlappingPrefix") fun `non-overlapping prefix`() {
        expect(5 .. 5) { 5 .. 10 intersect 3 .. 4 }
    }

    @Test @JsName("nonOverlappingPostfix") fun `non-overlapping postfix`() {
        expect(10 .. 10) { 5 .. 10 intersect 13 .. 14 }
    }

    @Test @JsName("overlappingPrefixTrue") fun `overlapping prefix == true`() {
        expect(true) { 5 .. 10 intersects 0 .. 7 }
    }

    @Test @JsName("overlappingMiddleTrue") fun `overlapping middle == true`() {
        expect(true) { 5 .. 10 intersects 6 .. 7 }
    }

    @Test @JsName("overlappingExactTrue") fun `overlapping exact == true`() {
        expect(true) { 5 .. 10 intersects 5 .. 10 }
    }

    @Test @JsName("overlappingPostfixTrue") fun `overlapping postfix == true`() {
        expect(true) { 5 .. 10 intersects 7 .. 77 }
    }

    @Test @JsName("overlappingPrePostfixTrue") fun `overlapping pre and postfix == true`() {
        expect(true) { 5 .. 10 intersects 0 .. 77 }
    }

    @Test @JsName("nonOverlappingPrefixFalse") fun `non-overlapping prefix == false`() {
        expect(false) { 5 .. 10 intersects 3 .. 4 }
    }

    @Test @JsName("nonOverlappingPostfixFalse") fun `non-overlapping postfix == false`() {
        expect(false) { 5 .. 10 intersects 13 .. 14 }
    }


    private fun <T: Comparable<T>> expect(range: ClosedRange<T>, block: () -> ClosedRange<T>) {
        block().let {
            expect(range.start       ) { it.start }
            expect(range.endInclusive) { it.endInclusive }
        }
    }
}