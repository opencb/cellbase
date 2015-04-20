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

package org.opencb.cellbase.server.ws.genomic;

import org.opencb.cellbase.core.common.core.Gene;
import org.opencb.cellbase.core.common.regulatory.RegulatoryRegion;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class Histogram {
	public int intervalSize;
	private List<?> objects;
	private List<Interval> intervals;
	private List<?> features;
	
	private int min_start = Integer.MAX_VALUE;
	private int max_end = Integer.MIN_VALUE;
	
	public Histogram(List<?> list, int intervalSize) {
		this.objects = list;
		this.intervals = new ArrayList<Interval>();
		this.intervalSize = intervalSize;
	}
	
	public List<Interval> getIntervals(){	
		this.features = joinFeatures(this.objects);
		
		int intervalsCount = 1 + (max_end - min_start)/this.intervalSize;
		
		for (int i = 0; i < intervalsCount; i++) {
			int range_start = min_start + (i * this.intervalSize);
			int range_end = range_start + this.intervalSize;
			System.out.println("\t\t" + range_start + ": " + range_end);
			this.intervals.add(new Interval(range_start, range_end, 0));
		}
		
		for (Object feature : this.features) {
			this.addFeatureToIntervals(feature);
		}
		
		if (this.features.size() > 0){
			normalizeIntervals(this.intervals);
		}
		return this.intervals;
	}
	
	private void normalizeIntervals(List<Interval> intervals){
		float max = 0;
		for (int i = 0; i < intervals.size(); i++) {
			if (intervals.get(i).value > max){
				max = intervals.get(i).value;
			}
		}
		if (max == 0) return;
		
		/** Reescalamos sobre 1 **/
		for (int i = 0; i < intervals.size(); i++) {
			intervals.get(i).value = intervals.get(i).value/max;
		}
	}
	
	private void addFeatureToIntervals(Object feature){
		for (int i = 0; i < this.intervals.size(); i++) {
			if (feature instanceof Gene){
				if (((Gene)feature).getStart() >= this.intervals.get(i).start &&(((Gene)feature).getStart() < this.intervals.get(i).end)){
					this.intervals.get(i).value ++;
				}
			}
			
			if (feature instanceof RegulatoryRegion){
				if (((RegulatoryRegion)feature).getStart() >= this.intervals.get(i).start &&(((RegulatoryRegion)feature).getStart() < this.intervals.get(i).end)){
					this.intervals.get(i).value ++;
				}
			}
		}
	}
	
	
	/** De una list<Object> o una List<List<Object>> coge todas las features y las mete en la misma estructura de datos **/
	private  List<?>  joinFeatures(List<?> list){
		List result = new ArrayList<Object>();
	
		for (Object object : list) {
			int start = -1;
			int end = -1;
			
			if (object instanceof Gene){
				result.add(object);
				start = ((Gene)object).getStart();
				end = ((Gene)object).getEnd();
			}
			
			if (object instanceof RegulatoryRegion){
				result.add(object);
				start = ((RegulatoryRegion)object).getStart();
				end = ((RegulatoryRegion)object).getEnd();
			}
			
			if (object instanceof List){
				result.addAll(joinFeatures((List)object));
			}
			
			/** Acutualizamos los valores maximo de end y minimo de start **/
			if ((start != -1)&&(start < min_start)){
				min_start = start;
			}
			
			if ((end != -1)&&(end > max_end)){
				max_end = end;
			}
		}
		
		return result;
	}
	
}

