package org.opencb.cellbase.build.transform;

/**
 * Created by lcruz on 26/05/14.
 */
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.broad.tribble.readers.TabixReader;
import org.opencb.biodata.models.variant.clinical.Gwas;

import java.io.*;
import java.text.ParseException;
import java.util.*;

/**
 * @author lcruz
 * @version 1.2.3
 * @since April 28, 2014
 */
public class GwasParser {
	public String inputFilePath = null;
	public String outputFilePath = null;
	public String dbSnpFilePath = null;
	public String inputFileSorted = null;
	public Map<String, Integer> mapChrTranslation = new HashMap<String, Integer>();
	public static final String GWAS_HEADER_CONSTANT = "gwasHeader";
	public static final String GWAS_STUDY_CONSTANT = "gwasStudy";
	public static final String GWAS_TEST_CONSTANT = "gwasTest";

	public GwasParser() {
		mapChrTranslation.put("X", 23);
		mapChrTranslation.put("Y", 24);
		mapChrTranslation.put("MT", 25);
	}

	public GwasParser(String inputFilePath, String outputFilePath, String dbSnpFilePath) {
		this.inputFilePath = inputFilePath;
		this.outputFilePath = outputFilePath;
		this.dbSnpFilePath = dbSnpFilePath;

		mapChrTranslation.put("X", 23);
		mapChrTranslation.put("Y", 24);
		mapChrTranslation.put("MT", 25);
	}

