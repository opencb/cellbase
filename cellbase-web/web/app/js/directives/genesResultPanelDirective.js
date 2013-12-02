myApp.directive('genesResult', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: './views/genes-result-panel.html'
    };
});