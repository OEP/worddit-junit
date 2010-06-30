package com.reddit.worddit.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
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
	/**
	 * Valid accounts.
	 */
	public Account USERS[] = {
				new Account("bob@example.com","panda"),
				new Account("alice@example.com", "kitten"),
				new Account("cindy@example.com", "puppy"),
				new Account("daniel@example.com", "kangaroo")
		};
	
	public static final String TAG = "WordditTest";
	protected WordditHome mActivity;
	
	public static final String URL = "http://130.160.75.97:8080/api";
	
	protected String GameID;
	
	
	public WordditTest() {
		super("com.reddit.worddit", WordditHome.class);
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
		} catch (Exception e) {
			handle(e);
		}
	}
	
	public void testRequest() {
		try {
			Session s[] = makeAuthedSessions(2);
			
			// User 1 requests a game with 2 people
			String id1, id2;
			assertNotNull(id1 = s[0].requestGame(2, "rule"));
			assertNotNull(id2 = s[1].requestGame(2, "rule"));
			
			// Should have been paired together
			assertEquals(id1, id2);
		} catch (Exception e) {
			handle(e);
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
			
		} catch (Exception e) {
			handle(e);
		}
	}
	
	public void testSetProfile() {
		try {
			Session s = makeAuthedSession();
			assertEquals(true, s.setProfile("idiot@example.com", "super-easy", "doofushead"));
			
		} catch (Exception e) {
			handle(e);
		}
	}
	
	public void testSetAvatar() {
		// TODO: Write the test case for this.
		fail("Not yet written");
	}
	
	public void testFindFriend() {
		try {
			Session noauth = getSession();
			Session auth = makeAuthedSession();
			Account b = getAccount();

			// Do it while not auth'd
			Profile p = getProfile(b,noauth);
			
			// Server shouldn't reveal email.
			assertNull(p.email);
			
			// Do it while auth'd
			p = getProfile(b, auth);
			
			// Email & avatar can be null
			assertNotNull(p.id);
			assertNotNull(p.nickname);
			
			
		} catch (Exception e) {
			handle(e);
		}
	}
	
	public void testGetFriends() {
		try {
			Session s = makeAuthedSession();
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
			handle(e);
		}
	}
	
	public void testBefriend() {
		try {
			PlayerGroup g = makeGroup(2);
			boolean result = g.S[0].befriend(g.P[1].id);
			assertTrue(String.format("%s friends %s -- ", g.P[0].id, g.P[1].id) + 
					Integer.toString(g.S[0].getLastResponse()), result);
		} catch (Exception e) {
			handle(e);
		}
	}
	
	public void testAcceptFriend() {
		try {
			PlayerGroup g = makeGroup(2);
			
			boolean result = g.S[0].befriend(g.P[1].id);
			assertTrue("Couldn't befriend", result);
			
			// Accepts the friendship
			assertTrue(g.S[1].acceptFriend(g.P[0].id));
			
		} catch (Exception e) {
			handle(e);
		}
	}
	
	public void testAcceptGame() {
		try {
			PlayerGroup g = makeGroup(2);
			
			String gameId = g.S[0].newGame(g.P[1].id, "woot");
			assertNotNull("New game not created. " + g.S[0].getLastResponse(), gameId);
			
			boolean result = g.S[1].acceptGame(gameId);
			assertTrue("Couldn't accept game: " + g.S[1].getLastResponse(), result);
		} catch (Exception e) {
			handle(e);
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
			handle(e);
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
			Game[] games = s.getGames();
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
			handle(e);
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
			handle(e);
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
			handle(e);
		}
	}
	
	public void testGetBoard() {
		try {
			TestGame game = makeGame(2);
			
			GameBoard board = game.Players.S[0].getBoard(game.ID);
			
			if(board == null) {
				assertEquals(Worddit.SUCCESS, game.Players.S[0].getLastResponse());
			}
			
		} catch (Exception e) {
			handle(e);
		}
	}
	
	protected void handle(Exception e) {
		e.printStackTrace();
		fail("Exception thrown: " + e.getMessage());
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

	
	protected Session makeAuthSession(Account a) throws IOException {
		Session s = getSession();
		assertTrue("Couldn't authenticate: " + a.user,
				s.login(a.user, a.password));
		return s;
	}
	
	static class TestGame {
		public PlayerGroup Players;
		public String ID;
	}
	
	static class PlayerGroup {
		public Session S[];
		public Account A[];
		public Profile P[];
		
		public ArrayList<String> getIDs() {
			ArrayList<String> l = new ArrayList<String>();
			if(P == null) return l;
			
			for(Profile p : P) {
				l.add(p.id);
			}
			
			return l;
		}
	}
	
	static class Account {
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
	
	
	public Session getSession() throws MalformedURLException {
		return Session.makeSession(URL);
	}
	

	public PlayerGroup makeGroup(int n) throws IOException {
		PlayerGroup g = new PlayerGroup();
		
		g.A = getAccounts(n);
		g.S = makeAuthedSessions(g.A);
		g.P = getProfiles(g.A, g.S[0]);
		
		return g;
	}
	
	public TestGame makeGame(int n) throws IOException {
		PlayerGroup group = makeGroup(n);
		TestGame game = new TestGame();
		
		Session l = group.S[0];
		ArrayList<String> list = new ArrayList<String>();
		list.add("test-junit");
		
		game.ID = l.newGame(group.getIDs(), list);
		assertNotNull("Couldn't make new game", game.ID);
		
		game.Players = group;
		
		return game;
	}
	
	public Profile getProfile(Account a, Session s) throws IOException {
		Profile p;
		assertNotNull("Couldn't get profile: " + a.user, p = s.findUser(a.user));
		return p;
	}
	
	public Profile[] getProfiles(Account a[], Session s) throws IOException {
		Profile p[] = new Profile[a.length];
		for(int i = 0; i < a.length; i++) {
			p[i] = getProfile(a[i], s);
		}
		return p;
	}
	
	public Account getAccount() {
		Random rng = new Random();
		return USERS[ rng.nextInt(USERS.length) ];
	}
	
	public Account getUniqueAccount(Account ... accounts) {
		boolean tested[] = new boolean[USERS.length];
		Random rng = new Random();
		
		int r;
		int uniques = 0;

		while(uniques < USERS.length) {
			r = rng.nextInt(USERS.length);
			while(tested[r] == true) {
				r = rng.nextInt(USERS.length);
			}
			
			boolean collision = false;
			for(Account a : accounts) {
				if(a != null && a.equals(USERS[r])) {
					tested[r] = true;
					collision = true;
					uniques++;
					break;
				}
			}
			
			if(!collision) return USERS[r];
		}
		
		throw new IllegalArgumentException("Ran out of unique accounts.");
	}
	
	public Account[] getAccounts(int n) {
		Account a[] = new Account[n];
		
		for(int i = 0; i < n; i++) {
			a[i] = getUniqueAccount(a);
		}
		
		return a;
	}
	
	public Session makeAuthedSession() throws IOException {
		Session s[] = makeAuthedSessions(1);
		return s[0];
	}
	
	public Session makeAuthedSession(Account a) throws IOException {
		Account tmpAccounts[] = new Account[1];
		Session tmpSessions[] = makeAuthedSessions(tmpAccounts);
		return tmpSessions[0];
	}
	
	public Session[] makeAuthedSessions(int n) throws IOException {
		Account a[] = getAccounts(n);
		Session s[] = makeAuthedSessions(a);
		return s;
	}
	
	public Session[] makeAuthedSessions(Account a[]) throws IOException {
		Session s[] = new Session[a.length];
		for(int i = 0; i < a.length; i++) {
			s[i] = getSession();
			assertTrue("Couldn't log in: " + a[i].user, s[i].login(a[i].user, a[i].password));
		}
		return s;
	}
}
