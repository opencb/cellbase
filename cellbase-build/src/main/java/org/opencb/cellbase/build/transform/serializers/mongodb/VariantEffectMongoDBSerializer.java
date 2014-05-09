package org.opencb.cellbase.build.transform.serializers.mongodb;

import com.mongodb.BasicDBObject;
import java.util.Arrays;
import java.util.List;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.effect.VariantEffect;
import org.opencb.commons.io.DataWriter;
import org.opencb.commons.utils.CryptoUtils;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class VariantEffectMongoDBSerializer implements DataWriter<VariantEffect> {

    @Override
    public boolean open() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean pre() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean post() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean write(VariantEffect elem) {
        return write(Arrays.asList(elem));
    }

    @Override
    public boolean write(List<VariantEffect> batch) {
        for (VariantEffect effect : batch) {
            String rowkey = buildRowkey(effect);
            BasicDBObject mongoEffect = new BasicDBObject("_id", rowkey)
                    .append("chr", effect.getChromosome())
                    .append("start", effect.getStart()).append("end", effect.getEnd())
                    .append("ref", effect.getReferenceAllele());
            
        }
        
        return true;
    }
    
    
    private String buildRowkey(VariantEffect v) {
        StringBuilder builder = new StringBuilder(v.getChromosome());
        builder.append("_");
        builder.append(v.getStart());
        builder.append("_");
        if (v.getReferenceAllele().length() < Variant.SV_THRESHOLD) {
            builder.append(v.getReferenceAllele());
        } else {
            builder.append(new String(CryptoUtils.encryptSha1(v.getReferenceAllele())));
        }
         
        return builder.toString();
    }
    
}