	public void parseFile() {
		File file = new File(inputFilePath);
		File dbSnpFile = new File(dbSnpFilePath);
		if (file.exists() && dbSnpFile.exists()) {
			String line;
			try {
				// Test if the output file folder exists
				String outputFolder = outputFilePath.substring(0,
						outputFilePath.lastIndexOf("/"));
				File folder = new File(outputFolder);
				if (!folder.exists()) {
					folder.mkdirs();
				}

				// Extract the name of the sorted temp file
				String fileName = inputFilePath.substring(inputFilePath
						.lastIndexOf("/"));
				inputFileSorted = inputFilePath.substring(0,
						inputFilePath.lastIndexOf("/"))
						+ fileName.substring(0, fileName.lastIndexOf("."))
						+ ".sorted"
						+ fileName.substring(fileName.lastIndexOf("."));

				FileReader fr = new FileReader(inputFilePath);
				FileWriter writer = new FileWriter(outputFilePath);
				BufferedReader br = new BufferedReader(fr);

				// Read the header
				line = br.readLine();
				/*String[] array = line.split("\t");
				for (int i = 0; i < array.length; i++) {
					System.out.println("Campo: " + array[i]);
				}*/

				// Read the values of ref and alt
				/*String tabIndexPath = getClass().getClassLoader()
				.getResource("resources/dbsnp").getPath()
				+ "/dbSnp137-00-All.vcf.gz";*/
				TabixReader t = new TabixReader(dbSnpFilePath);
				TabixReader.Iterator tabixIterator;

				try {
					String ref, alt, tabIndexString;
					boolean founded = false;
					int numLinea = 0;
					Map<String, List<Gwas>> mapVariants = new HashMap<String, List<Gwas>>();

					while ((line = br.readLine()) != null) {
						if (!line.isEmpty()) {
							ref = null;
							alt = null;
							founded = false;

							// Create the object with the GWAS information
							Gwas gwasVO = new Gwas(line.split("\t"));

							// Obtain the reference and alternative from the chromosome and position
							if (gwasVO.getChrId() != null
									&& gwasVO.getChrPos() != null) {
								tabixIterator = t.query(gwasVO.getChrId() + ":"
										+ gwasVO.getChrPos() + "-"
										+ gwasVO.getChrPos());
								tabIndexString = tabixIterator.next();

								while (tabIndexString != null && !founded) {
									String[] tabIndexLine = tabIndexString.split("\t");
									// If the rs is the same in dbsnpn that the founded in gwas
									if (gwasVO.getSnps().equalsIgnoreCase(
											tabIndexLine[2])) {
										ref = tabIndexLine[3];
										alt = tabIndexLine[4];
										gwasVO.setRef(ref);
										gwasVO.setAlt(alt);
										founded = true;
									}

									tabIndexString = tabixIterator.next();
								}

								if (founded) {
									// If the alternative has more than one nucleotide, we print one line of each
									// alternative
									/*if (gwasVO.getChrId().equalsIgnoreCase("16")
											&& gwasVO.getChrPos().compareTo(
													new Integer("56997233")) == 0) {
										System.out.println("Linea: " + numLinea);
									}*/
									
									if (!alt.contains(",")) {
										if (mapVariants.containsKey(gwasVO.getChrId() + "::" + gwasVO.getChrPos()
												+ "::" + ref + "::" + alt)) {
											List<Gwas> listAux = mapVariants.get(gwasVO.getChrId() + "::"
															+ gwasVO.getChrPos() + "::" + ref + "::" + alt);
											listAux.add(gwasVO);
											mapVariants.put(gwasVO.getChrId() + "::" + gwasVO.getChrPos()
													+ "::" + ref + "::" + alt, listAux);
										} else {
											List<Gwas> listAux = new LinkedList<Gwas>();
											listAux.add(gwasVO);
											mapVariants.put(gwasVO.getChrId() + "::" + gwasVO.getChrPos()
													+ "::" + ref + "::" + alt, listAux);
										}
									} else {
										String[] alternativeArray = alt
												.split(",");

										for (int pos = 0; pos < alternativeArray.length; pos++) {
											if (mapVariants.containsKey(gwasVO.getChrId() + "::"
													+ gwasVO.getChrPos() + "::" + ref + "::" + alternativeArray[pos])) {
												List<Gwas> listAux = mapVariants.get(gwasVO.getChrId() + "::"
																+ gwasVO.getChrPos() + "::" + ref + "::" 
																+ alternativeArray[pos]);
												
												gwasVO.setAlt(alternativeArray[pos]);
												listAux.add(gwasVO);
												mapVariants.put(gwasVO.getChrId() + "::" + gwasVO.getChrPos()
																+ "::" + ref + "::" + alt, listAux);
											} else {
												List<Gwas> listAux = new LinkedList<Gwas>();
												
												gwasVO.setAlt(alternativeArray[pos]);
												listAux.add(gwasVO);
												mapVariants.put(gwasVO.getChrId()+ "::"+ gwasVO.getChrPos()
																+ "::" + ref + "::" + alt, listAux);
											}
										}
									}
								}
							}

							numLinea++;
						}
					}

					// Order the map of variants
					/*
					 * List<String> listVariants = new
					 * ArrayList<String>(mapVariants.keySet());
					 * Collections.sort(listVariants, new Comparator<String>(){
					 * public int compare(String obj1, String obj2) { int
					 * result;
					 * 
					 * String[] array1 = obj1.split("::"); String[] array2 =
					 * obj2.split("::"); Integer obj1chrId = null; try{
					 * obj1chrId = Integer.valueOf(array1[0]); } catch
					 * (NumberFormatException e) {
					 * if(mapChrTranslation.containsKey(array1[0])){ obj1chrId =
					 * mapChrTranslation.get(array1[0]); } } Integer obj1chrPos
					 * = Integer.valueOf(array1[1]); String obj1ref = array1[2];
					 * String obj1alt = array1[3];
					 * 
					 * Integer obj2chrId = null; try{ obj2chrId =
					 * Integer.valueOf(array2[0]); } catch
					 * (NumberFormatException e) {
					 * if(mapChrTranslation.containsKey(array2[0])){ obj2chrId =
					 * mapChrTranslation.get(array2[0]); } } Integer obj2chrPos
					 * = Integer.valueOf(array2[1]); String obj2ref = array2[2];
					 * String obj2alt = array2[3];
					 * 
					 * if(obj1chrId.compareTo(obj2chrId) != 0){ result =
					 * obj1chrId.compareTo(obj2chrId); } else
					 * if(obj1chrPos.compareTo(obj2chrPos) != 0){ result =
					 * obj1chrPos.compareTo(obj2chrPos); } else
					 * if(obj1ref.compareTo(obj2ref) != 0){ result =
					 * obj1ref.compareTo(obj2ref); } else { result =
					 * obj1alt.compareTo(obj2alt); }
					 * 
					 * return result; } });
					 */

					// Print JSON file in the output path
					this.printJsonFile(writer, mapVariants);

					System.out.println("Execution finished");
				} catch (ParseException e) {
					e.printStackTrace();
				} finally {
					writer.close();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Build the JSON object from the map of variants
	 * 
	 * @param mapVariants
	 */
	private void printJsonFile(FileWriter writer, Map<String, List<Gwas>> mapVariants) throws IOException{
    	StringBuilder outputLine = null;
    	Iterator it = mapVariants.entrySet().iterator();
    	
        List<List<Gwas>> listVariantStudies = null;
        Gwas gwasCurrent = null;
        String jsonString = null;
        
        // Each element of the iterator contains a list with the studies and test of a variant
    	while (it.hasNext()){
        	List<Gwas> listGwasSameVariant = (List)((Map.Entry)it.next()).getValue();
        	
        	listVariantStudies = new LinkedList<List<Gwas>>();
        	List<Gwas> listVariantTests = new LinkedList<Gwas>();
        	
        	// If the variant has only one study, add this study to the list of variants
        	if (listGwasSameVariant.size() == 1){
        		listVariantStudies.add(listGwasSameVariant);
        	} else {
        		// Order the list of studies by the dateAddedToCatalog
                Collections.sort(listGwasSameVariant, new Comparator<Gwas>(){
                	public int compare(Gwas obj1, Gwas obj2) {
                		return obj1.getDateAddedToCatalog().compareTo(obj2.getDateAddedToCatalog());
                    }
                });
                
        		Gwas gwasOld = null;
        		gwasCurrent = null;
        		
        		for(int i=0; i<listGwasSameVariant.size(); i++){
        			gwasCurrent = listGwasSameVariant.get(i);
        			
        			if(gwasOld == null){
        				listVariantTests.add(gwasCurrent);
        				gwasOld = gwasCurrent;
        			} else {
        				if (gwasOld.getDateAddedToCatalog().equalsIgnoreCase(
        						gwasCurrent.getDateAddedToCatalog())){
        					// If the study is the same we add the test
        					listVariantTests.add(gwasCurrent);
        					gwasOld = gwasCurrent;
        				} else {
        					// If the study is not the same, insert the last study and create a new study 
        					// in the list of studies
        					listVariantStudies.add(listVariantTests);
        					listVariantTests = new LinkedList<Gwas>();
        					
        					listVariantTests.add(gwasCurrent);
        				}
        			}
        		}
        		
        		// Insert the last study in the list
        		listVariantStudies.add(listVariantTests);
        	}
        	
        	// Print the list of studies with its tests
        	gwasCurrent = null;
        	if(listVariantStudies != null && listVariantStudies.size() > 0){
        		gwasCurrent = (listVariantStudies.get(0)).get(0);
        		
        		outputLine =
				new StringBuilder(
						gwasCurrent.getChrId()+"\t"+gwasCurrent.getChrPos()+"\t"+
						gwasCurrent.getRef()+"\t"+gwasCurrent.getAlt()+"\t");
        		outputLine.append("{\"gwas\":");
        		
        		/*if(gwasCurrent.getChrId().equalsIgnoreCase("16") && 
        				gwasCurrent.getChrPos().equals(new Integer("56997233"))){
        			System.out.println("Hola q ase");*/
        			if(listVariantStudies.size() < 2){
        				// If we have only one study, iterate all the tests of this study
            			listVariantTests = listVariantStudies.get(0);
                        
                        if(listVariantTests.size() < 2){
                        	// If there is only one test, print the whole object
                        	jsonString = createJsonString(gwasCurrent, null);
                        	outputLine.append(jsonString.substring(0, jsonString.length()-1));
                        } else {
                        	// If there is more than one test iterate the tests
                        	jsonString = createJsonString(gwasCurrent, GWAS_HEADER_CONSTANT);
                        	outputLine.append(jsonString.substring(0, jsonString.length()-1));
                        	
                        	jsonString = createJsonString(gwasCurrent, GWAS_STUDY_CONSTANT);
                        	outputLine.append(","+jsonString.substring(1, jsonString.length()-1));
                        	
                        	outputLine.append(",\"tests\":[");
                        	
                        	for(int j=0; j<listVariantTests.size(); j++){
                        		Gwas gwasTest = listVariantTests.get(j);
                				jsonString = createJsonString(gwasTest, GWAS_TEST_CONSTANT);
                            	outputLine.append(jsonString.substring(0, jsonString.length()-1));
                            	
                            	// Close the test JSON
                                outputLine.append("}");
                                if(j<listVariantTests.size()-1){
                                	outputLine.append(",");	
                                }
                        	}
                        	
                        	// Close the test set
                        	outputLine.append("]");
                        }
            		} else {
            			// If we have more than one study, iterate all the studies
            			outputLine =
                				new StringBuilder(
                						gwasCurrent.getChrId()+"\t"+gwasCurrent.getChrPos()+"\t"+
                						gwasCurrent.getRef()+"\t"+gwasCurrent.getAlt()+"\t");
                        outputLine.append("{\"gwas\":");
                        jsonString = createJsonString(gwasCurrent, GWAS_HEADER_CONSTANT);
                    	outputLine.append(jsonString.substring(0, jsonString.length()-1));
                    	outputLine.append(",\"studies\":[");
                        
            			for(int i=0; i<listVariantStudies.size(); i++){
            				Gwas gwasStudy = listVariantStudies.get(i).get(0);
            				jsonString = createJsonString(gwasStudy, GWAS_STUDY_CONSTANT);
                        	outputLine.append(jsonString.substring(0, jsonString.length()-1));
            				
                			listVariantTests = listVariantStudies.get(i);
                            
                            if(listVariantTests.size() < 2){
                            	// If there is only one test, print the whole object
                            	jsonString = createJsonString(gwasStudy, GWAS_TEST_CONSTANT);
                            	outputLine.append(","+jsonString.substring(1, jsonString.length()-1));
                            } else {
                            	// If there is more than one test iterate the tests
                            	outputLine.append(",\"tests\":[");
                            	
                            	for(int j=0; j<listVariantTests.size(); j++){
                            		Gwas gwasTest = listVariantTests.get(j);
                    				jsonString = createJsonString(gwasTest, GWAS_TEST_CONSTANT);
                                	outputLine.append(jsonString.substring(0, jsonString.length()-1));
                                	
                                	// Close the test JSON
                                    outputLine.append("}");
                                    if(j<listVariantTests.size()-1){
                                    	outputLine.append(",");	
                                    }
                            	}
                            	
                            	// Close the test set
                            	outputLine.append("]");
                            }
                            
                            // Close the study JSON
                            outputLine.append("}");
                            if(i<listVariantStudies.size()-1){
                            	outputLine.append(",");	
                            }
            			}
            			
            			// Close the study set
                        outputLine.append("]");
            		}
        			
        			// Close the gwas JSON
                	outputLine.append("}");
                	// Close the gwas set
                	outputLine.append("}");
                    writer.write(outputLine.toString() + "\n");
        		}
        	//}
        }
    }

	private String createJsonString(Gwas gwas, String jsonType) {
		String jsonString = new String();

		List<String> listIgnorableFields = new ArrayList<String>();
		listIgnorableFields.add("ref");
		listIgnorableFields.add("alt");

		if (jsonType != null) {
			if (jsonType.equalsIgnoreCase(GWAS_HEADER_CONSTANT)) {
				// Print only the header of the
				listIgnorableFields.add("dateAddedToCatalog");
				listIgnorableFields.add("pubmedId");
				listIgnorableFields.add("firstAuthor");
				listIgnorableFields.add("date");
				listIgnorableFields.add("journal");
				listIgnorableFields.add("link");
				listIgnorableFields.add("study");
				listIgnorableFields.add("diseaseTrait");
				listIgnorableFields.add("initialSampleSize");
				listIgnorableFields.add("platform");
				listIgnorableFields.add("pValue");
				listIgnorableFields.add("pValueMlog");
				listIgnorableFields.add("pValueText");
				listIgnorableFields.add("orBeta");
				listIgnorableFields.add("percentCI");
			} else if (jsonType.equalsIgnoreCase(GWAS_STUDY_CONSTANT)) {
				listIgnorableFields.add("replicationSampleSize");
				listIgnorableFields.add("region");
				listIgnorableFields.add("chrId");
				listIgnorableFields.add("chrPos");
				listIgnorableFields.add("reportedGenes");
				listIgnorableFields.add("mappedGene");
				listIgnorableFields.add("upstreamGeneId");
				listIgnorableFields.add("downstreamGeneId");
				listIgnorableFields.add("snpGeneIds");
				listIgnorableFields.add("upstreamGeneDistance");
				listIgnorableFields.add("downstreamGeneDistance");
				listIgnorableFields.add("strongestSNPRiskAllele");
				listIgnorableFields.add("snps");
				listIgnorableFields.add("merged");
				listIgnorableFields.add("snpIdCurrent");
				listIgnorableFields.add("context");
				listIgnorableFields.add("intergenic");
				listIgnorableFields.add("riskAlleleFrequency");
				listIgnorableFields.add("cnv");
				listIgnorableFields.add("pValue");
				listIgnorableFields.add("pValueMlog");
				listIgnorableFields.add("pValueText");
				listIgnorableFields.add("orBeta");
				listIgnorableFields.add("percentCI");
			} else if (jsonType.equalsIgnoreCase(GWAS_TEST_CONSTANT)) {
				listIgnorableFields.add("replicationSampleSize");
				listIgnorableFields.add("region");
				listIgnorableFields.add("chrId");
				listIgnorableFields.add("chrPos");
				listIgnorableFields.add("reportedGenes");
				listIgnorableFields.add("mappedGene");
				listIgnorableFields.add("upstreamGeneId");
				listIgnorableFields.add("downstreamGeneId");
				listIgnorableFields.add("snpGeneIds");
				listIgnorableFields.add("upstreamGeneDistance");
				listIgnorableFields.add("downstreamGeneDistance");
				listIgnorableFields.add("strongestSNPRiskAllele");
				listIgnorableFields.add("snps");
				listIgnorableFields.add("merged");
				listIgnorableFields.add("snpIdCurrent");
				listIgnorableFields.add("context");
				listIgnorableFields.add("intergenic");
				listIgnorableFields.add("riskAlleleFrequency");
				listIgnorableFields.add("cnv");
				listIgnorableFields.add("dateAddedToCatalog");
				listIgnorableFields.add("pubmedId");
				listIgnorableFields.add("firstAuthor");
				listIgnorableFields.add("date");
				listIgnorableFields.add("journal");
				listIgnorableFields.add("link");
				listIgnorableFields.add("study");
				listIgnorableFields.add("diseaseTrait");
				listIgnorableFields.add("initialSampleSize");
				listIgnorableFields.add("platform");
			}
		}

		String[] ignorableFieldNames = new String[listIgnorableFields.size()];
		for(int i=0; i<listIgnorableFields.size(); i++) {
			ignorableFieldNames[i] = listIgnorableFields.get(i);
		}
		
		// Filter the ignorable fields
		SimpleFilterProvider filterProvider = new SimpleFilterProvider();
		filterProvider.addFilter("gwasVOFilter", SimpleBeanPropertyFilter.serializeAllExcept(ignorableFieldNames));
		filterProvider.setFailOnUnknownId(false);

		// Create the JSON from the GWAS object
		ObjectMapper jsonMapper = new ObjectMapper();
		// Set the filter to show only some values
		jsonMapper.setFilters(filterProvider);
		// Configure to print only not null fields
		jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		ObjectWriter jsonWriter = jsonMapper.writer();

		try {
			jsonString = jsonMapper.writeValueAsString(gwas);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonString;
	}
}