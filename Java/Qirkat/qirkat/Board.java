package qirkat;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import static qirkat.PieceColor.*;
import static qirkat.Move.*;

/** A Qirkat board.   The squares are labeled by column (a char value between
 *  'a' and 'e') and row (a char value between '1' and '5'.
 *
 *  For some purposes, it is useful to refer to squares using a single
 *  integer, which we call its "linearized index".  This is simply the
 *  number of the square in row-major order (with row 0 being the bottom row)
 *  counting from 0).
 *
 *  Moves on this board are denoted by Moves.
 *  @author Wenqu Wang
 */
class Board extends Observable {

    /** A new, cleared board at the start of the game. */
    Board() {
        _board = new PieceColor[MAX_INDEX + 1];
        _direction = new int[]
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        clear();
    }

    /** A copy of B. */
    Board(Board b) {
        internalCopy(b);
    }

    /** Return a constant view of me (allows any access method, but no
     *  method that modifies it). */
    Board constantView() {
        return this.new ConstantBoard();
    }

    /** Clear me to my starting state, with pieces in their initial
     *  positions. */
    void clear() {
        _whoseMove = WHITE;
        _gameOver = false;
        _prev = new ArrayList<>();
        _direction = new int[]
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        String initial = "wwwwwwwwwwbb-wwbbbbbbbbbb";
        this.setPieces(initial, _whoseMove);
        _prev.add(new Board(this));

        setChanged();
        notifyObservers();
    }

    /** Copy B into me. */
    void copy(Board b) {
        internalCopy(b);
    }

    /** Copy B into me. */
    private void internalCopy(Board b) {
        _board = b._board.clone();
        _whoseMove = b.whoseMove();
        _prev = b._prev;
        _direction = b._direction.clone();
        _gameOver = b.gameOver();
    }

    /** Set my contents as defined by STR.  STR consists of 25 characters,
     *  each of which is b, w, or -, optionally interspersed with whitespace.
     *  These give the contents of the Board in row-major order, starting
     *  with the bottom row (row 1) and left column (column a). All squares
     *  are initialized to allow horizontal movement in either direction.
     *  NEXTMOVE indicates whose move it is.
     */
    void setPieces(String str, PieceColor nextMove) {
        if (nextMove == EMPTY || nextMove == null) {
            throw new IllegalArgumentException("bad player color");
        }
        str = str.replaceAll("\\s", "");
        if (!str.matches("[bw-]{25}")) {
            throw new IllegalArgumentException("bad board description");
        }


        for (int k = 0; k < str.length(); k += 1) {
            _direction[k] = 0;
            switch (str.charAt(k)) {
            case '-':
                set(k, EMPTY);
                break;
            case 'b': case 'B':
                set(k, BLACK);
                break;
            case 'w': case 'W':
                set(k, WHITE);
                break;
            default:
                break;
            }
        }

        _whoseMove = nextMove;

        setChanged();
        notifyObservers();
    }

    /** Return true iff the game is over: i.e., if the current player has
     *  no moves. */
    boolean gameOver() {
        return _gameOver;
    }

    /** Return the current contents of square C R, where 'a' <= C <= 'e',
     *  and '1' <= R <= '5'.  */
    PieceColor get(char c, char r) {
        assert validSquare(c, r);
        return get(index(c, r));
    }

    /** Return the current contents of the square at linearized index K. */
    PieceColor get(int k) {
        assert validSquare(k);
        return _board[k];
    }

    /** Set get(C, R) to V, where 'a' <= C <= 'e', and
     *  '1' <= R <= '5'. */
    private void set(char c, char r, PieceColor v) {
        assert validSquare(c, r);
        set(index(c, r), v);
    }

    /** Set get(K) to V, where K is the linearized index of a square. */
    private void set(int k, PieceColor v) {
        assert validSquare(k);
        _board[k] = v;
    }

