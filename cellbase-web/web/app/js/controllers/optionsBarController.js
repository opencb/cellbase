var optionsBarControl = myApp.controller('optionsBarController', ['$scope','mySharedService','Server', function ($scope, mySharedService, Server) {

    $scope.species = [
        {longName: "Homo sapiens", shortName:"hsapiens"},
        {longName: "Mus musculus", shortName:"mmusculus"},
        {longName: "Rattus norvegicus", shortName:"rnorvegicus"},
        {longName: "Danio rerio", shortName:"drerio"},
        {longName: "Drosophila melanogaster", shortName:"dmelanogaster"},
        {longName: "Caenorhabditis elegans", shortName:"celegans"},
        {longName: "Saccharomyces cerevisiae", shortName:"scerevisiae"},
        {longName: "Canis familiaris", shortName:"cfamiliaris"},
        {longName: "Sus scrofa", shortName:"sscrofa"},
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

