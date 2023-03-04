/* File DB.java */

package diskmgr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import bufmgr.BufMgrException;
import chainexception.ChainException;
import global.Convert;
import global.GlobalConst;
import global.Minibase;
import global.Page;
import global.PageId;

public class DiskMgr implements GlobalConst {

	private static final int bits_per_page = PAGE_SIZE * 8;

	/**
	 * Open the database with the given name.
	 *
	 * @param fname
	 *            DB_name
	 * @throws IOException
	 * @throws BufMgrException
	 * @throws FileIOException
	 */
	public void openDB(String fname) throws BufMgrException, FileIOException {

		name = fname;

		// Create a random access file
		try {
			fp = new RandomAccessFile(fname, "rw");

			PageId pageId = new PageId();
			Page apage = new Page();
			pageId.pid = 0;

			num_pages = 1; // temporary num_page value for pinpage to work

			pinPage(pageId, apage, false /* read disk */);

			DBFirstPage firstpg = new DBFirstPage();
			firstpg.openPage(apage);
			num_pages = firstpg.getNumDBPages();

			unpinPage(pageId, false /* undirty */);
		} catch (IOException e) {
			throw new FileIOException(e.getMessage());
		}
	}

	/**
	 * default constructor.
	 */
	public DiskMgr() {
	}

	/**
	 * DB Constructors. Create a database with the specified number of pages where
	 * the page size is the default page size.
	 *
	 * @param fname
	 *            DB name
	 * @param num_pgs
	 *            number of pages in DB
	 * @throws BufMgrException
	 * @throws FileIOException
	 * @throws @throws
	 *             InvalidPageNumberException
	 *
	 */
	public void openDB(String fname, int num_pgs) throws BufMgrException, InvalidPageNumberException, FileIOException {

		name = new String(fname);
		num_pages = (num_pgs > 2) ? num_pgs : 2;

		File DBfile = new File(name);

		DBfile.delete();

		try {
			// Create a random access file
			fp = new RandomAccessFile(fname, "rw");

			// Make the file num_pages pages long, filled with zeroes.
			fp.seek((long) (num_pages * PAGE_SIZE - 1));
			fp.writeByte(0);

			// Initialize space map and directory pages.

			// Initialize the first DB page
			Page apage = new Page();
			PageId pageId = new PageId();
			pageId.pid = 0;
			pinPage(pageId, apage, true /* no diskIO */);

			DBFirstPage firstpg = new DBFirstPage(apage);

			firstpg.setNumDBPages(num_pages);
			unpinPage(pageId, true /* dirty */);

			// Calculate how many pages are needed for the space map. Reserve pages
			// 0 and 1 and as many additional pages for the space map as are needed.
			int num_map_pages = (num_pages + bits_per_page - 1) / bits_per_page;

			set_bits(pageId, 1 + num_map_pages, 1);
		} catch (IOException e) {
			throw new FileIOException(e.getMessage());
		}

	}

	/**
	 * Close DB file.
	 * 
	 * @throws FileIOException
	 */
	public void closeDB() throws FileIOException {
		try {
			fp.close();
		} catch (IOException e) {
			throw new FileIOException(e.getMessage());
		}
	}

	/**
	 * Destroy the database, removing the file that stores it.
	 * 
	 * @throws FileIOException
	 * 
	 */
	public void DBDestroy() throws FileIOException {

		try {
			fp.close();
			File DBfile = new File(name);
			DBfile.delete();
		} catch (IOException e) {
			throw new FileIOException(e.getMessage());
		}
	}

	/**
	 * Read the contents of the specified page into a Page object
	 *
	 * @param pageno
	 *            pageId which will be read
	 * @param apage
	 *            page object which holds the contents of page
	 * @throws InvalidPageNumberException
	 * @throws FileIOException
	 *
	 */
	public void read_page(PageId pageno, Page apage) throws InvalidPageNumberException, FileIOException {

		if ((pageno.pid < 0) || (pageno.pid >= num_pages))
			throw new InvalidPageNumberException("BAD_PAGE_NUMBER");

		try {
			// Seek to the correct page
			fp.seek((long) (pageno.pid * PAGE_SIZE));

			// Read the appropriate number of bytes.
			byte[] buffer = apage.getpage(); // new byte[PAGE_SIZE];
			fp.read(buffer);
		} catch (IOException e) {
			throw new FileIOException(e.getMessage());
		}
	}

