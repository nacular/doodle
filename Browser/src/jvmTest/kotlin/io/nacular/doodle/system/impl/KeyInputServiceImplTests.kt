package io.nacular.doodle.system.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import io.nacular.doodle.dom.EventTarget
import io.nacular.doodle.event.KeyState
import io.nacular.doodle.system.KeyInputService.KeyResponse.Consumed
import io.nacular.doodle.system.KeyInputService.Listener
import io.nacular.doodle.system.KeyInputService.Postprocessor
import io.nacular.doodle.system.KeyInputService.Preprocessor
import io.nacular.doodle.system.impl.KeyInputServiceImpl.RawListener
import io.nacular.doodle.system.impl.KeyInputServiceStrategy.EventHandler
import kotlin.test.Test

class KeyInputServiceImplTests {
    @Test fun `start up not call without listener`() {
        val strategy = mockk<KeyInputServiceStrategy>()

        KeyInputServiceImpl(strategy)

        verify(exactly = 0) {
            strategy.startUp(any())
        }
    }

    @Test fun `+= listener works`() {
        val keyState = mockk<KeyState>()
        val handler  = slot<EventHandler>()
        val strategy = mockk<KeyInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val listener1 = mockk<Listener>()
        val listener2 = mockk<Listener>()

        KeyInputServiceImpl(strategy).apply {
            this += listener1
            this += listener2
        }

        handler.captured(keyState, mockk())

        verifyOrder {
            strategy.startUp(any())

            listener1(keyState)
            listener2(keyState)
        }

        verify(exactly = 0) { strategy.shutdown() }
    }

    @Test fun `-= listener works`() {
        val keyState1 = mockk<KeyState>()
        val keyState2 = mockk<KeyState>()
        val handler   = slot<EventHandler>()
        val strategy  = mockk<KeyInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val listener1 = mockk<Listener>()
        val listener2 = mockk<Listener>()

        val service = KeyInputServiceImpl(strategy).apply {
            this += listener1
            this += listener2
        }

        handler.captured(keyState1, mockk())

        service -= listener1

        handler.captured(keyState2, mockk())

        verifyOrder {
            strategy.startUp(any())

            listener1(keyState1)
            listener2(keyState1)
            listener2(keyState2)
        }

        verify(exactly = 0) { strategy.shutdown() }
    }

    @Test fun `shutdown when all listeners removed`() {
        val strategy  = mockk<KeyInputServiceStrategy>()
        val listener1 = mockk<Listener>()
        val listener2 = mockk<Listener>()

        val service = KeyInputServiceImpl(strategy).apply {
            this += listener1
            this += listener2
        }

        service -= listener1
        service -= listener2

        verifyOrder {
            strategy.startUp(any())
            strategy.shutdown()
        }
    }

    @Test fun `+= preprocessor works`() {
        val keyState = mockk<KeyState>()
        val handler  = slot<EventHandler>()
        val strategy = mockk<KeyInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val preprocessor1 = mockk<Preprocessor>()
        val preprocessor2 = mockk<Preprocessor>()

        KeyInputServiceImpl(strategy).apply {
            this += preprocessor1
            this += preprocessor2
        }

        handler.captured(keyState, mockk())

        verifyOrder {
            strategy.startUp(any())

            preprocessor1(keyState)
            preprocessor2(keyState)
        }

        verify(exactly = 0) { strategy.shutdown() }
    }

    @Test fun `-= preprocessor works`() {
        val keyState1 = mockk<KeyState>()
        val keyState2 = mockk<KeyState>()
        val handler   = slot<EventHandler>()
        val strategy  = mockk<KeyInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val preprocessor1 = mockk<Preprocessor>()
        val preprocessor2 = mockk<Preprocessor>()

        val service = KeyInputServiceImpl(strategy).apply {
            this += preprocessor1
            this += preprocessor2
        }

        handler.captured(keyState1, mockk())

        service -= preprocessor1

        handler.captured(keyState2, mockk())

        verifyOrder {
            strategy.startUp(any())

            preprocessor1(keyState1)
            preprocessor2(keyState1)
            preprocessor2(keyState2)
        }

        verify(exactly = 0) { strategy.shutdown() }
    }

