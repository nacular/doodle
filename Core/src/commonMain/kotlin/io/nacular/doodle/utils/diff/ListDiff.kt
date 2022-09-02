package io.nacular.doodle.utils.diff

import io.nacular.doodle.utils.diff.Operation.Delete
import io.nacular.doodle.utils.diff.Operation.Equal
import io.nacular.doodle.utils.diff.Operation.Insert
import kotlin.math.min

/**
 * Classes are derived works from [https://github.com/danielearwicker/ListDiff]
 */

internal enum class Operation {
    Delete, Insert, Equal;

    override fun toString(): String {
        return when (this) {
            Delete -> "-"
            Insert -> "+"
            Equal  -> "="
        }
    }
}

internal data class Diff<T>(val operation: Operation, var items: List<T>) {
    override fun toString(): String = "$operation $items"
}

internal fun <T> compare(first: List<T>, second: List<T>, dualThreshold: Int = 32, by: (T, T) -> Boolean = { a, b -> a == b }): Iterable<Diff<T>> = compareInternal(first, second, dualThreshold, by)

private fun <T> compareInternal(first: List<T>, second: List<T>, dualThreshold: Int, by: (T, T) -> Boolean): MutableList<Diff<T>> {
    // Check for equality (speedup)
    if (first.isEqual(second, by)) {
        return mutableListOf(Diff(Equal, first))
    }

    // Trim off common prefix (speedup)
    var commonLength = getCommonPrefix(first, second)
    val commonPrefix = first.subListOfSize(0, commonLength)

    var text1 = first.subListOfSize(commonLength)
    var text2 = second.subListOfSize(commonLength)

    // Trim off common suffix (speedup)
    commonLength = getCommonSuffix(text1, text2)

    val commonSuffix = text1.subListOfSize(text1.size - commonLength)

    text1 = text1.subListOfSize(0, text1.size - commonLength)
    text2 = text2.subListOfSize(0, text2.size - commonLength)

    // Compute the diff on the middle block
    val diffs = compute(text1, text2, dualThreshold, by)

    // Restore the prefix and suffix
    if (commonPrefix.isNotEmpty()) {
        diffs.add(0, Diff(Equal, commonPrefix))
    }
    if (commonSuffix.isNotEmpty()) {
        diffs.add(Diff(Equal, commonSuffix))
    }

    cleanupMerge(diffs)
    return diffs
}

private fun <T> compute(first: List<T>, second: List<T>, dualThreshold: Int, equality: (T, T) -> Boolean): MutableList<Diff<T>> {
    if (first.isEmpty()) {
        // Just add some Items (speedup)
        return mutableListOf(Diff(Insert, second))
    }

    if (second.isEmpty()) {
        // Just delete some Items (speedup)
        return mutableListOf(Diff(Delete, first))
    }

    var diffs = mutableListOf<Diff<T>>()

    val longText  = if (first.size > second.size) first  else second
    val shortText = if (first.size > second.size) second else first
    val i = longText.indexOf(shortText, by = equality)

    if (i != -1) {
        // Shorter Items is inside the longer Items (speedup)
        val op = if (first.size > second.size) Delete else Insert
        diffs.add(Diff(op, longText.subListOfSize(0, i)))
        diffs.add(Diff(Equal, shortText))
        diffs.add(Diff(op, longText.subListOfSize(i + shortText.size)))
        return diffs
    }

    // Check to see if the problem can be split in two.
    val hm = getHalfMatch(first, second, equality)
    if (hm != null) {
        // A half-match was found, sort out the return data.
        val text1A = hm[0]
        val text1B = hm[1]
        val text2A = hm[2]
        val text2B = hm[3]
        val midCommon = hm[4]
        // Send both pairs off for separate processing.
        val diffsA = compareInternal(text1A, text2A, dualThreshold, equality)
        val diffsB = compareInternal(text1B, text2B, dualThreshold, equality)
        // Merge the results.
        diffs = diffsA
        diffs.add(Diff(Equal, midCommon))
        diffs.addAll(diffsB)
        return diffs
    }

    return getMap(first, second, dualThreshold, equality) ?: mutableListOf(Diff(Delete, first), Diff(Insert, second))
}

