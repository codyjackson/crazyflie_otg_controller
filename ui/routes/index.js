var index = function(config) {
  cachebust = ''
  if (process.env.NODE_ENV !== "production") {
    cachebust = "?b=" + (new Date()).getTime()
  }

  // In the event plain html pages are being used, need to
  // switch to different page for optimized view
  var name = "index";
  if (config.isOptimize && config.server.views.html) {
    name += "-optimize";
  }

  return function(req, res) {
    var options = {
      reload:    config.liveReload.enabled,
      optimize:  config.isOptimize != null ? config.isOptimize : false,
      cachebust: cachebust,
      phone: req.headers['user-agent'].indexOf('Android') !== -1
    };

    res.render(name, options);
  };
};

exports.index = index;
