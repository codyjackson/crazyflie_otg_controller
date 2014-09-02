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
    .factory('radio', [function(){
        return {
            connect: function() {

            },
            setTargetThrust: function(percentage){
                console.log(percentage);
            },
            setTargetPitch: function(degrees){

            },
            setTargetRoll: function(degrees) {

            },
            setTargetYaw: function(degrees) {

            }
        };
    }])
    .directive('bar', [function(){
        return { 
            restrict: 'A',
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
    .directive('track', ['$parse', function($parse){
        var _leverElement = null;
        var _moving = false;

        function contains(str, substr) {
            return str.indexOf(substr) > -1;
        }

        function extractX(ev) {
            ev = ev.originalEvent;
            if(contains(ev.type, 'mouse')) {
                return ev.clientX;
            }
            if(ev.targetTouches.length != 1) {
                return;
            }

            return ev.targetTouches[0].clientX;
        }

        function killPropegation(ev) {
            ev.stopImmediatePropagation()
        }

        function fit(min, max, x) {
            if(x < min)
                return min;
            if(x > max)
                return max;
            return x;
        }

        return {
            restrict: 'A',
            controller: function(){
                this.registerLeverElement = function(leverElement){
                    _leverElement = $(leverElement);
                };
            },
            link: function(scope, element, attributes){
                var updatePercentage = function(){
                    var fn = $parse(attributes.track);
                    return function(percentage){ 
                        scope.$apply(function(){
                            fn(scope, {$percentage: percentage});
                        });
                    };
                }();

                function updateLever(ev) {
                    if(contains(ev.type, 'down') || contains(ev.type, 'start')) {
                        _moving = true;
                    }
                    if(contains(ev.type, 'up') || contains(ev.type, 'end')) {
                        _moving = false;
                    }

                    if(!_moving) {
                        return;
                    }

                    var inputX = extractX(ev);
                    if(!inputX) {
                        return;
                    }

                    var leverWidth = _leverElement.outerWidth();
                    var leverHalfWidth = leverWidth / 2;

                    var min = 0;
                    var max = element.outerWidth() - leverWidth;
                    var left = fit(min, max, inputX - leverHalfWidth);

                    _leverElement.css({left: left});
                    updatePercentage(left/max);
                }

                element.on('mousedown', updateLever);
                element.on('mouseup', updateLever);
                element.on('mousemove', updateLever);

                element.on('touchcancel', killPropegation);
                element.on('touchstart', updateLever);
                element.on('touchend', updateLever);
                element.on('touchmove', updateLever);
            }
        };
    }])
    .directive('lever', [function(){
        return {
            restrict: 'A',
            require: '^track',
            link: function(scope, element, attributes, trackControl) {
                trackControl.registerLeverElement(element);
            }
        };
    }])
    .controller('GlobalController', ['$scope', '$rootScope', function($scope, $rootScope){
        $scope.onScreenTap = function(){
            $rootScope.$emit('SCREEN_TAP');
        };
    }])
    .controller('ConnectController', ['$rootScope', '$location', function($rootScope, $location){
        $rootScope.$on('SCREEN_TAP', function(){
            $rootScope.phoneOrientation = 0;
            $location.path('calibrate');
        });
    }])
    .controller('CalibrateController', ['$scope', '$rootScope', '$location', function($scope, $rootScope, $location){
        $rootScope.$on('SCREEN_TAP', function(){
            $rootScope.phoneOrientation = 0;
            $location.path('fly');
        });
    }])
    .controller('FlyController', ['$rootScope', '$scope', 'radio' function($rootScope, $scope, radio){
        $scope.onNewThrust = function(percentage) {
            radio.setTargetThrust(percentage);
        }
    }])
    .controller('ReconnectController', [function(){
        console.log('ReconnectController');
    }]);

angular.element(document).ready(function(){
    angular.bootstrap(document, ['application']);
});