package com.vlim.controldetiempos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class LoginActivity extends AppCompatActivity {

    EditText et_usuario, et_pass;
    Button btn_entrar;
    TextView tv_siga, tv_usuario, tv_password;
    String usrLogin = null, passLogin = null, nombreUsuario = null, apaterno = null, amaterno = null, urlimg = null, idusr = null, logo = null;
    public static final     String KEY_USERNAME = "usr";
    public static final     String KEY_PASSWORD = "pass";

    ProgressDialog progressDialog;
    JSONArray jsonArr;
    String JsonResponse = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setBackgroundDrawableResource(R.mipmap.splash);
        setContentView(R.layout.activity_login);

        et_usuario  = findViewById(R.id.et_usr);
        et_pass     = findViewById(R.id.et_pass);
        btn_entrar  = findViewById(R.id.btn_entrar);
        tv_siga     = findViewById(R.id.tv_mensajeinicial);
        tv_usuario  = findViewById(R.id.tv_usr);
        tv_password = findViewById(R.id.tv_pass);

        Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(),"fonts/nasalization-rg.ttf");
        tv_siga.setTypeface(tf);
        tv_usuario.setTypeface(tf);
        tv_password.setTypeface(tf);


        recuperaInfoUsuario();

        btn_entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usrLogin  = et_usuario.getText().toString().trim();
                passLogin     = et_pass.getText().toString().trim();

                if(usrLogin.equalsIgnoreCase("")){
                    et_usuario.setError("Ingresa tu nombre de usuario");
                }
                else if(passLogin.equalsIgnoreCase("")){
                    et_pass.setError("Ingresa tu contraseña");
                }
                else{
                    preparaLogin(usrLogin, passLogin);
                }

                /*Intent formulario = new Intent().setClass(LoginActivity.this, Formulario.class);
                startActivity(formulario);
                finish();*/
            }


        });
    }

    private void recuperaInfoUsuario() {
        // lee datos del usuario
        UserSQLiteHelper dbLogin =
                new UserSQLiteHelper(this, "DBUsuarios", null, Config.VERSION_DB);
        SQLiteDatabase db = dbLogin.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT usuario, password FROM Usuarios", null);
        String password = null;
        if (c.moveToFirst()) {
            Log.v(Config.TAG, "hay cosas");
            String usr_recover = c.getString(0);
            password = c.getString(1);

            et_usuario.setText(usr_recover.toString());
            c.close();
            db.close();
            preparaLogin(usr_recover, password);

            Log.v(Config.TAG, usr_recover);
        }
        else{
            Log.v(Config.TAG, "NO hay cosas");
        }
    }

    private void preparaLogin(String usr, String pass) {
        JSONObject post_dict = new JSONObject();

        try {
            post_dict.put(KEY_USERNAME , usr);
            post_dict.put(KEY_PASSWORD, pass);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (post_dict.length() > 0) {
            Log.v(Config.TAG, "postdic len: " + String.valueOf(post_dict));

            login(String.valueOf(post_dict));
        }
    }

    private void login(String params) {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Validando...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setProgress(0);
        progressDialog.show();

        try {
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            final JSONObject jsonBody = new JSONObject();

            JSONObject jsonObject = new JSONObject(params);
            String usuario = jsonObject.getString(KEY_USERNAME);
            String password = jsonObject.getString(KEY_PASSWORD);

            jsonBody.put(KEY_USERNAME, usuario);
            jsonBody.put(KEY_PASSWORD, password);

            final String requestBody = jsonBody.toString();
            Log.d(Config.TAG, "requestBody: " + requestBody);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.LOGIN_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        jsonArr = new JSONArray(response);
                        JsonResponse = response;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //Log.i("VOLLEYresponse", String.valueOf(jsonArr));
                    Log.i(Config.TAG, "VOLLEY response login: " + response);
                    progressDialog.dismiss();
                    entrar(response);
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(Config.TAG, "VOLLEY: " + error.toString());
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Error en la conexión.", Toast.LENGTH_LONG).show();

                    if (error instanceof NetworkError) {
                    } else if (error instanceof ServerError) {
                    } else if (error instanceof AuthFailureError) {
                    } else if (error instanceof ParseError) {
                    } else if (error instanceof NoConnectionError) {
                    } else if (error instanceof TimeoutError) {
                        /*Toast.makeText(getApplicationContext(),
                                "Oops. Timeout error!",
                                Toast.LENGTH_LONG).show();*/
                        Log.d(Config.TAG, "Oops. Timeout error!");
                    }
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {

            e.printStackTrace();
        }
    }

    private void entrar(String response) {
        Log.i(Config.TAG, "Respuesta entrar: " + response);
        String nick = "", respuesta = "", nombreUsuario = "", idusuario = "", imagenusr = "";
        try {
            JSONArray array = new JSONArray(response);
            JSONObject obj;
            for (int i = 0; i < array.length(); i++) {
                obj = array.getJSONObject(i);
                respuesta = obj.getString("respuesta");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        //Toast.makeText(getApplicationContext(), respuesta + ", " , Toast.LENGTH_LONG).show();
        Log.d(Config.TAG, "Respuesta: " + respuesta);
        if(respuesta.equals("OK")){
            try {
                JSONArray arrayOK = new JSONArray(response);
                JSONObject obj;
                for (int i = 0; i < arrayOK.length(); i++) {
                    obj = arrayOK.getJSONObject(i);

                    nombreUsuario = obj.getString("nombre");
                    apaterno = obj.getString("paterno");
                    amaterno = obj.getString("materno");
                    //urlimg = obj.getString("urlimg");
                    idusr = obj.getString("id");
                    logo = obj.getString("logo");
                }

            }catch (Exception e){
                e.printStackTrace();
            }

            // Guarda en BD
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            UserSQLiteHelper usdbh =
                    new UserSQLiteHelper(LoginActivity.this, "DBUsuarios", null, Config.VERSION_DB);

            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if (db != null) {
                Log.v(Config.TAG, "Hay base login " + logo);
                //Insertamos los datos en la tabla Usuarios
                db.execSQL("INSERT INTO Usuarios (idusuario, nombre, apaterno, amaterno, usuario, password, urlimg) " +
                        "VALUES ('" + idusr + "', '" + nombreUsuario + "', '" + apaterno + "', '" + amaterno + "', '"+ usrLogin + "', '" + passLogin +"', '" + logo +"')");


                //Cerramos la base de datos
                db.close();
            } else {
                Log.v(Config.TAG, "No Hay base");
            }

            Intent formulario = new Intent().setClass(LoginActivity.this, FormularioActivity.class);
            /*Bundle bundledatos = new Bundle();
            bundledatos.putString("nombre", nombreUsuario);
            bundledatos.putString("apaterno", apaterno);
            bundledatos.putString("amaterno", amaterno);
            bundledatos.putString("urlimg", urlimg);
            bundledatos.putString("idusr", idusr);
            formulario.putExtras(bundledatos);*/
            startActivity(formulario);
            finish();
        }
        else{
            Toast.makeText(getApplicationContext(), "Usuario o contraseña incorrectos", Toast.LENGTH_LONG).show();
            borraBD();
        }
    }

    private void borraBD() {
        UserSQLiteHelper usdbh =
                new UserSQLiteHelper(this, "DBUsuarios", null, Config.VERSION_DB);
        SQLiteDatabase db = usdbh.getWritableDatabase();
        db.delete("Usuarios", null, null);
        Log.i(Config.TAG, "Elimina base Usuarios");
    }
}
