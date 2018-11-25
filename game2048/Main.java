 /** This file contains a SUGGESTION for the structure of your program.  You
 may change any of it, or add additional files to this directory (package),
 as long as you conform to the project specification.  Do not, however,
 modify the contents of the 'gui' subpackage.

 We have indicated parts of the file that you might especially want to
 fill in with "// FIXME"  or "// REPLACE..." comments.  But again,
 you can change just about anything.

 Comments that start with "//" are intended to be removed from your
 solutions. */

package game2048;

import ucb.util.CommandArgs;

import game2048.gui.Game;
import static game2048.Main.Side.*;

/** The main class for the 2048 game.
 *  @author Lois Ho
 */
public class Main {

    /** Size of the board: number of rows and of columns. */
    static final int SIZE = 4;
    /** Number of squares on the board. */
    static final int SQUARES = SIZE * SIZE;

    /** Symbolic names for the four sides of a board. */
    static enum Side { NORTH, EAST, SOUTH, WEST };
    /** 2048 is the winning number.*/
    public static final int WINNINGNUMBER = 2048;

    /** The main program.  ARGS may contain the options --seed=NUM,
     *  (random seed); --log (record moves and random tiles
     *  selected.); --testing (take random tiles and moves from
     *  standard input); and --no-display. */

    public static void main(String... args) {
        CommandArgs options =
            new CommandArgs("--seed=(\\d+) --log --testing --no-display",
                            args);
        if (!options.ok()) {
            System.err.println("Usage: java game2048.Main [ --seed=NUM ] "
                               + "[ --log ] [ --testing ] [ --no-display ]");
            System.exit(1);
        }

        Main game = new Main(options);
        game.clear();

        while (game.play()) {
            /* No action */
        }
        System.exit(0);
    }

    /** A new Main object using OPTIONS as options (as for main). */
    Main(CommandArgs options) {
        boolean log = options.contains("--log"),
            display = !options.contains("--no-display");
        long seed = !options.contains("--seed") ? 0 : options.getLong("--seed");
        _testing = options.contains("--testing");
        _game = new Game("2048", SIZE, seed, log, display, _testing);
    }

    /** Reset the score for the current game to 0 and clear the board. */
    void clear() {
        _score = 0;
        _count = 0;
        _game.clear();
        _game.setScore(_score, _maxScore);
        for (int r = 0; r < SIZE; r += 1) {
            for (int c = 0; c < SIZE; c += 1) {
                _board[r][c] = 0;
            }
        }
    }

    /** Play one game of 2048, updating the maximum score. Return true
     *  iff play should continue with another game, or false to exit. */
    boolean play() {

        setRandomPiece();
        while (true) {
            setRandomPiece();
            if (gameOver()) {
                _game.endGame();
            }

        GetMove:
            while (true) {
                String key = _game.readKey();
                switch (key) {
                case "Up": case "Down": case "Left": case "Right":
                    if (!gameOver() && tiltBoard(keyToSide(key))) {
                        break GetMove;
                    }
                    break;
                case "Quit":
                    return false;
                case "New Game":
                    clear();
                    return true;
                default:
                    break;
                }
            }
        }
    }
    /** Return true iff the current game is over (no more moves
     *  possible). */
    boolean gameOver() {
        if (_count == SQUARES) {
            for (int r = 0; r < SIZE; r += 1) {
                for (int c = 0; c < SIZE - 1; c += 1) {
                    int value1 = _board[r][c];
                    if (value1 == _board[r][c + 1]) {
                        return false;
                    }
                }
            }
            for (int r = 0; r < SIZE - 1; r += 1) {
                for (int c = 0; c < SIZE; c += 1) {
                    int value1 = _board[r][c];
                    if (value1 == _board[r + 1][c]) {
                        return false;
                    }
                }
            }
            if (_maxScore < _score) {
                _maxScore = _score;
            }
            _game.setScore(_score, _maxScore);
            return true;
        }
        return false;
    }

