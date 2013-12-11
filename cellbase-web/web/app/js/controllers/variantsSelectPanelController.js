var variantsSelect = myApp.controller('variantsSelect', ['$scope', 'mySharedService', 'CellbaseService', function ($scope, mySharedService, CellbaseService) {

    $scope.specie = mySharedService.selectedSpecies;
    $scope.chromSelected = [];
    $scope.regions = "20:32850000-32860000";
    $scope.listOfConseqTypes = [];
    $scope.snpIdFilter = "";
    $scope.conseqTypesFilter = [];


    $scope.chromNames = mySharedService.chromNames;

    $scope.init = function(){
        $scope.deselectAllChrom();
        $scope.deselectAllConseqTypeFilter();
        $scope.chromSelected = [];
        $scope.regions = "";
        $scope.listOfConseqTypes = [];
        $scope.snpIdFilter ="";
        $scope.conseqTypesFilter = [];
    };
    //comunicate that a is a new result
    $scope.setResult = function () {
        mySharedService.broadcastVariantsNewResult($scope.chromSelected, $scope.regions, $scope.snpIdFilter, $scope.conseqTypesFilter);
    };

    $scope.setSpecie = function(){
        $scope.specie = mySharedService.selectedSpecies;
        $scope.chromSelected = [];
        $scope.chromNames = mySharedService.chromNames;
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
//    $scope.addRegion = function(){
//        if ($scope.regions.search(mySharedService.regionFromGV) == -1) {
//            if ($scope.regions.search(":") == -1) {  //if there isn't a region
//                $scope.regions = mySharedService.regionFromGV;
//            }
//            else {
//                $scope.regions = $scope.regions + "," + mySharedService.regionFromGV;
//            }
//        }
//        else {
//            alert(mySharedService.regionFromChromosome + " already exist");
//        }
//        $scope.setResult();
//    };
    $scope.addConseqTypeFilter = function (conseqType) {
        var pos = $scope.conseqTypesFilter.indexOf(conseqType);

        if (pos == -1) {
            $scope.conseqTypesFilter.push(conseqType);
        }
        else {
            $scope.conseqTypesFilter.splice(pos, 1);
        }
    };

    $scope.selectAllChrom = function () {
        $('#variantsChromMultiSelect').children().children().prop('checked', true);
        for (var i in $scope.chromNames) {
            $scope.chromSelected.push($scope.chromNames[i])
        }
    };
    $scope.deselectAllChrom = function () {
        $scope.chromSelected = [];
        $('#variantsChromMultiSelect').children().children().prop('checked', false);
    };
    $scope.selectAllConsewTypeFilter = function () {
        $('#conseqTypeMultiSelect').children().children().prop('checked', true);
        for (var i in $scope.listOfConseqTypes) {
            $scope.conseqTypesFilter.push($scope.listOfConseqTypes[i]);
        }
    };
    $scope.deselectAllConseqTypeFilter = function () {
        $scope.conseqTypesFilter = [];
        $('#conseqTypeMultiSelect').children().children().prop('checked', false);
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
        $scope.regions = "20:32850000-32860000";
        $scope.setResult();
    });
//    $scope.$on('genesClear', function () {
//        $scope.init();
//        $scope.setSpecie();
//    });
    $scope.$on('variantsConseqTypes', function () {
        $scope.listOfConseqTypes = mySharedService.conseqTypes;
    });
//    $scope.$on('genesRegionGV', function () {
//        $scope.addRegion();
//        $scope.$apply();
//    });

    //tabs
    $scope.goToTab = function () {
        $(function () {
            $('#variantsTabs a:first').tab('show')
        })
        $('#myTab a').click(function (e) {
            e.preventDefault()
            $(this).tab('show')
        })
    };

}]);

variantsSelect.$inject = ['$scope', 'mySharedService'];
