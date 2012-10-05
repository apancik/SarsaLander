package uk.ac.ox.pancik.lunarlander;

import java.util.List;

import uk.ac.ox.pancik.lunarlander.entities.Obstacle;
import uk.ac.ox.pancik.lunarlander.entities.Vehicle;
import uk.ac.ox.pancik.lunarlander.sensors.DistanceSensor;
import uk.ac.ox.pancik.sarsa.sensors.Sensor;
import uk.ac.ox.pancik.utils.Vector2D;
import uk.ac.ox.pancik.utils.Vectors;

public class Simulation {
	private final static int MAX_OBSTACLES = 100;
	public static final int MAX_TIME = 2000;

	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	private final int width;
	private final int height;
	public static final Vector2D GRAVITY = new Vector2D(0, 0.04);

	private final Obstacle[] obstaclesArray = new Obstacle[MAX_OBSTACLES];
	private final Vehicle vehicle;
	private final List<Sensor> sensors;

	private int time;

	private boolean endState;

	private int result;

	private static final double FRICTION = 0.98;

	private static boolean inside(final double input, final double minValue, final double maxValue) {
		return minValue < input && input < maxValue;
	}

	public Simulation(final Vehicle vehicle, final List<Sensor> sensors, int width, int height) {
		this.width = width;
		this.height = height;
		
		this.vehicle = vehicle;
		this.sensors = sensors;

		for (int obstacle = 0; obstacle < MAX_OBSTACLES; obstacle++) {
			this.obstaclesArray[obstacle] = new Obstacle();
		}

		this.resetState();
	}

	public Obstacle[] getObstacles() {
		return this.obstaclesArray;
	}

	public int getResult() {
		return this.result;
	}

	public int getTime() {
		return this.time;
	}

	public Vehicle getVehicle() {
		return this.vehicle;
	}

	public boolean isEndState() {
		return this.endState;
	}

	public boolean isVehicleLanded() {
		DistanceSensor leftSensor = null;
		DistanceSensor rightSensor = null;

		for (final Sensor sensor : this.sensors) {
			if (sensor instanceof DistanceSensor) {
				if (leftSensor == null) {
					leftSensor = (DistanceSensor) sensor;
				}

				rightSensor = (DistanceSensor) sensor;
			}
		}

		final double maxLandingDistance = 40.0 / rightSensor.getMaxDistance();
		final double minLandingDistance = 15.0 / rightSensor.getMaxDistance();

		assert rightSensor != null;
		assert leftSensor != null;

		final boolean isLanded = inside(leftSensor.detect(), minLandingDistance, maxLandingDistance) && inside(rightSensor.detect(), minLandingDistance, maxLandingDistance) && this.vehicle.getVelocity().length() < 0.7;

		return isLanded;
	}

	private void randomizeGround() {
		for (final Obstacle obstacle : this.obstaclesArray) {
			obstacle.randomize(width, height);
		}
	}

	public void resetState() {
		this.time = 0;
		this.endState = false;
		this.randomizeGround();
		this.vehicle.randomize(width, height);
	}

	public void step() {
		// =========================
		// SIMULATE VEHICLE MOVEMENT
		// =========================
		this.vehicle.setVelocity(this.vehicle.getVelocity().times(Simulation.FRICTION).plus(Simulation.GRAVITY));
		this.vehicle.setDirection(Vectors.generateRadialVector(this.vehicle.getAngle(), 1.0));
		this.vehicle.setPosition(this.vehicle.getPosition().plus(this.vehicle.getVelocity()));

		// ====================
		// DETECT VEHICLE STATE
		// ====================

		// Landed
		if (this.isVehicleLanded()) {
			this.result = 1;
			this.endState = true;
			return;
		}

		// Collided
		if (this.vehicleCollides()) {
			this.result = 0;
			this.endState = true;
			return;
		}

		// =======================================
		// REGENERATE TERRAIN IF OUT OF THE SCREEN
		// =======================================
		if (this.vehicle.getPosition().getX() < 0) {
			this.vehicle.setPosition(new Vector2D(width, this.vehicle.getPosition().getY()));
			this.randomizeGround();
		}

		if (this.vehicle.getPosition().getX() > width) {
			this.vehicle.setPosition(new Vector2D(0, this.vehicle.getPosition().getY()));
			this.randomizeGround();
		}

		// ===============
		// TIME EXPIRATION
		// ===============
		if (this.time++ > MAX_TIME) {
			this.result = 0;
			this.endState = true;
		}
	}

	public boolean vehicleCollides() {
		// The vehicle is above the upper screen side
		if (!inside(this.vehicle.getPosition().getY(), 0, height)) {
			return true;
		}

		// Collision with the obstacle
		for (final Obstacle obstacle : this.obstaclesArray) {
			if (this.vehicle.collides(obstacle)) {
				return true;
			}
		}

		return false;
	}
}