	/**
	 * Write the contents in a page object to the specified page.
	 *
	 * @param pageno
	 *            pageId will be wrote to disk
	 * @param apage
	 *            the page object will be wrote to disk
	 *
	 * @throws FileIOException
	 * @throws InvalidPageNumberException
	 */
	public void write_page(PageId pageno, Page apage) throws FileIOException, InvalidPageNumberException {

		if ((pageno.pid < 0) || (pageno.pid >= num_pages))
			throw new InvalidPageNumberException("INVALID_PAGEID_NUMBER");

		try {
			// Seek to the correct page
			fp.seek((long) (pageno.pid * PAGE_SIZE));

			// Write the appropriate number of bytes.
			fp.write(apage.getpage());
		} catch (IOException e) {
			throw new FileIOException(e.getMessage());
		}

	}

	/**
	 * Allocates a set of pages on disk, given the run size.
	 * 
	 * @return The new page's id
	 * @throws InvalidPageNumberException
	 * @throws BufMgrException
	 * @throws FileIOException
	 */
	public PageId allocate_page(int run_size) throws InvalidPageNumberException, BufMgrException, FileIOException {

		// validate the run size
		if ((run_size < 1) || (run_size > num_pages)) {
			throw new IllegalArgumentException("Invalid run size; allocate aborted");
		}

		// calculate the run in the space map
		int num_map_pages = (num_pages + bits_per_page - 1) / bits_per_page;
		int current_run_start = 0;
		int current_run_length = 0;

		// this loop goes over each page in the space map
		PageId pgid = new PageId();
		Page apage = new Page();
		try {
			for (int i = 0; i < num_map_pages; ++i) {

				// pin the space-map page
				pgid.pid = i + 1;
				Minibase.BufferManager.pinPage(pgid, apage, PIN_DISKIO);

				// get the num of bits on current page
				int num_bits_this_page = num_pages - i * bits_per_page;
				if (num_bits_this_page > bits_per_page)
					num_bits_this_page = bits_per_page;

				// Walk the page looking for a sequence of 0 bits of the appropriate
				// length. The outer loop steps through the page's bytes, the inner
				// one steps through each byte's bits.
				byte[] pagebuf = apage.getData();
				for (int byteptr = 0; num_bits_this_page > 0 && current_run_length < run_size; byteptr++) {

					// initialize bit mask
					Byte mask = new Byte(new Integer(1).byteValue());
					byte tmpmask = mask.byteValue();

					// search the page for an empty run
					while (mask.intValue() != 0 && (num_bits_this_page > 0) && (current_run_length < run_size)) {

						// if a non-empty page is found
						if ((pagebuf[byteptr] & tmpmask) != 0) {
							current_run_start += current_run_length + 1;
							current_run_length = 0;
						} else {
							current_run_length++;
						}

						// advance to the next page
						tmpmask <<= 1;
						mask = new Byte(tmpmask);
						num_bits_this_page--;

					} // while

				} // inner loop

				// unpin the current space-map page
				Minibase.BufferManager.unpinPage(pgid, UNPIN_CLEAN);

			} // outer loop
		} catch (DiskMgrException e) {
			throw new BufMgrException(e.getMessage());
		}

		// check for disk full exception
		if (current_run_length < run_size) {
			throw new IllegalStateException("Not enough space left; allocate aborted");
		}

		// update the space map and return the resulting page id
		PageId firstpg = new PageId(current_run_start);
		set_bits(firstpg, run_size, 1);
		return firstpg;

	} // public PageId allocate_page(int run_size)

