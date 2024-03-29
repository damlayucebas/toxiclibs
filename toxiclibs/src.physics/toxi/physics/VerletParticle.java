package toxi.physics;

/*
 * Copyright (c) 2008 Karsten Schmidt
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * http://creativecommons.org/licenses/LGPL/2.1/
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

import java.util.ArrayList;

import toxi.geom.AABB;
import toxi.geom.Vec3D;
import toxi.physics.constraints.ParticleConstraint;

/**
 * An individual 3D particle for use by the VerletPhysics and VerletSpring
 * classes. A particle has weight, can be locked in space and its position
 * constrained inside an (optional) axis-aligned bounding box.
 * 
 * @author toxi
 */
public class VerletParticle extends Vec3D {

    protected Vec3D prev, temp;
    protected boolean isLocked;

    /**
     * Bounding box, by default set to null to disable
     */
    public AABB bounds;

    /**
     * An optional particle constraints, called immediately after a particle is
     * updated (and only used if particle is unlocked (default)
     */
    public ArrayList<ParticleConstraint> constraints;

    /**
     * Particle weight, default = 1
     */
    protected float weight, invWeight;

    /**
     * Creates particle at position xyz
     * 
     * @param x
     * @param y
     * @param z
     */
    public VerletParticle(float x, float y, float z) {
        this(x, y, z, 1);
    }

    /**
     * Creates particle at position xyz with weight w
     * 
     * @param x
     * @param y
     * @param z
     * @param w
     */
    public VerletParticle(float x, float y, float z, float w) {
        super(x, y, z);
        prev = new Vec3D(this);
        temp = new Vec3D();
        setWeight(w);
    }

    /**
     * Creates particle at the position of the passed in vector
     * 
     * @param v
     *            position
     */
    public VerletParticle(Vec3D v) {
        this(v.x, v.y, v.z, 1);
    }

    /**
     * Creates particle with weight w at the position of the passed in vector
     * 
     * @param v
     *            position
     * @param w
     *            weight
     */
    public VerletParticle(Vec3D v, float w) {
        this(v.x, v.y, v.z, w);
    }

    /**
     * Creates a copy of the passed in particle
     * 
     * @param p
     */
    public VerletParticle(VerletParticle p) {
        this(p.x, p.y, p.z, p.weight);
        isLocked = p.isLocked;
    }

    /**
     * Adds the given constraint implementation to the list of constraints
     * applied to this particle at each time step.
     * 
     * @param c
     *            constraint instance
     * @return itself
     */
    public VerletParticle addConstraint(ParticleConstraint c) {
        if (constraints == null) {
            constraints = new ArrayList<ParticleConstraint>(1);
        }
        constraints.add(c);
        return this;
    }

    public VerletParticle addVelocity(Vec3D v) {
        prev.subSelf(v);
        return this;
    }

    public void applyConstraints() {
        if (constraints != null) {
            for (ParticleConstraint pc : constraints) {
                pc.apply(this);
            }
        }
    }

    public VerletParticle clearVelocity() {
        prev.set(this);
        return this;
    }

    /**
     * Returns the particle's position at the most recent time step.
     * 
     * @return previous position
     */
    public Vec3D getPreviousPosition() {
        return prev;
    }

    /**
     * @return the weight
     */
    public float getWeight() {
        return weight;
    }

    /**
     * @return true, if particle is locked
     */
    public boolean isLocked() {
        return isLocked;
    }

    /**
     * Locks/immobilizes particle in space
     * 
     * @return itself
     */
    public VerletParticle lock() {
        isLocked = true;
        return this;
    }

    /**
     * Removes any currently applied constraints from this particle.
     * 
     * @return itself
     */
    public VerletParticle removeAllConstraints() {
        constraints.clear();
        return this;
    }

    /**
     * Attempts to remove the given constraint instance from the list of active
     * constraints.
     * 
     * @param c
     *            constraint to remove
     * @return true, if successfully removed
     */
    public boolean removeConstraint(ParticleConstraint c) {
        return constraints.remove(c);
    }

    public VerletParticle scaleVelocity(float scl) {
        prev.interpolateToSelf(this, 1 - scl);
        return this;
    }

    public void setWeight(float w) {
        weight = w;
        invWeight = 1f / w;
    }

    /**
     * Unlocks particle again
     * 
     * @return itself
     */
    public VerletParticle unlock() {
        clearVelocity();
        isLocked = false;
        return this;
    }

    protected void update(Vec3D force) {
        if (!isLocked) {
            // TODO pos += (curr-prev) + force * timestep * timestep ?
            temp.set(this);
            addSelf(sub(prev).addSelf(force.scale(weight)));
            prev.set(temp);
            if (constraints != null) {
                applyConstraints();
            }
        }
    }
}
