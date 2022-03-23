package snnu.cs.yolov5ncnn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder>
{
    private List<String> localDataSets;
    private Context mcontext;

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private final CardView historyCard;
        private final ImageView historyImg;
        private final Button historyBtn;

        public ViewHolder(View view)
        {
            super(view);
            historyCard = view.findViewById(R.id.history_card);
            historyImg = view.findViewById(R.id.history_img);
            historyBtn = view.findViewById(R.id.history_btn);

            historyCard.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    historyBtn.setVisibility(View.VISIBLE);
                    return true;
                }
            });
            historyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    File file = new File(mcontext.getCacheDir(), localDataSets.get(position));
                    if (file.delete()) {
                        Toast.makeText(mcontext, "图片已删除", Toast.LENGTH_SHORT).show();
                        localDataSets.remove(position);
                        notifyItemRemoved(position);
                    }
                }
            });
        }

        public ImageView getImageView()
        {
            return historyImg;
        }
    }

    public ImageListAdapter(String[] dataSet)
    {
        localDataSets = new LinkedList<>(Arrays.asList(dataSet));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        this.mcontext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        String filename = localDataSets.get(position);
        try {
            File file = new File(mcontext.getCacheDir(), filename);
            FileInputStream in = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            holder.getImageView().setImageBitmap(bitmap);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount()
    {
        return localDataSets.size();
    }

    public void updateDatasets(String[] dataSet)
    {
        localDataSets = new LinkedList<>(Arrays.asList(dataSet));
        notifyDataSetChanged();
    }

    public void clearAllImg()
    {
        String[] filenames = localDataSets.toArray(new String[0]);
        for (int i = filenames.length - 1; i >= 0; -- i) {
            File f = new File(mcontext.getCacheDir(), filenames[i]);
            if (f.delete()) {
                localDataSets.remove(i);
            }
        }
        // localDataSets.clear();
        notifyDataSetChanged();
    }
}
