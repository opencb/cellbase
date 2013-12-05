/*! Genome Viewer - v1.0.3 - 2013-12-04 18:49:15
* http://https://github.com/opencb/jsorolla/
* Copyright (c) 2013  Licensed GPLv2 */
/*! Genome Viewer - v1.0.3 - 2013-12-04 18:49:15
* http://https://github.com/opencb/jsorolla/
* Copyright (c) 2013  Licensed GPLv2 */
var Utils = {
        //properties
        characters: "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",

        //Methods
        formatNumber: function (position) {
            return position.toString().replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,");
        },
        formatText: function (text, spaceChar) {
            text = text.replace(new RegExp(spaceChar, "gi"), " ");
            text = text.charAt(0).toUpperCase() + text.slice(1);
            return text;
        },
        isFunction: function (s) {
            return typeof(s) === 'function' || s instanceof Function;
        },
        parseDate: function (strDate) {
            return strDate.substring(0, 4) + " " + strDate.substring(4, 6) + " " + strDate.substring(6, 8) + ", " + strDate.substring(8, 10) + ":" + strDate.substring(10, 12) + ":" + strDate.substring(12, 14);
        },
        genId: function (prefix) {
            prefix = prefix || '';
            prefix = prefix.length == 0 ? prefix : prefix + '-';
            return prefix + this.randomString();
        },
        randomString: function (length) {
            length = length || 10;
            var str = "";
            for (var i = 0; i < length; i++) {
                str += this.characters.charAt(this.getRandomInt(0, this.characters.length - 1));
            }
            return str;
        },
        getRandomInt: function (min, max) {
            // https://developer.mozilla.org/en-US/docs/JavaScript/Reference/Global_Objects/Math/random
            // Using Math.round() will give you a non-uniform distribution!
            return Math.floor(Math.random() * (max - min + 1)) + min;
        },
        endsWithIgnoreCase: function (str, test) {
            var regex = new RegExp('^.*\\.(' + test + ')$', 'i');
            return regex.test(str);
        },
        endsWith: function (str, test) {
            var regex = new RegExp('^.*\\.(' + test + ')$');
            return regex.test(str);
        },
        addQueryParamtersToUrl: function (paramsWS, url) {
            var chr = "?";
            if (url.indexOf("?") != -1) {
                chr = "&";
            }
            var query = "";
            for (var key in paramsWS) {
                if (paramsWS[key] != null)
                    query += key + "=" + paramsWS[key].toString() + "&";
            }
            if (query != "")
                query = chr + query.substring(0, query.length - 1);
            return url + query;
        },
        randomColor: function () {
            var color = "";
            for (var i = 0; i < 6; i++) {
                color += ([0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 'a', 'b', 'c', 'd', 'e', 'f'][Math.floor(Math.random() * 16)]);
            }
            return "#" + color;
        },
        colorLuminance: function (hex, lum) {
            // validate hex string
            hex = String(hex).replace(/[^0-9a-f]/gi, '');
            hex = String(hex).replace(/#/gi, '');
            if (hex.length < 6) {
                hex = hex[0] + hex[0] + hex[1] + hex[1] + hex[2] + hex[2];
            }
            lum = lum || 0;

            // convert to decimal and change luminosity
            var rgb = "#", c, i;
            for (i = 0; i < 3; i++) {
                c = parseInt(hex.substr(i * 2, 2), 16);
                c = Math.round(Math.min(Math.max(0, c + (c * lum)), 255)).toString(16);
                rgb += ("00" + c).substr(c.length);
            }

            return rgb;
        },
        getSpeciesFromAvailable: function (availableSpecies, speciesCode) {
            for (var i = 0; i < availableSpecies.items.length; i++) {
                var phylos = availableSpecies.items[i].items;
                for (var j = 0; j < phylos.length; j++) {
                    var species = phylos[j];
                    if (this.getSpeciesCode(species.text) == speciesCode) {
                        return species;
                    }
                }
            }
        },
        getSpeciesCode: function (speciesName) {
            var pair = speciesName.split(" ");
            var code;
            if (pair.length < 3) {
                code = (pair[0].charAt(0) + pair[1]).toLowerCase();
            } else {
                code = (pair[0].charAt(0) + pair[1] + pair[pair.length - 1].replace(/[/_().\-]/g, '')).toLowerCase();

            }
            return code;

        },
        test: function () {
            return this;
        },
        cancelFullscreen: function () {
            if (document.cancelFullScreen) {
                document.cancelFullScreen();
            } else if (document.mozCancelFullScreen) {
                document.mozCancelFullScreen();
            } else if (document.webkitCancelFullScreen) {
                document.webkitCancelFullScreen();
            }
        },
        launchFullScreen: function (element) {
            if (element.requestFullScreen) {
                element.requestFullScreen();
            } else if (element.mozRequestFullScreen) {
                element.mozRequestFullScreen();
            } else if (element.webkitRequestFullScreen) {
                element.webkitRequestFullScreen();
            }
        },
        parseJobCommand: function (item) {
            var commandObject = {};
            var commandArray = item.commandLine.split(/ -{1,2}/g);
            var tableHtml = '<table cellspacing="0" style="max-width:400px;border-collapse: collapse;border:1px solid #ccc;"><tbody>';
            tableHtml += '<tr style="border-collapse: collapse;border:1px solid #ccc;font-weight:bold;">';
            tableHtml += '<td style="min-width:50px;border-collapse: collapse;border:1px solid #ccc;padding: 5px;background-color: whiteSmoke;">Parameter</td>';
            tableHtml += '<td style="border-collapse: collapse;border:1px solid #ccc;padding: 5px;background-color: whiteSmoke;">Value</td>';
            tableHtml += '</tr>';
            for (var i = 1; i < commandArray.length; i++) {
                //ignore first argument
                var paramenter = commandArray[i];
                var paramenterArray = paramenter.split(/ {1}/g);
                var name = '';
                var value = '';
                if (paramenterArray.length < 2) {
                    name = paramenterArray[0];
                    value = '<span color:darkgray;font-weight:bold;>This paramenter is a flag</span>';
                } else {
                    name = paramenterArray[0];
                    value = paramenterArray[1];
                }
                commandObject[name] = value;
                /* clean values for viz*/
                value = value.replace(/\/httpd\/bioinfo\/opencga\/analysis\/.+\/examples\//, '');
                value = value.replace('/httpd/bioinfo/opencga/accounts/', '');
                value = value.replace(/,/g, ", ");

                tableHtml += '<tr style="border-collapse: collapse;border:1px solid #ccc;">';
                tableHtml += '<td style="border-collapse: collapse;border:1px solid #ccc;padding: 5px;background-color: whiteSmoke;color:steelblue;font-weight:bold;white-space: nowrap;">' + name + '</td>';
                tableHtml += '<td style="border-collapse: collapse;border:1px solid #ccc;padding: 5px;background-color: whiteSmoke;">' + value + '</td>';
                tableHtml += '</tr>';
            }
            tableHtml += '</tbody></table>';
            return {html: tableHtml, data: commandObject};
        }

    }
    ;


Utils.images = {
    add: "data:image/gif;base64,R0lGODlhEAAQAIcAAD2GNUKNNkOPOESMO0WNPEmPP0iNQUmPQlOVTFWWTVCZQVeeRV6cVmGeWGSgVWSgV2aiWGejW2WrVWirU2uqWGqsW2yqWm61WG+1WG+1WXS3W3S3XHC4WXK5W3O6XHG+X3asZ3iuaHe8YHi0ZH+yany6ZH28Zn2+Z3m9bn25an25a3+5bUD/QHDBY3nBZHrGa3zDa37BaX7Hb4K1boO1boa3cYi4d4y7doq5eYm+eI2+e5O/f4HMdYbJeobJfIXNeYrCeY/CfYnIf4rPfZW/gozLgY7MhI7Sg5LFgJXAgpfHhZfMhZPNiJjLhpjMh5jMipvBl5vBmJTTipbTiZXUipbUi5fVi5nRi53YkqTOlKbPlqbQlqDZlaDZlqXbm6rUnavUnKbIoKfJoa/fpa/fprPZpbTZpbTaprLbqLPdqbXbqLfaqrTdqrXfrLbdrLjVr7jdr7vcr7rWsbfgr77itr3ktsTcuMXducXowMvmw87pydTrz9fu0tzx2ODy3P///wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAMAACwALAAAAAAQABAAAAi/AFkIHEiwoME7SWrMwCHH4MAdWfLs0QNnRQiHN+L4qeOlyxg8QCAU3LGmDxYmRqpQOTJHRYSBdpTw4SJFyJ8/P2DIaLNAjEAibsgU8YHiZgURHq4gaSCQBh0rPW5K/cMhxpcCAkmkGcJj6k0OJ8AMEGjjyZQXLSR85dBhiY4EAt9MYOPig4ivFzacEQBlIIgUaJByyIBBQxkLBwo6GKHGiYkSTcxQAODwgYIgW7TkCGDAocAwDAoQQBDFs2mCAQEAOw==",
    del: "data:image/gif;base64,R0lGODlhEAAQAIcAAED/QLpSNL9TOr5WOb5cQL9cQMFNM8RQNMBVPcBZP8xSPNBPPttWS9ddUcJnTMRkTMdrVM1gUc5iVMxmVclrVs1oWNZgVNZuZNtpZdxraN5ratxuadRxZd14c955dOZWTOZYTOZZTulZTelbT+ZWUOZaUuddWepcUOxfVOBlXO5mUuljW+pmXO5qXvBkVvNzXeNrYeNuY+FvcOJwZuJ7deR4ceJ5eeN4eeJ/feN/fOl7cOh6del/ePJ3Y/N5Y+qDfe6Efe+Gfu6KdfaCaPaEbPCFcPCDe/CMd/GOeviGcPiMdvCRf/eRfveTfvmSfvqTf/iUf9ymltynl+6Mge2Tju6Sj/SOgfqah/qdi/GclvGdluGpnvSgnvSinvWjn/qjkfupnPqrneGroOqwrOuzr/Ono/WmoferofarovWsofWvpfKtqvivpPS0qvi2qPm5r/q6rvC1tfC2tvjDvvzHuvnLxPnTzPzUzf3b1P3c2P///wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAMAAAAALAAAAAAQABAAAAi6AAEIHEiwoEE5ODRk8EDG4EAbVObYqdNmxgWHMtbkgfMFCxg6OiQUvFEGz5UlSKA4UeImRoWBcX7cwdJECJGbRHywWSBGYA41YY6gGEq0hxUeFARuePOkiJ6nUEW00IJAIIYzSYZAjcoiywCBHaYweSGirNkRRmg8EDiGARoXKsyKAFHCy4EoAznASIPihIgQH0h0sVCgYIQUZoKsMAGES4MADico2FGlSg0DBBwK3AIhgQAHUjSLJhgQADs=",
    enable: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAKfSURBVDjLpZPrS1NhHMf9O3bOdmwDCWREIYKEUHsVJBI7mg3FvCxL09290jZj2EyLMnJexkgpLbPUanNOberU5taUMnHZUULMvelCtWF0sW/n7MVMEiN64AsPD8/n83uucQDi/id/DBT4Dolypw/qsz0pTMbj/WHpiDgsdSUyUmeiPt2+V7SrIM+bSss8ySGdR4abQQv6lrui6VxsRonrGCS9VEjSQ9E7CtiqdOZ4UuTqnBHO1X7YXl6Daa4yGq7vWO1D40wVDtj4kWQbn94myPGkCDPdSesczE2sCZShwl8CzcwZ6NiUs6n2nYX99T1cnKqA2EKui6+TwphA5k4yqMayopU5mANV3lNQTBdCMVUA9VQh3GuDMHiVcLCS3J4jSLhCGmKCjBEx0xlshjXYhApfMZRP5CyYD+UkG08+xt+4wLVQZA1tzxthm2tEfD3JxARH7QkbD1ZuozaggdZbxK5kAIsf5qGaKMTY2lAU/rH5HW3PLsEwUYy+YCcERmIjJpDcpzb6l7th9KtQ69fi09ePUej9l7cx2DJbD7UrG3r3afQHOyCo+V3QQzE35pvQvnAZukk5zL5qRL59jsKbPzdheXoBZc4saFhBS6AO7V4zqCpiawuptwQG+UAa7Ct3UT0hh9p9EnXT5Vh6t4C22QaUDh6HwnECOmcO7K+6kW49DKqS2DrEZCtfuI+9GrNHg4fMHVSO5kE7nAPVkAxKBxcOzsajpS4Yh4ohUPPWKTUh3PaQEptIOr6BiJjcZXCwktaAGfrRIpwblqOV3YKdhfXOIvBLeREWpnd8ynsaSJoyESFphwTtfjN6X1jRO2+FxWtCWksqBApeiFIR9K6fiTpPiigDoadqCEag5YUFKl6Yrciw0VOlhOivv/Ff8wtn0KzlebrUYwAAAABJRU5ErkJggg==",
    warning: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAIsSURBVDjLpVNLSJQBEP7+h6uu62vLVAJDW1KQTMrINQ1vPQzq1GOpa9EppGOHLh0kCEKL7JBEhVCHihAsESyJiE4FWShGRmauu7KYiv6Pma+DGoFrBQ7MzGFmPr5vmDFIYj1mr1WYfrHPovA9VVOqbC7e/1rS9ZlrAVDYHig5WB0oPtBI0TNrUiC5yhP9jeF4X8NPcWfopoY48XT39PjjXeF0vWkZqOjd7LJYrmGasHPCCJbHwhS9/F8M4s8baid764Xi0Ilfp5voorpJfn2wwx/r3l77TwZUvR+qajXVn8PnvocYfXYH6k2ioOaCpaIdf11ivDcayyiMVudsOYqFb60gARJYHG9DbqQFmSVNjaO3K2NpAeK90ZCqtgcrjkP9aUCXp0moetDFEeRXnYCKXhm+uTW0CkBFu4JlxzZkFlbASz4CQGQVBFeEwZm8geyiMuRVntzsL3oXV+YMkvjRsydC1U+lhwZsWXgHb+oWVAEzIwvzyVlk5igsi7DymmHlHsFQR50rjl+981Jy1Fw6Gu0ObTtnU+cgs28AKgDiy+Awpj5OACBAhZ/qh2HOo6i+NeA73jUAML4/qWux8mt6NjW1w599CS9xb0mSEqQBEDAtwqALUmBaG5FV3oYPnTHMjAwetlWksyByaukxQg2wQ9FlccaK/OXA3/uAEUDp3rNIDQ1ctSk6kHh1/jRFoaL4M4snEMeD73gQx4M4PsT1IZ5AfYH68tZY7zv/ApRMY9mnuVMvAAAAAElFTkSuQmCC",
    edit: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAB80lEQVR42o2T30tTURzArb8ioiAI6kHoZeF7CGE/IISCUDNCqAeL3rIWPfSwByskYUEJIhSChBhJFAiNqMVYPqRuc4tcW3NLt3C7u3d3d3c/+nS+0GRK0134cC6c8/ncc+7ltgFt6jqgcCg6duGQYq84deoBR6lU0iqVSq1arfI/1Dxut3u0Htke6BC5UChgmuYm+XyeXC5HOp1GIsnQNJHJi3x/7WJh/CSLT9r7Rd4jAVlgWRa2bSOjYBgGmqaRyWQwkq9Y8wyhLb0BI0VuaRrfo671xoDIwmakWCyi6zrr36bILt/HXp1l7cNDioEZqnEvgYmr1paAOgYy1u/l3NrqHNngPWpFL8XodTa+3CD8YoCvz/o078i5o1sC29FT78kG7lCzfJgrl7ESvejLThLPuxk8fbhP3KaBVFCdeX7on9yP9bOHfPAu0bEzmKkg4jQNpEKzhOduqW1/xIoNUEpcQlM7WXl6Cj39Q9Y0D4Q/TRJ662Tx3WOS/guYsV42Fm4THe/G/B2T97Jz4OVwJ+hxImPn8Tj381k91TfShfErIvLuAde1Y9g+N7Z/FL/rBDODR8gmgpTL5To7B3o69zF8pR3Pg7PMT90kn47LJ22kaeCPghapidP4Lxy3bduUiVZktdaQH7AxcFAiUm0Rhzji/gUhbp0s2Zf2xwAAAABJRU5ErkJggg==",
    info: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAIGNIUk0AAHolAACAgwAA+f8AAIDpAAB1MAAA6mAAADqYAAAXb5JfxUYAAAJ1SURBVHjafJJdSJNhFMd/z3QzLWdZrnQmSA2DPqRCK4KuhIq66kLoAy/qoqCguqqL6JsgLwoKKhCMSIy6CDKKRFZZYYQRVhJl02nWmG5uc19u7/vuPV0lW7QOnIsHnt+P8z/Pg4gw26aZ0263uzEUCn2IxWJjXq/3JqBETLIZ8gkePLhfKyKy/Z5HHJf7xe0Jic/n65mejizPK0inUiSTKUSE0dHRhxf6JoSDb4Rjr4QDz0REJBgMtmczFrJKKYVSCjCYnPR/W1FuAwQSGjbHXAA0LRXKZnIm0NJpgAKvd/hSOBz2iIj0eiPS2vtDYsmUPH/uPg9YshklIrOyCb+/eUG5ve3au5C99W2AqGbgKivk8R4X1lSkv2pJZaNmmBQVWWeZnAiGoa+3KovdyBjsW2kn/SvK4Jcgtaf7cDqrGkQMUDkBcgXVS2tOHjp8dG2jOXT1yo5lYOpgFTB0wKTAOqdQMlqOoDD7EE8kREwGXr/oWTg4HjxONAklBayuKSUeT/hFTxrwnwlAMa8I1qyrP3H95RiQgUiC/RsWM+wZ6jIME0M38wtSM0mmojM4nc6mzr5xKDQgnWb/pmoedT29EU3pTMUS+QVKKerq6kqnI3EVHwmAplO8qBh7WTFnzpz9bOg6FovlfxGEixfOrfT6YxCOQ1rDUaIAG4EJ38+PAwNb/95Bzj8ITAZwLHbMT0yHw3N33YVwEnQDqss41VzPkaalX6Iz+m6Xy/Xp34JAAICR7187nLWuvbe6h9C0DA2uRTTVV9J++87OlpaWJxUVFf9+xj+1cfOWls6OO93Nq1zblMVm9flG3pcvXNPm90+E/777ewB+UIqdqtYXHAAAAABJRU5ErkJggg==",
//    bucket: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAAZiS0dEAP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB90BCg4hBcbCoOMAAABsSURBVDjLY2RgYFBjYGCIZCAPLGeBam4g0wAGJgYKARMDA8NZCvSfZYQy6sk0oJEFiUNqODRQLQxGDYCAb2To/YZswEsyDHiJbMAHMgz4gO6F5aTkQpgXYElZkoGBgZeEbL2cgYHhMwMDw3MA93ARk+mSg4gAAAAASUVORK5CYII=",
    bucket: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3QkQDC8RTstxRAAAAGBJREFUOMtjYBgswIWBgeE/idiFgYGBgRFqwH8GBoYGEi1tYGBgYGRBE9QjUvMlGANmgCsDA8NuElzRANXDwAQV2ENGuO1BNoBsMGoAlQ3wJTIdNEDVYgU+ROQBH6rmQgAWgB19xco60wAAAABJRU5ErkJggg==",
//    dir: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAACXBIWXMAAAsSAAALEgHS3X78AAAKT2lDQ1BQaG90b3Nob3AgSUNDIHByb2ZpbGUAAHjanVNnVFPpFj333vRCS4iAlEtvUhUIIFJCi4AUkSYqIQkQSoghodkVUcERRUUEG8igiAOOjoCMFVEsDIoK2AfkIaKOg6OIisr74Xuja9a89+bN/rXXPues852zzwfACAyWSDNRNYAMqUIeEeCDx8TG4eQuQIEKJHAAEAizZCFz/SMBAPh+PDwrIsAHvgABeNMLCADATZvAMByH/w/qQplcAYCEAcB0kThLCIAUAEB6jkKmAEBGAYCdmCZTAKAEAGDLY2LjAFAtAGAnf+bTAICd+Jl7AQBblCEVAaCRACATZYhEAGg7AKzPVopFAFgwABRmS8Q5ANgtADBJV2ZIALC3AMDOEAuyAAgMADBRiIUpAAR7AGDIIyN4AISZABRG8lc88SuuEOcqAAB4mbI8uSQ5RYFbCC1xB1dXLh4ozkkXKxQ2YQJhmkAuwnmZGTKBNA/g88wAAKCRFRHgg/P9eM4Ors7ONo62Dl8t6r8G/yJiYuP+5c+rcEAAAOF0ftH+LC+zGoA7BoBt/qIl7gRoXgugdfeLZrIPQLUAoOnaV/Nw+H48PEWhkLnZ2eXk5NhKxEJbYcpXff5nwl/AV/1s+X48/Pf14L7iJIEyXYFHBPjgwsz0TKUcz5IJhGLc5o9H/LcL//wd0yLESWK5WCoU41EScY5EmozzMqUiiUKSKcUl0v9k4t8s+wM+3zUAsGo+AXuRLahdYwP2SycQWHTA4vcAAPK7b8HUKAgDgGiD4c93/+8//UegJQCAZkmScQAAXkQkLlTKsz/HCAAARKCBKrBBG/TBGCzABhzBBdzBC/xgNoRCJMTCQhBCCmSAHHJgKayCQiiGzbAdKmAv1EAdNMBRaIaTcA4uwlW4Dj1wD/phCJ7BKLyBCQRByAgTYSHaiAFiilgjjggXmYX4IcFIBBKLJCDJiBRRIkuRNUgxUopUIFVIHfI9cgI5h1xGupE7yAAygvyGvEcxlIGyUT3UDLVDuag3GoRGogvQZHQxmo8WoJvQcrQaPYw2oefQq2gP2o8+Q8cwwOgYBzPEbDAuxsNCsTgsCZNjy7EirAyrxhqwVqwDu4n1Y8+xdwQSgUXACTYEd0IgYR5BSFhMWE7YSKggHCQ0EdoJNwkDhFHCJyKTqEu0JroR+cQYYjIxh1hILCPWEo8TLxB7iEPENyQSiUMyJ7mQAkmxpFTSEtJG0m5SI+ksqZs0SBojk8naZGuyBzmULCAryIXkneTD5DPkG+Qh8lsKnWJAcaT4U+IoUspqShnlEOU05QZlmDJBVaOaUt2ooVQRNY9aQq2htlKvUYeoEzR1mjnNgxZJS6WtopXTGmgXaPdpr+h0uhHdlR5Ol9BX0svpR+iX6AP0dwwNhhWDx4hnKBmbGAcYZxl3GK+YTKYZ04sZx1QwNzHrmOeZD5lvVVgqtip8FZHKCpVKlSaVGyovVKmqpqreqgtV81XLVI+pXlN9rkZVM1PjqQnUlqtVqp1Q61MbU2epO6iHqmeob1Q/pH5Z/YkGWcNMw09DpFGgsV/jvMYgC2MZs3gsIWsNq4Z1gTXEJrHN2Xx2KruY/R27iz2qqaE5QzNKM1ezUvOUZj8H45hx+Jx0TgnnKKeX836K3hTvKeIpG6Y0TLkxZVxrqpaXllirSKtRq0frvTau7aedpr1Fu1n7gQ5Bx0onXCdHZ4/OBZ3nU9lT3acKpxZNPTr1ri6qa6UbobtEd79up+6Ynr5egJ5Mb6feeb3n+hx9L/1U/W36p/VHDFgGswwkBtsMzhg8xTVxbzwdL8fb8VFDXcNAQ6VhlWGX4YSRudE8o9VGjUYPjGnGXOMk423GbcajJgYmISZLTepN7ppSTbmmKaY7TDtMx83MzaLN1pk1mz0x1zLnm+eb15vft2BaeFostqi2uGVJsuRaplnutrxuhVo5WaVYVVpds0atna0l1rutu6cRp7lOk06rntZnw7Dxtsm2qbcZsOXYBtuutm22fWFnYhdnt8Wuw+6TvZN9un2N/T0HDYfZDqsdWh1+c7RyFDpWOt6azpzuP33F9JbpL2dYzxDP2DPjthPLKcRpnVOb00dnF2e5c4PziIuJS4LLLpc+Lpsbxt3IveRKdPVxXeF60vWdm7Obwu2o26/uNu5p7ofcn8w0nymeWTNz0MPIQ+BR5dE/C5+VMGvfrH5PQ0+BZ7XnIy9jL5FXrdewt6V3qvdh7xc+9j5yn+M+4zw33jLeWV/MN8C3yLfLT8Nvnl+F30N/I/9k/3r/0QCngCUBZwOJgUGBWwL7+Hp8Ib+OPzrbZfay2e1BjKC5QRVBj4KtguXBrSFoyOyQrSH355jOkc5pDoVQfujW0Adh5mGLw34MJ4WHhVeGP45wiFga0TGXNXfR3ENz30T6RJZE3ptnMU85ry1KNSo+qi5qPNo3ujS6P8YuZlnM1VidWElsSxw5LiquNm5svt/87fOH4p3iC+N7F5gvyF1weaHOwvSFpxapLhIsOpZATIhOOJTwQRAqqBaMJfITdyWOCnnCHcJnIi/RNtGI2ENcKh5O8kgqTXqS7JG8NXkkxTOlLOW5hCepkLxMDUzdmzqeFpp2IG0yPTq9MYOSkZBxQqohTZO2Z+pn5mZ2y6xlhbL+xW6Lty8elQfJa7OQrAVZLQq2QqboVFoo1yoHsmdlV2a/zYnKOZarnivN7cyzytuQN5zvn//tEsIS4ZK2pYZLVy0dWOa9rGo5sjxxedsK4xUFK4ZWBqw8uIq2Km3VT6vtV5eufr0mek1rgV7ByoLBtQFr6wtVCuWFfevc1+1dT1gvWd+1YfqGnRs+FYmKrhTbF5cVf9go3HjlG4dvyr+Z3JS0qavEuWTPZtJm6ebeLZ5bDpaql+aXDm4N2dq0Dd9WtO319kXbL5fNKNu7g7ZDuaO/PLi8ZafJzs07P1SkVPRU+lQ27tLdtWHX+G7R7ht7vPY07NXbW7z3/T7JvttVAVVN1WbVZftJ+7P3P66Jqun4lvttXa1ObXHtxwPSA/0HIw6217nU1R3SPVRSj9Yr60cOxx++/p3vdy0NNg1VjZzG4iNwRHnk6fcJ3/ceDTradox7rOEH0x92HWcdL2pCmvKaRptTmvtbYlu6T8w+0dbq3nr8R9sfD5w0PFl5SvNUyWna6YLTk2fyz4ydlZ19fi753GDborZ752PO32oPb++6EHTh0kX/i+c7vDvOXPK4dPKy2+UTV7hXmq86X23qdOo8/pPTT8e7nLuarrlca7nuer21e2b36RueN87d9L158Rb/1tWeOT3dvfN6b/fF9/XfFt1+cif9zsu72Xcn7q28T7xf9EDtQdlD3YfVP1v+3Njv3H9qwHeg89HcR/cGhYPP/pH1jw9DBY+Zj8uGDYbrnjg+OTniP3L96fynQ89kzyaeF/6i/suuFxYvfvjV69fO0ZjRoZfyl5O/bXyl/erA6xmv28bCxh6+yXgzMV70VvvtwXfcdx3vo98PT+R8IH8o/2j5sfVT0Kf7kxmTk/8EA5jz/GMzLdsAAAAgY0hSTQAAeiUAAICDAAD5/wAAgOkAAHUwAADqYAAAOpgAABdvkl/FRgAAAKNJREFUeNrEk7sNwkAQBefQ5m6BTiAAQssZiMh0QFUIMrAEpKYD8ynAJeD4nXQEkJHgu4CXv9GsdteFEEjJgMQ4gPli+aWx227cLwAD8FK8QZ4XTyCL6B6qal+YlzLgCpSn87HpbTCdzAKwAkpg1Bdgn/nbmDLQmby6hC3W5qUGGEcCGpNUJwBq09tgHdO+Pe61eamNvIMLgEkaxuoDuL9/42sAM20/EZafbV8AAAAASUVORK5CYII=",
    dir: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3QkQDBgWQKhE0wAAADRJREFUOMtjYBhowMjAwPCfCDU4AQuUNsQhf54aLmAgxgW9ZOovZqI0EEcNGBYGUJwSKQYAJoEFGqo9ooAAAAAASUVORK5CYII=",
    r: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAAZiS0dEAP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB90CDRIvNbHTpbwAAADjSURBVDjLpZFBbsIwEEUfVRZYahcVK3qKXoauMFK5C91nkyUB+xC5BqeAA7SKq1B5ugl2EiC04UkjayzN17NnROTRWvvJFbTWL8CBHqbGWOlSlqVkWSbGWAGm3aGHZiMiAByPP6FOd1rP2W7NvhvSCvDe10E+VJPFQpPnm1ZIcsmgPgJVVZGmaejX63y/XL4/AV/JJYPTCeDcN7PZWyuwKAqA8wARqSsGKDVGqXGjV8H07AnRQPq21TK8+YSBAQMN4hb6Df7wB/5eA+4zmEyehxk451itPrhFksSxUeP+lf+z+wXwdayJk/mqtgAAAABJRU5ErkJggg==",
    box: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAAZiS0dEAP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9wMHAwRAVvTmTAAAAK/SURBVDjLpZM9bFxFGEXPNzPvZ+39sb2xHceREjDQBwlDCqqIiiotokAghYKEjvSkQkKJkEiB0lOkoAHaBAokFCQKUATIIOLIMbHWrHfX+7zvvZk3MzQODUgU3PJK5+g2F/5n5N/Kb66/1NNK3hAxr4HcFqVuvfju18V/Cu58sPmMVnJZ4K32Qr+t8za+KnCz4kCUuiGibm5euTv5h+CL958/nxj1XivVF+e6C9TVhPmFdbROgEhwNU1d4m09UaJuInLjhct3DgDUh5ee7j14PLxulLvYP/0seadPkub88Wib0eB3bDkmxgbRoFPpxeCuKvjsyQIzOyqImT7/y8Mh++NveW7jLFmrx6m1NlWxz6PHA7otQ7tloAmYJE9isOeeCJRtIrULLLUTjsqG7+//xs72z7jZgCTNONlVJKEiuobW0jqSaoiet19dFQATJcc2FSFEciNoLYwOHcPDASvdjM5cQntxlbR9gqacoFSK84VsnOrkH11Zdmp0FFXjobSeCFgXSDS0Eo11ge7yGXSaU092UUlCaEpC8FK4tDcu4rzZ2a/S+bWI94HSAgFigDQD24Cvp4gIOp0juBJvC2L07B1Uc/Mtg9k7sHMbywZrA3lLECV4AtaCpAp79CcmzXHlhOBrAJrGyNbOVBY7qTO1C9r5EKyPSttAiJEs01SuQStFkrdp6gKd5AzHjixVxCDxp+1paZRUxoc4Kp36bndYbS53U5WlCq0CMYIPMY7GI0mNpiqmGK0oK4jIveGkPgRqfTBt3A8Pqtvrq52HtglnGh9XIaKUkCQ6nj6RyWBsmdXCtFI/bu2Fq5c+3roGzIAgWokCDNACOhfOLb781Ip+vd+RC2dXWibROkxKvvp1z376yZe7d4HpMdz8/YVjiQYyoA30Ti6la2++0n/n83vTW/e3ix1gcgzXgPchBoC/AFu/UBF5InryAAAAAElFTkSuQmCC",
    bluebox: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAAZiS0dEAP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9wMHAwTE5pcivoAAALsSURBVDjLXZPPaxxlGMc/77wz+3N2k822tWsTIrQFe/BWEKxKCV5UohdFhJ60p6rgUfpP6C0K4kHx0ENEkV7aElHwUikhFVosRGoTm83+3pnZnXfed2beHtItIQ98+Z6+H57nga8AsNYKDkYcEofcHvKZEEJYcSTszPzqL3fmf3+w/+a51tytby9d6D0N5UecGeBZ8MPv/jh9fy/6dKzMpVPHmvWdbl/XCvKn5Wbl6+ufrNwGssMgYa2VgFj58sZr7VB/LqX3zlKrydJzTTzXxdqcx90hO+0Bk2l8Z74i1z6+cOba5VfOqGeAb3579M/NR53T40xwrDGHFALPEUjn4LoMi0ktwWTKXqCIqAVrbyycvHj2hHYBR+bO8Q/Ov0imEzZ2xrRDRalQwC9LLBalUgaJQy+tU6gvIBJbv3j2RA4IFxDdICFa9ulMCrz/UgOs5kEwpeh57I4Nt/dzsmLOYlEThgFjUePp33IHoD9SJAbuTVyudRweixJvnVtg3/i00wpLPiwQ0hkO6YYKawWj0UjONqAfKHwDkxTqqeW/RHA3hO2+Zqk05e5wTD9KmOqMKDEUqoLNzU0PyF2AQaBoaIhiw0h6TIwgUDCODb5NiWJNlKREyhAozXwOW1tbFSmlcAHbD2KaytCdGgyWglfEs4LeNKeaa4axYRgpwlgTTTXVDDqdTslaewAYh4kNlKUbZsTGonOwCYwm1vq5Ft1AMYgU08SQR5o0gziOcRxHuoCNtdl6uPHX6/Vmi3Yyh9I5IoEgMdkgT9x+qJhEGrdQo77cJMuy+4DJskwLa60DOCtf3HhZpfZKtVx+L3x+sfCv8CFxTINd72HfodQ4aQp5fP24/v/Hd4Nf/5RSJmma6lkXZn1wPvvq5qndsbhS9esf/Zy/UEtzxnURfn8+/fuHV7m353mecV1XSym1lDI72kaxvr5e3N7eruyP0tpG/e3LK/rW2mLNUb7vm3K5nFarVdNqtbJer2dXV1fzJ6cDpboAZRAGAAAAAElFTkSuQmCC",
    refresh: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAAZiS0dEAP8A/wD/oL2nkwAAAAlwSFlzAAAK8AAACvABQqw0mAAAAAd0SU1FB90CFA8bKScXIjIAAAJ1SURBVDjLlVJdSJNRGH7es+/7NlduWFLTKAskwjJqZYiBglJhLKguqou6EFkFIkLQH9IPBFI3BsouzMJupGYRajcqmeBFhWBU5oUgpZikrSnzZ9v3nW/ndKEbziXUAwfOOe/7Puc9z/MCq1DwMmB1NX/rzfCNnsc/gK08lPgnnT8Cs33BULg0HI4YKdkHX9DqKwKArXXv1bTMTFcoyruC89E8MxaDw659t6rKhIUwRBLdP2t2v/5bBwQA+5pH8ibnIj3BucgWIQRASw8RERTGYFUtsGmWYUXKmqmr7t4UAnal54GQ8lq8MBlyOU0CEnA67MiwqfvHbhZ+Smgg6o9eV2L8Nhk6wI2lZeggrpvE+TTjxgxxQ4IbmJsJYSa00JQiotnguacJ8zIZOmDosAnzTpowt8tGj0s0ejZqprnDKmPHSNebjHDkUPatt4cTTbZ+LsmO79XK52dZxTNp9/ovAEDnaM62lo8HHrd9SVfiOelVryrSq9vrEx0s8sW2tuEzDgDgT875bcIsjy6owwAwHhjnYT5bGTL29PiHyuwAMO873aL/Ct5PiPjwXe5vq7KJW2hdJxENMFInGCkhIblLj80WRoyxGxZmh1XJGlSIlV8s6A8kuVDXn+MF6JHC7GBkBSNlOSRgiihMsQhAgJGGNNU1atc2HPG6O8YSBABwt2/nGyFlGSCSB4UIBMuyoQKMFNiUjIApRH5t8YfpFOOrO/JrhZBVUiJLxq2ipIkY8Z36uivpC6txqb3YbhqhIingFlLmxmLSKyXAGAaYqh13aFjfcHJwfE2ClSitK9psc85PMVC3M999orX4Kcf/wuPb27VW7A+O2QVVA1M1CQAAAABJRU5ErkJggg==",
    newitem: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAACXBIWXMAAAsTAAALEwEAmpwYAAAKT2lDQ1BQaG90b3Nob3AgSUNDIHByb2ZpbGUAAHjanVNnVFPpFj333vRCS4iAlEtvUhUIIFJCi4AUkSYqIQkQSoghodkVUcERRUUEG8igiAOOjoCMFVEsDIoK2AfkIaKOg6OIisr74Xuja9a89+bN/rXXPues852zzwfACAyWSDNRNYAMqUIeEeCDx8TG4eQuQIEKJHAAEAizZCFz/SMBAPh+PDwrIsAHvgABeNMLCADATZvAMByH/w/qQplcAYCEAcB0kThLCIAUAEB6jkKmAEBGAYCdmCZTAKAEAGDLY2LjAFAtAGAnf+bTAICd+Jl7AQBblCEVAaCRACATZYhEAGg7AKzPVopFAFgwABRmS8Q5ANgtADBJV2ZIALC3AMDOEAuyAAgMADBRiIUpAAR7AGDIIyN4AISZABRG8lc88SuuEOcqAAB4mbI8uSQ5RYFbCC1xB1dXLh4ozkkXKxQ2YQJhmkAuwnmZGTKBNA/g88wAAKCRFRHgg/P9eM4Ors7ONo62Dl8t6r8G/yJiYuP+5c+rcEAAAOF0ftH+LC+zGoA7BoBt/qIl7gRoXgugdfeLZrIPQLUAoOnaV/Nw+H48PEWhkLnZ2eXk5NhKxEJbYcpXff5nwl/AV/1s+X48/Pf14L7iJIEyXYFHBPjgwsz0TKUcz5IJhGLc5o9H/LcL//wd0yLESWK5WCoU41EScY5EmozzMqUiiUKSKcUl0v9k4t8s+wM+3zUAsGo+AXuRLahdYwP2SycQWHTA4vcAAPK7b8HUKAgDgGiD4c93/+8//UegJQCAZkmScQAAXkQkLlTKsz/HCAAARKCBKrBBG/TBGCzABhzBBdzBC/xgNoRCJMTCQhBCCmSAHHJgKayCQiiGzbAdKmAv1EAdNMBRaIaTcA4uwlW4Dj1wD/phCJ7BKLyBCQRByAgTYSHaiAFiilgjjggXmYX4IcFIBBKLJCDJiBRRIkuRNUgxUopUIFVIHfI9cgI5h1xGupE7yAAygvyGvEcxlIGyUT3UDLVDuag3GoRGogvQZHQxmo8WoJvQcrQaPYw2oefQq2gP2o8+Q8cwwOgYBzPEbDAuxsNCsTgsCZNjy7EirAyrxhqwVqwDu4n1Y8+xdwQSgUXACTYEd0IgYR5BSFhMWE7YSKggHCQ0EdoJNwkDhFHCJyKTqEu0JroR+cQYYjIxh1hILCPWEo8TLxB7iEPENyQSiUMyJ7mQAkmxpFTSEtJG0m5SI+ksqZs0SBojk8naZGuyBzmULCAryIXkneTD5DPkG+Qh8lsKnWJAcaT4U+IoUspqShnlEOU05QZlmDJBVaOaUt2ooVQRNY9aQq2htlKvUYeoEzR1mjnNgxZJS6WtopXTGmgXaPdpr+h0uhHdlR5Ol9BX0svpR+iX6AP0dwwNhhWDx4hnKBmbGAcYZxl3GK+YTKYZ04sZx1QwNzHrmOeZD5lvVVgqtip8FZHKCpVKlSaVGyovVKmqpqreqgtV81XLVI+pXlN9rkZVM1PjqQnUlqtVqp1Q61MbU2epO6iHqmeob1Q/pH5Z/YkGWcNMw09DpFGgsV/jvMYgC2MZs3gsIWsNq4Z1gTXEJrHN2Xx2KruY/R27iz2qqaE5QzNKM1ezUvOUZj8H45hx+Jx0TgnnKKeX836K3hTvKeIpG6Y0TLkxZVxrqpaXllirSKtRq0frvTau7aedpr1Fu1n7gQ5Bx0onXCdHZ4/OBZ3nU9lT3acKpxZNPTr1ri6qa6UbobtEd79up+6Ynr5egJ5Mb6feeb3n+hx9L/1U/W36p/VHDFgGswwkBtsMzhg8xTVxbzwdL8fb8VFDXcNAQ6VhlWGX4YSRudE8o9VGjUYPjGnGXOMk423GbcajJgYmISZLTepN7ppSTbmmKaY7TDtMx83MzaLN1pk1mz0x1zLnm+eb15vft2BaeFostqi2uGVJsuRaplnutrxuhVo5WaVYVVpds0atna0l1rutu6cRp7lOk06rntZnw7Dxtsm2qbcZsOXYBtuutm22fWFnYhdnt8Wuw+6TvZN9un2N/T0HDYfZDqsdWh1+c7RyFDpWOt6azpzuP33F9JbpL2dYzxDP2DPjthPLKcRpnVOb00dnF2e5c4PziIuJS4LLLpc+Lpsbxt3IveRKdPVxXeF60vWdm7Obwu2o26/uNu5p7ofcn8w0nymeWTNz0MPIQ+BR5dE/C5+VMGvfrH5PQ0+BZ7XnIy9jL5FXrdewt6V3qvdh7xc+9j5yn+M+4zw33jLeWV/MN8C3yLfLT8Nvnl+F30N/I/9k/3r/0QCngCUBZwOJgUGBWwL7+Hp8Ib+OPzrbZfay2e1BjKC5QRVBj4KtguXBrSFoyOyQrSH355jOkc5pDoVQfujW0Adh5mGLw34MJ4WHhVeGP45wiFga0TGXNXfR3ENz30T6RJZE3ptnMU85ry1KNSo+qi5qPNo3ujS6P8YuZlnM1VidWElsSxw5LiquNm5svt/87fOH4p3iC+N7F5gvyF1weaHOwvSFpxapLhIsOpZATIhOOJTwQRAqqBaMJfITdyWOCnnCHcJnIi/RNtGI2ENcKh5O8kgqTXqS7JG8NXkkxTOlLOW5hCepkLxMDUzdmzqeFpp2IG0yPTq9MYOSkZBxQqohTZO2Z+pn5mZ2y6xlhbL+xW6Lty8elQfJa7OQrAVZLQq2QqboVFoo1yoHsmdlV2a/zYnKOZarnivN7cyzytuQN5zvn//tEsIS4ZK2pYZLVy0dWOa9rGo5sjxxedsK4xUFK4ZWBqw8uIq2Km3VT6vtV5eufr0mek1rgV7ByoLBtQFr6wtVCuWFfevc1+1dT1gvWd+1YfqGnRs+FYmKrhTbF5cVf9go3HjlG4dvyr+Z3JS0qavEuWTPZtJm6ebeLZ5bDpaql+aXDm4N2dq0Dd9WtO319kXbL5fNKNu7g7ZDuaO/PLi8ZafJzs07P1SkVPRU+lQ27tLdtWHX+G7R7ht7vPY07NXbW7z3/T7JvttVAVVN1WbVZftJ+7P3P66Jqun4lvttXa1ObXHtxwPSA/0HIw6217nU1R3SPVRSj9Yr60cOxx++/p3vdy0NNg1VjZzG4iNwRHnk6fcJ3/ceDTradox7rOEH0x92HWcdL2pCmvKaRptTmvtbYlu6T8w+0dbq3nr8R9sfD5w0PFl5SvNUyWna6YLTk2fyz4ydlZ19fi753GDborZ752PO32oPb++6EHTh0kX/i+c7vDvOXPK4dPKy2+UTV7hXmq86X23qdOo8/pPTT8e7nLuarrlca7nuer21e2b36RueN87d9L158Rb/1tWeOT3dvfN6b/fF9/XfFt1+cif9zsu72Xcn7q28T7xf9EDtQdlD3YfVP1v+3Njv3H9qwHeg89HcR/cGhYPP/pH1jw9DBY+Zj8uGDYbrnjg+OTniP3L96fynQ89kzyaeF/6i/suuFxYvfvjV69fO0ZjRoZfyl5O/bXyl/erA6xmv28bCxh6+yXgzMV70VvvtwXfcdx3vo98PT+R8IH8o/2j5sfVT0Kf7kxmTk/8EA5jz/GMzLdsAAAAgY0hSTQAAeiUAAICDAAD5/wAAgOkAAHUwAADqYAAAOpgAABdvkl/FRgAAAtxJREFUeNqM0llIVHEUBvDv3vlfZ8ac6zZtWpmamtliUUHRDlFJWlEYLdBLtG/QBlFJQUTSU089RZRlG4EvkS9NlqkpldHyYLk1OurcOzp30vHO/y6nBwsSIvzgvBw+fpyHA8MwwDmHbdsjQwSbCACkYDBYp6pqU3Fxcfyf/Z+eYRjQOQf+Bnw+30IiIsMwhizL4n3lV6mn7BzZtm1yzn8SETU0NKz+J2ARobe3t85/+SI1506j9hOHqTEO9FYEtR/ZTx/n5FDH6eOkquoni2g00NjUtEzTtBYioneLCulVHKg2yUkNmelUn5VOtUlueu0SqDE/m4iIIpFI64fm5vU65xAMIlicR9rOn/UEKytgmQbYuARAEDAqRLCiQxBFhtTNWzDzxk1LcjgkFhuKIhLR2qJKcN5Al/q7reF/cXUHoA0MtA9Gh4klJIxz6ro+PZiVC0uOw1jimJEDWZbTDhw8lCi0+/3PtUeV696ePIPUnIwxAf3fOjG/7AK8e/e9ZH2K0uWdPRdivANm3NguED1OJBYWQunvDwgAXIqifO54+CC7/tSxMQELL11B/r6D3cnJybniQDis25Ikfn1wD2GdQLIMISkF5JFhudwgjwySkyCkpILkRER0wpf7d2FJkqSoapQRRPCYjoLDR+EY70VXbS2YxCC4nAARbAAQBJBlwTIMZJRsQN7W7eA6t9O8XkE0jRhWLV2y+Gdm9q0dT6rMhLw8dPn7EAoEMBSLIcpjCPUEEPD3gU1Kw+6qZ6TPKrizq3TbAjUUIkFRVYAIkkfG99bWp4P1b7Z0vq5BXtFGPN6zE6Zuo7SiAh01PkycV4jJRRt96VOmrOHhMESHiBEAgMkNlGwqmXC78mG1DXtQdruTgx/eF5g6x9Tly1pCmtYjMSnxatnFTeXXyn8wxiCMAgxz5EmcTjCXCynxblf1C9910eFwrl254nh/dDhqcQ5zeBgAwBiDIAr4NQAWJarVjshqqgAAAABJRU5ErkJggg=="
};

Utils.genBamVariants = function (seq, size, x, y) {
    var length = seq.length;
    var s = size / 6;
    //if(x==null){x=0;}
    //if(y==null){y=0;}
    var d = "";
    for (var i = 0; i < length; i++) {
        switch (seq.charAt(i)) {
            case "A" :
                d += "M" + ((2.5 * s) + x) + "," + (y) +
                    "l-" + (2.5 * s) + "," + (6 * s) +
                    "l" + s + ",0" +
                    "l" + (0.875 * s) + ",-" + (2 * s) +
                    "l" + (2.250 * s) + ",0" +
                    "l" + (0.875 * s) + "," + (2 * s) +
                    "l" + s + ",0" +
                    "l-" + (2.5 * s) + ",-" + (6 * s) +
                    "l-" + (0.5 * s) + ",0" +
                    "l0," + s +
                    "l" + (0.75 * s) + "," + (2 * s) +
                    "l-" + (1.5 * s) + ",0" +
                    "l" + (0.75 * s) + ",-" + (2 * s) +
                    "l0,-" + s +
                    " ";
                break;
            case "T" :
                d += "M" + ((0.5 * s) + x) + "," + (y) +
                    "l0," + s +
                    "l" + (2 * s) + ",0" +
                    "l0," + (5 * s) +
                    "l" + s + ",0" +
                    "l0,-" + (5 * s) +
                    "l" + (2 * s) + ",0" +
                    "l0,-" + s +
                    " ";
                break;
            case "C" :
                d += "M" + ((5 * s) + x) + "," + ((0 * s) + y) +
                    "l-" + (2 * s) + ",0" +
                    "l-" + (1.5 * s) + "," + (0.5 * s) +
                    "l-" + (0.5 * s) + "," + (1.5 * s) +
                    "l0," + (2 * s) +
                    "l" + (0.5 * s) + "," + (1.5 * s) +
                    "l" + (1.5 * s) + "," + (0.5 * s) +
                    "l" + (2 * s) + ",0" +
                    "l0,-" + s +
                    "l-" + (2 * s) + ",0" +
                    "l-" + (0.75 * s) + ",-" + (0.25 * s) +
                    "l-" + (0.25 * s) + ",-" + (0.75 * s) +
                    "l0,-" + (2 * s) +
                    "l" + (0.25 * s) + ",-" + (0.75 * s) +
                    "l" + (0.75 * s) + ",-" + (0.25 * s) +
                    "l" + (2 * s) + ",0" +
                    " ";
                break;
            case "G" :
                d += "M" + ((5 * s) + x) + "," + ((0 * s) + y) +
                    "l-" + (2 * s) + ",0" +
                    "l-" + (1.5 * s) + "," + (0.5 * s) +
                    "l-" + (0.5 * s) + "," + (1.5 * s) +
                    "l0," + (2 * s) +
                    "l" + (0.5 * s) + "," + (1.5 * s) +
                    "l" + (1.5 * s) + "," + (0.5 * s) +
                    "l" + (2 * s) + ",0" +
                    "l0,-" + (3 * s) +
                    "l-" + (s) + ",0" +
                    "l0," + (2 * s) +
                    "l-" + (s) + ",0" +
                    "l-" + (0.75 * s) + ",-" + (0.25 * s) +
                    "l-" + (0.25 * s) + ",-" + (0.75 * s) +
                    "l0,-" + (2 * s) +
                    "l" + (0.25 * s) + ",-" + (0.75 * s) +
                    "l" + (0.75 * s) + ",-" + (0.25 * s) +
                    "l" + (2 * s) + ",0" +
                    " ";
//                d += "M" + ((5 * s) + x) + "," + ((0 * s) + y) +
//                    "l-" + (2 * s) + ",0" +
//                    "l-" + (2 * s) + "," + (2 * s) +
//                    "l0," + (2 * s) +
//                    "l" + (2 * s) + "," + (2 * s) +
//                    "l" + (2 * s) + ",0" +
//                    "l0,-" + (3 * s) +
//                    "l-" + (1 * s) + ",0" +
//                    "l0," + (2 * s) +
//                    "l-" + (0.5 * s) + ",0" +
//                    "l-" + (1.5 * s) + ",-" + (1.5 * s) +
//                    "l0,-" + (1 * s) +
//                    "l" + (1.5 * s) + ",-" + (1.5 * s) +
//                    "l" + (1.5 * s) + ",0" +
//                    " ";
                break;
            case "N" :
                d += "M" + ((0.5 * s) + x) + "," + ((0 * s) + y) +
                    "l0," + (6 * s) +
                    "l" + s + ",0" +
                    "l0,-" + (4.5 * s) +
                    "l" + (3 * s) + "," + (4.5 * s) +
                    "l" + s + ",0" +
                    "l0,-" + (6 * s) +
                    "l-" + s + ",0" +
                    "l0," + (4.5 * s) +
                    "l-" + (3 * s) + ",-" + (4.5 * s) +
                    " ";
                break;
            case "d" :
                d += "M" + ((0 * s) + x) + "," + ((2.5 * s) + y) +
                    "l" + (6 * s) + ",0" +
                    "l0," + (s) +
                    "l-" + (6 * s) + ",0" +
                    "l0,-" + (s) +
                    " ";
                break;
            default:
                d += "M0,0";
                break;
        }
        x += size;
    }
    return d;
};

//Element.prototype.addChildSVG = function(elementName, attributes, index){
//	var el = document.createElementNS('http://www.w3.org/2000/svg', elementName);
//	for ( var key in attributes){
//		el.setAttribute(key, attributes[key]);
//	}
//	
//	// insert child at requested index, or as last child if index is too high or no index is specified
//    if ( null == index ) {
//      this.appendChild( el );
//    }
//    else {
//      var targetIndex = index + 1;
//      if ( 0 == index ) {
//        targetIndex = 0;
//      }
//      var targetEl = this.childNodes[ targetIndex ];
//      if ( targetEl ) {
//        this.insertBefore( el, targetEl ); 
//      }
//      else {
//        this.appendChild( el );
//      }
//    }
//    return el;
//};
//Element.prototype.initSVG = function(attributes){
//	return this.addChildSVG("svg", attributes);
//};

var SVG = {
	
	create : function (elementName, attributes){
		var el = document.createElementNS('http://www.w3.org/2000/svg', elementName);
		for ( var key in attributes){
			el.setAttribute(key, attributes[key]);
		}
		return el;
	},

	addChild : function (parent, elementName, attributes, index){
		var el = document.createElementNS('http://www.w3.org/2000/svg', elementName);
		for ( var key in attributes){
			el.setAttribute(key, attributes[key]);
		}
		return this._insert(parent, el, index);
	},
	
	addChildImage : function (parent, attributes, index){
		var el = document.createElementNS('http://www.w3.org/2000/svg', "image");
		for ( var key in attributes){
			if(key == "xlink:href"){
				el.setAttributeNS('http://www.w3.org/1999/xlink','href',attributes[key]);
			}
			el.setAttribute(key, attributes[key]);
		}
		return this._insert(parent, el, index);
	},
	
	_insert : function (parent, el, index){
		// insert child at requested index, or as last child if index is too high or no index is specified
	    if ( null == index ) {
	    	parent.appendChild( el );
	    }
	    else {
	      var targetIndex = index + 1;
	      if ( 0 == index ) {
	        targetIndex = 0;
	      }
	      var targetEl = parent.childNodes[ targetIndex ];
	      if ( targetEl ) {
	    	  parent.insertBefore( el, targetEl ); 
	      }
	      else {
	    	  parent.appendChild( el );
	      }
	    }
	    return el;
	},

	init : function (parent, attributes){
		return this.addChild(parent, "svg", attributes);
	},



    //
    /* Functions to generate arcs with PATH element  */
    //

    _polarToCartesian : function (centerX, centerY, radius, angleInDegrees) {
        var angleInRadians = (angleInDegrees - 90) * Math.PI / 180.0;

        return {
            x: centerX + (radius * Math.cos(angleInRadians)),
            y: centerY + (radius * Math.sin(angleInRadians))
        };
    },

    describeArc : function (x, y, radius, startAngle, endAngle) {

        var start = this._polarToCartesian(x, y, radius, endAngle);
        var end = this._polarToCartesian(x, y, radius, startAngle);

        var arcSweep = endAngle - startAngle <= 180 ? "0" : "1";
        var d = [
            "M", start.x, start.y,
            "A", radius, radius, 0, arcSweep, 0, end.x, end.y
        ].join(" ");

        return d;
    }
};

//createSVG = function(elementName, attributes){
//	var el = document.createElementNS('http://www.w3.org/2000/svg', elementName);
//	for ( var key in attributes){
//		el.setAttribute(key, attributes[key]);
//	}
//	return el;
//};



//var SVG =
//{
//		svgns : 'http://www.w3.org/2000/svg',
//		xlinkns : "http://www.w3.org/1999/xlink",
//
////	createSVGCanvas: function(parentNode, attributes)
////	{
//////		attributes['xmlns'] = SVG.svgns;
//////		attributes['xmlns:xlink'] = SVG.xlinkns;
//////		attributes.push( ['xmlns', SVG.svgns], ['xmlns:xlink', 'http://www.w3.org/1999/xlink']);
////		var svg = document.createElementNS(SVG.svgns, "svg");
////		
////		for ( var key in attributes){
////			svg.setAttribute(key, attributes[key]);
////		}
////		
////		parentNode.appendChild(svg);
////		return svg;
////	}, 
//	
//	//Shape types : rect, circle, ellipse, line, polyline, polygon , path
//	createElement : function (svgNode, shapeName, attributes) {
//		try{
//			if(attributes.width < 0){
//				console.log("BIOINFO Warn: on SVG.createRectangle: width is negative, will be set to 0");
//				attributes.width=0;
//			}
//			if(attributes.height < 0){
//				console.log("BIOINFO Warn: on SVG.createRectangle: height is negative, will be set to 0");
//				attributes.height=0;
//			}
//			
//			var shape = document.createElementNS('http://www.w3.org/2000/svg', shapeName);
//			for ( var key in attributes){
//				shape.setAttribute(key, attributes[key]);
//			}
//			svgNode.appendChild(shape);
//		}
//		catch(e){
//			console.log("-------------------- ");
//			console.log("Error on drawRectangle " + e);
//			console.log(attributes);
//			console.log("-------------------- ");
//		}
//		return shape;
//	}
//};
//
//
//
//var CanvasToSVG = {
//		
//	convert: function(sourceCanvas, targetSVG, x, y, id, attributes) {
//		
//		var img = this._convert(sourceCanvas, targetSVG, x, y, id);
//		
//		for (var i=0; i< attributes.length; i++)
//		{
//			img.setAttribute(attributes[i][0], attributes[i][1]);
//		}
//	},
//	
//	_convert: function(sourceCanvas, targetSVG, x, y, id) {
//		var svgNS = "http://www.w3.org/2000/svg";
//		var xlinkNS = "http://www.w3.org/1999/xlink";
//		// get base64 encoded png from Canvas
//		var image = sourceCanvas.toDataURL();
//
//		// must be careful with the namespaces
//		var svgimg = document.createElementNS(svgNS, "image");
//
//		svgimg.setAttribute('id', id);
//	
//		//svgimg.setAttribute('class', class);
//		//svgimg.setAttribute('xlink:href', image);
//		svgimg.setAttributeNS(xlinkNS, 'xlink:href', image);
//		
//
//		svgimg.setAttribute('x', x ? x : 0);
//		svgimg.setAttribute('y', y ? y : 0);
//		svgimg.setAttribute('width', sourceCanvas.width);
//		svgimg.setAttribute('height', sourceCanvas.height);
//		//svgimg.setAttribute('cursor', 'pointer');
//		svgimg.imageData = image;
//	
//		targetSVG.appendChild(svgimg);
//		return svgimg;
//	},
//	
//	importSVG: function(sourceSVG, targetCanvas) {
//	    svg_xml = sourceSVG;//(new XMLSerializer()).serializeToString(sourceSVG);
//	    var ctx = targetCanvas.getContext('2d');
//
//	    var img = new Image();
//	    img.src = "data:image/svg+xml;base64," + btoa(svg_xml);
////	    img.onload = function() {
//	        ctx.drawImage(img, 0, 0);
////	    };
//	}
//	
//};

function Region(args) {

    this.chromosome = null;
    this.start = null;
    this.end = null;

    if (_.isObject(args)) {
        this.load(args);
    } else if (_.isString(args)) {
        this.parse(args);
    }
}

Region.prototype = {
    load: function (obj) {
        if (_.isString(obj)) {
            return this.parse(obj);
        }
        this.chromosome = obj.chromosome || this.chromosome;
        this.chromosome = this.chromosome;

        (_.isUndefined(obj.start)) ? this.start = parseInt(this.start) : this.start = parseInt(obj.start);
        (_.isUndefined(obj.end)) ? this.end = parseInt(this.end) : this.end = parseInt(obj.end);
    },

    parse: function (str) {
        if (_.isObject(str)) {
            this.load(obj);
        }
        var pattern = /^([a-zA-Z0-9_])+\:([0-9])+\-([0-9])+$/;
        var pattern2 = /^([a-zA-Z0-9_])+\:([0-9])+$/;
        if (pattern.test(str) || pattern2.test(str)) {
            var splitDots = str.split(":");
            if (splitDots.length == 2) {
                var splitDash = splitDots[1].split("-");
                this.chromosome = splitDots[0];
                this.start = parseInt(splitDash[0]);
                if (splitDash.length == 2) {
                    this.end = parseInt(splitDash[1]);
                } else {
                    this.end = this.start;
                }
            }
            return true
        } else {
            return false;
        }
    },

    center: function () {
        return this.start + Math.floor((this.length()) / 2);
    },

    length: function () {
        return this.end - this.start + 1;
    },

    toString: function (formated) {
        var str;
        if (formated == true) {
            str = this.chromosome + ":" + Utils.formatNumber(this.start) + "-" + Utils.formatNumber(this.end);
        } else {
            str = this.chromosome + ":" + this.start + "-" + this.end;
        }
        return str;
    }
};



/**
 * A binary search tree implementation in JavaScript. This implementation
 * does not allow duplicate values to be inserted into the tree, ensuring
 * that there is just one instance of each value.
 * @class BinarySearchTree
 * @constructor
 */
function FeatureBinarySearchTree() {
    
    /**
     * Pointer to root node in the tree.
     * @property _root
     * @type Object
     * @private
     */
    this._root = null;
}

FeatureBinarySearchTree.prototype = {

    //restore constructor
    constructor: FeatureBinarySearchTree,
    
    //-------------------------------------------------------------------------
    // Private members
    //-------------------------------------------------------------------------
    
    /**
     * Appends some data to the appropriate point in the tree. If there are no
     * nodes in the tree, the data becomes the root. If there are other nodes
     * in the tree, then the tree must be traversed to find the correct spot
     * for insertion. 
     * @param {variant} value The data to add to the list.
     * @return {Void}
     * @method add
     */
    add: function (v){
        //create a new item object, place data in
        var node = { 
                value: v, 
                left: null,
                right: null 
            },
            
            //used to traverse the structure
            current;
    
        //special case: no items in the tree yet
        if (this._root === null){
            this._root = node;
            return true;
        } 
        	//else
            current = this._root;
            
            while(true){
            
                //if the new value is less than this node's value, go left
                if (node.value.end < current.value.start){
                
                    //if there's no left, then the new node belongs there
                    if (current.left === null){
                        current.left = node;
                        return true;
//                        break;
                    } 
                    	//else                  
                        current = current.left;
                    
                //if the new value is greater than this node's value, go right
                } else if (node.value.start > current.value.end){
                
                    //if there's no right, then the new node belongs there
                    if (current.right === null){
                        current.right = node;
                        return true;
//                        break;
                    } 
                    	//else
                        current = current.right;
 
                //if the new value is equal to the current one, just ignore
                } else {
                	return false;
//                    break;
                }
            }        
        
    },
    
    contains: function (v){
        var node = { 
                value: v, 
                left: null,
                right: null 
            },
    	found = false,
    	current = this._root;
          
      //make sure there's a node to search
      while(!found && current){
      
          //if the value is less than the current node's, go left
          if (node.value.end < current.value.start){
              current = current.left;
              
          //if the value is greater than the current node's, go right
          } else if (node.value.start > current.value.end){
              current = current.right;
              
          //values are equal, found it!
          } else {
              found = true;
          }
      }
      
      //only proceed if the node was found
      return found;   
        
    }
};
/*! Genome Viewer - v1.0.3 - 2013-12-04 18:49:15
* http://https://github.com/opencb/jsorolla/
* Copyright (c) 2013  Licensed GPLv2 */
var CellBaseManager = {
    get: function (args) {
        var success = args.success;
        var error = args.error;
        var async = (_.isUndefined(args.async) || _.isNull(args.async) ) ? true : args.async;
        var urlConfig = _.omit(args, ['success', 'error', 'async']);

        var url = CellBaseManager.url(urlConfig);
        if(typeof url === 'undefined'){
            return;
        }
        console.log(url);

        var d;
        $.ajax({
            type: "GET",
            url: url,
            dataType: 'json',//still firefox 20 does not auto serialize JSON, You can force it to always do the parsing by adding dataType: 'json' to your call.
            async: async,
            success: function (data, textStatus, jqXHR) {
                if($.isPlainObject(data) || $.isArray(data)){
//                    data.params = args.params;
//                    data.resource = args.resource;
//                    data.category = args.category;
//                    data.subCategory = args.subCategory;
                    if (_.isFunction(success)) success(data);
                    d = data;
                }else{
                    console.log('Cellbase returned a non json object or list, please check the url.');
                    console.log(url);
                    console.log(data)
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log("CellBaseManager: Ajax call returned : " + errorThrown + '\t' + textStatus + '\t' + jqXHR.statusText + " END");
                if (_.isFunction(error)) error(jqXHR, textStatus, errorThrown);
            }
        });
        return d;
    },
    url: function (args) {
        if (!$.isPlainObject(args)) args = {};
        if (!$.isPlainObject(args.params)) args.params = {};

        var version = 'latest';
        if(typeof CELLBASE_VERSION !== 'undefined'){
            version = CELLBASE_VERSION
        }
        if(typeof args.version !== 'undefined' && args.version != null){
            version = args.version
        }

        var host;
        if(typeof CELLBASE_HOST !== 'undefined'){
            host = CELLBASE_HOST
        }
        if (typeof args.host !== 'undefined' && args.version != null) {
            host =  args.host;
        }
        if(typeof host === 'undefined'){
            console.log("CELLBASE_HOST is not configured");
            return;
        }

        delete args.host;
        delete args.version;

        var config = {
            host: host,
            version: version
        };

        var params = {
            of: 'json'
        };

        _.extend(config, args);
        _.extend(config.params, params);

        var query = '';
        if(typeof config.query !== 'undefined' && config.query != null){
            if ($.isArray(config.query)) {
                config.query = config.query.toString();
            }
            query = '/' + config.query;
        }

        //species can be the species code(String) or an object with text attribute
        if ($.isPlainObject(config.species)) {
            config.species = Utils.getSpeciesCode(config.species.text);
        }

        var url = config.host + '/' + config.version + '/' + config.species + '/' + config.category + '/' + config.subCategory + query + '/' + config.resource;
        url = Utils.addQueryParamtersToUrl(config.params, url);
        return url;
    }
};
function InfoWidget(targetId, species, args){
	this.id = "InfoWidget_" + Math.round(Math.random()*10000000);
	this.targetId = null;
	
	this.species=species;
	
	this.title = null;
	this.featureId = null;
	this.width = 800;
	this.height = 460;
	
	this.feature = null;
	this.query = null;
	this.adapter = null;
	
	
	if (targetId!= null){
		this.targetId = targetId;       
	}
	if (args != null){
        if (args.title!= null){
        	this.title = args.title;       
        }
        if (args.width!= null){
        	this.width = args.width;       
        }
        if (args.height!= null){
        	this.height = args.height;       
        }
    }
	
	switch (Utils.getSpeciesCode(species.text)){
	case "hsapiens":
		this.ensemblSpecie = "Homo_sapiens"; 
		this.reactomeSpecie = "48887"; 
		this.wikipathwaysSpecie = "Homo+sapiens"; 
		this.omimSpecie = ""; 
		this.uniprotSpecie = ""; 
		this.intactSpecie = ""; 
		this.dbsnpSpecie = ""; 
		this.haphapSpecie = ""; 
//		this.Specie = ""; 
		break;
	case "mmusculus":
		this.ensemblSpecies = "Mus_musculus"; 
		this.reactomeSpecies = "48892";
		this.wikipathwaysSpecie = "Mus+musculus"; 
		this.omimSpecie = ""; 
		this.uniprotSpecie = ""; 
		this.intactSpecie = ""; 
		this.dbsnpSpecie = ""; 
		this.haphapSpecie = ""; 
//		this.Specie = ""; 
		break;
	case "drerio":
		this.ensemblSpecie = "Danio_rerio"; 
		this.reactomeSpecie = "68323"; 
		this.wikipathwaysSpecie = "Danio+rerio"; 
		this.omimSpecie = ""; 
		this.uniprotSpecie = ""; 
		this.intactSpecie = ""; 
		this.dbsnpSpecie = ""; 
		this.haphapSpecie = ""; 
//		this.Specie = ""; 
		break;
	}
	
	this.notFoundPanel = Ext.create('Ext.panel.Panel',{
		id:this.id+"notFoundPanel",
		cls:'ocb-border-left-lightgrey',
		border:false,
		flex:3,
		bodyPadding:'40',
		html:'No results found'
	});
	
};

InfoWidget.prototype.draw = function (args){
	console.log(args);
//	this.featureId = feature.id;
	this.query = args.query;
	this.feature = args.feature;
	this.adapter = args.adapter;
//	if(feature.getName()==null){
//		console.log("getName not defined");
////		var feature = new Object();
////		feature.getName = function(){return feature;};
//	}	
	
//	console.log(feature.getName());
//	this.feature.getName = function(){return "a";};
	
	this.panel=Ext.getCmp(this.title +" "+ this.query);
	if (this.panel == null){
		//the order is important
		this.render();
		this.panel.show();
		this.getData();
	}else{
		this.panel.show();
	}
};

InfoWidget.prototype.render = function (){
		/**MAIN PANEL**/
		this.panel = Ext.create('Ext.window.Window', {
		    title: this.title +" "+ this.query,
		    id : this.title +" "+ this.query,
//		    resizable: false,
		    minimizable :true,
			constrain:true,
		    closable:true,
		    height:this.height,
		    width:this.width,
//		    modal:true,
//			layout: {type: 'table',columns: 2},
		    layout: { type: 'hbox',align: 'stretch'},
		    items: [this.getTreePanel()],
		    buttonAlign:'right',
//		    buttons:[],
		    listeners: {
			       scope: this,
			       minimize:function(){
			       		this.panel.hide();
			       },
			       destroy: function(){
			       		delete this.panel;
			       }
	        }
		});
};

InfoWidget.prototype.getTreePanel = function (){
		var dataTypes = this.getdataTypes();
	   	this.checkDataTypes(dataTypes);
	        
		var treeStore = Ext.create('Ext.data.TreeStore', {
		    root: {
		        expanded: true,
		        text: "Options",
		        children: dataTypes
		    }
		});
		
		var treePan = Ext.create('Ext.tree.Panel', {
		    title: 'Detailed information',
		    bodyPadding:10,
		    flex:1,
		   	border:false,
		    store: treeStore,
		    useArrows: true,
		    rootVisible: false,
		    listeners : {
			    	scope: this,
			    	itemclick : function (este,record){
			    		this.optionClick(record.data);
		    		}
			}
		});
		return treePan;
};



InfoWidget.prototype.doGrid = function (columns,fields,modelName,groupField){
		var groupFeature = Ext.create('Ext.grid.feature.Grouping',{
			groupHeaderTpl: '{[values.name]} ({rows.length} Item{[values.rows.length > 1 ? "s" : ""]})',
			startCollapsed: true
	    });
		var filters = [];
		for(var i=0; i<fields.length; i++){
			filters.push({type:'string', dataIndex:fields[i]});
		}
		var filters = {
				ftype: 'filters',
				local: true, // defaults to false (remote filtering)
				filters: filters
		};
	    Ext.define(modelName, {
		    extend: 'Ext.data.Model',
	    	fields:fields
		});
	   	var store = Ext.create('Ext.data.Store', {
			groupField: groupField,
			model:modelName
	    });
		var grid = Ext.create('Ext.grid.Panel', {
			id: this.id+modelName,
	        store: store,
	        title : modelName,
	        border:false,
	        cls:'ocb-border-left-lightgrey',
			flex:3,        
	        features: [groupFeature,filters],
	        viewConfig: {
//	            stripeRows: true,
	            enableTextSelection: true
	        },
	        columns: columns,
	        bbar  : ['->', {
	            text:'Clear Grouping',
	            handler : function(){
	                groupFeature.disable();
	            }
	        }]
	    });
    return grid;
};


InfoWidget.prototype.checkDataTypes = function (dataTypes){
	for (var i = 0; i<dataTypes.length; i++){
		if(dataTypes[i]["children"]!=null){
			dataTypes[i]["iconCls"] ='icon-box';
			dataTypes[i]["expanded"] =true;
			this.checkDataTypes(dataTypes[i]["children"]);
		}else{
			dataTypes[i]["iconCls"] ='icon-blue-box';
			dataTypes[i]["leaf"]=true;
		}
	}
};

InfoWidget.prototype.getdataTypes = function (){
	//Abstract method
	return [];
};
InfoWidget.prototype.optionClick = function (){
	//Abstract method
};
InfoWidget.prototype.getData = function (){
	//Abstract method
};

InfoWidget.prototype.getGeneTemplate = function (){
	return  new Ext.XTemplate(
		    '<div><span class="panel-border-bottom"><span class="ssel s130">{name}</span> &nbsp; <span class="emph s120"> {id} </span></span>',
			' &nbsp; <a target="_blank" href="http://www.ensembl.org/'+this.ensemblSpecie+'/Location/View?g={id}">Ensembl</a>',
			' &nbsp; <a target="_blank" href="http://wikipathways.org//index.php?query={externalName}&species='+this.wikipathwaysSpecie+'&title=Special%3ASearchPathways&doSearch=1">Wikipathways</a>',
			'</div><br>',
		    '<div><span class="w75 infokey s90">Location: </span> <span class="">{chromosome}:{start}-{end} </span><span style="margin-left:50px" class=" infokey s90">Strand: </span> {strand}</div>',
		    '<div><span class="w75 infokey s90">Biotype: </span> {biotype}</div>',
		    '<div><span class="w75 infokey s90">Description: </span> <span><tpl if="description == &quot;&quot;">No description available</tpl>{description}</span></div>',
		    '<div><span class="w75 infokey s90">Source: </span> <span class="s110">{source}</span></div>',
//		    '<div><span class="w75 infokey s90">External DB: </span> {externalDb}</div>',
		    '<div><span class="w75 infokey s90">Status: </span> {status}</div>' // +  '<br>'+str
	);
};
InfoWidget.prototype.getTranscriptTemplate = function (){
	return new Ext.XTemplate(
		    '<div><span class="panel-border-bottom"><span class="ssel s130">{name}</span> &nbsp; <span class="emph s120"> {id} </span></span>',
		    ' &nbsp; <a target="_blank" href="http://www.ensembl.org/'+this.ensemblSpecie+'/Transcript/Transcript?t={id}">Ensembl</a>',
		    ' &nbsp; <a target="_blank" href="http://wikipathways.org//index.php?query={externalName}&species='+this.wikipathwaysSpecie+'&title=Special%3ASearchPathways&doSearch=1">Wikipathways</a>',
		    '</div><br>',
		    '<div><span class="w100 infokey s90">Location: </span> <span class="">{chromosome}:{start}-{end} </span><span style="margin-left:50px" class=" infokey s90">Strand: </span> {strand}</div>',
		    '<div><span class="w100 infokey s90">Biotype: </span> {biotype}</div>',
		    '<div><span class="w100 infokey s90">Description: </span> <span><tpl if="description == &quot;&quot;">No description available</tpl>{description}</span></div>',
		    '',
		    '<div><span class="w100 infokey s90">CDS &nbsp; (start-end): </span> {genomicCodingStart}-{genomicCodingEnd} <span style="margin-left:50px" class="w100 infokey s90">CDNA (start-end): </span> {cdnaCodingStart}-{cdnaCodingEnd}</div>',
            '<div><span class="w100 infokey s90">Protein: </span> {proteinID}</div>',
		    '<div><span class="w100 infokey s90">External DB: </span> {externalDb}</div>',
		    '<div><span class="w100 infokey s90">Status: </span> {status}</div><br>'// +  '<br>'+str
		);
};
InfoWidget.prototype.getSnpTemplate = function (){

//
//    alleleString: "C/T"
//    alternate: "T"
//    assembly: ""
//    chromosome: "13"
//    end: 32889669
//    featureAlias: "featureAlias"
//    featureId: "featureId"
//    id: "rs55880202"
//    populationFrequencies: null
//    reference: "C"
//    samples: Array[0]
//    source: ""
//    species: ""
//    start: 32889669
//    strand: "1"
//    transcriptVariations: Array[6]
//    type: "SNV"
//    validationStatus: "freq,1000Genome"
//    variantFreq: "variantFreq"
//    version: ""
//    xrefs: Array[0]

	return new Ext.XTemplate(
		    '<div><span class="panel-border-bottom"><span class="ssel s130">{id}</span></span>',
		    ' &nbsp; <a target="_blank" href="http://www.ensembl.org/'+this.ensemblSpecie+'/Variation/Summary?v={id}">Ensembl</a>',
		    '</div><br>',
		    '<div><span class="w140 infokey s90">Location: </span> <span class="">{chromosome}:{start}-{end} </span><span style="margin-left:50px" class=" infokey s90">Strand: </span> {strand}</div>',
		    '<div><span class="w140 infokey s90">Source: </span> <span class="s110">{source}</span></div>',
		    '<div><span class="w140 infokey s90">Type: </span> <span class="s110">{type}</span></div>',
		    '<div><span class="w140 infokey s90">Allele string: </span> {alleleString}</div>',
		    '<div><span class="w140 infokey s90">Ancestral allele: </span> {ancestralAllele}</div>',
		    '<div><span class="w140 infokey s90">Display SO consequence type: </span> {displayConsequenceType}</div>',
		    '<div><span class="w140 infokey s90">SO consequence types: </span> {consequenceTypes}</div>'
//		    '<div><span class="w140 infokey s90">Xrefs: </span> {xrefs}</div>'
//		    '<div><span class="w140 infokey s90">Sequence: </span> {sequence}</div>' // +  '<br>'+str
		);
};

InfoWidget.prototype.getExonTemplate = function (){
	return new Ext.XTemplate(
			'<span><span class="panel-border-bottom"><span class="ssel s110">{id}</span></span></span><br><br>',
			'<span><span class="infokey s90"> Location: </span> <span class="">{chromosome}:{start}-{end} </span></span><br>',
			'<span><span class="infokey s90"> Genomic coding (start-end) : </span> <span class="">{genomicCodingStart}-{genomicCodingEnd} </span></span><br>',
			'<span><span class="infokey s90"> cDNA (start-end) : </span> <span class="">{cdnaCodingStart}-{cdnaCodingEnd} </span></span><br>',
			'<span><span class="infokey s90"> CDS (start-end) : </span> <span class="">{cdsStart}-{cdsEnd} </span></span><br>',
			'<span><span class="infokey s90"> Phase: </span> {phase}</span><br>'
		);
};

InfoWidget.prototype.getProteinTemplate = function (){
	return new Ext.XTemplate(
			 '<div><span class="panel-border-bottom"><span class="ssel s130">{name}</span> &nbsp; <span class="emph s120"> {primaryAccession} </span></span></div>',
			 '<br>',
			 '<div><span class="w100 infokey s90">Full name: </span> <span class="">{fullName}</span></div>',
			 '<div><span class="w100 infokey s90">Gene name: </span> <span class="">{geneName}</span></div>',
			 '<div><span class="w100 infokey s90">Organism: </span> <span class="">{organism}</span></div>'
		);
};


InfoWidget.prototype.getVCFVariantTemplate = function (){
	return new Ext.XTemplate(
			'<div><span><span class="panel-border-bottom"><span class="ssel s130">{chromosome}:{start}-{alternate}</span> &nbsp; <span class="emph s120"> {id} </span></span></span></div><br>',
			'<div><span class="w75 infokey s90">Alt: </span> {alternate}</div>',
			'<div><span class="w75 infokey s90">Ref: </span> {reference}</div>',
			'<div><span class="w75 infokey s90">Quality: </span> {quality}</div>',
			'<div><span class="w75 infokey s90">Format: </span> {format}</div>',
			'<div><span class="w75 infokey s90">Samples: </span> {samples}</div>',
			'<div><span class="w75 infokey s90">Info: <br></span> {info}</div>'
		);
};

InfoWidget.prototype.getPWMTemplate = function (){
	return new Ext.XTemplate(
			 '<div><span class="panel-border-bottom"><span class="ssel s130">{accession}</span> &nbsp; <span class="emph s120"> {tfName} </span></span></div>',
			 '<br>',
			 '<div><span class="w100 infokey s90">Type: </span> <span class="">{source}</span></div>',
			 '<div><span class="w100 infokey s90">Source: </span> <span class="">{type}</span></div>',
			 '<div><span class="w100 infokey s90">Description: </span> <span class="">{description}</span></div>',
			 '<div><span class="w100 infokey s90">Length: </span> <span class="">{length}</span></div>',
			 '<div><span class="w100 infokey s90">Frequencies: </span> <span class="">{[this.parseFrequencies(values.frequencies)]}</span></div>',
			 {
				 parseFrequencies: function(values){
					 return '<div>'+values.replace(/,/gi, '<br>')+"</div>";
				 }
			 }
		);
};

InfoWidget.prototype.getProteinXrefTemplate = function (){
	return new Ext.XTemplate(
			'<div><span class="w75 emph s100">{[values.source.toUpperCase()]}</span> &nbsp; <span class="emph w125 s100"> {[this.generateLink(values)]} <span class="info">&raquo;</span> </span></div>',
			{
				generateLink: function(values){
					if(values.source!=null){
						switch(values.source.toUpperCase()){
						case "GO": 	return 		'<a TARGET="_blank" href="http://amigo.geneontology.org/cgi-bin/amigo/term_details?term='+values.name+'">'+values.name+'</a>'; break;
						case "REACTOME": return '<a TARGET="_blank" href="http://www.reactome.org/cgi-bin/eventbrowser_st_id?ST_ID='+values.name+'">'+values.name+'</a>'; break;
						case "KEGG": return 	'<a TARGET="_blank" href="http://www.genome.jp/dbget-bin/www_bget?'+values.name+'">'+values.name+'</a>'; break;
						case "INTACT": return 	'<a TARGET="_blank" href="http://www.ebi.ac.uk/intact/pages/interactions/interactions.xhtml?query='+values.name+'">'+values.name+'</a>'; break;
						case "MINT": return 	'<a TARGET="_blank" href="http://mint.bio.uniroma2.it/mint/search/search.do?queryType=protein&interactorAc='+values.name+'">'+values.name+'</a>'; break;
						case "DIP": return 		'<a TARGET="_blank" href="http://dip.doe-mbi.ucla.edu/dip/Browse.cgi?ID='+values.name+'">'+values.name+'</a>'; break;
						case "STRING": return 	'<a TARGET="_blank" href="http://string-db.org/newstring_cgi/show_network_section.pl?identifier=P51587">'+values.name+'</a>'; break;
						case "MIM": return 		'<a TARGET="_blank" href="http://www.omim.org/entry/'+values.name+'">'+values.name+'</a>'; break;
						case "PHARMGKB": return '<a TARGET="_blank" href="http://www.pharmgkb.org/do/serve?objId='+values.name+'&objCls=Gene">'+values.name+'</a>'; break;
						case "ORPHANET": return '<a TARGET="_blank" href="http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=EN&Expert='+values.name+'">'+values.name+'</a>'; break;
						}
					}
					else{
						return "";
					}
				}
			}
		);
};

InfoWidget.prototype.getSnpTranscriptTemplate = function (){
//    alleleString: "C/T"
//    cdnEnd: 0
//    cdnaStart: 0
//    cdsEnd: 0
//    cdsStart: 0
//    codonAlleleString: ""
//    consequenceTypes: Array[1]
//    distanceToTranscript: 188
//    hgvsGenomic: "13:g.32889669C>T"
//    hgvsProtein: ""
//    hgvsTranscript: ""
//    peptideAlleleString: ""
//    polyphenPrediction: ""
//    polyphenScore: 0
//    siftPrediction: ""
//    siftScore: 0
//    somatic: "0"
//    transcriptId: "ENST00000533490"
//    translationEnd: 0
//    translationStart: 0

	return new Ext.XTemplate(
		    '<div><span class="panel-border-bottom"><span class="ssel s130">{[this.getStableId(values)]}</span></span>',
		    ' &nbsp; <a target="_blank" href="http://www.ensembl.org/'+this.ensemblSpecie+'/Transcript/Transcript?t={[this.getStableId(values)]}">Ensembl</a>',
		    '</div><br>',
		    '<div><span class="w140 infokey s90">CDS &nbsp; (start - end): </span> {cdsStart} - {cdsEnd} <span style="margin-left:50px" class="w100 infokey s90">cDNA (start - end): </span> {cdnaStart} - {cdnaEnd}</div>',
		    '<div><span class="w140 infokey s90">Translation (start - end): </span> {translationStart} - {translationEnd}</div>',
		    '<div><span class="w140 infokey s90">Peptide allele: </span> {peptideAlleleString}</div>',
//		    '<div><span class="w140 infokey s90">Alt. peptide allele: </span> {alternativePeptideAlleleString}</div>',
			'<div><span class="w140 infokey s90">Codon: </span> {codonAlleleString}</div>',

            '<div><span class="w140 infokey s90">HGVS Genomic: </span> {hgvsGenomic}',
            '<div><span class="w140 infokey s90">HGVS Protein: </span> {hgvsProtein}',
            '<div><span class="w140 infokey s90">HGVS Transcript: </span> {hgvsTranscript}',

//			'<div><span class="w140 infokey s90">Reference codon: </span> {referenceCodon}</div>',
			'<div><span class="w140 infokey s90">Polyphen prediction: </span> {polyphenPrediction}',
			'<span style="margin-left:50px" class="w140 infokey s90">Polyphen score: </span> {polyphenScore}</div>',
			'<div><span class="w140 infokey s90">Sift prediction: </span> {siftPrediction}',
			'<span style="margin-left:50px" class="w140 infokey s90">Sift score: </span> {siftScore}</div>',
            '<div><span class="w140 infokey s90">SO consequence types: </span> {consequenceTypes}</div><br>',
		    {
		    	getStableId: function(values){
		    		if(values.transcriptId!=""){
		    			return values.transcriptId;
		    		}
		    		return "Intergenic SNP";
		    	}
		    }
		);
};


InfoWidget.prototype.getConsequenceTypeTemplate = function (){
	return new Ext.XTemplate(
		    '<div><span class="panel-border-bottom"><span class="ssel s130">{transcriptId}</span> &nbsp; </span></div><br>',
		    '<div><span class="w140 infokey s90">SO consequence types: </span> {consequenceTypes}</div><br>'
//		    '<div><span class="w100 infokey s90">SO term: </span> {consequenceType.soTerm}</div>',
//		    '<div><span class="w100 infokey s90">Feature So term: </span> {consequenceType.featureSoTerm}</div>',
//		    '<div><span class="w100 infokey s90">NCBI term: </span> {consequenceType.ncbiTerm}</div>',
//		    '<div><span class="w100 infokey s90">Rank: </span> {consequenceType.rank}</div><br>'
		);
};


InfoWidget.prototype.getPhenotypeTemplate = function (){
	return new Ext.XTemplate(
		    '<div><span class="panel-border-bottom"><span class="ssel s130">{phenotypeDescription}</span> &nbsp; <span class="emph s120"> {source} </span></span></div><br>',
			'<div><span class="w150 infokey s90">PValue: </span>{PValue}</div>',
			'<div><span class="w150 infokey s90">Assoc. gene name: </span>{associatedGeneName}</div>',
			'<div><span class="w150 infokey s90">Assoc. variant risk allele: </span>{associatedVariantRiskAllele}</div>',
			'<div><span class="w150 infokey s90">Phenotype description: </span>{phenotypeDescription}</div>',
			'<div><span class="w150 infokey s90">Phenotype name: </span>{phenotypeName}</div>',
			'<div><span class="w150 infokey s90">Risk allele freq in controls: </span>{riskAlleleFrequencyInControls}</div>',
			'<div><span class="w150 infokey s90">Source: </span>{source}</div>',
			'<div><span class="w150 infokey s90">Study name: </span>{studyName}</div>',
			'<div><span class="w150 infokey s90">Study type: </span>{studyType}</div>',
			'<div><span class="w150 infokey s90">Study URL: </span>{studyUrl}</div>',
			'<div><span class="w150 infokey s90">Study description: </span>{studyDescription}</div>'
		);
};

InfoWidget.prototype.getPopulationTemplate = function (){
	return new Ext.XTemplate(
		    '<div><span class="panel-border-bottom"><span class="ssel s130">{population}</span> &nbsp; <span class="emph s120"> {source} </span></span></div><br>',
		    '<div><span class="w140 infokey s90">Ref allele:  </span>{refAllele} ({refAlleleFrequency})</div>',
		    '<div><span class="w140 infokey s90">Other allele:  </span>{otherAllele} ({otherAlleleFrequency})</div>',
		    '<div><span class="w140 infokey s90">Ref allele homozygote:  </span>{refAlleleHomozygote} ({refAlleleHomozygoteFrequency})</div>',
		    '<div><span class="w140 infokey s90">Allele heterozygote:  </span>{alleleHeterozygote} ({alleleHeterozygoteFrequency})</div>',
			 '<div><span class="w140 infokey s90">Other allele homozygote:  </span>{otherAlleleHomozygote} ({otherAlleleHeterozygoteFrequency})</div>',
//			 'TODO cuidado <div><span class="w140 infokey s90">other allele heterozygote Frequency:  </span>{otherAlleleHeterozygoteFrequency}</div>',
			 '<div><span class="w140 infokey s90">Source:  </span>{source}</div>',
			 '<div><span class="w140 infokey s90">Population:  </span>{population}</div>'
		);
};

//not used
InfoWidget.prototype.getVariantEffectTemplate = function (){
		
	return new Ext.XTemplate(
		    '<div><span class="panel-border-bottom"><span class="ssel s130">{consequenceTypeObo}</span> &nbsp; <span class="emph s120"> {featureBiotype} </span></span></div><br>'
		);
};

GeneInfoWidget.prototype.draw = InfoWidget.prototype.draw;
GeneInfoWidget.prototype.render = InfoWidget.prototype.render;
GeneInfoWidget.prototype.getTreePanel = InfoWidget.prototype.getTreePanel;
GeneInfoWidget.prototype.checkDataTypes = InfoWidget.prototype.checkDataTypes;
GeneInfoWidget.prototype.doGrid = InfoWidget.prototype.doGrid;
GeneInfoWidget.prototype.getGeneTemplate = InfoWidget.prototype.getGeneTemplate;
GeneInfoWidget.prototype.getTranscriptTemplate = InfoWidget.prototype.getTranscriptTemplate;

function GeneInfoWidget(targetId, species, args){
	if (args == null){
		args = new Object();
	}
	args.title = "Gene Info";
	InfoWidget.prototype.constructor.call(this, targetId, species, args);
};

GeneInfoWidget.prototype.getdataTypes = function (){
	//Abstract method
	return dataTypes=[
	            { text: "Genomic", children: [
	                { text: "Information"},
	                { text: "Transcripts"},
                    { text: "Xrefs"}
	            ] },
	            { text: "Functional information", children: [
	                { text: "GO"},
	                { text: "Reactome"},
	                { text: "Interpro"}
	            ] },
	            { text: "Regulatory", children: [
	                { text: "TFBS"}
//	                { text: "miRNA targets"}
	            ]},
	            { text:"Protein", children: [
	                { text: "Features"},//protein profile
	                { text: "3D structure"}
	            ]}	     
	        ];
};

GeneInfoWidget.prototype.optionClick = function (item){
	//Abstract method
	if (item.leaf){
		if(this.panel.getComponent(1)!=null){
			this.panel.getComponent(1).hide();
			this.panel.remove(1,false);
		}
		switch (item.text){
			case "Information": this.panel.add(this.getGenePanel(this.data).show()); break;
			case "Transcripts": this.panel.add(this.getTranscriptPanel(this.data.transcripts).show());  break;
			case "Xrefs": this.panel.add(this.getXrefGrid(this.data.transcripts, 'Xref', 'transcript').show());  break;
//			case "GO": this.panel.add(this.getGoGrid().show()); break;
			case "GO": this.panel.add(this.getXrefGrid(this.data.transcripts, 'GO', 'transcript').show());  break;
			case "Interpro": this.panel.add(this.getXrefGrid(this.data.transcripts, 'Interpro', 'transcript').show());  break;
			case "Reactome": this.panel.add(this.getXrefGrid(this.data.transcripts, 'Reactome', 'transcript').show());  break;
			case "TFBS": this.panel.add(this.getTfbsGrid(this.data.transcripts).show());  break;
			case "miRNA targets": this.panel.add(this.getMirnaTargetGrid(this.data.mirnaTargets).show());  break;
			case "Features": this.panel.add(this.getProteinFeaturesGrid(this.data.proteinFeatures).show());  break;
			case "3D structure": this.panel.add(this.get3Dprotein(this.data.snps).show());  break;
		}
	}
};

GeneInfoWidget.prototype.getGenePanel = function(data){
	if(data==null){
		return this.notFoundPanel;
	}
    if(this.genePanel==null){
    	var tpl = this.getGeneTemplate();
    	
		this.genePanel = Ext.create('Ext.panel.Panel',{
			title:"Gene information",
	        border:false,
	        cls:'ocb-border-left-lightgrey',
			flex:3,
			bodyPadding:10,
			data:data,
			tpl:tpl
		});
    }
    return this.genePanel;
};


GeneInfoWidget.prototype.getTranscriptPanel = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.transcriptGrid==null){
    	
    	var tpl = this.getTranscriptTemplate();
    	
    	var panels = [];
    	for ( var i = 0; i < data.length; i++) {	
			var transcriptPanel = Ext.create('Ext.container.Container',{
				padding:5,
				data:data[i],
				tpl:tpl
			});
			panels.push(transcriptPanel);
    	}
		this.transcriptGrid = Ext.create('Ext.panel.Panel',{
			title:"Transcripts ("+i+")",
			border:false,
			cls:'ocb-border-left-lightgrey',
			flex:3,    
			bodyPadding:5,
			autoScroll:true,
			items:panels
		});
    }
    return this.transcriptGrid;
};


GeneInfoWidget.prototype.getXrefGrid = function(transcripts, dbname, groupField){
    var data = [];
    for(var i = 0; i<transcripts.length; i++){
        for(var j = 0; j<transcripts[i].xrefs.length; j++){
            var xref = transcripts[i].xrefs[j];
            if(dbname == 'Xref'){
                var shortName  = xref.dbNameShort.toLowerCase();
                if(shortName != 'go' && shortName != 'interpro' && shortName != 'reactome'){
                    xref.transcript = transcripts[i].id;
                    data.push(xref);
                }
            }else{
                if(xref.dbNameShort.toLowerCase() == dbname.toLowerCase()){
                    xref.transcript = transcripts[i].id;
                    data.push(xref);
                }
            }
        }
    }
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this[dbname+"Grid"]==null){
    	var groupField = groupField;
    	var modelName = dbname;
    	var fields = ['description','id', 'dbName', 'transcript'];
    	var columns = [
    	               {header : 'Display Id',dataIndex: 'id',flex:1},
    	               {header : 'DB name',dataIndex: 'dbName',flex:1},
    	               {header : 'Description',dataIndex: 'description',flex:3}
    	               ];
    	this[dbname+"Grid"] = this.doGrid(columns,fields,modelName,groupField);
    	this[dbname+"Grid"].store.loadData(data);
    }
    return this[dbname+"Grid"];
};

//GeneInfoWidget.prototype.getGoGrid = function(){
//    var _this = this;
//    if(this.goGrid==null){
//    	var groupField = 'namespace';
//    	var modelName = 'GO';
//	    var fields = ['id','name','description','level','directNumberOfGenes','namespace','parents','propagatedNumberOfGenes','score'];
//		var columns = [ {header : 'Database id',dataIndex: 'id',flex:2},
//						{header : 'Name',dataIndex: 'name',flex:1},
//						{header : 'Description',dataIndex: 'description',flex:2},
//		                {
//		                	xtype: 'actioncolumn',
//		                	header : '+info',
//		                    flex:1,
//		                    items: [{
//		                        iconCls: 'icon-blue-box',  // Use a URL in the icon config
//		                        tooltip: '+info',    
//		                        handler: function(grid, rowIndex, colIndex) {
//		                            var rec = _this.goStore.getAt(rowIndex);
//		                            Ext.Msg.alert(rec.get('name'), rec.get('description'));
//		                        }
//		                    }]
//		                 },
//		                {header : 'Direct genes',dataIndex: 'directNumberOfGenes',flex:2},
//						{header : 'Level',dataIndex: 'level',flex:1},
//						{header : 'Namespace',dataIndex: 'namespace',flex:2},
//						{header : 'Propagated genes',dataIndex: 'propagatedNumberOfGenes',flex:2.5}
//		             ];
//		this.goGrid = this.doGrid(columns,fields,modelName,groupField);
//		
//    }
//    return this.goGrid;
//};


GeneInfoWidget.prototype.getTfbsGrid = function(data){
    if(data.length<=0){
		return this.notFoundPanel;
	}
    var groupField = '';
    //check data are transcripts or tfbss

    if(data[0].id != null){
        var data2 = [];
        groupField = 'transcriptId';
        for(var i = 0; i<data.length; i++){
            transcript = data[i];
            if(transcript.tfbs != null){
                for(var j = 0; j<transcript.tfbs.length; j++){
                    transcript.tfbs[j].transcriptId = transcript.id;
                }
                data2 = data2.concat(transcript.tfbs);
            }
        }
        data = data2;
    }

    if(this.tfbsGrid==null){
    	var groupField = groupField;
    	var modelName = "TFBS";
	    var fields = ["chromosome","start","end","strand","tfName","relativeStart","relativeEnd","targetGeneName","score","sequence","transcriptId"];
		var columns = [
		                {header : 'Name',dataIndex: 'tfName',flex:1},
		            	{header : 'Location: chr:start-end (strand)', xtype:'templatecolumn', tpl:'{chromosome}:{start}-{end} ({strand})',flex:2.5},
		            	{header : 'Relative (start-end)',xtype:'templatecolumn',tpl:'{relativeStart}-{relativeEnd}',flex:1.5},
						{header : 'Target gene',dataIndex: 'targetGeneName',flex:1},
						{header : 'Score',dataIndex: 'score',flex:1},
						{header : 'Sequence',dataIndex: 'sequence',flex:1}
		             ];
		this.tfbsGrid = this.doGrid(columns,fields,modelName,groupField);
		this.tfbsGrid.store.loadData(data);
    }
    return this.tfbsGrid;
};

GeneInfoWidget.prototype.getMirnaTargetGrid = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.mirnaTargetGrid==null){
    	var groupField = "";
    	var modelName = "miRNA targets";
	    var fields = ["chromosome","start","end","strand","mirbaseId","score","experimentalMethod","source"];
		var columns = [
		                {header : 'Id',dataIndex: 'mirbaseId',flex:1},
		            	{header : 'Location: chr:start-end (strand)', xtype:'templatecolumn', tpl:'{chromosome}:{start}-{end} ({strand})',flex:2},
						{header : 'Score',dataIndex: 'score',flex:1},
						{header : 'Exp. Method',dataIndex: 'experimentalMethod',flex:1},
						{header : 'source',dataIndex: 'source',flex:1}
		             ];
		this.mirnaTargetGrid = this.doGrid(columns,fields,modelName,groupField);
		this.mirnaTargetGrid.store.loadData(data);
    }
    return this.mirnaTargetGrid;
};

GeneInfoWidget.prototype.getProteinFeaturesGrid = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.proteinFeaturesGrid==null){
    	var groupField = '';
    	var modelName = "Protein features";
	    var fields = ["identifier","start","end","original","type","description"];
		var columns = [
		                {header : 'Identifier',dataIndex: 'identifier',flex:1},
		               	{header : 'Location: (start-end)', xtype:'templatecolumn', tpl:'{start}-{end}',flex:1.2},
		               	{header : 'Original',dataIndex: 'original',flex:1},
						{header : 'Type',dataIndex: 'type',flex:1},
						{header : 'Description',dataIndex: 'description',flex:1.5}
		             ];
		this.proteinFeaturesGrid = this.doGrid(columns,fields,modelName,groupField);
		this.proteinFeaturesGrid.store.loadData(data);
    }
    return this.proteinFeaturesGrid;
};


GeneInfoWidget.prototype.getProteinFeaturesGrid = function(data){
    debugger
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.proteinFeaturesGrid==null){
    	var groupField = '';
    	var modelName = 'Protein features';
	    var fields = ["identifier","start","end","original","type","description"];
		var columns = [
		                {header : 'Identifier',dataIndex: 'identifier',flex:1},
		               	{header : 'Location: (start-end)', xtype:'templatecolumn', tpl:'{start}-{end}',flex:1.2},
		               	{header : 'Original',dataIndex: 'original',flex:1},
						{header : 'Type',dataIndex: 'type',flex:1},
						{header : 'Description',dataIndex: 'description',flex:1.5}
		             ];
		this.proteinFeaturesGrid = this.doGrid(columns,fields,modelName,groupField);
		this.proteinFeaturesGrid.store.loadData(data);
    }
    return this.proteinFeaturesGrid;
};

GeneInfoWidget.prototype.get3Dprotein = function(data){
	var _this=this;
    if(this.p3dProtein==null){
    	//ws
//    	
      	this.p3dProtein = Ext.create('Ext.tab.Panel',{
      		title:"3D Protein Viewer",
      		border:false,
      		cls:'ocb-border-left-lightgrey',
      		flex:3,
//    		bodyPadding:5,
      		autoScroll:true
//      		items:items
      	});
    	
    	var pdbs = [];

    	$.ajax({
//    		  url: 'http://ws.bioinfo.cipf.es/celldb/rest/v1/hsa/feature/id/brca2/xref?dbname=pdb',
    		  url:new CellBaseManager().host+'/v3/'+_this.species+'/feature/id/'+this.query+'/xref?dbname=pdb&of=json',
//    		  data: data,
//    		  dataType: dataType,
    		  async:false,
    		  success: function(data){
    			if(data!=""){
//      	    		console.log(data.trim());
      	    		pdbs = data[0];
//      	    		console.log(pdbs);
      	    		
      	    		for ( var i = 0; i < pdbs.length; i++) {
      	    			var pdb_name=pdbs[i].id;
      	    			var pan = Ext.create('Ext.panel.Panel',{
      	    				title:pdb_name,
      	    				bodyCls:'background-black',
      	    				html:'<canvas class="ChemDoodleWebComponent" id="pdb_canvas_'+pdb_name+'" width="600" height="600" style="width: 600px; height: 600px; ">This browser does not support HTML5/Canvas.</canvas>',
      	    				listeners:{
      	    					afterrender:function(este){
      	    						// JavaScript Document
      	    						var pdb_name=este.title;
      	    						
      	    				    	ChemDoodle.default_backgroundColor = '#000000';
      	    				    	
      	    				    	var pdb = new ChemDoodle.TransformCanvas3D('pdb_canvas_'+pdb_name, 300, 300);
      	    				    	if(!pdb.gl){
      	    				    	  pdb.emptyMessage = 'Your browser does not support WebGL';
      	    				    	  pdb.displayMessage();
      	    				    	}else{
      	    					    	pdb.specs.set3DRepresentation('Ball and Stick');
      	    					    	pdb.specs.proteins_ribbonCartoonize = true;
      	    					    	pdb.handle = null;
      	    					    	pdb.timeout = 15;
      	    					    	pdb.startAnimation = ChemDoodle._AnimatorCanvas.prototype.startAnimation;
      	    					    	pdb.stopAnimation = ChemDoodle._AnimatorCanvas.prototype.stopAnimation;
      	    					    	pdb.isRunning = ChemDoodle._AnimatorCanvas.prototype.isRunning;
      	    					    	pdb.dblclick = ChemDoodle.RotatorCanvas.prototype.dblclick;
      	    					    	pdb.nextFrame = function(delta){
      	    					    		var matrix = [];
      	    					    		mat4.identity(matrix);
      	    					    		var change = delta/1000;
      	    					    	        var increment = Math.PI/15;
      	    					    		mat4.rotate(matrix, increment*change, [ 1, 0, 0 ]);
      	    					    		mat4.rotate(matrix, increment*change, [ 0, 1, 0 ]);
      	    					    		mat4.rotate(matrix, increment*change, [ 0, 0, 1 ]);
      	    					    		mat4.multiply(this.rotationMatrix, matrix);
      	    					    	};
      	    					    	
//      	    					    	http://ws.bioinfo.cipf.es/celldb/rest/v1/hsa/feature/id/brca2/xref?dbname=pdb
//      	    				    	var mol = ChemDoodle.readPDB('HEADER    PLANT SEED PROTEIN                      30-APR-81   1CRN                                                                       \nDBREF  1CRN A    1    46  UNP    P01542   CRAM_CRAAB       1     46             \nSEQRES   1 A   46  THR THR CYS CYS PRO SER ILE VAL ALA ARG SER ASN PHE          \nSEQRES   2 A   46  ASN VAL CYS ARG LEU PRO GLY THR PRO GLU ALA ILE CYS          \nSEQRES   3 A   46  ALA THR TYR THR GLY CYS ILE ILE ILE PRO GLY ALA THR          \nSEQRES   4 A   46  CYS PRO GLY ASP TYR ALA ASN                                  \nHELIX    1  H1 ILE A    7  PRO A   19  13/10 CONFORMATION RES 17,19       13    \nHELIX    2  H2 GLU A   23  THR A   30  1DISTORTED 3/10 AT RES 30           8    \nSHEET    1  S1 2 THR A   1  CYS A   4  0                                        \nSHEET    2  S1 2 CYS A  32  ILE A  35 -1                                        \nSSBOND   1 CYS A    3    CYS A   40                          1555   1555  2.00  \nSSBOND   2 CYS A    4    CYS A   32                          1555   1555  2.04  \nSSBOND   3 CYS A   16    CYS A   26                          1555   1555  2.05  \nCRYST1   40.960   18.650   22.520  90.00  90.77  90.00 P 1 21 1      2          \nORIGX1      1.000000  0.000000  0.000000        0.00000                         \nORIGX2      0.000000  1.000000  0.000000        0.00000                         \nORIGX3      0.000000  0.000000  1.000000        0.00000                         \nSCALE1      0.024414  0.000000 -0.000328        0.00000                         \nSCALE2      0.000000  0.053619  0.000000        0.00000                         \nSCALE3      0.000000  0.000000  0.044409        0.00000                         \nATOM      1  N   THR A   1      17.047  14.099   3.625  1.00 13.79           N  \nATOM      2  CA  THR A   1      16.967  12.784   4.338  1.00 10.80           C  \nATOM      3  C   THR A   1      15.685  12.755   5.133  1.00  9.19           C  \nATOM      4  O   THR A   1      15.268  13.825   5.594  1.00  9.85           O  \nATOM      5  CB  THR A   1      18.170  12.703   5.337  1.00 13.02           C  \nATOM      6  OG1 THR A   1      19.334  12.829   4.463  1.00 15.06           O  \nATOM      7  CG2 THR A   1      18.150  11.546   6.304  1.00 14.23           C  \nATOM      8  N   THR A   2      15.115  11.555   5.265  1.00  7.81           N  \nATOM      9  CA  THR A   2      13.856  11.469   6.066  1.00  8.31           C  \nATOM     10  C   THR A   2      14.164  10.785   7.379  1.00  5.80           C  \nATOM     11  O   THR A   2      14.993   9.862   7.443  1.00  6.94           O  \nATOM     12  CB  THR A   2      12.732  10.711   5.261  1.00 10.32           C  \nATOM     13  OG1 THR A   2      13.308   9.439   4.926  1.00 12.81           O  \nATOM     14  CG2 THR A   2      12.484  11.442   3.895  1.00 11.90           C  \nATOM     15  N   CYS A   3      13.488  11.241   8.417  1.00  5.24           N  \nATOM     16  CA  CYS A   3      13.660  10.707   9.787  1.00  5.39           C  \nATOM     17  C   CYS A   3      12.269  10.431  10.323  1.00  4.45           C  \nATOM     18  O   CYS A   3      11.393  11.308  10.185  1.00  6.54           O  \nATOM     19  CB  CYS A   3      14.368  11.748  10.691  1.00  5.99           C  \nATOM     20  SG  CYS A   3      15.885  12.426  10.016  1.00  7.01           S  \nATOM     21  N   CYS A   4      12.019   9.272  10.928  1.00  3.90           N  \nATOM     22  CA  CYS A   4      10.646   8.991  11.408  1.00  4.24           C  \nATOM     23  C   CYS A   4      10.654   8.793  12.919  1.00  3.72           C  \nATOM     24  O   CYS A   4      11.659   8.296  13.491  1.00  5.30           O  \nATOM     25  CB  CYS A   4      10.057   7.752  10.682  1.00  4.41           C  \nATOM     26  SG  CYS A   4       9.837   8.018   8.904  1.00  4.72           S  \nATOM     27  N   PRO A   5       9.561   9.108  13.563  1.00  3.96           N  \nATOM     28  CA  PRO A   5       9.448   9.034  15.012  1.00  4.25           C  \nATOM     29  C   PRO A   5       9.288   7.670  15.606  1.00  4.96           C  \nATOM     30  O   PRO A   5       9.490   7.519  16.819  1.00  7.44           O  \nATOM     31  CB  PRO A   5       8.230   9.957  15.345  1.00  5.11           C  \nATOM     32  CG  PRO A   5       7.338   9.786  14.114  1.00  5.24           C  \nATOM     33  CD  PRO A   5       8.366   9.804  12.958  1.00  5.20           C  \nATOM     34  N   SER A   6       8.875   6.686  14.796  1.00  4.83           N  \nATOM     35  CA  SER A   6       8.673   5.314  15.279  1.00  4.45           C  \nATOM     36  C   SER A   6       8.753   4.376  14.083  1.00  4.99           C  \nATOM     37  O   SER A   6       8.726   4.858  12.923  1.00  4.61           O  \nATOM     38  CB  SER A   6       7.340   5.121  15.996  1.00  5.05           C  \nATOM     39  OG  SER A   6       6.274   5.220  15.031  1.00  6.39           O  \nATOM     40  N   ILE A   7       8.881   3.075  14.358  1.00  4.94           N  \nATOM     41  CA  ILE A   7       8.912   2.083  13.258  1.00  6.33           C  \nATOM     42  C   ILE A   7       7.581   2.090  12.506  1.00  5.32           C  \nATOM     43  O   ILE A   7       7.670   2.031  11.245  1.00  6.85           O  \nATOM     44  CB  ILE A   7       9.207   0.677  13.924  1.00  8.43           C  \nATOM     45  CG1 ILE A   7      10.714   0.702  14.312  1.00  9.78           C  \nATOM     46  CG2 ILE A   7       8.811  -0.477  12.969  1.00 11.70           C  \nATOM     47  CD1 ILE A   7      11.185  -0.516  15.142  1.00  9.92           C  \nATOM     48  N   VAL A   8       6.458   2.162  13.159  1.00  5.02           N  \nATOM     49  CA  VAL A   8       5.145   2.209  12.453  1.00  6.93           C  \nATOM     50  C   VAL A   8       5.115   3.379  11.461  1.00  5.39           C  \nATOM     51  O   VAL A   8       4.664   3.268  10.343  1.00  6.30           O  \nATOM     52  CB  VAL A   8       3.995   2.354  13.478  1.00  9.64           C  \nATOM     53  CG1 VAL A   8       2.716   2.891  12.869  1.00 13.85           C  \nATOM     54  CG2 VAL A   8       3.758   1.032  14.208  1.00 11.97           C  \nATOM     55  N   ALA A   9       5.606   4.546  11.941  1.00  3.73           N  \nATOM     56  CA  ALA A   9       5.598   5.767  11.082  1.00  3.56           C  \nATOM     57  C   ALA A   9       6.441   5.527   9.850  1.00  4.13           C  \nATOM     58  O   ALA A   9       6.052   5.933   8.744  1.00  4.36           O  \nATOM     59  CB  ALA A   9       6.022   6.977  11.891  1.00  4.80           C  \nATOM     60  N   ARG A  10       7.647   4.909  10.005  1.00  3.73           N  \nATOM     61  CA  ARG A  10       8.496   4.609   8.837  1.00  3.38           C  \nATOM     62  C   ARG A  10       7.798   3.609   7.876  1.00  3.47           C  \nATOM     63  O   ARG A  10       7.878   3.778   6.651  1.00  4.67           O  \nATOM     64  CB  ARG A  10       9.847   4.020   9.305  1.00  3.95           C  \nATOM     65  CG  ARG A  10      10.752   3.607   8.149  1.00  4.55           C  \nATOM     66  CD  ARG A  10      11.226   4.699   7.244  1.00  5.89           C  \nATOM     67  NE  ARG A  10      12.143   5.571   8.035  1.00  6.20           N  \nATOM     68  CZ  ARG A  10      12.758   6.609   7.443  1.00  7.52           C  \nATOM     69  NH1 ARG A  10      12.539   6.932   6.158  1.00 10.68           N  \nATOM     70  NH2 ARG A  10      13.601   7.322   8.202  1.00  9.48           N  \nATOM     71  N   SER A  11       7.186   2.582   8.445  1.00  5.19           N  \nATOM     72  CA  SER A  11       6.500   1.584   7.565  1.00  4.60           C  \nATOM     73  C   SER A  11       5.382   2.313   6.773  1.00  4.84           C  \nATOM     74  O   SER A  11       5.213   2.016   5.557  1.00  5.84           O  \nATOM     75  CB  SER A  11       5.908   0.462   8.400  1.00  5.91           C  \nATOM     76  OG  SER A  11       6.990  -0.272   9.012  1.00  8.38           O  \nATOM     77  N   ASN A  12       4.648   3.182   7.446  1.00  3.54           N  \nATOM     78  CA  ASN A  12       3.545   3.935   6.751  1.00  4.57           C  \nATOM     79  C   ASN A  12       4.107   4.851   5.691  1.00  4.14           C  \nATOM     80  O   ASN A  12       3.536   5.001   4.617  1.00  5.52           O  \nATOM     81  CB  ASN A  12       2.663   4.677   7.748  1.00  6.42           C  \nATOM     82  CG  ASN A  12       1.802   3.735   8.610  1.00  8.25           C  \nATOM     83  OD1 ASN A  12       1.567   2.613   8.165  1.00 12.72           O  \nATOM     84  ND2 ASN A  12       1.394   4.252   9.767  1.00  9.92           N  \nATOM     85  N   PHE A  13       5.259   5.498   6.005  1.00  3.43           N  \nATOM     86  CA  PHE A  13       5.929   6.358   5.055  1.00  3.49           C  \nATOM     87  C   PHE A  13       6.304   5.578   3.799  1.00  3.40           C  \nATOM     88  O   PHE A  13       6.136   6.072   2.653  1.00  4.07           O  \nATOM     89  CB  PHE A  13       7.183   6.994   5.754  1.00  5.48           C  \nATOM     90  CG  PHE A  13       7.884   8.006   4.883  1.00  5.57           C  \nATOM     91  CD1 PHE A  13       8.906   7.586   4.027  1.00  6.99           C  \nATOM     92  CD2 PHE A  13       7.532   9.373   4.983  1.00  6.52           C  \nATOM     93  CE1 PHE A  13       9.560   8.539   3.194  1.00  8.20           C  \nATOM     94  CE2 PHE A  13       8.176  10.281   4.145  1.00  6.34           C  \nATOM     95  CZ  PHE A  13       9.141   9.845   3.292  1.00  6.84           C  \nATOM     96  N   ASN A  14       6.900   4.390   3.989  1.00  3.64           N  \nATOM     97  CA  ASN A  14       7.331   3.607   2.791  1.00  4.31           C  \nATOM     98  C   ASN A  14       6.116   3.210   1.915  1.00  3.98           C  \nATOM     99  O   ASN A  14       6.240   3.144   0.684  1.00  6.22           O  \nATOM    100  CB  ASN A  14       8.145   2.404   3.240  1.00  5.81           C  \nATOM    101  CG  ASN A  14       9.555   2.856   3.730  1.00  6.82           C  \nATOM    102  OD1 ASN A  14      10.013   3.895   3.323  1.00  9.43           O  \nATOM    103  ND2 ASN A  14      10.120   1.956   4.539  1.00  8.21           N  \nATOM    104  N   VAL A  15       4.993   2.927   2.571  1.00  3.76           N  \nATOM    105  CA  VAL A  15       3.782   2.599   1.742  1.00  3.98           C  \nATOM    106  C   VAL A  15       3.296   3.871   1.004  1.00  3.80           C  \nATOM    107  O   VAL A  15       2.947   3.817  -0.189  1.00  4.85           O  \nATOM    108  CB  VAL A  15       2.698   1.953   2.608  1.00  4.71           C  \nATOM    109  CG1 VAL A  15       1.384   1.826   1.806  1.00  6.67           C  \nATOM    110  CG2 VAL A  15       3.174   0.533   3.005  1.00  6.26           C  \nATOM    111  N   CYS A  16       3.321   4.987   1.720  1.00  3.79           N  \nATOM    112  CA  CYS A  16       2.890   6.285   1.126  1.00  3.54           C  \nATOM    113  C   CYS A  16       3.687   6.597  -0.111  1.00  3.48           C  \nATOM    114  O   CYS A  16       3.200   7.147  -1.103  1.00  4.63           O  \nATOM    115  CB  CYS A  16       3.039   7.369   2.240  1.00  4.58           C  \nATOM    116  SG  CYS A  16       2.559   9.014   1.649  1.00  5.66           S  \nATOM    117  N   ARG A  17       4.997   6.227  -0.100  1.00  3.99           N  \nATOM    118  CA  ARG A  17       5.895   6.489  -1.213  1.00  3.83           C  \nATOM    119  C   ARG A  17       5.738   5.560  -2.409  1.00  3.79           C  \nATOM    120  O   ARG A  17       6.228   5.901  -3.507  1.00  5.39           O  \nATOM    121  CB  ARG A  17       7.370   6.507  -0.731  1.00  4.11           C  \nATOM    122  CG  ARG A  17       7.717   7.687   0.206  1.00  4.69           C  \nATOM    123  CD  ARG A  17       7.949   8.947  -0.615  1.00  5.10           C  \nATOM    124  NE  ARG A  17       9.212   8.856  -1.337  1.00  4.71           N  \nATOM    125  CZ  ARG A  17       9.537   9.533  -2.431  1.00  5.28           C  \nATOM    126  NH1 ARG A  17       8.659  10.350  -3.032  1.00  6.67           N  \nATOM    127  NH2 ARG A  17      10.793   9.491  -2.899  1.00  6.41           N  \nATOM    128  N   LEU A  18       5.051   4.411  -2.204  1.00  4.70           N  \nATOM    129  CA  LEU A  18       4.933   3.431  -3.326  1.00  5.46           C  \nATOM    130  C   LEU A  18       4.397   4.014  -4.620  1.00  5.13           C  \nATOM    131  O   LEU A  18       4.988   3.755  -5.687  1.00  5.55           O  \nATOM    132  CB  LEU A  18       4.196   2.184  -2.863  1.00  6.47           C  \nATOM    133  CG  LEU A  18       4.960   1.178  -1.991  1.00  7.43           C  \nATOM    134  CD1 LEU A  18       3.907   0.097  -1.634  1.00  8.70           C  \nATOM    135  CD2 LEU A  18       6.129   0.606  -2.768  1.00  9.39           C  \nATOM    136  N   PRO A  19       3.329   4.795  -4.543  1.00  4.28           N  \nATOM    137  CA  PRO A  19       2.792   5.376  -5.797  1.00  5.38           C  \nATOM    138  C   PRO A  19       3.573   6.540  -6.322  1.00  6.30           C  \nATOM    139  O   PRO A  19       3.260   7.045  -7.422  1.00  9.62           O  \nATOM    140  CB  PRO A  19       1.358   5.766  -5.472  1.00  5.87           C  \nATOM    141  CG  PRO A  19       1.223   5.694  -3.993  1.00  6.47           C  \nATOM    142  CD  PRO A  19       2.421   4.941  -3.408  1.00  6.45           C  \nATOM    143  N   GLY A  20       4.565   7.047  -5.559  1.00  4.94           N  \nATOM    144  CA  GLY A  20       5.366   8.191  -6.018  1.00  5.39           C  \nATOM    145  C   GLY A  20       5.007   9.481  -5.280  1.00  5.03           C  \nATOM    146  O   GLY A  20       5.535  10.510  -5.730  1.00  7.34           O  \nATOM    147  N   THR A  21       4.181   9.438  -4.262  1.00  4.10           N  \nATOM    148  CA  THR A  21       3.767  10.609  -3.513  1.00  3.94           C  \nATOM    149  C   THR A  21       5.017  11.397  -3.042  1.00  3.96           C  \nATOM    150  O   THR A  21       5.947  10.757  -2.523  1.00  5.82           O  \nATOM    151  CB  THR A  21       2.992  10.188  -2.225  1.00  4.13           C  \nATOM    152  OG1 THR A  21       2.051   9.144  -2.623  1.00  5.45           O  \nATOM    153  CG2 THR A  21       2.260  11.349  -1.551  1.00  5.41           C  \nATOM    154  N   PRO A  22       4.971  12.703  -3.176  1.00  5.04           N  \nATOM    155  CA  PRO A  22       6.143  13.513  -2.696  1.00  4.69           C  \nATOM    156  C   PRO A  22       6.400  13.233  -1.225  1.00  4.19           C  \nATOM    157  O   PRO A  22       5.485  13.061  -0.382  1.00  4.47           O  \nATOM    158  CB  PRO A  22       5.703  14.969  -2.920  1.00  7.12           C  \nATOM    159  CG  PRO A  22       4.676  14.893  -3.996  1.00  7.03           C  \nATOM    160  CD  PRO A  22       3.964  13.567  -3.811  1.00  4.90           C  \nATOM    161  N   GLU A  23       7.728  13.297  -0.921  1.00  5.16           N  \nATOM    162  CA  GLU A  23       8.114  13.103   0.500  1.00  5.31           C  \nATOM    163  C   GLU A  23       7.427  14.073   1.410  1.00  4.11           C  \nATOM    164  O   GLU A  23       7.036  13.682   2.540  1.00  5.11           O  \nATOM    165  CB  GLU A  23       9.648  13.285   0.660  1.00  6.16           C  \nATOM    166  CG  GLU A  23      10.440  12.093   0.063  1.00  7.48           C  \nATOM    167  CD  GLU A  23      11.941  12.170   0.391  1.00  9.40           C  \nATOM    168  OE1 GLU A  23      12.416  13.225   0.681  1.00 10.40           O  \nATOM    169  OE2 GLU A  23      12.539  11.070   0.292  1.00 13.32           O  \nATOM    170  N   ALA A  24       7.212  15.334   0.966  1.00  4.56           N  \nATOM    171  CA  ALA A  24       6.614  16.317   1.913  1.00  4.49           C  \nATOM    172  C   ALA A  24       5.212  15.936   2.350  1.00  4.10           C  \nATOM    173  O   ALA A  24       4.782  16.166   3.495  1.00  5.64           O  \nATOM    174  CB  ALA A  24       6.605  17.695   1.246  1.00  5.80           C  \nATOM    175  N   ILE A  25       4.445  15.318   1.405  1.00  4.37           N  \nATOM    176  CA  ILE A  25       3.074  14.894   1.756  1.00  5.44           C  \nATOM    177  C   ILE A  25       3.085  13.643   2.645  1.00  4.32           C  \nATOM    178  O   ILE A  25       2.315  13.523   3.578  1.00  4.72           O  \nATOM    179  CB  ILE A  25       2.204  14.637   0.462  1.00  6.42           C  \nATOM    180  CG1 ILE A  25       1.815  16.048  -0.129  1.00  7.50           C  \nATOM    181  CG2 ILE A  25       0.903  13.864   0.811  1.00  7.65           C  \nATOM    182  CD1 ILE A  25       0.756  16.761   0.757  1.00  7.80           C  \nATOM    183  N   CYS A  26       4.032  12.764   2.313  1.00  3.92           N  \nATOM    184  CA  CYS A  26       4.180  11.549   3.187  1.00  4.37           C  \nATOM    185  C   CYS A  26       4.632  11.944   4.596  1.00  3.95           C  \nATOM    186  O   CYS A  26       4.227  11.252   5.547  1.00  4.74           O  \nATOM    187  CB  CYS A  26       5.038  10.518   2.539  1.00  4.63           C  \nATOM    188  SG  CYS A  26       4.349   9.794   1.022  1.00  5.61           S  \nATOM    189  N   ALA A  27       5.408  13.012   4.694  1.00  3.89           N  \nATOM    190  CA  ALA A  27       5.879  13.502   6.026  1.00  4.43           C  \nATOM    191  C   ALA A  27       4.696  13.908   6.882  1.00  4.26           C  \nATOM    192  O   ALA A  27       4.528  13.422   8.025  1.00  5.44           O  \nATOM    193  CB  ALA A  27       6.880  14.615   5.830  1.00  5.36           C  \nATOM    194  N   THR A  28       3.827  14.802   6.358  1.00  4.53           N  \nATOM    195  CA  THR A  28       2.691  15.221   7.194  1.00  5.08           C  \nATOM    196  C   THR A  28       1.672  14.132   7.434  1.00  4.62           C  \nATOM    197  O   THR A  28       0.947  14.112   8.468  1.00  7.80           O  \nATOM    198  CB  THR A  28       1.986  16.520   6.614  1.00  6.03           C  \nATOM    199  OG1 THR A  28       1.664  16.221   5.230  1.00  7.19           O  \nATOM    200  CG2 THR A  28       2.914  17.739   6.700  1.00  7.34           C  \nATOM    201  N   TYR A  29       1.621  13.190   6.511  1.00  5.01           N  \nATOM    202  CA  TYR A  29       0.715  12.045   6.657  1.00  6.60           C  \nATOM    203  C   TYR A  29       1.125  11.125   7.815  1.00  4.92           C  \nATOM    204  O   TYR A  29       0.286  10.632   8.545  1.00  7.13           O  \nATOM    205  CB  TYR A  29       0.755  11.229   5.322  1.00  9.66           C  \nATOM    206  CG  TYR A  29      -0.203  10.044   5.354  1.00 11.56           C  \nATOM    207  CD1 TYR A  29      -1.547  10.337   5.645  1.00 12.85           C  \nATOM    208  CD2 TYR A  29       0.193   8.750   5.100  1.00 14.44           C  \nATOM    209  CE1 TYR A  29      -2.496   9.329   5.673  1.00 16.61           C  \nATOM    210  CE2 TYR A  29      -0.801   7.705   5.156  1.00 17.11           C  \nATOM    211  CZ  TYR A  29      -2.079   8.031   5.430  1.00 19.99           C  \nATOM    212  OH  TYR A  29      -3.097   7.057   5.458  1.00 28.98           O  \nATOM    213  N   THR A  30       2.470  10.984   7.995  1.00  5.31           N  \nATOM    214  CA  THR A  30       2.986   9.994   8.950  1.00  5.70           C  \nATOM    215  C   THR A  30       3.609  10.505  10.230  1.00  6.28           C  \nATOM    216  O   THR A  30       3.766   9.715  11.186  1.00  8.77           O  \nATOM    217  CB  THR A  30       4.076   9.103   8.225  1.00  6.55           C  \nATOM    218  OG1 THR A  30       5.125  10.027   7.824  1.00  6.57           O  \nATOM    219  CG2 THR A  30       3.493   8.324   7.035  1.00  7.29           C  \nATOM    220  N   GLY A  31       3.984  11.764  10.241  1.00  4.99           N  \nATOM    221  CA  GLY A  31       4.769  12.336  11.360  1.00  5.50           C  \nATOM    222  C   GLY A  31       6.255  12.243  11.106  1.00  4.19           C  \nATOM    223  O   GLY A  31       7.037  12.750  11.954  1.00  6.12           O  \nATOM    224  N   CYS A  32       6.710  11.631   9.992  1.00  4.30           N  \nATOM    225  CA  CYS A  32       8.140  11.694   9.635  1.00  4.89           C  \nATOM    226  C   CYS A  32       8.500  13.141   9.206  1.00  5.50           C  \nATOM    227  O   CYS A  32       7.581  13.949   8.944  1.00  5.82           O  \nATOM    228  CB  CYS A  32       8.504  10.686   8.530  1.00  4.66           C  \nATOM    229  SG  CYS A  32       8.048   8.987   8.881  1.00  5.33           S  \nATOM    230  N   ILE A  33       9.793  13.410   9.173  1.00  6.02           N  \nATOM    231  CA  ILE A  33      10.280  14.760   8.823  1.00  5.24           C  \nATOM    232  C   ILE A  33      11.346  14.658   7.743  1.00  5.16           C  \nATOM    233  O   ILE A  33      11.971  13.583   7.552  1.00  7.19           O  \nATOM    234  CB  ILE A  33      10.790  15.535  10.085  1.00  5.49           C  \nATOM    235  CG1 ILE A  33      12.059  14.803  10.671  1.00  6.85           C  \nATOM    236  CG2 ILE A  33       9.684  15.686  11.138  1.00  6.45           C  \nATOM    237  CD1 ILE A  33      12.733  15.676  11.781  1.00  8.94           C  \nATOM    238  N   ILE A  34      11.490  15.773   7.038  1.00  5.52           N  \nATOM    239  CA  ILE A  34      12.552  15.877   6.036  1.00  6.82           C  \nATOM    240  C   ILE A  34      13.590  16.917   6.560  1.00  6.92           C  \nATOM    241  O   ILE A  34      13.168  18.006   6.945  1.00  9.22           O  \nATOM    242  CB  ILE A  34      11.987  16.360   4.681  1.00  8.11           C  \nATOM    243  CG1 ILE A  34      10.914  15.338   4.163  1.00  9.59           C  \nATOM    244  CG2 ILE A  34      13.131  16.517   3.629  1.00  9.73           C  \nATOM    245  CD1 ILE A  34      10.151  16.024   2.938  1.00 13.41           C  \nATOM    246  N   ILE A  35      14.856  16.493   6.536  1.00  7.06           N  \nATOM    247  CA  ILE A  35      15.930  17.454   6.941  1.00  7.52           C  \nATOM    248  C   ILE A  35      16.913  17.550   5.819  1.00  6.63           C  \nATOM    249  O   ILE A  35      17.097  16.660   4.970  1.00  7.90           O  \nATOM    250  CB  ILE A  35      16.622  16.995   8.285  1.00  8.07           C  \nATOM    251  CG1 ILE A  35      17.360  15.651   8.067  1.00  9.41           C  \nATOM    252  CG2 ILE A  35      15.592  16.974   9.434  1.00  9.46           C  \nATOM    253  CD1 ILE A  35      18.298  15.206   9.219  1.00  9.85           C  \nATOM    254  N   PRO A  36      17.664  18.669   5.806  1.00  8.07           N  \nATOM    255  CA  PRO A  36      18.635  18.861   4.738  1.00  8.78           C  \nATOM    256  C   PRO A  36      19.925  18.042   4.949  1.00  8.31           C  \nATOM    257  O   PRO A  36      20.593  17.742   3.945  1.00  9.09           O  \nATOM    258  CB  PRO A  36      18.945  20.364   4.783  1.00  9.67           C  \nATOM    259  CG  PRO A  36      18.238  20.937   5.908  1.00 10.15           C  \nATOM    260  CD  PRO A  36      17.371  19.900   6.596  1.00  9.53           C  \nATOM    261  N   GLY A  37      20.172  17.730   6.217  1.00  8.48           N  \nATOM    262  CA  GLY A  37      21.452  16.969   6.513  1.00  9.20           C  \nATOM    263  C   GLY A  37      21.143  15.478   6.427  1.00 10.41           C  \nATOM    264  O   GLY A  37      20.138  15.023   5.878  1.00 12.06           O  \nATOM    265  N   ALA A  38      22.055  14.701   7.032  1.00  9.24           N  \nATOM    266  CA  ALA A  38      22.019  13.242   7.020  1.00  9.24           C  \nATOM    267  C   ALA A  38      21.944  12.628   8.396  1.00  9.60           C  \nATOM    268  O   ALA A  38      21.869  11.387   8.435  1.00 13.65           O  \nATOM    269  CB  ALA A  38      23.246  12.697   6.275  1.00 10.43           C  \nATOM    270  N   THR A  39      21.894  13.435   9.436  1.00  8.70           N  \nATOM    271  CA  THR A  39      21.936  12.911  10.809  1.00  9.46           C  \nATOM    272  C   THR A  39      20.615  13.191  11.521  1.00  8.32           C  \nATOM    273  O   THR A  39      20.357  14.317  11.948  1.00  9.89           O  \nATOM    274  CB  THR A  39      23.131  13.601  11.593  1.00 10.72           C  \nATOM    275  OG1 THR A  39      24.284  13.401  10.709  1.00 11.66           O  \nATOM    276  CG2 THR A  39      23.340  12.935  12.962  1.00 11.81           C  \nATOM    277  N   CYS A  40      19.827  12.110  11.642  1.00  7.64           N  \nATOM    278  CA  CYS A  40      18.504  12.312  12.298  1.00  8.05           C  \nATOM    279  C   CYS A  40      18.684  12.451  13.784  1.00  7.63           C  \nATOM    280  O   CYS A  40      19.533  11.718  14.362  1.00  9.64           O  \nATOM    281  CB  CYS A  40      17.582  11.117  11.996  1.00  7.80           C  \nATOM    282  SG  CYS A  40      17.199  10.929  10.237  1.00  7.30           S  \nATOM    283  N   PRO A  41      17.880  13.266  14.426  1.00  8.00           N  \nATOM    284  CA  PRO A  41      17.924  13.421  15.877  1.00  8.96           C  \nATOM    285  C   PRO A  41      17.392  12.206  16.594  1.00  9.06           C  \nATOM    286  O   PRO A  41      16.652  11.368  16.033  1.00  8.82           O  \nATOM    287  CB  PRO A  41      17.076  14.658  16.145  1.00 10.39           C  \nATOM    288  CG  PRO A  41      16.098  14.689  14.997  1.00 10.99           C  \nATOM    289  CD  PRO A  41      16.859  14.150  13.779  1.00 10.49           C  \nATOM    290  N   GLY A  42      17.728  12.124  17.884  1.00  7.55           N  \nATOM    291  CA  GLY A  42      17.334  10.956  18.691  1.00  8.00           C  \nATOM    292  C   GLY A  42      15.875  10.688  18.871  1.00  7.22           C  \nATOM    293  O   GLY A  42      15.434   9.550  19.166  1.00  8.41           O  \nATOM    294  N   ASP A  43      15.036  11.747  18.715  1.00  5.54           N  \nATOM    295  CA  ASP A  43      13.564  11.573  18.836  1.00  5.85           C  \nATOM    296  C   ASP A  43      12.936  11.227  17.470  1.00  5.87           C  \nATOM    297  O   ASP A  43      11.720  11.040  17.428  1.00  7.29           O  \nATOM    298  CB  ASP A  43      12.933  12.737  19.580  1.00  6.72           C  \nATOM    299  CG  ASP A  43      13.140  14.094  18.958  1.00  8.59           C  \nATOM    300  OD1 ASP A  43      14.109  14.303  18.212  1.00  9.59           O  \nATOM    301  OD2 ASP A  43      12.267  14.963  19.265  1.00 11.45           O  \nATOM    302  N   TYR A  44      13.725  11.174  16.425  1.00  5.22           N  \nATOM    303  CA  TYR A  44      13.257  10.745  15.081  1.00  5.56           C  \nATOM    304  C   TYR A  44      14.275   9.687  14.612  1.00  4.61           C  \nATOM    305  O   TYR A  44      14.930   9.862  13.568  1.00  6.04           O  \nATOM    306  CB  TYR A  44      13.200  11.914  14.071  1.00  5.41           C  \nATOM    307  CG  TYR A  44      12.000  12.819  14.399  1.00  5.34           C  \nATOM    308  CD1 TYR A  44      12.119  13.853  15.332  1.00  6.59           C  \nATOM    309  CD2 TYR A  44      10.775  12.617  13.762  1.00  5.94           C  \nATOM    310  CE1 TYR A  44      11.045  14.675  15.610  1.00  5.97           C  \nATOM    311  CE2 TYR A  44       9.676  13.433  14.048  1.00  5.17           C  \nATOM    312  CZ  TYR A  44       9.802  14.456  14.996  1.00  5.96           C  \nATOM    313  OH  TYR A  44       8.740  15.265  15.269  1.00  8.60           O  \nATOM    314  N   ALA A  45      14.342   8.640  15.422  1.00  4.76           N  \nATOM    315  CA  ALA A  45      15.445   7.667  15.246  1.00  5.89           C  \nATOM    316  C   ALA A  45      15.171   6.533  14.280  1.00  6.67           C  \nATOM    317  O   ALA A  45      16.093   5.705  14.039  1.00  7.56           O  \nATOM    318  CB  ALA A  45      15.680   7.099  16.682  1.00  6.82           C  \nATOM    319  N   ASN A  46      13.966   6.502  13.739  1.00  5.80           N  \nATOM    320  CA  ASN A  46      13.512   5.395  12.878  1.00  6.15           C  \nATOM    321  C   ASN A  46      13.311   5.853  11.455  1.00  6.61           C  \nATOM    322  O   ASN A  46      13.733   6.929  11.026  1.00  7.18           O  \nATOM    323  CB  ASN A  46      12.266   4.769  13.501  1.00  7.27           C  \nATOM    324  CG  ASN A  46      12.538   4.304  14.922  1.00  7.98           C  \nATOM    325  OD1 ASN A  46      11.982   4.849  15.886  1.00 11.00           O  \nATOM    326  ND2 ASN A  46      13.407   3.298  15.015  1.00 10.32           N  \nATOM    327  OXT ASN A  46      12.703   4.973  10.746  1.00  7.86           O  \nTER     328      ASN A  46                                                      \nCONECT   20  282                                                                \nCONECT   26  229                                                                \nCONECT  116  188                                                                \nCONECT  188  116                                                                \nCONECT  229   26                                                                \nCONECT  282   20                                                                \nMASTER      227    0    0    2    2    1    0    6  327    1    6    4          \nEND                                                                             \n', 1);
      						    		$.get('http://www.rcsb.org/pdb/files/'+pdb_name+'.pdb', function(data) {			
      						    			var mol = ChemDoodle.readPDB(data);
      						    			pdb.loadMolecule(mol);
      						    			pdb.startAnimation();
      						    		});
      	    				    	}
      	    					}
      	    				}
      	    			});
      	    			
      	    			_this.p3dProtein.add(pan);
      	    		}
    			}
    			else{
    				_this.p3dProtein.setTitle('No proteins found');
    			}


  	    	}
    	});
    	
//    	$.get('http://ws.bioinfo.cipf.es/celldb/rest/v1/hsa/feature/id/brca2/xref?dbname=pdb', 
    	
    	
    	
    	
//    	http://www.rcsb.org/pdb/files/1A17.pdb
    	
//    	http://www.rcsb.org/pdb/files/AAAA.pdb
    	
//		var pan = Ext.create('Ext.panel.Panel',{
//			title:"3D Protein Viewer",
//	        border:false,
//	        cls:'ocb-border-left-lightgrey',
//			flex:3,
//			bodyPadding:5,
//			autoScroll:true,
//			html:'<canvas class="ChemDoodleWebComponent" id="pdb_canvas_prueba" width="600" height="600" style="width: 600px; height: 600px; ">This browser does not support HTML5/Canvas.</canvas>',
//
//		});

    }
    return this.p3dProtein;

};




GeneInfoWidget.prototype.getEnsembleId = function (){

};


GeneInfoWidget.prototype.getData = function (){
	var _this = this;
	this.panel.disable();
	this.panel.setLoading("Getting information...");
//	category, subcategory, query, resource, callbackFunction
	CellBaseManager.get({
        species:this.species,
        category:'feature',
        subCategory:'gene',
        query:this.query,
        resource:"info",
        success:function(data){
            _this.dataReceived(data.response[0].result[0]);
        }
    });
};
GeneInfoWidget.prototype.dataReceived = function (data){
	this.data=data;
	console.log(this.data);
	this.optionClick({"text":"Information","leaf":"true"});
	this.panel.enable();
	this.panel.setLoading(false);
};

GeneOrangeInfoWidget.prototype.draw = InfoWidget.prototype.draw;
GeneOrangeInfoWidget.prototype.render = InfoWidget.prototype.render;
GeneOrangeInfoWidget.prototype.getTreePanel = InfoWidget.prototype.getTreePanel;
GeneOrangeInfoWidget.prototype.checkDataTypes = InfoWidget.prototype.checkDataTypes;
GeneOrangeInfoWidget.prototype.doGrid = InfoWidget.prototype.doGrid;
GeneOrangeInfoWidget.prototype.getGeneTemplate = InfoWidget.prototype.getGeneTemplate;
GeneOrangeInfoWidget.prototype.getTranscriptTemplate = InfoWidget.prototype.getTranscriptTemplate;

function GeneOrangeInfoWidget(targetId, species, args){
	if (args == null){
		args = new Object();
	}
	args.title = "Gene Info";
	InfoWidget.prototype.constructor.call(this, targetId, species, args);
};

GeneOrangeInfoWidget.prototype.getdataTypes = function (){
	//Abstract method
	return dataTypes=[
	            { text: "Genomic", children: [
	                { text: "Information"},
	                { text: "Transcripts"}
	            ] },
	            { text: "Functional information", children: [
	                { text: "GO"},
	                { text: "KEGG"},
	                { text: "Interpro"}
	            ] }
	        ];
};

GeneOrangeInfoWidget.prototype.optionClick = function (item){
	//Abstract method
	if (item.leaf){
		if(this.panel.getComponent(1)!=null){
			this.panel.getComponent(1).hide();
			this.panel.remove(1,false);
		}
		switch (item.text){
			case "Information": this.panel.add(this.getGenePanel(this.data).show()); break;
			case "Transcripts": this.panel.add(this.getTranscriptPanel(this.data.transcripts).show());  break;
//			case "GO": this.panel.add(this.getGoGrid().show()); break;
			case "GO": this.panel.add(this.getXrefGrid(this.data.go, "GO").show());  break;
			case "Interpro": this.panel.add(this.getXrefGrid(this.data.interpro, "Interpro").show());  break;
			case "KEGG": this.panel.add(this.getXrefGrid(this.data.kegg, "KEGG").show());  break;
		}
	}
};

GeneOrangeInfoWidget.prototype.getGenePanel = function(data){
	if(data==null){
		return this.notFoundPanel;
	}
    if(this.genePanel==null){
    	var tpl = this.getGeneTemplate();
    	
		this.genePanel = Ext.create('Ext.panel.Panel',{
			title:"Gene information",
	        border:false,
	        cls:'panel-border-left',
			flex:3,
			bodyPadding:10,
			data:data,
			tpl:tpl
		});
    }
    return this.genePanel;
};


GeneOrangeInfoWidget.prototype.getTranscriptPanel = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.transcriptGrid==null){
    	
    	var tpl = this.getTranscriptTemplate();
    	
    	var panels = [];
    	for ( var i = 0; i < data.length; i++) {	
			var transcriptPanel = Ext.create('Ext.container.Container',{
				padding:5,
				data:data[i],
				tpl:tpl
			});
			panels.push(transcriptPanel);
    	}
		this.transcriptGrid = Ext.create('Ext.panel.Panel',{
			title:"Transcripts ("+i+")",
			border:false,
			cls:'panel-border-left',
			flex:3,    
			bodyPadding:5,
			autoScroll:true,
			items:panels
		});
    }
    return this.transcriptGrid;
};


GeneOrangeInfoWidget.prototype.getXrefGrid = function(data, dbname){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this[dbname+"Grid"]==null){
    	var groupField = '';
    	var modelName = dbname;
    	var fields = ['description','displayId'];
    	var columns = [
    	               {header : 'Display Id',dataIndex: 'displayId',flex:1},
    	               {header : 'Description',dataIndex: 'description',flex:3}
    	               ];
    	this[dbname+"Grid"] = this.doGrid(columns,fields,modelName,groupField);
    	this[dbname+"Grid"].store.loadData(data);
    }
    return this[dbname+"Grid"];
};

//GeneOrangeInfoWidget.prototype.getGoGrid = function(){
//    var _this = this;
//    if(this.goGrid==null){
//    	var groupField = 'namespace';
//    	var modelName = 'GO';
//	    var fields = ['id','name','description','level','directNumberOfGenes','namespace','parents','propagatedNumberOfGenes','score'];
//		var columns = [ {header : 'Database id',dataIndex: 'id',flex:2},
//						{header : 'Name',dataIndex: 'name',flex:1},
//						{header : 'Description',dataIndex: 'description',flex:2},
//		                {
//		                	xtype: 'actioncolumn',
//		                	header : '+info',
//		                    flex:1,
//		                    items: [{
//		                        iconCls: 'icon-blue-box',  // Use a URL in the icon config
//		                        tooltip: '+info',    
//		                        handler: function(grid, rowIndex, colIndex) {
//		                            var rec = _this.goStore.getAt(rowIndex);
//		                            Ext.Msg.alert(rec.get('name'), rec.get('description'));
//		                        }
//		                    }]
//		                 },
//		                {header : 'Direct genes',dataIndex: 'directNumberOfGenes',flex:2},
//						{header : 'Level',dataIndex: 'level',flex:1},
//						{header : 'Namespace',dataIndex: 'namespace',flex:2},
//						{header : 'Propagated genes',dataIndex: 'propagatedNumberOfGenes',flex:2.5}
//		             ];
//		this.goGrid = this.doGrid(columns,fields,modelName,groupField);
//		
//    }
//    return this.goGrid;
//};


GeneOrangeInfoWidget.prototype.getTfbsGrid = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.tfbsGrid==null){
    	var groupField = "";
    	var modelName = "TFBS";
	    var fields = ["chromosome","start","end","strand","tfName","relativeStart","relativeEnd","targetGeneName","score","sequence"];
		var columns = [
		                {header : 'Name',dataIndex: 'tfName',flex:1},
		            	{header : 'Location: chr:start-end (strand)', xtype:'templatecolumn', tpl:'{chromosome}:{start}-{end} ({strand})',flex:2.5},
		            	{header : 'Relative (start-end)',xtype:'templatecolumn',tpl:'{relativeStart}-{relativeEnd}',flex:1.5},
						{header : 'Target gene',dataIndex: 'targetGeneName',flex:1},
						{header : 'Score',dataIndex: 'score',flex:1},
						{header : 'Sequence',dataIndex: 'sequence',flex:1}
		             ];
		this.tfbsGrid = this.doGrid(columns,fields,modelName,groupField);
		this.tfbsGrid.store.loadData(data);
    }
    return this.tfbsGrid;
};

GeneOrangeInfoWidget.prototype.getMirnaTargetGrid = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.mirnaTargetGrid==null){
    	var groupField = "";
    	var modelName = "miRNA targets";
	    var fields = ["chromosome","start","end","strand","mirbaseId","score","experimentalMethod","source"];
		var columns = [
		                {header : 'Id',dataIndex: 'mirbaseId',flex:1},
		            	{header : 'Location: chr:start-end (strand)', xtype:'templatecolumn', tpl:'{chromosome}:{start}-{end} ({strand})',flex:2},
						{header : 'Score',dataIndex: 'score',flex:1},
						{header : 'Exp. Method',dataIndex: 'experimentalMethod',flex:1},
						{header : 'source',dataIndex: 'source',flex:1}
		             ];
		this.mirnaTargetGrid = this.doGrid(columns,fields,modelName,groupField);
		this.mirnaTargetGrid.store.loadData(data);
    }
    return this.mirnaTargetGrid;
};

GeneOrangeInfoWidget.prototype.getProteinFeaturesGrid = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.proteinFeaturesGrid==null){
    	var groupField = '';
    	var modelName = "Protein features";
	    var fields = ["identifier","start","end","original","type","description"];
		var columns = [
		                {header : 'Identifier',dataIndex: 'identifier',flex:1},
		               	{header : 'Location: (start-end)', xtype:'templatecolumn', tpl:'{start}-{end}',flex:1.2},
		               	{header : 'Original',dataIndex: 'original',flex:1},
						{header : 'Type',dataIndex: 'type',flex:1},
						{header : 'Description',dataIndex: 'description',flex:1.5}
		             ];
		this.proteinFeaturesGrid = this.doGrid(columns,fields,modelName,groupField);
		this.proteinFeaturesGrid.store.loadData(data);
    }
    return this.proteinFeaturesGrid;
};


GeneOrangeInfoWidget.prototype.getProteinFeaturesGrid = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.proteinFeaturesGrid==null){
    	var groupField = '';
    	var modelName = 'Protein features';
	    var fields = ["identifier","start","end","original","type","description"];
		var columns = [
		                {header : 'Identifier',dataIndex: 'identifier',flex:1},
		               	{header : 'Location: (start-end)', xtype:'templatecolumn', tpl:'{start}-{end}',flex:1.2},
		               	{header : 'Original',dataIndex: 'original',flex:1},
						{header : 'Type',dataIndex: 'type',flex:1},
						{header : 'Description',dataIndex: 'description',flex:1.5}
		             ];
		this.proteinFeaturesGrid = this.doGrid(columns,fields,modelName,groupField);
		this.proteinFeaturesGrid.store.loadData(data);
    }
    return this.proteinFeaturesGrid;
};

GeneOrangeInfoWidget.prototype.get3Dprotein = function(data){
	var _this=this;
    if(this.p3dProtein==null){
    	//ws
//    	
      	this.p3dProtein = Ext.create('Ext.tab.Panel',{
      		title:"3D Protein Viewer",
      		border:false,
      		cls:'panel-border-left',
      		flex:3,
//    		bodyPadding:5,
      		autoScroll:true
//      		items:items
      	});
    	
//		$.get('http://ws.bioinfo.cipf.es/celldb/rest/v1/hsa/feature/id/'+_this.feature.feature.stableId+'/xref?dbname=pdb', function(data){
    
    	var pdbs = [];
    	$.ajax({
//    		  url: 'http://ws.bioinfo.cipf.es/celldb/rest/v1/hsa/feature/id/brca2/xref?dbname=pdb',
    		  url: 'http://ws.bioinfo.cipf.es/cellbase/rest/v1/hsa/feature/id/'+this.query+'/xref?dbname=pdb',
//    		  data: data,
//    		  dataType: dataType,
    		  async:false,
    		  success: function(data){
    			if(data!=""){
//      	    		console.log(data.trim());
      	    		pdbs = data.trim().split("\n");
//      	    		console.log(pdbs);
      	    		
      	    		for ( var i = 0; i < pdbs.length; i++) {
      	    			var pdb_name=pdbs[i].trim();
      	    			var pan = Ext.create('Ext.panel.Panel',{
      	    				title:pdb_name,
      	    				bodyCls:'background-black',
      	    				html:'<canvas class="ChemDoodleWebComponent" id="pdb_canvas_'+pdb_name+'" width="600" height="600" style="width: 600px; height: 600px; ">This browser does not support HTML5/Canvas.</canvas>',
      	    				listeners:{
      	    					afterrender:function(este){
      	    						// JavaScript Document
      	    						var pdb_name=este.title;
      	    						
      	    				    	ChemDoodle.default_backgroundColor = '#000000';
      	    				    	
      	    				    	var pdb = new ChemDoodle.TransformCanvas3D('pdb_canvas_'+pdb_name, 300, 300);
      	    				    	if(!pdb.gl){
      	    				    	  pdb.emptyMessage = 'Your browser does not support WebGL';
      	    				    	  pdb.displayMessage();
      	    				    	}else{
      	    					    	pdb.specs.set3DRepresentation('Ball and Stick');
      	    					    	pdb.specs.proteins_ribbonCartoonize = true;
      	    					    	pdb.handle = null;
      	    					    	pdb.timeout = 15;
      	    					    	pdb.startAnimation = ChemDoodle._AnimatorCanvas.prototype.startAnimation;
      	    					    	pdb.stopAnimation = ChemDoodle._AnimatorCanvas.prototype.stopAnimation;
      	    					    	pdb.isRunning = ChemDoodle._AnimatorCanvas.prototype.isRunning;
      	    					    	pdb.dblclick = ChemDoodle.RotatorCanvas.prototype.dblclick;
      	    					    	pdb.nextFrame = function(delta){
      	    					    		var matrix = [];
      	    					    		mat4.identity(matrix);
      	    					    		var change = delta/1000;
      	    					    	        var increment = Math.PI/15;
      	    					    		mat4.rotate(matrix, increment*change, [ 1, 0, 0 ]);
      	    					    		mat4.rotate(matrix, increment*change, [ 0, 1, 0 ]);
      	    					    		mat4.rotate(matrix, increment*change, [ 0, 0, 1 ]);
      	    					    		mat4.multiply(this.rotationMatrix, matrix);
      	    					    	};
      	    					    	
//      	    					    	http://ws.bioinfo.cipf.es/celldb/rest/v1/hsa/feature/id/brca2/xref?dbname=pdb
//      	    				    	var mol = ChemDoodle.readPDB('HEADER    PLANT SEED PROTEIN                      30-APR-81   1CRN                                                                       \nDBREF  1CRN A    1    46  UNP    P01542   CRAM_CRAAB       1     46             \nSEQRES   1 A   46  THR THR CYS CYS PRO SER ILE VAL ALA ARG SER ASN PHE          \nSEQRES   2 A   46  ASN VAL CYS ARG LEU PRO GLY THR PRO GLU ALA ILE CYS          \nSEQRES   3 A   46  ALA THR TYR THR GLY CYS ILE ILE ILE PRO GLY ALA THR          \nSEQRES   4 A   46  CYS PRO GLY ASP TYR ALA ASN                                  \nHELIX    1  H1 ILE A    7  PRO A   19  13/10 CONFORMATION RES 17,19       13    \nHELIX    2  H2 GLU A   23  THR A   30  1DISTORTED 3/10 AT RES 30           8    \nSHEET    1  S1 2 THR A   1  CYS A   4  0                                        \nSHEET    2  S1 2 CYS A  32  ILE A  35 -1                                        \nSSBOND   1 CYS A    3    CYS A   40                          1555   1555  2.00  \nSSBOND   2 CYS A    4    CYS A   32                          1555   1555  2.04  \nSSBOND   3 CYS A   16    CYS A   26                          1555   1555  2.05  \nCRYST1   40.960   18.650   22.520  90.00  90.77  90.00 P 1 21 1      2          \nORIGX1      1.000000  0.000000  0.000000        0.00000                         \nORIGX2      0.000000  1.000000  0.000000        0.00000                         \nORIGX3      0.000000  0.000000  1.000000        0.00000                         \nSCALE1      0.024414  0.000000 -0.000328        0.00000                         \nSCALE2      0.000000  0.053619  0.000000        0.00000                         \nSCALE3      0.000000  0.000000  0.044409        0.00000                         \nATOM      1  N   THR A   1      17.047  14.099   3.625  1.00 13.79           N  \nATOM      2  CA  THR A   1      16.967  12.784   4.338  1.00 10.80           C  \nATOM      3  C   THR A   1      15.685  12.755   5.133  1.00  9.19           C  \nATOM      4  O   THR A   1      15.268  13.825   5.594  1.00  9.85           O  \nATOM      5  CB  THR A   1      18.170  12.703   5.337  1.00 13.02           C  \nATOM      6  OG1 THR A   1      19.334  12.829   4.463  1.00 15.06           O  \nATOM      7  CG2 THR A   1      18.150  11.546   6.304  1.00 14.23           C  \nATOM      8  N   THR A   2      15.115  11.555   5.265  1.00  7.81           N  \nATOM      9  CA  THR A   2      13.856  11.469   6.066  1.00  8.31           C  \nATOM     10  C   THR A   2      14.164  10.785   7.379  1.00  5.80           C  \nATOM     11  O   THR A   2      14.993   9.862   7.443  1.00  6.94           O  \nATOM     12  CB  THR A   2      12.732  10.711   5.261  1.00 10.32           C  \nATOM     13  OG1 THR A   2      13.308   9.439   4.926  1.00 12.81           O  \nATOM     14  CG2 THR A   2      12.484  11.442   3.895  1.00 11.90           C  \nATOM     15  N   CYS A   3      13.488  11.241   8.417  1.00  5.24           N  \nATOM     16  CA  CYS A   3      13.660  10.707   9.787  1.00  5.39           C  \nATOM     17  C   CYS A   3      12.269  10.431  10.323  1.00  4.45           C  \nATOM     18  O   CYS A   3      11.393  11.308  10.185  1.00  6.54           O  \nATOM     19  CB  CYS A   3      14.368  11.748  10.691  1.00  5.99           C  \nATOM     20  SG  CYS A   3      15.885  12.426  10.016  1.00  7.01           S  \nATOM     21  N   CYS A   4      12.019   9.272  10.928  1.00  3.90           N  \nATOM     22  CA  CYS A   4      10.646   8.991  11.408  1.00  4.24           C  \nATOM     23  C   CYS A   4      10.654   8.793  12.919  1.00  3.72           C  \nATOM     24  O   CYS A   4      11.659   8.296  13.491  1.00  5.30           O  \nATOM     25  CB  CYS A   4      10.057   7.752  10.682  1.00  4.41           C  \nATOM     26  SG  CYS A   4       9.837   8.018   8.904  1.00  4.72           S  \nATOM     27  N   PRO A   5       9.561   9.108  13.563  1.00  3.96           N  \nATOM     28  CA  PRO A   5       9.448   9.034  15.012  1.00  4.25           C  \nATOM     29  C   PRO A   5       9.288   7.670  15.606  1.00  4.96           C  \nATOM     30  O   PRO A   5       9.490   7.519  16.819  1.00  7.44           O  \nATOM     31  CB  PRO A   5       8.230   9.957  15.345  1.00  5.11           C  \nATOM     32  CG  PRO A   5       7.338   9.786  14.114  1.00  5.24           C  \nATOM     33  CD  PRO A   5       8.366   9.804  12.958  1.00  5.20           C  \nATOM     34  N   SER A   6       8.875   6.686  14.796  1.00  4.83           N  \nATOM     35  CA  SER A   6       8.673   5.314  15.279  1.00  4.45           C  \nATOM     36  C   SER A   6       8.753   4.376  14.083  1.00  4.99           C  \nATOM     37  O   SER A   6       8.726   4.858  12.923  1.00  4.61           O  \nATOM     38  CB  SER A   6       7.340   5.121  15.996  1.00  5.05           C  \nATOM     39  OG  SER A   6       6.274   5.220  15.031  1.00  6.39           O  \nATOM     40  N   ILE A   7       8.881   3.075  14.358  1.00  4.94           N  \nATOM     41  CA  ILE A   7       8.912   2.083  13.258  1.00  6.33           C  \nATOM     42  C   ILE A   7       7.581   2.090  12.506  1.00  5.32           C  \nATOM     43  O   ILE A   7       7.670   2.031  11.245  1.00  6.85           O  \nATOM     44  CB  ILE A   7       9.207   0.677  13.924  1.00  8.43           C  \nATOM     45  CG1 ILE A   7      10.714   0.702  14.312  1.00  9.78           C  \nATOM     46  CG2 ILE A   7       8.811  -0.477  12.969  1.00 11.70           C  \nATOM     47  CD1 ILE A   7      11.185  -0.516  15.142  1.00  9.92           C  \nATOM     48  N   VAL A   8       6.458   2.162  13.159  1.00  5.02           N  \nATOM     49  CA  VAL A   8       5.145   2.209  12.453  1.00  6.93           C  \nATOM     50  C   VAL A   8       5.115   3.379  11.461  1.00  5.39           C  \nATOM     51  O   VAL A   8       4.664   3.268  10.343  1.00  6.30           O  \nATOM     52  CB  VAL A   8       3.995   2.354  13.478  1.00  9.64           C  \nATOM     53  CG1 VAL A   8       2.716   2.891  12.869  1.00 13.85           C  \nATOM     54  CG2 VAL A   8       3.758   1.032  14.208  1.00 11.97           C  \nATOM     55  N   ALA A   9       5.606   4.546  11.941  1.00  3.73           N  \nATOM     56  CA  ALA A   9       5.598   5.767  11.082  1.00  3.56           C  \nATOM     57  C   ALA A   9       6.441   5.527   9.850  1.00  4.13           C  \nATOM     58  O   ALA A   9       6.052   5.933   8.744  1.00  4.36           O  \nATOM     59  CB  ALA A   9       6.022   6.977  11.891  1.00  4.80           C  \nATOM     60  N   ARG A  10       7.647   4.909  10.005  1.00  3.73           N  \nATOM     61  CA  ARG A  10       8.496   4.609   8.837  1.00  3.38           C  \nATOM     62  C   ARG A  10       7.798   3.609   7.876  1.00  3.47           C  \nATOM     63  O   ARG A  10       7.878   3.778   6.651  1.00  4.67           O  \nATOM     64  CB  ARG A  10       9.847   4.020   9.305  1.00  3.95           C  \nATOM     65  CG  ARG A  10      10.752   3.607   8.149  1.00  4.55           C  \nATOM     66  CD  ARG A  10      11.226   4.699   7.244  1.00  5.89           C  \nATOM     67  NE  ARG A  10      12.143   5.571   8.035  1.00  6.20           N  \nATOM     68  CZ  ARG A  10      12.758   6.609   7.443  1.00  7.52           C  \nATOM     69  NH1 ARG A  10      12.539   6.932   6.158  1.00 10.68           N  \nATOM     70  NH2 ARG A  10      13.601   7.322   8.202  1.00  9.48           N  \nATOM     71  N   SER A  11       7.186   2.582   8.445  1.00  5.19           N  \nATOM     72  CA  SER A  11       6.500   1.584   7.565  1.00  4.60           C  \nATOM     73  C   SER A  11       5.382   2.313   6.773  1.00  4.84           C  \nATOM     74  O   SER A  11       5.213   2.016   5.557  1.00  5.84           O  \nATOM     75  CB  SER A  11       5.908   0.462   8.400  1.00  5.91           C  \nATOM     76  OG  SER A  11       6.990  -0.272   9.012  1.00  8.38           O  \nATOM     77  N   ASN A  12       4.648   3.182   7.446  1.00  3.54           N  \nATOM     78  CA  ASN A  12       3.545   3.935   6.751  1.00  4.57           C  \nATOM     79  C   ASN A  12       4.107   4.851   5.691  1.00  4.14           C  \nATOM     80  O   ASN A  12       3.536   5.001   4.617  1.00  5.52           O  \nATOM     81  CB  ASN A  12       2.663   4.677   7.748  1.00  6.42           C  \nATOM     82  CG  ASN A  12       1.802   3.735   8.610  1.00  8.25           C  \nATOM     83  OD1 ASN A  12       1.567   2.613   8.165  1.00 12.72           O  \nATOM     84  ND2 ASN A  12       1.394   4.252   9.767  1.00  9.92           N  \nATOM     85  N   PHE A  13       5.259   5.498   6.005  1.00  3.43           N  \nATOM     86  CA  PHE A  13       5.929   6.358   5.055  1.00  3.49           C  \nATOM     87  C   PHE A  13       6.304   5.578   3.799  1.00  3.40           C  \nATOM     88  O   PHE A  13       6.136   6.072   2.653  1.00  4.07           O  \nATOM     89  CB  PHE A  13       7.183   6.994   5.754  1.00  5.48           C  \nATOM     90  CG  PHE A  13       7.884   8.006   4.883  1.00  5.57           C  \nATOM     91  CD1 PHE A  13       8.906   7.586   4.027  1.00  6.99           C  \nATOM     92  CD2 PHE A  13       7.532   9.373   4.983  1.00  6.52           C  \nATOM     93  CE1 PHE A  13       9.560   8.539   3.194  1.00  8.20           C  \nATOM     94  CE2 PHE A  13       8.176  10.281   4.145  1.00  6.34           C  \nATOM     95  CZ  PHE A  13       9.141   9.845   3.292  1.00  6.84           C  \nATOM     96  N   ASN A  14       6.900   4.390   3.989  1.00  3.64           N  \nATOM     97  CA  ASN A  14       7.331   3.607   2.791  1.00  4.31           C  \nATOM     98  C   ASN A  14       6.116   3.210   1.915  1.00  3.98           C  \nATOM     99  O   ASN A  14       6.240   3.144   0.684  1.00  6.22           O  \nATOM    100  CB  ASN A  14       8.145   2.404   3.240  1.00  5.81           C  \nATOM    101  CG  ASN A  14       9.555   2.856   3.730  1.00  6.82           C  \nATOM    102  OD1 ASN A  14      10.013   3.895   3.323  1.00  9.43           O  \nATOM    103  ND2 ASN A  14      10.120   1.956   4.539  1.00  8.21           N  \nATOM    104  N   VAL A  15       4.993   2.927   2.571  1.00  3.76           N  \nATOM    105  CA  VAL A  15       3.782   2.599   1.742  1.00  3.98           C  \nATOM    106  C   VAL A  15       3.296   3.871   1.004  1.00  3.80           C  \nATOM    107  O   VAL A  15       2.947   3.817  -0.189  1.00  4.85           O  \nATOM    108  CB  VAL A  15       2.698   1.953   2.608  1.00  4.71           C  \nATOM    109  CG1 VAL A  15       1.384   1.826   1.806  1.00  6.67           C  \nATOM    110  CG2 VAL A  15       3.174   0.533   3.005  1.00  6.26           C  \nATOM    111  N   CYS A  16       3.321   4.987   1.720  1.00  3.79           N  \nATOM    112  CA  CYS A  16       2.890   6.285   1.126  1.00  3.54           C  \nATOM    113  C   CYS A  16       3.687   6.597  -0.111  1.00  3.48           C  \nATOM    114  O   CYS A  16       3.200   7.147  -1.103  1.00  4.63           O  \nATOM    115  CB  CYS A  16       3.039   7.369   2.240  1.00  4.58           C  \nATOM    116  SG  CYS A  16       2.559   9.014   1.649  1.00  5.66           S  \nATOM    117  N   ARG A  17       4.997   6.227  -0.100  1.00  3.99           N  \nATOM    118  CA  ARG A  17       5.895   6.489  -1.213  1.00  3.83           C  \nATOM    119  C   ARG A  17       5.738   5.560  -2.409  1.00  3.79           C  \nATOM    120  O   ARG A  17       6.228   5.901  -3.507  1.00  5.39           O  \nATOM    121  CB  ARG A  17       7.370   6.507  -0.731  1.00  4.11           C  \nATOM    122  CG  ARG A  17       7.717   7.687   0.206  1.00  4.69           C  \nATOM    123  CD  ARG A  17       7.949   8.947  -0.615  1.00  5.10           C  \nATOM    124  NE  ARG A  17       9.212   8.856  -1.337  1.00  4.71           N  \nATOM    125  CZ  ARG A  17       9.537   9.533  -2.431  1.00  5.28           C  \nATOM    126  NH1 ARG A  17       8.659  10.350  -3.032  1.00  6.67           N  \nATOM    127  NH2 ARG A  17      10.793   9.491  -2.899  1.00  6.41           N  \nATOM    128  N   LEU A  18       5.051   4.411  -2.204  1.00  4.70           N  \nATOM    129  CA  LEU A  18       4.933   3.431  -3.326  1.00  5.46           C  \nATOM    130  C   LEU A  18       4.397   4.014  -4.620  1.00  5.13           C  \nATOM    131  O   LEU A  18       4.988   3.755  -5.687  1.00  5.55           O  \nATOM    132  CB  LEU A  18       4.196   2.184  -2.863  1.00  6.47           C  \nATOM    133  CG  LEU A  18       4.960   1.178  -1.991  1.00  7.43           C  \nATOM    134  CD1 LEU A  18       3.907   0.097  -1.634  1.00  8.70           C  \nATOM    135  CD2 LEU A  18       6.129   0.606  -2.768  1.00  9.39           C  \nATOM    136  N   PRO A  19       3.329   4.795  -4.543  1.00  4.28           N  \nATOM    137  CA  PRO A  19       2.792   5.376  -5.797  1.00  5.38           C  \nATOM    138  C   PRO A  19       3.573   6.540  -6.322  1.00  6.30           C  \nATOM    139  O   PRO A  19       3.260   7.045  -7.422  1.00  9.62           O  \nATOM    140  CB  PRO A  19       1.358   5.766  -5.472  1.00  5.87           C  \nATOM    141  CG  PRO A  19       1.223   5.694  -3.993  1.00  6.47           C  \nATOM    142  CD  PRO A  19       2.421   4.941  -3.408  1.00  6.45           C  \nATOM    143  N   GLY A  20       4.565   7.047  -5.559  1.00  4.94           N  \nATOM    144  CA  GLY A  20       5.366   8.191  -6.018  1.00  5.39           C  \nATOM    145  C   GLY A  20       5.007   9.481  -5.280  1.00  5.03           C  \nATOM    146  O   GLY A  20       5.535  10.510  -5.730  1.00  7.34           O  \nATOM    147  N   THR A  21       4.181   9.438  -4.262  1.00  4.10           N  \nATOM    148  CA  THR A  21       3.767  10.609  -3.513  1.00  3.94           C  \nATOM    149  C   THR A  21       5.017  11.397  -3.042  1.00  3.96           C  \nATOM    150  O   THR A  21       5.947  10.757  -2.523  1.00  5.82           O  \nATOM    151  CB  THR A  21       2.992  10.188  -2.225  1.00  4.13           C  \nATOM    152  OG1 THR A  21       2.051   9.144  -2.623  1.00  5.45           O  \nATOM    153  CG2 THR A  21       2.260  11.349  -1.551  1.00  5.41           C  \nATOM    154  N   PRO A  22       4.971  12.703  -3.176  1.00  5.04           N  \nATOM    155  CA  PRO A  22       6.143  13.513  -2.696  1.00  4.69           C  \nATOM    156  C   PRO A  22       6.400  13.233  -1.225  1.00  4.19           C  \nATOM    157  O   PRO A  22       5.485  13.061  -0.382  1.00  4.47           O  \nATOM    158  CB  PRO A  22       5.703  14.969  -2.920  1.00  7.12           C  \nATOM    159  CG  PRO A  22       4.676  14.893  -3.996  1.00  7.03           C  \nATOM    160  CD  PRO A  22       3.964  13.567  -3.811  1.00  4.90           C  \nATOM    161  N   GLU A  23       7.728  13.297  -0.921  1.00  5.16           N  \nATOM    162  CA  GLU A  23       8.114  13.103   0.500  1.00  5.31           C  \nATOM    163  C   GLU A  23       7.427  14.073   1.410  1.00  4.11           C  \nATOM    164  O   GLU A  23       7.036  13.682   2.540  1.00  5.11           O  \nATOM    165  CB  GLU A  23       9.648  13.285   0.660  1.00  6.16           C  \nATOM    166  CG  GLU A  23      10.440  12.093   0.063  1.00  7.48           C  \nATOM    167  CD  GLU A  23      11.941  12.170   0.391  1.00  9.40           C  \nATOM    168  OE1 GLU A  23      12.416  13.225   0.681  1.00 10.40           O  \nATOM    169  OE2 GLU A  23      12.539  11.070   0.292  1.00 13.32           O  \nATOM    170  N   ALA A  24       7.212  15.334   0.966  1.00  4.56           N  \nATOM    171  CA  ALA A  24       6.614  16.317   1.913  1.00  4.49           C  \nATOM    172  C   ALA A  24       5.212  15.936   2.350  1.00  4.10           C  \nATOM    173  O   ALA A  24       4.782  16.166   3.495  1.00  5.64           O  \nATOM    174  CB  ALA A  24       6.605  17.695   1.246  1.00  5.80           C  \nATOM    175  N   ILE A  25       4.445  15.318   1.405  1.00  4.37           N  \nATOM    176  CA  ILE A  25       3.074  14.894   1.756  1.00  5.44           C  \nATOM    177  C   ILE A  25       3.085  13.643   2.645  1.00  4.32           C  \nATOM    178  O   ILE A  25       2.315  13.523   3.578  1.00  4.72           O  \nATOM    179  CB  ILE A  25       2.204  14.637   0.462  1.00  6.42           C  \nATOM    180  CG1 ILE A  25       1.815  16.048  -0.129  1.00  7.50           C  \nATOM    181  CG2 ILE A  25       0.903  13.864   0.811  1.00  7.65           C  \nATOM    182  CD1 ILE A  25       0.756  16.761   0.757  1.00  7.80           C  \nATOM    183  N   CYS A  26       4.032  12.764   2.313  1.00  3.92           N  \nATOM    184  CA  CYS A  26       4.180  11.549   3.187  1.00  4.37           C  \nATOM    185  C   CYS A  26       4.632  11.944   4.596  1.00  3.95           C  \nATOM    186  O   CYS A  26       4.227  11.252   5.547  1.00  4.74           O  \nATOM    187  CB  CYS A  26       5.038  10.518   2.539  1.00  4.63           C  \nATOM    188  SG  CYS A  26       4.349   9.794   1.022  1.00  5.61           S  \nATOM    189  N   ALA A  27       5.408  13.012   4.694  1.00  3.89           N  \nATOM    190  CA  ALA A  27       5.879  13.502   6.026  1.00  4.43           C  \nATOM    191  C   ALA A  27       4.696  13.908   6.882  1.00  4.26           C  \nATOM    192  O   ALA A  27       4.528  13.422   8.025  1.00  5.44           O  \nATOM    193  CB  ALA A  27       6.880  14.615   5.830  1.00  5.36           C  \nATOM    194  N   THR A  28       3.827  14.802   6.358  1.00  4.53           N  \nATOM    195  CA  THR A  28       2.691  15.221   7.194  1.00  5.08           C  \nATOM    196  C   THR A  28       1.672  14.132   7.434  1.00  4.62           C  \nATOM    197  O   THR A  28       0.947  14.112   8.468  1.00  7.80           O  \nATOM    198  CB  THR A  28       1.986  16.520   6.614  1.00  6.03           C  \nATOM    199  OG1 THR A  28       1.664  16.221   5.230  1.00  7.19           O  \nATOM    200  CG2 THR A  28       2.914  17.739   6.700  1.00  7.34           C  \nATOM    201  N   TYR A  29       1.621  13.190   6.511  1.00  5.01           N  \nATOM    202  CA  TYR A  29       0.715  12.045   6.657  1.00  6.60           C  \nATOM    203  C   TYR A  29       1.125  11.125   7.815  1.00  4.92           C  \nATOM    204  O   TYR A  29       0.286  10.632   8.545  1.00  7.13           O  \nATOM    205  CB  TYR A  29       0.755  11.229   5.322  1.00  9.66           C  \nATOM    206  CG  TYR A  29      -0.203  10.044   5.354  1.00 11.56           C  \nATOM    207  CD1 TYR A  29      -1.547  10.337   5.645  1.00 12.85           C  \nATOM    208  CD2 TYR A  29       0.193   8.750   5.100  1.00 14.44           C  \nATOM    209  CE1 TYR A  29      -2.496   9.329   5.673  1.00 16.61           C  \nATOM    210  CE2 TYR A  29      -0.801   7.705   5.156  1.00 17.11           C  \nATOM    211  CZ  TYR A  29      -2.079   8.031   5.430  1.00 19.99           C  \nATOM    212  OH  TYR A  29      -3.097   7.057   5.458  1.00 28.98           O  \nATOM    213  N   THR A  30       2.470  10.984   7.995  1.00  5.31           N  \nATOM    214  CA  THR A  30       2.986   9.994   8.950  1.00  5.70           C  \nATOM    215  C   THR A  30       3.609  10.505  10.230  1.00  6.28           C  \nATOM    216  O   THR A  30       3.766   9.715  11.186  1.00  8.77           O  \nATOM    217  CB  THR A  30       4.076   9.103   8.225  1.00  6.55           C  \nATOM    218  OG1 THR A  30       5.125  10.027   7.824  1.00  6.57           O  \nATOM    219  CG2 THR A  30       3.493   8.324   7.035  1.00  7.29           C  \nATOM    220  N   GLY A  31       3.984  11.764  10.241  1.00  4.99           N  \nATOM    221  CA  GLY A  31       4.769  12.336  11.360  1.00  5.50           C  \nATOM    222  C   GLY A  31       6.255  12.243  11.106  1.00  4.19           C  \nATOM    223  O   GLY A  31       7.037  12.750  11.954  1.00  6.12           O  \nATOM    224  N   CYS A  32       6.710  11.631   9.992  1.00  4.30           N  \nATOM    225  CA  CYS A  32       8.140  11.694   9.635  1.00  4.89           C  \nATOM    226  C   CYS A  32       8.500  13.141   9.206  1.00  5.50           C  \nATOM    227  O   CYS A  32       7.581  13.949   8.944  1.00  5.82           O  \nATOM    228  CB  CYS A  32       8.504  10.686   8.530  1.00  4.66           C  \nATOM    229  SG  CYS A  32       8.048   8.987   8.881  1.00  5.33           S  \nATOM    230  N   ILE A  33       9.793  13.410   9.173  1.00  6.02           N  \nATOM    231  CA  ILE A  33      10.280  14.760   8.823  1.00  5.24           C  \nATOM    232  C   ILE A  33      11.346  14.658   7.743  1.00  5.16           C  \nATOM    233  O   ILE A  33      11.971  13.583   7.552  1.00  7.19           O  \nATOM    234  CB  ILE A  33      10.790  15.535  10.085  1.00  5.49           C  \nATOM    235  CG1 ILE A  33      12.059  14.803  10.671  1.00  6.85           C  \nATOM    236  CG2 ILE A  33       9.684  15.686  11.138  1.00  6.45           C  \nATOM    237  CD1 ILE A  33      12.733  15.676  11.781  1.00  8.94           C  \nATOM    238  N   ILE A  34      11.490  15.773   7.038  1.00  5.52           N  \nATOM    239  CA  ILE A  34      12.552  15.877   6.036  1.00  6.82           C  \nATOM    240  C   ILE A  34      13.590  16.917   6.560  1.00  6.92           C  \nATOM    241  O   ILE A  34      13.168  18.006   6.945  1.00  9.22           O  \nATOM    242  CB  ILE A  34      11.987  16.360   4.681  1.00  8.11           C  \nATOM    243  CG1 ILE A  34      10.914  15.338   4.163  1.00  9.59           C  \nATOM    244  CG2 ILE A  34      13.131  16.517   3.629  1.00  9.73           C  \nATOM    245  CD1 ILE A  34      10.151  16.024   2.938  1.00 13.41           C  \nATOM    246  N   ILE A  35      14.856  16.493   6.536  1.00  7.06           N  \nATOM    247  CA  ILE A  35      15.930  17.454   6.941  1.00  7.52           C  \nATOM    248  C   ILE A  35      16.913  17.550   5.819  1.00  6.63           C  \nATOM    249  O   ILE A  35      17.097  16.660   4.970  1.00  7.90           O  \nATOM    250  CB  ILE A  35      16.622  16.995   8.285  1.00  8.07           C  \nATOM    251  CG1 ILE A  35      17.360  15.651   8.067  1.00  9.41           C  \nATOM    252  CG2 ILE A  35      15.592  16.974   9.434  1.00  9.46           C  \nATOM    253  CD1 ILE A  35      18.298  15.206   9.219  1.00  9.85           C  \nATOM    254  N   PRO A  36      17.664  18.669   5.806  1.00  8.07           N  \nATOM    255  CA  PRO A  36      18.635  18.861   4.738  1.00  8.78           C  \nATOM    256  C   PRO A  36      19.925  18.042   4.949  1.00  8.31           C  \nATOM    257  O   PRO A  36      20.593  17.742   3.945  1.00  9.09           O  \nATOM    258  CB  PRO A  36      18.945  20.364   4.783  1.00  9.67           C  \nATOM    259  CG  PRO A  36      18.238  20.937   5.908  1.00 10.15           C  \nATOM    260  CD  PRO A  36      17.371  19.900   6.596  1.00  9.53           C  \nATOM    261  N   GLY A  37      20.172  17.730   6.217  1.00  8.48           N  \nATOM    262  CA  GLY A  37      21.452  16.969   6.513  1.00  9.20           C  \nATOM    263  C   GLY A  37      21.143  15.478   6.427  1.00 10.41           C  \nATOM    264  O   GLY A  37      20.138  15.023   5.878  1.00 12.06           O  \nATOM    265  N   ALA A  38      22.055  14.701   7.032  1.00  9.24           N  \nATOM    266  CA  ALA A  38      22.019  13.242   7.020  1.00  9.24           C  \nATOM    267  C   ALA A  38      21.944  12.628   8.396  1.00  9.60           C  \nATOM    268  O   ALA A  38      21.869  11.387   8.435  1.00 13.65           O  \nATOM    269  CB  ALA A  38      23.246  12.697   6.275  1.00 10.43           C  \nATOM    270  N   THR A  39      21.894  13.435   9.436  1.00  8.70           N  \nATOM    271  CA  THR A  39      21.936  12.911  10.809  1.00  9.46           C  \nATOM    272  C   THR A  39      20.615  13.191  11.521  1.00  8.32           C  \nATOM    273  O   THR A  39      20.357  14.317  11.948  1.00  9.89           O  \nATOM    274  CB  THR A  39      23.131  13.601  11.593  1.00 10.72           C  \nATOM    275  OG1 THR A  39      24.284  13.401  10.709  1.00 11.66           O  \nATOM    276  CG2 THR A  39      23.340  12.935  12.962  1.00 11.81           C  \nATOM    277  N   CYS A  40      19.827  12.110  11.642  1.00  7.64           N  \nATOM    278  CA  CYS A  40      18.504  12.312  12.298  1.00  8.05           C  \nATOM    279  C   CYS A  40      18.684  12.451  13.784  1.00  7.63           C  \nATOM    280  O   CYS A  40      19.533  11.718  14.362  1.00  9.64           O  \nATOM    281  CB  CYS A  40      17.582  11.117  11.996  1.00  7.80           C  \nATOM    282  SG  CYS A  40      17.199  10.929  10.237  1.00  7.30           S  \nATOM    283  N   PRO A  41      17.880  13.266  14.426  1.00  8.00           N  \nATOM    284  CA  PRO A  41      17.924  13.421  15.877  1.00  8.96           C  \nATOM    285  C   PRO A  41      17.392  12.206  16.594  1.00  9.06           C  \nATOM    286  O   PRO A  41      16.652  11.368  16.033  1.00  8.82           O  \nATOM    287  CB  PRO A  41      17.076  14.658  16.145  1.00 10.39           C  \nATOM    288  CG  PRO A  41      16.098  14.689  14.997  1.00 10.99           C  \nATOM    289  CD  PRO A  41      16.859  14.150  13.779  1.00 10.49           C  \nATOM    290  N   GLY A  42      17.728  12.124  17.884  1.00  7.55           N  \nATOM    291  CA  GLY A  42      17.334  10.956  18.691  1.00  8.00           C  \nATOM    292  C   GLY A  42      15.875  10.688  18.871  1.00  7.22           C  \nATOM    293  O   GLY A  42      15.434   9.550  19.166  1.00  8.41           O  \nATOM    294  N   ASP A  43      15.036  11.747  18.715  1.00  5.54           N  \nATOM    295  CA  ASP A  43      13.564  11.573  18.836  1.00  5.85           C  \nATOM    296  C   ASP A  43      12.936  11.227  17.470  1.00  5.87           C  \nATOM    297  O   ASP A  43      11.720  11.040  17.428  1.00  7.29           O  \nATOM    298  CB  ASP A  43      12.933  12.737  19.580  1.00  6.72           C  \nATOM    299  CG  ASP A  43      13.140  14.094  18.958  1.00  8.59           C  \nATOM    300  OD1 ASP A  43      14.109  14.303  18.212  1.00  9.59           O  \nATOM    301  OD2 ASP A  43      12.267  14.963  19.265  1.00 11.45           O  \nATOM    302  N   TYR A  44      13.725  11.174  16.425  1.00  5.22           N  \nATOM    303  CA  TYR A  44      13.257  10.745  15.081  1.00  5.56           C  \nATOM    304  C   TYR A  44      14.275   9.687  14.612  1.00  4.61           C  \nATOM    305  O   TYR A  44      14.930   9.862  13.568  1.00  6.04           O  \nATOM    306  CB  TYR A  44      13.200  11.914  14.071  1.00  5.41           C  \nATOM    307  CG  TYR A  44      12.000  12.819  14.399  1.00  5.34           C  \nATOM    308  CD1 TYR A  44      12.119  13.853  15.332  1.00  6.59           C  \nATOM    309  CD2 TYR A  44      10.775  12.617  13.762  1.00  5.94           C  \nATOM    310  CE1 TYR A  44      11.045  14.675  15.610  1.00  5.97           C  \nATOM    311  CE2 TYR A  44       9.676  13.433  14.048  1.00  5.17           C  \nATOM    312  CZ  TYR A  44       9.802  14.456  14.996  1.00  5.96           C  \nATOM    313  OH  TYR A  44       8.740  15.265  15.269  1.00  8.60           O  \nATOM    314  N   ALA A  45      14.342   8.640  15.422  1.00  4.76           N  \nATOM    315  CA  ALA A  45      15.445   7.667  15.246  1.00  5.89           C  \nATOM    316  C   ALA A  45      15.171   6.533  14.280  1.00  6.67           C  \nATOM    317  O   ALA A  45      16.093   5.705  14.039  1.00  7.56           O  \nATOM    318  CB  ALA A  45      15.680   7.099  16.682  1.00  6.82           C  \nATOM    319  N   ASN A  46      13.966   6.502  13.739  1.00  5.80           N  \nATOM    320  CA  ASN A  46      13.512   5.395  12.878  1.00  6.15           C  \nATOM    321  C   ASN A  46      13.311   5.853  11.455  1.00  6.61           C  \nATOM    322  O   ASN A  46      13.733   6.929  11.026  1.00  7.18           O  \nATOM    323  CB  ASN A  46      12.266   4.769  13.501  1.00  7.27           C  \nATOM    324  CG  ASN A  46      12.538   4.304  14.922  1.00  7.98           C  \nATOM    325  OD1 ASN A  46      11.982   4.849  15.886  1.00 11.00           O  \nATOM    326  ND2 ASN A  46      13.407   3.298  15.015  1.00 10.32           N  \nATOM    327  OXT ASN A  46      12.703   4.973  10.746  1.00  7.86           O  \nTER     328      ASN A  46                                                      \nCONECT   20  282                                                                \nCONECT   26  229                                                                \nCONECT  116  188                                                                \nCONECT  188  116                                                                \nCONECT  229   26                                                                \nCONECT  282   20                                                                \nMASTER      227    0    0    2    2    1    0    6  327    1    6    4          \nEND                                                                             \n', 1);
      						    		$.get('http://www.rcsb.org/pdb/files/'+pdb_name+'.pdb', function(data) {			
      						    			var mol = ChemDoodle.readPDB(data);
      						    			pdb.loadMolecule(mol);
      						    			pdb.startAnimation();
      						    		});
      	    				    	}
      	    					}
      	    				}
      	    			});
      	    			
      	    			_this.p3dProtein.add(pan);
      	    		}
    			}
    			else{
    				_this.p3dProtein.setTitle('No proteins found');
    			}


  	    	}
    	});
    	
//    	$.get('http://ws.bioinfo.cipf.es/celldb/rest/v1/hsa/feature/id/brca2/xref?dbname=pdb', 
    	
    	
    	
    	
//    	http://www.rcsb.org/pdb/files/1A17.pdb
    	
//    	http://www.rcsb.org/pdb/files/AAAA.pdb
    	
//		var pan = Ext.create('Ext.panel.Panel',{
//			title:"3D Protein Viewer",
//	        border:false,
//	        cls:'panel-border-left',
//			flex:3,
//			bodyPadding:5,
//			autoScroll:true,
//			html:'<canvas class="ChemDoodleWebComponent" id="pdb_canvas_prueba" width="600" height="600" style="width: 600px; height: 600px; ">This browser does not support HTML5/Canvas.</canvas>',
//
//		});

    }
    return this.p3dProtein;

};




GeneOrangeInfoWidget.prototype.getEnsembleId = function (){

};


GeneOrangeInfoWidget.prototype.getData = function (){
	var _this = this;
	this.panel.disable();
	this.panel.setLoading("Getting information...");
//	category, subcategory, query, resource, callbackFunction
	var cellBaseManager = new CellBaseManager(this.species);
	cellBaseManager.success.addEventListener(function(sender,data){
		_this.dataReceived(JSON.parse(data.result));//TODO
	});
	cellBaseManager.get("feature","gene", this.query, "fullinfo");
};
GeneOrangeInfoWidget.prototype.dataReceived = function (data){
	this.data=data[0][0];
	console.log(this.data);
	this.optionClick({"text":"Information","leaf":"true"});
	this.panel.enable();
	this.panel.setLoading(false);
};
MirnaInfoWidget.prototype.draw = InfoWidget.prototype.draw;
MirnaInfoWidget.prototype.render = InfoWidget.prototype.render;
MirnaInfoWidget.prototype.getTreePanel = InfoWidget.prototype.getTreePanel;
MirnaInfoWidget.prototype.checkDataTypes = InfoWidget.prototype.checkDataTypes;
MirnaInfoWidget.prototype.doGrid = InfoWidget.prototype.doGrid;
MirnaInfoWidget.prototype.getTranscriptTemplate = InfoWidget.prototype.getTranscriptTemplate;
MirnaInfoWidget.prototype.getGeneTemplate = InfoWidget.prototype.getGeneTemplate;


function MirnaInfoWidget(targetId, species, args){
	if (args == null){
		args = new Object();
	}
	args.title = "miRNA Information";
	InfoWidget.prototype.constructor.call(this, targetId, species, args);
};

MirnaInfoWidget.prototype.getdataTypes = function (){
	//Abstract method
	return dataTypes=[
	            { text: "Information", children: [
	                { text: "miRNA"},
	                { text: "Transcript"}, 
	                { text: "Gene"} 
	            ] },
	            { text: "Regulatory", children: [
  	                { text: "Target Genes"}
  	            ] },
  	            { text: "Disease", children: [
  	              { text: "Related Diseases"}
  	            ] }
	        ];
};
MirnaInfoWidget.prototype.optionClick = function (item){
	//Abstract method
	if (item.leaf){
		if(this.panel.getComponent(1)!=null){
			this.panel.getComponent(1).hide();
			this.panel.remove(1,false);
		}
		switch (item.text){
			case "miRNA":  this.panel.add(this.getMirnaPanel(this.data.mirna).show()); break;
			case "Transcript": this.panel.add(this.getTranscriptPanel(this.data.transcripts).show()); break;
			case "Gene": this.panel.add(this.getGenePanel(this.data.genes).show()); break;
			case "Target Genes": this.panel.add(this.getTargetGenesGrid(this.data.targetGenes).show()); break;
			case "Related Diseases": this.panel.add(this.getMirnaDiseasesGrid(this.data.mirnaDiseases).show()); break;
		}
	}
};

MirnaInfoWidget.prototype.getMirnaPanel = function(data){
	if(data.mirnaMature.length<=0 && data.mirnaGenes.length<=0){
		return this.notFoundPanel;
	}
    if(this.mirnaPanel==null){
    	
    	
    	var tplMature = this.getMirnaMatureTemplate();
    	var tplGene = this.getMirnaGeneTemplate();

    	var panels = [];
    	for ( var i = 0; i < data.mirnaMature.length; i++) {
			var maturePan = Ext.create('Ext.container.Container',{
				padding:5,
				data:data.mirnaMature[i],
				tpl:tplMature
			});
			panels.push(maturePan);
    	}
    	
    	for ( var i = 0; i < data.mirnaGenes.length; i++) {
			var genePan = Ext.create('Ext.container.Container',{
				padding:5,
				data:data.mirnaGenes[i],
				tpl:tplGene
			});
			panels.push(genePan);
    	}
		this.mirnaPanel = Ext.create('Ext.panel.Panel',{
			title:"miRNA",
			border:false,
			cls:'ocb-border-left-lightgrey',
			flex:3,    
			bodyPadding:5,
			autoScroll:true,
			items:panels
		});
    }
    return this.mirnaPanel;
};


MirnaInfoWidget.prototype.getTranscriptPanel = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.transcriptGrid==null){
    	
    	var tpl = this.getTranscriptTemplate();
    	
    	var panels = [];
    	for ( var i = 0; i < data.length; i++) {	
			var transcriptPanel = Ext.create('Ext.container.Container',{
				padding:5,
				data:data[i],
				tpl:tpl
			});
			panels.push(transcriptPanel);
    	}
		this.transcriptGrid = Ext.create('Ext.panel.Panel',{
			title:"Transcripts ("+i+")",
			border:false,
			cls:'ocb-border-left-lightgrey',
			flex:3,    
			bodyPadding:5,
			autoScroll:true,
			items:panels
		});
    }
    return this.transcriptGrid;
};

MirnaInfoWidget.prototype.getGenePanel = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.genePanel==null){
    	
    	var tpl = this.getGeneTemplate();
    	
    	var panels = [];
    	for ( var i = 0; i < data.length; i++) {	
			var genePan = Ext.create('Ext.container.Container',{
				padding:5,
				data:data[i],
				tpl:tpl
			});
			panels.push(genePan);
    	}
		this.genePanel = Ext.create('Ext.panel.Panel',{
			title:"Genes ("+i+")",
			border:false,
			cls:'ocb-border-left-lightgrey',
			flex:3,    
			bodyPadding:5,
			autoScroll:true,
			items:panels
		});
    }
    return this.genePanel;
};

MirnaInfoWidget.prototype.getTargetGenesGrid = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.targetGenesGrid==null){
//    	console.log(data);
    	var groupField = '';
    	var modelName = "targetGenes";
    	var fields = ['externalName','stableId','biotype','chromosome','start','end','strand','description'];
    	var columns = [
    	               {header : 'Name',dataIndex: 'externalName',flex:1},
    	               {header : 'Stable Id',dataIndex: 'stableId',flex:2},
    	               {header : 'Biotype',dataIndex: 'biotype',flex:1.5},
    	               {header : 'Chr',dataIndex: 'chromosome',flex:0.5},
    	               {header : 'Start',dataIndex: 'start',flex:1},
    	               {header : 'End',dataIndex: 'end',flex:1},
    	               {header : 'Strand',dataIndex: 'strand',flex:0.5},
    	               {header : 'Description',dataIndex: 'description',flex:1}
    	               ];
    	this.targetGenesGrid = this.doGrid(columns,fields,modelName,groupField);
    	this.targetGenesGrid.store.loadData(data);
    }
    return this.targetGenesGrid;
};

MirnaInfoWidget.prototype.getMirnaDiseasesGrid = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.mirnaDiseasesGrid==null){
//    	console.log(data);
    		var groupField = '';
    		var modelName = "mirnaDiseases";
    		var fields = ['mirnaDiseaseId','mirnaGene','mirbaseId','diseaseName','pubmedId','description'];
    		var columns = [
    		               {header : 'Name',dataIndex: 'mirbaseId',flex:1.5},
    		               {header : 'Disease',dataIndex: 'diseaseName',flex:1.5},
    		               {header : 'PubMed id',dataIndex: 'pubmedId',flex:1},
    		               {header : 'Description',dataIndex: 'description',flex:3}
    		               ];
    		this.mirnaDiseasesGrid = this.doGrid(columns,fields,modelName,groupField);
    		this.mirnaDiseasesGrid.store.loadData(data);
    }
    return this.mirnaDiseasesGrid;
};


MirnaInfoWidget.prototype.getData = function (){
	var _this = this;
	this.panel.disable();
	this.panel.setLoading("Getting information...");
//	category, subcategory, query, resource, callbackFunction
	var cellBaseDataAdapter = new CellBaseDataAdapter(this.species);
	cellBaseDataAdapter.successed.addEventListener(function (evt){
		_this.dataReceived(cellBaseDataAdapter.toJSON());
	});
	cellBaseDataAdapter.fill("regulatory","mirna_mature", this.feature.getName(), "fullinfo");
	console.log(this.feature.getName());
};
MirnaInfoWidget.prototype.dataReceived = function (data){
	var parseData = JSON.parse(data);
	this.data=parseData[0];
	console.log(this.data);
	this.optionClick({"text":"miRNA","leaf":"true"});
	this.panel.enable();
	this.panel.setLoading(false);
};

MirnaInfoWidget.prototype.getMirnaMatureTemplate = function (){
	return new Ext.XTemplate(
			 '<p><span class="panel-border-bottom"><span class="ssel s130">miRNA mature</span> &nbsp; <span class="emph s120"> {mirbaseId} </span></span></p>',
			 '<br>',
			 '<p><span class="w140 dis s90">miRBase Accession: </span> <span class="">{mirbaseAcc}</span></p>',
			 '<span class="w140 dis s90">Sequence: </span> <span class="">{sequence}</span>'
		);
};

MirnaInfoWidget.prototype.getMirnaGeneTemplate = function (){
	return new Ext.XTemplate(
			 '<p><span class="panel-border-bottom"><span class="ssel s130">miRNA gene</span> &nbsp; <span class="emph s120"> {mirbaseId} </span></span></p>',
			 '<br>',
			 '<p><span class="w140 dis s90">miRBase Accession: </span> <span class="">{mirbaseAcc}</span></p>',
			 '<span class="w140 dis s90">Sequence: </span> <span class="">{sequence}</span>',
			 '<p><span class="w140 dis s90">Status: </span> <span class="">{status}</span></p>'
		);
};



ProteinInfoWidget.prototype.draw = InfoWidget.prototype.draw;
ProteinInfoWidget.prototype.render = InfoWidget.prototype.render;
ProteinInfoWidget.prototype.getTreePanel = InfoWidget.prototype.getTreePanel;
ProteinInfoWidget.prototype.checkDataTypes = InfoWidget.prototype.checkDataTypes;
ProteinInfoWidget.prototype.doGrid = InfoWidget.prototype.doGrid;

function ProteinInfoWidget(targetId, species, args){
	if (args == null){
		args = new Object();
	}
	args.title = "Protein Info";
	InfoWidget.prototype.constructor.call(this, targetId, species, args);
};

ProteinInfoWidget.prototype.getdataTypes = function (){
	//Abstract method
	return dataTypes=[
	            { text: "Sumary", children: [
	                { text: "Long"},
	                { text: "Seq"}
	            ] },
	            { text: "Functional information", children: [
	                { text: "GO"},
	                { text: "Reactome"},
	                { text: "Interpro"}
	            ] },
	            { text: "Interactions"},
	            { text: "Variations"}
	           
	        ];
};
ProteinInfoWidget.prototype.optionClick = function (item){
	//Abstract method
	if (item.leaf){
		if(this.panel.getComponent(1)!=null){
			this.panel.getComponent(1).hide();
			this.panel.remove(1,false);
		}
		switch (item.text){
			case "":  break;
			case "":  break;
//			case "GO": this.panel.add(this.getGoGrid().show()); break;
			case "Reactome": break;
			case "Interpro": break;
			case "": break;
			case "": break;
			case "": break;
		}
	}
};
SnpInfoWidget.prototype.draw = InfoWidget.prototype.draw;
SnpInfoWidget.prototype.render = InfoWidget.prototype.render;
SnpInfoWidget.prototype.getTreePanel = InfoWidget.prototype.getTreePanel;
SnpInfoWidget.prototype.checkDataTypes = InfoWidget.prototype.checkDataTypes;
SnpInfoWidget.prototype.doGrid = InfoWidget.prototype.doGrid;
SnpInfoWidget.prototype.getSnpTemplate = InfoWidget.prototype.getSnpTemplate;
SnpInfoWidget.prototype.getSnpTranscriptTemplate = InfoWidget.prototype.getSnpTranscriptTemplate;
SnpInfoWidget.prototype.getConsequenceTypeTemplate = InfoWidget.prototype.getConsequenceTypeTemplate;
SnpInfoWidget.prototype.getPhenotypeTemplate = InfoWidget.prototype.getPhenotypeTemplate;
SnpInfoWidget.prototype.getPopulationTemplate = InfoWidget.prototype.getPopulationTemplate;

function SnpInfoWidget(targetId, species, args){
	if (args == null){
		args = new Object();
	}
	args.title = "SNP Info";
	InfoWidget.prototype.constructor.call(this, targetId, species, args);
};

SnpInfoWidget.prototype.getdataTypes = function (){
	//Abstract method
	return dataTypes=[
	            { text: "Genomic", children: [
	                { text: "Information"},
	                { text: "Transcripts"}
	            ] },
	            { text: "Consequence type"},
	            { text: "Annotated phenotype"}
//	            { text: "Population frequency"}
	           
	        ];
};
SnpInfoWidget.prototype.optionClick = function (item){
	//Abstract method
	if (item.leaf){
		if(this.panel.getComponent(1)!=null){
			this.panel.getComponent(1).hide();
			this.panel.remove(1,false);
		}
		switch (item.text){
			case "Information":  this.panel.add(this.getInfoPanel(this.data).show()); break;
			case "Transcripts": this.panel.add(this.getSnpTranscriptPanel(this.data.transcriptVariations).show()); break;
			case "Consequence type": this.panel.add(this.getConsequenceTypePanel(this.data.transcriptVariations).show()); break;
			case "Annotated phenotype": this.panel.add(this.getPhenotypePanel(this.data.phenotype).show()); break;
//			case "Population frequency": this.panel.add(this.getPopulationPanel(this.data.population).show()); break;
		}
	}
};

SnpInfoWidget.prototype.getInfoPanel = function(data){
	if(data==null){
		return this.notFoundPanel;
	}
    if(this.infoPanel==null){
    	var tpl = this.getSnpTemplate();

		this.infoPanel = Ext.create('Ext.panel.Panel',{
			title:"Information",
	        border:false,
	        cls:'ocb-border-left-lightgrey',
			flex:3,    
			bodyPadding:10,
			data:data,
			tpl:tpl
		});

    }
    return this.infoPanel;
};


SnpInfoWidget.prototype.getSnpTranscriptPanel = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.snpTranscriptGrid==null){
    	var tpl = this.getSnpTranscriptTemplate();
    	
    	var panels = [];
    	for ( var i = 0; i < data.length; i++) {	
			var snpTranscriptPanel = Ext.create('Ext.container.Container',{
				padding:5,
				data:data[i],
				tpl:tpl
			});
			panels.push(snpTranscriptPanel);
    	}
		this.snpTranscriptGrid = Ext.create('Ext.panel.Panel',{
			title:"Transcripts ("+i+")",
			border:false,
			cls:'ocb-border-left-lightgrey',
			flex:3,    
			bodyPadding:5,
			autoScroll:true,
			items:panels
		});
    }
    return this.snpTranscriptGrid;
};

SnpInfoWidget.prototype.getConsequenceTypePanel = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.consequencePanel==null){
    	var tpl = this.getConsequenceTypeTemplate();


        var data2 = [];
        for(var i = 0; i<data.length; i++){
            for(var j = 0; j<data[i].consequenceTypes.length; j++){
                var consequenceType = data[i].consequenceTypes[j];
                data[i].consequenceType = consequenceType;
                data2.push(data[i]);
            }
        }

        var groupField = 'consequenceType';
        var modelName = 'transcriptVariation';
        var fields = ['transcriptId','consequenceType'];
        var columns = [
            {header : 'Transcript id',dataIndex: 'transcriptId',flex:1},
            {header : 'Consequence type',dataIndex: 'consequenceType',flex:1}
        ];
        this.consequencePanel = this.doGrid(columns,fields,modelName,groupField);
        this.consequencePanel.store.loadData(data2);

//    	var panels = [];
//    	for ( var i = 0; i < data.length; i++) {
//			var consPanel = Ext.create('Ext.container.Container',{
//				padding:5,
//				data:data[i],
//				tpl:tpl
//			});
//			panels.push(consPanel);
//    	}
//		this.consequencePanel = Ext.create('Ext.panel.Panel',{
//			title:"Consequence type ("+i+")",
//			border:false,
//			cls:'ocb-border-left-lightgrey',
//			flex:3,
//			bodyPadding:5,
//			autoScroll:true,
//			items:panels
//		});
    }
    return this.consequencePanel;
};


SnpInfoWidget.prototype.getPhenotypePanel = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.phenotypePanel==null){
    	var tpl = this.getPhenotypeTemplate();
    	
    	var panels = [];
    	for ( var i = 0; i < data.length; i++) {	
			var pan = Ext.create('Ext.container.Container',{
				padding:5,
				data:data[i],
				tpl:tpl
			});
			panels.push(pan);
    	}
		this.phenotypePanel = Ext.create('Ext.panel.Panel',{
			title:"Phenotype ("+i+")",
			border:false,
			cls:'ocb-border-left-lightgrey',
			flex:3,    
			bodyPadding:5,
			autoScroll:true,
			items:panels
		});
    }
    return this.phenotypePanel;
};



SnpInfoWidget.prototype.getPopulationPanel = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.populationPanel==null){
    	var tpl = this.getPopulationTemplate();
    	
    	var panels = [];
    	for ( var i = 0; i < data.length; i++) {	
			var pan = Ext.create('Ext.container.Container',{
				padding:5,
				data:data[i],
				tpl:tpl
			});
			panels.push(pan);
    	}
		this.populationPanel = Ext.create('Ext.panel.Panel',{
			title:"Population ("+i+")",
			border:false,
			cls:'ocb-border-left-lightgrey',
			flex:3,    
			bodyPadding:5,
			autoScroll:true,
			items:panels
		});
    }
    return this.populationPanel;
};


SnpInfoWidget.prototype.getData = function (){
	var _this = this;
	this.panel.disable();
	this.panel.setLoading("Getting information...");

    CellBaseManager.get({
        species:this.species,
        category:'feature',
        subCategory:'snp',
        query:this.query,
        resource:"info",
        success:function(data){
            _this.dataReceived(data.response[0].result[0]);
        }
    });

};
SnpInfoWidget.prototype.dataReceived = function (data){
//	var mappedSnps = data[0];
//	for ( var i = 0; i < mappedSnps.length; i++) {
//		if (mappedSnps[i].chromosome == this.feature.chromosome && mappedSnps[i].start == this.feature.start && mappedSnps[i].end == this.feature.end ){
//			this.data=mappedSnps[i];
//			console.log(mappedSnps[i]);
//		}
//	}
    this.data=data;
    console.log(this.data);
	this.optionClick({"text":"Information","leaf":"true"});
	this.panel.enable();
	this.panel.setLoading(false);
};

TFInfoWidget.prototype.draw = InfoWidget.prototype.draw;
TFInfoWidget.prototype.render = InfoWidget.prototype.render;
TFInfoWidget.prototype.getTreePanel = InfoWidget.prototype.getTreePanel;
TFInfoWidget.prototype.checkDataTypes = InfoWidget.prototype.checkDataTypes;
TFInfoWidget.prototype.doGrid = InfoWidget.prototype.doGrid;
TFInfoWidget.prototype.getTranscriptTemplate = InfoWidget.prototype.getTranscriptTemplate;
TFInfoWidget.prototype.getProteinTemplate =InfoWidget.prototype.getProteinTemplate;
TFInfoWidget.prototype.getGeneTemplate = InfoWidget.prototype.getGeneTemplate;
TFInfoWidget.prototype.getPWMTemplate = InfoWidget.prototype.getPWMTemplate;
TFInfoWidget.prototype.getProteinXrefTemplate = InfoWidget.prototype.getProteinXrefTemplate;

function TFInfoWidget(targetId, species, args){
	if (args == null){
		args = new Object();
	}
	args.title = "Transcription Factor Info";
	InfoWidget.prototype.constructor.call(this, targetId, species, args);
};

TFInfoWidget.prototype.getdataTypes = function (){
	//Abstract method
	return dataTypes=[
	            { text: "Information", children: [
	                { text: "Protein"},
	                { text: "Transcript"}, 
	                { text: "Gene"} 
	            ] },
	            { text: "Regulatory", children: [
  	                { text: "PWM"},//position weight matrix (PWM)
  	                { text: "Target Genes"}
  	            ] },
  	            { text: "Protein Features", children: [
  	              { text: "Protein profile"},//position weight matrix (PWM)
  	              { text: "Mutation sites"},
  	              { text: "Pathways"},
  	              { text: "Protein interactions"},
  	              { text: "Related Diseases"}
  	            ] }
	        ];
};
TFInfoWidget.prototype.optionClick = function (item){
	//Abstract method
	if (item.leaf){
		if(this.panel.getComponent(1)!=null){
			this.panel.getComponent(1).hide();
			this.panel.remove(1,false);
		}
		switch (item.text){
			case "Protein":  this.panel.add(this.getProteinPanel(this.data.proteins).show()); break;
			case "Transcript": this.panel.add(this.getTranscriptPanel(this.data.transcripts).show()); break;
			case "Gene": this.panel.add(this.getGenePanel(this.data.gene).show()); break;
			case "PWM": this.panel.add(this.getPWMPanel(this.data.pwm).show()); break;
			case "Target Genes": this.panel.add(this.getTargetGenesGrid(this.data.targetGenes).show()); break;
			case "Protein profile": this.panel.add(this.getProteinFeatureGrid(this.data.protein_feature, "Protein profile").show());  break;
			case "Mutation sites": this.panel.add(this.getProteinFeatureGrid(this.data.protein_feature, "Mutation sites").show());  break;
			case "Pathways": this.panel.add(this.getProteinXrefPanel(this.data.protein_xref, "Pathway").show()); break;
			case "Protein interactions": this.panel.add(this.getProteinXrefPanel(this.data.protein_xref, "Interaction").show()); break;
			case "Related Diseases": this.panel.add(this.getProteinXrefPanel(this.data.protein_xref, "Disease").show()); break;
		}
	}
};

TFInfoWidget.prototype.getProteinPanel = function(data){
	if(data==null){
		return this.notFoundPanel;
	}
    if(this.proteinPanel==null){

    	var tpl = this.getProteinTemplate();

		this.proteinPanel = Ext.create('Ext.panel.Panel',{
			title:"Protein",
	        border:false,
	        cls:'ocb-border-left-lightgrey',
			flex:3,    
			bodyPadding:10,
			data:data[0],
			tpl:tpl
		});

    }
    return this.proteinPanel;
};


TFInfoWidget.prototype.getTranscriptPanel = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.transcriptGrid==null){
    	
    	var tpl = this.getTranscriptTemplate();
    	
    	var panels = [];
    	for ( var i = 0; i < data.length; i++) {	
			var transcriptPanel = Ext.create('Ext.container.Container',{
				padding:5,
				data:data[i],
				tpl:tpl
			});
			panels.push(transcriptPanel);
    	}
		this.transcriptGrid = Ext.create('Ext.panel.Panel',{
			title:"Transcripts ("+i+")",
			border:false,
			cls:'ocb-border-left-lightgrey',
			flex:3,    
			bodyPadding:5,
			autoScroll:true,
			items:panels
		});
    }
    return this.transcriptGrid;
};

TFInfoWidget.prototype.getGenePanel = function(data){
	if(data==null){
		return this.notFoundPanel;
	}
    if(this.genePanel==null){
    	
    	var tpl = this.getGeneTemplate();
    	
		this.genePanel = Ext.create('Ext.panel.Panel',{
			title:"Gene information",
	        border:false,
	        cls:'ocb-border-left-lightgrey',
			flex:3,
			bodyPadding:10,
			data:data,
			tpl:tpl
		});

    }
    return this.genePanel;
};

TFInfoWidget.prototype.getPWMPanel = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.pwmPanel==null){
    	var tpl = this.getPWMTemplate();
    	
    	var panels = [];
    	for ( var i = 0; i < data.length; i++) {	
			var pwmPan = Ext.create('Ext.container.Container',{
				padding:5,
				data:data[i],
				tpl:tpl
			});
			panels.push(pwmPan);
    	}
		this.pwmPanel = Ext.create('Ext.panel.Panel',{
			title:"PWM ("+i+")",
	        border:false,
	        cls:'ocb-border-left-lightgrey',
			flex:3,
			bodyPadding:5,
			autoScroll:true,
			items:panels
		});
    }
    return this.pwmPanel;
};

TFInfoWidget.prototype.getTargetGenesGrid = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.targetGenesGrid==null){
//    	console.log(data);
    	
    	var groupField = '';
    	var modelName = "targetGenes";
    	var fields = ['externalName','stableId','biotype','chromosome','start','end','strand','description'];
    	var columns = [
    	               {header : 'Name',dataIndex: 'externalName',flex:1},
    	               {header : 'Stable Id',dataIndex: 'stableId',flex:2},
    	               {header : 'Biotype',dataIndex: 'biotype',flex:1.5},
    	               {header : 'Chr',dataIndex: 'chromosome',flex:0.5},
    	               {header : 'Start',dataIndex: 'start',flex:1},
    	               {header : 'End',dataIndex: 'end',flex:1},
    	               {header : 'Strand',dataIndex: 'strand',flex:0.5},
    	               {header : 'Description',dataIndex: 'description',flex:1}
    	               ];
    	this.targetGenesGrid = this.doGrid(columns,fields,modelName,groupField);
    	this.targetGenesGrid.store.loadData(data);
    }
    return this.targetGenesGrid;
};


TFInfoWidget.prototype.getProteinFeatureGrid = function(data, type){
//	console.log(data.length)
    if(this[type+"Grid"]==null){
//    	console.log(data);
    	
    	//Filtering Mutagenesis
		var notMutaData = new Array();
		var mutaData = new Array();
		for ( var i = 0; i < data.length; i++) {
			if(data[i].type=="mutagenesis site"){
				mutaData.push(data[i]);
			}else{
				notMutaData.push(data[i]);
			}
		}    	
		
    	if(type!=null){
    		if(type=="Protein profile"){
    			var data = notMutaData;
    		}
    		if(type=="Mutation sites"){
    			var data = mutaData;
    		}
    	}
    	if(data.length<=0){
    		return this.notFoundPanel;
    	}
    	
    	var groupField = '';
    	var modelName = type;
    	var fields = ['type','start','end','original','variation','description'];
    	var columns = [
    	               {header : 'Type',dataIndex: 'type',flex:1.5},
    	               {header : 'Start',dataIndex: 'start',flex:0.5},
    	               {header : 'End',dataIndex: 'end',flex:0.5},
    	               {header : 'Original',dataIndex: 'original',flex:0.7},
    	               {header : 'Variation',dataIndex: 'variation',flex:0.7},
    	               {header : 'Description',dataIndex: 'description',flex:3}
    	               ];
    	this[type+"Grid"] = this.doGrid(columns,fields,modelName,groupField);
    	this[type+"Grid"].store.loadData(data);
    }
    return this[type+"Grid"];
};

TFInfoWidget.prototype.getProteinXrefPanel = function(data, type){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this[type+"panel"]==null){
    	var tpl = this.getProteinXrefTemplate();
    	
    	//Filtering Xref
		var pathwayData = new Array();
		var interacData = new Array();
		var diseaseData = new Array();
		
		for ( var i = 0; i < data.length; i++) {
			var src = data[i].source;
			if(src=="Go" || src=="Reactome" || src=="KEGG"){
				pathwayData.push(data[i]);
			}
			if(src=="IntAct" || src=="MINT" || src=="DIP" || src=="String"){
				interacData.push(data[i]);
			}
			if(src=="MIN" || src=="PharmGKB" || src=="Orphanet"){
				diseaseData.push(data[i]);
			}
		}
		
    	if(type!=null){
    		switch(type){
    		case "Pathway":data = pathwayData;break;
    		case "Interaction":data = interacData;break;
    		case "Disease":data = diseaseData;break;
    		}
    	}
    	
    	var panels = [];
    	for ( var i = 0; i < data.length; i++) {	
			var pan = Ext.create('Ext.panel.Panel',{
		        border:false,
				bodyPadding:5,
				data:data[i],
				tpl:tpl
			});
			panels.push(pan);
    	}
    	this[type+"panel"] = Ext.create('Ext.panel.Panel',{
			title:type+" ("+i+")",
	        border:false,
	        cls:'ocb-border-left-lightgrey',
			flex:3,
			bodyPadding:5,
			autoScroll:true,
			items:panels
		});
    }
    return this[type+"panel"];
};


TFInfoWidget.prototype.getData = function (){
	var _this = this;
	this.panel.disable();
	this.panel.setLoading("Getting information...");
//	category, subcategory, query, resource, callbackFunction
	var cellBaseDataAdapter = new CellBaseDataAdapter(this.species);
	cellBaseDataAdapter.successed.addEventListener(function (evt){
		_this.dataReceived(cellBaseDataAdapter.toJSON());
	});
	cellBaseDataAdapter.fill("regulatory","tf", this.feature.getName(), "fullinfo");
	console.log(this.feature.getName());
};
TFInfoWidget.prototype.dataReceived = function (data){
	var parseData = JSON.parse(data);
	this.data=parseData[0];
	console.log(this.data);
	this.optionClick({"text":"Protein","leaf":"true"});
	this.panel.enable();
	this.panel.setLoading(false);
};

TranscriptInfoWidget.prototype.draw = InfoWidget.prototype.draw;
TranscriptInfoWidget.prototype.render = InfoWidget.prototype.render;
TranscriptInfoWidget.prototype.getTreePanel = InfoWidget.prototype.getTreePanel;
TranscriptInfoWidget.prototype.checkDataTypes = InfoWidget.prototype.checkDataTypes;
TranscriptInfoWidget.prototype.doGrid = InfoWidget.prototype.doGrid;
TranscriptInfoWidget.prototype.getGeneTemplate = InfoWidget.prototype.getGeneTemplate;
TranscriptInfoWidget.prototype.getTranscriptTemplate = InfoWidget.prototype.getTranscriptTemplate;
TranscriptInfoWidget.prototype.getExonTemplate = InfoWidget.prototype.getExonTemplate;
//shared with gene
TranscriptInfoWidget.prototype.get3Dprotein = GeneInfoWidget.prototype.get3Dprotein;
TranscriptInfoWidget.prototype.getGenePanel = GeneInfoWidget.prototype.getGenePanel;
TranscriptInfoWidget.prototype.getXrefGrid = GeneInfoWidget.prototype.getXrefGrid;
TranscriptInfoWidget.prototype.getTfbsGrid = GeneInfoWidget.prototype.getTfbsGrid;
TranscriptInfoWidget.prototype.getMirnaTargetGrid = GeneInfoWidget.prototype.getMirnaTargetGrid;
TranscriptInfoWidget.prototype.getProteinFeaturesGrid = GeneInfoWidget.prototype.getProteinFeaturesGrid;

function TranscriptInfoWidget(targetId, species, args){
	if (args == null){
		args = new Object();
	}
	args.title = "Transcript";
	InfoWidget.prototype.constructor.call(this, targetId, species, args);
};

TranscriptInfoWidget.prototype.getdataTypes = function (){
	//Abstract method
	return dataTypes=[
	            { text: "Genomic", children: [
	                 { text: "Information"},
	                 { text: "Gene"},
	                 { text: "Exons"},
	                 { text: "Xrefs"}
	            ] },
	            { text: "Functional information", children: [
	                  { text: "GO"},
	                  { text: "Reactome"},
	                  { text: "Interpro"}
	            ] },
	            { text: "Variation", children: [
	                  { text: "SNPs"},
	                  { text: "Mutations"}
	            ] },
	            { text: "Regulatory", children: [
	                  { text: "TFBS"},
	                  { text: "miRNA targets"}                   
	            ]},
	            { text:"Protein", children: [
	                  { text: "Features"},//protein profile
	                  { text: "3D structure"}
	            ]}	            
	        ];
};
TranscriptInfoWidget.prototype.optionClick = function (item){
	//Abstract method
	if (item.leaf){
		if(this.panel.getComponent(1)!=null){
			this.panel.getComponent(1).hide();
			this.panel.remove(1,false);
		}
		switch (item.text){
			case "Information": this.panel.add(this.getInfoPanel(this.data).show()); break;
			case "Gene": this.panel.add(this.getGenePanel(this.data.gene).show());  break;
			case "Exons": this.panel.add(this.getExonsGrid(this.data.exons).show());  break;
			case "Xrefs": this.panel.add(this.getXrefGrid([this.data], "Xref", 'dbName').show());  break;
			case "GO": this.panel.add(this.getXrefGrid([this.data], "GO").show());  break;
			case "Interpro": this.panel.add(this.getXrefGrid([this.data], "Interpro").show());  break;
			case "Reactome": this.panel.add(this.getXrefGrid([this.data], "Reactome").show());  break;
			case "SNPs": this.panel.add(this.getSnpsGrid(this.data.snps).show());  break;
			case "Mutations": this.panel.add(this.getMutationsGrid(this.data.mutations).show());  break;
			case "TFBS": this.panel.add(this.getTfbsGrid(this.data.tfbs).show());  break;
			case "miRNA targets": this.panel.add(this.getMirnaTargetGrid(this.data.mirnaTargets).show());  break;
			case "Features": this.panel.add(this.getProteinFeaturesGrid(this.data.proteinFeatures).show());  break;
			case "3D structure": this.panel.add(this.get3Dprotein(this.data.snps).show());  break;
		}
	}
};

TranscriptInfoWidget.prototype.getInfoPanel = function(data){
	if(data==null){
		return this.notFoundPanel;
	}
	if(this.infoPanel==null){
		
    	var tpl = this.getTranscriptTemplate();
    	
		this.infoPanel = Ext.create('Ext.panel.Panel',{
			title:"Information",
			border:false,
			cls:'ocb-border-left-lightgrey',
			flex:3,    
			bodyPadding:10,
			autoScroll:true,
			data:data,//para el template
			tpl:tpl
		});
	}
	return this.infoPanel;
};


TranscriptInfoWidget.prototype.getExonsGrid = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.exonsGrid==null){

    	var tpl = this.getExonTemplate();
    	
    	var panels = [];
    	for ( var i = 0; i < data.length; i++) {	
			var exonPanel = Ext.create('Ext.container.Container',{
				padding:5,
				data:data[i],
				tpl:tpl
			});
			panels.push(exonPanel);
    	}
		this.exonsGrid = Ext.create('Ext.panel.Panel',{
			title:"Exons ("+i+")",
	        border:false,
	        cls:'ocb-border-left-lightgrey',
			flex:3,
			bodyPadding:5,
			autoScroll:true,
			items:panels
		});
    }
    return this.exonsGrid;
};



//TODO hay muchos y tarda
TranscriptInfoWidget.prototype.getSnpsGrid = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.snpsGrid==null){
    	var groupField = '';
    	var modelName = 'SNPs';
	    var fields = ['chromosome','start','end','name',"strand","alleleString","displaySoConsequence"];
		var columns = [
		               	{header : 'Name',dataIndex: 'name',flex:2},
		               	{header : 'Location: chr:start-end (strand)', xtype:'templatecolumn', tpl:'{chromosome}:{start}-{end} ({strand})',flex:2},
						{header : 'Alleles',dataIndex: 'alleleString',flex:0.7},
						{header : 'Most severe SO term',dataIndex: 'displaySoConsequence',flex:2}
		             ];
		this.snpsGrid = this.doGrid(columns,fields,modelName,groupField);
		this.snpsGrid.store.loadData(data);
    }
    return this.snpsGrid;
};

TranscriptInfoWidget.prototype.getMutationsGrid = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.mutationsGrid==null){
    	var groupField = '';
    	var modelName = 'Mutations';
	    var fields = ["chromosome","start","end","mutationAa","mutationCds","primaryHistology","source"];
		var columns = [
		                {header : 'Mutation AA',dataIndex: 'mutationAa',flex:1},
		               	{header : 'Mutation CDS',dataIndex: 'mutationCds',flex:1.5},
		               	{header : 'Location: chr:start-end', xtype:'templatecolumn', tpl:'{chromosome}:{start}-{end}',flex:1.7},
						{header : 'Primary histology',dataIndex: 'primaryHistology',flex:1},
						{header : 'Source',dataIndex: 'source',flex:1}
		             ];
		this.mutationsGrid = this.doGrid(columns,fields,modelName,groupField);
		this.mutationsGrid.store.loadData(data);
    }
    return this.mutationsGrid;
};


TranscriptInfoWidget.prototype.getData = function (){
	var _this = this;
	this.panel.disable();
	this.panel.setLoading("Getting information...");
//	category, subcategory, query, resource, callbackFunction

    CellBaseManager.get({
        species:this.species,
        category:'feature',
        subCategory:'transcript',
        query:this.query,
        resource:"info",
        success:function(data){
            _this.dataReceived(data.response[0].result[0].transcripts);
        }
    });

};
TranscriptInfoWidget.prototype.dataReceived = function (data){
	this.data=data;
	console.log(this.data);
	this.optionClick({"text":"Information","leaf":"true"});
	this.panel.enable();
	this.panel.setLoading(false);
};




TranscriptOrangeInfoWidget.prototype.draw = InfoWidget.prototype.draw;
TranscriptOrangeInfoWidget.prototype.render = InfoWidget.prototype.render;
TranscriptOrangeInfoWidget.prototype.getTreePanel = InfoWidget.prototype.getTreePanel;
TranscriptOrangeInfoWidget.prototype.checkDataTypes = InfoWidget.prototype.checkDataTypes;
TranscriptOrangeInfoWidget.prototype.doGrid = InfoWidget.prototype.doGrid;
TranscriptOrangeInfoWidget.prototype.getGeneTemplate = InfoWidget.prototype.getGeneTemplate;
TranscriptOrangeInfoWidget.prototype.getTranscriptTemplate = InfoWidget.prototype.getTranscriptTemplate;
TranscriptOrangeInfoWidget.prototype.getExonTemplate = InfoWidget.prototype.getExonTemplate;
//shared with gene
//TranscriptOrangeInfoWidget.prototype.get3Dprotein = GeneInfoWidget.prototype.get3Dprotein;
TranscriptOrangeInfoWidget.prototype.getGenePanel = GeneOrangeInfoWidget.prototype.getGenePanel;
TranscriptOrangeInfoWidget.prototype.getXrefGrid = GeneOrangeInfoWidget.prototype.getXrefGrid;
//TranscriptOrangeInfoWidget.prototype.getTfbsGrid = GeneOrangeInfoWidget.prototype.getTfbsGrid;
//TranscriptOrangeInfoWidget.prototype.getMirnaTargetGrid = GeneOrangeInfoWidget.prototype.getMirnaTargetGrid;
//TranscriptOrangeInfoWidget.prototype.getProteinFeaturesGrid = GeneInfoWidget.prototype.getProteinFeaturesGrid;

function TranscriptOrangeInfoWidget(targetId, species, args){
	if (args == null){
		args = new Object();
	}
	args.title = "Transcript";
	InfoWidget.prototype.constructor.call(this, targetId, species, args);
};

TranscriptOrangeInfoWidget.prototype.getdataTypes = function (){
	//Abstract method
	return dataTypes=[
	            { text: "Genomic", children: [
	                 { text: "Information"},
	                 { text: "Gene"},
	                 { text: "Exons"}
	            ] },
	            { text: "Functional information", children: [
	                  { text: "GO"},
	                  { text: "KEGG"},
	                  { text: "Interpro"}
	            ] }	            
	        ];
};
TranscriptOrangeInfoWidget.prototype.optionClick = function (item){
	//Abstract method
	if (item.leaf){
		if(this.panel.getComponent(1)!=null){
			this.panel.getComponent(1).hide();
			this.panel.remove(1,false);
		}
		switch (item.text){
			case "Information": this.panel.add(this.getInfoPanel(this.data).show()); break;
			case "Gene": this.panel.add(this.getGenePanel(this.data.gene).show());  break;
			case "Exons": this.panel.add(this.getExonsGrid(this.data.exons).show());  break;
			case "GO": this.panel.add(this.getXrefGrid(this.data.go, "GO").show());  break;
			case "Interpro": this.panel.add(this.getXrefGrid(this.data.interpro, "Interpro").show());  break;
			case "KEGG": this.panel.add(this.getXrefGrid(this.data.kegg, "KEGG").show());  break;
//			case "SNPs": this.panel.add(this.getSnpsGrid(this.data.snps).show());  break;
//			case "Mutations": this.panel.add(this.getMutationsGrid(this.data.mutations).show());  break;
//			case "TFBS": this.panel.add(this.getTfbsGrid(this.data.tfbs).show());  break;
//			case "miRNA targets": this.panel.add(this.getMirnaTargetGrid(this.data.mirnaTargets).show());  break;
//			case "Features": this.panel.add(this.getProteinFeaturesGrid(this.data.proteinFeatures).show());  break;
//			case "3D structure": this.panel.add(this.get3Dprotein(this.data.snps).show());  break;
		}
	}
};

TranscriptOrangeInfoWidget.prototype.getInfoPanel = function(data){
	if(data==null){
		return this.notFoundPanel;
	}
	if(this.infoPanel==null){
		
    	var tpl = this.getTranscriptTemplate();
    	
		this.infoPanel = Ext.create('Ext.panel.Panel',{
			title:"Information",
			border:false,
			cls:'panel-border-left',
			flex:3,    
			bodyPadding:10,
			autoScroll:true,
			data:data,//para el template
			tpl:tpl
		});
	}
	return this.infoPanel;
};


TranscriptOrangeInfoWidget.prototype.getExonsGrid = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.exonsGrid==null){

    	var tpl = this.getExonTemplate();
    	
    	var panels = [];
    	for ( var i = 0; i < data.length; i++) {	
			var exonPanel = Ext.create('Ext.container.Container',{
				padding:5,
				data:data[i],
				tpl:tpl
			});
			panels.push(exonPanel);
    	}
		this.exonsGrid = Ext.create('Ext.panel.Panel',{
			title:"Exons ("+i+")",
	        border:false,
	        cls:'panel-border-left',
			flex:3,
			bodyPadding:5,
			autoScroll:true,
			items:panels
		});
    }
    return this.exonsGrid;
};



//TODO hay muchos y tarda
TranscriptOrangeInfoWidget.prototype.getSnpsGrid = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.snpsGrid==null){
    	var groupField = '';
    	var modelName = 'SNPs';
	    var fields = ['chromosome','start','end','name',"strand","alleleString","displaySoConsequence"];
		var columns = [
		               	{header : 'Name',dataIndex: 'name',flex:2},
		               	{header : 'Location: chr:start-end (strand)', xtype:'templatecolumn', tpl:'{chromosome}:{start}-{end} ({strand})',flex:2},
						{header : 'Alleles',dataIndex: 'alleleString',flex:0.7},
						{header : 'Most severe SO term',dataIndex: 'displaySoConsequence',flex:2}
		             ];
		this.snpsGrid = this.doGrid(columns,fields,modelName,groupField);
		this.snpsGrid.store.loadData(data);
    }
    return this.snpsGrid;
};

TranscriptOrangeInfoWidget.prototype.getMutationsGrid = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
    if(this.mutationsGrid==null){
    	var groupField = '';
    	var modelName = 'Mutations';
	    var fields = ["chromosome","start","end","mutationAa","mutationCds","primaryHistology","source"];
		var columns = [
		                {header : 'Mutation AA',dataIndex: 'mutationAa',flex:1},
		               	{header : 'Mutation CDS',dataIndex: 'mutationCds',flex:1.5},
		               	{header : 'Location: chr:start-end', xtype:'templatecolumn', tpl:'{chromosome}:{start}-{end}',flex:1.7},
						{header : 'Primary histology',dataIndex: 'primaryHistology',flex:1},
						{header : 'Source',dataIndex: 'source',flex:1}
		             ];
		this.mutationsGrid = this.doGrid(columns,fields,modelName,groupField);
		this.mutationsGrid.store.loadData(data);
    }
    return this.mutationsGrid;
};


TranscriptOrangeInfoWidget.prototype.getData = function (){
	var _this = this;
	this.panel.disable();
	this.panel.setLoading("Getting information...");
//	category, subcategory, query, resource, callbackFunction
	
	var cellBaseManager = new CellBaseManager(this.species);
	cellBaseManager.success.addEventListener(function(sender,data){
		_this.dataReceived(JSON.parse(data.result));//TODO
	});
	cellBaseManager.get("feature","transcript", this.query, "fullinfo");
};
TranscriptOrangeInfoWidget.prototype.dataReceived = function (data){
	this.data=data[0];
	console.log(this.data);
	this.optionClick({"text":"Information","leaf":"true"});
	this.panel.enable();
	this.panel.setLoading(false);
};




VCFVariantInfoWidget.prototype.draw = InfoWidget.prototype.draw;
VCFVariantInfoWidget.prototype.render = InfoWidget.prototype.render;
VCFVariantInfoWidget.prototype.getTreePanel = InfoWidget.prototype.getTreePanel;
VCFVariantInfoWidget.prototype.checkDataTypes = InfoWidget.prototype.checkDataTypes;
VCFVariantInfoWidget.prototype.doGrid = InfoWidget.prototype.doGrid;
VCFVariantInfoWidget.prototype.getVCFVariantTemplate = InfoWidget.prototype.getVCFVariantTemplate;
VCFVariantInfoWidget.prototype.getVariantEffectTemplate = InfoWidget.prototype.getVariantEffectTemplate;

function VCFVariantInfoWidget(targetId, species, args){
	if (args == null){
		args = new Object();
	}
	args.title = "VCF variant Info";
	InfoWidget.prototype.constructor.call(this, targetId, species, args);
};

VCFVariantInfoWidget.prototype.getdataTypes = function (){
	return dataTypes=[
	            { text: "Genomic", children: [
	                { text: "Information"},
	                { text: "Variant effect"},
	                { text: "Header"},
	                { text: "Samples"}
	            ] }
	        ];
};
VCFVariantInfoWidget.prototype.optionClick = function (item){
	//Abstract method
	if (item.leaf){
		if(this.panel.getComponent(1)!=null){
			this.panel.getComponent(1).hide();
			this.panel.remove(1,false);
		}
		switch (item.text){
			case "Information":  this.panel.add(this.getInfoPanel(this.data.feature).show()); break;
			case "Variant effect":this.panel.add(this.getEffectPanel(this.data.consequenceType).show()); break;
			case "Header":this.panel.add(this.getHeaderPanel(this.data.header).show()); break;
			case "Samples":this.panel.add(this.getSamplesGrid(this.data.feature.sampleData,this.data.samples,this.data.feature.format).show()); break;
			case "Population": break;
		}
	}
};

VCFVariantInfoWidget.prototype.getInfoPanel = function(data){
	if(data==null){
		return this.notFoundPanel;
	}
    if(this.infoPanel==null){

    	var tpl = this.getVCFVariantTemplate();

		this.infoPanel = Ext.create('Ext.panel.Panel',{
			title:"Information",
	        border:false,
	        cls:'ocb-border-left-lightgrey',
			flex:3,    
			bodyPadding:10,
			data:data,
			tpl:tpl
		});

    }
    return this.infoPanel;
};

VCFVariantInfoWidget.prototype.getEffectPanel = function(data){
	if(data.length<=0){
		return this.notFoundPanel;
	}
	for ( var i = 0; i < data.length; i++) {
		data[i].consequence = data[i].consequenceType+" - "+data[i].consequenceTypeObo;
		if(data[i].featureName == ""){data[i].featureName="-";}
		if(data[i].geneId == ""){data[i].geneId="-";}
		if(data[i].transcriptId == ""){data[i].transcriptId="-";}
		if(data[i].featureBiotype == ""){data[i].featureBiotype="-";}
		if(data[i].aaPosition == ""){data[i].aaPosition="-";}
		if(data[i].aminoacidChange == ""){data[i].aminoacidChange="-";}

	}

    if(this.effectGrid==null){
    	var groupField = 'consequence';
    	var modelName = "effectGridModel";
    	var fields = ['featureName','geneId','transcriptId','featureBiotype','aaPosition','aminoacidChange','consequence'];
    	var columns = [
    	               {header : 'Feature',dataIndex: 'featureName',flex:1},
    	               {header : 'Gene Id',dataIndex: 'geneId',flex:1.5},
    	               {header : 'Transcript Id',dataIndex: 'transcriptId',flex:1.5},
    	               {header : 'Feat.Biotype',dataIndex: 'featureBiotype',flex:1},
    	               {header : 'aa Position',dataIndex: 'aaPosition',flex:1},
    	               {header : 'aa Change',dataIndex: 'aminoacidChange',flex:1}
    	               ];
    	this.effectGrid = this.doGrid(columns,fields,modelName,groupField);
    	this.effectGrid.store.loadData(data);
    }
    return this.effectGrid;
	
//    if(this.effectPanel==null){
//    	var tpl = this.getVariantEffectTemplate();
//    	//sort by consequenceTypeObo
//    	data.sort(function(a,b){
//    		if(a.consequenceTypeObo == b.consequenceTypeObo){return 0;}
//    		return (a.consequenceTypeObo < b.consequenceTypeObo) ? -1 : 1;
//    	});
//    	
//    	
//    	var panels = [];
//    	for ( var i = 0; i < data.length; i++) {
//			var transcriptPanel = Ext.create('Ext.container.Container',{
//				padding:5,
//				data:data[i],
//				tpl:tpl
//			});
//			panels.push(transcriptPanel);
//    	}
//		this.effectPanel = Ext.create('Ext.panel.Panel',{
//			title:"Effects ("+i+")",
//			border:false,
//			cls:'ocb-border-left-lightgrey',
//			flex:3,    
//			bodyPadding:5,
//			autoScroll:true,
//			items:panels
//		});
//    }
//    return this.effectPanel;
};

VCFVariantInfoWidget.prototype.getHeaderPanel = function(data){
	if(data==""){
		return this.notFoundPanel;
	}
    if(this.headerPanel==null){

		this.headerPanel = Ext.create('Ext.panel.Panel',{
			title:"Information",
	        border:false,
	        cls:'ocb-border-left-lightgrey',
			flex:3,    
			bodyPadding:10,
			html:data
		});

    }
    return this.headerPanel;
};

VCFVariantInfoWidget.prototype.getSamplesGrid = function(samplesData,samples,format){
	var sData = samplesData.split("\t").slice(9);
	if(sData.length<=0){
		return this.notFoundPanel;
	}
	var data = new Array(samples.length);
	for ( var i = 0, li = data.length; i < li; i++) {
		data[i] = {id:samples[i],info:sData[i]};
	}
	
    if(this.samplesGrid==null){
    	var groupField = '';
    	var modelName = 'VCF samples';
	    var fields = ["id","info"];
		var columns = [
		                {header : 'Identifier',dataIndex: 'id',flex:1},
		                {header : format,dataIndex: 'info',flex:5}
		             ];
		this.samplesGrid = this.doGrid(columns,fields,modelName,groupField);
		this.samplesGrid.store.loadData(data);
    }
    return this.samplesGrid;
};

VCFVariantInfoWidget.prototype.getData = function (){
	var _this = this;
	this.panel.disable();
	this.panel.setLoading("Getting information...");
	var query = this.feature.chromosome+":"+this.feature.start+":"+this.feature.reference+":"+this.feature.alternate;
    CellBaseManager.get({
        host : 'http://ws-beta.bioinfo.cipf.es/cellbase/rest',
        version : 'v2',
        species:Utils.getSpeciesCode(this.species.text).substring(0,3),
        category:'genomic',
        subCategory:'variant',
        query:query,
        resource:'consequence_type',
        success:function(data){
            _this.dataReceived(data);
        }
    });
};

VCFVariantInfoWidget.prototype.dataReceived = function (data){
    debugger
	this.data = new Object();
	this.data["header"] = this.adapter.header;
	this.data["samples"] = this.adapter.samples;
	this.data["feature"] = this.feature;
	this.data["consequenceType"] = data;
	this.optionClick({"text":"Information","leaf":"true"});
	this.panel.enable();
	this.panel.setLoading(false);
};

function NavigationBar(args) {

    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    var _this = this;

    this.id = Utils.genId("NavigationBar");

    this.species = 'Homo sapiens';
    this.increment = 3;

    //set instantiation args, must be last
    _.extend(this, args);

    //set new region object
    this.region = new Region(this.region);

    this.currentChromosomeList = [];

    this.on(this.handlers);

    this.zoomChanging = false;

    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
};

NavigationBar.prototype = {

    render: function (targetId) {
        var _this = this;
        this.targetId = (targetId) ? targetId : this.targetId;
        if ($('#' + this.targetId).length < 1) {
            console.log('targetId not found in DOM');
            return;
        }

        var navgationHtml = '' +
            '<div class="btn-toolbar" role="toolbar">' +
            '   <div class="btn-group btn-group-xs">' +
            '       <button id="restoreDefaultRegionButton" class="btn btn-default" type="button"><span class="glyphicon glyphicon-repeat"></span></button>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '       <button id="regionHistoryButton" class="btn btn-default dropdown-toggle" data-toggle="dropdown"  type="button" ><span class="glyphicon glyphicon-time"></span> <span class="caret"></button>' +
            '       <ul id="regionHistoryMenu" class="dropdown-menu" role="menu">' +
            '       </ul>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '       <button id="speciesButton" class="btn btn-default dropdown-toggle" data-toggle="dropdown"  type="button" >' +
            '           <span id="speciesText"></span>&nbsp;<span class="caret"></span>' +
            '       </button>' +
            '       <ul id="speciesMenu" class="dropdown-menu" role="menu">' +
            '       </ul>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
//            '       <div class="pull-left" style="height:22px;line-height: 22px;color:#708090">Chr&nbsp;</div>' +
            '       <button id="chromosomesButton" class="btn btn-default dropdown-toggle" data-toggle="dropdown"  type="button" >' +
            '           <span id="chromosomesText"></span>&nbsp;<span class="caret"></span>' +
            '       </button>' +
            '       <ul id="chromosomesMenu" class="dropdown-menu" role="menu">' +
            '       </ul>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs" data-toggle="buttons">' +
            '       <label id="karyotypeButton" class="btn btn-default"><input type="checkbox"><span class="ocb-icon ocb-icon-karyotype"></span></label>' +
            '       <label id="chromosomeButton" class="btn btn-default"><input type="checkbox"><span class="ocb-icon ocb-icon-chromosome"></span></label>' +
            '       <label id="regionButton" class="btn btn-default"><input type="checkbox"><span class="ocb-icon ocb-icon-region"></span></label>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs" style="margin:0px 0px 0px 15px;">' +
            '       <button id="zoomOutButton" class="btn btn-default btn-xs" type="button"><span class="glyphicon glyphicon-minus"></span></button>' +
            '       <div id="progressBarCont" class="progress pull-left" style="width:120px;height:10px;margin:5px 2px 0px 2px;background-color: #d5d5d5">' +
            '           <div id="progressBar" class="progress-bar" role="progressbar" aria-valuenow="45" aria-valuemin="0" aria-valuemax="100" style="width: 100%">' +
            '           </div>' +
            '       </div>' +
            '       <button id="zoomInButton" class="btn btn-default btn-xs" type="button"><span class="glyphicon glyphicon-plus"></span></button>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs" style="margin:0px 0px 0px 10px;">' +
            '       <div class="pull-left" style="height:22px;line-height: 22px;font-size:14px;">Window size:&nbsp;</div>' +
            '       <input id="windowSizeField" type="text" class="form-control pull-left" placeholder="Window size" style="padding:0px 4px;height:22px;width:60px">' +
            '   </div>' +
            '   <div class="btn-group" style="margin:0px 0px 0px 10px;">' +
            '       <div class="pull-left" style="height:22px;line-height: 22px;font-size:14px;">Position:&nbsp;</div>' +
            '       <div class="input-group pull-left">' +
            '           <input id="regionField" type="text" class="form-control" placeholder="region..." style="padding:0px 4px;width:160px;height:22px">' +
            '       </div>' +
            '       <button id="goButton" class="btn btn-default btn-xs" type="button">Go!</button>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '       <button id="moveFurtherLeftButton" class="btn btn-default" type="button"><span class="ocb-icon ocb-icon-arrow-w-bold"></span></button>' +
            '       <button id="moveLeftButton" class="btn btn-default" type="button"><span class="ocb-icon ocb-icon-arrow-w"></span></button>' +
            '       <button id="moveRightButton" class="btn btn-default" type="button"><span class="ocb-icon ocb-icon-arrow-e"></span></button>' +
            '       <button id="moveFurtherRightButton" class="btn btn-default" type="button"><span class="ocb-icon ocb-icon-arrow-e-bold"></span></button>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '       <button id="autoheightButton" class="btn btn-default" type="button"><span class="ocb-icon ocb-icon-track-autoheight"></span></button>' +
            '   </div>' +
            '    <div class="btn-group btn-group-xs">' +
            '       <button id="compactButton" class="btn btn-default" type="button"><span class="ocb-icon glyphicon glyphicon-compressed"></span></button>' +
            '   </div>' +
            '   <div class="btn-group pull-right">' +
            '       <div class="pull-left" style="height:22px;line-height: 22px;font-size:14px;">Search:&nbsp;</div>' +
            '       <div class="input-group pull-left">' +
            '           <input id="searchField" list="searchDataList" type="text" class="form-control" placeholder="gene, snp..." style="padding:0px 4px;height:22px;width:100px">' +
            '           <datalist id="searchDataList">' +
            '           </datalist>' +
            '       </div>' +
//            '       <ul id="quickSearchMenu" class="dropdown-menu" role="menu">' +
//            '       </ul>' +
            '       <button id="quickSearchButton" class="btn btn-default btn-xs" type="button"><span class="glyphicon glyphicon-search"></span></button>' +
            '   </div>' +
            '</div>' +
            '';


        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div id="navigation-bar" class="gv-navigation-bar unselectable">' + navgationHtml + '</div>')[0];
        $(this.targetDiv).append(this.div);


        this.restoreDefaultRegionButton = $(this.div).find('#restoreDefaultRegionButton')[0];

        this.regionHistoryButton = $(this.div).find('#regionHistoryButton')[0];
        this.regionHistoryMenu = $(this.div).find('#regionHistoryMenu')[0];

        this.speciesButton = $(this.div).find('#speciesButton')[0];
        this.speciesText = $(this.div).find('#speciesText')[0];
        this.speciesMenu = $(this.div).find('#speciesMenu')[0];

        this.chromosomesButton = $(this.div).find('#chromosomesButton')[0];
        this.chromosomesText = $(this.div).find('#chromosomesText')[0];
        this.chromosomesMenu = $(this.div).find('#chromosomesMenu')[0];

        this.karyotypeButton = $(this.div).find('#karyotypeButton')[0];
        this.chromosomeButton = $(this.div).find('#chromosomeButton')[0];
        this.regionButton = $(this.div).find('#regionButton')[0];

        this.progressBar = $(this.div).find('#progressBar')[0];
        this.progressBarCont = $(this.div).find('#progressBarCont')[0];
        this.zoomOutButton = $(this.div).find('#zoomOutButton')[0];
        this.zoomInButton = $(this.div).find('#zoomInButton')[0];

        this.regionField = $(this.div).find('#regionField')[0];
        this.goButton = $(this.div).find('#goButton')[0];

        this.moveFurtherLeftButton = $(this.div).find('#moveFurtherLeftButton');
        this.moveFurtherRightButton = $(this.div).find('#moveFurtherRightButton');
        this.moveLeftButton = $(this.div).find('#moveLeftButton');
        this.moveRightButton = $(this.div).find('#moveRightButton');

        this.autoheightButton = $(this.div).find('#autoheightButton');
        this.compactButton = $(this.div).find('#compactButton');

        this.searchField = $(this.div).find('#searchField')[0];
//        this.quickSearchMenu = $(this.div).find('#quickSearchMenu')[0];
        this.searchDataList = $(this.div).find('#searchDataList')[0];
        this.quickSearchButton = $(this.div).find('#quickSearchButton')[0];
        this.windowSizeField = $(this.div).find('#windowSizeField')[0];

        /*** ***/
        $(this.restoreDefaultRegionButton).click(function (e) {
            _this.trigger('restoreDefaultRegion:click', {clickEvent: e, sender: {}})
        });

        this._addRegionHistoryMenuItem(this.region);
        this._setChromosomeMenu();
        this._setSpeciesMenu();
        $(this.chromosomesText).text(this.region.chromosome);
        $(this.speciesText).text(this.species.text);


        $(this.karyotypeButton).click(function () {
            _this.trigger('karyotype-button:change', {selected: $(this).hasClass('active'), sender: _this});
        });
        $(this.chromosomeButton).click(function () {
            _this.trigger('chromosome-button:change', {selected: $(this).hasClass('active'), sender: _this});
        });
        $(this.regionButton).click(function () {
            _this.trigger('region-button:change', {selected: $(this).hasClass('active'), sender: _this});
        });


        $(this.zoomOutButton).click(function () {
            _this._handleZoomOutButton();
        });
        $(this.zoomInButton).click(function () {
            _this._handleZoomInButton();
        });
        $(this.progressBarCont).click(function (e) {
            var offsetX = e.clientX - $(this).offset().left;
            console.log('offsetX '+offsetX);
            console.log('e.offsetX '+ e.offsetX);
            var zoom = 100 / $(this).width() * offsetX;
            if (!_this.zoomChanging) {
                $(_this.progressBar).width(offsetX);
                _this.zoomChanging = true;
                setTimeout(function () {
                    _this._handleZoomSlider(zoom);
                    _this.zoomChanging = false;
                }, 500);
            }
        });
        $(this.regionField).val(this.region.toString());

        $(this.goButton).click(function () {
            _this._goRegion($(_this.regionField).val());
        });

        $(this.moveFurtherLeftButton).click(function () {
            _this._handleMoveRegion(10);
        });

        $(this.moveFurtherRightButton).click(function () {
            _this._handleMoveRegion(-10);
        });

        $(this.moveLeftButton).click(function () {
            _this._handleMoveRegion(1);
        });

        $(this.moveRightButton).click(function () {
            _this._handleMoveRegion(-1);
        });

        $(this.autoheightButton).click(function (e) {
            _this.trigger('autoHeight-button:click', {clickEvent: e, sender: _this});
        });

        $(this.compactButton).click(function (e) {
            $(".ocb-compactable").toggle();
        });


//        var speciesCode = Utils.getSpeciesCode(this.species.text).substr(0, 3);
//        var url = CellBaseManager.url({
//            host: 'http://ws.bioinfo.cipf.es/cellbase/rest',
//            species: speciesCode,
//            version: 'latest',
//            category: 'feature',
//            subCategory: 'id',
//            query: '%QUERY',
//            resource: 'starts_with',
//            params: {
//                of: 'json'
//            }
//        });

//        $(this.div).find('#searchField').typeahead({
//            remote: {
//                url: url,
//                filter: function (parsedResponse) {
//                    return parsedResponse[0];
//                }
//            },
//            valueKey: 'displayId',
//            limit: 20
//        }).bind('typeahead:selected', function (obj, datum) {
//                _this._goFeature(datum.displayId);
//            });
//
//        $(this.div).find('#searchField').parent().find('.tt-hint').addClass('form-control tt-query').css({
//            height: '22px'
//        });
//        $(this.div).find('.tt-dropdown-menu').css({
//            'font-size': '14px'
//        });

        var lastQuery = '';
        $(this.searchField).bind("keyup", function (event) {
            var query = $(this).val();
            if (query.length > 3 && lastQuery !== query && event.which !== 13) {
                _this._setQuickSearchMenu(query);
                lastQuery = query;
            }
            if (event.which === 13) {
                var item = _this.quickSearchDataset[query];
                _this.trigger('quickSearch:select', {item: item, sender: _this});
            }
        });

        $(this.quickSearchButton).click(function () {
            var query = $(this.searchField).val();
            var item = _this.quickSearchDataset[query];
            _this.trigger('quickSearch:go', {item: item, sender: _this});
        });

        $(this.windowSizeField).val(this.region.length());
        $(this.windowSizeField).bind("keyup", function (event) {
            var value = $(this).val();
            var pattern = /^([0-9])+$/;
            if (event.which === 13 && pattern.test(value)) {
                var regionSize = parseInt(value);
                var haflRegionSize = Math.floor(regionSize / 2);
                var start = _this.region.center() - haflRegionSize;
                var end = _this.region.center() + haflRegionSize;
                _this.region.start = start;
                _this.region.end = end;
                _this.trigger('region:change', {region: _this.region});
            }
        });
        this.rendered = true;
    },

    _addRegionHistoryMenuItem: function (region) {
        var _this = this;
        var menuEntry = $('<li role="presentation"><a tabindex="-1" role="menuitem">' + region.toString() + '</a></li>')[0];
        $(this.regionHistoryMenu).append(menuEntry);
        $(menuEntry).click(function () {
            _this.region.parse($(this).text());
            $(_this.chromosomesText).text(_this.region.chromosome);
            $(_this.regionField).val(_this.region.toString());
            _this.trigger('region:change', {region: _this.region, sender: _this});
            console.log($(this).text());
        });
    },

    _setQuickSearchMenu: function (query) {
        if (typeof this.quickSearchResultFn === 'function') {
            $(this.searchDataList).empty();
            this.quickSearchDataset = {};
            var items = this.quickSearchResultFn(query);
            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                var itemKey = item;
                if ($.type(this.quickSearchDisplayKey) === "string") {
                    itemKey = item[this.quickSearchDisplayKey];
                }
                this.quickSearchDataset[itemKey] = item;
                var menuEntry = $('<option value="' + itemKey + '">')[0];
                $(this.searchDataList).append(menuEntry);
            }
        } else {
            console.log('the quickSearchResultFn function is not valid');
        }
    },

    _setChromosomeMenu: function () {
        var _this = this;

        $(this.chromosomesMenu).empty();

        //find species object
        var list = [];
        for (var i in this.availableSpecies.items) {
            for (var j in this.availableSpecies.items[i].items) {
                var species = this.availableSpecies.items[i].items[j];
                if (species.text === this.species.text) {
                    list = species.chromosomes;
                    break;
                }
            }
        }

        this.currentChromosomeList = list;
        //add bootstrap elements to the menu
        for (var i in list) {
            var menuEntry = $('<li role="presentation"><a tabindex="-1" role="menuitem">' + list[i] + '</a></li>')[0];
            $(this.chromosomesMenu).append(menuEntry);
            $(menuEntry).click(function () {
                _this.region.chromosome = $(this).text();
                $(_this.chromosomesText).text($(this).text());
                $(_this.regionField).val(_this.region.toString());
                _this._addRegionHistoryMenuItem(_this.region);
                _this.trigger('region:change', {region: _this.region, sender: _this});
                console.log($(this).text());
            });
        }
    },

    _setSpeciesMenu: function () {
        var _this = this;

        var createEntry = function (species) {
            var menuEntry = $('<li role="presentation"><a tabindex="-1" role="menuitem">' + species.text + '</a></li>')[0];
            $(_this.speciesMenu).append(menuEntry);
            $(menuEntry).click(function () {
                _this.species = species;
                $(_this.speciesText).text($(this).text());
                _this._setChromosomeMenu();
                _this.trigger('species:change', {species: species, sender: _this});
            });
        };
        //find species object
        var list = [];
        for (var i in this.availableSpecies.items) {
            for (var j in this.availableSpecies.items[i].items) {
                var species = this.availableSpecies.items[i].items[j];
                createEntry(species);
            }
        }
    },
    _goRegion: function (value) {
        var reg = new Region();
        if (!reg.parse(value) || reg.start < 0 || reg.end < 0 || _.indexOf(this.currentChromosomeList, reg.chromosome) == -1) {
            $(this.regionField).css({opacity: 0.0});
            $(this.regionField).animate({opacity: 1}, 700);
        } else {
            this.region.load(reg);
            $(this.windowSizeField).val(this.region.length());
            $(this.chromosomesText).text(this.region.chromosome);
            this._addRegionHistoryMenuItem(this.region);
            this.trigger('region:change', {region: this.region, sender: this});
        }
    },

    _handleZoomOutButton: function () {
        this._handleZoomSlider(Math.max(0, this.zoom - 1));
    },
    _handleZoomSlider: function (value) {
        this.zoom = value;
        this.trigger('zoom:change', {zoom: this.zoom, sender: this});
    },
    _handleZoomInButton: function () {
        this._handleZoomSlider(Math.min(100, this.zoom + 1));
    },

    _handleMoveRegion: function (positions) {
        var pixelBase = (this.width - this.svgCanvasWidthOffset) / this.region.length();
        var disp = Math.round((positions * 10) / pixelBase);
        this.region.start -= disp;
        this.region.end -= disp;
        $(this.regionField).val(this.region.toString());
        this.trigger('region:move', {region: this.region, disp: disp, sender: this});
    },

    setVisible: function (obj) {
        for (key in obj) {
            var query = $(this.div).find('#' + key);
            if (obj[key]) {
                query.show();
            } else {
                query.hide();
            }
        }
    },

    setRegion: function (region) {
        this.region.load(region);
        $(this.chromosomesText).text(this.region.chromosome);
        $(this.regionField).val(this.region.toString());
        $(this.windowSizeField).val(this.region.length());
        this._addRegionHistoryMenuItem(region);
    },
    moveRegion: function (region) {
        this.region.load(region);
        $(this.chromosomesText).text(this.region.chromosome);
        $(this.regionField).val(this.region.toString());
    },

    setWidth: function (width) {
        this.width = width;
    },
    setZoom: function (zoom) {
        this.zoom = zoom;
        $(this.progressBar).css("width", this.zoom + '%');
    },
    draw: function () {
        if (!this.rendered) {
            console.info(this.id + ' is not rendered yet');
            return;
        }
    }
}
function ChromosomePanel(args) {

    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    this.id = Utils.genId('ChromosomePanel');

    this.pixelBase;
    this.species = 'hsapiens';
    this.width = 600;
    this.height = 75;
    this.collapsed = false;
    this.collapsible = false;

    //set instantiation args, must be last
    _.extend(this, args);

    //set own region object
    this.region = new Region(this.region);


    this.lastChromosome = "";
    this.data;

    this.on(this.handlers);

    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
};

ChromosomePanel.prototype = {
    show: function () {
        $(this.div).css({display: 'block'});
    },
    hide: function () {
        $(this.div).css({display: 'none'});
    },
    showContent: function () {
        $(this.svg).css({display: 'inline'});
        this.collapsed = false;
        $(this.collapseDiv).removeClass('active');
        $(this.collapseDiv).children().first().removeClass('glyphicon-plus');
        $(this.collapseDiv).children().first().addClass('glyphicon-minus');
    },
    hideContent: function () {
        $(this.svg).css({display: 'none'});
        this.collapsed = true;
        $(this.collapseDiv).addClass('active');
        $(this.collapseDiv).children().first().removeClass('glyphicon-minus');
        $(this.collapseDiv).children().first().addClass('glyphicon-plus');
    },
    setVisible: function (bool) {
        if (bool) {
            $(this.div).css({display: 'block'});
        } else {
            $(this.div).css({display: 'none'});
        }
    },
    setTitle: function (title) {
        if ('titleDiv' in this) {
            $(this.titleDiv).first().html(title);
        }
    },
    setWidth: function (width) {
        this.width = width;
        this.svg.setAttribute("width", width);
//        this.tracksViewedRegion = this.width / Utils.getPixelBaseByZoom(this.zoom);

        this.clean();
        this._drawSvg(this.data);
    },

    render: function (targetId) {
        var _this = this;
        this.targetId = (targetId) ? targetId : this.targetId;
        if ($('#' + this.targetId).length < 1) {
            console.log('targetId not found in DOM');
            return;
        }
        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div id="chromosome-panel"></div>')[0];
        $(this.targetDiv).append(this.div);

        if ('title' in this && this.title !== '') {
            this.titleDiv = $('<div id="tl-title" class="gv-panel-title unselectable"><span style="line-height: 24px;margin-left: 5px;">' + this.title + '</span></div>')[0];
            $(this.div).append(this.titleDiv);

            if (this.collapsible == true) {
                this.collapseDiv = $('<div type="button" class="btn btn-default btn-xs pull-right" style="display:inline;margin:2px;height:20px"><span class="glyphicon glyphicon-minus"></span></div>');
                $(this.titleDiv).dblclick(function () {
                    if (_this.collapsed) {
                        _this.showContent();
                    } else {
                        _this.hideContent();
                    }
                });
                $(this.collapseDiv).click(function () {
                    if (_this.collapsed) {
                        _this.showContent();
                    } else {
                        _this.hideContent();
                    }
                });
                $(this.titleDiv).append(this.collapseDiv);
            }

        }

        this.svg = SVG.init(this.div, {
            "width": this.width,
            "height": this.height
        });
        $(this.div).addClass('unselectable');

        this.colors = {gneg: "#eeeeee", stalk: "#666666", gvar: "#CCCCCC", gpos25: "silver", gpos33: "lightgrey", gpos50: "gray", gpos66: "dimgray", gpos75: "darkgray", gpos100: "black", gpos: "gray", acen: "blue", clementina: '#ffc967'};
        this.rendered = true;
    },

    setSpecies: function (species) {
        this.species = species;
    },
    clean: function () {
        $(this.svg).empty();
    },
    draw: function () {
        if (!this.rendered) {
            console.info(this.id + ' is not rendered yet');
            return;
        }
        var _this = this;

        this.clean();

        CellBaseManager.get({
            species: this.species,
            category: 'genomic',
            subCategory: 'chromosome',
            query: this.region.chromosome,
            resource: 'info',
            success: function (data) {
                _this.data = data.response[0].result.chromosomes;
                _this.data.cytobands.sort(function (a, b) {
                    return (a.start - b.start);
                });
                _this._drawSvg(_this.data);
            }
        });

        this.lastChromosome = this.region.chromosome;


        if (this.collapsed) {
            _this.hideContent();
        }
    },
    _drawSvg: function (chromosome) {
        // This method uses less svg elements
        var _this = this;
        var offset = 20;
        var group = SVG.addChild(_this.svg, "g", {"cursor": "pointer"});
        this.chromosomeLength = chromosome.size;
        this.pixelBase = (this.width - 40) / this.chromosomeLength;

        /**/
        /*Draw Chromosome*/
        /**/
        var backrect = SVG.addChild(group, 'rect', {
            'x': offset,
            'y': 4,
            'width': this.width - 40 + 1,
            'height': 22,
            'fill': '#555555'
        });

        var cytobandsByStain = {};
        var textDrawingOffset = offset;
        for (var i = 0; i < chromosome.cytobands.length; i++) {
            var cytoband = chromosome.cytobands[i];
            cytoband.pixelStart = cytoband.start * this.pixelBase;
            cytoband.pixelEnd = cytoband.end * this.pixelBase;
            cytoband.pixelSize = cytoband.pixelEnd - cytoband.pixelStart;

            if (typeof cytobandsByStain[cytoband.stain] == 'undefined') {
                cytobandsByStain[cytoband.stain] = [];
            }
            cytobandsByStain[cytoband.stain].push(cytoband);

            var middleX = textDrawingOffset + (cytoband.pixelSize / 2);
            var textY = 28;
            var text = SVG.addChild(group, "text", {
                "x": middleX,
                "y": textY,
                "font-size": 10,
                "transform": "rotate(90, " + middleX + ", " + textY + ")",
                "fill": "black"
            });
            text.textContent = cytoband.name;
            textDrawingOffset += cytoband.pixelSize;
        }

        for (var cytobandStain in cytobandsByStain) {
            var cytobands_d = '';
            if (cytobandStain != 'acen') {
                for (var j = 0; j < cytobandsByStain[cytobandStain].length; j++) {
                    var cytoband = cytobandsByStain[cytobandStain][j];
                    cytobands_d += 'M' + (cytoband.pixelStart + offset + 1) + ',15' + ' L' + (cytoband.pixelEnd + offset) + ',15 ';
                }
                var path = SVG.addChild(group, 'path', {
                    "d": cytobands_d,
                    "stroke": this.colors[cytobandStain],
//                "stroke": 'red',
                    "stroke-width": 20,
                    "fill": 'none'
                });
            }
        }

        if(typeof cytobandsByStain['acen'] !== 'undefined'){
            var firstStain = cytobandsByStain['acen'][0];
            var lastStain = cytobandsByStain['acen'][1];
            var backrect = SVG.addChild(group, 'rect', {
                'x': (firstStain.pixelStart + offset + 1),
                'y': 4,
                'width': (lastStain.pixelEnd + offset) - (firstStain.pixelStart + offset + 1),
                'height': 22,
                'fill': 'white'
            });
            var firstStainXStart = (firstStain.pixelStart + offset + 1);
            var firstStainXEnd = (firstStain.pixelEnd + offset);
            var lastStainXStart = (lastStain.pixelStart + offset + 1);
            var lastStainXEnd = (lastStain.pixelEnd + offset);
            var path = SVG.addChild(group, 'path', {
                'd': 'M' + firstStainXStart + ',4' + ' L' + (firstStainXEnd - 5) + ',4 ' + ' L' + firstStainXEnd + ',15 ' + ' L ' + (firstStainXEnd - 5) + ',26 ' + ' L ' + firstStainXStart + ',26 z',
                'fill': this.colors['acen']
            });
            var path = SVG.addChild(group, 'path', {
                'd': 'M' + lastStainXStart + ',15' + ' L' + (lastStainXStart + 5) + ',4 ' + ' L' + lastStainXEnd + ',4 ' + ' L ' + lastStainXEnd + ',26 ' + ' L ' + (lastStainXStart + 5) + ',26 z',
                'fill': this.colors['acen']
            });
        }


        /**/
        /* Resize elements and events*/
        /**/
        var status = '';
        var centerPosition = _this.region.center();
        var pointerPosition = (centerPosition * _this.pixelBase) + offset;
        $(this.svg).on('mousedown', function (event) {
            status = 'setRegion';
        });

        // selection box, will appear when selection is detected
        var selBox = SVG.addChild(this.svg, "rect", {
            "x": 0,
            "y": 2,
            "stroke-width": "2",
            "stroke": "deepskyblue",
            "opacity": "0.5",
            "fill": "honeydew"
        });


        var positionBoxWidth = _this.region.length() * _this.pixelBase;
        var positionGroup = SVG.addChild(group, 'g');
        this.positionBox = SVG.addChild(positionGroup, 'rect', {
            'x': pointerPosition - (positionBoxWidth / 2),
            'y': 2,
            'width': positionBoxWidth,
            'height': _this.height - 3,
            'stroke': 'orangered',
            'stroke-width': 2,
            'opacity': 0.5,
            'fill': 'navajowhite',
            'cursor': 'move'
        });
        $(this.positionBox).on('mousedown', function (event) {
            status = 'movePositionBox';
        });


        var resizeLeft = SVG.addChild(positionGroup, 'rect', {
            'x': pointerPosition - (positionBoxWidth / 2),
            'y': 2,
            'width': 5,
            'height': _this.height - 3,
            'opacity': 0.5,
            'fill': 'orangered',
            'visibility': 'hidden'
        });
        $(resizeLeft).on('mousedown', function (event) {
            status = 'resizePositionBoxLeft';
        });

        var resizeRight = SVG.addChild(positionGroup, 'rect', {
            'x': positionBoxWidth - 5,
            'y': 2,
            'width': 5,
            'height': _this.height - 3,
            'opacity': 0.5,
            'fill': 'orangered',
            'visibility': 'hidden'
        });
        $(resizeRight).on('mousedown', function (event) {
            status = 'resizePositionBoxRight';
        });

        $(this.positionBox).off('mouseenter');
        $(this.positionBox).off('mouseleave');

        var recalculateResizeControls = function () {
            var postionBoxX = parseInt(_this.positionBox.getAttribute('x'));
            var postionBoxWidth = parseInt(_this.positionBox.getAttribute('width'));
            resizeLeft.setAttribute('x', postionBoxX - 5);
            resizeRight.setAttribute('x', (postionBoxX + postionBoxWidth));
            $(resizeLeft).css({"cursor": "ew-resize"});
            $(resizeRight).css({"cursor": "ew-resize"});
        };

        var hideResizeControls = function () {
            resizeLeft.setAttribute('visibility', 'hidden');
            resizeRight.setAttribute('visibility', 'hidden');
        };

        var showResizeControls = function () {
            resizeLeft.setAttribute('visibility', 'visible');
            resizeRight.setAttribute('visibility', 'visible');
        };

        var recalculatePositionBox = function () {
            var genomicLength = _this.region.length();
            var pixelWidth = genomicLength * _this.pixelBase;
            var x = (_this.region.start * _this.pixelBase) + 20;//20 is the margin
            _this.positionBox.setAttribute("x", x);
            _this.positionBox.setAttribute("width", pixelWidth);
        };
        var limitRegionToChromosome = function (args) {
            args.start = (args.start < 1) ? 1 : args.start;
            args.end = (args.end > _this.chromosomeLength) ? _this.chromosomeLength : args.end;
            return args;
        };

        $(positionGroup).mouseenter(function (event) {
            recalculateResizeControls();
            showResizeControls();
        });
        $(positionGroup).mouseleave(function (event) {
            hideResizeControls();
        });


        /*Remove event listeners*/
        $(this.svg).off('contextmenu');
        $(this.svg).off('mousedown');
        $(this.svg).off('mouseup');
        $(this.svg).off('mousemove');
        $(this.svg).off('mouseleave');

        //Prevent browser context menu
        $(this.svg).contextmenu(function (e) {
            e.preventDefault();
        });
        var downY, downX, moveX, moveY, lastX, increment;

        $(this.svg).mousedown(function (event) {
//            downX = (event.pageX - $(_this.svg).offset().left);
            downX = (event.clientX - $(this).parent().offset().left); //using parent offset works well on firefox and chrome. Could be because it is a div instead of svg
            selBox.setAttribute("x", downX);
            lastX = _this.positionBox.getAttribute("x");
            if (status == '') {
                status = 'setRegion'
            }
            hideResizeControls();
            $(this).mousemove(function (event) {
//                moveX = (event.pageX - $(_this.svg).offset().left);
                moveX = (event.clientX - $(this).parent().offset().left); //using parent offset works well on firefox and chrome. Could be because it is a div instead of svg
                hideResizeControls();
                switch (status) {
                    case 'resizePositionBoxLeft' :
                        var inc = moveX - downX;
                        var newWidth = parseInt(_this.positionBox.getAttribute("width")) - inc;
                        if (newWidth > 0) {
                            _this.positionBox.setAttribute("x", parseInt(_this.positionBox.getAttribute("x")) + inc);
                            _this.positionBox.setAttribute("width", newWidth);
                        }
                        downX = moveX;
                        break;
                    case 'resizePositionBoxRight' :
                        var inc = moveX - downX;SVG
                        var newWidth = parseInt(_this.positionBox.getAttribute("width")) + inc;
                        if (newWidth > 0) {
                            _this.positionBox.setAttribute("width", newWidth);
                        }
                        downX = moveX;
                        break;
                    case 'movePositionBox' :
                        var inc = moveX - downX;
                        _this.positionBox.setAttribute("x", parseInt(_this.positionBox.getAttribute("x")) + inc);
                        downX = moveX;
                        break;
                    case 'setRegion':
                    case 'selectingRegion' :
                        status = 'selectingRegion';
                        if (moveX < downX) {
                            selBox.setAttribute("x", moveX);
                        }
                        selBox.setAttribute("width", Math.abs(moveX - downX));
                        selBox.setAttribute("height", _this.height - 3);
                        break;
                }

            });
        });


        $(this.svg).mouseup(function (event) {
            $(this).off('mousemove');
            if (downX != null) {

                switch (status) {
                    case 'resizePositionBoxLeft' :
                    case 'resizePositionBoxRight' :
                    case 'movePositionBox' :
                        if (moveX != null) {
                            var w = parseInt(_this.positionBox.getAttribute("width"));
                            var x = parseInt(_this.positionBox.getAttribute("x"));

                            var pixS = x;
                            var pixE = x + w;
                            var bioS = (pixS - offset) / _this.pixelBase;
                            var bioE = (pixE - offset) / _this.pixelBase;
                            var se = limitRegionToChromosome({start:bioS,end:bioE});// returns object with start and end
                            _this.region.start = Math.round(se.start);
                            _this.region.end = Math.round(se.end);
                            recalculatePositionBox();
                            recalculateResizeControls();
                            showResizeControls();
                            _this.trigger('region:change', {region: _this.region, sender: _this});
                            recalculateResizeControls();
                            showResizeControls();
                        }
                        break;
                    case 'setRegion' :
                        if(downX > offset && downX < (_this.width - offset)){
                            var w = _this.positionBox.getAttribute("width");

                            _this.positionBox.setAttribute("x", downX - (w / 2));

                            var pixS = downX - (w / 2);
                            var pixE = downX + (w / 2);
                            var bioS = (pixS - offset) / _this.pixelBase;
                            var bioE = (pixE - offset) / _this.pixelBase;
                            var se = limitRegionToChromosome({start: bioS, end: bioE});// returns object with start and end
                            _this.region.start = Math.round(se.start);
                            _this.region.end = Math.round(se.end);
                            recalculatePositionBox();
                            _this.trigger('region:change', {region: _this.region, sender: _this});
                        }
                        break;
                    case 'selectingRegion' :
                        var bioS = (downX - offset) / _this.pixelBase;
                        var bioE = (moveX - offset) / _this.pixelBase;
                        var start = Math.min(bioS,bioE);
                        var end = Math.max(bioS,bioE);
                        var se = limitRegionToChromosome({start:start,end:end});// returns object with start and end
                        _this.region.start = parseInt(se.start);
                        _this.region.end = parseInt(se.end);
                        recalculatePositionBox();
//                        var w = Math.abs(downX - moveX);
//                        _this.positionBox.setAttribute("width", w);
//                        _this.positionBox.setAttribute("x", Math.abs((downX + moveX) / 2) - (w / 2));
                        _this.trigger('region:change', {region: _this.region, sender: _this});
                        break;
                }
                status = '';

            }
            selBox.setAttribute("width", 0);
            selBox.setAttribute("height", 0);
            downX = null;
            moveX = null;
            lastX = _this.positionBox.getAttribute("x");
        });
        $(this.svg).mouseleave(function (event) {
            $(this).off('mousemove')
            if (lastX != null) {
                _this.positionBox.setAttribute("x", lastX);
            }
            selBox.setAttribute("width", 0);
            selBox.setAttribute("height", 0);
            downX = null;
            moveX = null;
            lastX = null;
            overPositionBox = false;
            movingPositionBox = false;
            selectingRegion = false;
        });
    },
    setRegion: function (region) {//item.chromosome, item.region
        this.region.load(region);
        var needDraw = false;

        if (this.lastChromosome != this.region.chromosome) {
            needDraw = true;
        }

        //recalculate positionBox
        var genomicLength = this.region.length();
        var pixelWidth = genomicLength * this.pixelBase;
        var x = (this.region.start * this.pixelBase) + 20;//20 is the margin
        this.positionBox.setAttribute("x", x);
        this.positionBox.setAttribute("width", pixelWidth);

        if (needDraw) {
            this.draw();
        }
    }
}
function KaryotypePanel(args) {
    // Using Underscore 'extend' function to extend and add Backbone Events

    _.extend(this, Backbone.Events);

    this.id = Utils.genId('KaryotypePanel');

    this.pixelBase;
    this.species;
    this.width = 600;
    this.height = 75;
    this.collapsed = false;
    this.collapsible = true;


//set instantiation args, must be last
        _.extend(this, args);

    //set own region object
    this.region = new Region(this.region);

    this.lastSpecies = this.species;

    this.chromosomeList;
    this.data2;

    this.on(this.handlers);

    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
};

KaryotypePanel.prototype = {
    show: function () {
        $(this.div).css({display: 'block'});
    },
    hide: function () {
        $(this.div).css({display: 'none'});
    },
    showContent: function () {
        $(this.svg).css({display: 'inline'});
        this.collapsed = false;
        $(this.collapseDiv).removeClass('active');
        $(this.collapseDiv).children().first().removeClass('glyphicon-plus');
        $(this.collapseDiv).children().first().addClass('glyphicon-minus');
    },
    hideContent: function () {
        $(this.svg).css({display: 'none'});
        this.collapsed = true;
        $(this.collapseDiv).addClass('active');
        $(this.collapseDiv).children().first().removeClass('glyphicon-minus');
        $(this.collapseDiv).children().first().addClass('glyphicon-plus');
    },
    setVisible: function (bool) {
        if (bool) {
            $(this.div).css({display: 'block'});
        } else {
            $(this.div).css({display: 'none'});
        }
    },
    setTitle: function (title) {
        if ('titleDiv' in this) {
            $(this.titleDiv).children().first().html(title);
        }
    },
    setWidth: function (width) {
        this.width = width;
        this.svg.setAttribute("width", width);


        this.clean();
        this._drawSvg(this.chromosomeList, this.data2);
    },

    render: function (targetId) {
        var _this = this;
        this.targetId = (targetId) ? targetId : this.targetId;
        if ($('#' + this.targetId).length < 1) {
            console.log('targetId not found in DOM');
            return;
        }
        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div id="karyotype-panel"></div>')[0];
        $(this.targetDiv).append(this.div);

        if ('title' in this && this.title !== '') {
            this.titleDiv = $('<div id="tl-title" class="gv-panel-title unselectable"><span style="line-height: 24px;margin-left: 5px;">' + this.title + '</span></div>')[0];
            $(this.div).append(this.titleDiv);

            if(this.collapsible == true){
                this.collapseDiv = $('<div type="button" class="btn btn-default btn-xs pull-right" style="display:inline;margin:2px;height:20px"><span class="glyphicon glyphicon-minus"></span></div>');
                $(this.titleDiv).dblclick(function () {
                    if (_this.collapsed) {
                        _this.showContent();
                    } else {
                        _this.hideContent();
                    }
                });
                $(this.collapseDiv).click(function () {
                    if (_this.collapsed) {
                        _this.showContent();
                    } else {
                        _this.hideContent();
                    }
                });
                $(this.titleDiv).append(this.collapseDiv);
            }
        }

        this.svg = SVG.init(this.div, {
            "width": this.width,
            "height": this.height
        });
        this.markGroup = SVG.addChild(this.svg, "g", {"cursor": "pointer"});
        $(this.div).addClass('unselectable');

        this.colors = {gneg: "white", stalk: "#666666", gvar: "#CCCCCC", gpos25: "silver", gpos33: "lightgrey", gpos50: "gray", gpos66: "dimgray", gpos75: "darkgray", gpos100: "black", gpos: "gray", acen: "blue"};

        this.rendered = true;
    },

    setSpecies: function (species) {
        this.lastSpecies = this.species;
        this.species = species;
    },
    clean: function () {
        $(this.svg).empty();
    },
    draw: function () {
        if (!this.rendered) {
            console.info(this.id + ' is not rendered yet');
            return;
        }
        var _this = this;

        this.clean();

        var sortfunction = function (a, b) {
            var IsNumber = true;
            for (var i = 0; i < a.name.length && IsNumber == true; i++) {
                if (isNaN(a.name[i])) {
                    IsNumber = false;
                }
            }
            if (!IsNumber) return 1;
            return (a.name - b.name);
        };

        CellBaseManager.get({
            species: this.species,
            category: 'genomic',
            subCategory: 'chromosome',
            resource: 'all',
            success: function (data) {
                _this.chromosomeList = data.response.result.chromosomes;
                _this.chromosomeList.sort(sortfunction);
                _this._drawSvg(_this.chromosomeList);
            }
        });

        if (this.collapsed) {
            _this.hideContent();
        }
    },

    _drawSvg: function (chromosomeList) {
        var _this = this;

        var x = 20;
        var xOffset = _this.width / chromosomeList.length;
        var yMargin = 2;

        ///////////
        var biggerChr = 0;
        for (var i = 0, len = chromosomeList.length; i < len; i++) {
            var size = chromosomeList[i].size;
            if (size > biggerChr) {
                biggerChr = size;
            }
        }
        _this.pixelBase = (_this.height - 10) / biggerChr;
        _this.chrOffsetY = {};
        _this.chrOffsetX = {};

        for (var i = 0, len = chromosomeList.length; i < len; i++) { //loop over chromosomes
            var chromosome = chromosomeList[i];
//		var chr = chromosome.name;
            var chrSize = chromosome.size * _this.pixelBase;
            var y = yMargin + (biggerChr * _this.pixelBase) - chrSize;
            _this.chrOffsetY[chromosome.name] = y;
            var firstCentromere = true;

            var centerPosition = _this.region.center();
            var pointerPosition = (centerPosition * _this.pixelBase);

            var group = SVG.addChild(_this.svg, "g", {"cursor": "pointer", "chr": chromosome.name});
            $(group).click(function (event) {
                var chrClicked = this.getAttribute("chr");
//			for ( var k=0, len=chromosomeList.length; k<len; k++) {
//			var offsetX = (event.pageX - $(_this.svg).offset().left);
//			if(offsetX > _this.chrOffsetX[chromosomeList[k]]) chrClicked = chromosomeList[k];
//			}

                var offsetY = (event.pageY - $(_this.svg).offset().top);
//			var offsetY = event.originalEvent.layerY - 3;

                _this.positionBox.setAttribute("x1", _this.chrOffsetX[chrClicked] - 10);
                _this.positionBox.setAttribute("x2", _this.chrOffsetX[chrClicked] + 23);
                _this.positionBox.setAttribute("y1", offsetY);
                _this.positionBox.setAttribute("y2", offsetY);

                var clickPosition = parseInt((offsetY - _this.chrOffsetY[chrClicked]) / _this.pixelBase);
                _this.region.chromosome = chrClicked;
                _this.region.start = clickPosition;
                _this.region.end = clickPosition;

                _this.trigger('region:change', {region: _this.region, sender: _this});
            });

            for (var j = 0, lenJ = chromosome.cytobands.length; j < lenJ; j++) { //loop over chromosome objects
                var cytoband = chromosome.cytobands[j];
                var height = _this.pixelBase * (cytoband.end - cytoband.start);
                var width = 13;

                var color = _this.colors[cytoband.stain];
                if (color == null) color = "purple";

                if (cytoband.stain == "acen") {
                    var points = "";
                    var middleX = x + width / 2;
                    var middleY = y + height / 2;
                    var endX = x + width;
                    var endY = y + height;
                    if (firstCentromere) {
                        points = x + "," + y + " " + endX + "," + y + " " + endX + "," + middleY + " " + middleX + "," + endY + " " + x + "," + middleY;
                        firstCentromere = false;
                    } else {
                        points = x + "," + endY + " " + x + "," + middleY + " " + middleX + "," + y + " " + endX + "," + middleY + " " + endX + "," + endY;
                    }
                    SVG.addChild(group, "polyline", {
                        "points": points,
                        "stroke": "black",
                        "opacity": 0.8,
                        "fill": color
                    });
                } else {
                    SVG.addChild(group, "rect", {
                        "x": x,
                        "y": y,
                        "width": width,
                        "height": height,
                        "stroke": "grey",
                        "opacity": 0.8,
                        "fill": color
                    });
                }

                y += height;
            }
            var text = SVG.addChild(_this.svg, "text", {
                "x": x + 1,
                "y": _this.height,
                "font-size": 9,
                "fill": "black"
            });
            text.textContent = chromosome.name;

            _this.chrOffsetX[chromosome.name] = x;
            x += xOffset;
        }
        _this.positionBox = SVG.addChild(_this.svg, "line", {
            "x1": _this.chrOffsetX[_this.region.chromosome] - 10,
            "y1": pointerPosition + _this.chrOffsetY[_this.region.chromosome],
            "x2": _this.chrOffsetX[_this.region.chromosome] + 23,
            "y2": pointerPosition + _this.chrOffsetY[_this.region.chromosome],
            "stroke": "orangered",
            "stroke-width": 2,
            "opacity": 0.5
        });

        _this.rendered = true;
        _this.trigger('after:render',{sender:_this});
    },


    setRegion: function (region) {//item.chromosome, item.position, item.species
        this.region.load(region);
        var needDraw = false;

        if (this.lastSpecies != this.species) {
            needDraw = true;
            this.lastSpecies = this.species;
        }

        //recalculate positionBox
        var centerPosition = this.region.center();
        var pointerPosition = centerPosition * this.pixelBase + this.chrOffsetY[this.region.chromosome];
        this.positionBox.setAttribute("x1", this.chrOffsetX[this.region.chromosome] - 10);
        this.positionBox.setAttribute("x2", this.chrOffsetX[this.region.chromosome] + 23);
        this.positionBox.setAttribute("y1", pointerPosition);
        this.positionBox.setAttribute("y2", pointerPosition);

        if (needDraw) {
            this.draw();
        }
    },


    updatePositionBox: function () {
        this.positionBox.setAttribute("x1", this.chrOffsetX[this.region.chromosome] - 10);
        this.positionBox.setAttribute("x2", this.chrOffsetX[this.region.chromosome] + 23);

        var centerPosition = Utils.centerPosition(this.region);
        var pointerPosition = centerPosition * this.pixelBase + this.chrOffsetY[this.region.chromosome];
        this.positionBox.setAttribute("y1", pointerPosition);
        this.positionBox.setAttribute("y2", pointerPosition);
    },

    addMark: function (item) {//item.chromosome, item.position
        var _this = this;

        var mark = function () {
            if (_this.region.chromosome != null && _this.region.start != null) {
                if (_this.chrOffsetX[_this.region.chromosome] != null) {
                    var x1 = _this.chrOffsetX[_this.region.chromosome] - 10;
                    var x2 = _this.chrOffsetX[_this.region.chromosome];
                    var y1 = (_this.region.start * _this.pixelBase + _this.chrOffsetY[_this.region.chromosome]) - 4;
                    var y2 = _this.region.start * _this.pixelBase + _this.chrOffsetY[_this.region.chromosome];
                    var y3 = (_this.region.start * _this.pixelBase + _this.chrOffsetY[_this.region.chromosome]) + 4;
                    var points = x1 + "," + y1 + " " + x2 + "," + y2 + " " + x1 + "," + y3 + " " + x1 + "," + y1;
                    SVG.addChild(_this.markGroup, "polyline", {
                        "points": points,
                        "stroke": "black",
                        "opacity": 0.8,
                        "fill": "#33FF33"
                    });
                }
            }
        };

        if (this.rendered) {
            mark();
        } else {
            _this.on('after:render',function (e) {
                mark();
            });
        }
    },

    unmark: function () {
        $(this.markGroup).empty();
    }
}

function StatusBar(args) {

    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    var _this = this;

    this.id = Utils.genId("StatusBar");

    //set instantiation args, must be last
    _.extend(this, args);

    //set new region object
    this.region = new Region(this.region);

    this.rendered=false;
    if(this.autoRender){
        this.render();
    }
};

StatusBar.prototype = {
    render: function (targetId) {
        this.targetId = (targetId) ? targetId : this.targetId;
        if($('#' + this.targetId).length < 1){
            console.log('targetId not found in DOM');
            return;
        }
        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div id="' + this.id + '" class="gv-status-bar" align="right"></div>')[0];
        $(this.targetDiv).append(this.div);

        this.mousePositionDiv = $('<div id="' + this.id + 'position" style="display: inline">&nbsp;</div>')[0];
        $(this.mousePositionDiv).css({
            'margin-left': '5px',
            'margin-right': '5px',
            'font-size':'12px'
        });

        this.versionDiv = $('<div id="' + this.id + 'version" style="display: inline">' + this.version + '</div>')[0];
        $(this.versionDiv).css({
            'margin-left': '5px',
            'margin-right': '5px'
        });


        $(this.div).append(this.mousePositionDiv);
        $(this.div).append(this.versionDiv);

        this.rendered = true;
    },
    setRegion: function (event) {
        this.region.load(event.region);
        $(this.mousePositionDiv).html(Utils.formatNumber(event.region.center()));
    },
    setMousePosition: function (event) {
        $(this.mousePositionDiv).html(event.baseHtml+' '+this.region.chromosome+':'+Utils.formatNumber(event.mousePos));
    }

}
function DataSource() {
	
};

DataSource.prototype.fetch = function(){

};

FileDataSource.prototype.fetch = DataSource.prototype.fetch;

function FileDataSource(file) {
	DataSource.prototype.constructor.call(this);
	
    _.extend(this, Backbone.Events);

    this.file = file;
    this.maxSize = 500*1024*1024;

};

FileDataSource.prototype.error = function(){
	alert("File is too big. Max file size is "+this.maxSize+" bytes");
};

FileDataSource.prototype.fetch = function(async){
	var _this = this;
	if(this.file.size <= this.maxSize){
		if(async){
			var  reader = new FileReader();
			reader.onload = function(evt) {
                _this.trigger('success',evt.target.result);
			};
			reader.readAsText(this.file, "UTF-8");
		}else{
			// FileReaderSync web workers only
			var reader = new FileReaderSync();
			return reader.readAsText(this.file, "UTF-8");
		}
	}else{
		_this.error();
		_this.trigger('error',{sender:this});
	}
};

StringDataSource.prototype.fetch = DataSource.prototype.fetch;

function StringDataSource(str) {
	DataSource.prototype.constructor.call(this);

    _.extend(this, Backbone.Events);
	this.str = str;
};

StringDataSource.prototype.fetch = function(async){
	if(async){
		this.trigger('success',this.str);
	}else{
		return this.str;
	}
};

UrlDataSource.prototype.fetch = DataSource.prototype.fetch;

function UrlDataSource(url, args) {
	DataSource.prototype.constructor.call(this);
	
	this.url = url;
	this.proxy = CELLBASE_HOST+"/latest/utils/proxy?url=";
	if(args != null){
		if(args.proxy != null){
			if(typeof(args.proxy) == "boolean"){
				if(args.proxy == false){
					this.proxy = false;
				}
				else{
					this.url = this.proxy + url;
				}
			}else if(typeof(args.proxy) == "string"){
				this.url = args.proxy + url;
			}
		}
	}
	this.success = new Event();
	this.error = new Event();
};

UrlDataSource.prototype.fetch = function(async){
	var _this = this;
	
	var datos = null;
	
	if(this.url){
		$.ajax({
			type : "GET",
			url : this.url,
			async : async,
			success : function(data, textStatus, jqXHR) {
				if(async){
					_this.success.notify(data);
				}else{
					datos = data;
				}
			},
			error : function(jqXHR, textStatus, errorThrown){
				console.log("URL Data source: Ajax call returned : "+errorThrown+'\t'+textStatus+'\t'+jqXHR.statusText+" END");
				_this.error.notify();
			}
		});
		
		return datos;
	}
};

function CellBaseAdapter(args) {

    _.extend(this, Backbone.Events);

    _.extend(this, args);

    this.on(this.handlers);

    this.cache = {};
}

CellBaseAdapter.prototype = {

    getData: function (args) {
        var _this = this;

        /** Check region and parameters **/
        var region = args.region;
        if (region.start > 300000000 || region.end < 1) {
            return;
        }
        region.start = (region.start < 1) ? 1 : region.start;
        region.end = (region.end > 300000000) ? 300000000 : region.end;


        var params = {};
        _.extend(params, this.params);
        _.extend(params, args.params);

        var dataType = args.dataType;
        if (_.isUndefined(dataType)) {
            console.log("dataType must be provided!!!");
        }
        var chunkSize;


        /** Check dataType histogram  **/
        if (dataType == 'histogram') {
            // Histogram chunks will be saved in different caches by interval size
            // The chunkSize will be the histogram interval
            var histogramId = dataType + '_' + params.interval;
            if (_.isUndefined(this.cache[histogramId])) {
                this.cache[histogramId] = new FeatureChunkCache({chunkSize: params.interval});
            }
            chunkSize = this.cache[histogramId].chunkSize;

            // Extend region to be adjusted with the chunks
            //        --------------------             -> Region needed
            // |----|----|----|----|----|----|----|    -> Logical chunk division
            //      |----|----|----|----|----|         -> Chunks covered by needed region
            //      |------------------------|         -> Adjusted region
            var adjustedRegions = this.cache[histogramId].getAdjustedRegions(region);
            if (adjustedRegions.length > 0) {
                // Get CellBase data
                CellBaseManager.get({
                    host: this.host,
                    species: this.species,
                    category: this.category,
                    subCategory: this.subCategory,
                    query: adjustedRegions,
                    resource: this.resource,
                    params: params,
                    success: function (data) {
                        _this._cellbaseHistogramSuccess(data, dataType, histogramId);
                    }
                });
            } else {
                // Get chunks from cache
                var chunksByRegion = this.cache[histogramId].getCachedByRegion(region);
                var chunksCached = this.cache[histogramId].getByRegions(chunksByRegion.cached);
                this.trigger('data:ready', {items: chunksCached, dataType: dataType, chunkSize: chunkSize, sender: this});
            }

        /** Features: genes, snps ... **/
        } else {
            // Features will be saved using the dataType features
            if (_.isUndefined(this.cache[dataType])) {
                this.cache[dataType] = new FeatureChunkCache(this.cacheConfig);
            }
            chunkSize = this.cache[dataType].chunkSize;

            // Get cached chunks and not cached chunk regions
            //        --------------------             -> Region needed
            // |----|----|----|----|----|----|----|    -> Logical chunk division
            //      |----|----|----|----|----|         -> Chunks covered by needed region
            //      |----|++++|++++|----|----|         -> + means the chunk is cached so its region will not be retrieved
            var chunksByRegion = this.cache[dataType].getCachedByRegion(region);

            if (chunksByRegion.notCached.length > 0) {
                var queryRegionStrings = _.map(chunksByRegion.notCached, function (region) {
                    return new Region(region).toString();
                });

                // Multiple CellBase calls will be performed, each one will
                // query 50 or less chunk regions
                var n = 50;
                var lists = _.groupBy(queryRegionStrings, function (a, b) {
                    return Math.floor(b / n);
                });
                // Each element on queriesList contains and array of 50 or less regions
                var queriesList = _.toArray(lists); //Added this to convert the returned object to an array.

                for (var i = 0; i < queriesList.length; i++) {
                    CellBaseManager.get({
                        host: this.host,
                        species: this.species,
                        category: this.category,
                        subCategory: this.subCategory,
                        query: queriesList[i],
                        resource: this.resource,
                        params: params,
                        success: function (data) {
                            _this._cellbaseSuccess(data, dataType);
                        }
                    });
                }
            }
            // Get chunks from cache
            if (chunksByRegion.cached.length > 0) {
                var chunksCached = this.cache[dataType].getByRegions(chunksByRegion.cached);
                this.trigger('data:ready', {items: chunksCached, dataType: dataType, chunkSize: chunkSize, sender: this});
            }
        }

    },

    _cellbaseSuccess: function (data, dataType) {
        var timeId = this.resource + " save " + Utils.randomString(4);
        console.time(timeId);
        /** time log **/

        var chunkSize = this.cache[dataType].chunkSize;

        var chunks = [];
        for (var i = 0; i < data.response.length; i++) {
            var queryResult = data.response[i];

            var region = new Region(queryResult.id);
            var features = queryResult.result;
            var chunk = this.cache[dataType].putByRegion(region, features);
            chunks.push(chunk);
        }

        /** time log **/
        console.timeEnd(timeId);


        if (chunks.length > 0) {
            this.trigger('data:ready', {items: chunks, dataType: dataType, chunkSize: chunkSize, sender: this});
        }


    },
    _cellbaseHistogramSuccess: function (data, dataType, histogramId) {
        var timeId = Utils.randomString(4);
        console.time(this.resource + " save " + timeId);
        /** time log **/

        var chunkSize = this.cache[histogramId].chunkSize;

        var chunks = [];
        for (var i = 0; i < data.response.length; i++) {
            var queryResult = data.response[i];
            for (var j = 0; j < queryResult.result.length; j++) {
                var interval = queryResult.result[j];
                var region = new Region(queryResult.id);
                region.load(interval);
                chunks.push(this.cache[histogramId].putByRegion(region, interval));
            }
        }

        this.trigger('data:ready', {items: chunks, dataType: dataType, chunkSize: chunkSize, sender: this});
        /** time log **/
        console.timeEnd(this.resource + " get and save " + timeId);
    }
};


function SequenceAdapter(args){

    _.extend(this, Backbone.Events);

    this.id = Utils.genId("TrackListPanel");

    //set default args
	this.host;
	this.gzip = true;

    //set instantiation args, must be last
    _.extend(this, args);

	this.sequence = {};
	this.start = {};
	this.end = {};

    this.on(this.handlers);
}

SequenceAdapter.prototype.clearData = function(){
	this.sequence = {};
	this.start = {};
	this.end = {};
};

SequenceAdapter.prototype.getData = function(args){
    var _this = this;

	this.sender = args.sender;
    var region = args.region;
	var chromosome = region.chromosome;

    region.start = (region.start < 1) ? 1 : region.start;
    region.end = (region.end > 300000000) ? 300000000 : region.end;

	//clean when the new position is too far from current
	if(region.start<this.start[chromosome]-5000 || region.end > this.end[chromosome]+5000){
		this.clearData();
	}

    var params = {};
    _.extend(params, this.params);


	var queryString = this._getSequenceQuery(region);

	if(queryString != ""){

        CellBaseManager.get({
            host: this.host,
            species: this.species,
            category:this.category,
            subCategory:this.subCategory,
            query:queryString,
            resource: this.resource,
            params: params,
            success:function (data) {
                _this._processSequenceQuery(data,true);
            }
        });


	}else{
		if(this.sender != "move"){
			this.trigger('data:ready',{
                items:{
                    sequence:this.sequence[chromosome],
                    start:this.start[chromosome],
                    end:this.end[chromosome]
                },
                params:params
            });
            this.trigger('data:ready',{
                items:{
                    sequence:this.sequence[chromosome],
                    start:this.start[chromosome],
                    end:this.end[chromosome]
                },
                params:params,
                sender:this
            });
		}
	}
	
};

SequenceAdapter.prototype._getSequenceQuery = function(region){
	var _this = this;
	var chromosome = region.chromosome;
	
	var s,e, query, querys = [];
	if(_this.start[chromosome]==null && _this.end[chromosome]==null){
			//args.start -= 100;
			//args.end += 100;
			_this.start[chromosome] = region.start;
			_this.end[chromosome] = region.end;
			s = region.start;
			e = region.end;
			query = chromosome+":"+s+"-"+e;
			querys.push(query);
	}else{
		if(region.start <= _this.start[chromosome]){
			s = region.start;
			e = _this.start[chromosome]-1;
            e = (e<1) ? region.end=1 : e ;
			_this.start[chromosome] = s;
			query = region.chromosome+":"+s+"-"+e;
			querys.push(query);
		}
		if(region.end >= _this.end[chromosome]){
			e = region.end;
			s = _this.end[chromosome]+1;
			_this.end[chromosome] = e;
			query = region.chromosome+":"+s+"-"+e;
			querys.push(query);
		}
	}
	return querys.toString();
};

SequenceAdapter.prototype._processSequenceQuery = function(data, throwNotify){
	var _this = this;
	var params = data.params;


    for(var i = 0; i < data.response.length; i++) {
        var queryResponse = data.response[i];
        var splitDots = queryResponse.id.split(":");
        var splitDash = splitDots[1].split("-");
        var queryStart = parseInt(splitDash[0]);
        var queryEnd = parseInt(splitDash[1]);

        var queryId = queryResponse.id;
	    var seqResponse = queryResponse.result;
	    var chromosome = seqResponse.chromosome;

        if(this.sequence[chromosome] == null){
            this.sequence[chromosome] =  seqResponse.sequence;
        }else{
            if(queryStart == this.start[chromosome]){
                this.sequence[chromosome] = seqResponse.sequence + this.sequence[chromosome];
            }else{
                this.sequence[chromosome] = this.sequence[chromosome] + seqResponse.sequence;
            }
        }

        if(this.sender == "move" && throwNotify == true){
            this.trigger('data:ready',{
                items:{
                    sequence:seqResponse.sequence,
                    start:queryStart,
                    end:queryEnd
                },
                params:params,
                sender:this
            });
        }
    }

	if(this.sender != "move" && throwNotify == true){
        this.trigger('data:ready',{
            items:{
                sequence:this.sequence[chromosome],
                start:this.start[chromosome],
                end:this.end[chromosome]
            },
            params:params,
            sender:this
        });
	}
};

//Used by bam to get the mutations
SequenceAdapter.prototype.getNucleotidByPosition = function(args){
	var _this=this;
    if(args.start > 0 && args.end>0){
        var queryString = this._getSequenceQuery(args);

        var chromosome = args.chromosome;

        if(queryString != ""){

            var data = CellBaseManager.get({
                host: this.host,
                species: this.species,
                category:this.category,
                subCategory:this.subCategory,
                query:queryString,
                resource: this.resource,
                params: this.params,
                async:false
            });
            _this._processSequenceQuery(data);

        }
        if(this.sequence[chromosome] != null){
            var referenceSubStr = this.sequence[chromosome].substr((args.start-this.start[chromosome]),1);
            return referenceSubStr;
        }else{
            console.log("SequenceRender: this.sequence[chromosome] is undefined");
            return "";
        }
    }
};

function BamAdapter(args){

    _.extend(this, Backbone.Events);

    if(typeof args != 'undefined'){
        this.host = args.host || this.host;
        this.category = args.category || this.category;
		this.resource = args.resource || this.resource;
		this.params = args.params || this.params;
		this.filters = args.filters || this.filters;
		this.options = args.options || this.options;
        this.species = args.species || this.species;
        var argsFeatureCache = args.featureCache || {};
    }
	if (args != null){
		if(args.featureConfig != null){
			if(args.featureConfig.filters != null){
				this.filtersConfig = args.featureConfig.filters;
			}
			if(args.featureConfig.options != null){//apply only check boxes
				this.optionsConfig = args.featureConfig.options;
				for(var i = 0; i < this.optionsConfig.length; i++){
					if(this.optionsConfig[i].checked == true){
						this.options[this.optionsConfig[i].name] = true;
						this.params[this.optionsConfig[i].name] = true;
					}				
				}
			}
		}
	}

	this.featureCache = new BamCache(argsFeatureCache);
//	this.onGetData = new Event();
}

BamAdapter.prototype = {
    host : null,
    gzip : true,
    params : {}
};

BamAdapter.prototype.clearData = function(){
	this.featureCache.clear();
};

BamAdapter.prototype.setFilters = function(filters){
	this.clearData();
	this.filters = filters;
	for(filter in filters){
		var value = filters[filter].toString();
		delete this.params[filter];
		if(value != ""){
			this.params[filter] = value;
		}
	}
};
BamAdapter.prototype.setOption = function(opt, value){
	if(opt.fetch){
		this.clearData();
	}
	this.options[opt.name] = value;
	for(option in this.options){
		if(this.options[opt.name] != null){
			this.params[opt.name] = this.options[opt.name];
		}else{
			delete this.params[opt.name];
		}
	}
};


BamAdapter.prototype.getData = function(args){
	var _this = this;
	//region check
	this.params["histogram"] = args.histogram;
	this.params["histogramLogarithm"] = args.histogramLogarithm;
	this.params["histogramMax"] = args.histogramMax;
	this.params["interval"] = args.interval;
	this.params["transcript"] = args.transcript;
	this.params["chromosome"] = args.chromosome;
	this.params["resource"] = this.resource.id;
	this.params["category"] = this.category;
	this.params["species"] = Utils.getSpeciesCode(this.species.text);


	if(args.start<1){
		args.start=1;
	}
	if(args.end>300000000){
		args.end=300000000;
	}
	
	var dataType = "data";
	if(args.histogram){
		dataType = "histogram"+args.interval;
	}

	this.params["dataType"] = dataType;
	
	var firstChunk = this.featureCache._getChunk(args.start);
	var lastChunk = this.featureCache._getChunk(args.end);
	var chunks = [];
	var itemList = [];
	for(var i=firstChunk; i<=lastChunk; i++){
		var key = args.chromosome+":"+i;
		if(this.featureCache.cache[key] == null || this.featureCache.cache[key][dataType] == null) {
			chunks.push(i);
		}else{
			var item = this.featureCache.getFeatureChunk(key);
			itemList.push(item);
		}
	}

    var regionSuccess = function (data) {
		var splitDots = data.query.split(":");
		var splitDash = splitDots[1].split("-");
		var query = {chromosome:splitDots[0],start:splitDash[0],end:splitDash[1]};


		var dataType = "data";
		if(data.params.histogram){
			dataType = "histogram"+data.params.interval;
		    _this.featureCache.putHistogramFeaturesByRegion(data.result, query, data.resource, dataType);
		}else{
		    _this.featureCache.putFeaturesByRegion(data.result, query, data.resource, dataType);
        }

		var items = _this.featureCache.getFeatureChunksByRegion(query, dataType);
		itemList = itemList.concat(items);
		if(itemList.length > 0){
            _this.trigger('data:ready',{items:itemList, params:_this.params, cached:false, sender:_this});
//			_this.onGetData.notify({items:itemList, params:_this.params, cached:false});
		}
	};

	var querys = [];
	var updateStart = true;
	var updateEnd = true;
	if(chunks.length > 0){//chunks needed to retrieve
//		console.log(chunks);
		
		for ( var i = 0; i < chunks.length; i++) {
			
			if(updateStart){
				var chunkStart = parseInt(chunks[i] * this.featureCache.chunkSize);
				updateStart = false;
			}
			if(updateEnd){
				var chunkEnd = parseInt((chunks[i] * this.featureCache.chunkSize) + this.featureCache.chunkSize-1);
				updateEnd = false;
			}
			
			if(chunks[i+1]!=null){
				if(chunks[i]+1==chunks[i+1]){
					updateEnd =true;
				}else{
					var query = args.chromosome+":"+chunkStart+"-"+chunkEnd;
					querys.push(query);
					updateStart = true;
					updateEnd = true;
				}
			}else{
				var query = args.chromosome+":"+chunkStart+"-"+chunkEnd;
				querys.push(query);
				updateStart = true;
				updateEnd = true;
			}
		}
//		console.log(querys);
		for ( var i = 0, li = querys.length; i < li; i++) {
			console.time("dqs");
			//accountId, sessionId, bucketname, objectname, region,
            var cookie = $.cookie("bioinfo_sid");
            cookie = ( cookie != '' && cookie != null ) ?  cookie : 'dummycookie';
            OpencgaManager.region({
                accountId: this.resource.account,
                sessionId: cookie,
                bucketId: this.resource.bucketId,
                objectId: this.resource.oid,
                region: querys[i],
                queryParams: this.params,
                success:regionSuccess
            });
		}
	}else{//no server call
		if(itemList.length > 0){
            _this.trigger('data:ready',{items:itemList, params:this.params, cached:false, sender:this});
//			this.onGetData.notify({items:itemList, params:this.params});
		}
	}
};

function OpencgaAdapter(args) {

    _.extend(this, Backbone.Events);

    _.extend(this, args);

    this.on(this.handlers);

    this.cache = {};
}

OpencgaAdapter.prototype = {
    getData: function (args) {
        var _this = this;
        /********/

        var region = args.region;
        if (region.start > 300000000 || region.end < 1) {
            return;
        }
        region.start = (region.start < 1) ? 1 : region.start;
        region.end = (region.end > 300000000) ? 300000000 : region.end;

        var params = {species: Utils.getSpeciesCode(this.species.text)};
        _.extend(params, this.params);
        _.extend(params, args.params);

        var dataType = args.dataType;
        if (_.isUndefined(dataType)) {
            console.log("dataType must be provided!!!");
        }
        var chunkSize;
        /********/

        if (dataType == 'histogram') {

        } else {
            //Create one FeatureChunkCache by datatype
            if (_.isUndefined(this.cache[dataType])) {
                this.cache[dataType] = new FeatureChunkCache(this.cacheConfig);
            }
            chunkSize = this.cache[dataType].chunkSize;

            var chunksByRegion = this.cache[dataType].getCachedByRegion(region);

            if (chunksByRegion.notCached.length > 0) {
                var queryRegionStrings = _.map(chunksByRegion.notCached, function (region) {
                    return new Region(region).toString();
                });

                //limit queries
                var n = 50;
                var lists = _.groupBy(queryRegionStrings, function (a, b) {
                    return Math.floor(b / n);
                });
                var queriesList = _.toArray(lists); //Added this to convert the returned object to an array.

                for (var i = 0; i < queriesList.length; i++) {
                    var cookie = $.cookie("bioinfo_sid");
                    cookie = ( cookie != '' && cookie != null ) ? cookie : 'dummycookie';
                    OpencgaManager.region({
                        accountId: this.resource.account,
                        sessionId: cookie,
                        bucketId: this.resource.bucketId,
                        objectId: this.resource.oid,
                        region: queriesList[i],
                        queryParams: params,
                        success: function (data) {
                            _this._opencgaSuccess(data, dataType);
                        }
                    });
//                    CellBaseManager.get({
//                        host: this.host,
//                        species: this.species,
//                        category: this.category,
//                        subCategory: this.subCategory,
//                        query: queriesList[i],
//                        resource: this.resource,
//                        params: params,
//                        success: function (data) {
//                            _this._cellbaseSuccess(data, dataType);
//                        }
//                    });
                }
            }
            if (chunksByRegion.cached.length > 0) {
                var chunksCached = this.cache[dataType].getByRegions(chunksByRegion.cached);
                this.trigger('data:ready', {items: chunksCached, dataType: dataType, chunkSize: chunkSize, sender: this});
            }
        }

    },
    _opencgaSuccess: function (data, dataType) {
        var timeId = this.resource + " save " + Utils.randomString(4);
        console.time(timeId);
        /** time log **/

        var chunkSize = this.cache[dataType].chunkSize;

        var chunks = [];
        for (var i = 0; i < data.response.length; i++) {
            var queryResult = data.response[i];

            var region = new Region(queryResult.id);
            var features = queryResult.result;
            var chunk = this.cache[dataType].putByRegion(region, features);
            chunks.push(chunk);
        }

        /** time log **/
        console.timeEnd(timeId);

        if (chunks.length > 0) {
            this.trigger('data:ready', {items: chunks, dataType: dataType, chunkSize: chunkSize, sender: this});
        }
    }
}


OpencgaAdapter.prototype.getDataOld = function (args) {
    debugger
    var _this = this;
    //region check

    this.params["histogram"] = args.histogram;
    this.params["histogramLogarithm"] = args.histogramLogarithm;
    this.params["histogramMax"] = args.histogramMax;
    this.params["interval"] = args.interval;
    this.params["transcript"] = args.transcript;


    if (args.start < 1) {
        args.start = 1;
    }
    if (args.end > 300000000) {
        args.end = 300000000;
    }

    var type = "data";
    if (args.histogram) {
        type = "histogram" + args.interval;
    }

    var firstChunk = this.featureCache._getChunk(args.start);
    var lastChunk = this.featureCache._getChunk(args.end);

    var chunks = [];
    var itemList = [];
    for (var i = firstChunk; i <= lastChunk; i++) {
        var key = args.chromosome + ":" + i;
        if (this.featureCache.cache[key] == null || this.featureCache.cache[key][type] == null) {
            chunks.push(i);
        } else {
            var items = this.featureCache.getFeatureChunk(key, type);
            itemList = itemList.concat(items);
        }
    }
////	//notify all chunks
//	if(itemList.length>0){
//		this.onGetData.notify({data:itemList, params:this.params, cached:true});
//	}


    //CellBase data process
    //TODO check host
    var calls = 0;
    var querys = [];
    regionSuccess = function (data) {
        console.timeEnd("dqs");
        console.time("dqs-cache");
        var type = "data";
        if (data.params.histogram) {
            type = "histogram" + data.params.interval;
        }
        _this.params["dataType"] = type;

        var splitDots = data.query.split(":");
        var splitDash = splitDots[1].split("-");
        var query = {chromosome: splitDots[0], start: splitDash[0], end: splitDash[1]};

        //check if features contains positon or start-end
        if (data.result[0] != null && data.result[0]['position'] != null) {
            for (var i = 0; i < data.result.length; i++) {
                data.result[i]['start'] = data.result[i].position;
                data.result[i]['end'] = data.result[i].position;
            }
        }

        _this.featureCache.putFeaturesByRegion(data.result, query, _this.category, type);
        var items = _this.featureCache.getFeatureChunksByRegion(query, type);
        console.timeEnd("dqs-cache");
        if (items != null) {
            itemList = itemList.concat(items);
        }
        if (calls == querys.length) {
//			_this.onGetData.notify({items:itemList, params:_this.params, cached:false});
            _this.trigger('data:ready', {items: itemList, params: _this.params, cached: false, sender: _this});
        }
    };

    var updateStart = true;
    var updateEnd = true;
    if (chunks.length > 0) {
//		console.log(chunks);

        for (var i = 0; i < chunks.length; i++) {

            if (updateStart) {
                var chunkStart = parseInt(chunks[i] * this.featureCache.chunkSize);
                updateStart = false;
            }
            if (updateEnd) {
                var chunkEnd = parseInt((chunks[i] * this.featureCache.chunkSize) + this.featureCache.chunkSize - 1);
                updateEnd = false;
            }

            if (chunks[i + 1] != null) {
                if (chunks[i] + 1 == chunks[i + 1]) {
                    updateEnd = true;
                } else {
                    var query = args.chromosome + ":" + chunkStart + "-" + chunkEnd;
                    querys.push(query);
                    updateStart = true;
                    updateEnd = true;
                }
            } else {
                var query = args.chromosome + ":" + chunkStart + "-" + chunkEnd;

                querys.push(query);
                updateStart = true;
                updateEnd = true;
            }
        }
//		console.log(querys)
        for (var i = 0, li = querys.length; i < li; i++) {
            console.time("dqs");
            calls++;
//			opencgaManager.region(this.category, this.resource, querys[i], this.params);
            var cookie = $.cookie("bioinfo_sid");
            cookie = ( cookie != '' && cookie != null ) ? cookie : 'dummycookie';
            OpencgaManager.region({
                accountId: this.resource.account,
                sessionId: cookie,
                bucketId: this.resource.bucketId,
                objectId: this.resource.oid,
                region: querys[i],
                queryParams: this.params,
                success: regionSuccess
            });
        }
    } else {
        if (itemList.length > 0) {
            this.trigger('data:ready', {items: itemList, params: this.params, cached: false, sender: this});
//			this.onGetData.notify({items:itemList, params:this.params});
        }
    }
};
function FeatureDataAdapter(dataSource, args) {
    var _this = this;
    _.extend(this, Backbone.Events);

    this.dataSource = dataSource;
    this.gzip = true;

    this.params = {};
    if (args != null) {
        if (args.gzip != null) {
            this.gzip = args.gzip;
        }
        if (args.species != null) {
            this.species = args.species;
        }
        if (args.params != null) {
            this.params = args.params;
        }
    }

    this.featureCache = new FeatureCache({chunkSize: 10000, gzip: this.gzip});

//	this.onLoad = new Event();
//	this.onGetData = new Event();

    //chromosomes loaded
    this.chromosomesLoaded = {};
}

FeatureDataAdapter.prototype.getData = function (args) {
    console.log("TODO comprobar histograma");
    console.log(args.region);
    this.params["dataType"] = "data";
    this.params["chromosome"] = args.region.chromosome;

    //check if the chromosome has been already loaded
    if (this.chromosomesLoaded[args.region.chromosome] != true) {
        this._fetchData(args.region);
        this.chromosomesLoaded[args.region.chromosome] = true;
    }

    var itemList = this.featureCache.getFeatureChunksByRegion(args.region);
    if (itemList != null) {
        this.trigger('data:ready', {items: itemList, params: this.params, chunkSize:this.featureCache.chunkSize, cached: true, sender: this});
    }
};

FeatureDataAdapter.prototype._fetchData = function (region) {
    var _this = this;
    if (this.dataSource != null) {//could be null in expression genomic attributer widget 59
        if (this.async) {
            this.dataSource.on('success', function (data) {
                _this.parse(data, region);
//				_this.onLoad.notify();
                _this.trigger('file:load', {sender: _this});


                var itemList = _this.featureCache.getFeatureChunksByRegion(region);
                if (itemList != null) {
                    _this.trigger('data:ready', {items: itemList, params: _this.params, chunkSize:_this.featureCache.chunkSize, cached: true, sender: _this});
                }

            });
            this.dataSource.fetch(this.async);
        } else {
            var data = this.dataSource.fetch(this.async);
            this.parse(data, region);
        }
    }
}

FeatureDataAdapter.prototype.addFeatures = function (features) {
    this.featureCache.putFeatures(features, "data");
};

BEDDataAdapter.prototype.getData = FeatureDataAdapter.prototype.getData;
BEDDataAdapter.prototype._fetchData = FeatureDataAdapter.prototype._fetchData;

function BEDDataAdapter(dataSource, args){
	FeatureDataAdapter.prototype.constructor.call(this, dataSource, args);
	var _this = this;
	
	this.async = true;
	
	//stat atributes
	this.featuresCount = 0;
	this.featuresByChromosome = {};

	if (args != null){
		if(args.async != null){
			this.async = args.async;
		}
	}
};

BEDDataAdapter.prototype.parse = function(data, region){
	var _this = this;
	var dataType = "value";
	var lines = data.split("\n");
//	console.log("creating objects");
	for (var i = 0; i < lines.length; i++){
		var line = lines[i].replace(/^\s+|\s+$/g,"");
		if ((line != null)&&(line.length > 0)){
			var fields = line.split("\t");
			var chromosome = fields[0].replace("chr", "");
			if(chromosome == region.chromosome){// load only one chromosome on the cache
			
				var feature = {
						"label":fields[3],
						"chromosome": chromosome, 
						"start": parseFloat(fields[1]), 
						"end": parseFloat(fields[2]), 
						"score":fields[4],
						"strand":fields[5],
						"thickStart":fields[6],
						"thickEnd":fields[7],
						"itemRgb":fields[8],
						"blockCount":fields[9],
						"blockSizes":fields[10],
						"blockStarts":fields[11],
						"featureType":	"bed"
				};

				this.featureCache.putFeatures(feature, dataType);
				
				if (this.featuresByChromosome[chromosome] == null){
					this.featuresByChromosome[chromosome] = 0;
				}
				this.featuresByChromosome[chromosome]++;
				this.featuresCount++;
			}
		}
	}
};

GFF2DataAdapter.prototype.getData = FeatureDataAdapter.prototype.getData;
GFF2DataAdapter.prototype._fetchData = FeatureDataAdapter.prototype._fetchData;

function GFF2DataAdapter(dataSource, args){
	FeatureDataAdapter.prototype.constructor.call(this, dataSource, args);
	var _this = this;
	
	this.async = true;
	
	//stat atributes
	this.featuresCount = 0;
	this.featuresByChromosome = {};

	if (args != null){
		if(args.async != null){
			this.async = args.async;
		}
	}
};

GFF2DataAdapter.prototype.parse = function(data, region){
	var _this = this;
	var dataType = "value";
	var lines = data.split("\n");
//	console.log("creating objects");
	for (var i = 0; i < lines.length; i++){
		var line = lines[i].replace(/^\s+|\s+$/g,"");
		if ((line != null)&&(line.length > 0)){
			var fields = line.split("\t");
			var chromosome = fields[0].replace("chr", "");
			if(chromosome == region.chromosome){// load only one chromosome on the cache

				//NAME  SOURCE  TYPE  START  END  SCORE  STRAND  FRAME  GROUP
				var feature = {
						"chromosome": chromosome, 
						"label": fields[2], 
						"start": parseInt(fields[3]), 
						"end": parseInt(fields[4]), 
						"score": fields[5],
						"strand": fields[6], 
						"frame": fields[7],
						"group": fields[8],
						"featureType":	"gff2"
				} ;

				this.featureCache.putFeatures(feature, dataType);
				
				if (this.featuresByChromosome[chromosome] == null){
					this.featuresByChromosome[chromosome] = 0;
				}
				this.featuresByChromosome[chromosome]++;
				this.featuresCount++;
			}
		}
	}
};

GFF3DataAdapter.prototype.getData = FeatureDataAdapter.prototype.getData;
GFF3DataAdapter.prototype._fetchData = FeatureDataAdapter.prototype._fetchData;

function GFF3DataAdapter(dataSource, args){
	FeatureDataAdapter.prototype.constructor.call(this, dataSource, args);
	var _this = this;
	
	this.async = true;

	//stat atributes
	this.featuresCount = 0;
	this.featuresByChromosome = {};

	if (args != null){
		if(args.async != null){
			this.async = args.async;
		}
	}
};

GFF3DataAdapter.prototype.parse = function(data, region){
	var _this = this;
	
	//parse attributes column
	var getAttr = function(column){
		var obj = {};
        if(typeof column !== 'undefined'){
            var arr = column.replace(/ /g,'').split(";");
            for (var i = 0, li = arr.length; i<li ; i++){
                var item = arr[i].split("=");
                obj[item[0]] = item[1];
            }
        }
		return obj;
	};
	var dataType = "value";
	var lines = data.split("\n");
//	console.log("creating objects");
	for (var i = 0; i < lines.length; i++){
		var line = lines[i].replace(/^\s+|\s+$/g,"");
		if ((line != null)&&(line.length > 0)){
			var fields = line.split("\t");
			var chromosome = fields[0].replace("chr", "");
			if(chromosome == region.chromosome){// load only one chromosome on the cache

				//NAME  SOURCE  TYPE  START  END  SCORE  STRAND  FRAME  GROUP
				var feature = {
						"chromosome": chromosome, 
						"label": fields[2], 
						"start": parseInt(fields[3]), 
						"end": parseInt(fields[4]), 
						"score": fields[5],
						"strand": fields[6], 
						"frame": fields[7],
						"attributes": getAttr(fields[8]),
						"featureType":	"gff3"
				} ;

				this.featureCache.putFeatures(feature, dataType);
				if (this.featuresByChromosome[chromosome] == null){
					this.featuresByChromosome[chromosome] = 0;
				}
				this.featuresByChromosome[chromosome]++;
				this.featuresCount++;

			}
		}
	}
};

GTFDataAdapter.prototype.getData = FeatureDataAdapter.prototype.getData;
GTFDataAdapter.prototype._fetchData = FeatureDataAdapter.prototype._fetchData;

function GTFDataAdapter(dataSource, args){
	FeatureDataAdapter.prototype.constructor.call(this, dataSource, args);
	var _this = this;
	
	this.async = true;
	
	//stat atributes
	this.featuresCount = 0;
	this.featuresByChromosome = {};

	if (args != null){
		if(args.async != null){
			this.async = args.async;
		}
	}
};

GTFDataAdapter.prototype.parse = function(data, region){
	var _this = this;
	
	//parse attributes column
	var getAttr = function(column){
		var arr = column.split(";");
		var obj = {};
		for (var i = 0, li = arr.length; i<li ; i++){
			var item = arr[i].split("=");
			obj[item[0]] = item[1];
		}
		return obj;
	};
	
	var dataType = "value";
	var lines = data.split("\n");
//	console.log("creating objects");
	for (var i = 0; i < lines.length; i++){
		var line = lines[i].replace(/^\s+|\s+$/g,"");
		if ((line != null)&&(line.length > 0)){
			var fields = line.split("\t");
			var chromosome = fields[0].replace("chr", "");
			if(chromosome == region.chromosome){// load only one chromosome on the cache
			
				//NAME  SOURCE  TYPE  START  END  SCORE  STRAND  FRAME  GROUP
				var feature = {
						"chromosome": chromosome, 
						"label": fields[2], 
						"start": parseInt(fields[3]), 
						"end": parseInt(fields[4]), 
						"score": fields[5],
						"strand": fields[6], 
						"frame": fields[7],
						"attributes": getAttr(fields[8]),
						"featureType":	"gtf"
				} ;

				this.featureCache.putFeatures(feature, dataType);
				if (this.featuresByChromosome[chromosome] == null){
					this.featuresByChromosome[chromosome] = 0;
				}
				this.featuresByChromosome[chromosome]++;
				this.featuresCount++;
			}
		}
	}
};

VCFDataAdapter.prototype.getData = FeatureDataAdapter.prototype.getData;
VCFDataAdapter.prototype._fetchData = FeatureDataAdapter.prototype._fetchData;

function VCFDataAdapter(dataSource, args){
	FeatureDataAdapter.prototype.constructor.call(this, dataSource, args);
	var _this = this;
	
	this.async = true;
	//stat atributes
	this.featuresCount = 0;
	this.featuresByChromosome = {};
	
	this.header = "";
	this.samples = [];

	if (args != null){
		if(args.async != null){
			this.async = args.async;
		}
	}
}

VCFDataAdapter.prototype.parse = function(data, region){
//	console.log(data);
	var _this = this;
	var dataType = "value";
	var lines = data.split("\n");
//    debugger
//	console.log("creating objects");
	for (var i = 0; i < lines.length; i++){
//        debugger
		var line = lines[i].replace(/^\s+|\s+$/g,"");
		if ((line != null)&&(line.length > 0)){
			var fields = line.split("\t");
			if(fields[0]==region.chromosome){// load only one chromosome on the cache
			
				if(line.substr(0,1)==="#"){
					if(line.substr(1,1)==="#"){
						this.header+=line.replace(/</gi,"&#60;").replace(/>/gi,"&#62;")+"<br>";
					}else{
						this.samples = fields.slice(9);
					}
				}else{
	//				_this.addQualityControl(fields[5]);
					var feature = {
							"chromosome": 	fields[0],
							"position": 	parseInt(fields[1]), 
							"start": 		parseInt(fields[1]),//added
							"end": 			parseInt(fields[1]),//added
							"id":  			fields[2],
							"reference": 			fields[3],
							"alternate": 			fields[4],
							"quality": 		fields[5], 
							"filter": 		fields[6], 
							"info": 		fields[7].replace(/;/gi,"<br>"), 
							"format": 		fields[8],
							"sampleData":	line,
	//						"record":		fields,
	//						"label": 		fields[2] + " " +fields[3] + "/" + fields[4] + " Q:" + fields[5],
							"featureType":	"vcf"
					};
					
					this.featureCache.putFeatures(feature, dataType);
					
					if (this.featuresByChromosome[fields[0]] == null){
						this.featuresByChromosome[fields[0]] = 0;
					}
					this.featuresByChromosome[fields[0]]++;
					this.featuresCount++;
				}
			}
		}
	}
};

function MemoryStore(args) {

    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    // configurable parameters
//    this.limit = 500;

    // Now we set the args parameters
    _.extend(this, args);

    // internal parameters
    this.size = 0;
    this.store = {};
};

MemoryStore.prototype = {
    add: function (key, value) {
        if (typeof this.store === 'undefined') {
            this.store = {};
        }
        var item = {key: key, value: value};

        // a item can be overwritten
        this.store[key] = item;

        if (this.tail) {
            this.tail.newer = item;
            item.older = this.tail;
        } else {
            // the item is the first one
            this.head = item;
        }

        // add new item to the end of the linked list, it's now the freshest item.
        this.tail = item;

//        if (this.size === this.limit) {
//            // we hit the limit, remove the head
//            this.shift();
//        } else {
//            // increase the size counter
//            this.size++;
//        }
        this.size++;

    },
    shift: function () {
        // todo: handle special case when limit == 1
        var item = this.head;
        if (item) {
            if (this.head.newer) {
                this.head = this.head.newer;
                this.head.older = undefined;
            } else {
                this.head = undefined;
            }
            // Remove last strong reference to <item> and remove links from the purged
            // item being returned:
            item.newer = item.older = undefined;
            // delete is slow, but we need to do this to avoid uncontrollable growth:
            delete this.store[item.key];
        }
    },
    get : function(key) {
        // First, find our cache item
        var item = this.store[key];
        if (item === undefined) return; // Not cached. Sorry.
        // As <key> was found in the cache, register it as being requested recently
        if (item === this.tail) {
            // Already the most recenlty used item, so no need to update the list
            return item.value;
        }
        // HEAD--------------TAIL
        //   <.older   .newer>
        //  <--- add direction --
        //   A  B  C  <D>  E
        if (item.newer) {
            if (item === this.head){
                this.head = item.newer;
            }
            item.newer.older = item.older; // C <-- E.
        }
        if (item.older){
            item.older.newer = item.newer; // C. --> E
        }
        item.newer = undefined; // D --x
        item.older = this.tail; // D. --> E
        if (this.tail)
            this.tail.newer = item; // E. <-- D
        this.tail = item;
        return item.value;
    },

    init: function () {
        this.size = 0;
        this.store = {};
        this.head = undefined;
        this.tail = undefined;
    },
    clear: function () {
        this.store = null;
        this.init();
    }


//    get: function (key) {
//        if (typeof this.dataStore === 'undefined') {
//            return undefined;
//        } else {
//            var ms = this.counter++;
//            this.dataStore[key].ms = ms;
//            return this.dataStore[key].data;
//        }
//    },

//    addCollection: function (key, featureArray) {
//        // If 'featureArray' is an Array then we add all elements,
//        // otherwise we call to add()
//        if ($.isArray(featureArray)) {
//            if (typeof this.dataStore === 'undefined') {
//                this.dataStore = {};
//            }
//            for (var feature in featureArray) {
//                this.dataStore[key] = feature;
//                this.lru.push({key: key, ms: this.counter});
//            }
//        } else {
//            this.add(key, featureArray);
//        }
//    },

//    delete: function (key) {
//        if (typeof this.dataStore !== 'undefined') {
//            var aux = this.dataStore[key];
//            delete this.dataStore[key];
//            return aux;
//        }
//    },

//    free: function () {
//        this.lru = [];
//        for (var i in this.dataStore) {
//            this.lru.push({key: i, ms: this.dataStore[i].ms});
//        }
//        this.lru.sort(function (a, b) {
//            return a.ms - b.ms;
//        });
//        this.delete(this.lru[0].key);
//        this.lru.splice(0, 1);
//    },
//
//    close: function () {
//        this.dataStore = null;
//    }
};
function FeatureChunkCache(args) {
    _.extend(this, Backbone.Events);

    // Default values
    this.id = Utils.genId("FeatureChunkCache");

    this.chunkSize = 50000;
    this.limit;

    _.extend(this, args);

    this.store = new MemoryStore({});

    this.verbose = false;
}


FeatureChunkCache.prototype = {

    getChunk: function (chunkId) {
        return this.store.get(chunkId);
    },

    getAdjustedRegion: function (region) {
        var start = this.getChunkId(region.start) * this.chunkSize;
        var end = (this.getChunkId(region.end) * this.chunkSize) + this.chunkSize - 1;

        return new Region({chromosome: region.chromosome, start: start, end: end});
    },


    getAdjustedRegions: function (region) {
        var firstChunkId = this.getChunkId(region.start);
        var lastChunkId = this.getChunkId(region.end);

        var regions = [], updateStart = true, updateEnd = true, chunkStart, chunkEnd;
        for (var chunkId = firstChunkId; chunkId <= lastChunkId; chunkId++) {
            var chunkKey = this.getChunkKey(region.chromosome, chunkId);
            var nextChunkKey = this.getChunkKey(region.chromosome, chunkId + 1);
            var chunk = this.getChunk(chunkKey);
            var nextChunk = this.getChunk(nextChunkKey);
            if (updateStart) {
                chunkStart = parseInt(chunkId * this.chunkSize);
                updateStart = false;
            }
            if (updateEnd) {
                chunkEnd = parseInt((chunkId * this.chunkSize) + this.chunkSize - 1);
                updateEnd = false;
            }

            if (!chunk) {
                updateEnd = true;
                if (nextChunk && chunkId < lastChunkId) {
                    var r = new Region({chromosome: region.chromosome, start: chunkStart, end: chunkEnd})
                    regions.push(r);
                    updateStart = true;
                }
                if (chunkId == lastChunkId) {
                    var r = new Region({chromosome: region.chromosome, start: chunkStart, end: chunkEnd})
                    regions.push(r);
                }
            } else {
                updateStart = true;
                updateEnd = true;
            }
        }
        return regions;
    },

    getByRegions: function (regions) {
        var chunks = [];
        for (var i in regions) {
            var chunkId = this.getChunkId(regions[i].start);
            var chunkKey = this.getChunkKey(regions[i].chromosome, chunkId);
            chunks.push(this.getChunk(chunkKey));
        }
        return chunks;
    },


    getCachedByRegion: function (region) {
        var chunkRegions = {cached: [], notCached: []};

        var firstChunkId = this.getChunkId(region.start);
        var lastChunkId = this.getChunkId(region.end);

        for (var chunkId = firstChunkId; chunkId <= lastChunkId; chunkId++) {
            var chunkKey = this.getChunkKey(region.chromosome, chunkId);
            var chunk = this.getChunk(chunkKey);

            var chunkRegionStart = parseInt(chunkId * this.chunkSize) || 1;
            var chunkRegionEnd = parseInt(chunkId * this.chunkSize + this.chunkSize - 1);
            var chunkRegion = new Region({chromosome: region.chromosome, start: chunkRegionStart, end: chunkRegionEnd});

            if (_.isUndefined(chunk)) {
                chunkRegions.notCached.push(chunkRegion);
            } else {
                chunkRegions.cached.push(chunkRegion);
            }

            if (this.verbose) {
                console.log(chunkRegions);
            }
        }
        return chunkRegions;
    },

    putChunk: function (chunkKey, value) {
        var value = {value: value, chunkKey: chunkKey};
        this.store.add(chunkKey, value);
        return value;
    },

    putByRegion: function (region, value) {
        var chunkId = this.getChunkId(region.start);
        var chunkKey = this.getChunkKey(region.chromosome, chunkId);
        return this.putChunk(chunkKey, value);
    },

    getChunkKey: function (chromosome, chunkId) {
        return chromosome + ":" + chunkId;
    },

    getChunkId: function (position) {
        return Math.floor(position / this.chunkSize);
    },


    getChunkSize: function () {
        return this.chunkSize;
    }


}
function FeatureCache(args) {
	this.args = args;
	this.id = Math.round(Math.random() * 10000000); // internal id for this class

	this.chunkSize = 50000;
	this.gzip = true;
	this.maxSize = 10*1024*1024;
	this.size = 0;
	
	if (args != null){
		if(args.chunkSize != null){
			this.chunkSize = args.chunkSize;
		}
		if(args.gzip != null){
			this.gzip = args.gzip;
		}
	}
	
	this.cache = {};
	this.chunksDisplayed = {};
	
	this.maxFeaturesInterval = 0;
	
	//XXX
	this.gzip = false;

};

FeatureCache.prototype._getChunk = function(position){
	return Math.floor(position/this.chunkSize);
};

FeatureCache.prototype.getChunkRegion = function(region){
	start = this._getChunk(region.start) * this.chunkSize;
	end = (this._getChunk(region.end) * this.chunkSize) + this.chunkSize-1;
	return {start:start,end:end};
};

FeatureCache.prototype.getFirstFeature = function(){
	var feature;
	if(this.gzip) {
		feature = JSON.parse(RawDeflate.inflate(this.cache[Object.keys(this.cache)[0]].data[0]));
	}else{
		feature = this.cache[Object.keys(this.cache)[0]].data[0];
	}
	return feature;
};


//new 
FeatureCache.prototype.getFeatureChunk = function(key){
	if(this.cache[key] != null) {
		return this.cache[key];
	}
	return null;
};
FeatureCache.prototype.getFeatureChunkByDataType = function(key,dataType){
	if(this.cache[key] != null) {
        if(this.cache[key][dataType] != null){
		    return this.cache[key][dataType];
        }
	}
	return null;
};
//new
FeatureCache.prototype.getFeatureChunksByRegion = function(region){
	var firstRegionChunk, lastRegionChunk,  chunks = [], key;
	firstRegionChunk = this._getChunk(region.start);
	lastRegionChunk = this._getChunk(region.end);
	for(var i=firstRegionChunk; i<=lastRegionChunk; i++){
		key = region.chromosome+":"+i;
		// check if this key exists in cache (features from files)
		if(this.cache[key] != null ){
			chunks.push(this.cache[key]);
		}
		
	}
	//if(chunks.length == 0){
		//return null;
	//}
	return chunks;
};


FeatureCache.prototype.putFeaturesByRegion = function(featureDataList, region, featureType, dataType){
	var key, firstRegionChunk, lastRegionChunk, firstChunk, lastChunk, feature, gzipFeature;


	//initialize region
	firstRegionChunk = this._getChunk(region.start);
	lastRegionChunk = this._getChunk(region.end);

	for(var i=firstRegionChunk; i<=lastRegionChunk; i++){
		key = region.chromosome+":"+i;
		if(this.cache[key]==null){
			this.cache[key] = {};
			this.cache[key].key = key;
		}
//        else{
//            // TODO
//            console.log(region.chromosome+region.start+region.end+'-'+featureType+'-'+dataType);
////            return;
//        }
		if(this.cache[key][dataType]==null){
			this.cache[key][dataType] = [];
		}
	}

    //Check if is a single object
    if(featureDataList.constructor != Array){
        featureDataList = [featureDataList];
    }

    //loop over features and set on corresponding chunks
	for(var index = 0, len = featureDataList.length; index<len; index++) {
		feature = featureDataList[index];
		feature.featureType = featureType;
		firstChunk = this._getChunk(feature.start);
		lastChunk = this._getChunk(feature.end);
		
		if(this.gzip) {
			gzipFeature = RawDeflate.deflate(JSON.stringify(feature));
		}else{
			gzipFeature = feature;
		}
		
		for(var i=firstChunk; i<=lastChunk; i++) {
			if(i >= firstRegionChunk && i<= lastRegionChunk){//only if is inside the called region
				key = region.chromosome+":"+i;
				this.cache[key][dataType].push(gzipFeature);
			}
		}
	}
//        console.log(this.cache[region.chromosome+":"+firstRegionChunk][dataType].length)
};


//used by BED, GFF, VCF
FeatureCache.prototype.putFeatures = function(featureDataList, dataType){
	var feature, key, firstChunk, lastChunk;

	//Check if is a single object
	if(featureDataList.constructor != Array){
		featureDataList = [featureDataList];
	}

	for(var index = 0, len = featureDataList.length; index<len; index++) {
		feature = featureDataList[index];
		firstChunk = this._getChunk(feature.start);
		lastChunk = this._getChunk(feature.end);
		for(var i=firstChunk; i<=lastChunk; i++) {
			key = feature.chromosome+":"+i;
			if(this.cache[key]==null){
				this.cache[key] = [];
				this.cache[key].key = key;
			}
			if(this.cache[key][dataType]==null){
				this.cache[key][dataType] = [];
			}
			if(this.gzip) {
				this.cache[key][dataType].push(RawDeflate.deflate(JSON.stringify(feature)));
			}else{
				this.cache[key][dataType].push(feature);
			}

		}
	}
};



FeatureCache.prototype.putChunk = function(key, item){
	this.cache[key] = item;
};

FeatureCache.prototype.getChunk = function(key){
	return this.cache[key];
};

FeatureCache.prototype.putCustom = function(f){
	f(this);
};

FeatureCache.prototype.getCustom = function(f){
	f(this);
};



FeatureCache.prototype.remove = function(region){
	var firstChunk = this._getChunk(region.start);
	var lastChunk = this._getChunk(region.end);
	for(var i=firstChunk; i<=lastChunk; i++){
		var key = region.chromosome+":"+i;
		this.cache[key] = null;
	}
};

FeatureCache.prototype.clear = function(){
		this.size = 0;		
		this.cache = {};
};


//END



//THOSE METHODS ARE NOT USED



/*
FeatureCache.prototype.getFeaturesByChunk = function(key, dataType){
	var features =  [];
	var feature, firstChunk, lastChunk;
	
	if(this.cache[key] != null && this.cache[key][dataType] != null) {
		for ( var i = 0, len = this.cache[key][dataType].length; i < len; i++) {
			if(this.gzip) {
				feature = JSON.parse(RawDeflate.inflate(this.cache[key][dataType][i]));
			}else{
				feature = this.cache[key][dataType][i];
			}
			
			//check if any feature chunk has been already displayed 
			var displayed = false;
			firstChunk = this._getChunk(feature.start);
			lastChunk = this._getChunk(feature.end);
			for(var f=firstChunk; f<=lastChunk; f++){
				var fkey = feature.chromosome+":"+f;
				if(this.chunksDisplayed[fkey+dataType]==true){
					displayed = true;
					break;
				}
			}
			
			if(!displayed){
				features.push(feature);
				returnNull = false;
			}
		}
		this.chunksDisplayed[key+dataType]=true;
		return features;
	}
	
	return null;
};


FeatureCache.prototype.getFeaturesByRegion = function(region, dataType){
	var firstRegionChunk, lastRegionChunk, firstChunk, lastChunk, features = [], feature, key, returnNull = true, displayed;
	firstRegionChunk = this._getChunk(region.start);
	lastRegionChunk = this._getChunk(region.end);
	for(var i=firstRegionChunk; i<=lastRegionChunk; i++){
		key = region.chromosome+":"+i;
		 //check if this key exists in cache (features from files)
		if(this.cache[key] != null && this.cache[key][dataType] != null){
			for ( var j = 0, len = this.cache[key][dataType].length; j < len; j++) {
				if(this.gzip) {
					try {
						feature = JSON.parse(RawDeflate.inflate(this.cache[key][dataType][j]));
					} catch (e) {
						//feature es "" 
						console.log(e)
						debugger
						
					}
					
				}else{
					feature = this.cache[key][dataType][j];
				}
				// we only get those features in the region AND check if chunk has been already displayed
				if(feature.end > region.start && feature.start < region.end){

			//		 check displayCheck argument 
					if(region.displayedCheck != false){
				//		check if any feature chunk has been already displayed 
						displayed = false;
						firstChunk = this._getChunk(feature.start);
						lastChunk = this._getChunk(feature.end);
						for(var f=firstChunk; f<=lastChunk; f++){
							var fkey = region.chromosome+":"+f;
							if(this.chunksDisplayed[fkey+dataType]==true){
								displayed = true;
								break;
							}
						}
						
						if(!displayed){
							features.push(feature);
							returnNull = false;
						}
					}else{
						features.push(feature);
						returnNull = false;
					}

					
				}
			}
		}
		 //check displayCheck argument 
		if(region.displayedCheck != false){
			this.chunksDisplayed[key+dataType]=true;//mark chunk as displayed
		}
	}
	if(returnNull){
		return null;
	}else{
		return features;
	}
};
*/




/*

FeatureCache.prototype.putChunk = function(featureDataList, chunkRegion, dataType){
	var feature, key, chunk;
	chunk = this._getChunk(chunkRegion.start);
	key = chunkRegion.chromosome+":"+chunk;

	if(this.cache[key]==null){
		this.cache[key] = [];
	}
	if(this.cache[key][dataType]==null){
		this.cache[key][dataType] = [];
	}

	if(featureDataList.constructor == Object){
		if(this.gzip) {
			this.cache[key][dataType].push(RawDeflate.deflate(JSON.stringify(featureDataList)));
		}else{
			this.cache[key][dataType].push(featureDataList);
		}
	}else{
		for(var index = 0, len = featureDataList.length; index<len; index++) {
			feature = featureDataList[index];
			if(this.gzip) {
				this.cache[key][dataType].push(RawDeflate.deflate(JSON.stringify(feature)));
			}else{
				this.cache[key][dataType].push(feature);
			}
		}
	}
	
};

*/


//NOT USED dev not tested
//FeatureCache.prototype.histogram = function(region, interval){
//
	//var intervals = (region.end-region.start+1)/interval;
	//var intervalList = [];
	//
	//for ( var i = 0; i < intervals; i++) {
		//var featuresInterval = 0;
		//
		//var intervalStart = i*interval;//deberia empezar en 1...
		//var intervalEnd = ((i+1)*interval)-1;
		//
		//var firstChunk = this._getChunk(intervalStart+region.start);
		//var lastChunk = this._getChunk(intervalEnd+region.start);
		//
		//console.log(this.cache);
		//for(var j=firstChunk; j<=lastChunk; j++){
			//var key = region.chromosome+":"+j;
			//console.log(key);
			//console.log(this.cache[key]);
			//for ( var k = 0, len = this.cache[key].length; k < len; k++) {
				//if(this.gzip) {
					//feature = JSON.parse(RawDeflate.inflate(this.cache[key][k]));
				//}else{
					//feature = this.cache[key][k];
				//}
				//if(feature.start > intervalStart && feature.start < intervalEnd);
				//featuresInterval++;
			//}
			//
		//}
		//intervalList[i]=featuresInterval;
		//
		//if(this.maxFeaturesInterval<featuresInterval){
			//this.maxFeaturesInterval = featuresInterval;
		//}
	//}
	//
	//for ( var inter in  intervalList) {
		//intervalList[inter]=intervalList[inter]/this.maxFeaturesInterval;
	//}
//};

BamCache.prototype.putHistogramFeaturesByRegion = FeatureCache.prototype.putFeaturesByRegion;

function BamCache(args) {
	this.args = args;
	this.id = Math.round(Math.random() * 10000000); // internal id for this class

	this.chunkSize = 50000;
	this.gzip = true;
	this.maxSize = 10*1024*1024;
	this.size = 0;
	
	if (args != null){
		if(args.chunkSize != null){
			this.chunkSize = args.chunkSize;
		}
		if(args.gzip != null){
			this.gzip = args.gzip;
		}
	}
	
	this.cache = {};

	//deprecated trackSvg has this object now
	//this.chunksDisplayed = {};
	
	this.maxFeaturesInterval = 0;//for local histogram
	
	//XXX
	this.gzip = false;
};

BamCache.prototype._getChunk = function(position){
	return Math.floor(position/this.chunkSize);
};

//new 
BamCache.prototype.getFeatureChunk = function(key){
	if(this.cache[key] != null) {
		return this.cache[key];
	}
	return null;
};
//new
BamCache.prototype.getFeatureChunksByRegion = function(region){
	var firstRegionChunk, lastRegionChunk,  chunks = [], key;
	firstRegionChunk = this._getChunk(region.start);
	lastRegionChunk = this._getChunk(region.end);
	for(var i=firstRegionChunk; i<=lastRegionChunk; i++){
		key = region.chromosome+":"+i;
		// check if this key exists in cache (features from files)
		if(this.cache[key] != null ){
			chunks.push(this.cache[key]);
		}
		
	}
	//if(chunks.length == 0){
		//return null;
	//}
	return chunks;
};



BamCache.prototype.putFeaturesByRegion = function(resultObj, region, featureType, dataType){
	var key, firstChunk, lastChunk, firstRegionChunk, lastRegionChunk, read, gzipRead;
	var reads = resultObj.reads;
	var coverage = resultObj.coverage;
	
	//initialize region
	firstRegionChunk = this._getChunk(region.start);
	lastRegionChunk = this._getChunk(region.end);
	
	var chunkIndex = 0;
	console.time("BamCache.prototype.putFeaturesByRegion1")
	//TODO the region for now is a chunk region, so this for is always 1 loop
	for(var i=firstRegionChunk, c=0; i<=lastRegionChunk; i++, c++){
		key = region.chromosome+":"+i;
		if(this.cache[key]==null || this.cache[key][dataType] == null){
			this.cache[key] = {};
			this.cache[key][dataType] = [];
			this.cache[key].key = key;
			this.cache[key].start = parseInt(region.start)+(c*this.chunkSize);
			this.cache[key].end = parseInt(region.start)+((c+1)*this.chunkSize)-1;
		}
        if(dataType === 'data'){
            //divide the coverage array in multiple arrays of chunksize length
    //		var chunkCoverage = coverage.slice(chunkIndex,chunkIndex+this.chunkSize);
            var chunkCoverageAll = coverage.all.slice(chunkIndex,chunkIndex+this.chunkSize);
            var chunkCoverageA = coverage.a.slice(chunkIndex,chunkIndex+this.chunkSize);
            var chunkCoverageC = coverage.c.slice(chunkIndex,chunkIndex+this.chunkSize);
            var chunkCoverageG = coverage.g.slice(chunkIndex,chunkIndex+this.chunkSize);
            var chunkCoverageT = coverage.t.slice(chunkIndex,chunkIndex+this.chunkSize);
            var chunkCoverage = {
                "all":chunkCoverageAll,
                "a":chunkCoverageA,
                "c":chunkCoverageC,
                "g":chunkCoverageG,
                "t":chunkCoverageT
            };
        }

		if(this.gzip) {
			this.cache[key]["coverage"]=RawDeflate.deflate(JSON.stringify(chunkCoverage));
		}else{
			this.cache[key]["coverage"]=chunkCoverage;
		}
		chunkIndex+=this.chunkSize;
	}
	console.timeEnd("BamCache.prototype.putFeaturesByRegion1")
	console.time("BamCache.prototype.putFeaturesByRegion")
	var ssss = 0;


    if(dataType === 'data'){
        for(var index = 0, len = reads.length; index<len; index++) {
            read = reads[index];
            read.featureType = 'bam';
            firstChunk = this._getChunk(read.start);
            lastChunk = this._getChunk(read.end == 0?read.end=-1:read.end);//0 is not a position, i set to -1 to avoid enter in for
    //		Some reads has end = 0. So will not be drawn IGV does not draw those reads

            if(this.gzip) {
                gzipRead = RawDeflate.deflate(JSON.stringify(read));
                //ssss+= gzipRead.length;
            }else{
                gzipRead = read;
                //ssss+= JSON.stringify(gzipRead).length;
            }

            for(var i=firstChunk, c=0; i<=lastChunk; i++, c++) {
                if(i >= firstRegionChunk && i<= lastRegionChunk){//only if is inside the called region
                    key = read.chromosome+":"+i;
//                    if(this.cache[key].start==null){
//                        this.cache[key].start = parseInt(region.start)+(c*this.chunkSize);
//                    }
//                    if(this.cache[key].end==null){
//                        this.cache[key].end = parseInt(region.start)+((c+1)*this.chunkSize)-1;
//                    }
//                    if(this.cache[key][dataType] != null){
//                        this.cache[key][dataType] = [];
                        this.cache[key][dataType].push(gzipRead);
//                    }

                }
            }
        }
    }


	console.timeEnd("BamCache.prototype.putFeaturesByRegion");
	console.log("BamCache.prototype.putFeaturesByRegion"+ssss)
};

BamCache.prototype.clear = function(){
	this.size = 0;		
	this.cache = {};
	console.log("bamCache cleared")
};

/*
BamCache.prototype.getFeaturesByChunk = function(key, dataType){
	var features =  [];
	var feature, firstChunk, lastChunk, chunk;
	var chr = key.split(":")[0], chunkId = key.split(":")[1];
	var region = {chromosome:chr,start:chunkId*this.chunkSize,end:chunkId*this.chunkSize+this.chunkSize-1};
	
	if(this.cache[key] != null && this.cache[key][dataType] != null) {
		if(this.gzip) {
			coverage = JSON.parse(RawDeflate.inflate(this.cache[key]["coverage"]));
		}else{
			coverage = this.cache[key]["coverage"];
		}
		
		for ( var i = 0, len = this.cache[key]["data"].length; i < len; i++) {
			if(this.gzip) {
				feature = JSON.parse(RawDeflate.inflate(this.cache[key]["data"][i]));
			}else{
				feature = this.cache[key]["data"][i];
			}
			
			//check if any feature chunk has been already displayed 
			var displayed = false;
			firstChunk = this._getChunk(feature.start);
			lastChunk = this._getChunk(feature.end);
			for(var f=firstChunk; f<=lastChunk; f++){
				var fkey = feature.chromosome+":"+f;
				if(this.chunksDisplayed[fkey+dataType]==true){
					displayed = true;
					break;
				}
			}
			
			if(!displayed){
				features.push(feature);
				returnNull = false;
			}
		}
		this.chunksDisplayed[key+dataType]=true;
		chunk = {reads:features,coverage:coverage,region:region};
		return chunk;
	}
	
};

BamCache.prototype.getFeaturesByRegion = function(region, dataType){
	var firstRegionChunk, lastRegionChunk, firstChunk, lastChunk, chunks = [], feature, key, coverage, features = [], displayed;
	firstRegionChunk = this._getChunk(region.start);
	lastRegionChunk = this._getChunk(region.end);
	for(var i=firstRegionChunk; i<=lastRegionChunk; i++){
		key = region.chromosome+":"+i;
		if(this.cache[key] != null){
			if(this.gzip) {
				coverage = JSON.parse(RawDeflate.inflate(this.cache[key]["coverage"]));
			}else{
				coverage = this.cache[key]["coverage"];
			}

			for ( var j = 0, len = this.cache[key]["data"].length; j < len; j++) {
				if(this.gzip) {
					feature = JSON.parse(RawDeflate.inflate(this.cache[key]["data"][j]));
				}else{
					feature = this.cache[key]["data"][j];
				}
				
				
//				check if any feature chunk has been already displayed 
				displayed = false;
				firstChunk = this._getChunk(feature.start);
				lastChunk = this._getChunk(feature.end);
				for(var f=firstChunk; f<=lastChunk; f++){
					var fkey = region.chromosome+":"+f;
					if(this.chunksDisplayed[fkey+dataType]==true){
						displayed = true;
						break;
					}
				}
				
				if(!displayed){
					features.push(feature);
				}
				
			}
		}
		this.chunksDisplayed[key+dataType]=true;//mark chunk as displayed
		chunks.push({reads:features,coverage:coverage,region:region});
	}
	return chunks;
};
*/



//BamCache.prototype.remove = function(region){
//	var firstChunk = this._getChunk(region.start);
//	var lastChunk = this._getChunk(region.end);
//	for(var i=firstChunk; i<=lastChunk; i++){
//		var key = region.chromosome+":"+i;
//		this.cache[key] = null;
//	}
//};
//

//
//BamCache.prototype.clearType = function(dataType){
//	this.cache[dataType] = null;
//};

function TrackListPanel(args) {//parent is a DOM div element
    var _this = this;

    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    //set default args
    this.id = Utils.genId("TrackListPanel");
    this.collapsed = false;
    this.collapsible = false;

    this.fontClass = 'ocb-font-sourcesanspro ocb-font-size-14';

    this.trackSvgList = [];
    this.swapHash = {};

    this.parentLayout;
    this.mousePosition;
    this.windowSize;

    this.zoomMultiplier = 1;
    this.showRegionOverviewBox = false;


    this.height = 0;

    //set instantiation args, must be last
    _.extend(this, args);

    //set new region object
    this.region = new Region(this.region);
    this.width -= 18;


    this.status;

    //this region is used to do not modify original region, and will be used by trackSvg
    this.visualRegion = new Region(this.region);

    /********/
    this._setPixelBase();
    /********/

    this.on(this.handlers);

    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }

};

TrackListPanel.prototype = {
    show: function () {
        $(this.div).css({display: 'block'});
    },

    hide: function () {
        $(this.div).css({display: 'none'});
    },
    setVisible: function (bool) {
        if (bool) {
            $(this.div).css({display: 'block'});
        } else {
            $(this.div).css({display: 'none'});
        }
    },
    setTitle: function (title) {
        if ('titleDiv' in this) {
            $(this.titleDiv).html(title);
        }
    },
    showContent: function () {
        $(this.tlHeaderDiv).css({display: 'block'});
        $(this.panelDiv).css({display: 'block'});
        this.collapsed = false;
        $(this.collapseDiv).removeClass('active');
        $(this.collapseDiv).children().first().removeClass('glyphicon-plus');
        $(this.collapseDiv).children().first().addClass('glyphicon-minus');
    },
    hideContent: function () {
        $(this.tlHeaderDiv).css({display: 'none'});
        $(this.panelDiv).css({display: 'none'});
        this.collapsed = true;
        $(this.collapseDiv).addClass('active');
        $(this.collapseDiv).children().first().removeClass('glyphicon-minus');
        $(this.collapseDiv).children().first().addClass('glyphicon-plus');
    },
    render: function (targetId) {
        this.targetId = (targetId) ? targetId : this.targetId;
        if ($('#' + this.targetId).length < 1) {
            console.log('targetId not found in DOM');
            return;
        }
        var _this = this;

        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div id="tracklist-panel" style="height:100%;position: relative;"></div>')[0];
        $(this.targetDiv).append(this.div);

        if ('title' in this && this.title !== '') {
            var titleDiv = $('<div id="tl-title" class="gv-panel-title unselectable"><div style="display:inline-block;line-height: 24px;margin-left: 5px;width:120px">' + this.title + '</div></div>')[0];
            $(this.div).append(titleDiv);
            var windowSizeDiv = $('<div style="display:inline;margin-left:35%" id="windowSizeSpan"></div>');
            $(titleDiv).append(windowSizeDiv);

            if (this.collapsible == true) {
                this.collapseDiv = $('<div type="button" class="btn btn-default btn-xs pull-right" style="display:inline;margin:2px;height:20px"><span class="glyphicon glyphicon-minus"></span></div>');
                $(titleDiv).dblclick(function () {
                    if (_this.collapsed) {
                        _this.showContent();
                    } else {
                        _this.hideContent();
                    }
                });
                $(this.collapseDiv).click(function () {
                    if (_this.collapsed) {
                        _this.showContent();
                    } else {
                        _this.hideContent();
                    }
                });
                $(titleDiv).append(this.collapseDiv);
            }

        }

        var tlHeaderDiv = $('<div id="tl-header" class="unselectable"></div>')[0];

        var panelDiv = $('<div id="tl-panel"></div>')[0];
        $(panelDiv).css({position: 'relative', width: '100%'});


        this.tlTracksDiv = $('<div id="tl-tracks"></div>')[0];
        $(this.tlTracksDiv).css({ position: 'relative', 'z-index': 3});


        $(this.div).append(tlHeaderDiv);
        $(this.div).append(panelDiv);

        $(panelDiv).append(this.tlTracksDiv);


        //Main SVG and his events
        this.svgTop = SVG.init(tlHeaderDiv, {
            "width": this.width,
            "height": 12
        });

        var mid = this.width / 2;
        var yOffset = 11;
        this.positionText = SVG.addChild(this.svgTop, 'text', {
            'x': mid - 30,
            'y': yOffset,
            'fill': 'steelblue',
            'class': this.fontClass
        });
        this.nucleotidText = SVG.addChild(this.svgTop, 'text', {
            'x': mid + 35,
            'y': yOffset,
            'class': this.fontClass
        });
        this.firstPositionText = SVG.addChild(this.svgTop, 'text', {
            'x': 0,
            'y': yOffset,
            'fill': 'steelblue',
            'class': this.fontClass
        });
        this.lastPositionText = SVG.addChild(this.svgTop, 'text', {
            'x': this.width - 70,
            'y': yOffset,
            'fill': 'steelblue',
            'class': this.fontClass
        });
//        this.viewNtsArrow = SVG.addChild(this.svgTop, 'rect', {
//            'x': 2,
//            'y': 6,
//            'width': this.width - 4,
//            'height': 2,
//            'opacity': '0.5',
//            'fill': 'black'
//        });
//        this.viewNtsArrowLeft = SVG.addChild(this.svgTop, 'polyline', {
//            'points': '0,1 2,1 2,13 0,13',
//            'opacity': '0.5',
//            'fill': 'black'
//        });
//        this.viewNtsArrowRight = SVG.addChild(this.svgTop, 'polyline', {
//            'points': this.width + ',1 ' + (this.width - 2) + ',1 ' + (this.width - 2) + ',13 ' + this.width + ',13',
//            'opacity': '0.5',
//            'fill': 'black'
//        });
        this.windowSize = 'Window size: ' + Utils.formatNumber(this.region.length()) + ' nts';
//        this.viewNtsTextBack = SVG.addChild(this.svgTop, 'rect', {
//            'x': mid - 40,
//            'y': 0,
//            'width': 0,
//            'height': 13,
//            'fill': 'white'
//        });
        this.viewNtsText = SVG.addChild(this.svgTop, 'text', {
            'x': mid - (this.windowSize.length * 7 / 2),
            'y': 11,
            'fill': 'black',
            'class': this.fontClass
        });
        this.viewNtsText.setAttribute('hidden');
//        this.viewNtsTextBack.setAttribute('width', $(this.viewNtsText).width() + 15);
        this.viewNtsText.textContent = this.windowSize;
        $(this.div).find('#windowSizeSpan').html(this.windowSize);
        this._setTextPosition();


        this.centerLine = $('<div id="' + this.id + 'centerLine"></div>')[0];
        $(panelDiv).append(this.centerLine);
        $(this.centerLine).css({
            'z-index': 2,
            'position': 'absolute',
            'left': mid - 1,
            'top': 0,
            'width': this.pixelBase,
//            'height': '100%',
            'height': 'calc(100% - 8px)',
            'opacity': 0.5,
            'border': '1px solid orangered',
            'background-color': 'orange'
        });


        this.mouseLine = $('<div id="' + this.id + 'mouseLine"></div>')[0];
        $(panelDiv).append(this.mouseLine);
        $(this.mouseLine).css({
            'z-index': 1,
            'position': 'absolute',
            'left': -20,
            'top': 0,
            'width': this.pixelBase,
            'height': 'calc(100% - 8px)',
            'border': '1px solid lightgray',
            'opacity': 0.7,
            'visibility': 'hidden',
            'background-color': 'gainsboro'
        });

        //allow selection in trackSvgLayoutOverview


        var selBox = $('<div id="' + this.id + 'selBox"></div>')[0];
        $(panelDiv).append(selBox);
        $(selBox).css({
            'z-index': 0,
            'position': 'absolute',
            'left': 0,
            'top': 0,
            'height': '100%',
            'border': '2px solid deepskyblue',
            'opacity': 0.5,
            'visibility': 'hidden',
            'background-color': 'honeydew'
        });

        if (this.showRegionOverviewBox) {
            var regionOverviewBoxLeft = $('<div id="' + this.id + 'regionOverviewBoxLeft"></div>')[0];
            var regionOverviewBoxRight = $('<div id="' + this.id + 'regionOverviewBoxRight"></div>')[0];
            $(panelDiv).append(regionOverviewBoxLeft);
            $(panelDiv).append(regionOverviewBoxRight);
            var regionOverviewBoxWidth = this.region.length() * this.pixelBase;
            var regionOverviewDarkBoxWidth = (this.width - regionOverviewBoxWidth) / 2
            $(regionOverviewBoxLeft).css({
                'z-index': 0,
                'position': 'absolute',
                'left': 1,
                'top': 0,
                'width': regionOverviewDarkBoxWidth,
                'height': 'calc(100% - 8px)',
                'border': '1px solid gray',
                'opacity': 0.5,
                //            'visibility': 'hidden',
                'background-color': 'lightgray'
            });
            $(regionOverviewBoxRight).css({
                'z-index': 0,
                'position': 'absolute',
                'left': (regionOverviewDarkBoxWidth + regionOverviewBoxWidth),
                'top': 0,
                'width': regionOverviewDarkBoxWidth,
                'height': 'calc(100% - 8px)',
                'border': '1px solid gray',
                'opacity': 0.5,
                //            'visibility': 'hidden',
                'background-color': 'lightgray'
            });
        }


        $(this.div).mousemove(function (event) {
            var centerPosition = _this.region.center();
            var mid = _this.width / 2;
            var mouseLineOffset = _this.pixelBase / 2;
            var offsetX = (event.clientX - $(_this.tlTracksDiv).offset().left);
            var cX = offsetX - mouseLineOffset;
            var rcX = (cX / _this.pixelBase) | 0;
            var pos = (rcX * _this.pixelBase) + (mid % _this.pixelBase) - 1;
            $(_this.mouseLine).css({'left': pos});
//
            var posOffset = (mid / _this.pixelBase) | 0;
            _this.mousePosition = centerPosition + rcX - posOffset;
            _this.trigger('mousePosition:change', {mousePos: _this.mousePosition, baseHtml: _this.getMousePosition(_this.mousePosition)});
        });

        $(this.tlTracksDiv).dblclick(function (event) {
            var halfLength = _this.region.length() / 2;
            var mouseRegion = new Region({chromosome: _this.region.chromosome, start: _this.mousePosition - halfLength, end: _this.mousePosition + halfLength})
            _this.trigger('region:change', {region: mouseRegion, sender: _this});
        });

        var downX, moveX;
        $(this.tlTracksDiv).mousedown(function (event) {
            $('html').addClass('unselectable');
//                            $('.qtip').qtip('hide').qtip('disable'); // Hide AND disable all tooltips
            $(_this.mouseLine).css({'visibility': 'hidden'});

            var mouseState = event.which;
            if (event.ctrlKey) {
                mouseState = 'ctrlKey' + event.which;
            }
            switch (mouseState) {
                case 1: //Left mouse button pressed
                    $(this).css({"cursor": "move"});
                    downX = event.clientX;
                    var lastX = 0;
                    $(this).mousemove(function (event) {
                        var newX = (downX - event.clientX) / _this.pixelBase | 0;//truncate always towards zero
                        if (newX != lastX) {
                            var disp = lastX - newX;
                            var centerPosition = _this.region.center();
                            var p = centerPosition - disp;
                            if (p > 0) {//avoid 0 and negative positions
                                _this.region.start -= disp;
                                _this.region.end -= disp;
                                _this._setTextPosition();
                                //						_this.onMove.notify(disp);
                                _this.trigger('region:move', {region: _this.region, disp: disp, sender: _this});
                                _this.trigger('trackRegion:move', {region: _this.region, disp: disp, sender: _this});
                                lastX = newX;
                                _this.setNucleotidPosition(p);
                            }
                        }
                    });

                    break;
                case 2: //Middle mouse button pressed
                case 'ctrlKey1': //ctrlKey and left mouse button
                    $(selBox).css({'visibility': 'visible'});
                    $(selBox).css({'width': 0});
                    downX = (event.pageX - $(_this.tlTracksDiv).offset().left);
                    $(selBox).css({"left": downX});
                    $(this).mousemove(function (event) {
                        moveX = (event.pageX - $(_this.tlTracksDiv).offset().left);
                        if (moveX < downX) {
                            $(selBox).css({"left": moveX});
                        }
                        $(selBox).css({"width": Math.abs(moveX - downX)});
                    });


                    break;
                case 3: //Right mouse button pressed
                    break;
                default: // other button?
            }


        });

        $(this.tlTracksDiv).mouseup(function (event) {
            $('html').removeClass("unselectable");
            $(this).css({"cursor": "default"});
            $(_this.mouseLine).css({'visibility': 'visible'});
            $(this).off('mousemove');

            var mouseState = event.which;
            if (event.ctrlKey) {
                mouseState = 'ctrlKey' + event.which;
            }
            switch (mouseState) {
                case 1: //Left mouse button pressed

                    break;
                case 2: //Middle mouse button pressed
                case 'ctrlKey1': //ctrlKey and left mouse button
                    $(selBox).css({'visibility': 'hidden'});
                    $(this).off('mousemove');
                    if (downX != null && moveX != null) {
                        var ss = downX / _this.pixelBase;
                        var ee = moveX / _this.pixelBase;
                        ss += _this.visualRegion.start;
                        ee += _this.visualRegion.start;
                        _this.region.start = parseInt(Math.min(ss, ee));
                        _this.region.end = parseInt(Math.max(ss, ee));
                        _this.trigger('region:change', {region: _this.region, sender: _this});
                        moveX = null;
                    } else if (downX != null && moveX == null) {
                        var mouseRegion = new Region({chromosome: _this.region.chromosome, start: _this.mousePosition, end: _this.mousePosition})
                        _this.trigger('region:change', {region: mouseRegion, sender: _this});
                    }
                    break;
                case 3: //Right mouse button pressed
                    break;
                default: // other button?
            }

        });

        $(this.tlTracksDiv).mouseleave(function (event) {
            $(this).css({"cursor": "default"});
            $(_this.mouseLine).css({'visibility': 'hidden'});
            $(this).off('mousemove');
            $("body").off('keydown.genomeViewer');

            $(selBox).css({'visibility': 'hidden'});
            downX = null;
            moveX = null;
        });

        $(this.tlTracksDiv).mouseenter(function (e) {
//            $('.qtip').qtip('enable'); // To enable them again ;)
            $(_this.mouseLine).css({'visibility': 'visible'});
            $("body").off('keydown.genomeViewer');
            enableKeys();
        });

        var enableKeys = function () {
            //keys
            $("body").bind('keydown.genomeViewer', function (e) {
                var disp = 0;
                switch (e.keyCode) {
                    case 37://left arrow
                        if (e.ctrlKey) {
                            disp = Math.round(100 / _this.pixelBase);
                        } else {
                            disp = Math.round(10 / _this.pixelBase);
                        }
                        break;
                    case 39://right arrow
                        if (e.ctrlKey) {
                            disp = Math.round(-100 / _this.pixelBase)
                        } else {
                            disp = Math.round(-10 / _this.pixelBase);
                        }
                        break;
                }
                if (disp != 0) {
                    _this.region.start -= disp;
                    _this.region.end -= disp;
                    _this._setTextPosition();
//					_this.onMove.notify(disp);
                    _this.trigger('region:move', {region: _this.region, disp: disp, sender: _this});
                    _this.trigger('trackRegion:move', {region: _this.region, disp: disp, sender: _this});
                }
            });
        };

        this.tlHeaderDiv = tlHeaderDiv;
        this.panelDiv = panelDiv;

        this.rendered = true;
    },

    setHeight: function (height) {
//        this.height=Math.max(height,60);
//        $(this.tlTracksDiv).css('height',height);
//        //this.grid.setAttribute("height",height);
//        //this.grid2.setAttribute("height",height);
//        $(this.centerLine).css("height",parseInt(height));//25 es el margen donde esta el texto de la posicion
//        $(this.mouseLine).css("height",parseInt(height));//25 es el margen donde esta el texto de la posicion
    },

    setWidth: function (width) {
        console.log(width);
        this.width = width - 18;
        var mid = this.width / 2;
        this._setPixelBase();

        $(this.centerLine).css({'left': mid - 1, 'width': this.pixelBase});
        $(this.mouseLine).css({'width': this.pixelBase});

        this.svgTop.setAttribute('width', this.width);
        this.positionText.setAttribute("x", mid - 30);
        this.nucleotidText.setAttribute("x", mid + 35);
        this.lastPositionText.setAttribute("x", this.width - 70);
//        this.viewNtsArrow.setAttribute("width", this.width - 4);
//        this.viewNtsArrowRight.setAttribute("points", this.width + ",1 " + (this.width - 2) + ",1 " + (this.width - 2) + ",13 " + this.width + ",13");
        this.viewNtsText.setAttribute("x", mid - (this.windowSize.length * 7 / 2));
//        this.viewNtsTextBack.setAttribute("x", mid - 40);
        this.trigger('trackWidth:change', {width: this.width, sender: this})

        this._setTextPosition();
    },

    highlight: function (event) {
        this.trigger('trackFeature:highlight', event)
    },


    moveRegion: function (event) {
        this.region.load(event.region);
        this.visualRegion.load(event.region);
        this._setTextPosition();
        this.trigger('trackRegion:move', event);
    },

    setSpecies: function (species) {
        this.species = species;
        this.trigger('trackSpecies:change', {species: species, sender: this})
    },

    setRegion: function (region) {//item.chromosome, item.position, item.species
        var _this = this;
        this.region.load(region);
        this.visualRegion.load(region);
        this._setPixelBase();
        //get pixelbase by Region


        $(this.centerLine).css({'width': this.pixelBase});
        $(this.mouseLine).css({'width': this.pixelBase});

        this.windowSize = "Window size: " + Utils.formatNumber(this.region.length()) + " nts";
        this.viewNtsText.textContent = this.viewNtsText.textContent;
        $(this.div).find('#windowSizeSpan').html(this.windowSize);
        this._setTextPosition();
        this.trigger('window:size', {windowSize: this.windowSize});

//        if (region.species != null) {
//            //check species and modify CellBaseAdapter, clean cache
//            for (i in this.trackSvgList) {
//                if (this.trackSvgList[i].trackData.adapter instanceof CellBaseAdapter ||
//                    this.trackSvgList[i].trackData.adapter instanceof SequenceAdapter
//                    ) {
//                    this.trackSvgList[i].trackData.adapter.species = region.species;
//                    //this.trackSvgList[i].trackData.adapter.featureCache.clear();
//
//                    this.trackSvgList[i].trackData.adapter.clearData();
//                }
//            }
//        }
        this.trigger('trackRegion:change', {region: this.visualRegion, sender: this})

        this.nucleotidText.textContent = "";//remove base char, will be drawn later if needed

        this.status = 'rendering';

//        this.onRegionChange.notify();

        //this.minRegionRect.setAttribute("width",this.minRectWidth);
        //this.minRegionRect.setAttribute("x",(this.width/2)-(this.minRectWidth/2)+6);
    },

    draw: function () {
        this.trigger('track:draw', {sender: this});
    },
    checkTracksReady: function () {
        var _this = this;
        /************ Loading ************/
        var checkAllTrackStatus = function (status) {
            for (i in _this.trackSvgList) {
                if (_this.trackSvgList[i].status != status) return false;
            }
            return true;
        };
        if (checkAllTrackStatus('ready')) {
//            console.log('all ready')
            this.status = 'ready';
            _this.trigger('tracks:ready', {sender: _this});
        }
//        var checkStatus = function () {
//            if (checkAllTrackStatus('ready')) {
//                _this.trigger('tracks:ready', {sender: _this});
//            } else {
//                setTimeout(checkStatus, 100);
//            }
//        };
//        setTimeout(checkStatus, 10);
        /***************************/
    },
    addTrack: function (track) {
        if (_.isArray(track)) {
            for (var i in track) {
                this._addTrack(track[i]);
            }
        } else {
            this._addTrack(track);
        }
    },
    _addTrack: function (track) {
        if (!this.rendered) {
            console.info(this.id + ' is not rendered yet');
            return;
        }
        var _this = this;

        var i = this.trackSvgList.push(track);
        this.swapHash[track.id] = {index: i - 1, visible: true};

        track.set('pixelBase', this.pixelBase);
        track.set('region', this.visualRegion);
        track.set('width', this.width);

        // Track must be initialized after we have created
        // de DIV element in order to create the elements in the DOM
        if (!track.rendered) {
            track.render(this.tlTracksDiv);
        }

        // Once tack has been initialize we can call draw() function
        track.draw();


        //trackEvents
        track.set('track:draw', function (event) {
            track.draw();
        });


        track.set('trackSpecies:change', function (event) {
            track.setSpecies(event.species);
        });


        track.set('trackRegion:change', function (event) {
            track.set('pixelBase', _this.pixelBase);
            track.set('region', event.region);
            track.draw();
        });


        track.set('trackRegion:move', function (event) {
            track.set('region', event.region);
            track.set('pixelBase', _this.pixelBase);
            track.move(event.disp);
        });


        track.set('trackWidth:change', function (event) {
            track.setWidth(event.width);
            track.set('pixelBase', _this.pixelBase);
            track.draw();
        });


        track.set('trackFeature:highlight', function (event) {


            var attrName = event.attrName || 'feature_id';
            if ('attrValue' in event) {
                event.attrValue = ($.isArray(event.attrValue)) ? event.attrValue : [event.attrValue];
                for (var key in event.attrValue) {
                    var queryStr = attrName + '~=' + event.attrValue[key];
                    var group = $(track.svgdiv).find('g[' + queryStr + ']')
                    $(group).each(function () {
                        var animation = $(this).find('animate');
                        if (animation.length == 0) {
                            animation = SVG.addChild(this, 'animate', {
                                'attributeName': 'opacity',
                                'attributeType': 'XML',
                                'begin': 'indefinite',
                                'from': '0.0',
                                'to': '1',
                                'begin': '0s',
                                'dur': '0.5s',
                                'repeatCount': '5'
                            });
                        } else {
                            animation = animation[0];
                        }
                        var y = $(group).find('rect').attr("y");
                        $(track.svgdiv).scrollTop(y);
                        animation.beginElement();
                    });
                }
            }
        });

        this.on('track:draw', track.get('track:draw'));
        this.on('trackSpecies:change', track.get('trackSpecies:change'));
        this.on('trackRegion:change', track.get('trackRegion:change'));
        this.on('trackRegion:move', track.get('trackRegion:move'));
        this.on('trackWidth:change', track.get('trackWidth:change'));
        this.on('trackFeature:highlight', track.get('trackFeature:highlight'));

        track.on('track:ready', function () {
            _this.checkTracksReady();
        });
    },

    removeTrack: function (trackId) {
        // first hide the track
        this._hideTrack(trackId);

        var i = this.swapHash[trackId].index;

        // remove track from list and hash data
        var track = this.trackSvgList.splice(i, 1)[0];
        delete this.swapHash[trackId];

        // delete listeners
        this.off('track:draw', track.get('track:draw'));
        this.off('trackSpecies:change', track.get('trackSpecies:change'));
        this.off('trackRegion:change', track.get('trackRegion:change'));
        this.off('trackRegion:move', track.get('trackRegion:move'));
        this.off('trackWidth:change', track.set('trackWidth:change'));
        this.off('trackFeature:highlight', track.get('trackFeature:highlight'));


        //uddate swapHash with correct index after splice
        for (var i = 0; i < this.trackSvgList.length; i++) {
            this.swapHash[this.trackSvgList[i].id].index = i;
        }
        return track;
    },

    restoreTrack: function (track, index) {
        var _this = this;

        this.addTrack(track);

        if (index != null) {
            this.setTrackIndex(track.id, index);
        }
//        this._showTrack(track.id);
    },

    enableAutoHeight: function () {
        for (var i = 0; i < this.trackSvgList.length; i++) {
            var track = this.trackSvgList[i];
            track.enableAutoHeight();
        }
    },
    updateHeight: function () {
        for (var i = 0; i < this.trackSvgList.length; i++) {
            var track = this.trackSvgList[i];
            track.updateHeight(true);
        }
    },

    _redraw: function () {
        $(this.tlTracksDiv)
        for (var i = 0; i < this.trackSvgList.length; i++) {
            var track = this.trackSvgList[i];
            $(track.div).detach();
            if (this.swapHash[track.id].visible) {
                $(this.tlTracksDiv).append(track.div);
            }
        }
    },

    //This routine is called when track order is modified
    _reallocateAbove: function (trackId) {
        var i = this.swapHash[trackId].index;
        console.log(i + " wants to move up");
        if (i > 0) {
            var aboveTrack = this.trackSvgList[i - 1];
            var underTrack = this.trackSvgList[i];

            var y = parseInt(aboveTrack.main.getAttribute("y"));
            var h = parseInt(underTrack.main.getAttribute("height"));
            aboveTrack.main.setAttribute("y", y + h);
            underTrack.main.setAttribute("y", y);

            this.trackSvgList[i] = aboveTrack;
            this.trackSvgList[i - 1] = underTrack;
            this.swapHash[aboveTrack.id].index = i;
            this.swapHash[underTrack.id].index = i - 1;
        } else {
            console.log("is at top");
        }
    },

    //This routine is called when track order is modified
    _reallocateUnder: function (trackId) {
        var i = this.swapHash[trackId].index;
        console.log(i + " wants to move down");
        if (i + 1 < this.trackSvgList.length) {
            var aboveTrack = this.trackSvgList[i];
            var underTrack = this.trackSvgList[i + 1];

            var y = parseInt(aboveTrack.main.getAttribute("y"));
            var h = parseInt(underTrack.main.getAttribute("height"));
            aboveTrack.main.setAttribute("y", y + h);
            underTrack.main.setAttribute("y", y);

            this.trackSvgList[i] = underTrack;
            this.trackSvgList[i + 1] = aboveTrack;
            this.swapHash[underTrack.id].index = i;
            this.swapHash[aboveTrack.id].index = i + 1;

        } else {
            console.log("is at bottom");
        }
    },

    setTrackIndex: function (trackId, newIndex) {
        var oldIndex = this.swapHash[trackId].index;

        //remove track from old index
        var track = this.trackSvgList.splice(oldIndex, 1)[0]

        //add track at new Index
        this.trackSvgList.splice(newIndex, 0, track);

        //uddate swapHash with correct index after slice
        for (var i = 0; i < this.trackSvgList.length; i++) {
            this.swapHash[this.trackSvgList[i].id].index = i;
        }

        //update track div positions
        this._redraw();
    },

    scrollToTrack: function (trackId) {
        var swapTrack = this.swapHash[trackId];
        if (swapTrack != null) {
            var i = swapTrack.index;
            var track = this.trackSvgList[i];
            var y = $(track.div).position().top;
            $(this.tlTracksDiv).scrollTop(y);

//            $(this.svg).parent().parent().scrollTop(track.main.getAttribute("y"));
        }
    },


    _hideTrack: function (trackId) {
        this.swapHash[trackId].visible = false;
        var i = this.swapHash[trackId].index;
        var track = this.trackSvgList[i];

        track.hide();

//        this.setHeight(this.height - track.getHeight());

        this._redraw();
    },

    _showTrack: function (trackId) {
        this.swapHash[trackId].visible = true;
        var i = this.swapHash[trackId].index;
        var track = this.trackSvgList[i];

        track.show();

//        this.svg.appendChild(track.main);

//        this.setHeight(this.height + track.getHeight());

        this._redraw();
    },
    _setPixelBase: function () {
        this.pixelBase = this.width / this.region.length();
        this.pixelBase = this.pixelBase / this.zoomMultiplier;
        this.halfVirtualBase = (this.width * 3 / 2) / this.pixelBase;
    },

    _setTextPosition: function () {
        var centerPosition = this.region.center();
        var baseLength = parseInt(this.width / this.pixelBase);//for zoom 100
        var aux = Math.ceil((baseLength / 2) - 1);
        this.visualRegion.start = Math.floor(centerPosition - aux);
        this.visualRegion.end = Math.floor(centerPosition + aux);

        this.positionText.textContent = Utils.formatNumber(centerPosition);
        this.firstPositionText.textContent = Utils.formatNumber(this.visualRegion.start);
        this.lastPositionText.textContent = Utils.formatNumber(this.visualRegion.end);


        this.windowSize = "Window size: " + Utils.formatNumber(this.visualRegion.length()) + " nts";
        this.viewNtsText.textContent = this.windowSize;
        $(this.div).find('#windowSizeSpan').html(this.windowSize);

//        this.viewNtsTextBack.setAttribute("width", this.viewNtsText.textContent.length * 7);
//        this.viewNtsTextBack.setAttribute('width', $(this.viewNtsText).width() + 15);
    },

    getTrackSvgById: function (trackId) {
        if (this.swapHash[trackId] != null) {
            var position = this.swapHash[trackId].index;
            return this.trackSvgList[position];
        }
        return null;
    },
    getSequenceTrack: function () {
        //if multiple, returns the first found
        for (var i = 0; i < this.trackSvgList.length; i++) {
            var track = this.trackSvgList[i];
            if (track instanceof SequenceTrack) {
                return track;
            }
        }
        return;
    },

    getMousePosition: function (position) {
        var base = '';
        var colorStyle = '';
        if (position > 0) {
            base = this.getSequenceNucleotid(position);
            colorStyle = 'color:' + SEQUENCE_COLORS[base];
        }
//        this.mouseLine.setAttribute('stroke',SEQUENCE_COLORS[base]);
//        this.mouseLine.setAttribute('fill',SEQUENCE_COLORS[base]);
        return '<span style="' + colorStyle + '">' + base + '</span>';
    },

    getSequenceNucleotid: function (position) {
        var seqTrack = this.getSequenceTrack();
        if (seqTrack != null && this.visualRegion.length() <= seqTrack.visibleRegionSize) {
            var nt = seqTrack.dataAdapter.getNucleotidByPosition({start: position, end: position, chromosome: this.region.chromosome})
            return nt;
        }
        return '';
    },

    setNucleotidPosition: function (position) {
        var base = this.getSequenceNucleotid(position);
        this.nucleotidText.setAttribute("fill", SEQUENCE_COLORS[base]);
        this.nucleotidText.textContent = base;
    }
};
function Track(args) {
    this.width = 200;
    this.height = 200;


    this.dataAdapter;
    this.renderer;
    this.resizable = true;
    this.autoHeight = false;
    this.targetId;
    this.id;
    this.title;
    this.minHistogramRegionSize = 300000000;
    this.maxLabelRegionSize = 300000000;
    this.height = 100;
    this.visibleRegionSize;
        this.fontClass = 'ocb-font-sourcesanspro ocb-font-size-14';

    _.extend(this, args);

    this.pixelBase;
    this.svgCanvasWidth = 500000;//mesa
    this.pixelPosition = this.svgCanvasWidth / 2;
    this.svgCanvasOffset;
    this.svgCanvasFeatures;
    this.status;
    this.histogram;
    this.histogramLogarithm;
    this.histogramMax;
    this.interval;

    this.svgCanvasLeftLimit;
    this.svgCanvasRightLimit;


    this.invalidZoomText;

    this.renderedArea = {};//used for renders to store binary trees
    this.chunksDisplayed = {};//used to avoid painting multiple times features contained in more than 1 chunk

    if ('handlers' in this) {
        for (eventName in this.handlers) {
            this.on(eventName, this.handlers[eventName]);
        }
    }

    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
};

Track.prototype = {

    get: function (attr) {
        return this[attr];
    },

    set: function (attr, value) {
        this[attr] = value;
    },
    hide: function () {
        $(this.div).css({display: 'hidden'});
    },
    show: function () {
        $(this.div).css({display: 'auto'});
    },
    hideContent: function () {
        $(this.svgdiv).css({display: 'hidden'});
        $(this.titlediv).css({display: 'hidden'});
    },
    showContent: function () {
        $(this.svgdiv).css({display: 'auto'});
        $(this.titlediv).css({display: 'auto'});
    },
    toggleContent: function () {
        $(this.svgdiv).toggle('hidden');
        $(this.resizeDiv).toggle('hidden');
        $(this.configBtn).toggle('hidden');
    },
    setSpecies: function (species) {
        this.species = species;
        this.dataAdapter.species = this.species
    },

    setWidth: function (width) {
        this.width = width;
        this.main.setAttribute("width", width);
    },
    _updateDIVHeight: function () {
//        $(this.rrr).remove();
//        delete this.rrr;
//        this.rrr = SVG.addChild(this.svgCanvasFeatures, "rect", {
//            'x': 0,
//            'y': 0,
//            'width': 0,
//            'height': 18,
//            'stroke': '#3B0B0B',
//            'stroke-width': 1,
//            'stroke-opacity': 1,
//            'fill': 'black',
//            'cursor': 'pointer'
//        });
        if (this.resizable) {
            if (this.histogram) {
                $(this.svgdiv).css({'height': this.height + 10});
            } else {
                var x = this.pixelPosition;
                var width = this.width;
                var lastContains = 0;
                for (var i in this.renderedArea) {
                    if (this.renderedArea[i].contains({start: x, end: x + width })) {
                        lastContains = i;
                    }
                }
                var divHeight = parseInt(lastContains) + 20;
                $(this.svgdiv).css({'height': divHeight + 25});
//                this.rrr.setAttribute('x', x);
//                this.rrr.setAttribute('y', divHeight);
//                this.rrr.setAttribute('width', width);
            }
        }
    },
    _updateSVGHeight: function () {
        if (this.resizable && !this.histogram) {
            var renderedHeight = Object.keys(this.renderedArea).length * 20;//this must be passed by config, 20 for test
            this.main.setAttribute('height', renderedHeight);
            this.svgCanvasFeatures.setAttribute('height', renderedHeight);
            this.hoverRect.setAttribute('height', renderedHeight);
        }
    },
    updateHeight: function (ignoreAutoHeight) {
        this._updateSVGHeight();
        if (this.autoHeight || ignoreAutoHeight) {
            this._updateDIVHeight();
        }
    },
    enableAutoHeight: function () {
        this.autoHeight = true;
        this.updateHeight();
    },
    setTitle: function (title) {
        $(this.titlediv).html(title);
    },

    setLoading: function (bool) {
        if (bool) {
            this.svgLoading.setAttribute("visibility", "visible");
            this.status = "rendering";
        } else {
            this.svgLoading.setAttribute("visibility", "hidden");
            this.status = "ready";
            this.trigger('track:ready', {sender: this});
        }
    },

    updateHistogramParams: function () {
        if (this.region.length() > this.minHistogramRegionSize) {
            this.histogram = true;
            this.histogramLogarithm = true;
            this.histogramMax = 500;
            this.interval = Math.ceil(10 / this.pixelBase);//server interval limit 512
        } else {
            this.histogram = undefined;
            this.histogramLogarithm = undefined;
            this.histogramMax = undefined;
            this.interval = undefined;
        }

//        if (this.histogramRenderer) {
//            if (this.zoom <= this.histogramZoom) {
//                this.histogramGroup.setAttribute('visibility', 'visible');
//            } else {
//                this.histogramGroup.setAttribute('visibility', 'hidden');
//            }
//        }
    },

    cleanSvg: function (filters) {//clean
//        console.time("-----------------------------------------empty");
        while (this.svgCanvasFeatures.firstChild) {
            this.svgCanvasFeatures.removeChild(this.svgCanvasFeatures.firstChild);
        }
//        console.timeEnd("-----------------------------------------empty");
        this.chunksDisplayed = {};
        this.renderedArea = {};
    },

    initializeDom: function (targetId) {

        var _this = this;
        var div = $('<div id="' + this.id + '-div"></div>')[0];
        var titleBardiv = $('' +
            '<div class="btn-toolbar ocb-compactable">' +
            '   <div class="btn-group btn-group-xs">' +
            '   <button id="configBtn" type="button" class="btn btn-xs btn-primary"><span class="glyphicon glyphicon-cog"></span></button>' +
            '   <button id="titleBtn" type="button" class="btn btn-xs btn-default" data-toggle="button"><span id="titleDiv">' + this.title + '</span></button>' +
            '   </div>' +
            '</div>')[0];

        if (_.isUndefined(this.title)) {
            $(titleBardiv).addClass("hidden");
        }

        var titlediv = $(titleBardiv).find('#titleDiv')[0];
        var titleBtn = $(titleBardiv).find('#titleBtn')[0];
        var configBtn = $(titleBardiv).find('#configBtn')[0];


        var svgdiv = $('<div id="' + this.id + '-svgdiv"></div>')[0];
        var resizediv = $('<div id="' + this.id + '-resizediv" class="ocb-track-resize"></div>')[0];

        $(targetId).addClass("unselectable");
        $(targetId).append(div);
        $(div).append(titleBardiv);
        $(div).append(svgdiv);
        $(div).append(resizediv);


        /** title div **/
        $(titleBardiv).css({'padding': '4px'})
            .on('dblclick', function (e) {
                e.stopPropagation();
            });
        $(titleBtn).click(function (e) {
            _this.toggleContent();
        });

        /** svg div **/
        $(svgdiv).css({
            'z-index': 3,
            'height': this.height,
            'overflow-y': (this.resizable) ? 'auto' : 'hidden',
            'overflow-x': 'hidden'
        });

        var main = SVG.addChild(svgdiv, 'svg', {
            'id': this.id,
            'class': 'trackSvg',
            'x': 0,
            'y': 0,
            'width': this.width,
            'height': this.height
        });


        if (this.resizable) {
            $(resizediv).mousedown(function (event) {
                $('html').addClass('unselectable');
                event.stopPropagation();
                var downY = event.clientY;
                $('html').bind('mousemove.genomeViewer', function (event) {
                    var despY = (event.clientY - downY);
                    var actualHeight = $(svgdiv).outerHeight();
                    $(svgdiv).css({height: actualHeight + despY});
                    downY = event.clientY;
                    _this.autoHeight = false;
                });
            });
            $('html').bind('mouseup.genomeViewer', function (event) {
                $('html').removeClass('unselectable');
                $('html').off('mousemove.genomeViewer');
            });
            $(svgdiv).closest(".trackListPanels").mouseup(function (event) {
                _this.updateHeight();
            });


            $(resizediv).mouseenter(function (event) {
                $(this).css({'cursor': 'ns-resize'});
                $(this).css({'opacity': 1});
            });
            $(resizediv).mouseleave(function (event) {
                $(this).css({'cursor': 'default'});
                $(this).css({'opacity': 0.3});
            });

        }

        this.svgGroup = SVG.addChild(main, "g", {
        });

        var text = this.title;
        var hoverRect = SVG.addChild(this.svgGroup, 'rect', {
            'x': 0,
            'y': 0,
            'width': this.width,
            'height': this.height,
            'opacity': '0.6',
            'fill': 'transparent'
        });

        this.svgCanvasFeatures = SVG.addChild(this.svgGroup, 'svg', {
            'class': 'features',
            'x': -this.pixelPosition,
            'width': this.svgCanvasWidth,
            'height': this.height
        });


        this.fnTitleMouseEnter = function () {
            hoverRect.setAttribute('opacity', '0.1');
            hoverRect.setAttribute('fill', 'lightblue');
        };
        this.fnTitleMouseLeave = function () {
            hoverRect.setAttribute('opacity', '0.6');
            hoverRect.setAttribute('fill', 'transparent');
        };

        $(this.svgGroup).off('mouseenter');
        $(this.svgGroup).off('mouseleave');
        $(this.svgGroup).mouseenter(this.fnTitleMouseEnter);
        $(this.svgGroup).mouseleave(this.fnTitleMouseLeave);


        this.invalidZoomText = SVG.addChild(this.svgGroup, 'text', {
            'x': 154,
            'y': 18,
            'opacity': '0.6',
            'fill': 'black',
            'visibility': 'hidden',
            'class': this.fontClass
        });
        this.invalidZoomText.textContent = "Zoom in to view the sequence";


        var loadingImg = '<?xml version="1.0" encoding="utf-8"?>' +
            '<svg version="1.1" width="22px" height="22px" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">' +
            '<defs>' +
            '<g id="pair">' +
            '<ellipse cx="7" cy="0" rx="4" ry="1.7" style="fill:#ccc; fill-opacity:0.5;"/>' +
            '<ellipse cx="-7" cy="0" rx="4" ry="1.7" style="fill:#aaa; fill-opacity:1.0;"/>' +
            '</g>' +
            '</defs>' +
            '<g transform="translate(11,11)">' +
            '<g>' +
            '<animateTransform attributeName="transform" type="rotate" from="0" to="360" dur="1.5s" repeatDur="indefinite"/>' +
            '<use xlink:href="#pair"/>' +
            '<use xlink:href="#pair" transform="rotate(45)"/>' +
            '<use xlink:href="#pair" transform="rotate(90)"/>' +
            '<use xlink:href="#pair" transform="rotate(135)"/>' +
            '</g>' +
            '</g>' +
            '</svg>';

        this.svgLoading = SVG.addChildImage(main, {
            "xlink:href": "data:image/svg+xml," + encodeURIComponent(loadingImg),
            "x": 10,
            "y": 0,
            "width": 22,
            "height": 22,
            "visibility": "hidden"
        });

        this.div = div;
        this.svgdiv = svgdiv;
        this.titlediv = titlediv;
        this.resizeDiv = resizediv;
        this.configBtn = configBtn;

        this.main = main;
        this.hoverRect = hoverRect;
//        this.titleText = titleText;


//        if (this.histogramRenderer) {
//            this._drawHistogramLegend();
//        }

        this.rendered = true;
        this.status = "ready";

    },
    _drawHistogramLegend: function () {
        var histogramHeight = this.histogramRenderer.histogramHeight;
        var multiplier = this.histogramRenderer.multiplier;

        this.histogramGroup = SVG.addChild(this.svgGroup, 'g', {
            'class': 'histogramGroup',
            'visibility': 'hidden'
        });
        var text = SVG.addChild(this.histogramGroup, "text", {
            "x": 21,
            "y": histogramHeight + 4,
            "font-size": 12,
            "opacity": "0.9",
            "fill": "orangered",
            'class': this.fontClass
        });
        text.textContent = "0-";
        var text = SVG.addChild(this.histogramGroup, "text", {
            "x": 14,
            "y": histogramHeight + 4 - (Math.log(10) * multiplier),
            "font-size": 12,
            "opacity": "0.9",
            "fill": "orangered",
            'class': this.fontClass
        });
        text.textContent = "10-";
        var text = SVG.addChild(this.histogramGroup, "text", {
            "x": 7,
            "y": histogramHeight + 4 - (Math.log(100) * multiplier),
            "font-size": 12,
            "opacity": "0.9",
            "fill": "orangered",
            'class': this.fontClass
        });
        text.textContent = "100-";
        var text = SVG.addChild(this.histogramGroup, "text", {
            "x": 0,
            "y": histogramHeight + 4 - (Math.log(1000) * multiplier),
            "font-size": 12,
            "opacity": "0.9",
            "fill": "orangered",
            'class': this.fontClass
        });
        text.textContent = "1000-";
    },

//    showInfoWidget: function (args) {
//        if (this.dataAdapter.species == "orange") {
//            //data.resource+="orange";
//            if (args.featureType.indexOf("gene") != -1)
//                args.featureType = "geneorange";
//            if (args.featureType.indexOf("transcript") != -1)
//                args.featureType = "transcriptorange";
//        }
//        switch (args.featureType) {
//            case "gene":
//                new GeneInfoWidget(null, this.dataAdapter.species).draw(args);
//                break;
//            case "geneorange":
//                new GeneOrangeInfoWidget(null, this.dataAdapter.species).draw(args);
//                break;
//            case "transcriptorange":
//                new TranscriptOrangeInfoWidget(null, this.dataAdapter.species).draw(args);
//                break;
//            case "transcript":
//                new TranscriptInfoWidget(null, this.dataAdapter.species).draw(args);
//                break;
//            case "snp" :
//                new SnpInfoWidget(null, this.dataAdapter.species).draw(args);
//                break;
//            case "vcf" :
//                new VCFVariantInfoWidget(null, this.dataAdapter.species).draw(args);
//                break;
//            default:
//                break;
//        }
//    },

    draw: function () {

    },

    getFeaturesToRenderByChunk: function (response, filters) {
        //Returns an array avoiding already drawn features in this.chunksDisplayed

        var getChunkId = function (position) {
            return Math.floor(position / response.chunkSize);
        };
        var getChunkKey = function (chromosome, chunkId) {
            return chromosome + ":" + chunkId;
        };

        var chunks = response.items;
        var features = [];


        var feature, displayed, featureFirstChunk, featureLastChunk, features = [];
        for (var i = 0, leni = chunks.length; i < leni; i++) {
            if (this.chunksDisplayed[chunks[i].chunkKey] != true) {//check if any chunk is already displayed and skip it

                for (var j = 0, lenj = chunks[i].value.length; j < lenj; j++) {
                    feature = chunks[i].value[j];

                    //check if any feature has been already displayed by another chunk
                    displayed = false;
                    featureFirstChunk = getChunkId(feature.start);
                    featureLastChunk = getChunkId(feature.end);
                    for (var chunkId = featureFirstChunk; chunkId <= featureLastChunk; chunkId++) {
                        var chunkKey = getChunkKey(feature.chromosome, chunkId);
                        if (this.chunksDisplayed[chunkKey] == true) {
                            displayed = true;
                            break;
                        }
                    }
                    if (!displayed) {
                        //apply filter
                        // if(filters != null) {
                        //		var pass = true;
                        // 		for(filter in filters) {
                        // 			pass = pass && filters[filter](feature);
                        //			if(pass == false) {
                        //				break;
                        //			}
                        // 		}
                        //		if(pass) features.push(feature);
                        // } else {
                        features.push(feature);
                    }
                }
                this.chunksDisplayed[chunks[i].chunkKey] = true;
            }
        }
        return features;
    }
};

BamTrack.prototype = new Track({});

function BamTrack(args) {
    Track.call(this,args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    //set default args

    //save default render reference;
    this.defaultRenderer = this.renderer;
    this.histogramRenderer = new HistogramRenderer();


    this.chunksDisplayed = {};

    //set instantiation args, must be last
    _.extend(this, args);

    this.dataType = 'features';
};

BamTrack.prototype.render = function(targetId){
    var _this = this;
    this.initializeDom(targetId);

    this.svgCanvasOffset = (this.width * 3 / 2) / this.pixelBase;
    this.svgCanvasLeftLimit = this.region.start - this.svgCanvasOffset*2;
    this.svgCanvasRightLimit = this.region.start + this.svgCanvasOffset*2

    this.dataAdapter.on('data:ready',function(event){
        var features;
        if (event.dataType == 'histogram') {
            _this.renderer = _this.histogramRenderer;
            features = event.items;
        } else {
            _this.renderer = _this.defaultRenderer;
            features = _this._removeDisplayedChunks(event);
        }
        _this.renderer.render(features, {
            svgCanvasFeatures : _this.svgCanvasFeatures,
            featureTypes:_this.featureTypes,
            renderedArea:_this.renderedArea,
            pixelBase : _this.pixelBase,
            position : _this.region.center(),
            region : _this.region,
            width : _this.width,
            regionSize: _this.region.length(),
            maxLabelRegionSize: _this.maxLabelRegionSize,
            pixelPosition : _this.pixelPosition
        });

        _this.updateHeight();
        _this.setLoading(false);
    });

};

BamTrack.prototype.draw = function(){
    var _this = this;

    this.svgCanvasOffset = (this.width * 3 / 2) / this.pixelBase;
    this.svgCanvasLeftLimit = this.region.start - this.svgCanvasOffset*2;
    this.svgCanvasRightLimit = this.region.start + this.svgCanvasOffset*2

    this.updateHistogramParams();
    this.cleanSvg();

    this.dataType = 'features';
    if (this.histogram) {
        this.dataType = 'histogram';
    }

    if (typeof this.visibleRegionSize === 'undefined' || this.region.length() < this.visibleRegionSize) {
        this.setLoading(true);
        this.dataAdapter.getData({
            dataType: this.dataType,
            region: new Region({
                chromosome: this.region.chromosome,
                start: this.region.start - this.svgCanvasOffset * 2,
                end: this.region.end + this.svgCanvasOffset * 2
            }),
            params: {
                histogram: this.histogram,
                histogramLogarithm: this.histogramLogarithm,
                histogramMax: this.histogramMax,
                interval: this.interval
            }
        });

        this.invalidZoomText.setAttribute("visibility", "hidden");
    }else{
        this.invalidZoomText.setAttribute("visibility", "visible");
    }
    _this.updateHeight();
};


BamTrack.prototype.move = function(disp){
    var _this = this;

    this.dataType = 'features';
    if (this.histogram) {
        this.dataType = 'histogram';
    }

    _this.region.center();
    var pixelDisplacement = disp*_this.pixelBase;
    this.pixelPosition-=pixelDisplacement;

    //parseFloat important
    var move =  parseFloat(this.svgCanvasFeatures.getAttribute("x")) + pixelDisplacement;
    this.svgCanvasFeatures.setAttribute("x",move);

    var virtualStart = parseInt(this.region.start - this.svgCanvasOffset);
    var virtualEnd = parseInt(this.region.end + this.svgCanvasOffset);

    if (typeof this.visibleRegionSize === 'undefined' || this.region.length() < this.visibleRegionSize) {

        if(disp>0 && virtualStart < this.svgCanvasLeftLimit){
            this.dataAdapter.getData({
                dataType: this.dataType,
                region: new Region({
                    chromosome: _this.region.chromosome,
                    start: parseInt(this.svgCanvasLeftLimit - this.svgCanvasOffset),
                    end: this.svgCanvasLeftLimit
                }),
                params: {
                    histogram: this.histogram,
                    histogramLogarithm: this.histogramLogarithm,
                    histogramMax: this.histogramMax,
                    interval: this.interval
                }
            });
            this.svgCanvasLeftLimit = parseInt(this.svgCanvasLeftLimit - this.svgCanvasOffset);
        }

        if(disp<0 && virtualEnd > this.svgCanvasRightLimit){
            this.dataAdapter.getData({
                dataType: this.dataType,
                region: new Region({
                    chromosome: _this.region.chromosome,
                    start: this.svgCanvasRightLimit,
                    end: parseInt(this.svgCanvasRightLimit + this.svgCanvasOffset)
                }),
                params: {
                    histogram: this.histogram,
                    histogramLogarithm: this.histogramLogarithm,
                    histogramMax: this.histogramMax,
                    interval: this.interval
                }
            });
            this.svgCanvasRightLimit = parseInt(this.svgCanvasRightLimit+this.svgCanvasOffset);
        }

    }

};

BamTrack.prototype._removeDisplayedChunks = function(response){
    //Returns an array avoiding already drawn features in this.chunksDisplayed
    var chunks = response.items;
    var dataType = response.dataType;
    var newChunks = [];
//    var chromosome = response.params.chromosome;

    var feature, displayed, featureFirstChunk, featureLastChunk, features = [];
    for ( var i = 0, leni = chunks.length; i < leni; i++) {//loop over chunks
        if(this.chunksDisplayed[chunks[i].chunkKey] != true){//check if any chunk is already displayed and skip it

            features = []; //initialize array, will contain features not drawn by other drawn chunks
            for ( var j = 0, lenj =  chunks[i].value.reads.length; j < lenj; j++) {
                feature = chunks[i].value.reads[j];
                var chrChunkCache = this.dataAdapter.cache[dataType];

                //check if any feature has been already displayed by another chunk
                displayed = false;
                featureFirstChunk = chrChunkCache.getChunkId(feature.start);
                featureLastChunk = chrChunkCache.getChunkId(feature.end);
                for(var chunkId=featureFirstChunk; chunkId<=featureLastChunk; chunkId++){//loop over chunks touched by this feature
                    var chunkKey = chrChunkCache.getChunkKey(feature.chromosome, chunkId);
                    if(this.chunksDisplayed[chunkKey]==true){
                        displayed = true;
                        break;
                    }
                }
                if(!displayed){
                    features.push(feature);
                }
            }
            this.chunksDisplayed[chunks[i].chunkKey]=true;
            chunks[i].value.reads = features;//update features array
            newChunks.push(chunks[i]);
        }
    }
    response.items = newChunks;
    return response;
};
FeatureTrack.prototype = new Track({});

function FeatureTrack(args) {
    Track.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    //set default args

    //save default render reference;
    this.defaultRenderer = this.renderer;
//    this.histogramRenderer = new FeatureClusterRenderer();
    this.histogramRenderer = new HistogramRenderer(args);

    this.featureType = 'Feature';
    //set instantiation args, must be last
    _.extend(this, args);


    this.resource = this.dataAdapter.resource;
    this.species = this.dataAdapter.species;

    this.dataType = 'features';
};

FeatureTrack.prototype.render = function (targetId) {
    var _this = this;
    this.initializeDom(targetId);

    this.svgCanvasOffset = (this.width * 3 / 2) / this.pixelBase;
    this.svgCanvasLeftLimit = this.region.start - this.svgCanvasOffset * 2;
    this.svgCanvasRightLimit = this.region.start + this.svgCanvasOffset * 2

    this.dataAdapter.on('data:ready', function (event) {
        var features;
        if (event.dataType == 'histogram') {
            _this.renderer = _this.histogramRenderer;
            features = event.items;
        } else {
            _this.renderer = _this.defaultRenderer;
            features = _this.getFeaturesToRenderByChunk(event);
        }
        _this.renderer.render(features, {
            svgCanvasFeatures: _this.svgCanvasFeatures,
            featureTypes: _this.featureTypes,
            renderedArea: _this.renderedArea,
            pixelBase: _this.pixelBase,
            position: _this.region.center(),
            regionSize: _this.region.length(),
            maxLabelRegionSize: _this.maxLabelRegionSize,
            width: _this.width,
            pixelPosition: _this.pixelPosition,
            resource:_this.resource,
            species:_this.species,
            featureType:_this.featureType
        });
        _this.updateHeight();
        _this.setLoading(false);
    });
};

FeatureTrack.prototype.draw = function () {
    var _this = this;

    this.svgCanvasOffset = (this.width * 3 / 2) / this.pixelBase;
    this.svgCanvasLeftLimit = this.region.start - this.svgCanvasOffset * 2;
    this.svgCanvasRightLimit = this.region.start + this.svgCanvasOffset * 2;

    this.updateHistogramParams();
    this.cleanSvg();

    this.dataType = 'features';
    if (this.histogram) {
        this.dataType = 'histogram';
    }

    if (typeof this.visibleRegionSize === 'undefined' || this.region.length() < this.visibleRegionSize) {
        this.setLoading(true);
        this.dataAdapter.getData({
            dataType: this.dataType,
            region: new Region({
                chromosome: this.region.chromosome,
                start: this.region.start - this.svgCanvasOffset * 2,
                end: this.region.end + this.svgCanvasOffset * 2
            }),
            params: {
                histogram: this.histogram,
                histogramLogarithm: this.histogramLogarithm,
                histogramMax: this.histogramMax,
                interval: this.interval
            }
        });

        this.invalidZoomText.setAttribute("visibility", "hidden");
    } else {
        this.invalidZoomText.setAttribute("visibility", "visible");
    }
    _this.updateHeight();
};


FeatureTrack.prototype.move = function (disp) {
    var _this = this;

    this.dataType = 'features';
    if (this.histogram) {
        this.dataType = 'histogram';
    }

    _this.region.center();
    var pixelDisplacement = disp * _this.pixelBase;
    this.pixelPosition -= pixelDisplacement;

    //parseFloat important
    var move = parseFloat(this.svgCanvasFeatures.getAttribute("x")) + pixelDisplacement;
    this.svgCanvasFeatures.setAttribute("x", move);

    var virtualStart = parseInt(this.region.start - this.svgCanvasOffset);
    var virtualEnd = parseInt(this.region.end + this.svgCanvasOffset);

    if (typeof this.visibleRegionSize === 'undefined' || this.region.length() < this.visibleRegionSize) {

        if (disp > 0 && virtualStart < this.svgCanvasLeftLimit) {
            this.dataAdapter.getData({
                dataType: this.dataType,
                region: new Region({
                    chromosome: _this.region.chromosome,
                    start: parseInt(this.svgCanvasLeftLimit - this.svgCanvasOffset),
                    end: this.svgCanvasLeftLimit
                }),
                params: {
                    histogram: this.histogram,
                    histogramLogarithm: this.histogramLogarithm,
                    histogramMax: this.histogramMax,
                    interval: this.interval
                }
            });
            this.svgCanvasLeftLimit = parseInt(this.svgCanvasLeftLimit - this.svgCanvasOffset);
        }

        if (disp < 0 && virtualEnd > this.svgCanvasRightLimit) {
            this.dataAdapter.getData({
                dataType: this.dataType,
                region: new Region({
                    chromosome: _this.region.chromosome,
                    start: this.svgCanvasRightLimit,
                    end: parseInt(this.svgCanvasRightLimit + this.svgCanvasOffset)
                }),
                params: {
                    histogram: this.histogram,
                    histogramLogarithm: this.histogramLogarithm,
                    histogramMax: this.histogramMax,
                    interval: this.interval
                }
            });
            this.svgCanvasRightLimit = parseInt(this.svgCanvasRightLimit + this.svgCanvasOffset);
        }

    }

};

GeneTrack.prototype = new Track({});

function GeneTrack(args) {
    Track.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    //set default args
    this.minTranscriptRegionSize;

    //save default render reference;
    this.defaultRenderer = this.renderer;
//    this.histogramRenderer = new FeatureClusterRenderer();
    this.histogramRenderer = new HistogramRenderer(args);


    //set instantiation args, must be last
    _.extend(this, args);

    this.exclude;

};

GeneTrack.prototype.render = function (targetId) {
    var _this = this;
    this.initializeDom(targetId);

    this.svgCanvasOffset = (this.width * 3 / 2) / this.pixelBase;
    this.svgCanvasLeftLimit = this.region.start - this.svgCanvasOffset * 2;
    this.svgCanvasRightLimit = this.region.start + this.svgCanvasOffset * 2

    this.dataAdapter.on('data:ready', function (event) {
        var features;
        if (event.dataType == 'histogram') {
            _this.renderer = _this.histogramRenderer;
            features = event.items;
        } else {
            _this.renderer = _this.defaultRenderer;
            features = _this.getFeaturesToRenderByChunk(event);
        }
        _this.renderer.render(features, {
            svgCanvasFeatures: _this.svgCanvasFeatures,
            featureTypes: _this.featureTypes,
            renderedArea: _this.renderedArea,
            pixelBase: _this.pixelBase,
            position: _this.region.center(),
            regionSize: _this.region.length(),
            maxLabelRegionSize: _this.maxLabelRegionSize,
            width: _this.width,
            pixelPosition: _this.pixelPosition

        });
        _this.updateHeight();
        _this.setLoading(false);
    });

    this.renderer.on('feature:click', function (event) {
        _this.showInfoWidget(event);
    });
};

GeneTrack.prototype.updateTranscriptParams = function () {
    if (this.region.length() < this.minTranscriptRegionSize) {
        this.exclude = this.dataAdapter.params.exclude;
    } else {
        this.exclude = 'transcripts';
    }
};

GeneTrack.prototype.draw = function () {
    var _this = this;

    this.svgCanvasOffset = (this.width * 3 / 2) / this.pixelBase;
    this.svgCanvasLeftLimit = this.region.start - this.svgCanvasOffset * 2;
    this.svgCanvasRightLimit = this.region.start + this.svgCanvasOffset * 2;

    this.updateTranscriptParams();
    this.updateHistogramParams();
    this.cleanSvg();

    var dataType = 'features';

    if (!_.isUndefined(this.exclude)) {
        dataType = 'features' + this.exclude;
    }

    if (this.histogram) {
        dataType = 'histogram';
    }


    if (typeof this.visibleRegionSize === 'undefined' || this.region.length() < this.visibleRegionSize) {
        this.setLoading(true);
        var data = this.dataAdapter.getData({
            dataType: dataType,
            region: new Region({
                chromosome: this.region.chromosome,
                start: this.region.start - this.svgCanvasOffset * 2,
                end: this.region.end + this.svgCanvasOffset * 2
            }),
            params: {
                histogram: this.histogram,
                histogramLogarithm: this.histogramLogarithm,
                histogramMax: this.histogramMax,
                interval: this.interval,
                exclude: this.exclude
            }
        });

        this.invalidZoomText.setAttribute("visibility", "hidden");
    } else {
        this.invalidZoomText.setAttribute("visibility", "visible");
    }
    _this.updateHeight();
};


GeneTrack.prototype.move = function (disp) {
    var _this = this;

    this.dataType = 'features';

    if (!_.isUndefined(this.exclude)) {
        dataType = 'features' + this.exclude;
    }

    if (this.histogram) {
        this.dataType = 'histogram';
    }

//    trackSvg.position = _this.region.center();
    _this.region.center();
    var pixelDisplacement = disp * _this.pixelBase;
    this.pixelPosition -= pixelDisplacement;

    //parseFloat important
    var move = parseFloat(this.svgCanvasFeatures.getAttribute("x")) + pixelDisplacement;
    this.svgCanvasFeatures.setAttribute("x", move);

    var virtualStart = parseInt(this.region.start - this.svgCanvasOffset);
    var virtualEnd = parseInt(this.region.end + this.svgCanvasOffset);
    // check if track is visible in this zoom

//    console.log(virtualStart+'  ----  '+virtualEnd)
//    console.log(this.svgCanvasLeftLimit+'  ----  '+this.svgCanvasRightLimit)
//    console.log(this.svgCanvasOffset)

    if (typeof this.visibleRegionSize === 'undefined' || this.region.length() < this.visibleRegionSize) {

        if (disp > 0 && virtualStart < this.svgCanvasLeftLimit) {
            console.log('left')
            this.dataAdapter.getData({
                dataType: this.dataType,
                region: new Region({
                    chromosome: _this.region.chromosome,
                    start: parseInt(this.svgCanvasLeftLimit - this.svgCanvasOffset),
                    end: this.svgCanvasLeftLimit
                }),
                params: {
                    histogram: this.histogram,
                    histogramLogarithm: this.histogramLogarithm,
                    histogramMax: this.histogramMax,
                    interval: this.interval,
                    exclude: this.exclude
                }
            });
            this.svgCanvasLeftLimit = parseInt(this.svgCanvasLeftLimit - this.svgCanvasOffset);
        }

        if (disp < 0 && virtualEnd > this.svgCanvasRightLimit) {
            console.log('right')
            this.dataAdapter.getData({
                dataType: this.dataType,
                region: new Region({
                    chromosome: _this.region.chromosome,
                    start: this.svgCanvasRightLimit,
                    end: parseInt(this.svgCanvasRightLimit + this.svgCanvasOffset)
                }),
                params: {
                    histogram: this.histogram,
                    histogramLogarithm: this.histogramLogarithm,
                    histogramMax: this.histogramMax,
                    interval: this.interval,
                    exclude: this.exclude
                }
            });
            this.svgCanvasRightLimit = parseInt(this.svgCanvasRightLimit + this.svgCanvasOffset);
        }
    }
};

GeneTrack.prototype.showInfoWidget = function (args) {
    switch (args.featureType) {
        case "gene":
            new GeneInfoWidget(null, this.dataAdapter.species).draw(args);
            break;
        case "transcript":
            new TranscriptInfoWidget(null, this.dataAdapter.species).draw(args);
            break;
        default:
            break;
    }
};

SequenceTrack.prototype = new Track({});

function SequenceTrack(args) {
    args.resizable = false;
    Track.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    //set default args

    _.extend(this, args);
};

SequenceTrack.prototype.render = function (targetId) {
    var _this = this;
    this.initializeDom(targetId);

    this.svgCanvasOffset = (this.width * 3 / 2) / this.pixelBase;
    this.svgCanvasLeftLimit = this.region.start - this.svgCanvasOffset * 2;
    this.svgCanvasRightLimit = this.region.start + this.svgCanvasOffset * 2

    this.dataAdapter.on('data:ready', function (event) {
        _this.renderer.render(event, {
            svgCanvasFeatures: _this.svgCanvasFeatures,
            pixelBase: _this.pixelBase,
            position: _this.region.center(),
            width: _this.width,
            pixelPosition: _this.pixelPosition
        });
        _this.setLoading(false);
    });
};

SequenceTrack.prototype.draw = function () {
    var _this = this;
    this.svgCanvasOffset = (this.width * 3 / 2) / this.pixelBase;
    this.svgCanvasLeftLimit = this.region.start - this.svgCanvasOffset * 2;
    this.svgCanvasRightLimit = this.region.start + this.svgCanvasOffset * 2

    this.cleanSvg();

    if (typeof this.visibleRegionSize === 'undefined' || this.region.length() < this.visibleRegionSize) {
        this.setLoading(true);
        var data = this.dataAdapter.getData({
            region: new Region({
                chromosome: this.region.chromosome,
                start: this.region.start - this.svgCanvasOffset * 2,
                end: this.region.end + this.svgCanvasOffset * 2
            })
        });
        this.invalidZoomText.setAttribute("visibility", "hidden");
    } else {
        this.invalidZoomText.setAttribute("visibility", "visible");
    }


};


SequenceTrack.prototype.move = function (disp) {
    var _this = this;
    var pixelDisplacement = disp * _this.pixelBase;
    this.pixelPosition -= pixelDisplacement;

    //parseFloat important
    var move = parseFloat(this.svgCanvasFeatures.getAttribute("x")) + pixelDisplacement;
    this.svgCanvasFeatures.setAttribute("x", move);

    var virtualStart = parseInt(this.region.start - this.svgCanvasOffset);
    var virtualEnd = parseInt(this.region.end + this.svgCanvasOffset);

    // check if track is visible in this region size
    if (typeof this.visibleRegionSize === 'undefined' || this.region.length() < this.visibleRegionSize) {
        if (disp > 0 && virtualStart < this.svgCanvasLeftLimit) {
            this.dataAdapter.getData({
                region: new Region({
                    chromosome: _this.region.chromosome,
                    start: parseInt(this.svgCanvasLeftLimit - this.svgCanvasOffset),
                    end: this.svgCanvasLeftLimit
                }),
                sender: 'move'
            });
            this.svgCanvasLeftLimit = parseInt(this.svgCanvasLeftLimit - this.svgCanvasOffset);
        }

        if (disp < 0 && virtualEnd > this.svgCanvasRightLimit) {
            this.dataAdapter.getData({
                region: new Region({
                    chromosome: _this.region.chromosome,
                    start: this.svgCanvasRightLimit,
                    end: parseInt(this.svgCanvasRightLimit + this.svgCanvasOffset),
                }),
                sender: 'move'
            });
            this.svgCanvasRightLimit = parseInt(this.svgCanvasRightLimit + this.svgCanvasOffset);
        }

    }

};
//Parent class for all renderers
function Renderer(args) {


};

Renderer.prototype = {

    render: function (items) {

    },

    getFeatureX: function (feature, args) {//returns svg feature x value from feature genomic position
        var middle = args.width / 2;
        var x = args.pixelPosition + middle - ((args.position - feature.start) * args.pixelBase);
        return x;
    },

    getDefaultConfig: function (type) {
        return FEATURE_TYPES[type];
    },
    getLabelWidth: function (label, args) {
        /* insert in dom to get the label width and then remove it*/
        var svgLabel = SVG.create("text", {
            'font-weight': 400,
            'class':this.fontClass
        });
        svgLabel.textContent = label;
        $(args.svgCanvasFeatures).append(svgLabel);
        var svgLabelWidth = $(svgLabel).width();
        $(svgLabel).remove();
        return svgLabelWidth;
    }
}
;
//any item with chromosome start end
BamRenderer.prototype = new Renderer({});

function BamRenderer(args) {
    Renderer.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    this.fontClass = 'ocb-font-sourcesanspro ocb-font-size-12';
    this.toolTipfontClass = 'ocb-font-default';

    if (_.isObject(args)) {
        _.extend(this, args);
    }

    this.on(this.handlers);
};


BamRenderer.prototype.render = function (response, args) {
    var _this = this;


    //CHECK VISUALIZATON MODE
    if (_.isUndefined(response.params)) {
        response.params = {};
    }

    var viewAsPairs = false;
    if (response.params["view_as_pairs"] != null) {
        viewAsPairs = true;
    }
    console.log("viewAsPairs " + viewAsPairs);
    var insertSizeMin = 0;
    var insertSizeMax = 0;
    var variantColor = "orangered";
    if (response.params["insert_size_interval"] != null) {
        insertSizeMin = response.params["insert_size_interval"].split(",")[0];
        insertSizeMax = response.params["insert_size_interval"].split(",")[1];
    }
    console.log("insertSizeMin " + insertSizeMin);
    console.log("insertSizeMin " + insertSizeMax);

    //Prevent browser context menu
    $(args.svgCanvasFeatures).contextmenu(function (e) {
        console.log("click derecho")
        e.preventDefault();
    });

    console.time("BamRender " + response.params.resource);

    var chunkList = response.items;

//    var middle = this.width / 2;

    var bamCoverGroup = SVG.addChild(args.svgCanvasFeatures, "g", {
        "class": "bamCoverage",
        "cursor": "pointer"
    });
    var bamReadGroup = SVG.addChild(args.svgCanvasFeatures, "g", {
        "class": "bamReads",
        "cursor": "pointer"
    });

    var drawCoverage = function (chunk) {
        //var coverageList = chunk.coverage.all;
        var coverageList = chunk.coverage.all;
        var coverageListA = chunk.coverage.a;
        var coverageListC = chunk.coverage.c;
        var coverageListG = chunk.coverage.g;
        var coverageListT = chunk.coverage.t;
        var start = parseInt(chunk.start);
        var end = parseInt(chunk.end);
        var pixelWidth = (end - start + 1) * args.pixelBase;

        var middle = args.width / 2;
        var points = "", pointsA = "", pointsC = "", pointsG = "", pointsT = "";
        var baseMid = (args.pixelBase / 2) - 0.5;//4.5 cuando pixelBase = 10

        var x, y, p = parseInt(chunk.start);
        var lineA = "", lineC = "", lineG = "", lineT = "";
        var coverageNorm = 200, covHeight = 50;
        for (var i = 0; i < coverageList.length; i++) {
            //x = _this.pixelPosition+middle-((_this.position-p)*_this.pixelBase)+baseMid;
            x = args.pixelPosition + middle - ((args.position - p) * args.pixelBase);
            xx = args.pixelPosition + middle - ((args.position - p) * args.pixelBase) + args.pixelBase;

            lineA += x + "," + coverageListA[i] / coverageNorm * covHeight + " ";
            lineA += xx + "," + coverageListA[i] / coverageNorm * covHeight + " ";
            lineC += x + "," + (coverageListC[i] + coverageListA[i]) / coverageNorm * covHeight + " ";
            lineC += xx + "," + (coverageListC[i] + coverageListA[i]) / coverageNorm * covHeight + " ";
            lineG += x + "," + (coverageListG[i] + coverageListC[i] + coverageListA[i]) / coverageNorm * covHeight + " ";
            lineG += xx + "," + (coverageListG[i] + coverageListC[i] + coverageListA[i]) / coverageNorm * covHeight + " ";
            lineT += x + "," + (coverageListT[i] + coverageListG[i] + coverageListC[i] + coverageListA[i]) / coverageNorm * covHeight + " ";
            lineT += xx + "," + (coverageListT[i] + coverageListG[i] + coverageListC[i] + coverageListA[i]) / coverageNorm * covHeight + " ";

            p++;
        }

        //reverse to draw the polylines(polygons) for each nucleotid
        var rlineC = lineC.split(" ").reverse().join(" ").trim();
        var rlineG = lineG.split(" ").reverse().join(" ").trim();
        var rlineT = lineT.split(" ").reverse().join(" ").trim();

        var firstPoint = args.pixelPosition + middle - ((args.position - parseInt(chunk.start)) * args.pixelBase) + baseMid;
        var lastPoint = args.pixelPosition + middle - ((args.position - parseInt(chunk.end)) * args.pixelBase) + baseMid;

        var polA = SVG.addChild(bamCoverGroup, "polyline", {
            "points": firstPoint + ",0 " + lineA + lastPoint + ",0",
            //"opacity":"1",
            //"stroke-width":"1",
            //"stroke":"gray",
            "fill": "green"
        });
        var polC = SVG.addChild(bamCoverGroup, "polyline", {
            "points": lineA + " " + rlineC,
            //"opacity":"1",
            //"stroke-width":"1",
            //"stroke":"black",
            "fill": "blue"
        });
        var polG = SVG.addChild(bamCoverGroup, "polyline", {
            "points": lineC + " " + rlineG,
            //"opacity":"1",
            //"stroke-width":"1",
            //"stroke":"black",
            "fill": "gold"
        });
        var polT = SVG.addChild(bamCoverGroup, "polyline", {
            "points": lineG + " " + rlineT,
            //"opacity":"1",
            //"stroke-width":"1",
            //"stroke":"black",
            "fill": "red"
        });

        var dummyRect = SVG.addChild(bamCoverGroup, "rect", {
            "x": args.pixelPosition + middle - ((args.position - start) * args.pixelBase),
            "y": 0,
            "width": pixelWidth,
            "height": covHeight,
            "opacity": "0.5",
            "fill": "lightgray",
            "cursor": "pointer"
        });


        $(dummyRect).qtip({
            content: " ",
            position: {target: 'mouse', adjust: {x: 15, y: 0}, viewport: $(window), effect: false},
            style: { width: true, classes: _this.toolTipfontClass + ' ui-tooltip-shadow'}
        });


//        args.trackSvgLayout.onMousePosition.addEventListener(function (sender, obj) {
//            var pos = obj.mousePos - parseInt(chunk.start);
//            //if(coverageList[pos]!=null){
//            var str = 'depth: <span class="ssel">' + coverageList[pos] + '</span><br>' +
//                '<span style="color:green">A</span>: <span class="ssel">' + chunk.coverage.a[pos] + '</span><br>' +
//                '<span style="color:blue">C</span>: <span class="ssel">' + chunk.coverage.c[pos] + '</span><br>' +
//                '<span style="color:darkgoldenrod">G</span>: <span class="ssel">' + chunk.coverage.g[pos] + '</span><br>' +
//                '<span style="color:red">T</span>: <span class="ssel">' + chunk.coverage.t[pos] + '</span><br>';
//            $(dummyRect).qtip('option', 'content.text', str);
//            //}
//        });
    };

    var drawSingleRead = function (feature) {
        //var start = feature.start;
        //var end = feature.end;
        var start = feature.unclippedStart;
        var end = feature.unclippedEnd;
        var length = (end - start) + 1;
        var diff = feature.diff;

        //get feature render configuration
        var color = _.isFunction(_this.color) ? _this.color(feature, args.region.chromosome) : _this.color;
        var strokeColor = _.isFunction(_this.strokeColor) ? _this.strokeColor(feature, args.region.chromosome) : _this.strokeColor;
        var label = _.isFunction(_this.label) ? _this.label(feature) : _this.label;
        var height = _.isFunction(_this.height) ? _this.height(feature) : _this.height;
        var tooltipTitle = _.isFunction(_this.tooltipTitle) ? _this.tooltipTitle(feature) : _this.tooltipTitle;
        var tooltipText = _.isFunction(_this.tooltipText) ? _this.tooltipText(feature) : _this.tooltipText;
        var strand = _.isFunction(_this.strand) ? _this.strand(feature) : _this.strand;
        var mateUnmappedFlag = _.isFunction(_this.mateUnmappedFlag) ? _this.mateUnmappedFlag(feature) : _this.mateUnmappedFlag;
        var infoWidgetId = _.isFunction(_this.infoWidgetId) ? _this.infoWidgetId(feature) : _this.infoWidgetId;

        if (insertSizeMin != 0 && insertSizeMax != 0 && !mateUnmappedFlag) {
            if (Math.abs(feature.inferredInsertSize) > insertSizeMax) {
                color = 'maroon';
            }
            if (Math.abs(feature.inferredInsertSize) < insertSizeMin) {
                color = 'navy';
            }
        }

        //transform to pixel position
        var width = length * args.pixelBase;
        //calculate x to draw svg rect
        var x = _this.getFeatureX(feature, args);
//		try{
//			var maxWidth = Math.max(width, /*settings.getLabel(feature).length*8*/0); //XXX cuidado : text.getComputedTextLength()
//		}catch(e){
//			var maxWidth = 72;
//		}
        maxWidth = width;

        var rowHeight = 12;
        var rowY = 70;
//		var textY = 12+settings.height;
        while (true) {
            if (args.renderedArea[rowY] == null) {
                args.renderedArea[rowY] = new FeatureBinarySearchTree();
            }
            var enc = args.renderedArea[rowY].add({start: x, end: x + maxWidth - 1});
            if (enc) {
                var featureGroup = SVG.addChild(bamReadGroup, "g", {'feature_id': feature.name});
                var points = {
                    "Reverse": x + "," + (rowY + (height / 2)) + " " + (x + 5) + "," + rowY + " " + (x + width - 5) + "," + rowY + " " + (x + width - 5) + "," + (rowY + height) + " " + (x + 5) + "," + (rowY + height),
                    "Forward": x + "," + rowY + " " + (x + width - 5) + "," + rowY + " " + (x + width) + "," + (rowY + (height / 2)) + " " + (x + width - 5) + "," + (rowY + height) + " " + x + "," + (rowY + height)
                }
                var poly = SVG.addChild(featureGroup, "polygon", {
                    "points": points[strand],
                    "stroke": strokeColor,
                    "stroke-width": 1,
                    "fill": color,
                    "cursor": "pointer"
                });

                //var rect = SVG.addChild(featureGroup,"rect",{
                //"x":x+offset[strand],
                //"y":rowY,
                //"width":width-4,
                //"height":settings.height,
                //"stroke": "white",
                //"stroke-width":1,
                //"fill": color,
                //"clip-path":"url(#"+_this.id+"cp)",
                //"fill": 'url(#'+_this.id+'bamStrand'+strand+')',
                //});
                //readEls.push(rect);

                if (diff != null && args.regionSize < 400) {
                    //var	t = SVG.addChild(featureGroup,"text",{
                    //"x":x+1,
                    //"y":rowY+settings.height-1,
                    //"fill":"darkred",
                    //"textLength":width,
                    //"cursor": "pointer"
                    //});
                    //t.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:space","preserve");
                    //t.textContent = diff;
                    //readEls.push(t);
                    var path = SVG.addChild(featureGroup, "path", {
                        "d": Utils.genBamVariants(diff, args.pixelBase, x, rowY),
                        "fill": variantColor
                    });
                }
                $(featureGroup).qtip({
                    content: {text: tooltipText, title: tooltipTitle},
                    position: {target: "mouse", adjust: {x: 25, y: 15}},
                    style: { width: 300, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'},
                    show: 'click',
                    hide: 'click mouseleave'
                });


//                $(featureGroup).click(function (event) {
//                    console.log(feature);
//                    _this.trigger('feature:click', {query: feature[infoWidgetId], feature: feature, featureType: feature.featureType, clickEvent: event})
////                    _this.showInfoWidget({query: feature[settings.infoWidgetId], feature: feature, featureType: feature.featureType, adapter: _this.trackData.adapter});
//                });
                break;
            }
            rowY += rowHeight;
//			textY += rowHeight;
        }
    };

    var drawPairedReads = function (read, mate) {
        var readStart = read.unclippedStart;
        var readEnd = read.unclippedEnd;
        var mateStart = mate.unclippedStart;
        var mateEnd = mate.unclippedEnd;
        var readDiff = read.diff;
        var mateDiff = mate.diff;
        /*get type settings object*/
        var readSettings = _this.types[read.featureType];
        var mateSettings = _this.types[mate.featureType];
        var readColor = readSettings.getColor(read, _this.region.chromosome);
        var mateColor = mateSettings.getColor(mate, _this.region.chromosome);
        var readStrand = readSettings.getStrand(read);
        var matestrand = mateSettings.getStrand(mate);

        if (insertSizeMin != 0 && insertSizeMax != 0) {
            if (Math.abs(read.inferredInsertSize) > insertSizeMax) {
                readColor = 'maroon';
                mateColor = 'maroon';
            }
            if (Math.abs(read.inferredInsertSize) < insertSizeMin) {
                readColor = 'navy';
                mateColor = 'navy';
            }
        }

        var pairStart = readStart;
        var pairEnd = mateEnd;
        if (mateStart <= readStart) {
            pairStart = mateStart;
        }
        if (readEnd >= mateEnd) {
            pairEnd = readEnd;
        }

        /*transform to pixel position*/
        var pairWidth = ((pairEnd - pairStart) + 1) * _this.pixelBase;
        var pairX = _this.pixelPosition + middle - ((_this.position - pairStart) * _this.pixelBase);

        var readWidth = ((readEnd - readStart) + 1) * _this.pixelBase;
        var readX = _this.pixelPosition + middle - ((_this.position - readStart) * _this.pixelBase);

        var mateWidth = ((mateEnd - mateStart) + 1) * _this.pixelBase;
        var mateX = _this.pixelPosition + middle - ((_this.position - mateStart) * _this.pixelBase);

        var rowHeight = 12;
        var rowY = 70;
//		var textY = 12+settings.height;

        while (true) {
            if (args.renderedArea[rowY] == null) {
                args.renderedArea[rowY] = new FeatureBinarySearchTree();
            }
            var enc = args.renderedArea[rowY].add({start: pairX, end: pairX + pairWidth - 1});
            if (enc) {
                var readEls = [];
                var mateEls = [];
                var readPoints = {
                    "Reverse": readX + "," + (rowY + (readSettings.height / 2)) + " " + (readX + 5) + "," + rowY + " " + (readX + readWidth - 5) + "," + rowY + " " + (readX + readWidth - 5) + "," + (rowY + readSettings.height) + " " + (readX + 5) + "," + (rowY + readSettings.height),
                    "Forward": readX + "," + rowY + " " + (readX + readWidth - 5) + "," + rowY + " " + (readX + readWidth) + "," + (rowY + (readSettings.height / 2)) + " " + (readX + readWidth - 5) + "," + (rowY + readSettings.height) + " " + readX + "," + (rowY + readSettings.height)
                }
                var readPoly = SVG.addChild(bamReadGroup, "polygon", {
                    "points": readPoints[readStrand],
                    "stroke": readSettings.getStrokeColor(read),
                    "stroke-width": 1,
                    "fill": readColor,
                    "cursor": "pointer"
                });
                readEls.push(readPoly);
                var matePoints = {
                    "Reverse": mateX + "," + (rowY + (mateSettings.height / 2)) + " " + (mateX + 5) + "," + rowY + " " + (mateX + mateWidth - 5) + "," + rowY + " " + (mateX + mateWidth - 5) + "," + (rowY + mateSettings.height) + " " + (mateX + 5) + "," + (rowY + mateSettings.height),
                    "Forward": mateX + "," + rowY + " " + (mateX + mateWidth - 5) + "," + rowY + " " + (mateX + mateWidth) + "," + (rowY + (mateSettings.height / 2)) + " " + (mateX + mateWidth - 5) + "," + (rowY + mateSettings.height) + " " + mateX + "," + (rowY + mateSettings.height)
                }
                var matePoly = SVG.addChild(bamReadGroup, "polygon", {
                    "points": matePoints[matestrand],
                    "stroke": mateSettings.getStrokeColor(mate),
                    "stroke-width": 1,
                    "fill": mateColor,
                    "cursor": "pointer"
                });
                mateEls.push(matePoly);

                var line = SVG.addChild(bamReadGroup, "line", {
                    "x1": (readX + readWidth),
                    "y1": (rowY + (readSettings.height / 2)),
                    "x2": mateX,
                    "y2": (rowY + (readSettings.height / 2)),
                    "stroke-width": "1",
                    "stroke": "gray",
                    //"stroke-color": "black",
                    "cursor": "pointer"
                });

                if (args.regionSize < 400) {
                    if (readDiff != null) {
                        var readPath = SVG.addChild(bamReadGroup, "path", {
                            "d": Utils.genBamVariants(readDiff, _this.pixelBase, readX, rowY),
                            "fill": variantColor
                        });
                        readEls.push(readPath);
                    }
                    if (mateDiff != null) {
                        var matePath = SVG.addChild(bamReadGroup, "path", {
                            "d": Utils.genBamVariants(mateDiff, _this.pixelBase, mateX, rowY),
                            "fill": variantColor
                        });
                        mateEls.push(matePath);
                    }
                }

                $(readEls).qtip({
                    content: {text: readSettings.getTipText(read), title: readSettings.getTipTitle(read)},
                    position: {target: "mouse", adjust: {x: 15, y: 0}, viewport: $(window), effect: false},
                    style: { width: 280, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'},
                    show: 'click',
                    hide: 'click mouseleave'
                });
                $(readEls).click(function (event) {
                    console.log(read);
                    _this.showInfoWidget({query: read[readSettings.infoWidgetId], feature: read, featureType: read.featureType, adapter: _this.trackData.adapter});
                });
                $(mateEls).qtip({
                    content: {text: mateSettings.getTipText(mate), title: mateSettings.getTipTitle(mate)},
                    position: {target: "mouse", adjust: {x: 15, y: 0}, viewport: $(window), effect: false},
                    style: { width: 280, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'},
                    show: 'click',
                    hide: 'click mouseleave'
                });
                $(mateEls).click(function (event) {
                    console.log(mate);
                    _this.showInfoWidget({query: mate[mateSettings.infoWidgetId], feature: mate, featureType: mate.featureType, adapter: _this.trackData.adapter});
                });
                break;
            }
            rowY += rowHeight;
//			textY += rowHeight;
        }
    };

    var drawChunk = function (chunk) {
        drawCoverage(chunk.value);
        var readList = chunk.value.reads;
        for (var i = 0, li = readList.length; i < li; i++) {
            var read = readList[i];
            if (viewAsPairs) {
                var nextRead = readList[i + 1];
                if (nextRead != null) {
                    if (read.name == nextRead.name) {
                        drawPairedReads(read, nextRead);
                        i++;
                    } else {
                        drawSingleRead(read);
                    }
                }
            } else {
                drawSingleRead(read);
            }
        }
    };

    //process features
    if (chunkList.length > 0) {
        for (var i = 0, li = chunkList.length; i < li; i++) {
            drawChunk(chunkList[i]);
        }
//        var newHeight = Object.keys(this.renderedArea).length * 24;
//        if (newHeight > 0) {
//            this.setHeight(newHeight + /*margen entre tracks*/10 + 70);
//        }
        //TEST
//        this.setHeight(200);
    }
    console.timeEnd("BamRender " + response.params.resource);
};

ConservedRenderer.prototype = new Renderer({});

function ConservedRenderer(args){
    Renderer.call(this,args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    //set default args
    //set instantiation args
    _.extend(this, args);

};


ConservedRenderer.prototype.render = function(features, args) {
    var middle = args.width/2;
    var multiplier = 20;
    var histogramHeight = 75;
    var points = '';
    var width = args.pixelBase;

    var firstFeature = features[0];
    var x = args.pixelPosition+middle-((args.position-parseInt(firstFeature.start))*args.pixelBase);
    points = (x+(width/2))+','+histogramHeight+' ';

    for ( var i = 0, len = features.length; i < len; i++) {
        var feature = features[i];
        feature.start = parseInt(feature.start);
        feature.end = parseInt(feature.end);

        for ( var j = 0, len = feature.values; j < len; j++) {
            var value = feature.values[j];
            var height = value*multiplier;
            var s = start+j;
            var x = args.pixelPosition+middle-((args.position-s)*args.pixelBase);
            points += (x+(width/2))+","+(histogramHeight - height)+" ";
        }
    }
    points += (x+(width/2))+","+(histogramHeight)+" ";

    var pol = SVG.addChild(args.svgCanvasFeatures,"polyline",{
        "points":points,
        "stroke": "#000000",
        "stroke-width": 0.2,
        "fill": 'salmon',
        "cursor": "pointer"
    });


};

FeatureClusterRenderer.prototype = new Renderer({});

function FeatureClusterRenderer(args) {
    Renderer.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    //set default args
    this.histogramHeight = 75;
    this.multiplier = 7;



//    this.maxValue = 100;
//    if (args != null) {
//        if (args.height != null) {
//            this.histogramHeight = args.height * 0.95;
//        }
//        if (args.histogramMaxFreqValue != null) {
//            this.maxValue = args.histogramMaxFreqValue;
//        }
//    }
//    this.multiplier = this.histogramHeight / this.maxValue;

    this.fontClass = 'ocb-font-sourcesanspro ocb-font-size-12';
    this.toolTipfontClass = 'ocb-font-default';

    //set instantiation args
    _.extend(this, args);

};


FeatureClusterRenderer.prototype.render = function (features, args) {
    var _this = this;
    var middle = args.width / 2;
    var maxValue = 0;

    var drawFeature = function (feature) {
        var d = '';

        feature.start = parseInt(feature.start);
        feature.end = parseInt(feature.end);
        var width = (feature.end - feature.start);

        width = width * args.pixelBase;
        var x = _this.getFeatureX(feature, args);

        if (feature.features_count == null) {
//            var height = Math.log(features[i].absolute);
            if (feature.absolute != 0) {
                feature.features_count = Math.log(features[i].absolute);
            } else {
                feature.features_count = 0;
            }
        }

        var height = feature.features_count * _this.multiplier;

        var rect = SVG.addChild(args.svgCanvasFeatures, "rect", {
            'x': x + 1,
            'y': 0,
            'width': width - 1,
            'height': height,
            'stroke': 'smokewhite',
            'stroke-width': 1,
            'fill': '#9493b1',
            'cursor': 'pointer'
        });

        var getInfo = function (feature) {
            var resp = '';
            return resp += Math.round(Math.exp(feature.features_count));
        };


        var url = CellBaseManager.url({
            species: args.species,
            category: 'genomic',
            subCategory: 'region',
            query: new Region(feature).toString(),
            resource: args.resource,
            params: {
                include: 'chromosome,start,end,id',
                limit: 20
            },
            async: false
//            success:function(data){
//                str+=data.response[0].result.length+' cb';
//            }
        });

        $(rect).qtip({
            content: {
                text: 'Loading...', // The text to use whilst the AJAX request is loading
                ajax: {
                    url: url, // URL to the local file
                    type: 'GET', // POST or GET
                    success: function (data, status) {
                        var items = data.response[0].result;
                        var ids = '';
                        for (var i = 0; i < items.length; i++) {
                            var f = items[i];
                            var r = new Region(f);
                            ids += '<span class="emph">' + f.id + '</span> <span class="info">' + r.toString() + '</span><br>';
                        }
                        var fc = Math.round(Math.exp(feature.features_count));
                        if (fc <= 20) {
                            this.set('content.title', 'Count: ' + items.length);
                            this.set('content.text', ids);
                        } else {
                            this.set('content.title', 'Count: ' + fc);
                            this.set('content.text', ids + '...');
                        }
                    }
                }
            },
            position: {target: 'mouse', adjust: {x: 25, y: 15}},
            style: { width: true, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'}
        });

//        $(rect).qtip({
//            content: {text: getInfo(feature), title: 'Count'},
//
//        });

//        $(rect).mouseenter(function(){
//            var str = '';
////            $(rect).qtip({
////                content: {text: str, title: 'Info'},
//////                position: {target: "mouse", adjust: {x: 25, y: 15}},
////                style: { width: true, classes: 'ui-tooltip ui-tooltip-shadow'}
////            });
//        });

    };

    for (var i = 0, len = features.length; i < len; i++) {
        drawFeature(features[i].value);
    }
};

//any item with chromosome start end
FeatureRenderer.prototype = new Renderer({});

function FeatureRenderer(args) {
    Renderer.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    this.fontClass = 'ocb-font-sourcesanspro ocb-font-size-12';
    this.toolTipfontClass = 'ocb-font-default';

     if (_.isObject(args)) {
        _.extend(this, args);
    }

    this.on(this.handlers);
};


FeatureRenderer.prototype.render = function (features, args) {
    var _this = this;
    var draw = function (feature, svgGroup) {

        if (typeof feature.featureType === 'undefined') {
            feature.featureType = args.featureType;
        }
        //get feature render configuration
        var color = _.isFunction(_this.color) ? _this.color(feature) : _this.color;
        var label = _.isFunction(_this.label) ? _this.label(feature) : _this.label;
        var height = _.isFunction(_this.height) ? _this.height(feature) : _this.height;
        var tooltipTitle = _.isFunction(_this.tooltipTitle) ? _this.tooltipTitle(feature) : _this.tooltipTitle;
        var tooltipText = _.isFunction(_this.tooltipText) ? _this.tooltipText(feature) : _this.tooltipText;
        var infoWidgetId = _.isFunction(_this.infoWidgetId) ? _this.infoWidgetId(feature) : _this.infoWidgetId;

        //get feature genomic information
        var start = feature.start;
        var end = feature.end;
        var length = (end - start) + 1;

        //check genomic length
        length = (length < 0) ? Math.abs(length) : length;
        length = (length == 0) ? 1 : length;

        //transform to pixel position
        var width = length * args.pixelBase;

//        var svgLabelWidth = _this.getLabelWidth(label, args);
        var svgLabelWidth = label.length * 6.4;

        //calculate x to draw svg rect
        var x = _this.getFeatureX(feature, args);

        var maxWidth = Math.max(width, 2);
        var textHeight = 0;
        if (args.maxLabelRegionSize > args.regionSize) {
            textHeight = 9;
            maxWidth = Math.max(width, svgLabelWidth);
        }


        var rowY = 0;
        var textY = textHeight + height;
        var rowHeight = textHeight + height + 2;

        while (true) {
            if (!(rowY in args.renderedArea)) {
                args.renderedArea[rowY] = new FeatureBinarySearchTree();
            }
            var foundArea = args.renderedArea[rowY].add({start: x, end: x + maxWidth - 1});

            if (foundArea) {
                var featureGroup = SVG.addChild(svgGroup, "g", {'feature_id': feature.id});
                var rect = SVG.addChild(featureGroup, "rect", {
                    'x': x,
                    'y': rowY,
                    'width': width,
                    'height': height,
                    'stroke': '#3B0B0B',
                    'stroke-width': 1,
                    'stroke-opacity': 0.7,
                    'fill': color,
                    'cursor': 'pointer'
                });
                if (args.maxLabelRegionSize > args.regionSize) {
                    var text = SVG.addChild(featureGroup, "text", {
                        'i': i,
                        'x': x,
                        'y': textY,
                        'font-weight': 400,
                        'opacity': null,
                        'fill': 'black',
                        'cursor': 'pointer',
                        'class': _this.fontClass
                    });
                    text.textContent = label;
                }

                if ('tooltipText' in _this) {
                    $(featureGroup).qtip({
                        content: {text: tooltipText, title: tooltipTitle},
//                        position: {target: "mouse", adjust: {x: 15, y: 0}, effect: false},
                        position: {target: "mouse", adjust: {x: 25, y: 15}},
                        style: { width: true, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'}
                    });
                }

                $(featureGroup).mouseover(function (event) {
                    _this.trigger('feature:mouseover', {query: feature[infoWidgetId], feature: feature, featureType: feature.featureType, mouseoverEvent: event})
                });

                $(featureGroup).click(function (event) {
                    _this.trigger('feature:click', {query: feature[infoWidgetId], feature: feature, featureType: feature.featureType, clickEvent: event})
                });
                break;
            }
            rowY += rowHeight;
            textY += rowHeight;
        }
    };


    /****/
    var timeId = "write dom " + Utils.randomString(4);
    console.time(timeId);
    console.log(features.length);
    /****/


    var svgGroup = SVG.create('g');
    for (var i = 0, leni = features.length; i < leni; i++) {
        draw(features[i], svgGroup);
    }
    args.svgCanvasFeatures.appendChild(svgGroup);


    /****/
    console.timeEnd(timeId);
    /****/
};

//any item with chromosome start end
GeneRenderer.prototype = new Renderer({});

function GeneRenderer(args) {
    Renderer.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    this.fontClass = 'ocb-font-sourcesanspro ocb-font-size-12';
    this.toolTipfontClass = 'ocb-font-default';

    if (_.isObject(args)) {
        _.extend(this, args);
    }

    this.on(this.handlers);
};

GeneRenderer.prototype.setFeatureConfig = function (configObject) {
    _.extend(this, configObject);
};

GeneRenderer.prototype.render = function (features, args) {
    var _this = this;
    var draw = function (feature) {
        //get feature render configuration

        //get feature render configuration
        _this.setFeatureConfig(FEATURE_TYPES.gene);
        var color = _.isFunction(_this.color) ? _this.color(feature) : _this.color;
        var label = _.isFunction(_this.label) ? _this.label(feature) : _this.label;
        var height = _.isFunction(_this.height) ? _this.height(feature) : _this.height;
        var tooltipTitle = _.isFunction(_this.tooltipTitle) ? _this.tooltipTitle(feature) : _this.tooltipTitle;
        var tooltipText = _.isFunction(_this.tooltipText) ? _this.tooltipText(feature) : _this.tooltipText;
        var infoWidgetId = _.isFunction(_this.infoWidgetId) ? _this.infoWidgetId(feature) : _this.infoWidgetId;


        //get feature genomic information
        var start = feature.start;
        var end = feature.end;
        var length = (end - start) + 1;

        //transform to pixel position
        var width = length * args.pixelBase;


//        var svgLabelWidth = _this.getLabelWidth(label, args);
        var svgLabelWidth = label.length * 6.4;

        //calculate x to draw svg rect
        var x = _this.getFeatureX(feature, args);

        var maxWidth = Math.max(width, 2);
        var textHeight = 0;
        if (args.maxLabelRegionSize > args.regionSize) {
            textHeight = 9;
            maxWidth = Math.max(width, svgLabelWidth);
        }

        var rowY = 0;
        var textY = textHeight + height + 1;
        var rowHeight = textHeight + height + 5;

        while (true) {
            if (!(rowY in args.renderedArea)) {
                args.renderedArea[rowY] = new FeatureBinarySearchTree();
            }

            var foundArea;//if true, i can paint

            //check if gene transcripts can be painted
            var checkRowY = rowY;
            var foundTranscriptsArea = true;
            if (!_.isEmpty(feature.transcripts)) {
                for (var i = 0, leni = feature.transcripts.length + 1; i < leni; i++) {
                    if (!(checkRowY in args.renderedArea)) {
                        args.renderedArea[checkRowY] = new FeatureBinarySearchTree();
                    }
                    if (args.renderedArea[checkRowY].contains({start: x, end: x + maxWidth - 1})) {
                        foundTranscriptsArea = false;
                        break;
                    }
                    checkRowY += rowHeight;
                }
                if (foundTranscriptsArea == true) {
                    foundArea = args.renderedArea[rowY].add({start: x, end: x + maxWidth - 1});
                }
            } else {
                foundArea = args.renderedArea[rowY].add({start: x, end: x + maxWidth - 1});
            }

            //paint genes
            if (foundArea) {
                var featureGroup = SVG.addChild(args.svgCanvasFeatures, "g", {'feature_id': feature.id});
                var rect = SVG.addChild(featureGroup, 'rect', {
                    'x': x,
                    'y': rowY,
                    'width': width,
                    'height': height,
                    'stroke': '#3B0B0B',
                    'stroke-width': 0.5,
                    'fill': color,
                    'cursor': 'pointer'
                });

                if (args.maxLabelRegionSize > args.regionSize) {
                    var text = SVG.addChild(featureGroup, 'text', {
                        'i': i,
                        'x': x,
                        'y': textY,
                        'fill': 'black',
                        'cursor': 'pointer',
                        'class': _this.fontClass
                    });
                    text.textContent = label;
                }

                $(featureGroup).qtip({
                    content: {text: tooltipText, title: tooltipTitle},
//                    position: {target: "mouse", adjust: {x: 15, y: 0}, viewport: $(window), effect: false},
                    position: {target: "mouse", adjust: {x: 25, y: 15}},
                    style: { width: true, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'}
                });

                $(featureGroup).click(function (event) {
                    _this.trigger('feature:click', {query: feature[infoWidgetId], feature: feature, featureType: feature.featureType, clickEvent: event});
                });


                //paint transcripts
                var checkRowY = rowY + rowHeight;
                var checkTextY = textY + rowHeight;
                if (!_.isEmpty(feature.transcripts)) {
                    for (var i = 0, leni = feature.transcripts.length; i < leni; i++) { /*Loop over transcripts*/
                        if (!(checkRowY in args.renderedArea)) {
                            args.renderedArea[checkRowY] = new FeatureBinarySearchTree();
                        }
                        var transcript = feature.transcripts[i];
                        var transcriptX = _this.getFeatureX(transcript, args);
                        var transcriptWidth = (transcript.end - transcript.start + 1) * ( args.pixelBase);

                        //get type settings object
                        _this.setFeatureConfig(FEATURE_TYPES.transcript);
                        var transcriptColor = _.isFunction(_this.color) ? _this.color(transcript) : _this.color;
                        var label = _.isFunction(_this.label) ? _this.label(transcript) : _this.label;
                        var height = _.isFunction(_this.height) ? _this.height(transcript) : _this.height;
                        var tooltipTitle = _.isFunction(_this.tooltipTitle) ? _this.tooltipTitle(transcript) : _this.tooltipTitle;
                        var tooltipText = _.isFunction(_this.tooltipText) ? _this.tooltipText(transcript) : _this.tooltipText;
                        var infoWidgetId = _.isFunction(_this.infoWidgetId) ? _this.infoWidgetId(transcript) : _this.infoWidgetId;

                        //se resta el trozo del final del gen hasta el principio del transcrito y se le suma el texto del transcrito
//                        var svgLabelWidth = _this.getLabelWidth(label, args);
                        var svgLabelWidth = label.length * 6.4;
                        var maxWidth = Math.max(width, width - ((feature.end - transcript.start) * ( args.pixelBase)) + svgLabelWidth);


                        //add to the tree the transcripts size
                        args.renderedArea[checkRowY].add({start: x, end: x + maxWidth - 1});


                        var transcriptGroup = SVG.addChild(args.svgCanvasFeatures, 'g', {
                            "widgetId": transcript[infoWidgetId]
                        });


                        var rect = SVG.addChild(transcriptGroup, 'rect', {//this rect its like a line
                            'x': transcriptX,
                            'y': checkRowY + 1,
                            'width': transcriptWidth,
                            'height': height,
                            'fill': 'gray',
                            'cursor': 'pointer',
                            'feature_id': transcript.id
                        });
                        var text = SVG.addChild(transcriptGroup, 'text', {
                            'x': transcriptX,
                            'y': checkTextY,
                            'opacity': null,
                            'fill': 'black',
                            'cursor': 'pointer',
                            'class': _this.fontClass
                        });
                        text.textContent = label;


                        $(transcriptGroup).qtip({
                            content: {text: tooltipText, title: tooltipTitle},
//                            position: {target: 'mouse', adjust: {x: 15, y: 0}, viewport: $(window), effect: false},
                            position: {target: "mouse", adjust: {x: 25, y: 15}},
                            style: { width: true, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'}
                        });
                        $(transcriptGroup).click(function (event) {
                            var query = this.getAttribute("widgetId");
                            _this.trigger('feature:click', {query: query, feature: transcript, featureType: transcript.featureType, clickEvent: event});
                        });

                        //paint exons
                        for (var e = 0, lene = feature.transcripts[i].exons.length; e < lene; e++) {/* loop over exons*/
                            var exon = feature.transcripts[i].exons[e];
                            var exonStart = parseInt(exon.start);
                            var exonEnd = parseInt(exon.end);
                            var middle = args.width / 2;

                            var exonX = args.pixelPosition + middle - ((args.position - exonStart) * args.pixelBase);
                            var exonWidth = (exonEnd - exonStart + 1) * ( args.pixelBase);


                            _this.setFeatureConfig(FEATURE_TYPES.exon);
                            var color = _.isFunction(_this.color) ? _this.color(exon) : _this.color;
                            var label = _.isFunction(_this.label) ? _this.label(exon) : _this.label;
                            var height = _.isFunction(_this.height) ? _this.height(exon) : _this.height;
                            var tooltipTitle = _.isFunction(_this.tooltipTitle) ? _this.tooltipTitle(exon) : _this.tooltipTitle;
                            var tooltipText = _.isFunction(_this.tooltipText) ? _this.tooltipText(exon, transcript) : _this.tooltipText;
                            var infoWidgetId = _.isFunction(_this.infoWidgetId) ? _this.infoWidgetId(exon) : _this.infoWidgetId;

                            var exonGroup = SVG.addChild(args.svgCanvasFeatures, "g");

                            $(exonGroup).qtip({
                                content: {text: tooltipText, title: tooltipTitle},
//                                position: {target: 'mouse', adjust: {x: 15, y: 0}, viewport: $(window), effect: false},
                                position: {target: "mouse", adjust: {x: 25, y: 15}},
                                style: { width: true, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'}
                            });

                            var eRect = SVG.addChild(exonGroup, "rect", {//paint exons in white without coding region
                                "i": i,
                                "x": exonX,
                                "y": checkRowY - 1,
                                "width": exonWidth,
                                "height": height,
                                "stroke": "gray",
                                "stroke-width": 1,
                                "fill": "white",
                                "cursor": "pointer"
                            });
                            //XXX now paint coding region
                            var codingStart = 0;
                            var codingEnd = 0;
                            // 5'-UTR
                            if (transcript.genomicCodingStart > exonStart && transcript.genomicCodingStart < exonEnd) {
                                codingStart = parseInt(transcript.genomicCodingStart);
                                codingEnd = exonEnd;
                            } else {
                                // 3'-UTR
                                if (transcript.genomicCodingEnd > exonStart && transcript.genomicCodingEnd < exonEnd) {
                                    codingStart = exonStart;
                                    codingEnd = parseInt(transcript.genomicCodingEnd);
                                } else
                                // all exon is transcribed
                                if (transcript.genomicCodingStart < exonStart && transcript.genomicCodingEnd > exonEnd) {
                                    codingStart = exonStart;
                                    codingEnd = exonEnd;
                                }
//									else{
//										if(exonEnd < transcript.genomicCodingStart){
//
//									}
                            }
                            var coding = codingEnd - codingStart;
                            var codingX = args.pixelPosition + middle - ((args.position - codingStart) * args.pixelBase);
                            var codingWidth = (coding + 1) * ( args.pixelBase);

                            if (coding > 0) {
                                var cRect = SVG.addChild(exonGroup, "rect", {
                                    "i": i,
                                    "x": codingX,
                                    "y": checkRowY - 1,
                                    "width": codingWidth,
                                    "height": height,
                                    "stroke": transcriptColor,
                                    "stroke-width": 1,
                                    "fill": transcriptColor,
                                    "cursor": "pointer"
                                });
                                //XXX draw phase only at zoom 100, where this.pixelBase=10
                                for (var p = 0, lenp = 3 - exon.phase; p < lenp && Math.round(args.pixelBase) == 10 && exon.phase != -1 && exon.phase != null; p++) {//==10 for max zoom only
                                    SVG.addChild(exonGroup, "rect", {
                                        "i": i,
                                        "x": codingX + (p * 10),
                                        "y": checkRowY - 1,
                                        "width": args.pixelBase,
                                        "height": height,
                                        "stroke": color,
                                        "stroke-width": 1,
                                        "fill": 'white',
                                        "cursor": "pointer"
                                    });
                                }
                            }


                        }

                        checkRowY += rowHeight;
                        checkTextY += rowHeight;
                    }
                }// if transcrips != null
                break;
            }
            rowY += rowHeight;
            textY += rowHeight;
        }
    };

    //process features
    for (var i = 0, leni = features.length; i < leni; i++) {
        draw(features[i]);
    }
};
HistogramRenderer.prototype = new Renderer({});

function HistogramRenderer(args) {
    Renderer.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    //set default args
    this.histogramHeight = 75;
//    this.multiplier = 7;

    this.maxValue = 10;
    if (args != null) {
        if (args.height != null) {
            this.histogramHeight = args.height * 0.95;
        }
        if (args.histogramMaxFreqValue != null) {
            this.maxValue = args.histogramMaxFreqValue;
        }
    }
    //this.multiplier = 7;
    this.multiplier = this.histogramHeight / this.maxValue;

    //set instantiation args
    _.extend(this, args);

};


HistogramRenderer.prototype.render = function (features, args) {
    var middle = args.width / 2;
    var points = '';
    if (features.length > 0) {//Force first point at this.histogramHeight
        var firstFeature = features[0].value;
        var width = (firstFeature.end - firstFeature.start) * args.pixelBase;
        var x = args.pixelPosition + middle - ((args.position - parseInt(firstFeature.start)) * args.pixelBase);
        points = (x + (width / 2)) + ',' + this.histogramHeight + ' ';
    }

    var maxValue = 0;

    for (var i = 0, len = features.length; i < len; i++) {

        var feature = features[i].value;
        feature.start = parseInt(feature.start);
        feature.end = parseInt(feature.end);
        var width = (feature.end - feature.start);
        //get type settings object

        width = width * args.pixelBase;
        var x = args.pixelPosition + middle - ((args.position - feature.start) * args.pixelBase);

        if (feature.features_count == null) {
//            var height = Math.log(features[i].absolute);
            if (feature.absolute != 0) {
                feature.features_count = Math.log(features[i].absolute);
            } else {
                feature.features_count = 0;
            }
        }

//        var height = features[i].features_count;
//        if (height == null) {
//            height = features[i].value;
//            height = this.histogramHeight * height;
//        } else {
//        }
        var height = feature.features_count * this.multiplier;


        points += (x + (width / 2)) + "," + (this.histogramHeight - height) + " ";

    }
    if (features.length > 0) {//force last point at this.histogramHeight
        var lastFeature = features[features.length - 1].value;
        var width = (lastFeature.end - lastFeature.start) * args.pixelBase;
        var x = args.pixelPosition + middle - ((args.position - parseInt(lastFeature.start)) * args.pixelBase);
        points += (x + (width / 2)) + ',' + this.histogramHeight + ' ';

    }

    var pol = SVG.addChild(args.svgCanvasFeatures, "polyline", {
        "points": points,
        "stroke": "#000000",
        "stroke-width": 0.2,
        "fill": '#9493b1',
        "cursor": "pointer"
    });
};

SequenceRenderer.prototype = new Renderer({});

function SequenceRenderer(args){
    Renderer.call(this,args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    this.fontClass = 'ocb-font-ubuntumono ocb-font-size-16';
    this.toolTipfontClass = 'ocb-font-default';

    _.extend(this, args);

};


SequenceRenderer.prototype.render = function(features, args) {

    console.time("Sequence render "+features.items.sequence.length);
    var middle = args.width/2;

    var start = features.items.start;
    var seqStart = features.items.start;
    var seqString = features.items.sequence;

    for ( var i = 0; i < seqString.length; i++) {
        var x = args.pixelPosition+middle-((args.position-start)*args.pixelBase);
        start++;

        var text = SVG.addChild(args.svgCanvasFeatures,"text",{
            'x':x+1,
            'y':12,
            'fill':SEQUENCE_COLORS[seqString.charAt(i)],
            'class': this.fontClass
        });
        text.textContent = seqString.charAt(i);
        $(text).qtip({
            content:seqString.charAt(i)+" "+(seqStart+i).toString().replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,")/*+'<br>'+phastCons[i]+'<br>'+phylop[i]*/,
            position: {target: 'mouse', adjust: {x:15, y:0}, viewport: $(window), effect: false},
            style: { width:true, classes: this.toolTipfontClass+' qtip-light qtip-shadow'}
        });
    }

    console.timeEnd("Sequence render "+features.items.sequence.length);
//    this.trackSvgLayout.setNucleotidPosition(this.position);

};

//any item with chromosome start end
VcfMultisampleRenderer.prototype = new Renderer({});

function VcfMultisampleRenderer(args) {
    Renderer.call(this, args);
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    this.fontClass = 'ocb-font-sourcesanspro ocb-font-size-12';
    this.toolTipfontClass = 'ocb-font-default';

    if (_.isObject(args)) {
        _.extend(this, args);
    }

    this.on(this.handlers);
};


VcfMultisampleRenderer.prototype.render = function (features, args) {
    var _this = this;
    var draw = function (feature) {
        //get feature render configuration
        var color = _.isFunction(_this.color) ? _this.color(feature) : _this.color;
        var label = _.isFunction(_this.label) ? _this.label(feature) : _this.label;
        var height = _.isFunction(_this.height) ? _this.height(feature) : _this.height;
        var tooltipTitle = _.isFunction(_this.tooltipTitle) ? _this.tooltipTitle(feature) : _this.tooltipTitle;
        var tooltipText = _.isFunction(_this.tooltipText) ? _this.tooltipText(feature) : _this.tooltipText;
        var infoWidgetId = _.isFunction(_this.infoWidgetId) ? _this.infoWidgetId(feature) : _this.infoWidgetId;

        //get feature genomic information
        var start = feature.start;
        var end = feature.end;
        var length = (end - start) + 1;

        //check genomic length
        length = (length < 0) ? Math.abs(length) : length;
        length = (length == 0) ? 1 : length;

        //transform to pixel position
        var width = length * args.pixelBase;

        var svgLabelWidth = _this.getLabelWidth(label, args);

        //calculate x to draw svg rect
        var x = _this.getFeatureX(feature, args);

        var maxWidth = Math.max(width, 2);
        var textHeight = 0;
        if (args.regionSize < args.maxLabelRegionSize) {
            textHeight = 9;
            maxWidth = Math.max(width, svgLabelWidth);
        }


        var rowY = 0;
        var textY = textHeight + height;
        var rowHeight = textHeight + height + 2;


//        azul osucuro: 0/0
//        negro: ./.
//        rojo: 1/1
//        naranja 0/1

        var d00 = '';
        var dDD = '';
        var d11 = '';
        var d01 = '';
        var xs = x; // x start
        var xe = x + width; // x end
        var ys = 1; // y
        var yi = 6; //y increment
        var yi2 = 10; //y increment
        for (var i = 0, leni = feature.samples.length; i < leni; i++) {
            args.renderedArea[ys] = new FeatureBinarySearchTree();
            args.renderedArea[ys].add({start: xs, end: xe});
            var genotype = feature.samples[i].split(':')[0];
            switch (genotype) {
                case '0|0':
                case '0/0':
                    d00 += 'M' + xs + ',' + ys + ' L' + xe + ',' + ys + ' ';
                    d00 += 'L' + xe + ',' + (ys + yi) + ' L' + xs + ',' + (ys + yi) + ' z ';
                    break;
                case '.|.':
                case './.':
                    dDD += 'M' + xs + ',' + ys + ' L' + xe + ',' + ys + ' ';
                    dDD += 'L' + xe + ',' + (ys + yi) + ' L' + xs + ',' + (ys + yi) + ' z ';
                    break;
                case '1|1':
                case '1/1':
                    d11 += 'M' + xs + ',' + ys + ' L' + xe + ',' + ys + ' ';
                    d11 += 'L' + xe + ',' + (ys + yi) + ' L' + xs + ',' + (ys + yi) + ' z ';
                    break;
                case '0|1':
                case '0/1':
                case '1|0':
                case '1/0':
                    d01 += 'M' + xs + ',' + ys + ' L' + xe + ',' + ys + ' ';
                    d01 += 'L' + xe + ',' + (ys + yi) + ' L' + xs + ',' + (ys + yi) + ' z ';
                    break;
            }
            ys += yi2;
        }
        var featureGroup = SVG.addChild(args.svgCanvasFeatures, "g", {'feature_id': feature.id});
        var dummyRect = SVG.addChild(featureGroup, "rect", {
            'x': xs,
            'y': 1,
            'width': width,
            'height': ys,
            'fill': 'transparent',
            'cursor': 'pointer'
        });
        if (d00 != '') {
            var path = SVG.addChild(featureGroup, "path", {
                'd': d00,
                'fill': 'blue',
                'cursor': 'pointer'
            });
        }
        if (dDD != '') {
            var path = SVG.addChild(featureGroup, "path", {
                'd': dDD,
                'fill': 'black',
                'cursor': 'pointer'
            });
        }
        if (d11 != '') {
            var path = SVG.addChild(featureGroup, "path", {
                'd': d11,
                'fill': 'red',
                'cursor': 'pointer'
            });
        }
        if (d01 != '') {
            var path = SVG.addChild(featureGroup, "path", {
                'd': d01,
                'fill': 'orange',
                'cursor': 'pointer'
            });
        }


        var lastSampleIndex = 0;
        $(featureGroup).qtip({
            content: {text: tooltipText + '<br>' + feature.samples[lastSampleIndex], title: tooltipTitle},
//                        position: {target: "mouse", adjust: {x: 15, y: 0}, effect: false},
            position: {target: "mouse", adjust: {x: 25, y: 15}},
            style: { width: true, classes: _this.toolTipfontClass + ' ui-tooltip ui-tooltip-shadow'}
        });
        $(featureGroup).mousemove(function (event) {
            var sampleIndex = parseInt(event.offsetY / yi2);
            if (sampleIndex != lastSampleIndex) {
                console.log(sampleIndex);
                $(featureGroup).qtip('option', 'content.text', tooltipText + '<br>' + feature.samples[sampleIndex]);
            }
            lastSampleIndex = sampleIndex;
        });
    };

    //process features
    for (var i = 0, leni = features.length; i < leni; i++) {
        var feature = features[i];
        draw(feature);
    }
};

function LegendPanel(args){
	this.width = 200;
	this.height = 250;
	
	if (args != null){
        if (args.title!= null){
        	this.title = args.title;       
        }
        if (args.targetId!= null){
        	this.targetId = args.targetId;       
        }
        if (args.width!= null){
        	this.width = args.width;       
        }
        if (args.height!= null){
        	this.height = args.height;       
        }
    }
	
	
};

LegendPanel.prototype.getColorItems = function(legend){
	panelsArray = new Array();
	
	for ( var item in legend) {
//		var color = legend[item].toString().replace("#", "");
//		var cp = new Ext.picker.Color();
//		cp.width = 20;
//		cp.colors = [color];
		var size=15;
		var color = Ext.create('Ext.draw.Component', {
        width: size,
        height: size,
        items:[{
				type: 'rect',
				fill: legend[item],
				x:0,y:0,
				width: size,
				height : size
				}]
		});
		
		var name = Utils.formatText(item, "_");
		
		var panel = Ext.create('Ext.panel.Panel', {
			height:size,
			border:false,
			flex:1,
			margin:"1 0 0 1",
		    layout: {type: 'hbox',align:'stretch' },
		    items: [color, {xtype: 'tbtext',text:name, margin:"1 0 0 3"} ]
		});
		
		panelsArray.push(panel);
	}
	
	return panelsArray;
};




LegendPanel.prototype.getPanel = function(legend){
	var _this=this;
	
	if (this.panel == null){
		
		var items = this.getColorItems(legend);
		
		this.panel  = Ext.create('Ext.panel.Panel', {
			bodyPadding:'0 0 0 2',
			border:false,
			layout: {
		        type: 'vbox',
		        align:'stretch' 
		    },
			items:items,
			width:this.width,
			height:items.length*20
		});		
	}	
	
	return this.panel;
};

LegendPanel.prototype.getButton = function(legend){
	var _this=this;
	
	if (this.button == null){
		
		this.button = Ext.create('Ext.button.Button', {
			text : this.title,
			menu : {
                plain:true,
                items: [this.getPanel(legend)]
            }
		});
	}	
	return this.button;
	
};

function LegendWidget(args){
	
	this.width = 300;
	this.height = 300;
	this.title = "Legend";
	
	if (args != null){
        if (args.title!= null){
        	this.title = args.title;       
        }
        if (args.targetId!= null){
        	this.targetId = args.targetId;       
        }
        if (args.width!= null){
        	this.width = args.width;       
        }
        if (args.height!= null){
        	this.height = args.height;       
        }
    }
	
	this.legendPanel = new LegendPanel();
	
};

LegendWidget.prototype.draw = function(legend){
	var _this = this;
	if(this.panel==null){
		
		var item = this.legendPanel.getPanel(legend);
	
		this.panel = Ext.create('Ext.ux.Window', {
			title : this.title,
			resizable: false,
			constrain:true,
			closable:true,
			width: item.width+10,
			height: item.height+70,
			items : [item],
			buttonAlign:'right',
			 layout: {
		        type: 'hbox',
		        align:'stretch' 
		    },
			buttons:[
					{text:'Close', handler: function(){_this.panel.close();}}
			]
		});
	}
	this.panel.show();
	
	
};
function UrlWidget(args) {
    var _this = this;

    _.extend(this, Backbone.Events);

    this.id = Utils.genId("UrlWidget");

    this.targetId = null;
    this.title = "Custom url";
    this.width = 500;
    this.height = 400;

    _.extend(this, args);
    this.on(this.handlers);

};

UrlWidget.prototype.draw = function () {
    if (this.panel == null) {
        this.render();
    }
    this.panel.show();
};

UrlWidget.prototype.render = function () {
    var _this = this;

    this.urlField = Ext.create('Ext.form.field.Text', {
        margin: "0 2 2 0",
        labelWidth: 30,
        width: this.width - 55,
        fieldLabel: 'URL',
        emptyText: 'enter a valid url',
//		value : "http://das.sanger.ac.uk/das/grc_region_GRCh37/features",
        value: "http://www.ensembl.org/das/Homo_sapiens.GRCh37.gene/features",
        listeners: { change: {fn: function () {
            var dasName = this.value.split('/das/')[1].split('/')[0];
            _this.trackNameField.setValue(dasName);
        }}
        }
    });
    this.checkButton = Ext.create('Ext.button.Button', {
        text: 'Check',
        handler: function () {
            _this.form.setLoading();
//			var dasDataAdapter = new DasRegionDataAdapter({
//				url : _this.urlField.getValue()
//			});
//			dasDataAdapter.successed.addEventListener(function() {
//				_this.contentArea.setValue(dasDataAdapter.xml);
//				_this.form.setLoading(false);
//			});
//
//			dasDataAdapter.onError.addEventListener(function() {
//				_this.contentArea.setValue("XMLHttpRequest cannot load. This server is not allowed by Access-Control-Allow-Origin");
//				_this.form.setLoading(false);
//			});
//			dasDataAdapter.fill(1, 1, 1);

            var dasAdapter = new DasAdapter({
                url: _this.urlField.getValue(),
                featureCache: {
                    gzip: false,
                    chunkSize: 10000
                },
                handlers: {
                    'url:check': function (event) {
                        console.log(event.data);
                        _this.contentArea.setValue(event.data);
                        _this.form.setLoading(false);

                    },
                    'error': function () {
                        _this.contentArea.setValue("XMLHttpRequest cannot load. This server is not allowed by Access-Control-Allow-Origin");
                        _this.form.setLoading(false);

                    }
                }
            });

            dasAdapter.checkUrl();
        }
    });
    this.trackNameField = Ext.create('Ext.form.field.Text', {
        name: 'file',
//        fieldLabel: 'Track name',
        allowBlank: false,
        value: _this.urlField.value.split('/das/')[1].split('/')[0],
        emptyText: 'Choose a name',
        flex: 1
    });
    this.panelSettings = Ext.create('Ext.panel.Panel', {
        layout: 'hbox',
        border: false,
        title: 'Track name',
        cls: "panel-border-top",
        bodyPadding: 10,
        width: this.width - 2,
        items: [this.trackNameField]
    });
    this.contentArea = Ext.create('Ext.form.field.TextArea', {
        margin: "-1",
        width: this.width,
        height: this.height
    });
    this.infobar = Ext.create('Ext.toolbar.Toolbar', {
        height: 28,
        cls: "bio-border-false",
        items: [this.urlField, this.checkButton]
    });
    this.form = Ext.create('Ext.panel.Panel', {
        border: false,
        items: [this.infobar, this.contentArea, this.panelSettings]
    });

    this.panel = Ext.create('Ext.ux.Window', {
        title: this.title,
        layout: 'fit',
        resizable: false,
        items: [this.form],
        buttons: [
            {
                text: 'Add',
                handler: function () {
                    _this.trigger('addButton:click', {name: _this.trackNameField.getValue(), url: _this.urlField.getValue()});
                    _this.panel.close();
                }
            },
            {text: 'Cancel', handler: function () {
                _this.panel.close();
            }}
        ],
        listeners: {
            destroy: function () {
                delete _this.panel;
            }
        }
    });
};
function FileWidget(args){
	var _this=this;

    _.extend(this, Backbone.Events);

    this.id = Utils.genId("FileWidget");
	this.targetId;
	this.wum = true;
	this.tags = [];
    this.viewer;
    this.title;
	this.dataAdapter;

    this.args = args;

    _.extend(this, args);


    this.on(this.handlers);

//	this.browserData = new BrowserDataWidget();
	/** Events i listen **/
//	this.browserData.onSelect.addEventListener(function (sender, data){
//		_this.trackNameField.setValue(data.filename);
//		_this.fileNameLabel.setText('<span class="emph">'+ data.filename +'</span> <span class="info">(server)</span>',false);
//		_this.panel.setLoading();
//	});
//    this.browserData.adapter.onReadData.addEventListener(function (sender, data){
//    	console.log(data)
//    	_this.trackNameField.setValue(data.filename);
//    	_this.fileNameLabel.setText('<span class="emph">'+ data.filename +'</span> <span class="info">(server)</span>',false);
//    	_this.loadFileFromServer(data);
//    	_this.panel.setLoading(false);
//	});
    
//    this.chartWidgetByChromosome = new ChartWidget({height:200,width:570});
};

FileWidget.prototype.getTitleName = function(){
	return this.trackNameField.getValue();
};


FileWidget.prototype.getFileFromServer = function(){
	//abstract method
};

FileWidget.prototype.loadFileFromLocal = function(){
	//abstract method
};

//FileWidget.prototype.getChartItems = function(){
//	return [this.chartWidgetByChromosome.getChart(["features","chromosome"])];
//};

FileWidget.prototype.getFileUpload = function(){
	var _this = this;
	this.uploadField = Ext.create('Ext.form.field.File', {
		msgTarget : 'side',
		flex:1,
        padding:1,
//		width:75,
		emptyText: 'Choose a file',
        allowBlank: false,
        anchor: '100%',
		buttonText : 'Browse local',
//		buttonOnly : true,
		listeners : {
			change : {
				fn : function() {
					_this.panel.setLoading();
					var file = document.getElementById(_this.uploadField.fileInputEl.id).files[0];

					_this.trackNameField.setValue(file.name);
					_this.fileNameLabel.setText('<span class="emph">'+ file.name +'</span> <span class="info">(local)</span>',false);
					_this.loadFileFromLocal(file);
					_this.panel.setLoading(false);

				}
			}
		}
	});
	return this.uploadField;
};


FileWidget.prototype.draw = function(){
	var _this = this;
	
	if (this.openDialog == null){
	
		/** Bar for the chart **/
		var featureCountBar = Ext.create('Ext.toolbar.Toolbar');
		this.featureCountLabel = Ext.create('Ext.toolbar.TextItem', {
			text:'<span class="dis">No file loaded</span>'
		});
		featureCountBar.add([this.featureCountLabel]);
		
		/** Bar for the file upload browser **/
		var browseBar = Ext.create('Ext.toolbar.Toolbar',{cls:'bio-border-false'});
		browseBar.add(this.getFileUpload());
		
		this.panel = Ext.create('Ext.panel.Panel', {
			border: false,
			cls:'panel-border-top panel-border-bottom',
	//		padding: "0 0 10 0",
			height:230,
			title: "Previsualization",
//		    items : this.getChartItems(),
		    bbar:featureCountBar
		});
		
	//	var colorPicker = Ext.create('Ext.picker.Color', {
	//	    value: '993300',  // initial selected color
	//	    listeners: {
	//	        select: function(picker, selColor) {
	//	            alert(selColor);
	//	        }
	//	    }
	//	});
		this.trackNameField = Ext.create('Ext.form.field.Text',{
			name: 'file',
            fieldLabel: 'Track Name',
            allowBlank: false,
            value: 'New track from '+this.title+' file',
            emptyText: 'Choose a name',
            flex:1
		});
		
		var panelSettings = Ext.create('Ext.panel.Panel', {
			border: false,
			layout: 'hbox',
			bodyPadding: 10,
		    items : [this.trackNameField]	 
		});
		
		
		if(this.wum){
//			this.btnBrowse = Ext.create('Ext.button.Button', {
//		        text: 'Browse server',
//		        disabled:true,
////		        iconCls:'icon-local',
////		        cls:'x-btn-default-small',
//		        handler: function (){
//	    	   		_this.browserData.draw($.cookie('bioinfo_sid'),_this.tags);
//	       		}
//			});
			
//			browseBar.add(this.btnBrowse);
			
			if($.cookie('bioinfo_sid') != null){
				this.sessionInitiated();
			}else{
				this.sessionFinished();
			}
		}
		
		this.fileNameLabel = Ext.create('Ext.toolbar.TextItem', {
//			text:'<span class="emph">Select a <span class="info">local</span> file or a <span class="info">server</span> file from your account.</span>'
		});
//		browseBar.add(['->',this.fileNameLabel]);
		
		
		
		this.btnOk = Ext.create('Ext.button.Button', {
			text:'Ok',
			disabled:true,
			handler: function(){
				_this.trigger('okButton:click',{fileName:_this.file.name, adapter:_this.adapter});
				_this.openDialog.close();
			}
		});
		
		this.openDialog = Ext.create('Ext.window.Window', {
			title : 'Open '+this.title+' file',
//			taskbar:Ext.getCmp(this.args.viewer.id+'uxTaskbar'),
			width : 600,
	//		bodyPadding : 10,
			resizable:false,
			items : [browseBar, /*this.panel,*/ panelSettings],
			buttons:[this.btnOk, 
			         {text:'Cancel', handler: function(){_this.openDialog.close();}}],
			listeners: {
			    	scope: this,
			    	minimize:function(){
						this.openDialog.hide();
			       	},
			      	destroy: function(){
			       		delete this.openDialog;
			      	}
		    	}
		});
		
	}
	this.openDialog.show();
};

//FileWidget.prototype._loadChartInfo = function(){
//
//	var datastore = new Array();
// 	for ( var chromosome in this.adapter.featuresByChromosome) {
//		datastore.push({ features: this.adapter.featuresByChromosome[chromosome], chromosome: chromosome });
//	}
// 	this.chartWidgetByChromosome.getStore().loadData(datastore);
//
// 	this.panel.setLoading(false);
// 	this.featureCountLabel.setText("Features count: " + this.adapter.featuresCount, false);
//};



FileWidget.prototype.sessionInitiated = function (){
//	if(this.btnBrowse!=null){
//		this.btnBrowse.enable();
//	}
};
FileWidget.prototype.sessionFinished = function (){
//	if(this.btnBrowse!=null){
//		this.btnBrowse.disable();
//	}
};
BEDFileWidget.prototype.getTitleName = FileWidget.prototype.getTitleName;
BEDFileWidget.prototype.getFileUpload = FileWidget.prototype.getFileUpload;
BEDFileWidget.prototype.draw = FileWidget.prototype.draw;
BEDFileWidget.prototype.sessionInitiated = FileWidget.prototype.sessionInitiated;
BEDFileWidget.prototype.sessionFinished = FileWidget.prototype.sessionFinished;
BEDFileWidget.prototype.getChartItems = FileWidget.prototype.getChartItems;
BEDFileWidget.prototype._loadChartInfo = FileWidget.prototype._loadChartInfo;

function BEDFileWidget(args){
	if (args == null){
		args = new Object();
	}
	args.title = "BED";
	args.tags = ["bed"];
	FileWidget.prototype.constructor.call(this, args);
	
};


BEDFileWidget.prototype.loadFileFromLocal = function(file){
	var _this = this;
	this.file = file;
	this.adapter = new BEDDataAdapter(new FileDataSource(file),{species:this.viewer.species});
    this.adapter.on('file:load',function(e){
//		_this._loadChartInfo();
    });
	_this.btnOk.enable();
};


BEDFileWidget.prototype.loadFileFromServer = function(data){
	this.file = {name:data.filename};
	this.adapter = new BEDDataAdapter(new StringDataSource(data.data),{async:false,species:this.viewer.species});
	this._loadChartInfo();
	this.btnOk.enable();
};

GFFFileWidget.prototype.getTitleName = FileWidget.prototype.getTitleName;
GFFFileWidget.prototype.getFileUpload = FileWidget.prototype.getFileUpload;
GFFFileWidget.prototype.draw = FileWidget.prototype.draw;
GFFFileWidget.prototype.sessionInitiated = FileWidget.prototype.sessionInitiated;
GFFFileWidget.prototype.sessionFinished = FileWidget.prototype.sessionFinished;
GFFFileWidget.prototype.getChartItems = FileWidget.prototype.getChartItems;
GFFFileWidget.prototype._loadChartInfo = FileWidget.prototype._loadChartInfo;

function GFFFileWidget(args){
	if (args == null){
		args = {};
	}
	this.version = "2";
    if (args.version!= null){
    	this.version = args.version;       
    }
	args.title = "GFF"+this.version;
	args.tags = ["gff"];
	FileWidget.prototype.constructor.call(this, args);
};



GFFFileWidget.prototype.loadFileFromLocal = function(file){
	var _this = this;
	this.file = file;
	switch(this.version){
	case "2":
	case 2:
		this.adapter = new GFF2DataAdapter(new FileDataSource(file),{species:this.viewer.species});
		break;
	case "3":
	case 3:
		this.adapter = new GFF3DataAdapter(new FileDataSource(file),{species:this.viewer.species});
		break;
	default :
		this.adapter = new GFF2DataAdapter(new FileDataSource(file),{species:this.viewer.species});
		break;
	}
	
	this.adapter.on('file:load',function(e){
//		_this._loadChartInfo();
	});
	_this.btnOk.enable();
};


GFFFileWidget.prototype.loadFileFromServer = function(data){
	this.file = {name:data.filename};
	switch(this.version){
	case "2":
	case 2:
		this.adapter = new GFF2DataAdapter(new StringDataSource(data.data),{async:false,species:this.viewer.species});
		break;
	case "3":
	case 3:
		this.adapter = new GFF3DataAdapter(new StringDataSource(data.data),{async:false,species:this.viewer.species});
		break;
	default :
		this.adapter = new GFF2DataAdapter(new StringDataSource(data.data),{async:false,species:this.viewer.species});
		break;
	}
	
	this._loadChartInfo();
	this.btnOk.enable();
};

GTFFileWidget.prototype.getTitleName = FileWidget.prototype.getTitleName;
GTFFileWidget.prototype.getFileUpload = FileWidget.prototype.getFileUpload;
GTFFileWidget.prototype.draw = FileWidget.prototype.draw;
GTFFileWidget.prototype.sessionInitiated = FileWidget.prototype.sessionInitiated;
GTFFileWidget.prototype.sessionFinished = FileWidget.prototype.sessionFinished;
GTFFileWidget.prototype.getChartItems = FileWidget.prototype.getChartItems;
GTFFileWidget.prototype._loadChartInfo = FileWidget.prototype._loadChartInfo;

function GTFFileWidget(args){
	if (args == null){
		args = new Object();
	}
	args.title = "GTF";
	args.tags = ["gtf"];
	FileWidget.prototype.constructor.call(this, args);
	
};

GTFFileWidget.prototype.loadFileFromLocal = function(file){
	var _this = this;
	this.file = file;
	this.adapter = new GTFDataAdapter(new FileDataSource(file),{species:this.viewer.species});
	this.adapter.onLoad.addEventListener(function(sender){
		console.log(_this.adapter.featuresByChromosome);
		_this._loadChartInfo();
	});
	_this.btnOk.enable();
};


GTFFileWidget.prototype.loadFileFromServer = function(data){
	this.file = {name:data.filename};
	this.adapter = new GTFDataAdapter(new StringDataSource(data.data),{async:false,species:this.viewer.species});
	this._loadChartInfo();
	this.btnOk.enable();
};


VCFFileWidget.prototype.getTitleName = FileWidget.prototype.getTitleName;
VCFFileWidget.prototype.getFileUpload = FileWidget.prototype.getFileUpload;
VCFFileWidget.prototype.draw = FileWidget.prototype.draw;
VCFFileWidget.prototype.sessionInitiated = FileWidget.prototype.sessionInitiated;
VCFFileWidget.prototype.sessionFinished = FileWidget.prototype.sessionFinished;
VCFFileWidget.prototype.getChartItems = FileWidget.prototype.getChartItems;
VCFFileWidget.prototype._loadChartInfo = FileWidget.prototype._loadChartInfo;

function VCFFileWidget(args){
	if (args == null){
		args = new Object();
	}
	args.title = "VCF";
	args.tags = ["vcf"];
	FileWidget.prototype.constructor.call(this, args);
};

VCFFileWidget.prototype.loadFileFromLocal = function(file){
	var _this = this;
	this.file = file;
	this.adapter = new VCFDataAdapter(new FileDataSource(file),{species:this.viewer.species});
	this.adapter.on('file:load',function(sender){
		console.log(_this.adapter.featuresByChromosome);
//		_this._loadChartInfo();
	});
	_this.btnOk.enable();
};

VCFFileWidget.prototype.loadFileFromServer = function(data){
	this.file = {name:data.filename};
	this.adapter = new VCFDataAdapter(new StringDataSource(data.data),{async:false,species:this.viewer.species});
//	this._loadChartInfo();
	this.btnOk.enable();
};


function GenomeViewer(args) {
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);

    var _this = this;
    this.id = Utils.genId("GenomeViewer");

    //set default args
    this.version = 'Genome Viewer';
    this.targetId;

    this.quickSearchResultFn;
    this.quickSearchDisplayKey;

    this.drawNavigationBar = true;
    this.drawKaryotypePanel = true;
    this.drawChromosomePanel = true;
    this.drawRegionOverviewPanel = true;
    this.karyotypePanelConfig = {
        collapsed: false,
        collapsible: true
    }
    this.chromosomePanelConfig = {
        collapsed: false,
        collapsible: true
    }
    this.RegionPanelConfig = {
        collapsed: false,
        collapsible: true
    }
    this.drawStatusBar = true;
    this.border = true;
    this.resizable = true;
    this.sidePanel = true;//enable or disable sidePanel at construction
    this.trackListTitle = 'Detailed information';//enable or disable sidePanel at construction
    this.trackPanelScrollWidth = 18;
    this.availableSpecies = {
        "text": "Species",
        "items": [
            {
                "text": "Vertebrates",
                "items": [
                    {"text": "Homo sapiens", "assembly": "GRCh37.p10", "region": {"chromosome": "13", "start": 32889611, "end": 32889611}, "chromosomes": ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "MT"], "url": "ftp://ftp.ensembl.org/pub/release-71/"},
                    {"text": "Mus musculus", "assembly": "GRCm38.p1", "region": {"chromosome": "1", "start": 18422009, "end": 18422009}, "chromosomes": ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "X", "Y", "MT"], "url": "ftp://ftp.ensembl.org/pub/release-71/"}
                ]
            }
        ]
    };
    this.species = this.availableSpecies.items[0].items[0];
    this.zoom;

    this.chromosomes;
    this.chromosomeList;

    //set instantiation args, must be last
    _.extend(this, args);

    this.defaultRegion = new Region(this.region);

    this.width;
    this.height;
    this.sidePanelWidth = (this.sidePanel) ? 25 : 0;


    //events attachments
    this.on(this.handlers);

    this.fullscreen = false;
    this.resizing = false;


    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
}

GenomeViewer.prototype = {

    render: function (targetId) {
        var _this = this;
        this.targetId = (targetId) ? targetId : this.targetId;
        if ($('#' + this.targetId).length < 1) {
            console.log('targetId not found in DOM');
            return;
        }

        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div class="bootstrap" id="' + this.id + '" class="ocb-gv ocb-box-vertical"></div>')[0];
        $(this.targetDiv).append(this.div);

        var width = Math.max($(this.div).width(), $(this.targetDiv).width())
        if (width == 0) {
            console.log('target div width is zero');
            return
        }
        this.width = width;

        if (this.border) {
            var border = (_.isString(this.border)) ? this.border : '1px solid lightgray';
            $(this.div).css({border: border});
        }


        this.navigationbarDiv = $('<div id="navigation-' + this.id + '" class="ocb-gv-navigation"></div>')[0];
        $(this.div).append(this.navigationbarDiv);

        this.centerPanelDiv = $('<div id="center-' + this.id + '" class="ocb-gv-center"></div>')[0];
        $(this.div).append(this.centerPanelDiv);

        this.statusbarDiv = $('<div id="statusbar-' + this.id + '" class="ocb-gv-status"></div>');
        $(this.div).append(this.statusbarDiv);


        this.rightSidebarDiv = $('<div id="rightsidebar-' + this.id + '" style="position:absolute; z-index:50;right:0px;"></div>')[0];
        this.leftSidebarDiv = $('<div id="leftsidebar-' + this.id + '" style="position:absolute; z-index:50;left:0px;"></div>')[0];
        $(this.centerPanelDiv).append(this.rightSidebarDiv);
        $(this.centerPanelDiv).append(this.leftSidebarDiv);


        this.karyotypeDiv = $('<div id="karyotype-' + this.id + '"></div>');
        $(this.centerPanelDiv).append(this.karyotypeDiv);

        this.chromosomeDiv = $('<div id="chromosome-' + this.id + '"></div>');
        $(this.centerPanelDiv).append(this.chromosomeDiv);

        this.trackListPanelsDiv = $('<div id="trackListPanels-' + this.id + '" class="trackListPanels" ></div>');
        $(this.centerPanelDiv).append(this.trackListPanelsDiv);

        this.regionDiv = $('<div id="region-' + this.id + '" ></div>');
        $(this.trackListPanelsDiv).append(this.regionDiv);

        this.tracksDiv = $('<div id="tracks-' + this.id + '" ></div>');
        $(this.trackListPanelsDiv).append(this.tracksDiv);

        this.rendered = true;
    },
    draw: function () {
        if (!this.rendered) {
            console.info('Genome Viewer is not rendered yet');
            return;
        }
        var _this = this;

        this.chromosomes = this.getChromosomes();

        this._setWidth(this.width);
        this.setMinRegion(this.region, this.getSVGCanvasWidth());
        this.zoom = this._calculateZoomByRegion(this.region);

        // Resize
        if (this.resizable) {
            $(window).resize(function (event) {
                if (event.target == window) {
                    if (!_this.resizing) {//avoid multiple resize events
                        _this.resizing = true;
                        _this._setWidth($(_this.targetDiv).width());
                        setTimeout(function () {
                            _this.resizing = false;
                        }, 400);
                    }
                }
            });
//            $(this.targetDiv).resizable({
//                handles: 'e',
//                ghost: true,
//                stop: function (event, ui) {
//                    _this._setWidth($(_this.targetDiv).width());
//                }
//            });
        }


        /* Navigation Bar */
        if (this.drawNavigationBar) {
            this.navigationBar = this._createNavigationBar($(this.navigationbarDiv).attr('id'));
            this.navigationBar.setZoom(this.zoom);
        }


        /*karyotype Panel*/
        if (this.drawKaryotypePanel) {
            this.karyotypePanel = this._drawKaryotypePanel($(this.karyotypeDiv).attr('id'));
        }

        /* Chromosome Panel */
        if (this.drawChromosomePanel) {
            this.chromosomePanel = this._drawChromosomePanel($(this.chromosomeDiv).attr('id'));
        }

        /* Region Panel, is a TrackListPanel Class */
        if (this.drawRegionOverviewPanel) {
            this.regionOverviewPanel = this._createRegionOverviewPanel($(this.regionDiv).attr('id'));
        }
        /*TrackList Panel*/
        this.trackListPanel = this._createTrackListPanel($(this.tracksDiv).attr('id'));

        /*Status Bar*/
        if (this.drawStatusBar) {
            this.statusBar = this._createStatusBar($(this.statusbarDiv).attr('id'));
        }


        this.on('region:change region:move', function (event) {
            if (event.sender != _this) {
                _this._setRegion(event.region);
            }
        });

        this.on('species:change', function (event) {
            _this.species = event.species;
            _this.chromosomes = _this.getChromosomes();
        });

        $("html").bind('keydown.genomeViewer', function (e) {
            switch (e.keyCode) {
                case 40://down arrow
                case 109://minus key
                    if (e.shiftKey) {
                        _this.increaseZoom(-10);
                    }
                    break;
                case 38://up arrow
                case 107://plus key
                    if (e.shiftKey) {
                        _this.increaseZoom(10);
                    }
                    break;
            }
        });

    },

    destroy: function () {
        $(this.div).remove();
        this.off();
        this.rendered = false;
        $("html").unbind(".genomeViewer");
        $("body").unbind(".genomeViewer");
        delete this;
    },
    getChromosomes: function () {
        var saveChromosomes = function (chromsomeList) {
            var chromosomes = {};
            for (var i = 0; i < chromsomeList.length; i++) {
                var chromosome = chromsomeList[i];
                chromosomes[chromosome.name] = chromosome;
            }
            return chromosomes;
        }

        var chromosomes;
        if (typeof this.chromosomeList !== 'undefined') {
            chromosomes = saveChromosomes(this.chromosomeList);
        } else {
            CellBaseManager.get({
                species: this.species,
                category: 'genomic',
                subCategory: 'chromosome',
                resource: 'all',
                async: false,
                success: function (data) {
                    chromosomes = saveChromosomes(data.response.result.chromosomes);
                },
                error: function (data) {
                    console.log('Could not get chromosome list');
                }
            });
        }
        return chromosomes;
    },
    /**/
    /*Components*/
    /**/

    _createNavigationBar: function (targetId) {
        var _this = this;

        if (!$.isFunction(this.quickSearchResultFn)) {
            this.quickSearchResultFn = function (query) {
                var results = [];
                var speciesCode = Utils.getSpeciesCode(this.species.text).substr(0, 3);

                CellBaseManager.get({
                    host: 'http://ws.bioinfo.cipf.es/cellbase/rest',
                    species: speciesCode,
                    version: 'latest',
                    category: 'feature',
                    subCategory: 'id',
                    query: query,
                    resource: 'starts_with',
                    params: {
                        of: 'json'
                    },
                    async: false,
                    success: function (data, textStatus, jqXHR) {
                        for (var i in data[0]) {
                            results.push(data[0][i].displayId);
                        }
                    }
                });
                return results;
            };
        }

        var goFeature = function (featureName) {
            if (featureName != null) {
                if (featureName.slice(0, "rs".length) == "rs" || featureName.slice(0, "AFFY_".length) == "AFFY_" || featureName.slice(0, "SNP_".length) == "SNP_" || featureName.slice(0, "VAR_".length) == "VAR_" || featureName.slice(0, "CRTAP_".length) == "CRTAP_" || featureName.slice(0, "FKBP10_".length) == "FKBP10_" || featureName.slice(0, "LEPRE1_".length) == "LEPRE1_" || featureName.slice(0, "PPIB_".length) == "PPIB_") {
                    this.openSNPListWidget(featureName);
                } else {
                    console.log(featureName);
                    CellBaseManager.get({
                        species: _this.species,
                        category: 'feature',
                        subCategory: 'gene',
                        query: featureName,
                        resource: 'info',
                        params: {
                            include: 'chromosome,start,end'
                        },
                        success: function (data) {
                            var feat = data.response[0].result[0];
                            var regionStr = feat.chromosome + ":" + feat.start + "-" + feat.end;
                            var region = new Region();
                            region.parse(regionStr);
                            region = _this._checkRegion(region);
                            _this.region = region;
                            _this.trigger('region:change', {region: _this.region, sender: _this});
                        }
                    });
                }
            }
        };

        var navigationBar = new NavigationBar({
            targetId: targetId,
            availableSpecies: this.availableSpecies,
            species: this.species,
            region: this.region,
            width: this.width,
            svgCanvasWidthOffset: this.trackPanelScrollWidth + this.sidePanelWidth,
            autoRender: true,
            quickSearchResultFn: this.quickSearchResultFn,
            quickSearchDisplayKey: this.quickSearchDisplayKey,
            handlers: {
                'region:change': function (event) {
                    event.region = _this._checkRegion(event.region);
                    _this.setMinRegion(event.region, _this.getSVGCanvasWidth())
                    _this.trigger('region:change', event);
                },
                'zoom:change': function (event) {
                    _this.trigger('zoom:change', event);
                },
                'karyotype-button:change': function (event) {
                    if (event.selected) {
                        _this.karyotypePanel.show();
                    } else {
                        _this.karyotypePanel.hide();
                    }
                },
                'chromosome-button:change': function (event) {
                    if (event.selected) {
                        _this.chromosomePanel.show();
                    } else {
                        _this.chromosomePanel.hide();
                    }
                },
                'region-button:change': function (event) {
                    if (event.selected) {
                        _this.regionOverviewPanel.show();
                    } else {
                        _this.regionOverviewPanel.hide();
                    }
                },
                'region:move': function (event) {
                    _this.trigger('region:move', event);
                },
                'species:change': function (event) {
                    _this.trigger('species:change', event);
                    _this.setRegion(event.species.region);
                },
                'fullscreen:click': function (event) {
                    if (_this.fullscreen) {
                        $(_this.div).css({width: 'auto'});
                        Utils.cancelFullscreen();//no need to pass the dom object;
                        _this.fullscreen = false;
                    } else {
                        $(_this.div).css({width: screen.width});
                        Utils.launchFullScreen(_this.div);
                        _this.fullscreen = true;
                    }
                },
                'restoreDefaultRegion:click': function (event) {
                    event.region = _this._checkRegion(event.region);
                    _this.setMinRegion(_this.defaultRegion, _this.getSVGCanvasWidth());
                    event.region = _this.defaultRegion;
                    _this.trigger('region:change', event);
                },
                'autoHeight-button:click': function (event) {
                    _this.enableAutoHeight();
                },
                'quickSearch:select': function (event) {
                    goFeature(event.item);
                    _this.trigger('quickSearch:select', event);
                },
                'quickSearch:go': function (event) {
                    goFeature(event.item);
                }
            }
        });

        this.on('region:change', function (event) {
//            if (event.sender != navigationBar) {
            _this.navigationBar.setRegion(event.region);
//            }
            _this.zoom = _this._calculateZoomByRegion(event.region);
            _this.navigationBar.setZoom(_this.zoom);
        });
        this.on('zoom:change', function (event) {
            _this.navigationBar.setZoom(event.zoom);
            _this.region.load(_this._calculateRegionByZoom(event.zoom));
            if (event.sender != navigationBar) {
                _this.navigationBar.setRegion(_this.region);
            }
            _this.setRegion(_this.region);
        });
        this.on('region:move', function (event) {
            if (event.sender != navigationBar) {
                _this.navigationBar.moveRegion(event.region);
            }
        });
        this.on('width:change', function (event) {
            _this.navigationBar.setWidth(event.width);
        });

        navigationBar.draw();

        return navigationBar;
    },

    _drawKaryotypePanel: function (targetId) {
        var _this = this;
        karyotypePanel = new KaryotypePanel({
            targetId: targetId,
            width: this.width - this.sidePanelWidth,
            height: 125,
            species: this.species,
            title: 'Karyotype',
            collapsed: this.karyotypePanelConfig.collapsed,
            collapsible: this.karyotypePanelConfig.collapsible,
            region: this.region,
            autoRender: true,
            handlers: {
                'region:change': function (event) {
                    event.region = _this._checkRegion(event.region);
                    _this.setMinRegion(event.region, _this.getSVGCanvasWidth());
                    _this.trigger('region:change', event);
                }
            }
        });

        this.on('region:change region:move', function (event) {
            if (event.sender != karyotypePanel) {
                karyotypePanel.setRegion(event.region);
            }
        });

        this.on('width:change', function (event) {
            karyotypePanel.setWidth(event.width - _this.sidePanelWidth);
        });

        this.on('species:change', function (event) {
            karyotypePanel.setSpecies(event.species);
        });

        karyotypePanel.draw();

        return karyotypePanel;
    },

    _drawChromosomePanel: function (targetId) {
        var _this = this;


        var chromosomePanel = new ChromosomePanel({
            targetId: targetId,
            autoRender: true,
            width: this.width - this.sidePanelWidth,
            height: 65,
            species: this.species,
            title: 'Chromosome',
            collapsed: this.chromosomePanelConfig.collapsed,
            collapsible: this.chromosomePanelConfig.collapsible,
            region: this.region,
            handlers: {
                'region:change': function (event) {
                    event.region = _this._checkRegion(event.region);
                    _this.trigger('region:change', event);
                }
            }
        });

        this.on('region:change region:move', function (event) {
            if (event.sender != chromosomePanel) {
                chromosomePanel.setRegion(event.region);
            }
        });

        this.on('width:change', function (event) {
            chromosomePanel.setWidth(event.width - _this.sidePanelWidth);
        });

        this.on('species:change', function (event) {
            chromosomePanel.setSpecies(event.species);
        });

        chromosomePanel.draw();

        return chromosomePanel;
    },

    _createRegionOverviewPanel: function (targetId) {
        var _this = this;
        var trackListPanel = new TrackListPanel({
            targetId: targetId,
            autoRender: true,
            width: this.width - this.sidePanelWidth,
            zoomMultiplier: 8,
            title: 'Region overview',
            showRegionOverviewBox: true,
            collapsible: this.RegionPanelConfig.collapsible,
            region: this.region,
            handlers: {
                'region:change': function (event) {
                    event.sender = {};
                    event.region = _this._checkRegion(event.region);
                    _this.setMinRegion(event.region, _this.getSVGCanvasWidth())
                    _this.trigger('region:change', event);
                },
                'region:move': function (event) {
                    _this.trigger('region:move', event);
                },
                'tracks:ready': function () {
                    _this.checkTrackListReady();
                }
            }
        });

        this.on('region:change', function (event) {
            if (event.sender != trackListPanel) {
                trackListPanel.setRegion(event.region);
            }
        });

        this.on('region:move', function (event) {
            if (event.sender != trackListPanel) {
                trackListPanel.moveRegion(event);
            }
        });

        this.on('width:change', function (event) {
            trackListPanel.setWidth(event.width - _this.sidePanelWidth);
        });

        this.on('species:change', function (event) {
            trackListPanel.setSpecies(event.species);
        });

        return  trackListPanel;
    },

    _createTrackListPanel: function (targetId) {
        var _this = this;
        var trackListPanel = new TrackListPanel({
            targetId: targetId,
            autoRender: true,
            width: this.width - this.sidePanelWidth,
            title: this.trackListTitle,
            region: this.region,
            handlers: {
                'region:change': function (event) {
                    event.sender = {};
                    event.region = _this._checkRegion(event.region);
                    _this.setMinRegion(event.region, _this.getSVGCanvasWidth());
                    _this.trigger('region:change', event);
                },
                'region:move': function (event) {
                    _this.trigger('region:move', event);
                },
                'tracks:ready': function () {
                    _this.checkTrackListReady();
                }
            }
        });

        this.on('feature:highlight', function (event) {
            trackListPanel.highlight(event);
        });

        this.on('region:change', function (event) {
            if (event.sender != trackListPanel) {
                trackListPanel.setRegion(event.region);
            }
        });

        this.on('region:move', function (event) {
            if (event.sender != trackListPanel) {
                trackListPanel.moveRegion(event);
            }
        });
        this.on('width:change', function (event) {
            trackListPanel.setWidth(event.width - _this.sidePanelWidth);
        });

        this.on('species:change', function (event) {
            trackListPanel.setSpecies(event.species);
        });

        return  trackListPanel;
    },

    _createStatusBar: function (targetId) {
        var _this = this;
        var statusBar = new StatusBar({
            targetId: targetId,
            autoRender: true,
            region: this.region,
            width: this.width,
            version: this.version
        });

        this.trackListPanel.on('mousePosition:change', function (event) {
            statusBar.setMousePosition(event);
        });
        this.on('region:change', function (event) {
            statusBar.setRegion(event);
        });

        return  statusBar;
    },

    checkTrackListReady: function () {
        var _this = this;
        var checkAllTrackListStatus = function (status) {
            if (_this.regionOverviewPanel && _this.regionOverviewPanel.status != status) {
                return false;
            }
            if (_this.trackListPanel.status != status) {
                return false;
            }
            return true;
        };
        if (checkAllTrackListStatus('ready')) {
//            console.log('-------------all tracklist ready')
            _this.trigger('tracks:ready', {sender: _this});
        }
//        var checkStatus = function () {
//            if (checkAllTrackStatus('ready')) {
//                _this.trigger('tracks:ready', {sender: _this});
//            } else {
//                setTimeout(checkStatus, 100);
//            }
//        };
//        setTimeout(checkStatus, 10);
    },

    getRightSidePanelId: function () {
        return $(this.rightSidebarDiv).attr('id');
    },
    getLeftSidePanelId: function () {
        return $(this.leftSidebarDiv).attr('id');
    },
    getNavigationPanelId: function () {
        return $(this.navigationbarDiv).attr('id');
    },
    getStatusPanelId: function () {
        return $(this.statusbarDiv).attr('id');
    },
    setNavigationBar: function (navigationBar) {
        this.navigationBar = navigationBar;
        var config = {
            availableSpecies: this.availableSpecies,
            species: this.species,
            region: this.region,
            width: this.width,
            svgCanvasWidthOffset: this.trackPanelScrollWidth + this.sidePanelWidth
        };
        _.extend(this.navigationBar, config);
        navigationBar.render(this.getNavigationPanelId());
    },
    _setWidth: function (width) {
        this.width = width;
        this.trigger('width:change', {width: this.width, sender: this});
    },
    setWidth: function (width) {
        $(this.div).width(width);
        this._setWidth(width);
    },
    getSVGCanvasWidth: function () {
        return this.width - this.trackPanelScrollWidth - this.sidePanelWidth;
    },
    _setRegion: function (region) {
        //update internal parameters
        this.region.load(region);
    },
    setRegion: function (region) {
        var region = this._checkRegion(region);
        this.region.load(region);
        this.setMinRegion(this.region, this.getSVGCanvasWidth());
        this.trigger('region:change', {region: this.region, sender: this});
    },
    _checkRegion: function (newRegion) {
        var newChr = this.chromosomes[newRegion.chromosome];
        if (newRegion.chromosome !== this.region.chromosome) {
            newRegion.start = Math.round(newChr.size / 2);
            newRegion.end = Math.round(newChr.size / 2);
        }
        return newRegion;
    },
    setMinRegion: function (region, width) {
        var minLength = Math.floor(width / 10);
        if (region.length() < minLength) {
            var centerPosition = region.center();
            var aux = Math.ceil((minLength / 2) - 1);
            region.start = Math.floor(centerPosition - aux);
            region.end = Math.floor(centerPosition + aux);
        }
    },
    setZoom: function (zoom) {
        this.zoom = zoom;
        this.zoom = Math.min(100, this.zoom);
        this.zoom = Math.max(0, this.zoom);
        this.trigger('zoom:change', {zoom: this.zoom, sender: this});
    },
    increaseZoom: function (zoomToIncrease) {
        this.zoom += zoomToIncrease;
        this.setZoom(this.zoom);
    },
    _calculateRegionByZoom: function (zoom) {
        // mrl = minimum region length
        // zlm = zoom level multiplier

        // mrl * zlm ^ 100 = chr.size
        // zlm = (chr.size/mrl)^(1/100)
        // zlm = (chr.size/mrl)^0.01

        var minNtPixels = 10; // 10 is the minimum pixels per nt
        var chr = this.chromosomes[this.region.chromosome];
        var minRegionLength = this.getSVGCanvasWidth() / minNtPixels;
        var zoomLevelMultiplier = Math.pow(chr.size / minRegionLength, 0.01); // 0.01 = 1/100  100 zoom levels

//      regionLength = mrl * (Math.pow(zlm,ZOOM))
        var regionLength = minRegionLength * (Math.pow(zoomLevelMultiplier, 100 - zoom)); // 100 - zoom to change direction

        var centerPosition = this.region.center();
        var aux = Math.ceil((regionLength / 2) - 1);
        var start = Math.floor(centerPosition - aux);
        var end = Math.floor(centerPosition + aux);

        return {start: start, end: end};
    },
    _calculateZoomByRegion: function (region) {
        var minNtPixels = 10; // 10 is the minimum pixels per nt
        var chr = this.chromosomes[this.region.chromosome];
        var minRegionLength = this.getSVGCanvasWidth() / minNtPixels;
        var zoomLevelMultiplier = Math.pow(chr.size / minRegionLength, 0.01); // 0.01 = 1/100  100 zoom levels

        var regionLength = region.length();

//      zoom = Math.log(REGIONLENGTH/mrl) / Math.log(zlm);
        var zoom = Math.log(regionLength / minRegionLength) / Math.log(zoomLevelMultiplier);
        return 100 - zoom;
    },
    move: function (disp) {
//        var pixelBase = (this.width-this.svgCanvasWidthOffset) / this.region.length();
//        var disp = Math.round((disp*10) / pixelBase);
        this.region.start += disp;
        this.region.end += disp;
        this.trigger('region:move', {region: this.region, disp: -disp, sender: this});
    },
    mark: function (args) {
        var attrName = args.attrName || 'feature_id';
        var cssClass = args.class || 'feature-emph';
        if ('attrValues' in args) {
            args.attrValues = ($.isArray(args.attrValues)) ? args.attrValues : [args.attrValues];
            for (var key in args.attrValues) {
                $('rect[' + attrName + '~=' + args.attrValues[key] + ']').attr('class', cssClass);
            }

        }
    },
    unmark: function (args) {
        var attrName = args.attrName || 'feature_id';
        if ('attrValues' in args) {
            args.attrValues = ($.isArray(args.attrValues)) ? args.attrValues : [args.attrValues];
            for (var key in args.attrValues) {
                $('rect[' + attrName + '~=' + args.attrValues[key] + ']').attr('class', '');
            }

        }
    },

    highlight: function (args) {
        this.trigger('feature:highlight', args);
    },

    enableAutoHeight: function () {
        this.trackListPanel.enableAutoHeight();
        this.regionOverviewPanel.enableAutoHeight();
    },
    updateHeight: function () {
        this.trackListPanel.updateHeight();
        this.regionOverviewPanel.updateHeight();
    },


    setSpeciesVisible: function (bool) {
        this.navigationBar.setSpeciesVisible(bool);
    },

    setChromosomesVisible: function (bool) {
        this.navigationBar.setChromosomeMenuVisible(bool);
    },

    setKaryotypePanelVisible: function (bool) {
        this.karyotypePanel.setVisible(bool);
        this.navigationBar.setVisible({'karyotype': bool});
    },

    setChromosomePanelVisible: function (bool) {
        this.chromosomePanel.setVisible(bool);
        this.navigationBar.setVisible({'chromosome': bool});
    },

    setRegionOverviewPanelVisible: function (bool) {
        this.regionOverviewPanel.setVisible(bool);
        this.navigationBar.setVisible({'region': bool});
    },
    setRegionTextBoxVisible: function (bool) {
        this.navigationBar.setRegionTextBoxVisible(bool);
    },
    setSearchVisible: function (bool) {
        this.navigationBar.setSearchVisible(bool);
    },
    setFullScreenVisible: function (bool) {
        this.navigationBar.setFullScreenButtonVisible(bool);
    },

    /*Track management*/
    addOverviewTrack: function (trackData, args) {
        this.regionOverviewPanel.addTrack(trackData, args);
    },

    addTrack: function (trackData, args) {
        this.trackListPanel.addTrack(trackData, args);
    },

    getTrackSvgById: function (trackId) {
        return this.trackListPanel.getTrackSvgById(trackId);
    },

    removeTrack: function (trackId) {
        return this.trackListPanel.removeTrack(trackId);
    },

    restoreTrack: function (trackSvg, index) {
        return this.trackListPanel.restoreTrack(trackSvg, index);
    },

    setTrackIndex: function (trackId, newIndex) {
        return this.trackListPanel.setTrackIndex(trackId, newIndex);
    },

    scrollToTrack: function (trackId) {
        return this.trackListPanel.scrollToTrack(trackId);
    },

    showTrack: function (trackId) {
        this.trackListPanel._showTrack(trackId);
    },

    hideTrack: function (trackId) {
        this.trackListPanel._hideTrack(trackId);
    },

    checkRenderedTrack: function (trackId) {
        if (this.trackListPanel.swapHash[trackId]) {
            return true;
        }
        return false;
    }
};


