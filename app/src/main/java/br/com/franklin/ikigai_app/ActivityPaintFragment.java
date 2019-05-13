package br.com.franklin.ikigai_app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.franklin.ikigai_app.R;

public class ActivityPaintFragment extends Fragment {
    private DoodleView doodleView; //para desenhar e lidar com os eventos de toque na tela

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_paint, container, false);
        setHasOptionsMenu(true);
        doodleView = v.findViewById(R.id.doodleView);
        return v;
    }




}
