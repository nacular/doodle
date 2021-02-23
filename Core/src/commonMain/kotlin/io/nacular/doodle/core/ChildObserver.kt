package io.nacular.doodle.core

/**
 * Observes changes to the children of a parent, S
 */
public typealias ChildObserver<S> = (source: S, removed: Map<Int, View>, added: Map<Int, View>, moved: Map<Int, Pair<Int, View>>) -> Unit