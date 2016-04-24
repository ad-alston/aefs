grammar AccessPolicy;

@header {
    package abe.policy;
}

policy returns [ AccessPolicyNode node ]
 : attribute_expression EOF
 	{
 		$node = $attribute_expression.node;
 	}
 ;

attribute_expression returns [ AccessPolicyNode node ]
 : a=attribute_atom 
 	{ 
 		$node = $a.node;
 	}
 | b=attribute_expression AND c=attribute_expression 
 	{ 
 		$node = new AccessPolicyNode.AccessPolicyAndNode($b.node, $c.node);
 	}
 | d=attribute_expression OR e=attribute_expression
 	{
 		$node = new AccessPolicyNode.AccessPolicyOrNode($d.node, $e.node);
 	}
 | LPAREN f=attribute_expression RPAREN
 	{
 		$node = $f.node;
 	}
 ;
 
attribute_atom returns [ AccessPolicyNode node ]
 : ATTRIBUTE
 	{
 		$node = new AccessPolicyNode.AccessPolicyLeafNode($ATTRIBUTE.text);
 	} 
 ;

LPAREN: '(' ;
RPAREN: ')' ;

OR: 'OR' ;
AND: 'AND' ;

ATTRIBUTE : ('a'..'z'|'A'..'Z'|'_'|'0'..'9')+ ;

WHITESPACE : ( ' ' )+ -> skip ;