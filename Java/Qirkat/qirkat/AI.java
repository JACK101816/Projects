package qirkat;

import java.util.ArrayList;

import static qirkat.PieceColor.*;

/** A Player that computes its own moves.
 *  @author Wenqu Wang
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 5;
    /** A position magnitude indicating a win (for white if positive, black
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. */
    AI(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove() {
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        System.out.printf("%s moves %s.%n", myColor().toString()
                , move.toString());
        return move;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        if (myColor() == WHITE) {
            findMove(b, MAX_DEPTH, true, 1, INFTY, -INFTY);
        } else {
            findMove(b, MAX_DEPTH, true, -1, INFTY, -INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        Move best = null;
        int pruned;
        int delta;
        if (depth == 0) {
            return staticScore(board);
        } else {
            ArrayList<Move> possibleMoves = board.getMoves();
            if (possibleMoves.size() == 0) {
                return staticScore(board);
            }
            ArrayList<Move> legalMoves = new ArrayList<>();
            for (Move mov : possibleMoves) {
                Board board1 = new Board(board);
                board1.makeFakeMove(mov);
                if (sense == 1) {
                    delta = findMove(board1, depth - 1, saveMove
                            , -1, alpha, beta);
                    if (delta == beta) {
                        legalMoves.add(mov);
                    } else if (delta > beta) {
                        legalMoves = new ArrayList<>();
                        legalMoves.add(mov);
                        beta = delta;
                    }
                    if (delta >= alpha) {
                        break;
                    }
                } else {
                    delta = findMove(board1, depth - 1, saveMove
                            , 1, alpha, beta);
                    if (delta == alpha) {
                        legalMoves.add(mov);
                        alpha = delta;
                    } else if (delta < alpha) {
                        legalMoves = new ArrayList<>();
                        legalMoves.add(mov);
                        alpha = delta;
                    }
                    if (delta <= beta) {
                        break;
                    }
                }
            }
            if (sense == 1) {
                pruned = beta;
            } else {
                pruned = alpha;
            }
            if (!legalMoves.isEmpty()) {
                best = legalMoves.get(game().nextRandom(
                        Math.max(legalMoves.size() - 1, 1)));
            }
        }
        if (saveMove) {
            _lastFoundMove = best;
        }
        return pruned;
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        if (board.gameOver()) {
            if (board.whoseMove().equals(PieceColor.WHITE)) {
                return WINNING_VALUE;
            } else {
                return -WINNING_VALUE;
            }
        }
        int white = 0;
        int black = 0;
        for (int i = 0; i <= board.getMaxIndex(); i++) {
            if (board().get(i).equals(PieceColor.WHITE)) {
                white += 1;
            }
            if (board().get(i).equals(PieceColor.BLACK)) {
                black += 1;
            }
        }
        return white - black;
    }
}
