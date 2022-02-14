@file:Suppress("unused")

package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.ProgressBar
import io.nacular.doodle.controls.ProgressIndicator
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.buttons.RadioButton
import io.nacular.doodle.controls.buttons.Switch
import io.nacular.doodle.controls.date.DaysOfTheWeekPanel
import io.nacular.doodle.controls.date.MonthPanel
import io.nacular.doodle.controls.dropdown.Dropdown
import io.nacular.doodle.controls.dropdown.MutableDropdown
import io.nacular.doodle.controls.list.HorizontalList
import io.nacular.doodle.controls.list.HorizontalMutableList
import io.nacular.doodle.controls.list.List
import io.nacular.doodle.controls.list.MutableList
import io.nacular.doodle.controls.list.VerticalList
import io.nacular.doodle.controls.list.VerticalMutableList
import io.nacular.doodle.controls.panels.SplitPanel
import io.nacular.doodle.controls.panels.TabbedPanel
import io.nacular.doodle.controls.range.CircularRangeSlider
import io.nacular.doodle.controls.range.CircularSlider
import io.nacular.doodle.controls.range.RangeSlider
import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.controls.spinner.MutableModel
import io.nacular.doodle.controls.spinner.MutableSpinner
import io.nacular.doodle.controls.spinner.Spinner
import io.nacular.doodle.controls.table.MutableTable
import io.nacular.doodle.controls.table.Table
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.theme.CommonLabelBehavior
import io.nacular.doodle.controls.tree.MutableTree
import io.nacular.doodle.controls.tree.Tree
import io.nacular.doodle.controls.tree.TreeModel
import io.nacular.doodle.controls.treecolumns.TreeColumns
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
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
import io.nacular.doodle.drawing.RadialGradientPaint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.drawing.grayScale
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.SegmentBuilder
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.theme.Modules
import io.nacular.doodle.theme.Modules.Companion.ThemeModule
import io.nacular.doodle.theme.Modules.Companion.bindBehavior
import io.nacular.doodle.theme.PaintMapper
import io.nacular.doodle.theme.PathProgressIndicatorBehavior
import io.nacular.doodle.theme.PathProgressIndicatorBehavior.Direction
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.basic.dropdown.BasicDropdownBehavior
import io.nacular.doodle.theme.basic.dropdown.BasicMutableDropdownBehavior
import io.nacular.doodle.theme.basic.list.horizontalBasicListBehavior
import io.nacular.doodle.theme.basic.list.horizontalBasicMutableListBehavior
import io.nacular.doodle.theme.basic.list.verticalBasicListBehavior
import io.nacular.doodle.theme.basic.list.verticalBasicMutableListBehavior
import io.nacular.doodle.theme.basic.range.BasicCircularRangeSliderBehavior
import io.nacular.doodle.theme.basic.range.BasicCircularSliderBehavior
import io.nacular.doodle.theme.basic.range.BasicRangeSliderBehavior
import io.nacular.doodle.theme.basic.range.BasicSliderBehavior
import io.nacular.doodle.theme.basic.spinner.BasicMutableSpinnerBehavior
import io.nacular.doodle.theme.basic.spinner.BasicSpinnerBehavior
import io.nacular.doodle.theme.basic.tabbedpanel.BasicTabProducer
import io.nacular.doodle.theme.basic.tabbedpanel.BasicTabbedPanelBehavior
import io.nacular.doodle.theme.basic.tabbedpanel.SimpleTabContainer
import io.nacular.doodle.theme.basic.tabbedpanel.TabContainer
import io.nacular.doodle.theme.basic.tabbedpanel.TabProducer
import io.nacular.doodle.theme.basic.table.BasicMutableTableBehavior
import io.nacular.doodle.theme.basic.table.BasicTableBehavior
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
import org.kodein.di.DI
import org.kodein.di.DI.Module
import org.kodein.di.DirectDI
import org.kodein.di.bind
import org.kodein.di.erasedSet
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.provider
import org.kodein.di.singleton

private typealias BTheme                 = BasicTheme
private typealias ListModel<T>           = io.nacular.doodle.controls.ListModel<T>
private typealias SpinnerModel<T>        = io.nacular.doodle.controls.spinner.Model<T>
private typealias MutableTreeModel<T>    = io.nacular.doodle.controls.tree.MutableTreeModel<T>
private typealias TabContainerFactory<T> = DirectDI.(TabbedPanel<T>, TabProducer<T>) -> TabContainer<T>

