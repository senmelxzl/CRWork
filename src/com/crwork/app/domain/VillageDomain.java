package com.crwork.app.domain;

/**
 * Village data
 * 
 * @author xiezhenlin
 *
 */
public class VillageDomain {
	private int ID;
	private int villageID;
	private String villageName;
	private int belongID;
	private int statusMark;
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public int getVillageID() {
		return villageID;
	}
	public void setVillageID(int villageID) {
		this.villageID = villageID;
	}
	public String getVillageName() {
		return villageName;
	}
	public void setVillageName(String villageName) {
		this.villageName = villageName;
	}
	public int getBelongID() {
		return belongID;
	}
	public void setBelongID(int belongID) {
		this.belongID = belongID;
	}
	public int getStatusMark() {
		return statusMark;
	}
	public void setStatusMark(int statusMark) {
		this.statusMark = statusMark;
	}
}
