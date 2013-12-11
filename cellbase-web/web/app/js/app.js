var myApp = angular.module('project', []);

myApp.factory('mySharedService', function($rootScope, CellbaseService){

    var sharedService = {};

    sharedService.initSpecie =  {longName: "Homo sapiens", shortName:"hsapiens", ensemblName: "Homo_sapiens"};
    sharedService.selectedSpecies= sharedService.initSpecie;

    //--------------get the initial chromosomes---------------
    sharedService.chromAllData = CellbaseService.getSpecieChromosomes(sharedService.selectedSpecies.shortName);
    sharedService.chromNames = [];
    for (var i in sharedService.chromAllData) {
        sharedService.chromNames.push(sharedService.chromAllData[i].name);
    }

//    sharedService.sortChromosomes():
    //prepare the format for the function sort
    for (var i in sharedService.chromNames) {
        if (!isNaN(sharedService.chromNames[i])) {
            if (sharedService.chromNames[i].length == 1) {
                sharedService.chromNames[i] = 0 + sharedService.chromNames[i];
            }
        }
    }
    sharedService.chromNames = sharedService.chromNames.sort();
    //quit the format
    for (var i in sharedService.chromNames) {
        if (sharedService.chromNames[i][0] == "0") {
            sharedService.chromNames[i] = sharedService.chromNames[i].replace("0", "");
        }
    }

    //homo sapiens has two Y chromosomes, so delete the last one
    if (sharedService.selectedSpecies.shortName == "hsapiens") {
        sharedService.chromNames.pop();
    }
    //-----------------------------------------------------

    sharedService.setSpecie = function(specie){
//        $scope.specie = mySharedService.selectedSpecies;
        var chromAllData = CellbaseService.getSpecieChromosomes(specie.shortName);

        var chromNames = [];
        for (var i in chromAllData) {
            chromNames.push(chromAllData[i].name);
        }

        chromNames = this.sortChromosomes(chromNames);

        //homo sapiens has two Y chromosomes, so delete the last one
        if (specie.shortName == "hsapiens") {
            chromNames.pop();
        }

        return chromNames;
    };

    //sort the chromosomes, to use the function sort, it has to put a zero in the left if the number have one digit
    sharedService.sortChromosomes = function (chromNames) {
        for (var i in chromNames) {
            if (!isNaN(chromNames[i])) {
                if (chromNames[i].length == 1) {
                    chromNames[i] = 0 + chromNames[i];
                }
            }
        }
        chromNames = chromNames.sort();

        for (var i in chromNames) {
            if (chromNames[i][0] == "0") {
                chromNames[i] = chromNames[i].replace("0", "");
            }
        }

      return chromNames;
    };


    //------------------general events----------------------
    sharedService.broadcastSpecie = function(specie){
        this.selectedSpecies = specie;
        this.chromNames = this.setSpecie(specie);

        $rootScope.$broadcast('newSpecie');
    };
    sharedService.broadcastNew = function(specie){
        this.selectedSpecies = specie;
        this.chromNames = this.setSpecie(this.initSpecie);

        $rootScope.$broadcast('clear');
    };
    sharedService.broadcastExample = function(specie){
        this.selectedSpecies = specie;
        this.chromNames = this.setSpecie(this.initSpecie);
        $rootScope.$broadcast('example');
    };

    //========================== GENES =============================
    //from optionsBar to selectPanel
    sharedService.broadcastGenesNew = function(specie){
        this.selectedSpecies = specie;
        this.genesChromNames = this.setSpecie(this.initSpecie);
        $rootScope.$broadcast('genesClear');
    };
    //genesSelectPanel to GenesResultPanel
    sharedService.broadcastGenesNewResult = function(chromSelected, regions,genesIdFilter,biotypesFilters){
        this.chromSelected = chromSelected;
        this.regions = regions;
        this.genesIdFilter = genesIdFilter;
        this.biotypesFilter = biotypesFilters;

        if(this.genesIdFilter != ""){
            this.genesIdFilter = this.removeSpaces(this.genesIdFilter);
        }
        else if(this.regions!= ""){
            this.regions =  this.removeSpaces(this.regions );
        }

        if (this.genesIdFilter == "" && this.biotypesFilter.length == 0 && this.chromSelected.length == 0 && this.regions == "") {
            alert("No data selected");
        }
        else {
            this.regionsAndChromosomes = this.mergeChromosomesAndRegions(this.chromSelected, this.regions, this.chromAllData);
            $rootScope.$broadcast('genesNewResult');
        }
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


    //================= Variants ===================
    sharedService.broadcastVariantsNewResult = function(chromSelected, regions,snpIdFilter,conseqTypesFilter){
        this.chromSelected = chromSelected;
        this.regions = regions;
        this.snpIdFilter = snpIdFilter;
        this.conseqTypesFilter = conseqTypesFilter;

        if(this.snpIdFilter != ""){
            this.snpIdFilter = this.removeSpaces(this.snpIdFilter);
        }
        else if(this.regions!= ""){
            this.regions =  this.removeSpaces(this.regions );
        }

        if (this.snpIdFilter == "" && this.conseqTypesFilter.length == 0 && this.chromSelected.length == 0 && this.regions == "") {
            alert("No data selected");
        }
        else {
            this.regionsAndChromosomes = this.mergeChromosomesAndRegions(this.chromSelected, this.regions, this.chromAllData);
            $rootScope.$broadcast('variantsNewResult');
        }

    };

    //genesResultPanel to genesSelectPanel
    sharedService.broadcastVariantsConseqTypes = function(conseqTypes){
        this.conseqTypes= conseqTypes;
        $rootScope.$broadcast('variantsConseqTypes');
    };

    //-------------- Cheks ------------------
    sharedService.removeSpaces = function (data) {
        var espacio = data.search(" ");

        while (espacio != -1) {
            data = data.slice(0, espacio) + data.slice(espacio + 1, data.length);
            espacio = data.search(" ");
        }
        return data;
    };


    sharedService.mergeChromosomesAndRegions = function (chromSelected, regions, chromAllData) {
        var completeChromosome = true;
        var totalChromosomes = [];
        var completeRegion;

        if (regions != "") {
            regions = this.checkCorrectRegions(regions,chromAllData);
        }
        if (chromSelected.length == 0) {
            completeRegion = regions;
        }
        else if (regions.length == 0) {
            completeRegion = chromSelected.join();
        }
        else {
            //the variable regions has to be a sting to show it in an input, but for more facilities create an array with this information
            var arrayOfRegions = regions.split(",");

            //obtain the chromosomes that don't appear in a region
            for (var i in chromSelected) {
                for (var j in arrayOfRegions) {
                    if (arrayOfRegions[j].substring(0, arrayOfRegions[j].search(":")) == chromSelected[i])
                        completeChromosome = false
                }
                if (completeChromosome) {
                    totalChromosomes.push(chromSelected[i]);
                }
                completeChromosome = true;
            }
            if (totalChromosomes.length == 0) {
                completeRegion = regions;
            }
            else {
                completeRegion = totalChromosomes.join() + "," + regions;
            }
        }
        return completeRegion;
    };


    sharedService.checkCorrectRegions = function (regions, chromAllData) {
        var regions = regions.split(",");
        var correctRegions = [];
        var incorrectRegions = [];
        var chrom, start, end;
        var posDoublePoints, posLine;
        var correct = true;
        var chromExist = false;
        var messageError = "";

        for (var i in regions) {
            posDoublePoints = regions[i].search(":");
            posLine = regions[i].search("-");
            if (posDoublePoints == -1 || posLine == -1) {
                correct = false;
            }
            else {
                chrom = regions[i].slice(0, posDoublePoints);
                start = regions[i].slice(posDoublePoints + 1, posLine);
                end = regions[i].slice(posLine + 1, regions[i].length);


                //check if the chromosome exist
                for (var k in chromAllData) {
                    if (chromAllData[k].name == chrom) {
                        chromExist = true;
                    }
                }
                if (!chromExist) {
                    correct = false;
                }
                else {
                    chromExist = false;

                    //check if start and end are numbers
                    if (isNaN(start) || isNaN(end)) {
                        correct = false;
                    }
                    else if (parseInt(start) > parseInt(end)) {
                        correct = false;
                    }
                    else {
                        //check if the region is in the range
                        if (!this.checkRegionInRange(chrom, start, end, chromAllData)) {
                            correct = false;
                            alert(regions[i] + " is out of range");
                        }
                    }
                }
            }
            if (correct) {
                correctRegions.push(regions[i]);
            }
            else {
                incorrectRegions.push(regions[i]);
                correct = true;
            }
        }
        if (incorrectRegions.length != 0) {
            messageError = incorrectRegions[0];

            for (var i = 1; i < incorrectRegions.length; i++) {
                messageError = messageError + ", " + incorrectRegions[i];
            }
            messageError = messageError + " incorrect";
            alert(messageError);
        }
        return correctRegions.join();
    };

    sharedService.checkRegionInRange = function (chrom, start, end, chromAllData) {
        for (var i in chromAllData) {
            if (chromAllData[i].name == chrom) {
                if (start >= chromAllData[i].start && end <= chromAllData[i].end) {
                    return true;
                }
                else {
                    return false;
                }
            }
        }
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




    //------------------ G E N E S ------------------
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


    //---------------- V A R I A N T S -----------------
    this.getAllSNPData = function (species, regions, conseqTypesFilter) {
        var dataGet = [];
        var url;

        if (conseqTypesFilter.length == 0) {
            url = host + species + '/genomic/region/' + regions + '/snp?&of=json';
        }
        else {
            url = host + species + '/genomic/region/' + regions + '/snp?consequence_type=' + conseqTypesFilter.join() + '&of=json';
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

    //obtain all data of genes from a specie and filter by snpId or name
    this.getVariantsDataById = function (species, snpId) {
        var dataGet = [];

        $.ajax({
            url: host + species + '/feature/snp/' + snpId + '/info?&of=json',
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
});

