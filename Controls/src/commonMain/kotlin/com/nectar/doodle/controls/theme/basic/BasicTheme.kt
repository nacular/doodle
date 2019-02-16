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
import com.nectar.doodle.controls.theme.basic.list.BasicListUI
import com.nectar.doodle.controls.theme.basic.list.BasicMutableListUI
import com.nectar.doodle.controls.theme.basic.tree.BasicMutableTreeUI
import com.nectar.doodle.controls.theme.basic.tree.BasicTreeUI
import com.nectar.doodle.controls.tree.Model
import com.nectar.doodle.controls.tree.MutableTree
import com.nectar.doodle.controls.tree.Tree
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.black
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.theme.Renderer
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
            is Button            -> { it.renderer?.uninstall(it); it.renderer = buttonUI.apply     { install(it) } }
            is Slider            -> { it.renderer?.uninstall(it); it.renderer = sliderUI.apply     { install(it) } }
            is SplitPanel        -> { it.renderer?.uninstall(it); it.renderer = splitPanelUI.apply { install(it) } }
            is ProgressBar       -> { it.renderer?.uninstall(it); it.renderer = (progressBarUI as Renderer<ProgressIndicator>).apply { install(it) } }
            is MutableList<*, *> -> (it as MutableList<Any, MutableModel<Any>>    ).let { it.renderer?.uninstall(it); it.renderer = mutableListUI.apply { install(it) } }
            is MutableTree<*, *> -> (it as MutableTree<Any, MutableTreeModel<Any>>).let { it.renderer?.uninstall(it); it.renderer = mutableTreeUI.apply { install(it) } }
            is List<*, *>        -> (it as List<Any, ListModel<Any>>              ).let { it.renderer?.uninstall(it); it.renderer = listUI.apply        { install(it) } }
            is Tree<*, *>        -> (it as Tree<Any, Model<Any>>                  ).let { it.renderer?.uninstall(it); it.renderer = treeUI.apply        { install(it) } }
            is Spinner<*, *>     -> (it as Spinner<Any, SpinnerModel<Any>>        ).let { it.renderer?.uninstall(it); it.renderer = spinnerUI.apply     { install(it) } }
        }
    }

    override fun toString() = this::class.simpleName ?: ""

    private val listUI        by lazy { BasicListUI<Any>       (textMetrics               ) }
    private val treeUI        by lazy { BasicTreeUI<Any>       (labelFactory, focusManager) }
    private val buttonUI      by lazy { BasicButtonUI          (textMetrics, backgroundColor = backgroundColor, borderColor = borderColor, darkBackgroundColor = darkBackgroundColor, foregroundColor = foregroundColor) }
    private val sliderUI      by lazy { BasicSliderUI          (defaultBackgroundColor = defaultBackgroundColor, darkBackgroundColor = darkBackgroundColor) }
    private val spinnerUI     by lazy { BasicSpinnerUI         (borderColor = borderColor, backgroundColor = backgroundColor, labelFactory = labelFactory) }
    private val splitPanelUI  by lazy { BasicSplitPanelUI      () }
    private val mutableListUI by lazy { BasicMutableListUI<Any>(focusManager, textMetrics ) }
    private val progressBarUI by lazy { BasicProgressBarUI     (defaultBackgroundColor = defaultBackgroundColor, darkBackgroundColor = darkBackgroundColor) }
    private val mutableTreeUI by lazy { BasicMutableTreeUI<Any>(labelFactory, focusManager) }
}

//val basicThemeModule = Module {
//    bind<BasicTheme>  () with singleton { BasicTheme(instance(), instance(), instanceOrNull()) }
//    bind<TextMetrics> () with singleton { TextMetricsImpl(instance(), instance(), instance()) }
//    bind<LabelFactory>() with singleton { LabelFactoryImpl(instance()) }
//}