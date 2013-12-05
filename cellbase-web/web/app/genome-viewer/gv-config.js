FEATURE_CONFIG = {
    gene: {
        filters: [
            {
                name: "biotype",
                text: "Biotype",
                values: ["3prime_overlapping_ncrna", "ambiguous_orf", "antisense", "disrupted_domain", "IG_C_gene", "IG_D_gene", "IG_J_gene", "IG_V_gene", "lincRNA", "miRNA", "misc_RNA", "Mt_rRNA", "Mt_tRNA", "ncrna_host", "nonsense_mediated_decay", "non_coding", "non_stop_decay", "polymorphic_pseudogene", "processed_pseudogene", "processed_transcript", "protein_coding", "pseudogene", "retained_intron", "retrotransposed", "rRNA", "sense_intronic", "sense_overlapping", "snoRNA", "snRNA", "transcribed_processed_pseudogene", "transcribed_unprocessed_pseudogene", "unitary_pseudogene", "unprocessed_pseudogene"],
                selection: "multi"
            }
        ]
        //options:[
        //]
    },
    snp: {
        filters: [
            {
                name: "consequence_type",
                text: "Consequence Type",
                values: ["2KB_upstream_variant", "5KB_upstream_variant", "500B_downstream_variant", "5KB_downstream_variant", "3_prime_UTR_variant", "5_prime_UTR_variant", "coding_sequence_variant", "complex_change_in_transcript", "frameshift_variant", "incomplete_terminal_codon_variant", "inframe_codon_gain", "inframe_codon_loss", "initiator_codon_change", "non_synonymous_codon", "intergenic_variant", "intron_variant", "mature_miRNA_variant", "nc_transcript_variant", "splice_acceptor_variant", "splice_donor_variant", "splice_region_variant", "stop_gained", "stop_lost", "stop_retained_variant", "synonymous_codon"],
                selection: "multi"
            }
        ]
        //options:[
        //]
    },
    bam: {
        //filters:[{
        //name:"view",
        //text:"View",
        //values:["view_as_pairs","show_soft-clipped_bases"],
        //selection:"multi"
        //}
        //],
        options: [
            {
                text: "View as pairs",
                name: "view_as_pairs",
                type: "checkbox",
                fetch: true,
                checked: false
            },
            {
                text: "Show Soft-clipping",
                name: "show_softclipping",
                type: "checkbox",
                fetch: true,
                checked: false
            },
            {
                text: "Insert size interval",
                name: "insert_size_interval",
                type: "doublenumberfield",
                fetch: false,
                minValue: 0,
                maxValue: 0
            }
        ]
    }

};
FEATURE_OPTIONS = {
    gene: [
        {
            name: "biotype",
            text: "Biotype",
            values: ["3prime_overlapping_ncrna", "ambiguous_orf", "antisense", "disrupted_domain", "IG_C_gene", "IG_D_gene", "IG_J_gene", "IG_V_gene", "lincRNA", "miRNA", "misc_RNA", "Mt_rRNA", "Mt_tRNA", "ncrna_host", "nonsense_mediated_decay", "non_coding", "non_stop_decay", "polymorphic_pseudogene", "processed_pseudogene", "processed_transcript", "protein_coding", "pseudogene", "retained_intron", "retrotransposed", "rRNA", "sense_intronic", "sense_overlapping", "snoRNA", "snRNA", "transcribed_processed_pseudogene", "transcribed_unprocessed_pseudogene", "unitary_pseudogene", "unprocessed_pseudogene"],
            selection: "multi"
        }
    ],
    snp: [
        {
            name: "consequence_type",
            text: "Consequence Type",
            values: ["2KB_upstream_variant", "5KB_upstream_variant", "500B_downstream_variant", "5KB_downstream_variant", "3_prime_UTR_variant", "5_prime_UTR_variant", "coding_sequence_variant", "complex_change_in_transcript", "frameshift_variant", "incomplete_terminal_codon_variant", "inframe_codon_gain", "inframe_codon_loss", "initiator_codon_change", "non_synonymous_codon", "intergenic_variant", "intron_variant", "mature_miRNA_variant", "nc_transcript_variant", "splice_acceptor_variant", "splice_donor_variant", "splice_region_variant", "stop_gained", "stop_lost", "stop_retained_variant", "synonymous_codon"],
            selection: "multi"
        }
    ],
    bam: [
        {
            name: "view",
            text: "View",
            values: ["view_as_pairs", "show_soft-clipped_bases"],
            selection: "multi"
        }
    ]
};

