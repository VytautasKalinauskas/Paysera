package com.example.vytkal.paysera;

import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button button;
    DatabaseHelper db;
    RadioGroup rgFrom;
    RadioGroup rgTo;
    EditText amount;
    TextView balance;

    private RequestQueue mRequestQueue;
    private String currencyFrom;
    private String currencyTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DatabaseHelper(this);

        balance = findViewById(R.id.tv_balance);
        rgFrom = findViewById(R.id.rg_from);
        rgTo = findViewById(R.id.rg_to);
        amount = findViewById(R.id.et_convert_amount);
        mRequestQueue = Volley.newRequestQueue(this);
        resetBalance();
        resetKom();

        rgFrom.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {

                balance.setVisibility(View.VISIBLE);
                switch (id){
                    case R.id.rb_EurFrom:
                        currencyFrom = "EUR";
                        break;

                    case R.id.rb_UsdFrom:
                        currencyFrom = "USD";
                        break;

                    case R.id.rb_JpyFrom:
                        currencyFrom = "JPY";
                        break;

                }

            }
        });

        rgTo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (id){
                    case R.id.rb_EurTo:
                        currencyTo = "EUR";
                        break;

                    case R.id.rb_UsdTo:
                        currencyTo = "USD";
                        break;

                    case R.id.rb_JpyTo:
                        currencyTo = "JPY";
                        break;

                }
            }
        });


       button = findViewById(R.id.btn_convert);
       button.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {

            case R.id.btn_convert:

                if(currencyFrom.equals(currencyTo))
                    break;

                double kom = 0;
                if(db.getAmountKom() >= 5) {
                    kom = 0.007;
                }

                    if(Double.valueOf(amount.getText().toString()) * (1+kom)  > db.getBalance(currencyFrom)) {
                        showMessage(currencyFrom +" Balansas nepakankamas");
                        break;
                    }

                        sendRequest(amount.getText().toString(), currencyFrom, currencyTo, kom);
                break;
        }
    }

    /*
    sendRequest
    siunciama HTTP uzklausa valiutu konvertacijai
    @param amount - valiutos is kurios konvertuojama kiekis
    @param currencyFrom - valiutos is kurios konvertuojama pavadinimas
    @param currencyTo - valiutos i kuria konvertuojama pavadinimas
    @param kom - komisinis mokestis procentais
     */
    private void sendRequest(final String amount, final String currencyFrom, final String currencyTo, final double kom) {

        String url = "http://api.evp.lt/currency/commercial/exchange/" + amount + "-" + currencyFrom + "/"
                + currencyTo + "/latest";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    db.changeBalance(currencyFrom, Double.parseDouble(amount), currencyTo, response.getDouble("amount"),kom);
                    showMessage("Jūs konvertavote " +  String.format("%.2f", Double.valueOf(amount)) + " " + currencyFrom + " į " + String.format("%.2f", Double.valueOf(response.getDouble("amount")))
                            + " " + currencyTo + " Komisinis mokestis - " +  Double.valueOf(amount)*kom + " " + currencyFrom);

                    resetBalance();
                    resetKom();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showMessage("Konvertavimas nepavyko. Bandykite dar kartą");
            }
        });
        mRequestQueue.add(request);
    }

    /*
    resetBalance
    atnaujinamas vartotojo turimas balansas visomis valiutomis
     */
    private void resetBalance() {
        TextView balance = findViewById(R.id.tv_balance);
        balance.setText("Likutis: EUR "+ String.format("%.2f", db.getBalance("EUR")) + " USD " +
                String.format("%.2f", db.getBalance("USD")) + " JPY "
                + String.format("%.2f", db.getBalance("JPY")));
    }

    /*
   resetBalance
   atnaujinami vartotojo sumoketi komisiniai mokesciai visomis valiutomis
    */
    private void resetKom() {
        TextView eurKom = findViewById(R.id.tv_Eurkom);
        TextView usdKom = findViewById(R.id.tv_Usdkom);
        TextView jpyKom = findViewById(R.id.tv_Jpykom);

        Cursor cur = db.getKom();
        cur.moveToFirst();
        eurKom.setText(String.valueOf(cur.getDouble(0)) + " EUR");
        usdKom.setText(String.valueOf(cur.getDouble(1)) + " USD");
        jpyKom.setText(String.valueOf(cur.getDouble(2)) + " JPY");
    }

    /*
    showMessage
    rodomi pranešimai vartotojui
    @param message - pranesimas vartotojui
     */
    private void showMessage(String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

        alert.setMessage(message)
                .setPositiveButton("Ok", null)
                .create()
                .show();
    }
}
