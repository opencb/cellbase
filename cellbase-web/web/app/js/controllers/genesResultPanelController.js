var genesResult = myApp.controller('genesResult', ['$scope', 'mySharedService', 'CellbaseService', function ($scope, mySharedService, CellbaseService) {


    $scope.toggleTree = [];


    $scope.genesAndTranscriptsData = {};
    $scope.genesAllData = [];
    $scope.paginationData = [];

    $scope.firstGeneId = "";
    $scope.showAll = false;

    //--------pagination--------------
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

        for (var i = ini; i < ini + $scope.numDataPerPage; i++) {
            geneId = Object.keys($scope.genesAndTranscriptsData)[i];
            if (Object.keys($scope.genesAndTranscriptsData)[i] != null) {
                $scope.paginationData.push($scope.genesAndTranscriptsData[geneId]);
            }
        }
    };
    $scope.initPagination = function () {
        $scope.paginationData = [];
        $scope.maxNumberPagination = Math.ceil(Object.keys($scope.genesAndTranscriptsData).length / $scope.numDataPerPage);

        //  0 --> 10
        if (Object.keys($scope.genesAndTranscriptsData).length <= $scope.numDataPerPage) {
            for (var i in $scope.genesAndTranscriptsData) {
                $scope.paginationData.push($scope.genesAndTranscriptsData[i]);
            }
            $scope.showPagination = false;
        }
        // 11 --> 20
        else if (Object.keys($scope.genesAndTranscriptsData).length <= ($scope.numDataPerPage * 2)) {
            $scope.simplePagination = true;

            for (var i = 0; i < $scope.numDataPerPage; i++) {

                geneId = Object.keys($scope.genesAndTranscriptsData)[i];

                if (Object.keys($scope.genesAndTranscriptsData)[i] != null) {
                    $scope.paginationData.push($scope.genesAndTranscriptsData[geneId]);
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
            //indicamos que los primeros datos a mostrar son los de la pagina 1
            var geneId;

            for (var i = 0; i < $scope.numDataPerPage; i++) {
                geneId = Object.keys($scope.genesAndTranscriptsData)[i];
                if (Object.keys($scope.genesAndTranscriptsData)[i] != null) {
                    $scope.paginationData.push($scope.genesAndTranscriptsData[geneId]);
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
    $scope.clearPanelsAndTables = function () {
        $scope.showGenesTable = false;
        $scope.showGenePanel = false;
        $scope.showTranscriptPanel = false;
    };
    $scope.$on('newResult', function () {

        $scope.newResult();
    });



    $scope.newResult = function(){
        $scope.genesFilters = mySharedService.genesIdFilter;
        $scope.biotypeFilters = mySharedService.biotypeFilter;
        $scope.selectedSpecie = mySharedService.selectedSpecies;

        $scope.genesAndTranscriptsData = {};

        var genesIdFilter = [];
        var arrayOfGenes = [];

        //check if there are filters
        if ($scope.biotypeFilters.length != 0) {
            arrayOfGenes = CellbaseService.getGenesAndTranscripts($scope.selectedSpecie.shortName, mySharedService.regionsAndChromosomes, $scope.biotypeFilters);

            for (var i in arrayOfGenes) {
                $scope.genesAndTranscriptsData[arrayOfGenes[i].id] = arrayOfGenes[i];
            }
        }
        if ($scope.genesFilters.length != 0) {
            genesIdFilter = CellbaseService.getGenesAndTranscriptsByIdOrName($scope.selectedSpecie.shortName, $scope.genesFilters);  //obtener los datos
            //save thee correct results and alert the incorrect
            $scope.getCorrectDataFilterPerGene(genesIdFilter)
        }
        //if there aren't any filters, show all genes data
        if ($scope.biotypeFilters.length == 0 && $scope.genesFilters.length == 0) {
            arrayOfGenes = CellbaseService.getGenesAndTranscripts($scope.selectedSpecie.shortName, mySharedService.regionsAndChromosomes, []);
            //save the data in a hash table
            for (var i in arrayOfGenes) {
                $scope.genesAndTranscriptsData[arrayOfGenes[i].id] = arrayOfGenes[i];
            }
            $scope.getBiotypes();
        }
        $scope.numResults = Object.keys($scope.genesAndTranscriptsData).length;
        $scope.initPagination();
        $scope.clearPanelsAndTables();
//            $scope.initPaginationGenesTable();

        if($scope.numResults != 0){

            $scope.toggleTree = [];

            for(var i=0;i< 10; i++){
                $scope.toggleTree.push(false);
            }




            $scope.showAll = true;

            $scope.firstGeneId = Object.keys($scope.genesAndTranscriptsData)[0];
            $scope.lastDataShow = Object.keys($scope.genesAndTranscriptsData)[0];
            $scope.selectedGen = CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, $scope.lastDataShow);


            //show the informtion of the first gene
            $scope.geneSelected(Object.keys($scope.genesAndTranscriptsData)[0], 0);

//            $scope.$apply();
        }
        else{
            alert("No correct data selected");
            $scope.paginationData = [];
        }
    };


//    $scope.$watch('genesAndTranscriptsData', function() {
//        // do something here
//    }, true);



    $scope.getCorrectDataFilterPerGene = function(genesIdFilter){
        var genesIdError = [];
        var genesFilters =  $scope.genesFilters.split(",");
        var error = false;

        for(var i in genesIdFilter){
            if(genesIdFilter[i] == undefined){
                genesIdError.push(genesFilters[i]);
                error = true
            }
            else{
                $scope.genesAndTranscriptsData[genesIdFilter[i].id] = (genesIdFilter[i]);
            }
        }
        if(error){

        var messageError = "";

        if(genesIdError.length != 0){
            messageError = genesIdError[0];

            for(var i=1;i<genesIdError.length;i++){
                messageError = messageError + ", " + genesIdError[i];
            }
        }
        messageError = messageError + " incorrect";
        alert(messageError);
        }
    };
    $scope.$on('newSpecie', function () {
        $scope.hideAll();
    });
    $scope.$on('new', function () {
        $scope.hideAll();
    });

    $scope.hideAll = function(){
        $scope.showAll = false;
    };

    $scope.showAllGenes = function () {
        $scope.showGenesTable = true;
        $scope.showGenePanel = false;
        $scope.showTranscriptPanel = false;
    };
    $scope.biotypes = [];
    //obtain the list of the biotypes
    $scope.getBiotypes = function () {
        $scope.biotypes = [];
        for (var i in $scope.genesAndTranscriptsData) {
            if ($scope.biotypes.indexOf($scope.genesAndTranscriptsData[i].biotype) == -1) {
                $scope.biotypes.push($scope.genesAndTranscriptsData[i].biotype);
            }
        }
        mySharedService.biotypesNames($scope.biotypes);
    };
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
//================= show panels and tables ======================
    $scope.showGenesTable = false;
    $scope.showGenePanel = false;

    //show gene panel
    $scope.geneSelected = function (geneId, index) {

//        console.log("===========================");
//        console.log($scope.toggleTree);

        if($scope.toggleTree[index]){
            $scope.toggleTree[index] = false;
        }
        else{
            $scope.toggleTree[index] = true;
        }

//        console.log($scope.toggleTree);

        if ($scope.lastDataShow != geneId) {
            $scope.lastDataShow = geneId;
            $scope.showGenePanel = true;
            $scope.selectedGen = CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, geneId);

            $scope.showTranscriptPanel = false;
            $scope.expandAllPanels();
        }
        else {
            if (!$scope.showGenePanel) {
                $scope.showGenePanel = true;
            }
        }
        $scope.selectedTranscripts = $scope.selectedGen.transcripts;
        $scope.showGenesTable = false;
    };
    //===================== tree events ========================
    $scope.showTranscriptPanel = false;
    //show transcripts panel
    $scope.transcriptSelected = function (geneId, transcriptName) {
        var transcripts;

        if ($scope.lastDataShow != geneId) {
            $scope.lastDataShow = geneId;
            $scope.showGenePanel = false;
            $scope.selectedGen = CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, geneId);
            $scope.expandAllPanels();
        }

        $scope.showGenesTable = false;
        $scope.showTranscriptPanel = true;

        transcripts = $scope.selectedGen.transcripts;

        for (var i in transcripts) {
            if (transcripts[i].name == transcriptName) {
                $scope.selectedTranscript = transcripts[i];
            }
        }
    };
    //============== Directives calls =====================
    //show transcripts panel from transcripts table
    $scope.transcriptSelectedFromGrid = function (transcriptName) {
        var transcripts = $scope.selectedGen.transcripts;

        for (var i in transcripts) {
            if (transcripts[i].name == transcriptName) {
                $scope.selectedTranscript = transcripts[i];
            }
        }
        $scope.transcriptInfo = false;
        $scope.showTranscriptPanel = true;
    };
    //================= expand / collapse ===================
    //Expand/collapse elements in DOM
    $scope.expandAllPanels = function () {
        $scope.geneInfo = false;
        $scope.transcriptsInfo = false;
        $scope.transcriptInfo = false;
        $scope.moreInfo = false;

        $scope.genePanelStatus = "-";
        $scope.transcriptPanelStatus = "-";
    };
    $scope.collapseAllPanels = function () {
        $scope.geneInfo = true;
        $scope.transcriptsInfo = true;
        $scope.transcriptInfo = true;
        $scope.moreInfo = true;

        $scope.genePanelStatus = "+";
        $scope.transcriptPanelStatus = "+";
    };
    $scope.expandAllGenesTree = function () {
        $scope.transcriptsToggle = true;
    };
    $scope.collapseAllGenesTree = function () {
        $scope.transcriptsToggle = false;
    };
    $scope.moreAndLessGene = "+";
    $scope.genePanelMore = false;

    //show more info in gene panel
    $scope.moreGenePanel = function () {
        $scope.genePanelMore = !$scope.genePanelMore;
        if ($scope.moreAndLessGene == "+") {
            $scope.moreAndLessGene = "-";
        }
        else {
            $scope.moreAndLessGene = "+";
        }
    };
    $scope.moreAndLessTranscript = "+";
    $scope.transcriptPanelMore = false;

    //show more info in transcript panel
    $scope.moreTranscriptPanel = function () {
        $scope.transcriptPanelMore = !$scope.transcriptPanelMore;
        if ($scope.moreAndLessTranscript == "+") {
            $scope.moreAndLessTranscript = "-";
        }
        else {
            $scope.moreAndLessTranscript = "+";
        }
    };
    //show/hide gene panel information
    $scope.genePanelStatus = "-";

    $scope.openCloseGenePanel = function () {
        if ($scope.genePanelStatus == "+") {
            $scope.genePanelStatus = "-";
        }
        else {
            $scope.genePanelStatus = "+";
        }
    };
    //show/hide transcript panel information
    $scope.transcriptPanelStatus = "-";

    $scope.openCloseTranscriptPanel = function () {
        if ($scope.transcriptPanelStatus == "+") {
            $scope.transcriptPanelStatus = "-";
        }
        else {
            $scope.transcriptPanelStatus = "+";
        }
    };
    //=============== Dinamic styles ========================
    //genesResult div width is the document less genesSelect div (fixed)
    $scope.getWidth = function () {
        var resultPartWidth = $(document).width() - 220 - 260 - 60;

        console.log(resultPartWidth);
        return  {width: resultPartWidth}
    };





}]);

genesResult.$inject = ['$scope', 'mySharedService'];

