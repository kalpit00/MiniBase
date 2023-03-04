package diskmgr;

public class OutOfSpaceException extends DiskMgrException {

  /**
	 * 
	 */
	private static final long serialVersionUID = 2560165916249758898L;

public OutOfSpaceException(String msg)
    { 
      super(msg); 
    }
}

