var $ = require('jquery');
var angular = require('angular');
var angularRoute = require('angular-route');

var util = require('./util');

var Orientation = require('./orientation');

require('./services');
require('./controllers');
require('./directives');

angular.module('application', ['ngRoute', 'services', 'controllers', 'directives'])
    .config(['$routeProvider', function($routeProvider){
        $routeProvider
            .when('/', {
                templateUrl: util.getTemplatePath('partial-views/connect.html'),
                controller: 'ConnectController' 
            })
            .when('/calibrate', {
                templateUrl: util.getTemplatePath('partial-views/calibrate.html'),
                controller: 'CalibrateController'   
            })
            .when('/fly', {
                templateUrl: util.getTemplatePath('partial-views/fly.html'),
                controller: 'FlyController'   
            })
            .when('/reconnect', {
                templateUrl: util.getTemplatePath('partial-views/connect.html'),
                controller: 'ReconnectController'   
            });
    }]);

(function(){
    var applicationElement = angular.element(document);
    applicationElement.ready(function(){
        angular.bootstrap(document, ['application']);

        var injector = applicationElement.injector();
        var radioService = injector.get('radio');
        var phoneService = injector.get('phone');

        window.newCopterOrientation = function(yaw) {
            radioService._newCopterYaw(yaw);
        };

        window.addEventListener('deviceorientation', function(ev){
            phoneService._newPhoneOrientation(new Orientation(ev.beta, -ev.gamma, -ev.alpha));
        });
    });
})();
