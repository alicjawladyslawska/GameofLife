package gameoflife;

import gameoflife.window.*;
import gameoflife.window.MenuBar;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static String[] AUTHORS = new String[] { "220019540", "220004294", "220023971" };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Unable to use Nimbus theme!");
                // If Nimbus is not available, you can set the GUI to another look and feel.
            }

            var window = new JFrame("Game of Life");

            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setLayout(new BorderLayout());
            window.setPreferredSize(new Dimension(400, 500));
            window.setMinimumSize(new Dimension(325, 200));
            window.setLocationRelativeTo(null);

            CurrentGame game = new CurrentGame(window::pack);

            NewGameMenu newGameMenu = new NewGameMenu(game);
            newGameMenu.setLocationRelativeTo(window);

            GameBoard board = new GameBoard(game, window);
            window.add(board, BorderLayout.CENTER);
            var controls = new GameControls(game);
            window.add(controls, BorderLayout.SOUTH);
            game.setUI(board, controls);
            window.addWindowStateListener(e -> {
                if((e.getNewState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
                    board.resetTransformations();
                }
            });

            window.setJMenuBar(new MenuBar(game, newGameMenu, board, controls));
            window.setIconImage(new ImageIcon(Main.class.getResource("window/icons/icon.png")).getImage());

            window.pack();

            window.setVisible(true);

        });
    }
}