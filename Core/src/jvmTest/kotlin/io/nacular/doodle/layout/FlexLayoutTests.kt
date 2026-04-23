package io.nacular.doodle.layout

import io.nacular.doodle.core.View.Companion.fixed
import io.nacular.doodle.core.container
import io.nacular.doodle.core.view
import io.nacular.doodle.geometry.Size
import kotlin.test.Test

/**
 * Created by Nicholas Eddy on 4/22/26.
 */
class FlexLayoutTests {
    @Test fun `foo`() {
        container {
            + view {}.apply { preferredSize = fixed(Size(400)) }
            + view {}
            + view {}.apply { preferredSize = fixed(Size(200)) }

            insets = Insets    (30.0          )
            layout = FlexColumn(spacing = 20.0)

            val w = 500.0
            val h = 750.0

            val expectedWidth   = w - insets.run { left + right }
            val availableHeight = h - insets.run { top + bottom }

            suggestSize(w, h)
            doLayout_()

            val bounds = map { it.bounds }
            val bottom = last().bounds.bottom

            suggestHeight(740.0)
            doLayout_()

            suggestHeight(700.0)
            doLayout_()

//            val expectedWidth   = w - insets.run { left + right }
//            val availableHeight = h - insets.run { top + bottom }
//
//            expect(listOf(
//                Rectangle(insets.left, insets.top, expectedWidth, min(availableHeight * 400.0 / h, 400.0)),
//                Rectangle(insets.left, insets.top + 20 + min(availableHeight * 400.0 / h, 400.0), expectedWidth, 700.0),
//                Rectangle(insets.left, 0.0, expectedWidth, 700.0),
//            )) {
//                map { it.bounds }
//            }
        }
    }
}