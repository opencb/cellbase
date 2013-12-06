var genesSelect = myApp.controller('genesSelect', ['$scope', 'mySharedService', 'CellbaseService', function ($scope, mySharedService, CellbaseService) {

    $scope.specie = mySharedService.selectedSpecies;
    $scope.chromSelected = [];
    $scope.regions = "20:32850000-33500000";
    $scope.listOfbiotypeFilters = [];
    $scope.genesIdFilter = "";
    $scope.biotypesFilter = [];

    $scope.chromAllData = CellbaseService.getSpecieChromosomes($scope.specie.shortName);
    $scope.chromNames = [];
    for (var i in $scope.chromAllData) {
        $scope.chromNames.push($scope.chromAllData[i].name);
    }
//    $scope.sortChromosomes():
    //prepare the format for the function sort
    for (var i in $scope.chromNames) {
        if (!isNaN($scope.chromNames[i])) {
            if ($scope.chromNames[i].length == 1) {
                $scope.chromNames[i] = 0 + $scope.chromNames[i];
            }
        }
    }
    $scope.chromNames = $scope.chromNames.sort();
    //quit the format
    for (var i in $scope.chromNames) {
        if ($scope.chromNames[i][0] == "0") {
            $scope.chromNames[i] = $scope.chromNames[i].replace("0", "");
        }
    }
    //homo sapiens has two Y chromosomes, so delete the last one
    if ($scope.specie.shortName == "hsapiens") {
        $scope.chromNames.pop();
    }


    $scope.init = function(){
        $scope.deselectAllChrom();
        $scope.deselectAllBiotypeFilter();
        $scope.chromSelected = [];
        $scope.regions = "";
        $scope.listOfbiotypeFilters = [];
        $scope.genesIdFilter ="";
        $scope.biotypeFilters = [];
    };
    //comunicate that a is a new result
    $scope.setResult = function () {
        if($scope.genesIdFilter != ""){
            $scope.genesIdFilter = $scope.removeSpaces($scope.genesIdFilter);
        }
        if ($scope.genesIdFilter == "" && $scope.biotypesFilter.length == 0 && $scope.chromSelected == [] && $scope.regions == "") {
            alert("No data selected");
        }
        else {
            mySharedService.broadcastGenesNewResult($scope.chromNames, $scope.mergeChromosomesAndRegions(), $scope.genesIdFilter, $scope.biotypesFilter);
        }
    };
    $scope.setSpecie = function(){
        $scope.specie = mySharedService.selectedSpecies;
        $scope.chromAllData = CellbaseService.getSpecieChromosomes($scope.specie.shortName);

        $scope.chromNames = [];
        for (var i in $scope.chromAllData) {
            $scope.chromNames.push($scope.chromAllData[i].name);
        }

        $scope.chromSelected = [];
        $scope.sortChromosomes();
        //homo sapiens has two Y chromosomes, so delete the last one
        if ($scope.specie.shortName == "hsapiens") {
            $scope.chromNames.pop();
        }
    };
    $scope.addChrom = function (chrom) {
        var pos = $scope.chromSelected.indexOf(chrom);

        if (pos == -1) {
            $scope.chromSelected.push(chrom);
        }
        else {
            $scope.chromSelected.splice(pos, 1);
        }
    };
    $scope.addRegion = function(){
        if ($scope.regions.search(mySharedService.regionFromGV) == -1) {
            if ($scope.regions.search(":") == -1) {  //if there isn't a region
                $scope.regions = mySharedService.regionFromGV;
            }
            else {
                $scope.regions = $scope.regions + "," + mySharedService.regionFromGV;
            }
        }
        else {
            alert(mySharedService.regionFromChromosome + " already exist");
        }
        $scope.setResult();
    };
    $scope.addBiotypeFilter = function (biotype) {
        var pos = $scope.biotypesFilter.indexOf(biotype);

        if (pos == -1) {
            $scope.biotypesFilter.push(biotype);
        }
        else {
            $scope.biotypesFilter.splice(pos, 1);
        }
    };

    $scope.selectAllChrom = function () {
        $('#ChromMultiSelect').children().children().prop('checked', true);
        for (var i in $scope.chromNames) {
            $scope.chromSelected.push($scope.chromNames[i])
        }
    };
    $scope.deselectAllChrom = function () {
        $scope.chromSelected = [];
        $('#ChromMultiSelect').children().children().prop('checked', false);
    };
    $scope.selectAllBiotypeFilter = function () {
        $('#BiotypesMultiSelect').children().children().prop('checked', true);
        for (var i in $scope.listOfbiotypeFilters) {
            $scope.biotypesFilter.push($scope.listOfbiotypeFilters[i]);
        }
    };
    $scope.deselectAllBiotypeFilter = function () {
        $scope.biotypesFilter = [];
        $('#BiotypesMultiSelect').children().children().prop('checked', false);
    };

    $scope.removeSpaces = function (data) {
        var espacio = data.search(" ");

        while (espacio != -1) {
            data = data.slice(0, espacio) + data.slice(espacio + 1, data.length);
            espacio = data.search(" ");
        }
        return data;
    };
    $scope.checkRegionInRange = function (chrom, start, end) {
        for (var i in $scope.chromAllData) {
            if ($scope.chromAllData[i].name == chrom) {
                if (start >= $scope.chromAllData[i].start && end <= $scope.chromAllData[i].end) {
                    return true;
                }
                else {
                    return false;
                }
            }
        }
    };
    //check if the regions are correctly added
    $scope.checkCorrectRegions = function () {
        var regions = $scope.regions.split(",");
        var correctRegions = [];
        var incorrectRegions = [];
        var chrom, start, end;
        var posDoublePoints, posLine;
        var correct = true;
        var chromExist = false;
        var messageError = "";

        for (var i in regions) {
            posDoublePoints = regions[i].search(":");
            posLine = regions[i].search("-");
            if (posDoublePoints == -1 || posLine == -1) {
                correct = false;
            }
            else {
                chrom = regions[i].slice(0, posDoublePoints);
                start = regions[i].slice(posDoublePoints + 1, posLine);
                end = regions[i].slice(posLine + 1, regions[i].length);

                //check if the chromosome exist
                for (var k in $scope.chromNames) {
                    if ($scope.chromNames[k] == chrom) {
                        chromExist = true;
                    }
                }
                if (!chromExist) {
                    correct = false;
                }
                else {
                    chromExist = false;

                    //check if start and end are numbers
                    if (isNaN(start) || isNaN(end)) {
                        correct = false;
                    }
                    else if (parseInt(start) > parseInt(end)) {
                        correct = false;
                    }
                    else {
                        //check if the region is in the range
                        if (!$scope.checkRegionInRange(chrom, start, end)) {
                            correct = false;
                            alert(regions[i] + " is out of range");
                        }
                    }
                }
            }
            if (correct) {
                correctRegions.push(regions[i]);
            }
            else {
                incorrectRegions.push(regions[i]);
                correct = true;
            }
        }
        if (incorrectRegions.length != 0) {
            messageError = incorrectRegions[0];

            for (var i = 1; i < incorrectRegions.length; i++) {
                messageError = messageError + ", " + incorrectRegions[i];
            }
            messageError = messageError + " incorrect";
            alert(messageError);
        }
        return correctRegions.join();
    };
    //merge the chromosomes and the regions with an AND
    $scope.mergeChromosomesAndRegions = function () {
        var completeChromosome = true;
        var totalChromosomes = [];
        var completeRegion;
        $scope.regions = $scope.removeSpaces($scope.regions);

        if ($scope.regions != "") {
            $scope.regions = $scope.checkCorrectRegions();
        }
        if ($scope.chromSelected.length == 0) {
            completeRegion = $scope.regions;
        }
        else if ($scope.regions.length == 0) {
            completeRegion = $scope.chromSelected.join();
        }
        else {
            //the variable $scope.regions has to be a sting to show it in an input, but for more facilities create an array with this information
            var regions = $scope.regions.split(",");

            //obtain the chromosomes that don't appear in a region
            for (var i in $scope.chromSelected) {
                for (var j in regions) {
                    if (regions[j].substring(0, regions[j].search(":")) == $scope.chromSelected[i])
                        completeChromosome = false
                }
                if (completeChromosome) {
                    totalChromosomes.push($scope.chromSelected[i]);
                }
                completeChromosome = true;
            }
            if (totalChromosomes.length == 0) {
                completeRegion = $scope.regions;
            }
            else {
                completeRegion = totalChromosomes.join() + "," + $scope.regions;
            }
        }
        return completeRegion;
    };
    //sort the chromosomes, to use the function sort, it has to put a zero in the left if the number have one digit
    $scope.sortChromosomes = function () {
        for (var i in $scope.chromNames) {
            if (!isNaN($scope.chromNames[i])) {
                if ($scope.chromNames[i].length == 1) {
                    $scope.chromNames[i] = 0 + $scope.chromNames[i];
                }
            }
        }
        $scope.chromNames = $scope.chromNames.sort();

        for (var i in $scope.chromNames) {
            if ($scope.chromNames[i][0] == "0") {
                $scope.chromNames[i] = $scope.chromNames[i].replace("0", "");
            }
        }
    };

    //-----------EVENTS---------------
    $scope.$on('clear', function () {
        $scope.init();
        $scope.setSpecie();
    });
    $scope.$on('newSpecie', function () {
        $scope.init();
        $scope.setSpecie();
    });
    $scope.$on('example', function () {
        $scope.init();
        $scope.setSpecie();
        $scope.regions = "20:32850000-33500000";
        $scope.chromSelected = ["2","20"];

        var chromDiv = $('#ChromMultiSelect').children().children();
        chromDiv[1].setAttribute("checked", "checked");
        chromDiv[19].setAttribute("checked", "checked");
        $scope.setResult();
    });
    $scope.$on('genesClear', function () {
        $scope.init();
        $scope.setSpecie();
    });
    $scope.$on('genesBiotypes', function () {
        $scope.listOfbiotypeFilters = mySharedService.biotypes;
    });
    $scope.$on('genesRegionGV', function () {
        $scope.addRegion();
        $scope.$apply();
    });

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
}]);

genesSelect.$inject = ['$scope', 'mySharedService'];
