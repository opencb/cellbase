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

package org.opencb.cellbase.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by parce on 12/02/15.
 */
public class CellBaseConfiguration {

    private String version;
    private String apiVersion;
    private String wiki;
    private String defaultOutdir;
    private DatabaseProperties database;
    private DownloadProperties download;
    private SpeciesProperties species;


    public static CellBaseConfiguration load(InputStream configurationInputStream) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        return jsonMapper.readValue(configurationInputStream, CellBaseConfiguration.class);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getWiki() {
        return wiki;
    }

    public void setWiki(String wiki) {
        this.wiki = wiki;
    }

    public DatabaseProperties getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseProperties database) {
        this.database = database;
    }

    public String getDefaultOutdir() {
        return defaultOutdir;
    }

    public void setDefaultOutdir(String defaultOutdir) {
        this.defaultOutdir = defaultOutdir;
    }

    public DownloadProperties getDownload() {
        return download;
    }

    public void setDownload(DownloadProperties download) {
        this.download = download;
    }

    public SpeciesProperties getSpecies() {
        return species;
    }

    public void setSpecies(SpeciesProperties species) {
        this.species = species;
    }

    public List<SpeciesProperties.Species> getAllSpecies() {
        List<SpeciesProperties.Species> allSpecies = new ArrayList<>();
        allSpecies.addAll(species.getVertebrates());
        allSpecies.addAll(species.getMetazoa());
        allSpecies.addAll(species.getFungi());
        allSpecies.addAll(species.getProtist());
        allSpecies.addAll(species.getPlants());

        return allSpecies;
    }

    public static class DatabaseProperties {
        private String host;
        @Deprecated
        private String port;
        private String user;
        private String password;
        private Map<String, String> options;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Map<String, String> getOptions() {
            return options;
        }

        public void setOptions(Map<String, String> options) {
            this.options = options;
        }
    }

    public static class DownloadProperties {
        private EnsemblProperties ensembl;
        private EnsemblProperties ensemblGenomes;

        private URLProperties geneUniprotXref;
        private URLProperties geneExpressionAtlas;
        private URLProperties mirbase;
        private URLProperties targetScan;
        private URLProperties miRTarBase;
        private URLProperties uniprot;
        private URLProperties intact;
        private URLProperties interpro;
        private URLProperties conservation;
        private URLProperties gerp;
        private URLProperties clinvar;
        private URLProperties clinvarSummary;
        private URLProperties clinvarEfoTerms;
        private URLProperties hpo;
        private URLProperties disgenet;
        private URLProperties dgidb;
        private URLProperties gwasCatalog;
        private URLProperties dbsnp;
        private URLProperties cadd;


        public EnsemblProperties getEnsembl() {
            return ensembl;
        }

        public void setEnsembl(EnsemblProperties ensembl) {
            this.ensembl = ensembl;
        }

        public EnsemblProperties getEnsemblGenomes() {
            return ensemblGenomes;
        }

        public void setEnsemblGenomes(EnsemblProperties ensemblGenomes) {
            this.ensemblGenomes = ensemblGenomes;
        }

        public URLProperties getUniprot() {
            return uniprot;
        }

        public void setUniprot(URLProperties uniprot) {
            this.uniprot = uniprot;
        }

        public URLProperties getClinvar() {
            return clinvar;
        }

        public void setClinvar(URLProperties clinvar) {
            this.clinvar = clinvar;
        }

        public URLProperties getClinvarSummary() {
            return clinvarSummary;
        }

        public void setClinvarSummary(URLProperties clinvarSummary) {
            this.clinvarSummary = clinvarSummary;
        }

        public URLProperties getClinvarEfoTerms() {
            return clinvarEfoTerms;
        }

        public void setClinvarEfoTerms(URLProperties clinvarEfoTerms) {
            this.clinvarEfoTerms = clinvarEfoTerms;
        }

        public URLProperties getHpo() {
            return hpo;
        }

        public void setHpo(URLProperties hpo) {
            this.hpo = hpo;
        }

        public URLProperties getDisgenet() {
            return disgenet;
        }

        public void setDisgenet(URLProperties disgenet) {
            this.disgenet = disgenet;
        }

        public URLProperties getDgidb() {
            return dgidb;
        }

        public void setDgidb(URLProperties dgidb) {
            this.dgidb = dgidb;
        }

        public URLProperties getConservation() {
            return conservation;
        }

        public void setConservation(URLProperties conservation) {
            this.conservation = conservation;
        }

        public URLProperties getGerp() {
            return gerp;
        }

        public void setGerp(URLProperties gerp) {
            this.gerp = gerp;
        }

        public URLProperties getIntact() {
            return intact;
        }

        public void setIntact(URLProperties intact) {
            this.intact = intact;
        }

        public URLProperties getInterpro() {
            return interpro;
        }

        public void setInterpro(URLProperties interpro) {
            this.interpro = interpro;
        }

        public URLProperties getGeneExpressionAtlas() {
            return geneExpressionAtlas;
        }

        public void setGeneExpressionAtlas(URLProperties geneExpressionAtlas) {
            this.geneExpressionAtlas = geneExpressionAtlas;
        }

        public URLProperties getGeneUniprotXref() {
            return geneUniprotXref;
        }

        public void setGeneUniprotXref(URLProperties geneUniprotXref) {
            this.geneUniprotXref = geneUniprotXref;
        }

        public URLProperties getGwasCatalog() {
            return gwasCatalog;
        }

        public void setGwasCatalog(URLProperties gwasCatalog) {
            this.gwasCatalog = gwasCatalog;
        }

        public URLProperties getDbsnp() {
            return dbsnp;
        }

        public void setDbsnp(URLProperties dbsnp) {
            this.dbsnp = dbsnp;
        }

        public URLProperties getMirbase() {
            return mirbase;
        }

        public void setMirbase(URLProperties mirbase) {
            this.mirbase = mirbase;
        }

        public URLProperties getTargetScan() {
            return targetScan;
        }

        public void setTargetScan(URLProperties targetScan) {
            this.targetScan = targetScan;
        }

        public URLProperties getMiRTarBase() {
            return miRTarBase;
        }

        public void setMiRTarBase(URLProperties miRTarBase) {
            this.miRTarBase = miRTarBase;
        }

        public URLProperties getCadd() {
            return cadd;
        }

        public void setCadd(URLProperties cadd) {
            this.cadd = cadd;
        }

        public static class EnsemblProperties {
            private DatabaseProperties database;
            private String libs;
            private URLProperties url;

            public DatabaseProperties getDatabase() {
                return database;
            }

            public void setDatabase(DatabaseProperties database) {
                this.database = database;
            }

            public String getLibs() {
                return libs;
            }

            public void setLibs(String libs) {
                this.libs = libs;
            }

            public URLProperties getUrl() {
                return url;
            }

            public void setUrl(URLProperties url) {
                this.url = url;
            }
        }

        public static class URLProperties {
            private String host;

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }
        }
    }

    public static class SpeciesProperties {
        private List<Species> vertebrates;
        private List<Species> metazoa;
        private List<Species> fungi;
        private List<Species> protist;
        private List<Species> plants;

        public List<Species> getVertebrates() {
            return vertebrates;
        }

        public void setVertebrates(List<Species> vertebrates) {
            this.vertebrates = vertebrates;
        }

        public List<Species> getMetazoa() {
            return metazoa;
        }

        public void setMetazoa(List<Species> metazoa) {
            this.metazoa = metazoa;
        }

        public List<Species> getFungi() {
            return fungi;
        }

        public void setFungi(List<Species> fungi) {
            this.fungi = fungi;
        }

        public List<Species> getProtist() {
            return protist;
        }

        public void setProtist(List<Species> protist) {
            this.protist = protist;
        }

        public List<Species> getPlants() {
            return plants;
        }

        public void setPlants(List<Species> plants) {
            this.plants = plants;
        }

        public static class Species {

            private String id;
            private String scientificName;
            private String commonName;
            private List<Assembly> assemblies;
            private List<String> data;


            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder("Species{");
                sb.append("id='").append(id).append('\'');
                sb.append(", scientificName='").append(scientificName).append('\'');
                sb.append(", commonName='").append(commonName).append('\'');
                sb.append(", assemblies=").append(assemblies);
                sb.append(", data=").append(data);
                sb.append('}');
                return sb.toString();
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getScientificName() {
                return scientificName;
            }

            public void setScientificName(String scientificName) {
                this.scientificName = scientificName;
            }

            public String getCommonName() {
                return commonName;
            }

            public void setCommonName(String commonName) {
                this.commonName = commonName;
            }

            public List<Assembly> getAssemblies() {
                return assemblies;
            }

            public void setAssemblies(List<Assembly> assemblies) {
                this.assemblies = assemblies;
            }

            public List<String> getData() {
                return data;
            }

            public void setData(List<String> data) {
                this.data = data;
            }

            public static class Assembly {
                private String name;
                private String ensemblVersion;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getEnsemblVersion() {
                    return ensemblVersion;
                }

                public void setEnsemblVersion(String ensemblVersion) {
                    this.ensemblVersion = ensemblVersion;
                }
            }
        }
    }
}
