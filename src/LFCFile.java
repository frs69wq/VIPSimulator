
public class LFCFile {
	//TODO Should we add a type to distinguish REGULAR, INPUT, and MERGE files?
	public String logicalFileName;
	public long logicalFileSize;
	//TODO To be replace by a vector of Strings if we want to handle replicas
	public String SEName;

	public LFCFile(String logicalFileName, long logicalFileSize, String sEName) {
		super();
		this.logicalFileName = logicalFileName;
		this.logicalFileSize = logicalFileSize;
		SEName = sEName;
	}
	
}
