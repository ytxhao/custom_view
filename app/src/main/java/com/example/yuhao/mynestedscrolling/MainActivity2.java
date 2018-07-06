package com.example.yuhao.mynestedscrolling;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;
import android.view.WindowManager;

import com.example.yuhao.mynestedscrolling.view.AlertPullToRefresh;

import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;

public class MainActivity2 extends AppCompatActivity {

    RecyclerView rcc_list;

    private MultiTypeAdapter adapter;
    private Items items;


    RecyclerView rcc_list_down;
    private MultiTypeAdapter adapterDown;
    private Items itemsDown;

    AlertPullToRefresh mPullToRefresh;
    LinearLayoutManager mLayoutManager;

    Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main3);

        rcc_list_down = findViewById(R.id.rcc_list_down);
        mPullToRefresh = findViewById(R.id.recycler_refresh);
        rcc_list = findViewById(R.id.rcc_list);
        adapter = new MultiTypeAdapter();
        adapter.register(TextItem.class, new TextItemViewBinder());
        adapter.register(ImageItem.class, new ImageItemViewBinder());
        adapter.register(RichItem.class, new RichItemViewBinder());
        rcc_list.setAdapter(adapter);
        mPullToRefresh.setOnHeaderRefreshListener(new AlertPullToRefresh.OnHeaderRefreshListener() {
            @Override
            public void onHeaderRefresh(AlertPullToRefresh view) {
                //runOnUiThread();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefresh.onHeaderRefreshComplete();
                    }
                },1000);

            }
        });

        mPullToRefresh.setOnFooterRefreshListener(new AlertPullToRefresh.OnFooterRefreshListener() {
            @Override
            public void onFooterRefresh(AlertPullToRefresh view) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefresh.onFooterRefreshComplete();
                    }
                },1000);

            }
        });

        adapterDown = new MultiTypeAdapter();
        adapterDown.register(TextItem.class, new TextItemViewBinder());
        mLayoutManager = new LinearLayoutManager(this);
        rcc_list_down.setHasFixedSize(true);
        rcc_list_down.setLayoutManager(mLayoutManager);
        rcc_list_down.setAdapter(adapterDown);

        rcc_list_down.setNestedScrollingEnabled(false);
        TextItem textItem = new TextItem("world");
        ImageItem imageItem = new ImageItem(R.mipmap.ic_launcher);
        RichItem richItem = new RichItem("小艾大人赛高", R.mipmap.ic_launcher);

        items = new Items();
        for (int i = 0; i < 1; i++) {
            items.add(textItem);
            items.add(imageItem);
            items.add(richItem);
        }
        adapter.setItems(items);
        adapter.notifyDataSetChanged();


        TextItem textItem2 = new TextItem("world");
        itemsDown = new Items();
        for (int i = 0; i < 15; i++) {
            itemsDown.add(textItem2);
        }
        adapterDown.setItems(itemsDown);
        adapterDown.notifyDataSetChanged();

        mPullToRefresh.setIsHeaderLoad(true);
        mPullToRefresh.setIsFooterLoad(false);
        mPullToRefresh.setPermitToRefreshNoChildView(true);
        mPullToRefresh.setmHeaderTextId(R.string.alert_refresh_load);
        mPullToRefresh.setmFooterTextId(R.string.alert_refresh_load);
    }

}
