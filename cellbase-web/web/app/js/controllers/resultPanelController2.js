var resultPanelControl = myApp.controller('resultPanelController2', ['$scope','mySharedService','Server', function ($scope, mySharedService, Server) {

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

    //obtener los datos que hay en el rango establecido por el paginado, es mas rapido que aplicar un filtro
    $scope.getDataInPaginationLimits = function () {

        $scope.paginationData = [];

        for (i=$scope.firstData; i<$scope.lastData; i++){

            if($scope.data[i] != null){
                 $scope.paginationData.push($scope.data[i]);
            }
        }

    };


    //indicara los datos que va a mostrar dependiendo del numero de la paginacion
    $scope.setLimits = function (firstData) {
        $scope.firstData = firstData;
        $scope.lastData = firstData + $scope.numeroDatosMostrar;
    };


    $scope.$on('resultsBroadcast', function () {

        $scope.data = Server.get(mySharedService.selectedSpecies.shortName, mySharedService.selectedRegions);

        //indicamos que los primeros datos a mostrar son los de la pagina 1
        for (i=0; i<$scope.numeroDatosMostrar; i++){
            if($scope.data[i] != null){
                $scope.paginationData.push($scope.data[i]);
            }
        }


        //definimos el pagination
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
        get: function(species, regions) {

            var dataGet;
            var host = 'http://ws-beta.bioinfo.cipf.es/cellbase/rest/v3/'

            $.ajax({
                url: host + species + '/genomic/region/' + regions + '/gene?exclude=transcripts.xrefs,transcripts.exons,transcripts.tfbs&of=json',
                async: false,
                dataType: 'json',
                success: function (data, textStatus, jqXHR) {

                    dataGet=data.response[0].result;
                },
                error: function (jqXHR, textStatus, errorThrown) {
                }
            });

            return dataGet;
        }
    };
});

