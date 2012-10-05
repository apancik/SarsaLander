package uk.ac.ox.pancik.lunarlander.actions;

import uk.ac.ox.pancik.lunarlander.entities.Vehicle;
import uk.ac.ox.pancik.sarsa.actions.Action;

public class Turn implements Action {
	private final Vehicle vehicle;
	private final double turningAngle;

	public Turn(final Vehicle vehicle, final double turningAngle) {
		this.vehicle = vehicle;
		this.turningAngle = turningAngle;
	}

	@Override
	public void execute() {
		this.vehicle.turnBy(this.turningAngle);
	}
}
