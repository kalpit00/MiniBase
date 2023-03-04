package bufmgr;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import diskmgr.DiskMgrException;
import global.Minibase;
import global.Page;
import global.PageId;

/**
 * <h3>Minibase Buffer Manager</h3>
 * The buffer manager reads disk pages into a main memory page as needed. The
 * collection of main memory pages (called frames) used by the buffer manager
 * for this purpose is called the buffer pool. This is just an array of Page
 * objects. The buffer manager is used by access methods, heap files, and
 * relational operators to read, write, allocate, and de-allocate pages.
 */
public class BufMgr {

    // INSTANCE VARIABLES
    // Some of these are accessed during the test cases
	// DO NOT CHANGE THE BELOW INSTANCE VARIABLE NAMES - you should use them as appropriate in your code
	// You may add additional ones as you need to

    /** bufPool: the buffer pool. An array of Page objects */
	private Page[] bufPool = null;

    /** frmDescr: An arracy of FrameDescriptor objects, holding information about the contents of each frame. */
    private FrameDescriptor[] frmDescr = null;

    /** numOfFrames: the number of frames used for the maximum capacity of the buffer pool */
    private int numOfFrames = -1;

	/** replacementPolicy: will be set to FIFO when the constructor is called. */
    private static String replacementPolicy = "";

    /** map: Hash table to track which frame in the buffer a page is in <Key: PageID, Value: Frame> */
    private HashMap<Integer, Integer> pageMap = null;
	//

    /** fifo: queue for FIFO page replacement. All unpinned pages will be stored here, with the first element being
     * the pageID that was unpinned the longest time ago */
	private Queue<Integer> fifo = null;

	private boolean isBfrEmpty = true; // used to check if bufpool is empty or not

	private int next = 0; // keep track of fifo queue pointer

	// END OF REQUIRED INSTANCE VARIABLES

	/**
	 * Resets a FrameDescriptor to the default values with no pageID
	 */
	protected void resetFrameDescriptor(int frameId) {
		resetFrameDescriptor(frameId, -1);
	}

	/**
	 * Resets a FrameDescriptor to the default values with the given pageID
	 */
	protected void resetFrameDescriptor(int frameId, int pageno) {
		frmDescr[frameId].pageno = pageno;
		frmDescr[frameId].pinCount = 0;
		frmDescr[frameId].dirtyBit = false;
	}

	/**
	 * Create the BufMgr object. Allocate pages (frames) for the buffer pool in main
	 * memory and make the buffer manage aware that the replacement policy is
	 * specified by replacerArg (e.g., LH, Clock, LRU, MRU, LIRS, etc.).
	 *
	 * @param numbufs
	 *            number of buffers in the buffer pool
	 * @param lookAheadSize
	 *            number of pages to be looked ahead - can be ignored for this assignment
	 * @param replacementPolicy
	 *            Name of the replacement policy
	 */
	public BufMgr(int numbufs, int lookAheadSize, String replacementPolicy) {
		// we ignore replacementPolicy as there is only one policy implemented in the
		// system
		numOfFrames = numbufs;
		bufPool = new Page[numOfFrames];
		frmDescr = new FrameDescriptor[numOfFrames];
		fifo = new LinkedList<Integer>();
		this.replacementPolicy = replacementPolicy;
		for (int i = 0; i < numOfFrames; i++) {
			bufPool[i] = new Page();
			frmDescr[i] = new FrameDescriptor();
			frmDescr[i].pinCount = 0;
			frmDescr[i].pageno = -1;
			frmDescr[i].dirtyBit = false;
			resetFrameDescriptor(i, -1);
			fifo.add(i);
		}
		pageMap = new HashMap<Integer, Integer>();
	}

