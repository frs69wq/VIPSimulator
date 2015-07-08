import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;
import org.simgrid.msg.MsgException;

public class SE extends GridService {

	public SE (Host host, String name, String[]args) {
		super(host,name,args);
	}

	public void main(String[] args) throws MsgException {
		// Keep track of this process in a global list. This is used at the end
		// of the simulation to send a FINALIZE message and cleanly stop this 
		// process. 
		Msg.debug("Register SE on "+ name);
		VIPSimulator.getSEList().add(this);

		for (int i=0; i<10; i++){
			listeners.add(new Process(name, name+"_"+i) {
				public void main(String[] args) throws MsgException {
					String mailbox = getName();
					Msg.debug("Start a new listener on: " + mailbox);

					while (true){
						SEMessage message = SEMessage.getFrom(mailbox);
						
						switch(message.getType()){
						case DOWNLOAD_REQUEST:
							// A worker asked for a physical file. It is send 
							// in an asynchronous way so that this listener
							// doesn't have to wait for the completion of the 
							// transfer to handle subsequent  messages.
							// TODO This will have to be replaced/completed by 
							// TODO some I/O operations at some point to 
							// TODO increase realism.
							Msg.debug("SE '"+ name + "' send file '" +
									message.getLogicalName() + "' of size " +
									message.getSize() + " to '" +
									message.getSenderMailbox() + "'");
							SEMessage.sendTo("return-"+mailbox, 
									SEMessage.Type.SEND_FILE,
									message.getSize());

							break;
						case UPLOAD_FILE:
							// A physical file has been received (inducing a 
							// data transfer. An ACK is sent back to notify the 
							// reception of the file.
							// TODO This will have to be replaced/completed by 
							// TODO some I/O operations at some point to 
							// TODO increase realism.
							SEMessage.sendTo("return-"+mailbox, 
									SEMessage.Type.UPLOAD_ACK);

							Msg.debug("SE '"+ name + "' sent ack back to '" +
									message.getSenderMailbox() + "'");
							break;
						default:
							break;
						}
					}
				}
			});
		}
		for(Process p : listeners)
			p.start();
	}

	public void upload (long size) {
		String mailbox = this.findAvailableMailbox(100);
		SEMessage.sendTo(mailbox, SEMessage.Type.UPLOAD_FILE, 
				size);
		Msg.info("Sent upload request of size " + size +". Waiting for an ack");
		SEMessage.getFrom("return-"+mailbox);
	}

	public void download(String fileName, long fileSize){
		String mailbox = this.findAvailableMailbox(100);
		SEMessage.sendTo(mailbox, SEMessage.Type.DOWNLOAD_REQUEST, 
				fileName, fileSize);
		Msg.info("Sent download request for '" + fileName + 
				"'. Waiting for reception ...");
		SEMessage.getFrom("return-"+mailbox);
	}
}

