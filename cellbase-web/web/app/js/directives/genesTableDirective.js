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
//            'close': '&onClose',
            functionDirective: '=function'
        },
        templateUrl: './views/widgets/genes-table.html'
//        controller: function($scope, mySharedService){
//        }

    };
});