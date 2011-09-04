package ecologylab.semantics.metametadata.services.responses;

import ecologylab.serialization.ElementState;

/**
 * Container for the response when the authoring tool submits an MMD via post.
 * @author damaraju
 *
 */
public class MetaMetadataPostResponse extends ElementState
{
	
	@simpl_scalar
	boolean success;
	
	@simpl_scalar
	String message;

	
	public MetaMetadataPostResponse()
	{
		
	}
	
	public MetaMetadataPostResponse(Boolean Success, String message)
	{
		
	}
}
