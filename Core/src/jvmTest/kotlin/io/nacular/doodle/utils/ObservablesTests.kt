package io.nacular.doodle.utils

import io.mockk.mockk
import io.mockk.verify
import io.nacular.doodle.utils.diff.Delete
import io.nacular.doodle.utils.diff.Differences
import io.nacular.doodle.utils.diff.Equal
import io.nacular.doodle.utils.diff.Insert
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 4/5/18.
 */
class ObservableSetTests {
    @Test fun `add notifies`() {
        validateChanges<Int>(ObservableSet()) { set, changed ->
            set += 4
            set += 5
            set += listOf(6, 7, 8)

            verify { changed(set, emptySet(), setOf(4      )) }
            verify { changed(set, emptySet(), setOf(5      )) }
            verify { changed(set, emptySet(), setOf(6, 7, 8)) }

            expect(true) { (4 .. 8).all { it in set } }
        }
    }

    @Test fun `remove notifies`() {
        validateChanges(ObservableSet(mutableSetOf(4, 5, 6, 7, 8))) { set, changed ->
            set -= 4
            set -= 5
            set -= listOf(6, 7, 8)

            verify(exactly = 1) { changed(set, setOf(4      ), emptySet()) }
            verify(exactly = 1) { changed(set, setOf(5      ), emptySet()) }
            verify(exactly = 1) { changed(set, setOf(6, 7, 8), emptySet()) }

            expect(0   ) { set.size      }
            expect(true) { set.isEmpty() }
        }
    }

    @Test fun `remove unknown no-ops`() {
        validateChanges(ObservableSet(mutableSetOf(4, 5, 6, 7, 8))) { set, changed ->
            set -= 1
            set -= 2
            set -= listOf(0, 9, 10)

            verify(exactly = 0) { changed(any(), any(), any()) }

            expect(5    ) { set.size      }
            expect(false) { set.isEmpty() }
        }
    }

    @Test fun `clear notifies`() {
        validateChanges(ObservableSet(mutableSetOf(4, 5, 6, 7, 8))) { set, changed ->
            set.clear()

            verify(exactly = 1) { changed(set, setOf(4, 5, 6, 7, 8), emptySet()) }

            expect(0   ) { set.size      }
            expect(true) { set.isEmpty() }
        }
    }

    @Test fun `retainAll notifies`() {
        validateChanges(ObservableSet(mutableSetOf(4, 5, 6, 7, 8))) { set, changed ->
            set.retainAll(setOf(5, 6))

            verify(exactly = 1) { changed(set, setOf(4, 7, 8), emptySet()) }

            expect(2    ) { set.size      }
            expect(false) { set.isEmpty() }
        }
    }

    @Test fun `replace all no new notifies`() {
        validateChanges(ObservableSet(mutableSetOf(4, 5, 6, 7, 8))) { set, changed ->
            set.replaceAll(setOf(5, 6))

            verify(exactly = 1) { changed(set, setOf(4, 7, 8), emptySet()) }

            expect(2    ) { set.size      }
            expect(false) { set.isEmpty() }
        }
    }

    @Test fun `replace all new notifies`() {
        validateChanges(ObservableSet(mutableSetOf(4, 5, 6, 7, 8))) { set, changed ->
            set.replaceAll(setOf(1, 2, 3))

            verify(exactly = 1) { changed(set, setOf(4, 5, 6, 7, 8), setOf(1, 2, 3)) }

            expect(3    ) { set.size      }
            expect(false) { set.isEmpty() }
        }
    }

    @Test fun `replace all same no-ops`() {
        validateChanges(ObservableSet(mutableSetOf(4, 5, 6, 7, 8))) { set, changed ->
            set.replaceAll(setOf(4, 5, 6, 7, 8))

            verify(exactly = 0) { changed(any(), any(), any()) }

            expect(5    ) { set.size      }
            expect(false) { set.isEmpty() }
        }
    }

    @Test fun `batching notifies`() {
        validateChanges(ObservableSet(mutableSetOf(4, 5, 6, 7, 8))) { set, changed ->
            set.batch {
                remove(5)
                remove(7)
                addAll(setOf(1, 2, 3))
            }

            verify(exactly = 1) { changed(set, setOf(5, 7), setOf(1, 2, 3)) }
        }
    }

    private fun <T> validateChanges(set: ObservableSet<T>, block: (ObservableSet<T>, SetObserver<ObservableSet<T>, T>) -> Unit) {
        val changed = mockk<SetObserver<ObservableSet<T>, T>>()

        set.changed += changed

        block(set, changed)
    }
}

