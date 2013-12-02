myApp.directive('chromosomePanel', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: './views/chromosome-options.html',
        link: function (scope, element, attrs) {

            element.on('mousedown', function(event) {
                  console.log("hola");
            });
        }
    };
});