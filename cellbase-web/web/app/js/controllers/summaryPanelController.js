var summaryPanelControl = myApp.controller('summaryPanelController', ['$scope','mySharedService','Server', function ($scope, mySharedService, Server) {

    $scope.specie =  {longName: "Homo sapiens", shortName:"hsapiens"};
    $scope.chromosomes = "";
    $scope.regions = "20:32850000-33500000";

    //------------ejemplo----------------
    $scope.genesFilters=["ASIP", "ASIP-002"];
    $scope.biotypeFilters=["protein_coding"];
    //------------ejemplo----------------



    $scope.chromosomesPerSpecie = {
        hsapiens: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","X","Y","MT"],
        mmusculus: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","X","Y","MT"],
        rnorvegicus: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","X","Y","MT"],
        drerio: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","X","Y","MT"],
        dmelanogaster: ["2L", "2LHet", "2R", "2RHet","3L", "3LHet", "3R", "3RHet","4", "U", "Uextra", "X","XHet", "YHet", "dmel_mitochondrion_genome"],
        celegans : ["I", "II", "III","IV","V","X","MtDNA"],
        scerevisiae: ["I", "II", "III","IV","V","VI", "VII", "VIII", "IX", "X","XI", "XII", "XIII", "XIV", "XV", "XVI", "Mito"],
        cfamiliaris: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","38","X","MT"],
        sscrofa: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","X","Y","MT"],
        agambiae: ["2L", "2R", "3L", "3R", "X"],
        pfalciparum: ["01","02","03","04","05","06","07","08","09","10","11","12","13","14"]
    };

    $scope.newResults = function () {
        mySharedService.newResults($scope.getRegions());
    };
    $scope.addChrom = function (chrom) {

        var pos = $scope.chromosomes.search(chrom)


        if(pos == -1){
//            $scope.chromosomes.concat(chrom);
            if($scope.chromosomes.length == 0)
            {
                $scope.chromosomes = chrom;
            }
            else{
                $scope.chromosomes = $scope.chromosomes + "," + chrom;
            }
        }
        else
        {
            $scope.chromosomes = $scope.chromosomes.replace("," + chrom, "");    //intentar eliminar el chromosoma
        }


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
