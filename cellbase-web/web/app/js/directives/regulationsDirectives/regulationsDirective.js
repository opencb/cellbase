myApp.directive('regulations', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {
            data: '=regulations',
            specieName: '=specie'
        },
        templateUrl: './views/widgets/regulations.html'
    };
});
