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




    //------------------ G E N E S ------------------
    //obtain genes and transcripts from regions of a specie and filter by biotypes
    this.getGenesAndTranscripts = function (specie, regions, biotypesFilter) {
        var dataGet = [];
        var url;

        if (biotypesFilter.length == 0) {
            url = host + specie + '/genomic/region/' + regions + '/gene?exclude=transcripts.xrefs,transcripts.exons,transcripts.tfbs&of=json';
        }
        else {
            url = host + specie + '/genomic/region/' + regions + '/gene?biotype=' + biotypesFilter.join() + '&exclude=transcripts.xrefs,transcripts.exons,transcripts.tfbs&of=json';
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
    this.getGenesAndTranscriptsByIdOrName = function (specie, geneId) {
        var dataGet = [];

        $.ajax({
            url: host + specie + '/feature/gene/' + geneId + '/info?exclude=transcripts.xrefs,transcripts.exons,transcripts.tfbs&of=json',
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
    this.getGenesAllDataById = function (specie, geneId) {
        var dataGet = [];

        $.ajax({
            url: host + specie + '/feature/gene/' + geneId + '/info?&of=json',
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

    this.getProteinsLinks = function (specie, geneName) {


        var dataGet = [];
        var url = host + specie + '/network/protein/all?interactor=' + geneName + '&of=json';

        $.ajax({
            url: url,
            async: false,
            dataType: 'json',
            success: function (data, textStatus, jqXHR) {
                if(data != null){
                    for(var i in data.response.result){
                        dataGet.push(data.response.result[i]);
                    }
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
            }
        });

        return dataGet;
    };

    //obtain all data of genes from a specie and filter by snpId or name
    this.getBiotypes = function (specie) {
        var dataGet = [];

        $.ajax({
            url: host + specie + '/feature/gene/biotypes',
            async: false,
            dataType: 'json',
            success: function (data, textStatus, jqXHR) {

                if(data != null){
                    for (var i in data.response.result[0].biotypes) {
                        dataGet.push(data.response.result[0].biotypes[i]);
                    }
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
            }
        });


        return dataGet;
    };

    //---------------- V A R I A N T S -----------------
    this.getAllSNPData = function (specie, regions, conseqTypesFilter) {
        var dataGet = [];
        var url;

        if (conseqTypesFilter.length == 0) {
            url = host + specie + '/genomic/region/' + regions + '/snp?';
        }
        else {
            url = host + specie + '/genomic/region/' + regions + '/snp?consequence_type=' + conseqTypesFilter.join() + '&';
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

    this.getAllSNPDataPaginated = function (specie, regions, conseqTypesFilter, page) {
        var dataGet = [];
        var url;

        if (conseqTypesFilter.length == 0) {
            url = host + specie + '/genomic/region/' + regions + '/snp?limit=10&skip='+(page-1)*10;
//            url = host + specie + '/genomic/region/' + regions + '/snp?&of=json';
        }
        else {
            url = host + specie + '/genomic/region/' + regions + '/snp?limit=10&skip='+(page-1)*10+'&consequence_type=' + conseqTypesFilter.join();
        }

        $.ajax({
            url: url,
            async: false,
            dataType: 'json',
            success: function (data, textStatus, jqXHR) {


                //falta que los datos los devuelva todos juntos

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
    this.getCountSNPData = function (specie, regions) {
        var numResults;
        var url;
            url = host + specie + '/genomic/region/' + regions + '/snp?count=true';
//            url = host + specie + '/genomic/snp/consequenceTypes';

        $.ajax({
            url: url,
            async: false,
            dataType: 'json',
            success: function (data, textStatus, jqXHR) {

                if(data != null){
                    numResults = data.response[0].result[0].count;
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
            }
        });


        return numResults;
    };

    //obtain all data of genes from a specie and filter by snpId or name
    this.getVariantsDataById = function (specie, snpId) {
        var dataGet = [];

        $.ajax({
            url: host + specie + '/feature/snp/' + snpId + '/info?',
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
    //obtain all data of genes from a specie and filter by snpId or name
    this.getConsequenceTypes = function (specie) {
        var dataGet = [];

        $.ajax({
            url: host + specie + '/feature/snp/consequence_types',
            async: false,
            dataType: 'json',
            success: function (data, textStatus, jqXHR) {

                if(data != null){
                    for (var i in data.response.result[0].consequenceTypes) {
                        dataGet.push(data.response.result[0].consequenceTypes[i]);
                    }
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
            }
        });


        return dataGet;
    };


    //------------- R E G U L A T I O N S -----------------
    this.getAllRegulationsData = function (specie, regions, featureClassFilter) {


        var dataGet = [];
        var url;

        if (featureClassFilter.length == 0) {
            url = host + specie + '/genomic/region/' + regions + '/regulatory?&of=json';
        }
       else {
             url = host + specie + '/genomic/region/' + regions + '/feature?featureType='+ $featureClass.join() +'&of=json';
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

});

