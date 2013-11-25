/*
 * Copyright (c) 2012 Francisco Salavert (ICM-CIPF)
 * Copyright (c) 2012 Ruben Sanchez (ICM-CIPF)
 * Copyright (c) 2012 Ignacio Medina (ICM-CIPF)
 *
 * This file is part of JS Common Libs.
 *
 * JS Common Libs is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * JS Common Libs is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JS Common Libs. If not, see <http://www.gnu.org/licenses/>.
 */

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
        this.tracksViewedRegion = this.width / Utils.getPixelBaseByZoom(this.zoom);

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
                this.collapseDiv = $('<div type="button" class="btn btn-default btn-xs" style="margin-left:10px;height:20px"><span class="glyphicon glyphicon-minus"></span></div>');
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
            downX = (event.pageX - $(_this.svg).offset().left);
            selBox.setAttribute("x", downX);
            lastX = _this.positionBox.getAttribute("x");
            if (status == '') {
                status = 'setRegion'
            }
            hideResizeControls();
            $(this).mousemove(function (event) {
                moveX = (event.pageX - $(_this.svg).offset().left);
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
                        var inc = moveX - downX;
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
                            _this.region.start = Math.round(bioS);
                            _this.region.end = Math.round(bioE);
                            recalculateResizeControls();
                            showResizeControls();
                            _this.trigger('region:change', {region: _this.region, sender: _this});
                            recalculateResizeControls();
                            showResizeControls();
                        }
                        break;
                    case 'setRegion' :
                        var w = _this.positionBox.getAttribute("width");
                        var pixS = downX - (w / 2);
                        var pixE = downX + (w / 2);
                        var bioS = (pixS - offset) / _this.pixelBase;
                        var bioE = (pixE - offset) / _this.pixelBase;
                        _this.region.start = Math.round(bioS);
                        _this.region.end = Math.round(bioE);

                        _this.positionBox.setAttribute("x", downX - (w / 2));
                        _this.trigger('region:change', {region: _this.region, sender: _this});
                        break;
                    case 'selectingRegion' :
                        var bioS = (downX - offset) / _this.pixelBase;
                        var bioE = (moveX - offset) / _this.pixelBase;
                        _this.region.start = parseInt(Math.min(bioS, bioE));
                        _this.region.end = parseInt(Math.max(bioS, bioE));

                        var w = Math.abs(downX - moveX);
                        _this.positionBox.setAttribute("width", w);
                        _this.positionBox.setAttribute("x", Math.abs((downX + moveX) / 2) - (w / 2));
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