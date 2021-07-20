package com.example.huemidity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelectCity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectCity extends Fragment {
    private static final String CITY = "city";

    private String city;

    private EditText cityTxt;
    private ImageView btnBack;

    public SelectCity() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param city Parameter 1.
     * @return A new instance of fragment SelectCity.
     */
    public static SelectCity newInstance(String city) {
        SelectCity fragment = new SelectCity();
        Bundle args = new Bundle();
        args.putString(CITY, city);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            city = getArguments().getString(CITY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_city, container, false);
        this.cityTxt = (EditText) view.findViewById(R.id.input_city);
        cityTxt.setText(city, TextView.BufferType.EDITABLE);
        //TODO: make cards selectable, select city based on selection
        this.btnBack = (ImageView) view.findViewById(R.id.btn_back);
        this.btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getActivity().onBackPressed();
            }
        });
        return view;
    }

}