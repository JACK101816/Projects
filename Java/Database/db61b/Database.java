
package db61b;

import java.util.HashMap;
/** A collection of Tables, indexed by name.
 *  @author Wenqu Wang */
class Database {

    /** An empty database. */
    public Database() {
        tables =  new HashMap<>();
    }

    /** Return the Table whose name is NAME stored in this database, or null
     *  if there is no such table. */
    public Table get(String name) {
        return tables.get(name);
    }
    /** Set or replace the table named NAME in THIS to TABLE.  TABLE and
     *  NAME must not be null, and NAME must be a valid name for a table. */
    public void put(String name, Table table) {
        if (name == null || table == null) {
            throw new IllegalArgumentException("null argument");
        }
        tables.put(name, table);
    }
    /** Load a table into the current database.
     * @param name */
    public void loadTable(String name) {
        Table t1 = Table.readTable(name);
        put(name, t1);
    }
    /** Tables in the database. */
    private HashMap<String, Table> tables;

}
