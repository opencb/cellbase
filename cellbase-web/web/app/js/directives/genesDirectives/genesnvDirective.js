myApp.directive('genesNetworkViewer', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: './views/genes-nv.html',
        controller: function($scope,mySharedService) {

            $scope.networkViewer = new NetworkViewer({
                targetId: 'network-viewer-div',
                autoRender: true,
                sidePanel: false,
                overviewPanel: false
            });
            $scope.networkViewer.draw();

        }
    }
});