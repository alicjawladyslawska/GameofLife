package gameoflife.window;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Steps the game (using another thread) on an interval when asked.
 */
public class Autoplay implements Runnable {

    /**
     * Is the autoplay running
     */
    private final AtomicBoolean running = new AtomicBoolean(false);
    /**
     * The interval (in milliseconds) each game step runs.
     */
    private final AtomicInteger interval = new AtomicInteger(275);
    /**
     * Is the game running forwards or backwards.
     */
    private final AtomicBoolean reverse = new AtomicBoolean(false);
    private final CurrentGame game;

    public Autoplay(CurrentGame game) {
        this.game = game;
    }

    @Override
    public void run() {
        try {
            synchronized (this) {
                while(true) {
                    if(game.exists() && running.get()) {
                        if (this.isReverse()) {
                            if (this.game.current.getStep() == 1)
                                this.pause();
                            this.game.stepBack();
                        } else {
                            this.game.step();
                        }

                        Thread.sleep(interval.get());
                    } else {
                            wait();
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setInterval(int millis) {
        this.interval.setPlain(millis);
    }

    /**
     * Determine if game is currently running
     * @return true if game is running, false otherwise
     */
    public boolean isRunning() { return this.running.getPlain(); }

    /**
     * Determine if game is currently in reverse
     * @return true if game is in reverse, false otherwise
     */
    public boolean isReverse() { return this.reverse.getPlain(); }


    /**
     * Get the interval (in milliseconds) of how frequently the game is updated.
     * @return The interval in milliseconds.
     */
    public int getInterval() { return this.interval.getPlain(); }

    /**
     *
     */
    public void pause() {
        this.running.setPlain(false);
    }

    public void play() {
        this.running.setPlain(true);
        synchronized (this) {
            this.notify();
        }
    }

    public void setReverse(boolean reversed) {
        this.reverse.setPlain(reversed);
    }

}
