var regulationsSelect = myApp.controller('regulationsSelect', ['$scope', '$rootScope', 'mySharedService', 'CellbaseService', function ($scope, $rootScope, mySharedService, CellbaseService) {

    $scope.specie = mySharedService.regulationsSpecie;
    $scope.chromSelected = [];
    $scope.regions = "3:555-622666";
    $scope.listOfFeatureTypeFilters = [];
    $scope.featureClassFilter = [];
    $scope.chromNames = mySharedService.chromNames;

    $scope.typeOfData = "regulation";

    $scope.featureClassTypes = ["Histone", "Open Chromatin",  "Transcription Factor", "Polymerase", "microRNA" ];


    $scope.init = function(){
        $scope.deselectAllChrom();
        $scope.deselectAllFeatureClassFilter();
        $scope.chromSelected = [];
        $scope.regions = "";
        $scope.listOfFeatureTypeFilters = [];
        $scope.featureClassFilter = [];
    };
    //comunicate that a is a new result
    $scope.setResult = function () {
        mySharedService.broadcastRegulationsNewResult( $scope.chromSelected, $scope.regions,$scope.featureClassFilter);
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

        if($('#regulation'+chrom).hasClass("btn-primary")){
            $('#regulation'+chrom).removeClass("btn-primary");
        }
        else{
            $('#regulation'+chrom).addClass("btn-primary");
        }

    };
    $scope.addFeatureClassFilter = function (featureClass) {
        var pos = $scope.featureClassFilter.indexOf(featureClass);

        if (pos == -1) {
            $scope.featureClassFilter.push(featureClass);
        }
        else {
            $scope.featureClassFilter.splice(pos, 1);
        }


        if($("[id='"+featureClass+"']").hasClass("btn-primary")){
            $("[id='"+featureClass+"']").removeClass("btn-primary");
        }
        else{
            $("[id='"+featureClass+"']").addClass("btn-primary");
        }
    };


    $scope.selectAllChrom = function () {

        $('#regulationsChromMultiSelect').children().addClass("btn-primary");

        for (var i in $scope.chromNames) {
            $scope.chromSelected.push($scope.chromNames[i]);
        }


//        $('#regulationsChromMultiSelect').children().children().prop('checked', true);
//        for (var i in $scope.chromNames) {
//            $scope.chromSelected.push($scope.chromNames[i])
//        }
    };
    $scope.deselectAllChrom = function () {

        $('#regulationsChromMultiSelect').children().removeClass("btn-primary");
        $scope.chromSelected = [];

//        $scope.chromSelected = [];
//        $('#regulationsChromMultiSelect').children().children().prop('checked', false);
    };
    $scope.selectAllFeatureClassFilter = function () {

        $('#featureClassMultiSelect').children().children().addClass("btn-primary");

        for (var i in $scope.listOfFeatureTypeFilters) {
            $scope.featureClassFilter.push($scope.listOfFeatureTypeFilters[i]);
        }

//        $('#featureTypeMultiSelect').children().children().prop('checked', true);
//        for (var i in $scope.listOfFeatureTypeFilters) {
//            $scope.featureClassFilter.push($scope.listOfFeatureTypeFilters[i]);
//        }
    };
    $scope.deselectAllFeatureClassFilter = function () {

        $('#featureClassMultiSelect').children().children().removeClass("btn-primary");

        $scope.featureClassFilter = [];

//        $scope.featureClassFilter = [];
//        $('#featureTypeMultiSelect').children().children().prop('checked', false);
    };

    //-----------EVENTS---------------
    $scope.$on('clear', function () {
        $scope.init();
        $scope.setSpecie();
    });
    $scope.$on('newSpecie', function () {


        if(mySharedService.regulationsSpecie.shortName == "hsapiens"){
            $scope.init();
            $scope.setSpecie();

            if($scope.specie.shortName == "hsapiens"){
                $scope.regions = "3:555-622666";
            }

            $scope.setResult();
        }
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
        $scope.listOfFeatureTypeFilters = mySharedService.featureClassFilter;
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
