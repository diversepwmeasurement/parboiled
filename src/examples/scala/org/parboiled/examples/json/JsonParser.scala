package org.parboiled.examples.json

import org.parboiled.scala._
import java.lang.String
import org.parboiled.errors.{ErrorUtils, ParsingException}

/**
 * A complete JSON parser producing an AST representation of the parsed JSON source.
 */
class JsonParser extends Parser {

  /**
   * These case classes form the nodes of the AST.
   */
  sealed abstract class AstNode
  case class ObjectNode(members: List[MemberNode]) extends AstNode
  case class MemberNode(key: String, value: AstNode) extends AstNode
  case class ArrayNode(elements: List[AstNode]) extends AstNode
  case class StringNode(text: String) extends AstNode
  case class NumberNode(value: BigDecimal) extends AstNode
  case object True extends AstNode
  case object False extends AstNode
  case object Null extends AstNode

  // the root rule
  def JsonObject: Rule1[ObjectNode] = rule {
    WhiteSpace ~ "{ " ~ (Members | push(List.empty[MemberNode])) ~ "} " ~~> (list => ObjectNode(list.reverse))
  }

  def Members = rule {
    Pair ~~> (List(_)) ~ zeroOrMore(", " ~ Pair ~~> ((list: List[MemberNode], pair) => pair :: list))
  }

  def Pair = rule {
    JsonString ~ ": " ~ Value ~~> ((key, value) => MemberNode(key.text, value))
  }

  def Value: Rule1[AstNode] = rule {
    JsonString | JsonNumber | JsonObject | JsonArray | JsonTrue | JsonFalse | JsonNull
  }

  def JsonString = rule {
    "\"" ~ zeroOrMore(Character) ~> (StringNode(_)) ~ "\" "
  }

  def JsonNumber = rule {
    group(Integer ~ optional(Frac ~ optional(Exp))) ~> (s => NumberNode(BigDecimal(s))) ~ WhiteSpace
  }

  def JsonArray = rule {
    "[ " ~ Elements ~ "] " ~~> (elements => ArrayNode(elements.reverse))
  }

  def Elements = rule {
    Value ~~> (List(_)) ~ zeroOrMore(", " ~ Value ~~> ((list: List[AstNode], value) => value :: list))
  }

  def Character = rule { EscapedChar | NormalChar }

  def EscapedChar = rule { "\\" ~ (anyOf("\"\\/bfnrt") | Unicode) }

  def NormalChar = rule { !anyOf("\"\\") ~ ANY }

  def Unicode = rule { "u" ~ HexDigit ~ HexDigit ~ HexDigit ~ HexDigit }

  def Integer = rule { optional("-") ~ (("1" - "9") ~ Digits | Digit) }

  def Digits = rule { oneOrMore(Digit) }

  def Digit = rule { "0" - "9" }

  def HexDigit = rule { "0" - "9" | "a" - "f" | "A" - "Z" }

  def Frac = rule { "." ~ Digits }

  def Exp = rule { ignoreCase("e") ~ optional(anyOf("+-")) ~ Digits }

  def JsonTrue = rule { "true " ~ push(True) }

  def JsonFalse = rule { "false " ~ push(False) }

  def JsonNull = rule { "null " ~ push(Null) }

  def WhiteSpace = rule { zeroOrMore(anyOf(" \n\r\t\f")) }

  /**
   * We redefine the default string-to-rule conversion to also match trailing whitespace if the string ends with
   * a blank, this keeps the rules free from most whitespace matching clutter
   */
  override implicit def toRule(string: String) =
    if (string.endsWith(" "))
      str(string.trim) ~ WhiteSpace
    else
      str(string)

  /**
   * The main parsing method. Uses a ReportingParseRunner (which only reports the first error) for simplicity.
   */
  def parseJson(json: String): ObjectNode = {
    val result = ReportingParseRunner.run(JsonObject, json)
    if (result.hasErrors)
      throw new ParsingException("Invalid JSON source:\n" + ErrorUtils.printParseErrors(result))
    return result.resultValue
  }

}