package uk.ac.ox.pancik.lunarlander.sensors;

import uk.ac.ox.pancik.lunarlander.Simulation;
import uk.ac.ox.pancik.sarsa.NeuralNetwork;
import uk.ac.ox.pancik.sarsa.sensors.Sensor;

public class TimeSensor implements Sensor {

	private final Simulation simulation;

	public TimeSensor(final Simulation simulation) {
		this.simulation = simulation;
	}

	@Override
	public double detect() {
		return NeuralNetwork.activationFunction((double)this.simulation.getTime()/(double)Simulation.MAX_TIME);
	}
}
