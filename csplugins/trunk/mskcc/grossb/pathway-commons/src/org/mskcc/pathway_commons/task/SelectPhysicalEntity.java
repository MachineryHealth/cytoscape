package org.mskcc.pathway_commons.task;

import org.mskcc.pathway_commons.schemas.search_response.*;
import org.mskcc.pathway_commons.schemas.summary_response.SummaryResponseType;
import org.mskcc.pathway_commons.view.model.InteractionBundleModel;
import org.mskcc.pathway_commons.view.model.PathwayTableModel;

import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;
import javax.swing.*;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;

/**
 * Indicates that the user has selected a physical entity from the list of search results.
 *
 * @author Ethan Cerami.
 */
public class SelectPhysicalEntity {
    private HashMap<Long, SummaryResponseType> parentSummaryMap;

    /**
     * Constructor.
     *
     * @param parentSummaryMap  Parent Summary Map.
     */
    public SelectPhysicalEntity (HashMap<Long, SummaryResponseType> parentSummaryMap) {
        this.parentSummaryMap = parentSummaryMap;
    }

    /**
     * Select the Phsyical Entity specified by the selected index.
     *
     * @param peSearchResponse      SearchResponseType peSearchResponse.
     * @param selectedIndex         Selected Index.
     * @param interactionBundleModel Interaction Table Model.
     * @param pathwayTableModel     Pathway Table Model.
     * @param summaryDocumentModel  Summary Document Model.
     */
    public void selectPhysicalEntity(SearchResponseType peSearchResponse,
            int selectedIndex, InteractionBundleModel interactionBundleModel, PathwayTableModel
            pathwayTableModel, Document summaryDocumentModel,
            JTextPane textPane, JComponent textPaneOwner) {
        if (peSearchResponse != null) {
            java.util.List<SearchHitType> searchHits = peSearchResponse.getSearchHit();
            SearchHitType searchHit = searchHits.get(selectedIndex);

            StringBuffer html = new StringBuffer();
            html.append("<html>");

            html.append ("<h2>" + searchHit.getName() + "</h2>");

            OrganismType organism = searchHit.getOrganism();
            if (organism != null) {
                String speciesName = organism.getSpeciesName();
                html.append ("<H3>" + speciesName + "</H3>");
            }

            //  Next, add synonyms
            List <String> synList = searchHit.getSynonym();
            StringBuffer synBuffer = new StringBuffer();
            if (synList != null && synList.size() > 0) {
                for (String synonym:  synList) {
                    if (!synonym.equalsIgnoreCase(searchHit.getName())) {
                        synBuffer.append("<LI>- " + synonym + "</LI>");
                    }
                }
                if (synBuffer.length() > 0) {
                    html.append("<h4>Synonyms:</h4>");
                    html.append("<UL>");
                    html.append(synBuffer.toString());
                    html.append("</UL>");
                    html.append("<BR>");
                }
            }

            //  Next, add XRefs
            List <XRefType> xrefList = searchHit.getXref();
            if (xrefList != null && xrefList.size() > 0) {
                html.append("<H4>Links:</H4>");
                html.append("<UL>");
                for (XRefType xref:  xrefList) {
                    String url = xref.getUrl();
                    if (url != null && url.length() > 0) {
                        html.append("<LI>- <a class=\"link\" href=\"" + url + "\">"
                                + xref.getDb() + ":  "
                                + xref.getId() + "</a></LI>") ;
                    } else {
                        html.append("<LI>- " + xref.getDb() + ":  " + xref.getId() + "</LI>");
                    }
                }
                html.append("</UL>");
            }

            List <String> excerptList = searchHit.getExcerpt();
            if (excerptList != null && excerptList.size() > 0) {
                String primeExcerpt = null;
                for (String excerpt:  excerptList) {
                    if (primeExcerpt == null || excerpt.length() > primeExcerpt.length()) {
                        if (!excerpt.equalsIgnoreCase(searchHit.getName())) {
                            primeExcerpt = excerpt;
                        }
                    }
                }
                if (primeExcerpt != null) {
                    html.append("<BR><H4>Matching Excerpt(s):</H4>");
                    html.append("<span class='excerpt'>" + primeExcerpt + "</span><BR>") ;
                }
            }

            //  Temporarily commented out
            //java.util.List<String> commentList = searchHit.getComment();
            //if (commentList != null) {
            //    html.append("<BR><B>Description:</B>");
            //    for (int i = commentList.size() - 1; i >= 0; i--) {
            //        html.append("<BR>" + commentList.get(i) + "<BR>");
            //   }
            //}

            html.append ("</html>");
            textPane.setText(html.toString());
            textPane.setCaretPosition(0);
            updatePathwayData(searchHit, pathwayTableModel);
            updateInteractionData(searchHit, interactionBundleModel);
			textPaneOwner.repaint();
        }
    }

    /**
     * Updates Interaction Data.
     *
     * @param searchHit             Search Hit Object.
     * @param interactionBundleModel Interaction Bundle Model.
     */
    private void updateInteractionData(SearchHitType searchHit, InteractionBundleModel
            interactionBundleModel) {
        SummaryResponseType parentSummary = parentSummaryMap.get(searchHit.getPrimaryId());
        if (parentSummary != null) {
            interactionBundleModel.setNumInteractions(parentSummary.getRecord().size());
        }
    }

    /**
     * Updates Pathway Data.
     *
     * @param searchHit         SearchHit Object.
     * @param pathwayTableModel Pathway Table Model.
     */
    private void updatePathwayData(SearchHitType searchHit, PathwayTableModel pathwayTableModel) {
        List<PathwayType> pathwayList = searchHit.getPathwayList().getPathway();

        Vector dataVector = pathwayTableModel.getDataVector();
        dataVector.removeAllElements();

        if (pathwayList != null) {
            pathwayTableModel.setRowCount(pathwayList.size());
            pathwayTableModel.resetInternalIds(pathwayList.size());
            //  Only set the column count, if it is not already set.
            //  If we reset the column count, the user-modified column widths are lost.
            if (pathwayTableModel.getColumnCount() != 2) {
                pathwayTableModel.setColumnCount(2);
            }
            for (int i = 0; i < pathwayList.size(); i++) {
                PathwayType pathway = pathwayList.get(i);
                pathwayTableModel.setValueAt(pathway.getDataSource().getName(), i, 0);
                pathwayTableModel.setValueAt(pathway.getName(), i, 1);
                pathwayTableModel.setInternalId(i, pathway.getPrimaryId());
            }
        }
    }
}