    /** Return true iff MOV is legal on the current board. */
    boolean legalMove(Move mov) {
        if (mov == null) {
            return true;
        }
        if (jumpPossible() && checkJump(mov, false)) {
            return true;
        } else if (!jumpPossible() && !mov.isJump()) {
            if (get(mov.fromIndex()) == whoseMove() && get(mov.toIndex())
                    == EMPTY) {
                if (!(mov.isRightMove() && _direction[mov.fromIndex()] == -1)
                        && !(mov.isLeftMove()
                        && _direction[mov.fromIndex()] == 1)) {
                    if (whoseMove() == WHITE && mov.row0() != '5'
                            && mov.row0() <= mov.row1()) {
                        return true;
                    } else if (whoseMove() == BLACK && mov.row0() != '1'
                            && mov.row0() >= mov.row1()) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    /** Return a list of all legal moves from the current position. */
    ArrayList<Move> getMoves() {
        ArrayList<Move> result = new ArrayList<>();
        getMoves(result);
        return result;
    }

    /** Add all legal moves from the current position to MOVES. */
    void getMoves(ArrayList<Move> moves) {
        if (gameOver()) {
            return;
        }
        if (jumpPossible()) {
            for (int k = 0; k <= MAX_INDEX; k += 1) {
                getJumps(moves, k);
            }
        } else {
            for (int k = 0; k <= MAX_INDEX; k += 1) {
                getMoves(moves, k);
            }
        }
    }

    /** Add all legal non-capturing moves from the position
     *  with linearized index K to MOVES. */
    public void getMoves(ArrayList<Move> moves, int k) {
        char col0 = col(k);
        char row0 = row(k);
        PieceColor curr = get(k);
        if (!get(k).equals(whoseMove())) {
            return;
        }
        if (curr.equals(WHITE)) {
            if (row0 == '5') {
                return;
            }
        } else {
            if (row0 == '1') {
                return;
            }
        }
        if (_direction[k] != 1 && k % 5 != 0
                && get(k - 1) == EMPTY) {
            moves.add(move(col(k), row(k), col(k - 1), row(k - 1)));
        }
        if (_direction[k] != -1 && k % 5 != 4
                && get(k + 1) == EMPTY) {
            moves.add(move(col(k), row(k), col(k + 1), row(k + 1)));
        }
        if (curr == WHITE) {
            if (k / 5 != 4 && get(k + 5) == EMPTY) {
                moves.add(move(col(k), row(k), col(k + 5), row(k + 5)));
            }
            if (k % 2 == 0) {
                if (k / 5 < 4 && k % 5 < 4 && get(k + 6) == EMPTY) {
                    moves.add(move(col0, row0, col(k + 6), row(k + 6)));
                }
                if (k / 5 < 4 && k % 5 > 0 && get(k + 4) == EMPTY) {
                    moves.add(move(col0, row0, col(k + 4), row(k + 4)));
                }
            }
        } else {
            if (k / 5 != 0 && get(k - 5) == EMPTY) {
                moves.add(move(col(k), row(k), col(k - 5), row(k - 5)));
            }
            if (k % 2 == 0) {
                if (k / 5 > 0 && k % 5 > 0 && get(k - 6) == EMPTY) {
                    moves.add(move(col0, row0, col(k - 6), row(k - 6)));
                }
                if (k / 5 > 0 && k % 5 < 4 && get(k - 4) == EMPTY) {
                    moves.add(move(col0, row0, col(k - 4), row(k - 4)));
                }
            }
        }
    }

    /** Add all legal captures from the position with linearized index K
     *  to MOVES. */
    private void getJumps(ArrayList<Move> moves, int k) {
        for (int i = -2; i <= 2; i += 2) {
            if (k % 5 + i <= 4 && k % 5 + i >= 0) {
                for (int j = -2; j <= 2; j += 2) {
                    if (k / 5 + j <= 4 && k / 5 + j >= 0) {
                        Move mov = move(col(k), row(k), (char) (col(k) + i)
                                , (char) (row(k) + j));
                        jumpAdder(moves, mov);
                    }
                }
            }
        }
    }

    /** helper method for adding a move to an ArrayList.
     * M is the ArrayList to be modified
     * MOV is the move to be added in the list*/
    private void jumpAdder(ArrayList<Move> m, Move mov) {
        if (checkJump(mov, true)) {
            int start = mov.fromIndex();
            int middle = mov.jumpedIndex();
            int end = mov.toIndex();
            Board b1 = new Board();
            b1.setPieces(this.toString1(), _whoseMove);
            PieceColor curr = b1.get(start);
            b1.set(start, EMPTY);
            b1.set(middle, EMPTY);
            b1.set(end, curr);
            if (!b1.jumpPossible(end)) {
                m.add(mov);
            } else {
                ArrayList<Move> nextJump = new ArrayList<>();
                b1.getJumps(nextJump, end);
                for (Move mo : nextJump) {
                    m.add(move(mov, mo));
                }
            }
        }

    }


    /** Return true iff MOV is a valid jump sequence on the current board.
     *  MOV must be a jump or null.  If ALLOWPARTIAL, allow jumps that
     *  could be continued and are valid as far as they go.  */
    boolean checkJump(Move mov, boolean allowPartial) {
        int start = mov.fromIndex();
        int middle = mov.jumpedIndex();
        int end = mov.toIndex();
        PieceColor curr = get(start);
        PieceColor jumped = get(middle);
        PieceColor destination = get(end);
        if (whoseMove() != curr || !mov.isJump()) {
            return false;
        }
        if (start % 2 == 1 && mov.isDiagonalMove()) {
            return false;
        }
        if (destination == EMPTY && jumped == curr.opposite()) {
            if (!allowPartial) {
                Board b1 = new Board(this);
                b1.set(end, curr);
                b1.set(middle, EMPTY);
                b1.set(start, EMPTY);
                if (mov.jumpTail() == null) {
                    return !b1.jumpPossible(end);
                } else {
                    return b1.checkJump(mov.jumpTail(), false);
                }
            } else {
                return true;
            }
        }
        return false;
    }

    /** Return true iff a jump is possible for a piece at position C R. */
    boolean jumpPossible(char c, char r) {
        return jumpPossible(index(c, r));
    }

    /** Return true iff a jump is possible for a piece at position with
     *  linearized index K. */
    boolean jumpPossible(int k) {
        for (int i = -2; i <= 2; i += 2) {
            if (col(k) + i <= 'e' && col(k) + i >= 'a') {
                for (int j = -2; j <= 2; j += 2) {
                    if (row(k) + j <= '5' && row(k) + j >= '1') {
                        Move mov = move(col(k), row(k), (char) (col(k) + i)
                                , (char) (row(k) + j));
                        if (checkJump(mov, true)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /** Return true iff a jump is possible from the current board. */
    boolean jumpPossible() {
        for (int k = 0; k <= MAX_INDEX; k += 1) {
            if (jumpPossible(k)) {
                return true;
            }
        }
        return false;
    }

    /** Return the color of the player who has the next move.  The
     *  value is arbitrary if gameOver(). */
    PieceColor whoseMove() {
        return _whoseMove;
    }

    /** Perform the move C0R0-C1R1. Assumes that legalMove(C0, R0, C1, R1). */
    void makeMove(char c0, char r0, char c1, char r1) {
        makeMove(Move.move(c0, r0, c1, r1, null));
    }

    /** Make the multi-jump C0 R0-C1 R1..., where NEXT is C1R1....
     *  Assumes the result is legal. */
    void makeMove(char c0, char r0, char c1, char r1, Move next) {
        makeMove(Move.move(c0, r0, c1, r1, next));
    }

    /** Make the Move MOV on this Board, assuming it is legal. */
    void makeMove(Move mov) {
        assert legalMove(mov);
        if (mov == null) {
            _prev.add(new Board(this));
            _whoseMove = _whoseMove.opposite();
            return;
        } else {
            set(mov.fromIndex(), EMPTY);
            set(mov.toIndex(), whoseMove());
            if (mov.isJump()) {
                set(mov.jumpedIndex(), EMPTY);
                _direction[mov.toIndex()] = 0;
                makeMove(mov.jumpTail());
            } else {
                if (mov.isLeftMove()) {
                    _direction[mov.toIndex()] = -1;
                } else if (mov.isRightMove()) {
                    _direction[mov.toIndex()] = 1;
                } else {
                    _direction[mov.toIndex()] = 0;
                }
                _prev.add(new Board(this));
                _whoseMove = _whoseMove.opposite();
            }
        }

        if (!isMove()) {
            _gameOver = true;
        }

        setChanged();
        notifyObservers();
    }

    /** Make the move MOV, but do not add the board into _prev. */
    void makeFakeMove(Move mov) {
        makeMove(mov);
        _prev.remove(_prev.size() - 1);
    }

    /** Undo the last move, if any. */
    void undo() {
        Board b1 = _prev.get(_prev.size() - 1);
        for (int i = 0; i <= MAX_INDEX; i += 1) {
            if (!get(i).equals(b1.get(i))) {
                set(i, b1.get(i));
            }
        }
        if (b1.whoseMove().equals(WHITE)) {
            _whoseMove = WHITE;
        } else {
            _whoseMove = BLACK;
        }
        for (int i = 0; i < MAX_INDEX; i++) {
            _direction[i] = b1._direction[i];
        }
        _prev.remove(_prev.size() - 1);

        setChanged();
        notifyObservers();
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /** Return a text depiction of the board.  If LEGEND, supply row and
     *  column numbers around the edges. */
    String toString(boolean legend) {
        String out = " ";
        for (int i = 4; i >= 0; i--) {
            for (int j = i * 5; j <= i * 5 + 4; j++) {
                out += " ";
                out += _board[j].shortName();
            }
            if (i != 0) {
                out += "\n ";
            }
        }
        return out;
    }
    /** Special toString() that return in the the order of linearized index. */
    String toString1() {
        String out = "";
        for (int i = 0; i <= MAX_INDEX; i += 1) {
            out += _board[i].shortName();
        }

        return out;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Board) {
            Board b = (Board) obj;
            return this.toString().equals(obj.toString());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /** Return true iff there is a move for the current player. */
    private boolean isMove() {
        return !getMoves().isEmpty();
    }

    /** A array storing all the Piece on the board. */
    private PieceColor[] _board;

    /** All previous boards, including the current board. */
    private ArrayList<Board> _prev;

    /** Legal moving directions. */
    private int[] _direction;

    /** Player that is on move. */
    private PieceColor _whoseMove;

    /** Set true when game ends. */
    private boolean _gameOver;

    /** Convenience value giving values of pieces at each ordinal position. */
    static final PieceColor[] PIECE_VALUES = PieceColor.values();

    /** One cannot create arrays of ArrayList<Move>, so we introduce
     *  a specialized private list type for this purpose. */
    private static class MoveList extends ArrayList<Move> {
    }
    /** Return the MAX_INDEX. */
    public int getMaxIndex() {
        return MAX_INDEX;
    }

    /** A read-only view of a Board. */
    private class ConstantBoard extends Board implements Observer {
        /** A constant view of this Board. */
        ConstantBoard() {
            super(Board.this);
            Board.this.addObserver(this);
        }

        @Override
        void copy(Board b) {
            assert false;
        }

        @Override
        void clear() {
            assert false;
        }

        @Override
        void makeMove(Move move) {
            assert false;
        }

        /** Undo the last move. */
        @Override
        void undo() {
            assert false;
        }

        @Override
        public void update(Observable obs, Object arg) {
            super.copy((Board) obs);
            setChanged();
            notifyObservers(arg);
        }
    }

}
