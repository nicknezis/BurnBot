package org.nicknack.dailyburn.api;

import oauth.signpost.OAuthProvider;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

public class DaoFactory {
	private HttpClient client;
	private OAuthProvider provider;
	
	public DaoFactory(OAuthProvider provider) {
		this.client = new DefaultHttpClient();
		this.provider = provider;
	}
	public DaoFactory(HttpClient client, OAuthProvider provider) {
		this.client = client;
		this.provider = provider;	
	}
	
	public UserDao getUserDao() {
		UserDao dao = new UserDao(client, provider.getConsumer());
		return dao;
	}
}
