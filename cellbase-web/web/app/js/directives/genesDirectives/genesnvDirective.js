myApp.directive('genesNetworkViewer', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: './views/genes-nv.html',
        controller: function($scope,mySharedService) {


            $scope.$on('geneProteins', function () {

                $scope.networkViewer.networkSvgLayout.createVertex(40,40);

                for(var i in mySharedService.proteinsIdLinks){
                    $scope.networkViewer.networkSvgLayout.createVertex(40+(10*i), 80);
//                    $scope.networkViewer.networkSvgLayout.createEdge(5,i+5);
                }

            });



            $scope.networkViewer = new NetworkViewer({
                targetId: 'network-viewer-div',
                autoRender: true,
                sidePanel: false,
                overviewPanel: false
            });
            $scope.networkViewer.draw();
            $scope.networkViewer.networkSvgLayout.createVertex(200,300);
        }
    }
});