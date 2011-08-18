package ecologylab.semantics.metametadata.services.responses;

import java.util.ArrayList;

import ecologylab.serialization.ElementState;

public class MetaMetadataNameListResponse extends ElementState
{
	
	@simpl_collection("mmd_name_list")
	private ArrayList<String> mmdNameList;
	
	public MetaMetadataNameListResponse()
	{

	}
	
	public MetaMetadataNameListResponse(ArrayList<String> mmdNameList)
	{
		this.mmdNameList = mmdNameList;
	}
}
