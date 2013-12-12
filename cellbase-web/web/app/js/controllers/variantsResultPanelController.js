var variantsResult = myApp.controller('variantsResult', ['$scope', 'mySharedService', 'CellbaseService', function ($scope, mySharedService, CellbaseService) {
    $scope.toggleTree = [];
    $scope.snpData = {};
    $scope.paginationData = [];
    $scope.conseqTypes = [];

    $scope.firstVariantId = "";
    $scope.showAll = false;

    $scope.showVariantPanel = false;
    $scope.showMoreAndLessVariantData = "+";
    $scope.variantPanelMore = false;
    $scope.variantPanelStatus = "-";

    $scope.showTranscriptVarPanel = false;
    $scope.showMoreAndLessTranscriptVarData = "+";
    $scope.transcriptVarPanelMore = false;
    $scope.transcriptVarPanelStatus = "-";

    $scope.showPagination = false;
    $scope.firstPages = false;
    $scope.previousPage = false;
    $scope.nextPage = true;
    $scope.lastPages = true;
    $scope.paginationNumbers = [1, 2, 3];
    $scope.maxNumberPagination;
    $scope.numDataPerPage = 10;
    $scope.showPagination = false;
    $scope.lastPage = 1;
    $scope.disableFirstNumber = true;
    $scope.disableSecondNumber = false;
    $scope.disableThirdNumber = false;

    //========================Pagination==================================
    $scope.goToFirstPage = function () {
        $scope.paginationNumbers[0] = 1;
        $scope.paginationNumbers[1] = 2;
        $scope.paginationNumbers[2] = 3;

        $scope.firstPages = false;
        $scope.previousPage = false;
        $scope.nextPage = true;
        $scope.lastPages = true;

        $scope.disableAndEnablePaginationButtons(1);
        $scope.obtainPaginationLimits(1);
    };
    $scope.goToLastPage = function () {
        $scope.paginationNumbers[0] = $scope.maxNumberPagination - 2;
        $scope.paginationNumbers[1] = $scope.maxNumberPagination - 1;
        $scope.paginationNumbers[2] = $scope.maxNumberPagination;

        $scope.firstPages = true;
        $scope.previousPage = true;
        $scope.nextPage = false;
        $scope.lastPages = false;

        $scope.disableAndEnablePaginationButtons($scope.maxNumberPagination);
        $scope.obtainPaginationLimits($scope.maxNumberPagination);
    };
    $scope.goPreviousPage = function () {
        var page = $scope.lastPage - 1;

        $scope.firstPages = true;
        $scope.previousPage = true;
        $scope.nextPage = true;
        $scope.lastPages = true;

        if (page == 1) {
            $scope.firstPages = false;
            $scope.previousPage = false;

            $scope.paginationNumbers[0] = 1;
            $scope.paginationNumbers[1] = 2;
            $scope.paginationNumbers[2] = 3;
        }
        else if ($scope.paginationNumbers[0] != page && $scope.paginationNumbers[1] != page && $scope.paginationNumbers[2] != page) {
            $scope.paginationNumbers[0] = page - 2;
            $scope.paginationNumbers[1] = page - 1;
            $scope.paginationNumbers[2] = page;
        }
        $scope.disableAndEnablePaginationButtons(page);
        $scope.obtainPaginationLimits(page);
    };
    $scope.goNextPage = function () {
        var page = $scope.lastPage + 1;

        $scope.firstPages = true;
        $scope.previousPage = true;
        $scope.nextPage = true;
        $scope.lastPages = true;

        if (page == $scope.maxNumberPagination) {
            $scope.nextPage = false;
            $scope.lastPages = false;

            $scope.paginationNumbers[0] = page - 2;
            $scope.paginationNumbers[1] = page - 1;
            $scope.paginationNumbers[2] = page;
        }
        else if ($scope.paginationNumbers[0] != page && $scope.paginationNumbers[1] != page && $scope.paginationNumbers[2] != page) {
            $scope.paginationNumbers[0] = page;
            $scope.paginationNumbers[1] = page + 1;
            $scope.paginationNumbers[2] = page + 2;
        }
        $scope.disableAndEnablePaginationButtons(page);
        $scope.obtainPaginationLimits(page);
    };
    $scope.goToNumberPage = function (selectedPage) {
        if (!$scope.simplePagination) {
            if (selectedPage == $scope.maxNumberPagination) {
                $scope.nextPage = false;
                $scope.lastPages = false;
                $scope.firstPages = true;
                $scope.previousPage = true;
            }
            else if (selectedPage == 1) {
                $scope.firstPages = false;
                $scope.previousPage = false;
                $scope.nextPage = true;
                $scope.lastPages = true;
            }
            else {
                $scope.firstPages = true;
                $scope.previousPage = true;
                $scope.nextPage = true;
                $scope.lastPages = true;
            }
        }
        $scope.collapseAllVariantsTree();
        $scope.disableAndEnablePaginationButtons(selectedPage);
        $scope.obtainPaginationLimits(selectedPage);
    };
    $scope.disableAndEnablePaginationButtons = function (page) {
        if ($scope.paginationNumbers[0] == page) {
            $scope.disableFirstNumber = true;
            $scope.disableSecondNumber = false;
            $scope.disableThirdNumber = false;
        }
        else if ($scope.paginationNumbers[1] == page) {
            $scope.disableSecondNumber = true;
            $scope.disableFirstNumber = false;
            $scope.disableThirdNumber = false;
        }
        else {
            $scope.disableThirdNumber = true;
            $scope.disableSecondNumber = false;
            $scope.disableFirstNumber = false;
        }
    };
    $scope.obtainPaginationLimits = function (page) {
        $scope.lastPage = page;
        var ini = (page - 1) * $scope.numDataPerPage;
        $scope.paginationData = [];
        var variantId;

        for (var i = ini; i < ini + $scope.numDataPerPage; i++) {
            variantId = Object.keys($scope.snpData)[i];
            if (Object.keys($scope.snpData)[i] != null) {
                $scope.paginationData.push($scope.snpData[variantId]);
            }
        }
    };
    $scope.initPagination = function () {
        $scope.paginationData = [];
        $scope.maxNumberPagination = Math.ceil(Object.keys($scope.snpData).length / $scope.numDataPerPage);

        //  0 --> 10
        if (Object.keys($scope.snpData).length <= $scope.numDataPerPage) {
            for (var i in $scope.snpData) {
                $scope.paginationData.push($scope.snpData[i]);
            }
            $scope.showPagination = false;
        }
        // 11 --> 20
        else if (Object.keys($scope.snpData).length <= ($scope.numDataPerPage * 2)) {
            $scope.simplePagination = true;

            for (var i = 0; i < $scope.numDataPerPage; i++) {
                variantId = Object.keys($scope.snpData)[i];
                if (Object.keys($scope.snpData)[i] != null) {
                    $scope.paginationData.push($scope.snpData[variantId]);
                }
            }
            $scope.showPagination = true;
            $scope.lastPage = 1;

            $scope.disableFirstNumber = true;
            $scope.disableSecondNumber = false;
            $scope.disableThirdNumber = false;

            $scope.firstPages = false;
            $scope.previousPage = false;
            $scope.nextPage = false;
            $scope.lastPages = false;

            $scope.thirdNumber = false;
            $scope.paginationNumbers = [1, 2];
        }
        // 21 --> ...
        else {
            $scope.simplePagination = false;
            var variantId;

            for (var i = 0; i < $scope.numDataPerPage; i++) {
                variantId = Object.keys($scope.snpData)[i];
                if (Object.keys($scope.snpData)[i] != null) {
                    $scope.paginationData.push($scope.snpData[variantId]);
                }
            }
            $scope.firstPages = false;
            $scope.previousPage = false;
            $scope.nextPage = true;
            $scope.lastPages = true;

            $scope.thirdNumber = true;
            $scope.paginationNumbers = [1, 2, 3];
            $scope.showPagination = true;
            $scope.lastPage = 1;

            $scope.disableFirstNumber = true;
            $scope.disableSecondNumber = false;
            $scope.disableThirdNumber = false;
        }
    };


    $scope.clearAll = function(){
        $scope.showAll = false;
    };
    $scope.clear = function () {
        $scope.showVariantPanel = false;
        $scope.showTranscriptVarPanel = false;
    };
    $scope.setResult = function(){
        $scope.snpFilters = mySharedService.snpIdFilter;
        $scope.conseqTypesFilters = mySharedService.conseqTypesFilter;
        $scope.selectedSpecie = mySharedService.variantsSpecie;

        $scope.snpData = {};

        var snpFilter = [];
        var arrayOfSNP = [];

        //check if there are filters
        if ($scope.conseqTypesFilters.length != 0) {
            arrayOfSNP = CellbaseService.getAllSNPData($scope.selectedSpecie.shortName, mySharedService.regionsAndChromosomesVariants, $scope.conseqTypesFilters);

            for (var i in arrayOfSNP) {
                $scope.snpData[arrayOfSNP[i].id] = arrayOfSNP[i];
            }
        }
        if ($scope.snpFilters.length != 0) {
            snpFilter = CellbaseService.getVariantsDataById($scope.selectedSpecie.shortName, $scope.snpFilters);  //obtener los datos
            $scope.checkSNPFilter(snpFilter)
        }
        //if there aren't any filters, show all variants data
        if ($scope.conseqTypesFilters.length == 0 && $scope.snpFilters.length == 0) {


            arrayOfSNP = CellbaseService.getAllSNPData($scope.selectedSpecie.shortName, mySharedService.regionsAndChromosomesVariants, []);

            //save the data in a hash table
            for (var i in arrayOfSNP) {
                $scope.snpData[arrayOfSNP[i].id] = arrayOfSNP[i];
            }

            $scope.getConseqTypes();
        }

        $scope.numResults = Object.keys($scope.snpData).length;

        $scope.initPagination();
        $scope.clear();
        if($scope.numResults != 0){
            $scope.toggleTree = [];

            for(var i=0;i< 10; i++){
                $scope.toggleTree.push(false);
            }
            $scope.showAll = true;
            $scope.firstVariantId = Object.keys($scope.snpData)[0];
            $scope.lastDataShow = Object.keys($scope.snpData)[0];
            $scope.selectedVariant = CellbaseService.getVariantsDataById($scope.selectedSpecie.shortName, $scope.lastDataShow)[0];

            //show the informtion of the first variant
            $scope.showSelectedVariant(Object.keys($scope.snpData)[0], 0);

            $scope.showTranscriptVarPanel = true;
            $scope.selectedTranscriptVar = $scope.selectedVariant.transcriptVariations[0];
        }
        else{
            alert("No results with this data");
            $scope.paginationData = [];
        }
    };
    //save thee correct results and alert the incorrect
    $scope.checkSNPFilter = function(snpFilter){
        var snpIdError = [];
        var snpFilterss =  $scope.snpFilters.split(",");
        var error = false;

        for(var i in snpFilter){
            if(snpFilter[i] == undefined){
                snpIdError.push(snpFilterss[i]);
                error = true
            }
            else{
                $scope.snpData[snpFilter[i].id] = (snpFilter[i]);
            }
        }
        if(error){
            var messageError = "";
            if(snpIdError.length != 0){
                messageError = snpIdError[0];
                for(var i=1;i<snpIdError.length;i++){
                    messageError = messageError + ", " + snpIdError[i];
                }
            }
            messageError = messageError + " incorrect";
            alert(messageError);
        }
    };
    //obtain the list of the consequence types
    $scope.getConseqTypes = function () {
        $scope.conseqTypes = [];
        for (var i in $scope.snpData) {
            for(var j in $scope.snpData[i].transcriptVariations){
                for(var k in $scope.snpData[i].transcriptVariations[j].consequenceTypes){
                    if ($scope.conseqTypes.indexOf($scope.snpData[i].transcriptVariations[j].consequenceTypes[k]) == -1) {
                        $scope.conseqTypes.push($scope.snpData[i].transcriptVariations[j].consequenceTypes[k]);
                    }
                }
            }
        }

        mySharedService.broadcastVariantsConseqTypes($scope.conseqTypes);
    };
    //===================== tree events ========================
    //show variant panel
    $scope.showSelectedVariant = function (variantId, index) {
        if($scope.toggleTree[index]){
            $scope.toggleTree[index] = false;
        }
        else{
            $scope.toggleTree[index] = true;
        }

        if ($scope.lastDataShow != variantId) {
            $scope.lastDataShow = variantId;
            $scope.showVariantPanel = true;
            $scope.selectedVariant = CellbaseService.getVariantsDataById($scope.selectedSpecie.shortName, variantId)[0];

            $scope.showTranscriptVarPanel = false;
        }
        else {
            if (!$scope.showVariantPanel) {
                $scope.showVariantPanel = true;
            }
        }
        $scope.selectedTranscriptVar = $scope.selectedVariant.transcriptVariations;

        mySharedService.broadcastVariantsRegionToGV($scope.selectedVariant.chromosome+":"+$scope.selectedVariant.start+"-"+$scope.selectedVariant.end);
    };
    //show transcripts panel
    $scope.showSelectedTranscriptVar = function (variantId, transcriptId) {
        var transcripts;

        if ($scope.lastDataShow != variantId) {
            $scope.lastDataShow = variantId;
            $scope.showVariantPanel = false;
            $scope.selectedVariant = CellbaseService.getVariantsDataById($scope.selectedSpecie.shortName, variantId)[0];
        }
        $scope.showTranscriptVarPanel = true;

        for (var i in  $scope.selectedVariant.transcriptVariations) {
            if ($scope.selectedVariant.transcriptVariations[i].transcriptId == transcriptId) {
                $scope.selectedTranscriptVar = $scope.selectedVariant.transcriptVariations[i];
            }
        }
        mySharedService.broadcastVariantsRegionToGV($scope.selectedVariant.chromosome+":"+$scope.selectedVariant.start+"-"+$scope.selectedVariant.end);

    };

    //show transcripts panel from transcripts table
    $scope.showTanscriptVarFromTable = function (transcriptVarId) {

        for (var i in $scope.selectedVariant.transcriptVariations) {
            if ($scope.selectedVariant.transcriptVariations[i].transcriptId == transcriptVarId) {
                $scope.selectedTranscriptVar = $scope.selectedVariant.transcriptVariations[i];
            }
        }
        $scope.transcriptVarInfo = false;
        $scope.showTranscriptVarPanel = true;
    };

    $scope.expandAllVariantsTree = function () {
        for(var i in $scope.toggleTree){
            $scope.toggleTree[i] = true;
        }
    };
    $scope.collapseAllVariantsTree = function () {
        for(var i in $scope.toggleTree){
            $scope.toggleTree[i] = false;
        }
    };

    //show more info in variant panel
    $scope.showMoreVariantData = function () {
        $scope.variantPanelMore = !$scope.variantPanelMore;
        if ($scope.showMoreAndLessVariantData == "+") {
            $scope.showMoreAndLessVariantData = "-";
        }
        else {
            $scope.showMoreAndLessVariantData = "+";
        }
    };
    //show more info in transcript panel
    $scope.showMoreTranscriptVarData = function () {
        $scope.transcriptVarPanelMore = !$scope.transcriptVarPanelMore;
        if ($scope.showMoreAndLessTranscriptVarData == "+") {
            $scope.showMoreAndLessTranscriptVarData = "-";
        }
        else {
            $scope.showMoreAndLessTranscriptVarData = "+";
        }
    };
    //show/hide variant panel information
    $scope.openCloseVariantPanel = function () {
        if ($scope.variantPanelStatus == "+") {
            $scope.variantPanelStatus = "-";
        }
        else {
            $scope.variantPanelStatus = "+";
        }
    };
    //show/hide transcriptVar panel information
    $scope.openCloseTranscriptVarPanel = function () {
        if ($scope.transcriptVarPanelStatus == "+") {
            $scope.transcriptVarPanelStatus = "-";
        }
        else {
            $scope.transcriptVarPanelStatus = "+";
        }
    };

    //variantsResult div width is the rest of the document
    $scope.getWidth = function () {
        var resultPartWidth = $(document).width() - 220 - 260 - 60;

        console.log(resultPartWidth);
        return  {width: resultPartWidth}
    };

    //tabs
    $scope.goToTab = function () {

        $(function () {
            $('#transcriptsVarTab a:first').tab('show')
        })
        $('#transcriptsVarTab a').click(function (e) {
            e.preventDefault()
            $(this).tab('show')
        })
    };

    $scope.changeResultTab = function () {
        $(function () {
            $('#variantsResultTab a:first').tab('show')
        })
        $('#variantsResultTab a').click(function (e) {
            e.preventDefault()
            $(this).tab('show')
        })
    };

    $scope.setResult();

    //--------------EVENTS-------------------
    $scope.$on('clear', function () {
        $scope.clearAll();
    });
    $scope.$on('newSpecie', function () {
        $scope.clearAll();
    });
//    $scope.$on('variantsNewSpecieGV', function () {
//        $scope.clearAll();
//    });
    $scope.$on('variantsNewResult', function () {
        $scope.setResult();
    });


}]);

variantsResult.$inject = ['$scope', 'mySharedService'];

