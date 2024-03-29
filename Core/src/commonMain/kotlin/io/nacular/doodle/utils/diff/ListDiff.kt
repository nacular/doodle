package io.nacular.doodle.utils.diff

import io.nacular.doodle.utils.fastMutableMapOf
import io.nacular.doodle.utils.fastMutableSetOf
import kotlin.math.min

/**
 * Classes are derived works from [https://github.com/danielearwicker/ListDiff] and
 * [https://github.com/google/diff-match-patch/blob/master/java/src/name/fraser/neil/plaintext/diff_match_patch.java]
 */

/**
 * Base class for all differences types used in describing a change to a list via [compare].
 */
public sealed class Difference<T>(items: List<T>) {
    /**
     * The set of items in the list that "changed"
     */
    public var items: List<T> = items; internal set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Difference<*>) return false

        if (items != other.items) return false

        return true
    }
    override fun hashCode(): Int = items.hashCode()

    public abstract fun <R> map(block: (T) -> R): Difference<R>
}

/**
 * Indicates that a region of the original list compared via [compare] is unchanged.
 */
@Suppress("EqualsOrHashCode")
public class Equal<T>(items: List<T>): Difference<T>(items) {
    public constructor(vararg items: T): this(items.toList())

    override fun toString(           ): String  = "= $items"
    override fun equals  (other: Any?): Boolean = other is Equal<*> && super.equals(other)

    override fun <R> map(block: (T) -> R): Difference<R> = Equal(items.map(block))
}

/**
 * Indicates that a region of the original list compared via [compare] has items inserted.
 */
public class Insert<T>(items: List<T>): Difference<T>(items) {
    public constructor(vararg items: T): this(items.toList())

    /**
     * This method indicates if an inserted item was previously in the list at some index. It should be
     * used along with [Differences.computeMoves] to check whether an item has actually been moved in the list.
     * @param of the given item
     * @return the original index of the provided item, or `null` if that item is new to the list, or moves weren't computed
     * @see [Delete.destination]
     */
    public fun origin(of: T): Int? = origins?.get(of)

    private var origins: MutableMap<T, Int>? = null

    internal fun setOrigin(of: T, origin: Int) {
        if (origins == null) origins = fastMutableMapOf()

        origins!![of] = origin
    }

    override fun toString(): String = "+ $items"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Insert<*>) return false

        if (origins != other.origins) return false

        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + origins.hashCode()
        return result
    }

    override fun <R> map(block: (T) -> R): Difference<R> = Insert(items.map(block))
}

/**
 * Indicates that a region of the original list compared via [compare] has items removed.
 */
public class Delete<T>(items: List<T>): Difference<T>(items) {
    public constructor(vararg items: T): this(items.toList())

    /**
     * This method indicates if a deleted item was re-added to the list at some index. It should be
     * used along with [Differences.computeMoves] to check whether an item has actually been moved in the list.
     * @param of the given item
     * @return the destination index of the provided item, or `null` if that item does not remain in the list, or moves weren't computed
     * @see [Insert.origin]
     */
    public fun destination(of: T): Int? = destinations?.get(of)

    private var destinations: MutableMap<T, Int>? = null

    internal fun setDestination(of: T, destination: Int) {
        if (destinations == null) destinations = fastMutableMapOf()

        destinations!![of] = destination
    }

    override fun toString(): String = "- $items"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Delete<*>) return false

        if (destinations != other.destinations) return false

        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + destinations.hashCode()
        return result
    }

    override fun <R> map(block: (T) -> R): Difference<R> = Delete(items.map(block))
}

/**
 * The list of differences between two lists compared using [compare].
 */
public class Differences<T>(private val changes: List<Difference<T>>, private var movesComputed: Boolean = false): Iterable<Difference<T>> {

    /**
     * No "moves" ([Delete] followed by [Insert] or vice versa) are calculated by default to improve performance
     * in cases where they are not needed. This method computes the moves and updates the [Insert], [Delete]
     * pairs to point to their respective origin and destination indexes.
     */
    public fun computeMoves(): Differences<T> {
        if (!movesComputed) {
            movesComputed = true

            val inserts = mutableListOf<Pair<Insert<T>, Int>>()
            val deletes = mutableListOf<Pair<Delete<T>, Int>>()

            var index           = 0
            var previousInserts = 0

            changes.forEach { change ->
                when (change) {
                    is Insert -> { inserts += (change to index); index += change.items.size; previousInserts += change.items.size }
                    is Delete -> { deletes += (change to index - previousInserts); previousInserts -= change.items.size }
                    else      ->   index   += change.items.size
                }
            }

            inserts.forEach { (insert, insertStart) ->
                insert.items.forEachIndexed { insertIndex, insertItem ->
                    deletes.forEach { (delete, deleteStart) ->
                        delete.items.indexOfFirst { deleteItem ->
                            deleteItem == insertItem && delete.destination(of = insertItem) == null
                        }.takeIf { it >= 0 }?.let {
                            insert.setOrigin(insertItem, it + deleteStart)
                            delete.setDestination(insertItem, insertIndex + insertStart)
                        }
                    }
                }
            }
        }

        return this
    }

