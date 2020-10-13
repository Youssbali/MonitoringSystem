package com.example.projet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class activity_monitoring extends AppCompatActivity implements View.OnClickListener {
    int UID;
    String strPackageName;
    String RSS;
    List pkgAppsList;
    String Names[]; //le tableau des noms de processus
    int UIDS[]; //le tableau des UIDs
    String RSSs[]; //le tableau des RSSs
    int id_butt = 0;
    final int MSG_CALCUL = 1;
    final int MSG_ARRET = 2;
    int tab_click[]; //tableau des processus avec le nbre de click
    int nb_click[]; //pour afficher le pop up du handler qu'une seule fois
    int nb = 0;
    int proc_annule = 0; //pour désactiver la mise à jour
    int proc_actif = 0; //pour activer la mise à jour
    Boolean arret = true; //pour arrêter les threads correctement
    int h; //le nombre de processus actifs

    //Chaque partie du code est expliqué dans le rapport pour éviter de remplir le code de commentaires
    Runnable r = new Runnable() {
        public void run() {
            if (arret) {
                LinearLayout ll = (LinearLayout) findViewById(R.id.LineLay);
                ll.postDelayed(r, 5000);
                for (int j = 0; j < h; j++) {
                    if (tab_click[j] == 1) {
                        recupDonnees();
                        //System.out.println("je suis rentré" + j);
                        if (nb_click[j] == 0) {
                            String messageString = "Utilisation de mémoire mise-à-jour pour le proc " + j;
                            proc_actif = j;
                            Message msg = mHandler.obtainMessage(
                                    MSG_CALCUL, (Object) messageString);
                            mHandler.sendMessage(msg);
                            nb_click[j]++;
                        }
                    } else if (tab_click[j] == 2) {
                        if (nb_click[j] == 1) {
                            proc_annule = j;
                            String messageString = "Mise-à-jour arrêté pour le proc " + j;
                            Message msg = mHandler.obtainMessage(
                                    MSG_ARRET, (Object) messageString);
                            mHandler.sendMessage(msg);
                            nb_click[j]++;
                        }
                    }
                }
            }
        }
    };
    final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_CALCUL) {
                Button btn = (Button) findViewById(proc_actif);
                btn.setBackgroundColor(Color.parseColor("#a5d152"));
                TextView txt = (TextView) findViewById(proc_actif + h);
                txt.setText("RSS:" + RSSs[proc_actif]);
                Toast.makeText(getBaseContext(),
                        "Info:" + (String) msg.obj,
                        Toast.LENGTH_LONG).show();
            }
            if (msg.what == MSG_ARRET) {
                Button btn = (Button) findViewById(proc_annule);
                btn.setBackgroundColor(Color.parseColor("#ee1010"));
                Toast.makeText(getBaseContext(),
                        "Info:" + (String) msg.obj,
                        Toast.LENGTH_LONG).show();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        Intent intent = getIntent();
        String MonitorName = intent.getStringExtra("name");
    }

    public View createProcessView(int UID, String name, String Rs, int id) {
        RelativeLayout layout = new RelativeLayout(this);
        RelativeLayout.LayoutParams paramsTopLeft =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

        paramsTopLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                RelativeLayout.TRUE);
        paramsTopLeft.addRule(RelativeLayout.ALIGN_PARENT_TOP,
                RelativeLayout.TRUE);
        RelativeLayout.LayoutParams paramsTopRight =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsTopRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
                RelativeLayout.TRUE);
        paramsTopRight.addRule(RelativeLayout.ALIGN_PARENT_TOP,
                RelativeLayout.TRUE);
        RelativeLayout.LayoutParams paramsBottomLeft =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsBottomLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                RelativeLayout.TRUE);
        paramsBottomLeft.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
                RelativeLayout.TRUE);
        Button btn = new Button(this);
        btn.setId(id);
        btn.setText("Monitor");
        btn.setOnClickListener(this);
        layout.addView(btn, paramsTopRight);
        String don = "[" + UID + "]" + name;
        String R = "RSS:" + Rs;
        TextView someTextView = new TextView(this);
        someTextView.setText(don);
        TextView someTextView2 = new TextView(this);
        someTextView2.setId(id + h);
        someTextView2.setText(R);
        layout.addView(someTextView, paramsTopLeft);
        layout.addView(someTextView2, paramsBottomLeft);
        /*System.out.println("L'id du bouton est" +id );
        System.out.println("L'id du texte est" +(id + h) );*/
        return layout;
    }

    public void recupDonnees() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        pkgAppsList = getPackageManager()
                .queryIntentActivities(mainIntent, 0);
        Names = new String[pkgAppsList.size()];
        UIDS = new int[pkgAppsList.size()];
        Process process = null;
        try {
            process = new ProcessBuilder("ps").start();
        } catch (IOException e) {
            return;
        }
        InputStream in = process.getInputStream();
        Scanner scanner = new Scanner(in);
        RSSs = new String[pkgAppsList.size()];
        //System.out.println("la taille de la liste est: " + pkgAppsList.size());
        h = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("u0_")) {
                String[] temp = line.split(" ");
                String packageName = temp[temp.length - 1];
                for (Object object : pkgAppsList) {
                    ResolveInfo info = (ResolveInfo) object;
                    strPackageName = info.activityInfo
                            .applicationInfo.packageName.toString();
                    if (strPackageName.equals(packageName)) {
                        Names[h] = strPackageName;
                        UID = info.activityInfo.applicationInfo.uid;
                        UIDS[h] = UID;
                        RSS = temp[temp.length - 5];
                        RSSs[h] = RSS;
                        h++;
                    }
                }
            }
        }
    }

    public void nbre_click(int id_proc) {
        tab_click[id_proc]++;
    }

    @Override
    public void onClick(View v) {
        id_butt = v.getId();
        nbre_click(id_butt);
        new Thread(r).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //System.out.println("Died too soon");
        arret = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        //System.out.println("I'm back");
        recupDonnees();
        tab_click = new int[h];
        nb_click = new int[h];
        LinearLayout ll = (LinearLayout) findViewById(R.id.LineLay);
        for (int i = 0; i < h; ++i) {
            View v = createProcessView(UIDS[i], Names[i], RSSs[i], i);
            ll.addView(v);
        }
    }
}
