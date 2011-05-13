package cytoscape.genomespace;


import cytoscape.Cytoscape;
import cytoscape.util.CytoscapeAction;
import cytoscape.util.FileUtil;
import cytoscape.logger.CyLogger;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import java.io.File;
import java.awt.FileDialog;
import java.util.List;
import java.util.Map;
import java.util.Collection;

import org.genomespace.datamanager.core.GSFileMetadata;
import org.genomespace.client.GsSession;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.User;


/**
 * A simple action.  Change the names as appropriate and
 * then fill in your expected behavior in the actionPerformed()
 * method.
 */
public class DeleteFileInGenomeSpace extends CytoscapeAction {

	private static final long serialVersionUID = 4234432889999989L;
	private static final CyLogger logger = CyLogger.getLogger(DeleteFileInGenomeSpace.class);

	public DeleteFileInGenomeSpace() {
		// Give your action a name here
		super("Delete File in GenomeSpace",
		      new ImageIcon(DeleteFileInGenomeSpace.class.getResource("/images/genomespace_icon.gif")));

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File.GenomeSpace");
	}

	public void actionPerformed(ActionEvent e) {

		try {

		GsSession client = GSUtils.getSession(); 
		DataManagerClient dmc = client.getDataManagerClient();

		// list the files present for this user
		Map<String,GSFileMetadata> files = GSUtils.getFileNameMap( dmc.listDefaultDirectory().getContents() );

		String selectedFile = getSelectedFile( files.keySet() ); 

		// Delete the file from GenomeSpace
		if (selectedFile != null && files.get(selectedFile) != null) {
			logger.info("Deleting " + selectedFile);
			dmc.delete(files.get(selectedFile));
		}
	
		} catch (Exception ex) {
			logger.error("GenomeSpace failed",ex);
		}
	}

	private String getSelectedFile(Collection<String> names) {
		String s = (String)JOptionPane.showInputDialog(
                    Cytoscape.getDesktop(), "Select a file to delete:",
                    "Delete from GenomeSpace",
                    JOptionPane.WARNING_MESSAGE,
                    null, names.toArray() ,null);
		return s;
	}
}
