package gameoflife.window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.HashSet;
import java.util.Map;

/**
 * Draws the game's board and cells.
 */
public class GameBoard extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener {

    /**
     * The size in pixels of each tile.
     */
    private static final int tileSize = 32;

    /**
     * The game instance.
     */
    private final CurrentGame currentGame;

    private double scale = 1.0;
    private int x = 0, y = 0;
    private int[] mouseBoardPos = null;

    // Colors
    private static final Color COLOR_CELL = new Color(0x3b82f6); // blue
    private static final Color COLOR_GRID_1 = new Color(0xe5e7eb); // gray
    private static final Color COLOR_GRID_2 = new Color(0xd1d5db); // darker gray
    private static final Color COLOR_GRID_HOVER = new Color(0, 0, 0, 0.25f); // translucent black
    private static final Color COLOR_GRID_BORDER = Color.GRAY;

    public GameBoard(CurrentGame game, JFrame window) {
        super();
        this.currentGame = game;
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        this.window = window;
    }

//    public int getBoardX() {
//        return (this.getWidth() - this.getBoardWidth()) / 2;
//    }
//
//    public int getBoardY() {
//        return (this.getHeight() - this.getBoardHeight()) / 2;
//    }

    public int getBoardWidth() {
        return currentGame.current.width() * tileSize;
    }

    public int getBoardHeight() {
        return currentGame.current.height() * tileSize;
    }

    /**
     * Convert mouse coordinates to board coordinates
     * @param e The mouse event
     * @return The board coordinates as an array of two integers
     */
    private int[] mouseToBoard(MouseEvent e) {
        int cellX = (int) ((e.getX() / scale - x) / tileSize);
        int cellY = (int) ((e.getY() / scale - y) / tileSize);

        return new int[] {
            Math.min(Math.max(0, cellX), currentGame.current.width() - 1),
            Math.min(Math.max(0, cellY), currentGame.current.height() - 1)
        };
    }

    /**
     * Reset the transformations done to the board view.
     */
    public void resetTransformations() {
        x = 0;
        y = 0;
        scale = this.currentGame.exists() ? this.getPreferredScale() : 1.0;
    }

    /**
     * Get the preferred scale of the board by window size.
     * @return The preferred scale of
     */
    private double getPreferredScale() {
        if (this.getWidth() == 0 || this.getHeight() == 0)
            return 1.0;
        System.out.println(this.getHeight() + " " + this.getBoardHeight());
        int missingW = this.getWidth() - this.getBoardWidth();
        int missingH = this.getHeight() - this.getBoardHeight();
        if (Math.abs(missingW) > Math.abs(missingH)) {
            return missingH >= 0 ? 1.0 : ((double) this.getHeight()) / this.getBoardHeight();
        } else {
            return missingW >= 0 ? 1.0 : ((double) this.getWidth()) / this.getBoardWidth();
        }
    }

    /**
     * Draw the board after the transformations are done.
     */
    private void drawBoard(Graphics2D g) {
        Map<Integer, Integer> cells = currentGame.current.getCells();

        for (int ty = 0; ty < currentGame.current.height(); ty++) {
            for (int tx = 0; tx < currentGame.current.width(); tx++) {
                var cell = cells.get(tx + ty * currentGame.current.width());
                if (cell != null)
                    g.setColor(getCellColor(cell));
                else if ((tx + ty) % 2 == 0)
                    g.setColor(COLOR_GRID_1);
                else
                    g.setColor(COLOR_GRID_2);
                g.fillRect(tx * tileSize, ty * tileSize, tileSize, tileSize);
            }
        }

        // Draw mouse hover feedback
        if (mouseBoardPos != null) {
            g.setColor(COLOR_GRID_HOVER);
            g.fillRect(mouseBoardPos[0] * tileSize, mouseBoardPos[1] * tileSize, tileSize, tileSize);
        }

        // Draw border
        g.setColor(COLOR_GRID_BORDER);
        g.drawRect(0, 0, currentGame.current.width() * tileSize, currentGame.current.height() * tileSize);
    }

    /**
     * Tracks cell toggles during a mouse drag or click so if the cell is dragged over again,
     * or the mouse moves to a different location on the same cell, it won't turn off.
     */
    private final HashSet<Integer> cellsToggled = new HashSet<>();

    /**
     * Toggle a cell on a mouse drag or click.
     * @param e The mouse event needed to get the location of the click.
     */
    private void click(MouseEvent e) {
        if (currentGame.exists()) {
            int width = this.currentGame.current.width(), height = currentGame.current.height();
            int[] p = mouseToBoard(e);
            mouseBoardPos = p;

            if (p[0] < 0 || p[0] >= width || p[1] < 0 || p[1] >= height)
                return;
            int index = p[0] + p[1] * width;
            if (!cellsToggled.contains(index)) {
                cellsToggled.add(index);
                this.currentGame.current.toggleCell(p[0], p[1]);
                this.repaint();
            }
        }
    }

    /**
     * Gradually change the color of a cell as it ages, going from
     * the defined cell color to its complementary color.
     * @param age The age of the cell
     * @return The color of the cell
     */
    private static Color getCellColor(int age) {
        final int MAX_AGE = 50; // Steps until cell reaches its complementary color
        float step = 0.5f / MAX_AGE;

        float[] cellHSB = Color.RGBtoHSB(COLOR_CELL.getRed(), COLOR_CELL.getGreen(), COLOR_CELL.getBlue(), new float[3]);
        float cellHue = (cellHSB[0] + Math.min(age * step, 0.5f)) % 1.0f;

        return new Color(Color.HSBtoRGB(
            cellHue,
            cellHSB[1],
            cellHSB[2]));
    }

    @Override
    public Dimension getPreferredSize() {
        return currentGame.exists() ? new Dimension(currentGame.current.width() * tileSize, currentGame.current.height() * tileSize) : super.getPreferredSize();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();

        if (currentGame.exists()) {
            g.setTransform(new AffineTransform());
            g.scale(scale, scale);
            g.translate(x, y);
            this.drawBoard(g);
        } else {
            g.drawString("No game selected", this.getWidth() / 2 - 50, this.getHeight() / 2);
        }
    }

    private int buttonDown = 0, deltaX = 0, deltaY = 0;

    @Override
    public void mouseDragged(MouseEvent e) {
        switch (buttonDown) {
            case MouseEvent.BUTTON1 -> click(e);
            case MouseEvent.BUTTON2, MouseEvent.BUTTON3 -> {
                this.x += (e.getXOnScreen() - deltaX) / this.scale;
                this.y += (e.getYOnScreen() - deltaY) / this.scale;
                deltaX = e.getXOnScreen();
                deltaY = e.getYOnScreen();
                this.repaint();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseBoardPos = currentGame.exists() ? mouseToBoard(e) : null;
        this.repaint();
    }


    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            currentGame.pause();
            click(e);
        }
        deltaX = e.getXOnScreen();
        deltaY = e.getYOnScreen();
        buttonDown = e.getButton();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        buttonDown = MouseEvent.NOBUTTON;
        this.cellsToggled.clear();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private final JFrame window;

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double diff = e.getWheelRotation() / 10.0;
        this.scale -= diff;
        Point mp = window.getMousePosition();
        this.x += mp.getX() * diff;
        this.y += mp.getY() * diff;

        this.repaint();
    }
}
