package com.nectar.doodle.event


class ActionEvent<T>(source: T): Event<T>(source)

typealias ActionListener<T> = (ActionEvent<T>) -> Unit