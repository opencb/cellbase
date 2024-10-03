/*
 * Copyright 2015-2020 OpenCB
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


package org.opencb.cellbase.lib.builders;

import org.opencb.biodata.formats.obo.OboParser;
import org.opencb.biodata.models.core.OntologyTerm;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.nio.file.Path;
import java.util.List;

public class OntologyBuilder extends CellBaseBuilder {

    private Path hpoFile;
    private Path goFile;
    private Path doidFile;

    public OntologyBuilder(Path oboDirectoryPath, CellBaseSerializer serializer) {
        super(serializer);
        hpoFile = oboDirectoryPath.resolve(EtlCommons.HPO_FILE);
        goFile = oboDirectoryPath.resolve(EtlCommons.GO_FILE);
        doidFile = oboDirectoryPath.resolve(EtlCommons.DOID_FILE);
    }

    @Override
    public void parse() throws Exception {
        BufferedReader bufferedReader = FileUtils.newBufferedReader(hpoFile);
        OboParser parser = new OboParser();
        List<OntologyTerm> terms = parser.parseOBO(bufferedReader, "Human Phenotype Ontology");
        for (OntologyTerm term : terms) {
            term.setSource("HP");
            serializer.serialize(term);
        }

        bufferedReader = FileUtils.newBufferedReader(goFile);
        terms = parser.parseOBO(bufferedReader, "Gene Ontology");
        for (OntologyTerm term : terms) {
            term.setSource("GO");
            serializer.serialize(term);
        }

        bufferedReader = FileUtils.newBufferedReader(doidFile);
        terms = parser.parseOBO(bufferedReader, "Human Disease Ontology");
        for (OntologyTerm term : terms) {
            term.setSource("DOID");
            serializer.serialize(term);
        }

        serializer.close();
    }
}
