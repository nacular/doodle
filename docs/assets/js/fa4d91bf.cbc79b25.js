"use strict";(self.webpackChunkdoodle_docs=self.webpackChunkdoodle_docs||[]).push([[4014],{3712:(n,e,o)=>{o.r(e),o.d(e,{assets:()=>g,contentTitle:()=>u,default:()=>f,frontMatter:()=>m,metadata:()=>h,toc:()=>b});var t=o(7624),i=o(2172),a=o(1268),l=o(5388),r=o(5720);const s='plugins {\n    id ("org.jetbrains.kotlin.multiplatform") version "1.9.22"\n}\n\nversion = "1.0.0"\ngroup   = "com.my.cool.app"\n\nrepositories {\n    mavenCentral()\n}\n\nkotlin {\n    js {\n        browser()\n        binaries.executable()\n    }\n\n    val doodleVersion = "0.10.0" // <--- Latest Doodle version\n\n    dependencies {\n        implementation ("io.nacular.doodle:core:$doodleVersion"   )\n        implementation ("io.nacular.doodle:browser:$doodleVersion")\n\n        // Optional\n        // implementation ("io.nacular.doodle:controls:$doodleVersion" )\n        // implementation ("io.nacular.doodle:animation:$doodleVersion")\n        // implementation ("io.nacular.doodle:themes:$doodleVersion"   )\n    }\n}',d='plugins {\n    id ("org.jetbrains.kotlin.multiplatform") version "1.9.22"\n}\n\nversion = "1.0.0"\ngroup   = "com.my.cool.app"\n\nrepositories {\n    mavenCentral()\n}\n\nkotlin {\n    wasm {\n        browser()\n        binaries.executable()\n\n        // Apply binaryen to optimize output\n        if (project.gradle.startParameter.taskNames.find { it.contains("wasmJsBrowserProductionWebpack") } != null) {\n            applyBinaryen {\n                binaryenArgs = mutableListOf(\n                    "--enable-nontrapping-float-to-int",\n                    "--enable-gc",\n                    "--enable-reference-types",\n                    "--enable-exception-handling",\n                    "--enable-bulk-memory",\n                    "--inline-functions-with-loops",\n                    "--traps-never-happen",\n                    "--fast-math",\n                    "--closed-world",\n                    "--metrics",\n                    "-O3", "--gufa", "--metrics",\n                    "-O3", "--gufa", "--metrics",\n                    "-O3", "--gufa", "--metrics",\n                )\n            }\n        }\n    }\n\n    val doodleVersion = "0.10.0" // <--- Latest Doodle version\n\n    dependencies {\n        implementation ("io.nacular.doodle:core:$doodleVersion"   )\n        implementation ("io.nacular.doodle:browser:$doodleVersion")\n\n        // Optional\n        // implementation ("io.nacular.doodle:controls:$doodleVersion" )\n        // implementation ("io.nacular.doodle:animation:$doodleVersion")\n        // implementation ("io.nacular.doodle:themes:$doodleVersion"   )\n    }\n}',p='plugins {\n    id ("org.jetbrains.kotlin.jvm") version "1.9.22"\n    application\n}\n\nversion = "1.0.0"\ngroup   = "com.my.cool.app"\n\nrepositories {\n    mavenCentral()\n    maven {\n        url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")\n    }\n}\n\nkotlin {\n    target.compilations.all {\n        kotlinOptions {\n            jvmTarget = "11"\n        }\n    }\n\n    val doodleVersion = "0.10.0" // <--- Latest Doodle version\n\n    dependencies {\n        val osName = System.getProperty("os.name")\n        val targetOs = when {\n            osName == "Mac OS X"       -> "macos"\n            osName.startsWith("Win"  ) -> "windows"\n            osName.startsWith("Linux") -> "linux"\n            else                       -> error("Unsupported OS: $osName")\n        }\n\n        val osArch = System.getProperty("os.arch")\n        val targetArch = when (osArch) {\n            "x86_64", "amd64" -> "x64"\n            "aarch64"         -> "arm64"\n            else              -> error("Unsupported arch: $osArch")\n        }\n\n        val target = "$targetOs-$targetArch"\n\n        implementation ("io.nacular.doodle:core:$doodleVersion"               )\n        implementation ("io.nacular.doodle:desktop-jvm-$target:$doodleVersion") // Desktop apps are tied to specific platforms\n\n        // Optional\n        // implementation ("io.nacular.doodle:controls:$doodleVersion" )\n        // implementation ("io.nacular.doodle:animation:$doodleVersion")\n        // implementation ("io.nacular.doodle:themes:$doodleVersion"   )\n    }\n}\n\napplication {\n    mainClass.set("YOUR_CLASS")\n}',c='plugins {\n    id ("org.jetbrains.kotlin.multiplatform") version "1.9.22"\n    application\n}\n\nversion = "1.0.0"\ngroup   = "com.my.cool.app"\n\nrepositories {\n    mavenCentral()\n    maven {\n        url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")\n    }\n}\n\nkotlin {\n    js {\n        browser()\n        binaries.executable()\n    }\n\n    wasm {\n        browser()\n        binaries.executable()\n\n        // Apply binaryen to optimize output\n        if (project.gradle.startParameter.taskNames.find { it.contains("wasmJsBrowserProductionWebpack") } != null) {\n            applyBinaryen {\n                binaryenArgs = mutableListOf(\n                    "--enable-nontrapping-float-to-int",\n                    "--enable-gc",\n                    "--enable-reference-types",\n                    "--enable-exception-handling",\n                    "--enable-bulk-memory",\n                    "--inline-functions-with-loops",\n                    "--traps-never-happen",\n                    "--fast-math",\n                    "--closed-world",\n                    "--metrics",\n                    "-O3", "--gufa", "--metrics",\n                    "-O3", "--gufa", "--metrics",\n                    "-O3", "--gufa", "--metrics",\n                )\n            }\n        }\n    }\n\n    jvm {\n        withJava()\n        compilations.all {\n            kotlinOptions {\n                jvmTarget = "11"\n            }\n        }\n    }\n\n    val doodleVersion = "0.10.0" // <--- Latest Doodle version\n\n    sourceSets {\n        val commonMain by getting {\n            dependencies {\n                implementation ("io.nacular.doodle:core:$doodleVersion")\n\n                // Optional\n                // implementation ("io.nacular.doodle:controls:$doodleVersion" )\n                // implementation ("io.nacular.doodle:animation:$doodleVersion")\n                // implementation ("io.nacular.doodle:themes:$doodleVersion"   )\n            }\n        }\n\n        val jsMain by getting {\n            dependencies {\n                implementation ("io.nacular.doodle:browser:$doodleVersion")\n            }\n        }\n\n        val jvmMain by getting {\n            dependencies {\n                val osName = System.getProperty("os.name")\n                val targetOs = when {\n                    osName == "Mac OS X"       -> "macos"\n                    osName.startsWith("Win"  ) -> "windows"\n                    osName.startsWith("Linux") -> "linux"\n                    else                       -> error("Unsupported OS: $osName")\n                }\n\n                val osArch = System.getProperty("os.arch")\n                val targetArch = when (osArch) {\n                    "x86_64", "amd64" -> "x64"\n                    "aarch64"         -> "arm64"\n                    else              -> error("Unsupported arch: $osArch")\n                }\n\n                val target = "$targetOs-$targetArch"\n\n                implementation ("io.nacular.doodle:desktop-jvm-$target:$doodleVersion") // Desktop apps are tied to specific platforms\n            }\n        }\n    }\n}\n\napplication {\n    mainClass.set("YOUR_CLASS")\n}',m={hide_title:!0,title:"Installation"},u=void 0,h={id:"installation",title:"Installation",description:"Installation",source:"@site/docs/installation.mdx",sourceDirName:".",slug:"/installation",permalink:"/doodle/docs/installation",draft:!1,unlisted:!1,tags:[],version:"current",frontMatter:{hide_title:!0,title:"Installation"},sidebar:"tutorialSidebar",previous:{title:"Hello Doodle",permalink:"/doodle/docs/introduction"},next:{title:"Whats new in Doodle",permalink:"/doodle/docs/whatsnew"}},g={},b=[{value:"Installation",id:"installation",level:2},{value:"build.gradle.kts",id:"buildgradlekts",level:4},{value:"build.gradle.kts",id:"buildgradlekts-1",level:4},{value:"build.gradle.kts",id:"buildgradlekts-2",level:4},{value:"Multi-platform",id:"multi-platform",level:2},{value:"build.gradle.kts",id:"buildgradlekts-3",level:4}];function v(n){const e={a:"a",admonition:"admonition",code:"code",h2:"h2",h4:"h4",p:"p",strong:"strong",...(0,i.M)(),...n.components};return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsx)(e.h2,{id:"installation",children:"Installation"}),"\n",(0,t.jsxs)(e.p,{children:["Doodle apps are built using ",(0,t.jsx)(e.a,{href:"http://www.gradle.org",children:"Gradle"})," like other ",(0,t.jsx)(e.a,{href:"https://kotlinlang.org/docs/getting-started.html",children:"Kotlin apps"}),". These apps can be developed to target multiple platforms, with various build configurations depending on the mix of platforms supported."]}),"\n",(0,t.jsx)(e.p,{children:"The following shows how to configure your app for various targets."}),"\n",(0,t.jsxs)(a.c,{children:[(0,t.jsxs)(l.c,{value:"Browser (JavaScript)",children:[(0,t.jsx)(e.p,{children:"You can set up an app that runs in the Browser using JavaScript with the following build script."}),(0,t.jsx)(e.h4,{id:"buildgradlekts",children:"build.gradle.kts"}),(0,t.jsx)(r.A,{children:s}),(0,t.jsx)(e.admonition,{type:"tip",children:(0,t.jsxs)(e.p,{children:["Learn more about ",(0,t.jsx)(e.a,{href:"https://kotlinlang.org/docs/js-project-setup.html",children:"Kotlin for Javascript"}),"."]})})]}),(0,t.jsxs)(l.c,{value:"Browser (WasmJs)",children:[(0,t.jsx)(e.p,{children:"WasmJs apps run in the Browser and are configured as follows."}),(0,t.jsx)(e.h4,{id:"buildgradlekts-1",children:"build.gradle.kts"}),(0,t.jsx)(r.A,{children:d}),(0,t.jsx)(e.admonition,{type:"tip",children:(0,t.jsxs)(e.p,{children:["Learn more about ",(0,t.jsx)(e.a,{href:"https://kotlinlang.org/docs/wasm-get-started.html",children:"Kotlin for WebAssembly"}),"."]})})]}),(0,t.jsxs)(l.c,{value:"Desktop (JVM)",children:[(0,t.jsx)(e.p,{children:"You can set up a pure JVM app that runs on Desktop with the following build scripts."}),(0,t.jsx)(e.h4,{id:"buildgradlekts-2",children:"build.gradle.kts"}),(0,t.jsx)(r.A,{children:p}),(0,t.jsx)(e.admonition,{type:"tip",children:(0,t.jsxs)(e.p,{children:["Learn more about ",(0,t.jsx)(e.a,{href:"https://kotlinlang.org/docs/jvm-get-started.html",children:"Kotlin for the JVM"}),"."]})})]})]}),"\n",(0,t.jsx)(e.h2,{id:"multi-platform",children:"Multi-platform"}),"\n",(0,t.jsxs)(e.p,{children:["Doodle is a set of Kotlin ",(0,t.jsx)(e.a,{href:"https://kotlinlang.org/docs/multiplatform-get-started.html",children:"Multi-platform"})," libraries. Which means you can create an MPP for your app as well. The advantage of this is that you can write your app entirely (except for ",(0,t.jsx)(e.code,{children:"main"}),") in ",(0,t.jsx)(e.code,{children:"common"})," code and make it available on both Web (JS) and Desktop (JVM). The following shows how to create such an app."]}),"\n",(0,t.jsx)(e.admonition,{type:"tip",children:(0,t.jsxs)(e.p,{children:["App ",(0,t.jsx)(e.a,{href:"/doodle/docs/applications#launching-an-application",children:(0,t.jsx)(e.strong,{children:"launch code"})})," is the only portion that needs to be in ",(0,t.jsx)(e.code,{children:"jsMain"})," or ",(0,t.jsx)(e.code,{children:"jvmMain"}),"."]})}),"\n",(0,t.jsx)(e.h4,{id:"buildgradlekts-3",children:"build.gradle.kts"}),"\n",(0,t.jsx)(r.A,{children:c})]})}function f(n={}){const{wrapper:e}={...(0,i.M)(),...n.components};return e?(0,t.jsx)(e,{...n,children:(0,t.jsx)(v,{...n})}):v(n)}}}]);