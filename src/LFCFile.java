
public class LFCFile {
	//TODO Should we add a type to distinguish REGULAR, INPUT, and MERGE files?
	private String logicalFileName;
	private long logicalFileSize;
	//TODO To be replace by a vector of Strings if we want to handle replicas
	private String SEName;

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
		return SEName;
	}

	public void setSEName(String sEName) {
		SEName = sEName;
	}

	public LFCFile(String logicalFileName, long logicalFileSize, String sEName) {
		super();
		this.setLogicalFileName(logicalFileName);
		this.setLogicalFileSize(logicalFileSize);
		setSEName(sEName);
	}
	
}