GENE_BIOTYPE_COLORS = {
    "3prime_overlapping_ncrna": "Orange",
    "ambiguous_orf": "SlateBlue",
    "antisense": "SteelBlue",
    "disrupted_domain": "YellowGreen",
    "IG_C_gene": "#FF7F50",
    "IG_D_gene": "#FF7F50",
    "IG_J_gene": "#FF7F50",
    "IG_V_gene": "#FF7F50",
    "lincRNA": "#8b668b",
    "miRNA": "#8b668b",
    "misc_RNA": "#8b668b",
    "Mt_rRNA": "#8b668b",
    "Mt_tRNA": "#8b668b",
    "ncrna_host": "Fuchsia",
    "nonsense_mediated_decay": "seagreen",
    "non_coding": "orangered",
    "non_stop_decay": "aqua",
    "polymorphic_pseudogene": "#666666",
    "processed_pseudogene": "#666666",
    "processed_transcript": "#0000ff",
    "protein_coding": "#a00000",
    "pseudogene": "#666666",
    "retained_intron": "goldenrod",
    "retrotransposed": "lightsalmon",
    "rRNA": "indianred",
    "sense_intronic": "#20B2AA",
    "sense_overlapping": "#20B2AA",
    "snoRNA": "#8b668b",
    "snRNA": "#8b668b",
    "transcribed_processed_pseudogene": "#666666",
    "transcribed_unprocessed_pseudogene": "#666666",
    "unitary_pseudogene": "#666666",
    "unprocessed_pseudogene": "#666666",
    "": "orangered",
    "other": "#000000"
};


SNP_BIOTYPE_COLORS = {
    "2KB_upstream_variant": "#a2b5cd",
    "5KB_upstream_variant": "#a2b5cd",
    "500B_downstream_variant": "#a2b5cd",
    "5KB_downstream_variant": "#a2b5cd",
    "3_prime_UTR_variant": "#7ac5cd",
    "5_prime_UTR_variant": "#7ac5cd",
    "coding_sequence_variant": "#458b00",
    "complex_change_in_transcript": "#00fa9a",
    "frameshift_variant": "#ff69b4",
    "incomplete_terminal_codon_variant": "#ff00ff",
    "inframe_codon_gain": "#ffd700",
    "inframe_codon_loss": "#ffd700",
    "initiator_codon_change": "#ffd700",
    "non_synonymous_codon": "#ffd700",
    "intergenic_variant": "#636363",
    "intron_variant": "#02599c",
    "mature_miRNA_variant": "#458b00",
    "nc_transcript_variant": "#32cd32",
    "splice_acceptor_variant": "#ff7f50",
    "splice_donor_variant": "#ff7f50",
    "splice_region_variant": "#ff7f50",
    "stop_gained": "#ff0000",
    "stop_lost": "#ff0000",
    "stop_retained_variant": "#76ee00",
    "synonymous_codon": "#76ee00",
    "other": "#000000"
};


SEQUENCE_COLORS = {A: "#009900", C: "#0000FF", G: "#857A00", T: "#aa0000", N: "#555555"};

SAM_FLAGS = [
    ["read paired", 0x1],
    ["read mapped in proper pair", 0x2],
    ["read unmapped", 0x4],
    ["mate unmapped", 0x8],
    ["read reverse strand", 0x10],
    ["mate reverse strand", 0x20],
    ["first in pair", 0x40],
    ["second in pair", 0x80],
    ["not primary alignment", 0x100],
    ["read fails platform/vendor quality checks", 0x200],
    ["read is PCR or optical duplicate", 0x400]
];


