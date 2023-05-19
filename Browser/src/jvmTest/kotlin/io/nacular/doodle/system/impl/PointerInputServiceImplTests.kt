package io.nacular.doodle.system.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.PointerInputService.Listener
import io.nacular.doodle.system.PointerInputService.Preprocessor
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.doodle.system.impl.PointerInputServiceStrategy.EventHandler
import kotlin.test.Test
import kotlin.test.expect

class PointerInputServiceImplTests {
    @Test fun `cursor works`() {
        val mockCursor = mockk<Cursor>()
        val strategy   = mockk<PointerInputServiceStrategy>().apply {
            every { cursor } returns mockCursor
        }

        expect(PointerInputServiceImpl(strategy).cursor) { mockCursor }
    }

    @Test fun `set cursor works`() {
        val strategy   = mockk<PointerInputServiceStrategy>()
        val mockCursor = mockk<Cursor>()

        PointerInputServiceImpl(strategy).cursor = mockCursor

        verify(exactly = 1) {
            strategy.cursor = mockCursor
        }
    }

    @Test fun `tooltip works`() {
        val value    = "some tooltip text"
        val strategy = mockk<PointerInputServiceStrategy>().apply {
            every { toolTipText } returns value
        }

        expect(PointerInputServiceImpl(strategy).toolTipText) { value }
    }

    @Test fun `set tooltip works`() {
        val strategy = mockk<PointerInputServiceStrategy>()

        val value = "some tooltip text"

        PointerInputServiceImpl(strategy).toolTipText = value

        verify(exactly = 1) {
            strategy.toolTipText = value
        }
    }

    @Test fun `start up not call without listener`() {
        val strategy = mockk<PointerInputServiceStrategy>()

        PointerInputServiceImpl(strategy)

        verify(exactly = 0) {
            strategy.startUp(any())
        }
    }

    @Test fun `+= listener works`() {
        val sysEvent = mockk<SystemPointerEvent>()
        val handler  = slot<EventHandler>()
        val strategy = mockk<PointerInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val listener1 = mockk<Listener>()
        val listener2 = mockk<Listener>()

        PointerInputServiceImpl(strategy).apply {
            this += listener1
            this += listener2
        }

        handler.captured(sysEvent)

        verifyOrder {
            strategy.startUp(any())

            listener1(sysEvent)
            listener2(sysEvent)
        }

        verify(exactly = 0) { strategy.shutdown() }
    }

    @Test fun `-= listener works`() {
        val sysEvent1 = mockk<SystemPointerEvent>()
        val sysEvent2 = mockk<SystemPointerEvent>()
        val handler   = slot<EventHandler>()
        val strategy  = mockk<PointerInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val listener1 = mockk<Listener>()
        val listener2 = mockk<Listener>()

        val service = PointerInputServiceImpl(strategy).apply {
            this += listener1
            this += listener2
        }

        handler.captured(sysEvent1)

        service -= listener1

        handler.captured(sysEvent2)

        verifyOrder {
            strategy.startUp(any())

            listener1(sysEvent1)
            listener2(sysEvent1)
            listener2(sysEvent2)
        }

        verify(exactly = 0) { strategy.shutdown() }
    }

    @Test fun `shutdown when all listeners removed`() {
        val strategy  = mockk<PointerInputServiceStrategy>()
        val listener1 = mockk<Listener>()
        val listener2 = mockk<Listener>()

        val service = PointerInputServiceImpl(strategy).apply {
            this += listener1
            this += listener2
        }

        service -= listener1
        service -= listener2

        verifyOrder {
            strategy.startUp (any())
            strategy.shutdown(     )
        }
    }

    @Test fun `+= preprocessor works`() {
        val sysEvent = mockk<SystemPointerEvent>()
        val handler  = slot<EventHandler>()
        val strategy = mockk<PointerInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val preprocessor1 = mockk<Preprocessor>()
        val preprocessor2 = mockk<Preprocessor>()

        PointerInputServiceImpl(strategy).apply {
            this += preprocessor1
            this += preprocessor2
        }

        handler.captured(sysEvent)

        verifyOrder {
            strategy.startUp(any())

            preprocessor1(sysEvent)
            preprocessor2(sysEvent)
        }

        verify(exactly = 0) { strategy.shutdown() }
    }

