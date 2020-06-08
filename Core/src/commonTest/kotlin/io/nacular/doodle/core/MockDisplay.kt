package io.nacular.doodle.core

import io.nacular.doodle.utils.ObservableList
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

/**
 * Created by Nicholas Eddy on 3/14/20.
 */
fun display(): Display = mockk<Display>().apply {
    every { children } returns mockk<ObservableList<View>>().apply {
        val view = slot<View>()

        every { add(capture(view)) } answers {
            view.captured.addedToDisplay(mockk(), mockk(), mockk())

            true
        }
    }
}