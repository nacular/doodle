package io.nacular.doodle.utils

import io.mockk.mockk
import io.mockk.verify
import JsName
import io.mockk.verifyOrder
import io.nacular.doodle.utils.diff.Delete
import io.nacular.doodle.utils.diff.Differences
import io.nacular.doodle.utils.diff.Equal
import io.nacular.doodle.utils.diff.Insert
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 1/31/21.
 */
class FilteredListTests {
    @Test @JsName("iteratingWhenFilteredWorks")
    fun `iterating when filtered works`() {
        val source = ObservableList(listOf(0,1,2,3,4,5))

        val filteredList = FilteredList(source).apply {
            filter = { it.isEven }
        }

        expect(3    ) { filteredList.size }
        expect(true ) { 0 in filteredList }
        expect(false) { 1 in filteredList }
        expect(true ) { 2 in filteredList }
        expect(false) { 3 in filteredList }
        expect(true ) { 4 in filteredList }
        expect(false) { 5 in filteredList }

        expect(listOf(0,1,2,3,4,5)) { source }

        expect(filteredList) { listOf(0,2,4) }

        var count = 0
        for (item in filteredList) {
            count++
        }

        expect(3) { count }

//        expect(listOf(0,2,4)) { filteredList }
    }

    @Test @JsName("changingFilterWorks")
    fun `changing filter works`() {
        val source   = ObservableList(listOf(0,1,2,3,4,5))
        val listener = mockk<ListObserver<ObservableList<Int>, Int>>()

        val filteredList = FilteredList(source).apply {
            changed += listener
        }

        filteredList.filter = { it.isEven }

        verify(exactly = 1) { listener(filteredList, Differences(listOf(
            Equal (0),
            Delete(1),
            Equal (2),
            Delete(3),
            Equal (4),
            Delete(5),
        ))) }
    }

    @Test @JsName("removeWhenFilteredWorks")
    fun `remove when filtered works`() {
        val source   = ObservableList(listOf(0,1,2,3,4,5))
        val listener = mockk<ListObserver<ObservableList<Int>, Int>>()

        val filteredList = FilteredList(source).apply {
            filter = { it.isEven }
            changed += listener
        }

        source.batch {
            remove(1)
            remove(2)
            remove(0)
        }

        verify(exactly = 1) { listener(filteredList, Differences(listOf(Delete(0,2), Equal (4)))) }
    }

    @Test @JsName("removeFrontWhenNoOpFilteredWorks")
    fun `remove front when no-op filtered works`() {
        val source   = ObservableList(listOf(1,2,3))
        val listener = mockk<ListObserver<ObservableList<Int>, Int>>()

        val filteredList = FilteredList(source).apply {
            filter = { true }
            changed += listener
        }

        source.batch {
            removeAt(0)
        }

        verify(exactly = 1) { listener(filteredList, Differences(listOf(Delete(1), Equal(2,3)))) }
    }

    @Test @JsName("setWhenFilteredWorks")
    fun `set when filtered works`() {
        val source   = ObservableList(listOf(0,1,2,3,4,5))
        val listener = mockk<ListObserver<ObservableList<Int>, Int>>()

        val filteredList = FilteredList(source).apply {
            filter = { it.isEven }
            changed += listener
        }

        source[2] = 3

        verify(exactly = 1) { listener(filteredList, Differences(listOf(Equal(0), Delete(2), Equal(4)))) }
    }

    @Test @JsName("setWhenNoOpFilteredWorks")
    fun `set when no-op filtered works`() {
        val source   = ObservableList(listOf(0,2,4))
        val listener = mockk<ListObserver<ObservableList<Int>, Int>>()

        val filteredList = FilteredList(source).apply {
            filter = { it.isEven }
            changed += listener
        }

        source[1] = 3

        verify(exactly = 1) { listener(filteredList, Differences(listOf(Equal(0), Delete(2), Equal(4)))) }
    }

    @Test @JsName("toggleWhenFilteredWorks")
    fun `toggle when filtered works`() {
        val source   = ObservableList(listOf(0,2,3,4))
        val listener = mockk<ListObserver<ObservableList<Int>, Int>>()

        val filteredList = FilteredList(source).apply {
            filter = { it.isEven }
            changed += listener
        }

        source[1] = 3

        verify(exactly = 1) { listener(filteredList, Differences(listOf(Equal(0), Delete(2), Equal(4)))) }

        source[1] = 2

        verify(exactly = 1) { listener(filteredList, Differences(listOf(Equal(0), Insert(2), Equal(4)))) }
    }

    @Test @JsName("addWhenFilteredWorks")
    fun `add when filtered works`() {
        val source   = ObservableList(listOf(0,1,2,3,4,5))
        val listener = mockk<ListObserver<ObservableList<Int>, Int>>()

        val filteredList = FilteredList(source).apply {
            filter = { it.isEven }
            changed += listener
        }

        source.batch {
            add(2, 2)
            add(6)
        }

        // 0,2,4
        // 0,2,2,4,6

        verify(exactly = 1) { listener(filteredList, Differences(listOf(Equal(0,2), Insert(2), Equal(4), Insert(6)))) }
    }

    @Test @JsName("moveWhenFilteredWorks")
    fun `move when filtered works`() {
        val source   = ObservableList(listOf(0,1,2,3,4,5))
        val listener = mockk<ListObserver<ObservableList<Int>, Int>>()

        val filteredList = FilteredList(source).apply {
            filter = { it.isEven }
            changed += listener
        }

        source.batch {
            clear()
            addAll(sequenceOf(0,1,3,4,5,2))
        }

        // 0,2,4 -> 0,2,4
        // 0,3,5 -> 0,4,2

        try {
            verifyOrder { listener(filteredList, Differences(listOf(Equal(0), Insert(4), Equal(2), Delete(4)))) }
        } catch (exception: AssertionError) {
            verifyOrder { listener(filteredList, Differences(listOf(Equal(0), Delete(2), Equal(4), Insert(2)))) }
        }
    }

    @Test @JsName("clearWhenFilteredWorks")
    fun `clear when filtered works`() {
        val source   = ObservableList(listOf(0,1,2,3,4,5))
        val listener = mockk<ListObserver<ObservableList<Int>, Int>>()

        val filteredList = FilteredList(source).apply {
            filter = { it.isEven }
            changed += listener
        }

        filteredList.clear()

        verify(exactly = 1) { listener(filteredList, Differences(listOf(Delete(0,2,4)))) }

        expect(listOf(1,3,5)) { source }
    }

    @Test @JsName("batchWhenFilteredWorks")
    fun `batch when filtered works`() {
        val source   = ObservableList(listOf(0,1,2,3,4,5))
        val listener = mockk<ListObserver<ObservableList<Int>, Int>>()

        val filteredList = FilteredList(source).apply {
            filter = { it.isEven }
            changed += listener
        }

        filteredList.batch {
            clear()
            add(6)
        }

        verify(exactly = 1) { listener(filteredList, Differences(listOf(Delete(0,2,4), Insert(6)))) }

        expect(listOf(1,3,5,6)) { source }
    }
}