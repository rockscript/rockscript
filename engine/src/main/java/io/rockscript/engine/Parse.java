/*
 * Copyright ©2017, RockScript.io. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.rockscript.engine;

import io.rockscript.parser.ECMAScriptLexer;
import io.rockscript.parser.ECMAScriptParser;
import io.rockscript.parser.ECMAScriptParser.*;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Use
 *
 * EngineScript engineScript = Parse.parse(String);
 *
 * or
 *
 * Parse parse = Parse.create(String);
 * if (!parse.hasErrors()) {
 *   // use the engineScript
 *   parse.getEngineScript();
 * } else {
 *   // log
 *   parse.getErrors();
 * }
 */
public class Parse {

  static Logger log = LoggerFactory.getLogger(Parse.class);

  EngineScript engineScript;      // initialized in enterScript
  List<String> errors;
  int nextScriptElementId = 1;

  /** The parsed engineScript */
  public EngineScript getEngineScript() {
    return engineScript;
  }

  public List<String> getErrors() {
    return errors;
  }

  public String getErrorText() {
    return errors
        .stream()
        .collect(Collectors.joining("\n"));
  }

  public Parse throwIfError() {
    if (hasErrors()) {
      throw new RuntimeException(getErrorText());
    }
    return this;
  }

  public boolean hasErrors() {
    return errors!=null && !errors.isEmpty();
  }

  public void addError(String error) {
    if (errors==null) {
      errors = new ArrayList<>();
    }
    errors.add(error);
  }

  /** Convenience method that creates the Parse, parses
   * the given scriptText and returns the engineScript.
   * @throws RuntimeException if there was an error parsing the scriptText */
  public static EngineScript parse(String scriptText) {
    Parse scriptBuilder = create(scriptText);
    scriptBuilder.throwIfError();
    return scriptBuilder.engineScript;
  }

  /** Creates a Parse and parses the given scriptText.
   * From the returned Parse you can check the
   * {@link #getErrors()} and and get {@link #getEngineScript()}. */
  public static Parse create(String scriptText) {
    ECMAScriptLexer l = new ECMAScriptLexer(new ANTLRInputStream(scriptText));
    ECMAScriptParser p = new ECMAScriptParser(new CommonTokenStream(l));
    ProgramContext programContext = p.program();
    Parse parse = new Parse();
    parse.parseScript(programContext, scriptText);
    return parse;
  }

  private void parseScript(ProgramContext programContext, String scriptText) {
    this.engineScript = new EngineScript(null, createLocation(programContext));
    SourceElementsContext sourceElementsContext = programContext.sourceElements();
    List<SourceElement> sourceElements = parseSourceElements(sourceElementsContext);
    this.engineScript.setSourceElements(sourceElements);
    this.engineScript.initializeScriptElements(scriptText);
  }

  private List<SourceElement> parseSourceElements(SourceElementsContext sourceElementsContext) {
    List<SourceElement> sourceElements = new ArrayList<>();
    if (sourceElementsContext!=null) {
      List<SourceElementContext> sourceElementContexts = sourceElementsContext.sourceElement();
      for (SourceElementContext sourceElementContext: sourceElementContexts) {
        SourceElement sourceElement = parseSourceElement(sourceElementContext);
        sourceElements.add(sourceElement);
      }
    }
    return sourceElements;
  }

  private SourceElement parseSourceElement(SourceElementContext sourceElementContext) {
    StatementContext statementContext = sourceElementContext.statement();
    if (statementContext!=null) {
      return parseStatement(statementContext);
    }
    return parseFunctionDeclaration(sourceElementContext.functionDeclaration());
  }

