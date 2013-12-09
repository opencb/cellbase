myApp.directive('variantsSelect', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: './views/variants-select-panel.html'
    };
});