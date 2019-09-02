package de.dwslab.petar.walks;

import java.util.Dictionary;
import java.util.Hashtable;

public class PersonilisedQuery {
	
	private int id;
	private Dictionary<String, Integer> uris = new Hashtable<String, Integer>();
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Dictionary<String, Integer> getUris() {
		return uris;
	}
	public void setUris(Dictionary<String, Integer> uris) {
		this.uris = uris;
	}
	
	public void addElementDict (String uri, int value)
	{
		uris.put(uri, value);
	}
}
