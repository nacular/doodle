package io.nacular.doodle.controls.tree

import io.mockk.mockk
import io.mockk.verify
import io.nacular.doodle.utils.Path
import org.junit.Test
import kotlin.Result.Companion.success
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 8/2/22.
 */
class SimpleMutableTreeModelTests {
    @Test fun `add works`() {
        val model          = SimpleMutableTreeModel(MutableTreeNode("root"))
        val changeListener = mockk<ModelObserver<String>>(relaxed = true)

        model.changed += changeListener

        val path = Path<Int>() + 0

        model.add(path, "item")

        verify(exactly = 1) { changeListener(model, emptyMap(), mapOf(path to "item"), emptyMap()) }

        expect(success("item")) { model[path] }
    }
}