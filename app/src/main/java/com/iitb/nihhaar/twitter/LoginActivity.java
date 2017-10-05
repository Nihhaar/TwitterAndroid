package com.iitb.nihhaar.twitter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LoginCheck";
    Button button_sign;
    private TextView tv_register;
    private EditText inputEmail;
    private EditText inputPassword;
    private SharedPreferences logins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tv_register = (TextView) findViewById(R.id.register);
        tv_register.setOnClickListener(this);

        inputEmail = (EditText)findViewById(R.id.username);
        inputPassword = (EditText)findViewById(R.id.password);
        button_sign = (Button) findViewById(R.id.signin);
        button_sign.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String email = inputEmail.getText().toString();
        String pwd = inputPassword.getText().toString();
        if(v == button_sign){
            if ((!email.equals("")) && ( !pwd.equals("")))
            {
                String url = "http://" + AppUtils.servIP + ":" + AppUtils.servPort + "/" + AppUtils.webApp + "/Login";
                LoginAsyncTask loginAsyncTask = new LoginAsyncTask(this, new MyInterface() {
                    @Override
                    public void myMethod(String response) {
                        Log.d(TAG, "Result: " + response);
                        if(response.equals("0")){
                            Toast.makeText(LoginActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                        }
                        else if(response.equals("1")){
                            Toast.makeText(LoginActivity.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            logins = getSharedPreferences(AppUtils.LOGIN_PREFS_FILE, MODE_PRIVATE);
                            if(parseJsonData(response)) {
                                Intent main = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(main);
                                Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    }
                });
                loginAsyncTask.execute(url, email, pwd);
            }
            else if (!(email).equals(""))
            {
                Toast.makeText(getApplicationContext(),
                        "Password fields empty", Toast.LENGTH_SHORT).show();
            }
            else if (!(pwd).equals(""))
            {
                Toast.makeText(getApplicationContext(),
                        "Email field empty", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(),
                        "Email and Password field are empty", Toast.LENGTH_SHORT).show();
            }
        }

        if(v == tv_register){
            Intent reg = new Intent(LoginActivity.this,RegisterActivity.class);
            startActivity(reg);
        }
    }

    /*
        JSONResponse Attributes :
        a) If Auth Successful then :
        data : id of the user
        status : true

        b) If Auth Unsuccessful :
        message : Authentication Failed
        status : false
     */
    private Boolean parseJsonData(String jsonResponse){
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            Boolean status = jsonObject.getBoolean("status");
            if(status){
                /* Login Success - Store the credentials in shared prefs */
                String id = jsonObject.getString("data");
                SharedPreferences.Editor loginEditor = logins.edit();
                loginEditor.putString("id", id);
                loginEditor.putString("email", inputEmail.getText().toString());
                loginEditor.putString("password", inputPassword.getText().toString());
                loginEditor.putBoolean("signedin", true);
                loginEditor.apply();

                return true;
            }
            else{
                String msg = jsonObject.getString("message");
                Toast.makeText(LoginActivity.this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * We are using this interface to generate a toast message on UI thread using the asynctask.
     * Note that this can also be done using the 'runOnUiThread' method.
     */
    public interface MyInterface {
        public void myMethod(String result);
    }

    /* LoginAsyncTask - AsyncTask for login check */
    private class LoginAsyncTask extends AsyncTask<String, Void, String> {
        private String mTAG = "LoginAsyncTask";
        private MyInterface mListener;
        private Context mContext;

        public LoginAsyncTask(Context mContext, MyInterface mListener){
            this.mContext = mContext;
            this.mListener  = mListener;
        }

        @Override
        protected String doInBackground(String... args) {

            String result = "0";
            if(AppUtils.isNetworkAvailable(mContext)){
                try {
                    /* Authenticate with the server, if success store the credentials in shred prefs */
                    URLConnection conn = new URL(args[0]).openConnection();
                    conn.setDoOutput(true);

                    /* Create the post data */
                    String data = URLEncoder.encode("id", "UTF-8")
                            + "=" + URLEncoder.encode(args[1], "UTF-8");

                    data += "&" + URLEncoder.encode("password", "UTF-8") + "="
                            + URLEncoder.encode(args[2], "UTF-8");

                    /* Post the data */
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                    writer.write(data);
                    writer.flush();
                    writer.close();

                    /* Get the response from server */
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    result = stringBuilder.toString();

                } catch (IOException ex){
                    result = "1";
                    Log.e(mTAG, "Unable to connect to server");
                    ex.printStackTrace();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (mListener != null)
                mListener.myMethod(result);
        }
    }
}
