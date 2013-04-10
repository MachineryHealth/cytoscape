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
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import cytoscape.CyNode;
import cytoscape.data.CyAttributes;
import cytoscape.logger.CyLogger;
import cytoscape.render.stateful.CustomGraphic;
import cytoscape.util.URLUtil;
import giny.view.NodeView;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.ChemModel;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.fingerprint.IFingerprinter;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IAtomType.Hybridization;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.interfaces.IRingSet;
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
import org.openscience.cdk.ringsearch.SSSRFinder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
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
 * of the CDK IAtomContainer, which is used for conversion from InChI to SMILES, for calculation of the molecular weight,
 * and for the calculation of Tanimoto coefficients.
 */

public class Compound {
	public enum AttriType { smiles, inchi };
	public enum DescriptorType {
		IMAGE ("2D Image", "image", Compound.class),
		ATTRIBUTE ("Attribute", "attribute", String.class),
		IDENTIFIER ("Molecular String", "molstring", String.class),
		LIPINSKI ("Lipinski parameters", "lipinski", Map.class),
		SDF ("SDF parameters", "sdf", Map.class),
		ALOGP ("ALogP", "alogp", Double.class),
		ALOGP2 ("ALogP2", "alogp2", Double.class),
		AROMATICRINGSCOUNT ("Aromatic ring count", "naromatic", Integer.class),
		ATOMICCOMP ("Atomic composition", "atomiccomp", Double.class),
		MASS ("Exact Mass", "mass", Double.class),
		HEAVYATOMCOUNT ("Heavy atom count", "nheavy", Integer.class),
		HBONDACCEPTOR ("HBond Acceptors", "acceptors", Integer.class),
		HBONDDONOR ("HBond Donors", "donors", Integer.class),
		LOBMAX ("Length over Breadth Max", "lobmax", Double.class),
		LOBMIN ("Length over Breadth Min", "lobmin", Double.class),
		RULEOFFIVE ("Lipinski's Rule of Five Failures", "roff", Double.class),
		AMR ("Molar refractivity", "refractivity", Double.class),
		WEIGHT ("Molecular Wt.", "weight", Double.class),
		RINGCOUNT ("Ring count", "nrings", Integer.class),
		RBONDS ("Rotatable Bonds Count", "rotbonds", Integer.class),
		TPSA ("Topological Polar Surface Area", "polarsurface", Double.class),
		TBONDS ("Total Number of Bonds", "totbonds", Integer.class),
		WEINERPATH ("Wiener Path", "wienerpath", Double.class),
		WEINERPOL ("Wiener Polarity", "weinerpolarity", Double.class),
		XLOGP ("XLogP", "xlogp", Double.class),
		ZAGREBINDEX ("Zagreb Index", "zagrebindex", Double.class);

		private String name;
		private Class classType;
		private int columnCount;
		private String shortName;

		DescriptorType(String name, String shortName, Class classType) {
			this.name = name;
			this.shortName = shortName;
			this.classType = classType;
		}

		public String toString() { return this.name; }
		public String getShortName() { return this.shortName; }
		public Class getClassType() { return this.classType; }
	}

	// Class variables
	public static long totalTime = 0L;
	public static long totalFPTime = 0L;
	public static long totalSMILESTime = 0L;
	public static long totalGetFPTime = 0L;

