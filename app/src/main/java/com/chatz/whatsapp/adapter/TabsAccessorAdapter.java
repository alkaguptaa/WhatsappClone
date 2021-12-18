package com.chatz.whatsapp.adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.chatz.whatsapp.fragment.ChatsFragment;
import com.chatz.whatsapp.fragment.ContactsFragment;
import com.chatz.whatsapp.fragment.GroupsFragment;
import com.chatz.whatsapp.fragment.NewsFeedFragment;
import com.chatz.whatsapp.fragment.RequestsFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter {

    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {

        switch (i) {
            case 0:
                NewsFeedFragment newsFeedFragment = new NewsFeedFragment();
                return newsFeedFragment;

            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 2:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;

            case 3:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;

            case 4:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Feed";

            case 1:
                return "Chats";

            case 2:
                return "Groups";

            case 3:
                return "Contacts";

            case 4:
                return "Requests";

            default:
                return null;

        }
    }
}
