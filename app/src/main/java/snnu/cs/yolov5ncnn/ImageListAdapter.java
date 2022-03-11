package snnu.cs.yolov5ncnn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder>
{
    private String[] localDataSets;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private final ImageView imageView;
        private final Context mcontext;

        public ViewHolder(View view, Context context)
        {
            super(view);
            imageView = view.findViewById(R.id.history_img);
            mcontext = context;
        }

        public ImageView getImageView()
        {
            return imageView;
        }
    }

    public ImageListAdapter(String[] dataSet)
    {
        localDataSets = dataSet;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_row_item, parent, false);
        return new ViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        String filename = localDataSets[position];
        try {
            File file = new File(holder.mcontext.getCacheDir(), filename);
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
        return localDataSets.length;
    }
}
