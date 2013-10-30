//myApp.controller('resultPanelControl', ['$scope',function ($scope) {
var resultPanelControl = myApp.controller('resultPanelController2', ['$scope','mySharedService','Server', function ($scope, mySharedService, Server) {

//function resultPanelController($scope,mySharedService){


    $scope.data=[];
    $scope.paginationData=[];

    //------------------para el pagination--------------------
    $scope.numeroDatosMostrar = 4;
    //limites:
    $scope.firstData = 0;
    $scope.lastData = $scope.numeroDatosMostrar;
    //--------------------------------------------------------

    $scope.showAllGenes = function (index) {
    };
    $scope.getTranscriptName = function (index) {
        console.log(index);

    };

    //obtener los datos que hay en el rango establecido por el paginado, es mas rapido que aplicar un filtro
    $scope.getDataInPaginationLimits = function () {

        $scope.paginationData = [];

        for (i=$scope.firstData; i<$scope.lastData; i++){

            if($scope.data[i] != null){
                 $scope.paginationData.push($scope.data[i]);
            }
        }

    };




    $scope.species = ["Homo sapiens ","Mus musculus ","Rattus norvegicus ", "Danio rerio ","Drosophila melanogaster ","Caenorhabditis elegans ","Saccharomyces cerevisiae ","Canis familiaris ","Sus scrofa ","Anopheles gambiae ","Plasmodium falciparum"];
//    $scope.species = ["hsapiens","mmusculus","rnorvegicus","drerio","dmelanogaster","celegans","scerevisiae","cfamiliaris","sscrofa","agambiae","pfalciparum"]


    $scope.getSpecies = function () {
        return mySharedService.species;
    };

    $scope.selectedSpecie = "";

    $scope.setSelectedSpecie = function (specie) {

        $scope.selectedSpecie = specie;
        console.log( $scope.selectedSpecie);
    };

    //indicara los datos que va a mostrar dependiendo del numero de la paginacion
    $scope.setLimits = function (firstData) {

        $scope.firstData = firstData;
        $scope.lastData = firstData + $scope.numeroDatosMostrar;

    };

    $scope.$on('handleBroadcast', function () {
        $scope.message = mySharedService.message;
        console.log("result:   " + $scope.message);
    });

    $scope.$on('resultsBroadcast', function () {

        $scope.data = Server.get(mySharedService.selectedSpecie,mySharedService.selectedRegions);
//        console.log($scope.data);


        //indicamos que los primeros datos a mostrar son los de la pagina 1
        for (i=0; i<$scope.numeroDatosMostrar; i++){
            if($scope.data[i] != null){
                $scope.paginationData.push($scope.data[i]);
            }
        }


        var numeroDatos = $scope.data.length;  //21
        var numeroDePaginas = Math.ceil(numeroDatos / $scope.numeroDatosMostrar);


        var options = {
            currentPage: 1,   //en la que nos encontramos inicialmente
            totalPages: numeroDePaginas,
            numberOfPages: 3,  //las que se ven en numero
            size: 'mini',
            onPageClicked: function(e,originalEvent,type,page){
                $scope.setLimits((page-1) * $scope.numeroDatosMostrar);
            }
        }

        $('#pagination').bootstrapPaginator(options);


    });
//}
}]);

resultPanelControl.$inject = ['$scope','mySharedService'];


myApp.factory('Server', function ($http) {
    return {
        get: function(specie, regions) {

            var dataGet;
            var host = 'http://ws-beta.bioinfo.cipf.es/cellbasebeta2/rest/v3/'

            $.ajax({
//                url: url,
//                url: host + specie + '/genomic/region/' + regions + '/gene?exclude=transcripts&of=json',
                url: host + specie + '/genomic/region/' + regions + '/gene?exclude=transcripts.xrefs,transcripts.exons,transcripts.tfbs&of=json',
                async: false,
                dataType: 'json',
                success: function (data, textStatus, jqXHR) {
                    dataGet=data[regions].result;
                },
                error: function (jqXHR, textStatus, errorThrown) {
                }
            });

            return dataGet;
//            return $http.get(url);

        }
    };
});