	static private ConcurrentHashMap<GraphObject, List<Compound>> compoundMap;
	static private CyLogger logger = CyLogger.getLogger(Compound.class);
	static private Fingerprinter fingerprinter = Fingerprinter.PUBCHEM;
	static private DescriptorType[] descriptorTypes = {
		DescriptorType.IMAGE, DescriptorType.ATTRIBUTE, DescriptorType.IDENTIFIER, 
		DescriptorType.LIPINSKI, DescriptorType.SDF,
		DescriptorType.ALOGP,
		DescriptorType.ALOGP2,
		DescriptorType.AROMATICRINGSCOUNT,
		DescriptorType.ATOMICCOMP,
		DescriptorType.MASS,
		DescriptorType.HEAVYATOMCOUNT,
		DescriptorType.HBONDACCEPTOR,
		DescriptorType.HBONDDONOR,
		DescriptorType.LOBMAX,
		DescriptorType.LOBMIN,
		DescriptorType.RULEOFFIVE,
		DescriptorType.AMR,
		DescriptorType.WEIGHT, 
		DescriptorType.RINGCOUNT,
		DescriptorType.RBONDS,
		DescriptorType.TPSA,
		DescriptorType.TBONDS,
		DescriptorType.WEINERPATH,
		DescriptorType.WEINERPOL,
		DescriptorType.XLOGP
	};

	static private DescriptorType[] SDFTypes = {
		DescriptorType.XLOGP, DescriptorType.TPSA, DescriptorType.ZAGREBINDEX
	};
	static private DescriptorType[] LipinskiTypes = {
		DescriptorType.WEIGHT, DescriptorType.ALOGP, DescriptorType.HBONDACCEPTOR, DescriptorType.HBONDDONOR
	};

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
 	 * Static method to set the fingerprinter to use.
 	 *
 	 * @param fp the Fingerprinter we want to use
 	 */
	static public void setFingerprinter(Fingerprinter fp) {
		fingerprinter = fp;
	}

	static public List<DescriptorType> getDescriptorList() {
		return Arrays.asList(descriptorTypes);
	}

	static public DescriptorType[] getComboList(DescriptorType type) {
		if (type.getClassType() != Map.class) return null;
		if (type.equals(DescriptorType.LIPINSKI))
			return LipinskiTypes;
		if (type.equals(DescriptorType.SDF))
			return SDFTypes;

		return null;
	}

	static public void clearStructures(GraphObject object) {
		if (object == null) {
			compoundMap = null;
		} else {
			if (compoundMap.containsKey(object)) {
				compoundMap.remove(object);
			}
		}
	}

	static public void reloadStructures(GraphObject object) {
		if (object == null) {
			for (List<Compound> cList: compoundMap.values()) {
				for (Compound c: cList) {
					c.createStructure(null);
				}
			}
		} else if (compoundMap.containsKey(object)) {
			List<Compound> cList = compoundMap.get(object);
			for (Compound c: cList) {
				c.createStructure(null);
			}
		}
	}

	/************************************************************************************* 
	 *                             Instance methods                                      *
	 *************************************************************************************/ 

	// Instance variables
	private GraphObject source;
	private String smilesStr;
	private String moleculeString;
	private String attribute;
	protected Image renderedImage;
	protected boolean laidOut;
	private AttriType attrType;
	private IAtomContainer iMolecule;
	private IAtomContainer iMolecule3D;
	private BitSet fingerPrint;
	private IFingerprinter fp;
	private int lastImageWidth = -1;
	private int lastImageHeight = -1;
	private int index = 0;
	private	boolean	lastImageFailed = false;
	private DescriptorEngine descriptorEngine = null;

	/**
 	 * The constructor is creates a compound and stores it in
 	 * the compound map.  As a byproduct of the compound creation,
 	 * the CDK iMolecule is also created.  This form of the constructor
 	 * assumes that the compound is connected to a node or edge.
 	 *
 	 * @param source the graph object that holds this compound
 	 * @param attribute the attribute that has the compound string
 	 * @param mstring the compound descriptor itself
 	 * @param attrType the type of the compound descriptor (inchi or smiles)
 	 */
	public Compound(GraphObject source, String attribute, String mstring, 
	                AttriType attrType) {
		this(source, attribute, mstring, null, attrType);
	}

