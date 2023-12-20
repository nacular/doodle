package io.nacular.doodle.scheulder.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import io.nacular.doodle.dom.Window
import io.nacular.doodle.dom.clearInterval_
import io.nacular.doodle.dom.clearTimeout_
import io.nacular.doodle.dom.setInterval_
import io.nacular.doodle.dom.setTimeout_
import io.nacular.doodle.scheduler.impl.AnimationSchedulerImpl
import io.nacular.doodle.scheduler.impl.SchedulerImpl
import io.nacular.doodle.time.Timer
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.Time.Companion.seconds
import io.nacular.measured.units.times
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class SchedulerImplTests {
    init {
        mockkStatic(Window::setTimeout_   )
        mockkStatic(Window::clearTimeout_ )
        mockkStatic(Window::setInterval_  )
        mockkStatic(Window::clearInterval_)
    }

    @Test fun `animation scheduler calls on next frame`() {
        val window   = window()
        val callback = mockk<(Measure<Time>) -> Unit>()

        AnimationSchedulerImpl(window).apply {
            onNextFrame(callback)
        }

        verify(exactly = 1) { callback(10 * milliseconds) }
    }

    @Test fun `scheduler calls after delay`() {
        val window = window()

        val timer = mockk<Timer>().apply {
            every { this@apply.now } returnsMany listOf(10, 12, 13).map { it * seconds }
        }

        val callback1 = mockk<(Measure<Time>) -> Unit>()
        val callback2 = mockk<(Measure<Time>) -> Unit>()

        SchedulerImpl(window, timer).apply {
            after(1 * seconds, callback1)
            after(0 * seconds, callback2)
        }

        verifyOrder {
            callback1( 2 * seconds     )
            callback2(10 * milliseconds)
        }
    }

    @Test fun `scheduler calls repeatedly`() {
        val window = window()

        val timer = mockk<Timer>().apply {
            every { this@apply.now } returnsMany listOf(10, 12, 13, 14).map { it * seconds }
        }

        val callback = mockk<(Measure<Time>) -> Unit>()

        SchedulerImpl(window, timer).apply {
            every(1 * seconds, callback)
        }

        verifyOrder {
            window.setInterval_(any(), ((1 * seconds) `in` milliseconds).toInt())
            callback(2 * seconds)
            callback(1 * seconds)
            callback(1 * seconds)
        }
    }

    @Test fun `scheduler delays for time`() {
        val window = window()

        val timer = mockk<Timer>().apply {
            every { this@apply.now } returnsMany listOf(10, 12, 13).map { it * seconds }
        }

        runBlocking {
            SchedulerImpl(window, timer).delay(10 * seconds)
        }

        verifyOrder {
            window.setTimeout_(any(), ((10 * seconds) `in` milliseconds).toInt())
        }
    }

    @Test fun `scheduler delays until`() {
        val window = window()

        val timer = mockk<Timer>().apply {
            every { this@apply.now } returnsMany listOf(10, 12, 13).map { it * seconds }
        }

        val condition = mockk<(Measure<Time>) -> Boolean>().apply {
            every { this@apply.invoke(any()) } returnsMany listOf(false, false, true)
        }

        runBlocking {
            SchedulerImpl(window, timer).delayUntil(condition)
        }

        verifyOrder {
            condition.invoke(any())
            condition.invoke(any())
            condition.invoke(any())
        }
    }

    @Test fun `scheduler shuts down`() {
        SchedulerImpl(window(), mockk()).shutdown()
    }

    private fun window() = mockk<Window>().apply {
        val timeOut = slot<Int>()

        every { setTimeout_(captureLambda(), capture(timeOut)) } answers {
            lambda<() -> Unit>().captured()
            0
        }

        every { setInterval_(captureLambda(), capture(timeOut)) } answers {
            lambda<() -> Unit>().captured()
            lambda<() -> Unit>().captured()
            lambda<() -> Unit>().captured()
            0
        }

        every { requestAnimationFrame(captureLambda()) } answers {
            lambda<(Double) -> Unit>().captured(10.0)
            0
        }
    }
}