	/**
	 * Pin a page. First check if this page is already in the buffer pool. If it is,
	 * increment the pin_count and return a pointer to this page. If the pin_count
	 * was 0 before the call, the page was a replacement candidate, but is no longer
	 * a candidate. If the page is not in the pool, choose a frame (from the set of
	 * replacement candidates) to hold this page, read the page (using the
	 * appropriate method from diskmgr package) and pin it. Also, must write out the
	 * old page in chosen frame if it is dirty before reading new page.(You can
	 * assume that emptyPage==false for this assignment.)
	 *
	 * @param pageno
	 *            page number in the Minibase.
	 * @param page
	 *            the pointer point to the page.
	 * @throws BufferPoolExceededException if there are no valid replacement candidates when attempting to pin a page not already in memory
	 * @throws DiskMgrException if there is an error from the DiskMgr layer. This is likely caused by incorrect implementations of other methods in the BufferManager
	 */
	public void pinPage(PageId pageno, Page page, boolean emptyPage)
			throws BufferPoolExceededException, DiskMgrException {
		// YOUR CODE HERE

		//check if page is in bufpool, aka pageMap which is a hashtable
		Integer key = pageMap.get(pageno.pid);
		if (key != null) {
			//increment pin count and return ptr to this page -> use setpage
			FrameDescriptor frame = frmDescr[key];
			frame.pinCount++;
			page.setPage(bufPool[key]);
		}
		else {
			// page not in pool, find a frame from fifo queue, and also a new page p
			Page p = new Page();
			if (!isBfrEmpty) {
				//get the head of the queue if it is not null else throw error
				int idx = -1;
				Integer front = fifo.poll();
				if (front != null) {
					idx = pageMap.get(front);
					if (frmDescr[idx].pinCount > 0) {
						throw new BufferPoolExceededException("Queue has no head");
					}
				} else {
					throw new BufferPoolExceededException("Queue has no head");
				}
				// check if dirtybit is true, if it is write old page into frame. USE FLUSHPAGE
				if (frmDescr[idx].dirtyBit) {
					try {
						flushPage(new PageId(frmDescr[idx].pageno));
					}
					catch (PageNotFoundException e) {
						throw new DiskMgrException(e.getMessage());
					}
				}
				// finally read new page
				Minibase.DiskManager.read_page(pageno, p);
				// if a frame already exists, remove it from the ma
				if (frmDescr[idx] != null) {
					pageMap.remove(frmDescr[idx].pageno);
				}
				// add new page to the bufpool at the current index, create a frmdesc obj for it and increment pinCount
				bufPool[idx] = p;
				FrameDescriptor newFrame = new FrameDescriptor();
				newFrame.pageno = pageno.pid;
				newFrame.pinCount = 1;
				frmDescr[idx] = newFrame;
				pageMap.put(pageno.pid, idx);
				// put new page into the map
			}
			// bfr is full, do same but with next ptr
			else {
				Minibase.DiskManager.read_page(pageno, p);
				if (frmDescr[next] != null) {
					pageMap.remove(frmDescr[next].pageno);
				}
				bufPool[next] = p;
				FrameDescriptor frame = new FrameDescriptor();
				frame.pageno = pageno.pid;
				frmDescr[next] = frame;
				frame.pinCount++;
				pageMap.put(pageno.pid, next);
				// move forward in the fifo queue using global ptr, check we don't go over the queue limit
				if (next < bufPool.length) {
					next++;
				}
				// if going over limit, buffer is now full
				if (next >= bufPool.length) {
					isBfrEmpty = false;
				}
			}
			// finally call setPage on new page p
			page.setPage(p);
		}
		// remove the old page from the queue
		fifo.remove(pageMap.get(pageno.pid));
    }

	/**
	 * Unpin a page specified by a pageId. This method should be called with
	 * dirty==true if the client has modified the page. If so, this call should set
	 * the dirty bit for this frame. Further, if pin_count>0, this method should
	 * decrement it. If pin_count=0 before this call, throw an exception to report
	 * error. (For testing purposes, we ask you to throw an exception named
	 * PageUnpinnedException in case of error.)
	 *
	 * @param pageno
	 *            the PageID of the page
	 * @param dirty
	 *            whether or not the page is dirty
	 * @throws PageNotFoundException the page is not in memory
	 * @throws PageUnpinnedException the page is already unpinned
	 */
	public void unpinPage(PageId pageno, boolean dirty)
			throws PageNotFoundException, PageUnpinnedException {
        // YOUR CODE HERE
		Integer key = pageMap.get(pageno.pid);
		// if page does already exist in the map, get a frame for it
		if (key != null) {
			FrameDescriptor frame = frmDescr[key];
			// if dirty bit is set
			if (dirty) {
				frame.dirtyBit = true;
			}
			// if pincount > 0, decrement it
			// trick here, this will keep decrementing pincount till it is 0, at which point we unpin it
			// if the pincount was 0 before decrementing even once, it was already unpinned so throw exception
			if (frame.pinCount > 0) {
				frame.pinCount--;
				if (frame.pinCount == 0) {
					// unpinning
					fifo.offer(key);
				}
			} else {
				throw new PageUnpinnedException("Page Unpinned");
			}
		} else {
			// key null, page not found in map
			throw new PageNotFoundException("Page Not Found with Page ID: " + pageno.pid);
		}
	}

	/**
	 * Allocate new pages. Call DB object to allocate a run of new pages and find a
	 * frame in the buffer pool for the first page and pin it. (This call allows a
	 * client of the Buffer Manager to allocate pages on disk.) If buffer is full,
	 * i.e., you can't find a frame for the first page, ask DB to deallocate all
	 * these pages, and return null.
	 *
	 * @param firstpage
	 *            the address of the first page.
	 * @param howmany
	 *            total number of allocated new pages.
	 *
	 * @return the first page id of the new pages.__ null, if error.
	 * @throws DiskMgrException if there is an error from the DiskMgr layer. This is likely caused by incorrect implementations of other methods of the Buffer Manager
	 * @throws BufferPoolExceededException if the new page cannot be pinned after the run is allocated due to the buffer being full. If this exception is thrown, the newly allocated pages should be deallocated
	 */
	public PageId newPage(Page firstpage, int howmany) throws DiskMgrException, BufferPoolExceededException {
        // YOUR CODE HERE
		PageId pageId = null; // set this to null so it returns null on error
		try {
			// see allocate page in dskmgr
			pageId = Minibase.DiskManager.allocate_page(howmany);
		} catch (BufMgrException e) {
			throw new DiskMgrException(e.getMessage());
		}
		try {
			// pin first page, use pinPage method already implemented
			pinPage(pageId, firstpage, false);
			return pageId;
		} catch (BufferPoolExceededException e) {
			try {
				//buf is pool, deallocate pages
				Minibase.DiskManager.deallocate_page(pageId, howmany);
			} catch (BufMgrException err) {
				throw new DiskMgrException(err.getMessage());
			}
			throw new BufferPoolExceededException(e.getMessage());
		}
	}
	
