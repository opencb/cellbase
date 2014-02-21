myApp.directive('witchedGenesList', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {
            allData: '=pageData',
            toggleList: '=toggle',
            showFirstData: '=showFirstLevelData',
            showSecondData: '=showSecondLevelData'
        },
        templateUrl: './views/widgets/genesList.html'
    };
});
