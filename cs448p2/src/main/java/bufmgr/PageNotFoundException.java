package bufmgr;

public class PageNotFoundException extends BufMgrException
{
	private static final long serialVersionUID = 8527332129053870174L;

	public PageNotFoundException(String msg)
	{
		super(msg);
	}

}
