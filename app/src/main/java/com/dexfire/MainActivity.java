package com.dexfire;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;
import android.widget.VideoView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends Activity {

    //private View mContentView;
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;

    private final Handler mHideHandler = new Handler();
    private VideoView mVideoView;
    private ArrayList<HashMap<String, Object>> list = new ArrayList<>();
    private boolean assureToResetData = false;
    private static final String NAME = "name", IMGID = "imgId", REMAIM = "remain", SUMNUM = "sunNum";
    private int lastRowForUndo = -1;
    private int allNum;
    private String TAG = "夏日祭";
    private boolean rowing = false;
    private AnimatorSet anim_fade_away,anim_switch_row;
    private CircularImageView circularImageView;
    //private Image

    private void resetData() {
        if (assureToResetData) {
            new AlertDialog.Builder(this).setTitle("确认要重置数据吗？").setCancelable(true).setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    assureToResetData = false;
                    list.clear();
                    addPrizes("一等奖 - lovelive 亚克力摆件", R.mipmap.love_live, 2);
                    addPrizes("二等奖 - 某萌妹纸挑选的♂本子", R.mipmap.textbooks, 10);
                    addPrizes("三等奖 - 来历不明的卡贴", R.mipmap.cards, 30);
                    addPrizes("没有中奖呢~\n\n送你一朵小花花吧~ ✿ ", R.mipmap.hana, 30);
                    Toast.makeText(MainActivity.this, "报告舰长！！\n少女填装完毕！！", Toast.LENGTH_SHORT).show();
                    mButtonRow.setVisibility(View.VISIBLE);
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        } else {
            assureToResetData = true;
            Toast.makeText(this, "高能警告！！\n再点一次重置来确认操作！！", Toast.LENGTH_SHORT).show();
        }

    }

    private int[] getRemainNum() {
        int[] remain = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            HashMap<String, Object> map = list.get(i);
            remain[i] = (int) (map.get(REMAIM));
        }
        return remain;
    }

    /**
     * 返回一个阶梯状数组，其中包含了现有的奖品数目【先高级，后低级】
     * 如 int[]{1,5,6,7} => 一等奖 1 件， 二等奖 4 件， 三等奖 1 件， 四等奖 1 件。
     *
     * @return
     */
    private int[] getRemainLevelNum() {
        int[] remain = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            HashMap<String, Object> map = list.get(i);
            if (i > 0)
                remain[i] = remain[i - 1] + (int) (map.get(REMAIM));
            else
                remain[i] = (int) (map.get(REMAIM));
            Log.i(TAG, "getRemainLevelNum: " + "remain[" + i + "] = " + remain[i]);
        }
        return remain;
    }


    private void undoRow() {
        if (lastRowForUndo >= 0 && lastRowForUndo < list.size()) {
            list.get(lastRowForUndo).put(REMAIM, (int) (list.get(lastRowForUndo).get(REMAIM)) + 1);
            lastRowForUndo = -1;
            Toast.makeText(this, "时间啊，逆转吧！！！\n   你成功撤销了一次抽奖\n(⊙_⊙;)\n", Toast.LENGTH_SHORT).show();
        }

    }

    private void addPrizes(String name, int imgID, int sumNum) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(NAME, name);
        map.put(IMGID, imgID);
        map.put(REMAIM, sumNum);
        map.put(SUMNUM, sumNum);
        allNum += sumNum;
        list.add(map);
    }

    private void checkPrintRemain() {

        String report = "余量报告:\n";

        if (lastRowForUndo != -1 && lastRowForUndo < list.size())
            report += "上次获奖信息\n" + list.get(lastRowForUndo).get(NAME) + "\n\n";

        int[] rem = getRemainLevelNum();

        int remain = rem.length <= 0 ? 0 : rem[rem.length - 1];

        // 添加核能报警信息
        if (remain <= 0) {
            report += "核能警报：真的真的真的没有库存了~~" + "\n";
        } else if (remain <= 0.25 * allNum) {
            report += "警报：库存不足25%" + "\n";
        }

        // 详细报告
        report += "总余量:" + remain + "\n";
        for (HashMap<String, Object> map : list) {
            report += (String) (map.get(NAME)) + " : ";
            report += (int) (map.get(REMAIM)) + "/" + (int) (map.get(SUMNUM)) + "\n";
        }
        Toast.makeText(this, report, Toast.LENGTH_LONG).show();
    }

    //private View mControlsView;100
    private Button mButtonRow;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            //mControlsView.setVisibility(View.VISIBLE);
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mVisible = true;
        show();
        Toolbar tool = findViewById(R.id.toolbar);
        setActionBar(tool);
        circularImageView = findViewById(R.id.circularimageview);
        anim_fade_away = (AnimatorSet) (AnimatorInflater.loadAnimator(this, R.animator.anim_scale_fade_out));
        anim_switch_row = (AnimatorSet) (AnimatorInflater.loadAnimator(this, R.animator.anim_image_switch));
        mVideoView = findViewById(R.id.videoView);
        mButtonRow = findViewById(R.id.ButtonRow);
        mButtonRow.setHeight(mButtonRow.getWidth());
        mButtonRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realRow();
            }
        });
        mVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video_back));
        mVideoView.start();
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mVideoView.start();
            }
        });
        anim_fade_away.setTarget(mButtonRow);
        anim_fade_away.start();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video_back));
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        mVideoView.start();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mVideoView.pause();
        super.onStop();
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
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
//        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
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


    //region Making Row
    private static final int[] ROW_DELAY_TIME = new int[]{100, 200, 300, 400, 550, 750, 950, 1250,1550,2000};
    int[] row_anim_sequence = new int[ROW_DELAY_TIME.length];
    int row_anim_timepassed = 0;
    private CountDownTimer countDownTimer = new CountDownTimer(2000,50){
        @Override
        public void onTick(long millisUntilFinished) {
            row_anim_timepassed += 50;
            for(int i = 0; i < ROW_DELAY_TIME.length-1; i++){
                if(row_anim_timepassed ==ROW_DELAY_TIME[i]){
                    circularImageView.setImageDrawable(getDrawable((int)list.get(row_anim_sequence[i]).get(IMGID)));
                    anim_switch_row.setTarget(circularImageView);
                    anim_switch_row.start();
                }
            }
        }

        @Override
        public void onFinish() {
            rowing = false;
            int i =row_anim_sequence[ROW_DELAY_TIME.length-1];
            HashMap<String, Object> prize = list.get(i);
            circularImageView.setImageDrawable(getDrawable((int)prize.get(IMGID)));
            anim_switch_row.setTarget(circularImageView);
            anim_switch_row.start();
            prize.put(REMAIM, (int) prize.get(REMAIM) - 1);
            list.set(i, prize);          // 直行更改到列表
            Toast.makeText(MainActivity.this, "(●´∀｀●) 恭喜获奖\n\n\n♥ " + (String) prize.get(NAME) + " ♥", Toast.LENGTH_SHORT).show();
            lastRowForUndo = i;
            mHideHandler.postDelayed(mClearImageRunnable,3500);
        }
    };

    private Runnable mClearImageRunnable = new Runnable() {

        @Override
        public void run() {
            circularImageView.setVisibility(View.INVISIBLE);
            mButtonRow.setVisibility(View.VISIBLE);
        }
    };

    private int makeARow(int[] remainLevel, Random random) {
        if (remainLevel.length > 0 && remainLevel[remainLevel.length - 1] > 0) {
            int result = random.nextInt(remainLevel[remainLevel.length - 1]) + 1;
            int i;
            for (i = 0; i < remainLevel.length; i++) {
                if (result <= remainLevel[i] & result != 0) break;
            }
            HashMap<String, Object> prize = list.get(i);
            if ((int) prize.get(REMAIM) < 1) {
                return makeARow(remainLevel, random);      // 以防万一算错了，没了就重新Row
//                Toast.makeText(this,((String)prize.get(NAME))+"用光了。\n",Toast.LENGTH_SHORT).show();
//                checkPrintRemain();
            }
            return i;
        } else {
            Toast.makeText(this, "o(╯□╰)o，\n   奖品都没了，收摊回家吧~\n", Toast.LENGTH_SHORT).show();
            return -1;
        }
    }

    private void realRow() {
        int[] remainLevel = getRemainLevelNum();
        if (remainLevel.length > 0 && remainLevel[remainLevel.length - 1] > 0) {
            if (rowing) {
                Toast.makeText(this, "o(╯□╰)o，\n   点nmb，正在抽呢~\n", Toast.LENGTH_SHORT).show();
            } else {
                anim_fade_away.setTarget(mButtonRow);
                anim_fade_away.start();
                mHideHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mButtonRow.setVisibility(View.INVISIBLE);
                    }
                },500);
                Toast.makeText(this, "(●´∀｀●)，\n   开始抽了哦~\n", Toast.LENGTH_SHORT).show();
                rowing = true;
                int[] remain = getRemainLevelNum();
                Random random = new Random(System.currentTimeMillis());
                for (int i = 0; i < ROW_DELAY_TIME.length; i++) {
                    int prz = makeARow(remain, random);
                    if (prz != -1) {
                        row_anim_sequence[i] = prz;
                    } else return;
                }
                countDownTimer.start();
                row_anim_timepassed = 0 ;
                circularImageView.setVisibility(View.VISIBLE);
            }
        }else {
            Toast.makeText(this, "o(╯□╰)o，\n   奖品都没了，收摊回家吧~\n", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("查询").setIcon(android.R.drawable.ic_menu_info_details).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
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
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                toggle();
            }
        }
        return true;
    }

}
