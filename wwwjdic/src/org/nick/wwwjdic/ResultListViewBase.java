package org.nick.wwwjdic;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

public abstract class ResultListViewBase extends ListActivity implements
        ResultListView {

    private static final String DEFAULT_WWWJDIC_URL = "http://www.csse.monash.edu.au/~jwb/cgi-bin/wwwjdic.cgi";

    private static final String PREF_WWWJDIC_URL_KEY = "pref_wwwjdic_mirror_url";
    private static final String PREF_WWWJDIC_TIMEOUT_KEY = "pref_wwwjdic_timeout";

    protected SearchCriteria criteria;

    protected Handler guiThread;
    protected Future transPending;

    protected ProgressDialog progressDialog;
    protected String progressDialogMessage;

    protected ResultListViewBase() {
        initThreading();
    }

    @Override
    protected void onPause() {
        WwwjdicApplication app = getApp();
        if (progressDialog != null && progressDialog.isShowing()) {
            app.setProgressDialogMessage(progressDialogMessage);
            progressDialog.dismiss();
            progressDialog = null;
        }

        app.getTranslateTask().setResultListView(null);

        // transThread.shutdownNow();
        super.onPause();
    }

    @Override
    protected void onResume() {
        WwwjdicApplication app = getApp();
        progressDialogMessage = app.getProgressDialogMessage();
        if (progressDialogMessage != null) {
            app.setProgressDialogMessage(null);
            progressDialog = ProgressDialog.show(this, "",
                    progressDialogMessage, true);
        }

        if (app.getTranslateTask() != null) {
            app.getTranslateTask().setResultListView(this);
        }

        super.onResume();
    }

    private void initThreading() {
        guiThread = new Handler();
    }

    protected void submitTranslateTask(TranslateTask translateTask) {
        progressDialogMessage = getResources().getText(R.string.loading)
                .toString();
        progressDialog = ProgressDialog.show(this, "", progressDialogMessage,
                true);

        ExecutorService executorService = getApp().getExecutorService();
        transPending = executorService.submit(translateTask);
        WwwjdicApplication app = getApp();
        app.setTranslateTask(translateTask);
    }

    private WwwjdicApplication getApp() {
        WwwjdicApplication app = (WwwjdicApplication) getApplication();
        return app;
    }

    public void setError(final Exception ex) {
        guiThread.post(new Runnable() {
            public void run() {
                setTitle(getResources().getText(R.string.error));
                dismissProgressDialog();

                AlertDialog.Builder alert = new AlertDialog.Builder(
                        ResultListViewBase.this);

                alert.setTitle(R.string.error);

                if (ex instanceof SocketTimeoutException
                        || ex.getCause() instanceof SocketTimeoutException) {
                    alert.setMessage(getResources().getString(
                            R.string.timeout_error_message));
                } else if (ex instanceof SocketException
                        || ex.getCause() instanceof SocketException) {
                    alert.setMessage(getResources().getString(
                            R.string.socket_error_message));
                } else {
                    alert.setMessage(getResources().getString(
                            R.string.generic_error_message)
                            + "(" + ex.getMessage() + ")");
                }

                alert.setPositiveButton(getResources().getText(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                dialog.dismiss();
                                finish();
                            }
                        });

                alert.show();
            }
        });
    }

    public abstract void setResult(List<?> result);

    protected void extractSearchCriteria() {
        criteria = (SearchCriteria) getIntent().getSerializableExtra(
                Constants.CRITERIA_KEY);
    }

    protected String getWwwjdicUrl() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        return preferences.getString(PREF_WWWJDIC_URL_KEY, DEFAULT_WWWJDIC_URL);
    }

    protected int getHttpTimeoutSeconds() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        String timeoutStr = preferences.getString(PREF_WWWJDIC_TIMEOUT_KEY,
                "10");

        return Integer.parseInt(timeoutStr);
    }

    protected void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
            progressDialogMessage = null;
        }
    }
}
