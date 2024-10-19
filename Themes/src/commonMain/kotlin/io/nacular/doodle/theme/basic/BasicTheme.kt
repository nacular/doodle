@file:Suppress("unused")

package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.DynamicListModel
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.ProgressBar
import io.nacular.doodle.controls.ProgressIndicator
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.buttons.RadioButton
import io.nacular.doodle.controls.buttons.Switch
import io.nacular.doodle.controls.date.DaysOfTheWeekPanel
import io.nacular.doodle.controls.date.MonthPanel
import io.nacular.doodle.controls.list.HorizontalDynamicList
import io.nacular.doodle.controls.list.HorizontalList
import io.nacular.doodle.controls.list.HorizontalMutableList
import io.nacular.doodle.controls.list.List
import io.nacular.doodle.controls.list.MutableList
import io.nacular.doodle.controls.list.VerticalDynamicList
import io.nacular.doodle.controls.list.VerticalList
import io.nacular.doodle.controls.list.VerticalMutableList
import io.nacular.doodle.controls.panels.GridPanel
import io.nacular.doodle.controls.panels.SplitPanel
import io.nacular.doodle.controls.panels.TabbedPanel
import io.nacular.doodle.controls.popupmenu.Menu
import io.nacular.doodle.controls.range.CircularRangeSlider
import io.nacular.doodle.controls.range.CircularSlider
import io.nacular.doodle.controls.range.RangeSlider
import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.controls.selectbox.MutableSelectBox
import io.nacular.doodle.controls.selectbox.SelectBox
import io.nacular.doodle.controls.spinbutton.MutableSpinButton
import io.nacular.doodle.controls.spinbutton.MutableSpinButtonModel
import io.nacular.doodle.controls.spinbutton.SpinButton
import io.nacular.doodle.controls.spinbutton.SpinButtonModel
import io.nacular.doodle.controls.table.MutableTable
import io.nacular.doodle.controls.table.Table
import io.nacular.doodle.controls.table.TreeTable
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.theme.CommonLabelBehavior
import io.nacular.doodle.controls.tree.MutableTree
import io.nacular.doodle.controls.tree.Tree
import io.nacular.doodle.controls.tree.TreeModel
import io.nacular.doodle.controls.treecolumns.TreeColumns
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.GradientPaint
import io.nacular.doodle.drawing.ImagePaint
import io.nacular.doodle.drawing.LinearGradientPaint
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.PatternPaint
import io.nacular.doodle.drawing.RadialGradientPaint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.drawing.grayScale
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.SegmentBuilder
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.theme.Modules.BehaviorResolver
import io.nacular.doodle.theme.Modules.Companion.ThemeModule
import io.nacular.doodle.theme.Modules.Companion.bindBehavior
import io.nacular.doodle.theme.PaintMapper
import io.nacular.doodle.theme.PathProgressIndicatorBehavior
import io.nacular.doodle.theme.PathProgressIndicatorBehavior.Direction
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.basic.BasicMenuBehavior.Config
import io.nacular.doodle.theme.basic.date.BasicDaysOfTheWeekPanelBehavior
import io.nacular.doodle.theme.basic.date.BasicMonthPanelBehavior
import io.nacular.doodle.theme.basic.list.basicHorizontalListBehavior
import io.nacular.doodle.theme.basic.list.basicHorizontalMutableListBehavior
import io.nacular.doodle.theme.basic.list.basicVerticalListBehavior
import io.nacular.doodle.theme.basic.list.basicVerticalMutableListBehavior
import io.nacular.doodle.theme.basic.range.BasicCircularRangeSliderBehavior
import io.nacular.doodle.theme.basic.range.BasicCircularSliderBehavior
import io.nacular.doodle.theme.basic.range.BasicRangeSliderBehavior
import io.nacular.doodle.theme.basic.range.BasicSliderBehavior
import io.nacular.doodle.theme.basic.range.TickPresentation
import io.nacular.doodle.theme.basic.selectbox.BasicMutableSelectBoxBehavior
import io.nacular.doodle.theme.basic.selectbox.BasicSelectBoxBehavior
import io.nacular.doodle.theme.basic.spinbutton.BasicMutableSpinButtonBehavior
import io.nacular.doodle.theme.basic.spinbutton.BasicSpinButtonBehavior
import io.nacular.doodle.theme.basic.tabbedpanel.BasicTabProducer
import io.nacular.doodle.theme.basic.tabbedpanel.BasicTabbedPanelBehavior
import io.nacular.doodle.theme.basic.tabbedpanel.SimpleTabContainer
import io.nacular.doodle.theme.basic.tabbedpanel.TabContainer
import io.nacular.doodle.theme.basic.tabbedpanel.TabProducer
import io.nacular.doodle.theme.basic.table.BasicMutableTableBehavior
import io.nacular.doodle.theme.basic.table.BasicTableBehavior
import io.nacular.doodle.theme.basic.table.BasicTreeTableBehavior
import io.nacular.doodle.theme.basic.tree.BasicMutableTreeBehavior
import io.nacular.doodle.theme.basic.tree.BasicTreeBehavior
import io.nacular.doodle.theme.basic.treecolumns.BasicTreeColumnsBehavior
import io.nacular.doodle.theme.basic.treecolumns.SimpleTreeColumnRowIcon
import io.nacular.doodle.theme.basic.treecolumns.TreeColumnRowIcon
import io.nacular.doodle.utils.RotationDirection
import io.nacular.doodle.utils.RotationDirection.Clockwise
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times
import kotlinx.datetime.DayOfWeek
import org.kodein.di.DI
import org.kodein.di.DI.Module
import org.kodein.di.DirectDI
import org.kodein.di.bind
import org.kodein.di.bindings.NoArgBindingDI
import org.kodein.di.erasedSet
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.provider
import org.kodein.di.singleton

