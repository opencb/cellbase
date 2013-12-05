var optionsBarControl = myApp.controller('optionsBarController', ['$scope', 'mySharedService', 'CellbaseService', function ($scope, mySharedService, CellbaseService) {

    //this will be obtained from cellbase
    $scope.species = [
        {longName: "Homo sapiens", shortName: "hsapiens", ensemblName: "Homo_sapiens"},
        {longName: "Mus musculus", shortName: "mmusculus", ensemblName: "Mus_musculus"},
        {longName: "Rattus norvegicus", shortName: "rnorvegicus"},
        {longName: "Danio rerio", shortName: "drerio", ensembleName: "Danio_rerio"},
        {longName: "Drosophila melanogaster", shortName: "dmelanogaster", ensembleName: "Drosophila_melanogaster"},
        {longName: "Caenorhabditis elegans", shortName: "celegans", ensembleName: "Caenorhabditis_elegans"},
        {longName: "Saccharomyces cerevisiae", shortName: "scerevisiae", ensembleName: "Saccharomyces_cerevisiae"},
        {longName: "Canis familiaris", shortName: "cfamiliaris", ensembleName: "Canis_familiaris"},
        {longName: "Sus scrofa", shortName: "sscrofa", ensembleName: "Sus_scrofa", ensembleName: "Sus_scrofa"},
        {longName: "Anopheles gambiae", shortName: "agambiae"},
        {longName: "Plasmodium falciparum", shortName: "pfalciparum"}
    ];
    //tabs
    $scope.goToTab = function () {

        $(function () {
            $('#myTab a:first').tab('show')
        })
        $('#myTab a').click(function (e) {
            e.preventDefault()
            $(this).tab('show')
        })
    };
    //comunicate the new specie selected
    $scope.setSelectedSpecie = function (specie) {
        mySharedService.broadcastSpecie(specie);
    };
    $scope.example = function () {
        var specie = {longName: "Homo sapiens", shortName: "hsapiens", ensemblName: "Homo_sapiens"};
        mySharedService.broadcastExample(specie);
    };
    $scope.new = function () {
        var specie = {longName: "Homo sapiens", shortName: "hsapiens", ensemblName: "Homo_sapiens"};
        mySharedService.broadcastNew(specie);
    };
    //obtain the specie of genome viewer and take the complete information of the specie
    $scope.$on('newSpecieFromGenomeViewer', function () {

        for(var i in $scope.species){
            if($scope.species[i].longName == mySharedService.selectedSpecies){
                mySharedService.broadcastNew($scope.species[i]);
            }
        }
    });
}]);

optionsBarControl.$inject = ['$scope', 'mySharedService'];

