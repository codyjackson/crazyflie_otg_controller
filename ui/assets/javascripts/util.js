module.exports = {
    getTemplatePath: function getTemplatePath(path) {
        if(document.URL.indexOf( 'http://' ) !== -1 && document.URL.indexOf( 'https://' ) !== -1) {
            path = 'file:///android_asset/www/' + path;
        }
        return path;
    }
};