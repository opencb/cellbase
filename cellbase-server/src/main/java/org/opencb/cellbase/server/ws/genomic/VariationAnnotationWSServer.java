package org.opencb.cellbase.server.ws.genomic;

import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.lib.api.variation.VariantAnnotationDBAdaptor;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;

/**
 * Created by imedina on 10/07/14.
 */
@Path("/{version}/{species}/genomic/variant")
public class VariationAnnotationWSServer extends GenericRestWSServer {

    private String filename = "/home/imedina/variant_effect_chr1.json.gz";


    public VariationAnnotationWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/{variants}/annotation")
    public Response getEffectByPositionByGet(@PathParam("variants") String variants,
                                             @DefaultValue("") @QueryParam("exclude") String excludeSOTerms) {

        List<GenomicVariant> genomicVariantList = GenomicVariant.parseVariants(variants);
        VariantAnnotationDBAdaptor variantAnnotationDBAdaptor = dbAdaptorFactory.getVariantAnnotationDBAdaptor(this.species, this.assembly);

//        TabixReader currentTabix = null;
//        String line = "";
//        String document = "";
//        try {
//            currentTabix = new TabixReader(filename);
//            TabixReader.Iterator it = currentTabix.query(genomicVariantList.get(0).getChromosome() + ":" + genomicVariantList.get(0).getPosition() + "-" + genomicVariantList.get(0).getPosition());
//            String[] fields = null;
//            while (it != null && (line = it.next()) != null) {
//                fields = line.split("\t");
//                document = fields[2];
////                System.out.println(fields[2]);
////                listRecords = factory.create(source, line);
//
////                if(listRecords.size() > 0){
////
////                    tabixRecord = listRecords.get(0);
////
////                    if (tabixRecord.getReference().equals(record.getReference()) && tabixRecord.getAlternate().equals(record.getAlternate())) {
////                        controlBatch.add(tabixRecord);
////                        map.put(record, cont++);
////                    }
////                }
//                break;
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return Response.ok(line);
        return createOkResponse(variantAnnotationDBAdaptor.getAllEffectsByVariantList(genomicVariantList, queryOptions));
    }

    @GET
    @Path("/{variants}/consequencetype")
    public Response getAllConsequenceTypesByVariantList(@PathParam("variants") String variants,
                                             @DefaultValue("") @QueryParam("exclude") String excludeSOTerms) {

        List<GenomicVariant> genomicVariantList = GenomicVariant.parseVariants(variants);
        VariantAnnotationDBAdaptor variantAnnotationDBAdaptor = dbAdaptorFactory.getVariantAnnotationDBAdaptor(this.species, this.assembly);


        return createOkResponse(variantAnnotationDBAdaptor.getAllConsequenceTypesByVariantList(genomicVariantList, queryOptions));
    }

}
