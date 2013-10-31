var optionsBarControl = myApp.controller('optionsBarController', ['$scope','mySharedService','Server', function ($scope, mySharedService, Server) {

    $scope.species = [
        {longName: "Homo sapiens", shortName:"hsapiens"},
        {longName: "Mus musculus", shortName:"mmusculus"},
        {longName: "Rattus norvegicus", shortName:"rnorvegicus"},
        {longName: "Danio rerio", shortName:"drerio"},
        {longName: "Drosophila melanogaster", shortName:"dmelanogaster"},
        {longName: "Caenorhabditis elegans", shortName:"celegans"},
        {longName: "Saccharomyces cerevisiae", shortName:"scerevisiae"},
        {longName: "Canis familiaris", shortName:"cfamiliaris"},
        {longName: "Sus scrofa", shortName:"sscrofa"},
        {longName: "Anopheles gambiae", shortName:"agambiae"},
        {longName: "Plasmodium falciparum", shortName:"pfalciparum"}
    ];

    $scope.setSelectedSpecie = function (specie) {
        mySharedService.broadcastSpecie(specie);
    };

}]);

optionsBarControl.$inject = ['$scope','mySharedService'];

