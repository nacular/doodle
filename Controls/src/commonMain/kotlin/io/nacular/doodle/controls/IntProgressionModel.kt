package io.nacular.doodle.controls

import kotlin.collections.List

internal class IntProgressionModel(private val progression: IntProgression): ListModel<Int> {
    override val size: Int = progression.run { (last - first + 1) / step }

    override fun get(index: Int): Result<Int> = runCatching { progression.elementAt(index) }

    override fun section(range: ClosedRange<Int>): List<Int> = progression.asSequence().drop(range.start).take(range.endInclusive - range.start).toList()

    override fun contains(value: Int): Boolean = value in progression

    override fun iterator(): IntIterator = progression.iterator()
}