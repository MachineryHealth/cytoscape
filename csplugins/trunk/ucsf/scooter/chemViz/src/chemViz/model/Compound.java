/*
  Copyright (c) 2006, 2007, 2008 The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package chemViz.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import cytoscape.CyNode;
import cytoscape.data.CyAttributes;
import cytoscape.logger.CyLogger;
import cytoscape.util.URLUtil;

import org.openscience.cdk.ChemModel;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.modeling.builder3d.ModelBuilder3D;
import org.openscience.cdk.modeling.builder3d.TemplateHandler3D;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.RingGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.qsar.*;
import org.openscience.cdk.qsar.result.*;
import org.openscience.cdk.qsar.DescriptorEngine;
import org.openscience.cdk.qsar.descriptors.molecular.*;
// import org.openscience.cdk.tools.MFAnalyser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import net.sf.jniinchi.INCHI_RET;

import giny.model.GraphObject;

/**
 * The Compound class provides the main interface to molecule compounds.  A given node or edge in Cytoscape
 * could have multiple Compounds, either by having multiple attributes that contain compound descriptors or
 * by having a single attribute that contains multiple descriptors (e.g. comma-separated SMILES strings).  The
 * creation of a Compound results in the building of a cached 2D image for that compound, as well as the creation
 * of the CDK IMolecule, which is used for conversion from InChI to SMILES, for calculation of the molecular weight,
 * and for the calculation of Tanimoto coefficients.
 */

public class Compound {
	public enum AttriType { smiles, inchi };
	public enum DescriptorType {
		IMAGE ("2D Image", Compound.class),
		ATTRIBUTE ("Attribute", String.class),
		IDENTIFIER ("Molecular String", String.class),
		WEIGHT ("Molecular Wt.", Double.class),
		ALOGP ("ALogP", Double.class),
		ALOGP2 ("ALogP2", Double.class),
		AMR ("Molar refractivity", Double.class),
		HBONDACCEPTOR ("HBond Acceptors", Integer.class),
		HBONDDONOR ("HBond Donors", Integer.class),
		LOBMAX ("Length over Breadth Max", Double.class),
		LOBMIN ("Length over Breadth Min", Double.class),
		RBONDS ("Rotatable Bonds Count", Integer.class),
		RULEOFFIVE ("Rule of Five Failures", Double.class),
		TPSA ("Topological Polar Surface Area", Double.class),
		WEINERPATH ("Wiener Path", Double.class),
		WEINERPOL ("Wiener Polarity", Double.class),
		MASS ("Exact Mass", Double.class);

		private String name;
		private Class classType;
		private int columnCount;

		DescriptorType(String name, Class classType) {
			this.name = name;
			this.classType = classType;
		}

		public String toString() { return this.name; }
		public Class getClassType() { return this.classType; }
	}

	// Class variables
	static private HashMap<GraphObject, List<Compound>> compoundMap;
	static private CyLogger logger = CyLogger.getLogger(Compound.class);
	static private DescriptorType[] descriptorTypes = {
		DescriptorType.IMAGE, DescriptorType.ATTRIBUTE, DescriptorType.IDENTIFIER, DescriptorType.WEIGHT,
		DescriptorType.MASS,
		DescriptorType.ALOGP,DescriptorType.ALOGP2,DescriptorType.AMR,
		DescriptorType.HBONDACCEPTOR,DescriptorType.HBONDDONOR,
		// Exclude the LengthOverBreadth Descriptors until CDK gets better ring templates
		// DescriptorType.LOBMAX,DescriptorType.LOBMIN,
		DescriptorType.RBONDS,DescriptorType.RULEOFFIVE,
		DescriptorType.TPSA,
		DescriptorType.WEINERPATH,DescriptorType.WEINERPOL
	};

	/********************************************************************************************************************* 
	 *                                                Class (static) methods                                             *
	 ********************************************************************************************************************/ 

