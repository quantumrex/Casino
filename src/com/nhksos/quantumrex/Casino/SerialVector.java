package com.nhksos.quantumrex.Casino;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class SerialVector extends Vector implements Serializable {
	private static final long serialVersionUID = -7321603582633890237L;
	
	public SerialVector(Vector v){
		setX(v.getX());
		setY(v.getY());
		setZ(v.getZ());
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof SerialVector || o instanceof Vector || o instanceof BlockVector){
			SerialVector s = (SerialVector) o;
			return (x == s.getX() && y == s.getY() && z == s.getZ());
		}
		else if(o instanceof Block){
			return equals(new SerialVector(((Block)o).getLocation().toVector()));
		}
		return false;
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		setX(in.readDouble());
		setY(in.readDouble());
		setZ(in.readDouble());
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.writeDouble(getX());
		out.writeDouble(getY());
		out.writeDouble(getZ());
	}
	
	@Override
	public String toString(){
		String string = "" + x + ':' + y + ':' + z;
		return string;
	}
}