	/**
	 * Allocate a set of pages where the run size is taken to be 1 by default. Gives
	 * back the page number of the first page of the allocated run. with default
	 * run_size =1
	 *
	 * @param start_page_num
	 *            page number to start with
	 * @throws BufMgrException
	 * @throws OutOfSpaceException
	 * @throws InvalidPageNumberException
	 * @throws InvalidRunSizeException
	 */
	public void allocate_page(PageId start_page_num)
			throws InvalidRunSizeException, InvalidPageNumberException, OutOfSpaceException, BufMgrException {
		allocate_page(start_page_num, 1);
	}

	/**
	 * user specified run_size
	 *
	 * @param start_page_num
	 *            the starting page id of the run of pages
	 * @param runsize
	 *            the number of page need allocated
	 * @throws InvalidRunSizeException
	 * @throws BufMgrException
	 * @throws InvalidPageNumberException
	 * @throws OutOfSpaceException
	 * @throws DiskMgrException
	 */
	public void allocate_page(PageId start_page_num, int runsize)
			throws InvalidRunSizeException, BufMgrException, InvalidPageNumberException, OutOfSpaceException {

		if (runsize < 0)
			throw new InvalidRunSizeException("Negative run_size");

		int run_size = runsize;
		int num_map_pages = (num_pages + bits_per_page - 1) / bits_per_page;
		int current_run_start = 0;
		int current_run_length = 0;

		// This loop goes over each page in the space map.
		PageId pgid = new PageId();
		byte[] pagebuf;
		int byteptr;

		for (int i = 0; i < num_map_pages; ++i) {// start forloop01

			pgid.pid = 1 + i;
			// Pin the space-map page.

			Page apage = new Page();
			pinPage(pgid, apage, false /* read disk */);

			pagebuf = apage.getpage();
			byteptr = 0;

			// get the num of bits on current page
			int num_bits_this_page = num_pages - i * bits_per_page;
			if (num_bits_this_page > bits_per_page)
				num_bits_this_page = bits_per_page;

			// Walk the page looking for a sequence of 0 bits of the appropriate
			// length. The outer loop steps through the page's bytes, the inner
			// one steps through each byte's bits.

			for (; num_bits_this_page > 0 && current_run_length < run_size; ++byteptr) {// start forloop02

				Integer intmask = new Integer(1);
				Byte mask = new Byte(intmask.byteValue());
				byte tmpmask = mask.byteValue();

				while (mask.intValue() != 0 && (num_bits_this_page > 0) && (current_run_length < run_size))

				{
					if ((pagebuf[byteptr] & tmpmask) != 0) {
						current_run_start += current_run_length + 1;
						current_run_length = 0;
					} else
						++current_run_length;

					tmpmask <<= 1;
					mask = new Byte(tmpmask);
					--num_bits_this_page;
				}

			} // end of forloop02
				// Unpin the space-map page.

			unpinPage(pgid, false /* undirty */);

		} // end of forloop01

		if (current_run_length >= run_size) {
			start_page_num.pid = current_run_start;
			set_bits(start_page_num, run_size, 1);

			return;
		}

		throw new OutOfSpaceException("No space left");
	}

	/**
	 * Deallocate a set of pages starting at the specified page number and a run
	 * size can be specified.
	 *
	 * @param start_page_num
	 *            the start pageId to be deallocate
	 * @param run_size
	 *            the number of pages to be deallocated
	 * @throws InvalidRunSizeException
	 * @throws BufMgrException
	 * @throws InvalidPageNumberException
	 */
	public void deallocate_page(PageId start_page_num, int run_size)
			throws InvalidRunSizeException, InvalidPageNumberException, BufMgrException {

		if (run_size < 0)
			throw new InvalidRunSizeException("Negative run_size");

		set_bits(start_page_num, run_size, 0);
	}

	/**
	 * Deallocate a set of pages starting at the specified page number with run size
	 * = 1
	 *
	 * @param start_page_num
	 *            the start pageId to be deallocate
	 * @throws BufMgrException
	 * @throws InvalidPageNumberException
	 *
	 * 
	 */
	public void deallocate_page(PageId start_page_num) throws InvalidPageNumberException, BufMgrException {

		set_bits(start_page_num, 1, 0);
	}

