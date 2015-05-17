module.exports = {
    getTemplatePath: function getTemplatePath(path) {
        if(angular.isDefined(window.Cordova))
            path = 'file:///android_asset/www/' + path;
        return path;
    }
};