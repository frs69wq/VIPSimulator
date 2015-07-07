import java.util.Vector;
import org.simgrid.msg.Msg;
import org.simgrid.msg.NativeException;

public class VIPSimulator {
	private static LFC defaultLFC = null;
	private static Vector<LFC> lfcList = new Vector<LFC>();
	private static String defaultSE = null;
	private static Vector<SE> seList = new Vector<SE>();

	public static long totalParticleNumber;
	public static int numberOfGateJobs;
	public static long sosTime;
	public static int numberOfMergeJobs;
	public static int cpuMergeTime;
	public static double eventsPerSec;
	public static String logFile;

	public static LFC getDefaultLFC() {
		return defaultLFC;
	}

	public static void setDefaultLFC(LFC defaultLFC) {
		if (VIPSimulator.defaultLFC != null){
			Msg.warn("The default LFC has already been identified. Please " +
					"check there is only one 'DefaultLFC' process in the " +
					"deployement file.");
		} else {
			VIPSimulator.defaultLFC = defaultLFC;
			Msg.info("Default LFC is '"+ 
					VIPSimulator.defaultLFC.getName() + "'");
		}
	}

	public static Vector<LFC> getLFCList() {
		return lfcList;
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

	public static Vector<SE> getSEList() {
		return seList;
	}

	public static SE getSEbyName(String seName){
		SE notFound = null;
		for (SE se : seList) 
			if (se.getName().matches(seName))
				return se;
		Msg.error("Cannot find an SE named '" + seName +"'");
		return notFound;
	}
	
	public static void main(String[] args) throws NativeException {
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
