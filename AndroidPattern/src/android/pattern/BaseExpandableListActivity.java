/*
 * Copyright (c) 2015 著作权由郑志佳所有。著作权人保留一切权利。
 *
 * 这份授权条款，在使用者符合以下三条件的情形下，授予使用者使用及再散播本
 * 软件包装原始码及二进位可执行形式的权利，无论此包装是否经改作皆然：
 *
 * * 对于本软件源代码的再散播，必须保留上述的版权宣告、此三条件表列，以
 *   及下述的免责声明。
 * * 对于本套件二进位可执行形式的再散播，必须连带以文件以及／或者其他附
 *   于散播包装中的媒介方式，重制上述之版权宣告、此三条件表列，以及下述
 *   的免责声明。
 * * 未获事前取得书面许可，不得使用郑志佳或本软件贡献者之名称，
 *   来为本软件之衍生物做任何表示支持、认可或推广、促销之行为。
 *
 * 免责声明：本软件是由郑志佳及本软件之贡献者以现状（"as is"）提供，
 * 本软件包装不负任何明示或默示之担保责任，包括但不限于就适售性以及特定目
 * 的的适用性为默示性担保。郑志佳及本软件之贡献者，无论任何条件、
 * 无论成因或任何责任主义、无论此责任为因合约关系、无过失责任主义或因非违
 * 约之侵权（包括过失或其他原因等）而起，对于任何因使用本软件包装所产生的
 * 任何直接性、间接性、偶发性、特殊性、惩罚性或任何结果的损害（包括但不限
 * 于替代商品或劳务之购用、使用损失、资料损失、利益损失、业务中断等等），
 * 不负任何责任，即在该种使用已获事前告知可能会造成此类损害的情形下亦然。
 */

package android.pattern;

import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.SimpleExpandableListAdapter;

/**
 * Created by 郑志佳 on 1/23/15.
 * QQ: 34168035  Mail: statesman.zheng@gmail.com
 */
public class BaseExpandableListActivity extends BaseActivity {
    private ExpandableListView mListView;
    private SimpleExpandableListAdapter mAdapter;
    private int mExpandedGroupPosition;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_base_expandable_list);
    }

    @Override
    protected void initViews() {
        mListView = (ExpandableListView) findViewById(R.id.expandable_list);
        mListView.setOnGroupClickListener(new OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // We call collapseGroupWithAnimation(int) and
                // expandGroupWithAnimation(int) to animate group
                // expansion/collapse.
                if (mListView.isGroupExpanded(groupPosition)) {
                    mListView.collapseGroup(groupPosition);
                } else {
                    mListView.collapseGroup(mExpandedGroupPosition);
                    mListView.expandGroup(groupPosition);
                    mExpandedGroupPosition = groupPosition;
                }
                return true;
            }
        });
    }

    @Override
    protected void initEvents() {

    }

    protected void initExpandableList(List<Map<String, String>> groups, List<List<Map<String, String>>> childs) {
        mAdapter = new SimpleExpandableListAdapter(this, groups,
                        R.layout.expandable_list_groups, new String[] {
                        "group"
        }, new int[] {
                        R.id.group_content
        }, childs, R.layout.expandable_list_childs, new String[] {
                        "child"
        }, new int[] {
                        R.id.child_content
        });
        mListView.setAdapter(mAdapter);
        //        mListView.expandGroup(0, true);
    }
}