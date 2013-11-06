
myApp.directive('genePanel', function () {    //cambia -letra por letraMayuscula
    return {   //se devuelve a si mismo como un objeto
        restrict: 'A',//Define el tipo de elemento que queremos que se transforme en el DOM: Restricting as 'A' means you restrict it as an Attribute. 'E' for Element, 'C' for Class and 'M' for Comment. These have a default as 'EA'.
        replace: true,  //indicamos si queremos que se reemplace o no por ese elemento
        transclude: true,  // allows for existing DOM content to be copied into the directive, por ejemplo el texto del elemento
        templateUrl: './views/widgets/gene-panel.html',//... le pasariamos el fichero donde se encuentra lo que queremos reemplazar
        link: function (scope, element, attrs) {
            // DOM manipulation/events here!
//                console.log(scope);
//                console.log(element);
//                console.log(attrs);
        }
    };
});