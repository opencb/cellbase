var chromosomePanelControl = myApp.controller('chromosomePanelController', ['$scope', 'mySharedService', 'CellbaseService', function ($scope, mySharedService, CellbaseService) {

    $scope.showChromosome = false;
    $scope.allChromosomes = [];
    $scope.firstLoad = true;

    $scope.$on('newResult', function () {

        $scope.specie = mySharedService.selectedSpecies;


        if (mySharedService.regionsAndChromosomes != "") {

            $scope.regionsAndChromosomes = mySharedService.regionsAndChromosomes.split(",");


            var pos;
            $scope.allChromosomes = [];
            var addChrom = true;

            //obtain chromosomes from chromosomes and regions and remove the duplicates
            for (var i in $scope.regionsAndChromosomes) {
                pos = $scope.regionsAndChromosomes[i].search(":");
                if (pos == -1) {
                    $scope.allChromosomes.push($scope.regionsAndChromosomes[i]);
                }
                else {
                    //remove the duplicates
                    for (var j in $scope.allChromosomes) {
                        if ($scope.allChromosomes[j] == $scope.regionsAndChromosomes[i].substring(0, pos)) {
                            addChrom = false;
                        }
                    }
                    if (addChrom) {
                        $scope.allChromosomes.push($scope.regionsAndChromosomes[i].substring(0, pos));
                    }
                    addChrom = true;
                }
            }

            $scope.selectedChromosome = $scope.allChromosomes[0];

            if ($scope.firstLoad) {
                $scope.showChromosome = true;
                //create the div where the chromosome will be
                var chromosomeDiv = $('#chromosome-div')[0];
                var chrom = $('<div id="chromosome"></div>')[0];

                $(chromosomeDiv).append(chrom);

                $scope.drawChromosomePanel($(chrom).attr('id'));
                $scope.firstLoad = false;
            }
            else {
                $scope.chromosomePanel.setSpecies($scope.specie.shortName);
                $scope.showRegion($scope.selectedChromosome);
            }
        }

    });

    $scope.drawChromosomePanel = function (targetId) {

        $scope.chromosomePanelConfig = {
            collapsed: false,
            collapsible: true
        }

        CELLBASE_HOST = "http://ws-beta.bioinfo.cipf.es/cellbase/rest";
        CELLBASE_VERSION = "v3";

        $scope.chromosomePanel = new ChromosomePanel({
            targetId: targetId,
            autoRender: true,
            width: $(document).width() - 20,
            height: 65,
            species: $scope.specie.shortName,
            title: 'Chromosome',
            collapsed: $scope.chromosomePanelConfig.collapsed,
            collapsible: $scope.chromosomePanelConfig.collapsible,
            region: $scope.selectedChromosome + ":1-1",
            handlers: {
                'region:change': function (event) {
                    $scope.currentRegion = event.region.chromosome + ":" + event.region.start + "-" + event.region.end;
                }
            }
        });
        $($scope.chromosomePanel.titleDiv).remove();
        $($scope.chromosomePanel.collapseDiv).remove();

        $scope.chromosomePanel.draw();
    };

    $scope.showRegion = function (region) {

        //draw the begining of the chromosome
        var pos = region.search(":");

        if (pos == -1) {
            $scope.selectedChromosome = region;
            region = region + ":1-1";
        } else {
            $scope.selectedChromosome = region.substr(0, pos);
        }


        //add the chromosome if it doesn't exist
        var exist = false;
        for (var i in $scope.allChromosomes) {
            if ($scope.allChromosomes[i] == $scope.selectedChromosome) {
                exist = true;
            }
        }
        if (!exist) {
            $scope.allChromosomes.push($scope.selectedChromosome);
            exist = false;
        }

        $scope.chromosomePanel.setRegion(region);
        $scope.chromosomePanel.draw();
    };

    //add the new region to genesSelectPanel
    $scope.addRegion = function () {
        mySharedService.addRegionFromChromosome($scope.currentRegion);
    };

    //show the region in the input
    $scope.showRegionInInput = function () {
        $scope.currentRegion = $scope.currentRegion;
    };

}]);


chromosomePanelControl.$inject = ['$scope', 'mySharedService'];

