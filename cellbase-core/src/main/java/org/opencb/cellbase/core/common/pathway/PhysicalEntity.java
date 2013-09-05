package org.opencb.cellbase.core.common.pathway;

import java.util.List;
import java.util.Map;

public class PhysicalEntity {
	String name, type;
	Map<String, List<Object>> params;
	
	public PhysicalEntity(String id, String type, Map<String, List<Object>> params) {
		this.name = id;
		this.type = type;
		this.params = params;
	}
}
