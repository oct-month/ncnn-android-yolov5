package snnu.cs.yolov5ncnn;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FilenameFilter;

public class HistoryFragment extends Fragment
{
    public HistoryFragment()
    {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance()
    {
        HistoryFragment fragment = new HistoryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        RecyclerView imageListView = view.findViewById(R.id.image_list_view);
        LinearLayoutManager layoutManager = new GridLayoutManager(view.getContext(), 2, GridLayoutManager.VERTICAL, false);
        imageListView.setLayoutManager(layoutManager);
        // layoutManager.setOrientation(RecyclerView.VERTICAL);
        imageListView.setAdapter(new ImageListAdapter(getContext().getCacheDir().list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".jpg")) {
                    return true;
                }
                return false;
            }
        })));
        imageListView.addItemDecoration(new DividerItemDecoration(view.getContext(), RecyclerView.HORIZONTAL));
        imageListView.addItemDecoration(new DividerItemDecoration(view.getContext(), RecyclerView.VERTICAL));
        imageListView.setItemAnimator(new DefaultItemAnimator());
        return view;
    }
}