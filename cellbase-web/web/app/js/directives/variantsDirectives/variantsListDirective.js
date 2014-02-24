myApp.directive('witchedVariantsList', function () {
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
        templateUrl: './views/widgets/variantsList.html'
    };
});
