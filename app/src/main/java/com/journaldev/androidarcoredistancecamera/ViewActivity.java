package com.journaldev.androidarcoredistancecamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
    private ViewActivity m_viewActivity = this;
    private String[] m_items;
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
            FishListViewItem fishListViewItem =
                    (FishListViewItem) m_fishListViewAdapter.getItem(position);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.mnDelete:
                        m_localDbHandler.deleteSingle(fishListViewItem.getM_id());
                        setFishListViewFromDb();
                    break;
                    case R.id.mnModify:
                        showFishChooserDialog(fishListViewItem);
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
        m_localDbHandler = DbHandler.getInstance(this);
        Util.createFishTypeList(m_spnFishTypesCondition, this);
        Vector<String> fishTypes = DbHandler.getInstance(this).selectFromFishTypes();
        m_items = new String[fishTypes.size()];
        fishTypes.toArray(m_items);
        m_fishListViewAdapter= new FishListViewAdapter();
        m_lvSelectedImages.setAdapter(m_fishListViewAdapter);
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

    private void showFishChooserDialog(FishListViewItem fishListViewItem)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.modify_fish, null);
        builder.setView(view);

        final ListView listview = (ListView)view.findViewById(R.id.lvFishTypesCondition);
        final AlertDialog dialog = builder.create();
        final Button btnCancel = (Button)view.findViewById(R.id.btnCancel);

        ArrayAdapter<String> simpleAdapter = new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item, m_items);
        listview.setAdapter(simpleAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Util.toastMsg(m_viewActivity, m_items[position] + "를(을) 선택했습니다.");
                m_localDbHandler.replaceFishType(fishListViewItem.getM_id(), m_items[position]);
                setFishListViewFromDb();
                dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(v->{ dialog.dismiss(); });

        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }
}
