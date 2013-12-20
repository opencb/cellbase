myApp.directive('variantsGenomeViewer', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: './views/variants-gv.html',
        controller: function($scope,mySharedService) {

            CELLBASE_HOST = "http://ws-beta.bioinfo.cipf.es/cellbase/rest";
            CELLBASE_VERSION = "v3";

            $scope.broadcastRegion = true;

            $scope.$on('variantsRegionToGV', function () {

                if(mySharedService.genesSpecie.shortName == "hsapiens" || mySharedService.genesSpecie.shortName == "mmusculus"){
                    $scope.broadcastRegion = false;
                    $scope.genomeViewer.setRegion(new Region(mySharedService.variantsRegionToGV))
                }
            });


//  genomeViewer.setRegion(new Region('13:32889542-32889680'))

            /* region and species configuration */
            var region = new Region({
                chromosome: "13",
                start: 32889611,
                end: 32889611
            });
            var availableSpecies = {
                "text": "Species",
                "items": [{
                    "text": "Vertebrates",
                    "items": [{
                        "text": "Homo sapiens",
                        "assembly": "GRCh37.p10",
                        "region": {
                            "chromosome": "13",
                            "start": 32889611,
                            "end": 32889611
                        },
                        "chromosomes": ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "MT"],
                        "url": "ftp://ftp.ensembl.org/pub/release-71/"
                    }, {
                        "text": "Mus musculus",
                        "assembly": "GRCm38.p1",
                        "region": {
                            "chromosome": "1",
                            "start": 18422009,
                            "end": 18422009
                        },
                        "chromosomes": ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "X", "Y", "MT"],
                        "url": "ftp://ftp.ensembl.org/pub/release-71/"
                    }
                    ]
                }
                ]
            };
            var species = availableSpecies.items[0].items[0];

            $scope.genomeViewer = new GenomeViewer({
                targetId: 'variants-gv-div',
                region: region,
                availableSpecies: availableSpecies,
                species: species,
                sidePanel: false,
                autoRender: true,
                border: true,
                resizable: true,
                //        quickSearchResultFn:quickSearchResultFn,
                //        quickSearchDisplayKey:,
                karyotypePanelConfig: {
                    collapsed: false,
                    collapsible: true
                },
                chromosomePanelConfig: {
                    collapsed: false,
                    collapsible: true
                },
                handlers:{
                    'region:change':function(event){
                        if(mySharedService.genesSpecie.shortName == "hsapiens" || mySharedService.genesSpecie.shortName == "mmusculus"){
                                if( $scope.broadcastRegion){
                                mySharedService.broadcastVariantsRegionGV(event.region.chromosome + ":" + event.region.start + "-" + event.region.end);
                            }
                            $scope.broadcastRegion = true;
                        }
                    },
                    'region:move':function(event){
                        if(mySharedService.genesSpecie.shortName == "hsapiens" || mySharedService.genesSpecie.shortName == "mmusculus"){
                            mySharedService.broadcastVariantsRegionGV(event.region.chromosome + ":" + event.region.start + "-" + event.region.end);
                        }
                    },
//                    'chromosome-button:change':function(event){
//                    },
                    'species:change':function(event){
                        mySharedService.broadcastVariantsSpecieGV(event.species.text);
                    }
                }
                //        chromosomeList:[]
                //            trackListTitle: ''
//                            drawNavigationBar = true;
                //            drawKaryotypePanel: false,
//                            drawChromosomePanel: false,
                //            drawRegionOverviewPanel: false
            }); //the div must exist

            $scope.genomeViewer.draw();

            tracks = [];
            $scope.sequence = new SequenceTrack({
                targetId: null,
                id: 1,
                //        title: 'Sequence',
                height: 30,
                visibleRegionSize: 200,

                renderer: new SequenceRenderer(),

                dataAdapter: new SequenceAdapter({
                    category: "genomic",
                    subCategory: "region",
                    resource: "sequence",
                    species: $scope.genomeViewer.species
                })
            });

            tracks.push($scope.sequence);

            $scope.gene = new GeneTrack({
                targetId: null,
                id: 2,
                title: 'Gene',
                minHistogramRegionSize: 20000000,
                maxLabelRegionSize: 10000000,
                minTranscriptRegionSize: 200000,
                height: 140,

                renderer: new GeneRenderer(),

                dataAdapter: new CellBaseAdapter({
                    category: "genomic",
                    subCategory: "region",
                    resource: "gene",
                    species: $scope.genomeViewer.species,
                    params: {
                        exclude: 'transcripts.tfbs,transcripts.xrefs,transcripts.exons.sequence'
                    },
                    cacheConfig: {
                        chunkSize: 50000
                    }
                })
            });

            tracks.push($scope.gene);

            var renderer = new FeatureRenderer(FEATURE_TYPES.gene);
            renderer.on({
                'feature:click': function(event) {
                    // feature click event example
                    console.log(event)

                }
            });
            var gene = new FeatureTrack({
                targetId: null,
                id: 2,
                //        title: 'Gene',
                minHistogramRegionSize: 20000000,
                maxLabelRegionSize: 10000000,
                height: 100,

                renderer: renderer,

                dataAdapter: new CellBaseAdapter({
                    category: "genomic",
                    subCategory: "region",
                    resource: "gene",
                    params: {
                        exclude: 'transcripts'
                    },
                    species: $scope.genomeViewer.species,
                    cacheConfig: {
                        chunkSize: 50000
                    }
                })
            });
            $scope.genomeViewer.addOverviewTrack(gene);

            $scope.snp = new FeatureTrack({
                targetId: null,
                id: 4,
                title: 'SNP',
                featureType: 'SNP',
                minHistogramRegionSize: 12000,
                maxLabelRegionSize: 3000,
                height: 100,

                renderer: new FeatureRenderer(FEATURE_TYPES.snp),

                dataAdapter: new CellBaseAdapter({
                    category: "genomic",
                    subCategory: "region",
                    resource: "snp",
                    params: {
                        exclude: 'transcriptVariations,xrefs,samples'
                    },
                    species: $scope.genomeViewer.species,
                    cacheConfig: {
                        chunkSize: 10000
                    }
                })
            });
            tracks.push($scope.snp);

            //    /***************************************/
            //    var geneEnsembl = new FeatureTrack({
            //        targetId: null,
            //        id: 5,
            //        title: 'Gene Ensembl',
            //        minHistogramRegionSize: 20000000,
            //        maxLabelRegionSize: 10000000,
            //        height: 100,
            //        titleVisibility: 'hidden',
            //        featureTypes: FEATURE_TYPES,
            //
            //        renderer: new FeatureRenderer('gene'),
            //
            //        dataAdapter: new EnsemblAdapter({
            //            category: "feature",
            //            subCategory: "region",
            //            params: {
            //                feature: 'gene'
            //            },
            //            species: 'human',
            //            cacheConfig: {
            //                chunkSize: 50000
            //            }
            //        })
            //    });
            //    tracks.push(geneEnsembl);
            //    /***************************************/
            $scope.genomeViewer.addTrack(tracks);
        }
    }
});