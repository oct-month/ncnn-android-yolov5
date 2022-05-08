package snnu.cs.yolov5ncnn;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
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

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder>
{
    private static final String TAG = ImageListAdapter.class.getSimpleName();
    private List<String> localDataSets;
    private Context mcontext;

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private final ProgressDialog pDialog;
        private final CardView historyCard;
        private final ImageView historyImg;
        private final Button deleteBtn;
        private final Button uploadBtn;

        public ViewHolder(View view)
        {
            super(view);
            historyCard = view.findViewById(R.id.history_card);
            historyImg = view.findViewById(R.id.history_img);
            deleteBtn = view.findViewById(R.id.delete_btn);
            uploadBtn = view.findViewById(R.id.upload_btn);

            pDialog = new ProgressDialog(mcontext);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setTitle("图片上传中...");
            pDialog.setIcon(R.drawable.ic_baseline_sync_24);
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.setMax(1);

            historyCard.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    deleteBtn.setVisibility(View.VISIBLE);
                    uploadBtn.setVisibility(View.VISIBLE);
                    return true;
                }
            });
            deleteBtn.setOnClickListener(new View.OnClickListener() {
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
            uploadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    String imgT = localDataSets.get(position);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            uploadImg(imgT);
                        }
                    }).start();
                }
            });
        }

        public void uploadImg(String imgT)
        {
            Utils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pDialog.setProgress(0);
                    pDialog.show();
                }
            });

            final String url = "https://www.ablocker.top:8082/api/image";
            final OkHttpClient client = Utils.getUnsafeOkhttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "syncImageList: " + response);
                    return;
                }
                List<String> imgList = new Gson().fromJson(response.body().string(), ImageListResponse.class).getImglist();

                // 云端没有的上传
                boolean flag = true;
                for (String s : imgList) {
                    if (s.equals(imgT)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    File file = new File(mcontext.getCacheDir(), imgT);
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file", file.getName(), RequestBody.create(file, MediaType.parse("image/jpeg")))
                            .addFormDataPart("name", imgT)
                            .build();
                    request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();
                    client.newCall(request).execute();
                    Log.d(TAG, imgT + " uploaded..");
                    Utils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mcontext, "图片上传成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Utils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mcontext, "图片已存在于服务器", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                Utils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mcontext, "服务器错误", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            Utils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pDialog.setProgress(1);
                    pDialog.cancel();
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
