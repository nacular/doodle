package io.nacular.doodle.datatransport

import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 4/23/20.
 */
class DataBundleTests {
    @Test @JsName("refDoubleWorks")
    fun `ref Double works`() {
        expect(true) { ReferenceType<Double>() in refBundle(5.6) }
    }

    @Test @JsName("stringRepresentationValid")
    fun `string representation valid`() {

        mapOf(
                Json                    to "application/json",
                UriList                 to "text/uri-list",
                PlainText               to "text/plain",
                PlainText("utf-8")      to "text/plain;charset=utf-8",
                ReferenceType<Double>() to "application/doodle-reference-Double"
        ).forEach { (mime, string) ->
            expect(string) { mime.toString() }
        }
    }
}