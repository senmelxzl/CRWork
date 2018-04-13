package com.crwork.app.adapter;

import java.util.ArrayList;

import com.crwork.app.R;
import com.crwork.app.domain.LitterDomain;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LitterReportAdapter extends BaseAdapter  {
	private ArrayList<LitterDomain> mLitterDomainList; 
    private Context mContext; 
      
    public LitterReportAdapter(ArrayList<LitterDomain> list, Context context) { 
    	mLitterDomainList = list; 
        mContext = context; 
    } 
      
    public void refresh(ArrayList<LitterDomain> list) { 
    	mLitterDomainList = list; 
        notifyDataSetChanged(); 
    } 
      
  
    @Override 
    public int getCount() { 
        return mLitterDomainList.size(); 
    } 
  
    @Override 
    public Object getItem(int position) { 
        return mLitterDomainList.get(position); 
    } 
  
    @Override 
    public long getItemId(int position) { 
        return position; 
    } 
  
    @Override 
    public View getView(int position, View convertView, ViewGroup parent) { 
        Holder holder = null; 
        if (convertView == null) { 
            LayoutInflater inflater = LayoutInflater.from(mContext); 
            convertView = inflater.inflate(R.layout.list_litter_data_item, null); 
            holder = new Holder(); 
            holder.userID = (TextView)convertView.findViewById(R.id.userID); 
            holder.userName = (TextView)convertView.findViewById(R.id.userName);
            holder.littertypeID = (TextView)convertView.findViewById(R.id.littertypeID); 
            holder.weight = (TextView)convertView.findViewById(R.id.weight);   
            holder.litterdate = (TextView)convertView.findViewById(R.id.litterdate); 
            convertView.setTag(holder); 
        } else { 
            holder = (Holder) convertView.getTag(); 
        } 
        holder.userID.setText(String.valueOf(mLitterDomainList.get(position).getUserID())); 
        holder.userName.setText("÷‹”Ω∫Ë"); 
        holder.littertypeID.setText(mLitterDomainList.get(position).getLittertypeID() == 0
				? mContext.getResources().getString(R.string.litter_union_type)
				: mContext.getResources().getString(R.string.litter_recyclable_type)); 
        holder.weight.setText(String.valueOf(mLitterDomainList.get(position).getWeight())
				+ mContext.getResources().getString(R.string.tv_weight_count_tip)); 
        holder.litterdate.setText(mLitterDomainList.get(position).getLitterdate()); 
        return convertView; 
    } 
  
    class Holder { 
        private TextView userID, userName,littertypeID,weight,litterdate; 
    }
}
