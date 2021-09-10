package com.example.dailyentry;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    String UserId;
    private long BackPressedTime;
    private Toast BackToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.mainActivityToolBar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View MenuHeader =  navigationView.getHeaderView(0);
        TextView menu_fullName = (TextView)MenuHeader.findViewById(R.id.loginUserFullName);
        TextView menu_user_email = (TextView)MenuHeader.findViewById(R.id.loginUserEmail);
        ImageView menu_user_img = MenuHeader.findViewById(R.id.loginUserImageView);

        SessionManagement sessionManagement = new SessionManagement(MainActivity.this);
        String LoginUserFullName = sessionManagement.getSession("FirstName") +" "+ sessionManagement.getSession("LastName");
        String LoginUserEmail = sessionManagement.getSession("email");
        String LoginUserImg = sessionManagement.getSession("Img");
        UserId = sessionManagement.getSession("id");

        if (!LoginUserImg.equals("")) {
            byte[] decodedString = Base64.decode(LoginUserImg, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            menu_user_img.setImageBitmap(decodedByte);
        }

        menu_fullName.setText(LoginUserFullName);
        menu_user_email.setText(LoginUserEmail);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, FinancesForm.class));
            }
        });

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                int menuId = destination.getId();
                switch (menuId) {
                    case R.id.nav_home:
                    case R.id.nav_gallery:
                        fab.hide();
                        break;
                    case R.id.nav_slideshow:
                        fab.show();
                        break;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                SessionManagement sessionManagement = new SessionManagement(this);
                sessionManagement.ClearSession();
                finish();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
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

    @Override
    public void onBackPressed() {
        Toolbar toolbar = findViewById(R.id.mainActivityToolBar);
        if (toolbar.getTitle().toString().equals("Home")) {
            if (BackPressedTime + 2000 > System.currentTimeMillis()) {
                BackToast.cancel();
                finish();
                super.onBackPressed();
                return;
            } else {
                BackToast = Toast.makeText(getApplicationContext(), "Press back again to exit", Toast.LENGTH_SHORT);
                BackToast.show();
            }
            BackPressedTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }
}