package com.network.filetransfer;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    final int Gray = 0xFF808080;
    final int Blue =0xFF80C8E9;

    // 定义FragmentManager对象
    public FragmentManager fragmentManager;
    // 定义一个ViewPager容器
    private MyFragmentPagerAdapter fragmentPagerAdapter;
    private ViewPager viewPager;

    private RelativeLayout layout_home;
    private RelativeLayout layout_folders;
    private RelativeLayout layout_friends;
    private RelativeLayout layout_settings;
    private ImageView image_home;
    private ImageView image_folders;
    private ImageView image_friends;
    private ImageView image_settings;
    private TextView text_home;
    private TextView text_folders;
    private TextView text_friends;
    private TextView text_settings;

    public MyOnClick myclick;
    public MyPageChangeListener myPageChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 隐藏原生ActionBar
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        fragmentManager = getFragmentManager();
        initViewPager();
        initViews();
        initState();
    }

    private void initViewPager() {
        ArrayList<Fragment> fragmentsList = new ArrayList<>();
        fragmentsList.add(new HomeFragment());
        fragmentsList.add(new FoldersFragment());
        fragmentsList.add(new FriendsFragment());
        fragmentsList.add(new SettingsFragment());
        fragmentPagerAdapter = new MyFragmentPagerAdapter(fragmentManager, fragmentsList);
    }

    private void initViews() {
        myclick = new MyOnClick();
        myPageChange = new MyPageChangeListener();

        viewPager = (ViewPager) findViewById(R.id.viewpager_content);
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setOnPageChangeListener(myPageChange);

        layout_home = (RelativeLayout) findViewById(R.id.layout_home);
        layout_folders = (RelativeLayout) findViewById(R.id.layout_folders);
        layout_friends = (RelativeLayout) findViewById(R.id.layout_friends);
        layout_settings = (RelativeLayout) findViewById(R.id.layout_settings);
        layout_home.setOnClickListener(myclick);
        layout_folders.setOnClickListener(myclick);
        layout_friends.setOnClickListener(myclick);
        layout_settings.setOnClickListener(myclick);
        image_home = (ImageView) findViewById(R.id.image_home);
        image_folders = (ImageView) findViewById(R.id.image_folders);
        image_friends = (ImageView) findViewById(R.id.image_friends);
        image_settings = (ImageView) findViewById(R.id.image_settings);
        text_home = (TextView) findViewById(R.id.text_home);
        text_folders = (TextView) findViewById(R.id.text_folders);
        text_friends = (TextView) findViewById(R.id.text_friends);
        text_settings = (TextView) findViewById(R.id.text_settings);
    }

    //定义一个设置初始状态的方法
    private void initState() {
        image_home.setImageResource(R.mipmap.ic_tabbar_home_selected);
        text_home.setTextColor(Blue);
        viewPager.setCurrentItem(0);
    }

    public class MyOnClick implements OnClickListener {
        @Override
        public void onClick(View view) {
            clearChioce();
            iconChange(view.getId());
        }
    }

    public class MyPageChangeListener implements OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int arg0) {
            if(arg0 == 2) {
                int i = viewPager.getCurrentItem();
                clearChioce();
                iconChange(i);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {}

        @Override
        public void onPageSelected(int index) {}

    }

    //建立一个清空选中状态的方法
    public void clearChioce() {
        image_home.setImageResource(R.mipmap.ic_tabbar_home_unselected);
        text_home.setTextColor(Gray);
        image_folders.setImageResource(R.mipmap.ic_tabbar_folders_unselected);
        text_folders.setTextColor(Gray);
        image_friends.setImageResource(R.mipmap.ic_tabbar_friends_unselected);
        text_friends.setTextColor(Gray);
        image_settings.setImageResource(R.mipmap.ic_tabbar_settings_unselected);
        text_settings.setTextColor(Gray);
    }

    //定义一个底部导航栏图标变化的方法
    public void iconChange(int num) {
        switch (num) {
            case R.id.layout_home:case 0:
                image_home.setImageResource(R.mipmap.ic_tabbar_home_selected);
                text_home.setTextColor(Blue);
                viewPager.setCurrentItem(0);
                break;
            case R.id.layout_folders:case 1:
                image_folders.setImageResource(R.mipmap.ic_tabbar_folders_selected);
                text_folders.setTextColor(Blue);
                viewPager.setCurrentItem(1);
                break;
            case R.id.layout_friends:case 2:
                image_friends.setImageResource(R.mipmap.ic_tabbar_friends_selected);
                text_friends.setTextColor(Blue);
                viewPager.setCurrentItem(2);
                break;
            case R.id.layout_settings:case 3:
                image_settings.setImageResource(R.mipmap.ic_tabbar_settings_selected);
                text_settings.setTextColor(Blue);
                viewPager.setCurrentItem(3);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
