'use strict';

var myApp = angular.module('project', []);
//var foodMeApp = angular.module('foodMeApp', ['ngResource']);


myApp.factory('mySharedService', function($rootScope){

    var sharedService = {};

    sharedService.message = '';

    sharedService.selectedSpecie= "";
    sharedService.selectedRegions= "";


    sharedService.chromosomesPerSpecie = {
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

    sharedService.species = ["Homo sapiens ","Mus musculus ","Rattus norvegicus ", "Danio rerio ","Drosophila melanogaster ","Caenorhabditis elegans ","Saccharomyces cerevisiae ","Canis familiaris ","Sus scrofa ","Anopheles gambiae ","Plasmodium falciparum"];
//    $scope.species = ["hsapiens","mmusculus","rnorvegicus","drerio","dmelanogaster","celegans","scerevisiae","cfamiliaris","sscrofa","agambiae","pfalciparum"]



    sharedService.prepForBroadcast = function(msg){
        this.message = msg;
        this.broadcastItem();
    };

    //comunicar la especie de optionsBar a SummaryPanel
    sharedService.broadcastSpecie = function(specie){
        this.selectedSpecie = specie;

        this.broadcastSpecieItem();
    };

    //comunicar un nuevo resultado de summaryPanel a resultPanel
    sharedService.newResults = function(specie, regions){

        this.selectedSpecie= specie;
        this.selectedRegions= regions;

        this.broadcastResultsItem();
    };


    //con esta funcion hacemos el trigger para quien tenga el on
    sharedService.broadcastItem = function () {
        $rootScope.$broadcast('handleBroadcast');
    }
    sharedService.broadcastSpecieItem = function () {
        $rootScope.$broadcast('specieBroadcast');
    }
    sharedService.broadcastResultsItem = function () {
        $rootScope.$broadcast('resultsBroadcast');
    }



    return sharedService;
})


myApp.config(function($routeProvider) {

  $routeProvider.
      when('/', {
        controller: 'optionsBarController',
        templateUrl: 'views/options-bar.html'
      }).
      when('/', {
        controller: 'summaryPanelController',
        templateUrl: 'views/summary-panel.html'
      }).
      when('/', {
        controller: 'resultPanelController',
        templateUrl: 'views/result-panel.html'
      });
//      .
//      when('/checkout', {
//        controller: 'CheckoutController',
//        templateUrl: 'views/checkout.html'
//      }).
//      when('/thank-you', {
//        controller: 'ThankYouController',
//        templateUrl: 'views/thank-you.html'
//      }).
//      when('/customer', {
//        controller: 'CustomerController',
//        templateUrl: 'views/customer.html'
//      }).
//      when('/who-we-are', {
//        templateUrl: 'views/who-we-are.html'
//      }).
//      when('/how-it-works', {
//        templateUrl: 'views/how-it-works.html'
//      }).
//      when('/help', {
//        templateUrl: 'views/help.html'
//      });
});
