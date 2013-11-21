var resultPanelControl = myApp.controller('resultPanelController', ['$scope', 'mySharedService', 'CellbaseService', function ($scope, mySharedService, CellbaseService) {

    $scope.genesAndTranscriptsData = {};
    $scope.genesAllData = [];
    $scope.paginationData = [];

    $scope.selectedSpecie;
    $scope.firstGeneId = "";
    ;
    $scope.showAll = false;   //para mostrar los elementos de este div

    $scope.numResults;

//------------pruebas------------------
    $scope.prueba2 = "adios";

    $scope.prueba = [{id: "id1", data: {idData: [{id:"id1-uno"}, {id:"id1-dos"},{id: "id1-tres"}]}}, {id: "id2", data: {idData:[{id:"id2-uno"},{id:"id2-dos"}, {id:"id2-tres"}]}}];
    $scope.prueba2 = [{id:"id1-uno"}, {id:"id1-dos"},{id: "id1-tres"}];

    $scope.show = false;
    $scope.click = function () {
         $scope.show = !$scope.show;

    };


    //--------------------------------

    //------------------para el pagination--------------------
    $scope.numeroDatosMostrar = 4;
    //limites:
    $scope.firstData = 0;
    $scope.lastData = $scope.numeroDatosMostrar;
    //--------------------------------------------------------

    //--------my pagination--------------
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

        //si el elemento es el primero de todos
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

//                if($scope.paginationNumbers[2] == page){//si pasa a ser el ultimo numero  que aparece

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

    $scope.simplePagination;

    //-----------iniciar el paginador-------------------
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
    //------------------dejar de mostrar los paneles y tablas---------------
    $scope.clearPanelsAndTables = function () {
        $scope.showGenesTable = false;
        $scope.showGenePanel = false;
        $scope.showTranscriptPanel = false;
        $scope.showTranscriptsTable = false;
        $scope.showMoreInfoPanel = false;
    };
    //------------obtener nuevos resultados------------
    $scope.newResults = function () {

        $scope.showAll = true;
        var arrayOfGenes = [];
        $scope.selectedSpecie = mySharedService.selectedSpecies;

        arrayOfGenes = CellbaseService.getGenesAndTranscripts($scope.selectedSpecie.shortName, mySharedService.selectedRegions, []);
        $scope.numResults = arrayOfGenes.numResults;
        arrayOfGenes = arrayOfGenes.result;

        $scope.genesAndTranscriptsData = {};

        //pasamos el array a una tabla hash
        for (var i in arrayOfGenes) {
            $scope.genesAndTranscriptsData[arrayOfGenes[i].id] = arrayOfGenes[i];

        }
        $scope.getBiotypes();
        $scope.initPagination();
        $scope.clearPanelsAndTables();

        //mostramos por defecto el panel del primer gen
        $scope.lastDataShow = Object.keys($scope.genesAndTranscriptsData)[0];
        $scope.selectedGen = CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, $scope.lastDataShow)[0].result[0];  //obtener los datos
        $scope.showGenePanel = true;

        //para abrir la lista de primer gen
        $scope.firstGeneId = Object.keys($scope.genesAndTranscriptsData)[0];

        $scope.initPaginationGenesTable();


        //-----------------------------------------------------------------
//        $( '#'+ Object.keys($scope.genesAndTranscriptsData)[0]).attr("ng-show", true);
//        $( '#'+ Object.keys($scope.genesAndTranscriptsData)[0] +'-transcripts').attr("ng-show", true);

//        console.log($( '#'+ Object.keys($scope.genesAndTranscriptsData)[0]));
//        console.log($( '#'+ Object.keys($scope.genesAndTranscriptsData)[0] +'-transcripts'));

//        $( '#'+ Object.keys($scope.genesAndTranscriptsData)[0]).show();


//        console.log($('#' + Object.keys($scope.genesAndTranscriptsData)[0]));
//        $( '#'+ Object.keys($scope.genesAndTranscriptsData)[0] +'-transcripts').show();

//        var target = angular.element('#appBusyIndicator');
//        var target = angular.element('#' + Object.keys($scope.genesAndTranscriptsData)[0]);
//        console.log(target);

//        var myEl = angular.element( document.querySelector('#'+ Object.keys($scope.genesAndTranscriptsData)[0]) );
//        console.log(myEl);

        //--------------------------------------------------------------------


    };


    $scope.$on('newResult', function () {   //obtener la especie elegida en optionsBar

        $scope.genesFilters = mySharedService.genesIdFilter;
//        $scope.genesFilters = mySharedService.genesIdFilter.split(",");
        $scope.biotypeFilters = mySharedService.biotypeFilter;

        $scope.genesAndTranscriptsData = {};

        var genesIdFilter = [];

        var arrayOfGenes = [];

        //hecemos un or si existen los dos filtros
        if ($scope.biotypeFilters.length != 0) {

            arrayOfGenes = CellbaseService.getGenesAndTranscripts($scope.selectedSpecie.shortName, mySharedService.selectedRegions, $scope.biotypeFilters);
            arrayOfGenes = arrayOfGenes.result;

            //pasamos el array a una tabla hash
            for (var i in arrayOfGenes) {
                $scope.genesAndTranscriptsData[arrayOfGenes[i].id] = arrayOfGenes[i];

            }
        }
        if ($scope.genesFilters.length != 0) {
            genesIdFilter = CellbaseService.getGenesAndTranscriptsById($scope.selectedSpecie.shortName, $scope.genesFilters);  //obtener los datos

            for (var i in genesIdFilter) {
                $scope.genesAndTranscriptsData[genesIdFilter[i].id] = (genesIdFilter[i]);
            }
        }
        //si no hay ningun filtro aplicado mostramos todos los datos
        if ($scope.biotypeFilters.length == 0 && $scope.genesFilters.length == 0) {
            $scope.newResults();
        }

        $scope.numResults = Object.keys($scope.genesAndTranscriptsData).length;
        $scope.initPagination();
        $scope.clearPanelsAndTables();
//        $scope.firstGeneId = Object.keys($scope.genesAndTranscriptsData)[0];


        $scope.initPaginationGenesTable();


        //mostramos el panel del del primer gen y los trancritos
        $scope.geneSelected( Object.keys($scope.genesAndTranscriptsData)[0]);
        $scope.transcriptsSelected( Object.keys($scope.genesAndTranscriptsData)[0]);


    });

    $scope.$on('showAllGenes', function () {

        $scope.showGenesTable = true;

        $scope.showGenePanel = false;
        $scope.showTranscriptPanel = false;
        $scope.showTranscriptsTable = false;
        $scope.showMoreInfoPanel = false;
    });

    $scope.showAllGenes = function () {

        console.log("hola");
        $scope.showGenesTable = true;

        $scope.showGenePanel = false;
        $scope.showTranscriptPanel = false;
        $scope.showTranscriptsTable = false;
        $scope.showMoreInfoPanel = false;
    };




    $scope.biotypes = [];
    //-----------------------obtener la lista de biotipos del resultado-----------------
    $scope.getBiotypes = function () {

        $scope.biotypes = [];
        for (var i in $scope.genesAndTranscriptsData) {
            if ($scope.biotypes.indexOf($scope.genesAndTranscriptsData[i].biotype) == -1) {
                $scope.biotypes.push($scope.genesAndTranscriptsData[i].biotype);

            }
        }
        //informamos a options-panale sobre los biotipos
        mySharedService.biotypesNames($scope.biotypes);
    };

    //--------para gestionar los tabs------------
    $scope.goToTab = function () {

        $(function () {
            $('#myTab a:first').tab('show')
        })

        $('#myTab a').click(function (e) {
            e.preventDefault()
            $(this).tab('show')
        })
    };

//================= mostrar los paneles y tablas de los datos seleccionados ======================
    $scope.lastDataShow;

    $scope.showGenesTable = false;
    $scope.showGenePanel = false;
    $scope.selectedGen;

    //-------------mostrar panel de gene-------------
    $scope.geneSelected = function (geneId) {


        if ($scope.lastDataShow != geneId) {

            //cuando vemos un nuevo gen se esconde el anterior
//            $scope.firstGeneId = "";

            $scope.lastDataShow = geneId;   //nuevo gen
            $scope.showGenePanel = true;    //mostrar panel
            $scope.selectedGen = CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, geneId)[0].result[0];  //obtener los datos

            $scope.showTranscriptPanel = false;
            $scope.showTranscriptsTable = false;
            $scope.showMoreInfoPanel = false;

            //cuando cambiamos de gen expandimos la inforacion de cada pae
            $scope.expandAllPanels();

            //mostramos tambien la tabla de grids

        }
        else {
            if (!$scope.showGenePanel) {  //para que no se muestre cuando ya lo esta
                $scope.showGenePanel = true;    //mostrar panel

            }
        }
            $scope.selectedTranscripts = $scope.selectedGen.transcripts;
            $scope.showTranscriptsTable = true;
        $scope.showGenesTable = false;
    };



    //mostrat tabla de transcripts desde el el panel de un gene
    $scope.transcriptsSelectedFromGenesPanel = function (geneId) {
        $scope.transcriptsInfo = false;
        $scope.transcriptsSelected(geneId);
    };


    //-------------mostrar tabla de transcripts-------------
    $scope.transcriptsSelected = function (geneId) {

        if ($scope.lastDataShow != geneId) {

            $scope.lastDataShow = geneId;   //nuevo gen
            $scope.showTranscriptsTable = true;

            $scope.selectedGen = CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, geneId)[0].result[0];  //obtener los datos
            $scope.selectedTranscripts = $scope.selectedGen.transcripts;//CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, geneId)[0].result[0].transcripts;


            $scope.showTranscriptPanel = false;
            $scope.showGenePanel = false;
            $scope.showMoreInfoPanel = false;

            $scope.expandAllPanels();
        }
        else {
            if (!$scope.showTranscriptsTable) {  //para que no se muestre cuando ya lo esta
                $scope.selectedTranscripts = $scope.selectedGen.transcripts;
                $scope.showTranscriptsTable = true;
            }
        }
        $scope.showGenesTable = false;
    };

    $scope.showTranscriptPanel = false;
    $scope.selectedTranscript;

    //-------------mostrar panel de transcritos-------------
    $scope.transcriptSelected = function (geneId, transcriptName) {

        var transcripts;

        if ($scope.lastDataShow != geneId) {
            $scope.lastDataShow = geneId;   //nuevo gen

            $scope.showTranscriptsTable = false;
            $scope.showGenePanel = false;
            $scope.showMoreInfoPanel = false;

            $scope.selectedGen = CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, geneId)[0].result[0];

            $scope.expandAllPanels();
        }

        $scope.showGenesTable = false;
        $scope.showTranscriptPanel = true;

        transcripts = $scope.selectedGen.transcripts;//CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, geneId)[0].result[0].transcripts;

        for (var i in transcripts) {
            if (transcripts[i].name == transcriptName) {
                $scope.selectedTranscript = transcripts[i];
            }
        }

    };

    //-------------mostrar panel de transcitps dede la tabla transcripts-------------
    $scope.transcriptSelectedFromGrid = function (transcriptName) {

        var transcripts = $scope.selectedGen.transcripts;//CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, $scope.lastDataShow)[0].result[0].transcripts;

        for (var i in transcripts) {
            if (transcripts[i].name == transcriptName) {
                $scope.selectedTranscript = transcripts[i];
            }
        }

        $scope.transcriptInfo = false;

        $scope.showTranscriptPanel = true;
    };

    //-------------mostrar mas informacion desde el panel del  transcript-------------
    $scope.moreInfoSelectedFromPanel = function (transcriptName) {

        $scope.showMoreInfoPanel = true;

        $scope.selectedExons = $scope.selectedTranscript.exons;
        $scope.selectedTFBS = $scope.selectedTranscript.tfbs;
        $scope.selectedXrefs = $scope.selectedTranscript.xrefs;

        $scope.moreInfo = false;

    };

    $scope.showMoreInfoPanel = false;
    $scope.selectedExons;
    $scope.selectedTFBS;
    $scope.selectedXrefs;

    //-------------mostrar mas informacion (xrefs, tfbs y exons)-------------
    $scope.moreInfoSelected = function (geneId, transcriptName) {

        var transcripts;

        if ($scope.lastDataShow != geneId) {

            $scope.lastDataShow = geneId;   //nuevo gen

            $scope.showTranscriptsTable = false;
            $scope.showTranscriptPanel = false;
            $scope.showGenePanel = false;

            $scope.selectedGen = CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, geneId)[0].result[0];

            $scope.expandAllPanels();
        }

        transcripts = $scope.selectedGen.transcripts;//CellbaseService.getGenesAllDataById($scope.selectedSpecie.shortName, geneId)[0].result[0].transcripts;

        for (var i in transcripts) {
            if (transcripts[i].name == transcriptName) {

                $scope.selectedExons = transcripts[i].exons;
                $scope.selectedTFBS = transcripts[i].tfbs;
                $scope.selectedXrefs = transcripts[i].xrefs;

            }
        }
        $scope.showGenesTable = false;
        $scope.showMoreInfoPanel = true;

    };

    //---------------------dinamic styles----------------
    //damos al div con los resultados lo que sobra de pantalla, los otros dos tienen anchura fija
    $scope.getWidth = function () {

        var resultPartWidth = $(document).width() - 220 - 260 - 50;
        return  {width: resultPartWidth}
    };
    $scope.getMaxWidth = function () {

//        var resultPartWidth = $(document).width() - 210 - 300 - 60;
        var resultPartWidth = 30;
        return  {maxWidth: resultPartWidth}
    };

    $scope.expandAllPanels = function () {

            $scope.geneInfo = false;
            $scope.transcriptsInfo = false;
            $scope.transcriptInfo = false;
            $scope.moreInfo = false;
    };
    $scope.collapseAllPanels = function () {

            $scope.geneInfo = true;
            $scope.transcriptsInfo = true;
            $scope.transcriptInfo = true;
            $scope.moreInfo = true;
    };

    $scope.expandAllGenesTree = function () {
        $scope.transcriptsToggle = true;
    };
    $scope.collapseAllGenesTree = function () {
        $scope.transcriptsToggle = false;
    };


    $scope.moreAndLessGene = "+";
    $scope.genePanelMore = false;

    //------mostrar mas informacion del panel del gen
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

    //------mostrar mas informacion del panel del gen
    $scope.moreTranscriptPanel = function () {
        $scope.transcriptPanelMore = !$scope.transcriptPanelMore;
        if ($scope.moreAndLessTranscript == "+") {
            $scope.moreAndLessTranscript = "-";
        }
        else {
            $scope.moreAndLessTranscript = "+";
        }
    };

//}
}]);


resultPanelControl.$inject = ['$scope', 'mySharedService'];

