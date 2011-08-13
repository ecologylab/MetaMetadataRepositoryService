package ecologylab.semantics.metametadata.services;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.tidy.Tidy;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.MetaMetadataRepositoryInit;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataCollectionField;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataScalarField;
import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.semantics.metametadata.NestedMetaMetadataFieldTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.ElementState.FORMAT;


@SuppressWarnings("serial")
public class MMDJsonRepoServlet extends HttpServlet
{ 

	private static TranslationScope mmdTScope;
	private static MetaMetadataRepository repo;
	static
	{
		TranslationScope.setGraphSwitch(); 
		SemanticsSessionScope infoCollector = new SemanticsSessionScope(RepositoryMetadataTranslationScope.get(), Tidy.class);
		repo = infoCollector.getMetaMetadataRepository();
	}

	public MMDJsonRepoServlet()
	{

	}
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
  		
  		response.addHeader("Header set Access-Control-Allow-Origin", "*");
  		//As per gaurav, this header is required for the authoring tool.
  		
      response.setContentType("text/html");
      response.setStatus(HttpServletResponse.SC_OK);
      
			//writer.println("<h1>Hello World !</h1>");
      //writer.println("session=" + request.getSession(true).getId());
      String purlParam = request.getParameter("purl");
  		ParsedURL purl = ParsedURL.getAbsolute(purlParam);
  		PrintWriter printWriter;
  		if(purl == null)
  		{
  			printWriter = response.getWriter();
				printWriter.append("Invalid Purl parameter");
				printWriter.close();
  			 //append("Invalid Purl");
  			return;
  		}
  		
      
  		MetaMetadata requestedMM = repo.getDocumentMM(purl);
  		if(requestedMM == null)
  		{
  			printWriter = response.getWriter();
  			printWriter.append("No MM found for purl: " + purl);
  			printWriter.close();
  			return;
  		}
  		try
			{
  			//TranslationScope.setGraphSwitch(); 

    		OutputStream writer = response.getOutputStream();
      	System.out.println("Serializing MMD for purl: " + purl);
      	requestedMM.serialize(System.out, FORMAT.JSON);
      	requestedMM.serialize(writer, FORMAT.JSON);
			}
			catch (SIMPLTranslationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
  
  }
}
