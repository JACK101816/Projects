/* Author: Paul N. Hilfinger.  (C) 2008. */

package qirkat;

import org.junit.Test;
import static org.junit.Assert.*;

import static qirkat.Move.*;

/** Test Move creation.
 *  @author Wenqu Wang
 */
public class MoveTest {

    @Test
    public void testMove1() {
        Move m = move('a', '3', 'b', '2');
        assertNotNull(m);
        assertFalse("move should not be jump", m.isJump());
    }

    @Test
    public void testJump1() {
        Move m = move('a', '3', 'a', '5');
        assertNotNull(m);
        assertTrue("move should be jump", m.isJump());
    }

    @Test
    public void testString() {
        assertEquals("a3-b2", move('a', '3', 'b', '2').toString());
        assertEquals("a3-a5", move('a', '3', 'a', '5').toString());
        assertEquals("a3-a5-c3", move('a', '3', 'a', '5',
                                      move('a', '5', 'c', '3')).toString());
    }

    @Test
    public void testParseString() {
        assertEquals("a3-b2", parseMove("a3-b2").toString());
        assertEquals("a3-a5", parseMove("a3-a5").toString());
        assertEquals("a3-a5-c3", parseMove("a3-a5-c3").toString());
        assertEquals("a3-a5-c3-e1", parseMove("a3-a5-c3-e1").toString());
    }
    @Test
    public void testIsLeftMove() {
        assertEquals(false, move('a', '3', 'b', '3').isLeftMove());
        assertEquals(true, move('b', '3', 'a', '3').isLeftMove());
    }
    @Test
    public void testIsRightMove() {
        assertEquals(true, move('a', '3', 'b', '3').isRightMove());
        assertEquals(false, move('b', '3', 'a', '3').isRightMove());
    }
    @Test
    public void testJumpedRow() {
        assertEquals('4', move('a', '3', 'a', '5').jumpedRow());
        assertEquals('3', move('b', '2', 'd', '4').jumpedRow());
        assertEquals('2', move('c', '1', 'a', '3').jumpedRow());
        assertEquals('1', move('c', '1', 'a', '1').jumpedRow());
    }
    @Test
    public void testJumpedCol() {
        assertEquals('b', move('a', '2', 'c', '2').jumpedCol());
        assertEquals('c', move('b', '2', 'd', '4').jumpedCol());
    }
    @Test
    public void testMove() {
        Move m0 = move('e', '5', 'c', '5');
        Move m1 = move('c', '3', 'e', '5', m0);
        Move m2 = move('a', '1', 'c', '3', m1);
        Move m3 = move('c', '5', 'e', '5');
        assertEquals("a1-c3-e5-c5", move(m2, null).toString());
        assertEquals("a1-c3-e5-c5-e5", move(m2, m3).toString());


    }
}
