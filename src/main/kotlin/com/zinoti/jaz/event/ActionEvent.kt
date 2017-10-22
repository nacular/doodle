package com.zinoti.jaz.event


class ActionEvent<T>(source: T): Event<T>(source)

typealias ActionListener<T> = (ActionEvent<T>) -> Unit