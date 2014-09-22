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
      },
      "angular": {
        "path": "javascripts/vendor/angular/angular",
        "exports": "angular"
      },
      "sylvester-vector": {
        "path": "javascripts/vendor/sylvester/sylvester",
        "exports": "$V"
      },
      "sylvester-matrix": {
        "path": "javascripts/vendor/sylvester/sylvester",
        "exports": "$M"
      }
    },
    "aliases": {
      "angular-route": "javascripts/vendor/angular-route/angular-route"
    }
  }
}