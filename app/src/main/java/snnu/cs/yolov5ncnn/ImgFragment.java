package snnu.cs.yolov5ncnn;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ImgFragment extends Fragment
{
    private static final String ARG_PARAM1 = "param1";
    private Bitmap mParam1 = null;

    public ImgFragment()
    {
        // Required empty public constructor
    }

    public static ImgFragment newInstance(Bitmap bitmap)
    {
        ImgFragment fragment = new ImgFragment();
        if (bitmap != null) {
            Bundle args = new Bundle();
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bs);
            byte[] bitmapByte = bs.toByteArray();
            args.putByteArray(ARG_PARAM1, bitmapByte);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            byte[] bitmapByte = getArguments().getByteArray(ARG_PARAM1);
            mParam1 = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_img, container, false);
        if (mParam1 != null) {
            PhotoView photoView = view.findViewById(R.id.history_image_view);
            photoView.setImageBitmap(mParam1);
        }
        return view;
    }
}