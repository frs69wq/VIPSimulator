/*
 * Copyright (c) Centre de Calcul de l'IN2P3 du CNRS
 * Contributor(s) : Frédéric SUTER (2015)

 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package.
 */
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;

public abstract class Job extends Process {
	private String name;
	private SE closeSE;
	protected Timer downloadTime;
	protected Timer computeTime;
	protected Timer uploadTime;

	public String getName() {
		return name;
	}

	public void setName() {
		// Build the mailbox name from the PID and the host name. This might be
		// useful to distinguish different Gate processes running on a same host
		this.name = getPID() + "@" + getHost().getName();
	}

	public SE getCloseSE() {
		return closeSE;
	}

	public void begin() {
		GateMessage.sendTo(name, "BEGIN", 0);
	}

	public void carryOn() {
		GateMessage.sendTo(name, "CARRY_ON", 0);
	}

	public void end() {
		GateMessage.sendTo(name, "END", 0);
	}

	public Job(Host host, String name, String[] args) {
		super(host, name, args);
		if (host.getProperty("closeSE") != null)
			this.closeSE = VIPServer.getSEbyName(host.getProperty("closeSE"));
		this.downloadTime = new Timer();
		this.computeTime = new Timer();
		this.uploadTime = new Timer();
	}
}
