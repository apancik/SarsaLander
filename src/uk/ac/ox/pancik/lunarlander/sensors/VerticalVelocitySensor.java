package uk.ac.ox.pancik.lunarlander.sensors;

import uk.ac.ox.pancik.lunarlander.entities.Vehicle;
import uk.ac.ox.pancik.sarsa.sensors.Sensor;

public class VerticalVelocitySensor implements Sensor {

	private final Vehicle vehicle;

	public VerticalVelocitySensor(final Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	@Override
	public double detect() {
		return this.vehicle.getVelocity().getY() / 10.0;
	}

}