@Suppress("UNCHECKED_CAST")
public open class BasicTheme(private val configProvider: ConfigProvider, behaviors: Iterable<Modules.BehaviorResolver>): DynamicTheme(behaviors.filter { it.theme == BTheme::class }) {
    override fun install(display: Display, all: Sequence<View>) {
        configProvider.config = config

        super.install(display, all)
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
        public val hoverColorMapper      : ColorMapper get() = { it.darker(0.1f) }
        public val disabledColorMapper   : ColorMapper get() = { it.lighter()    }
        public val disabledPaintMapper   : PaintMapper get() = defaultDisabledPaintMapper
    }

    public interface ConfigProvider {
        public var config: BasicThemeConfig
    }

    private class ConfigProviderImpl: ConfigProvider {
        override var config = object: BasicThemeConfig {}
    }

    @Suppress("MemberVisibilityCanBePrivate")
    public companion object {
        public fun basicThemeModule(name: String, init: DI.Builder.() -> Unit): Module = Module(name = name) {
            importOnce(Config, allowOverride = true)

            init()
        }

        public val BasicTheme: Module = basicThemeModule(name = "BasicTheme") {
            importOnce(ThemeModule, allowOverride = true)

            bind<BasicTheme>() with singleton { BasicTheme(instance(), Instance(erasedSet())) }
        }

        private val Config = Module(name = "BasicThemeConfig") {
            bind<ConfigProvider>  () with singleton { ConfigProviderImpl()              }
            bind<BasicThemeConfig>() with provider  { instance<ConfigProvider>().config }
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
                    verticalBasicListBehavior(
                        focusManager          = instanceOrNull(),
                        evenItemColor         = evenItemColor         ?: this.evenItemColor,
                        oddItemColor          = oddItemColor          ?: this.oddItemColor,
                        selectionColor        = selectionColor        ?: this.selectionColor,
                        selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter(),
                        itemHeight            = itemHeight            ?: 20.0,
                        numColumns            = if (it is VerticalList) it.numColumns else 1
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
            bindBehavior<HorizontalList<Any, ListModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    horizontalBasicListBehavior(
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

        public fun basicMutableListBehavior(
            itemHeight           : Double? = null,
            evenItemColor        : Color?  = null,
            oddItemColor         : Color?  = null,
            selectionColor       : Color?  = null,
            selectionBlurredColor: Color?  = null
        ): Module = basicThemeModule(name = "BasicMutableListBehavior") {
            bindBehavior<MutableList<Any, MutableListModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    verticalBasicMutableListBehavior(
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
                    horizontalBasicMutableListBehavior(
                        focusManager          = instanceOrNull(),
                        evenItemColor         = evenItemColor         ?: this.evenItemColor,
                        oddItemColor          = oddItemColor          ?: this.oddItemColor,
                        selectionColor        = selectionColor        ?: this.selectionColor,
                        selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter(),
                        itemWidth             = itemWidth             ?: 20.0,
                        numRows               = it.numRows
                    ) }
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

        public fun basicLabelBehavior(foregroundColor: Color? = null): Module = basicThemeModule(name = "BasicLabelBehavior") {
            bindBehavior<Label>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    CommonLabelBehavior(instance(), foregroundColor ?: this.foregroundColor).apply {
                        disabledColorMapper  = this@run.disabledColorMapper
                    }
                }
            }
        }

        public fun basicTableBehavior(
                rowHeight            : Double? = null,
                headerColor          : Color?  = null,
                evenRowColor         : Color?  = null,
                oddRowColor          : Color?  = null,
                selectionColor       : Color?  = null,
                selectionBlurredColor: Color?  = null): Module = basicThemeModule(name = "BasicTableBehavior") {
            bindBehavior<Table<Any, ListModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicTableBehavior(
                        focusManager          = instanceOrNull(),
                        rowHeight             = rowHeight             ?: 20.0,
                        headerColor           = headerColor           ?: this.backgroundColor,
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
                evenRowColor         : Color?  = null,
                oddRowColor          : Color?  = null,
                selectionColor       : Color?  = null,
                selectionBlurredColor: Color?  = null): Module = basicThemeModule(name = "BasicMutableTableBehavior") {
            bindBehavior<MutableTable<Any, MutableListModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicMutableTableBehavior(
                        focusManager          = instanceOrNull(),
                        rowHeight             = rowHeight             ?: 20.0,
                        headerColor           = headerColor           ?: this.backgroundColor,
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
                            focusManager        = instanceOrNull()).apply {
                        hoverColorMapper     = this@run.hoverColorMapper
                        disabledColorMapper  = this@run.disabledColorMapper
                    }
                }
            }
        }

        public fun basicSliderBehavior(
                barFill             : Paint? = null,
                knobFill            : Paint? = null,
                rangeFill           : Paint? = null,
                grooveThicknessRatio: Float? = null): Module = basicThemeModule(name = "BasicSliderBehavior") {
            bindBehavior<Slider<Double>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicSliderBehavior<Double>(
                        barFill              = barFill              ?: defaultBackgroundColor.paint,
                        knobFill             = knobFill             ?: darkBackgroundColor.paint,
                        rangeFill            = rangeFill,
                        grooveThicknessRatio = grooveThicknessRatio ?: 0.5f,
                        focusManager         = instanceOrNull()
                    ).apply {
                        disabledPaintMapper = this@run.disabledPaintMapper
                    }
                }
            }
        }

