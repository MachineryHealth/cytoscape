package cytoscape.task.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Common UI element for displaying errors and stack traces.
 */
public class JErrorPanel extends JPanel {
    /**
     * The Error Object
     */
    private Throwable t;

    /**
     * A Human Readable Error Message
     */
    private String userErrorMessage;

    /**
     * Flag to Show/Hide Error Details.
     */
    private boolean showDetails = false;

    /**
     * Show/Hide Details Button.
     */
    private JButton detailsButton;

    /**
     * Scroll Pane used to display Stack Trace Elements.
     */
    private JScrollPane detailsPane;

    /**
     * JDialog Owner.
     */
    private JDialog owner;

    private static final String SHOW_TEXT = "Show Error Details";
    private static final String HIDE_TEXT = "Hide Error Details";

    /**
     * Private Constructor.
     *
     * @param owner            Window owner.
     * @param t                Throwable Object. May be null.
     * @param userErrorMessage User Readable Error Message. May be null.
     */
    JErrorPanel(JDialog owner, Throwable t, String userErrorMessage) {
        if (owner == null) {
            throw new IllegalArgumentException("owner parameter is null.");
        }
        this.owner = owner;
        this.t = t;
        this.userErrorMessage = userErrorMessage;
        initUI();
    }

    /**
     * Initializes UI.
     */
    private void initUI() {
        //  Use  Border Layout
        setLayout(new BorderLayout());

        //  Create North Panel with Error Message and Button.
        JPanel northPanel = createNorthPanel();
        add(northPanel, BorderLayout.NORTH);

        //  Create Center Panel with Error Details.
        JScrollPane centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        //  Repack and validate the owner
        owner.pack();
        owner.validate();
    }

    /**
     * Creates North Panel with Error Message and Details Button.
     *
     * @return JPanel Object.
     */
    private JPanel createNorthPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        if (userErrorMessage == null) {
            userErrorMessage = new String("An Error Has Occurred.  "
                    + "Please try again.");
        }

        //  Add an Error Icon
        //      Icon icon = UIManager.getIcon("OptionPane.errorIcon");
        //      JLabel l = new JLabel(icon);
        //      l.setAlignmentY(Component.TOP_ALIGNMENT);
        //      panel.add(l);

        //      Create Left Margin
        //      panel.add(Box.createHorizontalStrut(10));

        //  Add Error Message with Custom Font Properties
        JLabel errorLabel = new JLabel(StringUtils.truncateOrPadString
                ("Error:  " + userErrorMessage));
        errorLabel.setForeground(Color.BLUE);
        Font font = errorLabel.getFont();
        errorLabel.setFont(new Font(font.getFamily(), Font.BOLD,
                font.getSize()));
        errorLabel.setAlignmentY(Component.TOP_ALIGNMENT);
        errorLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(errorLabel);

        //  Conditionally Add Details Button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1));
        buttonPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        conditionallyAddDetailsButton(buttonPanel);

        //  Create a Close Button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                owner.dispose();
            }
        });
        buttonPanel.add(closeButton);
        panel.add(buttonPanel);

        return panel;
    }

    /**
     * Creates Center Panel with Error Details.
     *
     * @return JScrollPane Object.
     */
    private JScrollPane createCenterPanel() {
        detailsPane = new JScrollPane();

        if (t != null && t.getStackTrace() != null) {

            //  Get Stack Trace
            StackTraceElement ste[] = t.getStackTrace();

            //  Create a Tree of Stack Trace Elements
            StringBuffer rootBuffer = new StringBuffer(t.getClass().getName());
            if (t.getMessage() != null) {
                rootBuffer.append(":  " + t.getMessage());
            }
            DefaultMutableTreeNode top =
                    new DefaultMutableTreeNode(rootBuffer.toString());

            //  Create Individual Nodes in JTree
            DefaultMutableTreeNode current = top;
            for (int i = 0; i < ste.length; i++) {
                DefaultMutableTreeNode node =
                        new DefaultMutableTreeNode(ste[i]);
                current.add(node);
                current = node;
            }

            //  Create a JTree Object
            JTree tree = new JTree(top);

            //  Open all Nodes
            tree.scrollPathToVisible(new TreePath(current.getPath()));
            tree.setBorder(new EmptyBorder(4, 10, 10, 10));
            detailsPane.setViewportView(tree);
            detailsPane.setPreferredSize(new Dimension(10, 150));

        }
        //  By default, do not show
        detailsPane.setVisible(false);

        return detailsPane;
    }

    /**
     * Adds a Show/Hide Details Button.
     *
     * @param panel JPanel Object.
     */
    private void conditionallyAddDetailsButton(JPanel panel) {
        if (t != null && t.getStackTrace() != null) {
            detailsButton = new JButton(SHOW_TEXT);
            detailsButton.addActionListener(new ActionListener() {

                /**
                 * Toggle Show/Hide Error Details.
                 *
                 * @param e ActionEvent.
                 */
                public void actionPerformed(ActionEvent e) {
                    showDetails = !showDetails;
                    detailsPane.setVisible(showDetails);
                    owner.setResizable(showDetails);

                    if (showDetails) {
                        detailsButton.setText(HIDE_TEXT);
                    } else {
                        detailsButton.setText(SHOW_TEXT);
                    }
                    owner.pack();
                    owner.validate();
                }
            });
            detailsButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
            panel.add(detailsButton);
        }
    }
}