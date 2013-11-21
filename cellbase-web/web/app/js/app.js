'use strict';

var myApp = angular.module('project', []);
//var foodMeApp = angular.module('foodMeApp', ['ngResource']);


myApp.factory('mySharedService', function($rootScope){

    var sharedService = {};

    sharedService.message = '';

    sharedService.selectedSpecies=  {longName: "Homo sapiens", shortName:"hsapiens", ensemblName: "Homo_sapiens"};
    sharedService.selectedRegions= "";

//    sharedService.chromosomesPerSpecie = {
//        hsapiens: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","X","Y","MT"],
//        mmusculus: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","X","Y","MT"],
//        rnorvegicus: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","X","Y","MT"],
//        drerio: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","X","Y","MT"],
//        dmelanogaster: ["2L", "2LHet", "2R", "2RHet","3L", "3LHet", "3R", "3RHet","4", "U", "Uextra", "X","XHet", "YHet", "dmel_mitochondrion_genome"],
//        celegans : ["I", "II", "III","IV","V","X","MtDNA"],
//        scerevisiae: ["I", "II", "III","IV","V","VI", "VII", "VIII", "IX", "X","XI", "XII", "XIII", "XIV", "XV", "XVI", "Mito"],
//        cfamiliaris: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","38","X","MT"],
//        sscrofa: ["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","X","Y","MT"],
//        agambiae: ["2L", "2R", "3L", "3R", "X"],
//        pfalciparum: ["01","02","03","04","05","06","07","08","09","10","11","12","13","14"]
//    };

//    sharedService.species = ["Homo sapiens ","Mus musculus ","Rattus norvegicus ", "Danio rerio ","Drosophila melanogaster ","Caenorhabditis elegans ","Saccharomyces cerevisiae ","Canis familiaris ","Sus scrofa ","Anopheles gambiae ","Plasmodium falciparum"];
//    $scope.species = ["hsapiens","mmusculus","rnorvegicus","drerio","dmelanogaster","celegans","scerevisiae","cfamiliaris","sscrofa","agambiae","pfalciparum"]



    //comunicar la especie de optionsBar a SummaryPanel
    sharedService.broadcastSpecie = function(specie){

        this.selectedSpecies = specie;

        this.broadcastSpecieItem();
    };

    //comunicar un nuevo resultado de summaryPanel a resultPanel
    sharedService.newResults = function(regions){
        this.selectedRegions= regions;

        this.broadcastResultsItem();
    };

    //para pasarlo a optionsBar y se filtre
    sharedService.biotypesNames = function(biotypes){
        this.biotypes= biotypes;

        this.broadcastbiotypes();
    };

    sharedService.broadcastShowAllGenes = function(){

        this.broadcastToShowAllGenes();
    };
    sharedService.newFilter = function(genesIdFilter,biotypeFilter){

        this.genesIdFilter = genesIdFilter;
        this.biotypeFilter = biotypeFilter;

        this.broadcastFilter();
    };




    sharedService.broadcastSpecieItem = function () {
        $rootScope.$broadcast('specieBroadcast');
    }
    sharedService.broadcastResultsItem = function () {
        $rootScope.$broadcast('result');
    }
    sharedService.broadcastbiotypes = function () {
        $rootScope.$broadcast('biotypes');
    }

    sharedService.broadcastToShowAllGenes = function () {
        $rootScope.$broadcast('showAllGenes');
    }

    sharedService.broadcastFilter = function () {
        $rootScope.$broadcast('filter');
    }



    return sharedService;
})

