myApp.directive('geneAndTransc', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {
            data: '=gene',
            showMore: '=geneMoreInfo',
            specieName: '=specie',
            functionDirective: '=onClick'
        },
        templateUrl: './views/widgets/geneAndTranscripts.html'
    };
});