private typealias BTheme                 = BasicTheme
private typealias ListModel<T>           = io.nacular.doodle.controls.ListModel<T>
private typealias MutableTreeModel<T>    = io.nacular.doodle.controls.tree.MutableTreeModel<T>
private typealias TabContainerFactory<T> = DirectDI.(TabbedPanel<T>, TabProducer<T>) -> TabContainer<T>

@Suppress("UNCHECKED_CAST")
public open class BasicTheme(private val configProvider: ConfigProvider, behaviors: kotlin.collections.List<BehaviorResolver>): DynamicTheme(behaviors.filter { it.theme == BTheme::class }) {
    override fun selected() {
        configProvider.config = config

        super.selected()
    }

    public open val config: BasicThemeConfig = object: BasicThemeConfig {}

    override fun toString(): String = this::class.simpleName ?: ""

    public interface BasicThemeConfig {
        public val borderColor           : Color  get() = Color(0x888888u)
        public val oddItemColor          : Color  get() = foregroundColor.inverted
        public val evenItemColor         : Color  get() = lightBackgroundColor
        public val selectionColor        : Color  get() = Color(0x0063e1u)
        public val foregroundColor       : Color  get() = Black
        public val backgroundColor       : Color  get() = Color(0xccccccu)
        public val darkBackgroundColor   : Color  get() = Color(0xaaaaaau)
        public val lightBackgroundColor  : Color  get() = Color(0xf3f4f5u)
        public val defaultBackgroundColor: Color  get() = backgroundColor
        public val cornerRadius          : Double get() = 6.0
        public val checkBoxCornerRadius  : Double get() = 4.0
        public val hoverColorMapper      : ColorMapper get() = { it.darker(0.1f) }
        public val disabledColorMapper   : ColorMapper get() = { it.lighter()    }
        public val disabledPaintMapper   : PaintMapper get() = defaultDisabledPaintMapper
    }

    public interface ConfigProvider {
        public var config: BasicThemeConfig
    }

    private class ConfigProviderImpl: ConfigProvider {
        override var config: BasicThemeConfig = object: BasicThemeConfig {}
    }

