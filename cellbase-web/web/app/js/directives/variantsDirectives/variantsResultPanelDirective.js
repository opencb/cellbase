myApp.directive('variantsResult', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: './views/variants-result-panel.html'
    };
});