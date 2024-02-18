package io.nacular.doodle.theme

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.BreadthFirstTreeIterator
import io.nacular.doodle.utils.Node
import io.nacular.doodle.utils.ObservableSet
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.observable

/**
 * Themes are able to visually style [View]s within the [Display]. Installing one will trigger an update and provide the full set of [View]s
 * to the [Theme.install] method, allowing it to update any subset of [View]s it chooses.
 */
public interface Theme {
    /**
     * Called whenever a Theme is set as [ThemeManager.selected]. This allows the theme to update any of the [View]s and [Display]s.
     *
     * @param scene to apply the Theme to
     */
    public fun install(scene: Scene)

    /**
     * Called whenever a new View is added to a [Scene] this Theme has already been installed to.
     *
     * @param view being added to the scene
     */
    public fun install(view: View)

    // FIXME: Add uninstall once there's a clean way to support that given ad hoc behavior registration
}

/**
 * This manager keeps track of available [Theme]s and manages the application of new ones via [ThemeManager.selected].
 */
public interface ThemeManager {
    /** Convenient set of [Theme]s that an application can manage */
    public val themes: ObservableSet<Theme>

    /**
     * The currently selected [Theme]. Setting this will cause the new Theme to update the [Display] and [View]s therein.
     * A theme that is set as selected is also added to the [themes] set.
     */
    public var selected: Theme?

    /**
     * Notifies of changes to [selected]
     */
    public val selectionChanged: PropertyObservers<ThemeManager, Theme?>
}

/** @suppress */
@Internal
public abstract class InternalThemeManager internal constructor(): ThemeManager {
    internal abstract fun update(view: View)
}

/** @suppress */
@Internal
public interface Scene {
    public fun forEachView   (block: (View   ) -> Unit)
    public fun forEachDisplay(block: (Display) -> Unit)
}

/** @suppress */
@Internal
public class ThemeManagerImpl(private val scene: Scene): InternalThemeManager() {
    override val themes: ObservableSet<Theme> by lazy { ObservableSet() }

    override var selected: Theme? by observable(null) { old,new ->
        new?.apply {
            themes += this
            install(scene)
        }

        (selectionChanged as PropertyObserversImpl).forEach { it(this, old, new) }
    }

    override val selectionChanged: PropertyObservers<ThemeManager, Theme?> by lazy { PropertyObserversImpl<ThemeManager, Theme?>(this) }

    override fun update(view: View) {
        if (view.acceptsThemes) {
            selected?.install(view)
        }
    }
}

/** @suppress */
@Internal
public class SingleDisplayScene(private val display: InternalDisplay): Scene {

    private class DummyRoot(children: List<View>): Node<View> {
        override val value    = object: View() {}
        override val children = children.asSequence().map { NodeAdapter(it) }
    }

    private class NodeAdapter(override val value: View): Node<View> {
        override val children get() = value.children_.asSequence().map { NodeAdapter(it) }
    }

    private val allViews: Sequence<View> get() = Sequence {
        BreadthFirstTreeIterator(DummyRoot(display.popups + display.children))
    }.drop(1).filter { it.acceptsThemes }

    override fun forEachView(block: (View) -> Unit) {
        allViews.forEach(block)
    }

    override fun forEachDisplay(block: (Display) -> Unit) {
        block(this.display)
    }
}