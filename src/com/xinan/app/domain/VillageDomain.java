package com.xinan.app.domain;

/**
 * Village data
 * 
 * @author xiezhenlin
 *
 */
public class VillageDomain {
	private int ID;
	private String villageName;
	private int belongID;
	private int statusMark;
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
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
