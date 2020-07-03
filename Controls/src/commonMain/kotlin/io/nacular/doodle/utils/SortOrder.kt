package io.nacular.doodle.utils

import io.nacular.doodle.utils.SortOrder.Ascending
import io.nacular.doodle.utils.SortOrder.Descending

/**
 * Created by Nicholas Eddy on 6/26/20.
 */
enum class SortOrder {
    Ascending, Descending
}

val SortOrder.inverse: SortOrder get() = when (this) {
    Ascending -> Descending
    else      -> Ascending
}