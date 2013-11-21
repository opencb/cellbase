var optionsBarControl = myApp.controller('optionsBarController', ['$scope','mySharedService','CellbaseService', function ($scope, mySharedService, CellbaseService) {

    //todas las especies, cuando este implementado se obtendran de cellbase
    $scope.species = [
        {longName: "Homo sapiens", shortName:"hsapiens", ensemblName: "Homo_sapiens"},
        {longName: "Mus musculus", shortName:"mmusculus", ensemblName: "Mus_musculus"},
        {longName: "Rattus norvegicus", shortName:"rnorvegicus"},
        {longName: "Danio rerio", shortName:"drerio", ensembleName:"Danio_rerio"},
        {longName: "Drosophila melanogaster", shortName:"dmelanogaster", ensembleName:"Drosophila_melanogaster"},
        {longName: "Caenorhabditis elegans", shortName:"celegans", ensembleName:"Caenorhabditis_elegans"},
        {longName: "Saccharomyces cerevisiae", shortName:"scerevisiae", ensembleName:"Saccharomyces_cerevisiae"},
        {longName: "Canis familiaris", shortName:"cfamiliaris", ensembleName:"Canis_familiaris"},
        {longName: "Sus scrofa", shortName:"sscrofa", ensembleName:"Sus_scrofa", ensembleName:"Sus_scrofa"},
        {longName: "Anopheles gambiae", shortName:"agambiae"},
        {longName: "Plasmodium falciparum", shortName:"pfalciparum"}
    ];


    $scope.genesId = [];
    $scope.biotypes = [];
    $scope.genesIdFilter = [];
    $scope.biotypesFilter = [];

    //----------gestion de pestañas---------
    $scope.goToTab = function () {

        $(function () {
            $('#myTab a:first').tab('show')
        })

        $('#myTab a').click(function (e) {
            e.preventDefault()
            $(this).tab('show')
        })


    };

    //--------Filtros---------------
    $scope.addGeneIdFilter = function (geneId) {

        var pos = $scope.genesIdFilter.indexOf(geneId);

        if(pos == -1){
            $scope.genesIdFilter.push(geneId);
        }
        else
        {
             $scope.genesIdFilter.splice(pos, 1);
        }

    };
    $scope.addBiotypeFilter = function (biotype) {

        var pos = $scope.biotypesFilter.indexOf(biotype);

        if(pos == -1){
            $scope.biotypesFilter.push(biotype);

        }
        else{
            $scope.biotypesFilter.splice(pos, 1);
        }

    };

    //-----------obtener nueva especie seleccionada y comunicarlo a los demas-----------
    $scope.setSelectedSpecie = function (specie) {
        mySharedService.broadcastSpecie(specie);
    };





}]);

optionsBarControl.$inject = ['$scope','mySharedService'];

