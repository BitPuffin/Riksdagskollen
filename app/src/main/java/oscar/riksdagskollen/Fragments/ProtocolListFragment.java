package oscar.riksdagskollen.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;

import oscar.riksdagskollen.Activities.ProtocolReaderActivity;
import oscar.riksdagskollen.R;
import oscar.riksdagskollen.RikdagskollenApp;
import oscar.riksdagskollen.Utilities.Callbacks.ProtocolCallback;
import oscar.riksdagskollen.Utilities.JSONModels.Protocol;
import oscar.riksdagskollen.Utilities.Adapters.ProtocolAdapter;
import oscar.riksdagskollen.Utilities.Adapters.RiksdagenViewHolderAdapter;


/**
 * Created by oscar on 2018-03-29.
 */

public class ProtocolListFragment extends RiksdagenAutoLoadingListFragment {

    private List<Protocol> protocolList = new ArrayList<>();
    private ProtocolAdapter adapter;

    public static ProtocolListFragment newInstance(){
        ProtocolListFragment newInstance = new ProtocolListFragment();
        return newInstance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.prot);
        adapter = new ProtocolAdapter(protocolList, new RiksdagenViewHolderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object document) {
                /*Protocol protocol = (Protocol) document;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + protocol.getDokument_url_html().substring(2)));
                startActivity(browserIntent);*/

                Intent intent = new Intent(getContext(), ProtocolReaderActivity.class);
                intent.putExtra("url", ((Protocol) document).getDokument_url_html());
                intent.putExtra("title",((Protocol) document).getTitel());
                startActivity(intent);
            }
        });

    }

    /**
     * Load the next page and add it to the adapter when downloaded and parsed.
     * Hides the loading view.s
     */
    protected void loadNextPage(){
        setLoadingMoreItems(true);
        RikdagskollenApp.getInstance().getRiksdagenAPIManager().getProtocols( new ProtocolCallback() {

            @Override
            public void onProtocolsFetched(List<Protocol> protocols) {
                setShowLoadingView(false);
                protocolList.addAll(protocols);
                getAdapter().notifyDataSetChanged();
                setLoadingMoreItems(false);
            }

            @Override
            public void onFail(VolleyError error) {
                setLoadingMoreItems(false);
                decrementPage();
            }
        },getPageToLoad());

        incrementPage();
    }

    @Override
    RiksdagenViewHolderAdapter getAdapter() {
        return adapter;
    }
}