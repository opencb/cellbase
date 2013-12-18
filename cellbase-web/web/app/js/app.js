var myApp = angular.module('project', []);

myApp.factory('mySharedService', function($rootScope, CellbaseService){

    var sharedService = {};

    sharedService.initSpecie =  {longName: "Homo sapiens", shortName:"hsapiens", ensemblName: "Homo_sapiens"};

    //-----------genes--------------
    sharedService.genesSpecie= sharedService.initSpecie;
    sharedService.regionsAndChromosomesGenes = "20:32850000-33500000";
    sharedService.genesSpecieGV= sharedService.initSpecie;
    sharedService.genesIdFilter = "";
    sharedService.biotypesFilter = [];

    //-----------variants------------
    sharedService.variantsSpecie= sharedService.initSpecie;
    sharedService.regionsAndChromosomesVariants = "20:32850000-32860000";
    sharedService.variantsSpecieGV= sharedService.initSpecie;
    sharedService.snpIdFilter = "";
    sharedService.conseqTypesFilter = [];

    //---------regulations-----------
    sharedService.regulationsSpecie= sharedService.initSpecie;
    sharedService.regionsAndChromosomesRegulations = "20:32850000-32860000";
    sharedService.featureClassFilter = [];


    sharedService.getChromNamesSpecie = function(specie){
//        $scope.specie = mySharedService.selectedSpecies;
        this.chromAllData = CellbaseService.getSpecieChromosomes(specie.shortName);

        var chromNames = [];
        for (var i in this.chromAllData) {
            chromNames.push(this.chromAllData[i].name);
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

    //the initial chromosomes
    sharedService.chromNames = sharedService.getChromNamesSpecie(sharedService.genesSpecie);


    //------------------general events----------------------
    sharedService.broadcastSpecie = function(specie){
        this.genesSpecie = specie;
        this.variantsSpecie = specie;
        this.regulationsSpecie = specie;
        this.chromNames = this.getChromNamesSpecie(specie);

        $rootScope.$broadcast('newSpecie');
//        $rootScope.$broadcast('genesRegionToGV');

    };
    sharedService.broadcastNew = function(specie){
        this.genesSpecie = specie;
        this.variantsSpecie = specie;
        this.regulationsSpecie = specie;
        this.chromNames = this.getChromNamesSpecie(this.initSpecie);

        $rootScope.$broadcast('clear');
    };
    sharedService.broadcastExample = function(specie){
        this.genesSpecie = specie;
        this.variantsSpecie = specie;
        this.regulationsSpecie = specie;
        this.chromNames = this.getChromNamesSpecie(this.initSpecie);
        $rootScope.$broadcast('example');
    };

    //========================== GENES =============================
    //from optionsBar to selectPanel
    sharedService.broadcastGenesNew = function(specie){
        this.genesSpecieGV = specie;

        this.genesChromNames = this.getChromNamesSpecie(specie);
        $rootScope.$broadcast('genesNewSpecieGV');
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
            this.regionsAndChromosomesGenes = this.mergeChromosomesAndRegions(this.chromSelected, this.regions, this.chromAllData);
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
        this.genesSpecieGV = specie;
        $rootScope.$broadcast('genesSpecieGV');
    };
    sharedService.broadcastGenesRegionToGV = function(region){
        this.genesRegionToGV = region;

        $rootScope.$broadcast('genesRegionToGV');
    };


    //================= Variants ===================
    sharedService.broadcastVariantsNew = function(specie){
        this.variantsSpecieGV = specie;

        this.variantsChromNames = this.getChromNamesSpecie(specie);
        $rootScope.$broadcast('variantsNewSpecieGV');
    };
    sharedService.broadcastVariantsNewResult = function(chromSelected, regions,snpIdFilter,conseqTypesFilter){
        this.chromSelected = chromSelected;
        this.regions = regions;
        this.snpIdFilter = snpIdFilter;
        this.conseqTypesFilter = conseqTypesFilter;

        if(this.snpIdFilter != ""){
            this.snpIdFilter = this.removeSpaces(this.snpIdFilter);
        }
        else if(this.regions!= ""){
            this.regions =  this.removeSpaces(this.regions);
        }

        if (this.snpIdFilter == "" && this.conseqTypesFilter.length == 0 && this.chromSelected.length == 0 && this.regions == "") {
            alert("No data selected");
        }
        else {

            this.regionsAndChromosomesVariants = this.mergeChromosomesAndRegions(this.chromSelected, this.regions, this.chromAllData);
            $rootScope.$broadcast('variantsNewResult');
        }

    };

    sharedService.broadcastVariantsConseqTypes = function(conseqTypes){
        this.conseqTypes= conseqTypes;
        $rootScope.$broadcast('variantsConseqTypes');
    };
    sharedService.broadcastVariantsRegionGV = function(region){
        this.regionFromGV = region;
        $rootScope.$broadcast('variantsRegionGV');
    };
    sharedService.broadcastVariantsSpecieGV = function(specie){
        this.variantsSpecieGV = specie;
        $rootScope.$broadcast('variantsSpecieGV');
    };

    sharedService.broadcastVariantsRegionToGV = function(region){
        this.variantsRegionToGV = region;

        $rootScope.$broadcast('variantsRegionToGV');
    };


    //================= Regulations ===================
    sharedService.broadcastRegulationsNewResult = function(chromSelected, regions,featureClassFilter){
        this.chromSelected = chromSelected;
        this.regions = regions;
        this.featureClassFilter = featureClassFilter;

        if(this.featureClassFilter.length != 0){
            this.featureClassFilter = this.removeSpaces(this.featureClassFilter);
        }
        else if(this.regions!= ""){
            this.regions =  this.removeSpaces(this.regions);
        }

        if (this.featureClassFilter == "" && this.chromSelected.length == 0 && this.regions == "") {
            alert("No data selected");
        }
        else {
            this.regionsAndChromosomesRegulations = this.mergeChromosomesAndRegions(this.chromSelected, this.regions, this.chromAllData);
            $rootScope.$broadcast('regulationsNewResult');
        }
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


    //------------- R E G U L A T I O N S -----------------
    this.getAllRegulationsData = function (species, regions, featureClassFilter) {
        var dataGet = [];
        var url;

        if (featureClassFilter.length == 0) {
            url = host + species + '/genomic/region/' + regions + '/regulatory?&of=json';
        }
       else {
             url = host + species + '/genomic/region/' + regions + '/feature?featureType='+ $featureClass.join() +'&of=json';
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

