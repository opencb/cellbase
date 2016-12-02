package org.opencb.cellbase.lib.impl;

import htsjdk.samtools.SAMRecord;
import htsjdk.variant.variantcontext.VariantContext;
import org.apache.commons.lang3.time.StopWatch;
import org.ga4gh.models.ReadAlignment;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.tools.alignment.AlignmentOptions;
import org.opencb.biodata.tools.alignment.BamManager;
import org.opencb.biodata.tools.alignment.filters.AlignmentFilters;
import org.opencb.biodata.tools.alignment.filters.SamRecordFilters;
import org.opencb.biodata.tools.variant.VariantOptions;
import org.opencb.biodata.tools.variant.VcfManager;
import org.opencb.biodata.tools.variant.filters.VariantContextFilters;
import org.opencb.biodata.tools.variant.filters.VariantFilters;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by swaathi on 02/12/16.
 */
public class FilesDBAdaptor extends MongoDBAdaptor {

    public FilesDBAdaptor(CellBaseConfiguration cellBaseConfiguration) {
        super(cellBaseConfiguration);
    }

    public QueryResult<File> list(Path path) throws IOException {
        FileUtils.checkDirectory(path);
        List<File> files = new ArrayList<>();
        File[] fileArray = path.toFile().listFiles();

        if (fileArray != null) {
            files = new ArrayList<>(Arrays.asList(fileArray));
        }
        return new QueryResult(path.toString(), 0, files.size(), files.size(), null, null, files);
    }

    public QueryResult<File> listByFolder(Path path, Query query) throws IOException {
        FileUtils.checkDirectory(path);

        List<File> files = new ArrayList<>();
        File[] fileArray;
        if (query != null && query.get("format") != null) {
            fileArray = path.toFile().listFiles((dir, name) -> name.endsWith("." + query.getString("format")));
        } else {
            fileArray = path.toFile().listFiles();
        }

        if (fileArray != null) {
            files = new ArrayList<>(Arrays.asList(fileArray));
        }
        return new QueryResult("", 0, files.size(), files.size(), null, null, files);
    }

    public QueryResult<ReadAlignment> getBamFile(Path path, Query query, QueryOptions queryOptions) throws Exception {
        StopWatch watch = new StopWatch();
        watch.start();
        FileUtils.checkFile(path);

        BamManager alignmentManager = new BamManager(path);
        AlignmentFilters<SAMRecord> filters = SamRecordFilters.create().addMappingQualityFilter(query.getInt("mapQ", 0));
        AlignmentOptions options = new AlignmentOptions();

        String queryResultId = null;
        List<ReadAlignment> readAlignmentList = new ArrayList<>();
        String regionString = query.getString("region");
        if (regionString != null && !regionString.isEmpty()) {
            readAlignmentList = alignmentManager.query(Region.parseRegion(regionString), filters, options, ReadAlignment.class);
            queryResultId = regionString;
        } else {
            readAlignmentList = alignmentManager.query(filters, options, ReadAlignment.class);
        }

        alignmentManager.close();
        watch.stop();

        return new QueryResult(queryResultId, ((int) watch.getTime()), readAlignmentList.size(), readAlignmentList.size(),
                null, null, readAlignmentList);
    }

    public QueryResult<Variant> getVcfFile(Path path, Query query, QueryOptions queryOptions) throws Exception {
        StopWatch watch = new StopWatch();
        watch.start();
        FileUtils.checkFile(path);

        VcfManager vcfManager = new VcfManager(path);
        VariantFilters<VariantContext> variantContextVariantFilters = new VariantContextFilters();
        VariantOptions variantOptions = new VariantOptions();
        variantContextVariantFilters.addQualFilter(query.getDouble("qual", 0));
        if (query.get("filter") != null) {
            variantContextVariantFilters.addPassFilter(query.getString("filter"));
        }

        String regionString = query.getString("region");
        String queryResultId = null;
        List<Variant> variants;
        if (regionString != null && !regionString.isEmpty()) {
            variants = vcfManager.query(Region.parseRegion(regionString), variantContextVariantFilters, variantOptions, Variant.class);
            queryResultId = regionString;
        } else {
            variants = vcfManager.query(null, variantContextVariantFilters, variantOptions, Variant.class);
        }
        vcfManager.close();
        watch.stop();

        return new QueryResult(queryResultId, ((int) watch.getTime()), variants.size(), variants.size(), null, null,
                variants);
    }
}
