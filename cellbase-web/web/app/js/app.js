var myApp = angular.module('project', []);

myApp.factory('mySharedService', function($rootScope){

    var sharedService = {};

    sharedService.selectedSpecies=  {longName: "Homo sapiens", shortName:"hsapiens", ensemblName: "Homo_sapiens"};

    //------------------general events----------------------
    sharedService.broadcastSpecie = function(specie){
        this.selectedSpecies = specie;
        $rootScope.$broadcast('newSpecie');
    };
    sharedService.broadcastNew = function(specie){
        this.selectedSpecies = specie;
        $rootScope.$broadcast('clear');
    };
    sharedService.broadcastExample = function(specie){
        this.selectedSpecies = specie;
        $rootScope.$broadcast('example');
    };


    //========================== GENES =============================
    //from optionsBar to selectPanel
    sharedService.broadcastGenesNew = function(specie){
        this.selectedSpecies = specie;
        $rootScope.$broadcast('genesClear');
    };
    //genesSelectPanel to GenesResultPanel
    sharedService.broadcastGenesNewResult = function(chromosomes, regionsAndChromosomes, genesIdFilter,biotypeFilter){
        this.chromosomes = chromosomes;
        this.regionsAndChromosomes = regionsAndChromosomes;
        this.genesIdFilter = genesIdFilter;
        this.biotypeFilter = biotypeFilter;
        $rootScope.$broadcast('genesNewResult');
    };
    //genesResultPanel to genesSelectPanel
    sharedService.broadcastGenesBiotypes = function(biotypes){
        this.biotypes= biotypes;
        $rootScope.$broadcast('genesBiotypes');
    };
    //genesgvDirective to genesSelectPanel
    sharedService.broadcastGenesRegionGV = function(region){
        this.regionFromGV = region;
        $rootScope.$broadcast('genesRegionGV');
    };
    //genesgvDirective to optionsBar
    sharedService.broadcastGenesSpecieGV = function(specie){
        this.selectedSpecies = specie;
        $rootScope.$broadcast('genesSpecieGV');
    };

    return sharedService;
})

myApp.service('CellbaseService', function () {

    var host = 'http://ws-beta.bioinfo.cipf.es/cellbase/rest/v3/';

    ////Not implemeneted yet
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
    //obtain the chromosomes of a specie
    this.getSpecieChromosomes = function (specie) {
        var dataGet;

        $.ajax({
            url: host + specie + '/genomic/chromosome/all?of=json',
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
    //obtain genes and transcripts from regions of a specie and filter by biotypes
    this.getGenesAndTranscripts = function (species, regions, biotypesFilter) {
        var dataGet = [];
        var url;

        if (biotypesFilter.length == 0) {
            url = host + species + '/genomic/region/' + regions + '/gene?exclude=transcripts.xrefs,transcripts.exons,transcripts.tfbs&of=json';
        }
        else {
            url = host + species + '/genomic/region/' + regions + '/gene?biotype=' + biotypesFilter.join() + '&exclude=transcripts.xrefs,transcripts.exons,transcripts.tfbs&of=json';
        }

        $.ajax({
            url: url,
            async: false,
            dataType: 'json',
            success: function (data, textStatus, jqXHR) {

                if(data != null){
                    for(var i in data.response){
                        for(var j in data.response[i].result){
                            dataGet.push(data.response[i].result[j]);
                        }
                    }
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
            }
        });
        return dataGet;
    };
    //obtain genes and transcripts from a specie and filter by geneId or name
    this.getGenesAndTranscriptsByIdOrName = function (species, geneId) {
        var dataGet = [];

        $.ajax({
            url: host + species + '/feature/gene/' + geneId + '/info?exclude=transcripts.xrefs,transcripts.exons,transcripts.tfbs&of=json',
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
    //obtain all data of genes from a specie and filter by geneId or name
    this.getGenesAllDataById = function (species, geneId) {
        var dataGet = [];

        $.ajax({
            url: host + species + '/feature/gene/' + geneId + '/info?&of=json',
            async: false,
            dataType: 'json',
            success: function (data, textStatus, jqXHR) {
                dataGet = data.response[0].result[0];
            },
            error: function (jqXHR, textStatus, errorThrown) {
            }
        });
        return dataGet;
    };
});