class ObservableListTests {
    @Test fun `add notifies`() {
        validateChanges(ObservableList<Int>()) { list, changed ->
            list += 4
            list += 5
            list += listOf(6, 7, 8)

            verify(exactly = 1) { changed(list, Differences(listOf(            Insert(listOf(4    ))))) }
            verify(exactly = 1) { changed(list, Differences(listOf(Equal(4  ), Insert(listOf(5    ))))) }
            verify(exactly = 1) { changed(list, Differences(listOf(Equal(4,5), Insert(listOf(6,7,8))))) }

            expect(true) { (4 .. 8).all { it in list } }
        }
    }

    @Test fun `insert notifies`() {
        validateChanges(ObservableList()) { list, changed ->
            list += 4
            list += 6
            list.add(1, 5)

            verify(exactly = 1) { changed(list, Differences(listOf(          Insert(4          )))) }
            verify(exactly = 1) { changed(list, Differences(listOf(Equal(4), Insert(6          )))) }
            verify(exactly = 1) { changed(list, Differences(listOf(Equal(4), Insert(5), Equal(6)))) }

            expect(true) { (4 .. 6).all { it in list } }
        }
    }

    @Test fun `set notifies`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list[1] = 10
            list[2] = 12

            verify(exactly = 1) { changed(list, Differences(listOf(Equal(4   ), Delete(5), Insert(10), Equal(12,7,8)))) }
            verify(exactly = 1) { changed(list, Differences(listOf(Equal(4,10), Delete(6), Insert(12), Equal(   7,8)))) }

