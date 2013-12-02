myApp.directive('genesAndTranscriptsGrid', function () {
    return {
        restrict: 'E', //Restricting as 'E' means you restrict it as an Element
        replace: true,
        transclude: true,
        scope: {
            data: '=info'
        },
        templateUrl: './views/widgets/genes-and-transcripts-table.html'
    };
});