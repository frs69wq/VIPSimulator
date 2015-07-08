import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;
import org.simgrid.msg.Task;

import org.simgrid.msg.MsgException;

public class SE extends GridService {

	private SEMessage getFrom (String mailbox) {
		SEMessage message = null;
		try {
			message = (SEMessage) Task.receive(mailbox);
			Msg.debug("Received a '" + message.getType().toString() + 
					"' message from " + mailbox);
			// Simulate the cost of the local processing of the request.
			// Depends on the value set when the Message was created
			message.execute();
		} catch (MsgException e) {
			e.printStackTrace();
		}

		return message;
	}

	private void sendAckTo (String mailbox) {
		SEMessage.sendTo(mailbox, SEMessage.Type.UPLOAD_ACK, "", 0);
		Msg.debug("'SE@"+ getName() +"' sent an ACK on '" + mailbox + "'");
	}

	public void sendFileTo (String destination, long size) {
		SEMessage.sendTo(destination, SEMessage.Type.FILE_TRANSFER, null, size);
	}

	public SE (Host host, String name, String[]args) {
		super(host,name,args);
	}

	public void main(String[] args) throws MsgException {
		// Keep track of this process in a global list. This is used at the end
		// of the simulation to cleanly kill this process. 
		Msg.debug("Register SE on "+ name);
		VIPSimulator.getSEList().add(this);

		for (int i = 0; i < 10; i++){
			listeners.add(new Process(name, name+"_"+i) {
				public void main(String[] args) throws MsgException {
					String mailbox = getName();
					Msg.debug("Start a new listener on: " + mailbox);

					while (true){
						SEMessage message = getFrom(mailbox);
						
						switch(message.getType()){
						case DOWNLOAD_REQUEST:
							// A worker asked for a physical file. A data 
							// transfer of getSize() bytes occurs upon reply.
							// TODO This will have to be replaced/completed by 
							// TODO some I/O operations at some point to 
							// TODO increase realism.
							Msg.debug("SE '"+ name + "' send file '" +
									message.getFileName() + "' of size " +
									message.getSize() + " to '" +
									message.getSenderName() + "'");
							sendFileTo("return-"+mailbox, message.getSize());

							break;
						case FILE_TRANSFER:
							// A physical file has been received (inducing a 
							// data transfer). An ACK is sent back to notify  
							// the reception of the file.
							// TODO This will have to be replaced/completed by 
							// TODO some I/O operations at some point to 
							// TODO increase realism.
							sendAckTo("return-"+mailbox);
							break;
						default:
							break;
						}
					}
				}
			});
			listeners.lastElement().start();
		}
	}

	public void upload (long size) {
		String mailbox = this.findAvailableMailbox(100);
		SEMessage.sendTo(mailbox, SEMessage.Type.FILE_TRANSFER, null, size);
		Msg.info("Sent upload request of size " + size +". Waiting for an ack");
		getFrom("return-"+mailbox);
	}

	public void download(String fileName, long fileSize){
		String mailbox = this.findAvailableMailbox(100);
		SEMessage.sendTo(mailbox, SEMessage.Type.DOWNLOAD_REQUEST, 
				fileName, fileSize);
		Msg.info("Sent download request for '" + fileName + 
				"'. Waiting for reception ...");
		getFrom("return-"+mailbox);
	}
	

}

