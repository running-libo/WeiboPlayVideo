package com.example.weiboplay.weiboplayvideo;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.bumptech.glide.Glide;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import java.util.ArrayList;
import java.util.List;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CommonAdapter<LocalVideoBean> adapter;
    private ArrayList<LocalVideoBean> datas = new ArrayList();
    private LinearLayoutManager linearLayoutManager;

    /** 上一次播放位置 */
    private int lastPos;
    /** 这一次该播放位置 */
    private int curPos;
    private RelativeLayout curLLplay;
    private RelativeLayout lastLLplay;
    private View lastShadow;
    private View curShadow;
    private JCVideoPlayerStandard playView;
    private int screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initData();
    }

    private void init() {

        playView = new JCVideoPlayerStandard(this);

        screenHeight = getResources().getDisplayMetrics().heightPixels;

        recyclerView = (RecyclerView) findViewById(R.id.rv_part_detail);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new CommonAdapter<LocalVideoBean>(getApplicationContext(),R.layout.item,datas) {
            @Override
            protected void convert(ViewHolder holder, LocalVideoBean localVideoBean, int position) {
                if(position == datas.size()-1){
                    ImageView ivPlayIcon = holder.getView(R.id.iv_icon);
                    ivPlayIcon.setVisibility(View.GONE);
                    return;
                }

                ImageView ivCover = holder.getView(R.id.iv_cover);
                Glide.with(getApplicationContext()).load(localVideoBean.getPath()).into(ivCover);
            }
        };
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    chargeCenter();
                }
            }
        });
    }

    /**
     * 设置实际高度
     */
    private void setRealHeight(View view){
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        layoutParams.height = dp2px(200);
        view.setLayoutParams(layoutParams);
    }

    private void chargeCenter(){
        curPos = chargeWhichInCenter();

        //切换视频:  上一个还原遮罩,当前添加遮罩；上一个移除播放器，当前项添加播放器
        if(curPos != lastPos){

            if(lastLLplay != null){
                lastLLplay.removeView(playView);
            }

            if(lastShadow != null){
                alphaAnim(lastShadow,0,1);
                lastShadow.setVisibility(View.VISIBLE);
            }

            curShadow = linearLayoutManager.findViewByPosition(curPos).findViewById(R.id.view_shadow);
            curLLplay = linearLayoutManager.findViewByPosition(curPos).findViewById(R.id.rl_curplay);

            curLLplay.addView(playView);
            setRealHeight(playView);
            alphaAnim(curShadow,1,0);
            curShadow.setVisibility(View.GONE);

            playView.setUp(datas.get(curPos).getPath(),JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL,"标题");
            playView.startVideo();

            lastPos = curPos;
            lastShadow = curShadow;
            lastLLplay = curLLplay;
        }

    }

    private void initData(){
        datas.addAll(getList(getApplicationContext()));
        datas.add(new LocalVideoBean());
        adapter.notifyDataSetChanged();

        recyclerView.smoothScrollBy(0,3);
    }

    public List<LocalVideoBean> getList(Context context) {
        List<LocalVideoBean> sysVideoList = new ArrayList<>();
        // MediaStore.Video.Thumbnails.DATA:视频缩略图的文件路径
        String[] thumbColumns = {MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.VIDEO_ID};
        // 视频其他信息的查询条件
        String[] mediaColumns = {MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION};

        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media
                        .EXTERNAL_CONTENT_URI,
                mediaColumns, null, null, null);

        if (cursor == null) {
            return sysVideoList;
        }
        if (cursor.moveToFirst()) {
            do {
                LocalVideoBean info = new LocalVideoBean();

                info.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media
                        .DATA)));
                info.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video
                        .Media.DURATION)));
                sysVideoList.add(info);
            } while (cursor.moveToNext());
        }
        return sysVideoList;
    }

    /**
     * 判断当前应该哪个position视频
     */
    private int chargeWhichInCenter(){
        int firstPos = linearLayoutManager.findFirstVisibleItemPosition();
        int lastPos = linearLayoutManager.findLastVisibleItemPosition();
        int curpos = firstPos;
        for(int i=firstPos;i<=lastPos;i++){
            if(linearLayoutManager.findViewByPosition(i) != null &&
                    linearLayoutManager.findViewByPosition(i).findViewById(R.id.ll_item) != null){
                RelativeLayout curLlItem = linearLayoutManager.findViewByPosition(i).findViewById(R.id.ll_item);
                if(curLlItem.getTop() < screenHeight/2 && curLlItem.getBottom() > screenHeight/2){
                    curpos = i;
                }
            }

        }
        return curpos;
    }

    private void alphaAnim(View view, float fromAlpha, float toAlpha){
        AlphaAnimation anim = new AlphaAnimation(fromAlpha,toAlpha);
        anim.setDuration(800);
        view.startAnimation(anim);
    }

    private int dp2px(int dp){
        return (int) (getResources().getDisplayMetrics().density*dp+0.5);
    }
}
