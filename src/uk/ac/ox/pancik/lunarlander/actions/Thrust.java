package uk.ac.ox.pancik.lunarlander.actions;

import uk.ac.ox.pancik.lunarlander.entities.Vehicle;
import uk.ac.ox.pancik.sarsa.actions.Action;

public class Thrust implements Action {
	private final Vehicle vehicle;
	private final double step;

	public Thrust(final Vehicle vehicle, final double step) {
		this.vehicle = vehicle;
		this.step = step;
	}

	@Override
	public void execute() {
		this.vehicle.thrust(this.step);
	}
}
