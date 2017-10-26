package com.nectar.doodle.event


class ChangeEvent<T>(aSource: T): Event<T>(aSource)


typealias ChangeListener<T> = (ChangeEvent<T>) -> Unit