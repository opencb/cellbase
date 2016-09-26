package org.opencb.cellbase.app.transform;

import org.opencb.cellbase.app.cli.EtlCommons;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.rocksdb.RocksDB;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by fjlopez on 26/09/16.
 */
public class ClinicalVariantParser extends CellBaseParser {

    private final Path clinvarXMLFile;
    private final Path clinvarSummaryFile;
    private final Path clinvarEFOFile;
    private final Path cosmicFile;
    private final Path gwasFile;
    private final Path dbsnpFile;
    private final String assembly;

    public ClinicalVariantParser(Path clinicalVariantFolder, String assembly, CellBaseSerializer serializer) {
        this(clinicalVariantFolder.resolve(EtlCommons.CLINVAR_XML_FILE),
                clinicalVariantFolder.resolve(EtlCommons.CLINVAR_SUMMARY_FILE),
                clinicalVariantFolder.resolve(EtlCommons.CLINVAR_EFO_FILE),
                clinicalVariantFolder.resolve(EtlCommons.COSMIC_FILE),
                clinicalVariantFolder.resolve(EtlCommons.GWAS_FILE),
                clinicalVariantFolder.resolve(EtlCommons.DBSNP_FILE), assembly, serializer);
    }

    public ClinicalVariantParser(Path clinvarXMLFile, Path clinvarSummaryFile, Path clinvarEFOFile,
                                 Path cosmicFile, Path gwasFile, Path dbsnpFile, String assembly,
                                 CellBaseSerializer serializer) {
        super(serializer);
        this.clinvarXMLFile = clinvarXMLFile;
        this.clinvarSummaryFile = clinvarSummaryFile;
        this.clinvarEFOFile = clinvarEFOFile;
        this.cosmicFile = cosmicFile;
        this.gwasFile = gwasFile;
        this.dbsnpFile = dbsnpFile;
        this.assembly = assembly;
    }

    public void parse() throws IOException {

        RocksDB rdb = initializeRDB();

        if (this.clinvarXMLFile != null) {
            ClinVarIndexer clinvarIndexer = new ClinVarIndexer(RocksDB rdb, clinvarXMLFile, clinvarSummaryFile,
                    clinvarEFOFile);
            clinvarIndexer.index();
        }

        if (this.cosmicFile != null) {
            CosmicIndexer cosmicIndexer = new CosmicIndexer(RocksDB rdb, cosmicFile);
            cosmicIndexer.index();
        }

        if (this.gwasFile != null) {
            GwasIndexer cosmicIndexer = new GwasIndexer(RocksDB rdb, gwasFile);
            cosmicIndexer.index();
        }

        serializeRDB(rdb);

        rdb.close();

        serializer.close();
    }
}
