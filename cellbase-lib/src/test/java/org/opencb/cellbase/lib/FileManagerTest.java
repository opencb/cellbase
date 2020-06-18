package org.opencb.cellbase.lib;

import org.junit.Test;
import org.junit.platform.commons.util.CollectionUtils;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.tools.feature.BigWigManager;
import org.opencb.cellbase.core.api.queries.FileQuery;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.loader.LoadRunner;
import org.opencb.cellbase.lib.impl.core.MongoDBAdaptorFactory;
import org.opencb.cellbase.lib.managers.CellBaseManagerFactory;
import org.opencb.cellbase.lib.managers.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class FileManagerTest {

    protected CellBaseConfiguration cellBaseConfiguration;
    protected CellBaseManagerFactory cellBaseManagerFactory;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public FileManagerTest() throws IOException {
        cellBaseConfiguration = CellBaseConfiguration.load(
                FileManagerTest.class.getClassLoader().getResourceAsStream("configuration.test.yaml"),
                CellBaseConfiguration.ConfigurationFileFormat.YAML);
        cellBaseManagerFactory = new CellBaseManagerFactory(cellBaseConfiguration);
    }

    public void query(Path inputPath, String chrom, int start, int end, boolean display) throws Exception {
        BigWigManager bigWigManager = new BigWigManager(inputPath);
        Region region = new Region(chrom, start, end);
        double[] coverage = bigWigManager.query(region);

        if (display) {
            for (double v : coverage) {
                System.out.println((start++) + " :" + v);
            }
        }

        assertEquals(region.getEnd() - region.getStart() + 1, coverage.length);
    }

    @Test
    public void query1() throws Exception {

        // just testing the biodata
        Path bwPath = Paths.get(getClass().getResource("/wigVarStepExampleSmallChr21.bw").toURI());
        query(bwPath, "chr21", 9411190, 9411200, true);
        FileManager manager =  cellBaseManagerFactory.getFileManager();

        Region region = new Region("chr21", 9411190, 9411200);
        FileQuery query = new FileQuery();
        query.setFilePath(bwPath.toString());
        query.setRegions(Collections.singletonList(region));
        query.setFileType("bigwig");
        manager.search(query);
    }

}
