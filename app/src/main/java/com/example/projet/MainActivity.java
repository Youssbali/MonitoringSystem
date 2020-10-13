package com.example.projet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.projet.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = (Button) findViewById(R.id.BT);
        btn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.BT:
                Button btn = (Button) findViewById(R.id.BT);
                // Création d’une intention
                Intent playIntent = new Intent(this, activity_monitoring.class);
                // Ajout d’un parametre à l’intention
                Intent intent = playIntent.putExtra("name", "My Name");
                startActivity(playIntent);
                break;
        }
    }
}
