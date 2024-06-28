package io.nacular.doodle.core

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.nacular.doodle.core.ContentDirection.LeftRight
import io.nacular.doodle.core.ContentDirection.RightLeft
import io.nacular.doodle.core.LookupResult.Found
import io.nacular.doodle.core.LookupResult.Ignored
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.ObservableList
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.test.Ignore
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 9/17/21.
 */
class DisplayTests {
    @Test fun `width + height works`() {
        val size    = Size(100, 97)
        val display = mockk<Display>().apply {
            every { this@apply.size } returns size
        }

        expect(size.width ) { display.width  }
        expect(size.height) { display.height }
    }

    @Test fun `fill works`() {
        val display = mockk<Display>()

        display.fill(Red)

        verify (exactly = 1) { display.fill(Red.paint) }
    }

    @Test fun `center works`() {
        val size    = Size(100, 97)
        val display = mockk<Display>().apply {
            every { this@apply.size } returns size
        }

        expect(Point(size.width / 2, size.height / 2)) { display.center  }
    }

    @Test fun `+= view works`() {
        val child    = mockk<Container>()
        val children = mockk<ObservableList<View>>()
        val display  = display(children)

        display += child

        verify (exactly = 1) { children += child }
    }

    @Test fun `-= view works`() {
        val child    = mockk<Container>()
        val children = mockk<ObservableList<View>>()
        val display  = display(children)

        display -= child

        verify (exactly = 1) { children -= child }
    }

    @Test fun `+= collection works`() {
        val child1   = mockk<Container>()
        val child2   = mockk<View>     ()
        val children = mockk<ObservableList<View>>()
        val display  = display(children)

        display += listOf(child1, child2)

        verify (exactly = 1) { children += listOf(child1, child2) }
    }

    @Test fun `-= collection works`() {
        val child1   = mockk<Container>()
        val child2   = mockk<View>     ()
        val children = mockk<ObservableList<View>>()
        val display  = display(children)

        display -= listOf(child1, child2)

        verify (exactly = 1) { children -= listOf(child1, child2) }
    }

    // FIXME: Need to figure out how to test this since the interface calls to layout cannot be mocked
    @Ignore @Test fun `child at works`() {
        val at       = Point(11.0, 13.0)
        val result   = view {}
        val children = ObservableList(listOf(result))
        val layout   = mockk<Layout> {
            every { item(any(), at = at) } returns Found(result.positionable)
        }

        display(children, layout).apply {
            expect(result) { child(at) }

            every { layout.item(any(), at = at) } returns Ignored

            expect(null) { child(at) }

            verify(exactly = 2) { layout.item(any(), at) }
        }
    }

    private data class MirroredTestData(val contentDirection: ContentDirection, val mirrorWhenRightLeft: Boolean) {
        override fun toString() = "contentDirection: $contentDirection, mirrorWhenRightLeft: $mirrorWhenRightLeft"
    }

    private val mirroredTestData = listOf(
        MirroredTestData(contentDirection = RightLeft, mirrorWhenRightLeft = true ) to true,
        MirroredTestData(contentDirection = RightLeft, mirrorWhenRightLeft = false) to false,
        MirroredTestData(contentDirection = LeftRight, mirrorWhenRightLeft = true ) to false,
        MirroredTestData(contentDirection = LeftRight, mirrorWhenRightLeft = false) to false)

    @TestFactory fun `mirrored valid`() = mirroredTestData.map { (input, expected) ->
        DynamicTest.dynamicTest("mirrored is $expected when given $input") {
            val display = display(input.contentDirection, input.mirrorWhenRightLeft)
            expect(expected) { display.mirrored }
        }
    }

    private fun display(contentDirection: ContentDirection, mirrorWhenRightLeft: Boolean): Display {
        abstract class DisplayTester: Display

        return mockk<DisplayTester> {
            every { this@mockk.contentDirection    } returns contentDirection
            every { this@mockk.mirrorWhenRightLeft } returns mirrorWhenRightLeft
            every { mirrored                       } answers { callOriginal() }
        }
    }

    private fun display(children: ObservableList<View>, layout: Layout = mockk()): Display {
        abstract class DisplayTester2: Display

        return mockk<DisplayTester2> {
            every { this@mockk.layout                     } returns layout
            every { this@mockk.children                   } returns children
            every { this@mockk += any<View>()             } answers { callOriginal() }
            every { this@mockk -= any<View>()             } answers { callOriginal() }
            every { this@mockk += any<Collection<View>>() } answers { callOriginal() }
            every { this@mockk -= any<Collection<View>>() } answers { callOriginal() }
            every { this@mockk.child(any())               } answers { callOriginal() }
        }
    }
}