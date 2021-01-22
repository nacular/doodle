package io.nacular.doodle.utils

import io.nacular.doodle.core.Internal

public actual typealias TreeSet<E> = TreeSetJs<E>

@OptIn(Internal::class)
public actual typealias MutableTreeSet<E> = MutableTreeSetJs<E>