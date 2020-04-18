package com.nectar.doodle.core

import com.nectar.doodle.utils.ObservableList
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

/**
 * Created by Nicholas Eddy on 3/14/20.
 */
fun display(): Display = mockk<Display>(relaxed = true).apply {
    every { children } returns mockk<ObservableList<View>>(relaxed = true).apply {
        val view = slot<View>()

        every { add(capture(view)) } answers {
            view.captured.addedToDisplay(mockk(relaxed = true), mockk(relaxed = true))

            true
        }
    }
}