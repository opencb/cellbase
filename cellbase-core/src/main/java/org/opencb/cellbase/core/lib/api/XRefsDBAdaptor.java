package org.opencb.cellbase.core.lib.api;

import org.opencb.biodata.models.core.DBName;
import org.opencb.biodata.models.core.Xref;
import org.opencb.cellbase.core.common.XRefs;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;


public interface XRefsDBAdaptor {


    public List<DBName> getAllDBNames();

    public List<DBName> getAllDBNamesById(String id);

    public List<String> getAllTypes();

    public List<DBName> getAllDBNamesByType(String type);

    public List<String> getAllIdsByDBName(String dbname);


    public List<Xref> getById(String id);

    public List<List<Xref>> getAllByIdList(List<String> idList);


    public QueryResult getByStartsWithQuery(String id, QueryOptions options);

    public List<QueryResult> getByStartsWithQueryList(List<String> ids, QueryOptions options);

    public QueryResult getByStartsWithSnpQuery(String id, QueryOptions options);

    public List<QueryResult> getByStartsWithSnpQueryList(List<String> ids, QueryOptions options);


    public List<Xref> getByContainsQuery(String likeQuery);

    public List<List<Xref>> getByContainsQueryList(List<String> likeQuery);

    public XRefs getById(String id, String type);

    public List<XRefs> getAllByIdList(List<String> ids, String type);


    public List<Xref> getByDBName(String id, String dbname);

    public List<List<Xref>> getAllByDBName(List<String> ids, String dbname);

//	public List<Xref> getByDBNameList(String id, List<String> dbnames);

//	public List<List<Xref>> getAllByDBNameList(List<String> ids, List<String> dbnames);

    public QueryResult getByDBNameList(String id, QueryOptions options);

    public List<QueryResult> getAllByDBNameList(List<String> ids, QueryOptions options);


    public XRefs getByDBName(String id, String dbname, String type);

    public List<XRefs> getAllByDBName(List<String> ids, String dbname, String type);

    public XRefs getByDBNameList(String id, List<String> dbnames, String type);

    public List<XRefs> getAllByDBNameList(List<String> ids, List<String> dbnames, String type);


}
