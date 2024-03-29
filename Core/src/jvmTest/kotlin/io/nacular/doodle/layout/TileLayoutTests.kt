package io.nacular.doodle.layout

import io.mockk.every
import io.mockk.mockk
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.geometry.Size
import kotlin.test.Ignore
import kotlin.test.Test

/**
 * Created by Nicholas Eddy on 1/23/20.
 */
class TileLayoutTests {
    @Ignore() @Test() fun `empty container works`() {
        val container = mockk<PositionableContainer>().apply {
            every { insets   } returns Insets(10.0)
            every { size     } returns Size(1427, 10)
            every { width    } returns size.width
            every { height   } returns size.height
            every { children } returns listOf(mockk())
        }

        TileLayout().layout(container)
    }
}