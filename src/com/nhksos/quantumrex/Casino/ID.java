package com.nhksos.quantumrex.Casino;

public class ID{
	final int casinoID;
	final int gameID;
	
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
		System.out.println("Hash: " + Double.toHexString(value).hashCode()+ "Casino: " + casinoID + "Game: " + gameID);
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
}