	/**
	 * This method should be called to delete a page that is on disk. This routine
	 * must call the method in diskmgr package to deallocate the page.
	 *
	 * @param pageno
	 *            the page number in the data base.
	 * @throws PagePinnedException if the page is still pinned
	 * @throws DiskMgrException if there is an error in the DiskMgr layer. This is likely caused by incorrect implementations in other methods of the Buffer Manager
	 */
	public void freePage(PageId pageno) throws PagePinnedException, DiskMgrException {
        // YOUR CODE HERE
		Integer key = pageMap.get(pageno.pid);
		// check if page is found in map, and if is already pinned
		if (key != null && frmDescr[key].pinCount > 0) {
			throw new PagePinnedException("Page with ID: " + pageno.pid + " is already pinned");
		} else {
			// page found, but pincount was 0. remove this
			if (key != null) {
				pageMap.remove(pageno.pid);
				resetFrameDescriptor(key);
			}
			// also check if buffer is empty after removing the page. update the global var
			if (!isBfrEmpty) {
				isBfrEmpty = true;
			}
			// update the queue ptr accordingly, much work, check if after removing the page
			// the previous page is affected or not. basically set all memory references to null
			if (next > 0) {
				int newPageID = next;
				next--;
				fifo.remove(pageMap.get(pageno.pid));
				int oldPageID = pageMap.get(pageno.pid);
				pageMap.remove(pageno.pid);
				if (oldPageID == newPageID) {
					bufPool[newPageID] = null;
					frmDescr[newPageID] = null;
				}
				else {
					bufPool[oldPageID] = bufPool[newPageID];
					frmDescr[oldPageID] = frmDescr[newPageID];
					bufPool[newPageID] = null;
					frmDescr[newPageID] = null;
					pageMap.put(frmDescr[oldPageID].pageno, oldPageID);
				}
			}
			try {
				Minibase.DiskManager.deallocate_page(pageno);
			} catch (BufMgrException e) {
				throw new DiskMgrException(e.getMessage());
			}
		}
	}

	/**
	 * Used to flush a particular page of the buffer pool to disk. This method calls
	 * the write_page method of the diskmgr package.
	 *
	 * @param pageid
	 *            the page number in the database.
	 * @throws PageNotFoundException if the page is not in memory
	 * @throws DiskMgrException if there is an error in the DiskMgr layer. This is likely caused by incorrect implementations in other methods of the Buffer Manager
	 */
	public void flushPage(PageId pageid) throws PageNotFoundException, DiskMgrException {
		// find the frame holding that page
		Integer frameId = pageMap.get(pageid.pid);
		if (frameId == null) {
			throw new PageNotFoundException(
					"BufMgr.flushPage: Page with id " + pageid.pid + " does not exist in the buffer bool.");
		} else {
			Minibase.DiskManager.write_page(pageid, bufPool[frameId]);
			frmDescr[frameId].dirtyBit = false;
		}
	}

	/**
	 * Used to flush all dirty pages in the buffer pool to disk
	 * @throws DiskMgrException if there is an error in the DiskMgr layer. This is likely caused by incorrect implementations in other methods of the Buffer Manager
	 */
	public void flushAllPages() throws DiskMgrException {
		for (int i = 0; i < numOfFrames; i++) {
			if (frmDescr[i].dirtyBit == true) {
				Minibase.DiskManager.write_page(new PageId(frmDescr[i].pageno), bufPool[i]);
				frmDescr[i].dirtyBit = false;
			}
		}
	}

	/**
	 * Returns the total number of buffer frames.
	 */
	public int getNumBuffers() {
		return numOfFrames;
	}

	/**
	 * Returns the total number of unpinned buffer frames.
	 */
	public int getNumUnpinned() {
		int numUnpinned = 0;
		for (int i = 0; i < numOfFrames; i++) {
			if (frmDescr[i].pinCount <= 0) {
				numUnpinned++;
			}
		}
		return numUnpinned;
	}

	//*** DO NOT CHANGE ANY EXISTING METHODS BELOW THIS LINE ***
	// Accessor methods for use in test cases
	public FrameDescriptor getFrameDesc(int frameNum) {
		return frmDescr[frameNum];
	}

	public Page getPageFromFrame(int frameNum) {
		return bufPool[frameNum];
	}

	public Integer getFrameFromPage(PageId pid) {
		return pageMap.get(pid.pid);
	}
}