	/**
 	 * This alternative form of the constructor is called when we've calculated an IAtomContainer internally and it
 	 * bypasses the creation.
 	 *
 	 * @param source the graph object that holds this compound
 	 * @param attribute the attribute that has the compound string
 	 * @param mstring the compound descriptor itself
 	 * @param attrType the type of the compound descriptor (inchi or smiles)
 	 */
	public Compound(GraphObject source, String attribute, String mstring, IAtomContainer molecule,
	                AttriType attrType) {
		this.source = source;
		this.attribute = attribute;
		this.moleculeString = mstring.trim();
		this.attrType = attrType;

		if (source != null) {
			List<Compound> mapList = null;
			if (Compound.compoundMap == null) 
				Compound.compoundMap = new ConcurrentHashMap<GraphObject, List<Compound>>();

			if (Compound.compoundMap.containsKey(source)) {
				mapList = Compound.compoundMap.get(source);
			} else {
				mapList = new ArrayList();
			}
			this.index = mapList.size();
			mapList.add(this);
			Compound.compoundMap.put(source, mapList);
		}
		createStructure(molecule);
	}


	public void createStructure(IAtomContainer molecule) {
		long startTime = Calendar.getInstance().getTimeInMillis();

		this.renderedImage = null;
		this.laidOut = false;
		this.iMolecule = molecule;
		this.iMolecule3D = null;
		this.smilesStr = null;
		this.fp = Compound.fingerprinter.getFingerprinter();
		this.fingerPrint = null;
		long fpTime = Calendar.getInstance().getTimeInMillis();
		totalFPTime += fpTime-startTime;

		if (attrType == AttriType.inchi) {
			// Convert to smiles 
			this.smilesStr = convertInchiToSmiles(moleculeString);
		} else {
			if (moleculeString != null && moleculeString.length() > 0) {
				// Strip any blanks in the string
				this.smilesStr = moleculeString.replaceAll(" ", "");
			}
		}

		if (smilesStr == null)
			return;

		logger.debug("smiles string = "+smilesStr);

		if (this.iMolecule == null) {
			// Create the CDK Molecule object
			SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
			try {
				iMolecule = sp.parseSmiles(this.smilesStr);
			} catch (InvalidSmilesException e) {
				iMolecule = null;
				logger.warning("Unable to parse SMILES: "+smilesStr+" for "+source.getIdentifier()+": "+e.getMessage());
				// Something's a little flakey with the SmilesParser in 1.5.1, so try again
				sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
				try {
					iMolecule = sp.parseSmiles(this.smilesStr);
				} catch (InvalidSmilesException e2) {
					// No reason to tell the user again
					return;
				}
			}
		}

		long smilesTime = Calendar.getInstance().getTimeInMillis();
		totalSMILESTime += smilesTime-fpTime;

		// At this point, we should have an IAtomContainer
		try { 
			CDKHueckelAromaticityDetector.detectAromaticity(iMolecule);

			// Make sure we update our implicit hydrogens
			CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(iMolecule.getBuilder());
			adder.addImplicitHydrogens(iMolecule);

			// Don't calculate the fingerprint here -- this is *very* expensive
			// fingerPrint = fp.getFingerprint(addh(iMolecule));
			fingerPrint = null;
		} catch (CDKException e1) {
			fingerPrint = null;
		}

		long getFPTime = Calendar.getInstance().getTimeInMillis();
		totalGetFPTime += getFPTime-smilesTime;

		totalTime += getFPTime-startTime;
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
 	 * Return the CDK IAtomContainer for this compound
 	 *
 	 * @return the IAtomContainer for this compound
 	 */
	public IAtomContainer getIAtomContainer() {
		return iMolecule;
	}

	/**
 	 * Return the CDK fingerprint for this compound
 	 *
 	 * @return the fingerprint for this compound
 	 */
	public BitSet getFingerprint() {
		if (fingerPrint == null) {
			try {
				synchronized (fp) {
					fingerPrint = fp.getBitFingerprint(addh(iMolecule)).asBitSet();
				}
			} catch (Exception e) {
				logger.warning("Error calculating fingerprint: "+e);
				e.printStackTrace();
			}
		}
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

	public String toString() {
		if (source == null)
			return maxString(moleculeString, 10);

		return source.getIdentifier()+" ("+attribute+") ["+index+"]";
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
			case LIPINSKI:
				{
					if (iMolecule == null) return null;
					Map<DescriptorType, Object> lip = new HashMap<DescriptorType,Object>();
					for (DescriptorType subType: LipinskiTypes) {
						lip.put(subType, getDescriptor(subType));
					}
					return lip;
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
			case TBONDS:
				{
					if (iMolecule == null) return null;
					IMolecularDescriptor descriptor = new BondCountDescriptor();
					IntegerResult retval = (IntegerResult)(descriptor.calculate(iMolecule).getValue());
					return retval.intValue();
				}
			case SDF:
				{
					if (iMolecule == null) return null;
					Map<DescriptorType, Object> lip = new HashMap<DescriptorType,Object>();
					for (DescriptorType subType: SDFTypes) {
						lip.put(subType, getDescriptor(subType));
					}
					return lip;
				}
			case TPSA:
				{
					if (iMolecule == null) return null;
					IMolecularDescriptor descriptor = new TPSADescriptor();
					DoubleResult retval = (DoubleResult)(descriptor.calculate(iMolecule).getValue());
					return retval.doubleValue();
				}
			case RINGCOUNT:
			case AROMATICRINGSCOUNT:
				{
					if (iMolecule == null) return null;
					SSSRFinder finder = new SSSRFinder(iMolecule);
					IRingSet ringSet = finder.findSSSR();
					if (type == DescriptorType.RINGCOUNT)
						return new Integer(ringSet.getAtomContainerCount());

					// We have the number of rings, now we want to restrict
					// the ring set to aromatic rings only
					Iterator<IAtomContainer> i = ringSet.atomContainers().iterator();
					while (i.hasNext()) {
						IRing r = (IRing) i.next();
						if (r.getAtomCount() > 8) {
							i.remove();
						} else {
							for (IAtom a: r.atoms()) {
								Hybridization h = a.getHybridization();
								if (h == CDKConstants.UNSET
								    || !(h == Hybridization.SP2
								    || h == Hybridization.PLANAR3)) {
									i.remove();
									break;
								}
							}
						}
					}
					return new Integer(ringSet.getAtomContainerCount());
				}
			case HEAVYATOMCOUNT:
				{
					int heavyAtomCount = 0;
					for (int i = 0; i < iMolecule.getAtomCount(); i++) {
						if (!(iMolecule.getAtom(i).getSymbol()).equals("H"))
							heavyAtomCount++;
					}
					return new Integer(heavyAtomCount);
				}
			case XLOGP:
				{
					if (iMolecule == null) return null;
					try {
						IMolecularDescriptor descriptor = new XLogPDescriptor();
						Object[] params = {new Boolean(true), new Boolean(false)};
						descriptor.setParameters(params);
						DoubleResult retval = (DoubleResult)(descriptor.calculate(addh(iMolecule)).getValue());
						return retval.doubleValue();
					} catch (CDKException e) { return null; }
				}
			case ZAGREBINDEX:
				{
					if (iMolecule == null) return null;
					IMolecularDescriptor descriptor = new ZagrebIndexDescriptor();
					DoubleResult retval = (DoubleResult)(descriptor.calculate(iMolecule).getValue());
					return retval.doubleValue();
				}
			case ATOMICCOMP:
				{
					int totalAtoms = 0;
					int polarAtoms = 0;
					for (IAtom a: iMolecule.atoms()) {
						String symbol = a.getSymbol();
						if (symbol.equals("N")) {
							polarAtoms++;
						} else if (symbol.equals("C")) {
							totalAtoms++;
						} else if (symbol.equals("O")) {
							polarAtoms++;
						} else if (symbol.equals("P")) {
							polarAtoms++;
						} else if (symbol.equals("S")) {
							polarAtoms++;
						}
					}
					return new Double((double)polarAtoms/(double)(polarAtoms+totalAtoms));
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
		// System.out.println("depictWithCDK("+width+","+height+")");

		if (iMolecule == null || width == 0 || height == 0) {
			return blankImage(iMolecule, width, height);
		}

		int renderWidth = width;
		if (renderWidth < 200) renderWidth = 200;
		int renderHeight = height;
		if (renderHeight < 200) renderHeight = 200;

		BufferedImage bufferedImage = new BufferedImage(renderWidth, renderHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bufferedImage.createGraphics();

		g2d.setColor(background);
		g2d.setBackground(background);
		g2d.fillRect(0,0,width,height);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		AtomContainerRenderer renderer = getRenderer(background);
		if (renderer == null)
			return null;

		Rectangle2D bbox = new Rectangle2D.Double(0,0,renderWidth,renderHeight);
		renderer.setup(iMolecule, new Rectangle(renderWidth, renderHeight));
		renderer.paint(iMolecule, new AWTDrawVisitor(g2d), bbox, true);

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
		return bufferedImage;
	}

	public List<CustomGraphic> depictWithCDK(double x, double y, double width, double height, 
	                                         Color background, NodeView view) {
		if (iMolecule == null || width == 0.0 || height == 0.0) {
			return null;
		}

		double boxSize = 300.0;

		AtomContainerRenderer renderer = getRenderer(background);
		double scale = Math.min(width/boxSize, height/boxSize);
		Rectangle2D bbox = new Rectangle2D.Double(x/scale,y/scale,width/scale,height/scale);
		renderer.setup(iMolecule, new Rectangle((int)boxSize, (int)boxSize));
		CustomGraphicsVisitor cgV = new CustomGraphicsVisitor(view, scale);
		renderer.paint(iMolecule, cgV, bbox, true);
		return cgV.getCustomGraphics();
	}

	private AtomContainerRenderer getRenderer(Color background) {
		try {
			if (!laidOut) {
				// Is the structure connected?
				if (!ConnectivityChecker.isConnected(iMolecule)) {
					// No, for now, find the largest component and use that exclusively
					IAtomContainerSet molSet = ConnectivityChecker.partitionIntoMolecules(iMolecule);
					IAtomContainer largest = molSet.getAtomContainer(0);
					for (int i = 0; i < molSet.getAtomContainerCount(); i++) {
						if (molSet.getAtomContainer(i).getAtomCount() > largest.getAtomCount())
							largest = molSet.getAtomContainer(i);
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
			return renderer;
		} catch (Exception e) {
			logger.warning("Unable to depict molecule for "+source.getIdentifier()+" with CDK depiction: "+e.getMessage(), e);
			return null;
		}
	}

	private IAtomContainer addh(IAtomContainer mol) {
		IAtomContainer molH;
		try {
			molH = (IAtomContainer)mol.clone();
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

			InChIToStructure intostruct = factory.getInChIToStructure(inchi, SilentChemObjectBuilder.getInstance());

			// Get the structure
			INCHI_RET ret = intostruct.getReturnStatus();
			if (ret == INCHI_RET.WARNING) {
				logger.warning("InChI warning: " + intostruct.getMessage());
			} else if (ret != INCHI_RET.OKAY) {
				logger.warning("Structure generation failed: " + ret.toString()
     	               + " [" + intostruct.getMessage() + "]");
				return null;
			}

			IAtomContainer molecule = new AtomContainer(intostruct.getAtomContainer());
			// Use the molecule to create a SMILES string
			SmilesGenerator sg = new SmilesGenerator();
			return sg.createSMILES(molecule);
		} catch (Exception e) {
			logger.warning("Structure generation failed: " + e.getMessage(), e);
			return null;
		}

	}

	private Image blankImage(IAtomContainer mol, int width, int height) {
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

	private String maxString(String str, int max) {
		if (str.length() <= max) return str;

		return str.substring(0,max-3)+"...";
	}
}
