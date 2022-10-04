package io.nacular.doodle.utils

import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import io.nacular.doodle.utils.diff.Delete
import io.nacular.doodle.utils.diff.Differences
import io.nacular.doodle.utils.diff.Equal
import io.nacular.doodle.utils.diff.Insert
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 1/31/21.
 */
class FilteredListTests {
    @Test fun `iterating when filtered works`() {
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

        expect(filteredList) { listOf(0,2,4) }
    }

    @Test fun `changing filter works`() {
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

    @Test fun `remove from source when filtered works`() {
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

    @Test fun `remove when filtered works`() {
        val source   = ObservableList(listOf(0,1,2,3,4,5))
        val listener = mockk<ListObserver<ObservableList<Int>, Int>>()

        val filteredList = FilteredList(source).apply {
            filter = { it.isEven }
            changed += listener
        }

        filteredList.batch {
            remove(2)
            remove(0)
        }

        expect(listOf(4)) { filteredList.toList() }
        verify(exactly = 1) { listener(filteredList, Differences(listOf(Delete(0,2), Equal (4)))) }
        expect(listOf(1,3,4,5)) { source.toList() }
    }

    @Test fun `remove front when no-op filtered works`() {
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

    @Test fun `set when filtered works`() {
        val source   = ObservableList(listOf(0,1,2,3,4,5))
        val listener = mockk<ListObserver<ObservableList<Int>, Int>>()

        val filteredList = FilteredList(source).apply {
            filter = { it.isEven }
            changed += listener
        }

        source[2] = 3

        verify(exactly = 1) { listener(filteredList, Differences(listOf(Equal(0), Delete(2), Equal(4)))) }
    }

    @Test fun `set when no-op filtered works`() {
        val source   = ObservableList(listOf(0,2,4))
        val listener = mockk<ListObserver<ObservableList<Int>, Int>>()

        val filteredList = FilteredList(source).apply {
            filter = { it.isEven }
            changed += listener
        }

        source[1] = 3

        verify(exactly = 1) { listener(filteredList, Differences(listOf(Equal(0), Delete(2), Equal(4)))) }
    }

    @Test fun `toggle when filtered works`() {
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

    @Test fun `add to source when filtered works`() {
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

        verify(exactly = 1) { listener(filteredList, Differences(listOf(Equal(0,2), Insert(2), Equal(4), Insert(6)))) }

        expect(listOf(0,1,2,2,3,4,5,6)) { source.toList() }
    }

    @Test fun `add when filtered works`() {
        val source   = ObservableList(listOf(0,1,2,3,4,5))
        val listener = mockk<ListObserver<ObservableList<Int>, Int>>()

        val filteredList = FilteredList(source).apply {
            filter = { it.isEven }
            changed += listener
        }

        filteredList.batch {
            add(2, 2)
            add(6)
        }

        expect(listOf(0,2,2,4,6)) { filteredList.toList() }
        verify(exactly = 1) { listener(filteredList, Differences(listOf(Equal(0,2), Insert(2), Equal(4), Insert(6)))) }

        expect(listOf(0,1,2,3,2,4,6,5)) { source.toList() }
    }

    @Test fun `move when filtered works`() {
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

    @Test fun `clear when filtered works`() {
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

    @Test fun `batch when filtered works`() {
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

    @Test fun `index out of bounds for get`() {
        val source = ObservableList(listOf(0,1,2,3,4,5))

        val filteredList = FilteredList(source).apply {
            filter = { it.isEven }
        }

        assertThrows<IndexOutOfBoundsException> {
            filteredList[3]
        }
    }
}