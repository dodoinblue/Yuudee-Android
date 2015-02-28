package android.pattern.widget;

import android.app.Activity;
import android.view.View;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class CroutonWrapper implements View.OnClickListener {

    Activity mContext = null;

    private static final Style INFINITE = new Style.Builder().setBackgroundColorValue(Style.holoRedLight).build();

    private static final Configuration CONFIGURATION_INFINITE = new Configuration.Builder().setDuration(
            Configuration.DURATION_INFINITE).build();

    private Crouton mInfiniteCrouton;

    public CroutonWrapper(Activity context) {
        mContext = context;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            default: {
                if (mInfiniteCrouton != null) {
                    Crouton.hide(mInfiniteCrouton);
                    mInfiniteCrouton = null;
                }
                break;
            }
        }
    }

    public void showInfoCrouton(String info) {
        Configuration croutonConfiguration = new Configuration.Builder().setDuration(Configuration.DURATION_SHORT)
                .build();
        showCrouton(info, Style.INFO, croutonConfiguration);

    }

    public void showAlertCrouton(String info) {
        Configuration croutonConfiguration = new Configuration.Builder().setDuration(Configuration.DURATION_SHORT)
                .build();
        showCrouton(info, Style.ALERT, croutonConfiguration);

    }

    public void showConfirmCrouton(String info) {
        Configuration croutonConfiguration = new Configuration.Builder().setDuration(Configuration.DURATION_SHORT)
                .build();
        showCrouton(info, Style.CONFIRM, croutonConfiguration);

    }

    public void showInfiniteCrouton(String info) {
        showCrouton(info, INFINITE, CONFIGURATION_INFINITE);

    }

    private void showCrouton(String croutonText, Style croutonStyle, Configuration configuration) {
        final boolean infinite = INFINITE == croutonStyle;
        final Crouton crouton = Crouton.makeText(mContext, croutonText, croutonStyle);
        if (infinite) {
            mInfiniteCrouton = crouton;
        }
        crouton.setOnClickListener(this).setConfiguration(configuration).show();
    }
}