            expect(5    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test fun `remove notifies`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list -= 4
            list.removeAt(0)
            list -= listOf(6, 7, 8)

            verify(exactly = 1) { changed(list, Differences(listOf(Delete(4    ), Equal(5,6,7,8)))) }
            verify(exactly = 1) { changed(list, Differences(listOf(Delete(5    ), Equal(  6,7,8)))) }
            verify(exactly = 1) { changed(list, Differences(listOf(Delete(6,7,8)                ))) }

            expect(0   ) { list.size      }
            expect(true) { list.isEmpty() }
        }
    }

    @Test fun `remove unknown no-ops`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list -= 1
            list -= 2
            list -= listOf(0, 9, 10)

            verify(exactly = 0) { changed(any(), any()) }

            expect(5    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test fun `clear notifies`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list.clear()

            verify(exactly = 1) { changed(list, Differences(listOf(Delete(4,5,6,7,8)))) }

            expect(0   ) { list.size      }
            expect(true) { list.isEmpty() }
        }
    }

    @Test fun `addAll notifies`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list.addAll(1, setOf(1, 2))

            verify(exactly = 1) { changed(list, Differences(listOf(Equal(4), Insert(1, 2), Equal(5,6,7,8)))) }

            expect(7    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test fun `retainAll notifies`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list.retainAll(setOf(5, 6))

            verify(exactly = 1) { changed(list, Differences(listOf(Delete(4), Equal(5,6), Delete(7,8)))) }

            expect(2    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test fun `replace all no new notifies`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list.replaceAll(setOf(5, 6))

            verify(exactly = 1) { changed(list, Differences(listOf(Delete(4), Equal(5,6), Delete(7,8)))) }

            expect(2    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test fun `replace all new notifies`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list.replaceAll(setOf(1, 2, 3))

            verify(exactly = 1) { changed(list, Differences(listOf(Delete(4,5,6,7,8), Insert(1,2,3)))) }

            expect(3    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test fun `replace all same no-ops`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list.replaceAll(setOf(4, 5, 6, 7, 8))

            verify(exactly = 0) { changed(any(), any()) }

            expect(5    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test fun `batching notifies`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list.batch {
                remove(8)
                addAll(listOf(3, 67))
                add(8)
            }

            verify(exactly = 1) { changed(list, Differences(listOf(Equal(4,5,6,7), Insert(3,67), Equal(8)))) }

            expect(7    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test fun `batch move notifies`() {
        validateChanges(ObservableList(mutableListOf('a', 'b', 'c', 'd'))) { list, changed ->
            list.batch {
                remove('a')
                add(2, 'a')
            }

            verify(exactly = 1) { changed(list, Differences(listOf(Delete('a'), Equal('b','c'), Insert('a'), Equal('d')))) }

            expect(4    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test fun `move notifies`() {
        validateChanges(ObservableList(mutableListOf('a', 'b', 'c', 'd'))) { list, changed ->
            list.move('a', 2)

            verify(exactly = 1) { changed(list, Differences(listOf(Delete('a'), Equal('b','c'), Insert('a'), Equal('d')))) }

            expect(4    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
        validateChanges(ObservableList(mutableListOf('a', 'b', 'c', 'd'))) { list, changed ->
            list.move('a', 3)

            verify(exactly = 1) { changed(list, Differences(listOf(Delete('a'), Equal('b','c','d'), Insert('a')))) }

            expect(4    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
        validateChanges(ObservableList(mutableListOf('a', 'b', 'c', 'd'))) { list, changed ->
            list.move('d', 0)

            verify(exactly = 1) { changed(list, Differences(listOf(Insert('d'), Equal('a','b','c'), Delete('d')))) }

            expect(4    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
        validateChanges(ObservableList(mutableListOf('a', 'b', 'c', 'd'))) { list, changed ->
            list.move('d', 1)

            verify(exactly = 1) { changed(list, Differences(listOf(Equal('a'), Insert('d'), Equal('b','c'), Delete('d')))) }

            expect(4    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
        validateChanges(ObservableList(mutableListOf('a', 'b', 'c'))) { list, changed ->
            list.move('b', 0)

            verify(exactly = 1) { changed(list, Differences(listOf(Insert('b'), Equal('a'), Delete('b'), Equal('c')))) }

            expect(3    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test fun `move unknown element no-ops`() {
        validateChanges(ObservableList(mutableListOf('a', 'b', 'c', 'd'))) { list, changed ->
            list.move('q', 5)

            verify(exactly = 0) { changed(list, any()) }

            expect(listOf('a', 'b', 'c', 'd')) { list }
        }
    }

    @Test fun `move to invalid index no-ops`() {
        validateChanges(ObservableList(mutableListOf('a', 'b', 'c', 'd'))) { list, changed ->
            list.move('b', 5)

            verify(exactly = 0) { changed(list, any()) }

            expect(listOf('a', 'b', 'c', 'd')) { list }
        }
    }

    @Test fun `sort notifies`() {
        validateChanges(ObservableList(mutableListOf(5, 6, 1, 2))) { list, changed ->
            list.sortWith { a, b -> a.compareTo(b) }

            verify(exactly = 1) { changed(list, Differences(listOf(Insert(1,2), Equal(5,6), Delete(1,2)))) }

            expect(listOf(1, 2, 5, 6)) { list }
            expect(false) { list.isEmpty() }
        }
    }

    @Test fun `remove by iterator works`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            val it = list.iterator()
            it.next()
            it.remove()
            it.next()
            it.remove()

            verify(exactly = 1) { changed(list, Differences(listOf(Delete(4), Equal(5,6,7,8)))) }
            verify(exactly = 1) { changed(list, Differences(listOf(Delete(5), Equal(  6,7,8)))) }

            expect(listOf(6,7,8)) { list }
        }
    }

    @Test fun `iterator hasNext works`() {
        validateChanges(ObservableList(mutableListOf(4, 5))) { list, changed ->
            val it = list.iterator()
            it.next()
            it.remove()
            it.next()
            it.remove()

            verify(exactly = 1) { changed(list, Differences(listOf(Delete(4), Equal(5)))) }
            verify(exactly = 1) { changed(list, Differences(listOf(Delete(5)          ))) }

            expect(false) { it.hasNext() }
            expect(listOf()) { list }
        }
    }

    @Test fun `change front works`() {
        validateChanges(ObservableList(listOf(0, 1, 2, 3, 4, 5))) { list, changed ->
            list[0] = 34

            expect(listOf(34, 1, 2, 3, 4, 5)) { list }
            verify(exactly = 1) { changed(list, Differences(listOf(Delete(0), Insert(34), Equal(1, 2, 3, 4, 5)))) }
        }
    }

    @Test fun `change keep middle works`() {
        validateChanges(ObservableList(listOf(0, 1, 0))) { list, changed ->
            list.batch {
                this[0] = 1
                this[2] = 1
            }

            expect(listOf(1,1,1)) { list }
            verify(exactly = 1) { changed(list, Differences(listOf(Delete(0), Equal(1), Delete(0), Insert(1, 1)))) }
        }
    }

    private fun <T> validateChanges(list: ObservableList<T>, block: (ObservableList<T>, ListObserver<ObservableList<T>, T>) -> Unit) {
        val changed = mockk<ListObserver<ObservableList<T>, T>>()

        list.changed += changed

        block(list, changed)
    }
}