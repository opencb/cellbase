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

package org.opencb.cellbase.server.ws.network;

import org.opencb.biodata.formats.graph.dot.Dot;
import org.opencb.cellbase.core.db.api.systems.PathwayDBAdaptor;

import java.util.HashMap;
import java.util.Map;

public class DotServer {

    private Dot dot = null;

    private int splitChunk = 13;
    private String splitSep = "\\n";

    //private BioPaxServer dbAdaptor = null;
    private PathwayDBAdaptor dbAdaptor;
    private Map<Integer, Boolean> map = new HashMap<>();

    public DotServer() {
        this("hsa");
    }

    public DotServer(String species) {
//      dbAdaptor = dbAdaptorFactory.getBioPaxDBAdaptor(species);
    }

    //=========================================================================
    //    generate DOT
    //=========================================================================
    /*
    public Dot generateDot(Pathway input) {
        dot = new Dot(getFirstName(input.getBioEntity()), true);
        dot.setAttribute(Dot.BGCOLOR, "white");
        add(input);
//        dbAdaptor.getSession().clear();
        return dot;
    }

    public Dot generateDot(Complex input) {
        dot = new Dot(getFirstName(input.getPhysicalEntity().getBioEntity()), true);
        dot.setAttribute(Dot.BGCOLOR, "white");
        add(input);
//        dbAdaptor.getSession().clear();
        return dot;
    }

    //=========================================================================
    //    Complex
    //=========================================================================
    private void add(Complex input) {
        Edge edge = null;
        Node child = null;
        Node parent = new Node("entity_" + input.getPhysicalEntity().getBioEntity().getPkEntity());

        parent.setAttribute(Node.LABEL, getFirstName(input.getPhysicalEntity().getBioEntity()));
        parent.setAttribute(Node.ID, ""+input.getPkComplex());
        parent.setAttribute(Node.SHAPE, Node.SHAPE_VALUES.octagon.name());
        parent.setAttribute(Node.FILLCOLOR, "#CCFFFF");
        parent.setAttribute(Node.STYLE, Node.STYLE_VALUES.filled.name());

        dot.addNode(parent);

        Iterator it = input.getPhysicalEntities().iterator();
        while (it.hasNext()) {
            child = null;
            PhysicalEntity ph = (PhysicalEntity) it.next();
            if (dbAdaptor.isProtein(ph)) {
                child = new Node("entity_" + ph.getBioEntity().getPkEntity());

                Protein protein = dbAdaptor.getProtein(ph);
                String name = getFirstName(ph.getBioEntity());
                List<String> names = dbAdaptor.getProteinReferenceNames(protein);
                if (names!=null && names.size()>0) {
                    name = names.get(0);
                }

                child.setAttribute(Node.LABEL, name);
                child.setAttribute(Node.ID, ""+protein.getPkProtein());
                child.setAttribute(Node.SHAPE, Node.SHAPE_VALUES.box.name());
                child.setAttribute(Node.FILLCOLOR, "#CCFFCC");
                child.setAttribute(Node.STYLE, Node.STYLE_VALUES.filled.name() + "," + Node.STYLE_VALUES.rounded.name());
            } else if (dbAdaptor.isSmallMolecule(ph)) {
                child = new Node("entity_" + ph.getBioEntity().getPkEntity());

                child.setAttribute(Node.LABEL, getFirstName(ph.getBioEntity()));
                child.setAttribute(Node.ID, ""+dbAdaptor.getSmallMolecule(ph).getPkSmallMolecule());
                child.setAttribute(Node.SHAPE, Node.SHAPE_VALUES.ellipse.name());
                child.setAttribute(Node.FILLCOLOR, "#CCFFCC");
                child.setAttribute(Node.STYLE, Node.STYLE_VALUES.filled.name());
            } else if (dbAdaptor.isComplex(ph)) {
                add(dbAdaptor.getComplex(ph));
            } else {
                child = new Node("entity_" + ph.getBioEntity().getPkEntity());

                child.setAttribute(Node.LABEL, getFirstName(ph.getBioEntity()));
                child.setAttribute(Node.SHAPE, Node.SHAPE_VALUES.egg.name());
                child.setAttribute(Node.FILLCOLOR, "#CCFFCC");
                child.setAttribute(Node.STYLE, Node.STYLE_VALUES.filled.name());
            }

            if (child!=null) {
                dot.addNode(child);
            }

            edge = new Edge("entity_" + ph.getBioEntity().getPkEntity(), "entity_"
            + input.getPhysicalEntity().getBioEntity().getPkEntity(), true);
            edge.setAttribute(Edge.ARROWHEAD, "empty");
            dot.addEdge(edge);
        }
    }

    //=========================================================================
    //    Physical Entities
    //=========================================================================

    private void addNode(PhysicalEntity input) {

        Node node = new Node("entity_" + input.getBioEntity().getPkEntity());

        String name = getFirstName(input.getBioEntity());
        //node.setAttribute(Node.LABEL, insertStringEvery(name, splitSep, splitChunk));
        node.setAttribute(Node.LABEL, name);

        node.setAttribute(Node.SHAPE, Node.SHAPE_VALUES.egg.name());
        node.setAttribute(Node.FILLCOLOR, "#CCFFCC");
        node.setAttribute(Node.STYLE, Node.STYLE_VALUES.filled.name());

        String group = null;
        try {
            group = ((Term) input.getCellularLocationVocabulary().getControlledVocabulary().getTerms().iterator().next()).getTerm();
        } catch (Exception e) {
            group = null;
        }
        if (group!=null) {
            node.setAttribute(Node.GROUP, group);
        }

        if (dbAdaptor.isProtein(input)) {
            node.setAttribute(Node.ID, ""+dbAdaptor.getProtein(input).getPkProtein());
            node.setAttribute(Node.SHAPE, Node.SHAPE_VALUES.box.name());
            node.setAttribute(Node.STYLE, Node.STYLE_VALUES.filled.name() + "," + Node.STYLE_VALUES.rounded.name());
        } else if (dbAdaptor.isSmallMolecule(input)) {
            node.setAttribute(Node.ID, ""+dbAdaptor.getSmallMolecule(input).getPkSmallMolecule());
            node.setAttribute(Node.SHAPE, Node.SHAPE_VALUES.ellipse.name());
        } else if (dbAdaptor.isComplex(input)) {
            node.setAttribute(Node.ID, ""+dbAdaptor.getComplex(input).getPkComplex());
            node.setAttribute(Node.SHAPE, Node.SHAPE_VALUES.octagon.name());
            node.setAttribute(Node.FILLCOLOR, "#CCFFFF");
        }

        dot.addNode(node);
    }


    //=========================================================================
    //    Interactions
    //=========================================================================

    //-------------------------------------------------------------------------
    //    Control
    //-------------------------------------------------------------------------

    private String addEdges(Control input) {
        StringBuilder output = new StringBuilder();

        // controlled: 0..1, Interaction or Pathway
        //
        Pathway controlledPw= input.getPathway();
        Interaction controlledIt = input.getInteractionByControlledInteraction();

        // controller: 0..*, PhysicalEntity or Pathway
        //
        List<PhysicalEntity> controllerPes = new ArrayList<PhysicalEntity>();
        List<Pathway> controllerPws = new ArrayList<Pathway>();

        // PhysicalEntity

        if (input.getPhysicalEntities()!=null && !input.getPhysicalEntities().isEmpty()) {
            Iterator it = input.getPhysicalEntities().iterator();
            while (it.hasNext()) {
                PhysicalEntity pe = (PhysicalEntity) it.next();
                if (!controllerPes.contains(pe)) {
                    addNode(pe);
                    //output.append(getDot(pe));
                    controllerPes.add(pe);
                }
            }
        }
        // Pathway
        if (input.getPathwaies()!=null && !input.getPathwaies().isEmpty()) {
            Iterator it = input.getPathwaies().iterator();
            while (it.hasNext()) {
                Pathway pw = (Pathway) it.next();
                System.out.println("!! controller pathwaies not implemented (pathway: " + pw.getPkPathway() + ", "
                + getFirstName(pw.getBioEntity())+ ")");
            }
        }

        String arrowhead = "none";
        if ("activation".equalsIgnoreCase(input.getControlType())) {
            if (dbAdaptor.isCatalysis(input)) {
                arrowhead = "odot";
            } else {
                arrowhead = "empty";
            }
        } else if ("inhibition".equalsIgnoreCase(input.getControlType())) {
            arrowhead = "tee";
        }


        if (controlledIt!=null && controllerPes.size()>0) {
            Edge edge = null;
            for(PhysicalEntity pe: controllerPes) {
                edge = new Edge("entity_" + pe.getBioEntity().getPkEntity(), "entity_" + controlledIt.getBioEntity().getPkEntity(), true);
                edge.setAttribute(Edge.ARROWHEAD, arrowhead);
                dot.addEdge(edge);
            }
        } else {
//            if (controlledIt==null) {
//                System.out.println("!! controlled interaction is missing (interaction: " + name + ")");
//            }
//            if (controllerNames.size()==0) {
//                System.out.println("!! controller names are missing (interaction: " + name + ")");
//            }
        }

        return output.toString();
    }

    //-------------------------------------------------------------------------
    //    Conversion
    //-------------------------------------------------------------------------

    private void addEdges(Conversion input) {

        // left: 0..*, PhysicalEntity
        List<PhysicalEntity> leftPes = new ArrayList<PhysicalEntity> ();
        if (input.getPhysicalEntities()!=null && !input.getPhysicalEntities().isEmpty()) {
            Iterator it = input.getPhysicalEntities().iterator();
            while (it.hasNext()) {
                PhysicalEntity pe = (PhysicalEntity) it.next();
                if (!leftPes.contains(pe)) {
                    addNode(pe);
                    leftPes.add(pe);
                }
            }
        }

        // right: 0..*, PhysicalEntity
        List<PhysicalEntity> rightPes = new ArrayList<PhysicalEntity> ();
        if (input.getPhysicalEntities_1()!=null && !input.getPhysicalEntities_1().isEmpty()) {
            Iterator it = input.getPhysicalEntities_1().iterator();
            while (it.hasNext()) {
                PhysicalEntity pe = (PhysicalEntity) it.next();
                if (!rightPes.contains(pe)) {
                    addNode(pe);
                    rightPes.add(pe);
                }
            }
        }

        // left -> right
        String name = getFirstName(input.getInteraction().getBioEntity());
        if (leftPes.size()>0 && rightPes.size()>0) {
            Node node = new Node("entity_" + input.getInteraction().getBioEntity().getPkEntity());
            //node.setAttribute(Node.LABEL, insertStringEvery(name, splitSep, splitChunk));
            node.setAttribute(Node.LABEL, name);
            node.setAttribute(Node.SHAPE, Node.SHAPE_VALUES.box.name());
            dot.addNode(node);

            Edge edge = null;
            for(PhysicalEntity l: leftPes) {
                edge = new Edge("entity_" + l.getBioEntity().getPkEntity(), "entity_"
                + input.getInteraction().getBioEntity().getPkEntity(), true);
                dot.addEdge(edge);
            }
            for(PhysicalEntity r: rightPes) {
                edge = new Edge("entity_" + input.getInteraction().getBioEntity().getPkEntity(), "entity_"
                + r.getBioEntity().getPkEntity(), true);
                dot.addEdge(edge);
            }
        } else {
            if (leftPes.size()==0) {
                System.out.println("!! left physical entities are missing (interaction: " + name + ")");
            }
            if (rightPes.size()==0) {
                System.out.println("!! right physical entities are missing (interaction: " + name + ")");
            }
        }
    }

    //-------------------------------------------------------------------------
    //    Interaction
    //-------------------------------------------------------------------------

    private void add(Interaction input) {
        if (dbAdaptor.isControl(input)) {
            addEdges(dbAdaptor.getControl(input));
        } else if (dbAdaptor.isConversion(input)) {
            addEdges(dbAdaptor.getConversion(input));
//        } else if (dbAdaptor.isGeneticInteraction(input)) {
//            output.append(getDot(dbAdaptor.getGeneticInteraction(input)));
//        } else if (dbAdaptor.isMolecularInteraction(input)) {
//            output.append(getDot(dbAdaptor.getMolecularInteraction(input)));
//        } else if (dbAdaptor.isTemplateReaction(input)) {
//            output.append(getDot(dbAdaptor.getTemplateReaction(input)));
        }  else {
            System.out.println("Interacion not implemented yet !!");
        }
    }


    //=========================================================================
    //    Pathway
    //=========================================================================

    private void add(Pathway input) {
        Pathway pw = null;
        PathwayStep ps = null;
        Interaction inter = null;

        Iterator it = null;
        Set pathways = new HashSet();

        Set steps = input.getPathwaySteps_1();
        it = steps.iterator();
        while (it.hasNext()) {
            ps = ((PathwayStep) it.next());
            Iterator it1 = ps.getInteractions().iterator();
            while (it1.hasNext()) {
                inter = ((Interaction) it1.next());
                add(inter);
            }

            pathways.addAll(ps.getPathwaies());

            it1 = ps.getPathwaies().iterator();
            while (it1.hasNext()) {
                pw = ((Pathway) it1.next());
            }

            it1 = ps.getPathwayStepsForNextPathwaystep().iterator();
            while (it1.hasNext()) {
                ps = ((PathwayStep) it1.next());
            }
        }

        map.put(input.getPkPathway(), true);

        it = pathways.iterator();
        while (it.hasNext()) {
            pw = ((Pathway) it.next());
            if (!map.containsKey(pw.getPkPathway())) {
                add(pw);
            }
        }
    }

    //=========================================================================
    //    utils
    //=========================================================================

    private String insertStringEvery(String input, String sep, int splitChunk) {
        StringBuilder sb = new StringBuilder();

        String[] words = input.replace("\"", "'").split(" ");

        int count = 0;
        for(String w: words) {
            sb.append(w).append(" ");
            count += w.length();
            if (count>=splitChunk) {
                sb.append(sep);
                count = 0;
            }
        }

//        for(int i=0; i<input.length() ; i++) {
//            sb.append(input.charAt(i));
//            if (i>0 && i%splitChunk==0) {
//                sb.append(sep);
//            }
//        }

//        String aux = input;
//        while (aux.length()>splitChunk) {
//            sb.append(aux.substring(0, splitChunk)).append("\n");
//            aux.s
//        }
//        sb.append(aux);

        return sb.toString();
    }

    private String getFirstName(BioEntity entity) {
        String name = "NO-NAME";
        try {
            String aux = "";
            Iterator it = entity.getNameEntities().iterator();
            NameEntity ne = null;
            while (it.hasNext()) {
                ne = (NameEntity) it.next();
                if (name.equalsIgnoreCase("NO-NAME") || ne.getNameEntity().length()<name.length()) {
                    name = ne.getNameEntity();
                }
            }
            name = name.replace("\"", "'");
        } catch (Exception e) {
            name = "NO-NAME";
        }
        return name;
    }
*/
}
