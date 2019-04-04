package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.ProgressBar
import com.nectar.doodle.controls.ProgressIndicator
import com.nectar.doodle.controls.Slider
import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.list.List
import com.nectar.doodle.controls.list.MutableList
import com.nectar.doodle.controls.list.MutableModel
import com.nectar.doodle.controls.panels.SplitPanel
import com.nectar.doodle.controls.spinner.Spinner
import com.nectar.doodle.controls.text.LabelFactory
import com.nectar.doodle.controls.theme.basic.list.BasicListBehavior
import com.nectar.doodle.controls.theme.basic.list.BasicMutableListBehavior
import com.nectar.doodle.controls.theme.basic.tree.BasicMutableTreeBehavior
import com.nectar.doodle.controls.theme.basic.tree.BasicTreeBehavior
import com.nectar.doodle.controls.tree.Model
import com.nectar.doodle.controls.tree.MutableTree
import com.nectar.doodle.controls.tree.Tree
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.black
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.theme.Behavior
import com.nectar.doodle.theme.Theme

/**
 * Created by Nicholas Eddy on 2/12/18.
 */

private val borderColor            = Color(0x888888u)
private val foregroundColor        = black
private val backgroundColor        = Color(0xccccccu)
private val darkBackgroundColor    = Color(0xaaaaaau)
private val defaultBackgroundColor = backgroundColor


typealias ListModel<T>        = com.nectar.doodle.controls.list.Model<T>
typealias SpinnerModel<T>     = com.nectar.doodle.controls.spinner.Model<T>
typealias MutableTreeModel<T> = com.nectar.doodle.controls.tree.MutableModel<T>

@Suppress("UNCHECKED_CAST", "NestedLambdaShadowedImplicitParameter")
class BasicTheme(private val labelFactory: LabelFactory, private val textMetrics: TextMetrics, private val focusManager: FocusManager?): Theme {

    override fun install(display: Display, all: Sequence<View>) = all.forEach {
        when (it) {
            is Button            -> it.behavior = buttonBehavior
            is Slider            -> it.behavior = sliderBehavior
            is SplitPanel        -> it.behavior = splitPanelBehavior
            is ProgressBar       -> it.behavior = (progressBarBehavior as Behavior<ProgressIndicator>)
            is MutableList<*, *> -> (it as MutableList<Any, MutableModel<Any>>    ).behavior = mutableListBehavior
            is MutableTree<*, *> -> (it as MutableTree<Any, MutableTreeModel<Any>>).behavior = mutableTreeBehavior
            is List<*, *>        -> (it as List<Any, ListModel<Any>>              ).behavior = listBehavior
            is Tree<*, *>        -> (it as Tree<Any, Model<Any>>                  ).behavior = treeBehavior
            is Spinner<*, *>     -> (it as Spinner<Any, SpinnerModel<Any>>        ).behavior = spinnerBehavior
        }
    }

    override fun toString() = this::class.simpleName ?: ""

    private val listBehavior        by lazy { BasicListBehavior<Any>       (textMetrics               ) }
    private val treeBehavior        by lazy { BasicTreeBehavior<Any>       (labelFactory, focusManager) }
    private val buttonBehavior      by lazy { BasicButtonBehavior          (textMetrics, backgroundColor = backgroundColor, borderColor = borderColor, darkBackgroundColor = darkBackgroundColor, foregroundColor = foregroundColor) }
    private val sliderBehavior      by lazy { BasicSliderBehavior          (defaultBackgroundColor = defaultBackgroundColor, darkBackgroundColor = darkBackgroundColor) }
    private val spinnerBehavior     by lazy { BasicSpinnerBehavior         (borderColor = borderColor, backgroundColor = backgroundColor, labelFactory = labelFactory) }
    private val splitPanelBehavior  by lazy { BasicSplitPanelBehavior      () }
    private val mutableListBehavior by lazy { BasicMutableListBehavior<Any>(focusManager, textMetrics ) }
    private val progressBarBehavior by lazy { BasicProgressBarBehavior     (defaultBackgroundColor = defaultBackgroundColor, darkBackgroundColor = darkBackgroundColor) }
    private val mutableTreeBehavior by lazy { BasicMutableTreeBehavior<Any>(labelFactory, focusManager) }
}

//val basicThemeModule = Module(allowSilentOverride = true) {
//    bind<BasicTheme>  () with singleton { BasicTheme(instance(), instance(), instanceOrNull()) }
//    bind<TextMetrics> () with singleton { TextMetricsImpl(instance(), instance(), instance()) }
//    bind<LabelFactory>() with singleton { LabelFactoryImpl(instance()) }
//}