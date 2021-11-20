"use strict";(self.webpackChunkdoodle_docs=self.webpackChunkdoodle_docs||[]).push([[599],{2150:function(e,a,n){n.r(a),n.d(a,{frontMatter:function(){return l},contentTitle:function(){return o},metadata:function(){return r},toc:function(){return d},default:function(){return c}});var t=n(7462),i=n(3366),s=(n(7294),n(3905)),p=["components"],l={hide_title:!0},o="The Display",r={unversionedId:"display",id:"display",isDocsHomePage:!1,title:"The Display",description:"An app's root container",source:"@site/docs/display.mdx",sourceDirName:".",slug:"/display",permalink:"/docs/display",tags:[],version:"current",frontMatter:{hide_title:!0},sidebar:"tutorialSidebar",previous:{title:"Positioning Views",permalink:"/docs/positioning"},next:{title:"Rendering Overview",permalink:"/docs/rendering/overview"}},d=[{value:"An app&#39;s root container",id:"an-apps-root-container",children:[],level:2},{value:"Adding Views to the Display",id:"adding-views-to-the-display",children:[],level:2},{value:"Launch mode changes the Display",id:"launch-mode-changes-the-display",children:[],level:2}],h={toc:d};function c(e){var a=e.components,n=(0,i.Z)(e,p);return(0,s.kt)("wrapper",(0,t.Z)({},h,n,{components:a,mdxType:"MDXLayout"}),(0,s.kt)("h1",{id:"the-display"},"The Display"),(0,s.kt)("h2",{id:"an-apps-root-container"},"An app's root container"),(0,s.kt)("p",null,"The Display holds an app's View hierarchy, and behaves like a basic container. It is not a View however, so many of the capabilities\nof Views are not available for the Display."),(0,s.kt)("div",{className:"admonition admonition-tip alert alert--success"},(0,s.kt)("div",{parentName:"div",className:"admonition-heading"},(0,s.kt)("h5",{parentName:"div"},(0,s.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,s.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"},(0,s.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M6.5 0C3.48 0 1 2.19 1 5c0 .92.55 2.25 1 3 1.34 2.25 1.78 2.78 2 4v1h5v-1c.22-1.22.66-1.75 2-4 .45-.75 1-2.08 1-3 0-2.81-2.48-5-5.5-5zm3.64 7.48c-.25.44-.47.8-.67 1.11-.86 1.41-1.25 2.06-1.45 3.23-.02.05-.02.11-.02.17H5c0-.06 0-.13-.02-.17-.2-1.17-.59-1.83-1.45-3.23-.2-.31-.42-.67-.67-1.11C2.44 6.78 2 5.65 2 5c0-2.2 2.02-4 4.5-4 1.22 0 2.36.42 3.22 1.19C10.55 2.94 11 3.94 11 5c0 .66-.44 1.78-.86 2.48zM4 14h5c-.23 1.14-1.3 2-2.5 2s-2.27-.86-2.5-2z"}))),"tip")),(0,s.kt)("div",{parentName:"div",className:"admonition-content"},(0,s.kt)("p",{parentName:"div"},(0,s.kt)("a",{parentName:"p",href:"/docs/positioning"},"Layouts")," can be applied to the Display as well."))),(0,s.kt)("p",null,"The Display is available for injection by default."),(0,s.kt)("pre",null,(0,s.kt)("code",{parentName:"pre",className:"language-kotlin"},"class MyApp(display: Display): Application {\n    init {\n        // ...\n    }\n\n    override fun shutdown() {}\n}\n\nfun main() {\n    application {\n        MyApp(display = instance())\n    }\n}\n")),(0,s.kt)("h2",{id:"adding-views-to-the-display"},"Adding Views to the Display"),(0,s.kt)("p",null,"The Display has a ",(0,s.kt)("inlineCode",{parentName:"p"},"children")," property that contains all its direct descendants. These top-level views have no\n",(0,s.kt)("inlineCode",{parentName:"p"},"parent")," and are the top-most ancestors of all other Views in an app. An app can have any number of these Views."),(0,s.kt)("div",{className:"admonition admonition-caution alert alert--warning"},(0,s.kt)("div",{parentName:"div",className:"admonition-heading"},(0,s.kt)("h5",{parentName:"div"},(0,s.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,s.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"16",height:"16",viewBox:"0 0 16 16"},(0,s.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M8.893 1.5c-.183-.31-.52-.5-.887-.5s-.703.19-.886.5L.138 13.499a.98.98 0 0 0 0 1.001c.193.31.53.501.886.501h13.964c.367 0 .704-.19.877-.5a1.03 1.03 0 0 0 .01-1.002L8.893 1.5zm.133 11.497H6.987v-2.003h2.039v2.003zm0-3.004H6.987V5.987h2.039v4.006z"}))),"caution")),(0,s.kt)("div",{parentName:"div",className:"admonition-content"},(0,s.kt)("p",{parentName:"div"},"Top-level Views are displayed, yet they have no ",(0,s.kt)("inlineCode",{parentName:"p"},"parent"),". Similarly, Views can have parents even before they are\nin the display hierarchy: when the parents themselves are not displayed. This means the ",(0,s.kt)("inlineCode",{parentName:"p"},"parent")," property says nothing\nabout a View being in the Display hierarchy. Luckily, View has the ",(0,s.kt)("inlineCode",{parentName:"p"},"displayed")," property for this exact purpose."))),(0,s.kt)("p",null,"Add a top-level View like this."),(0,s.kt)("pre",null,(0,s.kt)("code",{parentName:"pre",className:"language-kotlin"},"class MyApp(display: Display): View() {\n    init {\n        display += view\n    }\n    // ...\n}\n")),(0,s.kt)("p",null,"And remove it like this."),(0,s.kt)("pre",null,(0,s.kt)("code",{parentName:"pre",className:"language-kotlin"},"class MyApp(display: Display): View() {\n    init {\n        display -= view\n    }\n    // ...\n}\n")),(0,s.kt)("h2",{id:"launch-mode-changes-the-display"},"Launch mode changes the Display"),(0,s.kt)("p",null,"A ",(0,s.kt)("a",{parentName:"p",href:"/docs/applications#top-level-apps"},(0,s.kt)("strong",{parentName:"a"},"Stand-Alone"))," Web app that uses the entire page will have a Display tied to the page body. While\none hosted in an element will have a Display that is tied to that element."),(0,s.kt)("p",null,"This is transparent to the app."),(0,s.kt)("pre",null,(0,s.kt)("code",{parentName:"pre",className:"language-kotlin"},"application(modules = listOf(/*...*/)) {\n    MyApp(display = instance() /*,...*/)\n}\n")),(0,s.kt)("p",null,"Here the Display will be tied to ",(0,s.kt)("inlineCode",{parentName:"p"},"someDiv"),"."),(0,s.kt)("pre",null,(0,s.kt)("code",{parentName:"pre",className:"language-kotlin"},"application(root = someDiv, modules = listOf(/*...*/)) {\n    MyApp(display = instance() /*,...*/)\n}\n")),(0,s.kt)("p",null,"The Displays for a ",(0,s.kt)("a",{parentName:"p",href:"/docs/applications#nested-apps"},(0,s.kt)("strong",{parentName:"a"},"Nested app"))," sits within the View hosting it. This means changes to that View's\nsize will change the Display size."))}c.isMDXComponent=!0}}]);