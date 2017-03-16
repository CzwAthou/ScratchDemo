package com.athou.scratchdemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    private ScratchView scratchView;

    private TextView tv_percent;
    private RadioGroup rg_color;
    private RadioGroup rg_mark;

    private SeekBar sbEraserSize;

    String mPercentFormatStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scratchView = (ScratchView) findViewById(R.id.scratch_view);
        scratchView.setOnPercentChangeListener(new ScratchView.OnPercentChangeListener() {
            @Override
            public void onChange(float percent) {
                tv_percent.setText(String.format(mPercentFormatStr, percent));
            }
        });

        tv_percent = (TextView) findViewById(R.id.tv_percent);
        mPercentFormatStr = getString(R.string.scratch_percent);

        rg_color = (RadioGroup) findViewById(R.id.rg_color);
        rg_mark = (RadioGroup) findViewById(R.id.rg_watermark);
        rg_color.setOnCheckedChangeListener(this);
        rg_mark.setOnCheckedChangeListener(this);

        sbEraserSize = (SeekBar) findViewById(R.id.sb_erase_size);
        sbEraserSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    scratchView.setEraserSize(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        findViewById(R.id.btn_clear).setOnClickListener(this);
        findViewById(R.id.btn_reset).setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        if (radioGroup == rg_color) {
            switch (i) {
                case R.id.rb_red:
                    scratchView.setMaskColor(Color.RED);
                    break;
                case R.id.rb_green:
                    scratchView.setMaskColor(Color.GREEN);
                    break;
                case R.id.rb_blue:
                    scratchView.setMaskColor(Color.BLUE);
                    break;
                case R.id.rb_origin:
                    scratchView.setMaskColor(ScratchView.DEFAULT_MASK_COLOR);
                    break;
            }
        } else if (radioGroup == rg_mark) {
            switch (i) {
                case R.id.rb_wechat:
                    scratchView.setWaterMark(R.mipmap.wechat);
                    break;
                case R.id.rb_no:
                    scratchView.setWaterMark(-1);
                    break;
            }
        }
        scratchView.reset();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_clear:
                scratchView.clear();
                break;
            case R.id.btn_reset:
                scratchView.reset();
                break;
        }
    }
}
