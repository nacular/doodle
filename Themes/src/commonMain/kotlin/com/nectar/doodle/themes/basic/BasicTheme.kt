package com.nectar.doodle.themes.basic

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
import com.nectar.doodle.controls.theme.LabelBehavior
import com.nectar.doodle.controls.tree.MutableTree
import com.nectar.doodle.controls.tree.Tree
import com.nectar.doodle.controls.tree.TreeModel
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.black
import com.nectar.doodle.drawing.grayScale
import com.nectar.doodle.theme.Behavior
import com.nectar.doodle.themes.Modules
import com.nectar.doodle.themes.Modules.Companion.bindBehavior
import com.nectar.doodle.themes.adhoc.AdhocTheme
import com.nectar.doodle.themes.basic.list.BasicListBehavior
import com.nectar.doodle.themes.basic.list.BasicMutableListBehavior
import com.nectar.doodle.themes.basic.table.BasicMutableTableBehavior
import com.nectar.doodle.themes.basic.table.BasicTableBehavior
import com.nectar.doodle.themes.basic.tree.BasicMutableTreeBehavior
import com.nectar.doodle.themes.basic.tree.BasicTreeBehavior
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
typealias ListModel<T>        = com.nectar.doodle.controls.ListModel<T>
typealias SpinnerModel<T>     = com.nectar.doodle.controls.spinner.Model<T>
typealias MutableTreeModel<T> = com.nectar.doodle.controls.tree.MutableTreeModel<T>

@Suppress("UNCHECKED_CAST")
open class BasicTheme(private val configProvider: ConfigProvider, behaviors: Iterable<Modules.BehaviorResolver>): AdhocTheme(behaviors.filter { it.theme == BasicTheme::class }) {
    override fun install(display: Display, all: Sequence<View>) {
        configProvider.config = config

        super.install(display, all)
    }

    open val config = object: BasicThemeConfig {}

    override fun toString() = this::class.simpleName ?: ""

    interface BasicThemeConfig {
        val borderColor            get() = Color(0x888888u)
        val oddRowColor            get() = foregroundColor.inverted
        val eventRowColor          get() = lightBackgroundColor
        val selectionColor         get() = Color(0x0063e1u)
        val foregroundColor        get() = black
        val backgroundColor        get() = Color(0xccccccu)
        val darkBackgroundColor    get() = Color(0xaaaaaau)
        val lightBackgroundColor   get() = Color(0xf3f4f5u)
        val defaultBackgroundColor get() = backgroundColor
    }

    interface ConfigProvider {
        var config: BasicThemeConfig
    }

    private class ConfigProviderImpl: ConfigProvider {
        override var config = object: BasicThemeConfig {}
    }

    companion object {
        internal fun basicModule(name: String, init: Kodein.Builder.() -> Unit) = Module(name = name) {
            importOnce(Companion.config, allowOverride = true)

            init()
        }

        val basicTheme = basicModule(name = "BasicTheme") {
            bind<BasicTheme>() with singleton { BasicTheme(instance(), Instance(erasedSet())) }
        }

        private val config = Module(name = "BasicThemeConfig") {
            bind<ConfigProvider>  () with singleton { ConfigProviderImpl()              }
            bind<BasicThemeConfig>() with provider  { instance<ConfigProvider>().config }
        }

        val basicListBehavior = basicModule(name = "BasicListBehavior") {
            bindBehavior<List<Any, ListModel<Any>>>(BasicTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicListBehavior(instance(), instance(), eventRowColor, oddRowColor, selectionColor, selectionColor.grayScale().lighter()) }
            }
        }

