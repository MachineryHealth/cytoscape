package fing.model;

/**
 * Please try to restrain from using this class.  This class was created so
 * that certain legacy applications would have an easier time using this
 * giny.model implementation.
 * @deprecated Use FingRootGraphFactory instead.
 * @see FingRootGraphFactory
 **/
public class FingExtensibleRootGraph extends FRootGraph
{

  public FingExtensibleRootGraph(FingNodeDepot nodeDepot,
                                 FingEdgeDepot edgeDepot)
  {
    super(nodeDepot, edgeDepot);
  }

}
