package org.opencb.cellbase.lib.builders;

import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.SpeciesConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;

class EnsemblGeneBuilderTest {

    public void testGeneBuilder() throws Exception {
        Path downloadPath = Paths.get("/home/jtarraga/data/cellbase/cb6/v6.1.0-dr1/homo_sapiens_grch38/download/gene");
        Path buildPath = Paths.get("/home/jtarraga/data/cellbase/cb6/v6.1.0-dr1/homo_sapiens_grch38/generated_json/gene");
        boolean flexibleGTFParsing = false;
        CellBaseConfiguration configuration = CellBaseConfiguration.load(Paths.get("/home/jtarraga/appl/cellbase/build/conf/configuration.yml"));
        SpeciesConfiguration speciesConfiguration = configuration.getSpeciesConfig("hsapiens");

        GeneBuilder geneBuilder = new GeneBuilder(downloadPath, buildPath, speciesConfiguration, flexibleGTFParsing);
        geneBuilder.check();
        geneBuilder.parse();
    }
}