import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;

public class SE extends Process {
	
	public String hostName;
	
	public SE (Host host, String name, String[]args) {
		super(host,name,args);
		this.hostName = this.getHost().getName();
	}
	
	public void main(String[] args) {
		boolean stop = false;
		Msg.debug("Register SE on "+ this.hostName);
		VIPSimulator.seList.add(this.getHost());

		while (!stop){
			Message message = Message.process(hostName);
			
			switch(message.type){
			case DOWNLOAD_REQUEST:
				handleDownloadRequest(message);
				break;
			case UPLOAD_REQUEST:
				handleUploadRequest(message);
				break;
			case FINALIZE:
				Msg.info("Goodbye!");
				stop = true;
				break;
			default:
				break;
			}
		}
	}
	
	public void handleUploadRequest(Message message) {
		//TODO this function should a file on disk at some point
		Message uploadAck= new Message(Message.Type.UPLOAD_ACK);
		
		uploadAck.emit(message.issuerHost.getName());
		Msg.debug("SE '"+ hostName + "' sent ack back to '" + message.issuerHost.getName() + "'");
	}

	public void handleDownloadRequest(Message message) {
		Message sendFile= new Message(Message.Type.SEND_FILE, message.logicalFileSize);
		sendFile.emit(message.issuerHost.getName());
		Msg.debug("SE '"+ hostName + "' sent file '" + message.logicalFileName +"' of size " + message.logicalFileSize + " to '" + message.issuerHost.getName() + "'");
	}
}
