/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.lib.managers;

import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.utils.SpeciesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CellBaseManagerFactory {

    private CellBaseConfiguration configuration;

    private Map<String, GeneManager> geneManagers;
    private Map<String, TranscriptManager> transcriptManagers;
    private Map<String, VariantManager> variantManagers;
    private Map<String, ProteinManager> proteinManagers;
    private Map<String, GenomeManager> genomeManagers;
    private Map<String, ClinicalManager> clinicalManagers;
    private Map<String, RegulatoryManager> regulatoryManagers;
    private Map<String, XrefManager> xrefManagers;
    private Map<String, RepeatsManager> repeatsManagers;
    private Map<String, TfbsManager> tfManagers;
    private MetaManager metaManager;
    private Map<String, OntologyManager> ontologyManagers;
    private FileManager fileManager;
    private PublicationManager publicationManager;
    private Map<String, PharmacogenomicsManager> pharmacogenomicsManagers;

    private Map<String, DataReleaseManager> dataReleaseManagers;

    private Logger logger;

    public CellBaseManagerFactory(CellBaseConfiguration configuration) {
        this.configuration = configuration;
        logger = LoggerFactory.getLogger(this.getClass());

        geneManagers = new HashMap<>();
        transcriptManagers = new HashMap<>();
        variantManagers = new HashMap<>();
        proteinManagers = new HashMap<>();
        genomeManagers = new HashMap<>();
        clinicalManagers = new HashMap<>();
        regulatoryManagers = new HashMap<>();
        xrefManagers = new HashMap<>();
        repeatsManagers = new HashMap<>();
        tfManagers = new HashMap<>();
        ontologyManagers = new HashMap<>();
        dataReleaseManagers = new HashMap<>();
        pharmacogenomicsManagers = new HashMap<>();
    }

    private String getMultiKey(String species, String assembly) {
        return species + "_" + assembly;
    }

    @Deprecated
    private String getMultiKey(String species, String assembly, String resource) {
        return species + "_" + assembly + "_" + resource;
    }

    public GeneManager getGeneManager(String species) throws CellBaseException {
        if (species == null) {
            throw new CellBaseException("Failed to get GeneManager. Species is required.");
        }
        SpeciesConfiguration.Assembly assembly = SpeciesUtils.getDefaultAssembly(configuration, species);
        return getGeneManager(species, assembly.getName());
    }

    public GeneManager getGeneManager(String species, String assembly) throws CellBaseException {
        String multiKey = getMultiKey(species, assembly);
        if (!geneManagers.containsKey(multiKey)) {
            if (!validateSpeciesAssembly(species, assembly)) {
                throw new CellBaseException("Invalid species " + species + " or assembly " + assembly);
            }
            geneManagers.put(multiKey, new GeneManager(species, assembly, configuration));
        }
        return geneManagers.get(multiKey);
    }

    private boolean validateSpeciesAssembly(String species, String assembly) {
        if (species == null) {
            logger.error("Species is required.");
            return false;
        }
        if (assembly == null) {
            logger.error("Assembly is required.");
            return false;
        }
        try {
            // check configuration to make sure this species assembly combo is valid.
            if (SpeciesUtils.getSpecies(configuration, species, assembly) == null) {
                logger.error("Invalid species or assembly");
                return false;
            }
        } catch (CellBaseException e) {
            logger.error(e.getMessage());
            return false;
        }
        return true;
    }

    public TranscriptManager getTranscriptManager(String species) throws CellBaseException {
        if (species == null) {
            throw new CellBaseException("Species is required.");
        }
        SpeciesConfiguration.Assembly assembly = SpeciesUtils.getDefaultAssembly(configuration, species);
        return getTranscriptManager(species, assembly.getName());
    }

    public TranscriptManager getTranscriptManager(String species, String assembly) throws CellBaseException {
        String multiKey = getMultiKey(species, assembly);
        if (!transcriptManagers.containsKey(multiKey)) {
            if (!validateSpeciesAssembly(species, assembly)) {
                throw new CellBaseException("Invalid species " + species + " or assembly " + assembly);
            }
            transcriptManagers.put(multiKey, new TranscriptManager(species, assembly, configuration));
        }
        return transcriptManagers.get(multiKey);
    }

    public VariantManager getVariantManager(String species) throws CellBaseException {
        if (species == null) {
            throw new CellBaseException("Species is required.");
        }
        SpeciesConfiguration.Assembly assembly = SpeciesUtils.getDefaultAssembly(configuration, species);
        return getVariantManager(species, assembly.getName());
    }

    public VariantManager getVariantManager(String species, String assembly) throws CellBaseException {
        String multiKey = getMultiKey(species, assembly);
        if (!variantManagers.containsKey(multiKey)) {
            if (!validateSpeciesAssembly(species, assembly)) {
                throw new CellBaseException("Invalid species " + species + " or assembly " + assembly);
            }
            variantManagers.put(multiKey, new VariantManager(species, assembly, configuration));
        }
        return variantManagers.get(multiKey);
    }

    public ProteinManager getProteinManager(String species) throws CellBaseException {
        if (species == null) {
            throw new CellBaseException("Species is required.");
        }
        SpeciesConfiguration.Assembly assembly = SpeciesUtils.getDefaultAssembly(configuration, species);
        return getProteinManager(species, assembly.getName());
    }

    public ProteinManager getProteinManager(String species, String assembly) throws CellBaseException {
        String multiKey = getMultiKey(species, assembly);
        if (!proteinManagers.containsKey(multiKey)) {
            if (!validateSpeciesAssembly(species, assembly)) {
                throw new CellBaseException("Invalid species " + species + " or assembly " + assembly);
            }
            proteinManagers.put(multiKey, new ProteinManager(species, assembly, configuration));
        }
        return proteinManagers.get(multiKey);
    }

    public GenomeManager getGenomeManager(String species) throws CellBaseException {
        if (species == null) {
            throw new CellBaseException("Species is required.");
        }
        SpeciesConfiguration.Assembly assembly = SpeciesUtils.getDefaultAssembly(configuration, species);
        return getGenomeManager(species, assembly.getName());
    }

    public GenomeManager getGenomeManager(String species, String assembly) throws CellBaseException {
        String multiKey = getMultiKey(species, assembly);
        if (!genomeManagers.containsKey(multiKey)) {
            if (!validateSpeciesAssembly(species, assembly)) {
                throw new CellBaseException("Invalid species " + species + " or assembly " + assembly);
            }
            genomeManagers.put(multiKey, new GenomeManager(species, assembly, configuration));
        }
        return genomeManagers.get(multiKey);
    }

    public ClinicalManager getClinicalManager(String species) throws CellBaseException {
        if (species == null) {
            throw new CellBaseException("Species is required.");
        }
        SpeciesConfiguration.Assembly assembly = SpeciesUtils.getDefaultAssembly(configuration, species);
        return getClinicalManager(species, assembly.getName());
    }

    public ClinicalManager getClinicalManager(String species, String assembly) throws CellBaseException {
        String multiKey = getMultiKey(species, assembly);
        if (!clinicalManagers.containsKey(multiKey)) {
            if (!validateSpeciesAssembly(species, assembly)) {
                throw new CellBaseException("Invalid species " + species + " or assembly " + assembly);
            }
            clinicalManagers.put(multiKey, new ClinicalManager(species, assembly, configuration));
        }
        return clinicalManagers.get(multiKey);
    }

    public RegulatoryManager getRegulatoryManager(String species) throws CellBaseException {
        if (species == null) {
            throw new CellBaseException("Species is required.");
        }
        SpeciesConfiguration.Assembly assembly = SpeciesUtils.getDefaultAssembly(configuration, species);
        return getRegulatoryManager(species, assembly.getName());
    }

    public RegulatoryManager getRegulatoryManager(String species, String assembly) throws CellBaseException {
        String multiKey = getMultiKey(species, assembly);
        if (!regulatoryManagers.containsKey(multiKey)) {
            if (!validateSpeciesAssembly(species, assembly)) {
                throw new CellBaseException("Invalid species " + species + " or assembly " + assembly);
            }
            regulatoryManagers.put(multiKey, new RegulatoryManager(species, assembly, configuration));
        }
        return regulatoryManagers.get(multiKey);
    }

    public XrefManager getXrefManager(String species) throws CellBaseException {
        if (species == null) {
            throw new CellBaseException("Species is required.");
        }
        SpeciesConfiguration.Assembly assembly = SpeciesUtils.getDefaultAssembly(configuration, species);
        return getXrefManager(species, assembly.getName());
    }

    public XrefManager getXrefManager(String species, String assembly) throws CellBaseException {
        String multiKey = getMultiKey(species, assembly);
        if (!xrefManagers.containsKey(multiKey)) {
            if (!validateSpeciesAssembly(species, assembly)) {
                throw new CellBaseException("Invalid species " + species + " or assembly " + assembly);
            }
            xrefManagers.put(multiKey, new XrefManager(species, assembly, configuration));
        }
        return xrefManagers.get(multiKey);
    }

    public RepeatsManager getRepeatsManager(String species) throws CellBaseException {
        if (species == null) {
            throw new CellBaseException("Species is required.");
        }
        SpeciesConfiguration.Assembly assembly = SpeciesUtils.getDefaultAssembly(configuration, species);
        return getRepeatsManager(species, assembly.getName());
    }

    public RepeatsManager getRepeatsManager(String species, String assembly) throws CellBaseException {
        String multiKey = getMultiKey(species, assembly);
        if (!repeatsManagers.containsKey(multiKey)) {
            if (!validateSpeciesAssembly(species, assembly)) {
                throw new CellBaseException("Invalid species " + species + " or assembly " + assembly);
            }
            repeatsManagers.put(multiKey, new RepeatsManager(species, assembly, configuration));
        }
        return repeatsManagers.get(multiKey);
    }

    public TfbsManager getTFManager(String species) throws CellBaseException {
        if (species == null) {
            throw new CellBaseException("Species is required.");
        }
        SpeciesConfiguration.Assembly assembly = SpeciesUtils.getDefaultAssembly(configuration, species);
        return getTFManager(species, assembly.getName());
    }

    public TfbsManager getTFManager(String species, String assembly) throws CellBaseException {
        String multiKey = getMultiKey(species, assembly);
        if (!tfManagers.containsKey(multiKey)) {
            if (!validateSpeciesAssembly(species, assembly)) {
                throw new CellBaseException("Invalid species " + species + " or assembly " + assembly);
            }
            tfManagers.put(multiKey, new TfbsManager(species, assembly, configuration));
        }
        return tfManagers.get(multiKey);
    }

    public MetaManager getMetaManager() throws CellBaseException {
        if (metaManager == null) {
            metaManager = new MetaManager(configuration);
        }
        return metaManager;
    }

    public FileManager getFileManager() throws CellBaseException {
        if (fileManager == null) {
            fileManager = new FileManager(configuration);
        }
        return fileManager;
    }

    public OntologyManager getOntologyManager(String species) throws CellBaseException {
        if (species == null) {
            throw new CellBaseException("Species is required.");
        }
        SpeciesConfiguration.Assembly assembly = SpeciesUtils.getDefaultAssembly(configuration, species);
        return getOntologyManager(species, assembly.getName());
    }

    public OntologyManager getOntologyManager(String species, String assembly) throws CellBaseException {
        String multiKey = getMultiKey(species, assembly);
        if (!ontologyManagers.containsKey(multiKey)) {
            if (!validateSpeciesAssembly(species, assembly)) {
                throw new CellBaseException("Invalid species " + species + " or assembly " + assembly);
            }
            ontologyManagers.put(multiKey, new OntologyManager(species, assembly, configuration));
        }
        return ontologyManagers.get(multiKey);
    }

    public DataReleaseManager getDataRelesaseManager(String species) throws CellBaseException {
        if (species == null) {
            throw new CellBaseException("Species is required.");
        }
        SpeciesConfiguration.Assembly assembly = SpeciesUtils.getDefaultAssembly(configuration, species);
        return getDataReleaseManager(species, assembly.getName());
    }

    public DataReleaseManager getDataReleaseManager(String species, String assembly) throws CellBaseException {
        String multiKey = getMultiKey(species, assembly);
        if (!dataReleaseManagers.containsKey(multiKey)) {
            if (!validateSpeciesAssembly(species, assembly)) {
                throw new CellBaseException("Invalid species " + species + " or assembly " + assembly);
            }
            dataReleaseManagers.put(multiKey, new DataReleaseManager(species, assembly, configuration));
        }
        return dataReleaseManagers.get(multiKey);
    }
//    public OntologyManager getOntologyManager(String species) throws CellBaseException {
//        if (species == null) {
//            throw new CellBaseException("Species is required.");
//        }
//        SpeciesConfiguration.Assembly assembly = SpeciesUtils.getDefaultAssembly(configuration, species);
//        return getOntologyManager(species, assembly.getName());
//    }

    public PublicationManager getPublicationManager() throws CellBaseException {
        if (publicationManager == null) {
            publicationManager = new PublicationManager(configuration);
        }
        return publicationManager;
    }

    public PharmacogenomicsManager getPharmacogenomicsManager(String species) throws CellBaseException {
        if (species == null) {
            throw new CellBaseException("Species is required.");
        }
        SpeciesConfiguration.Assembly assembly = SpeciesUtils.getDefaultAssembly(configuration, species);
        return getPharmacogenomicsManager(species, assembly.getName());
    }

    public PharmacogenomicsManager getPharmacogenomicsManager(String species, String assembly) throws CellBaseException {
        String multiKey = getMultiKey(species, assembly);
        if (!pharmacogenomicsManagers.containsKey(multiKey)) {
            if (!validateSpeciesAssembly(species, assembly)) {
                throw new CellBaseException("Invalid species " + species + " or assembly " + assembly);
            }
            pharmacogenomicsManagers.put(multiKey, new PharmacogenomicsManager(species, assembly, configuration));
        }
        return pharmacogenomicsManagers.get(multiKey);
    }
}
