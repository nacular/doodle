package io.nacular.doodle.core

import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY

/**
 * Indicates internal Doodle APIs that should only be accessed from other Doodle libraries.
 */
@RequiresOptIn(message = "This API is internal. It may be changed in the future without notice.")
@Retention(AnnotationRetention.BINARY)
@Target(CLASS, FUNCTION, PROPERTY, CONSTRUCTOR)
public annotation class Internal