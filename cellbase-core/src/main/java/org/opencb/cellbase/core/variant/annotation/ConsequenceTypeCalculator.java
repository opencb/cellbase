package org.opencb.cellbase.core.variant.annotation;

import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.MiRNAGene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.annotation.ConsequenceType;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.regulatory.RegulatoryRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by fjlopez on 19/06/15.
 */
public abstract class ConsequenceTypeCalculator {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected HashSet<String> SoNames = new HashSet<>();
    protected ConsequenceType consequenceType;
    protected Gene gene;
    protected Transcript transcript;
    protected GenomicVariant variant;
    protected Map<String,MiRNAGene> miRNAMap;

    public List<ConsequenceType> run(GenomicVariant variant, List<Gene> geneList, Map<String,MiRNAGene> miRNAMap,
                                     List<RegulatoryRegion> regulatoryRegionList) { return null; }

    protected void solveRegulatoryRegions(List<RegulatoryRegion> regulatoryRegionList, List<ConsequenceType> consequenceTypeList) {
        if(!regulatoryRegionList.isEmpty()) {
            consequenceTypeList.add(new ConsequenceType(VariantAnnotationUtils.REGULATORY_REGION_VARIANT));
            boolean TFBSFound=false;
            for(int i=0; (i<regulatoryRegionList.size() && !TFBSFound); i++) {
                TFBSFound = regulatoryRegionList.get(i).getType().equals("TF_binding_site") ||
                        regulatoryRegionList.get(i).getType().equals("TF_binding_site_motif");
            }
            if(TFBSFound) {
                consequenceTypeList.add(new ConsequenceType(VariantAnnotationUtils.TF_BINDING_SITE_VARIANT));
            }
        }
    }

}
