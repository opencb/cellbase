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

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class Region {

    private String chromosome;
    private int start;
    private int end;

    public Region(String chromosome, int start, int end) {
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
    }

    public static Region parseRegion(String regionString) {
        Region region = null;
        if (regionString != null && !regionString.equals("")) {
            if (regionString.indexOf(':') != -1) {
                String[] fields = regionString.split("[:-]", -1);
                if (fields.length == 3) {
                    region = new Region(fields[0], Integer.parseInt(fields[1]), Integer.parseInt(fields[2]));
                }
            } else {
                region = new Region(regionString, 0, Integer.MAX_VALUE);
            }
        }
        return region;
    }

    public static List<Region> parseRegions(String regionsString) {
        List<Region> regions = null;
        if (regionsString != null && !regionsString.equals("")) {
            String[] regionItems = regionsString.split(",");
            regions = new ArrayList<Region>(regionItems.length);
            String[] fields;
            for (String regionString : regionItems) {
                if (regionString.indexOf(':') != -1) {
                    fields = regionString.split("[:-]", -1);
                    if (fields.length == 3) {
                        regions.add(new Region(fields[0], Integer.parseInt(fields[1]), Integer.parseInt(fields[2])));
                    } else {
                        regions.add(null);
                    }
                } else {
                    regions.add(new Region(regionString, 0, Integer.MAX_VALUE));
                }
            }
        }
        return regions;
    }

    public static String parseRegionList(List<Region> regions) {
        if (regions == null) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < regions.size() - 1; i++) {
                if (regions.get(i) != null) {
                    sb.append(regions.get(i).toString()).append(",");
                } else {
                    sb.append("null,");
                }
            }
            if (regions.get(regions.size() - 1) != null) {
                sb.append(regions.get(regions.size() - 1).toString());
            } else {
                sb.append("null");
            }

            return sb.toString();
        }
    }


    @Override
    public String toString() {
        return chromosome + ":" + start + "-" + end;
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
     * @return the start
     */
    public int getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(int start) {
        this.start = start;
    }


    /**
     * @return the end
     */
    public int getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Region region = (Region) o;

        if (end != region.end) {
            return false;
        }
        if (start != region.start) {
            return false;
        }
        if (!chromosome.equals(region.chromosome)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = chromosome.hashCode();
        result = 31 * result + (int) (start ^ (start >>> 32));
        result = 31 * result + (int) (end ^ (end >>> 32));
        return result;
    }

    public boolean contains(String chr, int pos) {
        return this.chromosome.equals(chr) && this.start <= pos && this.end >= pos;
    }

}
