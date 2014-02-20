regulationsSelectModule.directive('regulationsSelect', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: './views/regulations-select-panel.html'
    };
});