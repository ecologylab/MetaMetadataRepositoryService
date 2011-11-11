package ecologylab.semantics.metametadata.services;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.cyberneko.CybernekoWrapper;
import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.semantics.metametadata.services.responses.MetaMetadataNameListResponse;
import ecologylab.semantics.metametadata.services.responses.MetaMetadataPostResponse;
import ecologylab.serialization.formatenums.*;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;


/**
 * Main servlet that has a single repo instance.
 * 
 * @author damaraju
 * 
 */
@SuppressWarnings("serial")
public class MMDJsonRepoServlet extends HttpServlet
{

	private static SimplTypesScope mmdTScope;
	private static String REPO_PATH_PREFIX = "../ecologylabSemantics/repository/powerUser/";
	private static MetaMetadataRepository	repo;
	static
	{
		//SimpleTypeScope.setGraphSwitch();
		
		SemanticsSessionScope infoCollector = new SemanticsSessionScope(RepositoryMetadataTranslationScope.get(), CybernekoWrapper.class);
		// new SemanticsSessionScope( RepositoryMetadataTranslationScope.get(), CybernekoWrapper.class);
		repo = infoCollector.getMetaMetadataRepository();
		
		mmdTScope = MetaMetadataTranslationScope.get();
		
		System.out.println("Performing restoration of children ");
		for (MetaMetadata globalMmd : repo.values())
			globalMmd.recursivelyRestoreChildComposite();
		for (Map<String, MetaMetadata> packageMmdScope : repo.getPackageMmdScopes().values())
		{
			for (MetaMetadata packageMmd : packageMmdScope.values())
				packageMmd.recursivelyRestoreChildComposite();
		}
		System.out.println("Done with restoration of children ");

	}

	/**
	 * 
	 */
	public MMDJsonRepoServlet()
	{

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		System.out.println("Got post");
		
		String mmdStringContent = request.getParameter("mmd");
		
		boolean success = false;
		String message = null;
		
		if(mmdStringContent != null && mmdStringContent.length() > 0)
		{
			System.out.println("mmdStringContent: " + mmdStringContent);
			try
			{
				MetaMetadata incomingMMD = (MetaMetadata) mmdTScope.deserialize(mmdStringContent, StringFormat.JSON);
				
				SimplTypesScope.serialize(incomingMMD, System.out, StringFormat.XML);
				
				System.out.println();
				String fileName = incomingMMD.getName() + ".xml";
				System.out.println("Successful deserialization, writing to file: " + REPO_PATH_PREFIX + fileName);
				File outputFile = new File(REPO_PATH_PREFIX + fileName);
				MetaMetadataRepository repoWrapper = new MetaMetadataRepository();
				repoWrapper.addMetaMetadata(incomingMMD);
				
				SimplTypesScope.serialize(repoWrapper, outputFile, Format.XML);
				
				message = "Successfully added to the repository, please re-run the MMD Compiler to use this code.";
				success = true;
			}
			catch (SIMPLTranslationException e)
			{
				message = "Failed translation of MMD";
				System.out.println(message);
				e.printStackTrace();
				
			}
		}
		
		try
		{
			MetaMetadataPostResponse mmdResponse = new MetaMetadataPostResponse(success, message);
			PrintWriter writer = response.getWriter();
			System.out.println("--Sending output: ");
			
			SimplTypesScope.serialize(mmdResponse, System.out, StringFormat.JSON);
			SimplTypesScope.serialize(mmdResponse, writer, StringFormat.JSON);
			writer.close();
			
			System.err.println("--End of output\n");
		}
		catch (IOException e){ e.printStackTrace(); } catch (SIMPLTranslationException e) {
			e.printStackTrace();
		}
		
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{

		response.addHeader("Access-Control-Allow-Origin", "*");

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		String purlParam = request.getParameter("purl");
		String mmdNameParam = request.getParameter("mmdName");
		String getMMDListParam = request.getParameter("getMMDList");
		
		
		
		PrintWriter printWriter;
		if (purlParam != null && purlParam.length() > 0)
		{
			ParsedURL purl = ParsedURL.getAbsolute(purlParam);
			sendMMDJsonForPurl(response, purl);
		}
		else if (getMMDListParam != null)
		{
			sendMMNameList(response);
		}
		else if (mmdNameParam != null && mmdNameParam.length() > 0)
		{
			sendMMDByName(response, mmdNameParam);
		}
		else
		{
			printWriter = response.getWriter();
			printWriter.append("Invalid request parameter");
			printWriter.close();
			// append("Invalid Purl");
		}

	}

	/**
	 * 
	 * @param response
	 * @param mmdNameParam
	 */
	private void sendMMDByName(HttpServletResponse response, String mmdNameParam)
	{
		MetaMetadata mmByName = repo.getMMByName(mmdNameParam);

		sendMM(response, mmByName);
	}

	/**
	 * 
	 * @param response
	 */
	private void sendMMNameList(HttpServletResponse response)
	{
		ArrayList<String> mmdNameList = repo.getMMNameList();
		if (mmdNameList != null && !mmdNameList.isEmpty())
		{

			try
			{
				MetaMetadataNameListResponse mmdList = new MetaMetadataNameListResponse(mmdNameList);
				PrintWriter writer = response.getWriter();
				SimplTypesScope.serialize(mmdList, writer, StringFormat.JSON);
			}
			catch (SIMPLTranslationException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param response
	 * @param purl
	 * @throws IOException
	 */
	private void sendMMDJsonForPurl(HttpServletResponse response, ParsedURL purl) throws IOException
	{
		MetaMetadata requestedMM = repo.getDocumentMM(purl);

		sendMM(response, requestedMM);

		return;
	}

	/**
	 * 
	 * @param response
	 * @param requestedMM
	 */
	private void sendMM(HttpServletResponse response, MetaMetadata requestedMM)
	{
		PrintWriter printWriter;

		try
		{

			if (requestedMM == null)
			{
				printWriter = response.getWriter();
				printWriter.append("No MM found");
				printWriter.close();
				return;
			}

			PrintWriter writer = response.getWriter();
			System.out.println("Serializing MMD " + requestedMM);
			//requestedMM.serialize(System.out, FORMAT.JSON);
			SimplTypesScope.serialize(requestedMM, writer, StringFormat.JSON);
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
