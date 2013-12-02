
myApp.directive('optionsBar', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: './views/options-bar.html'
    };
});
