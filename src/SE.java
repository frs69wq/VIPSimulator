import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;

public class SE extends Process {

	protected String hostName;

	public SE (Host host, String name, String[]args) {
		super(host,name,args);
		this.hostName = this.getHost().getName();
	}

	public void main(String[] args) {
		boolean stop = false;
		// Keep track of this process in a global list. This is used at the end
		// of the simulation to send a FINALIZE message and cleanly stop this 
		// process. 
		Msg.debug("Register SE on "+ hostName);
		VIPSimulator.getSEList().add(hostName);

		while (!stop){
			Message message = Message.getFrom(hostName);

			switch(message.getType()){
			case DOWNLOAD_REQUEST:
				// A worker asked for a physical file. It is send in an
				// asynchronous way so that this process doesn't have to wait 
				// for the completion of the transfer to handle subsequent
				// messages.
				// TODO This will have to be replaced/completed  by some I/O 
				// TODO operations at some point to increase realism.
				Message.sendAsynchronouslyTo(message.getSenderMailbox(), 
						Message.Type.SEND_FILE,
						message.getSize());

				Msg.debug("SE '"+ hostName + "' sent file '" +
						message.getLogicalName() + "' of size " +
						message.getSize() + " to '" +
						message.getSenderMailbox() + "'");
				break;
			case UPLOAD_FILE:
				// A physical file has been received (inducing a data transfer.
				// An ACK is sent back to notify the reception of the file.
				// TODO This will have to be replaced/completed  by some I/O 
				// TODO operations at some point to increase realism.
				Message.sendTo(message.getSenderMailbox(), 
						Message.Type.UPLOAD_ACK);

				Msg.debug("SE '"+ hostName + "' sent ack back to '" +
						message.getSenderMailbox() + "'");
				break;
			case FINALIZE:
				Msg.verb("Goodbye!");
				stop = true;
				break;
			default:
				break;
			}
		}
	}
}
