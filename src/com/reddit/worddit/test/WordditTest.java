package com.reddit.worddit.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Random;

import junit.framework.Assert;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.reddit.worddit.WordditHome;
import com.reddit.worddit.api.Session;
import com.reddit.worddit.api.Worddit;
import com.reddit.worddit.api.response.Game;
import com.reddit.worddit.api.response.Tile;

public class WordditTest extends ActivityInstrumentationTestCase2<WordditHome> {
	public static final String TAG = "WordditTest";
	protected WordditHome mActivity;
	
	protected String URL = Session.API_URL;
	
	protected String GameID;
	
	/**
	 * Valid accounts.
	 */
	protected Account USERS[] = {
			new Account("bob@example.com","panda"),
			new Account("alice@example.com", "kitten")
	};
	
	public WordditTest() {
		super("com.reddit.worddit", WordditHome.class);
	}
	
	protected Session getSession() throws MalformedURLException {
		return Session.makeSession(URL);
	}
	
	protected Account getAccount() {
		Random rng = new Random();
		return USERS[ rng.nextInt(USERS.length) ];
	}
	
	public void testAAAPreconditions() {
		try {
			Session s = getSession();
			for(int i = 0; i < USERS.length; i++) {
				Account a = USERS[i];
				boolean result = s.createAccount(a.user, a.password);
				if(result == false) {
					assertEquals(s.getLastResponse(), Worddit.USER_EXISTS);
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testLogin() {
		Account a = getAccount();
		try {
			Session s = getSession();
			assertEquals(s.login(a.user, a.password),true);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testBadLogin() {
		Account a = getAccount();
		try {
			Session s = getSession();
			assertEquals(s.login(a.user, a.password.substring(1)),false);
			assertEquals(s.login(a.user.substring(1), a.password),false);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testBadCreate() {
		try {
			Session s = getSession();
			Account a = getAccount();
			assertEquals(s.createAccount(a.user, a.password),false);
			assertEquals(s.getLastResponse(),Worddit.USER_EXISTS);
		} catch (MalformedURLException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void testUserCreate() {
		try {
			Session s = getSession();
			
			Random rng = new Random();
			StringBuffer username = new StringBuffer();
			StringBuffer password = new StringBuffer();
			int l = 5 + rng.nextInt(10);
			
			for(int i = 0; i < l; i++) {
				username.append((char) ('a' + rng.nextInt(26)));
				password.append((char) ('a' + rng.nextInt(26)));
			}
			username.append("@example.com");
			
			System.out.printf("Creating user %s with password %s\n", username, password);
			
			assertEquals(s.createAccount(username.toString(), password.toString()),true);
		} catch (MalformedURLException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void info(String msg) {
		Log.i(TAG, msg);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = this.getActivity();
	}
	
	class Account {
		public Account(String u, String p) {
			user = u;
			password = p;
		}
		String user, password;
	}
}
