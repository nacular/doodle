package io.nacular.doodle.controls

internal class IntProgressionModel(private val progression: IntProgression): ListModel<Int> {
    override val size: Int = progression.run { (last - first + 1) / step }

    override fun get(index: Int): Result<Int> = runCatching { progression.fastElementAt(index) }

    override fun section(range: ClosedRange<Int>): List<Int> = progression.asSequence().drop(range.start).take(range.endInclusive - range.start).toList()

    override fun contains(value: Int): Boolean = progression.containsFast(value)

    override fun iterator(): IntIterator = progression.iterator()
}

internal fun IntProgression.fastElementAt(index: Int): Int {
    if (index < 0) throw IndexOutOfBoundsException("Index $index is negative.")

    // Calculate total elements to validate index
    val size = (last - first) / step + 1
    if (index >= size) throw IndexOutOfBoundsException("Index $index out of bounds for size $size.")

    return first + index * step
}

internal fun IntProgression.containsFast(value: Int): Boolean {
    // 1. Bounds check (handle both increasing and decreasing progressions)
    if (step > 0) {
        if (value !in first..last) return false
    } else {
        if (value !in last..first) return false
    }

    // 2. Modulo check: (Value - First) must be a multiple of Step
    return (value - first) % step == 0
}
