import junit.framework.*;
import java.util.*;

import cern.colt.bitvector.BitVector;

public class OrTest extends AbstractNodeTest
{
    OrFactorNode f;
    List x; // list of EdgeMessages
    
    private double ep = OrFactorNode.epOR; // epsilon
   
    protected void setUp()
    {
        f = OrFactorNode.getInstance();

        x = new ArrayList();
        x.add(pt2em(createPathActive(.4, .6)));
        x.add(pt2em(createPathActive(.85, .15))); // target node
        x.add(pt2em(createPathActive(.2, .8)));
        x.add(pt2em(createPathActive(.3, .7)));

    }

    public void testMaxProductEdge1() throws AlgorithmException
    {
        ProbTable pt = f.maxProduct(x, 1, VariableNode.createPathActive(1));

        double p0 = Math.max(Math.max(.4*.8*.7, .6*.2*.7), .6*.8*.3);
        double p1 = .6*.8*.7;

        checkProbs10(pt, p1, p0);
    }


    public void testMaxProductEdge3() throws AlgorithmException
    {
        ProbTable pt = f.maxProduct(x, 3, VariableNode.createPathActive(1));

        double p0 = .6*.85*.8;
        double p1 = .6*.85*.8;
        
        checkProbs10(pt, p1, p0);
    }

}
