package cytoscape.util.export;

import java.io.FileOutputStream;
import java.io.IOException;

import cytoscape.Cytoscape;
import org.cytoscape.view.GraphView;

import org.freehep.graphicsio.ps.PSGraphics2D;
import java.util.Properties;

public class PSExporter implements Exporter
{
	private boolean exportTextAsFont = true;

	public PSExporter()
	{
	}


	public void export(GraphView view, FileOutputStream stream) throws IOException
	{
		view.setPrintingTextAsShape(!exportTextAsFont);
		
		Properties p = new Properties();
	    p.setProperty(PSGraphics2D.PAGE_SIZE,"Letter");
		p.setProperty("org.freehep.graphicsio.AbstractVectorGraphicsIO.TEXT_AS_SHAPES",
		              Boolean.toString(!exportTextAsFont)); 

	    PSGraphics2D g = new PSGraphics2D(stream, view); 
	    g.setMultiPage(false); // true for PS file
	    g.setProperties(p); 

	    g.startExport(); 
	    view.print(g); 
	    g.endExport();

	}

	public void setExportTextAsFont(boolean pExportTextAsFont) {
		exportTextAsFont = pExportTextAsFont;
	}
}
