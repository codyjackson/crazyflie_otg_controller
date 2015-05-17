var angular = require('angular');
var Orientation = require('./orientation');

angular.module('services', [])
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
            yaw : 0
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
            _newCopterYaw: function(yaw) {
                $rootScope.$apply(function(){
                    $rootScope.copter.yaw = yaw;
                });
            }
        };
    }]);