package org.opencb.cellbase.build.transform;


import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.opencb.cellbase.core.common.pathway.BiopaxPathway;
import org.opencb.cellbase.core.common.pathway.Interaction;
import org.opencb.cellbase.core.common.pathway.PhysicalEntity;
import org.opencb.cellbase.core.common.pathway.SubPathway;
import org.opencb.commons.bioformats.commons.exception.FileFormatException;
import org.opencb.commons.bioformats.network.biopax.BioPax;
import org.opencb.commons.bioformats.network.biopax.BioPaxParser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BiopaxPathwayParser {
	BioPax bioPax = null;
	protected Map<String, String> parentMap = new HashMap<String, String>();
	protected Map<String, HashMap<String, HashMap<String, String>>> ecNumberMap = new HashMap<String, HashMap<String, HashMap<String, String>>>();
	
	public BiopaxPathwayParser() {
	}
	
	public List<DBObject> parseToJson(String filename) throws IOException, SecurityException, NoSuchMethodException, FileFormatException {
//		String json = "";
		List<BiopaxPathway> pathwayList = new ArrayList<BiopaxPathway>();
		List<DBObject> dbObjList = new ArrayList<DBObject>();
//		Gson gson = new GsonBuilder().create();

		try {
			BioPaxParser parser = new BioPaxParser(filename);
			bioPax = parser.parse();
			
			// Calculate parents
			for(String pathway: bioPax.getPathwayList() ) {
//				System.out.println("Pathway: "+pathway);
				findChildren(pathway);
			}
			
//			int cont = 0;
			for(String pathway: bioPax.getPathwayList()) {
//				System.out.println("Pathway: "+pathway);
//				if(cont++ == 10) break;
				
				String name = bioPax.getElementMap().get(pathway).getId();
				Map<String, List<String>> params = bioPax.getElementMap().get(pathway).getParams();
				List<Object> species = resolveSpecies(params.get("organism-id"));
				List<String> displayName = params.get("displayName");
				List<Object> xref = resolveXref(params.get("xref-id"));
				BiopaxPathway p = new BiopaxPathway(name, "Reactome", "Reactome", "39", species, displayName, xref);
				List<String> pathwayComponents = params.get("pathwayComponent-id");
				
				// loop pathway components
				if(pathwayComponents != null) {
					for(String component: pathwayComponents) {
//						System.out.println("Component: "+component);
						String type = bioPax.getElementMap().get(component).getBioPaxClassName();
						if(type.equalsIgnoreCase("Pathway")) {
							SubPathway sp = searchSubPathways(component, p);
							if(sp != null) p.getSubPathways().add(sp);
						}
						else if(type.equalsIgnoreCase("GeneticInteraction") || type.equalsIgnoreCase("MolecularInteraction") || type.equalsIgnoreCase("TemplateReaction") || type.equalsIgnoreCase("Catalysis") || type.equalsIgnoreCase("Modulation") || type.equalsIgnoreCase("TemplateReactionRegulation") || type.equalsIgnoreCase("BiochemicalReaction") || type.equalsIgnoreCase("ComplexAssembly") || type.equalsIgnoreCase("Degradation") || type.equalsIgnoreCase("Transport") || type.equalsIgnoreCase("TransportWithBiochemicalReaction")) {
//							Map<String, List<String>> interactionParams = bioPax.getElementMap().get(component).getParams();
							
							Map<String, List<Object>> interactionParams = new HashMap<String, List<Object>>();
							for (Map.Entry<String, List<String>> entry : bioPax.getElementMap().get(component).getParams().entrySet()) {
							    String key = entry.getKey();
							    List<Object> values = new ArrayList<Object>(entry.getValue());
							    
							    interactionParams.put(key, values);
							}
							
							if(interactionParams.containsKey("participantStoichiometry-id")) {
								List<Object> stoichiometry = new ArrayList<Object>();
								for(Object participant: interactionParams.get("participantStoichiometry-id")) {
									stoichiometry.add(bioPax.getElementMap().get(participant));
								}
								interactionParams.remove("participantStoichiometry-id");
								interactionParams.put("participantStoichiometry", stoichiometry);
							}
							
							if(interactionParams.containsKey("left-id")) {
								List<Object> left = new ArrayList<Object>();
								for(final Object leftId: interactionParams.get("left-id")) {
									String compId = bioPax.getElementMap().get(leftId).getId();
									List<String> dispName = bioPax.getElementMap().get(leftId).getParams().get("displayName");
									if(dispName == null) {
										dispName = bioPax.getElementMap().get(leftId).getParams().get("name");
									}
									Map<String, Object> leftParams = new HashMap<String, Object>();
									leftParams.put("id", compId);
									leftParams.put("name", dispName.get(0));
									left.add(leftParams);
								}
								interactionParams.remove("left-id");
								interactionParams.put("left", left);
							}
							
							if(interactionParams.containsKey("right-id")) {
								List<Object> right = new ArrayList<Object>();
								for(final Object rightId: interactionParams.get("right-id")) {
									String compId = bioPax.getElementMap().get(rightId).getId();
									List<String> dispName = bioPax.getElementMap().get(rightId).getParams().get("displayName");
									if(dispName == null) {
										dispName = bioPax.getElementMap().get(rightId).getParams().get("name");
									}
									Map<String, Object> rightParams = new HashMap<String, Object>();
									rightParams.put("id", compId);
									rightParams.put("name", dispName.get(0));
									right.add(rightParams);
								}
								interactionParams.remove("right-id");
								interactionParams.put("right", right);
							}
							
							if(interactionParams.containsKey("participant")) {
								List<Object> participant = new ArrayList<Object>();
								for(final Object participantId: interactionParams.get("participant")) {
									String compId = bioPax.getElementMap().get(participantId).getId();
									List<String> dispName = bioPax.getElementMap().get(participantId).getParams().get("displayName");
									if(dispName == null) {
										dispName = bioPax.getElementMap().get(participantId).getParams().get("name");
									}
									Map<String, Object> participantParams = new HashMap<String, Object>();
									participantParams.put("id", compId);
									participantParams.put("name", dispName.get(0));
									participant.add(participantParams);
								}
//								interactionParams.remove("participant");
								interactionParams.put("participant", participant);
							}
							
							if(interactionParams.containsKey("controller")) {
								List<Object> controller = new ArrayList<Object>();
								for(final Object controllerId: interactionParams.get("controller")) {
									String compId = bioPax.getElementMap().get(controllerId).getId();
									List<String> dispName = bioPax.getElementMap().get(controllerId).getParams().get("displayName");
									if(dispName == null) {
										dispName = bioPax.getElementMap().get(controllerId).getParams().get("name");
									}
									Map<String, Object> controllerParams = new HashMap<String, Object>();
									controllerParams.put("id", compId);
									controllerParams.put("name", dispName.get(0));
									controller.add(controllerParams);
								}
//								interactionParams.remove("controller");
								interactionParams.put("controller", controller);
							}
							
							if(interactionParams.containsKey("controlled")) {
								List<Object> controlled = new ArrayList<Object>();
								for(final Object controlledId: interactionParams.get("controlled")) {
									String compId = bioPax.getElementMap().get(controlledId).getId();
									List<String> dispName = bioPax.getElementMap().get(controlledId).getParams().get("displayName");
									if(dispName == null) {
										dispName = bioPax.getElementMap().get(controlledId).getParams().get("name");
									}
									Map<String, Object> controlledParams = new HashMap<String, Object>();
									controlledParams.put("id", compId);
									controlledParams.put("name", dispName.get(0));
									controlled.add(controlledParams);
								}
//								interactionParams.remove("controlled");
								interactionParams.put("controlled", controlled);
							}
							
							if(interactionParams.containsKey("xref-id")) {
								List<String> xrefTmp = new ArrayList<String>();
								for(Object o : interactionParams.get("xref-id")) xrefTmp.add((String) o);
								interactionParams.put("xref", resolveXref(xrefTmp));
								interactionParams.remove("xref-id");
							}
							
							if(interactionParams.containsKey("eCNumber")) {
								String ecNumber = (String) interactionParams.get("eCNumber").get(0);
								List<Object> l = new ArrayList<Object>();
								
								@SuppressWarnings("unchecked")
								HashMap<String, String> m = (HashMap<String, String>) species.get(0);
								String speciesName = m.get("name");
								
								if(ecNumberMap.containsKey(speciesName) && ecNumberMap.get(speciesName).containsKey(ecNumber)){
									l.add(ecNumberMap.get(speciesName).get(ecNumber));
									interactionParams.put("uniprot", l);
								}
							}
							
//							interactionParams.putAll((Map<? extends String, ? extends List<Object>>) bioPax.getElementMap().get(component).getParams());
							p.addInteraction(new Interaction(bioPax.getElementMap().get(component).getId(), type, interactionParams));
//							System.out.println("Found interaction: "+type);
							
							String compId = bioPax.getElementMap().get(component).getId();
							List<String> dispName = bioPax.getElementMap().get(component).getParams().get("displayName");
							if(dispName == null) {
								dispName = bioPax.getElementMap().get(component).getParams().get("name");
							}
							Map<String, Object> interactParams = new HashMap<String, Object>();
							interactParams.put("id", compId);
							interactParams.put("name", dispName.get(0));
							p.addInteractionId(interactParams);
							
							addPhysicalEntities(component, p, false);
							
//							addPhysicalEntityToInteraction(component, p);
						}
					}
				}
				
				// Set parent
				if(parentMap.containsKey(name)) p.setParentPathway(parentMap.get(name));
				else p.setParentPathway("none");
				p.addedEntities = null;
				p.addedInteractions = null;
				pathwayList.add(p);

                // TODO
//				dbObjList.add((DBObject)JSON.parseCosmic(gson.toJson(p)));
			}
//			System.out.println(gson.toJson(pathwayList));
//			String json = g.toJson(pathwayList);
//			IOUtils.write(new File("/tmp/test2.json"), json);
			
//			BSONObject b = (BSONObject)JSON.parseCosmic(json);
//			FileOutputStream fos = new FileOutputStream("/tmp/test2.bson");
//			fos.write(BSON.encode(b));
//			fos.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dbObjList;
	}
	
	public SubPathway searchSubPathways(String component, BiopaxPathway pathway) {
		String type = bioPax.getElementMap().get(component).getBioPaxClassName();
		
		if(type.equalsIgnoreCase("Pathway")) {
			List<String> displayName = bioPax.getElementMap().get(component).getParams().get("displayName");
			SubPathway sp = new SubPathway(component, displayName);
			if(bioPax.getElementMap().get(component).getParams().get("pathwayComponent-id") != null) {
//				pathway.setParentPathway(parent)
//				System.out.println("Component ID with pathway components: "+bioPax.getElementMap().get(component).getId()+" -- "+bioPax.getElementMap().get(component).getParams().get("pathwayComponent-id").size());
				for(String subcomponent: bioPax.getElementMap().get(component).getParams().get("pathwayComponent-id")) {
//					System.out.println("SubComponent ID: "+bioPax.getElementMap().get(subcomponent).getId());
					SubPathway sp2 = searchSubPathways(subcomponent, pathway);
					if(sp2 != null) sp.addSubpathways(sp2);
				}
			}
			return sp;
		}
		else if(type.equalsIgnoreCase("GeneticInteraction") || type.equalsIgnoreCase("MolecularInteraction") || type.equalsIgnoreCase("TemplateReaction") || type.equalsIgnoreCase("Catalysis") || type.equalsIgnoreCase("Modulation") || type.equalsIgnoreCase("TemplateReactionRegulation") || type.equalsIgnoreCase("BiochemicalReaction") || type.equalsIgnoreCase("ComplexAssembly") || type.equalsIgnoreCase("Degradation") || type.equalsIgnoreCase("Transport") || type.equalsIgnoreCase("TransportWithBiochemicalReaction")) {
//			System.out.println("Found interaction: "+type+" in subpathway");
//			p.interactions.add(new Interaction(bioPax.getElementMap().get(component).getId()));
			String compId = bioPax.getElementMap().get(component).getId();
			List<String> dispName = bioPax.getElementMap().get(component).getParams().get("displayName");
			if(dispName == null) {
				dispName = bioPax.getElementMap().get(component).getParams().get("name");
			}
			Map<String, Object> interactParams = new HashMap<String, Object>();
			interactParams.put("id", compId);
			interactParams.put("name", dispName.get(0));
			pathway.addInteractionId(interactParams);
			addPhysicalEntities(compId, pathway, true);
		}
//		else if(type.equalsIgnoreCase("PhysicalEntity") || type.equalsIgnoreCase("Complex") || type.equalsIgnoreCase("DNA") || type.equalsIgnoreCase("DNARegion") || type.equalsIgnoreCase("Protein") || type.equalsIgnoreCase("RNA") || type.equalsIgnoreCase("RNARegion") || type.equalsIgnoreCase("SmallMolecule")) {
//			System.out.println("Found physical entity: "+type+" in subpathway");
////			p.physicalEntities.add(new PhysicalEntity(bioPax.getElementMap().get(component).getId()));
//		}
		else System.out.println("Another type: "+type+" found.");
		return null;
	}
	
	public void addPhysicalEntities(String interaction, BiopaxPathway pathway, boolean onlyIDs) {
		String type = bioPax.getElementMap().get(interaction).getBioPaxClassName();
		List<String> tempList = new ArrayList<String>();
		
		if(type.equalsIgnoreCase("GeneticInteraction") || type.equalsIgnoreCase("MolecularInteraction")) {
			if(bioPax.getElementMap().get(interaction).getParams().get("participant") != null) {
				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("participant"));
			}
		}
		else if(type.equalsIgnoreCase("TemplateReaction")) {
			if(bioPax.getElementMap().get(interaction).getParams().get("template") != null) {
				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("template"));
			}
			if(bioPax.getElementMap().get(interaction).getParams().get("product-id") != null) {
				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("product-id"));
			}
		}
		else if(type.equalsIgnoreCase("Catalysis")) {
			if(bioPax.getElementMap().get(interaction).getParams().get("cofactor") != null) {
				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("cofactor"));
			}
			if(bioPax.getElementMap().get(interaction).getParams().get("controller") != null) {
				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("controller"));
			}
			if(bioPax.getElementMap().get(interaction).getParams().get("controlled") != null) {
				for(String catalysis: bioPax.getElementMap().get(interaction).getParams().get("controlled")) {
					addPhysicalEntities(catalysis, pathway, onlyIDs);
				}
			}
		}
		else if(type.equalsIgnoreCase("Modulation")) {
			if(bioPax.getElementMap().get(interaction).getParams().get("controller") != null) {
				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("controller"));
			}
			if(bioPax.getElementMap().get(interaction).getParams().get("controlled") != null) {
				for(String catalysis: bioPax.getElementMap().get(interaction).getParams().get("controlled")) {
					addPhysicalEntities(catalysis, pathway, onlyIDs);
				}
			}
		}
		else if(type.equalsIgnoreCase("TemplateReactionRegulation")) {
			if(bioPax.getElementMap().get(interaction).getParams().get("controller") != null) {
				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("controller"));
			}
			if(bioPax.getElementMap().get(interaction).getParams().get("controlled") != null) {
				for(String catalysis: bioPax.getElementMap().get(interaction).getParams().get("controlled")) {
					addPhysicalEntities(catalysis, pathway, onlyIDs);
				}
			}
		}
		else if(type.equalsIgnoreCase("BiochemicalReaction") || type.equalsIgnoreCase("ComplexAssembly") || type.equalsIgnoreCase("Degradation") || type.equalsIgnoreCase("Transport") || type.equalsIgnoreCase("TransportWithBiochemicalReaction")) {
			if(bioPax.getElementMap().get(interaction).getParams().get("left-id") != null) {
				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("left-id"));
			}
			if(bioPax.getElementMap().get(interaction).getParams().get("right-id") != null) {
				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("right-id"));
			}
			if(bioPax.getElementMap().get(interaction).getParams().get("participant") != null) {
				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("participant"));
			}
		}
		
		if(onlyIDs) {
			for(String entity: tempList) {
				String compId = bioPax.getElementMap().get(entity).getId();
				List<String> dispName = bioPax.getElementMap().get(entity).getParams().get("displayName");
				if(dispName == null) {
					dispName = bioPax.getElementMap().get(entity).getParams().get("name");
				}
				Map<String, Object> entityObj = new HashMap<String, Object>();
				entityObj.put("id", compId);
				entityObj.put("name", dispName.get(0));
//				pathway.addInteractionId(entityObj);
				addPhysicalEntities(compId, pathway, true);
				pathway.addEntityId(entityObj);
			}
		}
		else {
			for(String entity: tempList) {
				if(bioPax.getElementMap().get(entity) != null) {
					addPhysicalEntity(entity, pathway);
				}
			}
		}
	}
	
	public void addPhysicalEntity(String entity, BiopaxPathway pathway) {
		String entityType = bioPax.getElementMap().get(entity).getBioPaxClassName();
		
//		Map<String, List<String>> entityParams = bioPax.getElementMap().get(entity).getParams();
		
		Map<String, List<Object>> entityParams = new HashMap<String, List<Object>>();
		for (Map.Entry<String, List<String>> entry : bioPax.getElementMap().get(entity).getParams().entrySet()) {
			String key = entry.getKey();
			List<Object> values = new ArrayList<Object>(entry.getValue());
			
			entityParams.put(key, values);
		}
		
		if(entityParams.containsKey("xref-id")) {
			List<String> xrefTmp = new ArrayList<String>();
			for(Object o : entityParams.get("xref-id")) xrefTmp.add((String) o);
			entityParams.put("xref", resolveXref(xrefTmp));
			entityParams.remove("xref-id");
		}
		
		if(entityParams.containsKey("cellularLocation-id")) {
			List<String> cellLocTmp = new ArrayList<String>();
			for(Object o : entityParams.get("cellularLocation-id")) cellLocTmp.add((String) o);
			entityParams.put("cellularLocation", resolveCellularLocation(cellLocTmp));
			entityParams.remove("cellularLocation-id");
		}
		
		if(entityParams.containsKey("componentStoichiometry-id")) {
			List<Object> stoichiometry = new ArrayList<Object>();
			for(Object compSto: entityParams.get("componentStoichiometry-id")) {
				stoichiometry.add(bioPax.getElementMap().get(compSto));
			}
			entityParams.remove("componentStoichiometry-id");
			entityParams.put("componentStoichiometry", stoichiometry);
		}
		
		pathway.addPhysicalEntity(new PhysicalEntity(entity, entityType, entityParams));
		
		String compId = bioPax.getElementMap().get(entity).getId();
		List<String> dispName = bioPax.getElementMap().get(entity).getParams().get("displayName");
		if(dispName == null) {
			dispName = bioPax.getElementMap().get(entity).getParams().get("name");
		}
		Map<String, Object> entityObj = new HashMap<String, Object>();
		entityObj.put("id", compId);
		entityObj.put("name", dispName.get(0));
//		pathway.addInteractionId(entityObj);
		addPhysicalEntities(compId, pathway, true);
		pathway.addEntityId(entityObj);
		
		// If entity is a complex add too the component-id entity
		if(entityType.equals("Complex") && entityParams.containsKey("component-id")) {
			addPhysicalEntity((String) entityParams.get("component-id").get(0), pathway);
		}
	}
	
