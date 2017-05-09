package org.opencb.cellbase.app.transform;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fjlopez on 08/05/17.
 */
public class DgvParser extends CellBaseParser {

    private static final int VARIANT_SUBTYPE_COLUMN = 5;
    private final Path file;

    public DgvParser(Path file, CellBaseSerializer serializer) {
        super(serializer);
        this.file = file;
    }

    @Override
    public void parse() throws Exception {

        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(file)) {
            // Skip header
            bufferedReader.readLine();
            String line = bufferedReader.readLine();
            while(line != null) {
                List<Variant> variantList = parseVariants(line);
                line = bufferedReader.readLine();

                for (Variant variant : variantList) {
                    serializer.serialize(variant.getImpl());
                }
            }
        }
    }

    private List<Variant> parseVariants(String line) {
        String[] fields = line.split("\t");
        String[] variantSubtypes = fields[VARIANT_SUBTYPE_COLUMN].split("\\+");
        List<Variant> variantList = new ArrayList<>(variantSubtypes.length);

        for (String subtype : variantSubtypes) {
            variantList.add(new Variant(fields[CHR_COLUMN], Integer.valueOf(fields[START_COLUMN]),
                    Integer.valueOf(fields[END_COLUMN]), UNKNOWN_NT, ));
        }

    }
}
