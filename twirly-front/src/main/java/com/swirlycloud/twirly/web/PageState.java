/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.swirlycloud.twirly.web.Realm;

public final class PageState implements Realm {
    private final UserService userService;
    private final User user;
    private Page page;
    private int traderCount = -1;

    public PageState() {
        userService = UserServiceFactory.getUserService();
        user = userService.getCurrentUser();
    }

    @Override
    public final String getUserEmail() {
        return isUserLoggedIn() ? user.getEmail() : null;
    }

    @Override
    public final String getLoginUrl(String targetUrl) {
        return userService.createLoginURL(targetUrl);
    }

    @Override
    public final String getLogoutUrl(String targetUrl) {
        return userService.createLogoutURL(targetUrl);
    }

    @Override
    public final boolean isDevEnv() {
        return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
    }

    @Override
    public final boolean isUserLoggedIn() {
        return user != null;
    }

    @Override
    public final boolean isUserAdmin() {
        return isUserLoggedIn() && userService.isUserAdmin();
    }

    @Override
    public final boolean isUserTrader() {
        if (!isUserLoggedIn()) {
            return false;
        }
        if (traderCount < 0) {
            // Lazy.
            final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            final Filter filter = new FilterPredicate("email", FilterOperator.EQUAL,
                    user.getEmail());
            final Query q = new Query("Trader").setFilter(filter).setKeysOnly();
            final PreparedQuery pq = datastore.prepare(q);
            traderCount = pq.countEntities(FetchOptions.Builder.withLimit(1));
        }
        return traderCount == 1;
    }

    public final void setPage(Page page) {
        this.page = page;
    }

    public final String getLoginURL() {
        return getLoginUrl(page.getPath());
    }

    public final String getLogoutURL() {
        return getLoginUrl(Page.HOME.getPath());
    }

    public final boolean isHomePage() {
        return page == Page.HOME;
    }

    public final boolean isTradePage() {
        return page == Page.TRADE;
    }

    public final boolean isContrPage() {
        return page == Page.CONTR;
    }

    public final boolean isAdminPage() {
        return page == Page.MARKET || page == Page.TRADER;
    }

    public final boolean isMarketPage() {
        return page == Page.MARKET;
    }

    public final boolean isTraderPage() {
        return page == Page.TRADER;
    }

    public final boolean isAboutPage() {
        return page == Page.ABOUT;
    }

    public final boolean isContactPage() {
        return page == Page.CONTACT;
    }
}
