myApp.directive('genesSelect', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: './views/genes-select-panel.html'
    };
});