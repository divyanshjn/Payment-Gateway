package com.tcsdks.braintreesdk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.interfaces.HttpResponseCallback;
import com.braintreepayments.api.internal.HttpClient;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE=1234;
    final String API_GET_TOKEN="http://bicyclerent.epizy.com/braintree/main.php";
   // final String API_GET_TOKEN="http://10.0.2.2:8083/braintree/main.php";
    final String API_CHECK_OUT="http://10.0.2.2:8083/braintree/checkout.php";
    public String token;
    String amount;
    HashMap<String,String> paramHash;
    Button btn_pay;
    TextView edt_amount;
    LinearLayout group_waiting,group_payment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edt_amount=findViewById(R.id.edt_amount);
        group_payment=findViewById(R.id.group_payment);
        group_waiting=findViewById(R.id.group_waiting);
        btn_pay=findViewById(R.id.btn_pay);
      getToken gt=  new getToken();
      gt.execute();
        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //edt_amount.setText(token);
                submitPayment();
            }
        });
    }

    private void submitPayment() {
        DropInRequest dropInRequest=new DropInRequest().clientToken(token);
        startActivityForResult(dropInRequest.getIntent(this),REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Exception error;
        if (requestCode==REQUEST_CODE){
            if (resultCode == RESULT_OK){
                DropInResult result=data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce=result.getPaymentMethodNonce();
                String strNonce=nonce.getNonce();
                if(!edt_amount.getText().toString().isEmpty()){
                    amount=edt_amount.getText().toString();
                    paramHash=new HashMap<>();
                    paramHash.put("amount",amount);
                    paramHash.put("nonce",strNonce);
                    sendPayments();
                }
                else {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                }
            }
            else if (resultCode == RESULT_CANCELED)
                Toast.makeText(this, "User Cancel", Toast.LENGTH_SHORT).show();
            else {
                error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                Log.d("Error: Internal Server",error.toString());
            }
        }
    }

    private void sendPayments() {
        RequestQueue requestQueue= Volley.newRequestQueue(MainActivity.this);
        StringRequest stringRequest=new StringRequest(Request.Method.POST, API_CHECK_OUT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.toString().contains("Successful")){
                    Toast.makeText(MainActivity.this, "Transactional Successful !", Toast.LENGTH_SHORT).show();
                    group_payment.setVisibility(View.GONE);
                    group_waiting.setVisibility(View.VISIBLE);

                }
                else {
                    Toast.makeText(MainActivity.this, "Transactional Unsuccessful !", Toast.LENGTH_SHORT).show();
                }
                Log.d("Transaction Log",response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error in payment",error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if (paramHash==null)
                    return null;
                Map<String,String> params=new HashMap<>();
                for (String key:paramHash.keySet()){
                    params.put(key,paramHash.get(key));
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params=new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public class getToken extends AsyncTask {

        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog=new ProgressDialog(MainActivity.this,android.R.style.Theme_DeviceDefault_Dialog);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Please wait");
            progressDialog.show();
        }
        @Override
        protected Object doInBackground(Object[] objects) {
String dataFetched="";

//                try {
//                    URL url=new URL(API_GET_TOKEN);
//                    HttpURLConnection httpURLConnection= (HttpURLConnection) url.openConnection();
//                    InputStream inputStream=httpURLConnection.getInputStream();
//                    BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
//                    String line="";
//                    while(line!=null){
//                        line=bufferedReader.readLine();
//                        dataFetched=dataFetched+line;
//                    }
////            JSONArray jsonArray=new JSONArray(data);
////            for (int i=0;i<jsonArray.length();i++){
////                JSONObject jsonObject= (JSONObject) jsonArray.get(i);
////                singleparsed=""
////            }
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();}
////        } catch (JSONException e) {
////            e.printStackTrace();
////        }
//            token=dataFetched;
//                if(!token.isEmpty()){
//                    group_waiting.setVisibility(View.GONE);
//                    group_payment.setVisibility(View.VISIBLE);
//                }
//                return null;


            HttpClient httpClient=new HttpClient();
            httpClient.get(API_GET_TOKEN, new HttpResponseCallback() {
                @Override
                public void success(final String responseBody) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            group_waiting.setVisibility(View.GONE);
                            group_payment.setVisibility(View.VISIBLE);
                            token=responseBody;
                            token="eyJ2ZXJzaW9uIjoyLCJhdXRob3JpemF0aW9uRmluZ2VycHJpbnQiOiIwNDA3ZDc3NzQxOWM3N2Q3M2I3ZjUwZDE5N2Y5ZmMxODQxNjkzMDQyY2MxNmZkZmFhZGU2ZTcxZjQxMjY2Y2E1fGNyZWF0ZWRfYXQ9MjAxOC0wNi0yOFQxNDo0MjoxNi43MDYyNjQ3NzQrMDAwMFx1MDAyNm1lcmNoYW50X2lkPXp3aGt4bTh3ZGNkaHg2NXdcdTAwMjZwdWJsaWNfa2V5PWp4Z2tkam5zMjI0ZGdxamciLCJjb25maWdVcmwiOiJodHRwczovL2FwaS5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tOjQ0My9tZXJjaGFudHMvendoa3htOHdkY2RoeDY1dy9jbGllbnRfYXBpL3YxL2NvbmZpZ3VyYXRpb24iLCJjaGFsbGVuZ2VzIjpbXSwiZW52aXJvbm1lbnQiOiJzYW5kYm94IiwiY2xpZW50QXBpVXJsIjoiaHR0cHM6Ly9hcGkuc2FuZGJveC5icmFpbnRyZWVnYXRld2F5LmNvbTo0NDMvbWVyY2hhbnRzL3p3aGt4bTh3ZGNkaHg2NXcvY2xpZW50X2FwaSIsImFzc2V0c1VybCI6Imh0dHBzOi8vYXNzZXRzLmJyYWludHJlZWdhdGV3YXkuY29tIiwiYXV0aFVybCI6Imh0dHBzOi8vYXV0aC52ZW5tby5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tIiwiYW5hbHl0aWNzIjp7InVybCI6Imh0dHBzOi8vb3JpZ2luLWFuYWx5dGljcy1zYW5kLnNhbmRib3guYnJhaW50cmVlLWFwaS5jb20vendoa3htOHdkY2RoeDY1dyJ9LCJ0aHJlZURTZWN1cmVFbmFibGVkIjp0cnVlLCJwYXlwYWxFbmFibGVkIjp0cnVlLCJwYXlwYWwiOnsiZGlzcGxheU5hbWUiOiJHTEEgVW5pdmVyc2l0eSIsImNsaWVudElkIjoiQVY4OVk1S3ViOXZzeUFVM1dhRmduMXlMLUZCdGpGaEYyYjJYRVg4TlJ3TDlxQk1KcFhVcjRpZFhYRXNfdWVNZTFhb0RtRGZhWWhwZW1IQmkiLCJwcml2YWN5VXJsIjoiaHR0cDovL2V4YW1wbGUuY29tL3BwIiwidXNlckFncmVlbWVudFVybCI6Imh0dHA6Ly9leGFtcGxlLmNvbS90b3MiLCJiYXNlVXJsIjoiaHR0cHM6Ly9hc3NldHMuYnJhaW50cmVlZ2F0ZXdheS5jb20iLCJhc3NldHNVcmwiOiJodHRwczovL2NoZWNrb3V0LnBheXBhbC5jb20iLCJkaXJlY3RCYXNlVXJsIjpudWxsLCJhbGxvd0h0dHAiOnRydWUsImVudmlyb25tZW50Tm9OZXR3b3JrIjpmYWxzZSwiZW52aXJvbm1lbnQiOiJvZmZsaW5lIiwidW52ZXR0ZWRNZXJjaGFudCI6ZmFsc2UsImJyYWludHJlZUNsaWVudElkIjoibWFzdGVyY2xpZW50MyIsImJpbGxpbmdBZ3JlZW1lbnRzRW5hYmxlZCI6dHJ1ZSwibWVyY2hhbnRBY2NvdW50SWQiOiJCaWN5Y2xlUmVudGFsIiwiY3VycmVuY3lJc29Db2RlIjoiSU5SIn0sIm1lcmNoYW50SWQiOiJ6d2hreG04d2RjZGh4NjV3IiwidmVubW8iOiJvZmYifQ==";
                        }
                    });
                }

                @Override
                public void failure(Exception exception) {
                    Log.d("Error: Internal Server",exception.toString());

                }
            });
            return null;
        }
        @Override
        protected void onPostExecute(Object o) {

            super.onPostExecute(o);
           progressDialog.dismiss();
        }

    }


}
