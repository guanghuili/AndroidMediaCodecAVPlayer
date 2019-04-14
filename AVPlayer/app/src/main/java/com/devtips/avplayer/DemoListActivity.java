package com.devtips.avplayer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

/**
 * @PACKAGE_NAME: com.devtips.avplayer
 * @Package: com.devtips.avplayer
 * @ClassName: DemoListActivity
 * @Author: ligh
 * @CreateDate: 2019/3/18 3:56 PM
 * @Version: 1.0
 * @Description:
 */
public class DemoListActivity extends Activity {

    QMUITopBarLayout mTopBar;
    QMUIGroupListView mGroupListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_list);
        initTopBar();

        initGroupListView();

    }
    private void initTopBar() {
        mTopBar = findViewById(R.id.topbar);
        mTopBar.setBackgroundResource(com.qmuiteam.qmui.R.color.qmui_config_color_blue);
        TextView textView = mTopBar.setTitle("AVPlayer");
        textView.setTextColor(Color.WHITE);
    }

    private void initGroupListView() {
        mGroupListView = findViewById(R.id.groupListView);
        QMUICommonListItemView normalItem = mGroupListView.createItemView(
null,
                "MediaExtractor 示例",
                null,
                QMUICommonListItemView.HORIZONTAL,
                QMUICommonListItemView.ACCESSORY_TYPE_NONE);
        normalItem.setOrientation(QMUICommonListItemView.VERTICAL);
        normalItem.setTag(0);


        QMUICommonListItemView itemWithDetail = mGroupListView.createItemView(
                null,
                "MediaCodec 示例",
                null,
                QMUICommonListItemView.HORIZONTAL,
                QMUICommonListItemView.ACCESSORY_TYPE_NONE);
        itemWithDetail.setTag(1);


        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch ((int)v.getTag()) {
                    case 0:{
                        Intent intent = new Intent(DemoListActivity.this,DemoMediaExtractorActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case 1:{
                        Intent intent = new Intent(DemoListActivity.this,DemoMediaCodecActivity.class);
                        startActivity(intent);
                        break;
                    }
                }
            }
        };
        int size = QMUIDisplayHelper.dp2px(this, 20);
        QMUIGroupListView.newSection(this)
                .setTitle("示例")
                .setLeftIconSize(size, ViewGroup.LayoutParams.WRAP_CONTENT)
                .addItemView(normalItem, onClickListener)
                .addItemView(itemWithDetail, onClickListener)
                .addTo(mGroupListView);

    }
}
