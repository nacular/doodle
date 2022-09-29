package io.nacular.doodle.core

import io.nacular.doodle.utils.diff.Differences

/**
 * Observes changes to the children of a parent, S
 */
public typealias ChildObserver<S> = (source: S, diffs: Differences<View>) -> Unit