package io.nacular.doodle.controls.form

import JsName
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import io.nacular.doodle.controls.form.Form.Invalid
import io.nacular.doodle.controls.form.Form.Valid
import io.nacular.doodle.controls.form.FormTests.Gender.Female
import io.nacular.doodle.controls.form.FormTests.Gender.Male
import io.nacular.doodle.core.view
import io.nacular.doodle.utils.PropertyObserversImpl
import kotlin.test.Test

class FormTests {
    private enum class Gender { Male, Female }
    private data class Person(val name: String, val gender: Gender)

    @Test @JsName("stateChangesToReady")
    fun `state changes to ready`() {
        val onValid       = mockk<(Person) -> Unit>(relaxed = true)
        val onInvalid     = mockk<(      ) -> Unit>(relaxed = true)
        val nameChanged   = PropertyObserversImpl<Any?, String?>(null)
        val genderChanged = PropertyObserversImpl<Any?, Gender?>(null)

        Form { this (
            + field<String> {
                view {
                    nameChanged += { _, _, new ->
                        value = new?.let { Valid(it) } ?: Invalid()
                    }
                }
            },
            + field<Gender> {
                view {
                    genderChanged += { _, _, new ->
                        value = new?.let { Valid(it) } ?: Invalid()
                    }
                }
            },
            onInvalid = onInvalid
        ) { name, gender ->
            onValid(Person(name, gender))
        } }

        nameChanged  (null, "Jennifer")
        genderChanged(null, Female    )

        verify(exactly = 1) {
            onValid(Person("Jennifer", Female))
        }

        verify(exactly = 0) {
            onInvalid()
        }
    }

    @Test @JsName("stateChangesToInvalid")
    fun `state changes to invalid`() {
        val onValid       = mockk<(Person) -> Unit>(relaxed = true)
        val onInvalid     = mockk<(      ) -> Unit>(relaxed = true)
        val nameChanged   = PropertyObserversImpl<Any?, String?>(null)
        val genderChanged = PropertyObserversImpl<Any?, Gender?>(null)

        Form { this (
                "Jennifer" to field {
                    view {
                        nameChanged += { _,_,new ->
                            value = new?.let { Valid(it) } ?: Invalid()
                        }
                    }
                },
                Female to field {
                    view {
                        genderChanged += { _,_, new ->
                            value = new?.let { Valid(it) } ?: Invalid()
                        }
                    }
                },
                onInvalid = onInvalid
        ) { name, gender ->
            onValid(Person(name, gender))
        } }

        nameChanged("Jennifer", null)

        verifyOrder {
            onValid  (Person("Jennifer", Female))
            onInvalid(                          )
        }
    }

    @Test @JsName("defaultsWork")
    fun `defaults work`() {
        val onValid       = mockk<(Person) -> Unit>(relaxed = true)
        val onInvalid     = mockk<(      ) -> Unit>(relaxed = true)
        val nameChanged   = PropertyObserversImpl<Any?, String?>(null)
        val genderChanged = PropertyObserversImpl<Any?, Gender?>(null)

        Form { this(
            + field<String> {
                view {
                    nameChanged += { _,_,new ->
                        value = new?.let { Valid(it) } ?: Invalid()
                    }
                }
            },
            Male to field {
                view {
                    genderChanged += { _,_,new ->
                        value = new?.let { Valid(it) } ?: Invalid()
                    }
                }
            },
            onInvalid = onInvalid
        ) { name, gender ->
            onValid(Person(name, gender))
        } }

        nameChanged  (null, "John")
        genderChanged(null, Female)

        verifyOrder {
            onValid(Person("John", Male  ))
            onValid(Person("John", Female))
        }
    }
}