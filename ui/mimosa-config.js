exports.config = {
  "modules": [
    "server",
    "browserify",
    "jshint",
    "minify-js",
    "minify-css",
    "live-reload",
    "bower",
    "less",
    "server-template-compile",
    "copy"
  ],
  "server": {
    "views": {
      "compileWith": "handlebars",
      "extension": "hbs"
    }
  },
  "watch" : {
    "exclude": [/\.db$/]
  },
  "browserify": {
    "bundles": [
      { 
        "entries": ["javascripts/main.js"],
        "outputFile": "bundle.js" 
      }
    ],
    "paths": ["javascripts"],
    "shims": {
      "jquery": {
        "path": "javascripts/vendor/jquery/jquery",
        "exports": "$"
      },
      "angular": {
        "path": "javascripts/vendor/angular/angular",
        "exports": "angular"
      }
    },
    "noParse" : [
      "javascripts/vendor/jquery/jquery",
      "javascripts/vendor/angular/angular"
    ],
    "aliases": {
      "angular-route": "javascripts/vendor/angular-route/angular-route"
    }
  }
}