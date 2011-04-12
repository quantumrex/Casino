package com.nhksos.quantumrex.Casino;

import java.io.Serializable;

import org.bukkit.util.Vector;

public class SerialVector extends Vector implements Serializable {
	private static final long serialVersionUID = -7321603582633890237L;
	
	public SerialVector(Vector v){
		setX(v.getX());
		setY(v.getY());
		setZ(v.getZ());
	}
}
