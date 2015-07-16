import org.simgrid.msg.Msg;

public class VIPSimulator {
	public static long totalParticleNumber;
	public static int numberOfGateJobs;
	public static long sosTime;
	public static int numberOfMergeJobs;
	public static int cpuMergeTime;
	public static double eventsPerSec;
	public static String logFile;

	public static void main(String[] args) {
		Msg.init(args);
		String platform_file  = null;
		String deployment_file =  null;

		if (args.length < 2){
			Msg.error("This simulator requires at least a platform and a " + 
					"deployment files tu run");
			System.exit(1);
		} else {
			platform_file  = args[0];
			deployment_file =  args[1];
			Msg.info("SCENARIO: platform is " + platform_file + 
					"', deployment is '" + deployment_file+ "'");
		}

		totalParticleNumber = args.length > 2 ? 
				Long.valueOf(args[2]).longValue() : 1000000;
		numberOfGateJobs = args.length > 3 ? 
				Integer.valueOf(args[3]).intValue() : 5;
		// SOS time is given in seconds on command line, but sleeps take values
		// in milliseconds.
		sosTime = 1000*(args.length > 4 ? 
				Long.valueOf(args[4]).longValue() : 300);
		numberOfMergeJobs = args.length > 5 ? 
				Integer.valueOf(args[5]).intValue() : 1;
		cpuMergeTime = args.length > 6 ? 
				Integer.valueOf(args[6]).intValue() : 10;

		eventsPerSec = args.length > 7 ? 
				Double.valueOf(args[7]).doubleValue() : 200;
		logFile = args.length > 8 ? args[8] : "logs.txt";

		Msg.info("PARAMS:   sostime is "+ sosTime +
				", number of Gate tasks is "+ 
				numberOfGateJobs + ", number of merge tasks is " +
				numberOfMergeJobs +", cpu merge time is " + cpuMergeTime);

		// Load the platform description 
		Msg.createEnvironment(platform_file);
		// and deploy the application 
		Msg.deployApplication(deployment_file);

		// Now, execute the simulation. 
		Msg.run();
	}
}