    @Test fun `-= preprocessor works`() {
        val sysEvent1 = mockk<SystemPointerEvent>()
        val sysEvent2 = mockk<SystemPointerEvent>()
        val handler   = slot<EventHandler>()
        val strategy  = mockk<PointerInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val preprocessor1 = mockk<Preprocessor>()
        val preprocessor2 = mockk<Preprocessor>()

        val service = PointerInputServiceImpl(strategy).apply {
            this += preprocessor1
            this += preprocessor2
        }

        handler.captured(sysEvent1)

        service -= preprocessor1

        handler.captured(sysEvent2)

        verifyOrder {
            strategy.startUp(any())

            preprocessor1(sysEvent1)
            preprocessor2(sysEvent1)
            preprocessor2(sysEvent2)
        }

        verify(exactly = 0) { strategy.shutdown() }
    }

    @Test fun `shutdown when all preprocessor removed`() {
        val strategy      = mockk<PointerInputServiceStrategy>()
        val preprocessor1 = mockk<Preprocessor>()
        val preprocessor2 = mockk<Preprocessor>()

        val service = PointerInputServiceImpl(strategy).apply {
            this += preprocessor1
            this += preprocessor2
        }

        service -= preprocessor1
        service -= preprocessor2

        verifyOrder {
            strategy.startUp(any())
            strategy.shutdown()
        }
    }

    @Test fun `obeys listener, processor precedence`() {
        val sysEvent = mockk<SystemPointerEvent>()
        val handler  = slot<EventHandler>()
        val strategy = mockk<PointerInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val listener1      = mockk<Listener>     ()
        val listener2      = mockk<Listener>     ()
        val preprocessor1  = mockk<Preprocessor> ()
        val preprocessor2  = mockk<Preprocessor> ()

        PointerInputServiceImpl(strategy).apply {
            this += listener1
            this += preprocessor1
            this += listener2
            this += preprocessor2
        }

        handler.captured(sysEvent)

        verifyOrder {
            strategy.startUp(any())

            preprocessor1(sysEvent)
            preprocessor2(sysEvent)
            listener1    (sysEvent)
            listener2    (sysEvent)
        }

        verify(exactly = 0) { strategy.shutdown() }
    }

    @Test fun `preprocessor consumes`() {
        val sysEvent = mockk<SystemPointerEvent>()
        val handler  = slot<EventHandler>()
        val strategy = mockk<PointerInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val listener1      = mockk<Listener>     ()
        val listener2      = mockk<Listener>     ()
        val preprocessor1  = mockk<Preprocessor> ()
        val preprocessor2  = mockk<Preprocessor> ().apply {
            every { this@apply.invoke(any()) } answers {
                every { sysEvent.consumed } returns true
            }
        }

        PointerInputServiceImpl(strategy).apply {
            this += listener1
            this += preprocessor1
            this += listener2
            this += preprocessor2
        }

        handler.captured(sysEvent)

        verifyOrder {
            strategy.startUp(any())

            preprocessor1(sysEvent)
            preprocessor2(sysEvent)
        }

        verify(exactly = 0) {
            strategy.shutdown()

            listener1(any())
            listener2(any())
        }
    }

    @Test fun `listener consumes`() {
        val sysEvent = mockk<SystemPointerEvent>()
        val handler  = slot<EventHandler>()
        val strategy = mockk<PointerInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val listener1      = mockk<Listener>     ()
        val listener2      = mockk<Listener>     ().apply {
            every { this@apply.invoke(any()) } answers {
                every { sysEvent.consumed } returns true
            }
        }
        val preprocessor1 = mockk<Preprocessor>()
        val preprocessor2 = mockk<Preprocessor>()

        PointerInputServiceImpl(strategy).apply {
            this += listener1
            this += preprocessor1
            this += listener2
            this += preprocessor2
        }

        handler.captured(sysEvent)

        verifyOrder {
            strategy.startUp(any())

            preprocessor1(sysEvent        )
            preprocessor2(sysEvent        )
            listener1    (sysEvent        )
            listener2    (sysEvent        )
        }

        verify(exactly = 0) {
            strategy.shutdown()
        }
    }

    @Test fun `shutdown when all handles removed`() {
        val strategy      = mockk<PointerInputServiceStrategy>()
        val listener1      = mockk<Listener>     ()
        val listener2      = mockk<Listener>     ()
        val preprocessor1  = mockk<Preprocessor> ()
        val preprocessor2  = mockk<Preprocessor> ()

        val service = PointerInputServiceImpl(strategy).apply {
            this += listener1
            this += preprocessor1
            this += listener2
            this += preprocessor2
        }

        service -= listener2
        service -= listener1
        service -= preprocessor1
        service -= preprocessor2

        verifyOrder {
            strategy.startUp(any())
            strategy.shutdown()
        }
    }
}