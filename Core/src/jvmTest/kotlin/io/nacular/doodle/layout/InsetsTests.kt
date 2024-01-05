package io.nacular.doodle.layout

import io.nacular.doodle.layout.Insets.Companion.None
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 9/23/20.
 */
class InsetsTests {
    @Test fun `none works`() {
        None.apply {
            expect(0.0) { top    }
            expect(0.0) { left   }
            expect(0.0) { right  }
            expect(0.0) { bottom }
        }
    }

    @Test fun `equals works`() {
        listOf(
                None                       to Insets(                  ) to true,
                None                       to Insets(1.0               ) to false,
                Insets(3.0, 4.0, 1.0, 2.0) to Insets(3.0, 4.0, 1.0, 2.0) to true
        ).forEach { (data, expected) ->
            expect(expected, "${data.first           } == ${data.second           }") { data.first            == data.second            }
            expect(expected, "${data.first.hashCode()} == ${data.second.hashCode()}") { data.first.hashCode() == data.second.hashCode() }
        }
    }
}