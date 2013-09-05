package org.opencb.cellbase.core.common;

import java.util.ArrayList;
import java.util.List;


public class Position {

	private String chromosome;
	private int position;

	public Position(String chromosome, int position) {
		this.chromosome = chromosome;
		this.position = position;
	}

	public static Position parsePosition(String positionString) {
		Position position = null;
		if(positionString != null && !positionString.equals("")) {
			String[] fields = positionString.split(":", -1);
			if(fields.length == 2) {
				position = new Position(fields[0], Integer.parseInt(fields[1]));
			}else {
				position = null;
			}
		}
		return position;
	}

	public static List<Position> parsePositions(String positionString) {
		List<Position> positions = null;
		if(positionString != null && !positionString.equals("")) {
			String[] positionItems = positionString.split(",");
			positions = new ArrayList<Position>(positionItems.length);
			String[] fields;
			for(String posString: positionItems) {
				fields = posString.split(":", -1);
				if(fields.length == 2) {
					positions.add(new Position(fields[0], Integer.parseInt(fields[1])));
				}else {
					positions.add(null);
				}
			}			
		}
		return positions;
	}

	
	@Override
	public String toString() {
		return chromosome+":"+position;
	}

	
	/**
	 * @return the chromosome
	 */
	public String getChromosome() {
		return chromosome;
	}

	/**
	 * @param chromosome the chromosome to set
	 */
	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}

	
	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}
	
}
