package com.zinoti.jaz.dom

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.get
import kotlin.browser.document


fun HTMLElement.cloneNode() = this.cloneNode(true)

fun HTMLElement.childAt(index: Int): Node? {
    if( index in 0 until this.childNodes.length ) {
        return this.childNodes[index]
    }

    return null
}

fun HTMLElement.numChildren() = this.childNodes.length

fun HTMLElement.index(of: Node) = (0 until this.childNodes.length).firstOrNull { this.childNodes[it] == of } ?: -1

fun HTMLElement.insert(element: Node, index: Int) {
    insertBefore(element, childAt(index))
}

fun HTMLElement.remove(element: Node) = this.removeChild(element)

fun HTMLElement.removeAll() {
    while(this.firstChild != null)
    {
        this.firstChild?.let { this.remove(it) }
    }
}

val HTMLElement.top    get() = this.offsetTop.toDouble   ()
val HTMLElement.left   get() = this.offsetLeft.toDouble  ()
val HTMLElement.width  get() = this.offsetWidth.toDouble ()
val HTMLElement.height get() = this.offsetHeight.toDouble()

private val sPrototypes = mutableMapOf<String, Element>()

fun create(tag: String): Element {
    var master: Element? = sPrototypes[tag]

    // No cached version exists

    if (master == null) {
        master = document.createElement(tag)

        sPrototypes.put(tag, master)
    }

    return master.cloneNode(false) as Element
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