        val basicTreeBehavior = basicModule(name = "BasicTreeBehavior") {
            bindBehavior<Tree<Any,TreeModel<Any>>>(BasicTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicTreeBehavior(instance(), eventRowColor, oddRowColor, selectionColor, selectionColor.grayScale().lighter(), foregroundColor, instanceOrNull()) }
            }
        }

        val basicLabelBehavior = basicModule(name = "BasicLabelBehavior") {
            bindBehavior<Label>(BasicTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { LabelBehavior(foregroundColor) }
            }
        }

        val basicTableBehavior = basicModule(name = "BasicTableBehavior") {
            bindBehavior<Table<Any, ListModel<Any>>>(BasicTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicTableBehavior(instanceOrNull(), 20.0, backgroundColor, eventRowColor, oddRowColor, selectionColor, selectionColor.grayScale().lighter()) }
            }
        }

        val basicButtonBehavior = basicModule(name = "BasicButtonBehavior") {
            bindBehavior<Button>(BasicTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicButtonBehavior(instance(), backgroundColor = backgroundColor, borderColor = borderColor, darkBackgroundColor = darkBackgroundColor, foregroundColor = foregroundColor) }
            }
        }

        val basicSliderBehavior = basicModule(name = "BasicSliderBehavior") {
            bindBehavior<Slider>(BasicTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicSliderBehavior(defaultBackgroundColor = defaultBackgroundColor, darkBackgroundColor = darkBackgroundColor) }
            }
        }

        val basicSpinnerBehavior = basicModule(name = "BasicSpinnerBehavior") {
            bindBehavior<Spinner<Any, SpinnerModel<Any>>>(BasicTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicSpinnerBehavior(borderColor = borderColor, backgroundColor = backgroundColor, labelFactory = instance()) }
            }
        }

        val basicCheckBoxBehavior = basicModule(name = "BasicCheckBoxBehavior") {
            bindBehavior<CheckBox>(BasicTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicCheckBoxBehavior(instance()) as Behavior<Button> }
            }
        }

        val basicSplitPanelBehavior = basicModule(name = "BasicSplitPanelBehavior") {
            bindBehavior<SplitPanel>(BasicTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicSplitPanelBehavior() }
            }
        }

        val basicRadioButtonBehavior = basicModule(name = "BasicRadioButtonBehavior") {
            bindBehavior<RadioButton>(BasicTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicRadioBehavior(instance()) as Behavior<Button> }
            }
        }

        val basicMutableListBehavior = basicModule(name = "BasicMutableListBehavior") {
            bindBehavior<MutableList<Any, MutableListModel<Any>>>(BasicTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicMutableListBehavior(instance(), instance(), eventRowColor, oddRowColor, selectionColor, selectionColor.grayScale().lighter()) }
            }
        }

        val basicProgressBarBehavior = basicModule(name = "BasicProgressBarBehavior") {
            bindBehavior<ProgressBar>(BasicTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicProgressBarBehavior(defaultBackgroundColor = defaultBackgroundColor, darkBackgroundColor = darkBackgroundColor) as Behavior<ProgressIndicator> }
            }
        }

        val basicMutableTreeBehavior = basicModule(name = "BasicMutableTreeBehavior") {
            bindBehavior<MutableTree<Any, MutableTreeModel<Any>>>(BasicTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicMutableTreeBehavior(instance(), eventRowColor, oddRowColor, selectionColor, selectionColor.grayScale().lighter(), foregroundColor, instanceOrNull()) }
            }
        }

        val basicMutableTableBehavior = basicModule(name = "BasicMutableTableBehavior") {
            bindBehavior<MutableTable<Any, MutableListModel<Any>>>(BasicTheme::class) {
                it.behavior = instance<BasicThemeConfig>().run { BasicMutableTableBehavior(instanceOrNull(), 20.0, backgroundColor, eventRowColor, oddRowColor, selectionColor, selectionColor.grayScale().lighter()) }
            }
        }

        val basicThemeBehaviors = Module(name = "BasicThemeBehaviors") {
            importAll(listOf(
                    basicListBehavior,
                    basicTreeBehavior,
                    basicLabelBehavior,
                    basicTableBehavior,
                    basicButtonBehavior,
                    basicSliderBehavior,
                    basicSpinnerBehavior,
                    basicCheckBoxBehavior,
                    basicSplitPanelBehavior,
                    basicRadioButtonBehavior,
                    basicMutableListBehavior,
                    basicProgressBarBehavior,
                    basicMutableTreeBehavior,
                    basicMutableTableBehavior),
                    allowOverride = true)
        }
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
    }

    override val config = DarkBasicThemeConfig()

    companion object {
        val darkBasicTheme = basicModule(name = "DarkBasicTheme") {
            bind<DarkBasicTheme>() with singleton { DarkBasicTheme(instance(), Instance(erasedSet())) }
        }
    }
}