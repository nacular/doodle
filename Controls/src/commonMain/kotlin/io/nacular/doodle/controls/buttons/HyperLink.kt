package io.nacular.doodle.controls.buttons

import io.nacular.doodle.core.Icon

/**
 * Created by Nicholas Eddy on 12/7/19.
 */
class HyperLink(
        val url  : String,
            text : String        = url,
            icon : Icon<Button>? = null,
            model: ButtonModel   = ButtonModelImpl()): PushButton(text, icon, model) {

    var visited = false

    init {
        model.fired += {
            visited = true
        }
    }
}