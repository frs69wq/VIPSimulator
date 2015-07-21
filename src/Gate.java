import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;
import org.simgrid.msg.MsgException;

public class Gate extends Job {

	private long simulateForNsec(long nSec) throws HostFailureException {
		double nbPart;

		Process.sleep(nSec);
		nbPart = VIPSimulator.eventsPerSec* nSec;
		Msg.info("simulateForNsec: '"+ getName() + "' simulated "+ 
				(long) nbPart + " particles");
		//  WARNING TEMPORARY HACK FOR FIRST TEST
		return 1;
		// SHOULD BE REPLACED BY
//		return (long) (nbPart);
	}

	private void connect (){
		// Use of some simulation magic here, every worker knows the mailbox of 
		// the VIP server
		GateMessage.sendTo("VIPServer", "GATE_CONNECT", 0);
	}

	private void sendProgress(long simulatedParticles){
		// Use of some simulation magic here, every worker knows the mailbox of 
		// the VIP server
		GateMessage.sendTo("VIPServer", "GATE_PROGRESS", 
				simulatedParticles);
	}

	private void disconnect (){
		// Use of some simulation magic here, every worker knows the mailbox of 
		// the VIP server
		GateMessage.sendTo("VIPServer", "GATE_DISCONNECT", 0);
	}

	public Gate(Host host, String name, String[]args) {
		super(host,name,args);
	}

	public void main(String[] args) throws MsgException {
		//TODO have to set the name here, might be a bug in simgrid
		setName();
		long nbParticles = 0;
		long simulatedParticles = 0;
		//TODO get the output file size from logs and give it as argument of 
		// the GATE process. If no value is given, we rely on the same default
		// value as in the C version.

		int jobId = (args.length > 0 ? 
				Integer.valueOf(args[0]).intValue() : 1);
		long executionTime = (args.length > 1 ? 
				1000*Long.valueOf(args[1]).longValue() : VIPSimulator.sosTime);
		long uploadFileSize= (args.length > 2 ? 
				Long.valueOf(args[2]).longValue() : 1000000);

		Msg.info("Register GATE on '"+ getName()+ "'");
		// Use of some simulation magic here, every worker knows the mailbox of 
		// the VIP server
		this.connect();

		while (true){
			GateMessage message = (GateMessage) Message.getFrom(getName());

			switch(message.getType()){
			case "BEGIN":
				Msg.info("Processing GATE");

				// upload-test 
				// TODO to be factored at some point
				uploadTime.start();
				LCG.cr("output-0.tar.gz-uploadTest", 12, 
						"output-0.tar.gz-uploadTest", 
						getCloseSE(), VIPServer.getDefaultLFC());
				uploadTime.stop();

				// downloading inputs
				// TODO what is below is very specific to GATE
				// Added to temporarily improve the realism of the simulation
				// Have to be generalized at some point.

				downloadTime.start();
				LCG.cp("inputs/gate.sh.tar.gz", 
						"/scratch/gate.sh.tar.gz", 
						VIPServer.getDefaultLFC());
				LCG.cp("inputs/opengate_version_7.0.tar.gz", 
						"/scratch/opengate_version_7.0.tar.gz", 
						VIPServer.getDefaultLFC());
				LCG.cp("inputs/file-1032746166739830.zip", 
						"/scratch/file-1032746166739830.zip", 
						VIPServer.getDefaultLFC());
				downloadTime.stop();

			case "CARRY_ON":
				// Compute for sosTime seconds
				computeTime.start();

				//TODO Discuss what we can do here. Make the process just sleep 
				// for now
				simulatedParticles = simulateForNsec(executionTime);

				computeTime.stop();

				nbParticles += simulatedParticles;

				Msg.info("Sending computed number of particles to 'VIPServer'");
				sendProgress(simulatedParticles);

				break;
			case "END":
				Msg.info("Stopping Gate job and uploading results. " +
						nbParticles + " particles have been simulated by '" +
						getName() +"'");

				//TODO Discuss what we can do here
				//TODO what is the actual size of the generated file ?
				//TODO use the actual size obtained from the logs for now
				String logicalFileName = "results/"+ 
						Long.toString(nbParticles) +
						"-partial-"+ getName() + "-" +
						Double.toString(Msg.getClock()) + ".tgz";

				uploadTime.start();
				LCG.cr("local_file.tgz", uploadFileSize, logicalFileName, 
						getCloseSE(), VIPServer.getDefaultLFC());
				uploadTime.stop();

				Msg.info("Disconnecting GATE job. Inform VIP server.");
				this.disconnect();

				Msg.info("Spent " + downloadTime.getValue() + 
						"s downloading, " + computeTime.getValue() + 
						"s computing, and " + uploadTime.getValue() +
						"s uploading.");
				System.out.println(jobId + "," + downloadTime.getValue() + "," +
						uploadTime.getValue() + "," + computeTime.getValue() + 
						"," + (downloadTime.getValue() + uploadTime.getValue() +
								computeTime.getValue()));
				break;
			default:
				break;
			}
		}
	}

}
