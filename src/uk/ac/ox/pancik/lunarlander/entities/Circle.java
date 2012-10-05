package uk.ac.ox.pancik.lunarlander.entities;

import uk.ac.ox.pancik.utils.Vector2D;

public abstract class Circle {
	public boolean collides(final Circle with) {
		return this.getPosition().distanceTo(with.getPosition()) - with.getRadius() < this.getRadius();
	}

	public abstract Vector2D getPosition();

	public abstract int getRadius();
}
