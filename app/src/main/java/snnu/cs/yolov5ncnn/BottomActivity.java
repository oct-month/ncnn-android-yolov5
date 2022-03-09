package snnu.cs.yolov5ncnn;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class BottomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_history:
                        openFragment(HistoryFragment.newInstance());
                        return true;
                    case R.id.navigation_camera:
                        openFragment(CameraFragment.newInstance());
                        return true;
                    case R.id.navigation_about:
                        openFragment(AboutFragment.newInstance());
                        return true;
                }
                return false;
            }
        });
        navView.setSelectedItemId(R.id.navigation_history);
    }

    private void openFragment(Fragment fragment)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_fragment, fragment);
        transaction.disallowAddToBackStack();
        // transaction.addToBackStack(null);
        transaction.commit();
    }

}