var $ = require('jquery');
var angular = require('angular');
var angularRoute = require('angular-route');

function getTemplatePath(path) {
    if(angular.isDefined(window.Cordova))
        path = 'file:///android_asset/www/' + path;
    return path;
}

function radians(degrees){
    return degrees*Math.PI/180.0;
}

function degrees(radians){
    return radians*180.0/Math.PI;
}

function Orientation(pitch, roll, yaw) {
    this.pitch = pitch;
    this.roll = roll;
    this.yaw = yaw;
}

Orientation.prototype.calculateCorrectedOrientation = function (yawFrameOfReference, currentYaw) {
    var yawDeviation =  currentYaw - yawFrameOfReference;
    var v = Vector.create([this.roll, this.pitch, 0]);
    var m = Matrix.RotationZ(radians(yawDeviation));
    var v2 = m.multiply(v);

    console.log([v2.e(1), v2.e(2)]);
    return new Orientation(v2.e(1), v2.e(2), 0);
};

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

        function makeCordovaApi(name, mock) {
            function impl() {
                var argsArray = Array.prototype.slice.call(arguments, 0);

                var deferred = $q.defer();
                cordova.exec(function(){
                    deferred.resolve.apply(deferred, arguments);
                }, function(){
                    deferred.reject.apply(deferred, arguments);
                }, 'Radio', name, argsArray);
                return deferred.promise;
            }

            function mockImpl() {
                if(mock) {
                    return mock.apply(mock, arguments);
                }

                var deferred = $q.defer();
                deferred.resolve();
                return deferred.promise;
            }

            return typeof cordova !== 'undefined' ? impl : mockImpl;
        }

        return {
            connect: makeCordovaApi('connect'),
            updateOrientation: makeCordovaApi('updateOrientation'),
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
    .controller('ConnectController', ['$rootScope', '$scope', '$location', 'radio', function($rootScope, $scope, $location, radio){
        $location.path('calibrate');
        return;

        var deRegister = $rootScope.$on('SCREEN_TAP', function(){
            radio.connect().then(function(){
                $location.path('calibrate');
            }, function(s){
                alert('Failed to connect. Try again.');
            });
        });

        $scope.$on("$destroy", function() {
            deRegister();
        });
    }])
    .controller('CalibrateController', ['$scope', '$rootScope', '$location', '$timeout', function($scope, $rootScope, $location, $timeout){
        var deRegister = $rootScope.$on('SCREEN_TAP', function(){
            $rootScope.phone.frameOfReferenceYaw = $rootScope.phone.orientation.yaw;
            $rootScope.copter.frameOfReferenceYaw = $rootScope.copter.orientation.yaw;

            $timeout(function(){
                $location.path('fly');
            });
        });

        $scope.$on("$destroy", function() {
            deRegister();
        });
    }])
    .controller('FlyController', ['$rootScope', '$scope', 'radio', function($rootScope, $scope, radio){
        $scope.orientation = new Orientation(0, 0, 0);
        $scope.thrustPercentage = 0;

        $scope.onNewThrust = function(percentage) {
            $scope.thrustPercentage = percentage;
        };

        $rootScope.$watch(function(){
            return $rootScope.phone.orientation;
        }, function(){
            $scope.targetOrientation = $rootScope.phone.orientation.calculateCorrectedOrientation($rootScope.phone.frameOfReferenceYaw + 90, $rootScope.phone.orientation.yaw);
        });

        (function(){
            var intervalHandle = setInterval(function(){
                //var copterCorrectedOrientation = $scope.targetOrientation.calculateCorrectedOrientation($rootScope.copter.frameOfReferenceYaw , $rootScope.copter.orientation.yaw);
                radio.updateOrientation($rootScope.phone.orientation.pitch/2, $rootScope.phone.orientation.roll/2, 0, $scope.thrustPercentage);
            }, 100);

            $scope.$on("$destroy", function() {
                clearInterval(intervalHandle);
            });
        })();
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


        window.newCopterOrientation = function(pitch, roll, yaw) {
            radioService._newCopterOrientation(new Orientation(pitch, roll, yaw));
        };

        window.addEventListener('deviceorientation', function(ev){
            phoneService._newPhoneOrientation(new Orientation(ev.gamma, -ev.beta, ev.alpha));
        });
    });
})();