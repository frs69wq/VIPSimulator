import org.simgrid.msg.Msg;

public class LCG {

	public static void crInput(String mailbox, String logicalFileName,
			long logicalFileSize, String SEName, String LFCName) {
		LogicalFile file = new LogicalFile(logicalFileName, logicalFileSize, 
				SEName);
		Msg.info("Ask '"+ LFCName + "' to register " + file.toString());

		Message.sendTo(LFCName, Message.Type.REGISTER_FILE, file);
		Message.getFrom(mailbox);

		Msg.debug("lcg-cr-input of '" + logicalFileName +"' on LFC '" + 
				LFCName +"' completed");
	}

	public static void cr(String mailbox, String SEName, String localFileName,
			long localFileSize, String logicalFileName, String LFCName) {
		Msg.debug("lcg-cr '" + logicalFileName + "' from '" + localFileName + 
				"' using lfc '" + LFCName +"'");

		// upload file to SE
		Message.sendAsynchronouslyTo(SEName, Message.Type.UPLOAD_REQUEST, 
				localFileSize);

		//waiting for upload to finish
		Msg.info("Sent upload request to SE '" + SEName + "' for file '" + 
				logicalFileName +"' of size " + localFileSize +". Waiting for" +
				" an ack");

		Message.getFrom(mailbox);
		Msg.info("SE '"+ SEName + "' replied with an ACK");


		// Register file into LFC
		LogicalFile file = new LogicalFile(logicalFileName, localFileSize, 
				SEName);
		Msg.info("Ask '"+ LFCName + "' to register " + file.toString());

		Message.sendTo(LFCName, Message.Type.REGISTER_FILE, file);
		Message.getFrom(mailbox);
		
		Msg.info("LFC '"+ LFCName + "' registered " + file.toString());

		Msg.debug("lcg-cr of '" + logicalFileName +"' on LFC '" + LFCName +
				"' completed");
	}

	public static void cp(String mailbox, String logicalFileName,
			String localFileName, String LFCName){
		String SEName = null;
		long logicalFileSize = 0;

		Msg.info("lcg-cp '" + logicalFileName + "' to '" + localFileName +
				"' using LFC '" + LFCName + "'");

		// get information on Logical File from the LFC
		Message.sendTo(LFCName, Message.Type.ASK_LOGICAL_FILE, logicalFileName);

		Msg.info("Asked about '" + logicalFileName + "' to LFC '" + LFCName + 
				"'. Waiting for information ...");
		
		Message getFileInfo = Message.getFrom(mailbox);

		SEName = getFileInfo.getFile().getSEName();
		logicalFileSize = getFileInfo.getFile().getSize();

		Msg.info("LFC '"+ LFCName + "' replied: " + 
				getFileInfo.getFile().toString()); 

		// Download physical File from SE
		Msg.info("Downloading file '" + logicalFileName + "' from SE '" + 
				SEName + "' using LFC '" + LFCName +"'");

		Message.sendTo(SEName, Message.Type.DOWNLOAD_REQUEST, 
				logicalFileName, logicalFileSize);

		Msg.info("Sent download request for " + 
				getFileInfo.getFile().toString() + 
				". Waiting for reception ...");

		Message.getFrom(mailbox);

		Msg.info("SE '"+ SEName + "' sent " + getFileInfo.getFile().toString());

		Msg.debug("lcg-cp of '" + logicalFileName +"' to '" + localFileName +
				"' completed");
	};
}
