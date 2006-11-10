/** Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander
 **
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
package org.cytoscape.coreplugin.psi_mi.model;

import java.util.ArrayList;

/**
 * Encapsulates a Single Interaction.
 *
 * @author Ethan Cerami
 */
public class Interaction extends AttributeBag {
    private ArrayList interactors;

    /**
     * Gets All Interactors
     *
     * @return ArrayList of Interactors.
     */
    public ArrayList getInteractors() {
        return interactors;
    }

    /**
     * Sets All Interactors.
     *
     * @param interactors ArrayList of Interactors.
     */
    public void setInteractors(ArrayList interactors) {
        this.interactors = interactors;
    }

    /**
     * To String Method.
     *
     * @return Interaction Description.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("Interaction: ");
        String description = this.getDescription();
        buffer.append(description);
        return buffer.toString();
    }

    /**
     * Gets Interaction Description.
     *
     * @return Interaction Description.
     */
    public String getDescription() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < interactors.size(); i++) {
            Interactor interactor = (Interactor) interactors.get(i);
            String name = interactor.getName();
            buffer.append(" [" + name + "]");
        }
        ExternalReference xrefs[] = getExternalRefs();
        buffer.append (" [Interaction XREFs --> ");
        if (xrefs == null || xrefs.length ==0) {
            buffer.append ("None");
        } else {
            for (int i=0; i<xrefs.length; i++) {
                buffer.append (xrefs[i].toString() + " ");
            }
        }
        buffer.append ("]");
        return buffer.toString();
    }
}