package oscar.riksdagskollen.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import oscar.riksdagskollen.Activity.RepresentativeDetailActivity;
import oscar.riksdagskollen.Manager.RepresentativeManager;
import oscar.riksdagskollen.RiksdagskollenApp;
import oscar.riksdagskollen.Util.Adapter.RepresentativeAdapter;
import oscar.riksdagskollen.Util.Adapter.RiksdagenViewHolderAdapter;
import oscar.riksdagskollen.Util.JSONModel.Party;
import oscar.riksdagskollen.Util.JSONModel.RepresentativeModels.Representative;


/**
 * Created by oscar on 2018-03-29.
 */

public class PartyRepresentativeFragment extends RiksdagenAutoLoadingListFragment implements RepresentativeManager.RepresentativeDownloadListener {

    private final List<Representative> representativeList = new ArrayList<>();
    private RepresentativeAdapter adapter;
    private Party party;

    public static PartyRepresentativeFragment newInstance(Party party) {
        Bundle args = new Bundle();
        args.putParcelable("party", party);
        PartyRepresentativeFragment newInstance = new PartyRepresentativeFragment();
        newInstance.setArguments(args);
        return newInstance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.party = getArguments().getParcelable("party");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final RiksdagskollenApp app = RiksdagskollenApp.getInstance();
        if (app.getRepresentativeManager().isRepresentativesDownloaded()) {
            setShowLoadingView(false);
            ArrayList<Representative> representatives = app.getRepresentativeManager().getCurrentRepresentativesForParty(party.getID());
            representativeList.addAll(representatives);
            getAdapter().addAll(representatives);
            setLoadingMoreItems(false);
        } else {
            app.scheduleDownloadRepresentativesJobIfNotRunning();
            app.getRepresentativeManager().addDownloadListener(this);
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new RepresentativeAdapter(representativeList, RepresentativeAdapter.NAME_COMPARATOR, this, new RiksdagenViewHolderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object document) {
                Intent repDetailsIntent = new Intent(getContext(), RepresentativeDetailActivity.class);
                repDetailsIntent.putExtra("representative", (Representative) document);
                startActivity(repDetailsIntent);
            }
        });

    }

    /**
     * Not used for this fragment
     */
    protected void loadNextPage() {
    }

    @Override
    protected void clearItems() {
    }

    @Override
    RiksdagenViewHolderAdapter getAdapter() {
        return adapter;
    }

    @Override
    protected void refresh() {
        showNoConnectionWarning(false);
        if (adapter.getItemCount() == 0 && !RiksdagskollenApp.getInstance().isDownloadRepsRunningOrScheduled()) {
            RiksdagskollenApp.getInstance().scheduleDownloadRepresentativesJobIfNotRunning();
        }
        if (!RiksdagskollenApp.getInstance().isDownloadRepsRunningOrScheduled()) {
            setLoadingMoreItems(false);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        RiksdagskollenApp.getInstance().getRepresentativeManager().removeDownloadListener(this);
    }

    @Override
    public void onRepresentativesDownloaded(ArrayList<Representative> representatives) {
        setShowLoadingView(false);
        ArrayList<Representative> partyReps = RiksdagskollenApp.getInstance().getRepresentativeManager().getCurrentRepresentativesForParty(party.getID());
        representativeList.addAll(partyReps);
        getAdapter().addAll(partyReps);
        setLoadingMoreItems(false);
    }

    @Override
    public void onFail() {
        onLoadFail();
    }
}
