var optionsModule = angular.module('cellbaseWeb.options', []);

//------------GENES---------------
var  genesSelectModule = angular.module('cellbaseWeb.genesSelect', []);
var  genesResultModule = angular.module('cellbaseWeb.genesResult', []);
var  genesGVModule = angular.module('cellbaseWeb.genesGV', []);
var  genesNVModule = angular.module('cellbaseWeb.genesNV', []);

//---------VARIATIONS---------------
var  variationsSelectModule = angular.module('cellbaseWeb.variationsSelect', []);
var  variationsResultModule = angular.module('cellbaseWeb.variationsResult', []);
var  variationsGVModule = angular.module('cellbaseWeb.variationsGV', []);

//-------REGULATIONS-------------
var  regulationsSelectModule = angular.module('cellbaseWeb.regulationsSelect', []);
var  regulationsResultModule = angular.module('cellbaseWeb.regulationsResult', []);


var myApp = angular.module('cellbaseWeb',
    [
        'cellbaseWeb.options',
        'cellbaseWeb.genesSelect',
        'cellbaseWeb.genesResult',
        'cellbaseWeb.genesGV',
        'cellbaseWeb.genesNV',
        'cellbaseWeb.variationsSelect',
        'cellbaseWeb.variationsResult',
        'cellbaseWeb.variationsGV',
        'cellbaseWeb.regulationsSelect',
        'cellbaseWeb.regulationsResult'
    ]);


myApp.factory('mySharedService', function($rootScope, CellbaseService){

    var sharedService = {};


    sharedService.initSpecie =  {longName: "Homo sapiens", shortName:"hsapiens", ensemblName: "Homo_sapiens"};

    //-----------genes--------------
    sharedService.genesSpecie= sharedService.initSpecie;
    sharedService.regionsAndChromosomesGenes = "20:32850000-33500000";
    sharedService.genesSpecieGV= sharedService.initSpecie;
    sharedService.genesIdFilter = "";
    sharedService.biotypesFilter = [];
    this.geneProteinId = "";
    this.proteinsIdLinks = [];

    //-----------variants------------
    sharedService.variantsSpecie= sharedService.initSpecie;
    sharedService.regionsAndChromosomesVariants = "20:32850000-32860000";
    sharedService.variantsSpecieGV= sharedService.initSpecie;
    sharedService.snpIdFilter = "";
    sharedService.conseqTypesFilter = [];

    //---------regulations-----------
    sharedService.regulationsSpecie= sharedService.initSpecie;
    sharedService.regionsAndChromosomesRegulations = "3:555-622666";
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


        if(specie.data.search("variation") == -1){
            //disable variation tab
            if(!$('#variationDiv').hasClass("disabled")){
                $('#variationDiv').addClass("disabled");
            }
        }
        else{
            //enable variation tab
            if($('#variationDiv').hasClass("disabled")){
                $('#variationDiv').removeClass("disabled");
            }
        }

        if(specie.data.search("regulation") == -1){
            //disable regulation tab
            if(!$('#regulationDiv').hasClass("disabled")){
                $('#regulationDiv').addClass("disabled");
            }
        }
        else{
            //enable regulation tab
            if($('#regulationDiv').hasClass("disabled")){
                $('#regulationDiv').removeClass("disabled");
            }
        }



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
    sharedService.broadcastGeneProteinsToNV = function(geneProteinId,proteinsIdLinks){
        this.geneProteinId = geneProteinId;
        this.proteinsIdLinks = proteinsIdLinks;

        $rootScope.$broadcast('geneProteins');
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


//--------------------
//        if(this.featureClassFilter.length != 0){
//            this.featureClassFilter = this.removeSpaces(this.featureClassFilter);
//        }
//        else if(this.regions!= ""){
//            this.regions =  this.removeSpaces(this.regions);
//        }

        //-----------
        if(this.regions!= ""){
            this.regions =  this.removeSpaces(this.regions);
        }
        //---------


        if (this.featureClassFilter == "" && this.chromSelected.length == 0 && this.regions == "") {
            alert("No data selected");
        }
        else {

            this.regionsAndChromosomesRegulations = this.regions;  //por ahora
            //this.regionsAndChromosomesRegulations = this.mergeChromosomesAndRegions(this.chromSelected, this.regions, this.chromAllData);
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

