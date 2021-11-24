package au.csiro.redmatch.compiler;

/**
 * Visitor interface.
 *
 * @author Alejandro Metke-Jimenez
 */
public interface GrammarObjectVisitor {
  void visit(Attribute a);
  void visit(AttributeValue a);
  void visit(Body b);
  void visit(Value v);
  void visit(Condition c);
  void visit(Document d);
  void visit(Mapping m);
  void visit(RepeatsClause r);
  void visit(Resource r);
  void visit(Rule r);
  void visit(RuleList r);
  void visit(Schema s);
}
