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
    private android.content.SharedPreferences prefs;
    private TextView odometerView, tripView, resetView;
    private EditText odometerInput;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences(PREFS,MODE_PRIVATE);
        setContentView(buildUi());
        refresh();
    }

    private View buildUi(){
        int pad=dp(20);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad,pad,pad,pad);
        root.setBackgroundColor(Color.WHITE);

        TextView title=text("RutasKM",28,true);
        root.addView(title);
        TextView subtitle=text("Control de kilometraje y recorrido",15,false);
        subtitle.setPadding(0,dp(4),0,dp(22)); root.addView(subtitle);

        odometerView=text("",18,true); root.addView(odometerView);
        tripView=text("",26,true); tripView.setPadding(0,dp(12),0,dp(8)); root.addView(tripView);
        resetView=text("",13,false); resetView.setPadding(0,0,0,dp(20)); root.addView(resetView);

        odometerInput=new EditText(this);
        odometerInput.setHint("Km actual del vehículo");
        odometerInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        root.addView(odometerInput,new LinearLayout.LayoutParams(-1,-2));

        Button save=new Button(this); save.setText("Actualizar kilometraje");
        save.setOnClickListener(v->saveOdometer());
        root.addView(save,new LinearLayout.LayoutParams(-1,-2));

        Button reset=new Button(this); reset.setText("Restablecer recorrido a 0 km");
        reset.setOnClickListener(v->confirmReset());
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2); rp.topMargin=dp(14);
        root.addView(reset,rp);

        TextView note=text("Restablecer el recorrido no modifica el kilometraje real del vehículo ni los datos de mantenimiento.",13,false);
        note.setPadding(0,dp(12),0,0); root.addView(note);
        return root;
    }

    private void saveOdometer(){
        String s=odometerInput.getText().toString().trim();
        if(s.isEmpty()){ toast("Ingresa el kilometraje actual."); return; }
        try{
            float n=Float.parseFloat(s);
            float old=prefs.getFloat(KEY_ODOMETER,0f);
            if(n<old){ toast("El kilometraje real no puede disminuir."); return; }
            android.content.SharedPreferences.Editor e=prefs.edit().putFloat(KEY_ODOMETER,n);
            if(!prefs.contains(KEY_TRIP_BASE)) e.putFloat(KEY_TRIP_BASE,n);
            e.apply(); odometerInput.setText(""); refresh();
        }catch(Exception e){ toast("Kilometraje no válido."); }
    }

    private void confirmReset(){
        float current=prefs.getFloat(KEY_ODOMETER,0f);
        new AlertDialog.Builder(this)
          .setTitle("Restablecer recorrido")
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
        TextView v=new TextView(this); v.setText(s); v.setTextSize(sp); v.setTextColor(Color.rgb(25,25,25));
        if(bold)v.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD); return v;
    }
    private int dp(int n){ return Math.round(n*getResources().getDisplayMetrics().density); }
    private void toast(String s){ Toast.makeText(this,s,Toast.LENGTH_SHORT).show(); }
}
