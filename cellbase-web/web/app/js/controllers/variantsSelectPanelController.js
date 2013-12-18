var variantsSelect = myApp.controller('variantsSelect', ['$scope', 'mySharedService', 'CellbaseService', function ($scope, mySharedService, CellbaseService) {

    $scope.specie = mySharedService.variantsSpecie;
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
        $scope.specie = mySharedService.variantsSpecie;
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

        if($('#variants'+chrom).hasClass("btn-primary")){
            $('#variants'+chrom).removeClass("btn-primary");
        }
        else{
            $('#variants'+chrom).addClass("btn-primary");
        }
    };

    $scope.addConseqTypeFilter = function (conseqType) {
        var pos = $scope.conseqTypesFilter.indexOf(conseqType);

        if (pos == -1) {
            $scope.conseqTypesFilter.push(conseqType);
        }
        else {
            $scope.conseqTypesFilter.splice(pos, 1);
        }

        if($('#'+conseqType).hasClass("btn-primary")){
            $('#'+conseqType).removeClass("btn-primary");
        }
        else{
            $('#'+conseqType).addClass("btn-primary");
        }
    };

    $scope.selectAllChrom = function () {

        $('#variantsChromMultiSelect').children().addClass("btn-primary");

        for (var i in $scope.chromNames) {
            $scope.chromSelected.push($scope.chromNames[i]);
        }

//        $('#variantsChromMultiSelect').children().children().prop('checked', true);
//        for (var i in $scope.chromNames) {
//            $scope.chromSelected.push($scope.chromNames[i])
//        }
    };
    $scope.deselectAllChrom = function () {

        $('#variantsChromMultiSelect').children().removeClass("btn-primary");
        $scope.chromSelected = [];

//        $scope.chromSelected = [];
//        $('#variantsChromMultiSelect').children().children().prop('checked', false);
    };
    $scope.selectAllConseqTypeFilter = function () {

        $('#conseqTypeMultiSelect').children().children().addClass("btn-primary");
        for (var i in $scope.listOfConseqTypes) {
            $scope.conseqTypesFilter.push($scope.listOfConseqTypes[i]);
        }

//        $('#conseqTypeMultiSelect').children().children().prop('checked', true);
//        for (var i in $scope.listOfConseqTypes) {
//            $scope.conseqTypesFilter.push($scope.listOfConseqTypes[i]);
//        }
    };
    $scope.deselectAllConseqTypeFilter = function () {

        $('#conseqTypeMultiSelect').children().children().removeClass("btn-primary");
        $scope.conseqTypesFilter = [];

//        $scope.conseqTypesFilter = [];
//        $('#conseqTypeMultiSelect').children().children().prop('checked', false);
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

    $scope.$on('variantsNewSpecieGV', function () {
        $scope.init();
        $scope.specie = mySharedService.variantsSpecieGV;
        $scope.chromNames = mySharedService.variantsChromNames;

        $scope.$apply();
//        $scope.setSpecie();
    });
    $scope.$on('variantsConseqTypes', function () {
        $scope.listOfConseqTypes = mySharedService.conseqTypes;
    });
    $scope.$on('variantsRegionGV', function () {
        $scope.specie = mySharedService.variantsSpecieGV;
        $scope.regions = mySharedService.regionFromGV;
        $scope.setResult();
        $scope.$apply();
    });

    //tabs
    $scope.goToTab = function () {
        $(function () {
            $('#variantsTabs a:first').tab('show')
        })
        $('#variantsTabs a').click(function (e) {
            e.preventDefault()
            $(this).tab('show')
        })
    };

}]);

variantsSelect.$inject = ['$scope', 'mySharedService'];
