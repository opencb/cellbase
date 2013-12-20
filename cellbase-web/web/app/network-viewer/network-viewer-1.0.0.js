/*! Genome Viewer - v1.0.3 - 2013-12-18 19:31:07
* http://https://github.com/opencb/jsorolla/
* Copyright (c) 2013  Licensed GPLv2 */
/*! Genome Viewer - v1.0.3 - 2013-12-18 19:31:07
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
//    dir: "data:image/png;base64,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",
    dir: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3QkQDBgWQKhE0wAAADRJREFUOMtjYBhowMjAwPCfCDU4AQuUNsQhf54aLmAgxgW9ZOovZqI0EEcNGBYGUJwSKQYAJoEFGqo9ooAAAAAASUVORK5CYII=",
    r: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAAZiS0dEAP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB90CDRIvNbHTpbwAAADjSURBVDjLpZFBbsIwEEUfVRZYahcVK3qKXoauMFK5C91nkyUB+xC5BqeAA7SKq1B5ugl2EiC04UkjayzN17NnROTRWvvJFbTWL8CBHqbGWOlSlqVkWSbGWAGm3aGHZiMiAByPP6FOd1rP2W7NvhvSCvDe10E+VJPFQpPnm1ZIcsmgPgJVVZGmaejX63y/XL4/AV/JJYPTCeDcN7PZWyuwKAqA8wARqSsGKDVGqXGjV8H07AnRQPq21TK8+YSBAQMN4hb6Df7wB/5eA+4zmEyehxk451itPrhFksSxUeP+lf+z+wXwdayJk/mqtgAAAABJRU5ErkJggg==",
    box: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAAZiS0dEAP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9wMHAwRAVvTmTAAAAK/SURBVDjLpZM9bFxFGEXPNzPvZ+39sb2xHceREjDQBwlDCqqIiiotokAghYKEjvSkQkKJkEiB0lOkoAHaBAokFCQKUATIIOLIMbHWrHfX+7zvvZk3MzQODUgU3PJK5+g2F/5n5N/Kb66/1NNK3hAxr4HcFqVuvfju18V/Cu58sPmMVnJZ4K32Qr+t8za+KnCz4kCUuiGibm5euTv5h+CL958/nxj1XivVF+e6C9TVhPmFdbROgEhwNU1d4m09UaJuInLjhct3DgDUh5ee7j14PLxulLvYP/0seadPkub88Wib0eB3bDkmxgbRoFPpxeCuKvjsyQIzOyqImT7/y8Mh++NveW7jLFmrx6m1NlWxz6PHA7otQ7tloAmYJE9isOeeCJRtIrULLLUTjsqG7+//xs72z7jZgCTNONlVJKEiuobW0jqSaoiet19dFQATJcc2FSFEciNoLYwOHcPDASvdjM5cQntxlbR9gqacoFSK84VsnOrkH11Zdmp0FFXjobSeCFgXSDS0Eo11ge7yGXSaU092UUlCaEpC8FK4tDcu4rzZ2a/S+bWI94HSAgFigDQD24Cvp4gIOp0juBJvC2L07B1Uc/Mtg9k7sHMbywZrA3lLECV4AtaCpAp79CcmzXHlhOBrAJrGyNbOVBY7qTO1C9r5EKyPSttAiJEs01SuQStFkrdp6gKd5AzHjixVxCDxp+1paZRUxoc4Kp36bndYbS53U5WlCq0CMYIPMY7GI0mNpiqmGK0oK4jIveGkPgRqfTBt3A8Pqtvrq52HtglnGh9XIaKUkCQ6nj6RyWBsmdXCtFI/bu2Fq5c+3roGzIAgWokCDNACOhfOLb781Ip+vd+RC2dXWibROkxKvvp1z376yZe7d4HpMdz8/YVjiQYyoA30Ti6la2++0n/n83vTW/e3ix1gcgzXgPchBoC/AFu/UBF5InryAAAAAElFTkSuQmCC",
    bluebox: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAAZiS0dEAP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9wMHAwTE5pcivoAAALsSURBVDjLXZPPaxxlGMc/77wz+3N2k822tWsTIrQFe/BWEKxKCV5UohdFhJ60p6rgUfpP6C0K4kHx0ENEkV7aElHwUikhFVosRGoTm83+3pnZnXfed2beHtItIQ98+Z6+H57nga8AsNYKDkYcEofcHvKZEEJYcSTszPzqL3fmf3+w/+a51tytby9d6D0N5UecGeBZ8MPv/jh9fy/6dKzMpVPHmvWdbl/XCvKn5Wbl6+ufrNwGssMgYa2VgFj58sZr7VB/LqX3zlKrydJzTTzXxdqcx90hO+0Bk2l8Z74i1z6+cOba5VfOqGeAb3579M/NR53T40xwrDGHFALPEUjn4LoMi0ktwWTKXqCIqAVrbyycvHj2hHYBR+bO8Q/Ov0imEzZ2xrRDRalQwC9LLBalUgaJQy+tU6gvIBJbv3j2RA4IFxDdICFa9ulMCrz/UgOs5kEwpeh57I4Nt/dzsmLOYlEThgFjUePp33IHoD9SJAbuTVyudRweixJvnVtg3/i00wpLPiwQ0hkO6YYKawWj0UjONqAfKHwDkxTqqeW/RHA3hO2+Zqk05e5wTD9KmOqMKDEUqoLNzU0PyF2AQaBoaIhiw0h6TIwgUDCODb5NiWJNlKREyhAozXwOW1tbFSmlcAHbD2KaytCdGgyWglfEs4LeNKeaa4axYRgpwlgTTTXVDDqdTslaewAYh4kNlKUbZsTGonOwCYwm1vq5Ft1AMYgU08SQR5o0gziOcRxHuoCNtdl6uPHX6/Vmi3Yyh9I5IoEgMdkgT9x+qJhEGrdQo77cJMuy+4DJskwLa60DOCtf3HhZpfZKtVx+L3x+sfCv8CFxTINd72HfodQ4aQp5fP24/v/Hd4Nf/5RSJmma6lkXZn1wPvvq5qndsbhS9esf/Zy/UEtzxnURfn8+/fuHV7m353mecV1XSym1lDI72kaxvr5e3N7eruyP0tpG/e3LK/rW2mLNUb7vm3K5nFarVdNqtbJer2dXV1fzJ6cDpboAZRAGAAAAAElFTkSuQmCC",
    refresh: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAAZiS0dEAP8A/wD/oL2nkwAAAAlwSFlzAAAK8AAACvABQqw0mAAAAAd0SU1FB90CFA8bKScXIjIAAAJ1SURBVDjLlVJdSJNRGH7es+/7NlduWFLTKAskwjJqZYiBglJhLKguqou6EFkFIkLQH9IPBFI3BsouzMJupGYRajcqmeBFhWBU5oUgpZikrSnzZ9v3nW/ndKEbziXUAwfOOe/7Puc9z/MCq1DwMmB1NX/rzfCNnsc/gK08lPgnnT8Cs33BULg0HI4YKdkHX9DqKwKArXXv1bTMTFcoyruC89E8MxaDw659t6rKhIUwRBLdP2t2v/5bBwQA+5pH8ibnIj3BucgWIQRASw8RERTGYFUtsGmWYUXKmqmr7t4UAnal54GQ8lq8MBlyOU0CEnA67MiwqfvHbhZ+Smgg6o9eV2L8Nhk6wI2lZeggrpvE+TTjxgxxQ4IbmJsJYSa00JQiotnguacJ8zIZOmDosAnzTpowt8tGj0s0ejZqprnDKmPHSNebjHDkUPatt4cTTbZ+LsmO79XK52dZxTNp9/ovAEDnaM62lo8HHrd9SVfiOelVryrSq9vrEx0s8sW2tuEzDgDgT875bcIsjy6owwAwHhjnYT5bGTL29PiHyuwAMO873aL/Ct5PiPjwXe5vq7KJW2hdJxENMFInGCkhIblLj80WRoyxGxZmh1XJGlSIlV8s6A8kuVDXn+MF6JHC7GBkBSNlOSRgiihMsQhAgJGGNNU1atc2HPG6O8YSBABwt2/nGyFlGSCSB4UIBMuyoQKMFNiUjIApRH5t8YfpFOOrO/JrhZBVUiJLxq2ipIkY8Z36uivpC6txqb3YbhqhIingFlLmxmLSKyXAGAaYqh13aFjfcHJwfE2ClSitK9psc85PMVC3M999orX4Kcf/wuPb27VW7A+O2QVVA1M1CQAAAABJRU5ErkJggg==",
    newitem: "data:image/png;base64,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"
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
function ToolBar(args) {
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);
    this.id = Utils.genId('tool-bar');

    //set default args
    this.targetId;
    this.autoRender = false;
    this.zoom = 100;

    //set instantiation args, must be last
    _.extend(this, args);

    this.on(this.handlers);

    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
};

ToolBar.prototype = {
    render: function (targetId) {
        var _this = this;
        if (targetId)this.targetId = targetId;
        if ($('#' + this.targetId).length < 1) {
            console.log('targetId not found in DOM');
            return;
        }

        var navgationHtml = '' +
            '<div class="btn-toolbar" role="toolbar">' +
            '   <div class="btn-group btn-group-xs">' +
            '       <button id="collapseButton" class="btn btn-default" type="button"><span class="ocb-icon icon-collapse"></span></button>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '       <button id="layoutButton" class="btn btn-default dropdown-toggle" data-toggle="dropdown"  type="button" ><span class="ocb-icon icon-layout"></span><span class="caret"></button>' +
            '       <ul id="layoutMenu" class="dropdown-menu" role="menu">' +
            '       </ul>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '       <button id="labelSizeButton" class="btn btn-default dropdown-toggle" data-toggle="dropdown"  type="button" ><span class="ocb-icon icon-label-size"></span><span class="caret"></button>' +
            '       <ul id="labelSizeMenu" class="dropdown-menu" role="menu">' +
            '       </ul>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '       <button id="autoSelectButton" class="btn btn-default dropdown-toggle" data-toggle="dropdown"  type="button" ><span class="ocb-icon icon-auto-select"></span><span class="caret"></button>' +
            '       <ul id="autoSelectMenu" class="dropdown-menu" role="menu">' +
            '       </ul>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '       <button id="backgroundButton" class="btn btn-default" type="button"><span class="ocb-icon icon-background-option"></span></button>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs" style="margin:0px 0px 0px 15px;">' +
            '       <button id="zoomOutButton" class="btn btn-default btn-xs" type="button"><span class="ocb-icon ocb-icon-plus"></span></button>' +
            '       <div id="progressBarCont" class="progress pull-left" style="width:120px;height:10px;margin:5px 2px 0px 2px;background-color: #d5d5d5">' +
            '           <div id="progressBar" class="progress-bar" role="progressbar" aria-valuenow="45" aria-valuemin="0" aria-valuemax="100" style="width: 100%">' +
            '           </div>' +
            '       </div>' +
            '       <button id="zoomInButton" class="btn btn-default btn-xs" type="button"><span class="ocb-icon ocb-icon-minus"></span></button>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '       <button id="showOverviewButton" class="btn btn-default" type="button"><span class="ocb-icon icon-select"></span></button>' +
            '   </div>' +
            '   <div class="btn-group pull-right">' +
            '       <div class="pull-left" style="height:22px;line-height: 22px;font-size:14px;">Search:&nbsp;</div>' +
            '       <div class="input-group pull-left">' +
            '           <input id="searchField" list="searchDataList" type="text" class="form-control" placeholder="..." style="padding:0px 4px;height:22px;width:100px">' +
            '           <datalist id="searchDataList">' +
            '           </datalist>' +
            '       </div>' +
            '       <button id="quickSearchButton" class="btn btn-default btn-xs" type="button"><span class="glyphicon glyphicon-search"></span></button>' +
            '   </div>' +
            '</div>' +
            '';



        /**************/
        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div id="tool-bar" class="gv-navigation-bar unselectable">' + navgationHtml + '</div>')[0];
        $(this.targetDiv).append(this.div);
        /**************/

        this.collapseButton = $(this.div).find('#collapseButton');

        this.layoutButton = $(this.div).find('#layoutButton');
        this.layoutMenu = $(this.div).find('#layoutMenu');

        this.labelSizeButton = $(this.div).find('#labelSizeButton');
        this.labelSizeMenu = $(this.div).find('#labelSizeMenu');

        this.autoSelectButton = $(this.div).find('#autoSelectButton');
        this.autoSelectMenu = $(this.div).find('#autoSelectMenu');

        this.backgroundButton = $(this.div).find('#backgroundButton');

        this.progressBar = $(this.div).find('#progressBar')[0];
        this.progressBarCont = $(this.div).find('#progressBarCont')[0];
        this.zoomOutButton = $(this.div).find('#zoomOutButton')[0];
        this.zoomInButton = $(this.div).find('#zoomInButton')[0];

        this.showOverviewButton = $(this.div).find('#showOverviewButton');

        this.searchField = $(this.div).find('#searchField')[0];
        this.searchDataList = $(this.div).find('#searchDataList')[0];
        this.quickSearchButton = $(this.div).find('#quickSearchButton');

        $(this.collapseButton).click(function (e) {
            _this.trigger('collapseButton:click', {clickEvent: e, sender: {}})
        });

        this._setLayoutMenu();
        this._setLabelSizeMenu();
        this._setAutoSelectMenu();

        $(this.backgroundButton).click(function (e) {
            _this.trigger('backgroundButton:click', {clickEvent: e, sender: {}})
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

        $(this.showOverviewButton).click(function () {
            $(this).toggleClass('active');
            _this.trigger('showOverviewButton:change', {selected: $(this).hasClass('active'), sender: _this});
        });

        $(this.searchField).bind("keyup", function (event) {
            var query = $(this).val();
            if (event.which === 13) {
                _this.trigger('search', {query: query, sender: _this});
            }
        });

        $(this.quickSearchButton).click(function () {
            var query = $(_this.searchField).val();
            _this.trigger('search', {query: query, sender: _this});
        });
        this.rendered = true;
    },
    draw: function () {

    },
    _setLayoutMenu: function () {
        var _this = this;
        var options = ['Dot', 'Neato', 'Twopi', 'Circo', 'Fdp', 'Sfdp', 'Random', 'Circle', 'Square'];
        for (var i in options) {
            var menuEntry = $('<li role="presentation"><a tabindex="-1" role="menuitem">' + options[i] + '</a></li>')[0];
            $(this.layoutMenu).append(menuEntry);
            $(menuEntry).click(function () {
                _this.trigger('layout:change', {option: $(this).text(), sender: _this});
            });
        }
    },
    _setLabelSizeMenu: function () {
        var _this = this;
        var size = {
            "None": 0,
            "Small": 8,
            "Medium": 10,
            "Large": 12,
            "x-Large": 16
        };
        var options = ['None', 'Small', 'Medium', 'Large', 'x-Large'];
        for (var i in options) {
            var menuEntry = $('<li role="presentation"><a tabindex="-1" role="menuitem">' + options[i] + '</a></li>')[0];
            $(this.labelSizeMenu).append(menuEntry);
            $(menuEntry).click(function () {
                _this.trigger('labelSize:change', {option: size[$(this).text()], sender: _this});
            });
        }
    },
    _setAutoSelectMenu: function () {
        var _this = this;
        var options = ['All Nodes', 'All Edges', 'Everything', 'Adjacent', 'Neighbourhood', 'Connected'];
        for (var i in options) {
            var menuEntry = $('<li role="presentation"><a tabindex="-1" role="menuitem">' + options[i] + '</a></li>')[0];
            $(this.autoSelectMenu).append(menuEntry);
            $(menuEntry).click(function () {
                _this.trigger('select:change', {option: $(this).text(), sender: _this});
            });
        }
    },
    _handleZoomOutButton: function () {
        this._handleZoomSlider(Math.max(0, this.zoom - 1));
        $(this.progressBar).css("width", this.zoom + '%');
    },
    _handleZoomSlider: function (value) {
        this.zoom = value;
        this.trigger('zoom:change', {zoom: this.zoom, sender: this});
    },
    _handleZoomInButton: function () {
        this._handleZoomSlider(Math.min(100, this.zoom + 1));
        $(this.progressBar).css("width", this.zoom + '%');
    }
}

