package com.zinoti.jaz.event


class ChangeEvent<T>(aSource: T): Event<T>(aSource)


typealias ChangeListener<T> = (ChangeEvent<T>) -> Unit