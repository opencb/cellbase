myApp.directive('genesNetworkViewer', function () {
    return {
        restrict: 'E',
        replace: false,
        transclude: true,
        scope: {
            targetId: '@id'
        },
        controller: function($scope, $rootScope) {

            $scope.$on($scope.targetId + ':geneProteins', function (event, geneProteinId, proteinsIdLinks) {
//                $scope.networkViewer.networkSvgLayout.createVertex(40,40);
//
//                for(var i in proteinsIdLinks){
//                    $scope.networkViewer.networkSvgLayout.createVertex(40+(10*i), 80);
//                }
            });

            $scope.networkViewer = new NetworkViewer({
                targetId: $scope.targetId,
                autoRender: true,
                sidePanel: false,
                overviewPanel: false
            });
            $scope.networkViewer.draw();
//            $scope.networkViewer.networkSvgLayout.createVertex(200,300);
        }
    }
});