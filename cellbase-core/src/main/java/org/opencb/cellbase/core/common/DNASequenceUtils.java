/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.core.common;

import java.util.HashMap;
import java.util.Map;

public class DNASequenceUtils {

    public static final Map<Character, Character> COMPLEMENT = new HashMap<>();

    public static final Map<String, String> CODON_TO_AMINOACID = new HashMap<>();
    public static final Map<String, String> CODON_TO_AMINOACID_SHORT = new HashMap<>();

    static {
        COMPLEMENT.put('A', 'T');
        COMPLEMENT.put('T', 'A');
        COMPLEMENT.put('C', 'G');
        COMPLEMENT.put('G', 'C');
        COMPLEMENT.put('N', 'N');
    }

    static {
        CODON_TO_AMINOACID.put("UUU", "Phe");
        CODON_TO_AMINOACID.put("UUC", "Phe");
        CODON_TO_AMINOACID.put("UUA", "Leu");
        CODON_TO_AMINOACID.put("UUG", "Leu");
        CODON_TO_AMINOACID.put("UCU", "Ser");
        CODON_TO_AMINOACID.put("UCC", "Ser");
        CODON_TO_AMINOACID.put("UCA", "Ser");
        CODON_TO_AMINOACID.put("UCG", "Ser");
        CODON_TO_AMINOACID.put("UAU", "Tyr");
        CODON_TO_AMINOACID.put("UAC", "Tyr");
        CODON_TO_AMINOACID.put("UAA", "Stop");
        CODON_TO_AMINOACID.put("UAG", "Stop");
        CODON_TO_AMINOACID.put("UGU", "Cys");
        CODON_TO_AMINOACID.put("UGC", "Cys");
        CODON_TO_AMINOACID.put("UGA", "Stop");
        CODON_TO_AMINOACID.put("UGG", "Trp");
        CODON_TO_AMINOACID.put("CUU", "Leu");
        CODON_TO_AMINOACID.put("CUC", "Leu");
        CODON_TO_AMINOACID.put("CUA", "Leu");
        CODON_TO_AMINOACID.put("CUG", "Leu");
        CODON_TO_AMINOACID.put("CCU", "Pro");
        CODON_TO_AMINOACID.put("CCC", "Pro");
        CODON_TO_AMINOACID.put("CCA", "Pro");
        CODON_TO_AMINOACID.put("CCG", "Pro");
        CODON_TO_AMINOACID.put("CAU", "His");
        CODON_TO_AMINOACID.put("CAC", "His");
        CODON_TO_AMINOACID.put("CAA", "Gln");
        CODON_TO_AMINOACID.put("CAG", "Gln");
        CODON_TO_AMINOACID.put("CGU", "Arg");
        CODON_TO_AMINOACID.put("CGC", "Arg");
        CODON_TO_AMINOACID.put("CGA", "Arg");
        CODON_TO_AMINOACID.put("CGG", "Arg");
        CODON_TO_AMINOACID.put("AUU", "Ile");
        CODON_TO_AMINOACID.put("AUC", "Ile");
        CODON_TO_AMINOACID.put("AUA", "Ile");
        CODON_TO_AMINOACID.put("AUG", "Met");
        CODON_TO_AMINOACID.put("ACU", "Thr");
        CODON_TO_AMINOACID.put("ACC", "Thr");
        CODON_TO_AMINOACID.put("ACA", "Thr");
        CODON_TO_AMINOACID.put("ACG", "Thr");
        CODON_TO_AMINOACID.put("AAU", "Asn");
        CODON_TO_AMINOACID.put("AAC", "Asn");
        CODON_TO_AMINOACID.put("AAA", "Lys");
        CODON_TO_AMINOACID.put("AAG", "Lys");
        CODON_TO_AMINOACID.put("AGU", "Ser");
        CODON_TO_AMINOACID.put("AGC", "Ser");
        CODON_TO_AMINOACID.put("AGA", "Arg");
        CODON_TO_AMINOACID.put("AGG", "Arg");
        CODON_TO_AMINOACID.put("GUU", "Val");
        CODON_TO_AMINOACID.put("GUC", "Val");
        CODON_TO_AMINOACID.put("GUA", "Val");
        CODON_TO_AMINOACID.put("GUG", "Val");
        CODON_TO_AMINOACID.put("GCU", "Ala");
        CODON_TO_AMINOACID.put("GCC", "Ala");
        CODON_TO_AMINOACID.put("GCA", "Ala");
        CODON_TO_AMINOACID.put("GCG", "Ala");
        CODON_TO_AMINOACID.put("GAU", "Asp");
        CODON_TO_AMINOACID.put("GAC", "Asp");
        CODON_TO_AMINOACID.put("GAA", "Glu");
        CODON_TO_AMINOACID.put("GAG", "Glu");
        CODON_TO_AMINOACID.put("GGU", "Gly");
        CODON_TO_AMINOACID.put("GGC", "Gly");
        CODON_TO_AMINOACID.put("GGA", "Gly");
        CODON_TO_AMINOACID.put("GGG", "Gly");
    }

