package abe.policy;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import abe.policy.AccessPolicyParser.PolicyContext;

/**
 * Provides static functionality required to parse access policy
 * strings and produce an equivalent LSSSTree.
 * 
 * @author Aubrey Alston (ada2145@columbia.edu)
 */
public class AccessPolicyInterpreter {
	
	/**
	 * Returns a the given access policy in the form of a tree.
	 * @param policyString policy to translate
	 */
	public static AccessPolicyNode parsePolicy(String policyString){
		if(policyString == null){
			return null;
		}
		
		try{
			// Parse the passed string
			AccessPolicyLexer lexer = 
					new AccessPolicyLexer(new ANTLRInputStream(policyString));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			
			AccessPolicyParser parser = new AccessPolicyParser(tokens);
			parser.setBuildParseTree(true);
			parser.setErrorHandler(new BailErrorStrategy());
			
			PolicyContext ctx = parser.policy();
			AccessPolicyNode node = ctx.node;
			
			return node;
		} catch(ParseCancellationException e){
			return null;
		}
	}
	
}
