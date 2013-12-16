var regulationsSelect = myApp.controller('regulationsSelect', ['$scope', 'mySharedService', 'CellbaseService', function ($scope, mySharedService, CellbaseService) {

    $scope.specie = mySharedService.regulationsSpecie;
    $scope.chromSelected = [];
    $scope.regions = "3:555-622666";
    $scope.listOfFeatureTypeFilters = [];
    $scope.featureClassFilter = "";
    $scope.featureTypesFilter = [];
    $scope.chromNames = mySharedService.chromNames;


    $scope.init = function(){
        $scope.deselectAllChrom();
        $scope.deselectAllFeatureTypeFilter();
        $scope.chromSelected = [];
        $scope.regions = "";
        $scope.listOfFeatureTypeFilters = [];
        $scope.featureClassFilter ="";
        $scope.featureTypesFilter = [];
    };
    //comunicate that a is a new result
    $scope.setResult = function () {
        mySharedService.broadcastRegulationsNewResult( $scope.chromSelected, $scope.regions, $scope.featureClassFilter, $scope.featureTypesFilter);
    };
    $scope.setSpecie = function(){
        $scope.specie = mySharedService.regulationsSpecie;
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
    $scope.addFeatureTypesFilter = function (biotype) {
        var pos = $scope.featureTypesFilter.indexOf(biotype);

        if (pos == -1) {
            $scope.featureTypesFilter.push(biotype);
        }
        else {
            $scope.featureTypesFilter.splice(pos, 1);
        }

    };
    $scope.selectAllChrom = function () {
        $('#regulationsChromMultiSelect').children().children().prop('checked', true);
        for (var i in $scope.chromNames) {
            $scope.chromSelected.push($scope.chromNames[i])
        }
    };
    $scope.deselectAllChrom = function () {
        $scope.chromSelected = [];
        $('#regulationsChromMultiSelect').children().children().prop('checked', false);
    };
    $scope.selectAllFeatureTypeFilter = function () {
        $('#featureTypeMultiSelect').children().children().prop('checked', true);
        for (var i in $scope.listOfFeatureTypeFilters) {
            $scope.featureTypesFilter.push($scope.listOfFeatureTypeFilters[i]);
        }
    };
    $scope.deselectAllFeatureTypeFilter = function () {
        $scope.featureTypesFilter = [];
        $('#featureTypeMultiSelect').children().children().prop('checked', false);
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
        $scope.regions = "3:555-622666";
        $scope.chromSelected = [];

        $scope.setResult();

    });
//    $scope.$on('regulationsNewSpecieGV', function () {
//        $scope.init();
//        $scope.specie = mySharedService.regulationsSpecieGV;
//        $scope.chromNames = mySharedService.regulationsChromNames;
//
//        if(!$scope.$$phase) {
//            //$digest or $apply
//            $scope.$apply();
//        }
//
////        $scope.setSpecie();
//    });
    $scope.$on('regulationsFeatureTypes', function () {
        $scope.listOfFeatureTypeFilters = mySharedService.featureTypesFilter;
    });
//    $scope.$on('regulationsRegionGV', function () {
//        $scope.specie = mySharedService.regulationsSpecieGV;
//        $scope.regions = mySharedService.regionFromGV;
//        $scope.setResult();
//
//        if(!$scope.$$phase) {
//            //$digest or $apply
//        $scope.$apply();
//        }
//    });
//
    //tabs
    $scope.goToTab = function () {
        $(function () {
            $('#regulationsTabs a:first').tab('show')
        })
        $('#regulationsTabs a').click(function (e) {
            e.preventDefault()
            $(this).tab('show')
        })
    };
}]);

regulationsSelect.$inject = ['$scope', 'mySharedService'];
