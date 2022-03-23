package snnu.cs.yolov5ncnn;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HistoryFragment extends Fragment
{
    private static final String TAG = HistoryFragment.class.getSimpleName();

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
        ImageListAdapter imageListAdapter = new ImageListAdapter(getLocalImageList());
        imageListView.setAdapter(imageListAdapter);
        imageListView.addItemDecoration(new DividerItemDecoration(view.getContext(), RecyclerView.HORIZONTAL));
        imageListView.addItemDecoration(new DividerItemDecoration(view.getContext(), RecyclerView.VERTICAL));
        imageListView.setItemAnimator(new DefaultItemAnimator());

        Toolbar toolbar = view.findViewById(R.id.history_toolbar);
        toolbar.inflateMenu(R.menu.history_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.history_sync_btn:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                syncImageList();
                            }
                        }).start();
                        break;
                    case R.id.history_clear_btn:
                        clearImageLocal(imageListAdapter);
                        break;
                }
                return true;
            }
        });
        toolbar.getMenu().findItem(R.id.history_sync_btn).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        toolbar.getMenu().findItem(R.id.history_clear_btn).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return view;
    }

    private String[] getLocalImageList()
    {
        FilenameFilter imgFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".jpg")) {
                    return true;
                }
                return false;
            }
        };
        return getContext().getCacheDir().list(imgFilter);
    }

    private void clearImageLocal(ImageListAdapter imageListAdapter)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setMessage("删除所有图片？");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                imageListAdapter.clearAllImg();
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.show();
    }

    private OkHttpClient getUnsafeOkhttpClient()
    {
        try {
            final X509TrustManager manager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    manager
            };
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, manager)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .build();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveByteImage(String name, byte[] bytes)
    {
        try {
            String[] split = name.split("\\.");
            String ext = "." + split[split.length - 1];
            name = name.substring(0, name.length() - ext.length());
            File file = File.createTempFile(name, ext, getContext().getCacheDir());
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.flush();
            fos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refresh()
    {
        // TODO 同步后的刷新问题
        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_fragment);
        final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.detach(currentFragment);
        ft.attach(currentFragment);
        ft.commit();
    }

    // 同步服务器上的图片
    private void syncImageList()
    {
        final String url = "https://www.ablocker.top:8082/api/image";
        final String imgUrl = "https://www.ablocker.top:8082/uploads/";
        final OkHttpClient client = getUnsafeOkhttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                Log.e(TAG, "syncImageList: " + response);
                return;
            }
            List<String> imgList =  new Gson().fromJson(response.body().string(), ImageListResponse.class).getImglist();
            String[] localList = getLocalImageList();
            // 云端有的下载
            for (String p : imgList) {
                boolean flag = true;
                for (String s : localList) {
                    if (p.equals(s)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    request = new Request.Builder()
                            .url(imgUrl + p)
                            .build();
                    response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        byte[] bytes = response.body().bytes();
                        saveByteImage(p, bytes);
                    }
                }
            };
            // 云端没有的上传
            for (String s : localList) {
                boolean flag = true;
                for (String p : imgList) {
                    if (s.equals(p)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    File file = new File(getContext().getCacheDir(), s);
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file", file.getName(), RequestBody.create(file, MediaType.parse("image/jpeg")))
                            .addFormDataPart("name", s)
                            .build();
                    request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();
                    client.newCall(request).execute();
                }
            }
            refresh();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), "图片同步成功", Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
