package io.nacular.doodle.controls.buttons

public class ButtonGroup(public var allowDeselectAll: Boolean = false, vararg buttons: Button) {
    public val size: Int get() = buttons.size

    private val buttons = mutableSetOf<Button>()
    private var selectedModel: ButtonModel? = null

    init {
        buttons.forEach { this += it }
    }

    public operator fun plusAssign(button: Button) {
        buttons += button

        button.model.buttonGroup = this

        if (button.model.selected) {
            when (selectedModel) {
                null -> selectedModel = button.model
                else -> button.model.selected = false
            }
        }
    }

    public operator fun minusAssign(button: Button) {
        if (buttons.remove(button)) {
            button.model.buttonGroup = null

            if (button.model === selectedModel) {
                selectedModel = null
            }
        }
    }

    public fun clear() {
        buttons.forEach {
            it.model.buttonGroup = null
        }

        buttons.clear()

        selectedModel = null
    }

    internal fun setSelected(buttonModel: ButtonModel, selected: Boolean) {
        if (buttonModel.buttonGroup === this) {
            if (selectedModel == null) {
                if (selected) {
                    selectedModel = buttonModel
                }
            } else if (selectedModel != buttonModel && selected) {
                val temp = selectedModel

                selectedModel = buttonModel

                temp?.selected = false
            }

            if (!allowDeselectAll) {
                selectedModel?.selected = true
            }
        }
    }
}
