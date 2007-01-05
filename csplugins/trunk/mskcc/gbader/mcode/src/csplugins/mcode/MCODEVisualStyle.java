package csplugins.mcode;

import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.ShapeNodeRealizer;
import cytoscape.visual.VisualStyle;
import cytoscape.visual.calculators.GenericNodeColorCalculator;
import cytoscape.visual.calculators.GenericNodeShapeCalculator;
import cytoscape.visual.calculators.NodeColorCalculator;
import cytoscape.visual.calculators.NodeShapeCalculator;
import cytoscape.visual.mappings.*;

import java.awt.*;

/**
 * * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Gary Bader
 * * Authors: Gary Bader, Ethan Cerami, Chris Sander
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * Memorial Sloan-Kettering Cancer Center
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * Memorial Sloan-Kettering Cancer Center
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * Memorial Sloan-Kettering Cancer Center
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 * *
 * * User: Vuk Pavlovic
 * * Date: Dec 27, 2006
 * * Time: 1:20:19 PM
 * * Description: A visual style for MCODE modifying node shape and color
 */

public class MCODEVisualStyle extends VisualStyle {
    private double maxValue = 0.0;

    public MCODEVisualStyle (String name) {
        super(name);
        initCalculators();
    }

    public void initCalculators() {
        NodeAppearanceCalculator nac = new NodeAppearanceCalculator();

        createNodeShape(nac);
        createNodeColor(nac);

        this.setNodeAppearanceCalculator(nac);
    }

    private void createNodeShape(NodeAppearanceCalculator nac) {
        DiscreteMapping discreteMapping = new DiscreteMapping(new Byte(ShapeNodeRealizer.RECT), "MCODE_Node_Status", ObjectMapping.NODE_MAPPING);

        discreteMapping.putMapValue("Clustered",
                new Byte(ShapeNodeRealizer.ELLIPSE));
        discreteMapping.putMapValue("Seed",
                new Byte(ShapeNodeRealizer.RECT));
        discreteMapping.putMapValue("Unclustered",
                new Byte(ShapeNodeRealizer.DIAMOND));

        NodeShapeCalculator nodeShapeCalculator = new GenericNodeShapeCalculator("Seed and Cluster Status Calculator", discreteMapping);
        nac.setNodeShapeCalculator(nodeShapeCalculator);
    }

    private void createNodeColor(NodeAppearanceCalculator nac) {
        nac.setDefaultNodeFillColor(Color.WHITE);
        ContinuousMapping continuousMapping = new ContinuousMapping(Color.WHITE, ObjectMapping.NODE_MAPPING);
        continuousMapping.setControllingAttributeName("MCODE_Score", null, false);

        Interpolator fInt = new LinearNumberToColorInterpolator();
        continuousMapping.setInterpolator(fInt);

        Color minColor = Color.BLACK;
        Color maxColor = Color.RED;

        //Create Three Boundary Conditions
        BoundaryRangeValues bv0 = new BoundaryRangeValues(Color.WHITE, Color.WHITE, minColor);
        BoundaryRangeValues bv2 = new BoundaryRangeValues(maxColor, maxColor, maxColor);

        //Set Data Points
        double minValue = 0.0;
        continuousMapping.addPoint(minValue, bv0);
        continuousMapping.addPoint(maxValue, bv2);

        NodeColorCalculator nodeColorCalculator = new GenericNodeColorCalculator("MCODE Score Color Calculator", continuousMapping);
        nac.setNodeFillColorCalculator(nodeColorCalculator);
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }
}
