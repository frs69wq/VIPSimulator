import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;
import org.simgrid.msg.MsgException;

public class Gate extends Process {
	String hostName;
	String closeSEName;
	double downloadTime = 0.;
	double totalComputeTime = 0.;
	double uploadTime = 0.;
	
	public Gate(Host host, String name, String[]args) {
		super(host,name,args);
		this.hostName = host.getName();
		this.closeSEName = host.getProperty("closeSE");
	} 
	
	public void main(String[] args) throws MsgException {
		boolean stop = false;
		long nbParticles = 0;
		double computeTime;
		
		//TODO temporary hack (To be removed)
		long simulatedParticles = 10000;
		
		Msg.info("Register GATE on '"+ hostName + "'");
		GateMessage connect= new GateMessage(GateMessage.Type.GATE_CONNECT, getHost());
		// Use of some simulation magic here, every worker knows the mailbox of the VIP server
		connect.emit("VIPServer");
		
		while (!stop){
			GateMessage message = GateMessage.process(hostName);
		
			switch(message.type){
			case GATE_START:
				Msg.info("Processing GATE");
				
				//downloading inputs
				downloadTime = Msg.getClock();
				LCG.cp(getHost(), "input.tgz", "myinput.tgz", VIPSimulator.defaultLFC);
				downloadTime = Msg.getClock() - downloadTime;
			
			case GATE_CONTINUE:	
				// Compute for sosTime seconds
				computeTime = Msg.getClock();
				//TODO Discuss what we can do here. Make the process just sleep for now
				sleep(VIPSimulator.sosTime);
				computeTime = Msg.getClock() - computeTime;
				totalComputeTime += computeTime;
				
				nbParticles += simulatedParticles;
				Msg.info("Sending computed number of particles to 'VIPServer'");
				GateMessage progress = new GateMessage(GateMessage.Type.GATE_PROGRESS, getHost(), simulatedParticles);
				
				// Use of some simulation magic here, every worker knows the mailbox of the VIP server
				progress.emit("VIPServer");
				
				break;
			case GATE_STOP:
				Msg.info("Stopping Gate job and uploading results. " + nbParticles + " particles have been simulated by '" + hostName +"'");

				//TODO Discuss what we can do here
				String logicalFileName = Long.toString(nbParticles)+"-partial-"+ hostName + "-" + Double.toString(Msg.getClock()) + ".tgz";
				//TODO what is the actual size of the generated file ?
				
				uploadTime += Msg.getClock();
				LCG.cr(getHost(), closeSEName, "local_file.tgz", 1000000, logicalFileName, VIPSimulator.defaultLFC);
				uploadTime = Msg.getClock() - uploadTime;
				Msg.info("Stopping Gate job and exiting");
				
				Msg.info("Spent " + downloadTime + "s downloading, " + totalComputeTime + "s computing, and " + uploadTime + "s uploading.");
				Msg.info("Goodbye!");
				stop = true;
				break;
			default:
				break;
			}
		}
	}
}
