package org.opencb.cellbase.core.lib.api.variation;

import org.opencb.biodata.models.feature.Region;
import org.opencb.cellbase.core.common.Position;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;

/**
 * Created by imedina on 26/09/14.
 */
@Deprecated
public interface ClinVarDBAdaptor {

    public QueryResult getById(String id, QueryOptions options);

    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);


    public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options);

    public QueryResult getAllByPosition(Position position, QueryOptions options);

    public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options);


    public QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options);

    public QueryResult getAllByRegion(Region region, QueryOptions options);

    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options);

    public QueryResult getListAccessions(QueryOptions queryOptions);

}