function EditionBar(args) {
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);
    this.id = Utils.genId('EditionBar');

    //set default args
    this.targetId;
    this.autoRender = false;

    //set instantiation args, must be last
    _.extend(this, args);

    this.on(this.handlers);

    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
};

EditionBar.prototype = {
    render: function (targetId) {
        var _this = this;
        if (targetId)this.targetId = targetId;
        if ($('#' + this.targetId).length < 1) {
            console.log('targetId not found in DOM');
            return;
        }

        var navgationHtml = '' +
            '<div class="btn-toolbar" role="toolbar">' +
            '   <div class="btn-group btn-group-xs">' +
            '       <button id="selectButton" class="btn btn-default" type="button"><span class="ocb-icon icon-mouse-select"></span></button>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '       <button id="addButton" class="btn btn-default" type="button"><span class="ocb-icon icon-add"></span></button>' +
            '       <button id="linkButton" class="btn btn-default" type="button"><span class="ocb-icon icon-link"></span></button>' +
            '       <button id="deleteButton" class="btn btn-default" type="button"><span class="ocb-icon icon-delete"></span></button>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '       <button id="nodeShapeButton" class="btn btn-default dropdown-toggle" data-toggle="dropdown"  type="button" ><span class="ocb-icon icon-node-shape"></span><span class="caret"></button>' +
            '        <ul id="nodeShapeMenu" class="dropdown-menu" role="menu"></ul>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '        <button id="nodeSizeButton" class="btn btn-default dropdown-toggle" data-toggle="dropdown"  type="button" ><span class="ocb-icon icon-node-size"></span><span class="caret"></button>' +
            '        <ul id="nodeSizeMenu" class="dropdown-menu" role="menu"></ul>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '       <button id="nodeStrokeSizeButton" class="btn btn-default dropdown-toggle" data-toggle="dropdown"  type="button" ><span class="ocb-icon icon-stroke-size"></span><span class="caret"></button>' +
            '       <ul id="nodeStrokeSizeMenu" class="dropdown-menu" role="menu"></ul>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '   <div class="input-group">' +
            '       <span class="input-group-addon" style="display:inline-block;width:40px;height:23px;padding:3px;"><span class="ocb-icon icon-fill-color"></span>&nbsp;&nbsp;#</span>' +
            '       <input id="nodeColorField" class="form-control" type="text" style="padding:0px 4px;height:23px;width:60px;display:inline-block;">' +
            '       <span class="input-group-addon" style="display:inline-block;width:25px;height:23px;padding:2px;">' +
            '           <select id="nodeColorSelect"></select>' +
            '       </span>' +
            '   </div>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '   <div class="input-group">' +
            '       <span class="input-group-addon" style="display:inline-block;width:40px;height:23px;padding:3px;"><span class="ocb-icon icon-stroke-color"></span>&nbsp;&nbsp;#</span>' +
            '       <input id="nodeStrokeColorField" class="form-control" type="text" style="padding:0px 4px;height:23px;width:60px;display:inline-block;">' +
            '       <span class="input-group-addon" style="display:inline-block;width:25px;height:23px;padding:2px;">' +
            '           <select id="nodeStrokeColorSelect"></select>' +
            '       </span>' +
            '   </div>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '       <button id="nodeOpacityButton" class="btn btn-default dropdown-toggle" data-toggle="dropdown"  type="button" ><span class="ocb-icon icon-node-opacity"></span><span class="caret"></button>' +
            '       <ul id="nodeOpacityMenu" class="dropdown-menu" role="menu"></ul>' +
            '   </div>' +
            '   <div class="btn-group" style="width:220px;margin-left: 5px">' +
            '   <div class="input-group">' +
            '       <input id="nodeNameField" type="text" class="form-control" placeholder="node name" style="padding:0px 4px;height:23px;width:100px">' +
            '       <input id="nodeLabelField" type="text" class="form-control" placeholder="node label" style="padding:0px 4px;height:23px;width:100px">' +
            '   </div>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '       <button id="edgeShapeButton" class="btn btn-default dropdown-toggle" data-toggle="dropdown"  type="button" ><span class="ocb-icon icon-edge-type"></span><span class="caret"></button>' +
            '       <ul id="edgeShapeMenu" class="dropdown-menu" role="menu"></ul>' +
            '   </div>' +
            '   <div class="btn-group btn-group-xs">' +
            '       <input id="edgeColorField" type="text">' +
            '   </div>' +
            '   <div class="btn-group" style="width:110px;margin-left: 5px">' +
            '   <div class="input-group">' +
            '       <input id="edgeLabelField" type="text" class="form-control" placeholder="edge label" style="padding:0px 4px;height:23px;width:100px">' +
            '   </div>' +
            '   </div>' +
            '</div>' +
            '';


        /**************/
        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div id="edition-bar" class="gv-navigation-bar unselectable">' + navgationHtml + '</div>')[0];
        $(this.targetDiv).append(this.div);
        /**************/

        this.selectButton = $(this.div).find('#selectButton');

        this.addButton = $(this.div).find('#addButton');
        this.linkButton = $(this.div).find('#linkButton');
        this.deleteButton = $(this.div).find('#deleteButton');

        /* node */
        this.nodeShapeButton = $(this.div).find('#nodeShapeButton');
        this.nodeSizeButton = $(this.div).find('#nodeSizeButton');
        this.nodeStrokeSizeButton = $(this.div).find('#nodeStrokeSizeButton');
        this.nodeOpacityButton = $(this.div).find('#nodeOpacityButton');

        this.nodeShapeMenu = $(this.div).find('#nodeShapeMenu');
        this.nodeSizeMenu = $(this.div).find('#nodeSizeMenu');
        this.nodeStrokeSizeMenu = $(this.div).find('#nodeStrokeSizeMenu');
        this.nodeOpacityMenu = $(this.div).find('#nodeOpacityMenu');

        this.nodeColorField = $(this.div).find('#nodeColorField');
        this.nodeColorSelect = $(this.div).find('#nodeColorSelect');
        this.nodeStrokeColorField = $(this.div).find('#nodeStrokeColorField');
        this.nodeStrokeColorSelect = $(this.div).find('#nodeStrokeColorSelect');

        this.nodeNameField = $(this.div).find('#nodeNameField');
        this.nodeLabelField = $(this.div).find('#nodeLabelField');


        /* edge */
        this.edgeShapeButton = $(this.div).find('#edgeShapeButton');
        this.edgeShapeMenu = $(this.div).find('#edgeShapeMenu');

        this.edgeColorField = $(this.div).find('#edgeColorField');

        this.edgeLabelField = $(this.div).find('#edgeLabelField');

        /*************/
        this._setColorSelect(this.nodeColorSelect);
        $(this.nodeColorSelect).simplecolorpicker({picker: true}).on('change', function () {
            $(_this.nodeColorField).val($(_this.nodeColorSelect).val().replace('#', '')).change();
        });

        this._setColorSelect(this.nodeStrokeColorSelect);
        $(this.nodeStrokeColorSelect).simplecolorpicker({picker: true}).on('change', function () {
            $(_this.nodeStrokeColorField).val($(_this.nodeStrokeColorSelect).val().replace('#', '')).change();
        });
//        /* Color picker */
//        var pickAColorConfig = {
//            showSpectrum: true,
//            showSavedColors: true,
//            saveColorsPerElement: false,
//            fadeMenuToggle: true,
//            showAdvanced: true,
//            showBasicColors: true,
//            showHexInput: false,
//            allowBlank: true
//        }
//
//
//        $(this.nodeColorField).pickAColor(pickAColorConfig);
//        $(this.nodeStrokeColorField).pickAColor(pickAColorConfig);
//        $(this.edgeColorField).pickAColor(pickAColorConfig);
//
//        $(this.div).find('.pick-a-color-markup').addClass('pull-left');
//        $(this.div).find('.color-dropdown').css({
//            padding: '1px 4px'
//        });

//        $(this.nodeColorField).next().find('button').prepend('<span class="ocb-icon icon-fill-color"></span>');
//        $(this.nodeStrokeColorField).next().find('button').prepend('<span class="ocb-icon icon-stroke-color"></span>');
//        $(this.edgeColorField).next().find('button').prepend('<span class="ocb-icon icon-fill-color"></span>');

//        var colorPattern = /^([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$/;
        var colorPattern = /^([A-Fa-f0-9]{6})$/;
        $(this.nodeColorField).on("change input", function () {
            var val = $(this).val();
            if (colorPattern.test(val)) {
                var color = '#' + $(this).val();
                _this._checkSelectColor(color, _this.nodeColorSelect);
                $(_this.nodeColorSelect).simplecolorpicker('selectColor', color);
                _this.trigger('nodeColorField:change', {value: color, sender: {}})
            }
        });
        $(this.nodeStrokeColorField).on("change input", function () {
            var val = $(this).val();
            if (colorPattern.test(val)) {
                var color = '#' + $(this).val();
                _this._checkSelectColor(color, _this.nodeColorSelect);
                _this.trigger('nodeStrokeColorField:change', {value: color, sender: {}})
            }
        });
        $(this.edgeColorField).on("change input", function () {
            var val = $(this).val();
            if (colorPattern.test(val)) {
                var color = '#' + $(this).val();
                _this._checkSelectColor(color, _this.nodeColorSelect);
                _this.trigger('edgeColorField:change', {value: color, sender: {}})
            }
        });
        /* */

        /* buttons */
        $(this.selectButton).click(function (e) {
            _this.trigger('selectButton:click', {clickEvent: e, sender: {}})
        });
        $(this.addButton).click(function (e) {
            _this.trigger('addButton:click', {clickEvent: e, sender: {}})
        });
        $(this.linkButton).click(function (e) {
            _this.trigger('linkButton:click', {clickEvent: e, sender: {}})
        });
        $(this.deleteButton).click(function (e) {
            _this.trigger('deleteButton:click', {clickEvent: e, sender: {}})
        });


        /* menus */
        var opacities = {"none": '1', "low": '0.8', "medium": '0.5', "high": '0.2', "invisible": '0'};
        var sizeOptions = ['1', '2', '3', '4', '5', '6', '7', '8', '10', '12', '14', '16', '22', '28', '36', '72'];

        this._setMenu('nodeShape', this.nodeShapeMenu, ['circle', 'square', 'ellipse', 'rectangle']);
        this._setMenu('nodeSize', this.nodeSizeMenu, sizeOptions);
        this._setMenu('nodeStrokeSize', this.nodeStrokeSizeMenu, sizeOptions);
        this._setMenu('opacity', this.nodeOpacityMenu, ["none", "low", "medium", "high", "invisible"], opacities);
        this._setMenu('edgeShape', this.edgeShapeMenu, ["directed", "odirected", "undirected", "inhibited", "dot", "odot"]);


        /* fields */
        $(this.nodeNameField).bind("keyup", function (event) {
            if (event.which === 13) {
                _this.trigger('nodeNameField:change', {value: $(this).val(), sender: _this});
            }
        });
        $(this.nodeLabelField).bind("keyup", function (event) {
            if (event.which === 13) {
                _this.trigger('nodeLabelField:change', {value: $(this).val(), sender: _this});
            }
        });
        $(this.edgeLabelField).bind("keyup", function (event) {
            if (event.which === 13) {
                _this.trigger('edgeLabelField:change', {value: $(this).val(), sender: _this});
            }
        });

        this.rendered = true;
    },

    _setMenu: function (eventName, menu, options, hashTable) {
        var _this = this;
        for (var i in options) {
            var menuEntry = $('<li role="presentation"><a tabindex="-1" role="menuitem">' + options[i] + '</a></li>')[0];
            $(menu).append(menuEntry);
            $(menuEntry).click(function () {
                var value = $(this).text();
                if (typeof hashTable !== 'undefined') {
                    value = hashTable[value];
                }
                _this.trigger(eventName + ':change', {value: value, sender: _this});
            });
        }
    },
    _setColorSelect: function (select) {
        var colors = ["cccccc", "888888",
            "ac725e", "d06b64", "f83a22", "fa573c", "ff7537", "ffad46", "42d692", "16a765", "7bd148", "b3dc6c", "fbe983", "fad165",
            "92e1c0","9fe1e7", "9fc6e7", "4986e7", "9a9cff", "b99aff", "c2c2c2", "cabdbf","cca6ac", "f691b2", "cd74e6", "a47ae2",
            ];

        for (var i in colors) {
            var menuEntry = $('<option value="#' + colors[i] + '">#' + colors[i] + '</option>')[0];
            $(select).append(menuEntry);
        }
    },
    _checkSelectColor: function (color, select) {
        var found = ($(select).find('option[value="' + color + '"]').length > 0 ) ? true : false;
        if (!found) {
            var menuEntry = $('<option value="' + color + '">' + color + '</option>')[0];
            $(select).append(menuEntry);
            $(this.nodeColorSelect).simplecolorpicker('destroy');
            $(this.nodeColorSelect).simplecolorpicker({picker: true});
        }
    },

    setNodeColor: function (color) {
        this._checkSelectColor(color, this.nodeColorSelect);
        $(this.nodeColorSelect).simplecolorpicker('selectColor', color);
        $(this.nodeColorField).val($(this.nodeColorSelect).val().replace('#', ''));
    },
    setNodeStrokeColor: function (color) {
        this._checkSelectColor(color, this.nodeStrokeColorSelect);
        $(this.nodeStrokeColorSelect).simplecolorpicker('selectColor', color);
        $(this.nodeStrokeColorField).val($(this.nodeStrokeColorSelect).val().replace('#', ''));
    }

}

