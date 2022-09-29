package io.nacular.doodle.controls.panels

import io.mockk.mockk
import io.nacular.doodle.controls.ItemVisualizer
import org.junit.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 9/29/22.
 */
class TabbedPanelsTests {
    @Test fun `updates selection on add`() {
        val itemVisualizer = mockk<ItemVisualizer<Int, Any>>()
        val tabVisualizer  = mockk<ItemVisualizer<Int, Any>>()

        val panel = TabbedPanel(itemVisualizer, tabVisualizer, 0, 1, 2, 3, 4)

        panel.selection = 2
        expect(2) { panel.selectedItem }

        panel.add(at = 0, 10)
        panel.add(at = 2, 10)

        expect(4) { panel.selection    }
        expect(2) { panel.selectedItem }
    }

    @Test fun `updates selection on remove`() {
        val itemVisualizer = mockk<ItemVisualizer<Int, Any>>()
        val tabVisualizer  = mockk<ItemVisualizer<Int, Any>>()

        val panel = TabbedPanel(itemVisualizer, tabVisualizer, 0, 1, 2, 3, 4)

        panel.selection = 2
        expect(2) { panel.selectedItem }

        panel.remove(at = 0)
        panel.remove(at = 3)

        expect(1) { panel.selection    }
        expect(2) { panel.selectedItem }
    }

    @Test fun `updates selection on clear`() {
        val itemVisualizer = mockk<ItemVisualizer<Int, Any>>()
        val tabVisualizer  = mockk<ItemVisualizer<Int, Any>>()

        val panel = TabbedPanel(itemVisualizer, tabVisualizer, 0, 1, 2, 3, 4)

        panel.selection = 2
        expect(2) { panel.selectedItem }

        panel.clear()

        expect(null) { panel.selection    }
        expect(null) { panel.selectedItem }
    }
}