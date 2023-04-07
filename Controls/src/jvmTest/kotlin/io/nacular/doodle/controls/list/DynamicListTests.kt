package io.nacular.doodle.controls.list

import io.nacular.doodle.controls.MultiSelectionModel
import io.nacular.doodle.controls.SimpleMutableListModel
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 9/28/22.
 */
class DynamicListTests {
    @Test fun `updates selection when items added`() {
        val model          = SimpleMutableListModel(listOf("Hello", "World", "This is", "a Test"))
        val selectionModel = MultiSelectionModel<Int>()

        val list = DynamicList(model, selectionModel = selectionModel)

        list.setSelection(setOf(3,2,0,1))

        expect((0 until list.numItems).toList()) { list.selection.sorted() }

        model.add(1, "New" )
        model.add(3, "Item")

        expect(setOf(0, 2, 4, 5)) { list.selection }
    }

    @Test fun `updates selection when items removed`() {
        val model          = SimpleMutableListModel(listOf("Hello", "World", "This is", "a Test"))
        val selectionModel = MultiSelectionModel<Int>()

        val list = DynamicList(model, selectionModel = selectionModel)

        list.setSelection(setOf(3,2,0,1))

        expect((0 until list.numItems).toList()) { list.selection.sorted() }

        model.remove("World"  )
        model.remove("This is")

        expect(setOf(0, 1)) { list.selection }
    }

    @Test fun `updates selection when items added and removed`() {
        val model          = SimpleMutableListModel(listOf("Hello", "World", "This is", "a Test"))
        val selectionModel = MultiSelectionModel<Int>()

        val list = DynamicList(model, selectionModel = selectionModel)

        list.setSelection(setOf(3,2,0,1))

        expect((0 until list.numItems).toList()) { list.selection.sorted() }

        model.remove("World"  )
        model.remove("This is")
        model.add   (1, "New" )
        model.add   (2, "Item")

        expect(setOf(0, 3)) { list.selection }
    }

    @Test fun `updates selection when items added to empty`() {
        val model          = SimpleMutableListModel<String>()
        val selectionModel = MultiSelectionModel<Int>()

        val list = DynamicList(model, selectionModel = selectionModel)

        list.setSelection(setOf(0)) //setOf(3,2,0,1))

        model.addAll(listOf("Hello", "World", "This is", "a Test"))

//        expect((0 until list.numItems).toList()) { list.selection.sorted() }

        expect(setOf(0)) { list.selection }
    }
}