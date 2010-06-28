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
import com.reddit.worddit.api.response.Friend;
import com.reddit.worddit.api.response.Game;
import com.reddit.worddit.api.response.Profile;
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
			
			// Typed in a bad password
			assertEquals(s.login(a.user, a.password.substring(1)),false);
			
			// Typed in a bad username
			assertEquals(s.login(a.user.substring(1), a.password),false);
			
			// Typed in correct credentials.
			assertEquals(s.login(a.user, a.password),true);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testSetProfile() {
		try {
			Session s = getSession();
			Account a = getAccount();
			
			// Do it without logging in.
			assertEquals(s.setProfile("idiot@example.com", "super-easy", "doofushead"), false);
			
			// Log in and do it.
			assertEquals(s.login(a.user, a.password),true);
			assertEquals(s.setProfile("idiot@example.com", "super-easy", "doofushead"), true);
			
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(true,false);
		}
	}
	
	public void testSetAvater() {
		// TODO: Write the test case for this.
		assertNotNull(null);
	}
	
	public void testFindFriend() {
		try {
			Session s = getSession();
			Account a = getAccount();
			Account b = getAccount();
			
			assertNull(s.findUser(b.user));
			assertEquals(s.login(a.user, a.password),true);
			Profile p = s.findUser(b.user);
			assertNotNull(p);
			
			assertNotNull(p.avatar);
			// Email can be null
			assertNotNull(p.id);
			assertNotNull(p.nickname);
			
			
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(true,false);
		}
	}
	
	public void testGetFriends() {
		try {
			Session s = getSession();
			Account a = getAccount();
			
			// Without logging in.
			assertNull(s.getFriends());
			
			assertEquals(s.login(a.user, a.password), true);
			Friend friends[] = s.getFriends();
			assertNotNull(friends);
			
			for(Friend f : friends) {
				assertNotNull(f.id);
				assertEquals(
					f.isActive() || f.isPending() || f.isRequested(),
					true
				);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(true,false);
		}
	}
	
	public void testGetGames() {
		try {
			Session s = getSession();
			Account a = getAccount();
			
			// Do it without logging in.
			assertNull(s.getGames());
			
			// Log in and do it.
			assertEquals(s.login(a.user, a.password),true);
			Game games[] = s.getGames();
			assertNotNull(games);
			
			// Make sure the game data looks good.
			for(Game g : games) {
				assertNotNull(g.id);
				assertNotNull(g.current_player);
				assertNotNull(g.last_move_utc);
				assertNotNull(g.players);
				
				for(String player : g.players) {
					assertNotNull(player);
				}
				
				// Make sure 'status' is a valid constant.
				assertEquals(
						g.isAccepted() || g.isActive() || g.isInvited() || g.isWaiting(),
						true);
						
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(true,false);
		}
	}
	
	public void testUserCreate() {
		try {
			Session s = getSession();
			
			Account a = getAccount();
			// Try to create an account which we know exists.
			assertEquals(s.createAccount(a.user, a.password),false);
			// Make sure the web server responded with correct code.
			assertEquals(s.getLastResponse(),Worddit.USER_EXISTS);
			
			// Make a random username and create him.
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
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(true,false);
		}
	}
	
	public void testNewGame() {
		try {
			Session s = getSession();
			Account a = getAccount();
			
			// Try to get a game without logging in.
			assertNull(s.newGame("hi", "woot"));
			assertEquals(s.getLastResponse(), Worddit.AUTH_INVALID);
			
			// Try it while logging in.
			String id;
			assertEquals(s.login(a.user, a.password),true);
			error("Login sanity check",s);
			assertNotNull(id = s.newGame("hi", "woot"));
			if(id == null) {
				error("Game ID null", s);
			}
			
		} catch (Exception e) {
			assertEquals(true,false);
			e.printStackTrace();
		}
	}
	
	protected void error(String msg, Session s) {
		Log.e(TAG, msg);
		Log.e(TAG, "\tResponse code: " + s.getLastResponse());
		Log.e(TAG, "\tCookie: " + s.getCookie());
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
