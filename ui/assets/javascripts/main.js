var $ = require('jquery');
var angular = require('angular');
var angularRoute = require('angular-route');

function getTemplatePath(path) {
    if(angular.isDefined(window.Cordova))
        path = 'file:///android_asset/www/' + path;
    return path;
}

function Orientation(pitch, roll, yaw) {
    this.pitch = pitch;
    this.roll = roll;
    this.yaw = yaw;
}

angular.module('application', ['ngRoute'])
    .config(['$routeProvider', function($routeProvider){
        $routeProvider
            .when('/', {
                templateUrl: getTemplatePath('partial-views/connect.html'),
                controller: 'ConnectController' 
            })
            .when('/calibrate', {
                templateUrl: getTemplatePath('partial-views/calibrate.html'),
                controller: 'CalibrateController'   
            })
            .when('/fly', {
                templateUrl: getTemplatePath('partial-views/fly.html'),
                controller: 'FlyController'   
            })
            .when('/reconnect', {
                templateUrl: getTemplatePath('partial-views/connect.html'),
                controller: 'ReconnectController'   
            });
    }])
    .factory('phone', ['$rootScope', function($rootScope){
        $rootScope.phone = {
            'orientation' : new Orientation(0, 0, 0)
        };

        return {
            _newPhoneOrientation: function(orientation) {
                $rootScope.$apply(function(){
                    $rootScope.phone.orientation = orientation;
                });
            }
        };
    }])
    .factory('radio', ['$q', '$rootScope', function($q, $rootScope){
        $rootScope.copter = {
            'orientation' : new Orientation(0, 0, 0)
        };

        return {
            connect: function() {
                var deferred = $q.defer();

                cordova.exec(function(){
                    deferred.resolve.apply(deferred, arguments);
                }, function(){
                    deferred.reject.apply(deferred, arguments);
                }, 'Radio', 'doesntmatter', []);

                return deferred.promise;
            },
            setTargetThrust: function(percentage){
                console.log(percentage);
            },
            setTargetPitch: function(degrees){

            },
            setTargetRoll: function(degrees) {

            },
            setTargetYaw: function(degrees) {
            },
            //These should be called externally by cordova and not the application
            _newCopterOrientation: function(orientation) {
                $rootScope.$apply(function(){
                    $rootScope.copter.orientation = orientation;
                });
            }
        };
    }])
    .directive('bar', [function(){
        return { 
            restrict: 'A',
            templateUrl: getTemplatePath('partial-views/bar.html'),
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
            ev.stopImmediatePropagation();
        }

        function killDefault(ev) {
            ev.preventDefault();
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
                element.on('touchstart', killDefault);
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
    .controller('ConnectController', ['$rootScope', '$location', 'radio', function($rootScope, $location, radio){
        $rootScope.$on('SCREEN_TAP', function(){
            radio.connect().then(function(){
                $location.path('calibrate');
            }, function(s){
                alert('Failed to connect. Try again.');
            });
        });
    }])
    .controller('CalibrateController', ['$scope', '$rootScope', '$location', '$timeout', function($scope, $rootScope, $location, $timeout){
        $rootScope.$on('SCREEN_TAP', function(){
            console.log('did i get here?');
            var phoneYaw = $rootScope.phone.orientation.yaw;
            var copterYaw = $rootScope.copter.orientation.yaw;
            $rootScope.phoneAndCopterYawDiff = phoneYaw - copterYaw;
            $timeout(function(){
                $location.path('fly');
            });
        });
    }])
    .controller('FlyController', ['$rootScope', '$scope', 'radio', function($rootScope, $scope, radio){
        $scope.onNewThrust = function(percentage) {
            radio.setTargetThrust(percentage);
        };

        console.log($rootScope.phoneAndCopterYawDiff);
    }])
    .controller('ReconnectController', [function(){
        console.log('ReconnectController');
    }]);

(function(){
    var applicationElement = angular.element(document);
    applicationElement.ready(function(){
        angular.bootstrap(document, ['application']);

        var injector = applicationElement.injector();
        var radioService = injector.get('radio');
        var phoneService = injector.get('phone');

        setInterval(function(){
           radioService._newCopterOrientation(new Orientation(Math.random(), Math.random(), Math.random()));
        }, 200);

        window.addEventListener('deviceorientation', function(ev){
            phoneService._newPhoneOrientation(new Orientation(ev.gamma, -ev.beta, ev.alpha));
        });
    });
})();
