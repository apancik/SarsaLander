package uk.ac.ox.pancik.lunarlander.sensors;

import uk.ac.ox.pancik.lunarlander.entities.Vehicle;
import uk.ac.ox.pancik.sarsa.sensors.Sensor;

public class RotationSensor implements Sensor {

	private final Vehicle vehicle;

	public RotationSensor(final Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	@Override
	public double detect() {
		return Math.cos(this.vehicle.getAngle());
	}

}
