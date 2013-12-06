myApp.directive('transcripts', function () {
    return {
        restrict: 'E', //Restricting as 'E' means you restrict it as an Element
            replace: true,
        transclude: true,
        //the words in capital letters will change to "-" and the lower case
        scope: {
            data: '=info',
            showMore: '=infoMore',
            specieName: '=specie',
            changeTab: '=tabFunction'
        },
        templateUrl: './views/widgets/transcripts.html'
    };
});