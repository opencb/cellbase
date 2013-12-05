//myApp.directive('genesGrid', function () {
//    return {
//        restrict: 'A',
//        replace: true,
//        transclude: true,
//        templateUrl: './views/widgets/genes-table.html'
//    };
//
//});


myApp.directive('genesGrid', function () {

    return {
        restrict: 'E', //Restricting as 'E' means you restrict it as an Element
        replace: true,
        transclude: true,
        //the words in capital letters will change to "-" and the lower case
        scope: {
            data: '=info',
            functionDirective: '=function'
        },
        templateUrl: './views/widgets/genes-table.html'
    };
});


//myApp.directive('genesGrid', function () {
//
//    return {
//        restrict: 'E', //Restricting as 'E' means you restrict it as an Element
//        replace: true,
//        transclude: true,
//        //the words in capital letters will change to "-" and the lower case
//        scope: {
//            data: '=info',
////            'close': '&onClose',
//            functionDirective: '&function'
//
//        },
//        templateUrl: './views/widgets/genes-table.html',
//        controller: function($scope, mySharedService){
//            $scope.geneId = 'bb';
//
//            $scope.aFn = function(arg) {
//                console.log(arg)
//
//                $scope.geneId = arg;
//
//                console.log($scope.geneId)
//                debugger
//                $scope.functionDirective(arg)
//            }
//
//            $scope.$watch('geneId', function() {
//                // do something here
//                console.log("cambio");
//
//            }, true);
//
//
////
//        }
//
//    };
//});