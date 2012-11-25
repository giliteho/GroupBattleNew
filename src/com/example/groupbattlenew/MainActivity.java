package com.example.groupbattlenew;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;

import com.facebook.FacebookActivity;
import com.facebook.Session;
import com.facebook.SessionState;

public class MainActivity extends FacebookActivity {
	
	private static final int SPLASH = 0;
	private static final int SELECTION = 1;
	private static final int FRAGMENT_COUNT = SELECTION +1;
	private static final String FRAGMENT_PREFIX = "fragment";

	private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
	private static final String TAG = "GroupBattle";
	private boolean isResumed = false;
	private boolean restoredFragment = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		for(int i = 0; i < fragments.length; i++) {
		    restoreFragment(savedInstanceState, i);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    FragmentManager manager = getSupportFragmentManager();
	    // Since we're only adding one Fragment at a time, we 
	    // can only save one.
	    Fragment f = manager.findFragmentById(R.id.body_frame);
	    for (int i = 0; i < fragments.length; i++) {
	        if (fragments[i] == f) {
	            manager.putFragment(outState, 
	                    getBundleKey(i), fragments[i]);
	        }
	    }
	}
	
	@Override
	protected void onSessionStateChange(SessionState state, Exception exception) {
	    if (isResumed) {
	        FragmentManager manager = getSupportFragmentManager();
	        int backStackSize = manager.getBackStackEntryCount();
	        for (int i = 0; i < backStackSize; i++) {
	            manager.popBackStack();
	        }
	        if (state.isOpened()) {
	            FragmentTransaction transaction = manager.beginTransaction();
	            transaction.replace(R.id.body_frame, 
	                                fragments[SELECTION]).commit();
	        } else if (state.isClosed()) {
	            FragmentTransaction transaction = manager.beginTransaction();
	            transaction.replace(R.id.body_frame, 
	                                fragments[SPLASH]).commit();
	        }
	    }
	}
	
	@Override
	protected void onResumeFragments() {
	    super.onResumeFragments();
	    Session session = Session.getActiveSession();
	    if (session == null || session.getState().isClosed()) {
	        session = new Session(this);
	        Session.setActiveSession(session);
	    }

	    FragmentManager manager = getSupportFragmentManager();

	    if (restoredFragment) {
	        return;
	    }

	    // If we already have a valid token, then we can just open the session silently,
	    // otherwise present the splash screen and ask the user to login.
	    if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
	        // no need to add any fragments here since it will be 
	        // handled in onSessionStateChange
	        session.openForRead(this);
	    } else if (session.isOpened()) {
	        // if the session is already open, try to show the selection fragment
	        Fragment fragment = manager.findFragmentById(R.id.body_frame);
	        if (!(fragment instanceof SelectionFragment)) {
	            manager.beginTransaction().replace(R.id.body_frame, 
	                    fragments[SELECTION]).commit();
	        }
	    } else {
	        FragmentTransaction transaction = manager.beginTransaction();
	        transaction.replace(R.id.body_frame, 
	              fragments[SPLASH]).commit();
	    }
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    isResumed = true;
	}

	@Override
	public void onPause() {
	    super.onPause();
	    isResumed = false;
	}
	
	private String getBundleKey(int index) {
	    return FRAGMENT_PREFIX + Integer.toString(index);
	}
	
	private void restoreFragment(Bundle savedInstanceState, int fragmentIndex) {
	    Fragment fragment = null;
	    if (savedInstanceState != null) {
	        FragmentManager manager = getSupportFragmentManager();
	        fragment = manager.getFragment(savedInstanceState, 
	                   getBundleKey(fragmentIndex));
	    }
	    if (fragment != null) {
	        fragments[fragmentIndex] = fragment;
	        restoredFragment = true;
	    } else {
	        switch (fragmentIndex) {
	            case SPLASH:
	                fragments[SPLASH] = new SplashFragment();
	                break;
	            case SELECTION:
	                fragments[SELECTION] = new SelectionFragment();
	                break;
	            default:
	                Log.w(TAG, "invalid fragment index");
	                break;
	        }
	    }
	}

}