	/**
 	 * Returns all of the Compounds for a list of graph objects (Nodes or Edges) based on the SMILES
 	 * and InChI attributes.
 	 *
 	 * @param goSet the Collection of graph objects we're looking at
 	 * @param attributes the appropriate set of attributes (nodeAttributes or edgeAttributes)
 	 * @param sList the list of attributes that contain SMILES strings
 	 * @param iList the list of attributes that contain InChI strings
 	 * @return the list of compounds.  If the compounds have not already been created, they are created
 	 *         as a byproduct of this method.
 	 */
	static public List<Compound> getCompounds(Collection<GraphObject> goSet, CyAttributes attributes, 
	                                          List<String> sList, List<String> iList) {
		List<Compound> cList = new ArrayList();
		for (GraphObject go: goSet)
			cList.addAll(getCompounds(go, attributes, sList, iList, false));

		return cList;
	}

	/**
 	 * Returns all of the Compounds for a single graph object (Node or Edge) based on the SMILES
 	 * and InChI attributes.
 	 *
 	 * @param go the graph object we're looking at
 	 * @param attributes the appropriate set of attributes (nodeAttributes or edgeAttributes)
 	 * @param sList the list of attributes that contain SMILES strings
 	 * @param iList the list of attributes that contain InChI strings
 	 * @param noStructures if 'true', the structures are fetched in the background
 	 * @return the list of compounds.  If the compounds have not already been created, they are created
 	 *         as a byproduct of this method.
 	 */
	static public List<Compound> getCompounds(GraphObject go, CyAttributes attributes, 
	                                          List<String> sList, List<String> iList, 
	                                          boolean noStructures) {
		if ((sList == null || sList.size() == 0) 
		    && (iList == null || iList.size() == 0))
			return null;
		
		List<Compound> cList = new ArrayList();

		// Get the compound list from each attribute
		for (String attr: sList) {
			cList.addAll(getCompounds(go, attributes, attr, AttriType.smiles, noStructures));
		}

		for (String attr: iList) {
			cList.addAll(getCompounds(go, attributes, attr, AttriType.inchi, noStructures));
		}

		return cList;
	}

	/**
 	 * Returns the compound that matches the passed arguments or null if no such compound exists.
 	 *
 	 * @param go the graph object we're looking at
 	 * @param attr the attribute that contains the compound descriptor
 	 * @param molString the compound descriptor
 	 * @param type the type of the attribute (smiles or inchi)
 	 * @return the compound that matched or 'null' if no such compound exists.
 	 */
	static public Compound getCompound(GraphObject go, String attr, String molString, AttriType type) {
		if (compoundMap == null) return null;

		if (!compoundMap.containsKey(go))
			return null;

		List<Compound>compoundList = compoundMap.get(go);
		for (Compound c: compoundList) {
			if (c.getAttribute().equals(attr) && c.getMoleculeString().equals(molString))
				return c;
		}
		return null;
	}

	/**
 	 * Returns the list of attributes that might contain compound descriptors and are
 	 * used by any of the passed graph objects.
 	 *
 	 * @param goList the list of graph objects we're looking at
 	 * @param attributes the appropriate set of attributes (nodeAttributes or edgeAttributes)
 	 * @param attrList the entire list of compound attributes
 	 * @return the list of attributes that are in the attrList and used by objects in the goList
 	 */
	static public List<String> findAttributes(Collection<GraphObject> goList, CyAttributes attributes, 
	                                          List<String> attrList) {

		// Now get the names of all of the object attributes
		String[] attrNames = attributes.getAttributeNames();

		// Now see if any of the attributes are in our list
		ArrayList<String>attrsFound = new ArrayList();
		for (int i = 0; i < attrNames.length; i++) {
			if (attrList.contains(attrNames[i])) {
				attrsFound.add(attrNames[i]);
			}
		}

		if (attrsFound.size() == 0)
			return null;

		if (goList == null)
			return attrsFound;

		// We we know all of the attributes we're interested in -- see if these objects have any of them
		ArrayList<String>hasAttrs = new ArrayList();
		for (GraphObject go: goList) {
			for (String attribute: attrsFound) {
				if (attributes.hasAttribute(go.getIdentifier(),attribute)) {
					hasAttrs.add(attribute);
				}
			}
		}

		if (hasAttrs.size() > 0)
			return hasAttrs;

		return null;
	}

