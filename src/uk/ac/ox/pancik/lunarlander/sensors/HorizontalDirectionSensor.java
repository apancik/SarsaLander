package uk.ac.ox.pancik.lunarlander.sensors;

import uk.ac.ox.pancik.lunarlander.entities.Vehicle;
import uk.ac.ox.pancik.sarsa.sensors.Sensor;

public class HorizontalDirectionSensor implements Sensor {

	private final Vehicle vehicle;

	public HorizontalDirectionSensor(final Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	@Override
	public double detect() {
		return this.vehicle.getDirection().getY();
	}

}
