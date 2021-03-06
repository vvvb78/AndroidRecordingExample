package com.echo.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dd.processbutton.FlatButton;
import com.echo.R;
import com.echo.common.BackPressCloseHandler;
import com.echo.interfaces.MainPresenter;
import com.echo.view.presenter.MainPresenterImpl;
import com.gc.materialdesign.views.ProgressBarDeterminate;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements MainPresenter.View, View.OnClickListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.record_button)
    ImageView recordButton;
    @BindView(R.id.play_button)
    ImageView playButton;
    @BindView(R.id.record_progressbar)
    ProgressBarDeterminate progressBar;
    @BindView(R.id.record_progress_textview)
    TextView progressText;
    @BindView(R.id.send_button)
    FlatButton sendButton;
    @BindView(R.id.view_flipper)
    ViewFlipper viewFlipper;
    BackPressCloseHandler backPressCloseHandler;

    MainPresenterImpl presenter;
    boolean isPlayButtonExpanded;
    boolean isProgressVisible;
    boolean isRecording;
    boolean isPlaying;
    int pageToggle;
    MainDrawer drawer;

    @Override
    public void onBackPressed() {
        if (!viewReturnFirst()) {
            backPressCloseHandler.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);
        ButterKnife.bind(this);
        backPressCloseHandler = new BackPressCloseHandler(this);
        pageToggle = 1;
        setSupportActionBar(toolbar);

        presenter = new MainPresenterImpl();
        presenter.setView(this);
        initView();
    }


    @Override
    public void initView() {
        Animation flipperIn = AnimationUtils.loadAnimation(this, R.anim.main_view_flipper_in);
        Animation flipperOut = AnimationUtils.loadAnimation(this, R.anim.main_view_flipper_out);

        isPlayButtonExpanded = false;
        isProgressVisible = false;
        drawer = new MainDrawer();
        drawer.setView(this, toolbar);

        viewFlipper.setInAnimation(flipperIn);
        viewFlipper.setOutAnimation(flipperOut);
    }

    /**
     * 1. 초기상태의 경우 = Progress Expand
     * 2. 재녹음의 경우 = playButton Collapse
     */
    @Override
    public void onRecordStarted() {
        isRecording = true;

        //Progress 텍스트가 없다면 연다.
        if (!isProgressVisible)
            progressLayoutAnimated(true);

        //플레이버튼이 열려있다면 닫는다.
        if (isPlayButtonExpanded)
            playLayoutAnimated(false);

        Glide.with(this)
                .load(R.drawable.stop_64px_blue400)
                .dontAnimate()
                .placeholder(recordButton.getDrawable())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(recordButton);
    }

    @Override
    public void onRecordStopped() {
        isRecording = false;

        //Progress 텍스트가 있다면 없앤다
        if (isProgressVisible)
            progressLayoutAnimated(false);

        //플레이버튼이 없다면 연다
        if (!isPlayButtonExpanded)
            playLayoutAnimated(true);

        Glide.with(this)
                .load(R.drawable.recording_64px_red400)
                .dontAnimate()
                .placeholder(recordButton.getDrawable())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(recordButton);
    }

    @Override
    public void onProgressUpdated(final int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progress);
                progressText.setText(progress + " / 10 초");
            }
        });
    }

    @Override
    public void onPlayStarted() {
        //stop_64px_blue400
        Glide.with(this)
                .load(R.drawable.stop_64px_blue400)
                .placeholder(playButton.getDrawable())
                .dontAnimate()
                .into(playButton);
    }

    @Override
    public void onPlayStopped() {
        //stop_64px_blue400
        Glide.with(this)
                .load(R.drawable.stop_64px_blue400)
                .placeholder(playButton.getDrawable())
                .dontAnimate()
                .into(playButton);
    }

    @Override
    public void onSendButtonClicked() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        /*
         * ButterKnife 가 MainActivity가 아닌 Menu 안의 Layout의 View를 찾지 못해서 순수 등록
         */
        RelativeLayout menuLayout = (RelativeLayout) menu.findItem(R.id.menu_switch_item).getActionView();
        final ImageView toggleImage = (ImageView) menuLayout.findViewById(R.id.toggle_menu_image);
        final RelativeLayout toggleLayout = (RelativeLayout) menuLayout.findViewById(R.id.menu_layout);
        toggleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pageToggle == 1){
                    onPageFlipped(true);
                    Glide.with(MainActivity.this)
                            .load(R.drawable.list_plus_24px)
                            .placeholder(toggleImage.getDrawable())
                            .dontAnimate()
                            .into(toggleImage);
                    pageToggle = 0;
                }
                else{
                    onPageFlipped(false);
                    Glide.with(MainActivity.this)
                            .load(R.drawable.message_plus_24px)
                            .placeholder(toggleImage.getDrawable())
                            .dontAnimate()
                            .into(toggleImage);
                    pageToggle = 1;
                }
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onPageFlipped(boolean toMessageCheckLayout) {
        viewReturnFirst();
        //switch가 오른쪽(체크상태)일때. 뷰플리퍼가 메세지체크페이지를 가리킨다.
        if (toMessageCheckLayout) {
            //녹음중이라면 정지시킨다. + 데이터초기화
            viewFlipper.setDisplayedChild(1);
        }
        //switch가 왼쪽(비체크상태)일때, 뷰플리퍼가 녹음페이지를 가리킨다.
        else {
            viewFlipper.setDisplayedChild(0);
        }
    }

    @OnClick({R.id.record_button, R.id.play_button, R.id.send_button, R.id.send_homo_sexual_button, R.id.send_near_location, R.id.send_near_years})
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.record_button:
                if (!isRecording)
                    presenter.onRecordStarted();
                else
                    presenter.onRecordStopped();
                break;
            case R.id.play_button:
                if (!isPlaying)
                    presenter.onPlayStarted();
                else
                    presenter.onPlayStopped();
                break;
            case R.id.send_homo_sexual_button:
            case R.id.send_near_location:
            case R.id.send_near_years:
                toggleSendButton(findViewById(view.getId()));
        }
    }

    /*******************************************************************************************
     * 이 아래로는 private 함수.
     *******************************************************************************************/

    /**
     * 애니메이션을 주면서 record_layout 에 playButton visibility를 toggle한다.
     *
     * @param expanding true = playButton 출현
     *                  false = playButton 숨김
     */
    private void playLayoutAnimated(final boolean expanding) {
        final Animation playButtonShow = AnimationUtils.loadAnimation(this, R.anim.main_play_button_show);
        final Animation playButtonHide = AnimationUtils.loadAnimation(this, R.anim.main_play_button_hide);
        final Animation recordButtonShow = AnimationUtils.loadAnimation(this, R.anim.main_record_button_show);
        final Animation recordButtonHide = AnimationUtils.loadAnimation(this, R.anim.main_record_button_hide);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (expanding && playButton.getVisibility() == View.GONE) {
                    isPlayButtonExpanded = !isPlayButtonExpanded;
            /*
            1. 왼쪽으로 서서히 생기는 플레이버튼 애니메이션
            1. 오른쪽으로 이동하는 녹음버튼 애니메이션
            2. 플레이버튼 visible
             */
                    playButton.startAnimation(playButtonShow);
                    playButton.setVisibility(View.VISIBLE);

                    recordButton.startAnimation(recordButtonShow);
                } else if (!expanding && playButton.getVisibility() == View.VISIBLE) {
                    isPlayButtonExpanded = !isPlayButtonExpanded;
            /*
            1. 오른쪽으로 서서히 사라지는 플레이버튼 애니메이션
            1. 왼쪽으로 이동하는 녹음버튼 애니메이션
            3. 플레이버튼 gone
             */
                    playButton.startAnimation(playButtonHide);
                    playButton.setVisibility(View.GONE);

                    recordButton.startAnimation(recordButtonHide);
                }
            }
        });
    }

    /**
     * ProgressText 를 출현시킨다.
     *
     * @param visible true = ProgressText 보임
     *                false = ProgressText 가림
     */
    private void progressLayoutAnimated(final boolean visible) {
        final Animation progressTextShow = AnimationUtils.loadAnimation(this, R.anim.main_progress_text_show);
        Animation progressTextHide = AnimationUtils.loadAnimation(this, R.anim.main_progress_text_hide);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (visible) {

                    isProgressVisible = !isProgressVisible;

                    progressText.setVisibility(View.INVISIBLE);
                    progressText.startAnimation(progressTextShow);
                    progressText.setVisibility(View.VISIBLE);
                } else {
                    isProgressVisible = !isProgressVisible;

            /*progressText.setVisibility(View.INVISIBLE);
            progressText.startAnimation(progressTextHide);*/
                    progressText.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    /**
     * 초기상태로 되돌린다.
     *
     * @return true = 초기상태가 아니였고, 초기상태로 되돌아감
     * false = 함수 호출전부터 초기상태였음
     */
    private boolean viewReturnFirst() {
        if (isRecording) {
            //Recording 중에는 정지버튼 하나만 있다. 내부쓰레드 중지 후 이모습만 바꾼다.
            isRecording = !isRecording;

            progressLayoutAnimated(false);
            Glide.with(this)
                    .load(R.drawable.recording_64px_red400)
                    .dontAnimate()
                    .placeholder(recordButton.getDrawable())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(recordButton);
            return true;
        } else if (isPlayButtonExpanded) {
            playLayoutAnimated(false);
            return true;
        } else {
            return false;
        }
    }

    private void toggleSendButton(View button) {
        if (button.getTag().equals("unclicked")) {
            button.setTag("clicked");
            button.setBackgroundResource(R.color.colorPrimaryDark);
        } else {
            button.setTag("unclicked");
            button.setBackgroundResource(R.color.colorAccent);
        }
    }

    private void initProgress(){

    }
}

