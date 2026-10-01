package ec.fabi.rutaskm;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private static final String PREFS="rutaskm";
    private static final String KEY_ODOMETER="vehicle_odometer_km";
    private static final String KEY_TRIP_BASE="trip_baseline_km";
    private static final String KEY_RESET_AT="trip_reset_at";
    private SharedPreferences prefs;
    private TextView odometerView, tripView, resetView;
    private EditText odometerInput;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences(PREFS,MODE_PRIVATE);
        setContentView(buildUi());
        refresh();
    }

    private View buildUi(){
        ScrollView scroll=new ScrollView(this);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad=dp(18); root.setPadding(pad,pad,pad,dp(28));
        root.setBackgroundColor(Color.WHITE);
        scroll.addView(root,new ScrollView.LayoutParams(-1,-2));

        root.addView(text("RutasKM",28,true));
        TextView sub=text("Panel principal",15,false);
        sub.setPadding(0,dp(3),0,dp(16)); root.addView(sub);

        root.addView(sectionHeader("1. Conductor y vehículo"));
        root.addView(info("Datos del conductor, vehículo, marca, modelo, año, color y kilometraje."));
        odometerView=text("",17,true); root.addView(odometerView);
        odometerInput=new EditText(this);
        odometerInput.setHint("Km actual del vehículo");
        odometerInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        root.addView(odometerInput,new LinearLayout.LayoutParams(-1,-2));
        Button save=new Button(this); save.setText("Actualizar kilometraje");
        save.setOnClickListener(v->saveOdometer()); root.addView(save,new LinearLayout.LayoutParams(-1,-2));

        root.addView(sectionHeader("2. Ruta en curso"));
        tripView=text("",25,true); root.addView(tripView);
        resetView=text("",13,false); resetView.setPadding(0,dp(5),0,dp(8)); root.addView(resetView);
        Button reset=new Button(this); reset.setText("Restablecer recorrido a 0 km");
        reset.setOnClickListener(v->confirmReset()); root.addView(reset,new LinearLayout.LayoutParams(-1,-2));
        root.addView(info("El restablecimiento no modifica el kilometraje real ni el mantenimiento."));

        root.addView(sectionHeader("3. Mantenimiento de aceite"));
        root.addView(info("Kilometraje actual, próximo cambio de aceite y recordatorios de mantenimiento."));
        Button oil=new Button(this); oil.setText("Gestionar mantenimiento de aceite");
        oil.setOnClickListener(v->toast("Mantenimiento de aceite")); root.addView(oil,new LinearLayout.LayoutParams(-1,-2));

        root.addView(sectionHeader("4. Recuperación de acceso"));
        root.addView(info("Recuperación de usuario o contraseña mediante el correo registrado."));
        Button access=new Button(this); access.setText("Recuperar acceso");
        access.setOnClickListener(v->toast("Recuperación de acceso")); root.addView(access,new LinearLayout.LayoutParams(-1,-2));

        root.addView(sectionHeader("5. Historial"));
        root.addView(info("Consulta de recorridos y registros guardados."));
        Button history=new Button(this); history.setText("Ver historial");
        history.setOnClickListener(v->toast("Historial")); root.addView(history,new LinearLayout.LayoutParams(-1,-2));

        return scroll;
    }

    private TextView sectionHeader(String s){
        TextView v=text(s,20,true);
        v.setPadding(0,dp(22),0,dp(8));
        return v;
    }
    private TextView info(String s){
        TextView v=text(s,14,false); v.setPadding(0,0,0,dp(9)); return v;
    }
    private void saveOdometer(){
        String s=odometerInput.getText().toString().trim();
        if(s.isEmpty()){toast("Ingresa el kilometraje actual.");return;}
        try{
            float n=Float.parseFloat(s),old=prefs.getFloat(KEY_ODOMETER,0f);
            if(n<old){toast("El kilometraje real no puede disminuir.");return;}
            SharedPreferences.Editor e=prefs.edit().putFloat(KEY_ODOMETER,n);
            if(!prefs.contains(KEY_TRIP_BASE))e.putFloat(KEY_TRIP_BASE,n);
            e.apply(); odometerInput.setText(""); refresh();
        }catch(Exception e){toast("Kilometraje no válido.");}
    }
    private void confirmReset(){
        float current=prefs.getFloat(KEY_ODOMETER,0f);
        new AlertDialog.Builder(this).setTitle("Restablecer recorrido")
        .setMessage("El contador de recorrido volverá a 0 km. El kilometraje real del vehículo y los datos de mantenimiento no se modificarán.")
        .setNegativeButton("Cancelar",null)
        .setPositiveButton("Restablecer",(d,w)->{
            prefs.edit().putFloat(KEY_TRIP_BASE,current).putLong(KEY_RESET_AT,System.currentTimeMillis()).apply();
            refresh(); toast("Recorrido restablecido a 0 km.");
        }).show();
    }
    private void refresh(){
        float current=prefs.getFloat(KEY_ODOMETER,0f);
        float base=prefs.contains(KEY_TRIP_BASE)?prefs.getFloat(KEY_TRIP_BASE,current):current;
        float trip=Math.max(0f,current-base);
        odometerView.setText(String.format(Locale.getDefault(),"Kilometraje real: %.1f km",current));
        tripView.setText(String.format(Locale.getDefault(),"Tu recorrido: %.1f km",trip));
        long at=prefs.getLong(KEY_RESET_AT,0);
        resetView.setText(at==0?"Recorrido aún no restablecido.":"Último restablecimiento: "+new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault()).format(new Date(at)));
    }
    private TextView text(String s,int sp,boolean bold){
        TextView v=new TextView(this);v.setText(s);v.setTextSize(sp);v.setTextColor(Color.rgb(25,25,25));
        if(bold)v.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);return v;
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
