//myApp.controller('resultPanelControl', ['$scope',function ($scope) {
var resultPanelControl = myApp.controller('resultPanelController', ['$scope','mySharedService','Server', function ($scope, mySharedService, Server) {

//function resultPanelController($scope,mySharedService){


    $scope.prueba=[1,2,3,4];


    $scope.species = ["Homo sapiens ","Mus musculus ","Rattus norvegicus ", "Danio rerio ","Drosophila melanogaster ","Caenorhabditis elegans ","Saccharomyces cerevisiae ","Canis familiaris ","Sus scrofa ","Anopheles gambiae ","Plasmodium falciparum"];
//    $scope.species = ["hsapiens","mmusculus","rnorvegicus","drerio","dmelanogaster","celegans","scerevisiae","cfamiliaris","sscrofa","agambiae","pfalciparum"]


    $scope.getSpecies = function () {
        return mySharedService.species;
    };

    $scope.selectedSpecie = "";

    $scope.setSelectedSpecie = function (specie) {

        $scope.selectedSpecie = specie;
        console.log( $scope.selectedSpecie);
    };

    $scope.$on('handleBroadcast', function () {
        $scope.message = mySharedService.message;
        console.log("result:   " + $scope.message);
    });

    $scope.$on('resultsBroadcast', function () {

        var data = Server.get(mySharedService.selectedSpecie,mySharedService.selectedRegions);
        console.log(data);

    });
//}
}]);

resultPanelControl.$inject = ['$scope','mySharedService'];



myApp.factory('Server', function ($http) {
    return {
        get: function(specie, regions) {

            console.log(regions);

            var dataGet;
            var host = 'http://ws-beta.bioinfo.cipf.es/cellbasebeta2/rest/v3/'

            $.ajax({
//                url: url,
                url: host + specie + '/genomic/region/' + regions + '/gene?exclude=transcripts&of=json',
                async: false,
                dataType: 'json',
                success: function (data, textStatus, jqXHR) {
                    dataGet=data[regions].result;
                },
                error: function (jqXHR, textStatus, errorThrown) {
                }
            });

            return dataGet;
//            return $http.get(url);

        }
    };
});