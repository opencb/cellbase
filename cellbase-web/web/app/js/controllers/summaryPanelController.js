var summaryPanelControl = myApp.controller('summaryPanelController', ['$scope','mySharedService','Server', function ($scope, mySharedService, Server) {
//myApp.controller('summaryPanelControl', ['$scope' , function ($scope, mySharedService, Server) {
//function summaryPanelController($scope,mySharedService, Server) {

    $scope.species = "";
    $scope.chromosomes = [];
    $scope.regions = [];

    //------------ejemplo----------------
    $scope.genesFilters=["ASIP", "ASIP-002"];
    $scope.biotypeFilters=["protein_coding"];
    //------------ejemplo----------------


    $scope.addRegion = function (region) {

        var comaPosition;

        //Miramos si añadimos varios elementos a la vez o solo uno
        if(region.search(",") != -1)
        {
            while(region.search(",") != -1)
            {
                comaPosition = region.search(",");
                $scope.regions.push(region.substring(0, comaPosition));
                region = region.substring(comaPosition+1 , region.length);
            }
            $scope.regions.push(region.substring(0, region.length));
        }
        else{
            $scope.regions.push(region);
        }
    };

    $scope.addChrom = function (chrom) {

        var comaPosition;

        //Miramos si añadimos varios elementos a la vez o solo uno
        if(chrom.search(",") != -1)
        {
            while(chrom.search(",") != -1)
            {
                comaPosition = chrom.search(",");
                $scope.chromosomes.push(chrom.substring(0, comaPosition));
                chrom = chrom.substring(comaPosition+1 , chrom.length);
            }
            $scope.chromosomes.push(chrom.substring(0, chrom.length));
        }
        else{
            $scope.chromosomes.push(chrom);
        }
    };

    $scope.newResults = function () {
        mySharedService.newResults($scope.getSpecieShortName($scope.specie), $scope.getRegions());
    };

    $scope.getRegions = function () {

        //------------------ guardamos solo los que no tengan region -----------
        //para luego descargarlos enteros
        var completeChromosome = true;
        var totalChromosomes = [];

        for (var i in $scope.chromosomes) {
            for (var j in  $scope.regions) {
                if ($scope.regions[j].substring(0, $scope.regions[j].search(":")) == $scope.chromosomes[i])
                    completeChromosome = false
            }

            if (completeChromosome)  //si no tiene region se decarga entero
            {
                totalChromosomes.push($scope.chromosomes[i]);
            }
            completeChromosome = true;
        }

        var completeRegion;
//        console.log(totalChromosomes.join() + "," + $scope.regions.join());
        if (totalChromosomes.length == 0) {
            completeRegion = $scope.regions.join();
        }
        else if ($scope.regions.length == 0) {
            completeRegion = totalChromosomes.join();
        }
        else {
            completeRegion = totalChromosomes.join() + "," + $scope.regions.join();
        }

        return completeRegion;
    }

    $scope.getSpecieShortName = function (specie){

        var specieShortName;

        if( specie == 'Homo sapiens') {specieShortName = "hsapiens";}
        if( specie == 'Mus musculus') specieShortName = 'mmusculus';
        if( specie == 'Rattus norvegicus')  specieShortName =  "rnorvegicus";
        if( specie == 'Danio rerio') specieShortName = "drerio";
        if( specie == 'Drosophila melanogaster') specieShortName = "dmelanogaster";
        if( specie == 'Caenorhabditis elegans') specieShortName = "celegans";
        if( specie == 'Saccharomyces cerevisiae') specieShortName = "scerevisiae";
        if( specie == 'Canis familiaris') specieShortName = "cfamiliaris";
        if( specie == 'Sus scrofa') specieShortName = "sscrofa";
        if( specie == 'Anopheles gambiae')specieShortName = "agambiae";
        if( specie == 'Plasmodium falciparum') specieShortName = "pfalciparum";

        return specieShortName
    }


    $scope.$on('specieBroadcast', function () {   //obtener la especie elegida en optionsBar
        $scope.species = mySharedService.selectedSpecies;
    });

    //----------------ejemplo---------------------
    $scope.$on('handleBroadcast', function () {
        $scope.message = mySharedService.message;
        console.log("summary:   " + $scope.message);
    });
    //--------------------------------------------

//}
}]);

summaryPanelControl.$inject = ['$scope','mySharedService'];
//summaryPanelController.$inject = ['$scope','mySharedService'];
