package bufmgr;

/**
 * A frame descriptor; contains info about each page in the buffer pool.
 */
public class FrameDescriptor 
{
	/** Identifies the frame's page. */
	public int pageno = -1;

	/** The frame's pin count. */
	public int pinCount;

	/** The frame's dirty status. */
	public boolean dirtyBit;
}
