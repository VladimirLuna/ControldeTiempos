package com.vlim.controldetiempos;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;

public class FormularioActivity extends AppCompatActivity implements View.OnClickListener {

    Spinner sp_proyectos, sp_clientes;
    public TextView fechaView;
    Button btn_fecha, btn_guardar;
    EditText et_actividad, et_horas;
    ImageView imagenEmpresa;
    TextView tv_usuario, tv_proyecto, tv_cliente, tv_numhoras, tv_fechaelab, tv_fecha_sel, tv_selecciona_fecha;
    Integer flagProyecto = 0;
    Integer flagCliente = 0;
    static Integer flagFecha = 0;
    Integer migajas = 0;

    //Variables para obtener la fecha
    Calendar myCalendar = Calendar.getInstance();
    private int mYear, mMonth, mDay, mHour, mMinute;
    String diaSel, mesSel;

    String nombre = "", apaterno = "", amaterno = "", urlimagen = "", fechaSel = "";
    String[] mes = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};

    // variables de envio
    String IDusr = "";
    String tipo_proyecto = "";
    String tipo_cliente = "";
    String horas = "";
    String logo = "";
    static String fechaSeleccionada;

    /*final String[] catProyecto = new String[50];
    final String[] descrProyecto = new String[50];
    final int[] idProyecto = new int[50];
    final String[] descrCliente = new String[1000];
    final int[] arrayClientes = new int[10000];*/

    ProgressDialog progressDialog, progressDialogLista, progressDialogClientes;
    JSONArray jsonArr;
    String JsonResponse = null;
    ArrayList proyectosList, clientesList;
    String idProyecto[], idCliente[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);

        //progressDialog = ProgressDialog.show(FormularioActivity.this, "", "Leyendo información...");

        tv_usuario = findViewById(R.id.tv_usuario);
        tv_proyecto = findViewById(R.id.tv_proyecto);
        tv_cliente = findViewById(R.id.tv_cliente);
        tv_numhoras = findViewById(R.id.tv_numhoras);
        tv_fechaelab = findViewById(R.id.tv_fechaelab);
        fechaView = findViewById(R.id.tv_fecha);
        tv_selecciona_fecha = findViewById(R.id.tv_selecciona_fecha);

        sp_proyectos = findViewById(R.id.sp_proyecto);
        sp_clientes = findViewById(R.id.sp_cliente);

        btn_fecha = findViewById(R.id.btn_fecha);
        btn_guardar = findViewById(R.id.btn_guardar);

        et_horas = findViewById(R.id.et_horas);
        fechaView.setVisibility(View.INVISIBLE);

        imagenEmpresa = findViewById(R.id.img_empresa);

        /*Bundle datosEmpleado    = new Bundle();
        datosEmpleado   = getIntent().getExtras();
        nombre          = datosEmpleado.getString("nombre");
        apaterno        = datosEmpleado.getString("apaterno");
        amaterno        = datosEmpleado.getString("amaterno");
        urlimagen       = datosEmpleado.getString("urlimg");
        IDusr           = datosEmpleado.getString("idusr");*/

        recuperaInfoUsuario();

        Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/nasalization-rg.ttf");
        tv_usuario.setTypeface(tf);
        tv_proyecto.setTypeface(tf);
        tv_cliente.setTypeface(tf);
        tv_numhoras.setTypeface(tf);
        tv_fechaelab.setTypeface(tf);
        fechaView.setTypeface(tf);
        tv_selecciona_fecha.setTypeface(tf);

        Log.d(Config.TAG, "imagen empresa: " + logo);
        Glide.with(getApplicationContext())
                .load(logo)
                .into(imagenEmpresa);



        /*new DownloadImageFromInternet((ImageView) findViewById(R.id.img_empresa))
                .execute("http://www.vlim.com.mx/cajal/empresas/case.png");*/

        /*new DownloadImageFromInternet((ImageView) findViewById(R.id.img_empresa))
                .execute(urlimagen);*/

        catalogoProyectos();
        //pausa();
        catalogoClientes();

        sp_proyectos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                tipo_proyecto = idProyecto[position];
                Log.d(Config.TAG, "Select idProy: " + tipo_proyecto + ", proyecto: " + sp_proyectos.getSelectedItem().toString());
                flagProyecto = 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        sp_clientes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                tipo_cliente = idCliente[position];
                Log.d(Config.TAG, "Select idCliente: " + tipo_cliente + ", cliente: " + sp_clientes.getSelectedItem().toString());
                flagCliente = 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btn_fecha.setOnClickListener(this);

        btn_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Config.TAG, "Guarda: " + flagProyecto + ", " + flagCliente + ", " + flagFecha);
                if (flagProyecto.equals(1)) {
                    if (flagCliente.equals(1)) {
                        if (flagFecha.equals(1)) {
                            if (et_horas.length() > 0) {
                                horas = et_horas.getText().toString().trim();

                                Log.d(Config.TAG, "BTN Guardar: " + IDusr + ", " + tipo_proyecto + ", " + tipo_cliente + ", " + horas + ", " + fechaSeleccionada);
                                insert(IDusr, tipo_proyecto, tipo_cliente, horas, fechaSeleccionada);
                            } else {
                                et_horas.setError("Debes indicar el número de horas");
                                //showSnackBar("Debes indicar el número de horas.");
                            }
                        } else {
                            //showSnackBar("Selecciona una fecha");
                            Toast.makeText(getApplicationContext(), "Debes seleccionar una fecha", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        //showSnackBar("Debes seleccionar un cliente");
                        Toast.makeText(getApplicationContext(), "Debes seleccionar un cliente", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Debes seleccionar un proyecto", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void recuperaInfoUsuario() {
        // lee datos del usuario
        UserSQLiteHelper dbLogin =
                new UserSQLiteHelper(this, "DBUsuarios", null, Config.VERSION_DB);
        SQLiteDatabase db = dbLogin.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT nombre, apaterno, amaterno, idusuario, urlimg FROM Usuarios", null);

        if (c.moveToFirst()) {
            Log.v(Config.TAG, "hay usuario");
            nombre = c.getString(0);
            apaterno = c.getString(1);
            amaterno = c.getString(2);
            IDusr = c.getString(3);
            logo = c.getString(4);
            c.close();
            db.close();
            tv_usuario.setText("Bienvenid@\n" + nombre + " " + apaterno + " " + amaterno);
        }
        else{
            Log.v(Config.TAG, "NO hay cosas");
        }
    }

    private void pausa() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        progressDialog.dismiss();
    }

    private void insert(String IDusr, String IDproyecto, String IDcliente, String horas, String fechaSeleccionada) {

        progressDialog = new ProgressDialog(FormularioActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Enviando reporte.");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setProgress(0);
        progressDialog.show();

        try {
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            final JSONObject jsonBody = new JSONObject();

            jsonBody.put("IDusr", IDusr);
            jsonBody.put("IDproyecto", IDproyecto);
            jsonBody.put("IDcliente", IDcliente);
            jsonBody.put("horas", horas);
            jsonBody.put("fecha", fechaSeleccionada);

            final String requestBody = jsonBody.toString();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.GUARDA_REG_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i(Config.TAG, "VOLLEY response registro reporte: " + response);
                    progressDialog.dismiss();

                    JSONArray jsonRespuesta = null;
                    try {
                        jsonRespuesta = new JSONArray(response);
                        String respuesta = jsonRespuesta.getJSONObject(0).getString("respuesta");
                        //String mensaje = jsonRespuesta.getJSONObject(0).getString("mensaje");

                        if(respuesta.equals("OK")){
                            Log.i(Config.TAG, "Respuesta OK");
                            showAlert("Su reporte ha sido guardado");
                        }
                        else{
                            Log.i(Config.TAG, "Respuesta ERROR");
                            showAlert("Error en el envio. Por favor intente de nuevo." );
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(Config.TAG, "Error envio: " + e.getMessage());
                    }
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(Config.TAG, "VOLLEY: " + error.toString());
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Error en la conexión.", Toast.LENGTH_LONG).show();
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
    } // insert

    private void showAlert(String message) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("SIGA")
                .setCancelable(false)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // ok
                        finish();
                    }
                });
        android.app.AlertDialog alert = builder.create();
        alert.show();

    }

        /*final String[] respuesta = new String[5];
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("IDusr", IDusr);
            jsonObject.put("IDproyecto", IDproyecto);
            jsonObject.put("IDcliente", IDcliente);
            jsonObject.put("horas", horas);
            jsonObject.put("fecha", fechaSeleccionada);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        /*AndroidNetworking.post(insertURL)
                .addJSONObjectBody(jsonObject) // posting json
                .setTag("registro")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(Config.TAG, "Response: " + String.valueOf(response));

                        try {
                            JSONObject respObject = response.getJSONObject(0);
                            respuesta[0] = String.valueOf(respObject.getString("respuesta"));   // respuesta OK/Error

                            if(respuesta[0].equals("OK")){
                                Log.i(Config.TAG, "Respuesta: " + respuesta[0]);
                                /*respuesta[1] = String.valueOf(respObject.getString("nombre"));  // Nombre del usuario
                                respuesta[2] = String.valueOf(respObject.getString("paterno")); // Apellido paterno
                                respuesta[3] = String.valueOf(respObject.getString("materno")); // Apellido materno
                                respuesta[4] = "http://www.vlim.com.mx/cajal/empresas/case.png";   // URL imagen de la empresa
                                Log.i(Config.TAG, "Respuesta: " + respuesta[0] + ", " + respuesta[1] + " " + respuesta[2] + " " + respuesta[3] + " " + respuesta[4]);
                                */

                                /*progressDialog.dismiss();
                                showSnackBar("Actividad registrada");

                                et_horas.setText("");
                                fechaView.setText("dd/mm/yyyy");
                                /*finish();
                                startActivity(getIntent());*/
                           /* }
                            else if(respuesta[0].equals("Error")){
                                Log.e(Config.TAG, "Respuesta: " + respuesta[0]);
                                progressDialog.dismiss();
                                showSnackBar("Error en el registro de la actividad. Por favor intente de nuevo.");

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Log.i(Config.TAG, "Response: " + response.toString());
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.e(Config.TAG, "Error login: " + error.getErrorDetail() + ", " + error.getLocalizedMessage());
                    }
                });*/
    //}  // insert

    /*private void catalogoClientes() {

        final String[] idClienteObj = new String[1];

       /* final ArrayAdapter<String> adapterClientes = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapterClientes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterClientes.add("Selecciona un Cliente...");

        AndroidNetworking.get(catClienteURL)
                .setPriority(Priority.MEDIUM)
                .setTag("catclientes")
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {

                            for (int i = 0; i < response.length(); i++) {
                                //JSONObject clienteObj = response.getJSONObject(i);

                                adapterClientes.add(response.getJSONObject(i).getString("descripcion"));

                                JSONObject proyectoObj = response.getJSONObject(i);
                                //descrCliente[i] = proyectoObj.getString("descripcion");
                                arrayClientes[i+1] = Integer.parseInt(proyectoObj.getString("id"));
                                //Log.i(Config.TAG, "Cliente ID / " + idCliente[i]);
                            }

                            sp_clientes.setAdapter(adapterClientes);
                            sp_clientes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    Log.i(Config.TAG, "Sp cliente: " + String.valueOf(position));
                                    //IDcliente = String.valueOf(position);
                                    //position = position - 1;
                                    IDcliente = String.valueOf(arrayClientes[position]);
                                    //Log.d(Config.TAG, "tam: " + String.valueOf(arrayClientes.length));
                                    flagCliente = 1;
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(Config.TAG, "Error " + anError.getErrorBody());
                    }
                });*/
    //}

    private void catalogoProyectos() {
        progressDialogLista = new ProgressDialog(FormularioActivity.this);
        progressDialogLista.setCancelable(false);
        progressDialogLista.setMessage("Obteniendo proyectos...");
        progressDialogLista.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialogLista.setProgress(0);
        progressDialogLista.show();

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        final JSONObject jsonBody = new JSONObject();

        //jsonBody.put("idusr", idusr);
        final String requestBody = jsonBody.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.CAT_PROYECTO_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    jsonArr = new JSONArray(response);
                    JsonResponse = response;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Log.i("VOLLEYresponse", String.valueOf(jsonArr));
                Log.i(Config.TAG, "VOLLEY response proyectos: " + response);

                proyectosList = new ArrayList();
                idProyecto = new String[jsonArr.length()];

                for (int i = 0; i < jsonArr.length(); i++){
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = jsonArr.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        String nombre_proy = jsonObj.getString("descripcion");
                        String id_proy = jsonObj.getString("id");
                        System.out.println(id_proy + ", " + nombre_proy);

                        idProyecto[i] = id_proy;
                        proyectosList.add(nombre_proy);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }// for

                progressDialogLista.dismiss();

                ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(FormularioActivity.this, android.R.layout.simple_spinner_dropdown_item, proyectosList){
                    public View getView(int position, View convertView, android.view.ViewGroup parent) {
                        TextView v = (TextView) super.getView(position, convertView, parent);
                        //v.setTypeface(tf);
                        v.setTextColor(Color.BLACK);
                        return v;
                    }
                    public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                        TextView v = (TextView) super.getView(position, convertView, parent);
                        //v.setTypeface(tf);
                        return v;
                    }
                };
                adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sp_proyectos.setAdapter(adapter1);

            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Config.TAG, "VOLLEY: " + error.toString());
                progressDialogLista.dismiss();
                Toast.makeText(getApplicationContext(), "Error en la conexión.", Toast.LENGTH_LONG).show();
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
    }


    private void catalogoClientes() {
        progressDialogClientes = new ProgressDialog(FormularioActivity.this);
        progressDialogClientes.setCancelable(false);
        progressDialogClientes.setMessage("Obteniendo clientes...");
        progressDialogClientes.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialogClientes.setProgress(0);
        progressDialogClientes.show();

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        final JSONObject jsonBody = new JSONObject();

        //jsonBody.put("idusr", idusr);
        final String requestBody = jsonBody.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.CAT_CLIENTE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    jsonArr = new JSONArray(response);
                    JsonResponse = response;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Log.i("VOLLEYresponse", String.valueOf(jsonArr));
                Log.i(Config.TAG, "VOLLEY response CLIENTES: " + response);

                clientesList = new ArrayList();
                idCliente = new String[jsonArr.length()];

                for (int i = 0; i < jsonArr.length(); i++){
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = jsonArr.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        String nombre_cliente = jsonObj.getString("descripcion");
                        String id_cliente = jsonObj.getString("id");
                        System.out.println(id_cliente + ", " + nombre_cliente);

                        idCliente[i] = id_cliente;
                        clientesList.add(nombre_cliente);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }// for

                progressDialogClientes.dismiss();

                ArrayAdapter<String> adapterClientes = new ArrayAdapter<String>(FormularioActivity.this, android.R.layout.simple_spinner_dropdown_item, clientesList){
                    public View getView(int position, View convertView, android.view.ViewGroup parent) {
                        TextView v = (TextView) super.getView(position, convertView, parent);
                        //v.setTypeface(tf);
                        v.setTextColor(Color.BLACK);
                        return v;
                    }
                    public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                        TextView v = (TextView) super.getView(position, convertView, parent);
                        //v.setTypeface(tf);
                        return v;
                    }
                };
                adapterClientes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sp_clientes.setAdapter(adapterClientes);

            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Config.TAG, "VOLLEY: " + error.toString());
                progressDialogLista.dismiss();
                Toast.makeText(getApplicationContext(), "Error en la conexión.", Toast.LENGTH_LONG).show();
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
    }


    /*private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView = imageView;
            Toast.makeText(getApplicationContext(), "Leyendo información...", Toast.LENGTH_SHORT).show();
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                bimage = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e(Config.TAG, e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Alternativa 1
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.logout:
                // Cerrar sesion
                finish();
                return true;
            case R.id.free:
                // Liberar sesion web
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_fecha:
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                fechaSeleccionada = year + "-" + monthOfYear + "-" + dayOfMonth;
                                fechaView.setVisibility(View.VISIBLE);
                                //fechaView.setText(fechaSeleccionada);

                                fechaView.setText(dayOfMonth + " / " + (monthOfYear+1) + " / " + year);
                                if (dayOfMonth < 10) {
                                    diaSel = "0" + dayOfMonth;
                                }
                                if (monthOfYear < 10) {
                                    mesSel = "0" + (monthOfYear + 1);
                                }
                                fechaSel = year + "-" + mesSel + "-" + diaSel;   //2018-05-01
                                flagFecha = 1;

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
                break;
        }

    /*public class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            String mes = "" + (month + 1);
            String dia = "" + day;

            if((month + 1) < 10){
                mes = "0" + mes;
            }

            if(day < 10){
                dia = "0" + day;
            }

            fechaSeleccionada = year + "-" + mes + "-" + dia;
            fechaView.setVisibility(View.VISIBLE);
            fechaView.setText(fechaSeleccionada);
            flagFecha = 1;
        }
    }*/
    }
}
