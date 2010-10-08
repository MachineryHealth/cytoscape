package org.cytoscape.io.read;

import java.io.InputStream;
import java.net.URI;

/**
 * An object that registers all InputStreamReaderFactory singletons,
 * processes specified input to determine the appropriate factory to
 * use and then returns an instance of the correct CyPropertyReader 
 * for the input.
 */
public interface CyPropertyReaderManager {
    /**
     * Given a URI this method will attempt to find a InputStreamReaderFactory
     * that can read the URI, will set the InputStream for the factory and
     * will return the reader task.
     * @param uri The URI we're attempting to read. 
     * @return A reader than can read the specified URI. Will return null if
     * no reader can be found.
     */
    CyPropertyReader getReader(URI uri);

    /**
     * Given an InputStream this method will attempt to find a InputStreamReaderFactory
     * that can read the stream, will set the InputStream for the factory and
     * will return the reader task.
     * @param stream The input stream we're attempting to read. 
     * @return A reader than can read the specified stream. Will return null if
     * no reader can be found.
     */
    CyPropertyReader getReader(InputStream stream);
}
