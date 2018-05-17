package com.dexfire;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    //private View mContentView;
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();

    private VideoView mVideoView;
    private ArrayList<HashMap<String,Object>> list = new ArrayList<>();
    private boolean assureToResetData = false;
    private static final String NAME="name",IMGID="imgId",REMAIM="remain",SUMNUM="sunNum";
    private int lastRowForUndo = -1;
    private int allNum;
    private String TAG = "夏日祭";
    
    private void resetData(){
        if(assureToResetData){
            assureToResetData = false;
            addPrizes("一等奖 - 萌妹纸亲手挑选的♂本子",R.drawable.ic_launcher_background,3);
            addPrizes("二等奖 - 萌妹纸亲手挑选的♂本子",R.drawable.ic_launcher_background,20);
            addPrizes("三等奖 - 来历不明的钥匙链",R.drawable.ic_launcher_background,50);
            Toast.makeText(this,"报告舰长！！\n少女填装完毕！！",Toast.LENGTH_LONG).show();
        }else{
            assureToResetData = true;
            Toast.makeText(this,"高能警告！！\n再点一次重置来确认操作！！",Toast.LENGTH_LONG).show();
        }

    }

    private int[] getRemainNum(){
        int[] remain = new int[list.size()];
        for(int i=0;i<list.size();i++){
            HashMap<String,Object> map = list.get(i);
            remain[i]=(int)(map.get(REMAIM));
        }
        return remain;
    }

    /**
     * 返回一个阶梯状数组，其中包含了现有的奖品数目【先高级，后低级】
     * 如 int[]{1,5,6,7} => 一等奖 1 件， 二等奖 4 件， 三等奖 1 件， 四等奖 1 件。
     * @return
     */
    private int[] getRemainLevelNum(){
        int[] remain = new int[list.size()];
        for(int i=0;i<list.size();i++){
            HashMap<String,Object> map = list.get(i);
            if(i>0)
                remain[i]=remain[i-1]+(int)(map.get(REMAIM));
            else
                remain[i]=(int)(map.get(REMAIM));
            Log.i(TAG, "getRemainLevelNum: "+"remain["+i+"] = "+remain[i]);
        }
        return remain;
    }

    private int makeARow(){
        Random random = new Random(System.currentTimeMillis());
        int[] remain = getRemainLevelNum();
        if(remain.length>0 && remain[remain.length-1]>0){
            int result = random.nextInt(remain[remain.length-1]) + 1;
            int i;
            for(i=0;i<remain.length;i++){
                if(result<=remain[i] & result !=0) break;
            }
            HashMap<String,Object> prize = list.get(i);
            if((int)prize.get(REMAIM)<=1){
                return makeARow();      // 以防万一算错了，没了就重新Row
//                Toast.makeText(this,((String)prize.get(NAME))+"用光了。\n",Toast.LENGTH_LONG).show();
//                checkPrintRemain();
            }
            prize.put(REMAIM,(int)prize.get(REMAIM)-1);
            list.set(i,prize);          // 直行更改到列表
            Toast.makeText(this,"(●´∀｀●) 恭喜获奖\n\n\n♥ "+(String)prize.get(NAME)+" ♥",Toast.LENGTH_LONG).show();
            lastRowForUndo = result;
            return result;
        }else{
            Toast.makeText(this,"o(╯□╰)o，\n   奖品都没了，收摊回家吧~\n",Toast.LENGTH_LONG).show();
            return -1;
        }
    }

    private void undoRow(){
        if(lastRowForUndo>=0 && lastRowForUndo <list.size()){
            list.get(lastRowForUndo).put(REMAIM,(int)(list.get(lastRowForUndo).get(REMAIM))+1);
            Toast.makeText(this,"时间啊，逆转吧！！！\n   你成功撤销了一次抽奖\n(⊙_⊙;)\n",Toast.LENGTH_LONG).show();
        }

    }

    private void addPrizes(String name,int imgID,int sumNum){
        HashMap<String, Object> map = new HashMap<>();
        map.put(NAME,name);
        map.put(IMGID,imgID);
        map.put(REMAIM,sumNum);
        map.put(SUMNUM,sumNum);
        allNum += sumNum;
        list.add(map);
    }

    private void checkPrintRemain(){
        String report = "余量报告:\n";
        int[] rem = getRemainNum();

        int remain = rem.length <= 0 ? 0: rem[rem.length-1];

        // 添加核能报警信息
        if(remain<=0){
            report += "核能警报：真的真的真的没有库存了~~"+"\n";
        }else if(remain<=0.25*allNum){
            report += "警报：库存不足25%"+"\n";
        }

        // 详细报告
        report += "总余量:"+remain+"\n";
        for(HashMap<String,Object> map : list){
            report += (String)(map.get(NAME))+" : ";
            report += (int)(map.get(REMAIM))+"/"+(int)(map.get(SUMNUM))+"\n";
        }
        Toast.makeText(this,report,Toast.LENGTH_LONG).show();
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
//            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private Button mButtonRow;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mVideoView = findViewById(R.id.videoView);
        mButtonRow=findViewById(R.id.ButtonRow);
//        // Set up the user interaction to manually show or hide the system UI.
//        mContentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggle();
//            }
//        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        mButtonRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeARow();
            }
        });
        //mVideoView.setVideoURI(Uri.parse());
        mVideoView.setVideoURI(Uri.parse("android.resource://"+getPackageName()+ "/"+R.raw.video_back));
        mVideoView.start();

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mVideoView.start();
            }
        });
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
//        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("查询").setIcon(android.R.drawable.ic_menu_info_details).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(lastRowForUndo!=-1 && lastRowForUndo< list.size())
                Toast.makeText(MainActivity.this,"上次获奖信息\n\n"+list.get(lastRowForUndo).get(NAME),Toast.LENGTH_LONG).show();
                checkPrintRemain();
                return true;
            }
        }).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add("撤销").setIcon(android.R.drawable.ic_menu_revert).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                undoRow();
                return true;
            }
        }).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add("重置抽奖!!").setIcon(android.R.drawable.ic_menu_manage).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                resetData();
                return true;
            }
        }).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:{
                toggle();
            }
        }
        return true;
    }
}
