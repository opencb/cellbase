var variantsSelect = myApp.controller('variantsSelect', ['$scope', 'mySharedService', 'CellbaseService', function ($scope, mySharedService, CellbaseService) {

//    $scope.specie = {longName: "Homo sapiens", shortName: "hsapiens", ensemblName: "Homo_sapiens"};
//    $scope.chromosomes = [];
//    $scope.chromosomesAllData = CellbaseService.getSpecieChromosomes($scope.specie.shortName);
//    $scope.chromNames = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "MT"];
//
//    $scope.regions = "20:32850000-33500000";
//
//    $scope.genesIdFilter = "";
//    $scope.biotypesFilter = [];
//    $scope.listOfbiotypeFilters = [];
//
//    //obtain the name of the chromosome got by cellbase
//    $scope.obtainChromosomesInfo = function () {
//
//        $scope.chromNames = [];
//        for (var i in $scope.chromosomesAllData) {
//            $scope.chromNames.push($scope.chromosomesAllData[i].name);
//        }
//    };
//
//    $scope.addChrom = function (chrom) {
//
//        var pos = $scope.chromosomes.indexOf(chrom);
//
//        if (pos == -1) {
//            $scope.chromosomes.push(chrom);
//        }
//        else {
//            $scope.chromosomes.splice(pos, 1);
//        }
//    };
//
//    $scope.addBiotypeFilter = function (biotype) {
//
//        var pos = $scope.biotypesFilter.indexOf(biotype);
//
//
//        if (pos == -1) {
//            $scope.biotypesFilter.push(biotype);
//        }
//        else {
//            $scope.biotypesFilter.splice(pos, 1);
//        }
//
//    };
//
//    $scope.selectAllChrom = function () {
//
//        for (var i in $scope.chromNames) {
//            $scope.chromosomes.push($scope.chromNames[i])
//        }
//    };
//    $scope.deselectAllChrom = function () {
//        $scope.chromosomes = [];
//    };
//
//    $scope.selectAllBiotypeFilter = function () {
//
//        for (var i in $scope.listOfbiotypeFilters) {
//            $scope.biotypesFilter.push($scope.listOfbiotypeFilters[i]);
//        }
//    };
//
//    $scope.deselectAllBiotypeFilter = function () {
//        $scope.biotypesFilter = [];
//    };

    //comunicate that a is a new result
    $scope.newResult = function () {

//        $scope.genesIdFilter = $scope.removeSpaces($scope.genesIdFilter);
//
//        if ($scope.genesIdFilter == "" && $scope.biotypesFilter.length == 0 && $scope.chromosomes && $scope.regions == "") {
//            alert("No data selected");
//        }
//        else {
//            mySharedService.newResult($scope.chromNames, $scope.mergeChromosomesAndRegions(), $scope.genesIdFilter, $scope.biotypesFilter);
//        }

    };

//    $scope.removeSpaces = function (data) {
//
//        var espacio = data.search(" ");
//
//        while (espacio != -1) {
//            data = data.slice(0, espacio) + data.slice(espacio + 1, data.length);
//            espacio = data.search(" ");
//        }
//        return data;
//    };
//
//
//    $scope.checkRegionInRange = function (chrom, start, end) {
//
//        for (var i in $scope.chromosomesAllData) {
//
//            if ($scope.chromosomesAllData[i].name == chrom) {
//
//                if (start >= $scope.chromosomesAllData[i].start && end <= $scope.chromosomesAllData[i].end) {
//                    return true;
//                }
//                else {
//                    return false;
//                }
//            }
//        }
//
//    };
//
//
//    //check if the regions are correctly added
//    $scope.checkRegions = function () {
//
//        var regions = $scope.regions.split(",");
//        var correctRegions = [];
//        var incorrectRegions = [];
//
//        var chrom, start, end;
//        var posDoublePoints, posLine;
//
//        var correct = true;
//        var chromExist = false;
//
//        var messageError = "";
//
//        for (var i in regions) {
//            posDoublePoints = regions[i].search(":");
//            posLine = regions[i].search("-");
//
//            if (posDoublePoints == -1 || posLine == -1) {
//                correct = false;
//            }
//            else {
//                chrom = regions[i].slice(0, posDoublePoints);
//                start = regions[i].slice(posDoublePoints + 1, posLine);
//                end = regions[i].slice(posLine + 1, regions[i].length);
//
//                //check if the chromosome exist
//                for (var k in $scope.chromNames) {
//                    if ($scope.chromNames[k] == chrom) {
//                        chromExist = true;
//                    }
//                }
//                if (!chromExist) {
//                    correct = false;
//                }
//                else {
//                    chromExist = false;
//
//                    //check if start and end are numbers
//                    if (isNaN(start) || isNaN(end)) {
//                        correct = false;
//                    }
//                    else if (parseInt(start) > parseInt(end)) {
//                        correct = false;
//                    }
//                    else {
//                        //check if the region is in the range
//                        if (!$scope.checkRegionInRange(chrom, start, end)) {
//                            correct = false;
//                            alert(regions[i] + " is out of range");
//                        }
//                    }
//
//                }
//            }
//
//            if (correct) {
//                correctRegions.push(regions[i]);
//            }
//            else {
//                incorrectRegions.push(regions[i]);
//                correct = true;
//            }
//
//        }
//
//        if (incorrectRegions.length != 0) {
//
//
//            messageError = incorrectRegions[0];
//
//            for (var i = 1; i < incorrectRegions.length; i++) {
//                messageError = messageError + ", " + incorrectRegions[i];
//            }
//
//            messageError = messageError + " incorrect";
//
////            var lastComa =  messageError.lastIndexOf(",");
////            messageError = messageError.slice(0,lastComa) + messageError.slice(lastComa+1, messageError.length);
//
//            alert(messageError);
//        }
//
//        return correctRegions.join();
//    };
//
//
//    //merge the chromosomes and the regions with an AND
//    $scope.mergeChromosomesAndRegions = function () {
//
//        var completeChromosome = true;
//        var totalChromosomes = [];
//        var completeRegion;
//
//        $scope.regions = $scope.removeSpaces($scope.regions);
//
//        if ($scope.regions != "") {
//            $scope.regions = $scope.checkRegions();
//        }
//
//        if ($scope.chromosomes.length == 0) {
//            completeRegion = $scope.regions;
//
//        }
//        else if ($scope.regions.length == 0) {
//            completeRegion = $scope.chromosomes.join();
//        }
//        else {
//
//            //the variable $scope.regions has to be a sting to show it in an input, but for more facilities create an array with this information
//            var regions = $scope.regions.split(",");
//
//            //obtain the chromosomes that don't appear in a region
//            for (var i in $scope.chromosomes) {
//                for (var j in regions) {
//
//                    if (regions[j].substring(0, regions[j].search(":")) == $scope.chromosomes[i])
//                        completeChromosome = false
//                }
//
//                if (completeChromosome) {
//                    totalChromosomes.push($scope.chromosomes[i]);
//                }
//                completeChromosome = true;
//            }
//
//            if (totalChromosomes.length == 0) {
//                completeRegion = $scope.regions;
//            }
//            else {
//                completeRegion = totalChromosomes.join() + "," + $scope.regions;
//            }
//
//        }
//
//        return completeRegion;
//    };
//
//
//    //sort the chromosomes, to use the function sort, it has to put a zero in the left if the number have one digit
//    $scope.sortChromosomes = function () {
//
//        for (var i in $scope.chromNames) {
//            if (!isNaN($scope.chromNames[i])) {  //es un numero
//                if ($scope.chromNames[i].length == 1) {
//                    $scope.chromNames[i] = 0 + $scope.chromNames[i];
//                }
//            }
//        }
//
//        $scope.chromNames = $scope.chromNames.sort();
//
//        //se quitan los ceros
//        for (var i in $scope.chromNames) {
//            if ($scope.chromNames[i][0] == "0") {
//                $scope.chromNames[i] = $scope.chromNames[i].replace("0", "");
//            }
//        }
//
//    };
//
//    $scope.$on('newSpecie', function () {
//        $scope.specie = mySharedService.selectedSpecies;
//
//        $scope.chromosomesAllData = CellbaseService.getSpecieChromosomes($scope.specie.shortName);
//
//        $scope.obtainChromosomesInfo();
//
//        $scope.chromosomes = [];
//        $scope.chromosomesToShow = "";
//
//        $scope.sortChromosomes();
//
//
//        //homo sapiens has two Y chromosomes, so delete the last one
//        if ($scope.specie.shortName == "hsapiens") {
//            $scope.chromNames.pop();
//        }
//
//    });
//
//    $scope.$on('biotypes', function () {   //obtener la especie elegida en optionsBar
//        $scope.listOfbiotypeFilters = mySharedService.biotypes;
//    });
//
//    //put the new region obtained by the chromosome drawn
//    $scope.$on('newRegion', function () {
//
//        if ($scope.regions.search(mySharedService.regionFromChromosome) == -1) {
//
//            if ($scope.regions.search(":") == -1) {
//                $scope.regions = mySharedService.regionFromChromosome;
//            }
//            else {
//                $scope.regions = $scope.regions + "," + mySharedService.regionFromChromosome;
//            }
//        }
//        else {
//            alert(mySharedService.regionFromChromosome + " already exist");
//        }
//
//    });
//
//    //if the chromosome has been selected, mark it
//    $scope.getChromosomesColor = function (chrom) {
//
//        if ($scope.chromosomes.indexOf(chrom) != -1) {
//            return  {"background-color": "lightblue"};
////          return  {"background-color": "lightblue","font-weight": "bold"};
//        }
//        else {
//            return  {"background-color": "white"};
//        }
//    };
//
//    //if the biotype has been selected, mark it
//    $scope.getBiotypesColor = function (biotype) {
//
//        if ($scope.biotypesFilter.indexOf(biotype) != -1) {
//            return  {"background-color": "lightblue"};
////          return  {"background-color": "lightblue",  "font-weight": "bold" };
//        }
//        else {
//            return  {"background-color": "white"};
//        }
//    };
//
//    //tabs
//    $scope.goToTab = function () {
//        $(function () {
//            $('#myTab a:first').tab('show')
//        })
//
//        $('#myTab a').click(function (e) {
//            e.preventDefault()
//            $(this).tab('show')
//        })
//    };

}]);

variantsSelect.$inject = ['$scope', 'mySharedService'];
