package org.coscup.ccip.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.internal.bind.util.ISO8601Utils;

import org.coscup.ccip.R;
import org.coscup.ccip.adapter.SpeakerImageAdapter;
import org.coscup.ccip.model.Speaker;
import org.coscup.ccip.model.Submission;
import org.coscup.ccip.util.AlarmUtil;
import org.coscup.ccip.util.JsonUtil;
import org.coscup.ccip.util.PreferenceUtil;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SubmissionDetailActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_PROGRAM = "program";
    private static final SimpleDateFormat SDF_DATETIME = new SimpleDateFormat("MM/dd HH:mm");
    private static final SimpleDateFormat SDF_TIME = new SimpleDateFormat("HH:mm");

    private Activity mActivity;
    private boolean isStar = false;
    private Submission submission;
    private FloatingActionButton fab;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private TextView speakerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_detail);

        mActivity = this;
        ViewPager speakerViewPager = (ViewPager) findViewById(R.id.viewPager_speaker);

        submission = JsonUtil.fromJson(getIntent().getStringExtra(INTENT_EXTRA_PROGRAM), Submission.class);
        isStar = PreferenceUtil.loadStars(this).contains(submission);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(submission.getSpeaker().getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final List<Speaker> speakers = new ArrayList<Speaker>();

        SpeakerImageAdapter adapter = new SpeakerImageAdapter(this.getSupportFragmentManager(), speakers);
        speakerViewPager.setAdapter(adapter);
        speakerViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                speakerInfo.setText(speakers.get(position).getBio());
                collapsingToolbarLayout.setTitle(speakers.get(position).getName());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        TextView room, subject, time, type, community, slide, slido, lang, programAbstract;
        View spekaerInfoBlock;
        room = (TextView) findViewById(R.id.room);
        subject = (TextView) findViewById(R.id.subject);
        time = (TextView) findViewById(R.id.time);
        type = (TextView) findViewById(R.id.type);
        community = (TextView) findViewById(R.id.community);
        slide = (TextView) findViewById(R.id.slide);
        slido = (TextView) findViewById(R.id.slido);
        lang = (TextView) findViewById(R.id.lang);
        spekaerInfoBlock = findViewById(R.id.speaker_info_block);
        speakerInfo = (TextView) findViewById(R.id.speakerinfo);
        programAbstract = (TextView) findViewById(R.id.program_abstract);

        room.setText(submission.getRoom());
        subject.setText(submission.getSubject());
        subject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copy_to_clipboard((TextView) view);
            }
        });

        try {
            StringBuffer timeString = new StringBuffer();
            Date startDate = ISO8601Utils.parse(submission.getStart(), new ParsePosition(0));
            timeString.append(SDF_DATETIME.format(startDate));
            timeString.append(" ~ ");
            Date endDate = ISO8601Utils.parse(submission.getEnd(), new ParsePosition(0));
            timeString.append(SDF_TIME.format(endDate));

            timeString.append(", " + ((endDate.getTime() - startDate.getTime()) / 1000 / 60) + getResources().getString(R.string.min));

            time.setText(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            type.setText(Submission.getTypeString(submission.getType()));
        } catch (Resources.NotFoundException e) {
            type.setText("");
        }

        if (!TextUtils.isEmpty(submission.getCommunity())) {
            community.setText(submission.getCommunity());
        } else {
            findViewById(R.id.community_layout).setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(submission.getSlide())) {
            findViewById(R.id.slide_layout).setVisibility(View.VISIBLE);
            slide.setPaintFlags(slide.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            slide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(submission.getSlide())));
                }
            });
        }

        if (!TextUtils.isEmpty(submission.getSlido())) {
            findViewById(R.id.slido_layout).setVisibility(View.VISIBLE);
            slido.setText(submission.getSlido());
            slido.setPaintFlags(slido.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            slido.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.sli.do/" + submission.getSlido())));
                }
            });
        }

        if (submission.getSpeaker().getName().isEmpty()) spekaerInfoBlock.setVisibility(View.GONE);

        speakerInfo.setText(submission.getSpeaker().getBio());
        speakerInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copy_to_clipboard((TextView) view);
            }
        });
        programAbstract.setText(submission.getSummary());
        programAbstract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copy_to_clipboard((TextView) view);
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        checkFabIcon();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFab(view);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkFabIcon() {
        if (isStar) {
            fab.setImageResource(R.drawable.ic_bookmark_black_24dp);
        } else {
            fab.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
        }
        fab.getDrawable().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
    }

    private void toggleFab(View view) {
        isStar = !isStar;
        updateStarSubmissions(view);
        checkFabIcon();
    }

    private void updateStarSubmissions(View view) {
        List<Submission> submissions = PreferenceUtil.loadStars(this);
        if (submissions != null) {
            if (submissions.contains(submission)) {
                submissions.remove(submission);
                AlarmUtil.cancelSubmissionAlarm(this, submission);
                Snackbar.make(view, R.string.remove_bookmark, Snackbar.LENGTH_LONG).show();
            } else {
                submissions.add(submission);
                AlarmUtil.setSubmissionAlarm(this, submission);
                Snackbar.make(view, R.string.add_bookmark, Snackbar.LENGTH_LONG).show();
            }
        } else {
            submissions = Collections.singletonList(submission);
        }
        PreferenceUtil.saveStars(this, submissions);
    }

    private void copy_to_clipboard(TextView textView) {
        ClipboardManager cManager = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData cData = ClipData.newPlainText("text", textView.getText());
        cManager.setPrimaryClip(cData);
        Toast.makeText(mActivity, R.string.copy_to_clipboard, Toast.LENGTH_SHORT).show();
    }
}