	static public List<DescriptorType> getDescriptorList() {
		return Arrays.asList(descriptorTypes);
	}

	/**
 	 * Returns all of the Compounds for a single graph object (Node or Edge) based on the designated
 	 * attribute of the specific type
 	 *
 	 * @param go the graph object we're looking at
 	 * @param attributes the appropriate set of attributes (nodeAttributes or edgeAttributes)
 	 * @param attr the attribute that contains the compound descriptor
 	 * @param type the type of the attribute (smiles or inchi)
 	 * @param noStructures if 'true', the structures are fetched in the background
 	 * @return the list of compounds.  If the compounds have not already been created, they are created
 	 *         as a byproduct of this method.
 	 */
	static private List<Compound> getCompounds(GraphObject go, CyAttributes attributes, 
	                                           String attr, AttriType type,
	                                           boolean noStructures) {
		byte atype = attributes.getType(attr);
		List<Compound> cList = new ArrayList();
			
		if (!attributes.hasAttribute(go.getIdentifier(), attr)) 
			return cList;
		if (atype == CyAttributes.TYPE_STRING) {
			String cstring = attributes.getStringAttribute(go.getIdentifier(), attr);
			cList.addAll(getCompounds(go, attr, cstring, type, noStructures));
		} else if (atype == CyAttributes.TYPE_SIMPLE_LIST) {
			List<String> stringList = attributes.getListAttribute(go.getIdentifier(), attr);
			for (String cstring: stringList)
				cList.addAll(getCompounds(go, attr, cstring, type, noStructures));
		}
		return cList;
	}

	/**
 	 * Returns all of the Compounds for a single graph object (Node or Edge) based on the designated
 	 * attribute of the specific type
 	 *
 	 * @param go the graph object we're looking at
 	 * @param attr the attribute that contains the compound descriptor
 	 * @param compundString the compound descriptor
 	 * @param type the type of the attribute (smiles or inchi)
 	 * @param noStructures if 'true', the structures are fetched in the background
 	 * @return the list of compounds.  If the compounds have not already been created, they are created
 	 *         as a byproduct of this method.
 	 */
	static private List<Compound> getCompounds(GraphObject go, String attr, 
	                                           String compoundString, AttriType type,
	                                           boolean noStructures) {
		List<Compound> cList = new ArrayList();

		String[] cstrings = null;

		if (type == AttriType.smiles) {
			cstrings = compoundString.split(",");
		} else {
			cstrings = new String[1];
			cstrings[0] = compoundString;
		}

		for (int i = 0; i < cstrings.length; i++) {
			Compound c = getCompound(go, attr, cstrings[i], type);
			if (c == null)
				c = new Compound(go, attr, cstrings[i], type, noStructures);

			cList.add(c);
		}
		return cList;
	}

	/********************************************************************************************************************* 
	 *                                                Instance methods                                                   *
	 ********************************************************************************************************************/ 

	// Instance variables
	private GraphObject source;
	private String smilesStr;
	private String moleculeString;
	private String attribute;
	protected Image renderedImage;
	protected boolean laidOut;
	private AttriType attrType;
	private IMolecule iMolecule;
	private IMolecule iMolecule3D;
	private BitSet fingerPrint;
	private int lastImageWidth = -1;
	private int lastImageHeight = -1;
	private	boolean	lastImageFailed = false;
	private DescriptorEngine descriptorEngine = null;

