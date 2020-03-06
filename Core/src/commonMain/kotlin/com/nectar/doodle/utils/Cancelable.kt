package com.nectar.doodle.utils

import com.nectar.doodle.utils.CompletableImpl.State.Active
import com.nectar.doodle.utils.CompletableImpl.State.Canceled
import com.nectar.doodle.utils.CompletableImpl.State.Completed
import kotlin.js.JsName


interface Completable: Cancelable {
    val completed: Pool<(source: Completable) -> Unit>
    val canceled : Pool<(source: Completable) -> Unit>
}

internal class NoOpPool<T>: Pool<(source: T) -> Unit> {
    override fun plusAssign(item: (source: T) -> Unit) {}

    override fun minusAssign(item: (source: T) -> Unit) {}
}

internal class InstantPool<T>(private val source: T): Pool<(source: T) -> Unit> {
    override fun plusAssign(item: (source: T) -> Unit) {
        item(source)
    }

    override fun minusAssign(item: (source: T) -> Unit) {}
}

open class CompletableImpl: Completable {
    enum class State { Active, Completed, Canceled }

    protected var state: State = Active
        private set(new) {
            if (field != Active) { return }

            field = new

            when (new) {
                Completed -> {
                    completed_.forEach { it(this) }
                    completed_.clear()
                    completed = InstantPool(this)
                    canceled  = NoOpPool()
                }
                Canceled -> {
                    canceled_.forEach { it(this) }
                    canceled_.clear()
                    canceled  = InstantPool(this)
                    completed = NoOpPool()
                }
                else -> {}
            }
        }

    private val completed_ by lazy { mutableSetOf<(source: Completable) -> Unit>() }
    private val canceled_  by lazy { mutableSetOf<(source: Completable) -> Unit>() }

    override var completed: Pool<(source: Completable) -> Unit> = SetPool(completed_)
        protected set
    override var canceled : Pool<(source: Completable) -> Unit> = SetPool(canceled_ )
        protected set

    override  fun cancel() { state = Canceled }

    @JsName("completedFunc")
    protected open fun completed() { state = Completed }
}

val NoOpCompletable: Completable = object: CompletableImpl() {
    init {
        completed()
    }
}

interface Cancelable {
    fun cancel()
}