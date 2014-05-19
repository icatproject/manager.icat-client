package fr.esrf.icat.test;

/*
 * #%L
 * ICAT client API
 * %%
 * Copyright (C) 2014 ESRF - The European Synchrotron
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.btr.proxy.search.ProxySearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ProxySelector;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

public class TestConnection {

	private final static String ICAT_SERVER = "https://icatisis.esc.rl.ac.uk";
	
	private final static String ICAT_WSDL = "ICATService/ICAT?wsdl";  
	
	private final static Logger LOG = Logger.getLogger(TestConnection.class.getName());
	
	public static void main(String[] args) {
		
		com.btr.proxy.util.Logger.setBackend(new com.btr.proxy.util.Logger.LogBackEnd() {
			@Override
			public void log(Class<?> arg0, com.btr.proxy.util.Logger.LogLevel arg1, String arg2, Object... arg3) {
				LOG.log(Level.WARNING, String.format(arg2, arg3));
			}
			
			@Override
			public boolean isLogginEnabled(com.btr.proxy.util.Logger.LogLevel arg0) {
				return true;
			}
		});
		
		System.out.println("****** Starting client ********");			
		ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();
		
		ProxySelector myProxySelector = proxySearch.getProxySelector();
        
		ProxySelector.setDefault(myProxySelector);
		
		URL serverURL;
		try {
			serverURL = new URL(ICAT_SERVER);
			URL wsdlURL = new URL(serverURL, ICAT_WSDL);
		
			HttpsURLConnection con = (HttpsURLConnection) wsdlURL.openConnection();
			con.connect();
			
			print_https_cert(con);
			print_content(con);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	   private static void print_https_cert(HttpsURLConnection con){
		   
		    if(con!=null){
		 
		      try {
		 
			System.out.println("Response Code : " + con.getResponseCode());
			System.out.println("Cipher Suite : " + con.getCipherSuite());
			System.out.println("\n");
		 
			Certificate[] certs = con.getServerCertificates();
			for(Certificate cert : certs){
			   System.out.println("Cert Type : " + cert.getType());
			   System.out.println("Cert Hash Code : " + cert.hashCode());
			   System.out.println("Cert Public Key Algorithm : " + cert.getPublicKey().getAlgorithm());
			   System.out.println("Cert Public Key Format : " + cert.getPublicKey().getFormat());
			   System.out.println("\n");
			}
		 
			} catch (SSLPeerUnverifiedException e) {
				e.printStackTrace();
			} catch (IOException e){
				e.printStackTrace();
			}
		 
		     }
		 
		   }
		 
		   private static void print_content(HttpsURLConnection con){
			if(con!=null){
		 
			try {
		 
			   System.out.println("****** Content of the URL ********");			
			   BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		 
			   String input;
		 
			   while ((input = br.readLine()) != null){
			      System.out.println(input);
			   }
			   br.close();
		 
			} catch (IOException e) {
			   e.printStackTrace();
			}
		 
	       }
		 
	   }
}
