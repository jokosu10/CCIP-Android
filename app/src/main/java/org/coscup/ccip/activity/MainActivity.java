package org.coscup.ccip.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.coscup.ccip.R;
import org.coscup.ccip.fragment.AnnouncementFragment;
import org.coscup.ccip.fragment.IRCFragment;
import org.coscup.ccip.fragment.MainFragment;
import org.coscup.ccip.fragment.MyTicketFragment;
import org.coscup.ccip.fragment.PuzzleFragment;
import org.coscup.ccip.fragment.ScheduleTabFragment;
import org.coscup.ccip.fragment.SponsorFragment;
import org.coscup.ccip.fragment.StaffFragment;
import org.coscup.ccip.fragment.VenueFragment;
import org.coscup.ccip.util.PreferenceUtil;

public class MainActivity extends AppCompatActivity {
    private static final Uri URI_GITHUB = Uri.parse("https://github.com/CCIP-App/CCIP-Android");
    private static final Uri URI_TELEGRAM = Uri.parse("https://t.me/coscupchat");

    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private static TextView userTitleTextView, userIdTextView;
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = this;

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        userTitleTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_title);
        userIdTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_id);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        setSupportActionBar(toolbar);
        setupDrawerContent(navigationView);

        drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);

        setTitle(R.string.fast_pass);
        Fragment fragment = new MainFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        return jumpToFragment(menuItem);
                    }
                });
    }

    private boolean jumpToFragment(MenuItem menuItem) {
        menuItem.setChecked(true);

        if (menuItem.getItemId() == R.id.star) {
            mActivity.startActivity(new Intent(Intent.ACTION_VIEW, URI_GITHUB));
        } else if (menuItem.getItemId() == R.id.telegram) {
            mActivity.startActivity(new Intent(Intent.ACTION_VIEW, URI_TELEGRAM));
        } else {
            Fragment fragment = null;

            switch (menuItem.getItemId()) {
                case R.id.fast_pass:
                    fragment = new MainFragment();
                    break;
                case R.id.schedule:
                    fragment = new ScheduleTabFragment();
                    break;
                case R.id.announcement:
                    fragment = new AnnouncementFragment();
                    break;
                case R.id.puzzle:
                    fragment = new PuzzleFragment();
                    break;
                case R.id.ticket:
                    fragment = new MyTicketFragment();
                    break;
                case R.id.irc:
                    fragment = new IRCFragment();
                    break;
                case R.id.venue:
                    fragment = new VenueFragment();
                    break;
                case R.id.sponsors:
                    fragment = new SponsorFragment();
                    break;
                case R.id.staffs:
                    fragment = new StaffFragment();
                    break;
            }

            setTitle(menuItem.getTitle());
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        mDrawerLayout.closeDrawers();

        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public static void setUserTitle(String userTitle) {
        userTitleTextView.setVisibility(View.VISIBLE);
        userTitleTextView.setText(userTitle);
    }

    @Override
    public void onBackPressed() {
        if (navigationView.getMenu().findItem(R.id.fast_pass).isChecked()) {
            super.onBackPressed();
        } else {
            setTitle(R.string.fast_pass);
            Fragment fragment = new MainFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
            navigationView.setCheckedItem(R.id.fast_pass);
        }
    }

    public static void setUserId(String userId) {
        userIdTextView.setText(userId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null && result.getContents() != null) {
            PreferenceUtil.setIsNewToken(this, true);
            PreferenceUtil.setToken(this, result.getContents());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
