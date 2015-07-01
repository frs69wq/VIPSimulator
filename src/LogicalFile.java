import java.util.Arrays;
import java.util.Vector;

public class LogicalFile {
	private String name;
	private long size;
	private Vector<String>locations;

	public String getName() {
		return name;
	}

	public long getSize() {
		return size;
	}

	public void addLocation (String seName) {
		locations.add(seName);
	}

	public Vector<String> getLocations() {
		return locations;
	}

	//TODO These two functions might be redundant (and/or misleading)
	//TODO To be improved once we know more about the LFC internal algorithms.
	public String getSEName() {
		//TODO return the first SE for now. Might be interesting to implement 
		// some load balancing strategy
		return locations.get(0);
	}

	public void selectLocation(){
		//TODO return the first SE for now. Might be interesting to implement 
		// some load balancing strategy
		String selectedLocation = getSEName();
		locations.clear();
		locations.add(selectedLocation);
	}

	public String toString(){
		return  "file '" + name + "' of size " + size + " stored on " + 
				locations.toString();
	}

	@Override
	public boolean equals(Object obj) {
		LogicalFile file= (LogicalFile) obj;
		return name.equals(file.getName());
	}

	public LogicalFile(String name, long size, String[] locations) {
		super();
		this.name = name;
		this.size = size;
		this.locations = new Vector<String>();
		this.locations.addAll(Arrays.asList(locations));
	}

	// In most cases, this constructor with a single location will be used.
	public LogicalFile(String name, long size, String location) {
		this(name, size, new String[] {location});
	}
}