    static {
        CODON_TO_AMINOACID_SHORT.put("UUU", "F");
        CODON_TO_AMINOACID_SHORT.put("UUC", "F");
        CODON_TO_AMINOACID_SHORT.put("UUA", "L");
        CODON_TO_AMINOACID_SHORT.put("UUG", "L");
        CODON_TO_AMINOACID_SHORT.put("UCU", "S");
        CODON_TO_AMINOACID_SHORT.put("UCC", "S");
        CODON_TO_AMINOACID_SHORT.put("UCA", "S");
        CODON_TO_AMINOACID_SHORT.put("UCG", "S");
        CODON_TO_AMINOACID_SHORT.put("UAU", "Y");
        CODON_TO_AMINOACID_SHORT.put("UAC", "Y");
        CODON_TO_AMINOACID_SHORT.put("UAA", "Stop");
        CODON_TO_AMINOACID_SHORT.put("UAG", "Stop");
        CODON_TO_AMINOACID_SHORT.put("UGU", "C");
        CODON_TO_AMINOACID_SHORT.put("UGC", "C");
        CODON_TO_AMINOACID_SHORT.put("UGA", "Stop");
        CODON_TO_AMINOACID_SHORT.put("UGG", "W");

        CODON_TO_AMINOACID_SHORT.put("CUU", "L");
        CODON_TO_AMINOACID_SHORT.put("CUC", "L");
        CODON_TO_AMINOACID_SHORT.put("CUA", "L");
        CODON_TO_AMINOACID_SHORT.put("CUG", "L");
        CODON_TO_AMINOACID_SHORT.put("CCU", "P");
        CODON_TO_AMINOACID_SHORT.put("CCC", "P");
        CODON_TO_AMINOACID_SHORT.put("CCA", "P");
        CODON_TO_AMINOACID_SHORT.put("CCG", "P");
        CODON_TO_AMINOACID_SHORT.put("CAU", "H");
        CODON_TO_AMINOACID_SHORT.put("CAC", "H");
        CODON_TO_AMINOACID_SHORT.put("CAA", "Q");
        CODON_TO_AMINOACID_SHORT.put("CAG", "Q");
        CODON_TO_AMINOACID_SHORT.put("CGU", "R");
        CODON_TO_AMINOACID_SHORT.put("CGC", "R");
        CODON_TO_AMINOACID_SHORT.put("CGA", "R");
        CODON_TO_AMINOACID_SHORT.put("CGG", "R");

        CODON_TO_AMINOACID_SHORT.put("AUU", "I");
        CODON_TO_AMINOACID_SHORT.put("AUC", "I");
        CODON_TO_AMINOACID_SHORT.put("AUA", "I");
        CODON_TO_AMINOACID_SHORT.put("AUG", "M");
        CODON_TO_AMINOACID_SHORT.put("ACU", "T");
        CODON_TO_AMINOACID_SHORT.put("ACC", "T");
        CODON_TO_AMINOACID_SHORT.put("ACA", "T");
        CODON_TO_AMINOACID_SHORT.put("ACG", "T");
        CODON_TO_AMINOACID_SHORT.put("AAU", "N");
        CODON_TO_AMINOACID_SHORT.put("AAC", "N");
        CODON_TO_AMINOACID_SHORT.put("AAA", "K");
        CODON_TO_AMINOACID_SHORT.put("AAG", "K");
        CODON_TO_AMINOACID_SHORT.put("AGU", "S");
        CODON_TO_AMINOACID_SHORT.put("AGC", "S");
        CODON_TO_AMINOACID_SHORT.put("AGA", "R");
        CODON_TO_AMINOACID_SHORT.put("AGG", "R");

        CODON_TO_AMINOACID_SHORT.put("GUU", "V");
        CODON_TO_AMINOACID_SHORT.put("GUC", "V");
        CODON_TO_AMINOACID_SHORT.put("GUA", "V");
        CODON_TO_AMINOACID_SHORT.put("GUG", "V");
        CODON_TO_AMINOACID_SHORT.put("GCU", "A");
        CODON_TO_AMINOACID_SHORT.put("GCC", "A");
        CODON_TO_AMINOACID_SHORT.put("GCA", "A");
        CODON_TO_AMINOACID_SHORT.put("GCG", "A");
        CODON_TO_AMINOACID_SHORT.put("GAU", "D");
        CODON_TO_AMINOACID_SHORT.put("GAC", "D");
        CODON_TO_AMINOACID_SHORT.put("GAA", "E");
        CODON_TO_AMINOACID_SHORT.put("GAG", "E");
        CODON_TO_AMINOACID_SHORT.put("GGU", "G");
        CODON_TO_AMINOACID_SHORT.put("GGC", "G");
        CODON_TO_AMINOACID_SHORT.put("GGA", "G");
        CODON_TO_AMINOACID_SHORT.put("GGG", "G");
    }


    public static String translate(String dnaSequence) {
        return translate(dnaSequence, "-");
    }

    public static String translate(String dnaSequence, String separator) {
        StringBuilder aaSequence = new StringBuilder();
        dnaSequence = dnaSequence.toUpperCase();
        dnaSequence.replaceAll("T", "U");
        for (int i = 0; i < dnaSequence.length(); i += 3) {
            if (i + 2 < dnaSequence.length()) {
                aaSequence.append(CODON_TO_AMINOACID.get(dnaSequence.charAt(i) + dnaSequence.charAt(i + 1) + dnaSequence.charAt(i + 2)));
                if (i + 3 < dnaSequence.length()) {
                    aaSequence.append(separator);
                }
            }
        }
        return aaSequence.toString();
    }

    public static String reverseComplement(String dnaSequence) {
        StringBuilder revComplSequence = new StringBuilder(dnaSequence.length());
        for (int i = dnaSequence.length() - 1; i >= 0; i--) {
            revComplSequence.append(COMPLEMENT.get(dnaSequence.charAt(i)));
        }
        return revComplSequence.toString();
    }

}