  private Statement parseStatement(StatementContext statementContext) {
    VariableStatementContext variableStatementContext = statementContext.variableStatement();
    if (variableStatementContext!=null) {
      VariableDeclarationListContext variableDeclarationListContext = variableStatementContext.variableDeclarationList();
      return parseVariableDeclarationList(variableDeclarationListContext);
    }

    ExpressionStatementContext expressionStatementContext = statementContext.expressionStatement();
    if (expressionStatementContext!=null) {
      ExpressionSequenceContext expressionSequenceContext = expressionStatementContext.expressionSequence();
      List<SingleExpressionContext> singleExpressionContexts = expressionSequenceContext.singleExpression();

      ExpressionStatement expressionStatement = new ExpressionStatement(createNextScriptElementId(), createLocation(statementContext));

      List<SingleExpression> singleExpressions = new ArrayList<>();
      for (SingleExpressionContext singleExpressionContext: singleExpressionContexts) {
        SingleExpression singleExpression = parseSingleExpression(singleExpressionContext);
        singleExpressions.add(singleExpression);
      }

      expressionStatement.setSingleExpressions(singleExpressions);
      return expressionStatement;
    }

    throw newUnsupportedException("statement", statementContext);
  }

  private SourceElement parseFunctionDeclaration(FunctionDeclarationContext functionDeclarationContext) {
    throw newUnsupportedException("functionDeclaration", functionDeclarationContext);
  }

  private Statement parseVariableDeclarationList(VariableDeclarationListContext variableDeclarationListContext) {
    List<VariableDeclarationContext> variableDeclarationContexts = variableDeclarationListContext.variableDeclaration();
    if (variableDeclarationContexts.size()==1) {
      // This is an optimisation to skip the list if there is exactly one variable declaration
      VariableDeclarationContext variableDeclarationContext = variableDeclarationContexts.get(0);
      VariableDeclaration variableDeclaration = parseVariableDeclaration(variableDeclarationContext);
      return variableDeclaration;
    }
    VariableDeclarationList variableDeclarationList = new VariableDeclarationList(createNextScriptElementId(), createLocation(variableDeclarationListContext));
    List<VariableDeclaration> variableDeclarations = new ArrayList<>();
    for (VariableDeclarationContext variableDeclarationContext: variableDeclarationContexts) {
      VariableDeclaration variableDeclaration = parseVariableDeclaration(variableDeclarationContext);
      variableDeclarations.add(variableDeclaration);
    }
    variableDeclarationList.setVariableDeclarations(variableDeclarations);
    return variableDeclarationList;
  }

  private VariableDeclaration parseVariableDeclaration(VariableDeclarationContext variableDeclarationContext) {
    VariableDeclaration variableDeclarationStatement = new VariableDeclaration(createNextScriptElementId(), createLocation(variableDeclarationContext));
    String variableName = variableDeclarationContext.Identifier().getText();
    variableDeclarationStatement.setVariableName(variableName);
    InitialiserContext initialiser = variableDeclarationContext.initialiser();
    if (initialiser!=null) {
      SingleExpression initialiserExpression = parseSingleExpression(initialiser.singleExpression());
      variableDeclarationStatement.setInitialiser(initialiserExpression);
    }
    return variableDeclarationStatement;
  }

  private SingleExpression parseSingleExpression(SingleExpressionContext singleExpressionContext) {
    if (singleExpressionContext instanceof ArgumentsExpressionContext) {
      return parseArgumentsExpression((ArgumentsExpressionContext) singleExpressionContext);

    } else if (singleExpressionContext instanceof MemberDotExpressionContext) {
      return parseMemberDotExpression((MemberDotExpressionContext) singleExpressionContext);

    } else if (singleExpressionContext instanceof IdentifierExpressionContext) {
      return parseIdentifier((IdentifierExpressionContext) singleExpressionContext);

    } else if (singleExpressionContext instanceof LiteralExpressionContext) {
      return parseLiteralExpression((LiteralExpressionContext)singleExpressionContext);

    } else if (singleExpressionContext instanceof ObjectLiteralExpressionContext) {
      return parseObjectLiteralExpression((ObjectLiteralExpressionContext)singleExpressionContext);

    }
    throw newUnsupportedException("singleExpression", singleExpressionContext);
  }

