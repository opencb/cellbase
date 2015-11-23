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

package org.opencb.cellbase.core.db.api.regulatory;


import org.opencb.cellbase.core.common.IntervalFeatureFrequency;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.common.regulatory.MirnaDisease;
import org.opencb.cellbase.core.common.regulatory.MirnaGene;
import org.opencb.cellbase.core.common.regulatory.MirnaMature;
import org.opencb.cellbase.core.common.regulatory.MirnaTarget;

import java.util.List;

public interface MirnaDBAdaptor {


    List<MirnaGene> getMiRnaGeneByName(String geneName);

    List<List<MirnaGene>> getAllMiRnaGenesByNameList(List<String> geneNames);

    List<MirnaGene> getAllMiRnaGenesByMiRnaMature(String miRnaMatureName);

    List<List<MirnaGene>> getAllMiRnaGenesByMiRnaMatureList(List<String> miRnaMatureNameList);

    List<MirnaGene> getAllMiRnaGenesByDisease(String disease);

    List<List<MirnaGene>> getAllMiRnaGenesByDiseaseList(List<String> diseaseList);

    List<MirnaGene> getAllMiRnaGenesByGeneName(String geneName);

    List<List<MirnaGene>> getAllMiRnaGenesByGeneNameList(List<String> geneNames);


    List<MirnaMature> getMiRnaMatureByName(String miRnaMatureName);

    List<List<MirnaMature>> getAllMiRnaMaturesByNameList(List<String> miRnaMatureNameList);

    List<MirnaMature> getAllMiRnaMaturesByMiRnaGene(String miRnaGeneName);

    List<List<MirnaMature>> getAllMiRnaMaturesByMiRnaGeneList(List<String> miRnaGeneNameList);

    List<MirnaMature> getAllMiRnaMaturesByGeneName(String geneName);

    List<List<MirnaMature>> getAllMiRnaMaturesByGeneNameList(List<String> geneNames);


    List<MirnaTarget> getAllMiRnaTargetsByMiRnaMature(String id, List<String> source);

    List<List<MirnaTarget>> getAllMiRnaTargetsByMiRnaMatureList(List<String> ids, List<String> source);

    List<MirnaTarget> getAllMiRnaTargetsByMiRnaGene(String geneName, List<String> source);

    List<List<MirnaTarget>> getAllMiRnaTargetsByMiRnaGeneList(List<String> geneNames, List<String> sources);

    List<MirnaTarget> getAllMiRnaTargetsByGeneName(String geneName);

    List<List<MirnaTarget>> getAllMiRnaTargetsByGeneNameList(List<String> geneNames);

    List<MirnaTarget> getAllMiRnaTargetsByPosition(String chromosome, int start);

    List<MirnaTarget> getAllMiRnaTargetsByRegion(String chromosome, int start, int end);

    List<MirnaTarget> getAllMiRnaTargetsByRegion(Region region);

    List<List<MirnaTarget>> getAllMiRnaTargetsByRegionList(List<Region> regionList);

    List<IntervalFeatureFrequency> getAllMirnaTargetsIntervalFrequencies(Region region, int interval);


    List<MirnaDisease> getAllMiRnaDiseasesByMiRnaGene(String mirbaseId);

    List<List<MirnaDisease>> getAllMiRnaDiseasesByMiRnaGeneList(List<String> mirbaseId);

    List<MirnaDisease> getAllMiRnaDiseasesByMiRnaMature(String mirbaseId);

    List<List<MirnaDisease>> getAllMiRnaDiseasesByMiRnaMatureList(List<String> mirbaseId);


    List<Object> getAllAnnotation();

    List<Object> getAllAnnotationBySourceList(List<String> sourceList);

}
