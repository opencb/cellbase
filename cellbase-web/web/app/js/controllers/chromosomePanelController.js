var chromosomePanelControl = myApp.controller('chromosomePanelController', ['$scope', 'mySharedService', 'CellbaseService', function ($scope, mySharedService, CellbaseService) {

    $scope.$on('specieBroadcast', function () {   //obtener la especie elegida en optionsBar
        $scope.species = mySharedService.selectedSpecies;
    });

    $scope.$on('newResult', function () {   //obtener la especie elegida en optionsBar
        //añadimos el div en el que pondremos el chromosoma
        var chromosomeDiv = $('#chromosome-div')[0];
        var chrom = $('<div id="chromosome"></div>')[0];

        $(chromosomeDiv).append(chrom);


//        $scope.drawChromosomePanel($(chrom).attr('id'));

    });


    $scope.chromosomePanelConfig = {
        collapsed: false,
        collapsible: true
    }

    $scope.drawChromosomePanel = function (targetId) {

        console.log(targetId);

//        var _this = this;
        $scope.regions = mySharedService.selectedRegions;


        var chromosomePanel = new ChromosomePanel({
            targetId: targetId,
            autoRender: true,
            width: '100%', //  width: this.width - this.sidePanelWidth,
            height: 65,
            species: $scope.species, // species: this.species,
            title: 'Chromosome',
            collapsed: $scope.chromosomePanelConfig.collapsed, // collapsed: this.chromosomePanelConfig.collapsed,
            collapsible: $scope.chromosomePanelConfig.collapsible,  // collapsible: this.chromosomePanelConfig.collapsible,
            region: $scope.regions   // region: this.region
//            handlers: {
//                'region:change': function (event) {
//                    _this.trigger('region:change', event);
//                }
//            }
        });

//        this.on('region:change region:move', function (event) {
//            if (event.sender != chromosomePanel) {
//                chromosomePanel.setRegion(event.region);
//            }
//        });
//
//        this.on('width:change', function (event) {
//            chromosomePanel.setWidth(event.width - _this.sidePanelWidth);
//        });
//
//        this.on('species:change', function (event) {
//            chromosomePanel.setSpecies(event.species);
//        });

        chromosomePanel.draw();

//        return chromosomePanel;
    };

}]);


chromosomePanelControl.$inject = ['$scope', 'mySharedService'];

