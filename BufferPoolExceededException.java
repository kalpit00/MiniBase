package bufmgr;

public class BufferPoolExceededException extends BufMgrException 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3879729988119933114L;

	public BufferPoolExceededException(String msg)
	{
		super(msg);
	}
}
