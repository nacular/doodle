package com.zinoti.jaz.dom

import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.get
import kotlin.browser.document


fun HTMLElement.cloneNode() = cloneNode(true)

fun HTMLElement.childAt(index: Int): Node? {
    if( index in 0 until childNodes.length ) {
        return childNodes[index]
    }

    return null
}

val HTMLElement.parent get() = parentNode as HTMLElement?

val HTMLElement.numChildren get() = childNodes.length

fun HTMLElement.index(of: HTMLElement) = (0 until childNodes.length).firstOrNull { childNodes[it] == of } ?: -1

fun HTMLElement.add(child: HTMLElement) = appendChild(child)

fun HTMLElement.insert(element: HTMLElement, index: Int) {
    insertBefore(element, childAt(index))
}

fun HTMLElement.remove(element: HTMLElement) = removeChild(element)

fun HTMLElement.removeAll() {
    while(firstChild != null)
    {
        firstChild?.let { remove(it as HTMLElement) }
    }
}

val HTMLElement.top    get() = offsetTop.toDouble   ()
val HTMLElement.left   get() = offsetLeft.toDouble  ()
val HTMLElement.width  get() = offsetWidth.toDouble ()
val HTMLElement.height get() = offsetHeight.toDouble()

private val sPrototypes = mutableMapOf<String, HTMLElement>()

fun create(tag: String): HTMLElement {
    var master: HTMLElement? = sPrototypes[tag]

    // No cached version exists

    if (master == null) {
        master = document.createElement(tag) as HTMLElement

        sPrototypes.put(tag, master)
    }

    return master.cloneNode(false) as HTMLElement
}

fun createText(text: String) = document.createTextNode(text)


//    public static final class Type extends JavaScriptObject
//    {
//        protected Type() {}
//
//        public static Type create( String aValue )
//        {
//            if( GWT.isScript() ) { return createScript( aValue ); }
//            else                 { return createHosted( aValue ); }
//        }
//
//        public static Type createScript( String aValue ) /*-{ return aValue; }-*/;
//        public static Type createHosted( String aValue ) /*-{ this[0] = [aValue]; return this[0]; }-*/;
//
//        public static final Type text     = create( "text"     );
//        public static final Type radio    = create( "radio"    );
//        public static final Type button   = create( "button"   );
//        public static final Type checkbox = create( "checkbox" );
//    }
