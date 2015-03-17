package org.opencb.cellbase.core.common.variation;

import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.regulatory.RegulatoryRegion;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 10/31/13
 * Time: 11:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class GenomicVariantEffectPredictor {
    //	private static int FEATURE_MAP_CHUNK_SIZE = 400;

    /** Options **/
    private boolean showFeatures = false;
    private boolean showVariation = true;
    private boolean showRegulatory = true;
    private boolean showDiseases = true;

//    private DBAdaptorFactory dbAdaptorFact;
//    private GenomeSequenceDBAdaptor sequenceDbAdaptor;
//    private SnpDBAdaptor snpDbAdaptor;

    protected static ResourceBundle resourceBundle;
    private static Map<String, ConsequenceType> consequenceTypeMap;

    static {
        resourceBundle = ResourceBundle.getBundle("org.opencb.cellbase.core.variation.variation_consequences");
        consequenceTypeMap = new HashMap<>();

        if(resourceBundle != null) {
            Set<String> keys = resourceBundle.keySet();
            Iterator<String> iterator = keys.iterator();
            String nextKey;
            String[] fields;
            while(iterator.hasNext()) {
                nextKey = iterator.next();
                fields = resourceBundle.getString(nextKey).split(";", -1);
                if(fields.length == 2) {
                    consequenceTypeMap.put(nextKey, new ConsequenceType(nextKey, fields[1], fields[0], ""));
                }else {
                    consequenceTypeMap.put(nextKey, new ConsequenceType(nextKey, fields[1], fields[0], fields[2]));
                }
            }
        }

//        consequenceTypeMap.put("splice_acceptor", new ConsequenceType(23, "SO:0001574", "splice_acceptor_variant", "primary_transcript", "ESSENTIAL_SPLICE_SITE", 1, "splice-3", "Essential splice site", "In the first 2 or the last 2 basepairs of an intron"));
//        consequenceTypeMap.put("splice_donor", new ConsequenceType(20, "SO:0001575", "splice_donor_variant", "primary_transcript", "ESSENTIAL_SPLICE_SITE", 1, "splice-5", "Essential splice site", "In the first 2 or the last 2 basepairs of an intron"));
//        consequenceTypeMap.put("stop_gained", new ConsequenceType(11, "SO:0001587", "stop_gained", "mRNA", "STOP_GAINED", 3, "nonsense", "Stop gained", "In coding sequence, resulting in the gain of a stop codon"));
//        consequenceTypeMap.put("stop_lost", new ConsequenceType(19, "SO:0001578", "stop_lost", "mRNA", "STOP_LOST", 4, "", "Stop lost", "In coding sequence, resulting in the loss of a stop codon"));
//        consequenceTypeMap.put("non_synonymous_codon", new ConsequenceType(6, "SO:0001583", "non_synonymous_codon", "mRNA", "NON_SYNONYMOUS_CODING", 7, "missense", "Non-synonymous coding", "In coding sequence and results in an amino acid change in the encoded peptide sequence"));
//        consequenceTypeMap.put("splice_site", new ConsequenceType(14, "SO:0001630", "splice_region_variant", "primary_transcript", "SPLICE_SITE", 8, "", "Splice site", "1-3 bps into an exon or 3-8 bps into an intron"));
//        consequenceTypeMap.put("synonymous_codon", new ConsequenceType(9, "SO:0001588", "synonymous_codon", "mRNA", "SYNONYMOUS_CODING", 10, "cds-synon", "Synonymous coding", "In coding sequence, not resulting in an amino acid change (silent mutation)"));
//        consequenceTypeMap.put("exon", new ConsequenceType(24, "SO:0001791", "exon_variant", "mRNA", "CODING_UNKNOWN", 11, "", "Coding unknown", "A sequence variant that changes exon sequence"));
//        consequenceTypeMap.put("coding_sequence", new ConsequenceType(24, "SO:0001580", "coding_sequence_variant", "mRNA", "CODING_UNKNOWN", 11, "", "Coding unknown", "In coding sequence with indeterminate effect"));
//        consequenceTypeMap.put("5_prime_utr", new ConsequenceType(2, "SO:0001623", "5_prime_UTR_variant", "mRNA", "5PRIME_UTR", 13, "untranslated_5", "5 prime UTR", "In 5 prime untranslated region"));
//        consequenceTypeMap.put("3_prime_utr", new ConsequenceType(10, "SO:0001624", "3_prime_UTR_variant", "mRNA", "3PRIME_UTR", 14, "untranslated_3", "3 prime UTR", "In 3 prime untranslated region"));
//        consequenceTypeMap.put("intron", new ConsequenceType(26, "SO:0001627", "intron_variant", "primary_transcript", "INTRONIC", 15, "intron", "Intronic", "In intron"));
//        consequenceTypeMap.put("nmd_transcript", new ConsequenceType(5, "SO:0001621", "NMD_transcript_variant", "mRNA", "NMD_TRANSCRIPT", 16, "", "NMD transcript", "Located within a transcript predicted to undergo nonsense-mediated decay"));
//        consequenceTypeMap.put("nc_transcript", new ConsequenceType(15, "SO:0001619", "nc_transcript_variant", "ncRNA", "WITHIN_NON_CODING_GENE", 17, "", "Within non-coding gene", "Located within a gene that does not code for a protein"));
//        consequenceTypeMap.put("upstream", new ConsequenceType(28, "SO:0001635", "5KB_upstream_variant", "transcript", "UPSTREAM", 20, "", "Upstream", "Within 5 kb upstream of the 5 prime end of a transcript"));
//        consequenceTypeMap.put("downstream", new ConsequenceType(13, "SO:0001633", "5KB_downstream_variant", "transcript", "DOWNSTREAM", 21, "", "Downstream", "Within 5 kb downstream of the 3 prime end of a transcript"));
//        consequenceTypeMap.put("tfbs", new ConsequenceType(1, "SO:0001782", "TF_binding_site_variant", "TF_binding_site", "REGULATORY_REGION", 49, "", "Regulatory region", "A sequence variant located within a transcription factor binding site"));
//        consequenceTypeMap.put("regulatory_region", new ConsequenceType(7, "SO:0001566", "regulatory_region_variant", "regulatory_region", "REGULATORY_REGION", 50, "", "Regulatory region", "In regulatory region annotated by Ensembl"));
//        consequenceTypeMap.put("dnase1", new ConsequenceType(0, "SO:0000685", "DNAseI_hypersensitive_site", "regulatory_region", "REGULATORY_REGION", 100, "", "Regulatory region", ""));
//        consequenceTypeMap.put("polymerase", new ConsequenceType(0, "SO:0001203", "RNA_polymerase_promoter", "regulatory_region", "REGULATORY_REGION", 100, "", "Regulatory region", "A region (DNA) to which RNA polymerase binds, to begin transcription"));
//        consequenceTypeMap.put("mirna_target", new ConsequenceType(0, "SO:0000934", "miRNA_target_site", "miRNA_target_site", "REGULATORY_REGION", 100, "", "Regulatory region", "A miRNA target site is a binding site where the molecule is a micro RNA"));
//        consequenceTypeMap.put("mirna", new ConsequenceType(0, "SO:0000276", "miRNA", "miRNA", "miRNA", 100, "", "miRNA", "Small, ~22-nt, RNA molecule that is the endogenous transcript of a miRNA gene"));
//        consequenceTypeMap.put("lincrna", new ConsequenceType(0, "SO:0001463", "lincRNA", "lincRNA", "lincRNA", 100, "", "lincRNA", "A multiexonic non-coding RNA transcribed by RNA polymerase II"));
//        consequenceTypeMap.put("pseudogene", new ConsequenceType(0, "SO:0000336", "pseudogene", "pseudogene", "PSEUDOGENE", 100, "", "Pseudogene", "A sequence that closely resembles a known functional gene, at another locus within a genome, that is non-functional as a consequence of (usually several) mutations that prevent either its transcription or translation (or both)"));
//        consequenceTypeMap.put("cpg_island", new ConsequenceType(0, "SO:0000307", "CpG_island", "CpG_island", "CpG_ISLAND", 100, "", "CpG_island", "Regions of a few hundred to a few thousand bases in vertebrate genomes that are relatively GC and CpG rich; they are typically unmethylated and often found near the 5' ends of genes"));
//        consequenceTypeMap.put("snp", new ConsequenceType(0, "SO:0000694", "SNP", "SNP", "SNP", 100, "", "SNP", "SNPs are single base pair positions in genomic DNA at which different sequence alternatives exist in normal individuals in some population(s), wherein the least frequent variant has an abundance of 1% or greater"));
//        consequenceTypeMap.put("intergenic", new ConsequenceType(17, "SO:0001628", "intergenic_variant", "", "INTERGENIC", 100, "", "Intergenic", "More than 5 kb either upstream or downstream of a transcript"));
    }

//    public GenomicVariantEffectHibernateDBAdaptor(SessionFactory sessionFactory) {
//        super(sessionFactory);
//        dbAdaptorFact = new HibernateDBAdaptorFactory();
//        sequenceDbAdaptor = dbAdaptorFact.getGenomeSequenceDBAdaptor(species, version);
//        snpDbAdaptor = dbAdaptorFact.getSnpDBAdaptor(species, version);
//        //		sequenceDbAdaptor = (GenomeSequenceHibernateDBAdaptor) new GenomeSequenceHibernateDBAdaptor(sessionFactory);
//        //		snpDbAdaptor = (SnpDBAdaptor) new SnpHibernateDBAdaptor(sessionFactory);
//    }
//
//    public GenomicVariantEffectHibernateDBAdaptor(SessionFactory sessionFactory, String species, String version) {
//        super(sessionFactory, species, version);
//        dbAdaptorFact = new HibernateDBAdaptorFactory();
//        sequenceDbAdaptor = dbAdaptorFact.getGenomeSequenceDBAdaptor(species, version);
//        snpDbAdaptor = dbAdaptorFact.getSnpDBAdaptor(species, version);
//    }




//    @Override
//    public List<GenomicVariantEffect> getAllConsequenceTypeByVariant(GenomicVariant variant) {
//        return getAllConsequenceTypeByVariant(variant, null);
//    }
//
//    @Override
//    public List<GenomicVariantEffect> getAllConsequenceTypeByVariant(GenomicVariant variant, Set<String> excludeSet) {
//        Session session = this.openSession();
//        List<GenomicVariantEffect> consequenceTypesList = getAllEffectsByVariant(Arrays.asList(variant), excludeSet, null);
//        session.close();
//        return consequenceTypesList;
//
//    }


//    public List<GenomicVariantEffect> getAllConsequenceTypeByVariantList(List<GenomicVariant> variants) {
//        return getAllConsequenceTypeByVariantList(variants, null);
//    }
//
//    public List<GenomicVariantEffect> getAllConsequenceTypeByVariantList(List<GenomicVariant> variants, Set<String> excludeSet) {
//        List<GenomicVariantEffect> consequenceTypeList = new ArrayList<GenomicVariantEffect>();
//        //		getAllEffectsByVariant(variants, excludeSet);
//        Session session = this.openSession();
//        //		Query query= session.createQuery("select fm from FeatureMap fm where fm.chunkId = :CHUNK_ID and fm.chromosome = :CHROM and fm.start <= :START and fm.end >= :END");
//
//        consequenceTypeList.addAll(getAllEffectsByVariant(variants, excludeSet, session));
//
////		for(GenomicVariant variant: variants) {
////			consequenceTypeList.addAll(getAllEffectsByVariant(variant, excludeSet, session));
////		}
//        session.close();
//        return consequenceTypeList;
//    }



    @SuppressWarnings("unchecked")
//    public List<GenomicVariantEffect> getAllEffectsByVariant(List<GenomicVariant> variants, Set<String> excludeSet, Session session) {
    public List<GenomicVariantEffect> getAllEffectsByVariant(GenomicVariant variant, List<Gene> genes, List<RegulatoryRegion> regulatoryRegions) {

        List<GenomicVariantEffect> genomicVariantEffectList = new ArrayList<>();
//        List<Snp> snps;

        Map<String, Boolean> isFeatureUTR = new HashMap<String, Boolean>();
        //		boolean isUTR = false;

        //		Criteria criteria = this.openSession().createCriteria(FeatureMap.class);
        //		Criteria criteria = session.createCriteria(FeatureMap.class);
        int chunkId;


//        List<FeatureMap> featureMapList = null;
//        Query query= session.createQuery("select fm from FeatureMap fm where fm.chunkId = :CHUNK_ID and fm.chromosome = :CHROM and fm.start <= :START and fm.end >= :END");
//        for(GenomicVariant variant: variants) {
//            featureMapList = null;
//            if(variant != null) {
//                chunkId = variant.getPosition() / applicationProperties.getIntProperty("CELLBASE."+version.toUpperCase()+".FEATURE_MAP.CHUNK_SIZE", 500);
//                //			System.out.println("getAllConsequenceTypeByVariant: "+chunkId+", chromosome: "+variant.getSequenceName());
//                //			criteria.add(Restrictions.eq("chunkId", chunkId))
//                //				.add(Restrictions.eq("chromosome", variant.getSequenceName()))
//                //				.add(Restrictions.le("start", variant.getPosition()))
//                //				.add(Restrictions.ge("end", variant.getPosition()));
//                ////			featureMapList = (List<FeatureMap>) executeAndClose(criteria);
//                //			featureMapList = (List<FeatureMap>) execute(criteria);
//                query.setParameter("CHUNK_ID", chunkId).setParameter("CHROM", variant.getSequenceName()).setParameter("START", variant.getPosition()).setParameter("END", variant.getPosition());
//                featureMapList = (List<FeatureMap>) execute(query);
//            }
//        }

        boolean isForward;

        if(genes != null && genes.size() > 0) {
            for(Gene gene: genes) {

                for(Transcript transcript: gene.getTranscripts()) {

                    if(variant.getPosition() < transcript.getStart() - 5000 || variant.getPosition() > transcript.getEnd() + 5000 ) {
                        continue;
                    }

                    isForward = transcript.getStrand().equals("+") || transcript.getStrand().equals("1") || transcript.getStrand().equals("");

                    switch(transcript.getBiotype()) {
                        case "miRNA":   genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "mature_miRNA_variant"));
                            break;
                        case "nonsense_mediated_decay": genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "NMD_transcript_variant"));
                            break;
                        case "non_coding":  genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "nc_transcript_variant"));
                            break;
                    }

                    // UPS ?
                    if(variant.getPosition() >= transcript.getStart() - 5000 &&  variant.getPosition() < transcript.getStart()) {
                        if(isForward) {
                            genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "upstream_gene_variant"));
                        }else {
                            genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "downstream_gene_variant"));
                        }
                    }else {
                        // DOW ?
                        if(variant.getPosition() > transcript.getEnd() && variant.getPosition() <= transcript.getEnd() + 5000) {
                            if(isForward) {
                                genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "downstream_gene_variant"));
                            }else {
                                genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "upstream_gene_variant"));
                            }
                        }else {
                            // coding
                            for(Exon exon: transcript.getExons()) {

                                System.out.println(exon.toString());

                                if(variant.getPosition() < exon.getStart()) {
                                    genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "intron_variant"));

                                    if(variant.getPosition() >= exon.getStart() - 8 && variant.getPosition() <= exon.getStart() - 3 ) {
                                        genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "splice_region_variant"));
                                    }else {
                                        if(variant.getPosition() == exon.getStart() - 1 || variant.getPosition() == exon.getStart() - 2) {
                                            if(isForward) {
                                                genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "splice_acceptor_variant"));
                                            }else {
                                                genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "splice_donor_variant"));
                                            }
                                        }else {
                                            if(variant.getPosition() >= exon.getStart() && variant.getPosition() <= exon.getStart() + 3 ) {
                                                genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "splice_region_variant"));
                                            }
                                        }
                                    }
                                    break;
                                }

                                // Splices
                                if(variant.getPosition() >= exon.getStart() - 8 && variant.getPosition() <= exon.getStart() + 3) {
                                    if(variant.getPosition() >= exon.getStart() - 8 && variant.getPosition() <= exon.getStart() - 3 ) {
                                        genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "splice_region_variant"));
                                    }else {
                                        if(variant.getPosition() == exon.getStart() - 1 || variant.getPosition() == exon.getStart() - 2) {
                                            if(isForward) {
                                                genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "splice_acceptor_variant"));
                                            }else {
                                                genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "splice_donor_variant"));
                                            }
                                        }else {
                                            if(variant.getPosition() >= exon.getStart() && variant.getPosition() <= exon.getStart() + 3 ) {
                                                genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "splice_region_variant"));
                                            }
                                        }
                                    }
                                }else if(variant.getPosition() >= exon.getEnd() - 3 && variant.getPosition() <= exon.getEnd() + 8){
                                    if(variant.getPosition() >= exon.getEnd() - 3 && variant.getPosition() <= exon.getEnd() ) {
                                        genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "splice_region_variant"));
                                    }else {
                                        if(variant.getPosition() == exon.getEnd() + 1 || variant.getPosition() == exon.getEnd() + 2) {
                                            if(isForward) {
                                                genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "splice_donor_variant"));
                                            }else {
                                                genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "splice_acceptor_variant"));
                                            }
                                        }else {
                                            if(variant.getPosition() >= exon.getEnd() + 3 && variant.getPosition() <= exon.getEnd() + 8 ) {
                                                genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "splice_region_variant"));
                                            }
                                        }
                                    }
                                }


                                // primer y ultimo exon....
                                if(variant.getPosition() >= exon.getStart() && variant.getPosition() <= exon.getEnd()) {

                                    // UTRs
                                    if(exon.getGenomicCodingStart() == 0 && exon.getGenomicCodingEnd() == 0) {
                                        if(isForward){
                                            genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "5_prime_UTR_variant"));
                                        }else{
                                            genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "3_prime_UTR_variant"));
                                        }
                                    }else {
                                        if(variant.getPosition() < exon.getGenomicCodingStart()) {
                                            if(isForward){
                                                genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "5_prime_UTR_variant"));
                                            }else{
                                                genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "3_prime_UTR_variant"));
                                            }
                                        }else {
                                            if(variant.getPosition() > exon.getGenomicCodingEnd()){
                                                if(isForward){
                                                    genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "3_prime_UTR_variant"));
                                                }else{
                                                    genomicVariantEffectList.add(createGenomicVariantEffect(variant, gene, transcript, "5_prime_UTR_variant"));
                                                }
                                            }else {

                                                // non-syn
                                                if(transcript.getBiotype().equals("protein_coding") || transcript.getBiotype().equals("nonsense_mediated_decay")) {

                                                }
                                            }

                                        }

                                    }

                                    break;
                                }

                            }
                        }
                    }
                }
            }

        }else {
            genomicVariantEffectList.add(createIntergenicGenomicVariantEffect(variant));
        }



        return genomicVariantEffectList;
    }

    private GenomicVariantEffect createGenomicVariantEffect(GenomicVariant genomicVariant, Gene gene, Transcript transcript, String soTerm) {
        GenomicVariantEffect genomicVariantEffect = new GenomicVariantEffect(genomicVariant.getChromosome(), genomicVariant.getPosition(), genomicVariant.getReference(), genomicVariant.getAlternative(),
                transcript.getId(), transcript.getName(), "transcrri[pt", transcript.getBiotype(), transcript.getChromosome(), transcript.getStart(), transcript.getEnd(), transcript.getStrand(), "", "", "", gene.getId(),
                transcript.getId(), gene.getName(), consequenceTypeMap.get(soTerm).getSoAccesion(), soTerm, consequenceTypeMap.get(soTerm).getSoDescription(), "", -1, "", "", -1, "", -1, "", -1, -1);
        return genomicVariantEffect;
    }


    private GenomicVariantEffect createGenomicVariantEffect(GenomicVariant genomicVariant, Variation variation) {
        GenomicVariantEffect genomicVariantEffect = new GenomicVariantEffect();



        return genomicVariantEffect;
    }


    private GenomicVariantEffect createIntergenicGenomicVariantEffect(GenomicVariant genomicVariant) {
        GenomicVariantEffect genomicVariantEffect = new GenomicVariantEffect();
        genomicVariantEffect.setChromosome(genomicVariant.getChromosome());
        genomicVariantEffect.setPosition(genomicVariant.getPosition());
        genomicVariantEffect.setReferenceAllele(genomicVariant.getReference());
        genomicVariantEffect.setAlternativeAllele(genomicVariant.getAlternative());
        return genomicVariantEffect;
    }



        /*if(featureMapList != null) {
//				genomicVariantEffectList = new ArrayList<GenomicVariantEffect>(featureMapList.size());

            // we must know if the position is UTR for EACH of the transcripts
            isFeatureUTR.clear();
            for(FeatureMap featureMap: featureMapList) {
                if(featureMap.getFeatureType().equalsIgnoreCase("5_prime_utr") || featureMap.getFeatureType().equalsIgnoreCase("3_prime_utr")) {
                    isFeatureUTR.put(featureMap.getTranscriptStableId(), true);
                }else {
                    if(!isFeatureUTR.containsKey(featureMap.getTranscriptStableId())) {
                        isFeatureUTR.put(featureMap.getTranscriptStableId(), false);
                    }
                }
            }

            // to avoid NPE
            if(excludeSet == null) {
                excludeSet = new HashSet<String>();
            }

            for(FeatureMap featureMap: featureMapList) {
                //				if(featureMap.getFeatureType().equalsIgnoreCase("gene")) {
                ////				genomicVariantConsequenceType.add(new GenomicVariantEffect(chromosome, start, end, id, name, type, biotype, featureChromosome, featureStart, featureEnd, featureStrand, snpId, ancestral, alternative, geneId, transcriptId, geneName, consequenceTypeSoAccession, consequenceTypeObo, consequenceTypeDesc, consequenceTypeType, aminoacidChange, codonChange));
                //					continue;
                //				}

                if(featureMap.getFeatureType().equalsIgnoreCase("transcript")) {
                    if (featureMap.getBiotype().equalsIgnoreCase("mirna") && !excludeSet.contains(consequenceTypeMap.get("mirna").getSoTerm())){
                        genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "mirna"));
                    }
                    if (featureMap.getBiotype().equalsIgnoreCase("nonsense_mediated_decay") && !excludeSet.contains(consequenceTypeMap.get("nmd_transcript").getSoTerm())){
                        genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "nmd_transcript"));
                    }
                    if (featureMap.getBiotype().equalsIgnoreCase("lincrna") && !excludeSet.contains(consequenceTypeMap.get("lincrna").getSoTerm())){
                        genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "lincrna"));
                    }
                    if (featureMap.getBiotype().equalsIgnoreCase("pseudogene") && !excludeSet.contains(consequenceTypeMap.get("pseudogene").getSoTerm())){
                        genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "pseudogene"));
                    }
                    if (featureMap.getBiotype().equalsIgnoreCase("non_coding") && !excludeSet.contains(consequenceTypeMap.get("nc_transcript").getSoTerm())){
                        genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "nc_transcript"));
                    }
                    continue;
                }

                if(featureMap.getFeatureType().equalsIgnoreCase("intron") && !excludeSet.contains(consequenceTypeMap.get("intron").getSoTerm())) {
                    genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "intron"));
                    continue;
                }

                if(featureMap.getFeatureType(). equalsIgnoreCase("splice_site") && !excludeSet.contains(consequenceTypeMap.get("splice_site").getSoTerm())) {
                    genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "splice_site"));
                    continue;
                }

                if(featureMap.getFeatureType(). equalsIgnoreCase("splice_donor") && !excludeSet.contains(consequenceTypeMap.get("splice_donor").getSoTerm())) {
                    genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "splice_donor"));
                    continue;
                }

                if(featureMap.getFeatureType(). equalsIgnoreCase("splice_acceptor") && !excludeSet.contains(consequenceTypeMap.get("splice_acceptor").getSoTerm())) {
                    genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "splice_acceptor"));
                    continue;
                }

                if(featureMap.getFeatureType().equalsIgnoreCase("exon") && !excludeSet.contains(consequenceTypeMap.get("exon").getSoTerm())) {
                    genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "exon"));

                    //	int codonPosition = -1;
                    //					if (!isUTR && featureMap.getBiotype().equalsIgnoreCase("protein_coding")) {
                    if (isFeatureUTR.get(featureMap.getTranscriptStableId()) != null && !isFeatureUTR.get(featureMap.getTranscriptStableId()) && (featureMap.getBiotype().equalsIgnoreCase("protein_coding") || featureMap.getBiotype().equalsIgnoreCase("nonsense_mediated_decay"))) {
                        genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "coding_sequence"));
                        // && !featureMap.getExonPhase().equals("-1") ==> not needed!!
                        // If exon contains a UTR part is not processed here as 'if' clause above does not allow to enter this code
                        // so there is not need to check if phase is -1 (which means that there is a 5'-UTR part)
                        if(!featureMap.getExonPhase().equals("")) {
                            int aaPosition = -1;
                            int exonOffset = -1;
                            if(featureMap.getStrand().equals("1")) {
                                // If ExonPhase is -1 means we are in an exon with UTR and is not the UTR part,
                                // we only need to calculate the offset in cdna without getTranscriptCdnaCodingStart()
                                exonOffset = variant.getPosition() - featureMap.getStart() + 1;
                                if(featureMap.getExonPhase() != null && featureMap.getExonPhase().equals("-1") && exonOffset > featureMap.getTranscriptCdnaCodingStart()) {
                                    aaPosition = ((exonOffset - featureMap.getTranscriptCdnaCodingStart())/3)+1;
                                    System.out.println("UTR-EXON: aaPosition: "+aaPosition);
                                }else {
                                    aaPosition = ((exonOffset + featureMap.getExonCdnaCodingStart()-featureMap.getTranscriptCdnaCodingStart() -1 )/3)+1;
                                    System.out.println("EXON: aaPosition: "+aaPosition);
                                }
                            }else {
                                exonOffset = featureMap.getEnd()-variant.getPosition()+1;
                                if(featureMap.getExonPhase() != null && featureMap.getExonPhase().equals("-1") && exonOffset > featureMap.getTranscriptCdnaCodingStart()) {
                                    aaPosition = ((exonOffset - featureMap.getTranscriptCdnaCodingStart())/3)+1;
                                }else {
                                    aaPosition = ((exonOffset + featureMap.getExonCdnaCodingStart()-featureMap.getTranscriptCdnaCodingStart() - 1)/3)+1;
                                }
                            }

                            String[] codons =  getSequenceByCodon(variant, featureMap);
                            if(DNASequenceUtils.codonToAminoacidShort.get(codons[0]) != null && DNASequenceUtils.codonToAminoacidShort.get(codons[1]) != null) {
                                if(DNASequenceUtils.codonToAminoacidShort.get(codons[0]).equals(DNASequenceUtils.codonToAminoacidShort.get(codons[1]))){
                                    //								this.addConsequenceType(transcript, "synonymous_codon", "SO:0001588", "In coding sequence, not resulting in an amino acid change (silent mutation)", "consequenceTypeType" );
                                    genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "synonymous_codon", aaPosition, DNASequenceUtils.codonToAminoacidShort.get(codons[0])+"/"+DNASequenceUtils.codonToAminoacidShort.get(codons[1]), codons[0].replaceAll("U", "T")+"/"+codons[1].replaceAll("U", "T")));
                                }else{
                                    //								this.addConsequenceType(transcript, "non_synonymous_codon", "SO:0001583", "In coding sequence and results in an amino acid change in the encoded peptide sequence", "consequenceTypeType", DNASequenceUtils.codonToAminoacidShort.get(referenceSequence)+"/"+ DNASequenceUtils.codonToAminoacidShort.get(alternative), referenceSequence.replace("U", "T")+"/"+alternative.replace("U", "T")  );
                                    genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "non_synonymous_codon", aaPosition, DNASequenceUtils.codonToAminoacidShort.get(codons[0])+"/"+DNASequenceUtils.codonToAminoacidShort.get(codons[1]), codons[0].replaceAll("U", "T")+"/"+codons[1].replaceAll("U", "T")));

                                    if ((!DNASequenceUtils.codonToAminoacidShort.get(codons[0]).toLowerCase().equals("stop"))&& (DNASequenceUtils.codonToAminoacidShort.get(codons[1]).toLowerCase().equals("stop"))){
                                        //									this.addConsequenceType(transcript, "stop_gained", "SO:0001587", "In coding sequence, resulting in the gain of a stop codon", "consequenceTypeType", DNASequenceUtils.codonToAminoacidShort.get(referenceSequence)+"/"+ DNASequenceUtils.codonToAminoacidShort.get(alternative), referenceSequence.replace("U", "T")+"/"+alternative.replace("U", "T")  );
                                        genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "stop_gained", aaPosition, DNASequenceUtils.codonToAminoacidShort.get(codons[0])+"/"+DNASequenceUtils.codonToAminoacidShort.get(codons[1]), codons[0].replaceAll("U", "T")+"/"+codons[1].replaceAll("U", "T")));
                                    }

                                    if ((DNASequenceUtils.codonToAminoacidShort.get(codons[0]).toLowerCase().equals("stop"))&& (!DNASequenceUtils.codonToAminoacidShort.get(codons[1]).toLowerCase().equals("stop"))){
                                        //									this.addConsequenceType(transcript, "stop_lost", "SO:0001578", "In coding sequence, resulting in the loss of a stop codon", "consequenceTypeType", DNASequenceUtils.codonToAminoacidShort.get(referenceSequence)+"/"+ DNASequenceUtils.codonToAminoacidShort.get(alternative), referenceSequence.replace("U", "T")+"/"+alternative.replace("U", "T")  );
                                        genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "stop_lost", aaPosition, DNASequenceUtils.codonToAminoacidShort.get(codons[0])+"/"+DNASequenceUtils.codonToAminoacidShort.get(codons[1]), codons[0].replaceAll("U", "T")+"/"+codons[1].replaceAll("U", "T")));
                                    }
                                }
                            }
                        }
                    }
                    continue;
                }

                if(featureMap.getFeatureType().equalsIgnoreCase("regulatory_region") && !excludeSet.contains(consequenceTypeMap.get("regulatory_region").getSoTerm())) {
                    genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "regulatory_region"));

                    if(featureMap.getFeatureName().equalsIgnoreCase("dnase1") || featureMap.getFeatureName().equalsIgnoreCase("faire")) {
                        genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "dnase1"));
                    }

                    if(featureMap.getFeatureName().equalsIgnoreCase("PolII") || featureMap.getFeatureName().equalsIgnoreCase("PolIII")) {
                        genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "polymerase"));
                    }

                    continue;
                }

                if(featureMap.getFeatureType().equalsIgnoreCase("tfbs") && !excludeSet.contains(consequenceTypeMap.get("tfbs").getSoTerm())) {
                    genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "tfbs"));
                    continue;
                }

                if(featureMap.getFeatureType().equalsIgnoreCase("mirna_target") && !excludeSet.contains(consequenceTypeMap.get("mirna_target").getSoTerm())) {
                    genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "mirna_target"));
                    continue;
                }

                if(featureMap.getFeatureType().equalsIgnoreCase("upstream") && !excludeSet.contains(consequenceTypeMap.get("upstream").getSoTerm())) {
                    genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "upstream"));
                    continue;
                }

                if(featureMap.getFeatureType().equalsIgnoreCase("downstream") && !excludeSet.contains(consequenceTypeMap.get("downstream").getSoTerm())) {
                    genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "downstream"));
                    continue;
                }

                if(featureMap.getFeatureType().equalsIgnoreCase("5_prime_utr") && !excludeSet.contains(consequenceTypeMap.get("5_prime_utr").getSoTerm())) {
                    genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "5_prime_utr"));
                    continue;
                }

                if(featureMap.getFeatureType().equalsIgnoreCase("3_prime_utr") && !excludeSet.contains(consequenceTypeMap.get("3_prime_utr").getSoTerm())) {
                    genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "3_prime_utr"));
                    continue;
                }

                if(featureMap.getFeatureType().equalsIgnoreCase("CpG_island") && !excludeSet.contains(consequenceTypeMap.get("cpg_island").getSoTerm())) {
                    genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, "cpg_island"));
                    continue;
                }

                if(featureMap.getFeatureType().equalsIgnoreCase("snp") && !excludeSet.contains(consequenceTypeMap.get("snp").getSoTerm())) {
                    // special method
                    snps = snpDbAdaptor.getAllBySnpId(featureMap.getFeatureName());
                    if(snps != null && snps.size() > 0) {
                        genomicVariantEffectList.add(createGenomicVariantConsequenceType(variant, featureMap, snps.get(0), "snp"));
                    }
                    continue;
                }

            }
        }else {
            // intergenic!!
//				genomicVariantEffectList = new ArrayList<GenomicVariantEffect>(1);

        }*/



}