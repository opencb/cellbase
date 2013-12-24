package org.opencb.cellbase.lib.mongodb;

import com.mongodb.*;
import org.opencb.cellbase.core.common.GenomeSequenceFeature;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.common.core.GenomeSequenceChunk;
import org.opencb.cellbase.core.lib.api.GenomeSequenceDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Deprecated
public class GenomeSequenceMongoDBAdaptor extends MongoDBAdaptor implements GenomeSequenceDBAdaptor {

    public GenomeSequenceMongoDBAdaptor(DB db) {
        super(db);
    }

    public GenomeSequenceMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
        mongoDBCollection = db.getCollection("genome_sequence");
    }

    private int getChunk(int position) {
        return (position / Integer.parseInt(applicationProperties.getProperty("CELLBASE." + version.toUpperCase()
                + ".GENOME_SEQUENCE.CHUNK_SIZE", "2000")));
    }

    private int getOffset(int position) {
        return ((position) % Integer.parseInt(applicationProperties.getProperty("CELLBASE." + version.toUpperCase()
                + ".GENOME_SEQUENCE.CHUNK_SIZE", "2000")));
    }

    public static String getComplementarySequence(String sequence) {
        sequence = sequence.replace("A", "1");
        sequence = sequence.replace("T", "2");
        sequence = sequence.replace("C", "3");
        sequence = sequence.replace("G", "4");
        sequence = sequence.replace("1", "T");
        sequence = sequence.replace("2", "A");
        sequence = sequence.replace("3", "G");
        sequence = sequence.replace("4", "C");
        return sequence;
    }


    @Override
    public QueryResult getByRegion(String chromosome, int start, int end, QueryOptions options) {
        Region region = new Region(chromosome, start, end);
        return getAllByRegionList(Arrays.asList(region), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options) {
        /****/
        String chunkIdSuffix = Integer.parseInt(applicationProperties.getProperty("CELLBASE." + version.toUpperCase()
                + ".GENOME_SEQUENCE.CHUNK_SIZE", "2000")) / 1000 + "k";
        /****/

        List<DBObject> queries = new ArrayList<>();
        List<String> ids = new ArrayList<>(regions.size());
        List<String> chunkIds;
        List<Integer> integerChunkIds;
        for (Region region : regions) {
            chunkIds = new ArrayList<>();
            integerChunkIds = new ArrayList<>();
            // positions below 1 are not allowed
            if (region.getStart() < 1) {
                region.setStart(1);
            }
            if (region.getEnd() < 1) {
                region.setEnd(1);
            }

            /****/
            int regionChunkStart = getChunk(region.getStart());
            int regionChunkEnd = getChunk(region.getEnd());
            for (int chunkId = regionChunkStart; chunkId <= regionChunkEnd; chunkId++) {
                String chunkIdStr = region.getChromosome() + "_" + chunkId + "_" + chunkIdSuffix;
                chunkIds.add(chunkIdStr);
                integerChunkIds.add(chunkId);
            }
//            QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("chunkId").in(hunkIds);
            QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("chunkId").in(integerChunkIds);
            /****/

//            QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("chunkId")
//                    .greaterThanEquals(getChunk(region.getStart())).lessThanEquals(getChunk(region.getEnd()));
            queries.add(builder.get());
            ids.add(region.toString());

            logger.info(builder.get().toString());
        }

        List<QueryResult> queryResults = executeQueryList(ids, queries, options);


        for (int i = 0; i < regions.size(); i++) {
            Region region = regions.get(i);
            QueryResult queryResult = queryResults.get(i);

            BasicDBList list = (BasicDBList) queryResult.get("result");

            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < list.size(); j++) {
                BasicDBObject chunk = (BasicDBObject) list.get(j);
                sb.append(chunk.get("sequence"));
            }

            int startStr = getOffset(region.getStart());
            int endStr = getOffset(region.getStart()) + (region.getEnd() - region.getStart()) + 1;

            String subStr = "";

            if (getChunk(region.getEnd()) > 0) {
                if (sb.toString().length() > 0 && sb.toString().length() >= endStr) {
                    subStr = sb.toString().substring(startStr, endStr);
                }
            } else {
                if (sb.toString().length() > 0 && sb.toString().length() + 1 >= endStr) {
                    subStr = sb.toString().substring(startStr - 1, endStr - 1);
                }
            }
            GenomeSequenceFeature genomeSequenceFeature = new GenomeSequenceFeature(region.getChromosome(), region.getStart(), region.getEnd(), subStr);

            queryResult.setResult(genomeSequenceFeature);
        }

        return queryResults;
    }

    @Override
    public String getRevComp(String sequence) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    private List<GenomeSequenceChunk> executeQuery(DBObject query) {
        List<GenomeSequenceChunk> result = null;
        DBCursor cursor = mongoDBCollection.find(query);
        try {
            if (cursor != null) {
                result = new ArrayList<GenomeSequenceChunk>(cursor.size());
//				Gson jsonObjectMapper = new Gson();
                GenomeSequenceChunk chunk = null;
                while (cursor.hasNext()) {
//					chunk = (GenomeSequenceChunk) jsonObjectMapper.fromJson(cursor.next().toString(), GenomeSequenceChunk.class);
                    result.add(chunk);
                }
            }
        } finally {
            cursor.close();
        }
        return result;
    }


