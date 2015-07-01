import java.util.Vector;
import org.simgrid.msg.Msg;
import org.simgrid.msg.NativeException;

public class VIPSimulator {
	private static String defaultLFC = null;
	private static String defaultSE = null;

	public static long totalParticleNumber;
	public static int numberOfGateJobs;
	public static long sosTime;
	public static int numberOfMergeJobs;
	public static int cpuMergeTime;
	public static double eventsPerSec;
	public static String logFile;

	public static Vector<String> lfcList = new Vector<String>();

	public static Vector<String> seList = new Vector<String>();

	public static String getDefaultLFC() {
		return defaultLFC;
	}

	public static void setDefaultLFC(String defaultLFC) {
		if (VIPSimulator.defaultLFC != null){
			Msg.warn("The default LFC has already been identified. Please " +
					"check there is only one 'DefaultLFC' process in the " +
					"deployement file.");
		} else {
			VIPSimulator.defaultLFC = defaultLFC;
			Msg.info("Default LFC is '"+ VIPSimulator.defaultLFC + "'");
		}
	}

	public static String getDefaultSE() {
		return defaultSE;
	}

	public static void setDefaultSE(String defaultSE) {
		if (VIPSimulator.defaultSE != null){
			Msg.warn("The default SE has already been identified. Please " +
					"check there is only one 'DefaultSE' process in the " +
					"deployement file.");
		} else {
			VIPSimulator.defaultSE = defaultSE;
			Msg.info("Default SE is '"+ VIPSimulator.getDefaultSE()+ "'");
		}
	}

	public static void main(String[] args) throws NativeException {
		Msg.init(args);

		String platf  = args.length > 1 ? args[0] : "platform.xml";
		String deploy =  args.length > 1 ? args[1] :
				"Deployment_workflow-8tkxN4_2015-04-22.xml";
		totalParticleNumber = args.length > 1 ? 
				Long.valueOf(args[2]).longValue() : 1000000;
		numberOfGateJobs = args.length > 1 ? 
				Integer.valueOf(args[3]).intValue() : 5;
		// SOS time is given in seconds on command line, but sleeps take values
		// in milliseconds.
		sosTime = 1000*(args.length > 1 ? 
				Long.valueOf(args[4]).longValue() : 300);
		numberOfMergeJobs = args.length > 1 ? 
				Integer.valueOf(args[5]).intValue() : 1;
		cpuMergeTime = args.length > 1 ? 
				Integer.valueOf(args[6]).intValue() : 10;

		eventsPerSec = args.length > 1 ? 
				Double.valueOf(args[7]).doubleValue() : 200;
		logFile = args.length > 1 ? args[8] : "logs.txt";

		Msg.info("SCENARIO: platform is " + platf + ", deployment is " + 
				deploy);
		Msg.info("PARAMS:   sostime is "+ sosTime +
				", number of Gate tasks is "+ 
				numberOfGateJobs + ", number of merge tasks is " +
				numberOfMergeJobs +", cpu merge time is " + cpuMergeTime);

		// Load the platform description 
		Msg.createEnvironment(platf);
		// and deploy the application 
		Msg.deployApplication(deploy);

		// Now, execute the simulation. 
		Msg.run();
	}
}
