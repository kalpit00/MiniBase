package relop;

import global.RID;
import index.BucketScan;
import global.SearchKey;
import heap.HeapFile;
import index.HashIndex;

/**
 * Wrapper for bucket scan, an index access method.
 */
public class IndexScan extends Iterator {

    private HeapFile file = null;
    private BucketScan scan = null;
    private HashIndex index = null;
    private boolean isOpen = false;

  /**
   * Constructs an index scan, given the hash index and schema.
   */
  public IndexScan(Schema schema, HashIndex index, HeapFile file) {
  //Your code here
      this.file = file;
      this.schema = schema;
      this.index = index;
      this.scan = this.index.openScan();
      this.isOpen = true;
  }

  /**
   * Gives a one-line explaination of the iterator, repeats the call on any
   * child iterators, and increases the indent depth along the way.
   */
  public void explain(int depth) {
	  throw new UnsupportedOperationException("Not implemented");
  //Your code here
  }

  /**
   * Restarts the iterator, i.e. as if it were just constructed.
   */
  public void restart() {
  //Your code here
      if (this.isOpen()) {
          scan.close();
          this.isOpen = false;
      }

      this.scan = this.index.openScan();
      this.isOpen = true;
  }

  /**
   * Returns true if the iterator is open; false otherwise.
   */
  public boolean isOpen() {
  //Your code here
      return this.isOpen;
  }

  /**
   * Closes the iterator, releasing any resources (i.e. pinned pages).
   */
  public void close() {
  //Your code here
      if (this.isOpen()) {
          this.scan.close();
          this.scan = null;
          this.isOpen = false;
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
          RID rid = this.scan.getNext();
          byte[] data = this.file.selectRecord(rid);
          Tuple tuple = new Tuple(this.getSchema(), data);
          return tuple;
      }

      return null;
  }

  /**
   * Gets the key of the last tuple returned.
   */
  public SearchKey getLastKey() {
  //Your code here
      return this.scan.getLastKey();
  }

  /**
   * Returns the hash value for the bucket containing the next tuple, or maximum
   * number of buckets if none.
   */
  public int getNextHash() {
  //Your code here
      return this.scan.getNextHash();
  }

} // public class IndexScan extends Iterator
