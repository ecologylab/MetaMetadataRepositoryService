package ecologylab.semantics.metametadata.services;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.tidy.Tidy;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.cyberneko.CybernekoWrapper;
import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.semantics.metametadata.services.responses.MetaMetadataNameListResponse;
import ecologylab.semantics.metametadata.services.responses.MetaMetadataPostResponse;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.ElementState.FORMAT;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;

/**
 * Main servlet that has a single repo instance.
 * 
 * @author damaraju
 * 
 */
@SuppressWarnings("serial")
public class MMDJsonRepoServlet extends HttpServlet
{

	private static TranslationScope				mmdTScope;
	private static String REPO_PATH_PREFIX = "../ecologylabSemantics/repository/powerUser/";
	private static MetaMetadataRepository	repo;
	static
	{
		//TranslationScope.setGraphSwitch();
		
		SemanticsSessionScope infoCollector = new SemanticsSessionScope(RepositoryMetadataTranslationScope.get(), Tidy.class);
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
				MetaMetadata incomingMMD = (MetaMetadata) mmdTScope.deserializeCharSequence(mmdStringContent, FORMAT.JSON);
				incomingMMD.serialize(System.out);
				System.out.println();
				String fileName = incomingMMD.getName() + ".xml";
				System.out.println("Successful deserialization, writing to file: " + REPO_PATH_PREFIX + fileName);
				File outputFile = new File(REPO_PATH_PREFIX + fileName);
				MetaMetadataRepository repoWrapper = new MetaMetadataRepository();
				repoWrapper.addMetaMetadata(incomingMMD);
				
				repoWrapper.serialize(outputFile);
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
			OutputStream writer = response.getOutputStream();
			System.out.println("--Sending output: ");
			mmdResponse.serialize(System.out, FORMAT.JSON);
			System.err.println("--End of output\n");
			
			mmdResponse.serialize(writer, FORMAT.JSON);
			writer.close();
		}
		catch (SIMPLTranslationException e){ e.printStackTrace(); }
		catch (IOException e){ e.printStackTrace(); }
		
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
				OutputStream writer = response.getOutputStream();
				mmdList.serialize(writer, FORMAT.JSON);
			}
			catch (SIMPLTranslationException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
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

			OutputStream writer = response.getOutputStream();
			System.out.println("Serializing MMD " + requestedMM);
			// requestedMM.serialize(System.out, FORMAT.JSON);
			requestedMM.serialize(writer, FORMAT.JSON);
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
