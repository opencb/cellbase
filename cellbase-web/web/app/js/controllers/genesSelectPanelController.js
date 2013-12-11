var genesSelect = myApp.controller('genesSelect', ['$scope', 'mySharedService', 'CellbaseService', function ($scope, mySharedService, CellbaseService) {

    $scope.specie = mySharedService.selectedSpecies;
    $scope.chromSelected = [];
    $scope.regions = "20:32850000-33500000";
    $scope.listOfbiotypeFilters = [];
    $scope.genesIdFilter = "";
    $scope.biotypesFilter = [];


    $scope.chromNames = mySharedService.chromNames;

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
        mySharedService.broadcastGenesNewResult( $scope.chromSelected, $scope.regions, $scope.genesIdFilter, $scope.biotypesFilter);
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
        $('#genesChromMultiSelect').children().children().prop('checked', true);
        for (var i in $scope.chromNames) {
            $scope.chromSelected.push($scope.chromNames[i])
        }
    };
    $scope.deselectAllChrom = function () {
        $scope.chromSelected = [];
        $('#genesChromMultiSelect').children().children().prop('checked', false);
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

        var chromDiv = $('#genesChromMultiSelect').children().children();
        $scope.setResult();

//        console.log($scope.chromNames);
//        $scope.$apply();



        chromDiv[1].setAttribute("checked", "checked");
        chromDiv[19].setAttribute("checked", "checked");
    });
    $scope.$on('genesClear', function () {
        $scope.init();
        $scope.specie = mySharedService.genesChromNames;
//        $scope.setSpecie();
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
            $('#genesTabs a:first').tab('show')
        })
        $('#myTab a').click(function (e) {
            e.preventDefault()
            $(this).tab('show')
        })
    };
}]);

genesSelect.$inject = ['$scope', 'mySharedService'];
