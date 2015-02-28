package children.com.littlewalter;

import android.os.Bundle;

/**
 * Created by peter on 15/2/28.
 */
public class MainActivity extends BaseLittleWalterActivity {
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
    }

    @Override
    protected void initEvents() {

    }
}
