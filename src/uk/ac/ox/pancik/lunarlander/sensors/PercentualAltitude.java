package uk.ac.ox.pancik.lunarlander.sensors;

import uk.ac.ox.pancik.lunarlander.Simulation;
import uk.ac.ox.pancik.lunarlander.entities.Vehicle;
import uk.ac.ox.pancik.sarsa.sensors.Sensor;

public class PercentualAltitude implements Sensor {

	private final Vehicle vehicle;

	private double height;
	
	public PercentualAltitude(final Vehicle vehicle, Simulation simulation) {
		this.vehicle = vehicle;
		this.height = simulation.getHeight();
	}

	@Override
	public double detect() {
		return this.vehicle.getPosition().getY() / height;
	}

}
