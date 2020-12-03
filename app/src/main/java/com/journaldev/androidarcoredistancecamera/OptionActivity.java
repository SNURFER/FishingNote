package com.journaldev.androidarcoredistancecamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

public class OptionActivity extends Activity {
    private Button m_btnGoBack;
    private Button m_btnRegister;
    private Button m_btnUnregister;
    private EditText m_etFishType;

    private DbHandler m_localDbHandler;

    private boolean m_isRegistered = false;
    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        getView();
        setListeners();
        initialize();
     }

     private void getView() {
         m_btnGoBack = findViewById(R.id.btnGoBack);
         m_btnRegister = findViewById(R.id.btnRegister);
         m_btnUnregister = findViewById(R.id.btnUnregister);
         m_etFishType = findViewById(R.id.etFishType);
     }

     private void setListeners() {
         m_btnGoBack.setOnClickListener(v->{
             Intent intent = new Intent(this, MainActivity.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(intent);
             finish();
         });

         m_btnRegister.setOnClickListener(v->{
             String fishType = m_etFishType.getText().toString();
             if (!m_isRegistered) {
                 m_localDbHandler.insertIntoFishTypes(fishType);
                 m_isRegistered = true;
                 Util.toastMsg(this, "Fish type " + fishType + " registered");
             } else {
                 Util.toastMsg(this, "Already registered " + fishType);
             }
         });

         m_btnUnregister.setOnClickListener(v->{
             String fishType = m_etFishType.getText().toString();
             m_localDbHandler.deleteFishType(fishType);
             Util.toastMsg(this, "Fish type " + fishType + " unregistered");
         });

         m_etFishType.addTextChangedListener(new TextWatcher() {

             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {

                 if (s.toString().trim().length()==0){
                     m_btnRegister.setEnabled(false);
                     m_btnUnregister.setEnabled(false);
                 } else {
                     m_btnRegister.setEnabled(true);
                     m_btnUnregister.setEnabled(true);
                 }
             }

             @Override
             public void beforeTextChanged(CharSequence s, int start, int count,
                                           int after) {
             }

             @Override
             public void afterTextChanged(Editable s) {
             }
         });
     }

     private void initialize() {
        m_localDbHandler = DbHandler.getInstance(this);
         m_btnRegister.setEnabled(false);
         m_btnUnregister.setEnabled(false);
     }
}