    @Suppress("MemberVisibilityCanBePrivate")
    public companion object {
        private val Config = Module(name = "BasicThemeConfig") {
            bind<ConfigProvider>  () with singleton { ConfigProviderImpl()              }
            bind<BasicThemeConfig>() with provider  { instance<ConfigProvider>().config }
        }

        public fun basicThemeModule(name: String, init: DI.Builder.() -> Unit): Module = Module(name = name) {
            importOnce(Config, allowOverride = true)

            init()
        }

        public val BasicTheme: Module = basicThemeModule(name = "BasicTheme") {
            importOnce(ThemeModule, allowOverride = true)

            bind<BasicTheme>() with singleton { BasicTheme(instance(), Instance(erasedSet<BehaviorResolver>()).toList()) }
        }

        public fun basicListBehavior(
            itemHeight           : Double? = null,
            evenItemColor        : Color?  = null,
            oddItemColor         : Color?  = null,
            selectionColor       : Color?  = null,
            selectionBlurredColor: Color?  = null
        ): Module = basicThemeModule(name = "BasicListBehavior") {
            bindBehavior<List<Any, ListModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    basicVerticalListBehavior(
                        focusManager          = instanceOrNull(),
                        evenItemColor         = evenItemColor         ?: this.evenItemColor,
                        oddItemColor          = oddItemColor          ?: this.oddItemColor,
                        selectionColor        = selectionColor        ?: this.selectionColor,
                        selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter(),
                        itemHeight            = itemHeight            ?: 20.0,
                        numColumns            = when (it) {
                            is VerticalList        -> it.numColumns
                            is VerticalDynamicList -> it.numColumns
                            is VerticalMutableList -> it.numColumns
                            else                   -> 1
                        }
                    )
                }
            }
        }

        public fun basicHorizontalListBehavior(
            itemWidth            : Double? = null,
            evenItemColor        : Color?  = null,
            oddItemColor         : Color?  = null,
            selectionColor       : Color?  = null,
            selectionBlurredColor: Color?  = null
        ): Module = basicThemeModule(name = "BasicHorizontalListBehavior") {
            val a: NoArgBindingDI<*>.(List<Any, *>) -> Unit = {
                it.behavior = instance<BasicThemeConfig>().run {
                    basicHorizontalListBehavior(
                        focusManager          = instanceOrNull(),
                        evenItemColor         = evenItemColor         ?: this.evenItemColor,
                        oddItemColor          = oddItemColor          ?: this.oddItemColor,
                        selectionColor        = selectionColor        ?: this.selectionColor,
                        selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter(),
                        itemWidth             = itemWidth             ?: 20.0,
                        numRows               = when (it) {
                            is HorizontalList        -> it.numRows
                            is HorizontalDynamicList -> it.numRows
                            is HorizontalMutableList -> it.numRows
                            else                     -> 1
                        }
                    )
                }
            }

            bindBehavior<HorizontalList       <Any, ListModel       <Any>>>(BTheme::class, a)
            bindBehavior<HorizontalDynamicList<Any, DynamicListModel<Any>>>(BTheme::class, a)
            bindBehavior<HorizontalMutableList<Any, MutableListModel<Any>>>(BTheme::class, a)
        }

        public fun basicMutableListBehavior(
            itemHeight           : Double? = null,
            evenItemColor        : Color?  = null,
            oddItemColor         : Color?  = null,
            selectionColor       : Color?  = null,
            selectionBlurredColor: Color?  = null
        ): Module = basicThemeModule(name = "BasicMutableListBehavior") {
            bindBehavior<MutableList<Any, MutableListModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    basicVerticalMutableListBehavior(
                        focusManager          = instanceOrNull(),
                        evenItemColor         = evenItemColor         ?: this.evenItemColor,
                        oddItemColor          = oddItemColor          ?: this.oddItemColor,
                        selectionColor        = selectionColor        ?: this.selectionColor,
                        selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter(),
                        itemHeight            = itemHeight            ?: 20.0,
                        numColumns            = if (it is VerticalMutableList) it.numColumns else 1
                ) }
            }
        }

        public fun basicHorizontalMutableListBehavior(
            itemWidth            : Double? = null,
            evenItemColor        : Color?  = null,
            oddItemColor         : Color?  = null,
            selectionColor       : Color?  = null,
            selectionBlurredColor: Color?  = null
        ): Module = basicThemeModule(name = "BasicHorizontalMutableListBehavior") {
            bindBehavior<HorizontalMutableList<Any, MutableListModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    basicHorizontalMutableListBehavior(
                        focusManager          = instanceOrNull(),
                        evenItemColor         = evenItemColor         ?: this.evenItemColor,
                        oddItemColor          = oddItemColor          ?: this.oddItemColor,
                        selectionColor        = selectionColor        ?: this.selectionColor,
                        selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter(),
                        itemWidth             = itemWidth             ?: 20.0,
                        numRows               = it.numRows
                    )
                }
            }
        }

        public fun basicTreeBehavior(
                rowHeight            : Double?              = null,
                evenRowColor         : Color?               = null,
                oddRowColor          : Color?               = null,
                selectionColor       : Color?               = null,
                selectionBlurredColor: Color?               = null,
                iconFactory          : (() -> TreeRowIcon)? = null): Module = basicThemeModule(name = "BasicTreeBehavior") {
            bindBehavior<Tree<Any,TreeModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicTreeBehavior(
                            focusManager          = instanceOrNull(),
                            rowHeight             = rowHeight             ?: 20.0,
                            evenRowColor          = evenRowColor          ?: this.evenItemColor,
                            oddRowColor           = oddRowColor           ?: this.oddItemColor,
                            selectionColor        = selectionColor        ?: this.selectionColor,
                            selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter(),
                            iconFactory           = iconFactory           ?: { SimpleTreeRowIcon(foregroundColor, foregroundColor.inverted) }
                    )
                }
            }
        }

        public fun basicMutableTreeBehavior(
                rowHeight            : Double?              = null,
                evenRowColor         : Color?               = null,
                oddRowColor          : Color?               = null,
                selectionColor       : Color?               = null,
                selectionBlurredColor: Color?               = null,
                iconFactory          : (() -> TreeRowIcon)? = null): Module = basicThemeModule(name = "BasicMutableTreeBehavior") {
            bindBehavior<MutableTree<Any, MutableTreeModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicMutableTreeBehavior(
                            focusManager          = instanceOrNull(),
                            rowHeight             = rowHeight             ?: 20.0,
                            evenRowColor          = evenRowColor          ?: this.evenItemColor,
                            oddRowColor           = oddRowColor           ?: this.oddItemColor,
                            selectionColor        = selectionColor        ?: this.selectionColor,
                            selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter(),
                            iconFactory           = iconFactory           ?: { SimpleTreeRowIcon(foregroundColor, foregroundColor.inverted) }
                ) }
            }
        }

        public fun basicTreeTableBehavior(
            rowHeight            : Double?              = null,
            evenRowColor         : Color?               = null,
            oddRowColor          : Color?               = null,
            selectionColor       : Color?               = null,
            selectionBlurredColor: Color?               = null,
            iconFactory          : (() -> TreeRowIcon)? = null
        ): Module = basicThemeModule(name = "BasicTreeTableBehavior") {
            bindBehavior<TreeTable<Any, TreeModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicTreeTableBehavior(
                        focusManager          = instanceOrNull(),
                        rowHeight             = rowHeight             ?: 20.0,
                        evenRowColor          = evenRowColor          ?: this.evenItemColor,
                        oddRowColor           = oddRowColor           ?: this.oddItemColor,
                        selectionColor        = selectionColor        ?: this.selectionColor,
                        selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter(),
                        iconFactory           = iconFactory           ?: { SimpleTreeRowIcon(foregroundColor, foregroundColor.inverted) }
                    ) }
            }
        }

        public fun basicLabelBehavior(foregroundColor: Color? = null): Module = basicThemeModule(name = "BasicLabelBehavior") {
            bindBehavior<Label>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    CommonLabelBehavior(instance(), foregroundColor ?: this.foregroundColor).apply {
                        disabledColorMapper = this@run.disabledColorMapper
                    }
                }
            }
        }

        public fun basicTableBehavior(
                rowHeight            : Double? = null,
                headerColor          : Color?  = null,
                footerColor          : Color?  = null,
                evenRowColor         : Color?  = null,
                oddRowColor          : Color?  = null,
                selectionColor       : Color?  = null,
                selectionBlurredColor: Color?  = null): Module = basicThemeModule(name = "BasicTableBehavior") {
            bindBehavior<Table<Any, ListModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicTableBehavior(
                        focusManager          = instanceOrNull(),
                        rowHeight             = rowHeight             ?: 20.0,
                        headerColor           = headerColor           ?: this.backgroundColor,
                        footerColor           = footerColor           ?: this.backgroundColor,
                        evenRowColor          = evenRowColor          ?: this.evenItemColor,
                        oddRowColor           = oddRowColor           ?: this.oddItemColor,
                        selectionColor        = selectionColor        ?: this.selectionColor,
                        selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter()
                ) }
            }
        }

        public fun basicMutableTableBehavior(
                rowHeight            : Double? = null,
                headerColor          : Color?  = null,
                footerColor          : Color? = null,
                evenRowColor         : Color?  = null,
                oddRowColor          : Color?  = null,
                selectionColor       : Color?  = null,
                selectionBlurredColor: Color?  = null): Module = basicThemeModule(name = "BasicMutableTableBehavior") {
            bindBehavior<MutableTable<Any, MutableListModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicMutableTableBehavior(
                        focusManager          = instanceOrNull(),
                        rowHeight             = rowHeight             ?: 20.0,
                        headerColor           = headerColor           ?: this.backgroundColor,
                        footerColor           = footerColor           ?: this.backgroundColor,
                        evenRowColor          = evenRowColor          ?: this.evenItemColor,
                        oddRowColor           = oddRowColor           ?: this.oddItemColor,
                        selectionColor        = selectionColor        ?: this.selectionColor,
                        selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter()
                ) }
            }
        }

        public fun basicTreeColumnsBehavior(
                rowHeight            : Double? = null,
                columnSeparatorColor : Color?  = null,
                selectionColor       : Color?  = null,
                selectionBlurredColor: Color?  = null,
                backgroundColor      : Color?  = null,
                iconFactory          : (() -> TreeColumnRowIcon)? = null): Module = basicThemeModule(name = "BasicTreeColumnsBehavior") {
            bindBehavior<TreeColumns<Any, *>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicTreeColumnsBehavior (
                        focusManager          = instanceOrNull(),
                        rowHeight             = rowHeight             ?: 20.0,
                        columnSeparatorColor  = columnSeparatorColor  ?: this.backgroundColor,
                        backgroundColor       = backgroundColor       ?: this.oddItemColor,
                        selectionColor        = selectionColor        ?: this.selectionColor,
                        selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter(),
                        iconFactory           = iconFactory           ?: { SimpleTreeColumnRowIcon(foregroundColor, foregroundColor.inverted) }
                ) }
            }
        }

        public fun basicButtonBehavior(
                backgroundColor    : Color?  = null,
                darkBackgroundColor: Color?  = null,
                foregroundColor    : Color?  = null,
                borderColor        : Color?  = null,
                borderWidth        : Double? = null,
                cornerRadius       : Double? = null,
                insets             : Double? = null): Module = basicThemeModule(name = "BasicButtonBehavior") {
            bindBehavior<Button>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicButtonBehavior(
                            instance(),
                            backgroundColor     = backgroundColor     ?: this.backgroundColor,
                            darkBackgroundColor = darkBackgroundColor ?: this.darkBackgroundColor,
                            foregroundColor     = foregroundColor     ?: this.foregroundColor,
                            borderColor         = borderColor         ?: this.borderColor,
                            borderWidth         = borderWidth         ?: 0.0,
                            cornerRadius        = cornerRadius        ?: this.cornerRadius,
                            insets              = insets              ?: 8.0,
                            focusManager        = instanceOrNull()
                    ).apply {
                        hoverColorMapper     = this@run.hoverColorMapper
                        disabledColorMapper  = this@run.disabledColorMapper
                    }
                }
            }
        }

        public fun basicSliderBehavior(
                barFill             : Paint?            = null,
                knobFill            : Paint?            = null,
                rangeFill           : Paint?            = null,
                grooveThicknessRatio: Float?            = null,
                showTicks           : TickPresentation? = null): Module = basicThemeModule(name = "BasicSliderBehavior") {
            bindBehavior<Slider<Double>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicSliderBehavior<Double>(
                        barFill              = barFill              ?: defaultBackgroundColor.paint,
                        knobFill             = knobFill             ?: darkBackgroundColor.paint,
                        rangeFill            = rangeFill,
                        grooveThicknessRatio = grooveThicknessRatio ?: 0.5f,
                        showTicks            = showTicks,
                        focusManager         = instanceOrNull()
                    ).apply {
                        disabledPaintMapper = this@run.disabledPaintMapper
                    }
                }
            }
        }

        public fun basicRangeSliderBehavior(
            barFill             : Paint?            = null,
            startKnobFill       : Paint?            = null,
            endKnobFill         : Paint?            = startKnobFill,
            rangeFill           : Paint?            = endKnobFill,
            grooveThicknessRatio: Float?            = null,
            showTicks           : TickPresentation? = null
        ): Module = basicThemeModule(name = "BasicRangeSliderBehavior") {
            bindBehavior<RangeSlider<Double>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicRangeSliderBehavior<Double>(
                        barFill              = barFill              ?: defaultBackgroundColor.paint,
                        startKnobFill        = startKnobFill        ?: darkBackgroundColor.paint,
                        endKnobFill          = endKnobFill          ?: startKnobFill ?: darkBackgroundColor.paint,
                        rangeFill            = rangeFill            ?: darkBackgroundColor.paint,
                        grooveThicknessRatio = grooveThicknessRatio ?: 0.5f,
                        showTicks            = showTicks,
                        focusManager         = instanceOrNull()
                    ).apply {
                        disabledPaintMapper = this@run.disabledPaintMapper
                    }
                }
            }
        }

        public fun basicRangeSliderBehavior(
            barFill             : Paint?            = null,
            knobFill            : Paint?            = null,
            rangeFill           : Paint?            = knobFill,
            grooveThicknessRatio: Float?            = null,
            showTicks           : TickPresentation? = null): Module = basicRangeSliderBehavior(
            barFill              = barFill,
            startKnobFill        = knobFill,
            rangeFill            = rangeFill,
            grooveThicknessRatio = grooveThicknessRatio,
            showTicks            = showTicks
        )

        public fun basicSpinButtonBehavior(
            backgroundColor    : Color?  = null,
            darkBackgroundColor: Color?  = null,
            foregroundColor    : Color?  = null,
            cornerRadius       : Double? = null,
            buttonWidth        : Double? = null,
            incrementA11yLabel : String? = null,
            decrementA11yLabel : String? = null,
        ): Module = basicThemeModule(name = "BasicSpinButtonBehavior") {
            bindBehavior<SpinButton<Any, SpinButtonModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicSpinButtonBehavior<Any, SpinButtonModel<Any>>(
                        instance(),
                        buttonWidth         = buttonWidth         ?: 20.0,
                        focusManager        = instanceOrNull(),
                        cornerRadius        = cornerRadius        ?: this.cornerRadius,
                        backgroundColor     = backgroundColor     ?: this.backgroundColor,
                        foregroundColor     = foregroundColor     ?: this.foregroundColor,
                        incrementA11yLabel  = incrementA11yLabel,
                        decrementA11yLabel  = decrementA11yLabel,
                        darkBackgroundColor = darkBackgroundColor ?: this.darkBackgroundColor,
                    ).apply {
                        hoverColorMapper     = this@run.hoverColorMapper
                        disabledColorMapper  = this@run.disabledColorMapper
                    }
                }
            }
        }

        public fun basicMutableSpinButtonBehavior(
            backgroundColor    : Color?  = null,
            darkBackgroundColor: Color?  = null,
            foregroundColor    : Color?  = null,
            cornerRadius       : Double? = null,
            buttonWidth        : Double? = null,
            incrementA11yLabel : String? = null,
            decrementA11yLabel : String? = null,
        ): Module = basicThemeModule(name = "BasicMutableSpinButtonBehavior") {
            bindBehavior<MutableSpinButton<Any, MutableSpinButtonModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicMutableSpinButtonBehavior<Any, MutableSpinButtonModel<Any>>(
                        instance(),
                        buttonWidth         = buttonWidth         ?: 20.0,
                        cornerRadius        = cornerRadius        ?: this.cornerRadius,
                        focusManager        = instanceOrNull(),
                        incrementLabel      = incrementA11yLabel,
                        decrementLabel      = decrementA11yLabel,
                        backgroundColor     = backgroundColor     ?: this.backgroundColor,
                        foregroundColor     = foregroundColor     ?: this.foregroundColor,
                        darkBackgroundColor = darkBackgroundColor ?: this.darkBackgroundColor,
                    ).apply {
                        hoverColorMapper     = this@run.hoverColorMapper
                        disabledColorMapper  = this@run.disabledColorMapper
                    }
                }
            }
        }

        public fun basicCheckBoxBehavior(
            foregroundColor    : Color?  = null,
            backgroundColor    : Color?  = null,
            darkBackgroundColor: Color?  = null,
            cornerRadius       : Double? = null,
            iconTextSpacing    : Double? = null,
            iconInset          : Double? = null,
            checkInset         : ((CheckBox) -> Float)? = null,
            iconSize           : ((CheckBox) -> Size)? = null
        ): Module = basicThemeModule(name = "BasicCheckBoxBehavior") {
            bindBehavior<CheckBox>(BTheme::class) { checkBox ->
                val iconInsets = iconInset ?: 1.0

                checkBox.behavior = instance<BasicThemeConfig>().run { BasicCheckBoxBehavior(
                    instance(),
                    iconSize            = iconSize            ?: { Size(maxOf(0.0, minOf(16.0, it.height - 2 * iconInsets, it.width - 2 * iconInsets))) },
                    checkInset          = checkInset          ?: { 0.5f },
                    iconTextSpacing     = iconTextSpacing     ?: 8.0,
                    iconInset           = iconInsets,
                    cornerRadius        = cornerRadius        ?: this.checkBoxCornerRadius,
                    backgroundColor     = backgroundColor     ?: this.backgroundColor,
                    foregroundColor     = foregroundColor     ?: this.foregroundColor,
                    darkBackgroundColor = darkBackgroundColor ?: this.darkBackgroundColor,
                    hoverColorMapper    = this@run.hoverColorMapper,
                    disabledColorMapper = this@run.disabledColorMapper,
                    focusManager        = instanceOrNull()
                ) as Behavior<Button> }
            }
        }

        public fun basicRadioButtonBehavior(
            foregroundColor    : Color?                     = null,
            backgroundColor    : Color?                     = null,
            darkBackgroundColor: Color?                     = null,
            iconSpacing        : Double?                    = null,
            iconInset          : Double?                    = null,
            innerCircleInset   : ((RadioButton) -> Double)? = null,
            iconSize           : ((RadioButton) -> Size  )? = null,
        ): Module = basicThemeModule(name = "BasicRadioButtonBehavior") {
            bindBehavior<RadioButton>(BTheme::class) {radioButton ->
                val iconInsets = iconInset ?: 1.0

                radioButton.behavior = instance<BasicThemeConfig>().run { BasicRadioBehavior(
                    instance(),
                    iconTextSpacing     = iconSpacing         ?: 8.0,
                    iconSize            = iconSize            ?: { Size(maxOf(0.0, minOf(16.0, it.height - 2 * iconInsets, it.width - 2 * iconInsets))) },
                    backgroundColor     = backgroundColor     ?: this.backgroundColor,
                    foregroundColor     = foregroundColor     ?: this.foregroundColor,
                    iconInset           = iconInsets,
                    innerCircleInset    = innerCircleInset    ?: { 4.0 },
                    darkBackgroundColor = darkBackgroundColor ?: this.darkBackgroundColor,
                    hoverColorMapper    = this@run.hoverColorMapper,
                    disabledColorMapper = this@run.disabledColorMapper,
                    focusManager        = instanceOrNull(),
                ) as Behavior<Button> }
            }
        }

        public fun basicSwitchBehavior(
                onBackground : Color? = null,
                onForeground : Color? = null,
                offBackground: Color? = null,
                offForeground: Color? = null
        ): Module = basicThemeModule(name = "BasicSwitchBehavior") {
            bindBehavior<Switch>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicSwitchBehavior(
                            onBackground ?: Blue,
                            onForeground ?: White,
                            offBackground?: backgroundColor,
                            offForeground?: onForeground ?: White,
                            focusManager = instanceOrNull()).apply {
                        hoverColorMapper    = this@run.hoverColorMapper
                        disabledColorMapper = this@run.disabledColorMapper
                    } as Behavior<Button>
                }
            }
        }

        public fun basicSplitPanelBehavior(
            showDivider      : Boolean = false,
            background       : Paint?  = null,
            dividerBackground: Paint?  = null
        ): Module = basicThemeModule(name = "BasicSplitPanelBehavior") {
            bindBehavior<SplitPanel>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicSplitPanelBehavior(
                        background        = background        ?: this.backgroundColor.paint,
                        dividerBackground = dividerBackground ?: if (showDivider) this.backgroundColor.paint else null,
                    )
                }
            }
        }

        public fun basicProgressBarBehavior(
                background      : Paint?  = null,
                foreground      : Paint?  = null,
                outlineColor    : Color?  = null,
                backgroundRadius: Double? = null,
                foregroundRadius: Double? = backgroundRadius): Module = basicThemeModule(name = "BasicProgressBarBehavior") {
            bindBehavior<ProgressBar>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicProgressBarBehavior(
                            background       = background ?: defaultBackgroundColor.paint,
                            foreground       = foreground ?: darkBackgroundColor.paint,
                            outlineColor     = outlineColor,
                            backgroundRadius = backgroundRadius ?: cornerRadius,
                            foregroundRadius = foregroundRadius ?: 0.0).apply {
                        disabledPaintMapper = this@run.disabledPaintMapper
                    } as Behavior<ProgressIndicator>
                }
            }
        }

        public fun basicCircularProgressIndicatorBehavior(
                foreground      : Paint?            = null,
                background      : Paint?            = null,
                thickness       : Double            = 15.0,
                outline         : Stroke?           = null,
                startAngle      : Measure<Angle>    = -90 * degrees,
                direction       : RotationDirection = Clockwise,
                startCap        : SegmentBuilder    = { _,_  ->            },
                endCap          : SegmentBuilder    = { _,it -> lineTo(it) }): Module = basicThemeModule(name = "BasicCircularProgressBarBehavior") {
            bindBehavior<ProgressIndicator>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicCircularProgressIndicatorBehavior(
                        foreground ?: darkBackgroundColor.paint,
                        background ?: defaultBackgroundColor.paint,
                        thickness,
                        outline,
                        startAngle,
                        direction,
                        startCap,
                        endCap
                    ).apply {
                        disabledPaintMapper = this@run.disabledPaintMapper
                    }
                }
            }
        }

        public fun basicPathProgressIndicatorBehavior(
                path               : Path,
                foreground         : Paint?    = null,
                background         : Paint?    = null,
                foregroundThickness: Double    = 1.0,
                backgroundThickness: Double    = foregroundThickness,
                direction          : Direction = Direction.Forward
        ): Module = basicThemeModule(name = "BasicPathProgressIndicatorBehavior") {
            bindBehavior<ProgressIndicator>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    PathProgressIndicatorBehavior(
                        pathMetrics         = instance(),
                        path                = path,
                        foreground          = foreground ?: darkBackgroundColor.paint,
                        background          = background ?: defaultBackgroundColor.paint,
                        foregroundThickness = foregroundThickness,
                        backgroundThickness = backgroundThickness,
                        direction           = direction).apply {
                        disabledPaintMapper = this@run.disabledPaintMapper
                    }
                }
            }
        }

        public fun basicCircularSliderBehavior(
                barFill  : Paint? = null,
                knobFill : Paint? = null,
                rangeFill: Paint? = null,
                thickness: Double = 20.0
        ): Module = basicThemeModule(name = "BasicCircularSliderBehavior") {
            bindBehavior<CircularSlider<Double>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicCircularSliderBehavior<Double>(
                        barFill      = barFill   ?: defaultBackgroundColor.paint,
                        knobFill     = knobFill  ?: darkBackgroundColor.paint,
                        rangeFill    = rangeFill,
                        thickness    = thickness,
                        focusManager = instanceOrNull()
                    ).apply {
                        disabledPaintMapper = this@run.disabledPaintMapper
                    }
                }
            }
        }

        /**
         * Creates a basic behavior for rendering [CircularRangeSlider]s.
         *
         * @param barFill       for the slider's background
         * @param startKnobFill for the knob at the start of the slider's range
         * @param endKnobFill   for the knob at the end of the slider's range
         * @param rangeFill     for the slider's range region
         * @param thickness     of the slider
         */
        public fun basicCircularRangeSliderBehavior(
            barFill       : Paint? = null,
            startKnobFill : Paint? = null,
            endKnobFill   : Paint? = startKnobFill,
            rangeFill     : Paint? = endKnobFill,
            thickness     : Double = 20.0
        ): Module = basicThemeModule(name = "BasicCircularRangeSliderBehavior") {
            bindBehavior<CircularRangeSlider<Double>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicCircularRangeSliderBehavior<Double>(
                        barFill       = barFill       ?: defaultBackgroundColor.paint,
                        startKnobFill = startKnobFill ?: darkBackgroundColor.paint,
                        endKnobFill   = endKnobFill   ?: startKnobFill ?: darkBackgroundColor.paint,
                        rangeFill     = rangeFill     ?: darkBackgroundColor.paint,
                        thickness     = thickness,
                        focusManager  = instanceOrNull()
                    ).apply {
                        disabledPaintMapper = this@run.disabledPaintMapper
                    }
                }
            }
        }

        /**
         * Creates a basic behavior for rendering [CircularRangeSlider]s.
         *
         * @param barFill   for the slider's background
         * @param knobFill  for the knob at the start of the slider's range
         * @param rangeFill for the slider's range region
         * @param thickness of the slider
         */
        public fun basicCircularRangeSliderBehavior(
            barFill  : Paint? = null,
            knobFill : Paint? = null,
            rangeFill: Paint? = knobFill,
            thickness: Double = 20.0): Module = basicCircularRangeSliderBehavior(
            barFill       = barFill,
            startKnobFill = knobFill,
            rangeFill     = rangeFill,
            thickness     = thickness
        )

        public fun basicTabbedPanelBehavior(
                tabProducer    : TabProducer<Any>?         = null,
                backgroundColor: Color?                    = null,
                tabContainer   : TabContainerFactory<Any>? = null): Module = basicThemeModule(name = "BasicTabbedPanelBehavior") {
            bindBehavior<TabbedPanel<Any>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicTabbedPanelBehavior(
                        tabProducer ?: BasicTabProducer(
                            tabColor            = backgroundColor ?: this.backgroundColor,
                            hoverColorMapper    = this@run.hoverColorMapper,
                            selectedColorMapper = { foregroundColor.inverted }
                        ),
                        backgroundColor ?: this.backgroundColor,
                        tabContainer?.let {
                            { panel: TabbedPanel<Any>, tabProducer: TabProducer<Any> ->
                                it(this@bindBehavior, panel, tabProducer)
                            }
                        } ?: { panel, tabProducer -> SimpleTabContainer(panel, tabProducer) }
                    )
                }
            }
        }

        public fun basicSelectBoxBehavior(
            backgroundColor    : Color?  = null,
            darkBackgroundColor: Color?  = null,
            foregroundColor    : Color?  = null,
            cornerRadius       : Double? = null,
            buttonWidth        : Double? = null,
            buttonA11yLabel    : String? = null,
            inset              : Double? = null,
        ): Module = basicThemeModule(name = "BasicSelectBoxBehavior") {
            bindBehavior<SelectBox<Any, ListModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicSelectBoxBehavior<Any, ListModel<Any>>(
                        display             = instance(),
                        textMetrics         = instance(),
                        buttonWidth         = buttonWidth         ?: 20.0,
                        focusManager        = instanceOrNull(),
                        popupManager        = instanceOrNull(),
                        cornerRadius        = cornerRadius        ?: this.cornerRadius,
                        backgroundColor     = backgroundColor     ?: this.backgroundColor,
                        foregroundColor     = foregroundColor     ?: this.foregroundColor,
                        buttonA11yLabel     = buttonA11yLabel,
                        darkBackgroundColor = darkBackgroundColor ?: this.darkBackgroundColor,
                        inset               = inset               ?: 4.0,
                    ).apply {
                        hoverColorMapper     = this@run.hoverColorMapper
                        disabledColorMapper  = this@run.disabledColorMapper
                    }
                }
            }
        }

        public fun basicMutableSelectBoxBehavior(
            backgroundColor    : Color?  = null,
            darkBackgroundColor: Color?  = null,
            foregroundColor    : Color?  = null,
            cornerRadius       : Double? = null,
            buttonWidth        : Double? = null,
            buttonA11yLabel    : String? = null,
            inset              : Double? = null,
        ): Module = basicThemeModule(name = "BasicMutableSelectBoxBehavior") {
            bindBehavior<MutableSelectBox<Any, MutableListModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicMutableSelectBoxBehavior<Any, MutableListModel<Any>>(
                        display             = instance(),
                        textMetrics         = instance(),
                        buttonWidth         = buttonWidth         ?: 20.0,
                        focusManager        = instanceOrNull(),
                        popupManager        = instanceOrNull(),
                        cornerRadius        = cornerRadius        ?: this.cornerRadius,
                        backgroundColor     = backgroundColor     ?: this.backgroundColor,
                        foregroundColor     = foregroundColor     ?: this.foregroundColor,
                        darkBackgroundColor = darkBackgroundColor ?: this.darkBackgroundColor,
                        buttonA11yLabel     = buttonA11yLabel,
                        inset               = inset               ?: 4.0,
                    ).apply {
                        hoverColorMapper     = this@run.hoverColorMapper
                        disabledColorMapper  = this@run.disabledColorMapper
                    }
                }
            }
        }

        public fun basicMonthPanelBehavior(background: Paint? = null): Module = basicThemeModule(name = "BasicMonthPanelBehavior") {
            bindBehavior<MonthPanel>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicMonthPanelBehavior(background ?: this@run.backgroundColor.paint)
                }
            }
        }

        public fun basicDaysOfTheWeekPanelBehavior(
            background       : Paint? = null,
            defaultVisualizer: ItemVisualizer<DayOfWeek, Unit>? = null
        ): Module = basicThemeModule(name = "BasicDaysOfTheWeekPanelBehavior") {
            bindBehavior<DaysOfTheWeekPanel>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicDaysOfTheWeekPanelBehavior(background ?: this@run.backgroundColor.paint, defaultVisualizer)
                }
            }
        }

        public fun basicGridPanelBehavior(background: Paint? = null): Module = basicThemeModule(name = "BasicGridPanelBehavior") {
            bindBehavior<GridPanel>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    object: Behavior<GridPanel> {
                        override fun render(view: GridPanel, canvas: Canvas) {
                            canvas.rect(view.bounds.atOrigin, fill = (background ?: this@run.backgroundColor.paint))
                        }
                    }
                }
            }
        }

        public fun basicMenuBehavior(
            menuFillPaint            : Paint? = null,
            itemTextPaint            : Paint? = null,
            itemDisabledTextPaint    : Paint? = null,
            subMenuIconPaint         : Paint? = null,
            itemHighlightPaint       : Paint? = null,
            itemTextSelectedPaint    : Paint? = null,
            subMenuIconSelectedPaint : Paint? = null,
            separatorPaint           : Paint? = null,
        ): Module = basicThemeModule(name = "BasicMenuBehavior") {
            bindBehavior<Menu>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicMenuBehavior(instance(), instance(), config = Config(
                        menuFillPaint            = menuFillPaint            ?: this.backgroundColor.paint,
                        itemTextPaint            = itemTextPaint            ?: this.foregroundColor.paint,
                        itemDisabledTextPaint    = itemDisabledTextPaint    ?: this.disabledPaintMapper(this.foregroundColor.paint),
                        subMenuIconPaint         = subMenuIconPaint         ?: this.foregroundColor.paint,
                        itemHighlightPaint       = itemHighlightPaint       ?: this.selectionColor.paint,
                        itemTextSelectedPaint    = itemTextSelectedPaint    ?: White.paint,
                        subMenuIconSelectedPaint = subMenuIconSelectedPaint ?: White.paint,
                        separatorPaint           = separatorPaint           ?: this.darkBackgroundColor.paint,
                    ))
                }
            }
        }

        public fun basicThemeBehaviors(): kotlin.collections.List<Module> = listOf(
            basicListBehavior(),
            basicTreeBehavior(),
            basicLabelBehavior(),
            basicTableBehavior(),
            basicButtonBehavior(),
            basicSwitchBehavior(),
            basicSliderBehavior(),
            basicRangeSliderBehavior(),
            basicCircularSliderBehavior(),
            basicCircularRangeSliderBehavior(),
            basicSpinButtonBehavior(),
            basicCheckBoxBehavior(),
            basicSelectBoxBehavior(),
            basicSplitPanelBehavior(),
            basicRadioButtonBehavior(),
            basicMutableListBehavior(),
            basicProgressBarBehavior(foregroundRadius = null),
            basicMutableTreeBehavior(),
            basicTreeColumnsBehavior(),
            basicTabbedPanelBehavior(),
            basicMutableTableBehavior(),
            basicMutableSpinButtonBehavior(),
            basicMutableSelectBoxBehavior(),
            basicMonthPanelBehavior(),
            basicDaysOfTheWeekPanelBehavior(),
            basicGridPanelBehavior(),
            basicMenuBehavior(),
        )
    }
}

