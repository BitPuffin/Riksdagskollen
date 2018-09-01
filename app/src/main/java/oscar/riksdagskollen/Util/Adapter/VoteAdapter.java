package oscar.riksdagskollen.Util.Adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.flexbox.FlexboxLayout;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import oscar.riksdagskollen.Activity.MainActivity;
import oscar.riksdagskollen.Activity.VoteActivity;
import oscar.riksdagskollen.R;
import oscar.riksdagskollen.RikdagskollenApp;
import oscar.riksdagskollen.Util.Callback.StringRequestCallback;
import oscar.riksdagskollen.Util.DecicionCategory;
import oscar.riksdagskollen.Util.JSONModel.Party;
import oscar.riksdagskollen.Util.JSONModel.Vote;

/**
 * Created by oscar on 2018-03-29.
 */

public class VoteAdapter extends RiksdagenViewHolderAdapter {
    private final SortedList<Vote> voteList = new SortedList<Vote>(Vote.class, new SortedList.Callback<Vote>() {
        @Override
        public int compare(Vote o1, Vote o2) {
            return mComparator.compare(o1, o2);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(Vote oldItem, Vote newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(Vote item1, Vote item2) {
            return item1.equals(item2);
        }

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }
    });
    private Context context;

    private static final Comparator<Vote> DEFAULT_COMPARATOR = new Comparator<Vote>() {
        @Override
        public int compare(Vote a, Vote b) {
            return 0;
        }
    };
    private Comparator<Vote> mComparator = DEFAULT_COMPARATOR;


    public VoteAdapter(List<Vote> items, final OnItemClickListener listener) {
        super(listener);
        setSortedList(voteList);
        addAll(items);

        this.clickListener = listener;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        if(viewType == TYPE_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.vote_list_item, parent, false);
            return new VoteAdapter.VoteViewHolder(itemView);
        } else {
            FrameLayout frameLayout = new FrameLayout(parent.getContext());
            //make sure it fills the space
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new HeaderFooterViewHolder(frameLayout);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position < headers.size()){
            View v = headers.get(position);
            //add our view to a header view and display it
            prepareHeaderFooter((HeaderFooterViewHolder) holder, v);
        }else if(position >= headers.size() + voteList.size()){
            View v = footers.get(position-voteList.size()-headers.size());
            //add our view to a footer view and display it
            prepareHeaderFooter((HeaderFooterViewHolder) holder, v);
        }else {
            Vote document = voteList.get(position);
            ((VoteAdapter.VoteViewHolder) holder).bind(document, this.clickListener);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof VoteViewHolder) {
            ((VoteViewHolder) holder).noVoteContainer.removeAllViews();
            ((VoteViewHolder) holder).yesVoteContainer.removeAllViews();
            ((VoteViewHolder) holder).resultRequest.cancel(true);

        }
    }

    public void add(Vote model) {
        voteList.add(model);
    }

    public void remove(Vote model) {
        voteList.remove(model);
    }

    @Override
    public void addAll(List<?> items) {
        voteList.addAll((Collection<Vote>) items);
    }

    @Override
    public void removeAll(List<?> items) {
        voteList.beginBatchedUpdates();
        for (Object item : items) {
            voteList.remove((Vote) item);
        }
        voteList.endBatchedUpdates();
    }

    @Override
    public void replaceAll(List<?> items) {
        voteList.beginBatchedUpdates();
        voteList.clear();
        ;
        voteList.addAll((Collection<Vote>) items);
        voteList.endBatchedUpdates();
    }


    /**
     * Class for displaying individual items in the list.
     */
    public class VoteViewHolder extends RecyclerView.ViewHolder{
        private final TextView title;
        private final TextView date;
        private final View catColor;
        private final TextView catName;
        public final FlexboxLayout yesVoteContainer;
        public final FlexboxLayout noVoteContainer;
        public AsyncTask resultRequest;


        public VoteViewHolder(View textView) {
            super(textView);
            title = textView.findViewById(R.id.title);
            date = textView.findViewById(R.id.date);
            catColor = itemView.findViewById(R.id.category_border);
            catName = itemView.findViewById(R.id.category_name);
            yesVoteContainer = itemView.findViewById(R.id.vote_yes_icons);
            noVoteContainer = itemView.findViewById(R.id.vote_no_icons);
        }

        public void bind(final Vote item ,final OnItemClickListener listener) {

            title.setText(trimTitle(item.getTitel()));
            date.setText(item.getDatum());



            resultRequest = RikdagskollenApp.getInstance().getRequestManager().downloadHtmlPage("http:" + item.getDokument_url_html(), new StringRequestCallback() {
                @Override
                public void onResponse(String response) {
                    VoteActivity.VoteResults results = new VoteActivity.VoteResults(response);
                    ImageView partyIcon;
                    for (Party party : MainActivity.getParties()) {
                        int[] partyResult = results.getPartyVotes(party.getID().toUpperCase());
                        if (partyResult == null) continue;
                        int resultIndex = 0;
                        int max = 0;
                        for (int i = 0; i < 3; i++) {
                            if (i > 1) continue;
                            if (partyResult[i] > max) resultIndex = i;
                        }
                        partyIcon = new ImageView(context);
                        partyIcon.setImageResource(party.getDrawableLogo());
                        partyIcon.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
                        if (resultIndex == 0) yesVoteContainer.addView(partyIcon);
                        else if (resultIndex == 1) noVoteContainer.addView(partyIcon);
                    }
                }

                @Override
                public void onFail(VolleyError error) {

                }


            });


            DecicionCategory decicionCategory = DecicionCategory.getCategoryFromBet(item.getBeteckning());
            catColor.setBackgroundColor(context.getResources().getColor(decicionCategory.getCategoryColor()));
            catName.setText(decicionCategory.getCategoryName());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }

        //Removes text "Omröstning: Betänkande 2017:18Xyxyx" from title
        private String trimTitle(String title){
            return title.split(":")[2].substring(5).trim();
        }

    }



}

