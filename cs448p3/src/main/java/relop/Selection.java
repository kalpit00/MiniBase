package relop;

/**
 * The selection operator specifies which tuples to retain under a condition; in
 * Minibase, this condition is simply a set of independent predicates logically
 * connected by OR operators.
 */
public class Selection extends Iterator {

    private Iterator iter = null;
    private Predicate[] preds = null;
    //boolean hasNext;
    Tuple next = null;
    private boolean consumed;
    private boolean isOpen;
  /**
   * Constructs a selection, given the underlying iterator and predicates.
   */
  public Selection(Iterator aIter, Predicate... aPreds) {
    this.schema = aIter.getSchema();
    this.iter = aIter;
    this.preds = aPreds; 
    this.consumed = true;
    this.isOpen = true;
    this.next = null;
  }

  /**
   * Gives a one-line explanation of the iterator, repeats the call on any
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
    this.isOpen = false;
    this.iter.restart();
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
      this.iter.close();
      this.isOpen = false;
    }
  }

  /**
   * Returns true if there are more tuples, false otherwise.
   */
  public boolean hasNext() {
    //Your code here
    if (this.next != null) {
      return true;
    }

    while (this.iter.hasNext()) {
      if (this.preds.length == 0) {
        return true;
      }

      this.next = iter.getNext();

      for (int i = 0; i < this.preds.length; i++) {
        if (this.preds[i].evaluate(this.next)) {
          return true;
        }
      }
    }

    this.next = null;
    return false;
  }

  /**
   * Gets the next tuple in the iteration.
   * 
   * @throws IllegalStateException if no more tuples
   */
  public Tuple getNext() {
    //Your code here
    if (this.next != null) {
      Tuple tuple = this.next;
      this.next = null;
      return tuple;
    }

    while (this.iter.hasNext()) {
      Tuple tuple = this.iter.getNext();
      if (this.preds.length == 0) {
        return tuple;
      }

      this.next = iter.getNext();

      for (int i = 0; i < this.preds.length; i++) {
        if (this.preds[i].evaluate(this.next)) {
          return tuple;
        }
      }
    }
    return null;
  }

} // public class Selection extends Iterator
