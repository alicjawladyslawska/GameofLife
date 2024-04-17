package gameoflife.window;

import gameoflife.GameOfLife;
import static gameoflife.Main.AUTHORS;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

/**
 * The game's menu bar along with all of its functions.
 */
public class MenuBar extends JMenuBar {
    /**
     * The file dropdown menu and the button actions
     */
    private static class FileMenu extends JMenu {

        @SuppressWarnings("ResultOfMethodCallIgnored")
        public FileMenu(CurrentGame game, NewGameMenu newGameMenu) {
            super("File");
            this.setMnemonic('F');

            var newGame = new JMenuItem("New...");
            newGame.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
            newGame.setMnemonic('N');

            var open = new JMenuItem("Open...");
            open.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
            open.setMnemonic('O');

            // open an example file
//            var openExample = new JMenu("Open Example");
//            openExample.setMnemonic('E');

            var save = new JMenuItem("Save...");
            save.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
            save.setMnemonic('S');

            // open the new game popup
            newGame.addActionListener(e -> newGameMenu.setVisible(true));

            // open an open file popup
            open.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Open a Game of Life save");
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.addChoosableFileFilter(new FileNameExtensionFilter("Game of Life saves (*.gol)", "gol"));

                int result = chooser.showOpenDialog(open);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = chooser.getSelectedFile();
                    try {
                        PushbackInputStream input = new PushbackInputStream(new FileInputStream(selectedFile), GameOfLife.FILE_SIGNATURE.length);
                        byte[] b = input.readNBytes(GameOfLife.FILE_SIGNATURE.length);
                        input.unread(b);
                        if (Arrays.equals(b, GameOfLife.FILE_SIGNATURE))
                            game.setCurrent(GameOfLife.decodeFromBytes(input));
                        else
                            game.setCurrent(GameOfLife.decodeFromText(input));
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Problem loading file: " + selectedFile + System.lineSeparator() + "Error: " + ex.getMessage());
                    }
                }

            });

            /*
            
            // add example files to the open example menu
            
            final String EXAMPLES_PATH = getClass().getClassLoader().getResource("gameoflife/resources/examples").getPath();
            final File[] EXAMPLES = new File(EXAMPLES_PATH).listFiles();

            for (File example : EXAMPLES) {
                var exampleItem = new JMenuItem(example.getName());
                exampleItem.addActionListener(e -> {
                    try {
                        File exampleGol = example.listFiles(f -> f.getName().endsWith(".gol"))[0];
                        File exampleDesc = example.listFiles(f -> f.getName().endsWith(".html"))[0];

                        // Set game to example GOL file
                        game.setCurrent(GameOfLife.decodeFromBytes(new FileInputStream(exampleGol)));

                        // Read the description file and display it in a popup
                        Scanner scanner = new Scanner(exampleDesc);
                        StringBuilder desc = new StringBuilder();
                        while (scanner.hasNextLine()) {
                            desc.append(scanner.nextLine()).append(System.lineSeparator());
                        }

                        scanner.close();
                        JOptionPane.showMessageDialog(null, new JLabel(desc.toString()), example.getName(), JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Problem loading example: " + example + System.lineSeparator() + "Error: " + ex.getMessage());
                    }
                });

                openExample.add(exampleItem);
            }

            */

            // open a save file popup
            save.addActionListener(event -> {

                if (!game.exists())
                    return;

                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Save a game of life board");
                chooser.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter full = new FileNameExtensionFilter("Game of Life full saves (*.gol)", "gol");
                chooser.addChoosableFileFilter(full);
                chooser.addChoosableFileFilter(new FileNameExtensionFilter("Game of Life text saves (*.gol)", "gol"));

                int result = chooser.showSaveDialog(save);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    if (!file.getName().endsWith(".gol")) {
                        file = new File(file.getPath() + ".gol");
                    }
                    try {
                        file.createNewFile();
                        FileOutputStream output = new FileOutputStream(file);
                        if (chooser.getFileFilter().equals(full))
                            game.current.encodeIntoBytes(output);
                        else
                            game.current.encodeIntoText(output);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "Problem saving file: " + file + System.lineSeparator() + "Error: " + e.getMessage());
                    }
                }

            });

            this.add(newGame);
            this.add(open);
//            this.add(openExample);
            this.add(save);
        }

    }

    private static class EditMenu extends JMenu {

        public EditMenu(CurrentGame currentGame, GameBoard board, GameControls controls) {
            super("Edit");
            this.setMnemonic(KeyEvent.VK_E);

            var clear = new JMenuItem("Clear");
            clear.setAccelerator(KeyStroke.getKeyStroke("ctrl C"));
            clear.setMnemonic(KeyEvent.VK_C);

            clear.addActionListener(e -> {
                if (currentGame.exists()) {
                    currentGame.current.clear();
                    board.repaint();
                    controls.refresh();
                }
            });
            this.add(clear);
        }

    }

    private static class HelpMenu extends JMenu {
        public HelpMenu() {
            super("Help");

            var howto = new JMenuItem("How to Play");

            howto.addActionListener(ev -> {
                JFrame howtoFrame = new JFrame("How to Play");
                howtoFrame.setLayout(new BorderLayout());
                JLabel rules = new JLabel(RULES_HTML);
                Dimension p = rules.getPreferredSize();
                rules.setPreferredSize(new Dimension(p.width / 2, p.height * 2));
                howtoFrame.add(rules, BorderLayout.CENTER);
                howtoFrame.getRootPane().setBorder(BorderFactory.createEtchedBorder());
                howtoFrame.setLocationRelativeTo(HelpMenu.this);
                howtoFrame.pack();
                howtoFrame.setVisible(true);
            });

            var about = new JMenuItem("About");

            about.addActionListener(ev -> {
                JFrame aboutFrame = new JFrame("About");
                aboutFrame.setLayout(new FlowLayout());
                aboutFrame.add(new JLabel("Authors"));
                aboutFrame.setLocationRelativeTo(HelpMenu.this);
                JPanel authorsPanel = new JPanel();
                authorsPanel.setLayout(new BoxLayout(authorsPanel, BoxLayout.Y_AXIS));
                for (String author : AUTHORS) {
                    authorsPanel.add(new JLabel(author));
                }
                aboutFrame.add(authorsPanel);
                aboutFrame.pack();
                aboutFrame.setVisible(true);
            });

            this.add(howto);
            this.add(about);
        }

        private static final String RULES_HTML = "<html>" +
                "<p>The Game of Life takes place on a grid made up of squares, known as cells. These cells can either be of one of two states: alive or dead.</p>" +
                "<p>By standard rules, the state of a cell is determined by the following:</p>" +
                "<ol><li>Alive cells with less than 2 alive neighbours die.</li>" +
                "<li>Alive cells with greater than 3 alive neighbours die.</li>" +
                "<li>Alive cells with exactly 2 or 3 alive neighbours stay alive.</li>" +
                "<li>Dead cells with exactly 3 alive neighbours become alive.</li></ol>" +
                "<p>Players of the game start with a blank grid, which can be interacted with by left clicking the mouse to toggle the state of a cell.</p>" +
                "<p>The grid can be panned by dragging the mouse while middle or right clicking, and zoomed by scrolling.</p>";

    }



    public MenuBar(CurrentGame game, NewGameMenu newGame, GameBoard board, GameControls controls) {
        this.add(new FileMenu(game, newGame));
        this.add(new EditMenu(game, board, controls));
        this.add(new HelpMenu());
    }

}
