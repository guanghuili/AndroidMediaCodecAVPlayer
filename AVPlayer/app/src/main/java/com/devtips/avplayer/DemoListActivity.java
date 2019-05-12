package com.devtips.avplayer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.devtips.avplayer.opengl.DemoGLTextureActivity;
import com.devtips.avplayer.opengl.DemoGLTriangleActivity;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.util.HashMap;

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


        QMUICommonListItemView glTriangleItemWithDetail = mGroupListView.createItemView(
                null,
                "OpenGL 绘制三角形示例",
                null,
                QMUICommonListItemView.HORIZONTAL,
                QMUICommonListItemView.ACCESSORY_TYPE_NONE);
        glTriangleItemWithDetail.setTag(2);

        QMUICommonListItemView glTextureItemWithDetail = mGroupListView.createItemView(
                null,
                "OpenGL 绘制纹理示例",
                null,
                QMUICommonListItemView.HORIZONTAL,
                QMUICommonListItemView.ACCESSORY_TYPE_NONE);
        glTextureItemWithDetail.setTag(3);


        final HashMap<Integer,Class> mDemoMap = new HashMap(){

            {
                put(0,DemoMediaExtractorActivity.class);
                put(1,DemoMediaCodecActivity.class);
                put(2,DemoGLTriangleActivity.class);
                put(3,DemoGLTextureActivity.class);
            }

        };


        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int tag = (int) v.getTag();
                Class clazz = mDemoMap.get(tag);
                Intent intent = new Intent(DemoListActivity.this,clazz);
                startActivity(intent);
            }
        };
        int size = QMUIDisplayHelper.dp2px(this, 20);
        QMUIGroupListView.newSection(this)
                .setTitle("示例")
                .setLeftIconSize(size, ViewGroup.LayoutParams.WRAP_CONTENT)
                .addItemView(normalItem, onClickListener)
                .addItemView(itemWithDetail, onClickListener)
                .addItemView(glTriangleItemWithDetail, onClickListener)
                .addItemView(glTextureItemWithDetail, onClickListener)
                .addTo(mGroupListView);

    }
}