	/**
	 * Adds a file entry to the header page(s).
	 *
	 * @param fname
	 *            file entry name
	 * @param start_page_num
	 *            the start page number of the file entry
	 * @throws FileNameTooLongException
	 * @throws InvalidPageNumberException
	 * @throws DuplicateEntryException
	 * @throws BufMgrException
	 * @throws FileIOException
	 */
	public void add_file_entry(String fname, PageId start_page_num) throws FileNameTooLongException,
			InvalidPageNumberException, DuplicateEntryException, BufMgrException, FileIOException {

		if (fname.length() >= NAME_MAXLEN)
			throw new FileNameTooLongException("DB filename too long");
		if ((start_page_num.pid < 0) || (start_page_num.pid >= num_pages))
			throw new InvalidPageNumberException(" DB bad page number");

		// Does the file already exist?

		if (get_file_entry(fname) != null)
			throw new DuplicateEntryException("DB fileentry already exists");

		Page apage = new Page();

		boolean found = false;
		int free_slot = 0;
		PageId hpid = new PageId();
		PageId nexthpid = new PageId(0);
		DBHeaderPage dp;
		try {
			do {// Start DO01
				// System.out.println("start do01");
				hpid.pid = nexthpid.pid;

				// Pin the header page
				pinPage(hpid, apage, false /* read disk */);

				// This complication is because the first page has a different
				// structure from that of subsequent pages.
				if (hpid.pid == 0) {
					dp = new DBFirstPage();
					((DBFirstPage) dp).openPage(apage);
				} else {
					dp = new DBDirectoryPage();
					((DBDirectoryPage) dp).openPage(apage);
				}

				nexthpid = dp.getNextPage();
				int entry = 0;

				PageId tmppid = new PageId();
				while (entry < dp.getNumOfEntries()) {
					dp.getFileEntry(tmppid, entry);
					if (tmppid.pid == INVALID_PAGEID)
						break;
					entry++;
				}

				if (entry < dp.getNumOfEntries()) {
					free_slot = entry;
					found = true;
				} else if (nexthpid.pid != INVALID_PAGEID) {
					// We only unpin if we're going to continue looping.
					unpinPage(hpid, false /* undirty */);
				}

			} while ((nexthpid.pid != INVALID_PAGEID) && (!found)); // End of DO01

			// Have to add a new header page if possible.
			if (!found) {
				try {
					allocate_page(nexthpid);
				} catch (Exception e) { // need rethrow an exception!!!!
					unpinPage(hpid, false /* undirty */);
					e.printStackTrace();
				}

				// Set the next-page pointer on the previous directory page.
				dp.setNextPage(nexthpid);
				unpinPage(hpid, true /* dirty */);

				// Pin the newly-allocated directory page.
				hpid.pid = nexthpid.pid;

				pinPage(hpid, apage, true/* no diskIO */);
				dp = new DBDirectoryPage(apage);

				free_slot = 0;
			}

			// At this point, "hpid" has the page id of the header page with the free
			// slot; "pg" points to the pinned page; "dp" has the directory_page
			// pointer; "free_slot" is the entry number in the directory where we're
			// going to put the new file entry.

			dp.setFileEntry(start_page_num, fname, free_slot);

			unpinPage(hpid, true /* dirty */);
		} catch (IOException e) {
			throw new FileIOException(e.getMessage());
		}

	}

