import java.util.Arrays;
import java.util.Vector;


public class LogicalFile {
	//TODO Should we add a type to distinguish REGULAR, INPUT, and MERGE files?
	private String name;
	private long size;
	private Vector<String>locations;

	public String getName() {
		return name;
	}

	public void setName(String logicalFileName) {
		this.name = logicalFileName;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long logicalFileSize) {
		this.size = logicalFileSize;
	}

	public void addLocation (String seName) {
		locations.add(seName);
	}

	public String getSEName() {
		//TODO return the first SE for now. Might be interesting to implement 
		// some load balancing strategy
		return locations.get(0);
	}

	public Vector<String> getLocations() {
		return locations;
	}

	public void selectLocation(){
		//TODO return the first SE for now. Might be interesting to implement 
		// some load balancing strategy
		String selectedLocation = locations.get(0);
		locations.clear();
		locations.add(selectedLocation);
	}

	public String toString(){
		return  "File '" + name + "' of size " + size + 
				" stored on " + locations.toString();
	}

	@Override
	public boolean equals(Object obj) {
		LogicalFile file= (LogicalFile) obj;
		return name.equals(file.getName());
	}

	public LogicalFile(String logicalFileName, long logicalFileSize, 
			String SEName) {
		super();
		this.setName(logicalFileName);
		this.setSize(logicalFileSize);
		this.locations = new Vector<String>();
		this.locations.add(SEName);
	}

	public LogicalFile(String logicalFileName, long logicalFileSize, 
			String[] seNames) {
		super();
		this.setName(logicalFileName);
		this.setSize(logicalFileSize);
		this.locations = new Vector<String>();
		this.locations.addAll(Arrays.asList(seNames));
	}
}