	/**
 	 * The constructor is called from the various static getCompound methods to create a compound and store it in
 	 * the compound map.
 	 *
 	 * @param source the graph object that holds this compound
 	 * @param attribute the attribute that has the compound string
 	 * @param mstring the compound descriptor itself
 	 * @param attrType the type of the compound descriptor (inchi or smiles)
 	 * @param noStructures if 'true' get the structures on a separate thread
 	 */
	public Compound(GraphObject source, String attribute, String mstring, 
	                AttriType attrType, boolean noStructures) {
		this.source = source;
		this.attribute = attribute;
		this.moleculeString = mstring.trim();
		this.attrType = attrType;
		this.renderedImage = null;
		this.laidOut = false;
		this.iMolecule = null;
		this.iMolecule3D = null;
		this.fingerPrint = null;
		this.smilesStr = null;

		List<Compound> mapList = null;
		if (Compound.compoundMap == null) 
			Compound.compoundMap = new HashMap();

		if (Compound.compoundMap.containsKey(source)) {
			mapList = Compound.compoundMap.get(source);
		} else {
			mapList = new ArrayList();
		}
		mapList.add(this);
		Compound.compoundMap.put(source, mapList);

		if (attrType == AttriType.inchi) {
			// Convert to smiles 
			this.smilesStr = convertInchiToSmiles(moleculeString);
		} else {
			if (mstring != null && mstring.length() > 0) {
				// Strip any blanks in the string
				this.smilesStr = mstring.replaceAll(" ", "");
			}
		}

		if (smilesStr == null)
			return;

		logger.debug("smiles string = "+smilesStr);

		// Create the CDK Molecule object
		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder
						.getInstance());
		try {
			iMolecule = sp.parseSmiles(this.smilesStr);
		} catch (InvalidSmilesException e) {
			iMolecule = null;
			logger.warning("Unable to parse SMILES: "+smilesStr+" for "+source.getIdentifier()+": "+e.getMessage());
			return;
		}

