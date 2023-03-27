package io.dongxi.natty.view

import io.dongxi.natty.application.NattyAppConfig
import io.dongxi.natty.storage.DataStore
import io.dongxi.natty.util.ClassUtils
import io.dongxi.natty.util.PointUtils
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.core.View
import io.nacular.doodle.core.renderProperty
import io.nacular.doodle.drawing.*
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.theme.native.NativeHyperLinkStyler
import io.nacular.doodle.utils.Resizer
import kotlinx.coroutines.CoroutineDispatcher

class RightView(
    private val config: NattyAppConfig,
    private val uiDispatcher: CoroutineDispatcher,
    private val animator: Animator,
    private val pathMetrics: PathMetrics,
    private val textMetrics: TextMetrics,
    private val dataStore: DataStore,
    private val images: ImageLoader,
    private val linkStyler: NativeHyperLinkStyler,
    private val focusManager: FocusManager
) : View() {

    private var title by renderProperty(ClassUtils.simpleClassName(this))
    private val titleWidth = textMetrics.width(title)

    init {
        clipCanvasToBounds = false

        Resizer(this).apply { movable = false }
    }

    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, Color(0xe0bdbcu))  // From natty color tbl.

        canvas.text(
            text = title,
            font = config.titleFont,
            at = PointUtils.textCenterXPoint(this.width, this.titleWidth, 10),
            color = Color.Black
        )
    }
}