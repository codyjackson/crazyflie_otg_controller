var $ = require('jquery');
var angular = require('angular');
var util = require('./util');

angular.module('directives', [])
    .directive('bar', [function(){
        return { 
            restrict: 'A',
            templateUrl: util.getTemplatePath('partial-views/bar.html'),
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
    .directive('yawIndicator', [function(){
        return {
            restrict: 'A',
            templateUrl: util.getTemplatePath('partial-views/yaw-indicator.html'),
            scope: {
                angle: '='
            },
            transclude: 'replace',
            link: function(scope, element, attributes) {
                if(!(attributes.sideLength && attributes.color && scope.angle)) {
                    throw 'The "sideLength", "color" and "angle" attributes must be defined.';
                }

                scope.containerStyle = {
                    width: attributes.sideLength,
                    height: attributes.sideLength
                };

                function formatRotate(angle) {
                    return 'rotate(' + angle + 'deg)';
                }

                scope.indicatorStyle = {
                    borderColor: attributes.color,
                    transform: formatRotate(scope.angle)
                };

                scope.$watch('angle', function(angle){
                    scope.indicatorStyle.transform = formatRotate(angle);
                });
            }
        };
    }]);