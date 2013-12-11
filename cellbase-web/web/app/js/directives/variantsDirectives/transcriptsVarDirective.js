myApp.directive('transcriptsVar', function () {
    return {
        restrict: 'E',
            replace: true,
        transclude: true,
        scope: {
            data: '=info',
            showMore: '=infoMore',
            specieName: '=specie'
//            changeTab: '=tabFunction'
        },
        templateUrl: './views/widgets/transcriptsVar.html'
    };
});