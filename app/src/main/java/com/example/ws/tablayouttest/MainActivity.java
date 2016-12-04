package com.example.ws.tablayouttest;

import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import adapter.MyFragmentAdapter;
import fragment.FourFragment;
import fragment.HomeFragment;
import fragment.SecondFragment;
import fragment.ThirdFragment;


import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends AppCompatActivity {

    TabLayout tablayout ; //控件tablayout V
    ViewPager viewpager; //标题栏显示内容的容器。V

    List<Fragment> fragmentList = new ArrayList<>(); //内容碎集合 M
    List<String> titleList = new ArrayList<>(); //标题栏集合 M

    MyFragmentAdapter myFragmentAdapter;  //自定义Adapter C
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initAdapter();
        setAdapter();
    }

    private void setAdapter() {
        tablayout.setTabMode(TabLayout.MODE_FIXED); //设置模式
        tablayout.setSelectedTabIndicatorHeight(1);
        tablayout.setSelectedTabIndicatorColor(Color.RED);

        //循环添加标题栏的内容。
        for (int i = 0; i <titleList.size() ; i++) {
            tablayout.addTab(tablayout.newTab().setText(titleList.get(i)));
        }
        tablayout.setupWithViewPager(viewpager); //tablayout绑定viewpager。
        viewpager.setAdapter(myFragmentAdapter);//viewpager绑定适配器。
    }

    private void initAdapter() {
        myFragmentAdapter = new MyFragmentAdapter(fragmentManager,fragmentList,titleList);
    }

    private void initData() {

        //初始化碎片对象，添加到碎片集合中
        fragmentManager = getSupportFragmentManager();
        HomeFragment homeFragment = new HomeFragment();
        SecondFragment secondFragment = new SecondFragment();
        ThirdFragment thirdFragment = new ThirdFragment();
        FourFragment fourFragment = new FourFragment();
        fragmentList.add(homeFragment);
        fragmentList.add(secondFragment);
        fragmentList.add(thirdFragment);
        fragmentList.add(fourFragment);


        //初始化标题栏内容。
        String[] array = getResources().getStringArray(R.array.title);
        for (String string:array) {
            titleList.add(string);
        }

    }

    private void initView() {
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        tablayout = (TabLayout) findViewById(R.id.tablayout);
    }
}
