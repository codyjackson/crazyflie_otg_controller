var $ = require('jquery');
var test = require('./test');

$(document).ready(function(){
    $('p').css('background-color', 'purple');
    test.f();
});