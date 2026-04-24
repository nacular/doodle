package io.nacular.doodle.layout

import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.core.container
import io.nacular.doodle.core.forceSize
import io.nacular.doodle.core.view
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.lerp
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 4/23/26.
 */
class InteractiveLayoutTests {
    @Test fun `basic layout works`() {
        container {
            + view {}
            + view {}

            forceSize(Size(100))

            val startFirstBounds  = { size: Size -> Rectangle(width = size.width / 2, height = size.height) }
            val startSecondBounds = { size: Size -> startFirstBounds(size).run { at(x = right) } }
            val endFirstBounds    = { size: Size -> Rectangle(width = size.width, height = size.height / 2) }
            val endSecondBounds   = { size: Size -> endFirstBounds(size).run { at(y = bottom) } }

            val layout = InteractiveLayout(
                start = simpleLayout { views,_,current,_,_ ->
                    val (first, second) = views.toList()

                    first.bounds  = startFirstBounds (current)
                    second.bounds = startSecondBounds(current)

                    current
                },
                end = simpleLayout { views,_,current,_,_ ->
                    val (first, second) = views.toList()

                    first.bounds  = endFirstBounds (current)
                    second.bounds = endSecondBounds(current)

                    current
                },
            ).also { this.layout = it }

            listOf(0, 0.25, 0.5, 0.89, 1.0).forEach { progress ->
                layout.progress = progress.toFloat()
                doLayout_()

                expect(lerp(startFirstBounds(size), endFirstBounds(size), layout.progress), "progress: ${layout.progress}") {
                    children[0].bounds
                }

                expect(lerp(startSecondBounds(size), endSecondBounds(size), layout.progress), "progress: ${layout.progress}") {
                    children[1].bounds
                }
            }
        }
    }

    @Test fun `layout with insets works`() {
        container {
            + view {}
            + view {}

            insets = Insets(10.0)
            forceSize(Size(100))

            val startFirstBounds  = { size: Size -> Rectangle(x = insets.left, width = (size.width - insets.run { left + right }) / 2, height = size.height - insets.run { top + bottom }) }
            val startSecondBounds = { size: Size -> startFirstBounds(size).run { at(x = right) } }
            val endFirstBounds    = { size: Size -> Rectangle(y = insets.top, width = size.width - insets.run { left + right }, height = (size.height - insets.run { top + bottom}) / 2) }
            val endSecondBounds   = { size: Size -> endFirstBounds(size).run { at(y = bottom) } }

            val layout = InteractiveLayout(
                start = simpleLayout { views,_,current,_,_ ->
                    val (first, second) = views.toList()

                    first.bounds  = startFirstBounds (current)
                    second.bounds = startSecondBounds(current)

                    current
                },
                end = simpleLayout { views,_,current,_,_ ->
                    val (first, second) = views.toList()

                    first.bounds  = endFirstBounds (current)
                    second.bounds = endSecondBounds(current)

                    current
                },
            ).also { this.layout = it }

            listOf(0, 0.25, 0.5, 0.89, 1.0).forEach { progress ->
                layout.progress = progress.toFloat()
                doLayout_()

                expect(lerp(startFirstBounds(size), endFirstBounds(size), layout.progress), "progress: ${layout.progress}") {
                    children[0].bounds
                }

                expect(lerp(startSecondBounds(size), endSecondBounds(size), layout.progress), "progress: ${layout.progress}") {
                    children[1].bounds
                }
            }
        }
    }
}