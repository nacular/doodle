package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.files.FileSelector
import io.nacular.doodle.controls.files.FileSelectorBehavior
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View.Companion.fixed
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.impl.NativeFileSelectorFactory
import io.nacular.doodle.system.Cursor

internal class NativeFileSelectorStylerImpl(private val nativeFileSelectorFactory: NativeFileSelectorFactory):
    NativeFileSelectorStyler {
    override fun invoke(fileSelector: FileSelector, behavior: Behavior<FileSelector>): Behavior<FileSelector> = NativeFileSelectorBehaviorWrapper(
        nativeFileSelectorFactory,
        fileSelector,
        behavior
    )
}

private class NativeFileSelectorBehaviorWrapper(
    nativeFileSelectorFactory: NativeFileSelectorFactory,
    fileSelector             : FileSelector,
    private val delegate   : Behavior<FileSelector>): FileSelectorBehavior, Behavior<FileSelector> by delegate {

    private val nativePeer = nativeFileSelectorFactory(
        fileSelector,
        customRenderer = { selector, canvas -> delegate.render(selector, canvas) },
        delegate.clipCanvasToBounds(fileSelector)
    ) {
        fileSelector.files = it
    }

    override fun render(view: FileSelector, canvas: Canvas) {
        nativePeer.render(canvas)
    }
}

internal class NativeFileSelectorBehavior(nativeFileSelectorFactory: NativeFileSelectorFactory, fileSelector: FileSelector): FileSelectorBehavior {
    private val nativePeer = nativeFileSelectorFactory(fileSelector) {
        fileSelector.files = it
    }

    private var oldCursor        : Cursor? = null
    private var oldPreferredSize = fileSelector.preferredSize

    override fun render(view: FileSelector, canvas: Canvas) {
        nativePeer.render(canvas)
    }

    override fun mirrorWhenRightToLeft(view: FileSelector) = false

    override fun install(view: FileSelector) {
        super.install(view)

        view.apply {
            cursor        = Cursor.Default
            preferredSize = fixed(nativePeer.idealSize)

            rerender()
        }
    }

    override fun uninstall(view: FileSelector) {
        super.uninstall(view)

        nativePeer.discard()

        view.apply {
            cursor        = oldCursor
            preferredSize = oldPreferredSize
        }
    }
}