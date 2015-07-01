import java.util.Iterator;
import java.util.Vector;

import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;
import org.simgrid.msg.HostFailureException;

public class VIPServer extends Process {

	// Worker node management for registration and termination 
	private String mailbox;
	private Vector<String> gateWorkers = new Vector<String>();
	private Vector<Merge> mergeWorkers = new Vector<Merge>();
	private int endedGateWorkers = 0;

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

	public void main(String[] args) throws HostFailureException {
		Msg.info("A new simulation starts!");
		boolean stop=false;
		// Build the mailbox name from the PID and the host name. This might be 
		// useful to distinguish different Gate processes running on a same host
		setMailbox();
		// TODO what is below is very specific to GATE
		// Added to temporarily improve the realism of the simulation
		// Have to be generalized at some point.
		// WARNING: From log inspection, it seems that workers do not all get 
		// the input files from the default SE.
		LCG.crInput(getMailbox(),"gate.sh.tar.gz", 73043,
				VIPSimulator.getDefaultSE(), VIPSimulator.getDefaultLFC());
		LCG.crInput(getMailbox(),"opengate_version_7.0.tar.gz", 376927945,
				VIPSimulator.getDefaultSE(), VIPSimulator.getDefaultLFC());
		LCG.crInput(getMailbox(),"file-14539084101429.zip", 514388,
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
					Msg.info("Sending a 'GATE_STOP' message to '" +
							message.getSenderMailbox() +"'");
					GateMessage.sendTo(message.getSenderMailbox(), 
							GateMessage.Type.GATE_STOP);
					endedGateWorkers++;
				}

				if (endedGateWorkers == gateWorkers.size()){
					Msg.info("All GATE workers received a 'GATE_STOP' message" +
							"Wake up Merge" + mergeWorkers.size() + 
							" worker(s)");

					GateMessage.sendTo(mergeWorkers.firstElement().getMailbox(),
							GateMessage.Type.MERGE_START);
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
		Iterator<String> itLFC = VIPSimulator.getLFCList().iterator();
		while (itLFC.hasNext()){
			Message.sendTo(itLFC.next(), Message.Type.FINALIZE);
		}

		// Shutting down all the SEs
		Iterator<String> itSE = VIPSimulator.getSEList().iterator();
		while (itSE.hasNext()){
			Message.sendTo(itSE.next(), Message.Type.FINALIZE);
		}
	}
}
