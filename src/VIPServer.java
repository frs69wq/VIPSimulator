import java.util.Vector;

import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.HostNotFoundException;

public class VIPServer extends Process {

	// Worker node management for registration and termination 
	private String mailbox;
	private Vector<String> gateWorkers = new Vector<String>();
	private Vector<Merge> mergeWorkers = new Vector<Merge>();
	private int endedGateWorkers = 0;
	private int runningMergeWorkers = 0;

	private long totalParticleNumber = 0;

	public String getMailbox(){
		return this.mailbox;
	}

	private void setMailbox(){
		this.mailbox = Integer.toString(this.getPID()) + "@" +
				getHost().getName();
	}

	public VIPServer(Host host, String name, String[]args) {
		super(host,name,args);
		
	}

	public void main(String[] args) throws HostFailureException, 
		HostNotFoundException {
		Msg.info("A new simulation starts!");
		boolean stop=false, timer = false;
		// Build the mailbox name from the PID and the host name. This might be 
		// useful to distinguish different Gate processes running on a same host
		setMailbox();

		// TODO what is below is very specific to GATE
		// Added to temporarily improve the realism of the simulation
		// Have to be generalized at some point.
		// WARNING: From log inspection, it seems that workers do not all get 
		// the input files from the default SE.
		LCG.crInput(getMailbox(),"inputs/gate.sh.tar.gz", 73043,
				VIPSimulator.getDefaultSE(), VIPSimulator.getDefaultLFC());
		LCG.crInput(getMailbox(),"inputs/opengate_version_7.0.tar.gz", 376927945,
				VIPSimulator.getDefaultSE(), VIPSimulator.getDefaultLFC());
		LCG.crInput(getMailbox(),"inputs/file-14539084101429.zip", 514388,
				VIPSimulator.getDefaultSE(), VIPSimulator.getDefaultLFC());

		while(!stop){
			// Use of some simulation magic here, every worker knows the 
			// mailbox of the VIP server
			GateMessage message = GateMessage.getFrom("VIPServer");

			switch (message.getType()){
			case GATE_CONNECT:
				gateWorkers.add(message.getSenderMailbox());

				Msg.debug(gateWorkers.size() +
						" GATE worker(s) registered out of " +
						VIPSimulator.numberOfGateJobs);

				GateMessage.sendTo(message.getSenderMailbox(), 
						GateMessage.Type.GATE_START);
				break;
			case MERGE_CONNECT:
				mergeWorkers.add((Merge) message.getSender());
				
				Msg.debug(mergeWorkers.size() +
						" MERGE worker(s) registered out of " +
						VIPSimulator.numberOfMergeJobs);
				break;
			case GATE_PROGRESS:
				totalParticleNumber += message.getParticleNumber();
				Msg.info (totalParticleNumber +
						" particles have been computed. "+
						VIPSimulator.totalParticleNumber + " are expected.");
				if (totalParticleNumber < VIPSimulator.totalParticleNumber){
					Msg.info("Sending a 'GATE_CONTINUE' message to '" + 
							message.getSenderMailbox() +"'");
					GateMessage.sendTo(message.getSenderMailbox(), 
							GateMessage.Type.GATE_CONTINUE);
				} else {
					if (!timer){
						Msg.info("The expected number of particles has been "+
								"reached. Start a timer!");
						new Process(this.getHost(),"Timer"){
							public void main(String[] args) throws HostFailureException {
								Process.sleep(VIPSimulator.sosTime);
								Msg.info("Time Out ! I should wake some Mergers");
								if (runningMergeWorkers < 
										VIPSimulator.numberOfMergeJobs){
									GateMessage.sendTo(mergeWorkers.firstElement().getMailbox(),
											GateMessage.Type.MERGE_START);
									runningMergeWorkers++;
								} else {
									Msg.info("No need for Merge workers");
								}
							}
						}.start();
						timer = true;
					}

					Msg.info("Sending a 'GATE_STOP' message to '" +
							message.getSenderMailbox() +"'");
					GateMessage.sendTo(message.getSenderMailbox(), 
							GateMessage.Type.GATE_STOP);
				}
				break;
			case GATE_END:
				endedGateWorkers++;
				if (endedGateWorkers == VIPSimulator.numberOfGateJobs){
					if (runningMergeWorkers < VIPSimulator.numberOfMergeJobs){
						Msg.info("All GATE workers sent a 'GATE_END' message" +
								"Wake up " + mergeWorkers.size() + 
								" Merge worker(s)");
						runningMergeWorkers++;
						GateMessage.sendTo(
								mergeWorkers.firstElement().getMailbox(),
								GateMessage.Type.MERGE_START);
					}
					// then stop
					stop=true;
				}
				break;
			default:
				break;

			}
		}


		// sleep sosTime so that tasks have the time to finish before shutting 
		// down the LFCs and SEs
		Process.sleep(VIPSimulator.sosTime);

		Msg.info("Server waited for " + VIPSimulator.sosTime/1000 +" seconds." +
				" It's time to shutdown the system.");

		// Shutting down all the LFCs
		for (String LFC : VIPSimulator.getLFCList())
			Message.sendTo(LFC, Message.Type.FINALIZE);

		// Shutting down all the SEs
		for (String SE : VIPSimulator.getSEList())
			Message.sendTo(SE, Message.Type.FINALIZE);
	}
}