function NetworkSvgLayout(args) {
    var _this = this;
    _.extend(this, Backbone.Events);
    this.id = Utils.genId('networkSvg');

    this.bgColor = "white";
    this.scale = 1;
    this.zoom = 0;
    this.canvasOffsetX = 0;
    this.canvasOffsetY = 0;

    this.width;
    this.height;
    this.bgColor;
    this.species;
    this.parentNetwork;
    this.scale;


    //set instantiation args, must be last
    _.extend(this, args);

    /** Action mode **/
    this.mode = "select"; // Valid values: select, add, delete, join


    /** *** *** **/
    this.createdVertexCount = 0;
    this.network = new Network();

    /* join vertex click flag */
    this.joinSourceVertex = null;

    this.selectedVertices = [];
    this.selectedVerticesHash = {};

    this.on(this.handlers);

    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
};


NetworkSvgLayout.prototype = {
    render: function (targetId) {
        var _this = this;
        if (targetId)this.targetId = targetId;
        if ($('#' + this.targetId).length < 1) {
            console.log('targetId not found in DOM');
            return;
        }


        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div id="' + this.id + '" style="position:relative;"></div>')[0];
        $(this.targetDiv).append(this.div);

        /** SVG init **/
        this.svg = SVG.init(this.div, {
            "id": "mainSVG",
            "width": this.width,
            "height": this.height
        });
        this.defs = SVG.addChild(this.svg, "defs", {});

        /* background */
        this.backgroundSvg = SVG.init(this.svg, {
            "id": "backgroundSVG",
            "width": this.width,
            "height": this.height,
            "x": 0,
            "y": 0
        });

        this.backgroundImage = SVG.addChildImage(this.backgroundSvg, {
            "id": "backgroundImage",
            "x": "0",
            "y": "0",
            "width": this.width,
            "height": this.height
        });

        /* canvas svg */
        this.canvasSVG = SVG.init(this.svg, {
            "id": "svgCanvas",
            "width": 100000,
            "height": 100000,
            "x": 0,
            "y": 0
        });

        this.scaleGroupSVG = SVG.addChild(this.canvasSVG, "g", {
            id: 'scaleGroupSVG',
            "transform": "scale(" + this.scale + ")"
        });

        this.temporalLinkSvg = SVG.addChild(this.canvasSVG, 'line', {
            'x1': 0,
            'y1': 0,
            'x2': 0,
            'y2': 0,
            'stroke': 'slategray',
            'opacity': '1',
            'stroke-width': 2,
            'cursor': 'pointer'
        }, 0);
//
        this.selectAnimate = SVG.create('animate', {
            attributeType: 'XML',
            attributeName: 'opacity',
            from: '1',
            to: '0.6',
            dur: '2s',
            repeatCount: 'indefinite'
        });

        this.selectRect = SVG.addChild(this.canvasSVG, "rect", {
            "x": 0,
            "y": 0,
            "width": 0,
            "height": 0,
            "stroke-width": "2",
            "stroke": "deepskyblue",
            "opacity": "0.5",
            "fill": "honeydew"
        });


        $(this.svg).bind('mousedown.networkViewer', function (event) {
            event.preventDefault();
            switch (event.which) {
                case 1: //left click
                    _this.leftMouseDown(event);
                    break;
            }
        });
        $(this.svg).bind('mouseup.networkViewer', function (event) {
            switch (event.which) {
                case 1: //left click
                    _this.leftMouseUp(event);
                    break;
            }
        });
        $(this.svg).bind('contextmenu.networkViewer', function (event) {
            event.preventDefault();
            switch (event.which) {
                case 3: //right click
                    _this.contextMenu(event);
                    break;
            }

        });


    },
    draw: function () {

    },
    /*  */
    setMode: function (mode) {
        this.mode = mode;
        switch (mode) {
            case "add":
//                $(this.div).addClass("cursor-test");
                this.svg.setAttribute("cursor", "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAAAXNSR0IArs4c6QAABAhJREFUWMPVlW1Mm1UUx//tU14GyHTigI7SQcvYIkEJ06mouERMli3zA35YyFA/mKwkW6RjrJolJE0UI0RiRiRRE8I2MjKzgsmUlL2Q+cI2RgTWkTBkrAFXWiqU0tJCe8956ieJM3Ey00I8H885957f/d97zgXW2wYGBurWs76Smc19fX3mdQMgIqSlpdX19vZ+tF4KIDs7G+np6Se6u7s/WRcFAECtVkOtVn/Q1dXVsOYAzAxmhkajQVZWVm1HR0fDmgLIsgwiQjgchlarhVarrW1tbf1szQGEEFheXkZOTg50Ot3RlpaWz9cM4M9rkGUZAKDT6ZCXl/d+U1NTc8wBVCoVVCoVJEmCJEno7OwMWCyWX2w2W1gIcbi+vj5mEEohBBwOB6xWa8jj8SA5ORlElERE+2pqahJMJpNCCHG8rq5uQywAFG1tbREichHRm0R0rqysbKvL5cKNGzdOmUymd9fiDdwkol0Gg+EmERlHRkZQVFQEIUSl2WzeGHMAIcRrBoNhCgCqq6u/HR8fd83MzKC4uFhJRMaYA1RVVS391SGEOD40NAS9Xg8iOlpbWxsfSwDV3x1EdM5ms306ODg4zczfNzY2hmMJoPgvixovHdOxzBXE/DYxZZDMEEwuYj4tmM6ePNA+ETOAxks15cRys2azLjM/4xlsiEtBWA7DvTiN63d74Z793UlER75+57xlNftJj16cT+3Ul24q0pTAtezExOIYnEv3IakkFGbtgp/mHnPNufcW7NOP3b7w62jUFGi4WJPLMv/0bO5L6rz0QvzgvIhgIAgRISQmJiCCCEJyCDtSC3Bt7DLG7BPTguiVrsOX7z20C1YLwDIffHzjU+qtadvxs+sKlsQygmIJxuITqHr6GLyheXgWXLjuvIoXtu2GIp7Vgungv7bhagGIuVK7OR93FkYw65uFL+iDNzC/EnfYpyBCAQT8c7jvs6MkvwSCqfKR2/CfTDBtSYxLwqTfjrAcwofPf/xA/IvydgCAsacCDu89FDy5E4JpSxQVIASEH/6wD0ISD82NsAwFIhBMiKYCDsf8pD4lIQXeJQ+MP76HgNuHr976ZuXkAKBUKKFOzcaU2w5ickRNAcF0Ztjej+ykHERECAombHoi+YEcmSNQygpoUnPQfesKBNOZaCrQLoelQ/13r6pfzH0dfZNWkEww9lQgwgAQQbxSQmnuHliHv8OCf3GamNujNogGLCPzhfu3TXq83r0LIXf8q7o3oFLGIRQOIl4ZB13adhRmPAfrrQvoH70dIJkPDZtHr0V9FB/4ck+5YGpmiTJLd7yM9NRMIAL8Nj+FnqFe+BaDTpL5yLB51BKzz2j/yd06wVQhVj4jgmB2EfNplunssPnOBP4v9gezhEi+VkBYbwAAAABJRU5ErkJggg==), auto");
                break;

            case "join":
                this.svg.setAttribute("cursor", "url(data:image/png;base64,), auto");
                break;

            case "delete":
                this.svg.setAttribute("cursor", "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAAAXNSR0IArs4c6QAAA85JREFUWMPVlV1IpFUYx//ved/R+Z7WprUmddZGiZgN1rwoiCiXLIhi79o+2LK6aIyl9SOdIBCEMFrLFgKjC8WuZAO1K/Gi3KgsWVkEeQ1CSFv3dWYKJ9cdnXfnec55u1g2+sR1m1F6Ls+B5//j//zPc4D9rrm5uZ791BdSyt6ZmZnefQNgZoTD4Z7p6em398sB1NTUoLKy8q3Jycl39sUBAIhEIohEIm9OTEyc3nMAKSWklKiurkZVVVXX6Ojo6T0FUEqBmVEoFBCNRhGNRruGh4ff33MAIoJt26itrUUsFusYHBw8s2cA18eglAIAxGIx1NfXnxoYGPiw5ACGYcAwDOi6Dl3XMT4+vjU2NnZhYWGhQEQn+/r6SgYhiAiWZWFqaupqNpuFz+cDM3uZ+cnOzs7yZDKpEVF3T0+PpxQA2sjIiMPMaWY+xsxnm5ubD6XTaczOzn6STCZb9iID55n5/kQicZ6Z203TRENDA4joRG9vb6jkAET0SCKRuAgAbW1tny0tLaUzmQwaGxsFM7eXHKC1tTX/xwMi6p6fn0ddXR2YuaOrq6uspAB/PWDmswsLC6mhoaELzPxBf39/YbdN7ZZXvDccwt02d95IRgH4tffeXfxH8RdfjjtMjyried+no1/t2oEdxXV9xRHClKc64n8Tf+GlOIQwRSBwRhG9tvnUsXuKBnBdHOXlgNsNCGFS68nfIfInWuLQNFP3+1AWDsMVDB5XBXose7RZFGUETme321FqCx6P0AIBqM1NqI0NKDt/WBFDE8IUwQBcwRBs6xKuptJSEXkqzn1ORcuAfL3d7UiZ0zweXQRDkJc3wL9m4RBDDwTgCgVhX7Jgp9PSYfJXnPvCLnoI6dVWt2KZE16vbhy4BZAKUApQErZlwU5nrol/OW3f1DPcqVwff2Q7RH5aX1cgCaysAMvLgJSwU2mlqHDD4jcFAACKKKZpEJAMEAHMADM0XQhVoNh/WkQ71fYzz8ehwdQ9XoAJiESAyB0AM8pDIeiGbqaP3BcvySLaOv5cXBOaaXh9cAWDyKfWYGcyUhFp7gMVwhe5HdupNLbX1lC4kjt85w/fLxbNga2nn41rmmYaHi9cAR/y1rW0qwL5HSLflmXJKz8uwxu+FZ5wGEaZy1w9dNeOThi7mPtB4TIARyFvpZD/OSMdYn/4269tAEjde8Sfs6ycUyjoLp8PmhBwpDwIYLFoI9h4/Ikm4XJN0+ZlpYh9t333zZ/SbtXf7XakzJUFArqdzR6tWf3pXNG/z/WHHm765YEH3f92vxqtdV+sqmnC/6V+A4wb/YzHvgVzAAAAAElFTkSuQmCC), auto");
                break;

            default:
                this.svg.setAttribute("cursor", "default");
                break;
        }
        console.log(this.mode)
    },
    leftMouseDown: function (event) {
        var _this = this;
        var targetEl = event.target;
        switch (this.mode) {
            case "add":
                /* event coordinates */
                var downX = (event.clientX - $(this.svg).offset().left);
                var downY = (event.clientY - $(this.svg).offset().top);
                if ($(targetEl).attr('network-type') !== 'vertex') {
                    this.createVertex(downX, downY);
                }
                break;
            case "select":
                var downX = (event.clientX - $(_this.svg).offset().left);
                var downY = (event.clientY - $(_this.svg).offset().top);
                /* vertex clicked */
                if ($(targetEl).attr('network-type') === 'vertex') {

                    var vertexSvg = $(targetEl).parent().parent();
                    var isSelected = $(vertexSvg).find('circle[network-type="select-vertex"]').length > 0 ? true : false;
                    if (!isSelected) {
                        _this.selectVertexByClick(vertexSvg);
                    }

                    var lastX = downX;
                    var lastY = downY;
                    $(_this.svg).bind('mousemove.networkViewer', function (moveEvent) {
                        moveEvent.preventDefault();
                        var moveX = (moveEvent.clientX - $(_this.svg).offset().left);
                        var moveY = (moveEvent.clientY - $(_this.svg).offset().top);
                        var dispX = moveX - lastX;
                        var dispY = moveY - lastY;

                        _this._moveSelectedVertices(dispX, dispY);

                        lastX = moveX;
                        lastY = moveY;
                    });


                    /* background clicked*/
                } else {
                    var lastX = 0, lastY = 0;
                    $(_this.svg).bind('mousemove.networkViewer', function (moveEvent) {
                        moveEvent.preventDefault();
                        var moveX = (moveEvent.clientX - $(_this.svg).offset().left);
                        var moveY = (moveEvent.clientY - $(_this.svg).offset().top);
                        var dispX = moveX - downX;
                        var dispY = moveY - downY;
                        var x = (dispX >= 0) ? downX : downX - Math.abs(dispX);
                        var y = (dispY >= 0) ? downY : downY - Math.abs(dispY);
                        // Update selectRect size and position
                        _this.selectRect.setAttribute('x', x);
                        _this.selectRect.setAttribute('y', y);
                        _this.selectRect.setAttribute('width', Math.abs(dispX));
                        _this.selectRect.setAttribute('height', Math.abs(dispY));
                    });

                }
                break;
            case "join":
                /* vertex clicked */
                if ($(targetEl).attr('network-type') === 'vertex') {
                    var vertexId = $(targetEl).parent().parent().attr('id');
                    var vertex = _this.network.getVertexById(vertexId);
                    var vertexConfig = _this.network.getVertexConfig(vertex);
                    // first time vertex click
                    if (_this.joinSourceVertex == null) {
                        _this.joinSourceVertex = vertex;
                        $(_this.svg).bind('mousemove.networkViewer', function (moveEvent) {
                            moveEvent.preventDefault();
                            var moveX = (moveEvent.clientX - $(_this.svg).offset().left);
                            var moveY = (moveEvent.clientY - $(_this.svg).offset().top);
                            _this.temporalLinkSvg.setAttribute('x1', vertexConfig.coords.x);
                            _this.temporalLinkSvg.setAttribute('y1', vertexConfig.coords.y);
                            _this.temporalLinkSvg.setAttribute('x2', moveX);
                            _this.temporalLinkSvg.setAttribute('y2', moveY);
                        });
                        // second vertex click
                    } else if (_this.joinSourceVertex !== vertex) {
                        _this.createEdge(_this.joinSourceVertex, vertex);
                        _this.joinSourceVertex = null;
                    }
                }
                break;
            case "delete":
                if ($(targetEl).attr('network-type') === 'vertex') {
                    var vertexId = $(targetEl).parent().parent().attr('id');
                    var vertex = _this.network.getVertexById(vertexId);
                    this.removeVertex(vertex);
                }
                break;
        }
    },
    leftMouseUp: function (event) {
        var _this = this;
        var targetEl = event.target;
        switch (this.mode) {
            case "add":
                $(_this.svg).off('mousemove.networkViewer');
                break;
            case "select":
                if ($(targetEl).attr('network-type') !== 'vertex') {
                    var x = parseFloat(_this.selectRect.getAttribute('x'));
                    var y = parseFloat(_this.selectRect.getAttribute('y'));
                    var width = parseFloat(_this.selectRect.getAttribute('width'));
                    var height = parseFloat(_this.selectRect.getAttribute('height'));

                    _this.selectVerticesByCoords(x, y, width, height);

                    _this.selectRect.setAttribute('x', 0);
                    _this.selectRect.setAttribute('y', 0);
                    _this.selectRect.setAttribute('width', 0);
                    _this.selectRect.setAttribute('height', 0);
                } else {
                    var vertexId = $(targetEl).parent().parent().attr('id');
                    var vertex = _this.network.getVertexById(vertexId);
                    this.network.getVertexAttributes(vertex, function (attributes) {
                        _this.trigger('vertex:leftClick', {
                            vertex: vertex,
                            vertexConfig: _this.network.getVertexConfig(vertex),
                            attributes: attributes
                        });
                    });
                }
                $(_this.svg).off('mousemove.networkViewer');
                break;
            case "join":
                if ($(targetEl).attr('network-type') !== 'vertex') {
                    _this.joinSourceVertex = null;
                }
                if (_this.joinSourceVertex == null) {
                    $(_this.svg).off('mousemove.networkViewer');
                    _this.temporalLinkSvg.setAttribute('x1', 0);
                    _this.temporalLinkSvg.setAttribute('y1', 0);
                    _this.temporalLinkSvg.setAttribute('x2', 0);
                    _this.temporalLinkSvg.setAttribute('y2', 0);
                }
                break;
            case "delete":
                if ($(targetEl).attr('network-type') !== 'vertex') {

                }
                break;
        }
    },
    contextMenu: function (event) {
        var _this = this;
        var targetEl = event.target;
        switch (this.mode) {
            case "add":
                break;
            case "select":
                break;
            case "join":
                break;
            case "delete":
                break;
        }


        var downX = (event.clientX - $(this.svg).offset().left);
        var downY = (event.clientY - $(this.svg).offset().top);
        if ($(targetEl).attr('network-type') === 'vertex') {
            var vertexId = $(targetEl).parent().parent().attr('id');
            var vertex = _this.network.getVertexById(vertexId);
            _this.network.getVertexAttributes(vertex, function (attributes) {
                _this.trigger('vertex:rightClick', {
                    vertex: vertex,
                    attributes: attributes,
                    x: downX,
                    y: downY
                });
            });

        }
    },
    selectVertexByClick: function (vertexSvg) {
        this._deselectAllVertices();
        var vertexId = $(vertexSvg).attr('id');
        var vertex = this.network.getVertexById(vertexId);
        this._selectVertexSvg(vertex, vertexSvg);
        this.selectedVertices = [vertex];
        this.selectedVerticesHash[vertex.id] = vertex;
    },
    selectVerticesByCoords: function (x, y, width, height) {
        this._deselectAllVertices();
        var vertices = this.network.graph.vertices;
        this.selectedVertices = [];
        this.selectedVerticesHash = {};
        for (var i = 0, l = vertices.length; i < l; i++) {
            var vertex = vertices[i];
            if (typeof vertex !== 'undefined') {
                var vertexSvg = $(this.svg).find('svg[id="' + vertex.id + '"]')[0];
                var vertexConfig = this.network.getVertexConfig(vertex);
                if (vertexConfig.coords.x >= x && vertexConfig.coords.x <= x + width && vertexConfig.coords.y >= y && vertexConfig.coords.y <= y + height) {
                    this.selectedVertices.push(vertex);
                    this.selectedVerticesHash[vertex.id] = vertex;
                    this._selectVertexSvg(vertex, vertexSvg);
                }
            }
        }
    },
    _selectVertexSvg: function (vertex, vertexSvg) {
        var vertexConfig = this.network.getVertexConfig(vertex);
        var vertexGroup = $(vertexSvg).children().first()[0];

        var size = vertexConfig.renderer.size + vertexConfig.renderer.strokeSize;
        var size = size + (size * 0.3);
        var midOffset = size / 2;

        var r = SVG.addChild(vertexGroup, "circle", {
            r: midOffset,
            cx: midOffset,
            cy: midOffset,
            opacity: '0.5',
            fill: '#cccccc',
            'network-type': 'select-vertex'
        }, 0);
    },
    _deselectAllVertices: function () {
        this.selectedVertices = [];
        this.selectedVerticesHash = {};
        $(this.svg).find('circle[network-type="select-vertex"]').remove();
    },
    _moveSelectedVertices: function (dispX, dispY) {
        for (var i = 0, li = this.selectedVertices.length; i < li; i++) {
            var vertex = this.selectedVertices[i];
            var vertexSvg = $(this.svg).find('svg[id="' + vertex.id + '"]')[0];
            var vertexConfig = this.network.getVertexConfig(vertex);

            var currentX = parseFloat(vertexSvg.getAttribute('x'));
            var currentY = parseFloat(vertexSvg.getAttribute('y'));
            vertexSvg.setAttribute('x', currentX + dispX);
            vertexSvg.setAttribute('y', currentY + dispY);

            // Calculate center x and y and update vertexLayout
            var size = vertexConfig.renderer.size + vertexConfig.renderer.strokeSize;
            var size = size + (size * 0.3);
            var midOffset = size / 2;

            var x = currentX + dispX + midOffset;
            var y = currentY + dispY + midOffset;
            vertexConfig.setCoords(x, y);

            // Update edge position
            for (var j = 0; j < vertex.edges.length; j++) {
                var edge = vertex.edges[j];
                var sourceConfig = this.network.getVertexConfig(edge.source);
                var targetConfig = this.network.getVertexConfig(edge.target);

                var sourceIsSelected = typeof this.selectedVerticesHash[edge.source.id] !== 'undefined';
                var targeIsSelected = typeof this.selectedVerticesHash[edge.target.id] !== 'undefined';
                var linkSvg = $(this.scaleGroupSVG).find('#' + edge.id)[0];

                if (sourceIsSelected && vertex === edge.source) {
                    linkSvg.setAttribute('x1', sourceConfig.coords.x);
                    linkSvg.setAttribute('y1', sourceConfig.coords.y);
                }
                if (targeIsSelected && vertex === edge.target) {
                    linkSvg.setAttribute('x2', targetConfig.coords.x);
                    linkSvg.setAttribute('y2', targetConfig.coords.y);
                }
            }
        }
    },

    createVertex: function (x, y) {

        /* vertex graph */
        var vertex = new Vertex({
            name: this.createdVertexCount
        });

        /* vertex config */
        var vertexConfig = new VertexConfig({
            id: vertex.id,
            coords: {x: x, y: y},
            renderer: new DefaultVertexRenderer({

            })
        });

        //update variables
        this.createdVertexCount++;
        this.network.addVertex({
            vertex: vertex,
            vertexConfig: vertexConfig,
            target:this.scaleGroupSVG
        });
    },
    createEdge: function (vertexSource, vertexTarget) {
        /* edge graph */
        var edge = new Edge({
            source: vertexSource,
            target: vertexTarget
        });

        var edgeConfig = new EdgeConfig({
            id: edge.id,
            renderer: new DefaultEdgeRenderer({

            })
        });

        this.network.addEdge({
            edge: edge,
            edgeConfig: edgeConfig,
            target:this.scaleGroupSVG
        });
    },

    removeVertex: function (vertex) {
        var vertexSvg = $(this.svg).find('svg[id="' + vertex.id + '"]')[0];
        $(vertexSvg).remove();

        for (var i = 0; i < vertex.edges.length; i++) {
            var edge = vertex.edges[i];
            this.removeEdge(edge);
        }
        this.network.removeVertex(vertex);

    },
    removeEdge: function (edge) {
        var edgeSvg = $(this.scaleGroupSVG).find('#' + edge.id)[0];
        $(edgeSvg).remove();
    },
    setSelectedVerticesDisplayAttr: function (displayAttr, value) {
        for (var i = 0, li = this.selectedVertices.length; i < li; i++) {
            var vertex = this.selectedVertices[i];
            if (typeof vertex !== 'undefined') {
                var vertexSvg = $(this.svg).find('svg[id="' + vertex.id + '"]')[0];
                var isSelected = $(vertexSvg).find('circle[network-type="select-vertex"]').length > 0 ? true : false;
                $(vertexSvg).remove();
                var vertexConfig = this.network.getVertexConfig(vertex);
                vertexConfig.renderer[displayAttr] = value;
                this.network.renderVertex(vertex, this.scaleGroupSVG);

                var vertexSvg = $(this.svg).find('svg[id="' + vertex.id + '"]')[0];
                if (isSelected) {
                    this._selectVertexSvg(vertex, vertexSvg);
                }

                for (var j = 0; j < vertex.edges.length; j++) {
                    var edge = vertex.edges[j];
                    if (typeof edge !== 'undefined') {
                        var edgeSvg = $(this.scaleGroupSVG).find('#' + edge.id)[0];
                        $(edgeSvg).remove();
                        this.drawEdge({
                            edgeDisplay: this.network.getEdgeDisplay(edge),
                            edge: edge
                        });
                    }
                }
            }
        }
    },
    drawGraph: function () {
        $(this.scaleGroupSVG).empty();
        /* vertices */
        for (var i = 0; i < this.network.graph.vertices.length; i++) {
            var vertex = this.network.graph.vertices[i];
            if (typeof vertex !== 'undefined') {
                this.network.renderVertex(vertex, this.scaleGroupSVG);
            }
        }
        /* edges */
        for (var i = 0; i < this.network.graph.edges.length; i++) {
            var edge = this.network.graph.edges[i];
            if (typeof edge !== 'undefined') {
                this.drawEdge({
                    edgeDisplay: this.network.getEdgeDisplay(edge),
                    edge: edge
                });
            }
        }
    }

};