	/**
	 * Delete the entry corresponding to a file from the header page(s).
	 *
	 * @param fname
	 *            file entry name
	 * @throws BufMgrException
	 * @throws FileEntryNotFoundException
	 * @throws FileIOException
	 */
	public void delete_file_entry(String fname) throws BufMgrException, FileEntryNotFoundException, FileIOException {

		Page apage = new Page();
		boolean found = false;
		int slot = 0;
		PageId hpid = new PageId();
		PageId nexthpid = new PageId(0);
		PageId tmppid = new PageId();
		DBHeaderPage dp;

		try {
			do { // startDO01
				hpid.pid = nexthpid.pid;

				// Pin the header page.
				pinPage(hpid, apage, false/* read disk */);

				// This complication is because the first page has a different
				// structure from that of subsequent pages.
				if (hpid.pid == 0) {
					dp = new DBFirstPage();
					((DBFirstPage) dp).openPage(apage);
				} else {
					dp = new DBDirectoryPage();
					((DBDirectoryPage) dp).openPage(apage);
				}
				nexthpid = dp.getNextPage();

				int entry = 0;

				String tmpname;
				while (entry < dp.getNumOfEntries()) {
					tmpname = dp.getFileEntry(tmppid, entry);

					if ((tmppid.pid != INVALID_PAGEID) && (tmpname.compareTo(fname) == 0))
						break;
					entry++;
				}

				if (entry < dp.getNumOfEntries()) {
					slot = entry;
					found = true;
				} else {
					unpinPage(hpid, false /* undirty */);
				}

			} while ((nexthpid.pid != INVALID_PAGEID) && (!found)); // EndDO01

			if (!found) // Entry not found - nothing deleted
				throw new FileEntryNotFoundException("DB file not found");

			// Have to delete record at hpnum:slot
			tmppid.pid = INVALID_PAGEID;
			dp.setFileEntry(tmppid, "\0", slot);

			unpinPage(hpid, true /* dirty */);
		} catch (IOException e) {
			throw new FileIOException(e.getMessage());
		}

	}

	/**
	 * Get the entry corresponding to the given file.
	 *
	 * @param name
	 *            file entry name
	 * @throws BufMgrException
	 * @throws FileIOException
	 */
	public PageId get_file_entry(String name) throws BufMgrException, FileIOException {

		Page apage = new Page();
		boolean found = false;
		int slot = 0;
		PageId hpid = new PageId();
		PageId nexthpid = new PageId(0);
		DBHeaderPage dp;

		try {
			do {// Start DO01

				// System.out.println("get_file_entry do-loop01: "+name);
				hpid.pid = nexthpid.pid;

				// Pin the header page.
				pinPage(hpid, apage, false /* no diskIO */);

				// This complication is because the first page has a different
				// structure from that of subsequent pages.
				if (hpid.pid == 0) {
					dp = new DBFirstPage();
					((DBFirstPage) dp).openPage(apage);
				} else {
					dp = new DBDirectoryPage();
					((DBDirectoryPage) dp).openPage(apage);
				}
				nexthpid = dp.getNextPage();

				int entry = 0;
				PageId tmppid = new PageId();
				String tmpname;

				while (entry < dp.getNumOfEntries()) {
					tmpname = dp.getFileEntry(tmppid, entry);

					if ((tmppid.pid != INVALID_PAGEID) && (tmpname.compareTo(name) == 0))
						break;
					entry++;
				}
				if (entry < dp.getNumOfEntries()) {
					slot = entry;
					found = true;
				}

				unpinPage(hpid, false /* undirty */);

			} while ((nexthpid.pid != INVALID_PAGEID) && (!found));// End of DO01

			if (!found) // Entry not found - don't post error, just fail.
			{
				// System.out.println("entry NOT found");
				return null;
			}

			PageId startpid = new PageId();
			dp.getFileEntry(startpid, slot);
			return startpid;
		} catch (IOException e) {
			throw new FileIOException(e.getMessage());
		}
	}

	/**
	 * Functions to return some characteristics of the database.
	 */
	public String db_name() {
		return name;
	}

	public int db_num_pages() {
		return num_pages;
	}

	public int db_page_size() {
		return PAGE_SIZE;
	}

