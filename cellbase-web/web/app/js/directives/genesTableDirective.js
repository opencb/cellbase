myApp.directive('genesGrid', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: './views/widgets/genes-table.html'
    };
});