public class DarkBasicTheme(configProvider: ConfigProvider, behaviors: kotlin.collections.List<BehaviorResolver>): BasicTheme(configProvider, behaviors) {
    public class DarkBasicThemeConfig: BasicThemeConfig {
        override val borderColor           : Color = super.borderColor.inverted
        override val foregroundColor       : Color = super.foregroundColor.inverted
        override val backgroundColor       : Color = super.backgroundColor.inverted
        override val darkBackgroundColor   : Color = super.darkBackgroundColor.inverted
        override val lightBackgroundColor  : Color = Color(0x282928u)
        override val defaultBackgroundColor: Color = super.defaultBackgroundColor.inverted
        override val hoverColorMapper      : ColorMapper = { it.lighter(0.3f) }
        override val disabledColorMapper   : ColorMapper = { it.darker()      }
        override val disabledPaintMapper   : PaintMapper = { paint ->
            when (paint) {
                is ColorPaint          -> paint.color.darker().paint
                is LinearGradientPaint -> LinearGradientPaint(paint.colors.map { GradientPaint.Stop(it.color.darker(), it.offset) }, start = paint.start, end = paint.end)
                is RadialGradientPaint -> RadialGradientPaint(paint.colors.map { GradientPaint.Stop(it.color.darker(), it.offset) }, start = paint.start, end = paint.end)
                is ImagePaint          -> ImagePaint(image = paint.image, size = paint.size, opacity = paint.opacity * 0.5f)
                is PatternPaint        -> PatternPaint(paint.bounds, paint.transform, paint.opacity * 0.5f, paint.paint)
                else                   -> paint
            }
        }
    }

    override val config: BasicThemeConfig = DarkBasicThemeConfig()

    public companion object {
        public val DarkBasicTheme: Module = basicThemeModule(name = "DarkBasicTheme") {
            bind<DarkBasicTheme>() with singleton { DarkBasicTheme(instance(), Instance(erasedSet<BehaviorResolver>()).toList()) }
        }
    }
}