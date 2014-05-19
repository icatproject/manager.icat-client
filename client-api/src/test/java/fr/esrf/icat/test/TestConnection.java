package fr.esrf.icat.test;

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

	private final static String ICAT_ISIS_SERVER = "https://icatisis.esc.rl.ac.uk";
	
	private final static String ICAT_ESRF_SERVER = "https://wwws.esrf.fr/icat/";

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
		
		testServer(ICAT_ISIS_SERVER);
		
		testServer(ICAT_ESRF_SERVER);
	}

	private static void testServer(final String serverURLString) {
		System.out.println();
		System.out.println("****** Connecting to " + serverURLString + " ********");			
		URL serverURL;
		try {
			serverURL = new URL(serverURLString);
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
					System.out.println("****** SSL parameters ********");			
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
			 
				   System.out.println("****** Content ********");			
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
