package com.nectar.doodle.layout

import com.nectar.doodle.core.PositionableContainer
import com.nectar.doodle.geometry.Size
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test

/**
 * Created by Nicholas Eddy on 1/23/20.
 */
class TileLayoutTests {
    @Test fun `empty container works`() {
        val container = mockk<PositionableContainer>(relaxed = true).apply {
            every { insets   } returns Insets(10.0)
            every { size     } returns Size(1427, 10)
            every { width    } returns size.width
            every { height   } returns size.height
            every { children } returns listOf(mockk(relaxed = true))
        }

        TileLayout().layout(container)
    }
}