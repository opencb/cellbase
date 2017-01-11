/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.server.grpc;

import org.bson.Document;
import org.opencb.biodata.models.core.protobuf.GeneModel;
import org.opencb.biodata.models.core.protobuf.RegulatoryRegionModel;
import org.opencb.biodata.models.core.protobuf.TranscriptModel;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.biodata.models.variant.protobuf.VariantAnnotationProto;
import org.opencb.biodata.models.variant.protobuf.VariantProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by swaathi on 03/03/16.
 */
public class ProtoConverterUtils {

    public static GeneModel.Gene createGene(Document document) {
        GeneModel.Gene.Builder builder = GeneModel.Gene.newBuilder()
                .setId((String) document.getOrDefault("id", ""))
                .setName((String) document.getOrDefault("name", ""))
                .setChromosome((String) document.getOrDefault("chromosome", ""))
                .setStart(document.getInteger("start", -1))
                .setEnd(document.getInteger("end", -1))
                .setBiotype((String) document.getOrDefault("biotype", ""))
                .setStatus((String) document.getOrDefault("status", ""))
                .setStrand((String) document.getOrDefault("strand", ""))
                .setSource((String) document.getOrDefault("source", ""))
                .setDescription((String) document.getOrDefault("description", ""));

        ArrayList transcripts = document.get("transcripts", ArrayList.class);
        if (transcripts != null) {
            for (Object transcript : transcripts) {
                builder.addTranscripts(createTranscript((Document) transcript));
            }
        }
        return builder.build();
    }

    public static TranscriptModel.Transcript createTranscript(Document document) {
        TranscriptModel.Transcript.Builder builder = TranscriptModel.Transcript.newBuilder()
                .setId((String) document.getOrDefault("id", ""))
                .setName((String) document.getOrDefault("name", ""))
                .setBiotype((String) document.getOrDefault("biotype", ""))
                .setStatus((String) document.getOrDefault("status", ""))
                .setChromosome((String) document.getOrDefault("chromosome", ""))
                .setStart(document.getInteger("start", -1))
                .setEnd(document.getInteger("end", -1))
                .setStrand((String) document.getOrDefault("strand", ""))
                .setGenomicCodingStart(document.getInteger("genomicCodingStart", -1))
                .setGenomicCodingEnd(document.getInteger("genomicCodingEnd", -1))
                .setCdnaCodingStart(document.getInteger("cdnaCodingStart", -1))
                .setCdnaCodingEnd(document.getInteger("cdnaCodingEnd", -1))
                .setCdsLength(document.getInteger("cdsLength", -1))
                .setProteinId((String) document.getOrDefault("proteinID", ""))
                .setProteinSequence((String) document.getOrDefault("proteinID", ""))
                .setCdnaSequence((String) document.getOrDefault("cDnaSequence", ""));

        ArrayList xrefs = document.get("xrefs", ArrayList.class);
        if (xrefs != null) {
            for (Object xref : xrefs) {
                builder.addXrefs(createXref((Document) xref));
            }
        }

        ArrayList tfbs = document.get("tfbs", ArrayList.class);
        if (tfbs != null) {
            for (Object obj : tfbs) {
                builder.addTfbs(createTranscriptTfbs((Document) obj));
            }
        }

        ArrayList exons = document.get("exons", ArrayList.class);
        if (exons != null) {
            for (Object exon : exons) {
                builder.addExons(createExon((Document) exon));
            }
        }

        ArrayList annotationFlags = document.get("annotationFlags", ArrayList.class);
        if (annotationFlags != null) {
            for (Object annotationFlag: annotationFlags) {
                builder.addAnnotationFlags((String) annotationFlag);
            }
        }
        return builder.build();
    }

    public static TranscriptModel.Xref createXref(Document document) {
        TranscriptModel.Xref.Builder xrefBuilder = TranscriptModel.Xref.newBuilder()
                .setId((String) document.getOrDefault("id", ""))
                .setDbName((String) document.getOrDefault("dbName", ""))
                .setDbDisplayName((String) document.getOrDefault("dbDisplayName", ""));
        return xrefBuilder.build();
    }

    public static TranscriptModel.Exon createExon(Document document) {
        TranscriptModel.Exon.Builder exonBuilder = TranscriptModel.Exon.newBuilder()
                .setId((String) document.getOrDefault("id", ""))
                .setChromosome((String) document.getOrDefault("chromosome", ""))
                .setStart(document.getInteger("start", -1))
                .setEnd(document.getInteger("end", -1))
                .setStrand((String) document.getOrDefault("strand", ""))
                .setGenomicCodingStart(document.getInteger("genomicCodingStart", -1))
                .setGenomicCodingEnd(document.getInteger("genomicCodingEnd", -1))
                .setCdnaCodingStart(document.getInteger("cdnaCodingStart", -1))
                .setCdnaCodingEnd(document.getInteger("cdnaCodingEnd", -1))
                .setCdsStart(document.getInteger("cdsStart", -1))
                .setCdsEnd(document.getInteger("cdsEnd", -1))
                .setPhase(document.getInteger("phase", -1))
                .setExonNumber(document.getInteger("exonNumber", -1))
                .setSequence((String) document.getOrDefault("sequence", ""));
        return  exonBuilder.build();
    }

