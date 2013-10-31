var summaryPanelControl = myApp.controller('summaryPanelController', ['$scope','mySharedService','Server', function ($scope, mySharedService, Server) {

    $scope.specie =  {longName: "Homo sapiens", shortName:"hsapiens"};
    $scope.chromosomes = "";
    $scope.regions = "20:32850000-33500000";

    //------------ejemplo----------------
    $scope.genesFilters=["ASIP", "ASIP-002"];
    $scope.biotypeFilters=["protein_coding"];
    //------------ejemplo----------------


    $scope.newResults = function () {
        mySharedService.newResults($scope.getRegions());
    };

    //obtenemos las regiones y cromosomas haciendo que si de un cromosoma tenemos una region, solo guardamos la region
    $scope.getRegions = function () {

        var completeChromosome = true;
        var totalChromosomes = [];


        //unimos los cromosomas a las regiones
        var completeRegion;

        if ($scope.chromosomes.length == 0) {
            completeRegion = $scope.regions;

        }
        else if ($scope.regions.length == 0) {
            completeRegion = $scope.chromosomes;
        }
        else {

            var chromosomes = $scope.chromosomes.split(",");
            var regions = $scope.regions.split(",");

            //almacenamos los cromosomas que no tengan region
            for (var i in chromosomes) {
                for (var j in regions) {

                    if (regions[j].substring(0, regions[j].search(":")) == chromosomes[i])
                        completeChromosome = false
                }

                if (completeChromosome)  //si no tiene region se decarga entero
                {
                    totalChromosomes.push(chromosomes[i]);
                }
                completeChromosome = true;
            }

            completeRegion = totalChromosomes.join() + "," + $scope.regions;

        }

        return completeRegion;
    }


    $scope.$on('specieBroadcast', function () {   //obtener la especie elegida en optionsBar
        $scope.specie = mySharedService.selectedSpecies;
    });

}]);

summaryPanelControl.$inject = ['$scope','mySharedService'];
//summaryPanelController.$inject = ['$scope','mySharedService'];
