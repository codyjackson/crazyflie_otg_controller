exports.config = {
  "modules": [
    "copy",
    "server",
    "browserify",
    "jshint",
    "csslint",
    "minify-js",
    "minify-css",
    "live-reload",
    "bower",
    "less",
    "server-template-compile"
  ],
  "server": {
    "views": {
      "compileWith": "handlebars",
      "extension": "hbs"
    }
  },
  "browserify": {
    "bundles": [
      { 
        "entries": ["javascripts/main.js"],
        "outputFile": "bundle.js" 
      }
    ],
    "shims": {
      "jquery": {
        "path": "javascripts/vendor/jquery/jquery",
        "exports": "$"
      }
    }
  }
}