        public fun basicRangeSliderBehavior(
            barFill             : Paint? = null,
            startKnobFill       : Paint? = null,
            endKnobFill         : Paint? = startKnobFill,
            rangeFill           : Paint? = endKnobFill,
            grooveThicknessRatio: Float? = null): Module = basicThemeModule(name = "BasicRangeSliderBehavior") {
            bindBehavior<RangeSlider<Double>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicRangeSliderBehavior<Double>(
                        barFill              = barFill              ?: defaultBackgroundColor.paint,
                        startKnobFill        = startKnobFill        ?: darkBackgroundColor.paint,
                        endKnobFill          = endKnobFill          ?: startKnobFill ?: darkBackgroundColor.paint,
                        rangeFill            = rangeFill            ?: darkBackgroundColor.paint,
                        grooveThicknessRatio = grooveThicknessRatio ?: 0.5f,
                        focusManager         = instanceOrNull()
                    ).apply {
                        disabledPaintMapper = this@run.disabledPaintMapper
                    }
                }
            }
        }

        public inline fun basicRangeSliderBehavior(
            barFill             : Paint? = null,
            knobFill            : Paint? = null,
            rangeFill           : Paint? = knobFill,
            grooveThicknessRatio: Float? = null): Module = basicRangeSliderBehavior(
            barFill              = barFill,
            startKnobFill        = knobFill,
            rangeFill            = rangeFill,
            grooveThicknessRatio = grooveThicknessRatio
        )

        public fun basicSpinnerBehavior(
                backgroundColor    : Color?  = null,
                darkBackgroundColor: Color?  = null,
                foregroundColor    : Color?  = null,
                cornerRadius       : Double? = null,
                buttonWidth        : Double? = null): Module = basicThemeModule(name = "BasicSpinnerBehavior") {
            bindBehavior<Spinner<Any, SpinnerModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicSpinnerBehavior<Any, SpinnerModel<Any>>(
                            instance(),
                            buttonWidth         = buttonWidth         ?: 20.0,
                            cornerRadius        = cornerRadius        ?: this.cornerRadius,
                            backgroundColor     = backgroundColor     ?: this.backgroundColor,
                            foregroundColor     = foregroundColor     ?: this.foregroundColor,
                            darkBackgroundColor = darkBackgroundColor ?: this.darkBackgroundColor,
                            focusManager        = instanceOrNull()
                    ).apply {
                        hoverColorMapper     = this@run.hoverColorMapper
                        disabledColorMapper  = this@run.disabledColorMapper
                    }
                }
            }
        }

        public fun basicMutableSpinnerBehavior(
                backgroundColor    : Color?  = null,
                darkBackgroundColor: Color?  = null,
                foregroundColor    : Color?  = null,
                cornerRadius       : Double? = null,
                buttonWidth        : Double? = null): Module = basicThemeModule(name = "BasicMutableSpinnerBehavior") {
            bindBehavior<MutableSpinner<Any, MutableModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicMutableSpinnerBehavior<Any, MutableModel<Any>>(
                            instance(),
                            buttonWidth         = buttonWidth         ?: 20.0,
                            cornerRadius        = cornerRadius        ?: this.cornerRadius,
                            backgroundColor     = backgroundColor     ?: this.backgroundColor,
                            foregroundColor     = foregroundColor     ?: this.foregroundColor,
                            darkBackgroundColor = darkBackgroundColor ?: this.darkBackgroundColor,
                            focusManager        = instanceOrNull()
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
                iconSpacing        : Double? = null,
                checkInset         : ((CheckBox) -> Float)? = null,
                iconSize           : ((CheckBox) -> Size)? = null
        ): Module = basicThemeModule(name = "BasicCheckBoxBehavior") {
            bindBehavior<CheckBox>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicCheckBoxBehavior(
                        instance(),
                        iconSize            = iconSize            ?: { Size(maxOf(0.0, minOf(16.0, it.height - 2.0, it.width - 2.0))) },
                        checkInset          = checkInset          ?: { 0.5f },
                        iconSpacing         = iconSpacing         ?: 8.0,
                        cornerRadius        = cornerRadius        ?: this.cornerRadius,
                        backgroundColor     = backgroundColor     ?: this.backgroundColor,
                        foregroundColor     = foregroundColor     ?: this.foregroundColor,
                        darkBackgroundColor = darkBackgroundColor ?: this.darkBackgroundColor,
                        hoverColorMapper    = this@run.hoverColorMapper,
                        disabledColorMapper = this@run.disabledColorMapper,
                        focusManager        = instanceOrNull()
                    ) as Behavior<Button>
                }
            }
        }

        public fun basicRadioButtonBehavior(
                foregroundColor    : Color?                     = null,
                backgroundColor    : Color?                     = null,
                darkBackgroundColor: Color?                     = null,
                iconSpacing        : Double?                    = null,
                innerCircleInset   : ((RadioButton) -> Double)? = null,
                iconSize           : ((RadioButton) -> Size  )? = null,
        ): Module = basicThemeModule(name = "BasicRadioButtonBehavior") {
            bindBehavior<RadioButton>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicRadioBehavior(
                        instance(),
                        iconSpacing         = iconSpacing         ?: 8.0,
                        iconSize            = iconSize            ?: { Size(maxOf(0.0, minOf(16.0, it.height - 2.0, it.width - 2.0))) },
                        backgroundColor     = backgroundColor     ?: this.backgroundColor,
                        foregroundColor     = foregroundColor     ?: this.foregroundColor,
                        innerCircleInset    = innerCircleInset    ?: { 4.0 },
                        darkBackgroundColor = darkBackgroundColor ?: this.darkBackgroundColor,
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

        public fun basicSplitPanelBehavior(): Module = basicThemeModule(name = "BasicSplitPanelBehavior") {
            bindBehavior<SplitPanel>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicSplitPanelBehavior() }
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
                startCap        : SegmentBuilder    = { _,it -> lineTo(it) },
                endCap          : SegmentBuilder    = { _,_  ->            }): Module = basicThemeModule(name = "BasicCircularProgressBarBehavior") {
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

        public inline fun basicCircularRangeSliderBehavior(
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

        public fun basicDropdownBehavior(
                backgroundColor      : Color?  = null,
                darkBackgroundColor  : Color?  = null,
                foregroundColor      : Color?  = null,
                cornerRadius         : Double? = null,
                buttonWidth          : Double? = null): Module = basicThemeModule(name = "BasicDropdownBehavior") {
            bindBehavior<Dropdown<Any, ListModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicDropdownBehavior<Any, ListModel<Any>>(
                            display               = instance(),
                            textMetrics           = instance(),
                            buttonWidth           = buttonWidth         ?: 20.0,
                            focusManager          = instanceOrNull(),
                            cornerRadius          = cornerRadius        ?: this.cornerRadius,
                            backgroundColor       = backgroundColor     ?: this.backgroundColor,
                            foregroundColor       = foregroundColor     ?: this.foregroundColor,
                            darkBackgroundColor   = darkBackgroundColor ?: this.darkBackgroundColor
                    ).apply {
                        hoverColorMapper     = this@run.hoverColorMapper
                        disabledColorMapper  = this@run.disabledColorMapper
                    }
                }
            }
        }

        public fun basicMutableDropdownBehavior(
                backgroundColor      : Color?  = null,
                darkBackgroundColor  : Color?  = null,
                foregroundColor      : Color?  = null,
                cornerRadius         : Double? = null,
                buttonWidth          : Double? = null): Module = basicThemeModule(name = "BasicMutableDropdownBehavior") {
            bindBehavior<MutableDropdown<Any, MutableListModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicMutableDropdownBehavior<Any, MutableListModel<Any>>(
                            display               = instance(),
                            textMetrics           = instance(),
                            buttonWidth           = buttonWidth         ?: 20.0,
                            focusManager          = instanceOrNull(),
                            cornerRadius          = cornerRadius        ?: this.cornerRadius,
                            backgroundColor       = backgroundColor     ?: this.backgroundColor,
                            foregroundColor       = foregroundColor     ?: this.foregroundColor,
                            darkBackgroundColor   = darkBackgroundColor ?: this.darkBackgroundColor
                    ).apply {
                        hoverColorMapper     = this@run.hoverColorMapper
                        disabledColorMapper  = this@run.disabledColorMapper
                    }
                }
            }
        }

        public fun basicMonthPanelBehavior(backgroundColor: Color? = null): Module = basicThemeModule(name = "BasicMonthPanelBehavior") {
            bindBehavior<MonthPanel>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    object: Behavior<MonthPanel> {
                        override fun install(view: MonthPanel) {
                            super.install(view)
                            view.rerender()
                        }

                        override fun render(view: MonthPanel, canvas: Canvas) {
                            canvas.rect(view.bounds.atOrigin, fill = (backgroundColor ?: this@run.backgroundColor).paint)
                        }
                    }
                }
            }
        }

        public fun basicDaysOfTheWeekPanelBehavior(backgroundColor: Color?  = null): Module = basicThemeModule(name = "BasicDaysOfTheWeekPanelBehavior") {
            bindBehavior<DaysOfTheWeekPanel>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    object: Behavior<DaysOfTheWeekPanel> {
                        override fun install(view: DaysOfTheWeekPanel) {
                            super.install(view)
                            view.rerender()
                        }

                        override fun render(view: DaysOfTheWeekPanel, canvas: Canvas) {
                            canvas.rect(view.bounds.atOrigin, fill = (backgroundColor ?: this@run.backgroundColor).paint)
                        }
                    }
                }
            }
        }

        public val basicThemeBehaviors: kotlin.collections.List<Module> = listOf(
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
                basicSpinnerBehavior(),
                basicCheckBoxBehavior(),
                basicDropdownBehavior(),
                basicSplitPanelBehavior(),
                basicRadioButtonBehavior(),
                basicMutableListBehavior(),
                basicProgressBarBehavior(foregroundRadius = null),
                basicMutableTreeBehavior(),
                basicTreeColumnsBehavior(),
                basicTabbedPanelBehavior(),
                basicMutableTableBehavior(),
                basicMutableSpinnerBehavior(),
                basicMutableDropdownBehavior(),
                basicMonthPanelBehavior(),
                basicDaysOfTheWeekPanelBehavior(),
        )
    }
}

