var summaryPanelControl = myApp.controller('summaryPanelController', ['$scope','mySharedService','CellbaseService', function ($scope, mySharedService, CellbaseService) {

    $scope.specie =  {longName: "Homo sapiens", shortName:"hsapiens", ensemblName: "Homo_sapiens"};

    $scope.chromosomes = [];
    $scope.chromNames = ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","X","Y","MT"];
    $scope.regions = "20:32850000-33500000";

    $scope.genesIdFilter = "";

    $scope.biotypesFilter = [];

    $scope.listOfbiotypeFilters=[];

    //-----------obtener el campo name de los cromosomas devueltos por cellbase-------------
    $scope.obtainChromosomeNames = function (chrom){

        var chromNames = [];
        for (var i in chrom)
        {
            chromNames.push(chrom[i].name);
        }
        return chromNames;
    };

    //----------------avisar de un nuevo resultado---------------
    $scope.newResults = function () {
        mySharedService.newResults($scope.getRegions());
    };

    //------------añadir chromosomas------------------
    $scope.addChrom = function (chrom) {

            var pos = $scope.chromosomes.indexOf(chrom);

            if(pos == -1){
                $scope.chromosomes.push(chrom);
            }
            else{
                $scope.chromosomes.splice(pos, 1);

            }


    };

    $scope.addBiotypeFilter = function (biotype){

        var pos = $scope.biotypesFilter.indexOf(biotype);


        if(pos == -1){
            $scope.biotypesFilter.push(biotype);
        }
        else{
            $scope.biotypesFilter.splice(pos, 1);
        }

    };




    $scope.selectAllChrom = function(){

        for (var i in $scope.chromNames){
            $scope.chromosomes.push($scope.chromNames[i])
        }
    };
    $scope.deselectAllChrom = function(){

         $scope.chromosomes = [];
    };


    $scope.selectAllBiotypeFilter = function(){

        for (var i in $scope.listOfbiotypeFilters){
            $scope.biotypesFilter.push($scope.listOfbiotypeFilters[i]);
        }
//         $scope.biotypesFilter = $scope.listOfbiotypeFilters;
    };
    $scope.deselectAllBiotypeFilter = function(){
         $scope.biotypesFilter = [];
    };


    $scope.newFilters = function(){

        mySharedService.newFilter($scope.genesIdFilter, $scope.biotypesFilter);
    };






    //---obtenemos las regiones y cromosomas haciendo que si de un cromosoma tenemos una region, solo guardamos la region---
    $scope.getRegions = function () {

        var completeChromosome = true;
        var totalChromosomes = [];

        //unimos los cromosomas a las regiones
        var completeRegion;

        if ($scope.chromosomes.length == 0) {
            completeRegion = $scope.regions;

        }
        else if ($scope.regions.length == 0) {
            completeRegion = $scope.chromosomes.join();
        }
        else {

            //lo pasamos a un vector
            var regions = $scope.regions.split(",");

            //almacenamos los cromosomas que no tengan region
            for (var i in $scope.chromosomes) {
                for (var j in regions) {

                    if (regions[j].substring(0, regions[j].search(":")) == $scope.chromosomes[i])
                        completeChromosome = false
                }

                if (completeChromosome)  //si no tiene region se decarga entero
                {
                    totalChromosomes.push($scope.chromosomes[i]);
                }
                completeChromosome = true;
            }

            if(totalChromosomes.length == 0){
                completeRegion = $scope.regions;
            }
            else{
                completeRegion = totalChromosomes.join() + "," + $scope.regions;
            }

        }

        return completeRegion;
    }


    //--ordenar los cromosomas devueltos por cellbase. indicar un cero a la izquierda de los numeros con un digito para la funcion sort
    $scope.sortChromosomes = function () {

        for (var i in $scope.chromNames){
            if(!isNaN($scope.chromNames[i])){  //es un numero
                if($scope.chromNames[i].length == 1)
                {
                    $scope.chromNames[i] = 0 +  $scope.chromNames[i];
                }
            }
        }

        $scope.chromNames = $scope.chromNames.sort();

        //se quitan los ceros
        for (var i in $scope.chromNames){
            if($scope.chromNames[i][0] == "0"){
                $scope.chromNames[i] = $scope.chromNames[i].replace("0","");
            }
        }

    };


    //-----avisar a resultpanel que muestre todos los genes
    $scope.showAllGenes = function () {
        mySharedService.broadcastShowAllGenes();
    };
//
//    $scope.selectAllChrom = function () {
//        mySharedService.broadcastShowAllGenes();
//    };
//    $scope.showAllGenes = function () {
//        mySharedService.broadcastShowAllGenes();
//    };


    //-------obtener la especie seleccionada en options-panel----------
    $scope.$on('specieBroadcast', function () {   //obtener la especie elegida en optionsBar
        $scope.specie = mySharedService.selectedSpecies;


        var chrom = CellbaseService.getSpecieChromosomes($scope.specie.shortName);
        $scope.chromNames = $scope.obtainChromosomeNames(chrom);


        $scope.chromosomes = [];
        $scope.chromosomesToShow = "";
        //falta desmarcar de los checkboxes los chromosomas para que cuando se cambie de especie no se queden marcados !!!!

        //para ordanarlos ponemos un 0 a los numero con un digito para que la funcion sort de javascript
        //lo ordene bien, luego se quitan los ceros
        $scope.sortChromosomes();


        if( $scope.specie.shortName == "hsapiens"){

            $scope.chromNames.pop();
        }

    });


    $scope.$on('biotypes', function () {   //obtener la especie elegida en optionsBar

        $scope.listOfbiotypeFilters = mySharedService.biotypes;

    });



    $scope.getChromosomesColor = function (chrom) {

        if($scope.chromosomes.indexOf(chrom) != -1){
          return  {"background-color": "lightblue"};
        }
        else{
            return  {"background-color": "white"};
        }
    };

    $scope.getBiotypesColor = function (biotype) {


        if($scope.biotypesFilter.indexOf(biotype) != -1){
          return  {"background-color": "lightblue"};
        }
        else{
            return  {"background-color": "white"};
        }
    };

}]);

summaryPanelControl.$inject = ['$scope','mySharedService'];
