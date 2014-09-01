var $ = require('jquery');
var angular = require('angular');
var angularRoute = require('angular-route');

angular.module('application', ['ngRoute'])
    .config(['$routeProvider', function($routeProvider){
        $routeProvider
            .when('/', {
                templateUrl: '../partial-views/connect.html',
                controller: 'ConnectController' 
            })
            .when('/calibrate', {
                templateUrl: '../partial-views/calibrate.html',
                controller: 'CalibrateController'   
            })
            .when('/fly', {
                templateUrl: '../partial-views/fly.html',
                controller: 'FlyController'   
            })
            .when('/reconnect', {
                templateUrl: '../partial-views/connect.html',
                controller: 'ReconnectController'   
            });
    }])
    .directive('bar', [function(){
        return {
            restrict: 'E',
            templateUrl: '../partial-views/bar.html',
            transclude: true
        };
    }])
    .directive('screenTap', ['$document', '$parse', function($document, $parse){
        var instantiated = false;
        return {
            restrict: 'A',
            link: function(scope, element, attributes){
                if(instantiated) {
                    throw 'Only one screen-tap directive can be used at a time.';
                }

                var fnWrapper = $parse(attributes.screenTap);
                $document.on('click', function(ev){
                    scope.$apply(function(){
                        fnWrapper(scope, {$event: ev});
                    });
                });

                scope.$on('$destroy', function(){
                    $document.off('click');
                    instantiated = false;
                });
                instantiated = true;
            }
        };
    }])
    .controller('GlobalController', ['$scope', '$rootScope', function($scope, $rootScope){
        $scope.onScreenTap = function(){
            $rootScope.$emit('SCREEN_TAP');
        };
    }])
    .controller('ConnectController', [function(){
        console.log('ConnectController');
    }])
    .controller('CalibrateController', ['$scope', '$rootScope', function($scope, $rootScope){
        $rootScope.$on('SCREEN_TAP', function(){
        });
    }])
    .controller('FlyController', [function(){
        console.log('FlyController');
    }])
    .controller('ReconnectController', [function(){
        console.log('ReconnectController');
    }]);

angular.element(document).ready(function(){
    angular.bootstrap(document, ['application']);
});