function AttributeManager(args) {
    var _this = this;
    _.extend(this, Backbone.Events);
    this.id = Utils.genId('AttributeManager');
    this.dbName = 'AttributeManager';

    this._deleteDatabase();
    this._createDatabase();
};

AttributeManager.prototype = {
    _createDatabase: function () {
        var openRequest = indexedDB.open(this.dbName);
        openRequest.onerror = function (event) {
            console.log(event)
        };
        openRequest.onsuccess = function (event) {
            console.log(event)
        };

        openRequest.onupgradeneeded = function (event) {
            var db = event.target.result;

            // Create an objectStore to hold information about our customers. We're
            // going to use "ssn" as our key path because it's guaranteed to be
            // unique.
            var attributesObjectStore = db.createObjectStore("attribute", { autoIncrement: true });
            var attributeNameIdObjectStore = db.createObjectStore("attributeNameId", { autoIncrement: true });

            // Create an index to search customers by name. We may have duplicates
            // so we can't use a unique index.
            attributesObjectStore.createIndex("name", "name", { unique: false });
            attributesObjectStore.createIndex("attrId", "attrId", { unique: false });
            attributeNameIdObjectStore.createIndex("name", "name", { unique: true });
        };
    },
    _deleteDatabase: function () {
        // delete database
        var deleteRequest = indexedDB.deleteDatabase(this.dbName);
        deleteRequest.onsuccess = function (event) {
            console.log(event)
        };
        deleteRequest.onerror = function (event) {
            console.log(event)
        };
    },

    addAttribute: function (vertices, name, type, defaultValue) {
        var openRequest = indexedDB.open(this.dbName);
        openRequest.onerror = function (event) {
            console.log(event)
        };
        openRequest.onsuccess = function (event) {
            var db = openRequest.result;
            var transaction = db.transaction(["attribute", "attributeNameId"], "readwrite");
            var attributesObjectStore = transaction.objectStore("attribute");
            var attributeNameIdObjectStore = transaction.objectStore("attributeNameId");

            var addAttribute = attributeNameIdObjectStore.add({
                name: name,
                type: type
            });

            addAttribute.onsuccess = function (event) {
                var attributeKey = event.target.result;
                for (var i = 0; i < vertices.length; i++) {
                    var vertex = vertices[i];
                    attributesObjectStore.add({
                        name: vertex.name,
                        attrId: attributeKey,
                        value: defaultValue
                    });
                }
            };
        };
    },
    removeAttribute: function (name) {
        var openRequest = indexedDB.open(this.dbName);
        openRequest.onerror = function (event) {
            console.log(event);
        };
        openRequest.onsuccess = function (event) {

            var db = openRequest.result;
            var transaction = db.transaction(["attribute", "attributeNameId"], "readwrite");


            var attributesObjectStore = transaction.objectStore("attribute");
            var attributeNameIdObjectStore = transaction.objectStore("attributeNameId");


            var index = attributeNameIdObjectStore.index("name");
            index.getKey(name).onsuccess = function (event) {
                var attributeKey = event.target.result
                attributeNameIdObjectStore.delete(attributeKey);


                var attrIdIndex = attributesObjectStore.index("attrId");
                var singleKeyRange = IDBKeyRange.only(attributeKey);
                attrIdIndex.openKeyCursor(singleKeyRange).onsuccess = function (event) {
                    var cursor = event.target.result;
                    console.log(cursor)
                    if (cursor) {
                        attributesObjectStore.delete(cursor.primaryKey);
                        cursor.continue();
                    }
                };
            };

        };
    },
    getVertexAttributes: function (vertex,success) {
        var attributes = {};

        var openRequest = indexedDB.open(this.dbName);
        openRequest.onerror = function (event) {
            console.log(event);
        };
        openRequest.onsuccess = function (event) {

            var db = openRequest.result;
            var transaction = db.transaction(["attribute", "attributeNameId"]);//read

            var attributesObjectStore = transaction.objectStore("attribute");
            var attributeNameIdObjectStore = transaction.objectStore("attributeNameId");


            var index = attributesObjectStore.index("name");
            var singleKeyRange = IDBKeyRange.only(vertex.name);
            index.openCursor(singleKeyRange).onsuccess = function (event) {
                var cursor = event.target.result;
                if (cursor) {
                    var attrId = event.target.result.value.attrId;
                    var value = event.target.result.value.value;
                    attributeNameIdObjectStore.get(attrId).onsuccess = function (event) {
                        var attr = event.target.result.name;
                        attributes[attr] = value;
                    };
                    cursor.continue();
                } else {
                    success(attributes);
                }

            }
        };

    }

}


