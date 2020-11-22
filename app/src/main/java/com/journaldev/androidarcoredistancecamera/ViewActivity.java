package com.journaldev.androidarcoredistancecamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.Vector;

public class ViewActivity extends Activity {

    private Button m_btnGoBackToPreView;
    private Button m_btnSetView;
    private Spinner m_spnFishTypesCondition;
    private ListView m_lvSelectedImages;
    private ListViewAdapter m_listViewAdapter;

    private DbHandler m_localDbHandler;

    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view);

        getView();
        setListeners();
        initialize();
    }

    private void getView() {
        m_btnGoBackToPreView = findViewById(R.id.btnGoBackToPreview);
        m_btnSetView = findViewById(R.id.btnSetView);
        m_spnFishTypesCondition = findViewById(R.id.spnFishTypesCondition);
        m_lvSelectedImages = findViewById(R.id.lvSelectedImages);
    }

    private void setListeners() {
        m_btnGoBackToPreView.setOnClickListener(v->{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        m_btnSetView.setOnClickListener(v->{
            Vector<DbHandler.FishInfo> fishInfos = m_localDbHandler.selectFromFishInfo(null);
            if (fishInfos.isEmpty()) {
                Util.toastMsg(this, "No Fish Data");
            } else {
                m_listViewAdapter.clear();
                for (DbHandler.FishInfo fishInfo : fishInfos) {
                    m_listViewAdapter.addItem(fishInfo.image, fishInfo.name, fishInfo.size);
                }
                m_listViewAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initialize() {
        String[] items = getResources().getStringArray(R.array.FishTypes);
        Util.adaptSpinner(items, m_spnFishTypesCondition, this);
        m_listViewAdapter= new ListViewAdapter();
        m_lvSelectedImages.setAdapter(m_listViewAdapter);
        m_localDbHandler = new DbHandler(this);
    }
}
