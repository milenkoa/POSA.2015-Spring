package vandy.mooc.presenter;

import android.util.Log;

import vandy.mooc.common.GenericAsyncTask;
import vandy.mooc.common.GenericAsyncTaskOps;

/**
 * This class implements Command Processor pattern; all information about function is
 * stored in the class and execution is performed in worker thread using AsyncTask
 */

public class RateVideoCommand implements GenericAsyncTaskOps<Void, Void, Integer> {

    private final String TAG = getClass().getSimpleName();

    private long m_id;
    private int m_rating;
    private ShowVideoOps m_Ops;
    private GenericAsyncTask<Void, Void, Integer, RateVideoCommand> m_AsyncTask;


    public RateVideoCommand(ShowVideoOps ops, long id, int rating) {
        m_Ops = ops;
        m_id = id;
        m_rating = rating;
        m_AsyncTask = new GenericAsyncTask<>(this);
    }

    public void run() {
        m_AsyncTask.execute((Void) null);
    }

    @Override
    public Integer doInBackground(Void... params) {
        Log.d(TAG, "doInBackGround() - ID: " + m_id + ", RATING: " + m_rating);
        int result = m_Ops.getVideoController().rateVideo(m_id, m_rating);
        return result;

    }

    @Override
    public void onPostExecute(Integer result) {
        Log.d(TAG, "onPostExecute() - result: " + result);
        m_Ops.updateRating(result);

    }
}
