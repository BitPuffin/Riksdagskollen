package oscar.riksdagskollen.Util.Job;

import androidx.annotation.NonNull;

import com.android.volley.VolleyError;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import oscar.riksdagskollen.RepresentativeList.data.Representative;
import oscar.riksdagskollen.RiksdagskollenApp;
import oscar.riksdagskollen.Util.RiksdagenCallback.RepresentativeListCallback;

/**
 * Created by gustavaaro on 2018-09-27.
 */

public class DownloadAllRepresentativesJob extends Job {

    public static final String TAG = "job_download_representatives";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        final CountDownLatch latch = new CountDownLatch(1);
        if (RiksdagskollenApp.getInstance().getRepresentativeManager().isRepresentativesDownloaded())
            cancel();

        RiksdagskollenApp.getInstance().getRiksdagenAPIManager().getAllCurrentRepresentatives(new RepresentativeListCallback() {
            @Override
            public void onPersonListFetched(List<Representative> representatives) {
                RiksdagskollenApp.getInstance().getRepresentativeManager().addCurrentRepresentatives(representatives);
                latch.countDown();
            }

            @Override
            public void onFail(VolleyError error) {
                RiksdagskollenApp.getInstance().getRepresentativeManager().notifyError();
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }

        return Result.SUCCESS;
    }


    public static int scheduleJob() {
        return new JobRequest.Builder(DownloadAllRepresentativesJob.TAG)
                .startNow()
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }
}
