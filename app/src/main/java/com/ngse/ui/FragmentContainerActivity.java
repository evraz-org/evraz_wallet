package com.ngse.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.franmontiel.localechanger.LocaleChanger;
import com.ngse.ui.main.balanceitems.PortfolioFragment;

import org.evrazcoin.evrazwallet.R;


public class FragmentContainerActivity extends AppCompatActivity {
    private Toolbar mToolbar;



    public static void startThisActivityAndShowHideAssets(Activity activity) {
        Intent intent = new Intent(activity, FragmentContainerActivity.class);
        intent.putExtra("showAssets", true);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragment_container);


        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        boolean isShowAssets = getIntent().getBooleanExtra("showAssets", false);
        if (isShowAssets){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrameLayout, PortfolioFragment.newInstance(true)).commit();
            return;
        }

        findViewById(R.id.sign_up_next).setOnClickListener(v -> {
            Intent intent = new Intent(FragmentContainerActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        newBase = LocaleChanger.configureBaseContext(newBase);
        super.attachBaseContext(newBase);
    }
}