myApp.service('CellbaseService', function () {
    ////Not implemeneted yet

    var host = 'http://ws-beta.bioinfo.cipf.es/cellbase/rest/v3/';

    this.getSpecies = function () {
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
    };


    //obtener los chromosomas de una especie
    this.getSpecieChromosomes = function (specie) {

        var dataGet;

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
    };


    this.getGenesAndTranscripts = function (species, regions, biotypesFilter) {

        var dataGet;
        var url;

        if (biotypesFilter.length == 0) {
            url = host + species + '/genomic/region/' + regions + '/gene?exclude=transcripts.xrefs,transcripts.exons,transcripts.tfbs&of=json';
        }
        else {
            url = host + species + '/genomic/region/' + regions + '/gene?biotype=' + biotypesFilter.join() + '&exclude=transcripts.xrefs,transcripts.exons,transcripts.tfbs&of=json';
        }

        $.ajax({
            url: url,
//                url: host + species + '/genomic/region/' + regions + '/gene?exclude=transcripts.xrefs,transcripts.exons,transcripts.tfbs&of=json',
            async: false,
            dataType: 'json',
            success: function (data, textStatus, jqXHR) {

                dataGet = data.response[0];
            },
            error: function (jqXHR, textStatus, errorThrown) {
            }
        });


        return dataGet;
    };
    this.getGenesAllData = function (species, regions, biotypesFilter) {

        var dataGet;
        var url;

        if (biotypesFilter.length == 0) {
            url = host + species + '/genomic/region/' + regions + '/gene?exclude=transcripts&of=json';
        }
        else {
            url = host + species + '/genomic/region/' + regions + '/gene?biotype=' + biotypesFilter.join() + '&exclude=transcripts&of=json';
        }

        $.ajax({
            url: url,
//                url: host + species + '/genomic/region/' + regions + '/gene?exclude=transcripts&of=json',
            async: false,
            dataType: 'json',
            success: function (data, textStatus, jqXHR) {

                dataGet = data.response[0].result;
            },
            error: function (jqXHR, textStatus, errorThrown) {
            }
        });

        return dataGet;
    };
    this.getGenesAndTranscriptsById = function (species, geneId) {

        var dataGet = [];
        var url;


        $.ajax({
//                url: host + species + '/feature/gene/' + geneId + '/info?&of=json',
            url: host + species + '/feature/gene/' + geneId + '/info?exclude=transcripts.xrefs,transcripts.exons,transcripts.tfbs&of=json',
//              url: host + species + '/genomic/region/' + regions + '/gene?exclude=transcripts.xrefs,transcripts.exons,transcripts.tfbs&of=json',
            async: false,
            dataType: 'json',
            success: function (data, textStatus, jqXHR) {


                for (var i in data.response) {
                    dataGet.push(data.response[i].result[0]);
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
            }
        });

        return dataGet;
    };
    this.getGenesAllDataById = function (species, geneId) {

        var dataGet = [];
        var url;


        $.ajax({
            url: host + species + '/feature/gene/' + geneId + '/info?&of=json',
//                url: host + species + '/feature/gene/' + geneId + '/info?exclude=transcripts.xrefs,transcripts.exons,transcripts.tfbs&of=json',
//              url: host + species + '/genomic/region/' + regions + '/gene?exclude=transcripts.xrefs,transcripts.exons,transcripts.tfbs&of=json',
            async: false,
            dataType: 'json',
            success: function (data, textStatus, jqXHR) {

//                    for(var i in data.response)
//                    {
//                        dataGet.push(data.response[i].result[0]);
//                    }
                dataGet = data.response;
//                    dataGet = data.response[0];
            },
            error: function (jqXHR, textStatus, errorThrown) {
            }
        });

        return dataGet;
    };



});


//myApp.config(function($routeProvider) {
//
//  $routeProvider.
//      when('/', {
//        controller: 'optionsBarController',
//        templateUrl: 'views/options-bar.html'
//      }).
//      when('/', {
//        controller: 'summaryPanelController',
//        templateUrl: 'views/summary-panel.html'
//      }).
//      when('/', {
//        controller: 'resultPanelController',
//        templateUrl: 'views/result-panel.html'
//      });
////      .
////      when('/checkout', {
////        controller: 'CheckoutController',
////        templateUrl: 'views/checkout.html'
////      }).
////      when('/thank-you', {
////        controller: 'ThankYouController',
////        templateUrl: 'views/thank-you.html'
////      }).
////      when('/customer', {
////        controller: 'CustomerController',
////        templateUrl: 'views/customer.html'
////      }).
////      when('/who-we-are', {
////        templateUrl: 'views/who-we-are.html'
////      }).
////      when('/how-it-works', {
////        templateUrl: 'views/how-it-works.html'
////      }).
////      when('/help', {
////        templateUrl: 'views/help.html'
////      });
//});
