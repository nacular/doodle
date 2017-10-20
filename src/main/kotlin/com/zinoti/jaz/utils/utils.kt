package com.zinoti.jaz.utils

/**
 * Created by Nicholas Eddy on 10/20/17.
 */
fun <T: Comparable<T>> min(first: T, second: T): T = if (first <= second) first else second