package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.files.FileSelector
import io.nacular.doodle.core.Behavior

/**
 * Allows full control over how [FileSelector]s are styled while still retaining some native behaviors. The given behavior is delegated
 * to for all visual styling, but things like link traversal will be handled natively.
 */
public interface NativeFileSelectorStyler {
    /**
     * Wraps [behavior] with other native behavior for [FileSelector]s.
     *
     * @param fileSelector to apply [behavior] to
     * @param behavior to be "wrapped"
     * @return a new Behavior for the selector
     */
    public operator fun invoke(fileSelector: FileSelector, behavior: Behavior<FileSelector>): Behavior<FileSelector>
}