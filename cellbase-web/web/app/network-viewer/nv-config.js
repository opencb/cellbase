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

NODE_TYPES = {
	undefined:{
		fillcolor:"#993300",
		shape:"circle",
		opacity:1,
		height:1,
		width:1,
		size:7,
		color:"#000000",
		strokeSize:1,
		x:0,
		y:0
	},
	"pathway":{
		fillcolor:"#FFFFFF",
		shape:"rectangle",
		opacity:1,
		height:1,
		width:1,
		size:7,
		color:"#CCFFCC",
		strokeSize:5,
		x:0,
		y:0
	},
	"interaction":{
		fillcolor:"#FFFFFF",
		shape:"square",
		opacity:1,
		height:1,
		width:1,
		size:3,
		color:"#000000",
		strokeSize:1,
		x:0,
		y:0
	},
	"SmallMolecule":{
		fillcolor:"#CCFFCC",
		shape:"circle",
		opacity:1,
		height:1,
		width:1,
		size:5,
		color:"#000000",
		strokeSize:1,
		x:0,
		y:0
	},
	"Complex":{
		fillcolor:"#CCFFFF",
		shape:"rectangle",
		opacity:1,
		height:1,
		width:1.5,
		size:5,
		color:"#000000",
		strokeSize:1,
		x:0,
		y:0
	},
	"Protein":{
		fillcolor:"#CCFFCC",
		shape:"rectangle",
		opacity:1,
		height:1,
		width:1.5,
		size:5,
		color:"#000000",
		strokeSize:1,
		x:0,
		y:0
	},
	"PhysicalEntity":{
		fillcolor:"#CCFFCC",
		shape:"circle",
		opacity:1,
		height:1,
		width:1,
		size:5,
		color:"#000000",
		strokeSize:1,
		x:0,
		y:0
	},
	"Rna":{
		fillcolor:"#CCFFCC",
		shape:"rectangle",
		opacity:1,
		height:1,
		width:1.5,
		size:5,
		color:"#000000",
		strokeSize:1,
		x:0,
		y:0
	},
	"Dna":{
		fillcolor:"#CCFFCC",
		shape:"rectangle",
		opacity:1,
		height:1,
		width:1.5,
		size:5,
		color:"#000000",
		strokeSize:1,
		x:0,
		y:0
	},
	"none":{
		fillcolor:"#993300",
		shape:"circle",
		opacity:1,
		height:1,
		width:1,
		size:7,
		color:"#000000",
		strokeSize:1,
		x:0,
		y:0
	}
};

EDGE_TYPES = {
	undefined:{
		x1:0,
		y1:0,
		x2:0,
		y2:0,
		markerArrow:"url(#arrow-directed-14)"
	},
	"directed":{
		x1:0,
		y1:0,
		x2:0,
		y2:0,
		markerArrow:"url(#arrow-directed-14)"
	}
};

