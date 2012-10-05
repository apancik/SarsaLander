package uk.ac.ox.pancik.lunarlander.rewarders;

import uk.ac.ox.pancik.lunarlander.Simulation;

public class RewarderLanding implements Rewarder {

	private final Simulation simulation;

	public RewarderLanding(final Simulation simulation) {
		this.simulation = simulation;
	}

	// positive value if positive goal reached, negative value if negative goal reached
	@Override
	public double calculateReward() {
		final double speed = this.simulation.getVehicle().getVelocity().length();
		final double speedReward = 0.002;

		if (this.simulation.getTime() == Simulation.MAX_TIME) {
			return -0.9;
		} else if (this.simulation.isVehicleLanded()) {
			System.out.print('.');
			return 0.5 / (0.5 + speed); // from 0.416 to 1
		} else if (this.simulation.vehicleCollides()) {
			return -0.9;
		} else {
			final double yDir = this.simulation.getVehicle().getDirection().getY();
			final double yPos = this.simulation.getVehicle().getPosition().getY();
			
			return speedReward * (1.0 / (0.4 + Math.exp(speed)) - 2 - (yDir * yDir * yDir * 5 + 4.5) + (yPos - 200) * 4 / simulation.getWidth());
		}
	}
}
