package com.example.spacealarm.activity.widget;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.spacealarm.R;
import com.example.spacealarm.activity.MainActivity;
import com.example.spacealarm.fragment.AlarmFragment;
import com.example.spacealarm.fragment.MapFragment;
import com.example.spacealarm.fragment.SettingsFragment;

public class CustomBottomNavigation {

    private static final int[] ICONS = {
            R.drawable.clocks,
            R.drawable.map,
            R.drawable.setting
    };

    private static final String[] TITLES = {
            "主页", "闹钟", "设置"
    };

    static Fragment[] fragments = {
            new AlarmFragment(),
            new MapFragment(),
            new SettingsFragment()
    };

    public static void setup(AppCompatActivity activity, LinearLayout navLayout) {

        LayoutInflater inflater = LayoutInflater.from(activity);
        for (int i = 0; i < TITLES.length; i++) {
            @SuppressLint("ResourceType") View item = inflater.inflate(R.drawable.custom_bottom_nav_item, navLayout, false);
            ImageView icon = item.findViewById(R.id.nav_icon);
            TextView title = item.findViewById(R.id.nav_title);

            icon.setImageResource(ICONS[i]);
            title.setText(TITLES[i]);

            final int index = i;
            item.setOnClickListener(v -> {
                switchFragment(activity, index);
            });

            navLayout.addView(item);
        }

        switchFragment(activity, 0);
    }

    public static void switchFragment(AppCompatActivity activity, int selectedIndex) {
        CustomToolbarManager.switchToolbarForFragment(fragments[selectedIndex].getClass());

        LinearLayout navLayout = activity.findViewById(R.id.custom_bottom_nav);

        for (int i = 0; i < navLayout.getChildCount(); i++) {
            View child = navLayout.getChildAt(i);
            ImageView icon = child.findViewById(R.id.nav_icon);
            TextView title = child.findViewById(R.id.nav_title);

            if (i == selectedIndex) {
                title.setTextColor(Color.parseColor("#2196F3"));
                icon.setColorFilter(Color.parseColor("#2196F3"));
            } else {
                title.setTextColor(Color.parseColor("#444444"));
                icon.setColorFilter(Color.GRAY);
            }
        }

        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragments[selectedIndex])
                .commit();
    }
}