public class DarkBasicTheme(configProvider: ConfigProvider, behaviors: Iterable<Modules.BehaviorResolver>): BasicTheme(configProvider, behaviors) {
    public class DarkBasicThemeConfig: BasicThemeConfig {
        override val borderColor           : Color = super.borderColor.inverted
        override val foregroundColor       : Color = super.foregroundColor.inverted
        override val backgroundColor       : Color = super.backgroundColor.inverted
        override val darkBackgroundColor   : Color = super.darkBackgroundColor.inverted
        override val lightBackgroundColor  : Color = Color(0x282928u)
        override val defaultBackgroundColor: Color = super.defaultBackgroundColor.inverted
        override val hoverColorMapper      : ColorMapper = { it.lighter(0.3f) }
        override val disabledColorMapper   : ColorMapper = { it.darker()      }
        override val disabledPaintMapper   : PaintMapper = {
            when (it) {
                is ColorPaint          -> it.color.darker().paint
                is LinearGradientPaint -> LinearGradientPaint(it.colors.map { GradientPaint.Stop(it.color.darker(), it.offset) }, start = it.start, end = it.end)
                is RadialGradientPaint -> RadialGradientPaint(it.colors.map { GradientPaint.Stop(it.color.darker(), it.offset) }, start = it.start, end = it.end)
                is ImagePaint          -> ImagePaint(image = it.image, size = it.size, opacity = it.opacity * 0.5f)
                else                   -> it
            }
        }
    }

    override val config: BasicThemeConfig = DarkBasicThemeConfig()

    public companion object {
        public val DarkBasicTheme: Module = basicThemeModule(name = "DarkBasicTheme") {
            bind<DarkBasicTheme>() with singleton { DarkBasicTheme(instance(), Instance(erasedSet())) }
        }
    }
}