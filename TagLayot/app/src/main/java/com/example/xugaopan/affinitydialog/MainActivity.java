package com.example.xugaopan.affinitydialog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.xugaopan.affinitydialog.widget.TagContainerLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TAG_LAYOUT";
    TagContainerLayout tagContainerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tagContainerLayout = (TagContainerLayout) findViewById(R.id.new_container);
        final List<String> tags = new ArrayList<>();
        tags.add("钢管舞");
        tags.add("拉丁舞");
        tags.add("爵士舞");
        tags.add("吃香蕉");
        tags.add("唱情歌");
        tags.add("玩泡泡");
        tags.add("锁骨夹硬币");
        tags.add("倒挂金钩1小时");

        tagContainerLayout.setTags(tags);

        tagContainerLayout.setOnTagListener(new TagContainerLayout.TagListener() {
            @Override
            public void onTagClick(int position) {
                Log.d(TAG, "position:" +position+ tagContainerLayout.isSelcet(position));
                if (tagContainerLayout.isSelcet(position)) {
                    tagContainerLayout.unSelect(position);
                } else {
                    tagContainerLayout.setSelect(position);
                }
            }

            @Override
            public void removeTag(int position) {
                tagContainerLayout.removeTag(position);
            }
        });

        findViewById(R.id.select_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagContainerLayout.selectAll(true);
            }
        });
        findViewById(R.id.un_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagContainerLayout.selectAll(false);
            }
        });
        findViewById(R.id.add_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagContainerLayout.addTag("add tag");
            }
        });
        findViewById(R.id.add_index).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagContainerLayout.addTag(3, "three tag");
            }
        });
        findViewById(R.id.remove_index).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagContainerLayout.removeTag(3);
            }
        });
        findViewById(R.id.show_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagContainerLayout.setEnableCross(true);
            }
        });

        findViewById(R.id.hide_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagContainerLayout.setEnableCross(false);
            }
        });


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        tagContainerLayout.removeAllTags();
    }
}
