import java.util.Vector;

import org.simgrid.msg.Host;
import org.simgrid.msg.Msg;
import org.simgrid.msg.NativeException;

public class VIPSimulator {
	public static long totalParticleNumber;
	public static int numberOfGateJobs;
	public static long sosTime;
	public static int numberOfMergeJobs;
	public static int cpuMergeTime;
	public static double eventsPerSec;
	public static String logFile;

	public static String defaultLFC = null;
	public static Vector<Host> lfcList = new Vector<Host>();

	public static String defaultSE = null;
	public static Vector<Host> seList = new Vector<Host>();

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