    public static TranscriptModel.TranscriptTfbs createTranscriptTfbs(Document document) {
        TranscriptModel.TranscriptTfbs.Builder transcriptTfbsBuilder = TranscriptModel.TranscriptTfbs.newBuilder()
                .setTfName((String) document.getOrDefault("tfName", ""))
                .setPwm((String) document.getOrDefault("pwm", ""))
                .setChromosome((String) document.getOrDefault("chromosome", ""))
                .setStart(document.getInteger("start", -1))
                .setEnd(document.getInteger("end", -1))
                .setStrand((String) document.getOrDefault("strand", ""))
                .setRelativeStart(document.getInteger("relativeStart", -1))
                .setRelativeEnd(document.getInteger("relativeEnd", -1))
                .setScore(((Double)document.getOrDefault("score", -1.0)).floatValue());
        return  transcriptTfbsBuilder.build();
    }

    public static RegulatoryRegionModel.RegulatoryRegion createRegulatoryRegion(Document document) {
        RegulatoryRegionModel.RegulatoryRegion.Builder builder = RegulatoryRegionModel.RegulatoryRegion.newBuilder()
                .setId((String) document.getOrDefault("id", ""))
                .setChromosome((String) document.getOrDefault("chromosome", ""))
                .setSource((String) document.getOrDefault("source", ""))
                .setFeatureType((String) document.getOrDefault("featureType", ""))
                .setStart(document.getInteger("start", -1))
                .setEnd(document.getInteger("end", -1))
                .setScore((String) document.getOrDefault("score", ""))
                .setStrand((String) document.getOrDefault("strand", ""))
                .setFrame((String) document.getOrDefault("frame", ""))
                .setItemRGB((String) document.getOrDefault("itemRGB", ""))
                .setName((String) document.getOrDefault("name", ""))
                .setFeatureClass((String) document.getOrDefault("featureClass", ""))
                .setAlias((String) document.getOrDefault("alias", ""));

        List<String> cellTypes = document.get("cellTypes", ArrayList.class);
        if (cellTypes != null && cellTypes.size() > 0) {
            builder.addAllCellTypes(cellTypes);
        }
        builder.setMatrix((String) document.getOrDefault("matrix", ""));
        return builder.build();
    }

    public static VariantProto.Variant createVariant(Document document) {
        VariantProto.Variant.Builder builder = VariantProto.Variant.newBuilder()
                .setChromosome((String) document.getOrDefault("chromosome", ""))
                .setStart(document.getInteger("start", -1))
                .setEnd(document.getInteger("end", -1))
                .setReference((String) document.getOrDefault("reference", ""))
                .setAlternate((String) document.getOrDefault("alternate", ""))
                .setStrand((String) document.getOrDefault("strand", ""));

        List<String> names = document.get("names", ArrayList.class);
        if (names != null && names.size() > 0) {
            builder.addAllNames(names);
        }
        builder.setLength(document.getInteger("length", -1))
                .setType((VariantProto.VariantType) document.get("type"));

//        ArrayList studies = document.get("studies", ArrayList.class);
//        if (studies != null) {
//            for (Object study : studies) {
//                builder.addStudies()
//            }
//        }

        return builder.build();
    }

//    public static VariantProto.VariantSourceEntry createVariantSourceEntry(Document document) {
//        VariantProto.VariantSourceEntry.Builder builder = VariantProto.VariantSourceEntry.newBuilder()
//                .setStudyId((String) document.getOrDefault("studyId", ""))
//    }