private fun <T> getMap(first: List<T>, second: List<T>, dualThreshold: Int, by: (T, T) -> Boolean): MutableList<Diff<T>>? {
    // Cache the Items lengths to prevent multiple calls.
    val firstLength  = first.size
    val secondLength = second.size
    val maxD         = firstLength + secondLength - 1
    val doubleEnd    = dualThreshold * 2 < maxD
    var vMap1        = mutableListOf<HashSet<Long>>()
    var vMap2        = mutableListOf<HashSet<Long>>()
    val v1           = mutableMapOf<Int, Int>().apply { this[1] = 0 }
    val v2           = mutableMapOf<Int, Int>().apply { this[1] = 0 }
    var footstep     = 0L  // Used to track overlapping paths.
    val footsteps    = mutableMapOf<Long, Int>()
    var done         = false
    val front        = ((firstLength + secondLength) % 2 == 1) // If the total number of characters is odd, then the front path will collide with the reverse path.

    repeat(maxD) { d ->
        // Walk the front path one step.
        vMap1.add(HashSet())  // Adds at index 'd'.
        var x: Int
        var y: Int

        for (k in -d..d step 2) {
            when {
                k == -d || k != d && v1[k - 1]!! < v1[k + 1]!! -> x = v1[k + 1]!!
                else                                           -> x = v1[k - 1]!! + 1
            }
            y = x - k
            if (doubleEnd) {
                footstep = getFootprint(x, y)
                if (front && (footsteps.containsKey(footstep))) {
                    done = true
                }
                if (!front) {
                    footsteps[footstep] = d
                }
            }
            while (!done && x < firstLength && y < secondLength && by(first[x], second[y])) {
                x++
                y++
                if (doubleEnd) {
                    footstep = getFootprint(x, y)
                    if (front && (footsteps.containsKey(footstep))) {
                        done = true
                    }
                    if (!front) {
                        footsteps[footstep] = d
                    }
                }
            }

            when {
                v1.containsKey(k) -> v1[k] = x
                else              -> v1[k] = x
            }

            vMap1[d].add(getFootprint(x, y))
            if (x == firstLength && y == secondLength) {
                // Reached the end in single-path mode.
                return diffPath1(vMap1, first, second)
            } else if (done) {
                // Front path ran over reverse path.
                vMap2 = vMap2.subList(0, footsteps[footstep]!! + 1)
                val a = diffPath1(vMap1, first.subListOfSize(0, x), second.subListOfSize(0, y))
                a.addAll(diffPath2(vMap2, first.subListOfSize(x), second.subListOfSize(y)))
                return a
            }
        }

        if (doubleEnd) {
            // Walk the reverse path one step.
            vMap2.add(HashSet())  // Adds at index 'd'.

            for (k in -d..d step 2) {
                when {
                    k == -d || k != d && v2[k - 1]!! < v2[k + 1]!! -> x = v2[k + 1]!!
                    else                                           -> x = v2[k - 1]!! + 1
                }
                y = x - k
                footstep = getFootprint(firstLength - x, secondLength - y)
                if (!front && (footsteps.containsKey(footstep))) {
                    done = true
                }
                if (front) {
                    footsteps[footstep] = d
                }
                while (!done && x < firstLength && y < secondLength && by(first[firstLength - x - 1], second[secondLength - y - 1])) {
                    x++
                    y++
                    footstep = getFootprint(firstLength - x, secondLength - y)
                    if (!front && (footsteps.containsKey(footstep))) {
                        done = true
                    }
                    if (front) {
                        footsteps[footstep] = d
                    }
                }
                when {
                    v2.containsKey(k) -> v2[k] = x
                    else              -> v2[k] = x
                }
                vMap2[d].add(getFootprint(x, y))
                if (done) {
                    // Reverse path ran over front path.
                    vMap1 = vMap1.subList(0, footsteps[footstep]!! + 1)
                    val a = diffPath1(vMap1, first.subListOfSize(0, firstLength - x), second.subListOfSize(0, secondLength - y))
                    a.addAll(diffPath2(vMap2, first.subListOfSize(firstLength - x), second.subListOfSize(secondLength - y)))
                    return a
                }
            }
        }
    }

    // Number of diffs equals number of characters, no commonality at all.
    return null
}

