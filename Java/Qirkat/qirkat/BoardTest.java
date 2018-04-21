package qirkat;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/** Tests of the Board class.
 *  @author Wenqu Wang
 */
public class BoardTest {

    private static final String INIT_BOARD =
        "  b b b b b\n  b b b b b\n  b b - w w\n  w w w w w\n  w w w w w";

    private static final String[] GAME1 =
    { "c2-c3", "c4-c2",
      "c1-c3", "a3-c1",
      "c3-a3", "c5-c4",
      "a3-c5-c3"
    };

    private static final String[] GAME4 =
    { "b2-b1"
    };

    private static final String GAME1_BOARD =
        "  b b - b b\n  b - - b b\n  - - w w w\n  w - - w w\n  w w b w w";


    private static void makeMoves(Board b, String[] moves) {
        for (String s : moves) {
            b.makeMove(Move.parseMove(s));
        }
    }

    @Test
    public void testInit1() {
        Board b0 = new Board();
        assertEquals(INIT_BOARD, b0.toString());
        assertEquals(PieceColor.BLACK, b0.get(15));

    }

    @Test
    public void testMoves1() {
        Board b0 = new Board();
        makeMoves(b0, GAME1);
        assertEquals(GAME1_BOARD, b0.toString());
    }



    @Test
    public void testGameover() {
        Board b0 = new Board();
        b0.setPieces("------b-------------w----", PieceColor.BLACK);
        makeMoves(b0, GAME4);
        assertEquals(true, b0.gameOver());
    }

    @Test
    public void testGetMoves() {
        Board b0 = new Board();
        ArrayList<Move> a = new ArrayList<>();
        b0.getMoves(a, 13);
        assertEquals(1, a.size());
        assertEquals("d3-c3", a.get(0).toString());
    }



}
