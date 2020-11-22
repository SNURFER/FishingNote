package com.journaldev.androidarcoredistancecamera;

public class ListViewItem {
    private byte[] m_image;
    private String m_name;
    private Float m_size;

    public ListViewItem(byte[] image, String name, Float size) {
        m_image = image;
        m_name = name;
        m_size = size;
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