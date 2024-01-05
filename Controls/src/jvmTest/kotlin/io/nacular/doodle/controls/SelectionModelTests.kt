package io.nacular.doodle.controls

/**
 * Created by Nicholas Eddy on 9/14/22.
 */
class SelectionModelTests {
    @kotlin.test.Test fun `single selection can toggle`() {
        val selectionModel = SingleItemSelectionModel<Int>()

        selectionModel.add(10)

        kotlin.test.expect(listOf(10)) { selectionModel.toList() }

        selectionModel.add(11)

        kotlin.test.expect(listOf(11)) { selectionModel.toList() }

        selectionModel.toggle(listOf(11))

        kotlin.test.expect(emptyList()) { selectionModel.toList() }
    }
}