function CircosVertexRenderer(args) {
    var _this = this;
    _.extend(this, Backbone.Events);

    //defaults
    this.maxRaidus = 35;

    //set instantiation args, must be last
    _.extend(this, args);

}

CircosVertexRenderer.prototype = {
    render: function (args) {
        var vertex = args.vertex;
        var coords = args.coords;
        var targetSvg = args.target;

    },
    drawLink: function (args) {
        var angleStart1 = args.angleStart1;
        var angleEnd1 = args.angleEnd1;
        var angleStart2 = args.angleStart2;
        var angleEnd2 = args.angleEnd2;
        var coords = args.coords;

        var d = '';

        var coordsStart1 = SVG._polarToCartesian(coords.x, coords.y, radius - 20, angleStart1);
        var coordsEnd1 = SVG._polarToCartesian(coords.x, coords.y, coords.radius - 20, angleEnd1);

        var coordsStart2 = SVG._polarToCartesian(coords.x, coords.y, radius - 20, angleStart2);
        var coordsEnd2 = SVG._polarToCartesian(coords.x, coords.y, radius - 20, angleEnd2);


        d += SVG.describeArc(coords.x, coords.y, radius - 20, angleStart1, angleEnd1) + ' ';
        d += ['Q', coords.x, coords.y, coordsEnd2.x, coordsEnd2.y, ' '].join(' ');
        d += SVG.describeArc(coords.x, coords.y, radius - 20, angleStart2, angleEnd2) + ' ';
        d += [ 'Q', coords.x, coords.y, coordsEnd1.x, coordsEnd1.y, ' '].join(' ');

        var curve = SVG.addChild(this.group, 'path', {
            d: d,
            'stroke': 'red',
            'stroke-width': 2,
            'opacity': 1,
            'fill': 'crimson',
            'visibility': 'visible',
            'opacity': 0.7,
            'z-index': 10
        });

    }
}
function DefaultEdgeRenderer(args) {
    var _this = this;
    _.extend(this, Backbone.Events);

    //defaults
    this.shape = 'directed';
    this.size = 2;
    this.color = '#cccccc';
    this.strokeSize = 2;
    this.strokeColor = '#888888';
    this.opacity = 1;
    this.labelSize = 12;
    this.labelColor = '#111111';
//    this.labelPositionX = 5;
//    this.labelPositionY = 45;


    //set instantiation args, must be last
    _.extend(this, args);

}

