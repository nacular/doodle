package io.nacular.doodle.controls.text

import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 8/30/22.
 */
class TextFieldTests {
    @Test @JsName("deleteFullSelection") fun `delete full selection`() {
        TextField("this is a test").apply {
            select(4..7)

            expect("this is a test") { text }
            expect(7) { selection.anchor }
            expect(4..7) { selection.start .. selection.end }

            deleteSelected()

            expect("this a test") { text }
            expect(4 .. 4) { selection.start .. selection.end }
        }
    }

    @Test @JsName("deletePartialSelection") fun `delete partial selection`() {
        TextField("this is a test").apply {
            select(4..7)

            expect("this is a test") { text }
            expect(7) { selection.anchor }
            expect(4..7) { selection.start .. selection.end }

            delete(1 ..  3)

            expect("ts is a test") { text }
            expect(2 .. 5) { selection.start .. selection.end }
        }
    }

    @Test @JsName("deleteEmptySelection") fun `delete empty selection`() {
        TextField("this is a test").apply {
            select(4..4)

            expect("this is a test") { text }
            expect(4) { selection.anchor }
            expect(4..4) { selection.start .. selection.end }

            deleteSelected()

            expect("this is a test") { text }
            expect(4 .. 4) { selection.start .. selection.end }
        }
    }

    @Test @JsName("deleteEntireSelectedString") fun `delete entire selected text`() {
        TextField("this is a test").apply {
            selectAll()

            expect("this is a test") { text }
            expect(text.length) { selection.anchor }
            expect(0..text.length) { selection.start .. selection.end }

            deleteSelected()

            expect("") { text }
            expect(0 .. 0) { selection.start .. selection.end }
        }
    }

    @Test @JsName("insertBeforeSelection") fun `insert before selection`() {
        TextField("this test").apply {
            select(6 .. 9)
            insert("is a ", 5)

            expect("this is a test") { text }

            expect(11 .. 14) { selection.start .. selection.end }
        }
    }

    @Test @JsName("insertInSelection") fun `insert in selection`() {
        TextField("this test").apply {
            select(5 .. 7)
            insert("is a ", 5)

            expect("this is a test") { text }

            expect(5 .. 12) { selection.start .. selection.end }
        }
    }

    @Test @JsName("insertAfterSelection") fun `insert after selection`() {
        TextField("this test").apply {
            select(0 .. 4)
            insert("is a ", 5)

            expect("this is a test") { text }

            expect(0 .. 4) { selection.start .. selection.end }
        }
    }

    @Test @JsName("pasteUpdatesSelection") fun `paste updates selection`() {
        TextField("this test").apply {
            select(0 .. 4)
            paste("have you done your")

            expect("have you done your test") { text }

            expect(0 .. 0) { selection.start .. selection.end }
        }
    }

    @Test @JsName("cutWithSelection") fun `cut with selection`() {
        TextField("this test").apply {
            select(0 .. 4)

            expect("this" ) { cut() }
            expect(" test") { text }

            expect(0 .. 0) { selection.start .. selection.end }
        }
    }

    @Test @JsName("cutWithEmptySelection") fun `cut with empty selection`() {
        TextField("this test").apply {
            select(4 .. 4)

            expect("") { cut() }
            expect("this test") { text }

            expect(4 .. 4) { selection.start .. selection.end }
        }
    }
}