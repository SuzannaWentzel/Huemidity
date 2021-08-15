package com.example.huemidity;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelectCity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectCity extends Fragment {
    private static final String CITY = "city";
    private static final String WEATHER = "weather";

    private String city;
    private String weather;

    private EditText cityTxt;
    private ImageView btnBack;
    private FrameLayout fragmentContainer;

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
    public static SelectCity newInstance(String city, String weather) {
        SelectCity fragment = new SelectCity();
        Bundle args = new Bundle();
        args.putString(CITY, city);
        args.putString(WEATHER, weather);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            city = getArguments().getString(CITY);
            weather = getArguments().getString(WEATHER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_city, container, false);
        this.cityTxt = (EditText) view.findViewById(R.id.input_city);
        cityTxt.setText(city, TextView.BufferType.EDITABLE);
        cityTxt.setOnEditorActionListener(actionListener);

        // Show blurred background corresponding to weather
        fragmentContainer = (FrameLayout) view.findViewById(R.id.fragment_container);
        setBackground(weather);

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

    private TextView.OnEditorActionListener actionListener
            = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            ((MainActivity) getActivity()).saveCityInput(null);
        }

        return false;
    };

    private void setBackground(String weather) {
        switch (weather) {
            case "clouds":
                fragmentContainer.setBackgroundResource(R.drawable.clouded_blurred);
                break;
            case "rain":
                fragmentContainer.setBackgroundResource(R.drawable.rain_blurred);
                break;
            case "thunder":
                fragmentContainer.setBackgroundResource(R.drawable.thunder_blurred);
                break;
            case "snow":
                fragmentContainer.setBackgroundResource(R.drawable.snow_blurred);
                break;
            case "fog":
                fragmentContainer.setBackgroundResource(R.drawable.fog_blurred);
                break;
            case "hot":
                fragmentContainer.setBackgroundResource(R.drawable.hot_blurred);
                break;
            default:
                fragmentContainer.setBackgroundResource(R.drawable.sun_blurred);
                break;
        }
    }


}