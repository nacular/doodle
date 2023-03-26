package io.dongxi.natty.tabbedpanel

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
import io.nacular.doodle.layout.constraints.constrain
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

    private var title by renderProperty(ClassUtils.simpleClassName(this))  // var is not final (is mutable)
    private val titleWidth = textMetrics.width(title)     // val is final (immutable)

    init {
        clipCanvasToBounds = false

        layout = constrain(this) {
        }

        Resizer(this)
    }

    override fun render(canvas: Canvas) {
        val foreGround = (foregroundColor ?: Color.White).paint
        val backGround = (backgroundColor ?: Color.Yellow).paint

        canvas.rect(bounds.atOrigin, backGround)

        canvas.text(
            text = title,
            font = config.titleFont,
            at = PointUtils.textCenterXPoint(this.width, this.titleWidth, 10),
            color = Color.Black
        )
    }
}