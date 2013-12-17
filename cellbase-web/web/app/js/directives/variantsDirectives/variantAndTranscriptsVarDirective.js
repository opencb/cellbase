myApp.directive('variantsAndTranscVar', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {
            data: '=variant',
            showMore: '=variantMoreInfo',
            specieName: '=specie',
            functionDirective: '=onClick',
            changeTab: '=tabFunction'
        },
        templateUrl: './views/widgets/variantAndTranscriptsVar.html'
    };
});