    public static VariantAnnotationProto.VariantAnnotation createVariantAnnotation(VariantAnnotation annotation) {
        VariantAnnotationProto.VariantAnnotation.Builder builder = VariantAnnotationProto.VariantAnnotation.newBuilder()
                .setChromosome(annotation.getChromosome())
                .setStart(annotation.getStart())
                .setReference(annotation.getReference())
                .setAlternate(annotation.getAlternate())
                .setId(annotation.getId());
        List<Xref> xrefs = annotation.getXrefs();
        if (xrefs != null) {
            for (Xref xref : xrefs) {
                VariantAnnotationProto.VariantAnnotation.Xref.Builder xrefBuilder =
                        VariantAnnotationProto.VariantAnnotation.Xref.newBuilder()
                                .setId(xref.getId())
                                .setSource(xref.getSource());
                builder.addXrefs(xrefBuilder.build());
            }
        }

        List<String> hgvs = annotation.getHgvs();
        if (hgvs != null && hgvs.size() > 0) {
            builder.addAllHgvs(hgvs);
        }

        List<ConsequenceType> consequenceTypes = annotation.getConsequenceTypes();
        if (consequenceTypes != null) {
            for (ConsequenceType type : consequenceTypes) {
                builder.addConsequenceTypes(createConsequenceType(type));
            }
        }

        List<PopulationFrequency> populationFrequencies = annotation.getPopulationFrequencies();
        if (populationFrequencies != null) {
            for (PopulationFrequency pf: populationFrequencies) {
                VariantAnnotationProto.PopulationFrequency.Builder popFreqBuilder =
                        VariantAnnotationProto.PopulationFrequency.newBuilder()
                        .setStudy(pf.getStudy())
                        .setPopulation(pf.getPopulation())
                        .setRefAllele(pf.getRefAllele())
                        .setAltAllele(pf.getAltAllele())
                        .setRefAlleleFreq(pf.getRefAlleleFreq())
                        .setAltAlleleFreq(pf.getAltAlleleFreq())
                        .setRefHomGenotypeFreq(pf.getRefHomGenotypeFreq())
                        .setHetGenotypeFreq(pf.getHetGenotypeFreq())
                        .setAltHomGenotypeFreq(pf.getAltHomGenotypeFreq());
                builder.addPopulationFrequencies(popFreqBuilder.build());
            }
        }
        List<Score> conservation = annotation.getConservation();
        if (conservation != null) {
            for (Score score: conservation) {
                builder.addConservation(createVariantAnnotationScore(score));
            }
        }
        return builder.build();
    }

    public static VariantAnnotationProto.ConsequenceType createConsequenceType(ConsequenceType type) {
        VariantAnnotationProto.ConsequenceType.Builder builder= VariantAnnotationProto.ConsequenceType.newBuilder()
                .setGeneName(type.getGeneName())
                .setEnsemblGeneId(type.getEnsemblGeneId())
                .setEnsemblTranscriptId(type.getEnsemblTranscriptId())
                .setStrand(type.getStrand())
                .setBiotype(type.getBiotype())
                .setCDnaPosition(type.getCdnaPosition())
                .setCdsPosition(type.getCdsPosition())
                .setCodon(type.getCodon())
                .setProteinVariantAnnotation(createProteinVariantAnnotation(type.getProteinVariantAnnotation()));
        List<SequenceOntologyTerm> sequenceOntologyTerms = type.getSequenceOntologyTerms();
        if (sequenceOntologyTerms != null) {
            for (SequenceOntologyTerm so : sequenceOntologyTerms) {
                VariantAnnotationProto.SequenceOntologyTerm.Builder soBuilder =
                        VariantAnnotationProto.SequenceOntologyTerm.newBuilder()
                                .setAccession(so.getAccession())
                                .setName(so.getName());
                builder.addSequenceOntologyTerms(soBuilder.build());
            }
        }
        return builder.build();
    }

    public static VariantAnnotationProto.ProteinVariantAnnotation createProteinVariantAnnotation(
            ProteinVariantAnnotation proteinAnnotation) {
        VariantAnnotationProto.ProteinVariantAnnotation.Builder builder =
                VariantAnnotationProto.ProteinVariantAnnotation.newBuilder()
                .setUniprotAccession(proteinAnnotation.getUniprotAccession())
                .setUniprotName(proteinAnnotation.getUniprotName())
                .setPosition(proteinAnnotation.getPosition())
                .setReference(proteinAnnotation.getReference())
                .setAlternate(proteinAnnotation.getAlternate())
                .setUniprotVariantId(proteinAnnotation.getUniprotVariantId())
                .setFunctionalDescription(proteinAnnotation.getFunctionalDescription());

        List<Score> substitutionScores = proteinAnnotation.getSubstitutionScores();
        if (substitutionScores != null) {
            for (Score score: substitutionScores) {
                builder.addSubstitutionScores(createVariantAnnotationScore(score));
            }
        }

        List<String> keywords = proteinAnnotation.getKeywords();
        if (keywords != null && keywords.size() > 0) {
            builder.addAllKeywords(keywords);
        }

        List<ProteinFeature> features = proteinAnnotation.getFeatures();
        if (features != null) {
            for (ProteinFeature feature : features) {
                VariantAnnotationProto.ProteinFeature.Builder featureBuilder =
                        VariantAnnotationProto.ProteinFeature.newBuilder()
                        .setId(feature.getId())
                        .setStart(feature.getStart())
                        .setEnd(feature.getEnd())
                        .setType(feature.getType())
                        .setDescription(feature.getDescription());
                builder.addFeatures(featureBuilder.build());
            }
        }
        return builder.build();
    }

    public static VariantAnnotationProto.Score createVariantAnnotationScore(Score score) {
        VariantAnnotationProto.Score.Builder builder = VariantAnnotationProto.Score.newBuilder()
                .setScore(score.getScore())
                .setSource(score.getSource())
                .setDescription(score.getDescription());
        return builder.build();
    }
}
