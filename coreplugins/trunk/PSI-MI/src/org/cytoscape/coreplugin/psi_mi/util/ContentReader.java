/** Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander
 ** Modified by: Nisha Vinod
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 ** 
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center 
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center 
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 ** 
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.cytoscape.coreplugin.psi_mi.util;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

/**
 * Retrieves Content from Local File System, Web Site, or FTP Site.
 *
 * @author Ethan Cerami
 */
public class ContentReader {
    private static final String HTTP = "http";
    private static final String FTP = "ftp";

    /**
     * Retrieves Content from Local System, Web Site or FTP site.
     *
     * @param urlStr URL String.
     * @return File contents.
     * @throws DataServiceException Error Retrieving file.
     */
    public String retrieveContent(String urlStr) throws DataServiceException {

        String content = null;
        URL url = null;
        try {

            if (urlStr.startsWith(HTTP) || urlStr.startsWith(FTP)) {

                url = new URL(urlStr);

                if (url.getProtocol().equalsIgnoreCase(HTTP)) {

                    content = retrieveContentFromWeb(url);

                }
                if (url.getProtocol().equalsIgnoreCase(FTP)) {

                    content = retrieveContentFromFtp(url);

                }
            } else {

                File file =  new File(urlStr);

                InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
                        //determineFileType(new FileInputStream(file));

                content = this.retrieveContentFromFile(reader);//(file);
            }
        } catch (MalformedURLException e) {
            throw new DataServiceException (e, "URL is malformed:  "
                + url + ".  Please try again.");
        } catch (UnknownHostException e) {
            String msg = "Network error occurred while trying to "
                + "make network connection.  Could not find server:  "
                + e.getMessage()
                + ". Please check your server and network settings, "
                + "and try again.";
            throw new DataServiceException (e, msg);
        } catch (IOException e) {
            throw new DataServiceException (e, "Error occurred "
                + " while trying to retrieve data from:  " + urlStr);
        }
        return content;
    }

    /**
     * Retrieves Content from Web.
     *
     * @param url URL String.
     * @return File contents.
     */
    private String retrieveContentFromWeb(URL url) throws IOException {
        InputStreamReader isr = new InputStreamReader(url.openStream());
        final int bufSize = 65536;
        char[] buf = new char[bufSize];
        String content = "";
        int charsread = 0;
        while (true) {
            charsread = isr.read(buf, 0, bufSize);
            if (charsread == -1) {
                break;
            }
            String bufstring = new String(buf, 0, charsread);
            content += bufstring;
        }
        return content;
    }

    private URL addAuthorizationToUrl(URL url) throws MalformedURLException,
            IOException {
        //System.out.println("8");
        String protocol = url.getProtocol();
        String userinfo = url.getUserInfo();
        String host = url.getHost();
        int port = url.getPort();
        String file = url.getFile();
        String urlfinal = null;
        URL authorizedurl;
        if (userinfo == null || userinfo.length() == 0) {
            userinfo = "anonymous:anon@ftp.net";
            authorizedurl = url;
        }
        else
        {
            authorizedurl = new URL(protocol + "://" + userinfo
                    + "@" + host + ":" + port
                + file);
        }

        return  authorizedurl;
    }

    /**
     * Retrieves Content from FTP site.
     *
     * @param url URL String.
     * @return File contents.
     * @throws IOException Error Retrieving file.
     */
    private String retrieveContentFromFtp(URL url) throws IOException {
        URL authorizedurl = addAuthorizationToUrl(url);


        InputStreamReader isr = new InputStreamReader(url.openStream());
                //determineFileType(authorizedurl.openStream());
        final int bufSize = 65536;
        char[] buf = new char[bufSize];
        String content = "";
        int charsread = 0;
        while (true) {
            charsread = isr.read(buf, 0, bufSize);
            if (charsread == -1) {
                break;
            }
            String bufstring = new String(buf, 0, charsread);
            content += bufstring;
        }
        return content;
    }

    /**
     * Retrieves Content from local File System.
     *
     * @param reader Reader Object
     * @return File contents.
     * @throws IOException Error Retrieving file.  //File file
     */
    private String retrieveContentFromFile(InputStreamReader reader) throws IOException {

        final int bufSize = 65536;
        char[] buf = new char[bufSize];
        String content = "";
        int charsread = 0;
        while (true) {
            charsread = reader.read(buf, 0, bufSize);
            if (charsread == -1) {
                break;
            }
            String bufstring = new String(buf, 0, charsread);
            content += bufstring;
        }
        return content;
        /*FileReader reader = new FileReader(file);
        BufferedReader buffered = new BufferedReader(reader);
        String line = buffered.readLine();
        while (line != null) {
            content.append(line);
            content.append("\n");
            line = buffered.readLine();
        }
        System.out.println("Got content..." + content.toString().length());
        return content.toString();  */
    }

     /**
     * This method will try to determine whether this stream is connected to a zip file, a gzipped file, a possible text
     * file or a possible binary file, in that order. It will return an appropriate Reader for the identified filetype,
     * or 'null' if the type was not recognizable (ie: we could not read the four bytes constituting the magic number).
     *
     * @param aStream InputStream to determine the nature of
     *
     * @return Reader   with a Reader into the converted stream.
     */
    public static InputStreamReader determineFileType( InputStream aStream ) {
        InputStreamReader result = null;
        try {
            // Reading the first four bytes.
            byte[] firstFour = new byte[4];
            PushbackInputStream pbi = new PushbackInputStream( aStream, 4 );
            int count = pbi.read( firstFour, 0, 4 );

            // If we couldn't even read 4 bytes, it cannot be good. In that case we 'll therefore return 'null'.
            if ( count == 4 ) {
                /*System.out.println( "Read magic numbers (first four bytes: " +
                              firstFour[ 0 ] + " " +
                              firstFour[ 1 ] + " " +
                              firstFour[ 2 ] + " " +
                              firstFour[ 3 ] + " " + ")." ); */

                // Now unread our bytes.
                pbi.unread( firstFour );

                // Okay, let's check these magic numbers, shall we?
                if ( firstFour[ 0 ] == (byte) 0x1F &&
                     firstFour[ 1 ] == (byte) 0x8b ) {

                    // GZIP!
                    //System.out.println( "Detected GZIP format." );
                    result = new InputStreamReader( new GZIPInputStream( pbi ));

                } else if ( firstFour[ 0 ] == (byte) 0x50 &&
                            firstFour[ 1 ] == (byte) 0x4b &&
                            firstFour[ 2 ] == (byte) 0x03 &&
                            firstFour[ 3 ] == (byte) 0x04 ) {

                    // (pk)ZIP!
                    //System.out.println( "Detected ZIP format." );
                    ZipInputStream zis = new ZipInputStream( pbi );

                    ZipEntry ze = zis.getNextEntry();

                    // Extra check: ze cannot be 'null'!
                    if ( ze != null ) {
                        result = new InputStreamReader( zis );

                        // TODO may display a message here is there's more than one entry.
                    }




                }
                // If we are here and result is still 'null', we weren't able to identify it as either GZIP or ZIP.
                // So create a regular reader and go from here.
                if ( result == null ) {
                    System.out.println( "Defaulted to standard Reader." );
                    result = new InputStreamReader( pbi );
                }
            }
        } catch ( Exception ioe ) {
            System.out.println( "IOException while attempting to determine filetype."+ ioe );
        }

        return result;
    }


}
