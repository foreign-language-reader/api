package com.foreignlanguagereader.api.domain.definition

import play.api.libs.json._

/*
 * An enum that defines where a definition came from
 */
object DefinitionSource extends Enumeration {
  type DefinitionSource = Value

  val CEDICT: Value = Value("CEDICT")
  val MIRRIAM_WEBSTER_LEARNERS: Value = Value("MIRRIAM_WEBSTER_LEARNERS")
  val MIRRIAM_WEBSTER_SPANISH: Value = Value("MIRRIAM_WEBSTER_SPANISH")
  val WIKTIONARY: Value = Value("WIKTIONARY")
  val MULTIPLE: Value = Value("MULTIPLE")

  def fromString(source: String): Option[DefinitionSource] =
    DefinitionSource.values.find(_.toString == source)

  // Makes sure we can serialize and deserialize this to JSON
  implicit val sourceFormat: Format[DefinitionSource] =
    new Format[DefinitionSource] {
      def reads(json: JsValue): JsResult[DefinitionSource] =
        fromString(json.as[String]) match {
          case Some(source) => JsSuccess(source)
          case None         => JsError("Unknown definition source")
        }
      def writes(source: DefinitionSource.DefinitionSource) =
        JsString(source.toString)
    }
}