  private SingleExpression parseObjectLiteralExpression(ObjectLiteralExpressionContext objectLiteralExpressionContext) {
    ObjectLiteralExpression objectLiteralExpression = new ObjectLiteralExpression(createNextScriptElementId(), createLocation(objectLiteralExpressionContext));
    ObjectLiteralContext objectLiteralContext = objectLiteralExpressionContext.objectLiteral();
    PropertyNameAndValueListContext propertyNameAndValueListContext = objectLiteralContext.propertyNameAndValueList();
    List<PropertyAssignmentContext> propertyAssignmentContexts = propertyNameAndValueListContext.propertyAssignment();
    for (PropertyAssignmentContext propertyAssignmentContext: propertyAssignmentContexts) {
      log.debug(propertyAssignmentContext.getText());
      PropertyNameContext propertyNameContext = (PropertyNameContext) propertyAssignmentContext.getChild(0);
      String propertyName = parsePropertyName(propertyNameContext);
      SingleExpressionContext valueContext = (SingleExpressionContext) propertyAssignmentContext.getChild(2);
      SingleExpression valueExpression = parseSingleExpression(valueContext);
      objectLiteralExpression.addProperty(propertyName, valueExpression);
    }
    return objectLiteralExpression;
  }

  private String parsePropertyName(PropertyNameContext propertyNameContext) {
    IdentifierNameContext identifierNameContext = propertyNameContext.identifierName();
    if (identifierNameContext!=null) {
      return identifierNameContext.getText();
    }
    TerminalNode stringLiteralNode = propertyNameContext.StringLiteral();
    if (stringLiteralNode!=null) {
      String propertyNameWithQuotes = stringLiteralNode.getText();
      String propertyNameWithoutQuotes = removeQuotesFromString(propertyNameWithQuotes);
      return propertyNameWithoutQuotes;
    }
    throw newUnsupportedException("propertyName", propertyNameContext);
  }

  private SingleExpression parseLiteralExpression(LiteralExpressionContext literalExpressionContext) {
    LiteralContext literalContext = literalExpressionContext.literal();
    if (literalContext!=null) {
      return parseLiteral(literalContext);
    }
    throw newUnsupportedException("literal", literalContext);
  }

  private SingleExpression parseLiteral(LiteralContext literalContext) {
    if (literalContext.StringLiteral()!=null) {
      return parseStringLiteral(literalContext);
    }

    TerminalNode nullLiteral = literalContext.NullLiteral();
    if (nullLiteral!=null) {
      return new Literal(createNextScriptElementId(), createLocation(literalContext));
    }

    NumericLiteralContext numericLiteralContext = literalContext.numericLiteral();
    if (numericLiteralContext!=null) {
      if (numericLiteralContext.DecimalLiteral()!=null) {
        return parseDecimalNumberLiteral(numericLiteralContext);
      }
      throw newUnsupportedException("number", numericLiteralContext);
    }

    TerminalNode regularExpressionLiteral = literalContext.RegularExpressionLiteral();
    if (regularExpressionLiteral!=null) {
      String regexText = regularExpressionLiteral.getText();
      throw newUnsupportedException("regex", regularExpressionLiteral);
    }

    TerminalNode booleanLiteral = literalContext.BooleanLiteral();
    if (booleanLiteral != null) {
      if ("true".equals(booleanLiteral.getText())) {
        return parseBooleanLiteral(literalContext, true);
      } else if ("false".equals(booleanLiteral.getText())) {
        return parseBooleanLiteral(literalContext, false);
      }
    }
    return null;
  }

  private Literal parseBooleanLiteral(LiteralContext literalContext, boolean value) {
    Literal literal = new Literal(createNextScriptElementId(), createLocation(literalContext));
    literal.setValue(value);
    return literal;
  }

