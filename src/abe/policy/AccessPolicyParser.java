// Generated from AccessPolicy.g by ANTLR 4.5.3

    package abe.policy;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class AccessPolicyParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LPAREN=1, RPAREN=2, OR=3, AND=4, ATTRIBUTE=5, WHITESPACE=6;
	public static final int
		RULE_policy = 0, RULE_attribute_expression = 1, RULE_attribute_atom = 2;
	public static final String[] ruleNames = {
		"policy", "attribute_expression", "attribute_atom"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'('", "')'", "'OR'", "'AND'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "LPAREN", "RPAREN", "OR", "AND", "ATTRIBUTE", "WHITESPACE"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "AccessPolicy.g"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public AccessPolicyParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class PolicyContext extends ParserRuleContext {
		public AccessPolicyNode node;
		public Attribute_expressionContext attribute_expression;
		public Attribute_expressionContext attribute_expression() {
			return getRuleContext(Attribute_expressionContext.class,0);
		}
		public TerminalNode EOF() { return getToken(AccessPolicyParser.EOF, 0); }
		public PolicyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_policy; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AccessPolicyListener ) ((AccessPolicyListener)listener).enterPolicy(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AccessPolicyListener ) ((AccessPolicyListener)listener).exitPolicy(this);
		}
	}

	public final PolicyContext policy() throws RecognitionException {
		PolicyContext _localctx = new PolicyContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_policy);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(6);
			((PolicyContext)_localctx).attribute_expression = attribute_expression(0);
			setState(7);
			match(EOF);

			 		((PolicyContext)_localctx).node =  ((PolicyContext)_localctx).attribute_expression.node;
			 	
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Attribute_expressionContext extends ParserRuleContext {
		public AccessPolicyNode node;
		public Attribute_expressionContext b;
		public Attribute_expressionContext d;
		public Attribute_atomContext a;
		public Attribute_expressionContext f;
		public Attribute_expressionContext c;
		public Attribute_expressionContext e;
		public Attribute_atomContext attribute_atom() {
			return getRuleContext(Attribute_atomContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(AccessPolicyParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(AccessPolicyParser.RPAREN, 0); }
		public List<Attribute_expressionContext> attribute_expression() {
			return getRuleContexts(Attribute_expressionContext.class);
		}
		public Attribute_expressionContext attribute_expression(int i) {
			return getRuleContext(Attribute_expressionContext.class,i);
		}
		public TerminalNode AND() { return getToken(AccessPolicyParser.AND, 0); }
		public TerminalNode OR() { return getToken(AccessPolicyParser.OR, 0); }
		public Attribute_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attribute_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AccessPolicyListener ) ((AccessPolicyListener)listener).enterAttribute_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AccessPolicyListener ) ((AccessPolicyListener)listener).exitAttribute_expression(this);
		}
	}

	public final Attribute_expressionContext attribute_expression() throws RecognitionException {
		return attribute_expression(0);
	}

	private Attribute_expressionContext attribute_expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Attribute_expressionContext _localctx = new Attribute_expressionContext(_ctx, _parentState);
		Attribute_expressionContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_attribute_expression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(19);
			switch (_input.LA(1)) {
			case ATTRIBUTE:
				{
				setState(11);
				((Attribute_expressionContext)_localctx).a = attribute_atom();
				 
				 		((Attribute_expressionContext)_localctx).node =  ((Attribute_expressionContext)_localctx).a.node;
				 	
				}
				break;
			case LPAREN:
				{
				setState(14);
				match(LPAREN);
				setState(15);
				((Attribute_expressionContext)_localctx).f = attribute_expression(0);
				setState(16);
				match(RPAREN);

				 		((Attribute_expressionContext)_localctx).node =  ((Attribute_expressionContext)_localctx).f.node;
				 	
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(33);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(31);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
					case 1:
						{
						_localctx = new Attribute_expressionContext(_parentctx, _parentState);
						_localctx.b = _prevctx;
						_localctx.b = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_attribute_expression);
						setState(21);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(22);
						match(AND);
						setState(23);
						((Attribute_expressionContext)_localctx).c = attribute_expression(4);
						 
						           		((Attribute_expressionContext)_localctx).node =  new AccessPolicyNode.AccessPolicyAndNode(((Attribute_expressionContext)_localctx).b.node, ((Attribute_expressionContext)_localctx).c.node);
						           	
						}
						break;
					case 2:
						{
						_localctx = new Attribute_expressionContext(_parentctx, _parentState);
						_localctx.d = _prevctx;
						_localctx.d = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_attribute_expression);
						setState(26);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(27);
						match(OR);
						setState(28);
						((Attribute_expressionContext)_localctx).e = attribute_expression(3);

						           		((Attribute_expressionContext)_localctx).node =  new AccessPolicyNode.AccessPolicyOrNode(((Attribute_expressionContext)_localctx).d.node, ((Attribute_expressionContext)_localctx).e.node);
						           	
						}
						break;
					}
					} 
				}
				setState(35);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class Attribute_atomContext extends ParserRuleContext {
		public AccessPolicyNode node;
		public Token ATTRIBUTE;
		public TerminalNode ATTRIBUTE() { return getToken(AccessPolicyParser.ATTRIBUTE, 0); }
		public Attribute_atomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attribute_atom; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AccessPolicyListener ) ((AccessPolicyListener)listener).enterAttribute_atom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AccessPolicyListener ) ((AccessPolicyListener)listener).exitAttribute_atom(this);
		}
	}

	public final Attribute_atomContext attribute_atom() throws RecognitionException {
		Attribute_atomContext _localctx = new Attribute_atomContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_attribute_atom);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(36);
			((Attribute_atomContext)_localctx).ATTRIBUTE = match(ATTRIBUTE);

			 		((Attribute_atomContext)_localctx).node =  new AccessPolicyNode.AccessPolicyLeafNode((((Attribute_atomContext)_localctx).ATTRIBUTE!=null?((Attribute_atomContext)_localctx).ATTRIBUTE.getText():null));
			 	
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 1:
			return attribute_expression_sempred((Attribute_expressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean attribute_expression_sempred(Attribute_expressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 3);
		case 1:
			return precpred(_ctx, 2);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\b*\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3\26"+
		"\n\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\7\3\"\n\3\f\3\16\3%\13\3"+
		"\3\4\3\4\3\4\3\4\2\3\4\5\2\4\6\2\2)\2\b\3\2\2\2\4\25\3\2\2\2\6&\3\2\2"+
		"\2\b\t\5\4\3\2\t\n\7\2\2\3\n\13\b\2\1\2\13\3\3\2\2\2\f\r\b\3\1\2\r\16"+
		"\5\6\4\2\16\17\b\3\1\2\17\26\3\2\2\2\20\21\7\3\2\2\21\22\5\4\3\2\22\23"+
		"\7\4\2\2\23\24\b\3\1\2\24\26\3\2\2\2\25\f\3\2\2\2\25\20\3\2\2\2\26#\3"+
		"\2\2\2\27\30\f\5\2\2\30\31\7\6\2\2\31\32\5\4\3\6\32\33\b\3\1\2\33\"\3"+
		"\2\2\2\34\35\f\4\2\2\35\36\7\5\2\2\36\37\5\4\3\5\37 \b\3\1\2 \"\3\2\2"+
		"\2!\27\3\2\2\2!\34\3\2\2\2\"%\3\2\2\2#!\3\2\2\2#$\3\2\2\2$\5\3\2\2\2%"+
		"#\3\2\2\2&\'\7\7\2\2\'(\b\4\1\2(\7\3\2\2\2\5\25!#";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}