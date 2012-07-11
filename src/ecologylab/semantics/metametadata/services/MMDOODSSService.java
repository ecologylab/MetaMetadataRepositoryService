package ecologylab.semantics.metametadata.services;

import java.io.IOException;
import java.net.InetAddress;

import ecologylab.collections.Scope;
import ecologylab.net.NetTools;
import ecologylab.oodss.distributed.server.varieties.HttpPostServer;
import ecologylab.oodss.messages.DefaultServicesTranslations;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.cyberneko.CybernekoWrapper;
import ecologylab.semantics.downloaders.oodss.LookupMmdRequest;
import ecologylab.semantics.downloaders.oodss.LookupMmdResponse;
import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.serialization.SimplTypesScope;

public class MMDOODSSService {
	
	private static final int idleTimeout = -1;
	private static final int MTU = 30000;
	
	public static void main(String[] args) throws IOException
	{
		/*
		 *  get base translations with static accessor
		 */
		SimplTypesScope baseServices = DefaultServicesTranslations.get();
		
		/* 
		 * Classes that must be translated by the translation scope
		 * in order for the server to communicate w/ the client
		 */
		Class[] lookupMMDClasses = { LookupMmdRequest.class,
				 LookupMmdResponse.class };
		
		/*
		 * compose translations, to create the “histEchoTrans”
		 * space inheriting the base translations
		 */
		SimplTypesScope lookupMMDTranslations = SimplTypesScope.get("lookupMMDTrans",
				baseServices, lookupMMDClasses);	
		
		/* 
		 * Creates a scope for the server to use as an application scope
		 * as well as individual client session scopes.
		 */
		SemanticsSessionScope infoCollector = new SemanticsSessionScope(RepositoryMetadataTranslationScope.get(), CybernekoWrapper.class);
		
		/* 
		 * Initialize the ECHO_HISTORY registry in the application scope
		 * so that the performService(...) of HistoryEchoRequest modifies
		 * the history in the application scope.
		 */
		
		/* Acquire an array of all local ip-addresses */
		InetAddress[] locals = NetTools.getAllInetAddressesForLocalhost();
		
		/* 
		 * Create the server and start the server so that it can
		 * accept incoming connections.
		 */
		HttpPostServer mmdServer = new HttpPostServer(2107,
															locals,
															lookupMMDTranslations,
															infoCollector,
															idleTimeout,
															MTU);
		mmdServer.start();
	}
}
