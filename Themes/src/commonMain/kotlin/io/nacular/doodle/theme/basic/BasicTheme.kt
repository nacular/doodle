package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.ProgressBar
import io.nacular.doodle.controls.ProgressIndicator
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.buttons.RadioButton
import io.nacular.doodle.controls.buttons.Switch
import io.nacular.doodle.controls.list.List
import io.nacular.doodle.controls.list.MutableList
import io.nacular.doodle.controls.panels.SplitPanel
import io.nacular.doodle.controls.panels.TabbedPanel
import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.controls.spinner.Spinner
import io.nacular.doodle.controls.table.MutableTable
import io.nacular.doodle.controls.table.Table
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.theme.LabelBehavior
import io.nacular.doodle.controls.tree.MutableTree
import io.nacular.doodle.controls.tree.Tree
import io.nacular.doodle.controls.tree.TreeModel
import io.nacular.doodle.controls.treecolumns.TreeColumns
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.ColorFill
import io.nacular.doodle.drawing.Fill
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.drawing.grayScale
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.theme.Modules
import io.nacular.doodle.theme.Modules.Companion.ThemeModule
import io.nacular.doodle.theme.Modules.Companion.bindBehavior
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.basic.list.BasicListBehavior
import io.nacular.doodle.theme.basic.list.BasicMutableListBehavior
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
import org.kodein.di.DKodein
import org.kodein.di.Kodein
import org.kodein.di.Kodein.Module
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.instanceOrNull
import org.kodein.di.erased.provider
import org.kodein.di.erased.singleton
import org.kodein.di.erasedSet

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
private typealias ListModel<T>        = io.nacular.doodle.controls.ListModel<T>
private typealias SpinnerModel<T>     = io.nacular.doodle.controls.spinner.Model<T>
private typealias MutableTreeModel<T> = io.nacular.doodle.controls.tree.MutableTreeModel<T>
private typealias BTheme              = BasicTheme

private typealias TabContainerFactory<T> = DKodein.(TabbedPanel<T>, TabProducer<T>) -> TabContainer<T>


@Suppress("UNCHECKED_CAST")
open class BasicTheme(private val configProvider: ConfigProvider, behaviors: Iterable<Modules.BehaviorResolver>): DynamicTheme(behaviors.filter { it.theme == BTheme::class }) {
    override fun install(display: Display, all: Sequence<View>) {
        configProvider.config = config

        super.install(display, all)
    }

    open val config = object: BasicThemeConfig {}

    override fun toString() = this::class.simpleName ?: ""

    interface BasicThemeConfig {
        val borderColor            get() = Color(0x888888u)
        val oddRowColor            get() = foregroundColor.inverted
        val evenRowColor           get() = lightBackgroundColor
        val selectionColor         get() = Color(0x0063e1u)
        val foregroundColor        get() = Black
        val backgroundColor        get() = Color(0xccccccu)
        val darkBackgroundColor    get() = Color(0xaaaaaau)
        val lightBackgroundColor   get() = Color(0xf3f4f5u)
        val defaultBackgroundColor get() = backgroundColor
        val cornerRadius           get() = 4.0
        val hoverColorMapper       get() = { color: Color -> color.darker(0.1f) }
        val disabledColorMapper    get() = { color: Color -> color.lighter()    }
    }

    interface ConfigProvider {
        var config: BasicThemeConfig
    }

    private class ConfigProviderImpl: ConfigProvider {
        override var config = object: BasicThemeConfig {}
    }