		// At this point, we should have an IMolecule
		try { 
			CDKHueckelAromaticityDetector.detectAromaticity(iMolecule);

			// Make sure we update our implicit hydrogens
			CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(iMolecule.getBuilder());
			adder.addImplicitHydrogens(iMolecule);

			// Get our fingerprint
			Fingerprinter fp = new Fingerprinter();
			// Do we need to do the addh here?
			fingerPrint = fp.getFingerprint(addh(iMolecule));
		} catch (CDKException e1) {
			fingerPrint = null;
		}

	}

	/**
 	 * Return the original molecular string
 	 *
 	 * @return the SMILES or InChI string
 	 */
	public String getMoleculeString() {
		return moleculeString;
	}

	/**
 	 * Return the graph object (Node or Edge) that holds this compound
 	 *
 	 * @return the Node or Edge that holds this compound
 	 */
	public GraphObject getSource() {
		return source;
	}

	/**
 	 * Return the attribute that holds this compound
 	 *
 	 * @return the name of the attribute that holds this compound descriptor
 	 */
	public String getAttribute() {
		return attribute;
	}

	/**
 	 * Return the smiles string for this compound
 	 *
 	 * @return the smiles string
 	 */
	public String getSmiles() {
		return smilesStr;
	}

	/**
 	 * Return the CDK IMolecule for this compound
 	 *
 	 * @return the IMolecule for this compound
 	 */
	public IMolecule getIMolecule() {
		return iMolecule;
	}

	/**
 	 * Return the CDK fingerprint for this compound
 	 *
 	 * @return the fingerprint for this compound
 	 */
	public BitSet getFingerprint() {
		return fingerPrint;
	}
	
	/**
 	 * Return true if this compound has a moleculeString
 	 *
 	 * @return 'true' if this compound has a moleculeString
 	 */
	public boolean hasMolecule() {
		return null != moleculeString && !"".equals(moleculeString);
	}

	/**
 	 * Method to compare two compounds lexigraphically.  At this point
 	 * this is done by comparing the SMILES strings.
 	 *
 	 * @param o the compound we're being compared to
 	 * @return 0 if the structures are equal, -1 if o is less than us, 1 if it is greater
 	 */
	public int compareTo(Compound o) {
		return this.getSmiles().compareTo(o.getSmiles());
	}

	/**
 	 * Handle requests for various compound descriptors.  This is used primarily by the CompoundTable, but
 	 * could be used by other clients as well.
 	 */
	public Object getDescriptor(DescriptorType type) {
		switch(type) {
			case IMAGE:
				// We actually return ourselves in this case since
				// the table needs access to the specific getImage calls
				return this;
			case ATTRIBUTE:
				return getAttribute();
			case IDENTIFIER:
				return getMoleculeString();
			case MASS:
				return getExactMass();
			case WEIGHT:
				return getMolecularWeight();
			case WEINERPATH:
			case WEINERPOL:
				{
					if (iMolecule == null) return null;

					IMolecularDescriptor descriptor = new WienerNumbersDescriptor();
					DoubleArrayResult retval = (DoubleArrayResult)(descriptor.calculate(addh(iMolecule)).getValue());
					if (type == DescriptorType.WEINERPATH)
						return retval.get(0);
					else
						return retval.get(1);
				}
			case RULEOFFIVE:
				{
					if (iMolecule == null) return null;
					IMolecularDescriptor descriptor = new RuleOfFiveDescriptor();
					IntegerResult retval = (IntegerResult)(descriptor.calculate(addh(iMolecule)).getValue());
					return retval.intValue();
				}
			case ALOGP:
			case ALOGP2:
			case AMR:
				{
					if (iMolecule == null) return null;
					try {
						IMolecularDescriptor descriptor = new ALOGPDescriptor();
						DoubleArrayResult retval = (DoubleArrayResult)(descriptor.calculate(addh(iMolecule)).getValue());
						if (type == DescriptorType.ALOGP)
							return retval.get(0);
						else if (type == DescriptorType.ALOGP2)
							return retval.get(1);
						else
							return retval.get(2);
					} catch (Exception e) {
						logger.warning("Unable to calculate ALogP values: "+e.getMessage());
						return null;
					}
				}
			case HBONDACCEPTOR:
				{
					if (iMolecule == null) return null;
					IMolecularDescriptor descriptor = new HBondAcceptorCountDescriptor();
					IntegerResult retval = (IntegerResult)(descriptor.calculate(iMolecule).getValue());
					return retval.intValue();
				}
			case HBONDDONOR:
				{
					if (iMolecule == null) return null;
					IMolecularDescriptor descriptor = new HBondDonorCountDescriptor();
					IntegerResult retval = (IntegerResult)(descriptor.calculate(iMolecule).getValue());
					return retval.intValue();
				}
			case LOBMAX:
			case LOBMIN:
				{
					if (iMolecule == null) return null;
					// We need 3D coordinates for these descriptors
					if (iMolecule3D == null) {
						try {
							ModelBuilder3D mb3d = ModelBuilder3D.getInstance(TemplateHandler3D.getInstance(), "mm2");
							iMolecule3D = mb3d.generate3DCoordinates(addh(iMolecule), true);
						} catch (Exception e) {
							logger.warning("Unable to calculate 3D coordinates: "+e.getMessage());
							iMolecule3D = null;
							return null;
						}
					}

					IMolecularDescriptor descriptor = new LengthOverBreadthDescriptor();
					DescriptorValue val = descriptor.calculate(iMolecule3D);
					if (val.getException() != null) {
						logger.warning("Unable to calculate LengthOverBreadthDescriptor: "+val.getException().getMessage());
						return null;
					}
					DoubleArrayResult retval = (DoubleArrayResult)(val.getValue());
					if (type == DescriptorType.LOBMAX) {
						return retval.get(0);
					} else {
						return retval.get(1);
					}
				}
			case RBONDS:
				{
					if (iMolecule == null) return null;
					IMolecularDescriptor descriptor = new RotatableBondsCountDescriptor();
					IntegerResult retval = (IntegerResult)(descriptor.calculate(iMolecule).getValue());
					return retval.intValue();
				}
			case TPSA:
				{
					if (iMolecule == null) return null;
					IMolecularDescriptor descriptor = new TPSADescriptor();
					DoubleResult retval = (DoubleResult)(descriptor.calculate(iMolecule).getValue());
					return retval.doubleValue();
				}
		}
		return null;
	}

	/**
 	 * Return the 2D image for this compound.  Note that this might sleep if we're in the process
 	 * of fetching the image already.
 	 *
 	 * @param width the width of the rendered image
 	 * @param height the height of the rendered image
 	 * @return the fetched image
 	 */
	public Image getImage(int width, int height) {
		return getImage(width, height, new Color(255,255,255,0));
	}

	/**
 	 * Return the 2D image for this compound.  Note that this might sleep if we're in the process
 	 * of fetching the image already.
 	 *
 	 * @param width the width of the rendered image
 	 * @param height the height of the rendered image
 	 * @param background the background color to use for the image
 	 * @return the fetched image
 	 */
	public Image getImage(int width, int height, Color background) {
		if (lastImageWidth != width || lastImageHeight != height || (renderedImage == null && lastImageFailed == false)) {
			renderedImage = depictWithCDK(width, height, background);
			lastImageWidth = width;
			lastImageHeight = height;
		}
		return renderedImage;
	}

	/**
 	 * Return the molecular weight of this compound.
 	 *
 	 * @return the molecular weight
 	 */
	public double getMolecularWeight() {
		if (iMolecule == null) return 0.0f;

		IMolecularFormula mfa = MolecularFormulaManipulator.getMolecularFormula(addh(iMolecule));
		// System.out.println("Formula: "+MolecularFormulaManipulator.getString(mfa));

		// This method returns the total atomic number, *not* the total mass number.  Do this
		// by ourselves until this is fixed.
		// return MolecularFormulaManipulator.getTotalMassNumber(mfa);
		
		double mass = 0.0;
		for (IIsotope isotope : mfa.isotopes()) {
			try {
				IIsotope isotope2 = IsotopeFactory.getInstance(mfa.getBuilder()).getMajorIsotope(isotope.getSymbol());
				// System.out.println("Isotope: "+isotope.getSymbol()+" has atomic number "+isotope2.getAtomicNumber());
				// System.out.println("Isotope: "+isotope.getSymbol()+" has mass number "+isotope2.getMassNumber());
				mass += isotope2.getMassNumber() * mfa.getIsotopeCount(isotope);
			} catch (IOException e) {
				return 0.0f;
			}
		}
		return mass;
	}

	/**
 	 * Return the exact mass of this compound.
 	 *
 	 * @return the exact mass
 	 */
	public double getExactMass() {
		if (iMolecule == null) return 0.0f;

		IMolecularFormula mfa = MolecularFormulaManipulator.getMolecularFormula(addh(iMolecule));
		return MolecularFormulaManipulator.getTotalExactMass(mfa);
	}

	public Image depictWithCDK(int width, int height, Color background) {
		BufferedImage bufferedImage = null;

		// System.out.println("depictWithCDK("+width+","+height+")");

		if (iMolecule == null || width == 0 || height == 0) {
			return blankImage(iMolecule, width, height);
		}

		try {
			if (!laidOut) {
				// Is the structure connected?
				if (!ConnectivityChecker.isConnected(iMolecule)) {
					// No, for now, find the largest component and use that exclusively
					IMoleculeSet molSet = ConnectivityChecker.partitionIntoMolecules(iMolecule);
					IMolecule largest = molSet.getMolecule(0);
					for (int i = 0; i < molSet.getMoleculeCount(); i++) {
						if (molSet.getMolecule(i).getAtomCount() > largest.getAtomCount())
							largest = molSet.getMolecule(i);
					}
					iMolecule = largest;
				}
				StructureDiagramGenerator sdg = new StructureDiagramGenerator();
				sdg.setUseTemplates(false);
				sdg.setMolecule(iMolecule);
				sdg.generateCoordinates();
				this.iMolecule = sdg.getMolecule();
				laidOut = true;
			}

			// generators make the image elements
			List<IGenerator<IAtomContainer>> generators = new ArrayList<IGenerator<IAtomContainer>>();
			generators.add(new BasicSceneGenerator());
			generators.add(new BasicBondGenerator());
			generators.add(new RingGenerator());
			generators.add(new BasicAtomGenerator());
       
			// the renderer needs to have a toolkit-specific font manager 
			AtomContainerRenderer renderer = new AtomContainerRenderer(generators, new AWTFontManager());
			RendererModel model = renderer.getRenderer2DModel();

			if (background == null)
				background = new Color(255,255,255,255);

			// Set up our rendering parameters
			model.set(BasicSceneGenerator.UseAntiAliasing.class, true);
			model.set(BasicSceneGenerator.BackgroundColor.class, background);
			model.set(BasicBondGenerator.BondWidth.class, 2.0);
			model.set(RingGenerator.BondWidth.class, 2.0);
			model.set(BasicAtomGenerator.ColorByType.class, true);
			model.set(BasicAtomGenerator.ShowExplicitHydrogens.class, true);
			
			int renderWidth = width;
			if (renderWidth < 200) renderWidth = 200;
			int renderHeight = height;
			if (renderHeight < 200) renderHeight = 200;
			Rectangle2D bbox = new Rectangle2D.Double(0,0,renderWidth,renderHeight);
			renderer.setup(iMolecule, new Rectangle(renderWidth, renderHeight));

			bufferedImage = new BufferedImage(renderWidth, renderHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = bufferedImage.createGraphics();
			graphics.setColor(background);
			graphics.setBackground(background);
			graphics.fillRect(0,0,renderWidth,renderHeight);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			renderer.paint(iMolecule, new AWTDrawVisitor(graphics), bbox, true);

			if (renderWidth != width || renderHeight != height) {
				AffineTransform tx = new AffineTransform();
				if (width < height) {
					tx.scale((double)width/(double)renderWidth, (double)width/(double)renderWidth);
					// return bufferedImage.getScaledInstance(width, width, java.awt.Image.SCALE_SMOOTH);
				} else {
					tx.scale((double)height/(double)renderHeight, (double)height/(double)renderHeight);
					// return bufferedImage.getScaledInstance(height, height, java.awt.Image.SCALE_SMOOTH);
				}

				AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
				bufferedImage = op.filter(bufferedImage, null);
			}
		} catch (Exception e) {
			logger.warning("Unable to depict molecule for "+source.getIdentifier()+" with CDK depiction: "+e.getMessage(), e);
		}

		return bufferedImage;
	}

	private IMolecule addh(IMolecule mol) {
		IMolecule molH;
		try {
			molH = (IMolecule)mol.clone();
		} catch (Exception e) {
			return mol;
		}

		if (molH == null) return mol;

		// Make sure we handle hydrogens
		AtomContainerManipulator.convertImplicitToExplicitHydrogens(molH);
		return molH;
	}

	/**
	 * Convert from an InChI string to a SMILES string
	 * 
	 * @param inchi InChI string
	 * @return SMILES string
	 */
	private String convertInchiToSmiles(String inchi) {

		try {
			// Get the factory	
			InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
			if (!inchi.startsWith("InChI="))
				inchi = "InChI="+inchi;

			logger.debug("Getting structure for: "+inchi);

			InChIToStructure intostruct = factory.getInChIToStructure(inchi, DefaultChemObjectBuilder.getInstance());

			// Get the structure
			INCHI_RET ret = intostruct.getReturnStatus();
			if (ret == INCHI_RET.WARNING) {
				logger.warning("InChI warning: " + intostruct.getMessage());
			} else if (ret != INCHI_RET.OKAY) {
				logger.warning("Structure generation failed: " + ret.toString()
     	               + " [" + intostruct.getMessage() + "]");
				return null;
			}

			iMolecule = new Molecule(intostruct.getAtomContainer());
			// Use the molecule to create a SMILES string
			SmilesGenerator sg = new SmilesGenerator();
			return sg.createSMILES(iMolecule);
		} catch (Exception e) {
			logger.warning("Structure generation failed: " + e.getMessage(), e);
			return null;
		}

	}

	private Image blankImage(IMolecule mol, int width, int height) {
		final String noImage = "Image Unavailable";

		if (width == 0 || height == 0)
			return null;

		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = bufferedImage.createGraphics();
		graphics.setBackground(Color.WHITE);

		graphics.setColor(Color.WHITE);
		graphics.fillRect(0,0,width,height);
		graphics.setColor(Color.BLACK);

		// Create our font
		Font font = new Font("SansSerif", Font.PLAIN, 18);
		graphics.setFont(font);
		FontMetrics metrics = graphics.getFontMetrics();

		int length = metrics.stringWidth(noImage);
		while (length+6 >= width) {
			font = font.deriveFont((float)(font.getSize2D() * 0.9)); // Scale our font
			graphics.setFont(font);
			metrics = graphics.getFontMetrics();
			length = metrics.stringWidth(noImage);
		}

		int lineHeight = metrics.getHeight();

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.drawString(noImage, (width-length)/2, (height+lineHeight)/2);

		return bufferedImage;
	}

}
