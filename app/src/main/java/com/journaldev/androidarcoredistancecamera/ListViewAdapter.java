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

public class ListViewAdapter extends BaseAdapter {
    private ArrayList<ListViewItem> listViewItemList = new ArrayList<>() ;

    public ListViewAdapter() {
    }

    @Override
    public int getCount() {
        return listViewItemList.size() ;
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

        ListViewItem listViewItem = listViewItemList.get(position);

        Util.setImageView(listViewItem.getM_image(), ivFish);
        tvSize.setText(listViewItem.getM_size().toString());
        tvName.setText(listViewItem.getM_name());

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }

    public void addItem(byte[] image, String name, Float size) {
        ListViewItem item = new ListViewItem(image, name, size);
        listViewItemList.add(item);
    }

    public void clear() {
        listViewItemList.clear();
    }
}