package oscar.riksdagskollen.CurrentNews;

import android.content.Context;

import com.android.volley.VolleyError;

import java.util.List;

import oscar.riksdagskollen.CurrentNews.CurrentNewsJSONModels.CurrentNews;
import oscar.riksdagskollen.Manager.AlertManager;
import oscar.riksdagskollen.RiksdagskollenApp;
import oscar.riksdagskollen.Util.Helper.CustomTabs;

import static oscar.riksdagskollen.CurrentNews.CurrentNewsListFragment.SECTION_NAME_NEWS;

public class CurrentNewsPresenter implements CurrentNewsContract.Presenter, CurrentNewsCallback {

    private CurrentNewsContract.View view;
    private CurrentNewsModel model;

    public CurrentNewsPresenter(CurrentNewsContract.View view) {
        this.view = view;
        this.model = new CurrentNewsModel();
    }

    @Override
    public void loadMoreItems() {
        model.getNews(this);
        view.showLoadingItemsView(true);
    }

    @Override
    public void handleNotificationItemClick(String latestId) {
        boolean enabled = RiksdagskollenApp.getInstance().getAlertManager().toggleEnabledForPage(SECTION_NAME_NEWS, latestId);
        if (enabled) {
            view.fillNotificationItem();
        } else {
            view.unFillNotificationItem();
        }

    }

    @Override
    public void updateAlertsLatestDocument(String id) {
        if (isNotificationsEnabled()) {
            AlertManager.getInstance().setAlertEnabledForSection(SECTION_NAME_NEWS, id, true);
        }
    }

    @Override
    public void handleItemClick(CurrentNews newsItem, Context context) {
        if (newsItem.getUrl() != null && context != null)
            CustomTabs.openTab(context, newsItem.getNewsUrl());
    }

    @Override
    public boolean isNotificationsEnabled() {
        return AlertManager.getInstance().isAlertEnabledForSection(SECTION_NAME_NEWS);
    }


    @Override
    public void onNewsFetched(List<CurrentNews> currentNews) {
        view.addItemsToView(currentNews);
        view.showLoadingItemsView(false);
        view.showLoadingView(false);
        if (model.getPageToLoad() == 1) {
            if (RiksdagskollenApp.getInstance().shouldAskForRating()) {
                view.showPlayRatingView();
            }
            updateAlertsLatestDocument(currentNews.get(0).getId());
        }
        model.incrementPage();
    }

    @Override
    public void onFail(VolleyError error) {
        view.showLoadFailView();
    }
}
