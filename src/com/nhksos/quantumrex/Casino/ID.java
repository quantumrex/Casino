package com.nhksos.quantumrex.Casino;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ID implements Serializable, Comparable<ID>{
	
	private static final long serialVersionUID = 793060220239482147L;
	
	int casinoID;
	int gameID;
	
	public ID(int casino, int game){
		casinoID = casino;
		gameID = game;
	}
	
	public ID clone(){
		return new ID(casinoID, gameID);
	}
	
	@Override
	public int hashCode(){
		double value = (double)casinoID * Math.PI;
		value = Math.pow(value, (double)gameID + Math.hypot(casinoID, gameID));
		return Double.toHexString(value).hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof ID){
			if (((ID) o).hashCode() == hashCode())
				return true;
		}
		return false;
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		casinoID = in.readInt();
		gameID = in.readInt();
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.writeInt(casinoID);
		out.writeInt(gameID);
	}

	@Override
	public int compareTo(ID arg0) {
		if(this.casinoID == arg0.casinoID)
			return (this.gameID - arg0.gameID);
		else return (this.casinoID - arg0.casinoID);
	}
}