package db61b;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import ucb.junit.textui;

import static org.junit.Assert.assertEquals;

/** The suite of all JUnit tests for the qirkat package.
 *  @author P. N. Hilfinger
 */
public class UnitTest {
    @Test
    public void testColumns() {
        String[] a = new String[]{"first", "second"};
        Table t1 = new Table(a);
        assertEquals(2, t1.columns());
    }

    @Test
    public void testGetTitle() {
        String[] a = new String[]{"first", "second"};
        Table t1 = new Table(a);
        assertEquals("first", t1.getTitle(0));
    }

    @Test
    public void testFindColumn() {
        String[] a = new String[]{"first", "second"};
        Table t1 = new Table(a);
        assertEquals(0, t1.findColumn("first"));
        assertEquals(-1, t1.findColumn("no"));
    }

    @Test
    public void testSize() {
        String[] a = new String[]{"first", "second"};
        Table t1 = new Table(a);
        assertEquals(0, t1.size());
    }

    @Test
    public void testAdd() {
        String[] a = new String[]{"first", "second"};
        Table t1 = new Table(a);
        String[] v1 = new String[]{"1", "2"};
        assertEquals(true, t1.add(v1));
        assertEquals(false, t1.add(v1));
    }

    @Test
    public void testGet() {
        String[] a = new String[]{"first", "second"};
        Table t1 = new Table(a);
        String[] v1 = new String[]{"1", "2"};
        String[] v2 = new String[]{"3", "4"};
        t1.add(v1);
        t1.add(v2);
        assertEquals("1", t1.get(0, 0));
        assertEquals("3", t1.get(1, 0));
    }

    @Test
    public void testAdd2() {
        String[] a = new String[]{"first", "second"};
        Table t1 = new Table(a);
        String[] b = new String[]{"third", "fourth"};
        Table t2 = new Table(b);
        String[] v1 = new String[]{"1", "2"};
        String[] v2 = new String[]{"3", "4"};
        t1.add(v1);
        t2.add(v2);
        Table[] a1 = new Table[]{t1, t2};
        Column c1 = new Column("first", a1);
        Column c3 = new Column("third", a1);
        List<Column> x = new ArrayList<Column>();
        x.add(c1);
        x.add(c3);
        Integer[] i = new Integer[]{0, 0};
        t1.add(x, i);
        assertEquals("3", t1.get(1, 1));
    }

    @Test
    public void testPrintWrite() {
        String[] a = new String[]{"first", "second"};
        Table t1 = new Table(a);
        String[] v1 = new String[]{"1", "2"};
        String[] v2 = new String[]{"3", "4"};
        t1.add(v1);
        t1.add(v2);
        t1.print();
        t1.writeTable("t1");
    }

    @Test
    public void testReadTable() {
        Table t1 = Table.readTable("t1");
        assertEquals("1", t1.get(0, 0));
        assertEquals("3", t1.get(1, 0));
    }

    @Test
    public void testSelect() {
        String[] a = new String[]{"first", "second"};
        Table t1 = new Table(a);
        String[] v1 = new String[]{"1", "2"};
        String[] v2 = new String[]{"4", "3"};
        t1.add(v1);
        t1.add(v2);
        Table[] a1 = new Table[]{t1};
        Column c1 = new Column("first", a1);
        Column c2 = new Column("second", a1);
        Condition cond = new Condition(c1, ">", c2);
        List<Condition> conds = new ArrayList<>();
        List<String> names = new ArrayList<>();
        conds.add(cond);
        names.add("second");
        Table t2 = t1.select(names, conds);
        assertEquals("3", t2.get(0, 0));
    }


    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] args) {
        System.exit(textui.runClasses(UnitTest.class));
    }
}



