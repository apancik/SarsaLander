package uk.ac.ox.pancik.lunarlander.sensors;

import uk.ac.ox.pancik.lunarlander.Simulation;
import uk.ac.ox.pancik.lunarlander.entities.Obstacle;
import uk.ac.ox.pancik.lunarlander.entities.Vehicle;
import uk.ac.ox.pancik.sarsa.sensors.Sensor;
import uk.ac.ox.pancik.utils.Vector2D;
import uk.ac.ox.pancik.utils.Vectors;

import com.google.common.base.Preconditions;

public class DistanceSensor implements Sensor {

	private final Obstacle[] obstacles; // Array instead of list for the performance
	private final Vehicle vehicle;
	private final double angle;

	private final int maxDistance;

	public DistanceSensor(final Simulation simulation, final Vehicle vehicle, final double angle) {
		this.obstacles = Preconditions.checkNotNull(simulation.getObstacles());
		this.vehicle = Preconditions.checkNotNull(vehicle);
		this.angle = angle;
		maxDistance  = simulation.getWidth() * 2;
	}

	public int getMaxDistance() {
		return maxDistance;
	}
	
	@Override
	public double detect() {
		double distance = maxDistance;

		for (final Obstacle obstacle : this.obstacles) {
			distance = Math.min(distance, this.rayCircleDistance(obstacle));
		}

		return distance / maxDistance; // scale to be between 0 and 1
	}

	public Vector2D getCurrentSensorDirection() {
		return Vectors.generateRadialVector(this.vehicle.getAngle() + this.angle, 1);
	}

	private double rayCircleDistance(final Obstacle obstacle) {
		final Vector2D e = this.getCurrentSensorDirection().normalize(); // e=ray.dir
		final Vector2D h = obstacle.getPosition().minus(this.vehicle.getPosition()); // h=r.o-c.M

		final double lf = e.dot(h); // lf=e.h
		double s = obstacle.getRadius() * obstacle.getRadius() - h.dot(h) + lf * lf; // s=r^2-h^2+lf^2

		if (s < 0.0) {
			return Double.MAX_VALUE;
		}

		s = Math.sqrt(s); // s=sqrt(r^2-h^2+lf^2)

		int result = 0;
		if (lf < s) // S1 behind A ?
		{
			if (lf + s >= 0) // S2 before A ?}
			{
				s = -s; // swap S1 <-> S2}
				result = 1; // one intersection point
			}
		} else {
			result = 2; // 2 intersection points
		}

		if (result == 0) {
			return Double.MAX_VALUE;
		}

		final Vector2D intersection1 = e.times(lf - s).plus(this.vehicle.getPosition());
		final Vector2D intersection2 = e.times(lf + s).plus(this.vehicle.getPosition());

		return Math.min(intersection1.distanceTo(this.vehicle.getPosition()), intersection2.distanceTo(this.vehicle.getPosition()));
	}

}
