package org.opencb.cellbase.grpc.server;

import org.bson.Document;
import org.opencb.biodata.models.core.protobuf.GeneModel;
import org.opencb.biodata.models.core.protobuf.RegulatoryRegionModel;
import org.opencb.biodata.models.core.protobuf.TranscriptModel;
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
                .setStart(document.getInteger("start"))
                .setEnd(document.getInteger("end"))
                .setBiotype((String) document.getOrDefault("biotype", ""))
                .setStatus((String) document.getOrDefault("status", ""))
                .setStrand((String) document.getOrDefault("strand", ""))
                .setSource((String) document.getOrDefault("source", ""));

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
                .setId(document.getString("id"))
                .setName(document.getString("name"))
                .setBiotype(document.getString("biotype"))
                .setStatus(document.getString("status"))
                .setChromosome(document.getString("chromosome"))
                .setStart(document.getInteger("start"))
                .setEnd(document.getInteger("end"))
                .setCdnaSequence((String) document.getOrDefault("cDnaSequence", ""));

        ArrayList xrefs = document.get("xrefs", ArrayList.class);
        if (xrefs != null) {
            for (Object xref : xrefs) {
                builder.addXrefs(createXref((Document) xref));
            }
        }

        ArrayList exons = document.get("exons", ArrayList.class);
        if (exons != null) {
            for (Object exon : exons) {
                builder.addExons(createExon((Document) exon));
            }
        }
        return builder.build();
    }

    public static TranscriptModel.Xref createXref(Document document) {
        TranscriptModel.Xref.Builder xrefBuilder = TranscriptModel.Xref.newBuilder()
                .setId(document.getString("id"))
                .setDbName(document.getString("dbName"))
                .setDbDisplayName(document.getString("dbDisplayName"));
        return xrefBuilder.build();
    }

    public static TranscriptModel.Exon createExon(Document document) {
        TranscriptModel.Exon.Builder exonBuilder = TranscriptModel.Exon.newBuilder()
                .setId(document.getString("id"))
                .setChromosome(document.getString("chromosome"))
                .setStart(document.getInteger("start"))
                .setEnd(document.getInteger("end"))
                .setStrand(document.getString("strand"))
                .setExonNumber(document.getInteger("exonNumber"))
                .setSequence(document.getString("sequence"));
        return  exonBuilder.build();
    }

    public static TranscriptModel.TranscriptTfbs createTranscriptTfbs(Document document) {
        TranscriptModel.TranscriptTfbs.Builder transcriptTfbsBuilder = TranscriptModel.TranscriptTfbs.newBuilder()
                .setTfName(document.getString("tfName"))
                .setPwm(document.getString("pwm"))
                .setChromosome(document.getString("chromosome"))
                .setStart(document.getInteger("start"))
                .setEnd(document.getInteger("end"))
                .setStrand(document.getString("strand"))
                .setRelativeStart(document.getInteger("relativeStart"))
                .setRelativeEnd(document.getInteger("relativeEnd"));
//                .setScore(document.getDouble("score"));
        return  transcriptTfbsBuilder.build();
    }

    public static RegulatoryRegionModel.RegulatoryRegion createRegulatoryRegion(Document document) {
        RegulatoryRegionModel.RegulatoryRegion.Builder builder = RegulatoryRegionModel.RegulatoryRegion.newBuilder()
                .setId((String) document.getOrDefault("id", ""))
                .setChromosome((String) document.getOrDefault("chromosome", ""))
                .setSource((String) document.getOrDefault("source", ""))
                .setFeatureType((String) document.getOrDefault("featureType", ""))
                .setStart(document.getInteger("start", 0))
                .setEnd(document.getInteger("end", 0))
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
                .setChromosome(document.getString("chromosome"))
                .setStart(document.getInteger("start"))
                .setEnd(document.getInteger("end"))
                .setReference((String) document.getOrDefault("reference", ""))
                .setReference((String) document.getOrDefault("alternate", ""))
                .setStrand(document.getString("strand"));

        return builder.build();
    }
}
