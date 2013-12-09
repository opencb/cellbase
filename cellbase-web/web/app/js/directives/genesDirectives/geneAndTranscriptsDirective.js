myApp.directive('geneAndTransc', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {
            data: '=info',
            showMore: '=infoMore',
            specieName: '=specie',
            functionDirective: '=function'
        },
        templateUrl: './views/widgets/geneAndTranscripts.html'
    };
});
