import gameoflife.GameOfLife;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestBoard {

    public static final GameOfLife.Settings DEFUALT_SETTINGS = new GameOfLife.Settings(2, 3, 3, 25, 25, true);

    /**
     * Test if the 3 tall oscillator oscillates.
     */
    @Test
    public void testOscillator() {
        GameOfLife game = new GameOfLife(DEFUALT_SETTINGS);
        game.toggleCell(3, 3);
        game.toggleCell(3, 4);
        game.toggleCell(3, 5);
        game.step();
        // x + y * width
        Assertions.assertTrue(game.getCells().keySet().containsAll(List.of(2 + 4 * 25, 3 + 4 * 25, 4 + 4 * 25)));
        game.step();
        Assertions.assertTrue(game.getCells().keySet().containsAll(List.of(3 + 3 * 25, 3 + 4 * 25, 3 + 5 * 25)));
        game.step();
        Assertions.assertTrue(game.getCells().keySet().containsAll(List.of(2 + 4 * 25, 3 + 4 * 25, 4 + 4 * 25)));
    }

    /**
     * See if a single cell disappears and leaves the board empty
     */
    @Test
    public void testSingleCell() {
        GameOfLife game = new GameOfLife(DEFUALT_SETTINGS);

        game.toggleCell(10, 10);

        Assertions.assertTrue(game.getCells().containsKey(10 + 10 * 25));

        game.step();

        Assertions.assertTrue(game.getCells().isEmpty());
    }

    /**
     * Test cell respawning when stepping backwards
     */
    @Test
    public void testStepBack() {

        GameOfLife game = new GameOfLife(DEFUALT_SETTINGS);

        game.toggleCell(10, 10);

        Assertions.assertTrue(game.getCells().containsKey(10 + 10 * 25));

        game.step();

        Assertions.assertTrue(game.getCells().isEmpty());

        game.stepBack();

        Assertions.assertTrue(game.getCells().containsKey(10 + 10 * 25));

        game.step();
        game.step();

        game.stepBack();
        game.stepBack();

        Assertions.assertTrue(game.getCells().containsKey(10 + 10 * 25));
    }

    /**
     * 3 tall oscillator disappears on a non toroidal board edge.
     */
    @Test
    public void testNonToroidal() {

        GameOfLife game = new GameOfLife(new GameOfLife.Settings(2, 3, 3, 5, 5, false));

        game.toggleCell(4, 0);
        game.toggleCell(4, 1);
        game.toggleCell(4, 2);

        game.step();
        game.step();

        Assertions.assertTrue(game.getCells().keySet().isEmpty());

    }

}
