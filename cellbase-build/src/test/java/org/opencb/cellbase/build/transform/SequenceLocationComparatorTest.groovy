package org.opencb.cellbase.build.transform

import org.opencb.biodata.models.variant.clinical.clinvar.v1_5jaxb.SequenceLocationType
import spock.lang.Unroll

/**
 * Created by parce on 6/4/14.
 */
class SequenceLocationComparatorTest extends spock.lang.Specification {

    private static comparator

    void setupSpec() {
        setup:
        comparator = new ClinvarParser.SequenceLocationComparator()
    }

    @Unroll
    def "signum of #accesion1:#start1-#ref1-#alt1 compared to #accesion2:#start2-#ref2-#alt2 should be #comparation"() {
        given:
        def loc1 = new SequenceLocationType()
        def loc2 = new SequenceLocationType()
        loc1.setAccession(accesion1)
        loc2.setAccession(accesion2)
        loc1.setStart(new BigInteger(start1))
        loc2.setStart(new BigInteger(start2))
        loc1.setReferenceAllele(ref1)
        loc2.setReferenceAllele(ref2)
        loc1.setAlternateAllele(alt1)
        loc2.setAlternateAllele(alt2)

        expect:
        Math.signum(comparator.compare(loc1, loc2)) == comparation

        where: "chromosome comparation"
        accesion1      | accesion2      | start1 | start2 | ref1 | ref2 | alt1 | alt2 || comparation
        "NC_000008.10" | "NC_000010.10" | "100"  | "100"  | "A"  | "A"  | "C"  | "C"  || -1
        "NC_000014.8"  | "NC_000010.10" | "100"  | "100"  | "A"  | "A"  | "C"  | "C"  || 1
        and: "start comparation"
        "NC_000008.10" | "NC_000008.10" | "100"  | "200"  | "A"  | "A"  | "C"  | "C"  || -1
        "NC_000008.10" | "NC_000008.10" | "300"  | "100"  | "A"  | "A"  | "C"  | "C"  || 1
        and: "reference comparation"
        "NC_000008.10" | "NC_000008.10" | "100"  | "100"  | "A"  | "C"  | "C"  | "C"  || -1
        "NC_000008.10" | "NC_000008.10" | "100"  | "100"  | "C"  | "A"  | "C"  | "C"  || 1
        and: "alternative comparation"
        "NC_000008.10" | "NC_000008.10" | "100"  | "100"  | "A"  | "A"  | "A"  | "C"  || -1
        "NC_000008.10" | "NC_000008.10" | "100"  | "100"  | "A"  | "A"  | "C"  | "A"  || 1
        and: "deletion and insertion comparation"
        "NC_000008.10" | "NC_000008.10" | "100"  | "100"  | "-"  | "A"  | "A"  | "A"  || -1
        "NC_000008.10" | "NC_000008.10" | "100"  | "100"  | "A"  | "A"  | "-"  | "A"  || -1
        and: "same variant comparation"
        "NC_000008.10" | "NC_000008.10" | "100"  | "100"  | "A"  | "A"  | "A"  | "A"  || 0
    }
}
