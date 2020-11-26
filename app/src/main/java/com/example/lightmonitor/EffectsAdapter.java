package com.example.lightmonitor;

import android.os.Bundle;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import static java.lang.Math.min;

public class EffectsAdapter extends FragmentPagerAdapter {

    private ArrayList<ButtonSettings> list;

    EffectsAdapter(@NonNull FragmentManager fm) {
        super(fm);
        list = new ArrayList<>();
        list.add(new ButtonSettings("fade",true));
        list.add(new ButtonSettings("rainbow",false));
        list.add(new ButtonSettings("snake",true));
        list.add(new ButtonSettings("off",false));
        list.add(new ButtonSettings("pride",false));
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        EffectsFragment frag = new EffectsFragment();
        ArrayList<ButtonSettings> temp = new ArrayList<>();
        int iterations = min((position+1)*4,list.size());
        for(int i = position*4;i<iterations;i++){
            temp.add(list.get(i));
        }
        Bundle args = new Bundle();
        args.putParcelableArrayList("list", temp);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public int getCount() {
        return (list.size()/4)+1;
    }
}

