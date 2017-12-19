package com.xinan.app.domain;

/**
 * LitterDomain
 * 
 * @author xiezhenlin
 *
 */
public class LitterDomain {
	private int ID;
	private String littername;
	private int litterID;
	private int weight;
	private String litterdate;
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getLittername() {
		return littername;
	}
	public void setLittername(String littername) {
		this.littername = littername;
	}
	public int getLitterID() {
		return litterID;
	}
	public void setLitterID(int litterID) {
		this.litterID = litterID;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public String getLitterdate() {
		return litterdate;
	}
	public void setLitterdate(String litterdate) {
		this.litterdate = litterdate;
	}
}
