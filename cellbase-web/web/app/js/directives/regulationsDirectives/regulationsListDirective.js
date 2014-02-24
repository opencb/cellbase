myApp.directive('witchedRegulationsList', function () {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {
            allData: '=data',
            toggleList: '=toggle',
            showHistone: '=showHistone',
            showHistoneName: '=showHistoneName',
            showOpenChromatin: '=showOpenChromatin',
            showHpenChromatinName: '=showOpenChromatinName',
            showTranscriptionFactor: '=showTranscriptionFactor',
            showTranscriptionFactorName: '=showTranscriptionFactorName',
            showPolymerase: '=showPolymerase',
            showPolymeraseName: '=showPolymeraseName',
            showMicroRNA: '=showMicroRna',
            showMicroRNAName: '=showMicroRnaName'
        },
        templateUrl: './views/widgets/regulationsList.html'
    };
});
