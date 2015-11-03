/*
 * Copyright (c) Centre de Calcul de l'IN2P3 du CNRS
 * Contributor(s) : Frédéric SUTER (2015)

 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package.
 */
import java.util.Vector;

import org.simgrid.msg.Msg;

public abstract class LCG {

	public static void cr(String localFileName, long localFileSize,
			String logicalFileName, SE se, LFC lfc) {
		Msg.info("lcg-cr '" + logicalFileName + "' from '" + localFileName
				+ "' using '" + lfc.getName() + "'");

		// upload file to SE
		se.upload(localFileSize);
		Msg.info("SE '" + se.getName() + "' replied with an ACK");

		// Register file into LFC
		LogicalFile file = new LogicalFile(logicalFileName, localFileSize, se);
		Msg.info("Ask '" + lfc.getName() + "' to register " + file.toString());

		lfc.register(file);

		Msg.info("lcg-cr of '" + logicalFileName + "' on '" + lfc.getName()
				+ "' completed");
	}

	public static String cp(String logicalFileName, String localFileName,
			LFC lfc) {
		Timer duration = new Timer();
		duration.start();
		Msg.info("lcg-cp '" + logicalFileName + "' to '" + localFileName
				+ "' using '" + lfc.getName() + "'");

		// get Logical File from the LFC
		LogicalFile file = lfc.getLogicalFile(logicalFileName);

		Msg.info("LFC '" + lfc.getName() + "' replied: " + file.toString());

		// Download physical File from SE
		Msg.info("Downloading file '" + logicalFileName + "' from SE '"
				+ file.getLocation() + "' using '" + lfc.getName() + "'");

		file.getLocation().download(logicalFileName, file.getSize());

		Msg.info("lcg-cp of '" + logicalFileName + "' to '" + localFileName
				+ "' completed");
		duration.stop();
		return file.getLocation() + "," + file.getSize() + ","
				+ duration.getValue();
	};

	public static Vector<String> ls(LFC lfc, String directoryName) {
		Vector<String> results = new Vector<String>();

		// Ask the LFC for the list of files to merge
		Vector<LogicalFile> fileList = lfc
				.getLogicalDirectoryContents(directoryName);

		for (LogicalFile f : fileList)
			results.add(f.getName());

		return results;
	}
}
