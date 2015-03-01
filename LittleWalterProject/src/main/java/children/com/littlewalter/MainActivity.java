package children.com.littlewalter;

import android.os.Bundle;
import android.widget.GridView;

import java.util.ArrayList;

/**
 * Created by peter on 15/2/28.
 */
public class MainActivity extends BaseLittleWalterActivity {

    private ArrayList<LittleWalter> mWalterList = new ArrayList<LittleWalter>();
    private GridView mGridView;
    private LittleWalterAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addContentView(R.layout.activity_main, true);
        initViews();
        initEvents();
    }

    @Override
    protected void initViews() {
        mTopView.setLeftEnabled(false);
        mGridView = (GridView)findViewById(R.id.walter_gridview);
        mAdapter = new LittleWalterAdapter(this, mWalterList);
        mGridView.setAdapter(mAdapter);
    }

    @Override
    protected void initEvents() {

    }


}
