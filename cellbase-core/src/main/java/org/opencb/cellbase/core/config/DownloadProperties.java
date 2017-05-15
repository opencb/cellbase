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

package org.opencb.cellbase.core.config;

/**
 * Created by imedina on 19/08/16.
 */
public class DownloadProperties {

    private EnsemblProperties ensembl;
    private EnsemblProperties ensemblGenomes;

    private URLProperties geneUniprotXref;
    private URLProperties geneExpressionAtlas;
    private URLProperties mirbase;
    private URLProperties mirbaseReadme;
    private URLProperties targetScan;
    private URLProperties miRTarBase;
    private URLProperties uniprot;
    private URLProperties uniprotRelNotes;
    private URLProperties intact;
    private URLProperties interpro;
    private URLProperties interproRelNotes;
    private URLProperties conservation;
    private URLProperties gerp;
    private URLProperties clinvar;
    private URLProperties clinvarSummary;
    private URLProperties clinvarEfoTerms;
    private URLProperties dgv;
    private URLProperties simpleRepeats;
    private URLProperties windowMasker;
    private URLProperties genomicSuperDups;
    private URLProperties hpo;
    private URLProperties disgenet;
    private URLProperties disgenetReadme;
    private URLProperties dgidb;
    private URLProperties gwasCatalog;
    private URLProperties dbsnp;
    private URLProperties cadd;
    private URLProperties reactome;


    public EnsemblProperties getEnsembl() {
        return ensembl;
    }

    public DownloadProperties setEnsembl(EnsemblProperties ensembl) {
        this.ensembl = ensembl;
        return this;
    }

    public EnsemblProperties getEnsemblGenomes() {
        return ensemblGenomes;
    }

    public DownloadProperties setEnsemblGenomes(EnsemblProperties ensemblGenomes) {
        this.ensemblGenomes = ensemblGenomes;
        return this;
    }

    public URLProperties getGeneUniprotXref() {
        return geneUniprotXref;
    }

    public DownloadProperties setGeneUniprotXref(URLProperties geneUniprotXref) {
        this.geneUniprotXref = geneUniprotXref;
        return this;
    }

    public URLProperties getGeneExpressionAtlas() {
        return geneExpressionAtlas;
    }

    public DownloadProperties setGeneExpressionAtlas(URLProperties geneExpressionAtlas) {
        this.geneExpressionAtlas = geneExpressionAtlas;
        return this;
    }

    public URLProperties getMirbase() {
        return mirbase;
    }

    public DownloadProperties setMirbase(URLProperties mirbase) {
        this.mirbase = mirbase;
        return this;
    }

    public URLProperties getMirbaseReadme() {
        return mirbaseReadme;
    }

    public DownloadProperties setMirbaseReadme(URLProperties mirbaseReadme) {
        this.mirbaseReadme = mirbaseReadme;
        return this;
    }

    public URLProperties getTargetScan() {
        return targetScan;
    }

    public DownloadProperties setTargetScan(URLProperties targetScan) {
        this.targetScan = targetScan;
        return this;
    }

    public URLProperties getMiRTarBase() {
        return miRTarBase;
    }

    public DownloadProperties setMiRTarBase(URLProperties miRTarBase) {
        this.miRTarBase = miRTarBase;
        return this;
    }

    public URLProperties getUniprot() {
        return uniprot;
    }

    public DownloadProperties setUniprot(URLProperties uniprot) {
        this.uniprot = uniprot;
        return this;
    }

    public URLProperties getUniprotRelNotes() {
        return uniprotRelNotes;
    }

    public DownloadProperties setUniprotRelNotes(URLProperties uniprotRelNotes) {
        this.uniprotRelNotes = uniprotRelNotes;
        return this;
    }

    public URLProperties getIntact() {
        return intact;
    }

    public DownloadProperties setIntact(URLProperties intact) {
        this.intact = intact;
        return this;
    }

    public URLProperties getInterpro() {
        return interpro;
    }

