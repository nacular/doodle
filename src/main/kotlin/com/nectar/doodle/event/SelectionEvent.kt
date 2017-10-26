package com.nectar.doodle.event


class SelectionEvent<T>(source: T, val selected: Boolean): Event<T>(source)

typealias SelectionListener<T> = (SelectionEvent<T>) -> Unit