	/**
	 * Print out the space map of the database. The space map is a bitmap showing
	 * which pages of the db are currently allocated.
	 * 
	 * @throws BufMgrException
	 *
	 * @exception FileIOException
	 *                file I/O error
	 * @exception IOException
	 *                I/O errors
	 * @exception InvalidPageNumberException
	 *                invalid page number
	 * @exception DiskMgrException
	 *                error caused by other layers
	 */
	public void dump_space_map() throws BufMgrException {

		System.out.println("********  IN DUMP");
		int num_map_pages = (num_pages + bits_per_page - 1) / bits_per_page;
		int bit_number = 0;

		// This loop goes over each page in the space map.
		PageId pgid = new PageId();
		System.out.println("num_map_pages = " + num_map_pages);
		System.out.println("num_pages = " + num_pages);
		for (int i = 0; i < num_map_pages; i++) {// start forloop01

			pgid.pid = 1 + i; // space map starts at page1
			// Pin the space-map page.
			Page apage = new Page();
			pinPage(pgid, apage, false/* read disk */);

			// How many bits should we examine on this page?
			int num_bits_this_page = num_pages - i * bits_per_page;
			System.out.println("num_bits_this_page = " + num_bits_this_page);
			System.out.println("num_pages = " + num_pages);
			if (num_bits_this_page > bits_per_page)
				num_bits_this_page = bits_per_page;

			// Walk the page looking for a sequence of 0 bits of the appropriate
			// length. The outer loop steps through the page's bytes, the inner
			// one steps through each byte's bits.

			int pgptr = 0;
			byte[] pagebuf = apage.getpage();
			int mask;
			for (; num_bits_this_page > 0; pgptr++) {// start forloop02

				for (mask = 1; mask < 256
						&& num_bits_this_page > 0; mask = (mask << 1), --num_bits_this_page, ++bit_number) {// start
																											// forloop03

					int bit = pagebuf[pgptr] & mask;
					if ((bit_number % 10) == 0)
						if ((bit_number % 50) == 0) {
							if (bit_number > 0)
								System.out.println("\n");
							System.out.print("\t" + bit_number + ": ");
						} else
							System.out.print(' ');

					if (bit != 0)
						System.out.print("1");
					else
						System.out.print("0");

				} // end of forloop03

			} // end of forloop02

			unpinPage(pgid, false /* undirty */);

		} // end of forloop01

		System.out.println();

	}

	private RandomAccessFile fp;
	private int num_pages;
	private String name;

	/**
	 * Set runsize bits starting from start to value specified
	 * 
	 * @throws InvalidPageNumberException
	 * @throws BufMgrException
	 * @throws DiskMgrException
	 */
	private void set_bits(PageId start_page, int run_size, int bit) throws InvalidPageNumberException, BufMgrException {

		if ((start_page.pid < 0) || (start_page.pid + run_size > num_pages))
			throw new InvalidPageNumberException("Bad page number");

		// Locate the run within the space map.
		int first_map_page = start_page.pid / bits_per_page + 1;
		int last_map_page = (start_page.pid + run_size - 1) / bits_per_page + 1;
		int first_bit_no = start_page.pid % bits_per_page;

		// The outer loop goes over all space-map pages we need to touch.

		for (PageId pgid = new PageId(first_map_page); pgid.pid <= last_map_page; pgid.pid = pgid.pid
				+ 1, first_bit_no = 0) {// Start forloop01

			// Pin the space-map page.
			Page pg = new Page();

			pinPage(pgid, pg, false/* no diskIO */);

			byte[] pgbuf = pg.getpage();

			// Locate the piece of the run that fits on this page.
			int first_byte_no = first_bit_no / 8;
			int first_bit_offset = first_bit_no % 8;
			int last_bit_no = first_bit_no + run_size - 1;

			if (last_bit_no >= bits_per_page)
				last_bit_no = bits_per_page - 1;

			int last_byte_no = last_bit_no / 8;

			// This loop actually flips the bits on the current page.
			int cur_posi = first_byte_no;
			for (; cur_posi <= last_byte_no; ++cur_posi, first_bit_offset = 0) {// start forloop02

				int max_bits_this_byte = 8 - first_bit_offset;
				int num_bits_this_byte = (run_size > max_bits_this_byte ? max_bits_this_byte : run_size);

				int imask = 1;
				int temp;
				imask = ((imask << num_bits_this_byte) - 1) << first_bit_offset;
				Integer intmask = new Integer(imask);
				Byte mask = new Byte(intmask.byteValue());
				byte bytemask = mask.byteValue();

				if (bit == 1) {
					temp = (pgbuf[cur_posi] | bytemask);
					intmask = new Integer(temp);
					pgbuf[cur_posi] = intmask.byteValue();
				} else {

					temp = pgbuf[cur_posi] & (255 ^ bytemask);
					intmask = new Integer(temp);
					pgbuf[cur_posi] = intmask.byteValue();
				}
				run_size -= num_bits_this_byte;

			} // end of forloop02

			// Unpin the space-map page.

			unpinPage(pgid, true /* dirty */);

		} // end of forloop01

	}

