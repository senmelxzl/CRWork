package com.crwork.app.util;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * tools class
 * 
 * @author xiezhenlin
 *
 */
public class LitterUtil {
	public final static int LITTER_TYPE_NO_R = 0;
	public final static int LITTER_TYPE_YES_R = 1;
	public final static double LITTER_PRICE_NO_R = 0.3;
	public final static double LITTER_PRICE_YES_R = 0.7;

	/**
	 * 
	 * @return dateNowStr
	 */
	@SuppressLint("SimpleDateFormat")
	public static String getLitterDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d = new Date();
		String dateNowStr = sdf.format(d);
		return dateNowStr;
	}
}
