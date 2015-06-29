import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;
import org.simgrid.msg.MsgException;

public class Gate extends Process {
	private String mailbox;
	private String closeSEName;
	private double downloadTime = 0.;
	private double totalComputeTime = 0.;
	private double uploadTime = 0.;

	private void setMailbox(){
		this.mailbox = Integer.toString(this.getPID()) + "@" +
				getHost().getName();
	}

	public String getMailbox(){
		return this.mailbox;
	}

	private long simulateForNsec(long nSec) throws HostFailureException {
		double nbPart;

		Process.sleep(nSec);
		nbPart = VIPSimulator.eventsPerSec* nSec;
		Msg.info("simulateForNsec: '"+ mailbox + "' simulated "+ 
				(long) nbPart + " particles");

		return (long) (nbPart);

	}

	public Gate(Host host, String name, String[]args) {
		super(host,name,args);
		this.closeSEName = host.getProperty("closeSE");
	}

	public void main(String[] args) throws MsgException {
		boolean stop = false;
		long nbParticles = 0;
		long simulatedParticles = 0;
		double computeTime;
		// Build the mailbox name from the PID and the host name. This might be 
		// useful to distinguish different Gate processes running on a same host
		setMailbox();

		//TODO get the output file size from logs and give it as argument of 
		// the GATE process. If no value is given, we rely on the same default
		// value as in the C version.
		long uploadFileSize= (args.length > 0 ? 
				Long.valueOf(args[0]).longValue() : 1000000);

		Msg.info("Register GATE on '"+ mailbox + "'");
		GateMessage connect= new GateMessage(GateMessage.Type.GATE_CONNECT, 
				getMailbox());
		// Use of some simulation magic here, every worker knows the mailbox of 
		// the VIP server
		connect.emit("VIPServer");
		
		while (!stop){
			GateMessage message = GateMessage.process(mailbox);

			switch(message.getType()){
			case GATE_START:
				Msg.info("Processing GATE");

				// downloading inputs
				// TODO what is below is very specific to GATE
				// Added to temporarily improve the realism of the simulation
				// Have to be generalized at some point.

				downloadTime = Msg.getClock();
				LCG.cp(getHost(), "gate.sh.tar.gz", "/scratch/gate.sh.tar.gz",
						VIPSimulator.defaultLFC);
				LCG.cp(getHost(), "opengate_version_7.0.tar.gz", 
						"/scratch/opengate_version_7.0.tar.gz", 
						VIPSimulator.defaultLFC);
				LCG.cp(getHost(), "file-14539084101429.zip", 
						"/scratch/file-14539084101429.zip", 
						VIPSimulator.defaultLFC);
				downloadTime = Msg.getClock() - downloadTime;

			case GATE_CONTINUE:	
				// Compute for sosTime seconds
				computeTime = Msg.getClock();

				//TODO Discuss what we can do here. Make the process just sleep 
				// for now
				simulatedParticles = simulateForNsec(VIPSimulator.sosTime);
				
				computeTime = Msg.getClock() - computeTime;
				totalComputeTime += computeTime;
				
				nbParticles += simulatedParticles;
				Msg.info("Sending computed number of particles to 'VIPServer'");
				GateMessage progress = 
						new GateMessage(GateMessage.Type.GATE_PROGRESS, 
								getMailbox(), simulatedParticles);

				// Use of some simulation magic here, every worker knows the
				// mailbox of the VIP server
				progress.emit("VIPServer");

				break;
			case GATE_STOP:
				Msg.info("Stopping Gate job and uploading results. " +
						nbParticles + " particles have been simulated by '" +
						mailbox +"'");

				//TODO Discuss what we can do here
				//TODO what is the actual size of the generated file ?
				//TODO use the actual size obtained from the logs for now
				String logicalFileName = Long.toString(nbParticles) +
						"-partial-"+ mailbox + "-" +
						Double.toString(Msg.getClock()) + ".tgz";

				uploadTime += Msg.getClock();
				LCG.cr(getHost(), closeSEName, "local_file.tgz", uploadFileSize,
						logicalFileName, VIPSimulator.defaultLFC);
				uploadTime = Msg.getClock() - uploadTime;
				Msg.info("Stopping Gate job and exiting");

				Msg.info("Spent " + downloadTime + "s downloading, " +
						totalComputeTime + "s computing, and " + uploadTime +
						"s uploading.");

				Msg.verb("Goodbye!");
				stop = true;
				break;
			default:
				break;
			}
		}
	}

}
