var optionsBarControl = myApp.controller('optionsBarController', ['$scope','mySharedService','CellbaseSpecies', function ($scope, mySharedService, CellbaseSpecies) {

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

    $scope.setSelectedSpecie = function (specie) {
        mySharedService.broadcastSpecie(specie);
    };

    $scope.newFilter = function () {
        mySharedService.broadcastFilter($scope.genesIdFilter, $scope.biotypesFilter);
    };


    //-----------Obtener genesId y biotypes para filtrar--------------
    $scope.$on('genesIdAndBiotypes', function () {   //obtener la especie elegida en optionsBar
        $scope.genesId = mySharedService.genesId;
        $scope.biotypes = mySharedService.biotypes;
     });

}]);

optionsBarControl.$inject = ['$scope','mySharedService'];


//Not implemeneted yet
myApp.factory('CellbaseSpecies', function ($http) {
    return {
        getSpecies: function () {
            var dataGet;

            $.ajax({
                url: 'http://ws-beta.bioinfo.cipf.es/cellbase/rest/v3/?of=json',
                async: false,
                dataType: 'json',
                success: function (data, textStatus, jqXHR) {
                    dataGet = data;
                },
                error: function (jqXHR, textStatus, errorThrown) {
                }
            });
            return dataGet;
        }
    };

});