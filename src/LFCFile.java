import java.util.Vector;


public class LFCFile {
	//TODO Should we add a type to distinguish REGULAR, INPUT, and MERGE files?
	private String logicalFileName;
	private long logicalFileSize;
	private Vector<String>SENames;

	public String getLogicalFileName() {
		return logicalFileName;
	}

	public void setLogicalFileName(String logicalFileName) {
		this.logicalFileName = logicalFileName;
	}

	public long getLogicalFileSize() {
		return logicalFileSize;
	}

	public void setLogicalFileSize(long logicalFileSize) {
		this.logicalFileSize = logicalFileSize;
	}

	public String getSEName() {
		//TODO return the first SE for now. Might be interesting to implement 
		// some load balancing strategy
		return SENames.get(0);
	}

	public void setSEName(String SEName) {
		SENames.add(SEName);
	}

	public LFCFile(String logicalFileName, long logicalFileSize, 
			String SEName) {
		super();
		this.setLogicalFileName(logicalFileName);
		this.setLogicalFileSize(logicalFileSize);
		this.SENames = new Vector<String>();
		setSEName(SEName);
	}

}