DefaultEdgeRenderer.prototype = {
    render: function (args) {
        var edge = args.edge;
        var sourceCoords = args.sourceCoords;
        var targetCoords = args.targetCoords;
        var targetRenderer = args.targetRenderer;
        var targetSvg = args.target;


//        var sourceLayout = this.network.getVertexLayout(args.edge.source);
//        var targetLayout = this.network.getVertexLayout(args.edge.target);
//        var targetDisplay = this.network.getVertexDisplay(args.edge.target);

        var offset = (targetRenderer.size / 2 + targetRenderer.strokeSize / 2);
        // if not exists this marker, add new one to defs
        var markerArrowId = "#arrow-" + this.shape + "-" + offset + '-' + this.color;
        if ($(markerArrowId).length == 0) {
            this.addArrowShape(this.shape, offset, this.color, this.size,targetSvg);
        }
        var linkSvg = SVG.addChild(targetSvg, "line", {
            "id": edge.id,
            "x1": sourceCoords.x,
            "y1": sourceCoords.y,
            "x2": targetCoords.x,
            "y2": targetCoords.y,
            "stroke": this.color,
            "stroke-width": this.size,
            "cursor": "pointer",
            "marker-end": "url(" + markerArrowId + ")",
            'network-type': 'edge'
        }, 0);
    },
    /**/
    addArrowShape: function (type, offset, color, edgeSize,targetSvg) {
        var scale = 1 / edgeSize;

        if (typeof color === 'undefined') {
            color = '#000000';
        }
        var id = "arrow-" + type + '-' + offset + '-' + color;
        var marker = SVG.addChild(targetSvg, "marker", {
            "id": id,
            "orient": "auto",
            "style": "overflow:visible;"
        });

        switch (type) {
            case "directed":
                var arrow = SVG.addChild(marker, "polyline", {
                    "transform": "scale(" + scale + ") rotate(0) translate(0,0)",
                    "fill": color,
                    "stroke": color,
                    "stroke-width": edgeSize,
                    "points": "-" + offset + ",0 " + (-offset - 14) + ",-6 " + (-offset - 14) + ",6 -" + offset + ",0"
                });
                break;
            case "odirected":
                var arrow = SVG.addChild(marker, "polyline", {
                    "transform": "scale(0.5) rotate(0) translate(0,0)",
                    "fill": color,
                    "stroke": "black",
//			"points":"-14,0 -28,-6 -28,6 -14,0"
                    "points": "-" + offset + ",0 " + (-offset - 14) + ",-6 " + (-offset - 14) + ",6 -" + offset + ",0"
                });
                break;
            case "inhibited":
                var arrow = SVG.addChild(marker, "rect", {
                    "transform": "scale(0.5) rotate(0) translate(0,0)",
                    "fill": color,
                    "stroke": "black",
                    "x": -offset - 6,
                    "y": -6,
                    "width": 6,
                    "height": 12
                });
                break;
            case "dot":
                var arrow = SVG.addChild(marker, "circle", {
                    "transform": "scale(0.5) rotate(0) translate(0,0)",
                    "fill": color,
                    "stroke": "black",
                    "cx": -offset - 6,
                    "cy": 0,
                    "r": 6
                });
                break;
            case "odot":
                var arrow = SVG.addChild(marker, "circle", {
                    "transform": "scale(0.5) rotate(0) translate(0,0)",
                    "fill": color,
                    "stroke": "black",
                    "cx": -offset - 6,
                    "cy": 0,
                    "r": 6
                });
                break;
        }
    }

}
function DefaultVertexRenderer(args) {
    var _this = this;
    _.extend(this, Backbone.Events);

    //defaults
    this.shape = 'circle';
    this.size = 35;
    this.color = '#cccccc';
    this.strokeSize = 1;
    this.strokeColor = '#888888';
    this.opacity = 1;
    this.labelSize = 12;
    this.labelColor = '#111111';
//    this.labelPositionX = 5;
//    this.labelPositionY = 45;

    //set instantiation args, must be last
    _.extend(this, args);

}

DefaultVertexRenderer.prototype = {
    render: function (args) {
        switch (this.shape) {
            case "circle":
                this.drawCircleShape(args);
                break;
            case "square":
                this.drawSquareShape(args);
                break;
        }
    },
    drawCircleShape: function (args) {
        var vertex = args.vertex;
        var coords = args.coords;
        var targetSvg = args.target;



        var size = this.size + this.strokeSize;
        var size = size + (size * 0.3);
        var midOffset = size / 2;

        var vertexSvg = SVG.create("svg", {
            "id": vertex.id,
            "cursor": "pointer",
            x: coords.x - midOffset,
            y: coords.y - midOffset,
            'network-type': 'vertex-svg'
        });
        var groupSvg = SVG.addChild(vertexSvg, 'g');
        var circle = SVG.addChild(groupSvg, 'circle', {
            cx: midOffset,
            cy: midOffset,
            r: this.size / 2,
            stroke: this.strokeColor,
            'stroke-width': this.strokeSize,
            fill: this.color,
            'network-type': 'vertex'
        });
        var vertexText = SVG.addChild(vertexSvg, "text", {
            "x": 5,
            "y": this.labelSize+size,
            "font-size": this.labelSize,
            "fill": this.labelColor,
            'network-type': 'vertexLabel'
        });
        vertexText.textContent = vertex.name;
        targetSvg.appendChild(vertexSvg);
    },
    drawSquareShape: function (args) {

    }
}
function EdgeConfig(args) {

    this.id;
    this.renderer;
    this.type;
    this.visible;

    //set instantiation args, must be last
    _.extend(this, args);

}

EdgeConfig.prototype = {
    render:function(args){
        this.renderer.render(args);
    }
}
function Edge(args) {

    this.id = Utils.genId('Edge');

    this.name;
    this.source;
    this.target;
    this.weight;
    this.directed;

    //set instantiation args, must be last
    _.extend(this, args);

}

Edge.prototype = {
    getSource: function () {
        return this.source;
    },
    setSource: function (vertex) {
        this.source = vertex;
    },
    getTarget: function () {
        return this.target;
    },
    setTarget: function (vertex) {
        this.target = vertex;
    }
}
function GraphLayout(args) {
    _.extend(this, Backbone.Events);
    this.id = Utils.genId('GraphLayout');

    this.verticesList = [];

    //set instantiation args, must be last
    _.extend(this, args);

    this.vertices = {};

    this._init();

    this.on(this.handlers);
}