    @Test fun `shutdown when all preprocessor removed`() {
        val strategy      = mockk<KeyInputServiceStrategy>()
        val preprocessor1 = mockk<Preprocessor>()
        val preprocessor2 = mockk<Preprocessor>()

        val service = KeyInputServiceImpl(strategy).apply {
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

    @Test fun `+= postprocessor works`() {
        val keyState = mockk<KeyState>()
        val handler  = slot<EventHandler>()
        val strategy = mockk<KeyInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val postprocessor1 = mockk<Postprocessor>()
        val postprocessor2 = mockk<Postprocessor>()

        KeyInputServiceImpl(strategy).apply {
            this += postprocessor1
            this += postprocessor2
        }

        handler.captured(keyState, mockk())

        verifyOrder {
            strategy.startUp(any())

            postprocessor1(keyState)
            postprocessor2(keyState)
        }

        verify(exactly = 0) { strategy.shutdown() }
    }

    @Test fun `-= postprocessor works`() {
        val keyState1 = mockk<KeyState>()
        val keyState2 = mockk<KeyState>()
        val handler   = slot<EventHandler>()
        val strategy  = mockk<KeyInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val postprocessor1 = mockk<Postprocessor>()
        val postprocessor2 = mockk<Postprocessor>()

        val service = KeyInputServiceImpl(strategy).apply {
            this += postprocessor1
            this += postprocessor2
        }

        handler.captured(keyState1, mockk())

        service -= postprocessor1

        handler.captured(keyState2, mockk())

        verifyOrder {
            strategy.startUp(any())

            postprocessor1(keyState1)
            postprocessor2(keyState1)
            postprocessor2(keyState2)
        }

        verify(exactly = 0) { strategy.shutdown() }
    }

    @Test fun `shutdown when all postprocessor removed`() {
        val strategy      = mockk<KeyInputServiceStrategy>()
        val postprocessor1 = mockk<Postprocessor>()
        val postprocessor2 = mockk<Postprocessor>()

        val service = KeyInputServiceImpl(strategy).apply {
            this += postprocessor1
            this += postprocessor2
        }

        service -= postprocessor1
        service -= postprocessor2

        verifyOrder {
            strategy.startUp(any())
            strategy.shutdown()
        }
    }

    @Test fun `+= raw listener works`() {
        val keyState = mockk<KeyState>()
        val target   = mockk<EventTarget>()
        val handler  = slot<EventHandler>()
        val strategy = mockk<KeyInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val listener1 = mockk<RawListener>()
        val listener2 = mockk<RawListener>()

        KeyInputServiceImpl(strategy).apply {
            this += listener1
            this += listener2
        }

        handler.captured(keyState, target)

        verifyOrder {
            strategy.startUp(any())

            listener1(keyState, target)
            listener2(keyState, target)
        }

        verify(exactly = 0) { strategy.shutdown() }
    }

    @Test fun `-= raw listener works`() {
        val keyState1 = mockk<KeyState>()
        val keyState2 = mockk<KeyState>()
        val target1   = mockk<EventTarget>()
        val target2   = mockk<EventTarget>()
        val handler   = slot<EventHandler>()
        val strategy  = mockk<KeyInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val listener1 = mockk<RawListener>()
        val listener2 = mockk<RawListener>()

        val service = KeyInputServiceImpl(strategy).apply {
            this += listener1
            this += listener2
        }

        handler.captured(keyState1, target1)

        service -= listener1

        handler.captured(keyState2, target2)

        verifyOrder {
            strategy.startUp(any())

            listener1(keyState1, target1)
            listener2(keyState1, target1)
            listener2(keyState2, target2)
        }

        verify(exactly = 0) { strategy.shutdown() }
    }

    @Test fun `shutdown when all raw listeners removed`() {
        val strategy  = mockk<KeyInputServiceStrategy>()
        val listener1 = mockk<RawListener>()
        val listener2 = mockk<RawListener>()

        val service = KeyInputServiceImpl(strategy).apply {
            this += listener1
            this += listener2
        }

        service -= listener1
        service -= listener2

        verifyOrder {
            strategy.startUp(any())
            strategy.shutdown()
        }
    }

    @Test fun `obeys listener, processor precedence`() {
        val keyState = mockk<KeyState>()
        val target   = mockk<EventTarget>()
        val handler  = slot<EventHandler>()
        val strategy = mockk<KeyInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val listener1      = mockk<Listener>     ()
        val listener2      = mockk<Listener>     ()
        val preprocessor1  = mockk<Preprocessor> ()
        val preprocessor2  = mockk<Preprocessor> ()
        val postprocessor1 = mockk<Postprocessor>()
        val postprocessor2 = mockk<Postprocessor>()
        val rawListener1   = mockk<RawListener>()
        val rawListener2   = mockk<RawListener>()

        KeyInputServiceImpl(strategy).apply {
            this += postprocessor1
            this += listener1
            this += postprocessor2
            this += rawListener1
            this += preprocessor1
            this += listener2
            this += preprocessor2
            this += rawListener2
        }

        handler.captured(keyState, target)

        verifyOrder {
            strategy.startUp(any())

            rawListener1  (keyState, target)
            rawListener2  (keyState, target)
            preprocessor1 (keyState        )
            preprocessor2 (keyState        )
            listener1     (keyState        )
            listener2     (keyState        )
            postprocessor1(keyState        )
            postprocessor2(keyState        )
        }

        verify(exactly = 0) { strategy.shutdown() }
    }

    @Test fun `raw consumes`() {
        val keyState = mockk<KeyState>()
        val target   = mockk<EventTarget>()
        val handler  = slot<EventHandler>()
        val strategy = mockk<KeyInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val listener1      = mockk<Listener>     ()
        val listener2      = mockk<Listener>     ()
        val preprocessor1  = mockk<Preprocessor> ()
        val preprocessor2  = mockk<Preprocessor> ()
        val postprocessor1 = mockk<Postprocessor>()
        val postprocessor2 = mockk<Postprocessor>()
        val rawListener1   = mockk<RawListener>()
        val rawListener2   = mockk<RawListener>().apply {
            every { this@apply.invoke(any(), any()) } returns Consumed
        }

        KeyInputServiceImpl(strategy).apply {
            this += postprocessor1
            this += listener1
            this += postprocessor2
            this += rawListener1
            this += preprocessor1
            this += listener2
            this += preprocessor2
            this += rawListener2
        }

        handler.captured(keyState, target)

        verifyOrder {
            strategy.startUp(any())

            rawListener1 (keyState, target)
            rawListener2 (keyState, target)
        }

        verify(exactly = 0) {
            strategy.shutdown()

            preprocessor1 (any())
            preprocessor2 (any())
            listener1     (any())
            listener2     (any())
            postprocessor1(any())
            postprocessor2(any())
        }
    }

    @Test fun `preprocessor consumes`() {
        val keyState = mockk<KeyState>()
        val target   = mockk<EventTarget>()
        val handler  = slot<EventHandler>()
        val strategy = mockk<KeyInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val listener1      = mockk<Listener>     ()
        val listener2      = mockk<Listener>     ()
        val preprocessor1  = mockk<Preprocessor> ()
        val preprocessor2  = mockk<Preprocessor> ().apply {
            every { this@apply.invoke(any()) } returns Consumed
        }
        val postprocessor1 = mockk<Postprocessor>()
        val postprocessor2 = mockk<Postprocessor>()
        val rawListener1   = mockk<RawListener>()
        val rawListener2   = mockk<RawListener>()

        KeyInputServiceImpl(strategy).apply {
            this += postprocessor1
            this += listener1
            this += postprocessor2
            this += rawListener1
            this += preprocessor1
            this += listener2
            this += preprocessor2
            this += rawListener2
        }

        handler.captured(keyState, target)

        verifyOrder {
            strategy.startUp(any())

            rawListener1 (keyState, target)
            rawListener2 (keyState, target)
            preprocessor1(keyState        )
            preprocessor2(keyState        )
        }

        verify(exactly = 0) {
            strategy.shutdown()

            listener1     (any())
            listener2     (any())
            postprocessor1(any())
            postprocessor2(any())
        }
    }

    @Test fun `listener consumes`() {
        val keyState = mockk<KeyState>()
        val target   = mockk<EventTarget>()
        val handler  = slot<EventHandler>()
        val strategy = mockk<KeyInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val listener1      = mockk<Listener>     ()
        val listener2      = mockk<Listener>     ().apply {
            every { this@apply.invoke(any()) } returns Consumed
        }
        val preprocessor1  = mockk<Preprocessor> ()
        val preprocessor2  = mockk<Preprocessor> ()
        val postprocessor1 = mockk<Postprocessor>()
        val postprocessor2 = mockk<Postprocessor>()
        val rawListener1   = mockk<RawListener>()
        val rawListener2   = mockk<RawListener>()

        KeyInputServiceImpl(strategy).apply {
            this += postprocessor1
            this += listener1
            this += postprocessor2
            this += rawListener1
            this += preprocessor1
            this += listener2
            this += preprocessor2
            this += rawListener2
        }

        handler.captured(keyState, target)

        verifyOrder {
            strategy.startUp(any())

            rawListener1 (keyState, target)
            rawListener2 (keyState, target)
            preprocessor1(keyState        )
            preprocessor2(keyState        )
            listener1    (keyState        )
            listener2    (keyState        )
        }

        verify(exactly = 0) {
            strategy.shutdown()

            postprocessor1(any())
            postprocessor2(any())
        }
    }

    @Test fun `postprocessor consumes`() {
        val keyState = mockk<KeyState>()
        val target   = mockk<EventTarget>()
        val handler  = slot<EventHandler>()
        val strategy = mockk<KeyInputServiceStrategy>().apply {
            every { this@apply.startUp(capture(handler)) } answers {}
        }

        val listener1      = mockk<Listener>     ()
        val listener2      = mockk<Listener>     ()
        val preprocessor1  = mockk<Preprocessor> ()
        val preprocessor2  = mockk<Preprocessor> ()
        val postprocessor1 = mockk<Postprocessor>().apply {
            every { this@apply.invoke(any()) } returns Consumed
        }
        val postprocessor2 = mockk<Postprocessor>()
        val rawListener1   = mockk<RawListener>()
        val rawListener2   = mockk<RawListener>()

        KeyInputServiceImpl(strategy).apply {
            this += postprocessor1
            this += listener1
            this += postprocessor2
            this += rawListener1
            this += preprocessor1
            this += listener2
            this += preprocessor2
            this += rawListener2
        }

        handler.captured(keyState, target)

        verifyOrder {
            strategy.startUp(any())

            rawListener1  (keyState, target)
            rawListener2  (keyState, target)
            preprocessor1 (keyState        )
            preprocessor2 (keyState        )
            listener1     (keyState        )
            listener2     (keyState        )
            postprocessor1(keyState        )
        }

        verify(exactly = 0) {
            strategy.shutdown()

            postprocessor2(any())
        }
    }

    @Test fun `shutdown when all handles removed`() {
        val strategy      = mockk<KeyInputServiceStrategy>()
        val listener1      = mockk<Listener>     ()
        val listener2      = mockk<Listener>     ()
        val preprocessor1  = mockk<Preprocessor> ()
        val preprocessor2  = mockk<Preprocessor> ()
        val postprocessor1 = mockk<Postprocessor>()
        val postprocessor2 = mockk<Postprocessor>()
        val rawListener1   = mockk<RawListener>  ()
        val rawListener2   = mockk<RawListener>  ()

        val service = KeyInputServiceImpl(strategy).apply {
            this += postprocessor1
            this += listener1
            this += postprocessor2
            this += preprocessor1
            this += listener2
            this += preprocessor2
            this += rawListener1
            this += rawListener2
        }

        service -= listener2
        service -= postprocessor1
        service -= rawListener1
        service -= rawListener2
        service -= listener1
        service -= postprocessor2
        service -= preprocessor1
        service -= preprocessor2

        verifyOrder {
            strategy.startUp(any())
            strategy.shutdown()
        }
    }
}