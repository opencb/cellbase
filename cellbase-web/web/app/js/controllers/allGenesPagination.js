var allGenesPagination = myApp.controller('allGenesPagination', ['$scope', 'mySharedService', 'CellbaseService', function ($scope, mySharedService, CellbaseService) {

    $scope.firstPagesGenesTable=false;
    $scope.previousPageGenesTable=false;
    $scope.thirdNumberGenesTable=true;
    $scope.nextPageGenesTable=true;
    $scope.lastPagesGenesTable=true;

    $scope.showPaginationGenesTables = false;
    $scope.paginationNumbersGenesTables = [1, 2, 3];

    $scope.maxNumberPaginationGenesTable;
    $scope.numDataPerPageGeneTable = 10;
    $scope.lastPageGeneTable = 1;

    $scope.disableFirstNumberGeneTable = false;
    $scope.disableSecondNumberGeneTable = false;
    $scope.disableThirdNumberGeneTable = false;

    $scope.paginationDataGeneTable=[];

    $scope.goToFirstPageGenesTable = function () {

        $scope.paginationNumbersGenesTables[0] = 1;
        $scope.paginationNumbersGenesTables[1] = 2;
        $scope.paginationNumbersGenesTables[2] = 3;

        $scope.firstPagesGenesTable = false;
        $scope.previousPageGenesTable = false;
        $scope.nextPageGenesTable = true;
        $scope.lastPagesGenesTable = true;

        $scope.disableAndEnablePaginationButtonsGenesTable(1);
        $scope.obtainPaginationLimitsGenesTable(1);
    };
    $scope.goToLastPageGenesTable = function () {

        console.log( $scope.maxNumberPaginationGenesTable);

        $scope.paginationNumbersGenesTables[0] = $scope.maxNumberPaginationGenesTable - 2;
        $scope.paginationNumbersGenesTables[1] = $scope.maxNumberPaginationGenesTable - 1;
        $scope.paginationNumbersGenesTables[2] = $scope.maxNumberPaginationGenesTable;

        $scope.firstPagesGenesTable = true;
        $scope.previousPageGenesTable = true;
        $scope.nextPageGenesTable = false;
        $scope.lastPagesGenesTable = false;

        $scope.disableAndEnablePaginationButtonsGenesTable($scope.maxNumberPaginationGenesTable);
        $scope.obtainPaginationLimitsGenesTable($scope.maxNumberPaginationGenesTable);
    };
    $scope.goPreviousPageGenesTable = function () {

        var page = $scope.lastPageGeneTable - 1;

        $scope.firstPagesGenesTable = true;
        $scope.previousPageGenesTable = true;
        $scope.nextPageGenesTable = true;
        $scope.lastPagesGenesTable = true;

        if (page == 1) {
            $scope.firstPagesGenesTable = false;
            $scope.previousPageGenesTable = false;

            $scope.paginationNumbersGenesTables[0] = 1;
            $scope.paginationNumbersGenesTables[1] = 2;
            $scope.paginationNumbersGenesTables[2] = 3;
        }

        else if ($scope.paginationNumbersGenesTables[0] != page && $scope.paginationNumbersGenesTables[1] != page && $scope.paginationNumbersGenesTables[2] != page) {
            $scope.paginationNumbersGenesTables[0] = page - 2;
            $scope.paginationNumbersGenesTables[1] = page - 1;
            $scope.paginationNumbersGenesTables[2] = page;
        }
        $scope.disableAndEnablePaginationButtonsGenesTable(page);
        $scope.obtainPaginationLimitsGenesTable(page);
    };
    $scope.goNextPageGenesTable = function () {

        var page = $scope.lastPageGeneTable + 1;

        $scope.firstPagesGenesTable = true;
        $scope.previousPageGenesTable = true;
        $scope.nextPageGenesTable = true;
        $scope.lastPagesGenesTable = true;


        if (page == $scope.maxNumberPaginationGenesTable) {
            $scope.nextPageGenesTable = false;
            $scope.lastPagesGenesTable = false;

            $scope.paginationNumbersGenesTables[0] = page - 2;
            $scope.paginationNumbersGenesTables[1] = page - 1;
            $scope.paginationNumbersGenesTables[2] = page;

        }
        else if ($scope.paginationNumbersGenesTables[0] != page && $scope.paginationNumbersGenesTables[1] != page && $scope.paginationNumbersGenesTables[2] != page) {
            $scope.paginationNumbersGenesTables[0] = page;
            $scope.paginationNumbersGenesTables[1] = page + 1;
            $scope.paginationNumbersGenesTables[2] = page + 2;
        }
        $scope.disableAndEnablePaginationButtonsGenesTable(page);
        $scope.obtainPaginationLimitsGenesTable(page);
    };
    $scope.goToNumberPageGenesTable = function (selectedPage) {

        if(!$scope.simplePaginationGenesTable){
            if (selectedPage == $scope.maxNumberPaginationGenesTable) {
                $scope.nextPageGenesTable = false;
                $scope.lastPagesGenesTable = false;
                $scope.firstPagesGenesTable = true;
                $scope.previousPageGenesTable = true;
            }
            else if (selectedPage == 1) {
                $scope.firstPagesGenesTable = false;
                $scope.previousPageGenesTable = false;
                $scope.nextPageGenesTable = true;
                $scope.lastPagesGenesTable = true;
            }
            else{
                $scope.firstPagesGenesTable = true;
                $scope.previousPageGenesTable = true;
                $scope.nextPageGenesTable = true;
                $scope.lastPagesGenesTable = true;
            }
        }
        console.log("selected page" + selectedPage);
        $scope.disableAndEnablePaginationButtonsGenesTable(selectedPage);
        $scope.obtainPaginationLimitsGenesTable(selectedPage);

    };

    $scope.disableAndEnablePaginationButtonsGenesTable = function (page) {
        if ($scope.paginationNumbersGenesTables[0] == page) {
            $scope.disableFirstNumberGeneTable = true;
            $scope.disableSecondNumberGeneTable = false;
            $scope.disableThirdNumberGeneTable = false;
        }
        else if ($scope.paginationNumbersGenesTables[1] == page) {
            $scope.disableSecondNumberGeneTable = true;
            $scope.disableFirstNumberGeneTable = false;
            $scope.disableThirdNumberGeneTable = false;
        }
        else {
            $scope.disableThirdNumberGeneTable = true;
            $scope.disableSecondNumberGeneTable = false;
            $scope.disableFirstNumberGeneTable = false;
        }
    };

    $scope.obtainPaginationLimitsGenesTable = function (page) {

        $scope.lastPageGeneTable = page;
        var ini = (page - 1) * $scope.numDataPerPageGeneTable;
        $scope.paginationDataGeneTable = [];
        var geneId;

        for (var i = ini; i < ini + $scope.numDataPerPageGeneTable; i++) {

            geneId = Object.keys($scope.genesAndTranscriptsData)[i];

            if(Object.keys($scope.genesAndTranscriptsData)[i] != null){
                $scope.paginationDataGeneTable.push($scope.genesAndTranscriptsData[geneId]);
            }
        }
    };

    $scope.simplePaginationGenesTable;

    $scope.initPaginationGenesTable = function () {


        $scope.paginationDataGeneTable = [];
        $scope.maxNumberPaginationGenesTable = Math.ceil(Object.keys($scope.genesAndTranscriptsData).length / $scope.numDataPerPageGeneTable);

        //  0 --> 10
        if (Object.keys($scope.genesAndTranscriptsData).length <= $scope.numDataPerPageGeneTable) {
            for (var i in $scope.genesAndTranscriptsData){
              $scope.paginationDataGeneTable.push($scope.genesAndTranscriptsData[i]);
            }

            $scope.showPaginationGenesTables = false;
        }
        // 11 --> 20
        else if(Object.keys($scope.genesAndTranscriptsData).length <= ($scope.numDataPerPageGeneTable * 2)){

            $scope.simplePaginationGenesTable = true;

            for(var i = 0; i< $scope.numDataPerPageGeneTable; i++){

                geneId = Object.keys($scope.genesAndTranscriptsData)[i];

                if(Object.keys($scope.genesAndTranscriptsData)[i] != null){
                    $scope.paginationDataGeneTable.push($scope.genesAndTranscriptsData[geneId]);
                }
            }

            $scope.showPaginationGenesTables = true;
            $scope.lastPageGeneTable = 1;

            $scope.disableFirstNumberGeneTable = true;
            $scope.disableSecondNumberGeneTable = false;
            $scope.disableThirdNumberGeneTable = false;

            $scope.firstPagesGenesTable = false;
            $scope.previousPageGenesTable = false;
            $scope.nextPageGenesTable = false;
            $scope.lastPagesGenesTable = false;

            $scope.thirdNumberGenesTable = false;
            $scope.paginationNumbersGenesTables = [1, 2];
        }
        // 21 --> ...
        else {
            console.log("en el else");
            $scope.simplePaginationGenesTable = false;
            var geneId;

            for(var i = 0; i< $scope.numDataPerPageGeneTable; i++){

                geneId = Object.keys($scope.genesAndTranscriptsData)[i];

                if(Object.keys($scope.genesAndTranscriptsData)[i] != null){
                    $scope.paginationDataGeneTable.push($scope.genesAndTranscriptsData[geneId]);
                }
            }
            $scope.firstPagesGenesTable = false;
            $scope.previousPageGenesTable = false;
            $scope.nextPageGenesTable = true;
            $scope.lastPagesGenesTable = true;

            $scope.thirdNumberGenesTable = true;
            $scope.paginationNumbersGenesTables = [1, 2, 3];
            $scope.showPaginationGenesTables = true;
            $scope.lastPageGeneTable = 1;

            $scope.disableFirstNumberGeneTable = true;
            $scope.disableSecondNumberGeneTable = false;
            $scope.disableThirdNumberGeneTable = false;
        }
    };

}]);


allGenesPagination.$inject = ['$scope', 'mySharedService'];