	/**
	 * short cut to access the pinPage function in bufmgr package.
	 * 
	 * @see bufmgr.pinPage
	 */
	private void pinPage(PageId pageno, Page page, boolean emptyPage) throws BufMgrException {

		try {
			Minibase.BufferManager.pinPage(pageno, page, emptyPage);
		} catch (DiskMgrException e) {
			throw new BufMgrException(e.getMessage());
		}

	} // end of pinPage

	/**
	 * short cut to access the unpinPage function in bufmgr package.
	 * 
	 * @see bufmgr.unpinPage
	 */
	private void unpinPage(PageId pageno, boolean dirty) throws BufMgrException {
			Minibase.BufferManager.unpinPage(pageno, dirty);
	} // end of unpinPage

}// end of DB class

/**
 * interface of PageUsedBytes
 */
interface PageUsedBytes {
	int DIR_PAGE_USED_BYTES = 8 + 8;
	int FIRST_PAGE_USED_BYTES = DIR_PAGE_USED_BYTES + 4;
}

/**
 * Super class of the directory page and first page
 */
class DBHeaderPage implements PageUsedBytes, GlobalConst {

	protected static final int NEXT_PAGE = 0;
	protected static final int NUM_OF_ENTRIES = 4;
	protected static final int START_FILE_ENTRIES = 8;
	protected static final int SIZE_OF_FILE_ENTRY = 4 + NAME_MAXLEN + 2;

	protected byte[] data;

	/**
	 * Default constructor
	 */
	public DBHeaderPage() {
	}

	/**
	 * Constrctor of class DBHeaderPage
	 * 
	 * @param page
	 *            a page of Page object
	 * @param pageusedbytes
	 *            number of bytes used on the page
	 * @exception IOException
	 */
	public DBHeaderPage(Page page, int pageusedbytes) throws IOException {
		data = page.getpage();
		PageId pageno = new PageId();
		pageno.pid = INVALID_PAGEID;
		setNextPage(pageno);

		PageId temppid = getNextPage();

		int num_entries = (PAGE_SIZE - pageusedbytes) / SIZE_OF_FILE_ENTRY;
		setNumOfEntries(num_entries);

		for (int index = 0; index < num_entries; ++index)
			initFileEntry(INVALID_PAGEID, index);
	}

	/**
	 * set the next page number
	 * 
	 * @param pageno
	 *            next page ID
	 * @exception IOException
	 *                I/O errors
	 */
	public void setNextPage(PageId pageno) throws IOException {
		Convert.setIntValue(pageno.pid, NEXT_PAGE, data);
	}

	/**
	 * return the next page number
	 * 
	 * @return next page ID
	 * @exception IOException
	 *                I/O errors
	 */
	public PageId getNextPage() throws IOException {
		PageId nextPage = new PageId();
		nextPage.pid = Convert.getIntValue(NEXT_PAGE, data);
		return nextPage;
	}

	/**
	 * set number of entries on this page
	 * 
	 * @param numEntries
	 *            the number of entries
	 * @exception IOException
	 *                I/O errors
	 */

