package io.nacular.doodle.utils

import io.mockk.mockk
import io.mockk.verify
import JsName
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 4/5/18.
 */
class ObservableSetTests {
    @Test @JsName("addNotifies")
    fun `add notifies`() {
        validateChanges(ObservableSet<Int>()) { set, changed ->
            set += 4
            set += 5
            set += listOf(6, 7, 8)

            verify { changed(set, emptySet(), setOf(4      )) }
            verify { changed(set, emptySet(), setOf(5      )) }
            verify { changed(set, emptySet(), setOf(6, 7, 8)) }

            expect(true) { (4 .. 8).all { it in set } }
        }
    }

    @Test @JsName("removeNotifies")
    fun `remove notifies`() {
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

    @Test @JsName("removeUnknownNoops")
    fun `remove unknown no-ops`() {
        validateChanges(ObservableSet(mutableSetOf(4, 5, 6, 7, 8))) { set, changed ->
            set -= 1
            set -= 2
            set -= listOf(0, 9, 10)

            verify(exactly = 0) { changed(any(), any(), any()) }

            expect(5    ) { set.size      }
            expect(false) { set.isEmpty() }
        }
    }

    @Test @JsName("clearNotifies")
    fun `clear notifies`() {
        validateChanges(ObservableSet(mutableSetOf(4, 5, 6, 7, 8))) { set, changed ->
            set.clear()

            verify(exactly = 1) { changed(set, setOf(4, 5, 6, 7, 8), emptySet()) }

            expect(0   ) { set.size      }
            expect(true) { set.isEmpty() }
        }
    }

    @Test @JsName("retainAllNotifies")
    fun `retainAll notifies`() {
        validateChanges(ObservableSet(mutableSetOf(4, 5, 6, 7, 8))) { set, changed ->
            set.retainAll(setOf(5, 6))

            verify(exactly = 1) { changed(set, setOf(4, 7, 8), emptySet()) }

            expect(2    ) { set.size      }
            expect(false) { set.isEmpty() }
        }
    }

    @Test @JsName("replaceAllNoNewNotifies")
    fun `replace all no new notifies`() {
        validateChanges(ObservableSet(mutableSetOf(4, 5, 6, 7, 8))) { set, changed ->
            set.replaceAll(setOf(5, 6))

            verify(exactly = 1) { changed(set, setOf(4, 7, 8), emptySet()) }

            expect(2    ) { set.size      }
            expect(false) { set.isEmpty() }
        }
    }

    @Test @JsName("replaceAllNewNotifies")
    fun `replace all new notifies`() {
        validateChanges(ObservableSet(mutableSetOf(4, 5, 6, 7, 8))) { set, changed ->
            set.replaceAll(setOf(1, 2, 3))

            verify(exactly = 1) { changed(set, setOf(4, 5, 6, 7, 8), setOf(1, 2, 3)) }

            expect(3    ) { set.size      }
            expect(false) { set.isEmpty() }
        }
    }

    @Test @JsName("replaceAllSameNoops")
    fun `replace all same no-ops`() {
        validateChanges(ObservableSet(mutableSetOf(4, 5, 6, 7, 8))) { set, changed ->
            set.replaceAll(setOf(4, 5, 6, 7, 8))

            verify(exactly = 0) { changed(any(), any(), any()) }

            expect(5    ) { set.size      }
            expect(false) { set.isEmpty() }
        }
    }

    @Test @JsName("batchingNotifies")
    fun `batching notifies`() {
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
    @Test @JsName("addNotifies")
    fun `add notifies`() {
        validateChanges(ObservableList<Int>()) { list, changed ->
            list += 4
            list += 5
            list += listOf(6, 7, 8)

            verify(exactly = 1) { changed(list, emptyMap(), mapOf(0 to 4                ), emptyMap()) }
            verify(exactly = 1) { changed(list, emptyMap(), mapOf(1 to 5                ), emptyMap()) }
            verify(exactly = 1) { changed(list, emptyMap(), mapOf(2 to 6, 3 to 7, 4 to 8), emptyMap()) }

            expect(true) { (4 .. 8).all { it in list } }
        }
    }

    @Test @JsName("insertNotifies")
    fun `insert notifies`() {
        validateChanges(ObservableList<Int>()) { list, changed ->
            list += 4
            list += 6
            list.add(1, 5)

            verify(exactly = 1) { changed(list, emptyMap(), mapOf(0 to 4), emptyMap()) }
            verify(exactly = 1) { changed(list, emptyMap(), mapOf(1 to 6), emptyMap()) }
            verify(exactly = 1) { changed(list, emptyMap(), mapOf(1 to 5), emptyMap()) }

            expect(true) { (4 .. 6).all { it in list } }
        }
    }

    @Test @JsName("setNotifies")
    fun `set notifies`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list[1] = 10
            list[2] = 12

            verify(exactly = 1) { changed(list, mapOf(1 to 5), mapOf(1 to 10), emptyMap()) }
            verify(exactly = 1) { changed(list, mapOf(2 to 6), mapOf(2 to 12), emptyMap()) }

            expect(5    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test @JsName("removeNotifies")
    fun `remove notifies`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list -= 4
            list.removeAt(0)
            list -= listOf(6, 7, 8)

            verify(exactly = 1) { changed(list, mapOf(0 to 4                ), emptyMap(), emptyMap()) }
            verify(exactly = 1) { changed(list, mapOf(0 to 5                ), emptyMap(), emptyMap()) }
            verify(exactly = 1) { changed(list, mapOf(0 to 6, 1 to 7, 2 to 8), emptyMap(), emptyMap()) }

            expect(0   ) { list.size      }
            expect(true) { list.isEmpty() }
        }
    }

    @Test @JsName("removeUnknownNoops")
    fun `remove unknown no-ops`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list -= 1
            list -= 2
            list -= listOf(0, 9, 10)

            verify(exactly = 0) { changed(any(), any(), any(), any()) }

            expect(5    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test @JsName("clearNotifies")
    fun `clear notifies`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list.clear()

            verify(exactly = 1) { changed(list, mapOf(0 to 4, 1 to 5, 2 to 6, 3 to 7, 4 to 8), emptyMap(), emptyMap()) }

            expect(0   ) { list.size      }
            expect(true) { list.isEmpty() }
        }
    }

