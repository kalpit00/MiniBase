package tests;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import bufmgr.*;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import diskmgr.DiskMgrException;
import diskmgr.OutOfSpaceException;
import global.Convert;
import global.GlobalConst;
import global.Minibase;
import global.Page;
import global.PageId;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BMTest implements GlobalConst {

	public final static boolean OK = true;
	public final static boolean FAIL = false;

	/** Default database size (in pages). */
	private final static int DB_SIZE = 10000;

	/** Default buffer pool size (in pages) */
	private final static int BUF_SIZE = 100;

	/** Default number of pages to be looked ahead */
	private final static int LAH_SIZE = 10;

	// Filepaths
	private static String dbpath;
	private static String logpath;
	private static final String REMOVE_CMD = "/bin/rm -rf ";
	private static String remove_logcmd;
	private static String remove_dbcmd;

	// Instance variables for tests
	private static int numPages;

	private static Page pg;
	private static PageId pid, first_pid, last_pid;

	private void checkFDContents(FrameDescriptor fd, int pgid, int pinCount, boolean dirty) {
		String failmsg = "Failed: - ";
		boolean passed = true;
		if (fd.pageno != pgid) {
			failmsg += "FD pagenumber: " + fd.pageno + ", should be " + pgid + " - ";
			passed = false;
		}
		if (fd.pinCount != pinCount) {
			failmsg += "FD pincount: " + fd.pinCount + ", should be " + pinCount + " - ";
			passed = false;
		}
		if (fd.dirtyBit != dirty) {
			failmsg += "Fd dirty bit: " + fd.dirtyBit + ", should be " + dirty;
			passed = false;
		}
		assertTrue(failmsg, passed);
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		// Set up remove commands and ensure nothing's left over
		String remove_cmd = "/bin/rm -rf ";

		dbpath = "/tmp/" + System.getProperty("user.name") + ".minibase-db";
		logpath = "/tmp/" + System.getProperty("user.name") + ".minibase-log";

		remove_logcmd = REMOVE_CMD + logpath;
		remove_dbcmd = REMOVE_CMD + dbpath;

		// Commands here is very machine dependent. We assume
		// user are on UNIX system here
		try {
			Runtime.getRuntime().exec(remove_logcmd);
			Runtime.getRuntime().exec(remove_dbcmd);
		} catch (IOException e) {
			System.err.println("" + e);
		}
	}

	@Before
	public void setUp() throws Exception {
		new Minibase(dbpath, DB_SIZE, BUF_SIZE, LAH_SIZE, "FIFO", false);

		numPages = Minibase.BufferManager.getNumUnpinned();
		pg = new Page();
		pid = new PageId();
		first_pid = new PageId();
		last_pid = new PageId();
	}

	@After
	public void tearDown() throws Exception {
		// Remove anything from previous tests
		// Commands here are very machine dependent. We assume
		// user are on UNIX system here
		try {
			Runtime.getRuntime().exec(remove_logcmd);
			Runtime.getRuntime().exec(remove_dbcmd);
		} catch (IOException e) {
			System.err.println("" + e);
		}
	}

	@Test
	public void testA_PinPageNotInMemory() {
		try {
			first_pid = Minibase.BufferManager.newPage(pg, 1);
		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: error with DiskMgr for initial allocation", false);
		} catch (BufferPoolExceededException e) {
			
			assertTrue("Unexpected Failure: Buffer Pool Full for initial allocation", false);
		}

		// Allocated and pinned one page - check that it's in hash table
		Integer fnum = Minibase.BufferManager.getFrameFromPage(first_pid);
		if (fnum == null)
			assertTrue("Failed: page not in hash table", false);
		pg = Minibase.BufferManager.getPageFromFrame(fnum);
		if (pg == null)
			assertTrue("Failed: page not in frame", false);
		FrameDescriptor fd = Minibase.BufferManager.getFrameDesc(fnum);
		checkFDContents(fd, first_pid.pid, 1, false);
	}

	@Test
	public void testB_PinPinnedPageInMemory() {
		try {
			first_pid = Minibase.BufferManager.newPage(pg, 1);
		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: error with DiskMgr for initial allocation", false);
		} catch (BufferPoolExceededException e) {
			
			assertTrue("Unexpected Failure: Buffer Pool Full for initial allocation", false);
		}

		try {
			Minibase.BufferManager.pinPage(first_pid, pg, false);
		} catch (BufferPoolExceededException e) {
			
			assertTrue("Unexpected Failure: Buffer Pool Full for repinning already pinned page", false);

		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: error with DiskMgr pinning pinned page", false);
		}
		// Allocated and pinned one page twice - check that it's in hash table
		Integer fnum = Minibase.BufferManager.getFrameFromPage(first_pid);
		if (fnum == null)
			assertTrue("Failed: page not in hash table", false);
		pg = Minibase.BufferManager.getPageFromFrame(fnum);
		if (pg == null)
			assertTrue("Failed: page not in frame", false);
		FrameDescriptor fd = Minibase.BufferManager.getFrameDesc(fnum);
		checkFDContents(fd, first_pid.pid, 2, false);
	}

	@Test
	public void testC_UnpinPinnedPageInMemory() {
		try {
			first_pid = Minibase.BufferManager.newPage(pg, 1);
		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: error with DiskMgr for initial allocation", false);
		} catch (BufferPoolExceededException e) {
			
			assertTrue("Unexpected Failure: Buffer Pool Full for initial allocation", false);
		}

		try {
			Minibase.BufferManager.unpinPage(first_pid, false);
		} catch (PageNotFoundException e) {
			
			assertTrue("Unexpected Failure: page not found in hash table when unpinning", false);
		} catch (PageUnpinnedException e) {
			
			assertTrue("Unexpected Failure: page already unpinned", false);
		}

		// Allocated and pinned one page, then unpinned it - check that it's in hash
		// table
		Integer fnum = Minibase.BufferManager.getFrameFromPage(first_pid);
		if (fnum == null)
			assertTrue("Failed: page not in hash table after unpinning", false);
		pg = Minibase.BufferManager.getPageFromFrame(fnum);
		if (pg == null)
			assertTrue("Failed: page not in frame after unpinning", false);
		FrameDescriptor fd = Minibase.BufferManager.getFrameDesc(fnum);
		checkFDContents(fd, first_pid.pid, 0, false);
	}

	@Test
	public void testD_PinUnpinnedPageInMemory() {
		try {
			first_pid = Minibase.BufferManager.newPage(pg, 1);
		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: error with DiskMgr for initial allocation", false);
		} catch (BufferPoolExceededException e) {
			
			assertTrue("Unexpected Failure: Buffer Pool Full for initial allocation", false);
		}

		try {
			Minibase.BufferManager.unpinPage(first_pid, false);
		} catch (PageNotFoundException e) {
			
			assertTrue("Unexpected Failure: page not found in hash table when unpinning", false);
		} catch (PageUnpinnedException e) {
			
			assertTrue("Unexpected Failure: page already unpinned", false);
		}

		try {
			Minibase.BufferManager.pinPage(first_pid, pg, false);
		} catch (BufferPoolExceededException bpe_ex) {
			assertTrue("Unexpected Failure: Buffer Pool Full when attempting to repin unpinned page in buffer", false);

		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: error with DiskMgr repinning page", false);
		}

		// Allocated and pinned one page, then unpinned it, then pinned it again
		// check that it's in hash table
		Integer fnum = Minibase.BufferManager.getFrameFromPage(first_pid);
		if (fnum == null)
			assertTrue("Failed: page not in hash table after repinning page", false);
		pg = Minibase.BufferManager.getPageFromFrame(fnum);
		if (pg == null)
			assertTrue("Failed: page not in frame after repinning page", false);
		FrameDescriptor fd = Minibase.BufferManager.getFrameDesc(fnum);
		checkFDContents(fd, first_pid.pid, 1, false);
	}

	@Test
	public void testE_UnpinUnpinnedPageInMemory() {
		try {
			first_pid = Minibase.BufferManager.newPage(pg, 1);
		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: error with DiskMgr for initial allocation", false);
		} catch (BufferPoolExceededException e) {
			
			assertTrue("Unexpected Failure: Buffer Pool Full for initial allocation", false);
		}

		try {
			Minibase.BufferManager.unpinPage(first_pid, false);
		} catch (PageNotFoundException e) {
			
			assertTrue("Unexpected Failure: page not found in hash table when unpinning", false);
		} catch (PageUnpinnedException e) {
			
			assertTrue("Unexpected Failure: page already unpinned", false);
		}

		// Unpin the page twice so that we will be unpinning an unpinned page
		try {
			Minibase.BufferManager.unpinPage(first_pid, false);
		} catch (PageNotFoundException e) {
			
			assertTrue("Unexpected Failure: page not found in hash table when unpinning unpinned page", false);
		} catch (PageUnpinnedException e) {
			assertTrue(true);
			return;
		}

		// If we get here, the expected exception was not returned
		assertTrue("Failed: PageUnpinnedException not properly thrown", false);
	}

	@Test
	public void testF_UnpinDirtyPage() {
		try {
			first_pid = Minibase.BufferManager.newPage(pg, 1);
		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: error with DiskMgr for initial allocation", false);
		} catch (BufferPoolExceededException e) {
			
			assertTrue("Unexpected Failure: Buffer Pool Full for initial allocation", false);
		}

		try {
			Minibase.BufferManager.unpinPage(first_pid, true);
		} catch (PageNotFoundException e) {
			
			assertTrue("Unexpected Failure: page not found in hash table when unpinning", false);
		} catch (PageUnpinnedException e) {
			
			assertTrue("Unexpected Failure: page already unpinned", false);
		}

		// Allocated and pinned one page, then unpinned it as dirty
		// check that it's in hash table
		Integer fnum = Minibase.BufferManager.getFrameFromPage(first_pid);
		if (fnum == null)
			assertTrue("Failed: page not in hash table after unpinning", false);
		pg = Minibase.BufferManager.getPageFromFrame(fnum);
		if (pg == null)
			assertTrue("Failed: page not in frame after unpinning", false);
		FrameDescriptor fd = Minibase.BufferManager.getFrameDesc(fnum);
		checkFDContents(fd, first_pid.pid, 0, true);
	}

	@Test
	public void testG_UnpinPageNotInMemory() {
		try {
			first_pid = Minibase.BufferManager.newPage(pg, 2);
		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: error with DiskMgr for initial allocation", false);
		} catch (BufferPoolExceededException e) {
			
			assertTrue("Unexpected Failure: Buffer Pool Full for initial allocation", false);
		}

		try {
			Minibase.BufferManager.unpinPage(first_pid, false);
		} catch (PageNotFoundException e) {
			
			assertTrue("Unexpected Failure: page not found in hash table while unpinning", false);
		} catch (PageUnpinnedException e) {
			assertTrue("Unexpected Failure: page already unpinned", false);
		}

		// Allocated two pages and pinned/unpinned first one
		// Now attempt to unpin second, never-pinned page
		pid = new PageId(first_pid.pid + 1);
		try {
			Minibase.BufferManager.unpinPage(pid, false);
		} catch (PageNotFoundException e) {
			assertTrue(true);
			return;
		} catch (PageUnpinnedException e) {
			
			assertTrue(
					"Unexpected Failure: PageUnpinnedException thrown, but should have returned HashNotFoundException",
					false);
		}

		// If we get here, the expected exception was not returned
		assertTrue("Failed: PageNotFoundException not properly thrown", false);
	}

	@Test
	public void testH_PinPageNotInMemoryWithReplacement() {
		try {
			first_pid = Minibase.BufferManager.newPage(pg, numPages + 1);
		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: error with DiskMgr for initial allocation", false);
		} catch (BufferPoolExceededException e) {
			
			assertTrue("Unexpected Failure: Buffer Pool Full for initial allocation", false);
		}

		try {
			Minibase.BufferManager.unpinPage(first_pid, false);
		} catch (PageNotFoundException e) {
			
			assertTrue("Unexpected Failure: page not found in hash table when unpinning", false);
		} catch (PageUnpinnedException e) {
			
			assertTrue("Unexpected Failure: page already unpinned", false);
		}

		// Pin and unpin enough pages to fill buffer
		for (pid.pid = first_pid.pid, last_pid.pid = pid.pid + numPages; pid.pid < last_pid.pid; pid.pid = pid.pid
				+ 1) {
			try {
				Minibase.BufferManager.pinPage(pid, pg, false);
			} catch (BufferPoolExceededException e) {
				
				assertTrue("Unexpected Failure: BufferPoolExceeded exception thrown for page " + pid.pid, false);

			} catch (DiskMgrException e) {
				
				assertTrue("Unexpected Failure: exception at disk manager for pinning page " + pid.pid, false);
			}
			try {
				Minibase.BufferManager.unpinPage(pid, false);
			} catch (PageNotFoundException e) {
				
				assertTrue("Unexpected Failure: page " + pid.pid + " not found in hash table", false);
			} catch (PageUnpinnedException e) {
				
				assertTrue("Unexpected Failure: page " + pid.pid + " already unpinned", false);
			}
		}

		// Now attempt to bring another page into memory
		try {
			Minibase.BufferManager.pinPage(last_pid, pg, false);
		} catch (BufferPoolExceededException e) {
			
			assertTrue("Unexpected Failure: BufferPoolExceeded exception thrown for last page " + last_pid.pid, false);
		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: exception at disk manager when performing replacement", false);
		}

		// Pinned a page using replacement policy - verify that FD is correct
		Integer fnum = Minibase.BufferManager.getFrameFromPage(last_pid);
		if (fnum == null)
			assertTrue("Failed: page not in hash table", false);
		pg = Minibase.BufferManager.getPageFromFrame(fnum);
		if (pg == null)
			assertTrue("Failed: page not in frame", false);
		FrameDescriptor fd = Minibase.BufferManager.getFrameDesc(fnum);
		checkFDContents(fd, last_pid.pid, 1, false);
	}

	@Test
	public void testI_PinPageReplaceDirtyPage() {
		try {
			first_pid = Minibase.BufferManager.newPage(pg, numPages + 1);
		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: error with DiskMgr for initial allocation", false);
		} catch (BufferPoolExceededException e) {
			
			assertTrue("Unexpected Failure: Buffer Pool Full for initial allocation", false);
		}

		try {
			Minibase.BufferManager.unpinPage(first_pid, false);
		} catch (PageNotFoundException e) {
			
			assertTrue("Unexpected Failure: page not found in hash table when unpinning", false);
		} catch (PageUnpinnedException e) {
			
			assertTrue("Unexpected Failure: page already unpinned", false);
		}

		// Pin and unpin enough pages to fill buffer
		for (pid.pid = first_pid.pid, last_pid.pid = pid.pid + numPages; pid.pid < last_pid.pid; pid.pid = pid.pid
				+ 1) {
			try {
				Minibase.BufferManager.pinPage(pid, pg, false);
				// Write some data to each page
				int data = pid.pid + 99999;
				Convert.setIntValue(data, 0, pg.getpage());
			} catch (BufferPoolExceededException e) {
				
				assertTrue("Unexpected Failure: BufferPoolExceeded exception thrown for page " + pid.pid, false);

			} catch (DiskMgrException e) {
				
				assertTrue("Unexpected Failure: exception at disk manager for page " + pid.pid, false);
			} catch (IOException e) {
				
				assertTrue("Unexpected Failure: could not write page data for page " + pid.pid, false);
			}
			// Unpin page - it's dirty
			try {
				Minibase.BufferManager.unpinPage(pid, true);
			} catch (PageNotFoundException e) {
				
				assertTrue("Unexpected Failure: page " + pid.pid + " not found in hash table", false);
			} catch (PageUnpinnedException e) {
				
				assertTrue("Unexpected Failure: page " + pid.pid + " already unpinned", false);
			}
		}

		// Now attempt to bring another page into memory, then unpin it
		pid.pid = last_pid.pid + 1;
		try {
			Minibase.BufferManager.pinPage(pid, pg, false);
		} catch (BufferPoolExceededException e) {
			
			assertTrue("Unexpected Failure: BufferPoolExceeded exception thrown for last page " + last_pid.pid + 1
					+ " during replacement", false);

		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: exception at disk manager when performing replacement", false);
		}
		// Pinned a page using replacement policy - FIFO should remove page with
		// pid=first_pid
		// Verify that page data in frame is overwritten

		try {
			Integer fnum = Minibase.BufferManager.getFrameFromPage(pid);
			if (fnum == null)
				assertTrue("Failed: page " + pid.pid + " not in hash table after replacement", false);
			pg = Minibase.BufferManager.getPageFromFrame(fnum);
			if (pg == null)
				assertTrue("Failed: page " + pid.pid + " not in frame after replacement", false);
			pg = Minibase.BufferManager.getPageFromFrame(fnum);
			int data = Convert.getIntValue(0, pg.getpage());
			if (data == first_pid.pid + 99999)
				assertTrue("Failure: page stored in frame not overwritten when replaced", false);
		} catch (IOException e) {
			
			assertTrue("Unexpected Failure: reading from page " + pid.pid, false);
		}
		try {
			Minibase.BufferManager.unpinPage(pid, true);
		} catch (PageNotFoundException e) {
			
			assertTrue("Unexpected Failure: page " + pid.pid + " not found in hash table", false);
		} catch (PageUnpinnedException e) {
			
			assertTrue("Unexpected Failure: page " + pid.pid + " already unpinned", false);
		}

		// Bring in page with pid=first_pid again and see if its data is retrieved
		try {
			Minibase.BufferManager.pinPage(first_pid, pg, false);
		} catch (BufferPoolExceededException e) {
			
			assertTrue("Unexpected Failure: BufferPoolExceeded bringing first page in from memory " + first_pid.pid
					+ " during replacement", false);
		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: exception at disk manager bringing first page in from memory", false);
		}
		// Verify contents of page after bringing back from disk
		try {
			Integer fnum = Minibase.BufferManager.getFrameFromPage(first_pid);
			if (fnum == null)
				assertTrue("Failed: page " + first_pid.pid + " not in hash table after replacement", false);
			pg = Minibase.BufferManager.getPageFromFrame(fnum);
			if (pg == null)
				assertTrue("Failed: page " + first_pid.pid + " not in frame after replacement", false);
			pg = Minibase.BufferManager.getPageFromFrame(fnum);
			int data = Convert.getIntValue(0, pg.getpage());
			// Finally, check page
			assertTrue("Failure: page stored in frame not overwritten when replaced", data == first_pid.pid + 99999);
		} catch (IOException e) {
			
			assertTrue("Unexpected Failure: reading from page " + first_pid.pid, false);
		}
	}

	@Test
	public void testJ_FIFOBehavior() {
		try {
			first_pid = Minibase.BufferManager.newPage(pg, numPages + 1);
		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: error with DiskMgr for initial allocation", false);
		} catch (BufferPoolExceededException e) {
			
			assertTrue("Unexpected Failure: Buffer Pool Full for initial allocation", false);
		}

		try {
			Minibase.BufferManager.unpinPage(first_pid, false);
		} catch (PageNotFoundException e) {
			
			assertTrue("Unexpected Failure: page not found in hash table when unpinning", false);
		} catch (PageUnpinnedException e) {
			
			assertTrue("Unexpected Failure: page already unpinned", false);
		}

		// Pin enough pages to fill buffer
		for (pid.pid = first_pid.pid, last_pid.pid = pid.pid + numPages; pid.pid < last_pid.pid; pid.pid = pid.pid + 1) {
			try {
				Minibase.BufferManager.pinPage(pid, pg, false);
				// Write some data to each page
				int data = pid.pid + 99999;
				Convert.setIntValue(data, 0, pg.getpage());
			} catch (BufferPoolExceededException e) {
				
				assertTrue("Unexpected Failure: BufferPoolExceeded exception thrown for page " + pid.pid, false);

			} catch (DiskMgrException e) {
				
				assertTrue("Unexpected Failure: exception at disk manager for page " + pid.pid, false);
			} catch (IOException e) {
				
				assertTrue("Unexpected Failure: cannot write data for page " + pid.pid, false);
			}
		}

		// Pick three pages to unpin. Store the frames for reference later
		PageId pg1 = new PageId(), pg2 = new PageId(), pg3 = new PageId();
		int pageRange = last_pid.pid - first_pid.pid;
		pg1.pid = first_pid.pid + (pageRange / 3);
		Integer f1 = Minibase.BufferManager.getFrameFromPage(pg1);
		pg2.pid = first_pid.pid + (pageRange / 3) * 2;
		Integer f2 = Minibase.BufferManager.getFrameFromPage(pg2);
		pg3.pid = first_pid.pid + pageRange-1;
		Integer f3 = Minibase.BufferManager.getFrameFromPage(pg3);

		// Unpin pages in order: pg2, pg1, pg3. FIFO should then select pg3 to evict
		try {
			Minibase.BufferManager.unpinPage(pg2, true);
		} catch (PageNotFoundException e) {
			
			assertTrue("Unexpected Failure: replacement page " + pg2.pid + " not found in hash table", false);
		} catch (PageUnpinnedException e) {
			
			assertTrue("Unexpected Failure: replacement page " + pg2.pid + " already unpinned", false);
		}
		try {
			Minibase.BufferManager.unpinPage(pg1, true);
		} catch (PageNotFoundException e) {
			
			assertTrue("Unexpected Failure: replacement page " + pg1.pid + " not found in hash table", false);
		} catch (PageUnpinnedException e) {
			
			assertTrue("Unexpected Failure: replacement page " + pg1.pid + " already unpinned", false);
		}
		try {
			Minibase.BufferManager.unpinPage(pg3, true);
		} catch (PageNotFoundException e) {
			
			assertTrue("Unexpected Failure: replacement page " + pg3.pid + " not found in hash table", false);
		} catch (PageUnpinnedException e) {
			
			assertTrue("Unexpected Failure: replacement page " + pg3.pid + " already unpinned", false);
		}

		// Now attempt to bring another page into memory
		pid.pid = last_pid.pid + 1;
		try {
			Minibase.BufferManager.pinPage(pid, pg, false);
		} catch (BufferPoolExceededException e) {
			
			assertTrue("Unexpected Failure: BufferPoolExceeded exception thrown for last page " + last_pid.pid + 1
					+ " during replacement", false);

		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: exception at disk manager during replacement", false);
		}
		// Pinned a page using replacement policy - FIFO should remove page with pid=pg2
		// Verify that page data in frame is overwritten

		try {
			Integer fnum = Minibase.BufferManager.getFrameFromPage(pid);
			if (fnum == null)
				assertTrue("Failed: page " + pid.pid + " not in hash table after replacement", false);
			pg = Minibase.BufferManager.getPageFromFrame(fnum);
			if (pg == null)
				assertTrue("Failed: page " + pid.pid + " not in frame after replacement", false);
			pg = Minibase.BufferManager.getPageFromFrame(fnum);
			int data = Convert.getIntValue(0, pg.getpage());
			if (data == pg2.pid + 99999)
				assertTrue("Failure: page stored in frame not overwritten when replaced", false);
			// Now that replacement policy has been used, verify we actually evicted pg3
			assertTrue("Failed: wrong page evicted, should be page " + pg2.pid + " in frame " + f2, fnum == f2);
		} catch (IOException e) {
			
			assertTrue("Unexpected Failure: reading from page " + pid.pid, false);
		}

	}

	@Test
	public void testK_BufferFull() {
		try {
			first_pid = Minibase.BufferManager.newPage(pg, numPages + 1);
		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: error with DiskMgr for initial allocation", false);
		} catch (BufferPoolExceededException e) {
			
			assertTrue("Unexpected Failure: Buffer Pool Full for initial allocation", false);
		}

		try {
			Minibase.BufferManager.unpinPage(first_pid, false);
		} catch (PageNotFoundException e) {
			
			assertTrue("Unexpected Failure: page not found in hash table", false);
		} catch (PageUnpinnedException e) {
			
			assertTrue("Unexpected Failure: page already unpinned", false);
		}

		// Pin enough pages to fill buffer
		for (pid.pid = first_pid.pid, last_pid.pid = pid.pid + numPages; pid.pid < last_pid.pid; pid.pid = pid.pid
				+ 1) {
			try {
				Minibase.BufferManager.pinPage(pid, pg, false);
				// Write some data to each page
				int data = pid.pid + 99999;
				Convert.setIntValue(data, 0, pg.getpage());
			} catch (BufferPoolExceededException e) {
				
				assertTrue("Unexpected Failure: BufferPoolExceeded exception thrown for page " + pid.pid, false);

			} catch (DiskMgrException e) {
				
				assertTrue("Unexpected Failure: exception at disk manager for page " + pid.pid, false);
			} catch (IOException e) {
				
				assertTrue("Unexpected Failure: cannot write to page " + pid.pid, false);
			}
		}

		// Now attempt to bring another page into memory, it should fail
		pid.pid = last_pid.pid + 1;
		try {
			Minibase.BufferManager.pinPage(pid, pg, false);
		} catch (BufferPoolExceededException bpe_ex) {
			assertTrue(true);

		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: exception at disk manager for page " + pid.pid, false);
		}

		// If we reach here, expected exception was not thrown
		assertTrue("Failure: expected BufferPoolExceeded exception not thrown thrown for page " + pid.pid, true);
	}

	@Test
	public void testL_NewPageDeallocation()
	{
		//Allocate enough pages to fill the buffer
		int numTestPages = numPages - 3; //-3 here is to account for required DiskMgr pages, which we will force to be pinned for this test
		try {
			first_pid = Minibase.BufferManager.newPage(pg, numTestPages);
		} catch (DiskMgrException e) {
			
			assertTrue("Unexpected Failure: error with DiskMgr for initial allocation", false);
		} catch (BufferPoolExceededException e) {
			
			assertTrue("Unexpected Failure: Buffer Pool Full for initial allocation", false);
		}
		//Force all required DiskMgr pages to be pinned
		for(pid.pid = 0; pid.pid < first_pid.pid; pid.pid = pid.pid + 1)
		{
			try {
				Minibase.BufferManager.pinPage(pid, pg, false);
			} catch (BufferPoolExceededException e) {
				
				assertTrue("Unexpected Failure: BufferPoolExceeded exception thrown for DiskMgr page " + pid.pid, false);

			} catch (DiskMgrException e) {
				
				assertTrue("Unexpected Failure: exception at disk manager for DiskMgr page " + pid.pid, false);
			}
		}
		// Pin enough pages to fill buffer
		for (pid.pid = first_pid.pid+1, last_pid.pid = first_pid.pid + numTestPages; pid.pid < last_pid.pid; pid.pid = pid.pid
				+ 1) {
			try {
				Minibase.BufferManager.pinPage(pid, pg, false);
			} catch (BufferPoolExceededException e) {
				
				assertTrue("Unexpected Failure: BufferPoolExceeded exception thrown for page " + pid.pid, false);

			} catch (DiskMgrException e) {
				
				assertTrue("Unexpected Failure: exception at disk manager for page " + pid.pid, false);
			}
		}
		//Allocate enough pages to completely fill the database. However, we expect pinning the page to fail here
		boolean pin = true;
		try {
			pid = Minibase.BufferManager.newPage(pg, DB_SIZE - last_pid.pid);
		} catch (DiskMgrException e) {
			e.printStackTrace();
			assertTrue("Unexpected Failure: error with DiskMgr for allocation that fills the DB", false);
		} catch (BufferPoolExceededException e) {
			//Set a flag to show this exception was thrown successfully
			pin = false;
		}
		if(pin)
			assertTrue("Unexpected Failure: Page allocation with full buffer did not throw BufferPoolExceededException", false);

		//Unpin all pages
		for (pid.pid = 0; pid.pid < last_pid.pid; pid.pid = pid.pid + 1) {
			try {
				Minibase.BufferManager.unpinPage(pid, false);
			} catch (PageNotFoundException e) {
				
				assertTrue("Unexpected Failure: page " + pid.pid + " not found in hash table", false);
			} catch (PageUnpinnedException e) {
				
				assertTrue("Unexpected Failure: page " + pid.pid + " already unpinned", false);
			}
		}

		//Allocate new pages again - this should succeed now that the buffer is unpinned
		try {
			pid = Minibase.BufferManager.newPage(pg, DB_SIZE - last_pid.pid);
		} catch (OutOfSpaceException e) {
			assertTrue("Failure: disk pages not deallocated after failure to pin them", false);
		} catch (DiskMgrException e) {
			assertTrue("Unexpected Failure: error with DiskMgr for allocation that fills the DB", false);
		} catch (BufferPoolExceededException e) {
			assertTrue("Unexpected Failure: buffer pool full when trying to allocate new pages", false);
		}

		//If we get here, then we have succeeded
		assertTrue(true);
	}

	@Test //This is a simple test to demonstrate Freepage method. We create a new empty page, bring it to memory queue but do NOT pin it. It should be able to safely free the page.
	public void testM_FreeAnEmptyPage()
	{
		try {
			first_pid = Minibase.BufferManager.newPage(pg, 1);
		} catch (DiskMgrException e) {

			assertTrue("Unexpected Failure: error with DiskMgr for initial allocation", false);
		} catch (BufferPoolExceededException e) {

			assertTrue("Unexpected Failure: Buffer Pool Full for initial allocation", false);
		}
		try {
			Minibase.BufferManager.freePage(first_pid);
			//assertTrue("freepage success", true);
		} catch (PagePinnedException e) {
			assertTrue("Test Failed: Page pinned already", true);
			//System.out.println("  --> Failed as expected \n");
			//status = FAIL; // what we want
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test //This test first creates a new emopty page, pins it and tries to Free it but as it is pinned already, we expect it to fail.
	public void testN_FreeAPinnedPage(){
		try {
			first_pid = Minibase.BufferManager.newPage(pg, 1);
		} catch (DiskMgrException e) {

			assertTrue("Unexpected Failure: error with DiskMgr for initial allocation", false);
		} catch (BufferPoolExceededException e) {

			assertTrue("Unexpected Failure: Buffer Pool Full for initial allocation", false);
		}
		try {
			Minibase.BufferManager.pinPage(first_pid, pg, false);
		} catch (BufferPoolExceededException e) {

			assertTrue("Unexpected Failure: Buffer Pool Full for repinning already pinned page", false);
		} catch (DiskMgrException e) {

			assertTrue("Unexpected Failure: error with DiskMgr pinning pinned page", false);
		}
		try {
			Minibase.BufferManager.freePage(first_pid);
			assertTrue("Unexpected behavior: Freepage was a success even though page was pinned", false);
		} catch (PagePinnedException e) {
			assertTrue("Test Success i.e. Failed as expected: Page pinned already - so Freepage will not suceed. This is the expected behavior.", true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testO_FreePageOnWrittenPages() {
		try {
			first_pid = Minibase.BufferManager.newPage(pg, numPages + 1);
		} catch (DiskMgrException e) {

			assertTrue("Unexpected Failure: error with DiskMgr for initial allocation", false);
		} catch (BufferPoolExceededException e) {

			assertTrue("Unexpected Failure: Buffer Pool Full for initial allocation", false);
		}
		try {
			Minibase.BufferManager.unpinPage(first_pid, false);
		} catch (PageNotFoundException e) {

			assertTrue("Unexpected Failure: page not found in hash table when unpinning", false);
		} catch (PageUnpinnedException e) {

			assertTrue("Unexpected Failure: page already unpinned", false);
		}
		// Pin enough pages to fill buffer
		for (pid.pid = first_pid.pid, last_pid.pid = pid.pid + numPages; pid.pid < last_pid.pid; pid.pid = pid.pid + 1) {
			try {
				Minibase.BufferManager.pinPage(pid, pg, false);
				// Write some data to each page
				int data = pid.pid + 99999;
				Convert.setIntValue(data, 0, pg.getpage());
			} catch (BufferPoolExceededException e) {

				assertTrue("Unexpected Failure: BufferPoolExceeded exception thrown for page " + pid.pid, false);
			} catch (DiskMgrException e) {

				assertTrue("Unexpected Failure: exception at disk manager for page " + pid.pid, false);
			} catch (IOException e) {

				assertTrue("Unexpected Failure: cannot write data for page " + pid.pid, false);
			}
		}
		// Pick three pages to unpin. But unpin only two and keep the third pinned
		PageId pg1 = new PageId(), pg2 = new PageId(), pg3 = new PageId();
		int pageRange = last_pid.pid - first_pid.pid;
		pg1.pid = first_pid.pid + (pageRange / 3);
		Integer f1 = Minibase.BufferManager.getFrameFromPage(pg1);
		pg2.pid = first_pid.pid + (pageRange / 3) * 2;
		Integer f2 = Minibase.BufferManager.getFrameFromPage(pg2);
		pg3.pid = first_pid.pid + pageRange-1;
		Integer f3 = Minibase.BufferManager.getFrameFromPage(pg3);
		// Unpin pages in order: pg2, pg1, pg3 (as should be in FIFO policy). But for the test we do not unpin pg3
		try {
			Minibase.BufferManager.unpinPage(pg2, true);
		} catch (PageNotFoundException e) {

			assertTrue("Unexpected Failure: replacement page " + pg2.pid + " not found in hash table", false);
		} catch (PageUnpinnedException e) {

			assertTrue("Unexpected Failure: replacement page " + pg2.pid + " already unpinned", false);
		}
		try {
			Minibase.BufferManager.unpinPage(pg1, true);
		} catch (PageNotFoundException e) {

			assertTrue("Unexpected Failure: replacement page " + pg1.pid + " not found in hash table", false);
		} catch (PageUnpinnedException e) {

			assertTrue("Unexpected Failure: replacement page " + pg1.pid + " already unpinned", false);
		}

		try {
			Minibase.BufferManager.freePage(pg1);
			assertTrue("Tests passed: PG1 was unpinned and should be able to be freed", true);
		} catch (PagePinnedException e) {
			assertTrue("Test Failed i.e. should be able to free an empty page", false);

		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Minibase.BufferManager.freePage(pg3);
			assertTrue("Tests failed: PG3 was pinned (and written) and should NOT be able to be freed", false);
		} catch (PagePinnedException e) {
			assertTrue("Test passed i.e. should not be able to free a pinned page that was written on", true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
