package org.opencb.cellbase.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by parce on 12/02/15.
 */
public class CellBaseConfiguration {

    private String version;
    private String apiVersion;
    private String wiki;
    private DatabaseProperties database;
    private String defaultOutdir;
    private DownloadProperties download;
    private SpeciesProperties species;

    public static CellBaseConfiguration load(Path cellbaseConfigurationJsonFile) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        CellBaseConfiguration properties = jsonMapper.readValue(cellbaseConfigurationJsonFile.toFile(), CellBaseConfiguration.class);
        return properties;
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

    public static class DatabaseProperties {
        private String host;
        private String port;

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

        private String user;
        private String password;
    }

    public static class DownloadProperties {
        private EnsemblProperties ensembl;
        private EnsemblProperties ensemblGenomes;

        private URLProperties uniprot;

        private URLProperties clinvar;

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

        public static class EnsemblProperties {
            public DatabaseProperties getDatabase() {
                return database;
            }

            public void setDatabase(DatabaseProperties database) {
                this.database = database;
            }

            public URLProperties getUrl() {
                return url;
            }

            public void setUrl(URLProperties url) {
                this.url = url;
            }

            private DatabaseProperties database;
            private URLProperties url;
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
