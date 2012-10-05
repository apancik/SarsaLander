package uk.ac.ox.pancik.lunarlander.entities;

import uk.ac.ox.pancik.lunarlander.Simulation;
import uk.ac.ox.pancik.utils.RandomSingleton;
import uk.ac.ox.pancik.utils.Vector2D;
import uk.ac.ox.pancik.utils.Vectors;

public class Vehicle extends Circle {
	public static final double MAX_THRUST = 0.2;
	public static final double TURNING_ANGLE = 0.06;
	public static final int RADIUS = 20;

	private Vector2D position;
	private Vector2D direction;
	private Vector2D velocity;

	private double angle;

	public double getAngle() {
		return this.angle;
	}

	public Vector2D getDirection() {
		return this.direction;
	}

	@Override
	public Vector2D getPosition() {
		return this.position;
	}

	@Override
	public int getRadius() {
		return Vehicle.RADIUS;
	}

	public Vector2D getVelocity() {
		return this.velocity;
	}

	public void randomize(int width, int height) {
		final int bounds = width - Vehicle.RADIUS;

		this.position = Vectors.generateRandomVector(0, 0, width, height * 0.5);

		this.velocity = Vectors.generateRandomVector(-5.0, -1.0, 5.0, 1.0);

		this.angle = RandomSingleton.nextDouble(0, Math.PI * 2);
	}

	public void setDirection(final Vector2D direction) {
		this.direction = direction;
	}

	public void setPosition(final Vector2D position) {
		this.position = position;
	}

	public void setVelocity(final Vector2D velocity) {
		this.velocity = velocity;
	}

	public void thrust(final double power) {
		this.velocity = this.velocity.plus(this.direction.times(power));
	}

	public void turnBy(final double delta) {
		this.angle = this.angle + delta;
	}
}
