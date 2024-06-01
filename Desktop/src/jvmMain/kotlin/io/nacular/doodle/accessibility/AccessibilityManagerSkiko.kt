package io.nacular.doodle.accessibility

import io.nacular.doodle.core.View
import io.nacular.doodle.core.impl.DisplaySkiko
import javax.accessibility.AccessibleContext

/**
 * Created by Nicholas Eddy on 5/31/24.
 */
internal abstract class AccessibilityManagerSkiko: AccessibilityManager() {
    override fun syncLabel        (view : View             ) {}
    override fun syncEnabled      (view : View             ) {}
    override fun syncVisibility   (view : View             ) {}
    override fun syncDescription  (view : View             ) {}
    override fun syncNextReadOrder(view : View             ) {}
    override fun roleAdopted      (view : View             ) {}
    override fun roleUpdated      (view : View             ) {}
    override fun roleAbandoned    (view : View             ) {}
    override fun addOwnership     (owner: View, owned: View) {}
    override fun removeOwnership  (owner: View, owned: View) {}

    abstract fun accessibilityContext(display: DisplaySkiko): AccessibleContext
}