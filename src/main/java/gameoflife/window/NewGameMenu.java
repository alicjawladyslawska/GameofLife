package gameoflife.window;

import gameoflife.GameOfLife;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.util.function.Predicate;

/**
 * The new game popup.
 */
public class NewGameMenu extends JFrame {

    /**
     * The button to submit the current settings and make a new game.
     */
    private final JButton submit = new JButton("Create");

    /**
     * Board settings.
     */
    private final IntField min = new IntField(2),
        max = new IntField(3),
        needed = new IntField(3),
        width = new IntField(25),
        height = new IntField(25);

    /**
     * Create the popup that makes a new game.
     * @param current The current game instance.
     */
    public NewGameMenu(CurrentGame current) {
        super("New...");
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.getRootPane().setDefaultButton(submit); // Allow Enter keypress to submit

        SettingsPanel settingsBoard = new SettingsPanel("Board");

        // Board size dimensions
        JPanel settingsBoardSize = new JPanel();
        settingsBoardSize.setLayout(new BoxLayout(settingsBoardSize, BoxLayout.X_AXIS));
        settingsBoardSize.add(width);
        settingsBoardSize.add(new JLabel("×"));
        settingsBoardSize.add(height);
        settingsBoard.addSettings("Size:", settingsBoardSize);

        JCheckBox toroidalCheckbox = new JCheckBox(null, null, true);

        // Whether the board should be toroidal
        settingsBoard.addSettings("Toroidal?", toroidalCheckbox);

        this.add(settingsBoard);

        SettingsPanel settingsRules = new SettingsPanel("If a cell has n neighbours...");

        // Range of neighbors that can keep a cell alive
        JPanel settingsRulesStayAlive = new JPanel();
        settingsRulesStayAlive.setLayout(new BoxLayout(settingsRulesStayAlive, BoxLayout.X_AXIS));
        settingsRulesStayAlive.add(min);
        settingsRulesStayAlive.add(new JLabel("to"));
        settingsRulesStayAlive.add(max);
        settingsRules.addSettings("Stay Alive:", settingsRulesStayAlive);

        // Number of neighbors needed to revive a cell
        settingsRules.addSettings("Become Alive:", needed);

        this.add(settingsRules);

        // Create submit and cancel buttons
        JPanel actions = new JPanel();
        actions.setLayout(new BorderLayout());

        submit.addActionListener(e -> {

            var min = this.min.getValidInput(i -> i > 0 && i <= 8, "Value must be between 1 and 8");
            var max = this.max.getValidInput(i -> {
                if (i <= 8 && min != null)
                    return min <= i;
                return false;
            }, "Value must be between 1 and 8, and must be at least the minimum");
            var needed = this.needed.getValidInput(i -> {
                if (min != null && max != null)
                    return i >= min && i <= max;
                return true;
            }, "Value must be between the minimum and maximum");
            var width = this.width.getValidInput(i -> i > 0 && i <= 500, "Value must be between 1 and 500");
            var height = this.height.getValidInput(i -> i > 0 && i <= 500, "Value must be between 1 and 500");
            if (min == null || max == null || needed == null || width == null || height == null) return;

            current.setCurrent(new GameOfLife(new GameOfLife.Settings(min, max, needed, width, height, toroidalCheckbox.isSelected())));
            this.setVisible(false);
        });
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> this.setVisible(false));

        actions.add(submit, BorderLayout.LINE_END);
        actions.add(cancel, BorderLayout.LINE_START);
        this.add(actions);



        this.setResizable(false);
        this.setPreferredSize(new Dimension(300, 250));
        this.pack();
    }

    /**
     * A Swing panel that contains a section of the new game popup,
     * containing a grid of settings.
     */
    private static class SettingsPanel extends JPanel {
        private final GridBagConstraints c = new GridBagConstraints();

        SettingsPanel(String title) {
            this.setLayout(new GridBagLayout());
            this.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), title));

            c.ipadx = 6;
            c.ipady = 3;
        }

        void addSettings(String label, JComponent component) {
            c.gridx = 0;
            c.gridy++;
            c.anchor = GridBagConstraints.LINE_END;

            JLabel labelComponent = new JLabel(label);
            labelComponent.setLabelFor(component);
            this.add(labelComponent, c);

            c.gridx = 1;
            c.anchor = GridBagConstraints.LINE_START;
            this.add(component, c);
        }
    }

    /**
     * A Swing text field that automatically runs a method (the IntConsumer)
     * that takes the integer from the text field if it is able to be parsed as an int.
     */
    private static class IntField extends JTextField {
        private final Border defaultBorder = getBorder();

        IntField(int defaultInt) {
            super("" + defaultInt, 2);
            this.setHorizontalAlignment(JTextField.CENTER);
        }

        public Integer getValidInput(Predicate<Integer> check, String errorMessage) {
            var i = getInput();
            if (i == null || !check.test(i)) {
                this.setValid(false, errorMessage);
                return null;
            } else {
                this.setValid(true, null);
                return i;
            }
        }

        Integer getInput() {
            try {
                return Integer.parseInt(this.getText().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }

        void setValid(boolean valid, String errorMessage) {
            if (valid) {
                this.setBorder(defaultBorder);
                this.setToolTipText(null);
            } else {
                this.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
                this.setToolTipText(errorMessage);
            }
        }
    }
}
