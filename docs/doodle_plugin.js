// Plugin based on https://github.com/njleonzhang/docsify-demo-box-react
window.$docsify.plugins.push(
    function(hook, vm) {
        var currentId = 0

        function startApp(run, id) {
            setTimeout(function () {
                element              = document.getElementById(id)
                element.style.height = config.height+''

                run(element)
            })
        }

        window.$docsify.markdown = {
            renderer: {
                code: function(code, lang) {
                    if (lang == "doodle") {
                        id = "a" + currentId++

                        config = JSON.parse(code)

                        if (config.border == false) {
                            cls = ''
                        } else {
                            cls = 'class = "doodle"'
                        }

                        startApp(eval(config.run), id)

                        return '<div '+cls+' style="margin:var(--code-block-margin)"/><div id="' + id + '" style="position:relative"></div></div/>'
                    } else {
                        lang = lang || ''
                        hl   = Prism.highlight(code, Prism.languages[lang] || Prism.languages.markup)
                        return '<pre v-pre data-lang="' +lang+ '"><code class="lang-' +lang+ '">' +hl+ '</code></pre>'
                    }
                }
            }
        }
    }
)