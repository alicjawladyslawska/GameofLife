package gameoflife;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GameOfLife {

    /**
     * Variables to follow in Conway's game of life.
     * @param minNeighbors
     * @param maxNeighbors
     * @param neededNeighbors
     * @param width The width of the board.
     * @param height
     */
    public record Settings(int minNeighbors, int maxNeighbors, int neededNeighbors, int width, int height, boolean toroidal) implements Serializable {}

    private final Settings settings;
    private long step;

    private HashMap<Integer, Integer> cells = new HashMap<>();

    private record Toggle(int index, boolean state) implements Serializable {}

    private HashMap<Long, List<Toggle>> history = new HashMap<>();

    /**
     * Create a new instance of Conway's Game of Life.
     * @param settings The settings of the gam.
     */
    public GameOfLife(Settings settings) {
        this.settings = settings;
    }

    /**
     * @return The width of the board this game is being played on.
     */
    public int width() {
        return this.settings.width;
    }

    /**
     * @return The height of the board this game is being played on.
     */
    public int height() {
        return this.settings.height;
    }

    /**
     * Toggle a cell at specific coordinates.
     * @param x The x position of the cell (proportionate to the grid).
     * @param y The y position of the cell (proportionate to the grid).
     */
    public void toggleCell(int x, int y) {

        if(x >= width() || y >= height())
            return;

        int index = x + y * this.settings.width;
        if(this.cells.containsKey(index))
            this.cells.remove(index);
        else this.cells.put(index, 0);

        if (!this.history.containsKey(this.step)) {
            this.history.put(this.step, new ArrayList<>());
        }
        this.history.get(this.step).add(new Toggle(index, this.cells.containsKey(index)));
    }

    /**
     * Move forward one step.
     */
    public void step() {

        this.cells.replaceAll((k, v) -> v + 1);

        HashMap<Integer, Integer> next = new HashMap<>();

        /**
         * An object to hold an X and Y variable
         */
        record XY(int x, int y) {

            /**
             * A stream of all the neighbors of this x and y variable.
             * @param toroidal Defines if cell activity on board sides should be toroidal.
             * @param width The width of the board, needed in order to do logic on the sides of the board.
             * @param height The width of the board, needed in order to do logic on the sides of the board.
             * @return A stream of neighbors of this cell.
             */
            Stream<XY> neighbors(boolean toroidal, int width, int height) {
                Stream<XY> base = Stream.of(
                        new XY(x - 1, y - 1),
                        new XY(x - 1, y),
                        new XY(x - 1, y + 1),
                        new XY(x, y - 1),
                        new XY(x, y + 1),
                        new XY(x + 1, y - 1),
                        new XY(x + 1, y),
                        new XY(x + 1, y + 1)
                );
                if (toroidal) {
                    base = base.map(xy -> {
                        int x, y;
                        if (xy.x < 0)
                            x = xy.x + width;
                        else if (xy.x >= width)
                            x = xy.x - width;
                        else x = xy.x;
                        if (xy.y < 0)
                            y = xy.y + height;
                        else if (xy.y >= height)
                            y = xy.y - height;
                        else y = xy.y;
                        return new XY(x, y);
                    });
                } else {
                    base = base.filter(xy -> xy.x >= 0 && xy.x < width && xy.y >= 0 && xy.y < height);
                }
                return base;
            }

            int index(int width) {
                return x + y * width;
            }

            /**
             * A stream of all the neighbors of this x and y variable, including itself.
             * @param toroidal Defines if cell activity on board sides should be toroidal.
             * @param width The width of the board, needed in order to do logic on the sides of the board.
             * @param height The width of the board, needed in order to do logic on the sides of the board.
             * @return A stream of neighbors of this cell, including itself.
             */
            Stream<XY> neighborsAndSelf(boolean toroidal, int width, int height) {
                return Stream.concat(neighbors(toroidal, width, height), Stream.of(this));
            }

        }

        // get all cell positions and their neighbors,
        // count the living neighbors
        // place living and new cells into a new map
        // ignore dead cells as they do not make it into the new map
        this.cells.keySet().stream().map(idx -> new XY(idx % width(), idx / width())).flatMap(xy -> xy.neighborsAndSelf(settings.toroidal, settings.width, settings.height)).distinct().forEach(i -> {
            int index = i.index(width());
            boolean alive = this.cells.containsKey(index);
            long neighbors = i.neighbors(settings.toroidal, settings.width, settings.height).mapToInt(xy1 -> xy1.index(width())).filter(this.cells::containsKey).count();
            if (alive) {
                if (neighbors >= settings.minNeighbors && neighbors <= settings.maxNeighbors) {
                    next.put(index, this.cells.get(index));
                }
            } else {
                if(neighbors == settings.neededNeighbors) {
                    next.put(index, 0);
                }
            }
        });

        this.cells = next;

        this.step++;

    }

    /**
     * Move backwards one step.
     * This calculates each step from the beginning of the game and then saves it into a cache.
     */
    public void stepBack() {
        if (this.step > 0)
            this.stepTo(this.step - 1);
        this.history.remove(this.step + 1);
    }

    /**
     * Cached steps
     */
    private final HashMap<Long, HashMap<Integer, Integer>> stepCache = new HashMap<>();
    /**
     * Maximum cached steps
     */
    private static final int STEP_CACHE_SIZE = 100;

    /**
     * Step to a specific step in the game.
     * @param step The step to go to.
     */
    public void stepTo(long step) {
        if (!stepCache.containsKey(step)) {
            this.step = 0;
            this.cells.clear();
            for (long s = 0; s < step + 1; s++) {
                // get user's cell toggles
                var toggles = this.history.get(s);
                if (toggles != null) {
                    for (var t : toggles) {
                        if (t.state)
                            this.cells.put(t.index, 0);
                        else
                            this.cells.remove(t.index);
                    }
                }
                if (s >= step - STEP_CACHE_SIZE) {
                    stepCache.put(s, new HashMap<>(this.cells));
                }
                if (s != step)
                    this.step();
            }
        } else {
            this.cells = stepCache.remove(step);
            this.step = step;
        }
    }

    public long getStep() {
        return this.step;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public void clear() {
        this.step = 0;
        this.cells.clear();
        this.history.clear();
    }

    public Map<Integer, Integer> getCells() {
        return this.cells;
    }



    public static final byte[] FILE_SIGNATURE = "g22".getBytes(StandardCharsets.UTF_8);

    /**
     * Encode this game into a stream of bytes.
     * @param output A stream of bytes to write to, such as a file.
     * @throws IOException The output stream could not be written to.
     */
    public void encodeIntoBytes(OutputStream output) throws IOException {
        output.write(FILE_SIGNATURE);
        var stream = new ObjectOutputStream(new GZIPOutputStream(output));
        stream.writeObject(this.settings);
        stream.writeLong(this.step);
        stream.writeObject(this.cells);
        stream.writeObject(this.history);
        stream.close();
    }

    /**
     * Encode this game into a string.
     * @param output A stream (could be a file) to write the output string to.
     * @throws IOException The output stream could not be written to.
     */
    public void encodeIntoText(OutputStream output) throws IOException {
        // map every index to an 'o' or a '.' and add newlines to the end of each row.
        var text = IntStream.range(0, width() * height()).mapToObj(i -> {
            String c = this.cells.containsKey(i) ? "o" : ".";
            if (i % width() == width() - 1)
                c = c + "\n";
            return c;
        });
        // fold the stream into a string and write it to the output
        output.write(text.reduce("", (prev, next) -> prev + next).getBytes());
    }

    /**
     * Decode an input into an instance of Conway's game of life.
     * @param input A stream of bytes to read from, such as a file.
     * @return A previously saved game of life instance
     * @throws IOException The stream of bytes contains incorrect data, or could not be read.
     */
    public static GameOfLife decodeFromBytes(InputStream input) throws IOException {
        // check if the input has the correct signature
        var b = input.readNBytes(3);
        if (!Arrays.equals(b, FILE_SIGNATURE)) {
            throw new IOException("Invalid file signature!");
        }
        // unzip and extract objects out of the input
        ObjectInputStream stream = new ObjectInputStream(new GZIPInputStream(input));

        // decode the settings
        GameOfLife.Settings settings;
        try {
            settings = (GameOfLife.Settings) stream.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Could not decode game settings!");
        }

        // decode the step count

        long step;
        try {
            step = stream.readLong();
        } catch (IOException e) {
            throw new IOException("Could not decode step count!");
        }

        // decode the cells

        HashMap<Integer, Integer> cells;
        try {
            cells = (HashMap<Integer, Integer>) stream.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Could not decode cell map!");
        }

        // decode the history, then return the game

        try {
            HashMap<Long, List<GameOfLife.Toggle>> history = (HashMap<Long, List<GameOfLife.Toggle>>) stream.readObject();
            GameOfLife game = new GameOfLife(settings);
            game.step = step;
            game.cells = cells;
            game.history = history;
            stream.close();
            return game;
        } catch (ClassNotFoundException e) {
            throw new IOException("Could not decode toggle history!");

        }

    }

    /**
     * Decode an input (containing a correct string) into an instance of Conway's game of life.
     * @param input A stream of bytes (in string format) to read from, such as a file.
     * @return A previously saved game of life instance
     * @throws IOException The stream of bytes contains incorrect data, or could not be read.
     */
    public static GameOfLife decodeFromText(InputStream input) throws IOException {
        Stream<String> lines = Arrays.stream(new String(input.readAllBytes(), StandardCharsets.UTF_8).split("(\\r\\n|\\r|\\n)"));
        AtomicInteger y = new AtomicInteger(0), width = new AtomicInteger(0);
        Map<Integer, Integer> cells = lines.flatMapToInt(l -> {
            if (l.matches("^.*(o|.).*$")) {
                if (width.getPlain() == 0)
                    width.set(l.length());
                var s = IntStream.range(0, l.length()).filter(r -> l.charAt(r) == 'o').map(x -> x + y.get() * l.length());
                y.getAndIncrement();
                return s;
            } else return IntStream.empty();
        }).mapToObj(i -> Map.entry(i, 0)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        GameOfLife game = new GameOfLife(new Settings(2, 3, 3, width.getPlain(), y.getPlain(), true));
        game.cells = new HashMap<>(cells);
        return game;
    }

}
