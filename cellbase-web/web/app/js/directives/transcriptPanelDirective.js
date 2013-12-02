myApp.directive('transcriptPanel', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: './views/widgets/transcript-panel.html'
    };
});