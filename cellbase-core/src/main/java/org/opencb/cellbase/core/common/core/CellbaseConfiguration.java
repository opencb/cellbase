package org.opencb.cellbase.core.common.core;

import org.opencb.biodata.formats.drug.drugbank.v201312jaxb.Species;
import org.opencb.cellbase.core.common.variation.StructuralVariation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by javi on 16/09/14.
 */
public class CellbaseConfiguration {

    private int coreChunkSize = 5000;
    private int variationChunkSize = 1000;
    private int genomeSequenceChunkSize;
    private Map<String, String> speciesAlias = new HashMap<>();
    private Map<String, Species> availableSpeciesInfo = new HashMap<>();
    private Map<String, ConnectionParameters> availableSpeciesConnection = new HashMap<>();;

    class ConnectionParameters {
        private String host;
        private int port;
        private String driverClass;
        private String username;
        private String pass;
        private int maxPoolSize;
        private int timeout;

        public ConnectionParameters(String host, int port, String driverClass, String username, String pass,
                                    int maxPoolSize, int timeout) {
            this.host = host;
            this.port = port;
            this.driverClass = driverClass;
            this.username = username;
            this.pass = pass;
            this.maxPoolSize = maxPoolSize;
            this.timeout = timeout;
        }

    }

    public CellbaseConfiguration() { super(); }

    public void setCoreChunkSize(int coreChunkSize){ this.coreChunkSize = coreChunkSize; }

    public void setVariationChunkSize(int variationChunkSize){ this.variationChunkSize = variationChunkSize; }

    public void setGenomeSequenceChunkSize(int genomeSequenceChunkSize){ this.genomeSequenceChunkSize = genomeSequenceChunkSize; }

    public void addSpeciesInfo(String speciesId, String assembly, String taxonomy) {
        availableSpeciesInfo.put(speciesId, new Species(speciesid, assembly, taxonomy));
    }

    public void addSpeciesConnection(String speciesId, String host, int port, String driverClass, String username, String pass,
                               int maxPoolSize, int timeout) {
        availableSpeciesConnection.put(speciesId, new ConnectionParameters(host, port, driverClass, username, pass,
                                                                            maxPoolSize, timeout));
    }

    public void configaddSpeciesAlias(String al, String species){}

    public int getCoreChunkSize() { return coreChunkSize; }

    public int getVariationChunkSize() { return variationChunkSize; }

    public int getGenomeSequenceChunkSize() { return genomeSequenceChunkSize; }



}