GraphLayout.prototype = {
    _init: function () {
        for (var i in this.verticesList) {
            var vertex = this.verticesList[i];
            if (typeof vertex.x === 'undefined') {
                vertex.x = 0;
            }
            if (typeof vertex.y === 'undefined') {
                vertex.y = 0;
            }
            if (typeof vertex.z === 'undefined') {
                vertex.z = 0;
            }
            this.vertices[vertex.id] = vertex;
        }
    },
    getRandomArbitrary: function (min, max) {
        return Math.random() * (max - min) + min;
    },
    applyRandom3d: function () {
        for (var i in this.vertices) {
            var vertex = this.vertices[i];
            vertex.x = this.getRandomArbitrary(-300, 300);
            vertex.y = this.getRandomArbitrary(-300, 300);
            vertex.z = this.getRandomArbitrary(10, 600);
        }
    },
    applySphereSurface: function (offsetZ) {
        //        θ = theta
        //        φ = phi
        var radius = 200;
        var n = Object.keys(this.vertices).length;
        var i = 0;
        for (var key in this.vertices) {
            var vertex = this.vertices[key];

            var phi = Math.acos(-1 + ( 2 * i ) / n);
            var theta = Math.sqrt(n * Math.PI) * phi;

            vertex.x = radius * Math.cos(theta) * Math.sin(phi);
            vertex.y = radius * Math.sin(theta) * Math.sin(phi);
            vertex.z = radius * Math.cos(phi) + offsetZ;

            /* update */
            i++;
        }
    },
    getRandom2d: function () {

    }

}
/*

 public E addEdge(V sourceVertex, V targetVertex);
 public boolean addEdge(V sourceVertex, V targetVertex, E e);
 public boolean containsEdge(E e);
 public boolean containsEdge(V sourceVertex, V targetVertex);
 public List<E> getAllEdges();
 public E getEdge(String edgeName);
 public List<E> getAllEdges(String edgeName);
 public List<E> getAllEdges(V vertex);
 public E getEdge(V sourceVertex, V targetVertex);
 public List<E> getAllEdges(V sourceVertex, V targetVertex);
 void setEdges(List<E> edges);
 public boolean removeEdge(E e);
 public List<E> removeAllEdges(V vertex);
 public E removeEdge(V sourceVertex, V targetVertex);
 public List<E> removeAllEdges(V sourceVertex, V targetVertex);
 public boolean removeAllEdges(Collection<? extends E> edges);
 public V getEdgeSource(E e);
 public V getEdgeTarget(E e);




 public boolean addVertex(V v);
 boolean addAllVertices(Collection<? extends V> vertices);
 public boolean containsVertex(V vertex);
 public V getVertex(String vertexId);

 public List<V> getAllVertices();

 public int getDegreeOf(V v);
 public boolean copySubgraphAddVertex(V v);
 public List<V> getNotNullVertices();
 public List<V> getAdjacentVertices(V v);
 public boolean removeVertex(V v);
 public boolean removeVertices(Collection<? extends V> vertices);
 public int getNumberOfVertices();


 public void clear();
 public void setVertices(List<V> verticesList);
 public int getVerticesMapId(V v);
 public V getVertex(int mapPosition);
 public List<List<V>> getAllInformationComponents(boolean isolatedNode);
 public int getNumberOfBicomponents();

 */
function Graph(args) {
    _.extend(this, Backbone.Events);
    this.id = Utils.genId('Graph');

    this.vertices = [];
    this.edges = [];

    this.display = {
        style: {

        },
        layouts: {

        }
    };

    this.numberOfVertices = 0;
    this.numberOfEdges = 0;

    this.graphType = '';

    //set instantiation args, must be last
    _.extend(this, args);

    this.verticesIndex = {};
    this.edgesIndex = {};

    this.on(this.handlers);
}

Graph.prototype = {
    addEdge: function (edge) {
        if (edge.source == null || edge.target == null) {
            return false
        }

        this.addVertex(edge.source);
        this.addVertex(edge.target);
        var length = this.edges.push(edge);
        var insertPosition = length - 1;
        this.edgesIndex[edge.id] = insertPosition;

        edge.source.addEdge(edge);
        edge.target.addEdge(edge);
        this.trigger('edge:add', {edge: edge, graph: this});

        this.numberOfEdges++;
        return true;
    },
    addVertex: function (vertex) {
        if (vertex == null) {
            return false
        }
        // Check if already exists
        if (this.containsVertex(vertex)) {
            return false;
        }
        // Add the vertex
        var length = this.vertices.push(vertex);
        var insertPosition = length - 1;
        this.verticesIndex[vertex.id] = insertPosition;

        // the real number of vertices
        this.numberOfVertices++;

        this.trigger('vertex:add', {vertex: vertex, graph: this});
        return true;
    },
    removeEdge: function (edge) {
        if (edge == null) {
            return false
        }
        // Check if already exists
        if (!this.containsEdge(edge)) {
            return false;
        }

        //remove edge from vertex
        edge.source.removeEdge(edge);
        edge.target.removeEdge(edge);

        var position = this.edgesIndex[edge.id];
        delete this.edgesIndex[edge.id];
        delete this.edges[position];

        this.trigger('edge:remove', {edge: edge, graph: this});
        this.numberOfEdges--;
        return true;
    },
    removeVertex: function (vertex) {
        if (vertex == null) {
            return false
        }
        // Check if already exists
        if (!this.containsVertex(vertex)) {
            return false;
        }

        for (var i = 0; i < vertex.edges.length; i++) {
            var edge = vertex.edges[i];
            // remove edges from source or target
            if (edge.source !== vertex) {
                edge.source.removeEdge(edge);
            }
            if (edge.target !== vertex) {
                edge.target.removeEdge(edge);
            }
            var position = this.edgesIndex[edge.id];
            delete this.edgesIndex[edge.id];
            delete this.edges[position];
            this.trigger('edge:remove', {edge: edge, graph: this});
        }
        vertex.removeEdges();

        var position = this.verticesIndex[vertex.id];
        delete this.verticesIndex[vertex.id];
        delete this.vertices[position];

        this.trigger('vertex:remove', {vertex: vertex, graph: this});
        this.numberOfVertices--;
        return true;
    },
    containsEdge: function (edge) {
        if (typeof this.edgesIndex[edge.id] !== 'undefined') {
            return true;
        } else {
            return false;
        }
    },
    containsVertex: function (vertex) {
        if (typeof this.verticesIndex[vertex.id] !== 'undefined') {
            return true;
        } else {
            return false;
        }
    },
    /**/
    getVertexById: function (vertexId) {
        return this.vertices[this.verticesIndex[vertexId]];
    },
    /**/
    addLayout: function (layout) {
        this.display.layouts[layout.id] = layout;
    }

}
function NetworkConfig(args) {
    var _this = this;
    _.extend(this, Backbone.Events);
    this.id = Utils.genId('NetworkConfig');

    //set instantiation args, must be last
    _.extend(this, args);


    this.vertices = {}; // [{id:"one",color:red,...},...]
    this.edges = {};  // [{id:"one",color:red,...},...]
    this.general = {};

    this.on(this.handlers);
}

NetworkConfig.prototype = {
    setVertexConfig:function(vertexConfig){
        this.vertices[vertexConfig.id] = vertexConfig;
    },
    getVertexConfig:function(vertex){
        return this.vertices[vertex.id];
    },
    setEdgeConfig:function(edgeConfig){
        this.edges[edgeConfig.id] = edgeConfig;
    },
    getEdgeConfig:function(edge){
        return this.edges[edge.id];
    }
}
function Network(args) {
    var _this = this;
    _.extend(this, Backbone.Events);
    this.id = Utils.genId('Network');

    //set instantiation args, must be last
    _.extend(this, args);


    this.graph = new Graph();
    this.config = new NetworkConfig();
    this.attributeManager = new AttributeManager();

    this.on(this.handlers);


}

Network.prototype = {
    addVertex: function (args) {
        var vertex = args.vertex;
        var vertexConfig = args.vertexConfig;
        var target = args.target;

        this.graph.addVertex(vertex);
        this.setVertexConfig(vertexConfig);
        this.renderVertex(vertex, target);
    },
    addEdge: function (args) {
        var edge = args.edge;
        var edgeConfig = args.edgeConfig;
        var target = args.target;

        this.graph.addEdge(edge);
        this.setEdgeConfig(edgeConfig);
        this.renderEdge(edge, target);
    },
    setVertexConfig: function (vertexConfig) {
        this.config.setVertexConfig(vertexConfig);
    },
    setEdgeConfig: function (edgeConfig) {
        this.config.setEdgeConfig(edgeConfig);
    },
    getVertexConfig: function (vertex) {
        return this.config.getVertexConfig(vertex);
    },
    getEdgeConfig: function (edge) {
        return this.config.getEdgeConfig(edge);
    },
    getVertexById: function (vertexId) {
        return this.graph.getVertexById(vertexId);
    },
    removeVertex: function (vertex) {
        this.graph.removeVertex(vertex);
    },
    renderVertex: function (vertex, target) {
        var vertexConfig = this.config.getVertexConfig(vertex);
        vertexConfig.render({
            coords: vertexConfig.coords,
            vertex: vertex,
            target: target
        });
    },
    renderEdge: function (edge, target) {
        var edgeConfig = this.config.getEdgeConfig(edge);
        var sourceConfig = this.config.getVertexConfig(edge.source);
        var targetConfig = this.config.getVertexConfig(edge.target);
        edgeConfig.render({
            sourceCoords: sourceConfig.coords,
            targetCoords: targetConfig.coords,
            targetRenderer: targetConfig.renderer,
            edge: edge,
            target: target
        });
    },

    /* Attribute Manager */
    addAttribute: function (name, type, defaultValue) {
        this.attributeManager.addAttribute(this.graph.vertices, name, type, defaultValue);
    },
    removeAttribute: function (name) {
        this.attributeManager.removeAttribute(name);
    },
    getVertexAttributes: function (vertex, success) {
        this.attributeManager.getVertexAttributes(vertex, success);
    }
}
function VertexConfig(args) {

    this.id;
    this.coords;
    this.renderer;
    this.type;
    this.visible;

    //set instantiation args, must be last
    _.extend(this, args);

}

VertexConfig.prototype = {
    setCoords:function(x,y,z){
        this.coords.x = x;
        this.coords.y = y;
        this.coords.z = z;
    },
    getCoords:function(){
        return this.coords;
    },
    render:function(args){
        this.renderer.render(args);
    }
}
function Vertex(args) {
    this.id = Utils.genId('v');

    this.name;
    this.edges = [];

    //set instantiation args, must be last
    _.extend(this, args);

}

Vertex.prototype = {
    removeEdge: function (edge) {
        for (var i = 0; i < this.edges.length; i++) {
            if (this.edges[i].id === edge.id) {
                this.edges.splice(i, 1);
                break;
            }
        }
    },
    removeEdges: function(){
        this.edges = [];
    },
    addEdge: function (edge) {
        this.edges.push(edge);
    }
}
function NetworkViewer(args) {
    // Using Underscore 'extend' function to extend and add Backbone Events
    _.extend(this, Backbone.Events);
    this.id = Utils.genId('networkviewer');


    //set default args
    this.targetId;
    this.autoRender = false;
    this.sidePanel = false;
    this.overviewPanel = false;
    this.height;
    this.width;
    this.overviewScale = 0.2;

    //set instantiation args, must be last
    _.extend(this, args);

    this.toolBar;
    this.editionBar;
    this.networkSvgLayout;

    this.contextMenu;


    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
}