  private Literal parseStringLiteral(LiteralContext literalContext) {
    Literal literal = new Literal(createNextScriptElementId(), createLocation(literalContext));
    String textWithQuotes = literalContext.getText();
    String textWithoutQuotes = removeQuotesFromString(textWithQuotes);
    literal.setValue(textWithoutQuotes);
    return literal;
  }

  private Literal parseDecimalNumberLiteral(NumericLiteralContext decimalNumberNode) {
    Literal literal = new Literal(createNextScriptElementId(), createLocation(decimalNumberNode));
    String numberText = decimalNumberNode.getText();
    Double number = new Double(numberText);
    literal.setValue(number);
    return literal;
  }

  private SingleExpression parseIdentifier(IdentifierExpressionContext singleExpressionContext) {
    String identifier = singleExpressionContext.getText();
    IdentifierExpression identifierExpression = new IdentifierExpression(createNextScriptElementId(), createLocation(singleExpressionContext));
    identifierExpression.setIdentifier(identifier);
    return identifierExpression;
  }

  private SingleExpression parseMemberDotExpression(MemberDotExpressionContext memberDotExpressionContext) {
    SingleExpressionContext baseExpressionContext = memberDotExpressionContext.singleExpression();

    MemberDotExpression memberDotExpression = new MemberDotExpression(createNextScriptElementId(), createLocation(memberDotExpressionContext));
    SingleExpression baseExpression = parseSingleExpression(baseExpressionContext);
    memberDotExpression.setBaseExpression(baseExpression);

    IdentifierNameContext identifierNameContext = memberDotExpressionContext.identifierName();
    String identifierText = identifierNameContext.getText();
    memberDotExpression.setPropertyName(identifierText);

    return memberDotExpression;
  }

  private SingleExpression parseArgumentsExpression(ArgumentsExpressionContext argumentsExpressionContext) {
    ArgumentsExpression argumentsExpression = new ArgumentsExpression(createNextScriptElementId(), createLocation(argumentsExpressionContext));

    List<SingleExpression> argumentExpressions = new ArrayList<>();
    ArgumentsContext argumentsContext = argumentsExpressionContext.arguments();
    ArgumentListContext argumentListContext = argumentsContext.argumentList();
    if (argumentListContext!=null) {
      List<SingleExpressionContext> argumentExpressionContexts = argumentListContext.singleExpression();
      for (SingleExpressionContext argumentExpressionContext: argumentExpressionContexts) {
        SingleExpression argumentExpression = parseSingleExpression(argumentExpressionContext);
        argumentExpressions.add(argumentExpression);
      }
    }
    argumentsExpression.setArgumentExpressions(argumentExpressions);

    SingleExpressionContext functionExpressionContext = argumentsExpressionContext.singleExpression();
    SingleExpression functionExpression = parseSingleExpression(functionExpressionContext);
    argumentsExpression.setFunctionExpression(functionExpression);
    return argumentsExpression;
  }

  private RuntimeException newUnsupportedException(String elementName, ParseTree object) {
    return new RuntimeException("Unsupported "+elementName+": "+(object!=null ? object.getText()+" ("+object.getClass().getSimpleName()+")" : "null"));
  }

  private <T extends ScriptElement> T initScriptElement(T scriptElement, ParserRuleContext parserRuleContext) {
    scriptElement.setIndex(createNextScriptElementId());
    scriptElement.setLocation(createLocation(parserRuleContext));
    return scriptElement;
  }

  private Integer createNextScriptElementId() {
    return nextScriptElementId++;
  }

  private Location createLocation(ParserRuleContext parserRuleContext) {
    return new Location(parserRuleContext);
  }

  private String removeQuotesFromString(String string) {
    if (string==null
      || !string.startsWith("'")
      || !string.endsWith("'")) {
      addError("invalid string "+string);
      return null;
    }
    return string.substring(1, string.length()-1);
  }
}