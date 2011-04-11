package com.nhksos.quantumrex.Casino;

public class ID{
	int casinoID;
	int gameID;
	
	@Override
	public int hashCode(){
		double value = casinoID * 33;
		value = Math.pow(value, gameID);
		return (int)value;
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