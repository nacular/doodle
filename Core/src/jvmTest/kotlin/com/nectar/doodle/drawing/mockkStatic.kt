package com.nectar.doodle.drawing

actual inline fun mockkStatic(vararg classes: String) = io.mockk.mockkStatic(*classes)