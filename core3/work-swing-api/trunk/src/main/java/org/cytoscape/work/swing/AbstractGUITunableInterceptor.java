package org.cytoscape.work.swing;


import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.work.TunableHandlerFactory;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.spring.SpringTunableInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Vector;
import java.awt.Dimension;
import java.awt.Window;

/**
 * Interceptor of <code>Tunable</code>s that will be applied on <code>GUITunableHandlers</code>.
 *
 * <p><pre>
 * To set the new value to the original objects contained in the <code>GUITunableHandlers</code>:
 * <ul>
 *   <li>Creates the parent container for the GUI, or uses the one that has been specified</li>
 *   <li>Creates a GUI with Swing components for each intercepted <code>Tunable</code></li>
 *   <li>
 *     Displays the GUI to the user, following the layout construction rule specified in the <code>Tunable</code>
 *     annotations, and the dependencies to enable or disable the graphic components
 *   </li>
 *   <li>
 *     Applies the new <i>value,item,string,state...</i> to the object contained in the <code>GUITunableHandler</code>,
 *     if the modifications have been validated by the user.
 *   </li>
 * </ul>
 * </pre></p>
 *
 * @author pasteur
 */
public abstract class AbstractGUITunableInterceptor extends SpringTunableInterceptor<GUITunableHandler> implements GUITunableInterceptor<GUITunableHandler> {
	/** A reference to the parent <code>JPanel</code>, if any. */
	protected JPanel parentPanel = null;

	/** The list of handlers associated with this interceptor. */
	protected List<GUITunableHandler> handlers;

	/** The actual/original objects whose tunables this interceptor manages. */
	protected Object[] objectsWithTunables;

	/** Provides an initialised logger. */
	protected Logger logger;

	protected Window parent = null;
	
	/**
	 * Creates an Interceptor that will use the <code>GUITunableHandlers</code> created in a
	 * <code>TunableHandlerFactory</code> from intercepted <code>Tunables</code>.
	 * @param factory  used to create tunable handlers
	 */
	public AbstractGUITunableInterceptor(final TunableHandlerFactory<GUITunableHandler> factory) {
		super(factory);
		logger = LoggerFactory.getLogger(getClass());
	}

	/** {@inheritDoc} */
	public void setTunablePanel(final JPanel parent){
		this.parentPanel = (JPanel)parent;
	}
	
	/** {@inheritDoc} */
	final public JPanel getUI(final Object... proxyObjs) {
		this.objectsWithTunables = convertSpringProxyObjs(proxyObjs);
		handlers = new ArrayList<GUITunableHandler>();
		return constructUI();
	}

	/** This is the final action in getUI() after the translating the arguments to the original/actual annotated objects.
	 */
	abstract protected JPanel constructUI();

	/**
	 * Creates a GUI for the detected <code>Tunables</code>, following the graphic rules specified in <code>Tunable</code>s annotations
	 * or uses the JPanel provided by the method annotated with <code>@ProvidesGUI</code>
	 *
	 * The new values that have been entered for the Object contained in <code>GUITunableHandlers</code> are also set if the user clicks on <i>"OK"</i>
	 *
	 * @param proxyObjs an array of objects with <code>Tunables</code>s
	 *
	 * @return if new values have been successfully set
	 */
	final public boolean execUI(Object... proxyObjs) {
		final JPanel panel = getUI(proxyObjs);
		if (panel == null)
			return true;

		return displayGUI(panel, proxyObjs);
	}

	/** {@inheritDoc} */
	final public boolean hasTunables(final Object o) {
		return super.hasTunables(convertSpringProxyObj(o));
	}

	
	protected TunnableDialog tunnableDialog = null;
	
	
	/** This implements the final action in execUI() and executes the UI.
	 *  @param optionPanel  the panel containing the various UI elements corresponding to individual tunables
	 *  @param proxyObjs    represents the objects annotated with tunables
	 */
	protected boolean displayGUI(final JPanel optionPanel, Object... proxyObjs) {
		
		//Object[] buttons = { "OK", "Cancel" };
		//int result = JOptionPane.showOptionDialog(parentPanel, optionPanel,
		//					  "Set Parameters",
		//					  JOptionPane.YES_NO_CANCEL_OPTION,
		//					  JOptionPane.PLAIN_MESSAGE,
		//					  null,
		//					  buttons,
		//					  buttons[0]);
		//
		//if (result == JOptionPane.OK_OPTION)
		//	return validateAndWriteBackTunables(proxyObjs);
		//else
		//	return false;
		//
		
		tunnableDialog = new TunnableDialog();
		tunnableDialog.setLocationRelativeTo(parent);
		tunnableDialog.setTitle("Set Parameters");
		tunnableDialog.addComponent(optionPanel);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(tunnableDialog);
		} catch (Exception e) { /* not really a problem if this fails */ }

		tunnableDialog.setVisible(true);
		
		String userInput = tunnableDialog.getUserInput();
		
		if (userInput.equalsIgnoreCase("OK")){
			return validateAndWriteBackTunables(proxyObjs);			
		}
		else { // CANCEL
			return false;			
		}
	}

	abstract protected boolean validateTunableInput();

}
