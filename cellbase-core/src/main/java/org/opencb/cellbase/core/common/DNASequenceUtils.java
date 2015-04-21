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
	
	public static Map<String, String> complement = new HashMap<String, String>();
	
	public static Map<String, String> codonToAminoacid = new HashMap<String, String>();
	public static Map<String, String> codonToAminoacidShort = new HashMap<String, String>();
	
	static {
		complement.put("A", "T");complement.put("T", "A");complement.put("C", "G");complement.put("G", "C");
	}
	
	static {
		codonToAminoacid.put("UUU", "Phe");codonToAminoacid.put("UUC", "Phe");codonToAminoacid.put("UUA", "Leu");codonToAminoacid.put("UUG", "Leu");
		codonToAminoacid.put("UCU", "Ser");codonToAminoacid.put("UCC", "Ser");codonToAminoacid.put("UCA", "Ser");codonToAminoacid.put("UCG", "Ser");
		codonToAminoacid.put("UAU", "Tyr");codonToAminoacid.put("UAC", "Tyr");codonToAminoacid.put("UAA", "Stop");codonToAminoacid.put("UAG", "Stop");
		codonToAminoacid.put("UGU", "Cys");codonToAminoacid.put("UGC", "Cys");codonToAminoacid.put("UGA", "Stop");codonToAminoacid.put("UGG", "Trp");
		codonToAminoacid.put("CUU", "Leu");codonToAminoacid.put("CUC", "Leu");codonToAminoacid.put("CUA", "Leu");codonToAminoacid.put("CUG", "Leu");
		codonToAminoacid.put("CCU", "Pro");codonToAminoacid.put("CCC", "Pro");codonToAminoacid.put("CCA", "Pro");codonToAminoacid.put("CCG", "Pro");
		codonToAminoacid.put("CAU", "His");codonToAminoacid.put("CAC", "His");codonToAminoacid.put("CAA", "Gln");codonToAminoacid.put("CAG", "Gln");
		codonToAminoacid.put("CGU", "Arg");codonToAminoacid.put("CGC", "Arg");codonToAminoacid.put("CGA", "Arg");codonToAminoacid.put("CGG", "Arg");
		codonToAminoacid.put("AUU", "Ile");codonToAminoacid.put("AUC", "Ile");codonToAminoacid.put("AUA", "Ile");codonToAminoacid.put("AUG", "Met");
		codonToAminoacid.put("ACU", "Thr");codonToAminoacid.put("ACC", "Thr");codonToAminoacid.put("ACA", "Thr");codonToAminoacid.put("ACG", "Thr");
		codonToAminoacid.put("AAU", "Asn");codonToAminoacid.put("AAC", "Asn");codonToAminoacid.put("AAA", "Lys");codonToAminoacid.put("AAG", "Lys");
		codonToAminoacid.put("AGU", "Ser");codonToAminoacid.put("AGC", "Ser");codonToAminoacid.put("AGA", "Arg");codonToAminoacid.put("AGG", "Arg");
		codonToAminoacid.put("GUU", "Val");codonToAminoacid.put("GUC", "Val");codonToAminoacid.put("GUA", "Val");codonToAminoacid.put("GUG", "Val");
		codonToAminoacid.put("GCU", "Ala");codonToAminoacid.put("GCC", "Ala");codonToAminoacid.put("GCA", "Ala");codonToAminoacid.put("GCG", "Ala");
		codonToAminoacid.put("GAU", "Asp");codonToAminoacid.put("GAC", "Asp");codonToAminoacid.put("GAA", "Glu");codonToAminoacid.put("GAG", "Glu");
		codonToAminoacid.put("GGU", "Gly");codonToAminoacid.put("GGC", "Gly");codonToAminoacid.put("GGA", "Gly");codonToAminoacid.put("GGG", "Gly");
	}
	
	static {
		codonToAminoacidShort.put("UUU", "F");codonToAminoacidShort.put("UUC", "F");codonToAminoacidShort.put("UUA", "L");codonToAminoacidShort.put("UUG", "L");
		codonToAminoacidShort.put("UCU", "S");codonToAminoacidShort.put("UCC", "S");codonToAminoacidShort.put("UCA", "S");codonToAminoacidShort.put("UCG", "S");
		codonToAminoacidShort.put("UAU", "Y");codonToAminoacidShort.put("UAC", "Y");codonToAminoacidShort.put("UAA", "Stop");codonToAminoacidShort.put("UAG", "Stop");
		codonToAminoacidShort.put("UGU", "C");codonToAminoacidShort.put("UGC", "C");codonToAminoacidShort.put("UGA", "Stop");codonToAminoacidShort.put("UGG", "W");
		
		codonToAminoacidShort.put("CUU", "L");codonToAminoacidShort.put("CUC", "L");codonToAminoacidShort.put("CUA", "L");codonToAminoacidShort.put("CUG", "L");
		codonToAminoacidShort.put("CCU", "P");codonToAminoacidShort.put("CCC", "P");codonToAminoacidShort.put("CCA", "P");codonToAminoacidShort.put("CCG", "P");
		codonToAminoacidShort.put("CAU", "H");codonToAminoacidShort.put("CAC", "H");codonToAminoacidShort.put("CAA", "Q");codonToAminoacidShort.put("CAG", "Q");
		codonToAminoacidShort.put("CGU", "R");codonToAminoacidShort.put("CGC", "R");codonToAminoacidShort.put("CGA", "R");codonToAminoacidShort.put("CGG", "R");
		
		codonToAminoacidShort.put("AUU", "I");codonToAminoacidShort.put("AUC", "I");codonToAminoacidShort.put("AUA", "I");codonToAminoacidShort.put("AUG", "M");
		codonToAminoacidShort.put("ACU", "T");codonToAminoacidShort.put("ACC", "T");codonToAminoacidShort.put("ACA", "T");codonToAminoacidShort.put("ACG", "T");
		codonToAminoacidShort.put("AAU", "N");codonToAminoacidShort.put("AAC", "N");codonToAminoacidShort.put("AAA", "K");codonToAminoacidShort.put("AAG", "K");
		codonToAminoacidShort.put("AGU", "S");codonToAminoacidShort.put("AGC", "S");codonToAminoacidShort.put("AGA", "R");codonToAminoacidShort.put("AGG", "R");
		
		codonToAminoacidShort.put("GUU", "V");codonToAminoacidShort.put("GUC", "V");codonToAminoacidShort.put("GUA", "V");codonToAminoacidShort.put("GUG", "V");
		codonToAminoacidShort.put("GCU", "A");codonToAminoacidShort.put("GCC", "A");codonToAminoacidShort.put("GCA", "A");codonToAminoacidShort.put("GCG", "A");
		codonToAminoacidShort.put("GAU", "D");codonToAminoacidShort.put("GAC", "D");codonToAminoacidShort.put("GAA", "E");codonToAminoacidShort.put("GAG", "E");
		codonToAminoacidShort.put("GGU", "G");codonToAminoacidShort.put("GGC", "G");codonToAminoacidShort.put("GGA", "G");codonToAminoacidShort.put("GGG", "G");
	}
	
	
	public static String translate(String dnaSequence) {
		return translate(dnaSequence, "-");
	}
	
	public static String translate(String dnaSequence, String separator) {
		StringBuilder aaSequence = new StringBuilder();
		dnaSequence = dnaSequence.toUpperCase();
		dnaSequence.replaceAll("T", "U");
		for(int i=0; i<dnaSequence.length(); i+=3) {
			if(i+2 < dnaSequence.length()){
				aaSequence.append(codonToAminoacid.get(dnaSequence.charAt(i)+dnaSequence.charAt(i+1)+dnaSequence.charAt(i+2)));
				if(i+3 < dnaSequence.length()) {
					aaSequence.append(separator);
				}
			}
		}
		return aaSequence.toString();
	}
	
	public static String reverseComplement(String dnaSequence) {
		StringBuilder revComplSequence = new StringBuilder(dnaSequence.length());
		for(int i=dnaSequence.length()-1; i>=0; i--) {
			revComplSequence.append(complement.get(dnaSequence.charAt(i)));
		}
		return revComplSequence.toString();
	}
	
}