//	@Override
//	public GenomeSequenceFeature getByRegion(String chromosome, int start, int end) {
//		// positions below 1 are not allowed
//		if (start < 1) {
//			start = 1;
//		}
//        if (end < 1) {
//            end = 1;
//        }
//		QueryBuilder builder = QueryBuilder.start("chromosome").is(chromosome.trim()).and("chunkId")
//				.greaterThanEquals(getChunkId(start)).lessThanEquals(getChunkId(end));
//
//		System.out.println(builder.get().toString());
//		List<GenomeSequenceChunk> chunkList = executeQuery(builder.get());
//		StringBuilder sb = new StringBuilder();
//		for (GenomeSequenceChunk chunk : chunkList) {
//			sb.append(chunk.getSequence());
//		}
//
//		int startStr = getOffset(start);
//		int endStr = getOffset(start) + (end - start) + 1;
//		String subStr = "";
//		if (getChunkId(end) > 0) {
//			if (sb.toString().length() > 0 && sb.toString().length() >= endStr) {
//				subStr = sb.toString().substring(startStr, endStr);
//			}
//		} else {
//			if (sb.toString().length() > 0 && sb.toString().length() + 1 >= endStr) {
//				subStr = sb.toString().substring(startStr-1, endStr - 1);
//			}
//		}
//
//		return new GenomeSequenceFeature(chromosome, start, end, subStr);
//	}
//
//	@Override
//	public GenomeSequenceFeature getByRegion(String chromosome, int start, int end, int strand) {
//		GenomeSequenceFeature genomeSequence = this.getByRegion(chromosome, start, end);
//
//		if (strand == -1) {
//			genomeSequence.setSequence(getRevComp(genomeSequence.getSequence()));
//		}
//
//		return genomeSequence;
//	}
//
//	@Override
//	public List<GenomeSequenceFeature> getByRegionList(List<Region> regions) {
//		List<GenomeSequenceFeature> result = new ArrayList<GenomeSequenceFeature>(regions.size());
//		for (Region region : regions) {
//			result.add(getByRegion(region.getChromosome(), region.getStart(), region.getEnd(), 1));
//		}
//		return result;
//	}
//
//	@Override
//	public List<GenomeSequenceFeature> getByRegionList(List<Region> regions, int strand) {
//		List<GenomeSequenceFeature> result = new ArrayList<GenomeSequenceFeature>(regions.size());
//		for (Region region : regions) {
//			result.add(getByRegion(region.getChromosome(), region.getStart(), region.getEnd(), strand));
//		}
//		return result;
//	}
//
//	@Override
//	public String getRevComp(String sequence) {
//		String sequenceRef = new String();
//		sequenceRef = new StringBuffer(sequence).reverse().toString();
//		return getComplementarySequence(sequenceRef);
//	}


}
