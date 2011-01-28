
package org.cytoscape.io.write;

import org.cytoscape.session.CySession;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.AbstractTask;
import java.io.File;
import java.util.List;

/**
 * A utility Task implementation that writes a {@link org.cytoscape.session.CySession} to a file.
 */
public final class CySessionWriter extends AbstractTask implements CyWriter {

	private final CySession session; 
	private final CySessionWriterManager writerMgr; 
	private final File outputFile; 

	/**
	 * @param writerMgr The {@link org.cytoscape.io.write.CySessionWriterManager} contains single expected
	 * {@link org.cytoscape.io.write.CySessionWriterFactory} to use to write the file.
	 * @param session The {@link org.cytoscape.session.CySession} to be written out. 
	 * @param outputFile The file the {@link org.cytoscape.session.CySession} should be written to.
 	 */
	public CySessionWriter(CySessionWriterManager writerMgr, CySession session, File outputFile) {

		if ( writerMgr == null )
			throw new NullPointerException("Writer Manager is null");
		this.writerMgr = writerMgr;

		if ( session == null )
			throw new NullPointerException("Session Manager is null");
		this.session = session;

		if ( outputFile == null )
			throw new NullPointerException("Output File is null");
		this.outputFile = outputFile;
	}

	/**
	 * The method that will actually write the specified session to the specified
	 * file.
	 * @param tm The {@link org.cytoscape.work.TaskMonitor} provided by the TaskManager execution environment.
	 */
	public final void run(TaskMonitor tm) throws Exception {

		List<CyFileFilter> filters = writerMgr.getAvailableWriterFilters();
		if ( filters == null || filters.size() < 1)
			throw new NullPointerException("No Session file filters found");
		if ( filters.size() > 1 )
			throw new IllegalArgumentException("Found too many session filters!");

		CyWriter writer = writerMgr.getWriter(session,filters.get(0),outputFile); 
		if ( writer == null )
			throw new NullPointerException("No CyWriter found for specified file type!");

		insertTasksAfterCurrentTask( writer );
	}
}
