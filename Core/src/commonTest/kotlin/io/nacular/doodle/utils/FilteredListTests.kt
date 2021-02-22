package io.nacular.doodle.utils

import io.mockk.mockk
import io.mockk.verify
import kotlin.js.JsName
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

        verify(exactly = 1) { listener(filteredList, mapOf(1 to 1, 3 to 3, 5 to 5), emptyMap(), emptyMap()) }
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

        verify(exactly = 1) { listener(filteredList, mapOf(1 to 2, 0 to 0), emptyMap(), emptyMap()) }
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

        verify(exactly = 1) { listener(filteredList, mapOf(0 to 1), emptyMap(), emptyMap()) }
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

        verify(exactly = 1) { listener(filteredList, mapOf(1 to 2), emptyMap(), emptyMap()) }
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

        verify(exactly = 1) { listener(filteredList, mapOf(1 to 2), emptyMap(), emptyMap()) }
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

        verify(exactly = 1) { listener(filteredList, mapOf(1 to 2), emptyMap(), emptyMap()) }

        source[1] = 2

        verify(exactly = 1) { listener(filteredList, emptyMap(), mapOf(1 to 2), emptyMap()) }
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

        verify(exactly = 1) { listener(filteredList, emptyMap(), mapOf(2 to 2, 4 to 6), emptyMap()) }
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

        verify(exactly = 1) { listener(filteredList, emptyMap(), emptyMap(), mapOf(1 to (2 to 2), 2 to (1 to 4))) }
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

        verify(exactly = 1) { listener(filteredList, mapOf(0 to 0, 1 to 2, 2 to 4), emptyMap(), emptyMap()) }

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

        verify(exactly = 1) { listener(filteredList, mapOf(0 to 0, 1 to 2, 2 to 4), mapOf(0 to 6), emptyMap()) }

        expect(listOf(1,3,5,6)) { source }
    }
}