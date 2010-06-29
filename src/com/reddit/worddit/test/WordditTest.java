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
import com.reddit.worddit.api.response.GameBoard;

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
	
	protected Account getUniqueAccount(Account ... accounts) {
		boolean tested[] = new boolean[USERS.length];
		Random rng = new Random();
		
		int r = rng.nextInt(USERS.length);
		int uniques = 0;

		while(tested[r] == false && uniques < USERS.length) {
			boolean collision = false;
			for(Account a : accounts) {
				if(a.equals(tested[r])) {
					tested[r] = true;
					collision = true;
					uniques++;
					break;
				}
			}
			
			if(!collision) return USERS[r];
		}
		
		return null;
	}
	
	public void testAAAPreconditions() {
		try {
			Session s = getSession();
			for(int i = 0; i < USERS.length; i++) {
				Account a = USERS[i];
				boolean result = s.createAccount(a.user, a.password);
				if(result == false) {
					assertEquals(Worddit.USER_EXISTS, s.getLastResponse());
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
	
	public void testRequest() {
		try {
			Session s = getSession();
			Session s2 = getSession();
			
			Account a = getAccount();
			Account b = getUniqueAccount(a);
			
			// Fail cases: forgot to log in.
			assertNull(s.requestGame(2, "rule"));
			assertNull(s2.requestGame(2, "rule"));
			
			// Users log in.
			assertEquals(true, s.login(a.user, a.password));
			assertEquals(true, s2.login(b.user, b.password));
			
			// User 1 requests a game with 2 people
			String id1, id2;
			assertNotNull(id1 = s.requestGame(2, "rule"));
			assertNotNull(id2 = s2.requestGame(2, "rule"));
			
			// Should have been paired together
			assertEquals(id1, id2);
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(true,false);
		}
	}
	
	public void testLogin() {
		Account a = getAccount();
		try {
			Session s = getSession();
			
			// Typed in a bad password
			assertEquals(false, s.login(a.user, a.password.substring(1)));
			
			// Typed in a bad username
			assertEquals(false, s.login(a.user.substring(1), a.password));
			
			// Typed in correct credentials.
			assertEquals(true, s.login(a.user, a.password));
			
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
			assertEquals(false, s.setProfile("idiot@example.com", "super-easy", "doofushead"));
			
			// Log in and do it.
			assertEquals(true,s.login(a.user, a.password));
			assertEquals(true, s.setProfile("idiot@example.com", "super-easy", "doofushead"));
			
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(true,false);
		}
	}
	
	public void testSetAvatar() {
		// TODO: Write the test case for this.
		assertNotNull(null);
	}
	
	public void testFindFriend() {
		try {
			Session s = getSession();
			Account a = getAccount();
			Account b = getAccount();

			Profile p;
			assertNotNull(p = s.findUser(b.user));
			
			// Server shouldn't reveal email.
			assertNull(p.email);
			assertEquals(true,s.login(a.user, a.password));
			assertNotNull(p = s.findUser(b.user));
			
			// Email & avatar can be null
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
			
			assertEquals(true,s.login(a.user, a.password));
			Friend friends[] = s.getFriends();
			assertNotNull(friends);
			
			for(Friend f : friends) {
				assertNotNull(f.id);
				assertEquals(
					true,
					f.isActive() || f.isPending() || f.isRequested()
				);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(true,false);
		}
	}
	
	public void testBefriend() {
		try {
			Session s = getSession();
			Account a = getAccount();
			Account b = getUniqueAccount(a);
			
			// Should fail due to no login
			assertFalse(s.befriend(b.user));
			
			assertTrue(s.login(a.user, a.password));
			boolean result = s.befriend(b.user);
			assertTrue(b.user + " " + Integer.toString(s.getLastResponse()), result);
		} catch (Exception e) {
			info(e.getMessage());
		}
	}
	
	public void testAcceptFriend() {
		try {
			Session s = getSession(),
				s2 = getSession();
			Account a = getAccount();
			Account b = getUniqueAccount(a);
			
			// Should fail due to no login
			assertFalse(s.befriend(b.user));
			
			assertTrue(s.login(a.user, a.password));
			assertTrue(s2.login(b.user, b.password));
			
			boolean result = s.befriend(b.user);
			assertTrue(b.user + " " + Integer.toString(s.getLastResponse()), result);
			
			// Accepts the friendship
			assertTrue(s2.acceptFriend(a.user));
			
		} catch (Exception e) {
			info(e.getMessage());
		}
	}
	
	public void testAcceptGame() {
		try {
			Session s = getSession(),
				s2 = getSession();
			Account a = getAccount();
			Account b = getUniqueAccount(a);
			
			// Should fail due to no login
			String gameId;
			assertNull(s.newGame(b.user, "foo"));
			
			// Log in the users...
			assertTrue(s.login(a.user, a.password));
			assertTrue(s2.login(b.user, b.password));
			
			gameId = s.newGame(b.user, "woot");
			assertNotNull("New game not created. " + s.getLastResponse(), gameId);
			
			boolean result = s2.acceptGame(gameId);
			assertTrue("Couldn't accept game: " + s2.getLastResponse(), result);
		} catch (Exception e) {
			info(e.getMessage());
		}
	}
	
	public void testRejectGame() {
		try {
			Session s = getSession(),
				s2 = getSession();
			Account a = getAccount();
			Account b = getUniqueAccount(a);
			
			// Should fail due to no login
			String gameId;
			assertNull(s.newGame(b.user, "foo"));
			
			// Log in the users...
			assertTrue(s.login(a.user, a.password));
			assertTrue(s2.login(b.user, b.password));
			
			gameId = s.newGame(b.user, "foo");
			assertNotNull("New game not created. " + s.getLastResponse(), gameId);
			
			boolean result = s2.rejectGame(gameId);
			assertTrue("Couldn't reject game: " + s2.getLastResponse(), result);
		} catch (Exception e) {
			info(e.getMessage());
		}
	}
	
	public void testGetGames() {
		try {
			Session s = getSession();
			Account a = getAccount();
			
			// Do it without logging in.
			assertNull(s.getGames());
			
			// Log in and do it.
			assertEquals(true,s.login(a.user, a.password));
			Game games[] = s.getGames();
			assertNotNull(games);
			
			// Make sure the game data looks good.
			for(Game g : games) {
				assertNotNull(g.id);
				assertNotNull(g.players);
				
				for(Game.Player player : g.players) {
					assertNotNull(player);
					assertNotNull(player.id);
				}
				
				// Make sure 'status' is a valid constant.
				assertTrue("Invalid value: " + g.status,
						g.isAccepted() || g.isActive() || g.isInvited() || g.isWaiting());
						
			}
		} catch (Exception e) {
			info(e.getMessage());
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
			Account b = getUniqueAccount(a);
			
			// Try to get a game without logging in.
			assertNull(s.newGame(b.user, "woot"));
			assertEquals(Worddit.AUTH_INVALID, s.getLastResponse());
			
			// Try it while logging in.
			String id;
			assertEquals(true,s.login(a.user, a.password));
			error("Login sanity check",s);
			assertNotNull(id = s.newGame(b.user, "woot"));
			
		} catch (Exception e) {
			info(e.getMessage());
			e.printStackTrace();
			assertEquals(true,false);
		}
	}
	
	public void testGetBoard() {
		try {
			Session s = getSession();
			Account a = getAccount();
			Account b = getUniqueAccount(a);
			
			// Try to get a game without logging in.
			assertNull(s.newGame(b.user, "woot"));
			assertEquals(Worddit.AUTH_INVALID, s.getLastResponse());
			
			// Try it while logging in.
			String id;
			assertEquals(true,s.login(a.user, a.password));
			error("Login sanity check",s);
			assertNotNull(id = s.newGame(b.user, "woot"));
			
			GameBoard board = s.getBoard(id);
			
			if(board == null) {
				assertEquals(Worddit.SUCCESS, s.getLastResponse());
			}
			
		} catch (Exception e) {
			info(e.getMessage());
			e.printStackTrace();
			assertEquals(true,false);
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
		
		public boolean equals(Object b) {
			if(b instanceof WordditTest.Account == false) return false;
			Account other = (Account) b;
			return user.equalsIgnoreCase(other.user) &&
				password.equalsIgnoreCase(other.password);
		}
		
		public int hashCode() {
			return String.format("%s:%s", user,password).hashCode();
		}
		
		String user, password;
	}
}
