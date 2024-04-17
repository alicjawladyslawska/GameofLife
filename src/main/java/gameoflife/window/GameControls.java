package gameoflife.window;

import javax.swing.*;
import javax.swing.border.MatteBorder;

import java.awt.*;

/**
 * Controls the game
 */
public class GameControls extends JPanel {

    private final CurrentGame game;
    private final Autoplay autoplay;

    private final JLabel stepLabel = new JLabel("Use File to create or open a game");
    private final JLabel settingsLabel = new JLabel();

    private final JSlider speedSlider;
    private final IconButton speedSliderInc = new IconButton("plus", 16);
    private final IconButton speedSliderDec = new IconButton("minus", 16);

    private final IconButton playBWButton = new IconButton("play-backward", 16);
    private final IconButton playFWButton = new IconButton("play-forward", 16);
    private final IconButton stepBWButton = new IconButton("step-backward", 16);
    private final IconButton stepFWButton = new IconButton("step-forward", 16);

    private static final int SPEED_MIN = 50;
    private static final int SPEED_MAX = 500;

    /**
     * Create the game controls that lay at the bottom of the screen.
     * 
     * @param game The game to be controlled
     */
    public GameControls(CurrentGame game) {
        super();
        this.game = game;
        this.autoplay = new Autoplay(game);
        new Thread(this.autoplay).start();

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Upper panel: contains play/pause and step buttons
        JPanel upper = new JPanel();
        upper.setLayout(new BoxLayout(upper, BoxLayout.X_AXIS));

        stepBWButton.setToolTipText("Step Back");
        upper.add(stepBWButton);

        upper.add(playBWButton);
        upper.add(playFWButton);

        stepFWButton.setToolTipText("Step Forward");
        upper.add(stepFWButton);

        playBWButton.addActionListener(e -> {
            // If the game is not playing in reverse, play it in reverse
            if (!autoplay.isReverse()) {
                autoplay.setReverse(true);
                if (!autoplay.isRunning()) {
                    autoplay.play();
                    refresh();
                }
                return;
            }

            playPause();
        });
        playFWButton.addActionListener(e -> {
            // If the game is playing in reverse, play it forward
            if (autoplay.isReverse()) {
                autoplay.setReverse(false);
                if (!autoplay.isRunning()) {
                    autoplay.play();
                    refresh();
                }
                return;
            }

            playPause();
        });
        stepBWButton.addActionListener(e -> {
            if (game.exists())
                game.stepBack();
        });
        stepFWButton.addActionListener(e -> {
            if (game.exists())
                game.step();
        });

        // Middle panel: contains the speed slider
        JPanel middle = new JPanel();
        middle.setLayout(new FlowLayout());

        speedSlider = new JSlider(SPEED_MIN, SPEED_MAX, autoplay.getInterval());
        speedSlider.setPaintTrack(true);
        speedSlider.setMajorTickSpacing(75);
        speedSlider.setInverted(true);

        speedSlider.addChangeListener(e -> {
            autoplay.setInterval(speedSlider.getValue());
            refresh();
        });

        speedSliderInc.setToolTipText("Increase Speed");
        speedSliderDec.setToolTipText("Decrease Speed");
        speedSliderInc.addActionListener(e -> speedSlider.setValue(speedSlider.getValue() - 75));
        speedSliderDec.addActionListener(e -> speedSlider.setValue(speedSlider.getValue() + 75));

        middle.add(speedSliderDec);
        middle.add(speedSlider);
        middle.add(speedSliderInc);

        // Lower panel: displays status information
        JPanel lower = new JPanel();
        lower.setLayout(new BorderLayout());
  
        lower.add(stepLabel, BorderLayout.WEST);
        lower.add(settingsLabel, BorderLayout.EAST);

        // Set border of this component and lower panel to padded borders with a line on top
        MatteBorder borderMatteTop = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY);

        this.setBorder(BorderFactory.createCompoundBorder(borderMatteTop, BorderFactory.createEmptyBorder(3, 0, 0, 0)));
        lower.setBorder(BorderFactory.createCompoundBorder(borderMatteTop, BorderFactory.createEmptyBorder(3, 6, 3, 6)));

        this.add(upper);
        this.add(middle);
        this.add(lower);
        this.refresh();
    }

    /*
     * Update the controls to reflect the current state of the game.
     */
    public void refresh() {

        this.speedSlider.setToolTipText("Delay: " + speedSlider.getValue() + "ms");

        if (autoplay.isRunning()) {
            if (autoplay.isReverse()) {
                this.playFWButton.setIcon("play-forward");
                this.playBWButton.setIcon("pause");
                this.playBWButton.setToolTipText("Pause");
                this.playFWButton.setToolTipText("Play Forward");

            } else {
                this.playBWButton.setIcon("play-backward");
                this.playFWButton.setIcon("pause");
                this.playFWButton.setToolTipText("Pause");
                this.playBWButton.setToolTipText("Play Backward");
            }
        } else {
            this.playBWButton.setIcon("play-backward");
            this.playFWButton.setIcon("play-forward");
            this.playBWButton.setToolTipText("Play Backward");
            this.playFWButton.setToolTipText("Play Forward");
        }

        this.speedSliderInc.setEnabled(autoplay.getInterval() > SPEED_MIN);
        this.speedSliderDec.setEnabled(autoplay.getInterval() < SPEED_MAX);

        if (!game.exists()) {
            return;
        }

        long step = game.current.getStep();

        this.stepLabel.setText(String.format(
            "%s Step #%d",
            autoplay.isRunning() ? "▶️" : "⏹︎",
            step
        ));
        this.settingsLabel.setText(String.format(
            "SA: %d–%d • BA: %d • %d×%d %s",
            game.current.getSettings().minNeighbors(),
            game.current.getSettings().maxNeighbors(),
            game.current.getSettings().neededNeighbors(),
            game.current.getSettings().width(),
            game.current.getSettings().height(),
            game.current.getSettings().toroidal() ? "Toroidal" : "Non-Toroidal"
        ));

        if (step == 0) {
            this.playBWButton.setEnabled(false);
            this.stepBWButton.setEnabled(false);
        } else {
            this.playBWButton.setEnabled(true);
            this.stepBWButton.setEnabled(true);
        }
    }

    /**
     * Toggle the game's play state
     */
    private void playPause() {
        if (this.autoplay.isRunning()) {
            this.autoplay.pause();
        } else {
            this.autoplay.play();
        }
        this.refresh();
    }

    public void pause() {
        if (this.autoplay.isRunning()) {
            this.autoplay.pause();
            this.refresh();
        }
    }

    /**
     * A button that has an icon
     */
    private static class IconButton extends JButton {
        /**
         * The size of the icon in pixels
         */
        private final int size;

        /**
         * @param name The name of the icon in the icons folder
         * @param size The size of the icon in pixels
         */
        public IconButton(String name, int size) {
            super();
            this.size = size;
            setIcon(name);
        }

        public void setIcon(String name) {
            // Get the icon from the icons folder, relative to the gameoflife package
            ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("gameoflife/window/icons/" + name + ".png"));

            // Rescale and set the icon
            icon.setImage(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
            this.setIcon(icon);
        }
    }

}
