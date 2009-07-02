/* vim: set ts=2: */
/**
 * Copyright (c) 2008 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package csplugins.layout;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.Tunable;

enum WeightTypes {
	GUESS("Heuristic"),
	LOG("-Log(value)"),
	DISTANCE("1 - normalized value"),
	WEIGHT("normalized value");

	private String name;
	private WeightTypes(String str) { name=str; }
	public String toString() { return name; }
}

/**
 * The EdgeWeighter class.  This class is used as a container for information
 * about how to interpret weights in an weighted layout.
 */
public class EdgeWeighter {
	@Tunable(description="How to interpret weight values", group={"Edge Weight Settings"})
	public WeightTypes type = WeightTypes.GUESS;
	@Tunable(description="The minimum edge weight to consider", group={"Edge Weight Settings"})
	public double minWeightCutoff = 0;
	@Tunable(description="The maximum edge weight to consider", group={"Edge Weight Settings"})
	public double maxWeightCutoff = Double.MAX_VALUE;
	final static double EPSILON = .001;

	// Default normalization bounds
	final static double LOWER_BOUND = .1f;
	final static double UPPER_BOUND = .9f;

	double lowerBounds = LOWER_BOUND;
	double upperBounds = UPPER_BOUND;

	double maxWeight = -1000000;
	double minWeight = 1000000;
	double maxLogWeight = -1000000;
	double minLogWeight = 1000000;

	double logWeightCeiling = 1028;
	boolean logOverflow = false;

	// These are just here for efficiency reasons
	double normalFactor = Double.MAX_VALUE;

	@Tunable(description="The edge attribute that contains the weights", group={"Edge Weight Settings"})
	public String weightAttribute = null;

	static WeightTypes[] weightChoices = {WeightTypes.GUESS,
	                                      WeightTypes.LOG,
	                                      WeightTypes.DISTANCE,
	                                      WeightTypes.WEIGHT};

	public void reset() {
		maxWeight = -1000000;
		minWeight = 1000000;
		maxLogWeight = -1000000;
		minLogWeight = 1000000;
		normalFactor = Double.MAX_VALUE;
		logOverflow = false;
	}

	public void setWeightType(WeightTypes type) {
		this.type = type;
	}

	public void setNormalizedBounds(double lowerBound, double upperBound) {
		this.lowerBounds = lowerBound;
		this.upperBounds = upperBound;
	}

	public void setWeight(LayoutEdge layoutEdge) {
		CyEdge edge = layoutEdge.getEdge();
		CyRow row = edge.attrs();
		double eValue = 0.5; // FIXME: shouldn't we expose the default edge weight somehow?

		// System.out.println("Setting weight for "+layoutEdge+" using "+weightAttribute);

		if (weightAttribute != null) {
		    if (row.contains(weightAttribute, Integer.class)) {
				Integer val = row.get(weightAttribute, Integer.class);
				eValue = (double) val.intValue();
			} else if (row.contains(weightAttribute, Double.class)) {
				Double val = row.get(weightAttribute, Double.class);
				eValue = val.doubleValue();
			}
		}
		layoutEdge.setWeight(eValue);
		minWeight = Math.min(minWeight,eValue);
		maxWeight = Math.max(maxWeight,eValue);
		if (type == WeightTypes.GUESS || type == WeightTypes.LOG) {
			double logWeight;
			if (eValue == 0) {
				logWeight = logWeightCeiling;
				logOverflow = true;
			} else {
				logWeight = Math.min(-Math.log10(eValue), logWeightCeiling);
			}
			minLogWeight = Math.min(minLogWeight,logWeight);
			maxLogWeight = Math.max(maxLogWeight,logWeight);
			layoutEdge.setLogWeight(logWeight);
		}
	}

	public boolean normalizeWeight(LayoutEdge edge) {
		if (minWeight == maxWeight)
			return true;

		// We need to handle the special case of a weight of 0.0 when
		// we're doing logs.  When we set the value, we set it to
		// logWeightCeiling as a placeholder, but we really want to
		// just set it to a value somewhat larger than the maximum
		if (logOverflow) {
			maxLogWeight = maxLogWeight+5;
			logOverflow = false;
		}

		if ((edge.getWeight() <= minWeightCutoff) ||	
		    (edge.getWeight() > maxWeightCutoff))
			return false;

		double weight = 0;

		switch (this.type) {
		case GUESS:
			// System.out.print("Heuristic: ");
			if (Math.abs(maxLogWeight-minLogWeight) > 3) {
				weight = edge.getLogWeight();
				// System.out.print("Log weight = "+weight);
				if (weight == logWeightCeiling)
					weight = maxLogWeight;
				weight = logNormalize(weight);
				// System.out.println(" normalized weight = "+weight);
			} else {
				weight = normalize(edge.getWeight());
			}
			break;
	  case LOG:
			// System.out.print("Log: ");
			weight = edge.getLogWeight();
			// System.out.print("Log weight = "+weight);
			if (weight == logWeightCeiling)
				weight = maxLogWeight;
			weight = logNormalize(weight);
			// System.out.println(" normalized weight = "+weight);
			break;
	  case DISTANCE:
			// System.out.println("Distance");
			weight = edge.getWeight();
			weight = (lowerBounds + upperBounds) - normalize(weight);
			break;
	  case WEIGHT:
			// System.out.println("Weight");
			weight = normalize(edge.getWeight());
			break;
		}

		edge.setWeight(weight);

		// We're now normalized to the range 0-1, so we can safely
		// ignore really small weights since they should be very far away
		//if (weight < EPSILON)
		//	return false;

		return true;
	}

	private double logNormalize(double weight) {
		if (normalFactor == Double.MAX_VALUE) {
			normalFactor = (upperBounds-lowerBounds)/(maxLogWeight-minLogWeight);
		}
		return (weight-minLogWeight)*normalFactor+lowerBounds;
	}

	private double normalize(double weight) {
		if (normalFactor == Double.MAX_VALUE) {
			normalFactor = (upperBounds-lowerBounds)/(maxWeight-minWeight);
		}
		return (weight-minWeight)*normalFactor+lowerBounds;
	}

	public void setMaxWeightCutoff(double maxWeight) {
		maxWeightCutoff = maxWeight;
	}

	public void setMinWeightCutoff(double minWeight) {
		minWeightCutoff = minWeight;
	}
}
