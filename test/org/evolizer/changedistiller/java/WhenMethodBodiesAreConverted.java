package org.evolizer.changedistiller.java;

import static org.hamcrest.CoreMatchers.is;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.evolizer.changedistiller.model.classifiers.SourceRange;
import org.evolizer.changedistiller.model.classifiers.java.JavaEntityType;
import org.evolizer.changedistiller.model.entities.SourceCodeEntity;
import org.evolizer.changedistiller.treedifferencing.Node;
import org.evolizer.changedistiller.util.CompilationUtils;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class WhenMethodBodiesAreConverted extends WhenASTsAreConverted {

    @Test
    public void assignmentShouldBeTransformed() throws Exception {
        fSnippet = "b = foo.bar();";
        prepareCompilation();
        transform();
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.ASSIGNMENT));
        assertTreeStringCorrectness();
        assertSourceRangeCorrectness();
    }

    @Test
    public void compoundAssignmentShouldBeTransformed() throws Exception {
        fSnippet = "b += foo.bar();";
        prepareCompilation();
        transform();
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.ASSIGNMENT));
        assertTreeStringCorrectness();
        assertSourceRangeCorrectness();
    }

    @Test
    public void postfixExpressionShouldBeTransformed() throws Exception {
        fSnippet = "b ++;";
        prepareCompilation();
        transform();
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.POSTFIX_EXPRESSION));
        assertTreeStringCorrectness();
        assertSourceRangeCorrectness();
    }

    @Test
    public void prefixExpressionShouldBeTransformed() throws Exception {
        fSnippet = "++ b;";
        prepareCompilation();
        transform();
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.PREFIX_EXPRESSION));
        assertTreeStringCorrectness();
        assertSourceRangeCorrectness();
    }

    @Test
    public void allocationExpressionShouldBeTransformed() throws Exception {
        fSnippet = "new Foo(bar);";
        prepareCompilation();
        transform();
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.CLASS_INSTANCE_CREATION));
        assertTreeStringCorrectness();
        assertSourceRangeCorrectness();
    }

    @Test
    public void qualifiedAllocationExpressionShouldBeTransformed() throws Exception {
        fSnippet = "foo.new Bar();";
        prepareCompilation();
        transform();
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.CLASS_INSTANCE_CREATION));
        assertTreeStringCorrectness();
        assertSourceRangeCorrectness();
    }

    @Test
    public void assertStatementWithoutExceptionArgumentShouldBeTransformed() throws Exception {
        fSnippet = "assert list.isEmpty();";
        prepareCompilation();
        transform();
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.ASSERT_STATEMENT));
        assertThat(getTreeString(), is("method { list.isEmpty() }"));
        assertSourceRangeCorrectness();
    }

    @Test
    public void assertStatementWithExceptionArgumentShouldBeTransformed() throws Exception {
        fSnippet = "assert list.isEmpty(): \"list not empty\";";
        prepareCompilation();
        transform();
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.ASSERT_STATEMENT));
        assertThat(getTreeString(), is("method { list.isEmpty():\"list not empty\" }"));
        assertSourceRangeCorrectness();
    }

    @Test
    public void breakStatementWithoutLabelShouldBeTransformed() throws Exception {
        fSnippet = "break;";
        prepareCompilation();
        transform();
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.BREAK_STATEMENT));
        assertThat(getTreeString(), is("method {  }"));
        assertSourceRangeCorrectness();
    }

    @Test
    public void breakStatementWithLabelShouldBeTransformed() throws Exception {
        fSnippet = "break foo;";
        prepareCompilation();
        transform();
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.BREAK_STATEMENT));
        assertThat(getTreeString(), is("method { foo }"));
        assertSourceRangeCorrectness();
    }

    @Test
    public void explicitConstructorCallShouldBeTransformed() throws Exception {
        fSnippet = "this(a);";
        prepareCompilation();
        transform();
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.CONSTRUCTOR_INVOCATION));
        assertTreeStringCorrectness();
        assertSourceRangeCorrectness();
    }

    @Test
    public void continueStatementWithoutLabelShouldBeTransformed() throws Exception {
        fSnippet = "continue;";
        prepareCompilation();
        transform();
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.CONTINUE_STATEMENT));
        assertThat(getTreeString(), is("method {  }"));
        assertSourceRangeCorrectness();
    }

    @Test
    public void continueStatementWithLabelShouldBeTransformed() throws Exception {
        fSnippet = "continue foo;";
        prepareCompilation();
        transform();
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.CONTINUE_STATEMENT));
        assertThat(getTreeString(), is("method { foo }"));
        assertSourceRangeCorrectness();
    }

    @Test
    public void doStatementShouldBeTransformed() throws Exception {
        fSnippet = "do { System.out.print('.'); } while (!list.isEmpty());";
        prepareCompilation();
        transform();
        assertThat(getFirstChild().getLabel(), is(JavaEntityType.DO_STATEMENT));
        assertThat(getTreeString(), is("method { (! list.isEmpty()) { System.out.print('.'); } }"));
        assertSourceRangeCorrectness(getFirstChild());
    }

    @Test
    public void foreachStatemenShouldBeTransformed() throws Exception {
        fSnippet = "for (String st : list) { System.out.print('.'); }";
        prepareCompilation();
        transform();
        assertThat(getFirstChild().getLabel(), is(JavaEntityType.FOREACH_STATEMENT));
        assertThat(getTreeString(), is("method { String st:list { System.out.print('.'); } }"));
        assertSourceRangeCorrectness(getFirstChild());
    }

    @Test
    public void forStatementWithConditionShouldBeTransformed() throws Exception {
        fSnippet = "for (int i = 0; i < list.size(); i++) { System.out.print('.'); }";
        prepareCompilation();
        transform();
        assertThat(getFirstChild().getLabel(), is(JavaEntityType.FOR_STATEMENT));
        assertThat(getTreeString(), is("method { (i < list.size()) { System.out.print('.'); } }"));
        assertSourceRangeCorrectness(getFirstChild());
    }

    @Test
    public void forStatementWithoutConditionShouldBeTransformed() throws Exception {
        fSnippet = "for (;;) { System.out.print('.'); }";
        prepareCompilation();
        transform();
        assertThat(getFirstChild().getLabel(), is(JavaEntityType.FOR_STATEMENT));
        assertThat(getTreeString(), is("method {  { System.out.print('.'); } }"));
        assertSourceRangeCorrectness(getFirstChild());
    }

    @Test
    public void ifStatementShouldBeTransformed() throws Exception {
        fSnippet = "if (list.isEmpty()) { System.out.print(\"empty\"); } else { System.out.print(\"not empty\"); }";
        prepareCompilation();
        transform();
        assertThat(getFirstChild().getLabel(), is(JavaEntityType.IF_STATEMENT));
        assertThat(((Node) getFirstChild().getFirstChild()).getLabel(), is(JavaEntityType.THEN_STATEMENT));
        assertThat(((Node) getFirstChild().getLastChild()).getLabel(), is(JavaEntityType.ELSE_STATEMENT));
        assertThat(
                getTreeString(),
                is("method { list.isEmpty() { list.isEmpty() { System.out.print(\"empty\"); },list.isEmpty() { System.out.print(\"not empty\"); } } }"));
        assertSourceRangeCorrectness(getFirstChild());
    }

    @Test
    public void ifStatementWithoutElseShouldBeTransformed() throws Exception {
        fSnippet = "if (list.isEmpty()) { System.out.print(\"empty\"); }";
        prepareCompilation();
        transform();
        assertThat(getFirstChild().getLabel(), is(JavaEntityType.IF_STATEMENT));
        assertThat(((Node) getFirstChild().getFirstChild()).getLabel(), is(JavaEntityType.THEN_STATEMENT));
        assertThat(getTreeString(), is("method { list.isEmpty() { list.isEmpty() { System.out.print(\"empty\"); } } }"));
        assertSourceRangeCorrectness(getFirstChild());
    }

    @Test
    public void labeledStatementShouldBeTransformed() throws Exception {
        fSnippet = "label: a = 24;";
        prepareCompilation();
        transform();
        assertThat(getFirstChild().getLabel(), is(JavaEntityType.LABELED_STATEMENT));
        assertThat(getTreeString(), is("method { label { a = 24; } }"));
        assertSourceRangeCorrectness(getFirstChild());
    }

    @Test
    public void localDeclarationShouldBeTransformed() throws Exception {
        fSnippet = "float a = 24.0f;";
        prepareCompilation();
        transform();
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.VARIABLE_DECLARATION_STATEMENT));
        assertTreeStringCorrectness();
        assertSourceRangeCorrectness();
    }

    @Test
    public void messageSendShouldBeTransformed() throws Exception {
        fSnippet = "foo.bar(anInteger);";
        prepareCompilation();
        transform();
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.METHOD_INVOCATION));
        assertTreeStringCorrectness();
        assertSourceRangeCorrectness();
    }

    @Test
    public void emptyReturnStatementShouldBeTransformed() throws Exception {
        fSnippet = "return;";
        prepareCompilation();
        transform();
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.RETURN_STATEMENT));
        assertThat(getTreeString(), is("method {  }"));
        assertSourceRangeCorrectness();
    }

    @Test
    public void returnStatementShouldBeTransformed() throws Exception {
        fSnippet = "return Math.min(a, b);";
        prepareCompilation();
        transform();
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.RETURN_STATEMENT));
        assertThat(getTreeString(), is("method { Math.min(a, b); }"));
        assertSourceRangeCorrectness();
    }

    @Test
    public void switchStatementShouldBeTransformed() throws Exception {
        fSnippet = "switch (foo) { case ONE: a = 1; break; default: a = 2; }";
        prepareCompilation();
        transform();
        assertThat(getFirstChild().getLabel(), is(JavaEntityType.SWITCH_STATEMENT));
        assertThat(getFirstLeaf().getLabel(), is(JavaEntityType.SWITCH_CASE));
        assertThat(getTreeString(), is("method { foo { ONE,a = 1;,,default,a = 2; } }"));
        assertSourceRangeCorrectness(getFirstChild());
    }

    @Test
    public void synchronizedStatementShouldBeTransformed() throws Exception {
        fSnippet = "synchronized(foo) { foo.bar(b); }";
        prepareCompilation();
        transform();
        assertThat(getFirstChild().getLabel(), is(JavaEntityType.SYNCHRONIZED_STATEMENT));
        assertThat(getTreeString(), is("method { foo { foo.bar(b); } }"));
        assertSourceRangeCorrectness(getFirstChild());
    }

    @Test
    public void throwStatementShouldBeTransformed() throws Exception {
        fSnippet = "throw new RuntimeException(e);";
        prepareCompilation();
        transform();
        assertThat(getFirstChild().getLabel(), is(JavaEntityType.THROW_STATEMENT));
        assertThat(getTreeString(), is("method { new RuntimeException(e); }"));
        assertSourceRangeCorrectness(getFirstChild());
    }

    @Test
    public void tryStatementShouldBeTransformed() throws Exception {
        fSnippet =
                "try { foo.bar(e); } catch (IOException e) { return 2; } catch (Exception e) { return 3; } finally { cleanup(); }";
        prepareCompilation();
        transform();
        assertThat(getFirstChild().getLabel(), is(JavaEntityType.TRY_STATEMENT));
        assertThat(
                ((Node) ((Node) getFirstChild().getFirstChild()).getNextSibling()).getLabel(),
                is(JavaEntityType.CATCH_CLAUSES));
        assertThat(
                ((Node) ((Node) getFirstChild().getFirstChild()).getNextSibling().getFirstChild()).getLabel(),
                is(JavaEntityType.CATCH_CLAUSE));
        assertThat(((Node) getFirstChild().getLastChild()).getLabel(), is(JavaEntityType.FINALLY));
        assertThat(
                getTreeString(),
                is("method {  {  { foo.bar(e); }, { IOException { 2; },Exception { 3; } }, { cleanup(); } } }"));
        assertSourceRangeCorrectness(getFirstChild());
    }

    @Test
    public void tryStatementWithoutCatchClausesShouldBeTransformed() throws Exception {
        fSnippet = "try { foo.bar(e); } finally { cleanup(); }";
        prepareCompilation();
        transform();
        assertThat(((Node) getFirstChild().getLastChild()).getLabel(), is(JavaEntityType.FINALLY));
        assertThat(getTreeString(), is("method {  {  { foo.bar(e); }, { cleanup(); } } }"));
        assertSourceRangeCorrectness(getFirstChild());
    }

    @Test
    public void tryStatementWithoutFinallyShouldBeTransformed() throws Exception {
        fSnippet = "try { foo.bar(e); } catch (IOException e) { return 2; } catch (Exception e) { return 3; }";
        prepareCompilation();
        transform();
        assertThat(
                ((Node) ((Node) getFirstChild().getFirstChild()).getNextSibling()).getLabel(),
                is(JavaEntityType.CATCH_CLAUSES));
        assertThat(
                ((Node) ((Node) getFirstChild().getFirstChild()).getNextSibling().getFirstChild()).getLabel(),
                is(JavaEntityType.CATCH_CLAUSE));
        assertThat(getTreeString(), is("method {  {  { foo.bar(e); }, { IOException { 2; },Exception { 3; } } } }"));
        assertSourceRangeCorrectness(getFirstChild());
    }

    @Test
    public void whileStatementWithoutFinallyShouldBeTransformed() throws Exception {
        fSnippet = "while (i < a.length) { System.out.print('.'); }";
        prepareCompilation();
        transform();
        assertThat(getFirstChild().getLabel(), is(JavaEntityType.WHILE_STATEMENT));
        assertThat(getTreeString(), is("method { (i < a.length) { System.out.print('.'); } }"));
        assertSourceRangeCorrectness(getFirstChild());
    }

    private void assertTreeStringCorrectness() {
        assertThat(getTreeString(), is(getMethodString()));
    }

    private void assertSourceRangeCorrectness() {
        assertSourceRangeCorrectness(getFirstLeaf());
    }

    private void assertSourceRangeCorrectness(Node node) {
        SourceCodeEntity entity = node.getEntity();
        String source = fCompilation.getSource().substring(entity.getStartPosition(), entity.getEndPosition() + 1);
        assertThat(source, is(fSnippet));
    }

    private String getMethodString() {
        return "method { " + fSnippet + " }";
    }

    private void transform() {
        fRoot = new Node(new SourceCodeEntity("method", JavaEntityType.METHOD, new SourceRange()));
        AbstractMethodDeclaration method = CompilationUtils.findMethod(fCompilation.getCompilationUnit(), "method");
        JavaMethodBodyConverter bodyT =
                new JavaMethodBodyConverter(fRoot, method, null, fCompilation.getScanner(), new JavaASTHelper());
        method.traverse(bodyT, (ClassScope) null);
    }

    @Override
    protected String getSourceCodeWithSnippets(String... snippets) {
        StringBuilder src = new StringBuilder("public class Foo { ");
        src.append("public void method() { ");
        for (String statement : snippets) {
            src.append(statement).append(' ');
        }
        src.append("} }");
        return src.toString();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void assertThat(Object actual, Matcher matcher) {
        MatcherAssert.assertThat(actual, matcher);
    }

}