	protected void setNumOfEntries(int numEntries) throws IOException {
		Convert.setIntValue(numEntries, NUM_OF_ENTRIES, data);
	}

	/**
	 * return the number of file entries on the page
	 * 
	 * @return number of entries
	 * @exception IOException
	 *                I/O errors
	 */
	public int getNumOfEntries() throws IOException {
		return Convert.getIntValue(NUM_OF_ENTRIES, data);
	}

	/**
	 * initialize file entries as empty
	 * 
	 * @param empty
	 *            invalid page number (=-1)
	 * @param entryno
	 *            file entry number
	 * @exception IOException
	 *                I/O errors
	 */
	private void initFileEntry(int empty, int entryNo) throws IOException {
		int position = START_FILE_ENTRIES + entryNo * SIZE_OF_FILE_ENTRY;
		Convert.setIntValue(empty, position, data);
	}

	/**
	 * set file entry
	 * 
	 * @param pageno
	 *            page ID
	 * @param fname
	 *            the file name
	 * @param entryno
	 *            file entry number
	 * @exception IOException
	 *                I/O errors
	 */
	public void setFileEntry(PageId pageNo, String fname, int entryNo) throws IOException {

		int position = START_FILE_ENTRIES + entryNo * SIZE_OF_FILE_ENTRY;
		Convert.setIntValue(pageNo.pid, position, data);
		Convert.setStringValue(fname, position + 4, data);
	}

	/**
	 * return file entry info
	 * 
	 * @param pageno
	 *            page Id
	 * @param entryNo
	 *            the file entry number
	 * @return file name
	 * @exception IOException
	 *                I/O errors
	 */
	public String getFileEntry(PageId pageNo, int entryNo) throws IOException {

		int position = START_FILE_ENTRIES + entryNo * SIZE_OF_FILE_ENTRY;
		pageNo.pid = Convert.getIntValue(position, data);
		return (Convert.getStringValue(position + 4, data, NAME_MAXLEN + 2));
	}

}

/**
 * DBFirstPage class which is a subclass of DBHeaderPage class
 */
class DBFirstPage extends DBHeaderPage {

	protected static final int NUM_DB_PAGE = PAGE_SIZE - 4;

	/**
	 * Default construtor
	 */
	public DBFirstPage() {
		super();
	}

	/**
	 * Constructor of class DBFirstPage class
	 * 
	 * @param page
	 *            a page of Page object
	 * @exception IOException
	 *                I/O errors
	 */
	public DBFirstPage(Page page) throws IOException {
		super(page, FIRST_PAGE_USED_BYTES);
	}

	/**
	 * open an exist DB first page
	 * 
	 * @param page
	 *            a page of Page object
	 */
	public void openPage(Page page) {
		data = page.getpage();
	}

	/**
	 * set number of pages in the DB
	 * 
	 * @param num
	 *            the number of pages in DB
	 * @exception IOException
	 *                I/O errors
	 */
	public void setNumDBPages(int num) throws IOException {
		Convert.setIntValue(num, NUM_DB_PAGE, data);
	}

	/**
	 * return the number of pages in the DB
	 * 
	 * @return number of pages in DB
	 * @exception IOException
	 *                I/O errors
	 */
	public int getNumDBPages() throws IOException {

		return (Convert.getIntValue(NUM_DB_PAGE, data));
	}

}

/**
 * DBDirectoryPage class which is a subclass of DBHeaderPage class
 */
class DBDirectoryPage extends DBHeaderPage { // implements PageUsedBytes

	/**
	 * Default constructor
	 */
	public DBDirectoryPage() {
		super();
	}

	/**
	 * Constructor of DBDirectoryPage class
	 * 
	 * @param page
	 *            a page of Page object
	 * @exception IOException
	 */
	public DBDirectoryPage(Page page) throws IOException {
		super(page, DIR_PAGE_USED_BYTES);
	}

	/**
	 * open an exist DB directory page
	 * 
	 * @param page
	 *            a page of Page object
	 */
	public void openPage(Page page) {
		data = page.getpage();
	}

}
