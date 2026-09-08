package com.samp.mobile.launcher.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.joom.paranoid.Obfuscate;
import com.samp.mobile.R;
import com.samp.mobile.launcher.MainActivity;
import com.samp.mobile.launcher.adapters.FavouriteServerAdapter;

import java.util.Objects;

@Obfuscate
public class ServerPagesItemFragment extends Fragment {
    private int pagePosition;

    public static ServerPagesItemFragment newInstance(int page) {
        ServerPagesItemFragment instance = new ServerPagesItemFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("page", page);
        instance.setArguments(bundle);
        return instance;
    }

    public int getPage() {
        return pagePosition;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) pagePosition = getArguments().getInt("page");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        ((MainActivity) getActivity()).hideKeyboard(getActivity());

        View addButton = view.findViewById(R.id.buttonServer);
        if (addButton != null) addButton.setVisibility(View.GONE);

        RecyclerView recyclerView = view.findViewById(R.id.server_recycler);
        if (recyclerView.getItemAnimator() != null) {
            ((SimpleItemAnimator) Objects.requireNonNull(recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(new FavouriteServerAdapter(getActivity()));
        return view;
    }
}
