myApp.directive('transcriptsGrid', function () {    //the capital letters are replaced by - and the lowercase of the letter
    return {
        restrict: 'A',//the element that we want it to transform in DOM
        replace: true,  //we want to replace
        transclude: true,  // allows for existing DOM content to be copied into the directive
        templateUrl: './views/widgets/transcripts-table.html'//file where is the code to replace the div that have this directive
//        link: function (scope, element, attrs) {// DOM manipulation/events here!
//        }
    };
});