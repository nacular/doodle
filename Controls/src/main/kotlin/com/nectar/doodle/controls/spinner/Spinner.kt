package com.nectar.doodle.controls.spinner

import com.nectar.doodle.controls.ChangeObservers
import com.nectar.doodle.controls.ChangeObserversImpl
import com.nectar.doodle.controls.theme.SpinnerUI
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas


interface Model<T> {
    fun next    ()
    fun previous()

    val value      : T
    val hasNext    : Boolean
    val hasPrevious: Boolean
    val onChanged  : ChangeObservers<Model<T>>
}

abstract class AbstractModel<T>: Model<T> {
    @Suppress("PrivatePropertyName")
    protected val onChanged_ = ChangeObserversImpl<Model<T>>()

    override val onChanged: ChangeObservers<Model<T>> = onChanged_
}

class IntModel(private val progression: IntProgression): AbstractModel<Int>() {

    override val hasNext     get() = value + progression.step <= progression.last
    override val hasPrevious get() = value - progression.step >= progression.first

    override fun next    () { if (hasNext    ) { value += progression.step } }
    override fun previous() { if (hasPrevious) { value -= progression.step } }

    override var value = progression.first
        private set(new) {
            if (new == field) { return }

            field = new

            onChanged_.set.forEach { it(this) }
        }
}

class LongModel(private val progression: LongProgression): AbstractModel<Long>() {

    override val hasNext     get() = value + progression.step <= progression.last
    override val hasPrevious get() = value - progression.step >= progression.first

    override fun next    () { if (hasNext    ) { value += progression.step } }
    override fun previous() { if (hasPrevious) { value -= progression.step } }

    override var value = progression.first
        private set(new) {
            if (new == field) { return }

            field = new

            onChanged_.set.forEach { it(this) }
        }
}

class ListModel<T>(private val values: List<T>): AbstractModel<T>() {
    private var index = 0
        private set(new) {
            if (new == field) { return }

            field = new

            onChanged_.set.forEach { it(this) }
        }

    override val hasNext     get() = index < values.lastIndex
    override val hasPrevious get() = index > 0

    override fun next    () { if (hasNext    ) { ++index } }
    override fun previous() { if (hasPrevious) { --index } }

    override val value get() = values[index]
}

class Spinner<T>(model: Model<T>): Gizmo() {
    constructor(values: List<T>): this(ListModel(values))

    fun next    () = model.next()
    fun previous() = model.previous()

    var model = model
        set(new) {
            field.onChanged -= onModelChanged
            field = new
            field.onChanged += onModelChanged
        }

    val value       get() = model.value
    val hasNext     get() = model.hasNext
    val hasPrevious get() = model.hasPrevious

    var renderer: SpinnerUI<T>? = null
        set(new) {
            children.clear()

            field = new?.also {
                it.components(this).also {
                    children.addAll(it.components)
                    layout = it.layout()
                }
            }
        }

    override fun render(canvas: Canvas) {
        renderer?.render(this, canvas)
    }

    @Suppress("PrivatePropertyName")
    private val onChanged_ = ChangeObserversImpl<Spinner<T>>()

    val onChanged: ChangeObservers<Spinner<T>> = onChanged_

    private val onModelChanged: (Model<T>) -> Unit = {
        onChanged_.set.forEach { it(this) }
    }

    init {
        this.model.onChanged += onModelChanged
    }

//     private val mEditorGenerator = new DefaultEditorGenerator()

//    public void setEditorGenerator( final EditorGenerator aEditorGenerator )
//    {
//        setProperty( new AbstractNamedProperty<EditorGenerator>( EDITOR_GENERATOR )
//                     {
//                         @Override public EditorGenerator getValue()
//                         {
//                             return mEditorGenerator
//                         }
//
//                         @Override public void setValue( EditorGenerator aValue )
//                         {
//                             if( aValue == null ) { mEditorGenerator = new DefaultEditorGenerator(); }
//                             else                 { mEditorGenerator = aValue;                       }
//                         }
//                     },
//                     aEditorGenerator )
//    }
//
//    public EditorGenerator getEditorGenerator() { return mEditorGenerator; }
//
//    public interface EditorGenerator extends ItemEditor
//    {
//        Gizmo getGizmo( Spinner aSpinner, Object aObject )
//    }
//
//    private static class DefaultEditorGenerator implements EditorGenerator
//    {
//        public DefaultEditorGenerator()
//        {
//            mLabel = new Label()
//
//            mLabel.setTextHorizontalAlignment( Location.RIGHT )
//        }
//
//        @Override public Gizmo getGizmo( Spinner aSpinner, Object aObject )
//        {
//            mLabel.setText( aObject.toString() )
//
//            return mLabel
//        }
//
//        @Override public Object  getValue        (                    ) { return mLabel.getText(); }
//        @Override public Boolean isEditable      ( Event aEvent       ) { return false;            }
//        @Override public Boolean stopEditing     (                    ) { return false;            }
//        @Override public Boolean cancelEditing   (                    ) { return false;            }
//        @Override public Boolean shouldSelectItem( Event aEvent       ) { return false;            }
//        @Override public void    addListener     ( Listener aListener ) {                          }
//        @Override public void    removeListener  ( Listener aListener ) {                          }
//
//
//        private Label mLabel
//    }

    companion object {
        operator fun invoke(progression: IntProgression) = Spinner(IntModel(progression))
    }
}