    /** Add a tile to a random, empty position, choosing a value (2 or
     *  4) at random.  Has no effect if the board is currently full. */
    void setRandomPiece() {
        if (_count == SQUARES) {
            return;
        } else {
            int[] place = _game.getRandomTile();
            if (_board[place[1]][place[2]] == 0) {
                _game.addTile(place[0], place[1], place[2]);
                _board[place[1]][place[2]] = place[0];
                _count += 1;
            } else {
                setRandomPiece();
            }
        }
    }
    /** @param board loops through all tiles in board
    @param side transfers to board. */
    void transferring(int[][] board, Side side) {
        for (int r = 0; r < SIZE; r += 1) {
            for (int c = 0; c < SIZE; c += 1) {
                board[r][c] =
                    _board[tiltRow(side, r, c)][tiltCol(side, r, c)];
            }
        }
    }
    /** @param board loops through all tiles in board
    @param side transfer back to board. */
    void transferringBack(int[][] board, Side side) {
        for (int r = 0; r < SIZE; r += 1) {
            for (int c = 0; c < SIZE; c += 1) {
                _board[tiltRow(side, r, c)][tiltCol(side, r, c)]
                    = board[r][c];
            }
        }
    }
    /** @param board loops through all tiles in board
    @param side all odds converted to original. */
    void convertingBoardBack(int[][] board, Side side) {
        for (int c = 0; c < SIZE; c += 1) {
            for (int r = 0; r < SIZE; r += 1) {
                if (board[r][c] % 2 != 0) {
                    board[r][c] -= 1;
                }
            }
        }
    }
    /** Perform the result of tilting the board toward SIDE.
     *  Returns true iff the tilt changes the board. **/
    boolean tiltBoard(Side side) {
        int[][] board = new int[SIZE][SIZE];
        boolean tilted = false;
        transferring(board, side);
        for (int c = 0; c < SIZE; c += 1) {
            for (int r = 0; r < SIZE; r += 1) {
                if (board[r][c] != 0) {
                    int value = board[r][c]; int[] newTile = {value, r, c};
                    int emptyR;
                    for (emptyR = r - 1; emptyR >= 0; emptyR -= 1) {
                        if (board[emptyR][c] != 0) {
                            break;
                        }
                    }
                    if (emptyR != -1 && value == board[emptyR][c]) {
                        int tiltedRow = tiltRow(side, r, c);
                        int tiltedCol = tiltCol(side, r, c);
                        int tiltedAboveRow = tiltRow(side, emptyR, c);
                        int tiltedAboveCol = tiltCol(side, emptyR, c);
                        _game.mergeTile(value, value * 2, tiltedRow, tiltedCol,
                            tiltedAboveRow, tiltedAboveCol);
                        if (value == WINNINGNUMBER) {
                            _game.setScore(_score, _maxScore);
                            gameOver();
                        }
                        _count -= 1;
                        tilted = true;
                        _score += value * 2;
                        _game.setScore(_score, _maxScore);
                        board[emptyR][c] = value * 2 + 1;
                        board[r][c] = 0;
                    } else {
                        if (emptyR + 1 != r) {
                            int[] emptyTile = {emptyR + 1, c};
                            int correctNewRow = tiltRow(side, r, c);
                            int correctNewCol = tiltCol(side, r, c);
                            int correctEmptyRow = tiltRow(side, emptyTile[0],
                                emptyTile[1]);
                            int correctEmptyCol = tiltCol(side, emptyTile[0],
                                emptyTile[1]);
                            _game.moveTile(value, correctNewRow, correctNewCol,
                                correctEmptyRow, correctEmptyCol);
                            tilted = true;
                            board[emptyTile[0]][emptyTile[1]] = board[r][c];
                            board[r][c] = 0;
                        }
                    }
                }
            }
        }
        convertingBoardBack(board, side);
        transferringBack(board, side);
        _game.displayMoves();
        _game.setScore(_score, _maxScore);
        return tilted;
    }

    /** Return the row number on a playing board that corresponds to row R
     *  and column C of a board turned so that row 0 is in direction SIDE (as
     *  specified by the definitions of NORTH, EAST, etc.).  So, if SIDE
     *  is NORTH, then tiltRow simply returns R (since in that case, the
     *  board is not turned).  If SIDE is WEST, then column 0 of the tilted
     *  board corresponds to row SIZE - 1 of the untilted board, and
     *  tiltRow returns SIZE - 1 - C. */
    int tiltRow(Side side, int r, int c) {
        switch (side) {
        case NORTH:
            return r;
        case EAST:
            return c;
        case SOUTH:
            return SIZE - 1 - r;
        case WEST:
            return SIZE - 1 - c;
        default:
            throw new IllegalArgumentException("Unknown direction");
        }
    }

    /** Return the column number on a playing board that corresponds to row
     *  R and column C of a board turned so that row 0 is in direction SIDE
     *  (as specified by the definitions of NORTH, EAST, etc.). So, if SIDE
     *  is NORTH, then tiltCol simply returns C (since in that case, the
     *  board is not turned).  If SIDE is WEST, then row 0 of the tilted
     *  board corresponds to column 0 of the untilted board, and tiltCol
     *  returns R. */
    int tiltCol(Side side, int r, int c) {
        switch (side) {
        case NORTH:
            return c;
        case EAST:
            return SIZE - 1 - r;
        case SOUTH:
            return SIZE - 1 - c;
        case WEST:
            return r;
        default:
            throw new IllegalArgumentException("Unknown direction");
        }
    }

    /** Return the side indicated by KEY ("Up", "Down", "Left",
     *  or "Right"). */
    Side keyToSide(String key) {
        switch (key) {
        case "Up":
            return NORTH;
        case "Down":
            return SOUTH;
        case "Left":
            return WEST;
        case "Right":
            return EAST;
        default:
            throw new IllegalArgumentException("unknown key designation");
        }
    }

    /** Represents the board: _board[r][c] is the tile value at row R,
     *  column C, or 0 if there is no tile there. */
    private final int[][] _board = new int[SIZE][SIZE];

    /** True iff --testing option selected. */
    private boolean _testing;
    /** THe current input source and output sink. */
    private Game _game;
    /** The score of the current game, and the maximum final score
     *  over all games in this session. */
    private int _score, _maxScore;
    /** Number of tiles on the board. */
    private int _count;
}
