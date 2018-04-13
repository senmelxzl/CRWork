package com.crwork.app.domain;

/**
 * LitterDomain
 * 
 * @author xiezhenlin
 *
 */
public class LitterDomain {
	private int ID;
	private int userID;
	private int littertypeID;
	private Double weight;
	private String litterdate;

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public int getLittertypeID() {
		return littertypeID;
	}

	public void setLittertypeID(int littertypeID) {
		this.littertypeID = littertypeID;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public String getLitterdate() {
		return litterdate;
	}

	public void setLitterdate(String litterdate) {
		this.litterdate = litterdate;
	}
}
