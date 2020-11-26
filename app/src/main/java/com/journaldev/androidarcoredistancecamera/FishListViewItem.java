package com.journaldev.androidarcoredistancecamera;

public class FishListViewItem {
    private final int m_id;
    private final byte[] m_image;
    private final String m_name;
    private final Float m_size;

    public FishListViewItem(int id, byte[] image, String name, Float size) {
        m_id = id;
        m_image = image;
        m_name = name;
        m_size = size;
    }

    public int getM_id() {
       return this.m_id ;
    }

    public byte[] getM_image() {
        return this.m_image;
    }

    public String getM_name() {
        return this.m_name;
    }

    public Float getM_size() {
        return this.m_size;
    }
}