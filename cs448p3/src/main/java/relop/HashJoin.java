package relop;

import heap.HeapFile;
import index.HashIndex;
import global.SearchKey;
import global.RID;

import java.util.List;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import global.AttrOperator;

import global.AttrType;
public class HashJoin extends Iterator {

	private Iterator smaller;
	private Iterator larger;
	private Predicate equijoinPredicate;
	private HashTableDup hashTable;

	private int smallerJoinCol;
	private int largerJoinCol;

	// private boolean startJoin = true;
	Tuple leftTuple;

	// boolean variable to indicate whether the pre-fetched tuple is consumed or not
	// private boolean nextTupleIsConsumed;

	// pre-fetched tuple
	private Queue<Tuple> nextTupleBatch;
	// private Tuple nextTuple = null;

	public HashJoin(Iterator aIter1, Iterator aIter2, int aJoinCol1, int aJoinCol2){
		//Your code here
		this.smaller = aIter1;
		this.larger = aIter2;
		this.smallerJoinCol = aJoinCol1;
		this.largerJoinCol = aJoinCol2;
		this.schema = Schema.join(this.smaller.schema, this.larger.schema);
		this.equijoinPredicate = new Predicate(AttrOperator.EQ, AttrType.FIELDNO,
				aJoinCol1, AttrType.FIELDNO, aJoinCol2);

		this.nextTupleBatch = new ArrayDeque<Tuple>();

		// Build the lookup table.
		this.hashTable = new HashTableDup();
		while (this.smaller.hasNext()) {
			Tuple smallerTuple = this.smaller.getNext();
			SearchKey searchKey = new SearchKey(smallerTuple.getField(aJoinCol1));

			this.hashTable.add(searchKey, smallerTuple);
		}
		this.smaller.close();
	}

	@Override
	public void explain(int depth) {
		throw new UnsupportedOperationException("Not implemented");
		//Your code here
	}

	@Override
	public void restart() {
		//Your code here
		this.smaller.restart();
		this.larger.restart();
	}

	@Override
	public boolean isOpen() {
		//Your code here
		if (this.larger.isOpen()) {
			return true;
		}

		return false;
	}

	@Override
	public void close() {
		//Your code here
		if (this.isOpen()) {
			this.smaller.close();
			this.larger.close();
		}
	}

	@Override
	public boolean hasNext() {
		//Your code here
		if (this.nextTupleBatch.size() > 0) {
			// System.out.print("> HashJoin.hasNext : Queue has entries");
			return true;
		}

		while (this.larger.hasNext()) {
			// System.out.println("> HashJoin.hasNext : larger has next");
			Tuple rightTuple = this.larger.getNext();

			SearchKey key = new SearchKey(rightTuple.getField(this.largerJoinCol));
			List<Tuple> smallerMatches = Arrays.asList(this.hashTable.getAll(key));

			for (Tuple small : smallerMatches) {
				// System.out.println("> HashJoin.hasNext : enter for loop with " + small + " " +  rightTuple + " " + this.schema);
				Tuple nextTuple = Tuple.join(small, rightTuple, this.schema);
				// System.out.println("> HashJoin.hasNext : joined into " + nextTuple);
				this.nextTupleBatch.add(nextTuple);
				// System.out.println("> HashJoin.hasNext : added " + nextTuple + "; batch length is now " + this.nextTupleBatch.size());
			}

			if (this.nextTupleBatch.iterator().hasNext()) {
				// System.out.println("> HashJoin.hasNext : New item discovered, exiting");
				return true;
			}
		}

		// System.out.println("> HashJoin.hasNext : Iterator emptied");
		return false;
	}

	@Override
	public Tuple getNext() {
		//Your code here
		if (this.nextTupleBatch.size() > 0) {
			Tuple result = this.nextTupleBatch.remove();
			return result;
		}

		throw new IllegalStateException("Iterator has no more entries");
	}
} // end class HashJoin;
