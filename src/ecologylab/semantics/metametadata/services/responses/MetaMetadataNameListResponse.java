package ecologylab.semantics.metametadata.services.responses;

import java.util.ArrayList;

import ecologylab.serialization.annotations.simpl_collection;

public class MetaMetadataNameListResponse
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

	public ArrayList<String> getMmdNameList() {
		return mmdNameList;
	}

}
