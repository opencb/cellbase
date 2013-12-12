package org.opencb.cellbase.core.lib.api;


import org.opencb.cellbase.core.common.GenericFeature;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.List;

public interface RegulatoryRegionDBAdaptor extends FeatureDBAdaptor {


    public QueryResult getAllById(String id, QueryOptions options);

    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);


    public QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options);

    public QueryResult getAllByRegion(Region region, QueryOptions options);

    public List<QueryResult> getAllByRegionList(List<Region> region, QueryOptions options);



//    public List<GenericFeature> getByRegion(String chromosome, int start, int end, List<String> types);
//
//    public List<List<GenericFeature>> getByRegionList(List<Region> regions);
//
//    public List<List<GenericFeature>> getByRegionList(List<Region> regions, List<String> types);


}
