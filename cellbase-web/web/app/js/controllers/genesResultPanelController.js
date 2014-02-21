var genesResult = myApp.controller('genesResult', ['$scope', '$rootScope', 'mySharedService', 'CellbaseService', function ($scope, $rootScope, mySharedService, CellbaseService) {

    $scope.toggleTree = []; //array of booleans that will show of hide the elements of the tree
    $scope.genesAndTranscriptsData = {};
    $scope.paginationData = [];
    $scope.biotypes = [];

    $scope.firstGeneId = "";
    $scope.showAll = true;

    $scope.showGenePanel = false;

    $scope.showTranscriptPanel = false;

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

        $scope.collapseAllGenesTree();
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

        $scope.collapseAllGenesTree();
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
        $scope.collapseAllGenesTree();
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
        $scope.collapseAllGenesTree();
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
        $scope.collapseAllGenesTree();
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


    $scope.clearAll = function () {
        $scope.showAll = false;
    };
    $scope.clear = function () {
        $scope.showGenePanel = false;
        $scope.showTranscriptPanel = false;
    };
    $scope.setResult = function () {


        $scope.genesFilters = mySharedService.genesIdFilter;
        $scope.biotypeFilters = mySharedService.biotypesFilter;
        $scope.selectedSpecie = mySharedService.genesSpecie;

        $scope.genesAndTranscriptsData = {};

        var genesIdFilter = [];
        var arrayOfGenes = [];

        //check if there are filters
        if ($scope.biotypeFilters.length != 0) {
            arrayOfGenes = CellbaseService.getGenesAndTranscripts($scope.selectedSpecie.shortName, mySharedService.regionsAndChromosomesGenes, $scope.biotypeFilters);

            for (var i in arrayOfGenes) {
                $scope.genesAndTranscriptsData[arrayOfGenes[i].id] = arrayOfGenes[i];
            }
        }
        if ($scope.genesFilters.length != 0) {
            genesIdFilter = CellbaseService.getGenesAndTranscriptsByIdOrName($scope.selectedSpecie.shortName, $scope.genesFilters);  //obtener los datos

            $scope.checkGeneFilter(genesIdFilter)
        }
        //if there aren't any filters, show all genes data
        if ($scope.biotypeFilters.length == 0 && $scope.genesFilters.length == 0) {
            arrayOfGenes = CellbaseService.getGenesAndTranscripts($scope.selectedSpecie.shortName, mySharedService.regionsAndChromosomesGenes, []);
            //save the data in a hash table
            for (var i in arrayOfGenes) {
                $scope.genesAndTranscriptsData[arrayOfGenes[i].id] = arrayOfGenes[i];
            }
            $scope.getBiotypes();
        }
        $scope.numResults = Object.keys($scope.genesAndTranscriptsData).length;
        $scope.initPagination();
        $scope.clear();


        if ($scope.numResults != 0) {
            $scope.toggleTree = [];

            for (var i = 0; i < 10; i++) {
                $scope.toggleTree.push(false);
            }

            $scope.showAll = true;
            $scope.firstGeneId = Object.keys($scope.genesAndTranscriptsData)[0];

            $scope.lastDataShow = Object.keys($scope.genesAndTranscriptsData)[0];
            $scope.selectedGene = CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, $scope.lastDataShow);
            //show the informtion of the first gen
            $scope.showGene(Object.keys($scope.genesAndTranscriptsData)[0], 0);


            if ($scope.selectedGene.transcripts.length != 0) {
                $scope.showTranscriptPanel = true;
                $scope.selectedTranscript = $scope.selectedGene.transcripts[0];
            }
        }
        else {
//            alert("No results with this data");
//            alert("No correct data selected");
            $scope.paginationData = [];
        }
    };
    //save thee correct results and alert the incorrect
    $scope.checkGeneFilter = function (genesIdFilter) {
        var genesIdError = [];
        var genesFilters = $scope.genesFilters.split(",");
        var error = false;

        for (var i in genesIdFilter) {
            if (genesIdFilter[i] == undefined) {
                genesIdError.push(genesFilters[i]);
                error = true
            }
            else {
                $scope.genesAndTranscriptsData[genesIdFilter[i].id] = (genesIdFilter[i]);
            }
        }
        if (error) {
            var messageError = "";
            if (genesIdError.length != 0) {
                messageError = genesIdError[0];
                for (var i = 1; i < genesIdError.length; i++) {
                    messageError = messageError + ", " + genesIdError[i];
                }
            }
            messageError = messageError + " incorrect";
            alert(messageError);
        }
    };
    //obtain the list of the biotypes
    $scope.getBiotypes = function () {
        $scope.biotypes = [];
        for (var i in $scope.genesAndTranscriptsData) {
            if ($scope.biotypes.indexOf($scope.genesAndTranscriptsData[i].biotype) == -1) {
                $scope.biotypes.push($scope.genesAndTranscriptsData[i].biotype);
            }
        }

        mySharedService.broadcastGenesBiotypes($scope.biotypes);
    };
    //===================== tree events ========================


    $scope.showGene = function (geneId, index) {

//        console.time("v5");
        if ($scope.toggleTree[index]) {
            $scope.toggleTree[index] = false;
        }
        else {
            $scope.toggleTree[index] = true;
        }
        $scope.showSelectedGene(geneId);

        if ($scope.selectedGene.transcripts.length != 0) {
            $scope.showSelectedTranscript(geneId, $scope.selectedGene.transcripts[0].name);
        }
//        console.timeEnd("v5");
    };

    $scope.showTranscript = function (geneId, transcriptName) {
        $scope.showSelectedTranscript(geneId, transcriptName);
        $scope.showSelectedGene(geneId);
    };


    //show gen panel
    $scope.showSelectedGene = function (geneId) {
//        if($scope.toggleTree[index]){
//            $scope.toggleTree[index] = false;
//        }
//        else{
//            $scope.toggleTree[index] = true;
//        }

        if ($scope.lastDataShow != geneId) {
            $scope.lastDataShow = geneId;
            $scope.showGenePanel = true;

            $scope.selectedGene = CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, geneId);

            $scope.showTranscriptPanel = false;

        }
        else {
            if (!$scope.showGenePanel) {
                $scope.showGenePanel = true;
            }
        }


        $scope.selectedTranscripts = $scope.selectedGene.transcripts;


        if ($('#genes_GV').hasClass("active")) {
            mySharedService.broadcastGenesRegionToGV($scope.selectedGene.chromosome + ":" + $scope.selectedGene.start + "-" + $scope.selectedGene.end);
        }
        if ($('#genesNVtab').hasClass("active")) {
            $scope.proteinsAllData = CellbaseService.getProteinsLinks($scope.selectedSpecie.shortName, $scope.selectedGene.name);

//        console.log($scope.proteinsAllData);

            $scope.geneProteinId = "";
            $scope.proteinsIdLinks = [];

            if ($scope.proteinsAllData.length != 0) {

                if ($scope.proteinsAllData[0].interactorA.id == $scope.proteinsAllData[1].interactorA.id || $scope.proteinsAllData[0].interactorA.id == $scope.proteinsAllData[1].interactorB.id) {
                    $scope.geneProteinId = $scope.proteinsAllData[0].interactorA.id;
                }
                else {
                    $scope.geneProteinId = $scope.proteinsAllData[0].interactorB.id;
                }

                for (var i in $scope.proteinsAllData) {
                    if ($scope.proteinsAllData[i].interactorA.id != $scope.geneProteinId) {
                        $scope.proteinsIdLinks.push($scope.proteinsAllData[i].interactorA.id);
                    }
                    else {
                        $scope.proteinsIdLinks.push($scope.proteinsAllData[i].interactorB.id);
                    }
                }
//            console.log( $scope.proteinsIdLinks);

//                mySharedService.broadcastGeneProteinsToNV($scope.geneProteinId, $scope.proteinsIdLinks);
                $rootScope.$broadcast("geneProteins", $scope.geneProteinId, $scope.proteinsIdLinks);

            }
        }

    };

    //show transcripts panel
    $scope.showSelectedTranscript = function (geneId, transcriptName) {
        var transcripts;

        if ($scope.lastDataShow != geneId) {
            $scope.lastDataShow = geneId;
            $scope.showGenePanel = false;
            $scope.selectedGene = CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, geneId);
        }
        $scope.showTranscriptPanel = true;
        transcripts = $scope.selectedGene.transcripts;
        for (var i in transcripts) {
            if (transcripts[i].name == transcriptName) {
                $scope.selectedTranscript = transcripts[i];
            }
        }
        if ($('#genes_GV').hasClass("active")) {
            mySharedService.broadcastGenesRegionToGV($scope.selectedTranscript.chromosome + ":" + $scope.selectedTranscript.start + "-" + $scope.selectedTranscript.end);
        }

    };

    //show transcripts panel from transcripts table
    $scope.showTanscriptFromTable = function (transcriptName) {
        var transcripts = $scope.selectedGene.transcripts;
        for (var i in transcripts) {
            if (transcripts[i].name == transcriptName) {
                $scope.selectedTranscript = transcripts[i];
            }
        }
        $scope.transcriptInfo = false;
        $scope.showTranscriptPanel = true;
    };

    $scope.expandAllGenesTree = function () {
        for (var i in $scope.toggleTree) {
            $scope.toggleTree[i] = true;
        }
    };
    $scope.collapseAllGenesTree = function () {
        for (var i in $scope.toggleTree) {
            $scope.toggleTree[i] = false;
        }
    };

    $scope.obtainBiotypes = function () {
        $scope.biotypes = CellbaseService.getBiotypes(mySharedService.genesSpecie.shortName);
        mySharedService.broadcastGenesBiotypes($scope.biotypes);
    };

    //genesResult div width is the rest of the document
    $scope.getWidth = function () {
        var resultPartWidth = $(document).width() - 220 - 260 - 60;

//        console.log(resultPartWidth);
        return  {width: resultPartWidth}
    };
    //tabs
    $scope.goToTab = function () {
        $(function () {
            $('#transcriptsTab a:first').tab('show')
        })
        $('#transcriptsTab a').click(function (e) {
            e.preventDefault()
            $(this).tab('show')
        })
    };

    $scope.changeResultTab = function () {

        $(function () {
            $('#genesResultTab a:first').tab('show')
        })
        $('#genesResultTab a').click(function (e) {

            e.preventDefault()
            $(this).tab('show')
        })
    };

    //--------the initial result----------
    $scope.setResult();
    $scope.obtainBiotypes();

    //--------------EVENTS-------------------
    $scope.$on('clear', function () {
        $scope.clearAll();
    });
//    $scope.$on('newSpecie', function () {
//        $scope.clearAll();
//    });
//    $scope.$on('genesNewSpecieGV', function () {
//        $scope.clearAll();
//    });
    $scope.$on('genesNewResult', function () {
        $scope.setResult();
    });

}]);

genesResult.$inject = ['$scope', 'mySharedService'];