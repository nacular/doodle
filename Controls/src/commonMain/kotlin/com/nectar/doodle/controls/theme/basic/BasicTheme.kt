package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.MutableListModel
import com.nectar.doodle.controls.ProgressBar
import com.nectar.doodle.controls.ProgressIndicator
import com.nectar.doodle.controls.Slider
import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.buttons.CheckBox
import com.nectar.doodle.controls.buttons.RadioButton
import com.nectar.doodle.controls.list.List
import com.nectar.doodle.controls.list.MutableList
import com.nectar.doodle.controls.panels.SplitPanel
import com.nectar.doodle.controls.spinner.Spinner
import com.nectar.doodle.controls.table.MutableTable
import com.nectar.doodle.controls.table.Table
import com.nectar.doodle.controls.text.Label
import com.nectar.doodle.controls.text.LabelFactory
import com.nectar.doodle.controls.theme.LabelBehavior
import com.nectar.doodle.controls.theme.basic.list.BasicListBehavior
import com.nectar.doodle.controls.theme.basic.list.BasicMutableListBehavior
import com.nectar.doodle.controls.theme.basic.table.BasicMutableTableBehavior
import com.nectar.doodle.controls.theme.basic.table.BasicTableBehavior
import com.nectar.doodle.controls.theme.basic.tree.BasicMutableTreeBehavior
import com.nectar.doodle.controls.theme.basic.tree.BasicTreeBehavior
import com.nectar.doodle.controls.tree.MutableTree
import com.nectar.doodle.controls.tree.Tree
import com.nectar.doodle.controls.tree.TreeModel
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.black
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.drawing.grayScale
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.theme.Behavior
import com.nectar.doodle.theme.Theme

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
typealias ListModel<T>        = com.nectar.doodle.controls.ListModel<T>
typealias SpinnerModel<T>     = com.nectar.doodle.controls.spinner.Model<T>
typealias MutableTreeModel<T> = com.nectar.doodle.controls.tree.MutableTreeModel<T>

@Suppress("UNCHECKED_CAST")
open class BasicTheme(private val labelFactory: LabelFactory, private val textMetrics: TextMetrics, private val focusManager: FocusManager?): Theme {

    override fun install(display: Display, all: Sequence<View>) = all.forEach {
        when (it) {
            is Label              -> it.behavior = labelBehavior
            is CheckBox           -> it.behavior = checkBoxBehavior    as Behavior<Button>
            is RadioButton        -> it.behavior = radioButtonBehavior as Behavior<Button>
            is Button             -> it.behavior = buttonBehavior
            is Slider             -> it.behavior = sliderBehavior
            is SplitPanel         -> it.behavior = splitPanelBehavior
            is ProgressBar        -> it.behavior = (progressBarBehavior as Behavior<ProgressIndicator>)
            is MutableList<*, *>  -> (it as MutableList<Any, MutableListModel<Any>> ).behavior = mutableListBehavior
            is MutableTree<*, *>  -> (it as MutableTree<Any, MutableTreeModel<Any>> ).behavior = mutableTreeBehavior
            is List<*, *>         -> (it as List<Any, ListModel<Any>>               ).behavior = listBehavior
            is Tree<*, *>         -> (it as Tree<Any, TreeModel<Any>>               ).behavior = treeBehavior
            is Spinner<*, *>      -> (it as Spinner<Any, SpinnerModel<Any>>         ).behavior = spinnerBehavior
            is MutableTable<*, *> -> (it as MutableTable<Any, MutableListModel<Any>>).behavior = mutableTableBehavior
            is Table<*, *>        -> (it as Table<Any, ListModel<Any>>              ).behavior = tableBehavior
        }
    }

    override fun toString() = this::class.simpleName ?: ""

    protected open val borderColor                  = Color(0x888888u)
    protected open val selectionColor               = Color(0x0063e1u)
    protected open val foregroundColor              = black
    protected open val backgroundColor              = Color(0xccccccu)
    protected open val darkBackgroundColor          = Color(0xaaaaaau)
    protected open val lightBackgroundColor         = Color(0xf3f4f5u)
    protected open val defaultBackgroundColor get() = backgroundColor

    private val eventRowColor get() = lightBackgroundColor
    private val oddRowColor   get() = foregroundColor.inverted

    private val listBehavior         by lazy { BasicListBehavior<Any>        (focusManager, textMetrics, eventRowColor, oddRowColor, selectionColor, selectionColor.grayScale().lighter()) }
    private val treeBehavior         by lazy { BasicTreeBehavior<Any>        (labelFactory, eventRowColor, oddRowColor, selectionColor, selectionColor.grayScale().lighter(), foregroundColor, focusManager) }
    private val labelBehavior        by lazy { LabelBehavior                 (foregroundColor) }
    private val tableBehavior        by lazy { BasicTableBehavior<Any>       (focusManager, 20.0, backgroundColor, eventRowColor, oddRowColor, selectionColor, selectionColor.grayScale().lighter()) }
    private val buttonBehavior       by lazy { BasicButtonBehavior           (textMetrics, backgroundColor = backgroundColor, borderColor = borderColor, darkBackgroundColor = darkBackgroundColor, foregroundColor = foregroundColor) }
    private val sliderBehavior       by lazy { BasicSliderBehavior           (defaultBackgroundColor = defaultBackgroundColor, darkBackgroundColor = darkBackgroundColor) }
    private val spinnerBehavior      by lazy { BasicSpinnerBehavior          (borderColor = borderColor, backgroundColor = backgroundColor, labelFactory = labelFactory) }
    private val checkBoxBehavior     by lazy { BasicCheckBoxBehavior         (textMetrics               ) }
    private val splitPanelBehavior   by lazy { BasicSplitPanelBehavior       () }
    private val radioButtonBehavior  by lazy { BasicRadioBehavior            (textMetrics               ) }
    private val mutableListBehavior  by lazy { BasicMutableListBehavior<Any> (focusManager, textMetrics, eventRowColor, oddRowColor, selectionColor, selectionColor.grayScale().lighter() ) }
    private val progressBarBehavior  by lazy { BasicProgressBarBehavior      (defaultBackgroundColor = defaultBackgroundColor, darkBackgroundColor = darkBackgroundColor) }
    private val mutableTreeBehavior  by lazy { BasicMutableTreeBehavior<Any> (labelFactory, eventRowColor, oddRowColor, selectionColor, selectionColor.grayScale().lighter(), foregroundColor, focusManager) }
    private val mutableTableBehavior by lazy { BasicMutableTableBehavior<Any>(focusManager, 20.0, backgroundColor, eventRowColor, oddRowColor, selectionColor, selectionColor.grayScale().lighter()) }
}

class BasicDarkTheme(labelFactory: LabelFactory, textMetrics: TextMetrics, focusManager: FocusManager?): BasicTheme(labelFactory, textMetrics, focusManager) {
    override val borderColor            = super.borderColor.inverted
    override val foregroundColor        = super.foregroundColor.inverted
    override val backgroundColor        = super.backgroundColor.inverted
    override val darkBackgroundColor    = super.darkBackgroundColor.inverted
    override val lightBackgroundColor   = Color(0x282928u)
    override val defaultBackgroundColor = super.defaultBackgroundColor.inverted
}

//val basicThemeModule = Module(allowSilentOverride = true) {
//    bind<BasicTheme>  () with singleton { BasicTheme(instance(), instance(), instanceOrNull()) }
//    bind<TextMetrics> () with singleton { TextMetricsImpl(instance(), instance(), instance()) }
//    bind<LabelFactory>() with singleton { LabelFactoryImpl(instance()) }
//}