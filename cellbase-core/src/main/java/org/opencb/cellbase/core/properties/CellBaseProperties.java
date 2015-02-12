package org.opencb.cellbase.core.properties;

import java.util.List;

/**
 * Created by parce on 12/02/15.
 */
public class CellBaseProperties {

    private String version;
    private String apiVersion;
    private String wiki;
    private DatabaseProperties database;
    private String defaultOutdir;
    private DownloadProperties download;
    private SpeciesProperties species;

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
        private List<Specie> vertebrates;
        private List<Specie> metazoa;
        private List<Specie> fungi;
        private List<Specie> protist;
        private List<Specie> plants;

        public List<Specie> getVertebrates() {
            return vertebrates;
        }

        public void setVertebrates(List<Specie> vertebrates) {
            this.vertebrates = vertebrates;
        }

        public List<Specie> getMetazoa() {
            return metazoa;
        }

        public void setMetazoa(List<Specie> metazoa) {
            this.metazoa = metazoa;
        }

        public List<Specie> getFungi() {
            return fungi;
        }

        public void setFungi(List<Specie> fungi) {
            this.fungi = fungi;
        }

        public List<Specie> getProtist() {
            return protist;
        }

        public void setProtist(List<Specie> protist) {
            this.protist = protist;
        }

        public List<Specie> getPlants() {
            return plants;
        }

        public void setPlants(List<Specie> plants) {
            this.plants = plants;
        }

        public static class Specie {
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
