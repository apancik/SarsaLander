package uk.ac.ox.pancik.lunarlander.entities;

import uk.ac.ox.pancik.lunarlander.Simulation;
import uk.ac.ox.pancik.utils.RandomSingleton;
import uk.ac.ox.pancik.utils.Vector2D;
import uk.ac.ox.pancik.utils.Vectors;

public class Obstacle extends Circle {
	private static final int MIN_SIZE = 30;
	private static final int MAX_SIZE = 70;

	private Vector2D position;

	private int radius;

	@Override
	public Vector2D getPosition() {
		return this.position;
	}

	@Override
	public int getRadius() {
		return this.radius;
	}

	public void randomize(int width, int height) {
		// put obstacle to bottom 20% of the screen
		this.position = Vectors.generateRandomVector(0, height * 0.8, width, height);
		this.radius = RandomSingleton.nextInt(MIN_SIZE, MAX_SIZE);
	}
}