internal fun <T> diffPath1(vMap: List<Set<Long>>, first: List<T>, second: List<T>): MutableList<Diff<T>> {
    val path = mutableListOf<Diff<T>>()
    var x = first.size
    var y = second.size
    var lastOp = null as Operation?

    for (d in vMap.size - 2 downTo 0) {
        while (true) {
            if (vMap[d].contains(getFootprint(x - 1, y))) {
                x--
                when (lastOp) {
                    Delete -> path.first().items = first.subListOfSize(x, 1) + path.first().items
                    else   -> path.add(0, Diff(Delete, first.subListOfSize(x, 1)))
                }
                lastOp = Delete
                break
            }

            if (vMap[d].contains(getFootprint(x, y - 1))) {
                y--
                when (lastOp) {
                    Insert -> path.first().items = second.subListOfSize(y, 1) + path.first().items
                    else   -> path.add(0, Diff(Insert, second.subListOfSize(y, 1)))
                }
                lastOp = Insert
                break
            }

            x--
            y--
            when (lastOp) {
                Equal -> path.first().items = first.subListOfSize(x, 1) + path.first().items
                else  -> path.add(0, Diff(Equal, first.subListOfSize(x, 1)))
            }
            lastOp = Equal
        }
    }
    return path
}

internal fun <T> diffPath2(vMap: List<Set<Long>>, first: List<T>, second: List<T>): MutableList<Diff<T>> {
    val path = mutableListOf<Diff<T>>()
    var x = first.size
    var y = second.size
    var lastOp = null as Operation?
    for (d in vMap.size - 2 downTo 0) {
        while (true) {
            if (vMap[d].contains(getFootprint(x - 1, y))) {
                x--
                when (lastOp) {
                    Delete -> path.last().items = path.last().items + first.subListOfSize(first.size - x - 1, 1)
                    else   -> path.add(Diff(Delete, first.subListOfSize(first.size - x - 1, 1)))
                }
                lastOp = Delete
                break
            }

            if (vMap[d].contains(getFootprint(x, y - 1))) {
                y--
                when (lastOp) {
                    Insert -> path.last().items = path.last().items + second.subListOfSize(second.size - y - 1, 1)
                    else   -> path.add(Diff(Insert, second.subListOfSize(second.size - y - 1, 1)))
                }
                lastOp = Insert
                break
            }
            x--
            y--
            //assert (text1.charAt(text1.Count - x - 1)
            //        == text2.charAt(text2.Count - y - 1))
            //      : "No diagonal.  Can't happen. (DiffPath2)";
            when (lastOp) {
                Equal -> path.last().items = path.last().items + first.subListOfSize(first.size - x - 1, 1)
                else  -> path.add(Diff(Equal, first.subListOfSize(first.size - x - 1, 1)))
            }
            lastOp = Equal
        }
    }

    return path
}

internal fun getFootprint(x: Int, y: Int): Long {
    // The maximum size for a long is 9,223,372,036,854,775,807
    // The maximum size for an int is 2,147,483,647
    // Two Ints fit nicely in one long.
    var result: Long = x.toLong()
    result = result shl 32
    result += y
    return result
}

internal fun <T> getCommonPrefix(first: List<T>, second: List<T>): Int {
    // Performance analysis: http://neil.fraser.name/news/2007/10/09/
    val n = min(first.size, second.size)

    for (i in 0 until n) {
        if (first[i] != second[i]) {
            return i
        }
    }

    return n
}

internal fun <T> getCommonSuffix(first: List<T>, second: List<T>): Int {
    // Performance analysis: http://neil.fraser.name/news/2007/10/09/
    val n = min(first.size, second.size)

    for (i in 1 .. n) {
        if (first[first.size - i] != second[second.size - i]) {
            return i - 1
        }
    }
    return n
}