//	public void addPhysicalEntityToInteraction(String interaction, BiopaxPathway pathway) {
//		String type = bioPax.getElementMap().get(interaction).getBioPaxClassName();
//		List<String> tempList = new ArrayList<String>();
//		
//		if(type.equalsIgnoreCase("GeneticInteraction") || type.equalsIgnoreCase("MolecularInteraction")) {
//			if(bioPax.getElementMap().get(interaction).getParams().get("participant") != null) {
//				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("participant"));
//			}
//		}
////		else if(type.equalsIgnoreCase("TemplateReaction")) {
////			if(bioPax.getElementMap().get(interaction).getParams().get("template") != null) {
////				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("template"));
////			}
////			if(bioPax.getElementMap().get(interaction).getParams().get("product") != null) {
////				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("product"));
////			}
////		}
////		else if(type.equalsIgnoreCase("Catalysis")) {
////			if(bioPax.getElementMap().get(interaction).getParams().get("cofactor") != null) {
////				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("cofactor"));
////			}
////			if(bioPax.getElementMap().get(interaction).getParams().get("controller") != null) {
////				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("controller"));
////			}
////			if(bioPax.getElementMap().get(interaction).getParams().get("controlled") != null) {
////				for(String catalysis: bioPax.getElementMap().get(interaction).getParams().get("controlled")) {
////					addPhysicalEntities(catalysis, pathway, onlyIDs);
////				}
////			}
////		}
////		else if(type.equalsIgnoreCase("Modulation")) {
////			if(bioPax.getElementMap().get(interaction).getParams().get("controller") != null) {
////				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("controller"));
////			}
////			if(bioPax.getElementMap().get(interaction).getParams().get("controlled") != null) {
////				for(String catalysis: bioPax.getElementMap().get(interaction).getParams().get("controlled")) {
////					addPhysicalEntities(catalysis, pathway, onlyIDs);
////				}
////			}
////		}
////		else if(type.equalsIgnoreCase("TemplateReactionRegulation")) {
////			if(bioPax.getElementMap().get(interaction).getParams().get("controller") != null) {
////				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("controller"));
////			}
////			if(bioPax.getElementMap().get(interaction).getParams().get("controlled") != null) {
////				for(String catalysis: bioPax.getElementMap().get(interaction).getParams().get("controlled")) {
////					addPhysicalEntities(catalysis, pathway, onlyIDs);
////				}
////			}
////		}
//		else if(type.equalsIgnoreCase("BiochemicalReaction") || type.equalsIgnoreCase("ComplexAssembly") || type.equalsIgnoreCase("Degradation") || type.equalsIgnoreCase("Transport") || type.equalsIgnoreCase("TransportWithBiochemicalReaction")) {
//			if(bioPax.getElementMap().get(interaction).getParams().get("left-id") != null) {
//				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("left-id"));
//			}
//			if(bioPax.getElementMap().get(interaction).getParams().get("right-id") != null) {
//				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("right-id"));
//			}
//			if(bioPax.getElementMap().get(interaction).getParams().get("participant") != null) {
//				tempList.addAll(bioPax.getElementMap().get(interaction).getParams().get("participant"));
//			}
//		}
//		
//		for(String entity: tempList) {
//			if(bioPax.getElementMap().get(entity) != null) {
//				String entityType = bioPax.getElementMap().get(entity).getBioPaxClassName();
//				Map<String, List<String>> params = bioPax.getElementMap().get(entity).getParams();
//				pathway.getphysicalEntities().add(new PhysicalEntity(entity, entityType, params));
//
//				pathway.addEntityId(entity);					
//			}
//		}
//	}
	
	public void findChildren(String component) {
		String type = bioPax.getElementMap().get(component).getBioPaxClassName();
		
		if(type.equalsIgnoreCase("Pathway")) {
			if(bioPax.getElementMap().get(component).getParams().get("pathwayComponent-id") != null) {
				for(String subcomponent: bioPax.getElementMap().get(component).getParams().get("pathwayComponent-id")) {
					if(bioPax.getElementMap().get(subcomponent).getBioPaxClassName().equalsIgnoreCase("Pathway")) {
//						System.out.println("Subpathway: "+subcomponent+" Parent: "+component);
						parentMap.put(subcomponent, component);
					}
				}
			}
		}
	}
	
	public List<Object> resolveXref(List<String> xrefList) {
		List<Object> xrefObjList = new ArrayList<Object>();
		
		for(String xref : xrefList) {
			Map<String, String> xrefObj = new HashMap<String, String>();
			for(String key : bioPax.getElementMap().get(xref).getParams().keySet()) {
				xrefObj.put(key, bioPax.getElementMap().get(xref).getParams().get(key).get(0));
			}
			xrefObjList.add(xrefObj);
		}
		
		return xrefObjList;
	}
	
	public List<Object> resolveCellularLocation(List<String> cellLocList) {
		List<Object> cellLocObjList = new ArrayList<Object>();
		
		for(String cellLoc : cellLocList) {
			Map<String, Object> cellLocObj = new HashMap<String, Object>();
			for(String key : bioPax.getElementMap().get(cellLoc).getParams().keySet()) {
				List<String> value = bioPax.getElementMap().get(cellLoc).getParams().get(key);
				if(key.equals("xref-id")) {
					cellLocObj.put("xref", resolveXref(value));
				}
				else {
					cellLocObj.put(key, value.get(0));
				}
			}
			cellLocObjList.add(cellLocObj);
		}
		
		return cellLocObjList;
	}
	
	public List<Object> resolveSpecies(List<String> speciesList) {
		List<Object> speciesObjList = new ArrayList<Object>();
		
		for(String species : speciesList) {
			Map<String, Object> speciesObj = new HashMap<String, Object>();
			for(String key : bioPax.getElementMap().get(species).getParams().keySet()) {
				List<String> value = bioPax.getElementMap().get(species).getParams().get(key);
				if(key.equals("xref-id")) {
					speciesObj.put("xref", resolveXref(value));
				}
				else {
					speciesObj.put(key, value.get(0));
				}
			}
			speciesObjList.add(speciesObj);
		}
		
		return speciesObjList;
	}
	
	public void loadECNumbers(String filename) {
		try {
			List<String> lines = Files.readAllLines(Paths.get(filename), Charset.defaultCharset());
			for(String line: lines) {
				String[] fields = line.split("\t");
				
				String ecNumber = fields[0];
				String species = fields[3];
				
				HashMap<String, String> uniprotObj = new HashMap<String, String>();
				uniprotObj.put("primaryAccesion", fields[1]);
				uniprotObj.put("name", fields[2]);
				
				if(ecNumberMap.containsKey(species)) {
					ecNumberMap.get(species).put(ecNumber, uniprotObj);
				}
				else {
					HashMap<String, HashMap<String, String>> m = new HashMap<String, HashMap<String, String>>();
					m.put(ecNumber, uniprotObj);
					ecNumberMap.put(species, m);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

