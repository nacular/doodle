package io.nacular.doodle.layout

import io.mockk.mockk
import io.nacular.doodle.geometry.Size
import kotlin.test.Ignore
import kotlin.test.Test

/**
 * Created by Nicholas Eddy on 1/23/20.
 */
class TileLayoutTests {
    @Ignore() @Test() fun `empty container works`() {
        TileLayout().layout(sequenceOf(mockk()), Size.Empty, Size(1427, 10), Size.Infinite)
    }
}