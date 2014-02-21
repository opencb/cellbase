myApp.directive('witchedChrom', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {
            idOfChroms: '=ids',
            chromosomeNames: '=chromNames',
            add: '=onClick',
            myClass: '@myClass'
        },
        templateUrl: './views/widgets/chromosomes.html'
    };
});
