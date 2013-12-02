myApp.directive('moreInfoGrid', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: './views/widgets/moreInfo-tables.html'
    };
});