package com.journaldev.androidarcoredistancecamera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FishListViewAdapter extends BaseAdapter {
    private ArrayList<FishListViewItem> m_fishListViewItemList = new ArrayList<>() ;

    public FishListViewAdapter() {
    }

    @Override
    public int getCount() {
        return m_fishListViewItemList.size() ;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
        layoutParams.height = 300;

        ImageView ivFish = convertView.findViewById(R.id.ivFish);
        TextView tvSize = convertView.findViewById(R.id.tvSize);
        TextView tvName = convertView.findViewById(R.id.tvName);

        FishListViewItem fishListViewItem= m_fishListViewItemList.get(position);

        Util.setImageView(fishListViewItem.getM_image(), ivFish);
        tvSize.setText(fishListViewItem.getM_size().toString());
        tvName.setText(fishListViewItem.getM_name());

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public Object getItem(int position) {
        return m_fishListViewItemList.get(position) ;
    }

    public void addItem(int id, byte[] image, String name, Float size) {
        FishListViewItem item = new FishListViewItem(id, image, name, size);
        m_fishListViewItemList.add(item);
    }

    public void clear() {
        m_fishListViewItemList.clear();
    }
}