var regulationsResult = regulationsResultModule.controller('regulationsResult', ['$scope', 'mySharedService', 'CellbaseService', function ($scope, mySharedService, CellbaseService) {

    $scope.toggleTree = []; //array of booleans that will show of hide the elements of the tree

    $scope.regulationsData = []; //$scope.regulationsData = {};
//    $scope.paginationData = [];
//    $scope.featureTypes = [];
//
//    $scope.firstGeneId = "";
//    $scope.showAll = true;
//
//    $scope.showGenePanel = false;
//    $scope.showMoreAndLessGeneData = "+";
//    $scope.genePanelMore = false;
//    $scope.genePanelStatus = "-";
//
//    $scope.showTranscriptPanel = false;
//    $scope.showMoreAndLessTranscriptData = "+";
//    $scope.transcriptPanelMore = false;
//    $scope.transcriptPanelStatus = "-";

    $scope.showPagination = false;
    $scope.firstPages = false;
    $scope.previousPage = false;
    $scope.nextPage = true;
    $scope.lastPages = true;
    $scope.paginationNumbers = [1, 2, 3];
    $scope.maxNumberPagination;
    $scope.numDataPerPage = 9; //10;
    $scope.showPagination = false;
    $scope.lastPage = 1;
    $scope.disableFirstNumber = true;
    $scope.disableSecondNumber = false;
    $scope.disableThirdNumber = false;

    $scope.featureClassTypes = ["Histone", "Open Chromatin",  "Transcription Factor", "Polymerase", "microRNA" ];


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
//        $scope.collapseAllGenesTree();
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
        var geneId;
//
//        for (var i = ini; i < ini + $scope.numDataPerPage; i++) {
//            geneId = Object.keys($scope.regulationsData)[i];
//            if (Object.keys($scope.regulationsData)[i] != null) {
//                $scope.paginationData.push($scope.regulationsData[geneId]);
//            }
//        }

        for (var i = ini; i < ini + $scope.numDataPerPage; i++) {
            if ($scope.regulationsData[i] != null) {
                $scope.paginationData.push($scope.regulationsData[i]);
            }
        }

    };
    $scope.initPagination = function () {
        $scope.paginationData = [];
        $scope.maxNumberPagination = Math.ceil($scope.regulationsData.length / $scope.numDataPerPage);
//        $scope.maxNumberPagination = Math.ceil(Object.keys($scope.regulationsData).length / $scope.numDataPerPage);

        //  0 --> 9
//        if (Object.keys($scope.regulationsData).length <= $scope.numDataPerPage) {
        if ($scope.regulationsData.length <= $scope.numDataPerPage) {
            for (var i in $scope.regulationsData) {
                $scope.paginationData.push($scope.regulationsData[i]);
            }
            $scope.showPagination = false;
        }
        // 10 --> 18
//        else if (Object.keys($scope.regulationsData).length <= ($scope.numDataPerPage * 2)) {
        else if ($scope.regulationsData.length <= ($scope.numDataPerPage * 2)) {
            $scope.simplePagination = true;

            for (var i = 0; i < $scope.numDataPerPage; i++) {
                geneId = Object.keys($scope.regulationsData)[i];
                if (Object.keys($scope.regulationsData)[i] != null) {
                    $scope.paginationData.push($scope.regulationsData[geneId]);
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
        // 19 --> ...
        else {
            $scope.simplePagination = false;
//            var geneId;
//
//            for (var i = 0; i < $scope.numDataPerPage; i++) {
//                geneId = Object.keys($scope.regulationsData)[i];
//                if (Object.keys($scope.regulationsData)[i] != null) {
//                    $scope.paginationData.push($scope.regulationsData[geneId]);
//                }
//            }

            for (var i = 0; i < $scope.numDataPerPage; i++) {
                if ($scope.regulationsData[i] != null) {
                    $scope.paginationData.push($scope.regulationsData[i]);
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
//    $scope.clear = function () {
//        $scope.showGenePanel = false;
//        $scope.showTranscriptPanel = false;
//    };
    $scope.setResult = function(){
        $scope.featureClassFilters = mySharedService.featureClassFilter;
        $scope.selectedSpecie = mySharedService.regulationsSpecie;

        $scope.regulationsData = []; //$scope.regulationsData = {};


        var featureClassFilter = [];
        var arrayOfRegulations = [];



        //check if there are filters
//        if ($scope.featureTypeFilters.length != 0) {
//            arrayOfRegulations = CellbaseService.getGenesAndTranscripts($scope.selectedSpecie.shortName, mySharedService.regionsAndChromosomesGenes, $scope.featureTypeFilters);
//
//            for (var i in arrayOfRegulations) {
//                $scope.regulationsData[arrayOfRegulations[i].id] = arrayOfRegulations[i];
//            }
//        }
//        if ($scope.featureClassFilters.length != 0) {
//            featureClassFilter = CellbaseService.getGenesAndTranscriptsByIdOrName($scope.selectedSpecie.shortName, $scope.featureClassFilters);  //obtener los datos
//
//            $scope.checkGeneFilter(featureClassFilter)
//        }
//        //if there aren't any filters, show all genes data
        if ($scope.featureClassFilters.length == 0) {
//            arrayOfRegulations = CellbaseService.getAllRegulationsData($scope.selectedSpecie.shortName, mySharedService.regionsAndChromosomesRegulations, [], []);
            $scope.regulationsData= CellbaseService.getAllRegulationsData($scope.selectedSpecie.shortName, mySharedService.regionsAndChromosomesRegulations, []);


            $scope.separateFeatureClassTypes();


            //save the data in a hash table
//            for (var i in arrayOfRegulations) {
//                $scope.regulationsData[arrayOfRegulations[i].id] = arrayOfRegulations[i];
//            }
        }
        $scope.numResults = $scope.regulationsData.length; //$scope.numResults = arrayOfRegulations.length;
        $scope.initPagination();
//        $scope.clear();


//
        if($scope.numResults != 0){
            $scope.toggleTree = [];

            $scope.toggleTree.push(true);

            for(var i=1;i< 5; i++){
                $scope.toggleTree.push(false);
            }
            $scope.showAll = true;
//            $scope.firstGeneId = Object.keys($scope.regulationsData)[0];
//            $scope.lastDataShow = Object.keys($scope.regulationsData)[0];
//            $scope.selectedGene = CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, $scope.lastDataShow);
//            //show the informtion of the first gen
//            $scope.showSelectedGene(Object.keys($scope.regulationsData)[0], 0);
//
//            $scope.showTranscriptPanel = true;
//            $scope.selectedTranscript = $scope.selectedGene.transcripts[0];
        }
        else{
            alert("No results with this data");
////            alert("No correct data selected");
//            $scope.paginationData = [];
        }
    };
//    //save thee correct results and alert the incorrect
//    $scope.checkGeneFilter = function(featureClassFilter){
//        var genesIdError = [];
//        var featureClassFilters =  $scope.featureClassFilters.split(",");
//        var error = false;
//
//        for(var i in featureClassFilter){
//            if(featureClassFilter[i] == undefined){
//                genesIdError.push([i]);
//                error = true
//            }
//            else{
//                $scope.regulationsData[featureClassFilter[i].id] = (featureClassFilter[i]);
//            }
//        }
//        if(error){
//        var messageError = "";
//        if(genesIdError.length != 0){
//            messageError = genesIdError[0];
//            for(var i=1;i<genesIdError.length;i++){
//                messageError = messageError + ", " + genesIdError[i];
//            }
//        }
//        messageError = messageError + " incorrect";
//        alert(messageError);
//        }
//    };


    $scope.separateFeatureClassTypes = function () {

        $scope.histone = [];
        $scope.openChromatin = [];
        $scope.transcriptionFactor = [];
        $scope.polymerase = [];
        $scope.microRNA = [];


        $scope.dataNames={};

        $scope.dataNames.histone=[];
        $scope.dataNames.openChromatin=[];
        $scope.dataNames.transcriptionFactor=[];
        $scope.dataNames.polymerase=[];
        $scope.dataNames.microRNA=[];

        $scope.showHistoneNames = true;

        var pos;

        for(var i in $scope.regulationsData){
            if($scope.regulationsData[i].featureClass == "Histone")
            {
                $scope.histone.push($scope.regulationsData[i]);

                pos = $scope.dataNames.histone.indexOf($scope.regulationsData[i].name);
                if (pos == -1) {
                    $scope.dataNames.histone.push($scope.regulationsData[i].name);
                }

            }
            if($scope.regulationsData[i].featureClass == "Open Chromatin")
            {
                $scope.openChromatin.push($scope.regulationsData[i]);

                pos = $scope.dataNames.openChromatin.indexOf($scope.regulationsData[i].name);
                if (pos == -1) {
                    $scope.dataNames.openChromatin.push($scope.regulationsData[i].name);
                }
            }
            if($scope.regulationsData[i].featureClass == "Transcription Factor")
            {
                $scope.transcriptionFactor.push($scope.regulationsData[i]);

                pos = $scope.dataNames.transcriptionFactor.indexOf($scope.regulationsData[i].name);
                if (pos == -1) {
                    $scope.dataNames.transcriptionFactor.push($scope.regulationsData[i].name);
                }
            }
            if($scope.regulationsData[i].featureClass == "Polymerase")
            {
                $scope.polymerase.push($scope.regulationsData[i]);

                pos = $scope.dataNames.polymerase.indexOf($scope.regulationsData[i].name);
                if (pos == -1) {
                    $scope.dataNames.polymerase.push($scope.regulationsData[i].name);
                }

            }
            if($scope.regulationsData[i].featureClass == "microRNA")
            {
                $scope.microRNA.push($scope.regulationsData[i]);

                pos = $scope.dataNames.microRNA.indexOf($scope.regulationsData[i].name);
                if (pos == -1) {
                    $scope.dataNames.microRNA.push($scope.regulationsData[i].name);
                }
            }
        }

    };


//    //===================== tree events ========================
//    //show gen panel


    //-------------Show Type Info-----------------
    $scope.showHistoneInfo = function () {
        $scope.showTypeData(0,$scope.histone);
    };
    $scope.showOpenChromatinInfo = function () {
        $scope.showTypeData(1,$scope.openChromatin);
    };
    $scope.showTranscriptionFactorInfo = function () {
        $scope.showTypeData(2,$scope.transcriptionFactor);
    };
    $scope.showPolymeraseInfo = function () {
        $scope.showTypeData(3,$scope.polymerase);
    };
    $scope.showMicroRNAInfo = function () {
        $scope.showTypeData(4,$scope.microRNA);
    };
    $scope.showTypeData = function (index, data) {

        if($scope.toggleTree[index]){
            $scope.toggleTree[index] = false;
        }
        else{
            $scope.toggleTree[index] = true;
        }
        $scope.regulationsData = data;
        $scope.initPagination();
    };



    //--------------Show Name Info--------------
    $scope.showHistoneNameInfo = function (name) {
        $scope.showTypeNameData($scope.histone, name);
    };
    $scope.showOpenChromatinNamesInfo = function (name) {
        $scope.showTypeNameData($scope.openChromatin, name);
    };
    $scope.showTranscriptionFactorNamesInfo = function (name) {
        $scope.showTypeNameData($scope.transcriptionFactor, name);
    };
    $scope.showPolymeraseNamesInfo = function (name) {
        $scope.showTypeNameData($scope.polymerase, name);
    };
    $scope.showMicroRNANamesInfo = function (name) {
        $scope.showTypeNameData($scope.microRNA, name);
    };

    $scope.showTypeNameData = function (data, name) {
        $scope.regulationsData = [];

        for (var i in data){
            if(data[i].name == name){
                $scope.regulationsData.push(data[i]);
            }
        }
        $scope.initPagination();
    };


//    $scope.showSelectedType = function (type, index) {
//        if($scope.toggleTree[index]){
//            $scope.toggleTree[index] = false;
//        }
//        else{
//            $scope.toggleTree[index] = true;
//        }


//        if ($scope.lastDataShow != geneId) {
//            $scope.lastDataShow = geneId;
//            $scope.showGenePanel = true;
//            $scope.selectedGene = CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, geneId);
//
//
//            $scope.showTranscriptPanel = false;
//        }
//        else {
//            if (!$scope.showGenePanel) {
//                $scope.showGenePanel = true;
//            }
//        }
//        $scope.selectedTranscripts = $scope.selectedGene.transcripts;
//
//        mySharedService.broadcastGenesRegionToGV($scope.selectedGene.chromosome+":"+$scope.selectedGene.start+"-"+$scope.selectedGene.end);
//    };
    //show transcripts panel
    $scope.showSelectedRegulation = function (geneId, transcriptName) {
//        var transcripts;
//
//        if ($scope.lastDataShow != geneId) {
//            $scope.lastDataShow = geneId;
//            $scope.showGenePanel = false;
//            $scope.selectedGene = CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, geneId);
//        }
//        $scope.showTranscriptPanel = true;
//        transcripts = $scope.selectedGene.transcripts;
//        for (var i in transcripts) {
//            if (transcripts[i].name == transcriptName) {
//                $scope.selectedTranscript = transcripts[i];
//            }
//        }
//
//        mySharedService.broadcastGenesRegionToGV($scope.selectedTranscript.chromosome+":"+$scope.selectedTranscript.start+"-"+$scope.selectedTranscript.end);
//
    };
//
//    //show transcripts panel from transcripts table
//    $scope.showTanscriptFromTable = function (transcriptName) {
//        var transcripts = $scope.selectedGene.transcripts;
//        for (var i in transcripts) {
//            if (transcripts[i].name == transcriptName) {
//                $scope.selectedTranscript = transcripts[i];
//            }
//        }
//        $scope.transcriptInfo = false;
//        $scope.showTranscriptPanel = true;
//    };
//
//    $scope.expandAllGenesTree = function () {
//        for(var i in $scope.toggleTree){
//            $scope.toggleTree[i] = true;
//        }
//    };
//    $scope.collapseAllGenesTree = function () {
//        for(var i in $scope.toggleTree){
//            $scope.toggleTree[i] = false;
//        }
//    };
//
//    //show more info in gen panel
//    $scope.showMoreGeneData = function () {
//        $scope.genePanelMore = !$scope.genePanelMore;
//        if ($scope.showMoreAndLessGeneData == "+") {
//            $scope.showMoreAndLessGeneData = "-";
//        }
//        else {
//            $scope.showMoreAndLessGeneData = "+";
//        }
//    };
//    //show more info in transcript panel
//    $scope.showMoreTranscriptData = function () {
//        $scope.transcriptPanelMore = !$scope.transcriptPanelMore;
//        if ($scope.showMoreAndLessTranscriptData == "+") {
//            $scope.showMoreAndLessTranscriptData = "-";
//        }
//        else {
//            $scope.showMoreAndLessTranscriptData = "+";
//        }
//    };
//
//    //show/hide gen panel information
//    $scope.openCloseGenePanel = function () {
//        if ($scope.genePanelStatus == "+") {
//            $scope.genePanelStatus = "-";
//        }
//        else {
//            $scope.genePanelStatus = "+";
//        }
//    };
//    //show/hide transcript panel information
//    $scope.openCloseTranscriptPanel = function () {
//        if ($scope.transcriptPanelStatus == "+") {
//            $scope.transcriptPanelStatus = "-";
//        }
//        else {
//            $scope.transcriptPanelStatus = "+";
//        }
//    };
//
//    //genesResult div width is the rest of the document
//    $scope.getWidth = function () {
//        var resultPartWidth = $(document).width() - 220 - 260 - 60;
//
//        console.log(resultPartWidth);
//        return  {width: resultPartWidth}
//    };
//    //tabs
//    $scope.goToTab = function () {
//        $(function () {
//            $('#transcriptsTab a:first').tab('show')
//        })
//        $('#transcriptsTab a').click(function (e) {
//            e.preventDefault()
//            $(this).tab('show')
//        })
//    };
//
//    $scope.changeResultTab = function () {
//        $(function () {
//            $('#genesResultTab a:first').tab('show')
//        })
//        $('#genesResultTab a').click(function (e) {
//            e.preventDefault()
//            $(this).tab('show')
//        })
//    };
//
    //--------the initial result----------
    $scope.setResult();

    //--------------EVENTS-------------------
    $scope.$on('clear', function () {
        $scope.clearAll();
    });
//    $scope.$on('newSpecie', function () {
//        $scope.clearAll();
//    });
////    $scope.$on('genesNewSpecieGV', function () {
////        $scope.clearAll();
////    });
    $scope.$on('regulationsNewResult', function () {
        $scope.setResult();
    });

}]);

regulationsResult.$inject = ['$scope', 'mySharedService'];