package io.nacular.doodle.controls.spinner

public class LongSpinnerModel(private val progression: LongProgression): CommonSpinnerModel<Long>() {

    override val hasNext    : Boolean get() = value + progression.step <= progression.last
    override val hasPrevious: Boolean get() = value - progression.step >= progression.first

    override fun next    () { if (hasNext    ) { value += progression.step } }
    override fun previous() { if (hasPrevious) { value -= progression.step } }

    override var value: Long = progression.first
        private set(new) {
            if (new == field) { return }

            field = new

            changed_()
        }
}