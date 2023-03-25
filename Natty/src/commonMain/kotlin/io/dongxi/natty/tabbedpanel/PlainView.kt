package io.dongxi.natty.tabbedpanel

import io.dongxi.natty.util.ClassUtils
import io.nacular.doodle.core.View
import io.nacular.doodle.core.renderProperty
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.utils.Resizer

class PlainView(
    private val config: NattyAppConfig,
//    private val uiDispatcher: CoroutineDispatcher,
//    private val animator: Animator,
//    private val pathMetrics: PathMetrics,
    private val textMetrics: TextMetrics,
//    private val dataStore: DataStore,
//    private val images: ImageLoader,
//    private val linkStyler: NativeHyperLinkStyler,
//    private val focusManager: FocusManager
    val tabName: String
) : View() {

    private var title by renderProperty(ClassUtils.simpleClassName(this))  // var is not final (is mutable)
    private val titleWidth = textMetrics.width(title)     // val is final (immutable)

    val styledTabName: StyledText = StyledText(tabName, config.tabPanelFont, Color(0x733236u).paint)

    init {
        clipCanvasToBounds = false // nothing rendered shows beyond its [bounds]

        // layout = constrain(this) {}

        Resizer(this)
    }

    override fun render(canvas: Canvas) {
        val foreGround = (foregroundColor ?: Color.Cyan).paint
        val backGround = (backgroundColor ?: Color.Gray).paint
        canvas.rect(bounds.atOrigin, backGround)
    }
}