    override fun iterator(): Iterator<Difference<T>> = changes.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Differences<*>) return false

        if (changes != other.changes) return false

        return true
    }

    override fun hashCode(): Int {
        return changes.hashCode()
    }

    override fun toString(): String = changes.toString()

    /**
     * @return a Differences with the [transform] applied to each element it contains.
     */
    public fun <R> map(transform: (T) -> R): Differences<R> = Differences(changes.map { it.map(transform) })
}

internal enum class Operation {
    Delete, Insert, Equal
}

internal fun <T> compare(first: List<T>, second: List<T>, dualThreshold: Int = 32, by: (T, T) -> Boolean = { a, b -> a == b }): Differences<T> = Differences(compareInternal(first, second, dualThreshold, by))

private fun <T> compareInternal(first: List<T>, second: List<T>, dualThreshold: Int, by: (T, T) -> Boolean): MutableList<Difference<T>> {
    // Check for equality (speedup)
    if (first.isEqual(second, by)) {
        return mutableListOf()
    }

    // Trim off common prefix (speedup)
    var commonLength = getCommonPrefix(first, second, by)
    val commonPrefix = first.subListOfSize(0, commonLength)

    var text1 = first.subListOfSize(commonLength)
    var text2 = second.subListOfSize(commonLength)

    // Trim off common suffix (speedup)
    commonLength = getCommonSuffix(text1, text2, by)

    val commonSuffix = text1.subListOfSize(text1.size - commonLength)

    text1 = text1.subListOfSize(0, text1.size - commonLength)
    text2 = text2.subListOfSize(0, text2.size - commonLength)

    // Compute the diff on the middle block
    val diffs = compute(text1, text2, dualThreshold, by)

    // Restore the prefix and suffix
    if (commonPrefix.isNotEmpty()) {
        diffs.add(0, Equal(commonPrefix))
    }
    if (commonSuffix.isNotEmpty()) {
        diffs.add(Equal(commonSuffix))
    }

    cleanupMerge(diffs, by)
    return diffs
}

private fun <T> compute(first: List<T>, second: List<T>, dualThreshold: Int, equality: (T, T) -> Boolean): MutableList<Difference<T>> {
    if (first.isEmpty()) {
        // Just add some Items (speedup)
        return mutableListOf(Insert(second))
    }

    if (second.isEmpty()) {
        // Just delete some Items (speedup)
        return mutableListOf(Delete(first))
    }

    var diffs = mutableListOf<Difference<T>>()

    val longText  = if (first.size > second.size) first  else second
    val shortText = if (first.size > second.size) second else first
    val i = longText.indexOf(shortText, by = equality)

    if (i != -1) {
        // Shorter Items is inside the longer Items (speedup)
        val isDelete = first.size > second.size
        diffs.add(if (isDelete) Delete(longText.subListOfSize(0, i)) else Insert(longText.subListOfSize(0, i)))
        diffs.add(Equal(shortText))
        diffs.add(if (isDelete) Delete(longText.subListOfSize(i + shortText.size)) else Insert(longText.subListOfSize(i + shortText.size)))
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
        diffs.add(Equal(midCommon))
        diffs.addAll(diffsB)
        return diffs
    }

    return getMap(first, second, dualThreshold, equality) ?: mutableListOf(Delete(first), Insert(second))
}

