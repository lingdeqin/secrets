package com.lingdeqin.secrets;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private FloatingActionButton fab;
    private static final String TAG = "=====MainActivity=====:";
    private Menu mMenu;
    private NavController navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_secrets, R.id.nav_home,R.id.nav_secret)
                .setDrawerLayout(drawer)
                .build();


        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        //NavigationUI.setupWithNavController(navigationView, navController);
        //NavigationUI.setupWithNavController(toolbar,navController);
        NavigationUI.setupWithNavController(navigationView, navController);
        mMenu = findViewById(R.id.nav_menu);

        //AppBarConfiguration menuConfiguration = new AppBarConfiguration.Builder(R.id.nav_settings).setDrawerLayout(drawer).build();
        //NavigationUI.setupActionBarWithNavController(this,navController,menuConfiguration);

        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {

                //navController.navigate(R.id.nav_settings);
                Log.i(TAG, "onClick: "+navController.getGraph().getDisplayName());//.navigate(R.id.nav_settings);

                navController.navigate(R.id.nav_secret);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        //.setAction("Action", null).show();

            }
        });


    }

    public void fabInVisible(){
        fab.setVisibility(View.INVISIBLE);
    }

    public void fabVisible(){
        fab.setVisibility(View.VISIBLE);
    }

    public void navSecret(String id){
        Bundle bundle = new Bundle();
        bundle.putString("id", id);

        navController.navigate(R.id.nav_secret,bundle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_settings:
                navController.navigate(R.id.nav_settings);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}