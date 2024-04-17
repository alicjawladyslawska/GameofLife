package gameoflife.window;

import gameoflife.GameOfLife;


/**
 * Holds the current game.
 */
public class CurrentGame {

    public GameOfLife current;
    private GameControls controls;
    private GameBoard board;
    private final Runnable updateWindow;

    public CurrentGame(Runnable updateWindow) {
        this.updateWindow = updateWindow;
    }

    public void setCurrent(GameOfLife game) {
        System.out.println("Set new game with width " + game.width() + " and height " + game.height());
        this.current = game;
        this.board.resetTransformations();
        updateWindow.run();
        this.refresh();
    }

    public void setUI(GameBoard board, GameControls controls) {
        this.board = board;
        this.controls = controls;
    }

    /**
     * Step forward and update the graphics and controls.
     */
    public void step() {
        synchronized (this) {
            this.current.step();
            refresh();
        }
    }

    /**
     * Step backwards and update the graphics and controls.
     */
    public void stepBack() {
        synchronized (this) {
            this.current.stepBack();
            refresh();
        }
    }

    public void refresh() {
        this.controls.refresh();
        this.board.repaint();
    }

    public void pause() {
        this.controls.pause();
    }


    public boolean exists() {
        return this.current != null;
    }

}
