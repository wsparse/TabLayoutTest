package fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ws.tablayouttest.R;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

/**
 * Created by WS on 2016/12/3.
 */

public class HomeFragment extends Fragment {
    View view ;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.home_fragment,container,false);
        return view;
    }
}
