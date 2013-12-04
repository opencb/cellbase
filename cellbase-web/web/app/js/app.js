var myApp = angular.module('project', []);

myApp.factory('mySharedService', function($rootScope){

    var sharedService = {};

    sharedService.selectedSpeciesInit=  {longName: "Homo sapiens", shortName:"hsapiens", ensemblName: "Homo_sapiens"};
    sharedService.selectedSpecies=  {longName: "Homo sapiens", shortName:"hsapiens", ensemblName: "Homo_sapiens"};
    //region obtained by the drawn chromosome

    //comunicate specie from optionsBar to genesSelectPanel
    sharedService.broadcastSpecie = function(specie){
        this.selectedSpecies = specie;
        $rootScope.$broadcast('newSpecie');
    };
    //comunicate a new result
    sharedService.broadcastNew = function(specie){
        this.selectedSpecies = specie;
        $rootScope.$broadcast('new');
    };

    //comunicate to make an example
    sharedService.broadcastExample = function(specie){
        this.selectedSpecies = specie;
        $rootScope.$broadcast('example');
    };

    //comunicate a new result from genesSelectPanel to GenesResultPanel
    sharedService.newResult = function(chromosomes, regionsAndChromosomes, genesIdFilter,biotypeFilter){
        this.chromosomes = chromosomes;
        this.regionsAndChromosomes = regionsAndChromosomes;
        this.genesIdFilter = genesIdFilter;
        this.biotypeFilter = biotypeFilter;

        $rootScope.$broadcast('newResult');
    };

    //comunicate biotypes from genesResultPanel to genesSelectPanel
    sharedService.biotypesNames = function(biotypes){
        this.biotypes= biotypes;
        $rootScope.$broadcast('biotypes');
    };

    sharedService.addRegionFromChromosome = function(region){
        this.regionFromChromosome = region;
        $rootScope.$broadcast('newRegion');
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
    //                console.time("guardar los datos");
                    for(var i in data.response){
                        for(var j in data.response[i].result){
                            dataGet.push(data.response[i].result[j]);
                        }
                    }
    //                console.timeEnd("guardar los datos");
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

