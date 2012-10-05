package uk.ac.ox.pancik.lunarlander.sensors;

import uk.ac.ox.pancik.lunarlander.entities.Vehicle;
import uk.ac.ox.pancik.sarsa.sensors.Sensor;

public class VerticalDirectionSensor implements Sensor {

	private final Vehicle vehicle;

	public VerticalDirectionSensor(final Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	@Override
	public double detect() {
		return this.vehicle.getDirection().getX();
	}

}
