/*
 * Copyright (c) Centre de Calcul de l'IN2P3 du CNRS
 * Contributor(s) : Frédéric SUTER (2015)

 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package.
 */
import org.simgrid.msg.Msg;

public class Timer {
	private double value;
	private double delay;

	public Timer() {
		this.value = 0;
	}

	public double getValue() {
		return value;
	}

	public void start() {
		this.delay = Msg.getClock();
	}

	public void stop() {
		this.delay = Msg.getClock() - delay;
		this.value += delay;
	}

}
