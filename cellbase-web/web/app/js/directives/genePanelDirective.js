myApp.directive('genePanel', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: './views/widgets/gene-panel.html'
    };
});