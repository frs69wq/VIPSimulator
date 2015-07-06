import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

public class LogicalFile {
	private String name;
	private long size;
	private Vector<String>locations;
	private Random randomGenerator;

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
	public String getLocation() {
		//TODO return SE randomly for now. Might be interesting to implement 
		// some load balancing strategy
		int selectedIndex = randomGenerator.nextInt(locations.size());
		return locations.get(selectedIndex);
	}

	public void selectLocation(){
		//TODO return SE randomly for now. Might be interesting to implement 
		// some load balancing strategy
		String selectedLocation = getLocation();
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
		this.randomGenerator = new Random();
	}

	// In most cases, this constructor with a single location will be used.
	public LogicalFile(String name, long size, String location) {
		this(name, size, new String[] {location});
	}
}