internal fun <T> getHalfMatch(first: List<T>, second: List<T>, by: (T, T) -> Boolean): Array<List<T>>? {
    val longText  = if (first.size > second.size) first else second
    val shortText = if (first.size > second.size) second else first

    if (longText.size < 10 || shortText.isEmpty()) {
        return null // Pointless
    }

    // First check if the second quarter is the seed for a half-match.
    val hm1 = getHalfMatchI(longText, shortText, (longText.size + 3) / 4, by)

    // Check again based on the third quarter.
    val hm2 = getHalfMatchI(longText, shortText, (longText.size + 1) / 2, by)

    val hm = when {
        hm1 == null && hm2 == null -> return null
        hm2 == null                -> hm1!!
        hm1 == null                -> hm2
        else                       -> if (hm1[4].size > hm2[4].size) hm1 else hm2 // Both matched.  Select the longest.
    }

    // A half-match was found, sort out the return data.
    return if (first.size > second.size) hm else arrayOf(hm[2], hm[3], hm[0], hm[1], hm[4])
}

private fun <T> getHalfMatchI(longList: List<T>, shortList: List<T>, index: Int, by: (T, T) -> Boolean): Array<List<T>>? {
    // Start with a 1/4 length Substring at position i as a seed.
    val seed = longList.subListOfSize(index, longList.size / 4)
    var j = shortList.indexOf(seed, 0, by)
    var bestCommon     = emptyList<T>()
    var bestLongTextA  = emptyList<T>()
    var bestLongTextB  = emptyList<T>()
    var bestShortTextA = emptyList<T>()
    var bestShortTextB = emptyList<T>()

    while (j < shortList.size && j != -1) {
        val prefixLength = getCommonPrefix(longList.subListOfSize(index), shortList.subListOfSize(j))
        val suffixLength = getCommonSuffix(longList.subListOfSize(0, index), shortList.subListOfSize(0, j))

        if (bestCommon.size < suffixLength + prefixLength) {
            bestCommon     = shortList.subListOfSize(j - suffixLength, suffixLength) + shortList.subListOfSize(j, prefixLength)
            bestLongTextA  = longList.subListOfSize (0, index - suffixLength)
            bestLongTextB  = longList.subListOfSize (index + prefixLength)
            bestShortTextA = shortList.subListOfSize(0, j - suffixLength)
            bestShortTextB = shortList.subListOfSize(j + prefixLength)
        }

        j = shortList.indexOf(seed, j + 1, by)
    }

    return when {
        bestCommon.size >= longList.size / 2 -> arrayOf(bestLongTextA, bestLongTextB, bestShortTextA, bestShortTextB, bestCommon)
        else                                 -> null
    }
}

