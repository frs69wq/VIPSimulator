import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;
import org.simgrid.msg.MsgException;

public class Gate extends Job {
	private double downloadTime = 0.;
	private double totalComputeTime = 0.;
	private double uploadTime = 0.;

	private long simulateForNsec(long nSec) throws HostFailureException {
		double nbPart;

		Process.sleep(nSec);
		nbPart = VIPSimulator.eventsPerSec* nSec;
		Msg.info("simulateForNsec: '"+ getMailbox() + "' simulated "+ 
				(long) nbPart + " particles");

		return (long) (nbPart);
	}

	private void connect (){
		// Use of some simulation magic here, every worker knows the mailbox of 
		// the VIP server
		GateMessage.sendTo("VIPServer",Message.Type.GATE_CONNECT, 0);
	}

	private void sendProgress(long simulatedParticles){
		// Use of some simulation magic here, every worker knows the mailbox of 
		// the VIP server
		GateMessage.sendTo("VIPServer", Message.Type.GATE_PROGRESS, 
				simulatedParticles);
	}

	private void disconnect (){
		// Use of some simulation magic here, every worker knows the mailbox of 
		// the VIP server
		GateMessage.sendTo("VIPServer",Message.Type.GATE_DISCONNECT, 0);
	}

	public Gate(Host host, String name, String[]args) {
		super(host,name,args);
	}

	public void main(String[] args) throws MsgException {
		long nbParticles = 0;
		long simulatedParticles = 0;
		double computeTime;
		// Build the mailbox name from the PID and the host name. This might be 
		// useful to distinguish different Gate processes running on a same host
		setMailbox();

		//TODO get the output file size from logs and give it as argument of 
		// the GATE process. If no value is given, we rely on the same default
		// value as in the C version.

		int jobId = (args.length > 0 ? 
				Integer.valueOf(args[0]).intValue() : 1);
		long executionTime = (args.length > 1 ? 
				1000*Long.valueOf(args[1]).longValue() : VIPSimulator.sosTime);
		long uploadFileSize= (args.length > 2 ? 
				Long.valueOf(args[2]).longValue() : 1000000);

		Msg.info("Register GATE on '"+ getMailbox()+ "'");
		// Use of some simulation magic here, every worker knows the mailbox of 
		// the VIP server
		this.connect();

		while (true){
			GateMessage message = getFrom(getMailbox());

			switch(message.getType()){
			case START:
				Msg.info("Processing GATE");

				// downloading inputs
				// TODO what is below is very specific to GATE
				// Added to temporarily improve the realism of the simulation
				// Have to be generalized at some point.

				downloadTime = Msg.getClock();
				LCG.cp("inputs/gate.sh.tar.gz", 
						"/scratch/gate.sh.tar.gz", 
						VIPSimulator.getDefaultLFC());
				LCG.cp("inputs/opengate_version_7.0.tar.gz", 
						"/scratch/opengate_version_7.0.tar.gz", 
						VIPSimulator.getDefaultLFC());
				LCG.cp("inputs/file-14539084101429.zip", 
						"/scratch/file-14539084101429.zip", 
						VIPSimulator.getDefaultLFC());
				downloadTime = Msg.getClock() - downloadTime;

			case CARRY_ON:	
				// Compute for sosTime seconds
				computeTime = Msg.getClock();

				//TODO Discuss what we can do here. Make the process just sleep 
				// for now
				simulatedParticles = simulateForNsec(executionTime);

				computeTime = Msg.getClock() - computeTime;
				totalComputeTime += computeTime;

				nbParticles += simulatedParticles;

				Msg.info("Sending computed number of particles to 'VIPServer'");
				sendProgress(simulatedParticles);

				break;
			case STOP:
				Msg.info("Stopping Gate job and uploading results. " +
						nbParticles + " particles have been simulated by '" +
						getMailbox() +"'");

				//TODO Discuss what we can do here
				//TODO what is the actual size of the generated file ?
				//TODO use the actual size obtained from the logs for now
				String logicalFileName = "results/"+ 
						Long.toString(nbParticles) +
						"-partial-"+ getMailbox() + "-" +
						Double.toString(Msg.getClock()) + ".tgz";

				uploadTime = Msg.getClock();
				LCG.cr("local_file.tgz", uploadFileSize, logicalFileName, 
						getCloseSE(), VIPSimulator.getDefaultLFC());
				uploadTime = Msg.getClock() - uploadTime;

				Msg.info("Disconnecting GATE job. Inform VIP server.");
				this.disconnect();

				Msg.info("Spent " + downloadTime + "s downloading, " +
						totalComputeTime + "s computing, and " + uploadTime +
						"s uploading.");
				//	System.out.println(jobId + "," + downloadTime + "," + 
				//			uploadTime + "," + executionTime + "," + 
				//			downloadTime+uploadTime+executionTime));
				break;
			default:
				break;
			}
		}
	}

}
