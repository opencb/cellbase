//myApp.controller('optionsBarControl', ['$scope',function ($scope) {
var optionsBarControl = myApp.controller('optionsBarController', ['$scope','mySharedService','Server', function ($scope, mySharedService, Server) {
//function optionsBarController($scope, mySharedService) {

    $scope.species = {};
    $scope.species.longName = ["Homo sapiens","Mus musculus","Rattus norvegicus", "Danio rerio","Drosophila melanogaster","Caenorhabditis elegans","Saccharomyces cerevisiae","Canis familiaris","Sus scrofa","Anopheles gambiae","Plasmodium falciparum"];
    $scope.species.shortName = ["hsapiens","mmusculus","rnorvegicus","drerio","dmelanogaster","celegans","scerevisiae","cfamiliaris","sscrofa","agambiae","pfalciparum"];



//    $scope.species = ["Homo sapiens ","Mus musculus ","Rattus norvegicus ", "Danio rerio ","Drosophila melanogaster ","Caenorhabditis elegans ","Saccharomyces cerevisiae ","Canis familiaris ","Sus scrofa ","Anopheles gambiae ","Plasmodium falciparum"];

//    $scope.species = ["hsapiens","mmusculus","rnorvegicus","drerio","dmelanogaster","celegans","scerevisiae","cfamiliaris","sscrofa","agambiae","pfalciparum"]


    $scope.getSpecies = function () {
        return mySharedService.species;
    };

//    $scope.selectedSpecie = "";

    $scope.setSelectedSpecie = function (specie) {

//        $scope.selectedSpecie = specie;
//        mySharedService.specieSelected =  specie;
        mySharedService.broadcastSpecie(specie);

    };




    //------------------------------------------------------------------
    $scope.prueba = function (index) {
        console.log("clickado el boton:"+ index);
    };

    $scope.prueba3 = function () {

//        console.log($scope.summaryPanelControl.specie);
        console.log("pulsado boton");
    };

    //cuando clickamos en el botons, esto coge el valor que le pasamos desde el html y llama a la funcion
    //compartida para que lo pase a los demas
    $scope.handleClick = function(msg){
        mySharedService.prepForBroadcast(msg);
    }

    $scope.$on('handleBroadcast', function () {
        $scope.message = mySharedService.message;
        console.log("optionsBar:   " + $scope.message);
    });






//}
}]);

    optionsBarControl.$inject = ['$scope','mySharedService'];

