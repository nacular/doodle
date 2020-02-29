package com.nectar.doodle.drawing.impl

/**
 * Created by Nicholas Eddy on 2/25/20.
 */
actual inline fun mockkStatic(vararg classes: String) = io.mockk.mockkStatic(*classes)