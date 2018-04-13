package com.crwork.app.domain;

/**
 * litter type data
 * 
 * @author xiezhenlin
 *
 */
public class LitterTypeDomain {
	private int ID;
	private int typeID;
	private String typeName;
	private int typemark;
	private Double price;

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public int getTypeID() {
		return typeID;
	}

	public void setTypeID(int typeID) {
		this.typeID = typeID;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public int getTypemark() {
		return typemark;
	}

	public void setTypemark(int typemark) {
		this.typemark = typemark;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}
}