internal fun <T> cleanupMerge(diffs: MutableList<Diff<T>>) {
    while (true) {
        diffs.add(Diff(Equal, emptyList())) // Add a dummy entry at the end.
        var pointer = 0
        var countDelete = 0
        var countInsert = 0
        var textDelete = emptyList<T>()
        var textInsert = emptyList<T>()

        while (pointer < diffs.size) {
            when(diffs[pointer].operation) {
                Insert -> {
                    countInsert++
                    textInsert = textInsert + diffs[pointer].items
                    pointer++
                }
                Delete -> {
                    countDelete++
                    textDelete = textDelete + diffs[pointer].items
                    pointer++
                }
                Equal -> {
                    // Upon reaching an equality, check for prior redundancies.
                    if (countDelete != 0 || countInsert != 0) {
                        if (countDelete != 0 && countInsert != 0) {
                            // Factor out any common prefixes.
                            var commonLength = getCommonPrefix(textInsert, textDelete)
                            if (commonLength != 0) {
                                if ((pointer - countDelete - countInsert) > 0 && diffs[pointer - countDelete - countInsert - 1].operation == Equal) {
                                    val diff = diffs[pointer - countDelete - countInsert - 1]
                                    diff.items = diff.items + textInsert.subListOfSize(0, commonLength)
                                } else {
                                    diffs.add(0, Diff(Equal, textInsert.subListOfSize(0, commonLength)))
                                    pointer++
                                }
                                textInsert = textInsert.subListOfSize(commonLength)
                                textDelete = textDelete.subListOfSize(commonLength)
                            }
                            // Factor out any common suffixes.
                            commonLength = getCommonSuffix(textInsert, textDelete)
                            if (commonLength != 0) {
                                diffs[pointer].items = textInsert.subListOfSize(textInsert.size - commonLength) + diffs[pointer].items
                                textInsert = textInsert.subListOfSize(0, textInsert.size - commonLength)
                                textDelete = textDelete.subListOfSize(0, textDelete.size - commonLength)
                            }
                        }
                        // Delete the offending records and add the merged ones.
                        when {
                            countDelete == 0 -> diffs.splice(pointer - countDelete - countInsert, countDelete + countInsert, Diff(Insert, textInsert))
                            countInsert == 0 -> diffs.splice(pointer - countDelete - countInsert, countDelete + countInsert, Diff(Delete, textDelete))
                            else             -> diffs.splice(pointer - countDelete - countInsert, countDelete + countInsert, Diff(Delete, textDelete), Diff(Insert, textInsert))
                        }
                        pointer = pointer - countDelete - countInsert + if(countDelete != 0)  1 else 0 + if(countInsert != 0)  1 else 0 + 1
                    } else if (pointer != 0 && diffs[pointer - 1].operation == Equal) {
                        // Merge this equality with the previous one.
                        diffs[pointer - 1].items = diffs[pointer - 1].items + diffs[pointer].items
                        diffs.removeAt(pointer)
                    } else {
                        pointer++
                    }
                    countInsert = 0
                    countDelete = 0
                    textDelete = emptyList()
                    textInsert = emptyList()
                }
            }
        }

        if (diffs[diffs.size - 1].items.isEmpty()) {
            diffs.removeAt(diffs.size - 1) // Remove the dummy entry at the end.
        }

        // Second pass: look for single edits surrounded on both sides by equalities
        // which can be shifted sideways to eliminate an equality.
        // e.g: A<ins>BA</ins>C -> <ins>AB</ins>AC
        var changes = false
        pointer = 1
        // Intentionally ignore the first and last element (don't need checking).
        while (pointer < (diffs.size - 1)) {
            if (diffs[pointer - 1].operation == Equal && diffs[pointer + 1].operation == Equal) {
                // This is a single edit surrounded by equalities.
                if (diffs[pointer].items.endsWith(diffs[pointer - 1].items)) {
                    // Shift the edit over the previous equality.
                    diffs[pointer].items = diffs[pointer - 1].items + diffs[pointer].items.subListOfSize(0, diffs[pointer].items.size - diffs[pointer - 1].items.size)
                    diffs[pointer + 1].items = diffs[pointer - 1].items + diffs[pointer + 1].items
                    diffs.splice(pointer - 1, 1)
                    changes = true
                } else if (diffs[pointer].items.startsWith(diffs[pointer + 1].items)) {
                    // Shift the edit over the next equality.
                    diffs[pointer - 1].items = diffs[pointer - 1].items + diffs[pointer + 1].items
                    diffs[pointer].items = diffs[pointer].items.subListOfSize(diffs[pointer + 1].items.size) + diffs[pointer + 1].items
                    diffs.splice(pointer + 1, 1)
                    changes = true
                }
            }
            pointer++
        }
        // If shifts were made, the diff needs reordering and another shift sweep.
        if (changes) {
            continue
        }
        break
    }
}

private fun <T> List<T>.isEqual(other: List<T>, by: (T, T) -> Boolean = { a, b -> a == b }): Boolean {
    if (size != other.size) return false

    repeat(size) { index ->
        if (!by(this[index], other[index])) return false
    }

    return true
}

private fun <T> MutableList<T>.splice(start: Int, count: Int, vararg items: T): MutableList<T> {
    val deletedRange = subList(start, start + count)
    removeAll(deletedRange)
    addAll(start, items.toList())
    return deletedRange
}

private fun <T> List<T>.startsWith(other: List<T>) = size >= other.size && take(other.size) == other

private fun <T> List<T>.endsWith(other: List<T>) = size >= other.size && takeLast(other.size) == other

private fun <T> List<T>.subListOfSize(start: Int, length: Int = size - start): List<T> = subList(start, start + length)

private fun <T> List<T>.indexOf(other: List<T>, start: Int = 0, by: (T, T) -> Boolean): Int
{
    for (i in start until size - other.size) {
        if (compareRange(i, other, 0, other.size, by)) {
            return i
        }
    }

    return -1
}

private fun <T> List<T>.compareRange(offsetA: Int, listB: List<T>, offsetB: Int, count: Int, by: (T, T) -> Boolean): Boolean {
    for (j in 0 until count) {
        if (!by(this[offsetA + j], listB[offsetB + j])) {
            return false
        }
    }

    return true
}