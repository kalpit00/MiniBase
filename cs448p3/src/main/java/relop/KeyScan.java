package relop;

import global.RID;
import global.SearchKey;
import heap.HeapFile;
import index.HashIndex;
import index.HashScan;


/**
 * Wrapper for hash scan, an index access method.
 */
public class KeyScan extends Iterator {

    private HeapFile file = null; // needed for restart(), getFile();
    private HashScan scan = null;
    private HashIndex index = null;
    private SearchKey key = null;

    /**
   * Constructs an index scan, given the hash index and schema.
   */
  public KeyScan(Schema aSchema, HashIndex aIndex, SearchKey aKey, HeapFile aFile) {
    //Your code here
      this.schema = aSchema;
      this.index = aIndex;
      this.key = aKey;
      this.file = aFile;
      this.scan = this.index.openScan(this.key);
  }

  /**
   * Gives a one-line explanation of the iterator, repeats the call on any
   * child iterators, and increases the indent depth along the way.
   */
  public void explain(int depth) {
	  throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Restarts the iterator, i.e. as if it were just constructed.
   */
  public void restart() {
    //Your code here
      this.close();
      this.scan = this.index.openScan(this.key);
  }

  /**
   * Returns true if the iterator is open; false otherwise.
   */
  public boolean isOpen() {
    //Your code here
      if (this.scan != null) {
          return true;
      }

      return false;
  }

  /**
   * Closes the iterator, releasing any resources (i.e. pinned pages).
   */
  public void close() {
    //Your code here
      if (this.isOpen()) {
          this.scan.close();
          this.scan = null;
      }
  }

  /**
   * Returns true if there are more tuples, false otherwise.
   */
  public boolean hasNext() {
    //Your code here
      if (this.isOpen()) {
          return this.scan.hasNext();
      }

      return false;
  }

  /**
   * Gets the next tuple in the iteration.
   * 
   * @throws IllegalStateException if no more tuples
   */
  public Tuple getNext() {
    //Your code here
      if (this.isOpen()) {
          RID rid = scan.getNext();
          Tuple tuple = new Tuple(this.schema, this.file.selectRecord(rid));

          return tuple;
      }

      throw new IllegalStateException("No more remaining tuples");
  }

} // public class KeyScan extends Iterator