private fun <T> getMap(first: List<T>, second: List<T>, dualThreshold: Int, equal: (T, T) -> Boolean): MutableList<Difference<T>>? {
    // Cache the Items lengths to prevent multiple calls.
    val firstLength  = first.size
    val secondLength = second.size
    val maxD         = firstLength + secondLength - 1
    val doubleEnd    = dualThreshold * 2 < maxD
    var vMap1        = mutableListOf<MutableSet<Long>>()
    var vMap2        = mutableListOf<MutableSet<Long>>()
    val v1           = fastMutableMapOf<Int, Int>().apply { this[1] = 0 }
    val v2           = fastMutableMapOf<Int, Int>().apply { this[1] = 0 }
    var footstep     = 0L  // Used to track overlapping paths.
    val footsteps    = fastMutableMapOf<Long, Int>()
    var done         = false
    val front        = ((firstLength + secondLength) % 2 == 1) // If the total number of characters is odd, then the front path will collide with the reverse path.

    repeat(maxD) { d ->
        // Walk the front path one step.
        vMap1.add(fastMutableSetOf())  // Adds at index 'd'.
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
            while (!done && x < firstLength && y < secondLength && equal(first[x], second[y])) {
                ++x
                ++y
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
            vMap2.add(fastMutableSetOf())  // Adds at index 'd'.

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
                while (!done && x < firstLength && y < secondLength && equal(first[firstLength - x - 1], second[secondLength - y - 1])) {
                    ++x
                    ++y
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

internal fun <T> diffPath1(vMap: List<Set<Long>>, first: List<T>, second: List<T>): MutableList<Difference<T>> {
    val path = mutableListOf<Difference<T>>()
    var x = first.size
    var y = second.size
    var lastOp = null as Operation?

    for (d in vMap.size - 2 downTo 0) {
        while (true) {
            if (vMap[d].contains(getFootprint(x - 1, y))) {
                x--
                when (lastOp) {
                    Operation.Delete -> path.first().items = first.subListOfSize(x, 1) + path.first().items
                    else             -> path.add(0, Delete(first.subListOfSize(x, 1)))
                }
                lastOp = Operation.Delete
                break
            }

            if (vMap[d].contains(getFootprint(x, y - 1))) {
                y--
                when (lastOp) {
                    Operation.Insert -> path.first().items = second.subListOfSize(y, 1) + path.first().items
                    else             -> path.add(0, Insert(second.subListOfSize(y, 1)))
                }
                lastOp = Operation.Insert
                break
            }

            x--
            y--
            when (lastOp) {
                Operation.Equal -> path.first().items = first.subListOfSize(x, 1) + path.first().items
                else            -> path.add(0, Equal(first.subListOfSize(x, 1)))
            }
            lastOp = Operation.Equal
        }
    }
    return path
}

internal fun <T> diffPath2(vMap: List<Set<Long>>, first: List<T>, second: List<T>): MutableList<Difference<T>> {
    val path = mutableListOf<Difference<T>>()
    var x = first.size
    var y = second.size
    var lastOp = null as Operation?
    for (d in vMap.size - 2 downTo 0) {
        while (true) {
            if (vMap[d].contains(getFootprint(x - 1, y))) {
                x--
                when (lastOp) {
                    Operation.Delete -> path.last().items = path.last().items + first.subListOfSize(first.size - x - 1, 1)
                    else             -> path.add(Delete(first.subListOfSize(first.size - x - 1, 1)))
                }
                lastOp = Operation.Delete
                break
            }

            if (vMap[d].contains(getFootprint(x, y - 1))) {
                y--
                when (lastOp) {
                    Operation.Insert -> path.last().items = path.last().items + second.subListOfSize(second.size - y - 1, 1)
                    else             -> path.add(Insert(second.subListOfSize(second.size - y - 1, 1)))
                }
                lastOp = Operation.Insert
                break
            }
            x--
            y--
            //assert (text1.charAt(text1.Count - x - 1)
            //        == text2.charAt(text2.Count - y - 1))
            //      : "No diagonal.  Can't happen. (DiffPath2)";
            when (lastOp) {
                Operation.Equal -> path.last().items = path.last().items + first.subListOfSize(first.size - x - 1, 1)
                else            -> path.add(Equal(first.subListOfSize(first.size - x - 1, 1)))
            }
            lastOp = Operation.Equal
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

internal fun <T> getCommonPrefix(first: List<T>, second: List<T>, equal: (T, T) -> Boolean = { a, b -> a == b }): Int {
    // Performance analysis: http://neil.fraser.name/news/2007/10/09/
    val n = min(first.size, second.size)

    for (i in 0 until n) {
        if (!equal(first[i], second[i])) {
            return i
        }
    }

    return n
}

internal fun <T> getCommonSuffix(first: List<T>, second: List<T>, equal: (T, T) -> Boolean = { a, b -> a == b }): Int {
    // Performance analysis: http://neil.fraser.name/news/2007/10/09/
    val n = min(first.size, second.size)

    for (i in 1 .. n) {
        if (!equal(first[first.size - i], second[second.size - i])) {
            return i - 1
        }
    }
    return n
}

internal fun <T> getHalfMatch(first: List<T>, second: List<T>, equal: (T, T) -> Boolean): Array<List<T>>? {
    val longText  = if (first.size > second.size) first else second
    val shortText = if (first.size > second.size) second else first

    if (longText.size < 10 || shortText.isEmpty()) {
        return null // Pointless
    }

    // First check if the second quarter is the seed for a half-match.
    val hm1 = getHalfMatchI(longText, shortText, (longText.size + 3) / 4, equal)

    // Check again based on the third quarter.
    val hm2 = getHalfMatchI(longText, shortText, (longText.size + 1) / 2, equal)

    val hm = when {
        hm1 == null && hm2 == null -> return null
        hm2 == null                -> hm1!!
        hm1 == null                -> hm2
        else                       -> if (hm1[4].size > hm2[4].size) hm1 else hm2 // Both matched.  Select the longest.
    }

    // A half-match was found, sort out the return data.
    return if (first.size > second.size) hm else arrayOf(hm[2], hm[3], hm[0], hm[1], hm[4])
}

private fun <T> getHalfMatchI(longList: List<T>, shortList: List<T>, index: Int, equal: (T, T) -> Boolean): Array<List<T>>? {
    // Start with a 1/4 length Substring at position i as a seed.
    val seed = longList.subListOfSize(index, longList.size / 4)
    var j = shortList.indexOf(seed, 0, equal)
    var bestCommon     = emptyList<T>()
    var bestLongTextA  = emptyList<T>()
    var bestLongTextB  = emptyList<T>()
    var bestShortTextA = emptyList<T>()
    var bestShortTextB = emptyList<T>()

    while (j < shortList.size && j != -1) {
        val prefixLength = getCommonPrefix(longList.subListOfSize(   index), shortList.subListOfSize(   j), equal)
        val suffixLength = getCommonSuffix(longList.subListOfSize(0, index), shortList.subListOfSize(0, j), equal)

        if (bestCommon.size < suffixLength + prefixLength) {
            bestCommon     = shortList.subListOfSize(j - suffixLength, suffixLength) + shortList.subListOfSize(j, prefixLength)
            bestLongTextA  = longList.subListOfSize (0, index - suffixLength)
            bestLongTextB  = longList.subListOfSize (index + prefixLength)
            bestShortTextA = shortList.subListOfSize(0, j - suffixLength)
            bestShortTextB = shortList.subListOfSize(j + prefixLength)
        }

        j = shortList.indexOf(seed, j + 1, equal)
    }

    return when {
        bestCommon.size >= longList.size / 2 -> arrayOf(bestLongTextA, bestLongTextB, bestShortTextA, bestShortTextB, bestCommon)
        else                                 -> null
    }
}

internal fun <T> cleanupMerge(diffs: MutableList<Difference<T>>, equal: (T, T) -> Boolean = { a, b -> a == b }) {
    diffs.add(Equal(emptyList())) // Add a dummy entry at the end.
    var pointer     = diffs.listIterator()
    var countDelete = 0
    var countInsert = 0
    var itemsDelete = emptyList<T>()
    var itemsInsert = emptyList<T>()
    var thisDiff    = pointer.next() as Difference<T>?
    var prevEqual   = null as Difference<T>?
    var commonLength: Int

    while (thisDiff != null) {
        when (thisDiff) {
            is Insert -> {
                countInsert++
                itemsInsert = itemsInsert + thisDiff.items
                prevEqual = null
            }

            is Delete -> {
                countDelete++
                itemsDelete = itemsDelete + thisDiff.items
                prevEqual = null
            }

            else -> {
                if (countDelete + countInsert > 1) {
                    val bothTypes = countDelete != 0 && countInsert != 0
                    // Delete the offending records.
                    pointer.previous() // Reverse direction.
                    while (countDelete-- > 0) {
                        pointer.previous()
                        pointer.remove()
                    }
                    while (countInsert-- > 0) {
                        pointer.previous()
                        pointer.remove()
                    }
                    if (bothTypes) {
                        // Factor out any common prefixies.
                        commonLength = getCommonPrefix(itemsInsert, itemsDelete, equal)
                        if (commonLength != 0) {
                            when {
                                pointer.hasPrevious() -> {
                                    thisDiff = pointer.previous()
//                                    require(thisDiff is Equal) { "Previous diff should have been an equality." }
                                    thisDiff.items += itemsInsert.subListOfSize(0, commonLength)
                                    pointer.next()
                                }
                                else                  -> pointer.add(Equal(itemsInsert.subListOfSize(0, commonLength)))
                            }
                            itemsInsert = itemsInsert.subListOfSize(commonLength)
                            itemsDelete = itemsDelete.subListOfSize(commonLength)
                        }
                        // Factor out any common suffixies.
                        commonLength = getCommonPrefix(itemsInsert, itemsDelete, equal)
                        if (commonLength != 0) {
                            thisDiff       = pointer.next()
                            thisDiff.items = itemsInsert.subListOfSize(itemsInsert.size - commonLength) + thisDiff.items
                            itemsInsert    = itemsInsert.subListOfSize(0, (itemsInsert.size - commonLength))
                            itemsDelete    = itemsDelete.subListOfSize(0, (itemsDelete.size - commonLength))
                            pointer.previous()
                        }
                    }
                    // Insert the merged records.
                    if (itemsDelete.isNotEmpty()) {
                        pointer.add(Delete(itemsDelete))
                    }
                    if (itemsInsert.isNotEmpty()) {
                        pointer.add(Insert(itemsInsert))
                    }
                    // Step forward to the equality.
                    thisDiff = if (pointer.hasNext()) pointer.next() else null
                } else if (prevEqual != null) {
                    // Merge this equality with the previous one.
                    prevEqual.items += thisDiff.items
                    pointer.remove()
                    thisDiff = pointer.previous()
                    pointer.next() // Forward direction
                }

                countInsert = 0
                countDelete = 0
                itemsDelete = emptyList()
                itemsInsert = emptyList()
                prevEqual   = thisDiff
            }
        }

        thisDiff = if (pointer.hasNext()) pointer.next() else null
    }

    if (diffs.lastOrNull()?.items?.isEmpty() == true) {
        diffs.removeLast() // Remove the dummy entry at the end.
    }

    /*
     * Second pass: look for single edits surrounded on both sides by equalities
     * which can be shifted sideways to eliminate an equality.
     * e.g: A<ins>BA</ins>C -> <ins>AB</ins>AC
     */
    var changes = false

    // Create a new iterator at the start. (As opposed to walking the current one back.)
    pointer      = diffs.listIterator()
    var prevDiff = if (pointer.hasNext()) pointer.next() else null
    thisDiff     = if (pointer.hasNext()) pointer.next() else null
    var nextDiff = if (pointer.hasNext()) pointer.next() else null

    // Intentionally ignore the first and last element (don't need checking).
    while (nextDiff != null) {
        if (prevDiff is Equal && nextDiff is Equal) {
            // This is a single edit surrounded by equalities.
            if (thisDiff?.items?.endsWith(prevDiff.items) == true) {
                // Shift the edit over the previous equality.
                thisDiff.items = (prevDiff.items + thisDiff.items.subListOfSize(0, thisDiff.items.size - prevDiff.items.size))
                nextDiff.items = prevDiff.items + nextDiff.items
                pointer.previous() // Walk past nextDiff.
                pointer.previous() // Walk past thisDiff.
                pointer.previous() // Walk past prevDiff.
                pointer.remove  () // Delete prevDiff.
                pointer.next    () // Walk past thisDiff.
                thisDiff = pointer.next() // Walk past nextDiff.
                nextDiff = if (pointer.hasNext()) pointer.next() else null
                changes = true
            } else if (thisDiff?.items?.startsWith(nextDiff.items) == true) {
                // Shift the edit over the next equality.
                prevDiff.items += nextDiff.items
                thisDiff.items = (thisDiff.items.subListOfSize(nextDiff.items.size) + nextDiff.items)
                pointer.remove() // Delete nextDiff.
                nextDiff = if (pointer.hasNext()) pointer.next() else null
                changes = true
            }
        }
        prevDiff = thisDiff
        thisDiff = nextDiff
        nextDiff = if (pointer.hasNext()) pointer.next() else null
    }

    // If shifts were made, the diff needs reordering and another shift sweep.
    if (changes) {
        cleanupMerge(diffs, equal)
    }
}

private fun <T> List<T>.isEqual(other: List<T>, by: (T, T) -> Boolean = { a, b -> a == b }): Boolean {
    if (size != other.size) return false

    repeat(size) { index ->
        if (!by(this[index], other[index])) return false
    }

    return true
}

private fun <T> List<T>.startsWith(other: List<T>) = size >= other.size && take(other.size) == other

private fun <T> List<T>.endsWith(other: List<T>) = size >= other.size && takeLast(other.size) == other

private fun <T> List<T>.subListOfSize(start: Int, length: Int = size - start): List<T> = subList(start, start + length)

private fun <T> List<T>.indexOf(other: List<T>, start: Int = 0, by: (T, T) -> Boolean): Int {
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