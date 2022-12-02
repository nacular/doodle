package io.nacular.doodle.controls.files

import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.datatransport.LocalFile
import io.nacular.doodle.datatransport.MimeType
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.observable

/**
 * Defines how [FileSelector]s behave.
 */
public interface FileSelectorBehavior: Behavior<FileSelector> {
    /**
     * Provides access to the list of files within a [FileSelector].
     */
    public var FileSelector.files: List<LocalFile> get() = files; set(new) { files = new }
}

/**
 * Control that allows the user to initiate file selection on the local host.
 *
 * NOTE: This control delegates the actual selection to the OS and waits to receive
 * the list of files when the user is done. Therefore, it can generally be given
 * a smaller footprint since it does not need to display files itself.
 *
 * NOTE: This control requires native integration to function, which means it should
 * either have a native behavior or use a native styler.
 *
 * @property allowMultiple indicates whether multiple files can be selected
 * @property acceptedTypes defines which file types to allow
 */
public class FileSelector(public val allowMultiple: Boolean = false, public val acceptedTypes: Set<MimeType<*>> = AnyFile): View() {

    /**
     * Notifies when the list of selected files changes
     */
    public val filesLoaded: PropertyObservers<FileSelector, List<LocalFile>> = PropertyObserversImpl(this)

    internal var files: List<LocalFile> by observable(emptyList(), filesLoaded as PropertyObserversImpl)

    /**
     * Controls the selector's behavior
     */
    public var behavior: Behavior<FileSelector>? by behavior()

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    public companion object {
        public val AnyFile: Set<MimeType<*>> = emptySet()
    }
}