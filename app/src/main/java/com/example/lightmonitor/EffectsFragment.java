package com.example.lightmonitor;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import static android.view.View.VISIBLE;


public class EffectsFragment extends Fragment {

    public EffectsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_effects, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Button b1 = view.findViewById(R.id.button1);
        Button b2 = view.findViewById(R.id.button2);
        Button b3 = view.findViewById(R.id.button3);
        Button b4 = view.findViewById(R.id.button4);
        ArrayList<Button> butts = new ArrayList<>();
        butts.add(b1);
        butts.add(b2);
        butts.add(b3);
        butts.add(b4);
        ArrayList<ButtonSettings> list = getArguments().getParcelableArrayList("list");
        for(int i = 0; i< list.size(); i++){
            Button b = butts.get(i);
            b.setText(list.get(i).type);
            b.setVisibility(VISIBLE);
            setOnClick(b, list.get(i));
        }

        RelativeLayout layout = view.findViewById(R.id.layout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).hideBorders();
            }
        });
    }

    private void setOnClick(final Button btn, final ButtonSettings s){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((MainActivity)getActivity()).sendRequest(v.getContext(), s.type, s.twoColors);

            }
        });
    }



}
