package io.nacular.doodle.controls

import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 9/14/22.
 */
class SelectionModelTests {
    @Test @JsName("singleSelectionCanToggle") fun `single selection can toggle`() {
        val selectionModel = SingleItemSelectionModel<Int>()

        selectionModel.add(10)

        expect(listOf(10)) { selectionModel.toList() }

        selectionModel.add(11)

        expect(listOf(11)) { selectionModel.toList() }

        selectionModel.toggle(listOf(11))

        expect(emptyList()) { selectionModel.toList() }
    }
}