    companion object {
        fun basicThemeModule(name: String, init: Kodein.Builder.() -> Unit) = Module(name = name) {
            importOnce(Config, allowOverride = true)

            init()
        }

        val BasicTheme = basicThemeModule(name = "BasicTheme") {
            importOnce(ThemeModule, allowOverride = true)

            bind<BasicTheme>() with singleton { BasicTheme(instance(), Instance(erasedSet())) }
        }

        private val Config = Module(name = "BasicThemeConfig") {
            bind<ConfigProvider>  () with singleton { ConfigProviderImpl()              }
            bind<BasicThemeConfig>() with provider  { instance<ConfigProvider>().config }
        }

        fun basicListBehavior(
                rowHeight            : Double? = null,
                evenRowColor         : Color?  = null,
                oddRowColor          : Color?  = null,
                selectionColor       : Color?  = null,
                selectionBlurredColor: Color?  = null) = basicThemeModule(name = "BasicListBehavior") {
            bindBehavior<List<Any, ListModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicListBehavior(
                            focusManager          = instanceOrNull(),
                            textMetrics           = instance(),
                            evenRowColor          = evenRowColor          ?: this.evenRowColor,
                            oddRowColor           = oddRowColor           ?: this.oddRowColor,
                            selectionColor        = selectionColor        ?: this.selectionColor,
                            selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter(),
                            rowHeight             = rowHeight             ?: 20.0
                    )
                }
            }
        }

        fun basicMutableListBehavior(
                rowHeight            : Double? = null,
                evenRowColor         : Color?  = null,
                oddRowColor          : Color?  = null,
                selectionColor       : Color?  = null,
                selectionBlurredColor: Color?  = null) = basicThemeModule(name = "BasicMutableListBehavior") {
            bindBehavior<MutableList<Any, MutableListModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicMutableListBehavior(
                        focusManager          = instanceOrNull(),
                        textMetrics           = instance(),
                        evenRowColor          = evenRowColor          ?: this.evenRowColor,
                        oddRowColor           = oddRowColor           ?: this.oddRowColor,
                        selectionColor        = selectionColor        ?: this.selectionColor,
                        selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter(),
                        rowHeight             = rowHeight             ?: 20.0
                ) }
            }
        }

        fun basicTreeBehavior(
                rowHeight            : Double?              = null,
                evenRowColor         : Color?               = null,
                oddRowColor          : Color?               = null,
                selectionColor       : Color?               = null,
                selectionBlurredColor: Color?               = null,
                iconFactory          : (() -> TreeRowIcon)? = null) = basicThemeModule(name = "BasicTreeBehavior") {
            bindBehavior<Tree<Any,TreeModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicTreeBehavior(
                            focusManager          = instanceOrNull(),
                            textMetrics           = instance(),
                            rowHeight             = rowHeight             ?: 20.0,
                            evenRowColor          = evenRowColor          ?: this.evenRowColor,
                            oddRowColor           = oddRowColor           ?: this.oddRowColor,
                            selectionColor        = selectionColor        ?: this.selectionColor,
                            selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter(),
                            iconFactory           = iconFactory           ?: { SimpleTreeRowIcon(foregroundColor, foregroundColor.inverted) }
                    )
                }
            }
        }

        fun basicMutableTreeBehavior(
                rowHeight            : Double?              = null,
                evenRowColor         : Color?               = null,
                oddRowColor          : Color?               = null,
                selectionColor       : Color?               = null,
                selectionBlurredColor: Color?               = null,
                iconFactory          : (() -> TreeRowIcon)? = null) = basicThemeModule(name = "BasicMutableTreeBehavior") {
            bindBehavior<MutableTree<Any, MutableTreeModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicMutableTreeBehavior(
                            focusManager          = instanceOrNull(),
                            textMetrics           = instance(),
                            rowHeight             = rowHeight             ?: 20.0,
                            evenRowColor          = evenRowColor          ?: this.evenRowColor,
                            oddRowColor           = oddRowColor           ?: this.oddRowColor,
                            selectionColor        = selectionColor        ?: this.selectionColor,
                            selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter(),
                            iconFactory           = iconFactory           ?: { SimpleTreeRowIcon(foregroundColor, foregroundColor.inverted) }
                ) }
            }
        }

        fun basicLabelBehavior(foregroundColor: Color? = null) = basicThemeModule(name = "BasicLabelBehavior") {
            bindBehavior<Label>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { LabelBehavior(foregroundColor ?: this.foregroundColor) }
            }
        }

        fun basicTableBehavior(
                rowHeight            : Double? = null,
                headerColor          : Color?  = null,
                evenRowColor         : Color?  = null,
                oddRowColor          : Color?  = null,
                selectionColor       : Color?  = null,
                selectionBlurredColor: Color?  = null) = basicThemeModule(name = "BasicTableBehavior") {
            bindBehavior<Table<Any, ListModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicTableBehavior(
                        focusManager          = instanceOrNull(),
                        rowHeight             = rowHeight             ?: 20.0,
                        headerColor           = headerColor           ?: this.backgroundColor,
                        evenRowColor          = evenRowColor          ?: this.evenRowColor,
                        oddRowColor           = oddRowColor           ?: this.oddRowColor,
                        selectionColor        = selectionColor        ?: this.selectionColor,
                        selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter()
                ) }
            }
        }

        fun basicMutableTableBehavior(
                rowHeight            : Double? = null,
                headerColor          : Color?  = null,
                evenRowColor         : Color?  = null,
                oddRowColor          : Color?  = null,
                selectionColor       : Color?  = null,
                selectionBlurredColor: Color?  = null) = basicThemeModule(name = "BasicMutableTableBehavior") {
            bindBehavior<MutableTable<Any, MutableListModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicMutableTableBehavior(
                        focusManager          = instanceOrNull(),
                        rowHeight             = rowHeight             ?: 20.0,
                        headerColor           = headerColor           ?: this.backgroundColor,
                        evenRowColor          = evenRowColor          ?: this.evenRowColor,
                        oddRowColor           = oddRowColor           ?: this.oddRowColor,
                        selectionColor        = selectionColor        ?: this.selectionColor,
                        selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter()
                ) }
            }
        }

        fun basicTreeColumnsBehavior(
                rowHeight            : Double? = null,
                columnSeparatorColor : Color?  = null,
                selectionColor       : Color?  = null,
                selectionBlurredColor: Color?  = null,
                backgroundColor      : Color?  = null,
                iconFactory          : (() -> TreeColumnRowIcon)? = null) = basicThemeModule(name = "BasicTreeColumnsBehavior") {
            bindBehavior<TreeColumns<Any, *>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicTreeColumnsBehavior (
                        focusManager          = instanceOrNull(),
                        textMetrics           = instance(),
                        rowHeight             = rowHeight             ?: 20.0,
                        columnSeparatorColor  = columnSeparatorColor  ?: this.backgroundColor,
                        backgroundColor       = backgroundColor       ?: this.oddRowColor,
                        selectionColor        = selectionColor        ?: this.selectionColor,
                        selectionBlurredColor = selectionBlurredColor ?: this.selectionColor.grayScale().lighter(),
                        iconFactory           = iconFactory           ?: { SimpleTreeColumnRowIcon(foregroundColor, foregroundColor.inverted) }
                ) }
            }
        }

        fun basicButtonBehavior(
                backgroundColor    : Color?  = null,
                darkBackgroundColor: Color?  = null,
                foregroundColor    : Color?  = null,
                borderColor        : Color?  = null,
                borderWidth        : Double? = null,
                cornerRadius       : Double? = null,
                insets             : Double? = null) = basicThemeModule(name = "BasicButtonBehavior") {
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
                            insets              = insets              ?: 8.0).apply {
                        hoverColorMapper     = this@run.hoverColorMapper
                        disabledColorMapper  = this@run.disabledColorMapper
                    }
                }
            }
        }

        fun basicSliderBehavior(barColor: Color? = null, knobColor: Color? = null, grooveThicknessRatio: Float? = null) = basicThemeModule(name = "BasicSliderBehavior") {
            bindBehavior<Slider>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicSliderBehavior(
                            barColor             ?: defaultBackgroundColor,
                            knobColor            ?: darkBackgroundColor,
                            grooveThicknessRatio ?: 0.5f,
                            instanceOrNull()
                    )
                }
            }
        }

        fun basicSpinnerBehavior(
                backgroundColor    : Color?  = null,
                darkBackgroundColor: Color?  = null,
                foregroundColor    : Color?  = null,
                cornerRadius       : Double? = null) = basicThemeModule(name = "BasicSpinnerBehavior") {
            bindBehavior<Spinner<Any, SpinnerModel<Any>>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicSpinnerBehavior(
                            instance(),
                            cornerRadius        = cornerRadius        ?: this.cornerRadius,
                            backgroundColor     = backgroundColor     ?: this.backgroundColor,
                            foregroundColor     = foregroundColor     ?: this.foregroundColor,
                            darkBackgroundColor = darkBackgroundColor ?: this.darkBackgroundColor
                    ).apply {
                        hoverColorMapper     = this@run.hoverColorMapper
                        disabledColorMapper  = this@run.disabledColorMapper
                    }
                }
            }
        }

        fun basicCheckBoxBehavior(
                foregroundColor    : Color?  = null,
                backgroundColor    : Color?  = null,
                darkBackgroundColor: Color?  = null,
                cornerRadius       : Double? = null,
                iconSpacing        : Double? = null,
                iconInset          : Float?  = null,
                checkInset         : Float?  = null
        ) = basicThemeModule(name = "BasicCheckBoxBehavior") {
            bindBehavior<CheckBox>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicCheckBoxBehavior(
                        instance(),
                        iconInset           = iconInset           ?: 0.0f,
                        checkInset          = checkInset          ?: 0.5f,
                        iconSpacing         = iconSpacing         ?: 8.0,
                        cornerRadius        = cornerRadius        ?: this.cornerRadius,
                        backgroundColor     = backgroundColor     ?: this.backgroundColor,
                        foregroundColor     = foregroundColor     ?: this.foregroundColor,
                        darkBackgroundColor = darkBackgroundColor ?: this.darkBackgroundColor,
                        hoverColorMapper    = this@run.hoverColorMapper,
                        disabledColorMapper = this@run.disabledColorMapper) as Behavior<Button>
                }
            }
        }

        fun basicRadioButtonBehavior(
                foregroundColor    : Color?  = null,
                backgroundColor    : Color?  = null,
                darkBackgroundColor: Color?  = null,
                iconSpacing        : Double? = null,
                innerCircleInset   : Double? = null
        ) = basicThemeModule(name = "BasicRadioButtonBehavior") {
            bindBehavior<RadioButton>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicRadioBehavior(
                        instance(),
                        iconSpacing         = iconSpacing         ?: 8.0,
                        backgroundColor     = backgroundColor     ?: this.backgroundColor,
                        foregroundColor     = foregroundColor     ?: this.foregroundColor,
                        innerCircleInset    = innerCircleInset    ?: 4.0,
                        darkBackgroundColor = darkBackgroundColor ?: this.darkBackgroundColor
                ) as Behavior<Button> }
            }
        }

        fun basicSwitchBehavior(
                onBackground : Color? = null,
                onForeground : Color? = null,
                offBackground: Color? = null,
                offForeground: Color? = null
        ) = basicThemeModule(name = "BasicSwitchBehavior") {
            bindBehavior<Switch>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicSwitchBehavior(
                            onBackground ?: Blue,
                            onForeground ?: White,
                            offBackground?: backgroundColor,
                            offForeground?: onForeground ?: White).apply {
                        hoverColorMapper    = this@run.hoverColorMapper
                        disabledColorMapper = this@run.disabledColorMapper
                    } as Behavior<Button>
                }
            }
        }

        fun basicSplitPanelBehavior() = basicThemeModule(name = "BasicSplitPanelBehavior") {
            bindBehavior<SplitPanel>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicSplitPanelBehavior() }
            }
        }

        fun basicProgressBarBehavior(
                background  : Fill?  = null,
                foreground  : Fill?  = null,
                outlineColor: Color? = null,
                cornerRadius: Double = 2.0): Module = basicThemeModule(name = "BasicProgressBarBehavior") {
            bindBehavior<ProgressBar>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicProgressBarBehavior(
                            background ?: ColorFill(defaultBackgroundColor),
                            foreground ?: ColorFill(darkBackgroundColor   ),
                            outlineColor,
                            cornerRadius) as Behavior<ProgressIndicator> }
            }
        }

        fun basicTabbedPanelBehavior(
                tabProducer    : TabProducer<Any>?         = null,
                backgroundColor: Color?                    = null,
                tabContainer   : TabContainerFactory<Any>? = null) = basicThemeModule(name = "BasicTabbedPanelBehavior") {
            bindBehavior<TabbedPanel<Any>>(BTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run {
                    BasicTabbedPanelBehavior(
                            tabProducer     ?: BasicTabProducer(
                                    tabColor            = backgroundColor ?: this.backgroundColor,
                                    hoverColorMapper    = this@run.hoverColorMapper,
                                    selectedColorMapper = { foregroundColor.inverted }
                            ),
                            backgroundColor ?: this.backgroundColor,
                            tabContainer?.let { { panel: TabbedPanel<Any>, tabProducer: TabProducer<Any> ->
                                it(this@bindBehavior, panel, tabProducer)
                            } } ?: { panel, tabProducer -> SimpleTabContainer(panel, tabProducer) })
                }
            }
        }

        val basicThemeBehaviors = listOf(
                basicListBehavior(),
                basicTreeBehavior(),
                basicLabelBehavior(),
                basicTableBehavior(),
                basicButtonBehavior(),
                basicSwitchBehavior(),
                basicSliderBehavior(),
                basicSpinnerBehavior(),
                basicCheckBoxBehavior(),
                basicSplitPanelBehavior(),
                basicRadioButtonBehavior(),
                basicMutableListBehavior(),
                basicProgressBarBehavior(),
                basicMutableTreeBehavior(),
                basicTreeColumnsBehavior(),
                basicTabbedPanelBehavior(),
                basicMutableTableBehavior()
        )
    }
}

class DarkBasicTheme(configProvider: ConfigProvider, behaviors: Iterable<Modules.BehaviorResolver>): BasicTheme(configProvider, behaviors) {
    class DarkBasicThemeConfig: BasicThemeConfig {
        override val borderColor            = super.borderColor.inverted
        override val foregroundColor        = super.foregroundColor.inverted
        override val backgroundColor        = super.backgroundColor.inverted
        override val darkBackgroundColor    = super.darkBackgroundColor.inverted
        override val lightBackgroundColor   = Color(0x282928u)
        override val defaultBackgroundColor = super.defaultBackgroundColor.inverted
        override val hoverColorMapper       = { color: Color -> color.lighter(0.3f) }
        override val disabledColorMapper    = { color: Color -> color.darker()      }
    }

    override val config = DarkBasicThemeConfig()

    companion object {
        val DarkBasicTheme = basicThemeModule(name = "DarkBasicTheme") {
            bind<DarkBasicTheme>() with singleton { DarkBasicTheme(instance(), Instance(erasedSet())) }
        }
    }
}