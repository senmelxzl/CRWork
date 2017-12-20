package com.xinan.app.domain;

/**
 * User data
 * 
 * @author xiezhenlin
 *
 */
public class UserDomain {
	private int ID;
	private int userID;
	private String userName;
	private int villageID;
	private int userType;
	private String registeredDate;

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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getVillageID() {
		return villageID;
	}

	public void setVillageID(int villageID) {
		this.villageID = villageID;
	}

	public int getUserType() {
		return userType;
	}

	public void setUserType(int userType) {
		this.userType = userType;
	}

	public String getRegisteredDate() {
		return registeredDate;
	}

	public void setRegisteredDate(String registeredDate) {
		this.registeredDate = registeredDate;
	}

	public UserDomain() {
		this.ID = 20170101;
		this.userID = 19000101;
		this.userName = "��ӽ��";
		this.villageID = 1;
		this.userType = 1;
		this.registeredDate = "2017-12-20";
	}
}
