import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.simgrid.msg.Comm;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.Process;
import org.simgrid.msg.Task;

public class LFC extends Process {

	// A Logical File Catalog service is defined by:
	// hostName: the name of the host that runs the service
	// catalog: a vector of logical files
	protected String name;
	private Vector<LogicalFile> catalog;

	public String getName() {
		return name;
	}

	private Vector<String> mailboxes;
	private Vector<Process> listeners;
	
	// TODO WIP: try to have several listeners on the LFC to handle more than 
	// TODO one request at a time.
	public Vector<String> getMailboxes() {
		return mailboxes;
	}

	// A simulation can begin with some logical files referenced in the LFC.
	// In that case, the LFC process is launched with an argument which is the
	// name of a CSV file stored in working directory that contains logical 
	// file description in the following format:
	// name,size,se_1<:se_2:...:se_n>
	// The populate function reads and parses that file, create LogicalFile 
	// objects and add them to the local catalog.
	private void populate(String csvFile){
		Msg.info("Population of LFC '"+ name + "' from '"+ csvFile + "'");

		try {
			BufferedReader br = new BufferedReader(new FileReader(csvFile));
			String line = "";

			while ((line = br.readLine()) != null) {
				String[] fileInfo = line.split(",");
				String[] seNames = fileInfo[2].split(":");
				LogicalFile file = new LogicalFile(fileInfo[0], 
						Long.valueOf(fileInfo[1]).longValue(), seNames);
				Msg.info("Importing file '" + file.toString());
				catalog.add(file);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// A worker might want to register a new logical file, or a replica on an 
	// existing file in the catalog. This function first checks if a file with
	// the same name already exists. If it does, it determines whether it is a
	// new replica or not. Otherwise, it creates a new entry in the catalog for
	// that file.
	private void addtoCatalog(LogicalFile newFile){
		Msg.debug("Inserting '"+ newFile.toString() + "'into the catalog");
		if (catalog.contains((Object) newFile)){
			LogicalFile file = catalog.get(catalog.indexOf(newFile));
			if (!file.getLocations().contains((Object) newFile.getLocation())){
				// This has to be a new replica
				Msg.info("New replica for '" + newFile.getName () + "' on '" + 
						newFile.getLocation() + "'");
				file.addLocation(newFile.getLocation());
			} else {
				Msg.debug(file.toString() + "is already registered");
			}
		} else {
			// This file is not registered yet, create and add it
			Msg.debug ("'" + newFile.getName() + "' is not registered yet");
			catalog.add(newFile);
		}
		Msg.debug("LFC '"+ name + "' registered " + newFile.toString());
	}

	public LFC(Host host, String name, String[]args) {
		super(host,name,args);
		this.name = getHost().getName();
		this.catalog = new Vector<LogicalFile>();
		// TODO WIP: try to have several listeners on the LFC to handle more 
		// TODO than one request at a time.
		this.mailboxes = new Vector<String>();
		this.listeners = new Vector<Process>();
		for (int i=0; i<3; this.mailboxes.add(this.name+"_"+i++));
	}

	public void main(String[] args) throws MsgException {
		Msg.debug("Register LFC on "+ name);
		VIPSimulator.getLFCList().add(this);

		// If this LFC process is started with an argument, we populate the
		// catalog from the CSV file given as args[0]
		String csvFile = (args.length > 0 ? args[0] : null);

		if (csvFile != null){
			populate(csvFile);
			Msg.debug(this.toString());
		}
		
		for (String mailbox : mailboxes){
			listeners.add(new Process(name, "Listener", new String[] {mailbox}){
				public void main(String[] args) throws MsgException {
					String mailbox =  args[0];
					boolean stop = false;
					
					Msg.info("Start a new listener on: " + mailbox);
					Comm listener = Task.irecv(mailbox);

					while (!stop){
						listener.waitCompletion();
						LFCMessage message = (LFCMessage) listener.getTask();
						Msg.debug ("Received " + message.getType() + " from " + 
								message.getSenderMailbox() + " in "+ mailbox);
						message.execute();

						if (message.getType() == LFCMessage.Type.FINALIZE) {
							Msg.debug("Stop listening on "+ mailbox);
							stop = true;
						} else {
							switch(message.getType()){
							case REGISTER_FILE:
								// Add an entry in the catalog for the received 
								// logical file, if needed.
								// Then send back an ACK to the the sender.
								addtoCatalog(message.getFile());
								LFCMessage.sendTo("return-"+mailbox, 
										LFCMessage.Type.REGISTER_ACK);
								Msg.debug("LFC '"+ name + "' sent back an ACK" +
										" on 'return-" + mailbox + "'");
								break;
							case ASK_LOGICAL_FILE:
								LogicalFile file = 
									getLogicalFileByName(
											message.getLogicalName());

								// If this logical file is available on several 
								// locations, one is selected before returning 
								// the file to the worker asking for it.
								file.selectLocation();
								LFCMessage.sendTo("return-"+mailbox, 
										LFCMessage.Type.SEND_LOGICAL_FILE, 
										file);
								Msg.debug("LFC '"+ name + 
										"' sent logical " + file.toString() + 
										" back on 'return-" + mailbox + "'");
								break;
							case ASK_LS:
								Vector<LogicalFile> directoryContents = 
									new Vector<LogicalFile>();
								for (LogicalFile f : catalog) 
									if (f.getName().matches(
											message.getLogicalName()+"(.*)"))
										directoryContents.add(f);
								LFCMessage.sendTo("return-"+mailbox, 
										LFCMessage.Type.SEND_LS,
										directoryContents);
								break;
							default:
								break;
							}
							listener = Task.irecv(mailbox);
							Msg.debug("New irecv posted on "+ mailbox);
							
						}
					}
				}
			});
		}
		for(Process p : listeners)
			p.start();
	}

	public LogicalFile getLogicalFileByName (String logicalFileName) {
		LogicalFile file = catalog.get(catalog.indexOf((Object) 
				new LogicalFile (logicalFileName, 0, "")));
		if(file == null){
			Msg.error("File '" + logicalFileName + 
					"' is stored on no SE. Exiting with status 1");
			System.exit(1);
		}

		return file;
	}

	public String getSEName (String logicalFileName){
		return getLogicalFileByName(logicalFileName).getLocation();
	}

	public String toString () {
		return catalog.toString();
	}

	public void kill(){
		for (String mailbox : mailboxes){
			LFCMessage.sendTo(mailbox, LFCMessage.Type.FINALIZE);
		}
	}
	
	private String findAvailableMailbox(){
		while (true){
			for (String mailbox : this.getMailboxes()){
				if (Task.listen(mailbox)){
					Msg.info("Send a message to : " + mailbox + 
							" which is listening");
					return mailbox;
				}
			}
			Msg.warn("All the listeners are busy to register. Try Again!");
			try {
				Process.sleep(10);
			} catch (HostFailureException e) {
				e.printStackTrace();
			}
		}
	}

	public void register (LogicalFile file) {
		String mailbox = this.findAvailableMailbox();
		LFCMessage.sendTo(mailbox, LFCMessage.Type.REGISTER_FILE, file);
		LFCMessage.getFrom(mailbox);
	}

	public LogicalFile getLogicalFile (String logicalFileName) {
		String mailbox = this.findAvailableMailbox();
		LFCMessage.sendTo(mailbox, LFCMessage.Type.ASK_LOGICAL_FILE, 
				logicalFileName);
		Msg.info("Asked about '" + logicalFileName + 
				"'. Waiting for information ...");

		LFCMessage m = LFCMessage.getFrom(mailbox);
		return m.getFile();
	}

	public Vector<LogicalFile> getLogicalFileList(String directoryName){
		String mailbox = this.findAvailableMailbox();
		LFCMessage.sendTo(mailbox, LFCMessage.Type.ASK_LS, directoryName);
		Msg.info("Asked for list of files to merge in '" + directoryName + 
				"'. Waiting for reply ...");
		LFCMessage m = LFCMessage.getFrom(mailbox);
		return m.getFileList();
	}
}