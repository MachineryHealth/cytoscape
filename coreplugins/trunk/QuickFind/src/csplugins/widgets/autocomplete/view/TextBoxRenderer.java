package csplugins.widgets.autocomplete.view;

import csplugins.widgets.autocomplete.index.Hit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Custom Renderer for the TextIndexComboBox.
 *
 * @author Ethan Cerami
 */
public class TextBoxRenderer implements ListCellRenderer {
    private JComboBox box;
    private double popupSizeMultiple;
    private static final int HGAP = 10;
    private static final int RIGHT_MARGIN = 15;
    private static final boolean DEBUG = false;

    /**
     * Constructor.
     *
     * @param box               JComboBox.
     * @param popupSizeMultiple Popup size multiple.
     */
    public TextBoxRenderer(JComboBox box, double popupSizeMultiple) {
        this.box = box;
        this.popupSizeMultiple = popupSizeMultiple;
    }

    /**
     * This method finds the image and text corresponding
     * to the selected value and returns the label, set up
     * to display the text and image.
     *
     * @param list         JList Object.
     * @param value        Object value.
     * @param index        Index value.
     * @param isSelected   is item selected flag.
     * @param cellHasFocus call has focus flag.
     * @return Component Object.
     */
    public Component getListCellRendererComponent(JList list,
            Object value, int index, boolean isSelected, boolean cellHasFocus) {
        //  Create a JPanel Object
        JPanel panel = new JPanel();
        panel.setOpaque(true);

        //  Set different colors, depending on state
        if (isSelected) {
            panel.setBackground(list.getSelectionBackground());
            panel.setForeground(list.getSelectionForeground());
        } else {
            panel.setBackground(list.getBackground());
            panel.setForeground(list.getForeground());
        }

        //  Use Box Layout, X-AXIS
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(new EmptyBorder(2, 2, 2, 2));
        JLabel textMatchLabel = new JLabel(value.toString(), JLabel.LEFT);
        panel.setToolTipText(value.toString());

        String numResults = getNumResults(value);
        JLabel numResultsLabel = new JLabel(numResults, JLabel.RIGHT);
        numResultsLabel.setFont(new Font(numResultsLabel.getFont().getName(),
                Font.PLAIN,
                numResultsLabel.getFont().getSize() - 1));

        //  Set color to green (matches exist) or red (no matches exist);
        Color color = new Color(51, 102, 51);
        if (value instanceof Hit) {
            Hit hit = (Hit) value;
            if (hit.getAssociatedObjects() != null) {
                int numHits = hit.getAssociatedObjects().length;
                if (numHits == 0) {
                    color = new Color (150, 0, 0);
                }
            }
        }
        numResultsLabel.setForeground(color);

        //  Resize Labels
        resizeLabels(numResultsLabel, textMatchLabel);

        //  Add Label 1, then glue, then Label 2
        //  The glue forces Label 1 to be left aligned, and Label 2 to be
        //  right aligned
        panel.add(textMatchLabel);
        panel.add(Box.createHorizontalGlue());
        panel.add(numResultsLabel);
        return panel;
    }

    /**
     * Resizes the Labels, as needed.
     *
     * @param numResultsLabel Number of Results Label.
     * @param textMatchLabel  Text Match Label.
     */
    private void resizeLabels(JLabel numResultsLabel, JLabel textMatchLabel) {
        if (box.isPopupVisible()) {
            //  How wide is the popup window?
            int widthOfPopUpWindow = (int) (box.getSize().width
                    * this.popupSizeMultiple);

            //  How wide is numResultsLabel?
            int widthOfNumResultsText =
                    numResultsLabel.getPreferredSize().width;

            //  How wide is the vertical scrollbar?
            int widthOfVerticalScrollBar = 0;
            Object comp = box.getUI().getAccessibleChild(box, 0);
            if (comp instanceof JPopupMenu) {
                JScrollPane scrollPane = (JScrollPane)
                        ((JPopupMenu) comp).getComponent(0);
                JScrollBar verticalScrollBar =
                        scrollPane.getVerticalScrollBar();
                if (verticalScrollBar != null
                        && verticalScrollBar.isVisible()) {
                    widthOfVerticalScrollBar =
                            verticalScrollBar.getPreferredSize().width;
                }
            }

            //  Truncate label text, as needed
            int maxWidthOfTextValue = widthOfPopUpWindow
                    - (widthOfNumResultsText + HGAP + widthOfVerticalScrollBar
                    + RIGHT_MARGIN);
            int currentWidth = textMatchLabel.getPreferredSize().width;

            String str = textMatchLabel.getText();
            if (currentWidth > maxWidthOfTextValue) {
                textMatchLabel.setText(textMatchLabel.getText() + "...");

                //  while loop:  truncate string one letter at a time
                //  until we have right size string.
                while (currentWidth > maxWidthOfTextValue) {
                    str = textMatchLabel.getText();
                    str = str.substring(0, str.length() - 4) + "...";
                    textMatchLabel.setText(str);
                    currentWidth = textMatchLabel.getPreferredSize().width;
                }
            }

            if (DEBUG) {
                System.out.println("Width of window:  " + widthOfPopUpWindow);
                System.out.println("Width of num results text:  "
                        + widthOfNumResultsText);
                System.out.println("Bar:  " + widthOfVerticalScrollBar);
                System.out.println("Width of text:  " + maxWidthOfTextValue);
            }
        }
    }

    /**
     * Gets Number of Matching Results.
     *
     * @param value Object Value.
     * @return Number of Results String.
     */
    private String getNumResults(Object value) {
        String numResults = null;
        if (value instanceof Hit) {
            Hit hit = (Hit) value;
            Object objects[] = hit.getAssociatedObjects();
            if (objects != null) {
                if (objects.length == 1) {
                    numResults = "1 result ";
                } else {
                    numResults = objects.length + " results";
                }
            }
        }
        if (numResults == null) {
            numResults = " -- ";
        }
        return numResults;
    }
}