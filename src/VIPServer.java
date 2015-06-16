import java.util.Iterator;
import java.util.Vector;

import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;
import org.simgrid.msg.HostFailureException;

public class VIPServer extends Process {
	
	
	//TODO This input file should become a parameter, IMHO
	public String inputFileName = "input.tgz";
	public long inputFileSize = 50000000;
	public Vector<Host> gateSlaves = new Vector<Host>();
	
	int registeredGateJobs = 0;
	int endedGateJobs = 0;
	
	long totalParticleNumber = 0;
	public VIPServer(Host host, String name, String[]args) {
		super(host,name,args);
	} 
	
	public void main(String[] args) throws HostFailureException {
		Msg.info("A new simulation starts!");
		boolean stop=false;

		LCG.crInput(getHost(),inputFileName, inputFileSize, VIPSimulator.defaultSE, VIPSimulator.defaultLFC);

		// Wait for slaves to register
		while(!stop){
			// Use of some simulation magic here, every worker knows the mailbox of the VIP server
			GateMessage message = GateMessage.process("VIPServer");
			
			switch (message.type){
			case GATE_CONNECT:
				registeredGateJobs++;
				Msg.info(registeredGateJobs +" worker(s) registered out of " + VIPSimulator.numberOfGateJobs);

				GateMessage start = new GateMessage(GateMessage.Type.GATE_START, getHost());
				start.emit(message.issuerHost.getName());
				break;
			case GATE_PROGRESS:
				totalParticleNumber += message.particleNumber;
				Msg.info (totalParticleNumber + " particles have been computed. "+ VIPSimulator.totalParticleNumber + " are expected.");
				if (totalParticleNumber < VIPSimulator.totalParticleNumber){
					GateMessage again = new GateMessage(GateMessage.Type.GATE_CONTINUE);
					Msg.info("Sending a '" + again.type.toString() +"' message to '" + message.issuerHost.getName() +"'");
					again.emit(message.issuerHost.getName());
				} else {
					GateMessage endGate = new GateMessage(GateMessage.Type.GATE_STOP);
					Msg.info("Sending a '" + endGate.type.toString() +"' message to '" + message.issuerHost.getName() +"'");
					endGate.emit(message.issuerHost.getName());
					endedGateJobs++;
				}
				
				if (endedGateJobs == VIPSimulator.numberOfGateJobs){
					Msg.info("Exiting the interaction loop with " + endedGateJobs + " ended jobs");
					stop=true;
				}
				
				break;
			default:
				break;

			}
		}


		//sleep sosTime so that tasks have the time to finish before shutting down the LFCs and SEs
		Process.sleep(VIPSimulator.sosTime);

		// Shutting down all the LFCs
		Iterator<Host> it = VIPSimulator.lfcList.iterator();			
		while (it.hasNext()){
			Message endLFC = new Message(Message.Type.FINALIZE);
			endLFC.emit(it.next().getName());
		}

		// Shutting down all the SEs
		it = VIPSimulator.seList.iterator();			
		while (it.hasNext()){
			Message endSE = new Message(Message.Type.FINALIZE);
			endSE.emit(it.next().getName());
		}
	}
}
