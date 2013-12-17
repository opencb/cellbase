myApp.directive('regulationsResult', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: './views/regulations-result-panel.html'
    };
});