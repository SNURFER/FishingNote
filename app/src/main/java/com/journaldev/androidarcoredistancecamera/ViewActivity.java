package com.journaldev.androidarcoredistancecamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;

import java.util.Vector;

public class ViewActivity extends Activity {

    private Button m_btnGoBackToMain;
    private Button m_btnSetView;
    private Spinner m_spnFishTypesCondition;
    private ListView m_lvSelectedImages;
    private FishListViewAdapter m_fishListViewAdapter;

    private DbHandler m_localDbHandler;

    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view);

        getView();
        setListeners();
        initialize();
    }

    private void getView() {
        m_btnGoBackToMain = findViewById(R.id.btnGoBackToMain);
        m_btnSetView = findViewById(R.id.btnSetView);
        m_spnFishTypesCondition = findViewById(R.id.spnFishTypesCondition);
        m_lvSelectedImages = findViewById(R.id.lvSelectedImages);
    }

    private void setListeners() {
        m_btnGoBackToMain.setOnClickListener(v->{
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        m_btnSetView.setOnClickListener(v->{
            setFishListViewFromDb();
        });

        m_lvSelectedImages.setOnItemLongClickListener((parent, view, position, id) -> {
            PopupMenu popupMenu = new PopupMenu(this, view);
            getMenuInflater().inflate(R.menu.modify_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.mnDelete:
                        FishListViewItem fishListViewItem =
                                (FishListViewItem) m_fishListViewAdapter.getItem(position);
                        m_localDbHandler.deleteSingle(fishListViewItem.getM_id());
                        setFishListViewFromDb();
                    break;
                    /*TODO Update menu will be added*/
                }
                return true;
            });
            popupMenu.show();
            return true;
        });

        m_lvSelectedImages.setOnItemClickListener((parent, view, position, id) -> {
            FishListViewItem fishListViewItem =
                    (FishListViewItem) m_fishListViewAdapter.getItem(position);
            Intent intent = new Intent(this, FullImageActivity.class);
            intent.putExtra("image", fishListViewItem.getM_image());
            startActivity(intent);
        });
    }

    private void initialize() {
        String[] items = getResources().getStringArray(R.array.FishTypes);
        Util.adaptSpinner(items, m_spnFishTypesCondition, this);
        m_fishListViewAdapter= new FishListViewAdapter();
        m_lvSelectedImages.setAdapter(m_fishListViewAdapter);
        m_localDbHandler = new DbHandler(this);
    }

    private void setFishListViewFromDb() {
        m_fishListViewAdapter.clear();
        Vector<DbHandler.FishInfo> fishInfos = m_localDbHandler.selectFromFishInfo(null);
        if (!fishInfos.isEmpty()) {
            for (DbHandler.FishInfo fishInfo : fishInfos) {
                m_fishListViewAdapter.addItem(fishInfo.id, fishInfo.image, fishInfo.name,
                        fishInfo.size);
            }
        }
        m_fishListViewAdapter.notifyDataSetChanged();
    }
}
