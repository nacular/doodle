package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.core.Behavior

/**
 * Allows full control over how [HyperLink]s are styled while still retaining some native behaviors. The given behavior is delegated
 * to for all visual styling, but things like link traversal will be handled natively.
 */
public interface NativeHyperLinkStyler {
    /**
     * Wraps [behavior] with other native behavior for hyper links.
     *
     * @param hyperLink to apply [behavior] to
     * @param behavior to be "wrapped"
     * @return a new Behavior for the link
     */
    public operator fun invoke(hyperLink: HyperLink, behavior: Behavior<HyperLink>): Behavior<HyperLink>
}
