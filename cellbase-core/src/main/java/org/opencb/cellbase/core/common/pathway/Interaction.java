package org.opencb.cellbase.core.common.pathway;

import java.util.List;
import java.util.Map;

public class Interaction {
	String id, name, type;
	Map<String, List<Object>> params;
	
	public Interaction(String id, String type, Map<String, List<Object>> params) {
		this.name = id;
		this.type = type;
		this.params = params;
	}
}
