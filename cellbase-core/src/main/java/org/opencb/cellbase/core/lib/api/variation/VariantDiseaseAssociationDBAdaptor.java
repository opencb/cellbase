package org.opencb.cellbase.core.lib.api.variation;

import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.Position;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;

/**
 * Created by antonior on 11/18/14.
 */
public interface VariantDiseaseAssociationDBAdaptor {


    public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options);

    public QueryResult getAllByPosition(Position position, QueryOptions options);

    public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options);

    public QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options);

    public QueryResult getAllByRegion(Region region, QueryOptions options);

    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options);

    public QueryResult getAllByGenomicVariant(GenomicVariant variant, QueryOptions options);

    public List<QueryResult> getAllByGenomicVariantList(List<GenomicVariant> variantList, QueryOptions options);







}