NetworkViewer.prototype = {

    render: function (targetId) {
        if (targetId)this.targetId = targetId;
        if ($('#' + this.targetId).length < 1) {
            console.log('targetId not found in DOM');
            return;
        }

        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div id="' + this.id + '" class="bootstrap" style="height:100%;border:1px solid lightgrey;position:relative;"></div>')[0];
        $(this.targetDiv).append(this.div);

        this.height = $(this.targetDiv).height();
        this.width = $(this.targetDiv).width();

        this.toolbarDiv = $('<div id="nv-toolbar"></div>')[0];
        this.editionbarDiv = $('<div id="nv-editionbar"></div>')[0];
        this.centerPanelDiv = $('<div id="nv-centerpanel" style="postion:relative;"></div>')[0];
        this.statusbarDiv = $('<div id="nv-statusbar"></div>')[0];

        $(this.div).append(this.toolbarDiv);
        $(this.div).append(this.editionbarDiv);
        $(this.div).append(this.centerPanelDiv);
        $(this.div).append(this.statusbarDiv);

        this.mainPanelDiv = $('<div id="nv-mainpanel" style="postion:absolute;right:0px;height:100%;"></div>')[0];
        $(this.centerPanelDiv).append(this.mainPanelDiv);

        if (this.sidePanel) {
            this.sidePanelDiv = $('<div id="nv-sidepanel" style="postion:absolute;right:0px;height:100%;"></div>')[0];
            $(this.centerPanelDiv).append(this.sidePanelDiv);
        }

        if (this.overviewPanel) {
            this.overviewPanelDiv = $('<div id="nv-overviewpanel" style="postion:absolute;bottom:10px;right:10px;width:200px;height:200px;border:1px solid lightgrey;"></div>')[0];
            $(this.centerPanelDiv).append(this.overviewPanelDiv);
        }

        this.rendered = true;
    },
    draw: function () {
        if (!this.rendered) {
            console.info('Genome Viewer is not rendered yet');
            return;
        }

        /* Toolbar Bar */
        this.toolBar = this._createToolBar($(this.toolbarDiv).attr('id'));

        /* edition Bar */
        this.editionBar = this._createEditionBar($(this.editionbarDiv).attr('id'));

        this.networkSvgLayout = this._createNetworkSvgLayout($(this.mainPanelDiv).attr('id'));


        /* context menu*/
        this.contextMenu = this._createContextMenu();


//        this.networkSvgOverview = this._createNetworkSvgOverview($(this.overviewPanelDiv).attr('id'));


//        // networkSVG for the overview
//        if(this.overview) {
//            div = $('#'+this.getGraphCanvasId()+'_overview')[0];
//            this.networkSvgOverview = new NetworkSvg(div, this.networkData, {"width": "100%", "height": "100%", "parentNetwork": this.networkSvg, "scale": this.overviewScale});
//        }

    },

    _createToolBar: function (targetId) {
        var _this = this;
        var toolBar = new ToolBar({
            targetId: targetId,
            autoRender: true,
            handlers: {
                'collapseButton:click': function (event) {
                    console.log(event);
                    //todo
                },
                'layout:change': function (event) {
                    console.log(event);
                    _this.setLayout(event.option);
                },
                'labelSize:change': function (event) {
                    console.log(event);
                    _this.setLabelSize(event.option);
                },
                'select:change': function (event) {
                    console.log(event);
                    _this.select(event.option);
                },
                'backgroundButton:click': function (event) {
                    console.log(event);
                    //todo
                },
                'showOverviewButton:change': function (event) {
                    console.log(event);
                    //todo
                },
                'zoom:change': function (event) {
                    console.log(event.zoom);
                    //todo
                },
                'search': function (event) {
                    console.log(event);
                    //todo
                }
            }
        });
        return toolbar;
    },
    _createEditionBar: function (targetId) {
        var _this = this;
        var editionBar = new EditionBar({
            targetId: targetId,
            autoRender: true,
            handlers: {
                'selectButton:click': function (event) {
                    _this.networkSvgLayout.setMode("select");
                },
                'addButton:click': function (event) {
                    _this.networkSvgLayout.setMode("add");
                },
                'linkButton:click': function (event) {
                    _this.networkSvgLayout.setMode("join");
                },
                'deleteButton:click': function (event) {
                    _this.networkSvgLayout.setMode("delete");
                },
                'nodeShape:change': function (event) {
                    //TODO
                },
                'nodeSize:change': function (event) {
                    _this.networkSvgLayout.setSelectedVerticesDisplayAttr('size', parseInt(event.value));
                },
                'nodeStrokeSize:change': function (event) {
                    _this.networkSvgLayout.setSelectedVerticesDisplayAttr('strokeSize', parseInt(event.value));
                },
                'opacity:change': function (event) {
                    _this.networkSvgLayout.setSelectedVerticesDisplayAttr('opacity', parseInt(event.value));
                },
                'edgeShape:change': function (event) {
                    //TODO
                },
                'nodeColorField:change': function (event) {
                    _this.networkSvgLayout.setSelectedVerticesDisplayAttr('color', event.value);
                },
                'nodeStrokeColorField:change': function (event) {
                    _this.networkSvgLayout.setSelectedVerticesDisplayAttr('strokeColor', event.value);
                },
                'edgeColorField:change': function (event) {
                    _this.networkSvgLayout.setEdgeColor(event.value);
                },
                'nodeNameField:change': function (event) {
                    _this.networkSvgLayout.setNodeName(event.value);
                },
                'edgeLabelField:change': function (event) {
                    _this.networkSvgLayout.setEdgeLabel(event.value);
                },
                'nodeLabelField:change': function (event) {
                    _this.networkSvgLayout.setNodeLabel(event.value);
                }
            }
        });
        return editionBar;
    },

    _createNetworkSvgLayout: function (targetId) {
        var _this = this;

        var toolbarHeight = $(this.toolbarDiv).height();
        var editionbarHeight = $(this.editionbarDiv).height();
        var height = this.height - toolbarHeight - editionbarHeight;

        console.log(height)
        var networkSvgLayout = new NetworkSvgLayout({
            targetId: targetId,
            width: this.width,
            height: height,
            networkData: this.networkData,
            autoRender: true,
            handlers: {
                'vertex:leftClick': function (event) {
                    console.log(event);
                    _this.editionBar.setNodeColor(event.vertexConfig.renderer.color);
                    _this.editionBar.setNodeStrokeColor(event.vertexConfig.renderer.strokeColor);
                },
                'vertex:rightClick': function (event) {
                    console.log(event);
                    _this._fillContextMenu(event);
                    $(_this.contextMenuDiv).css({
                        display: "block",
                        left: event.x,
                        top: event.y
                    });
                }
            }
        });
        networkSvgLayout.createVertex(100, 100);
        networkSvgLayout.createVertex(200, 200);
        networkSvgLayout.createVertex(300, 300);
        networkSvgLayout.createVertex(400, 400);

        return networkSvgLayout;
    },
    _createContextMenu: function () {
        var _this = this;
        var html = '' +
            '<div id="nvContextMenu" class="dropdown clearfix">' +
            '    <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu" style="display:block;position:static;margin-bottom:5px;">' +
            '        <li><a tabindex="-1" href="#">Action</a></li>' +
            '        <li class="divider"></li>' +
            '        <li><a tabindex="-1" href="#">Separated link</a></li>' +
            '    </ul>' +
            '</div>';

        this.contextMenuDiv = $(html)[0];
        $(this.div).append(this.contextMenuDiv);


        $(_this.contextMenuDiv).bind('click.networkViewer', function (event) {
            var targetEl = event.target;
            var text = $(targetEl).text();
        });


        $(document).bind('click.networkViewer', function () {
            $(_this.contextMenuDiv).hide();
        });

        /**************/
    },
    _fillContextMenu: function (event) {
        var _this = this;
        var attributes = event.attributes;
        var vertex = event.vertex;
        var ul = $(this.contextMenuDiv).children().first()[0];
        $(ul).empty();
        for (var i in attributes) {
            var menuEntry = $('<li role="presentation"><a>' + attributes[i] + '</a></li>')[0];
            $(ul).append(menuEntry);
        }
        var menuEntry = $('<li role="presentation"><input id="nodeColorField" type="text"></li>')[0];
        var deleteEntry = $('<li role="presentation"><a tabindex="-1" role="menuitem">Delete</a></li>')[0];
//        $(ul).append(menuEntry);
        $(ul).append(deleteEntry);

        $(deleteEntry).bind('click.networkViewer', function (event) {
            _this.networkSvgLayout.removeVertex(vertex);
        });

//        var nodeColorField = $(ul).find('#nodeColorField');
//        var pickAColorConfig = {
//            showSpectrum: true,
//            showSavedColors: true,
//            saveColorsPerElement: false,
//            fadeMenuToggle: true,
//            showAdvanced: true,
//            showBasicColors: true,
//            showHexInput: true,
//            allowBlank: true
//        }
//        $(nodeColorField).pickAColor(pickAColorConfig);
    },
    _createNetworkSvgOverview: function (targetId) {
        var _this = this;
        var networkSvg = new NetworkSvg({
            targetId: targetId,
            width: 1000 * 0.2,
            height: 300 * 0.2,
            networkData: this.networkData,
            autoRender: true,
            handlers: {
            }
        });
        return networkSvg;
    },
    setLayout: function (type, nodeLst) {
        var nodeList = nodeLst || this.networkData.getNodesList();
        switch (type) {
            case "Circle":
                var vertexCoordinates = this.calculateLayoutVertex(type, nodeList.length);
                var aux = 0;
                for (var i = 0; i < nodeList.length; i++) {
                    var x = this.networkSvg.getWidth() * (0.05 + 0.85 * vertexCoordinates[aux].x);
                    var y = this.networkSvg.getHeight() * (0.05 + 0.85 * vertexCoordinates[aux].y);
                    this.networkSvg.moveNode(nodeList[i], x, y);
                    aux++;
                }
                break;
            case "Square":
                var vertexCoordinates = this.calculateLayoutVertex(type, nodeList.length);
                var aux = 0;
                for (var i = 0; i < nodeList.length; i++) {
                    var x = this.networkSvg.getWidth() * (0.05 + 0.85 * vertexCoordinates[aux].x);
                    var y = this.networkSvg.getHeight() * (0.05 + 0.85 * vertexCoordinates[aux].y);
                    this.networkSvg.moveNode(nodeList[i], x, y);
                    aux++;
                }
                break;
            case "Random":
                for (var i = 0; i < nodeList.length; i++) {
                    var x = this.networkSvg.getWidth() * (0.05 + 0.85 * Math.random());
                    var y = this.networkSvg.getHeight() * (0.05 + 0.85 * Math.random());
                    this.networkSvg.moveNode(nodeList[i], x, y);
                }
                break;
            default:
                var dotText = this.networkData.toDot();
                var url = "http://bioinfo.cipf.es/utils/ws/rest/network/layout/" + type + ".coords";
//		var url = "http://localhost:8080/opencga/rest/utils/network/layout/"+type+".coords";
                var _this = this;

                $.ajax({
                    async: false,
                    type: "POST",
                    url: url,
                    dataType: "text",
                    data: {
                        dot: dotText
                    },
                    cache: false,
                    success: function (data) {
                        var response = JSON.parse(data);
                        for (var nodeId in response) {
                            var x = _this.networkSvgLayout.getWidth() * (0.05 + 0.85 * response[nodeId].x);
                            var y = _this.networkSvgLayout.getHeight() * (0.05 + 0.85 * response[nodeId].y);
                            _this.networkSvgLayout.moveNode(nodeId, x, y);
                        }
                    }
                });
                break;
        }
        this.networkData.updateFromSvg(this.networkSvg.getNodeMetainfo());
    },
    select: function (option) {
        switch (option) {
            case 'All Nodes' :
                this.networkSvg.selectAllNodes();
                break;
            case 'All Edges' :
                this.networkSvg.selectAllEdges();
                break;
            case 'Everything' :
                this.networkSvg.selectAll();
                break;
            case 'Adjacent' :
                this.networkSvg.selectAdjacentNodes();
                break;
            case 'Neighbourhood' :
                this.networkSvg.selectNeighbourhood();
                break;
            case 'Connected' :
                this.networkSvg.selectConnectedNodes();
                break;
            default :
                console.log(option + " not yet defined");
        }
    },
    setLabelSize: function (option) {
        this.networkSvg.setLabelSize(option);
    }
}