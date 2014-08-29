exports.config = {
  //This is how we set locals for the server-template-compile module.
  //The locals get sent to the handlebars compiler
  "serverTemplate": {
    "locals": {
      "phoneLiveReload": true,
      "reload": true
    }
  },
  "watch" : {
    "compiledDir":"../www"
  }
}