    public DownloadProperties setInterpro(URLProperties interpro) {
        this.interpro = interpro;
        return this;
    }

    public URLProperties getInterproRelNotes() {
        return interproRelNotes;
    }

    public DownloadProperties setInterproRelNotes(URLProperties interproRelNotes) {
        this.interproRelNotes = interproRelNotes;
        return this;
    }

    public URLProperties getConservation() {
        return conservation;
    }

    public DownloadProperties setConservation(URLProperties conservation) {
        this.conservation = conservation;
        return this;
    }

    public URLProperties getGerp() {
        return gerp;
    }

    public DownloadProperties setGerp(URLProperties gerp) {
        this.gerp = gerp;
        return this;
    }

    public URLProperties getClinvar() {
        return clinvar;
    }

    public DownloadProperties setClinvar(URLProperties clinvar) {
        this.clinvar = clinvar;
        return this;
    }

    public URLProperties getClinvarSummary() {
        return clinvarSummary;
    }

    public DownloadProperties setClinvarSummary(URLProperties clinvarSummary) {
        this.clinvarSummary = clinvarSummary;
        return this;
    }

    public URLProperties getClinvarEfoTerms() {
        return clinvarEfoTerms;
    }

    public DownloadProperties setClinvarEfoTerms(URLProperties clinvarEfoTerms) {
        this.clinvarEfoTerms = clinvarEfoTerms;
        return this;
    }

    public URLProperties getDgv() {
        return dgv;
    }

    public void setDgv(URLProperties dgv) {
        this.dgv = dgv;
    }

    public URLProperties getSimpleRepeats() {
        return simpleRepeats;
    }

    public void setSimpleRepeats(URLProperties simpleRepeats) {
        this.simpleRepeats = simpleRepeats;
    }

    public URLProperties getGenomicSuperDups() {
        return genomicSuperDups;
    }

    public void setGenomicSuperDups(URLProperties genomicSuperDups) {
        this.genomicSuperDups = genomicSuperDups;
    }

    public URLProperties getWindowMasker() {
        return windowMasker;
    }

    public void setWindowMasker(URLProperties windowMasker) {
        this.windowMasker = windowMasker;
    }

    public URLProperties getHpo() {
        return hpo;
    }

    public DownloadProperties setHpo(URLProperties hpo) {
        this.hpo = hpo;
        return this;
    }

    public URLProperties getDisgenet() {
        return disgenet;
    }

    public DownloadProperties setDisgenet(URLProperties disgenet) {
        this.disgenet = disgenet;
        return this;
    }

    public URLProperties getDisgenetReadme() {
        return disgenetReadme;
    }

    public DownloadProperties setDisgenetReadme(URLProperties disgenetReadme) {
        this.disgenetReadme = disgenetReadme;
        return this;
    }

    public URLProperties getDgidb() {
        return dgidb;
    }

    public DownloadProperties setDgidb(URLProperties dgidb) {
        this.dgidb = dgidb;
        return this;
    }

    public URLProperties getGwasCatalog() {
        return gwasCatalog;
    }

    public DownloadProperties setGwasCatalog(URLProperties gwasCatalog) {
        this.gwasCatalog = gwasCatalog;
        return this;
    }

    public URLProperties getDbsnp() {
        return dbsnp;
    }

    public DownloadProperties setDbsnp(URLProperties dbsnp) {
        this.dbsnp = dbsnp;
        return this;
    }

    public URLProperties getCadd() {
        return cadd;
    }

    public DownloadProperties setCadd(URLProperties cadd) {
        this.cadd = cadd;
        return this;
    }

    public URLProperties getReactome() {
        return reactome;
    }

    public DownloadProperties setReactome(URLProperties reactome) {
        this.reactome = reactome;
        return this;
    }

    public static class EnsemblProperties {

        private DatabaseCredentials database;
        private String libs;
        private URLProperties url;

        public DatabaseCredentials getDatabase() {
            return database;
        }

        public void setDatabase(DatabaseCredentials database) {
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
