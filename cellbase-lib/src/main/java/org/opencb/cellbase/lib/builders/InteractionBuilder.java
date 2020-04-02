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

import org.opencb.biodata.models.protein.*;
import org.opencb.biodata.models.protein.Interactor;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
//import psidev.psi.mi.tab.PsimiTabReader;
//import psidev.psi.mi.tab.model.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/***
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 12/3/13
 * Time: 4:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class InteractionBuilder extends CellBaseBuilder {

    private final String species;
    private final Path psimiTabFile;

    public InteractionBuilder(Path psimiTabFile, String species, CellBaseSerializer serializer) {
        super(serializer);
        this.species = species;
        this.psimiTabFile = psimiTabFile;
    }

    public void parse() throws IOException {

        boolean isSpeciesA = false;
        boolean isSpeciesB = false;
        Set<String> speciesSet = new HashSet<>(Arrays.asList(species.split(",")));
        Set<String> nonCuratedTerms = new HashSet<>(Arrays
                .asList(species.split("MI:0001,MI:0024,MI:0026,MI:0035,MI:0036,MI:0037,MI:0045,MI:0046,MI:0057,MI:0058,MI:0063,MI:0064,"
                        + "MI:0085,MI:0087,MI:0100,MI:0101,MI:0105,MI:0110,MI:0254,MI:0362,MI:0363,MI:0364,MI:0439,MI:0441,MI:0686,MI:1176,"
                        + "MI:1177,MI:1178")));

        Interaction interaction;
        Interactor interactorA;
        Interactor interactorB;
    }
}
