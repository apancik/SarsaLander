package uk.ac.ox.pancik.lunarlander.rewarders;

import uk.ac.ox.pancik.lunarlander.Simulation;

public class RewarderHover implements Rewarder {

	private final Simulation simulation;

	public RewarderHover(final Simulation simulation) {
		this.simulation = simulation;
	}

	@Override
	public double calculateReward() {
		final double speed = this.simulation.getVehicle().getVelocity().length();
		
		return speed;
	}
}
