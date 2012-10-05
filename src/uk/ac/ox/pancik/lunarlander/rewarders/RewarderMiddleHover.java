package uk.ac.ox.pancik.lunarlander.rewarders;

import uk.ac.ox.pancik.lunarlander.Simulation;

public class RewarderMiddleHover implements Rewarder {

	private final Simulation simulation;

	public RewarderMiddleHover(final Simulation simulation) {
		this.simulation = simulation;
	}

	@Override
	public double calculateReward() {

		final double yDir = this.simulation.getVehicle().getVelocity().getY();
		final double SPEED_REWARD = 0.02;

		return -Math.abs(yDir) * SPEED_REWARD;
	}
}
