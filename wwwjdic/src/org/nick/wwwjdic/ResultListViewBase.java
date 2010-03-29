package org.nick.wwwjdic;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;

public abstract class ResultListViewBase extends ListActivity implements
        ResultListView {

    protected SearchCriteria criteria;

    protected Handler guiThread;
    protected ExecutorService transThread;
    protected Future transPending;

    protected ProgressDialog progressDialog;

    protected ResultListViewBase() {
        initThreading();
    }

    @Override
    protected void onDestroy() {
        transThread.shutdownNow();
        super.onDestroy();
    }

    private void initThreading() {
        guiThread = new Handler();
        transThread = Executors.newSingleThreadExecutor();
    }

    protected void submitTranslateTask(TranslateTask translateTask) {
        progressDialog = ProgressDialog.show(this, "", getResources().getText(
                R.string.loading), true);
        transPending = transThread.submit(translateTask);
    }

    public void setError(final Exception ex) {
        guiThread.post(new Runnable() {
            public void run() {
                setTitle(getResources().getText(R.string.error));
                progressDialog.dismiss();

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

}
