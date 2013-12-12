package org.opencb.cellbase.core.lib;

import org.opencb.cellbase.core.lib.api.*;
import org.opencb.cellbase.core.lib.api.network.PathwayDBAdaptor;
import org.opencb.cellbase.core.lib.api.network.ProteinProteinInteractionDBAdaptor;
import org.opencb.cellbase.core.lib.api.regulatory.RegulatoryRegionDBAdaptor;
import org.opencb.cellbase.core.lib.api.regulatory.TfbsDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.MutationDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.StructuralVariationDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.VariantEffectDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.VariationDBAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public abstract class DBAdaptorFactory {

	protected Logger logger;
	
//	protected static ResourceBundle resourceBundle;
//	protected static Config applicationProperties;

//	protected static Map<String, String> speciesAlias;
	
//	static {
//		speciesAlias = new HashMap<String, String>(20);
//
//		// reading application.properties file
//		cellbaseResourceBundle = ResourceBundle.getBundle("cellbase");
//		try {
//			cellbaseProperties = new Config(cellbaseResourceBundle);
//			String[] speciesArray = cellbaseProperties.getProperty("SPECIES").split(",");
//			String[] alias = null;
//			for(String species: speciesArray) {
//				species = species.toUpperCase();
//				alias = cellbaseProperties.getProperty(species + ".ALIAS").split(",");
//				for(String al: alias) {
//					speciesAlias.put(al, species);
//				}
//				// For to recognize the species code
//				speciesAlias.put(species, species);
//			}
//		} catch (IOException e) {
//			cellbaseProperties = new Config();
//			e.printStackTrace();
//		}
////		speciesAlias.put("Homo sapiens", "HSAPIENS");speciesAlias.put("Homo_sapiens", "HSAPIENS");speciesAlias.put("hsapiens", "HSAPIENS");speciesAlias.put("hsap", "HSAPIENS");speciesAlias.put("hsa", "HSAPIENS");speciesAlias.put("HSAPIENS", "HSAPIENS");
////		speciesAlias.put("Mus musculus", "MMUSCULUS");speciesAlias.put("Mus_musculus", "MMUSCULUS");speciesAlias.put("mmusculus", "MMUSCULUS");speciesAlias.put("mmus", "MMUSCULUS");speciesAlias.put("mmu", "MMUSCULUS");speciesAlias.put("MMUSCULUS", "MMUSCULUS");
////		speciesAlias.put("Rattus norvegicus", "RNORVEGICUS");speciesAlias.put("Rattus_norvegicus", "RNORVEGICUS");speciesAlias.put("rnorvegicus", "RNORVEGICUS");speciesAlias.put("rnor", "RNORVEGICUS");speciesAlias.put("rno", "RNORVEGICUS");speciesAlias.put("RNORVEGICUS", "RNORVEGICUS");
////		speciesAlias.put("Pan troglodytes", "PTROGLODYTES");speciesAlias.put("Pan_troglodytes", "PTROGLODYTES");speciesAlias.put("proglodytes", "PTROGLODYTES");speciesAlias.put("ptro", "PTROGLODYTES");speciesAlias.put("ptr", "PTROGLODYTES");speciesAlias.put("PTROGLODYTES", "PTROGLODYTES");
////
////		speciesAlias.put("Danio rerio", "DRERIO");speciesAlias.put("Danio_rerio", "DRERIO");speciesAlias.put("drerio", "DRERIO");speciesAlias.put("drer", "DRERIO");speciesAlias.put("dre", "DRERIO");speciesAlias.put("DRERIO", "DRERIO");
////		speciesAlias.put("Drosophila melanogaster", "DMELANOGASTER");speciesAlias.put("Drosophila_melanogaster", "DMELANOGASTER");speciesAlias.put("dmelanogaster", "DMELANOGASTER");speciesAlias.put("dmel", "DMELANOGASTER");speciesAlias.put("dme", "DMELANOGASTER");speciesAlias.put("DMELANOGASTER", "DMELANOGASTER");
////		speciesAlias.put("Caenorhabditis elegans", "CELEGANS");speciesAlias.put("Caenorhabditis_elegans", "CELEGANS");speciesAlias.put("celegans", "CELEGANS");speciesAlias.put("cele", "CELEGANS");speciesAlias.put("cel", "CELEGANS");speciesAlias.put("CELEGANS", "CELEGANS");
////		speciesAlias.put("Saccharomyces cerevisiae", "SCEREVISIAE");speciesAlias.put("Saccharomyces_cerevisiae", "SCEREVISIAE");speciesAlias.put("scerevisiae", "SCEREVISIAE");speciesAlias.put("scer", "SCEREVISIAE");speciesAlias.put("sce", "SCEREVISIAE");speciesAlias.put("SCEREVISIAE", "SCEREVISIAE");
////		speciesAlias.put("Canis familiaris", "CFAMILIARIS");speciesAlias.put("Canis_familiaris", "CFAMILIARIS");speciesAlias.put("cfamiliaris", "CFAMILIARIS");speciesAlias.put("cfar", "CFAMILIARIS");speciesAlias.put("cfa", "CFAMILIARIS");speciesAlias.put("CFAMILIARIS", "CFAMILIARIS");
////		speciesAlias.put("Sus scrofa", "SSCROFA");speciesAlias.put("Sus_scrofa", "SSCROFA");speciesAlias.put("sscrofa", "SSCROFA");speciesAlias.put("sscr", "SSCROFA");speciesAlias.put("ssc", "SSCROFA");speciesAlias.put("SSCROFA", "SSCROFA");
////		speciesAlias.put("Plasmodium falciparum", "PFALCIPARUM");speciesAlias.put("Plasmodium_falciparum", "PFALCIPARUM");speciesAlias.put("pfalciparum", "PFALCIPARUM");speciesAlias.put("pfalc", "PFALCIPARUM");speciesAlias.put("pfa", "PFALCIPARUM");speciesAlias.put("PFALCIPARUM", "PFALCIPARUM");
////		speciesAlias.put("Anopheles gambiae", "AGAMBIAE");speciesAlias.put("Anopheles_gambiae", "AGAMBIAE");speciesAlias.put("agambiae", "AGAMBIAE");speciesAlias.put("agam", "AGAMBIAE");speciesAlias.put("aga", "AGAMBIAE");speciesAlias.put("AGAMBIAE", "AGAMBIAE");
////		speciesAlias.put("Aspergillus fumigatus", "AFUMIGATUS");speciesAlias.put("Aspergillus_fumigatus", "AFUMIGATUS");speciesAlias.put("afumigatus", "AFUMIGATUS");speciesAlias.put("afum", "AFUMIGATUS");speciesAlias.put("afu", "AFUMIGATUS");speciesAlias.put("AFUMIGATUS", "AFUMIGATUS");
////		speciesAlias.put("Fusarium oxysporum", "FOXYSPORUM");speciesAlias.put("Fusarium_oxysporum", "FOXYSPORUM");speciesAlias.put("foxysporum", "FOXYSPORUM");speciesAlias.put("foxy", "FOXYSPORUM");speciesAlias.put("fox", "FOXYSPORUM");speciesAlias.put("FOXYSPORUM", "FOXYSPORUM");
////		speciesAlias.put("Citrus clementina", "CCLEMENTINA");speciesAlias.put("Citrus_clementina", "CCLEMENTINA");speciesAlias.put("cclementina", "CCLEMENTINA");speciesAlias.put("ccle", "CCLEMENTINA");speciesAlias.put("ccl", "CCLEMENTINA");speciesAlias.put("CCLEMENTINA", "CCLEMENTINA");
//	}
	
	public DBAdaptorFactory() {
		logger = LoggerFactory.getLogger(DBAdaptorFactory.class);
//		logger.setLevel(Logger.ERROR_LEVEL);
	}

//	protected String getSpeciesVersionPrefix(String species, String version) {
//		String speciesPrefix = null;
//		if(species != null && !species.equals("")) {
//			// coding an alias to application code species
//			species = speciesAlias.get(species);
//			// if 'version' parameter has not been provided the default version is selected
//			if(version == null || version.trim().equals("")) {
//				version = applicationProperties.getProperty(species+".DEFAULT.VERSION").toUpperCase();
////				logger.debug("HibernateDBAdaptorFactory in createSessionFactory(): 'version' parameter is null or empty, it's been set to: '"+version+"'");
//			}
//
//			// setting database configuration for the 'species.version'
//			speciesPrefix = species.toUpperCase() + "." + version.toUpperCase();
//		}
//
//		return speciesPrefix;
//	}

	public abstract void setConfiguration(Properties properties);
	
	public abstract void open(String species, String version);
	
	public abstract void close();


	public abstract GeneDBAdaptor getGeneDBAdaptor(String species);
	
	public abstract GeneDBAdaptor getGeneDBAdaptor(String species, String version);

	
	public abstract TranscriptDBAdaptor getTranscriptDBAdaptor(String species);
	
	public abstract TranscriptDBAdaptor getTranscriptDBAdaptor(String species, String version);
	
	
	public abstract ChromosomeDBAdaptor getChromosomeDBAdaptor(String species);
	
	public abstract ChromosomeDBAdaptor getChromosomeDBAdaptor(String species, String version);
	

	public abstract ExonDBAdaptor getExonDBAdaptor(String species);
	
	public abstract ExonDBAdaptor getExonDBAdaptor(String species, String version);
	

//	public abstract GenomicRegionFeatureDBAdaptor getFeatureMapDBAdaptor(String species);
//	
//	public abstract GenomicRegionFeatureDBAdaptor getFeatureMapDBAdaptor(String species, String version);
	
	
	public abstract VariantEffectDBAdaptor getGenomicVariantEffectDBAdaptor(String species);
	
	public abstract VariantEffectDBAdaptor getGenomicVariantEffectDBAdaptor(String species, String version);
	
	
	public abstract ProteinDBAdaptor getProteinDBAdaptor(String species);
	
	public abstract ProteinDBAdaptor getProteinDBAdaptor(String species, String version);
	
	
	public abstract SnpDBAdaptor getSnpDBAdaptor(String species);
	
	public abstract SnpDBAdaptor getSnpDBAdaptor(String species,  String version);

	
	public abstract GenomeSequenceDBAdaptor getGenomeSequenceDBAdaptor(String species);
	
	public abstract GenomeSequenceDBAdaptor getGenomeSequenceDBAdaptor(String species, String version);


	public abstract CytobandDBAdaptor getCytobandDBAdaptor(String species);
	
	public abstract CytobandDBAdaptor getCytobandDBAdaptor(String species, String version);


	public abstract XRefsDBAdaptor getXRefDBAdaptor(String species);
	
	public abstract XRefsDBAdaptor getXRefDBAdaptor(String species, String version);
	
	
	public abstract TfbsDBAdaptor getTfbsDBAdaptor(String species);
	
	public abstract TfbsDBAdaptor getTfbsDBAdaptor(String species, String version);


	public abstract RegulatoryRegionDBAdaptor getRegulatoryRegionDBAdaptor(String species);
	
	public abstract RegulatoryRegionDBAdaptor getRegulatoryRegionDBAdaptor(String species, String version);
	
	
	public abstract MirnaDBAdaptor getMirnaDBAdaptor(String species);
	
	public abstract MirnaDBAdaptor getMirnaDBAdaptor(String species, String version);


	public abstract MutationDBAdaptor getMutationDBAdaptor(String species);
	
	public abstract MutationDBAdaptor getMutationDBAdaptor(String species, String version);

	
	public abstract CpGIslandDBAdaptor getCpGIslandDBAdaptor(String species);
	
	public abstract CpGIslandDBAdaptor getCpGIslandDBAdaptor(String species, String version);
	
	
	public abstract StructuralVariationDBAdaptor getStructuralVariationDBAdaptor(String species);
	
	public abstract StructuralVariationDBAdaptor getStructuralVariationDBAdaptor(String species, String version);
	
	
	public abstract PathwayDBAdaptor getPathwayDBAdaptor(String species);
	
	public abstract PathwayDBAdaptor getPathwayDBAdaptor(String species, String version);


    public abstract ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species);

    public abstract ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species, String version);


//    public abstract RegulatoryRegionDBAdaptor getRegulationDBAdaptor(String species);
//
//    public abstract RegulatoryRegionDBAdaptor getRegulationDBAdaptor(String species, String version);


    public abstract VariationDBAdaptor getVariationDBAdaptor(String species);

    public abstract VariationDBAdaptor getVariationDBAdaptor(String species, String version);


    public abstract ConservedRegionDBAdaptor getConservedRegionDBAdaptor(String species);

    public abstract ConservedRegionDBAdaptor getConservedRegionDBAdaptor(String species, String version);


    public abstract ProteinFunctionPredictorDBAdaptor getProteinFunctionPredictorDBAdaptor(String species);

    public abstract ProteinFunctionPredictorDBAdaptor getProteinFunctionPredictorDBAdaptor(String species, String version);
}