    @Test @JsName("addAllNotifies")
    fun `addAll notifies`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list.addAll(1, setOf(1, 2))

            verify(exactly = 1) { changed(list, emptyMap(), mapOf(1 to 1, 2 to 2), emptyMap()) }

            expect(7    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test @JsName("retainAllNotifies")
    fun `retainAll notifies`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list.retainAll(setOf(5, 6))

            verify(exactly = 1) { changed(list, mapOf(0 to 4, 3 to 7, 4 to 8), emptyMap(), emptyMap()) }

            expect(2    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test @JsName("replaceAllNoNewNotifies")
    fun `replace all no new notifies`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list.replaceAll(setOf(5, 6))

            verify(exactly = 1) { changed(list, mapOf(0 to 4, 3 to 7, 4 to 8), emptyMap(), emptyMap()) }

            expect(2    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test @JsName("replaceAllNewNotifies")
    fun `replace all new notifies`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list.replaceAll(setOf(1, 2, 3))

            verify(exactly = 1) { changed(list, mapOf(0 to 4, 1 to 5, 2 to 6, 3 to 7, 4 to 8), mapOf(0 to 1, 1 to 2, 2 to 3), emptyMap()) }

            expect(3    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test @JsName("replaceAllSameNoops")
    fun `replace all same no-ops`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list.replaceAll(setOf(4, 5, 6, 7, 8))

            verify(exactly = 0) { changed(any(), any(), any(), any()) }

            expect(5    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test @JsName("batchingNotifies")
    fun `batching notifies`() {
        validateChanges(ObservableList(mutableListOf(4, 5, 6, 7, 8))) { list, changed ->
            list.batch {
                remove(8)
                addAll(listOf(3, 67))
                add(8)
            }

            verify(exactly = 1) { changed(list, emptyMap(), mapOf(4 to 3, 5 to 67), emptyMap()) }

            expect(7    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test @Ignore @JsName("moveNotifies")
    fun `move notifies`() {
        validateChanges(ObservableList(mutableListOf('a', 'b', 'c', 'd'))) { list, changed ->
            list.batch {
                remove('a')
                add(2, 'a')
            }

            verify(exactly = 1) { changed(list, emptyMap(), emptyMap(), mapOf(0 to (2 to 'a'))) }

            expect(4    ) { list.size      }
            expect(false) { list.isEmpty() }
        }
    }

    @Test @JsName("sortNotifies")
    fun `sort notifies`() {
        validateChanges(ObservableList(mutableListOf(5, 6, 1, 2))) { list, changed ->
            list.sortWith { a, b -> a.compareTo(b) }

            verify(exactly = 1) { changed(
                    list,
                    emptyMap(),
                    emptyMap(),
                    mapOf(0 to (2 to 5), 1 to (3 to 6))) }

            expect(listOf(1, 2, 5, 6)) { list }
            expect(false) { list.isEmpty() }
        }
    }

    private fun <T> validateChanges(list: ObservableList<T>, block: (ObservableList<T>, ListObserver<ObservableList<T>, T>) -> Unit) {
        val changed = mockk<ListObserver<ObservableList<T>, T>>()

        list.changed += changed

        block(list, changed)
    }
}