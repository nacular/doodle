package io.nacular.doodle.controls.form

import JsName
import io.mockk.mockk
import io.mockk.verify
import io.nacular.doodle.controls.form.FormControlsTests.Gender.*
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.utils.Encoder
import io.nacular.doodle.utils.PassThroughEncoder
import kotlin.test.Test
import kotlin.test.expect

class FormControlsTests {
    private enum class Gender { Male, Female }

    @Test @JsName("textFieldAcceptsValidInitialValue")
    fun `textField accepts valid initial value`() {
        val initial = "Foo"

        textFieldValidInitialValue(initial, encoder = PassThroughEncoder(), expectedText = initial)
    }

    @Test @JsName("textFieldIgnoresInValidInitialValue")
    fun `textField ignores invalid initial value`() {
        val initial = "Foo"
        val pattern = Regex("[0-9]+")

        textFieldInvalidInitialValue(
                initial,
                pattern,
                PassThroughEncoder(),
                validator    = { Regex("[0-9]+").matches(it) },
                expectedText = "")
    }

    @Test @JsName("radioListAcceptsValidInitialValue")
    fun `radio list accepts valid initial value`() {
        val onValid   = mockk<(Int) -> Unit>(relaxed = true)
        val onInvalid = mockk<(   ) -> Unit>(relaxed = true)

        Form { this (
            2 to radioList(1, 2, 3),
            onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (2) }
        verify(exactly = 0) { onInvalid( ) }
    }

    @Test @JsName("radioListIgnoresInValidInitialValue")
    fun `radio list ignores invalid initial value`() {
        val onValid   = mockk<(Int) -> Unit>(relaxed = true)
        val onInvalid = mockk<(   ) -> Unit>(relaxed = true)

        Form { this (
            23 to radioList(1, 2, 3),
            onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 0) { onValid  (any()) }
        verify(exactly = 0) { onInvalid(     ) }
    }

    @Test @JsName("optionalRadioListDefaultsToNull")
    fun `optional radio list defaults to null`() {
        val onValid   = mockk<(Int?) -> Unit>(relaxed = true)
        val onInvalid = mockk<(    ) -> Unit>(relaxed = true)

        Form { this (
            + optionalRadioList(1, 2, 3),
            onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (null) }
        verify(exactly = 0) { onInvalid(    ) }
    }

    @Test @JsName("optionalRadioListAcceptsValidInitialValue")
    fun `optional radio list accepts valid initial value`() {
        val onValid   = mockk<(Int?) -> Unit>(relaxed = true)
        val onInvalid = mockk<(    ) -> Unit>(relaxed = true)

        Form { this (
            2 to optionalRadioList(1, 2, 3),
            onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (2) }
        verify(exactly = 0) { onInvalid( ) }
    }

    @Test @JsName("optionalRadioListIgnoresInValidInitialValue")
    fun `optional radio list ignores invalid initial value`() {
        val onValid   = mockk<(Int?) -> Unit>(relaxed = true)
        val onInvalid = mockk<(    ) -> Unit>(relaxed = true)

        Form { this (
            23 to optionalRadioList(1, 2, 3),
            onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (null) }
        verify(exactly = 0) { onInvalid(    ) }
    }

    @Test @JsName("checkListDefaultsToEmpty")
    fun `check list defaults to empty`() {
        val onValid   = mockk<(List<Int>) -> Unit>(relaxed = true)
        val onInvalid = mockk<(         ) -> Unit>(relaxed = true)

        Form { this (
            + checkList(1, 2, 3),
            onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (emptyList()) }
        verify(exactly = 0) { onInvalid(           ) }
    }

    @Test @JsName("checkListAcceptsValidInitialValue")
    fun `check list accepts valid initial value`() {
        val onValid   = mockk<(List<Int>) -> Unit>(relaxed = true)
        val onInvalid = mockk<(         ) -> Unit>(relaxed = true)

        Form { this (
                listOf(2, 3) to checkList(1, 2, 3),
                onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (listOf(2, 3)) }
        verify(exactly = 0) { onInvalid(            ) }
    }

    @Test @JsName("checkListIgnoresInValidInitialValue")
    fun `check list ignores invalid initial value`() {
        val onValid   = mockk<(List<Int>) -> Unit>(relaxed = true)
        val onInvalid = mockk<(         ) -> Unit>(relaxed = true)

        Form { this (
                listOf(23) to checkList(1, 2, 3),
                onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (emptyList()) }
        verify(exactly = 0) { onInvalid(           ) }
    }

    @Test @JsName("checkListPartiallyAcceptsInValidInitialValue")
    fun `check list partially accepts invalid initial value`() {
        val onValid   = mockk<(List<Int>) -> Unit>(relaxed = true)
        val onInvalid = mockk<(         ) -> Unit>(relaxed = true)

        Form { this (
                listOf(1, 3, 23, 5) to checkList(1, 2, 3, 4, 5),
                onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (listOf(1, 3)) }
        verify(exactly = 0) { onInvalid(            ) }
    }

    @Test @JsName("switchListDefaultsToEmpty")
    fun `switch list defaults to empty`() {
        val onValid   = mockk<(List<Int>) -> Unit>(relaxed = true)
        val onInvalid = mockk<(         ) -> Unit>(relaxed = true)

        Form { this (
                + switchList(1, 2, 3),
                onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (emptyList()) }
        verify(exactly = 0) { onInvalid(           ) }
    }

    @Test @JsName("switchListAcceptsValidInitialValue")
    fun `switch list accepts valid initial value`() {
        val onValid   = mockk<(List<Int>) -> Unit>(relaxed = true)
        val onInvalid = mockk<(         ) -> Unit>(relaxed = true)

        Form { this (
                listOf(2, 3) to switchList(1, 2, 3),
                onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (listOf(2, 3)) }
        verify(exactly = 0) { onInvalid(            ) }
    }

    @Test @JsName("switchListIgnoresInValidInitialValue")
    fun `switch list ignores invalid initial value`() {
        val onValid   = mockk<(List<Int>) -> Unit>(relaxed = true)
        val onInvalid = mockk<(         ) -> Unit>(relaxed = true)

        Form { this (
                listOf(23) to switchList(1, 2, 3),
                onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (emptyList()) }
        verify(exactly = 0) { onInvalid(           ) }
    }

    @Test @JsName("switchListPartiallyAcceptsInValidInitialValue")
    fun `switch list partially accepts invalid initial value`() {
        val onValid   = mockk<(List<Int>) -> Unit>(relaxed = true)
        val onInvalid = mockk<(         ) -> Unit>(relaxed = true)

        Form { this (
                listOf(1, 3, 23, 5) to switchList(1, 2, 3, 4, 5),
                onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (listOf(1, 3)) }
        verify(exactly = 0) { onInvalid(            ) }
    }

    @Test @JsName("dropDownDefaultsToFirst")
    fun `dropdown defaults to first`() {
        val onValid   = mockk<(Gender) -> Unit>(relaxed = true)
        val onInvalid = mockk<(      ) -> Unit>(relaxed = true)

        Form { this (
            + dropDown(Female, Male),
            onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (Female) }
        verify(exactly = 0) { onInvalid(      ) }
    }

    @Test @JsName("dropDownAcceptsValidInitialValue")
    fun `dropdown accepts valid initial value`() {
        val onValid   = mockk<(Gender) -> Unit>(relaxed = true)
        val onInvalid = mockk<(      ) -> Unit>(relaxed = true)

        Form { this (
            Male to dropDown(Female, Male),
            onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (Male) }
        verify(exactly = 0) { onInvalid(    ) }
    }

    @Test @JsName("dropDownIgnoresInValidInitialValue")
    fun `dropdown ignores invalid initial value`() {
        val onValid   = mockk<(Int) -> Unit>(relaxed = true)
        val onInvalid = mockk<(   ) -> Unit>(relaxed = true)

        Form { this (
            23 to dropDown(1, 2, 3),
            onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (1) }
        verify(exactly = 0) { onInvalid( ) }
    }

    @Test @JsName("deselectableDropDownDefaultsToInvalid")
    fun `deselectable dropdown defaults to invalid`() {
        val onValid   = mockk<(Gender) -> Unit>(relaxed = true)
        val onInvalid = mockk<(      ) -> Unit>(relaxed = true)

        Form { this (
            + dropDown(Female, Male, unselectedLabel = "unselected"),
            onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 0) { onValid  (any()) }
        verify(exactly = 0) { onInvalid(     ) }
    }

    @Test @JsName("deselectableDropDownAcceptsValidInitialValue")
    fun `deselectable dropdown accepts valid initial value`() {
        val onValid   = mockk<(Gender) -> Unit>(relaxed = true)
        val onInvalid = mockk<(      ) -> Unit>(relaxed = true)

        Form { this (
                Male to dropDown(Female, Male, unselectedLabel = "unselected"),
                onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (Male) }
        verify(exactly = 0) { onInvalid(    ) }
    }

    @Test @JsName("deselectableDropDownIgnoresInValidInitialValue")
    fun `deselectable dropdown ignores invalid initial value`() {
        val onValid   = mockk<(Int) -> Unit>(relaxed = true)
        val onInvalid = mockk<(   ) -> Unit>(relaxed = true)

        Form { this (
                23 to dropDown(1, 2, 3, unselectedLabel = "unselected"),
                onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 0) { onValid  (any()) }
        verify(exactly = 0) { onInvalid(     ) }
    }

    @Test @JsName("optionalDropDownDefaultsToNull")
    fun `optional dropdown defaults to null`() {
        val onValid   = mockk<(Gender?) -> Unit>(relaxed = true)
        val onInvalid = mockk<(       ) -> Unit>(relaxed = true)

        Form { this (
            + optionalDropDown(Female, Male, unselectedLabel = "unselected"),
            onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (null) }
        verify(exactly = 0) { onInvalid(    ) }
    }

    @Test @JsName("optionalDropDownAcceptsValidInitialValue")
    fun `optional dropdown accepts valid initial value`() {
        val onValid   = mockk<(Gender?) -> Unit>(relaxed = true)
        val onInvalid = mockk<(       ) -> Unit>(relaxed = true)

        Form { this (
                Male to optionalDropDown(Female, Male, unselectedLabel = "unselected"),
                onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (Male) }
        verify(exactly = 0) { onInvalid(    ) }
    }

    @Test @JsName("optionalDropDownIgnoresInValidInitialValue")
    fun `optional dropdown ignores invalid initial value`() {
        val onValid   = mockk<(Int?) -> Unit>(relaxed = true)
        val onInvalid = mockk<(    ) -> Unit>(relaxed = true)

        Form { this (
                23 to optionalDropDown(1, 2, 3, unselectedLabel = "unselected"),
                onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (null) }
        verify(exactly = 0) { onInvalid(    ) }
    }

    @Test @JsName("listDefaultsToEmpty")
    fun `list defaults to empty`() {
        val onValid   = mockk<(List<Int>) -> Unit>(relaxed = true)
        val onInvalid = mockk<(         ) -> Unit>(relaxed = true)

        Form { this (
            + list(1, 2, 3),
            onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (emptyList()) }
        verify(exactly = 0) { onInvalid(           ) }
    }

    @Test @JsName("listAcceptsValidInitialValue")
    fun `list accepts valid initial value`() {
        val onValid   = mockk<(List<Int>) -> Unit>(relaxed = true)
        val onInvalid = mockk<(         ) -> Unit>(relaxed = true)

        Form { this (
                listOf(2, 3) to list(1, 2, 3),
                onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (listOf(2, 3)) }
        verify(exactly = 0) { onInvalid(            ) }
    }

    @Test @JsName("listIgnoresInValidInitialValue")
    fun `list ignores invalid initial value`() {
        val onValid   = mockk<(List<Int>) -> Unit>(relaxed = true)
        val onInvalid = mockk<(         ) -> Unit>(relaxed = true)

        Form { this (
                listOf(23) to list(1, 2, 3),
                onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (emptyList()) }
        verify(exactly = 0) { onInvalid(           ) }
    }

    @Test @JsName("listPartiallyAcceptsInValidInitialValue")
    fun `list partially accepts invalid initial value`() {
        val onValid   = mockk<(List<Int>) -> Unit>(relaxed = true)
        val onInvalid = mockk<(         ) -> Unit>(relaxed = true)

        Form { this (
                listOf(1, 3, 23, 5) to list(1, 2, 3, 4, 5),
                onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (listOf(1, 3)) }
        verify(exactly = 0) { onInvalid(            ) }
    }

    private fun <T> textFieldValidInitialValue(
            initial     : T,
            pattern     : Regex          = Regex(".*"),
            encoder     : Encoder<T, String>,
            validator   : (T) -> Boolean = { true },
            expectedText: String
    ): TextField {
        val onValid   = mockk<(T) -> Unit>(relaxed = true)
        val onInvalid = mockk<( ) -> Unit>(relaxed = true)
        lateinit var textField: TextField

        Form { this (
            initial to textField(pattern, encoder, validator) { textField = this.textField },
            onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 1) { onValid  (initial) }
        verify(exactly = 0) { onInvalid(       ) }

        expect(expectedText) { textField.text }

        return textField
    }

    private inline fun <reified T> textFieldInvalidInitialValue(
            initial     : T,
            pattern     : Regex          = Regex(".*"),
            encoder     : Encoder<T, String>,
            noinline validator: (T) -> Boolean = { true },
            expectedText: String
    ): TextField {
        val onValid   = mockk<(T) -> Unit>(relaxed = true)
        val onInvalid = mockk<( ) -> Unit>(relaxed = true)
        lateinit var textField: TextField

        Form { this (
            initial to textField(pattern, encoder, validator) { textField = this.textField },
            onInvalid = onInvalid
        ) {
            onValid(it)
        } }

        verify(exactly = 0) { onValid  (any()) }
        verify(exactly = 0) { onInvalid(     ) }
        expect(expectedText) { textField.text   }

        return textField
    }
}