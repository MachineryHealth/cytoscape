package csplugins.quickfind.view;

import csplugins.quickfind.plugin.QuickFindPlugIn;
import csplugins.quickfind.util.QuickFind;
import csplugins.widgets.autocomplete.index.GenericIndex;
import csplugins.widgets.autocomplete.index.IndexFactory;
import csplugins.widgets.autocomplete.index.NumberIndex;
import csplugins.widgets.autocomplete.index.TextIndex;
import csplugins.widgets.autocomplete.view.ComboBoxFactory;
import csplugins.widgets.autocomplete.view.TextIndexComboBox;
import csplugins.widgets.slider.JRangeSliderExtended;
import prefuse.data.query.NumberRangeModel;
import prefuse.util.ui.JRangeSlider;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * Quick Find UI Panel.
 *
 * @author Ethan Cerami.
 */
public class QuickFindPanel extends JPanel {
    private TextIndexComboBox comboBox;
    private JButton configButton;
    private JRangeSliderExtended rangeSlider;
    private NumberRangeModel rangeModel;
    private JLabel label;
    private static final String SEARCH_STRING = "Search:  ";
    private static final String SELECT_STRING = "Select:  ";

    /**
     * Constructor.
     */
    public QuickFindPanel() {
        //  Must use BoxLayout, as we want to control width
        //  of all components.
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        comboBox = createTextIndexComboBox();
        configButton = createConfigButton();
        label = createSearchLabel();
        rangeModel = new NumberRangeModel(0.0, 0.0, 0.0, 0.0);
        rangeSlider = createSlider(rangeModel, comboBox);

        add(label);
        add(comboBox);
        add(rangeSlider);
        add(configButton);

        //  Add Right Buffer, to prevent config button from occassionally
        //  being partially obscured.
        add(Box.createHorizontalStrut(5));
    }

    /**
     * Sets Current Index.
     *
     * @param index Generic Index Object.
     */
    public void setIndex(GenericIndex index) {
        if (index instanceof TextIndex) {
            comboBox.setVisible(true);
            rangeSlider.setVisible(false);
            label.setText(SEARCH_STRING);
            comboBox.setTextIndex((TextIndex) index);
        } else {
            NumberIndex numberIndex = (NumberIndex) index;
            rangeModel.setMinValue(numberIndex.getMinimumValue());
            rangeModel.setMaxValue(numberIndex.getMaximumValue());
            rangeModel.setLowValue(numberIndex.getMinimumValue());
            rangeModel.setHighValue(numberIndex.getMinimumValue());
            comboBox.setVisible(false);
            rangeSlider.setVisible(true);
            label.setText(SELECT_STRING);
        }
    }

    /**
     * No Network Current Available.
     */
    public void noNetworkLoaded() {
        disableAllQuickFindButtons();
        comboBox.setToolTipText ("Please select or load a network");
        rangeSlider.setToolTipText("Please select or load a network");
    }

    /**
     * Indexing Operating in Progress.
     */
    public void indexingInProgress() {
        disableAllQuickFindButtons();
        comboBox.setToolTipText ("Indexing network.  Please wait...");
        rangeSlider.setToolTipText ("Indexing network.  Please wait...");
    }

    /**
     * Gets the TextIndexComboBox Widget.
     *
     * @return TextIndexComboBox Widget.
     */
    public TextIndexComboBox getTextIndexComboBox () {
        return comboBox;
    }

    /**
     * Gets the Range Slider Widget.
     *
     * @return JRangeSliderExtended Object.
     */
    public JRangeSliderExtended getSlider() {
        return this.rangeSlider;
    }

    /**
     * Disables all Quick Find Buttons.
     */
    private void disableAllQuickFindButtons() {
        comboBox.removeAllText();
        comboBox.setEnabled(false);
        rangeSlider.setEnabled(false);
        comboBox.setVisible(true);
        rangeSlider.setVisible(false);
        configButton.setEnabled(false);
    }

    /**
     * Enables all Quick Find Buttons.
     */
    public void enableAllQuickFindButtons() {
        comboBox.setToolTipText("Enter search string");
        rangeSlider.setToolTipText("Select range");
        comboBox.setEnabled(true);
        rangeSlider.setEnabled(true);
        configButton.setEnabled(true);
    }

    /**
     * Creates Configure QuickFind Button.
     *
     * @return JButton Object.
     */
    private JButton createConfigButton() {
        URL configIconUrl = QuickFindPlugIn.class.getResource
                ("resources/config.png");
        ImageIcon configIcon = new ImageIcon(configIconUrl,
                "Configure search options");
        JButton button = new JButton(configIcon);
        button.setToolTipText("Configure search options");
        button.setEnabled(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new QuickFindConfigDialog();
            }
        });
        button.setBorderPainted(false);
        return button;
    }

    /**
     * Creates TextIndex ComboBox.
     *
     * @return TextIndexComboBox Object.
     */
    private TextIndexComboBox createTextIndexComboBox() {
        TextIndexComboBox box = null;
        try {
            TextIndex textIndex = IndexFactory.createDefaultTextIndex(QuickFind.INDEX_NODES);
            box = ComboBoxFactory.createTextIndexComboBox
                    (textIndex, 2.0);
            box.setEnabled(false);

            //  Set Size of ComboBox Display, based on # of specific chars
            box.setPrototypeDisplayValue("01234567");
            box.setToolTipText("Please select or load a network to "
                    + "activate search functionality.");

            //  Set Max Size of ComboBox to match preferred size
            box.setMaximumSize(box.getPreferredSize());

            return box;
        } catch (Exception e) {
        }
        return box;
    }

    /**
     * Creates Search Label.
     */
    private JLabel createSearchLabel() {
        JLabel label = new JLabel(SEARCH_STRING);
        label.setBorder(new EmptyBorder(0, 5, 0, 0));
        label.setForeground(Color.GRAY);

        //  Fix width of label
        label.setMaximumSize(label.getPreferredSize());
        return label;
    }

    /**
     * Creates Slider Widget.
     *
     * @param box TextIndexComboBox (used to tweak size of slider).
     * @return JRangeSliderExteneded Object
     */
    private JRangeSliderExtended createSlider(BoundedRangeModel model,
            TextIndexComboBox box) {
        JRangeSliderExtended slider = new JRangeSliderExtended
                (model, JRangeSlider.HORIZONTAL,
                        JRangeSlider.LEFTRIGHT_TOPBOTTOM);

        //  Hide slider range for now.
        slider.setVisible(false);

        //  Create Border
        slider.setBorder(new LineBorder(Color.GRAY, 1));

        //  Box Layout will respect the components' max size.
        //  Therefore set max size to match preferred size.
        Dimension dComboBox = box.getPreferredSize();
        Dimension dSlider = slider.getPreferredSize();

        //  Set RangeSlider to match width of combo box.
        dSlider.width = dComboBox.width;
        dSlider.height = dSlider.height + 3;
        slider.setMaximumSize(dSlider);
        slider.setPreferredSize(dSlider);
        return slider;
    }
}