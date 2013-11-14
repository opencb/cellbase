var summaryPanelControl = myApp.controller('summaryPanelController', ['$scope','mySharedService','Cellbase', function ($scope, mySharedService, Cellbase) {

    $scope.specie =  {longName: "Homo sapiens", shortName:"hsapiens", ensemblName: "Homo_sapiens"};
    $scope.chromosomes = "";
    $scope.regions = "20:32850000-33500000";

    //------------ejemplo----------------
    $scope.genesFilters=[];
    $scope.biotypeFilters=[];
    //------------ejemplo----------------



    $scope.chromosomesPerSpecie = {
        hsapiens: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","X","Y","MT"],
        mmusculus: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","X","Y","MT"],
        rnorvegicus: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","X","Y","MT"],
        drerio: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","X","Y","MT"],
        dmelanogaster: ["2L", "2LHet", "2R", "2RHet","3L", "3LHet", "3R", "3RHet","4", "U", "Uextra", "X","XHet", "YHet", "dmel_mitochondrion_genome"],
        celegans : ["I", "II", "III","IV","V","X","MtDNA"],
        scerevisiae: ["I", "II", "III","IV","V","VI", "VII", "VIII", "IX", "X","XI", "XII", "XIII", "XIV", "XV", "XVI", "Mito"],
        cfamiliaris: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","38","X","MT"],
        sscrofa: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","X","Y","MT"],
        agambiae: ["2L", "2R", "3L", "3R", "X"],
        pfalciparum: ["01","02","03","04","05","06","07","08","09","10","11","12","13","14"]
    };


    $scope.chromNames = ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","X","Y","MT"];



    $scope.obtainChromosomeNames = function (chrom){

        var chromNames = [];
        for (var i in chrom)
        {
            chromNames.push(chrom[i].name);
        }
        return chromNames;
    };


    $scope.newResults = function () {
        mySharedService.newResults($scope.getRegions());
    };
    $scope.addChrom = function (chrom) {

        var pos = $scope.chromosomes.search(chrom)


        if(pos == -1){
//            $scope.chromosomes.concat(chrom);
            if($scope.chromosomes.length == 0)
            {
                $scope.chromosomes = chrom;
            }
            else{

//                si es el primero no empieza en coma
//                if($scope.chromosomes.search(",") == -1){ //si hay solo un elemento
//                    $scope.chromosomes = chrom + ",";
//                }
//                else{
                $scope.chromosomes = $scope.chromosomes  + "," + chrom;
//                }
            }
        }
        else
        {
            $scope.chromosomes = $scope.chromosomes.replace("," + chrom, "");    //intentar eliminar el chromosoma
        }


    };

    //obtenemos las regiones y cromosomas haciendo que si de un cromosoma tenemos una region, solo guardamos la region
    $scope.getRegions = function () {

        var completeChromosome = true;
        var totalChromosomes = [];


        //unimos los cromosomas a las regiones
        var completeRegion;

        if ($scope.chromosomes.length == 0) {
            completeRegion = $scope.regions;

        }
        else if ($scope.regions.length == 0) {
            completeRegion = $scope.chromosomes;
        }
        else {

            var chromosomes = $scope.chromosomes.split(",");
            var regions = $scope.regions.split(",");

            //almacenamos los cromosomas que no tengan region
            for (var i in chromosomes) {
                for (var j in regions) {

                    if (regions[j].substring(0, regions[j].search(":")) == chromosomes[i])
                        completeChromosome = false
                }

                if (completeChromosome)  //si no tiene region se decarga entero
                {
                    totalChromosomes.push(chromosomes[i]);
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


//    var sortfunction = function (a, b) {
//        var IsNumber = true;
//        for (var i = 0; i < a.name.length && IsNumber == true; i++) {
//            if (isNaN(a.name[i])) {
//                IsNumber = false;
//            }
//        }
//        if (!IsNumber) return 1;
//        return (a.name - b.name);
//    };

    $scope.sortChromosomes = function () {

        for (var i in $scope.chromNames){
            if(!isNaN($scope.chromNames[i])){  //es un numero
                if($scope.chromNames[i].length == 1)
                {
                    $scope.chromNames[i] = 0 +  $scope.chromNames[i];
                }
            }
        }

//        console.log($scope.chromNames);
        $scope.chromNames = $scope.chromNames.sort();
//        console.log($scope.chromNames);

    };




    $scope.$on('specieBroadcast', function () {   //obtener la especie elegida en optionsBar
        $scope.specie = mySharedService.selectedSpecies;

        var chrom = Cellbase.getSpecieChromosomes($scope.specie.shortName);
        $scope.chromNames = $scope.obtainChromosomeNames(chrom);

        //para ordanarlos ponemos un 0 a los numero con un digito para que la funcion sort de javascript
        //lo ordene bien, luego se quitan los ceros
        $scope.sortChromosomes();

        //se quitan los ceros
        for (var i in $scope.chromNames){
            if($scope.chromNames[i][0] == "0"){
                $scope.chromNames[i] = $scope.chromNames[i].replace("0","");
            }
        }

    });





    $scope.$on('filter', function () {   //obtener la especie elegida en optionsBar

        $scope.genesFilters=mySharedService.genesIdFilter;
        $scope.biotypeFilters = mySharedService.biotypesFilter;

    });




}]);

summaryPanelControl.$inject = ['$scope','mySharedService'];
//summaryPanelController.$inject = ['$scope','mySharedService'];


myApp.factory('Cellbase', function ($http) {
    return {
        getSpecieChromosomes: function (specie) {

            var dataGet;
            var host = 'http://ws-beta.bioinfo.cipf.es/cellbase/rest/v3/'
            var url;



            $.ajax({
                url: host + specie + '/genomic/chromosome/all?of=json',
//                url: host + species + '/genomic/region/' + regions + '/gene?exclude=transcripts.xrefs,transcripts.exons,transcripts.tfbs&of=json',
                async: false,
                dataType: 'json',
                success: function (data, textStatus, jqXHR) {
                    dataGet = data.response.result.chromosomes;
                },
                error: function (jqXHR, textStatus, errorThrown) {
                }
            });

            return dataGet;
        }
    };

});