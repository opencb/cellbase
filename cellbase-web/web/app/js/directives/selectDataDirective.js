myApp.directive("select", function () {
    return function (scope, element) {


        element.bind("mousedown", function () {
        console.log(scope);
        console.log(element);
//
//
//            if(element.text().search("-") == -1)
//            {
//                element.addClass('cw-text-blue');
//
//                element.text('-' + element.text());
//            }
//            else{
//                element.removeClass('cw-text-blue');
//
//                element.text(element.text().substr(1,element.text().length));
//            }
        });
    };
});