FEATURE_TYPES = {

    //methods
    formatTitle: function (str) {
        var s = str;
        if(str){
            str.replace(/_/gi, " ");
            s = s.charAt(0).toUpperCase() + s.slice(1);
        }
        return s;
    },
    getTipCommons: function (f) {
        var strand = (f.strand != null) ? f.strand : "NA";
        return 'start-end:&nbsp;<span class="emph">' + f.start + '-' + f.end + '</span><br>' +
            'strand:&nbsp;<span class="emph">' + strand + '</span><br>' +
            'length:&nbsp;<span class="info">' + (f.end - f.start + 1).toString().replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,") + '</span><br>';
    },

    //items
    sequence: {
        color: SEQUENCE_COLORS
    },
    undefined: {
        getLabel: function (f) {
            var str = "";
            str += f.chromosome + ":" + f.start + "-" + f.end;
            return str;
        },
        getTipTitle: function (f) {
            return " ";
        },
        getTipText: function (f) {
            return " ";
        },
        getColor: function (f) {
            return "grey";
        },
//		infoWidgetId: "id",
        height: 10
//		histogramColor:"lightblue"
    },
    gene: {
        label: function (f) {
            var name = (f.name != null) ? f.name : f.id;
            var str = "";
            str += (f.strand < 0 || f.strand == '-') ? "<" : "";
            str += " " + name + " ";
            str += (f.strand > 0 || f.strand == '+') ? ">" : "";
            if (f.biotype != null && f.biotype != '') {
                str += " [" + f.biotype + "]";
            }
            return str;
        },
        tooltipTitle: function (f) {
            var name = (f.name != null) ? f.name : f.id;
            return FEATURE_TYPES.formatTitle('Gene') +' - <span class="ok">' + name + '</span>';
        },
        tooltipText: function (f) {
            var color = GENE_BIOTYPE_COLORS[f.biotype];
            return    'id:&nbsp;<span class="ssel">' + f.id + '</span><br>' +
                'biotype:&nbsp;<span class="emph" style="color:' + color + ';">' + f.biotype + '</span><br>' +
                FEATURE_TYPES.getTipCommons(f) +
                'source:&nbsp;<span class="ssel">' + f.source + '</span><br><br>' +
                'description:&nbsp;<span class="emph">' + f.description + '</span><br>';
        },
        color: function (f) {
            return GENE_BIOTYPE_COLORS[f.biotype];
        },
        infoWidgetId: "id",
        height: 4,
        histogramColor: "lightblue"
    },
//	geneorange:{
//		getLabel: function(f){
//			var str = "";
//			str+= (f.strand < 0) ? "<" : "";
//			str+= " "+f.name+" ";
//			str+= (f.strand > 0) ? ">" : "";
//			str+= " ["+f.biotype+"]";
//			return str;
//		},
//		getTipTitle: function(f){
//			return FEATURE_TYPES.formatTitle(f.featureType) +
//			' - <span class="ok">'+f.name+'</span>';
//		},
//		getTipText: function(f){
//			var color = GENE_BIOTYPE_COLORS[f.biotype];
//			return	'Ensembl&nbsp;ID:&nbsp;<span class="ssel">'+f.id+'</span><br>'+
//			'biotype:&nbsp;<span class="emph" style="color:'+color+';">'+f.biotype+'</span><br>'+
//			'description:&nbsp;<span class="emph">'+f.description+'</span><br>'+
//			FEATURE_TYPES.getTipCommons(f)+
//			'source:&nbsp;<span class="ssel">'+f.source+'</span><br>';
//		},
//		getColor: function(f){
//			return GENE_BIOTYPE_COLORS[f.biotype];
//		},
//		infoWidgetId: "id",
//		height:4,
//		histogramColor:"lightblue"
//	},
    transcript: {
        label: function (f) {
            var name = (f.name != null) ? f.name : f.id;
            var str = "";
            str += (f.strand < 0) ? "<" : "";
            str += " " + name + " ";
            str += (f.strand > 0) ? ">" : "";
            if (f.biotype != null && f.biotype != '') {
                str += " [" + f.biotype + "]";
            }
            return str;
        },
        tooltipTitle: function (f) {
            var name = (f.name != null) ? f.name : f.id;
            return FEATURE_TYPES.formatTitle('Transcript') +
                ' - <span class="ok">' + name + '</span>';
        },
        tooltipText: function (f) {
            var color = GENE_BIOTYPE_COLORS[f.biotype];
            return    'id:&nbsp;<span class="ssel">' + f.id + '</span><br>' +
                'biotype:&nbsp;<span class="emph" style="color:' + color + ';">' + f.biotype + '</span><br>' +
                'description:&nbsp;<span class="emph">' + f.description + '</span><br>' +
                FEATURE_TYPES.getTipCommons(f);
        },
        color: function (f) {
            return GENE_BIOTYPE_COLORS[f.biotype];
        },
        infoWidgetId: "id",
        height: 1,
        histogramColor: "lightblue"
    },
    exon: {//not yet
        label: function (f) {
            var name = (f.name != null) ? f.name : f.id;
            return name;
        },
        tooltipTitle: function (f) {
            var name = (f.name != null) ? f.name : f.id;
            if (name == null) {
                name = ''
            }
            return FEATURE_TYPES.formatTitle('Exon') + ' - <span class="ok">' + name + '</span>';
        },
        tooltipText: function (e, t) {
            var ename = (e.name != null) ? e.name : e.id;
            var tname = (t.name != null) ? t.name : t.id;
            var color = GENE_BIOTYPE_COLORS[t.biotype];
            return    'transcript name:&nbsp;<span class="ssel">' + t.name + '</span><br>' +
                'transcript Ensembl&nbsp;ID:&nbsp;<span class="ssel">' + t.id + '</span><br>' +
                'transcript biotype:&nbsp;<span class="emph" style="color:' + color + ';">' + t.biotype + '</span><br>' +
                'transcript description:&nbsp;<span class="emph">' + t.description + '</span><br>' +
                'transcript start-end:&nbsp;<span class="emph">' + t.start + '-' + t.end + '</span><br>' +
                'exon start-end:&nbsp;<span class="emph">' + e.start + '-' + e.end + '</span><br>' +
                'strand:&nbsp;<span class="emph">' + t.strand + '</span><br>' +
                'length:&nbsp;<span class="info">' + (e.end - e.start + 1).toString().replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,") + '</span><br>';
        },
        color: function (f) {
            return "black";
        },
        infoWidgetId: "id",
        height: 5,
        histogramColor: "lightblue"
    },
    snp: {
        label: function (f) {
            return ('name' in f) ? f.name : f.id;
        },
        tooltipTitle: function (f) {
            var name = (f.name != null) ? f.name : f.id;
            return f.featureType.toUpperCase() + ' - <span class="ok">' + name + '</span>';
        },
        tooltipText: function (f) {
            return 'alleles:&nbsp;<span class="ssel">' + f.alleleString + '</span><br>' +
                FEATURE_TYPES.getTipCommons(f) +
                'source:&nbsp;<span class="ssel">' + f.source + '</span><br>';

        },
        color: 'lightblue',
        infoWidgetId: "id",
        height: 8,
        histogramColor: "orange",
        handlers: {
            'feature:mouseover': function (e) {
                console.log(e)
            }
//            'feature:click': function (event) {
//                new SnpInfoWidget(null, genomeViewer.species).draw(event);
//            }
        }
    },
    file: {
        getLabel: function (f) {
            var str = "";
            str += f.label;
            return str;
        },
        getTipTitle: function (f) {
            return FEATURE_TYPES.formatTitle(f.featureType);
        },
        getTipText: function (f) {
            return FEATURE_TYPES.getTipCommons(f);
        },
        getColor: function (f) {
            return "black";
        },
        height: 8,
        histogramColor: "orange"
    },
    vcf: {
        label: function (f) {
            return f.id;
            try {
                var fields = f.sampleData.split("\t");
            } catch (e) {
                //Uncaught TypeError: Cannot call method 'split' of undefined
                console.log(e)
                debugger
            }

            if (fields.length > 10 || fields.length == 9)
                return f.id + " " + f.ref + "/" + f.alt + "";
            else {
                var gt = fields[9].split(":")[0];
                if (gt.indexOf(".") != -1 || gt.indexOf("-") != -1)
                    return gt;
                var label = "";
                var alt = f.alt.split(",");
                if (gt.charAt(0) == '0')
                    label = f.ref;
                else {
                    var pos = gt.charAt(0) - 1
                    label = alt[pos]
                }
                label += gt.charAt(1)
                if (gt.charAt(2) == '0')
                    label += f.ref;
                else {
                    var pos = gt.charAt(2) - 1
                    label += alt[pos]
                }

                return label;
            }
        },
        tooltipTitle: function (f) {
            return 'VCF variant - <span class="ok">' + f.id + '</span>';
        },
        tooltipText: function (f) {
            return 'alleles (ref/alt):&nbsp;<span class="emph">' + f.reference + "/" + f.alternate + '</span><br>' +
                'quality:&nbsp;<span class="emph">' + f.quality + '</span><br>' +
                'filter:&nbsp;<span class="emph">' + f.filter + '</span><br>' +
                FEATURE_TYPES.getTipCommons(f);
        },
        getColor: function (f) {
            return "black";
        },
        infoWidgetId: "id",
        height: 8,
        histogramColor: "gray"
    },
    gff2: {
        getLabel: function (f) {
            var str = "";
            str += f.label;
            return str;
        },
        getTipTitle: function (f) {
            return f.featureType.toUpperCase() +
                ' - <span class="ok">' + f.label + '</span>';
        },
        getTipText: function (f) {
            return 'score:&nbsp;<span class="emph">' + f.score + '</span><br>' +
                'frame:&nbsp;<span class="emph">' + f.frame + '</span><br>' +
                FEATURE_TYPES.getTipCommons(f);
        },
        getColor: function (f) {
            return "black";
        },
        height: 8,
        histogramColor: "gray"
    },
    gff3: {
        label: function (f) {
            var str = "";
            str += f.label;
            return str;
        },
        tooltipTitle: function (f) {
            return f.featureType.toUpperCase() +
                ' - <span class="ok">' + f.label + '</span>';
        },
        tooltipText: function (f) {
            return 'score:&nbsp;<span class="emph">' + f.score + '</span><br>' +
                'frame:&nbsp;<span class="emph">' + f.frame + '</span><br>' +
                FEATURE_TYPES.getTipCommons(f);
        },
        color: function (f) {
            return "black";
        },
        height: 8,
        histogramColor: "gray",
        infoWidgetId: 'id',
        handlers: {
            'feature:mouseover': function (e) {
                console.log(e)
            },
            'feature:click': function (e) {
                console.log(e)
            }
        }
    },
    gtf: {
        label: function (f) {
            var str = "";
            str += f.label;
            return str;
        },
        tooltipTitle: function (f) {
            return f.featureType.toUpperCase() +
                ' - <span class="ok">' + f.label + '</span>';
        },
        tooltipText: function (f) {
            return 'score:&nbsp;<span class="emph">' + f.score + '</span><br>' +
                'frame:&nbsp;<span class="emph">' + f.frame + '</span><br>' +
                FEATURE_TYPES.getTipCommons(f);
        },
        color: function (f) {
            return "black";
        },
        height: 8,
        histogramColor: "gray",
        infoWidgetId: 'id',
        handlers: {
            'feature:mouseover': function (e) {
                console.log(e)
            },
            'feature:click': function (e) {
                console.log(e)
            }
        }
    },
    bed: {
        label: function (f) {
            var str = "";
            str += f.label;
            return str;
        },
        tooltipTitle: function (f) {
            return FEATURE_TYPES.formatTitle(f.featureType);
        },
        tooltipText: function (f) {
            return FEATURE_TYPES.getTipCommons(f);
        },
        color: function (f) {
            //XXX convert RGB to Hex
            var rgbColor = new Array();
            rgbColor = f.itemRgb.split(",");
            var hex = function (x) {
                var hexDigits = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"];
                return isNaN(x) ? "00" : hexDigits[(x - x % 16) / 16] + hexDigits[x % 16];
            };
            var hexColor = hex(rgbColor[0]) + hex(rgbColor[1]) + hex(rgbColor[2]);
            return "#" + hexColor;
        },
        height: 8,
        histogramColor: "orange",
        infoWidgetId: 'id',
        handlers: {
            'feature:mouseover': function (e) {
                console.log(e)
            },
            'feature:click': function (e) {
                console.log(e)
            }
        }
    },
    bam: {
        explainFlags: function (flags) {
            var summary = '<div style="background:#FFEF93;font-weight:bold;margin:0 15px 0 0;">flags : <span class="ssel">' + flags + '</span></div>';
            for (var i = 0; i < SAM_FLAGS.length; i++) {
                if (SAM_FLAGS[i][1] & flags) {
                    summary += SAM_FLAGS[i][0] + "<br>";
                }
            }
            return summary;
        },
        label: function (f) {
            return  "bam  " + f.chromosome + ":" + f.start + "-" + f.end;
        },
        tooltipTitle: function (f) {
            return FEATURE_TYPES.formatTitle(f.featureType) + ' - <span class="ok">' + f.name + '</span>';
        },
        tooltipText: function (f) {
            f.strand = FEATURE_TYPES.bam.strand(f);
            var one = 'cigar:&nbsp;<span class="ssel">' + f.cigar + '</span><br>' +
                'insert size:&nbsp;<span class="ssel">' + f.inferredInsertSize + '</span><br>' +
                FEATURE_TYPES.getTipCommons(f) + '<br>' +
                this.explainFlags(f.flags);

            var three = '<div style="background:#FFEF93;font-weight:bold;">attributes</div>';
            delete f.attributes["BQ"];//for now because is too long
            for (var key in f.attributes) {
                three += key + ":" + f.attributes[key] + "<br>";
            }
            var style = "background:#FFEF93;font-weight:bold;";
            return '<div style="float:left">' + one + '</div>' +
                '<div style="float:right">' + three + '</div>';
        },
        color: function (f, chr) {
            if (f.mateReferenceName != chr) {
                return "lightgreen";
            }
            return (parseInt(f.flags) & (0x10)) == 0 ? "DarkGray" : "LightGray";
            /**/
        },
        strokeColor: function (f) {
            if (this.mateUnmappedFlag(f)) {
                return "tomato"
            }
            return (parseInt(f.flags) & (0x10)) == 0 ? "LightGray" : "DarkGray";
        },
        strand: function (f) {
            return (parseInt(f.flags) & (0x10)) == 0 ? "Forward" : "Reverse";
        },
        readPairedFlag: function (f) {
            return (parseInt(f.flags) & (0x1)) == 0 ? false : true;
        },
        firstOfPairFlag: function (f) {
            return (parseInt(f.flags) & (0x40)) == 0 ? false : true;
        },
        mateUnmappedFlag: function (f) {
            return (parseInt(f.flags) & (0x8)) == 0 ? false : true;
        },
        infoWidgetId: "id",
        height: 8,
        histogramColor: "grey"
    },
    das: {
        label: function (f) {
            var str = "";
            str += f.id;
            return str;
        },
        tooltipTitle: function (f) {
            return FEATURE_TYPES.formatTitle(f.featureType) + ('id' in f) ? f.id : '';
        },
        tooltipText: function (f) {
            return FEATURE_TYPES.getTipCommons(f);
        },
        color: function (f) {
            return "lightblue";
        },
        height: 8,
        histogramColor: "orange",
        infoWidgetId: 'id',
        handlers: {
            'feature:mouseover': function (e) {
                console.log(e)
            },
            'feature:click': function (e) {
                console.log(e)
            }
        }
    }
};

