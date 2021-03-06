package oscar.riksdagskollen.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;

import oscar.riksdagskollen.Activity.DocumentReaderActivity;
import oscar.riksdagskollen.Manager.AlertManager;
import oscar.riksdagskollen.R;
import oscar.riksdagskollen.RiksdagskollenApp;
import oscar.riksdagskollen.Util.Adapter.PartyListViewholderAdapter;
import oscar.riksdagskollen.Util.Adapter.RiksdagenViewHolderAdapter;
import oscar.riksdagskollen.Util.Enum.DocumentType;
import oscar.riksdagskollen.Util.JSONModel.Party;
import oscar.riksdagskollen.Util.JSONModel.PartyDocument;
import oscar.riksdagskollen.Util.RiksdagenCallback.PartyDocumentCallback;
import oscar.riksdagskollen.Util.View.FilterDialog;

/**
 * Created by gustavaaro on 2018-03-26.
 */

public class PartyListFragment extends RiksdagenAutoLoadingListFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Party party;
    private ArrayList<PartyDocument> documentList = new ArrayList<>();
    private PartyListViewholderAdapter adapter;
    private SharedPreferences preferences;
    private ArrayList<DocumentType> oldFilter;
    private MenuItem notificationItem;

    /**
     *
     * @param party the party object that the fragment will display feed for
     * @return a new instance of this fragment with the Party object in its arguments
     */

    public static PartyListFragment newInstance(Party party){
        Bundle args = new Bundle();
        args.putParcelable("party",party);
        PartyListFragment newInstance = new PartyListFragment();
        newInstance.setArguments(args);
        newInstance.setRetainInstance(true);
        return newInstance;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        preferences.registerOnSharedPreferenceChangeListener(this);
        showNoContentWarning(getFilter().isEmpty());
    }

    @Override
    public void onPause() {
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(party.getName());
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        applyFilter();
        showNoContentWarning(getFilter().isEmpty());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.party = getArguments().getParcelable("party");
        preferences = getActivity().getSharedPreferences(party.getID(),getActivity().MODE_PRIVATE);
        adapter = new PartyListViewholderAdapter(documentList, new RiksdagenViewHolderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object document) {
                Intent intent = new Intent(getContext(), DocumentReaderActivity.class);
                intent.putExtra("document",((PartyDocument)document));
                startActivity(intent);
            }
        }, this);
        getAdapter().setHasStableIds(true);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {
            case R.id.notification_menu_item:
                boolean enabled;
                try {
                    enabled = RiksdagskollenApp.getInstance().getAlertManager().toggleEnabledForParty(party.getID(), documentList.get(0).getId());
                } catch (IndexOutOfBoundsException e) {
                    // if no items has been loaded, fall back on an empty string. Doc id will be updated once some items has been loaded
                    enabled = RiksdagskollenApp.getInstance().getAlertManager().toggleEnabledForParty(party.getID(), "");
                }
                if (enabled) {
                    item.setIcon(R.drawable.notifications_border_to_filled_animated);
                    Toast.makeText(getContext(), String.format("Du kommer nu få en notis när %s publicerar ett nytt dokument", party.getName()), Toast.LENGTH_LONG).show();
                } else {
                    item.setIcon(R.drawable.notifications_filled_to_border_animated);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((Animatable) item.getIcon()).start();
                    }
                });
                break;
            case R.id.menu_filter:
                oldFilter = getFilter();
                final CharSequence[] items = DocumentType.getPartyDisplayNames();
                boolean[] checked = new boolean[items.length];
                for (int i = 0; i < items.length; i++) {
                    checked[i] = preferences.getBoolean(DocumentType.getPartyDokTypes().get(i).getDocType(), true);
                }

                final SharedPreferences.Editor editor = preferences.edit();

                FilterDialog dialog = new FilterDialog("Filtrera partiflöde", items, checked);
                dialog.setItemSelectedListener((which, isChecked) -> {
                    editor.putBoolean(DocumentType.getPartyDokTypes().get(which).getDocType(), isChecked);
                });
                dialog.setPositiveButtonListener(v -> editor.apply());
                dialog.setNegativeButtonListener(v -> editor.clear());
                dialog.setOnDismissListener(dialogInterface -> editor.clear());
                dialog.show(getFragmentManager(), "dialog");

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Load the next page and add it to the adapter when downloaded and parsed.
     * Hides the loading view.
     */
    protected void loadNextPage(){
        setLoadingMoreItems(true);
        RiksdagskollenApp.getInstance().getRiksdagenAPIManager().getDocumentsForParty(party, getPageToLoad(), new PartyDocumentCallback() {
            @Override
            public void onDocumentsFetched(List<PartyDocument> documents) {
                documentList.addAll(documents);
                List<PartyDocument> filteredDocuments = filter(documents);

                int itemCountBeforeLoad = getAdapter().getItemCount();
                getAdapter().addAll(filteredDocuments);

                if (getPageToLoad() <= 2) {
                    updateAlertsLatestDocument();
                }

                // Unpretty fix for a bug where recyclerview sometimes scrolls to the bottom after initial filter
                if (itemCountBeforeLoad <= 1) getRecyclerView().scrollToPosition(0);

                // Load next page if the requested page does not contain any documents matching the filter
                // or if there are too few documents in the list
                if ((filteredDocuments.isEmpty() || getAdapter().getItemCount() < MIN_DOC) && !getFilter().isEmpty()) {
                    loadNextPage();
                    setLoadingUntilFull(true);
                } else {
                    setLoadingUntilFull(false);
                }
                if (!isLoadingUntilFull()) setLoadingMoreItems(false);
                setShowLoadingView(false);

                if (notificationItem != null) notificationItem.setVisible(true);
            }

            @Override
            public void onFail(VolleyError error) {
                onLoadFail();
            }
        });
        incrementPage();
    }

    @Override
    protected void clearItems() {
        documentList.clear();
        adapter.clear();
    }

    private ArrayList<DocumentType> getFilter() {
        ArrayList<DocumentType> filter = new ArrayList<>();
        for (DocumentType documentType : DocumentType.getPartyDokTypes()) {
            if (preferences.getBoolean(documentType.getDocType(), true)) filter.add(documentType);
        }
        return filter;
    }

    private List<PartyDocument> filter(List<PartyDocument> documents) {
        final List<PartyDocument> filteredDocumentList = new ArrayList<>();
        for (PartyDocument document : documents) {
            if (getFilter().contains(DocumentType.getDocTypeForDocument(document))) {
                filteredDocumentList.add(document);
            }
        }
        return filteredDocumentList;
    }

    private void applyFilter() {
        getAdapter().replaceAll(filter(documentList));
    }

    private void onFilterChanged() {
        List<DocumentType> filter = getFilter();

        if (!filter.equals(oldFilter)) {
            applyFilter();
        }

        showNoContentWarning(filter.isEmpty());
        if (getAdapter().getItemCount() < MIN_DOC && !filter.isEmpty()) loadNextPage();

    }

    @Override
    protected RiksdagenViewHolderAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        onFilterChanged();
    }

    private void updateAlertsLatestDocument() {
        if (AlertManager.getInstance().isAlertEnabledForParty(party.getID())) {
            AlertManager.getInstance().setAlertEnabledForPartyDocuments(party.getID(), documentList.get(0).getId(), true);
        }
    }
}



