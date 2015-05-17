var angular = require('angular');
var Orientation = require('./orientation');

angular.module('controllers', [])
    .controller('GlobalController', ['$scope', '$rootScope', function($scope, $rootScope){
        $scope.onScreenTap = function(){
            $rootScope.$emit('SCREEN_TAP');
        };
    }])
    .controller('ConnectController', ['$rootScope', '$scope', '$location', 'radio', function($rootScope, $scope, $location, radio){
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
            $rootScope.copter.frameOfReferenceYaw = $rootScope.copter.yaw;

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

        $scope.phoneYaw = $rootScope.phone.orientation.yaw;
        $scope.copterYaw = $rootScope.copter.yaw;

        $scope.phoneYawDeviation = 0;
        $scope.copterYawDeviation = 0;

        $scope.onNewThrust = function(percentage) {
            $scope.thrustPercentage = percentage;
        };

        $rootScope.$watch(function(){
            return $rootScope.phone.orientation.yaw;
        }, function(){
            $scope.phoneYaw = $rootScope.phone.orientation.yaw;
            $scope.phoneYawDeviation = $scope.phoneYaw - $rootScope.phone.frameOfReferenceYaw;
        });

        $rootScope.$watch(function(){
            return $rootScope.copter.yaw;
        }, function(){
            $scope.copterYaw = $rootScope.copter.yaw;
            $scope.copterYawDeviation = $scope.copterYaw - $rootScope.copter.frameOfReferenceYaw;
        });

        (function(){
            var intervalHandle = setInterval(function(){
                var yawAggregateDeviation =  $scope.phoneYawDeviation - $scope.copterYawDeviation;
                var targetCopterOrientation = $rootScope.phone.orientation.rotateYaw(yawAggregateDeviation);

                radio.updateOrientation(targetCopterOrientation.pitch/2, targetCopterOrientation.roll/2, 0, $scope.thrustPercentage);
            }, 100);

            $scope.$on("$destroy", function() {
                clearInterval(intervalHandle);
            });
        })();
    }])
    .controller('ReconnectController', [function(){
        console.log('ReconnectController');
    }]);