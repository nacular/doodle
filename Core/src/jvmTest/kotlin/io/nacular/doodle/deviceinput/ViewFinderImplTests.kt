package io.nacular.doodle.deviceinput

import io.mockk.every
import io.mockk.impl.JvmMockKGateway
import io.mockk.impl.instantiation.JvmAnyValueGenerator
import io.mockk.mockk
import io.mockk.verify
import io.nacular.doodle.core.ChildObserver
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.SetPool
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.test.expect

class ViewFinderImplTests {
    private class NullableValueGenerator(voidInstance: Any): JvmAnyValueGenerator(voidInstance) {
        override fun anyValue(cls: KClass<*>, isNullable: Boolean, orInstantiateVia: () -> Any?): Any? = when {
            isNullable -> null
            else       -> super.anyValue(cls, isNullable, orInstantiateVia)
        }
    }

    init {
        JvmMockKGateway.anyValueGeneratorFactory = { voidInstance ->
            NullableValueGenerator(voidInstance)
        }
    }

    @Test fun `finds item at point`() {
        val finder  = ViewFinderImpl
        val child   = mockk<View>("child") {
            every { enabled } returns true
        }
        val parent  = mockk<View>("parent") {
            every { parent               } returns null
            every { enabled              } returns true
            every { child_(any(), any()) } returns child
        }

        val display = mockk<Display> {
            every { child(any()       ) } returns parent
            every { child(any(), any()) } returns parent
        }

        val point = Point(104, 567)

        expect(child) { finder.find(within = display, at = point) }

        verify(exactly = 1) { display.child(point) }
        verify(exactly = 1) { parent.child_(parent.toLocal(point, null), any()) }
    }

    @Test fun `skips disabled item at point`() {
        val finder  = ViewFinderImpl
        val child   = mockk<View> {
            every { enabled } returns false
        }
        val display = mockk<Display> {
            every { child(any()) } returns child
        }

        val point = Point(104, 567)

        expect(null) { finder.find(within = display, at = point) }

        verify(exactly = 1) { display.child(point) }
    }

    @Test fun `returns parent if disabled item at point`() {
        val finder  = ViewFinderImpl
        val child   = mockk<View> {
            every { enabled } returns false
        }
        val parent  = mockk<View> {
            every { parent       } returns null
            every { enabled      } returns true
            every { child_(any())} returns child
        }
        val display = mockk<Display> {
            every { child(any()) } returns parent
        }

        val point = Point(104, 567)

        expect(parent) { finder.find(within = display, at = point) }

        verify(exactly = 1) { display.child(point) }
        verify(exactly = 1) { parent.child_(parent.toLocal(point, null), any()) }
    }

    @Test fun `returns parent if item filtered at point`() {
        val finder  = ViewFinderImpl
        val child   = mockk<View>("child" ) { every { enabled } returns true }
        val parent  = mockk<View>("parent") {
            every { parent               } returns null
            every { enabled              } returns true
            every { child_(any())        } returns child
            every { child_(any(), any()) } returns null
        }
        val display = display(parent).apply {
            every { child(any(), any()) } returns parent
        }

        val point = Point(104, 567)

        expect(parent) { finder.find(within = display, starting = parent, at = point) { it != child } }
    }

    @Test fun `returns child below if item filtered at point`() {
        val finder  = ViewFinderImpl
        val child1  = mockk<View> { every { enabled } returns true }
        val child2  = mockk<View> { every { enabled } returns true }
        val parent  = mockk<View> {
            every { parent               } returns null
            every { enabled              } returns true
            every { child_(any())        } returnsMany listOf(child1, child2)
            every { child_(any(), any()) } returns child2
        }
        val display = display(parent).apply {
            every { child(any()) } returns parent
        }

        val point = Point(104, 567)

        expect(child2) { finder.find(within = display, starting = parent, at = point) { it != child1 } }
    }

    @Test fun `returns parent sibling below if items filtered at point`() {
        val finder  = ViewFinderImpl
        val child1  = mockk<View>("child1") { every { enabled } returns true }
        val child2  = mockk<View>("child2") { every { enabled } returns true }
        val parent  = mockk<View>("parent") {
            every { this@mockk.parent   } returns null
            every { enabled              } returns true
            every { child_(any())        } returnsMany listOf(child1, child2)
            every { child_(any(), any()) } returns null
        }
        val aunt    = mockk<View>("aunt") {
            every { this@mockk.parent    } returns null
            every { enabled              } returns true
            every { child_(any())        } returns null
            every { child_(any(), any()) } returns null
        }
        val display = display(parent, aunt).apply {
            every { child(any(), any()) } returns aunt
        }

        val point = Point(104, 567)

        expect(aunt) { finder.find(within = display, starting = parent, at = point) { it !in listOf(child1, child2, parent) } }
    }

    private fun display(vararg children: View): Display = mockk<Display> {
        val displayChildren = ObservableList<View>()

        displayChildren.addAll(children)

        val observers = SetPool<ChildObserver<Display>>()

        displayChildren.changed += { _, differences ->
            observers.forEach { it(this, differences) }
        }

        every { size                } returns Size(100, 100)
        every { this@mockk.children } returns displayChildren
        every { iterator()          } answers { displayChildren.iterator() }
        every { childrenChanged     } returns observers
        every { sizeChanged         } returns mockk()
    }
}
