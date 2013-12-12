myApp.directive('genesNetworkViewer', function () {
    return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: './views/genes-nv.html',
        controller: function($scope,mySharedService) {

        }
    }
});