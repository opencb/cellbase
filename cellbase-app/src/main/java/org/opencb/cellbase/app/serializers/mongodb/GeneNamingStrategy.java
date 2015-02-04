package org.opencb.cellbase.app.serializers.mongodb;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 24/11/13
 * Time: 10:54
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class GeneNamingStrategy extends PropertyNamingStrategy {

    private static Map<String, String> fieldNames;

    static {
        fieldNames = new HashMap<>(50);
        fieldNames.put("chromosome", "c");
        fieldNames.put("start", "s");
        fieldNames.put("end", "e");
        fieldNames.put("strand", "str");
        fieldNames.put("geneName", "gn");
        fieldNames.put("ensemblTranscriptId", "etId");
        fieldNames.put("sampleName", "sn");
        fieldNames.put("pubmedPMID", "pmid");
        fieldNames.put("mutationSomaticStatus", "mutSt");
        fieldNames.put("mutationZygosity", "mutZyg");
        fieldNames.put("mutationID", "mutId");
        fieldNames.put("mutationCDS", "mutCds");
        fieldNames.put("mutationAA", "mutAa");
    }

    @Override
    public String nameForField(MapperConfig<?> mapperConfig, AnnotatedField annotatedField, String s) {
//        return super.nameForField(mapperConfig, annotatedField, s);    //To change body of overridden methods use File | Settings | File Templates.
        return translate(s);
    }

    @Override
    public String nameForGetterMethod(MapperConfig<?> mapperConfig, AnnotatedMethod annotatedMethod, String s) {
//        return super.nameForGetterMethod(mapperConfig, annotatedMethod, s);    //To change body of overridden methods use File | Settings | File Templates.
        return translate(s);
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> mapperConfig, AnnotatedMethod annotatedMethod, String s) {
//        return super.nameForSetterMethod(mapperConfig, annotatedMethod, s);    //To change body of overridden methods use File | Settings | File Templates.
        return translate(s);
    }

    private String translate(String defaultName) {

        String name = fieldNames.get(defaultName);
        if(name != null) {
            return name;
        }else {
            return defaultName;
        }
    }
}
