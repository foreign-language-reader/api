package com.foreignlanguagereader.content.types.external.definition

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.external.definition.cedict.CEDICTDefinitionEntry
import com.foreignlanguagereader.content.types.external.definition.wiktionary.WiktionaryDefinitionEntry
import com.foreignlanguagereader.content.types.internal.ElasticsearchCacheable
import com.foreignlanguagereader.content.types.internal.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.content.types.internal.definition._
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import play.api.libs.json._

trait DefinitionEntry {
  val subdefinitions: List[String]
  val pronunciation: String
  val tag: Option[PartOfSpeech]
  val examples: Option[List[String]]

  val wordLanguage: Language
  val definitionLanguage: Language
  val source: DefinitionSource
  val token: String

  def toDefinition(partOfSpeech: PartOfSpeech): Definition
}

object DefinitionEntry {
  // Smart json handling that dispatches to the correct class
  implicit val formatDefinitionEntry: Format[DefinitionEntry] =
    new Format[DefinitionEntry] {
      override def reads(json: JsValue): JsResult[DefinitionEntry] = {
        json \ "source" match {
          case JsDefined(JsString(source)) =>
            DefinitionSource.fromString(source) match {
              case Some(DefinitionSource.CEDICT) =>
                CEDICTDefinitionEntry.format.reads(json)
              case Some(DefinitionSource.WIKTIONARY) =>
                WiktionaryDefinitionEntry.format.reads(json)
              case _ =>
                JsError("Unknown definition source")
            }
          case _ =>
            JsError(
              "Definition source was not defined, cannot decide how to handle"
            )
        }
      }
      override def writes(o: DefinitionEntry): JsValue =
        o match {
          case c: CEDICTDefinitionEntry =>
            CEDICTDefinitionEntry.format.writes(c)
          case w: WiktionaryDefinitionEntry =>
            WiktionaryDefinitionEntry.format.writes(w)
        }
    }
  implicit val readsList: Reads[List[DefinitionEntry]] =
    Reads.list(formatDefinitionEntry)

  def buildChineseDefinition(
      entry: DefinitionEntry,
      partOfSpeech: PartOfSpeech
  ): ChineseDefinition =
    ChineseDefinition(
      subdefinitions = entry.subdefinitions,
      tag = partOfSpeech,
      examples = entry.examples,
      inputPinyin = entry.pronunciation,
      inputSimplified = None,
      inputTraditional = None,
      definitionLanguage = entry.definitionLanguage,
      source = entry.source,
      token = entry.token
    )

  def buildEnglishDefinition(
      entry: DefinitionEntry,
      partOfSpeech: PartOfSpeech
  ): EnglishDefinition =
    EnglishDefinition(
      subdefinitions = entry.subdefinitions,
      ipa = entry.pronunciation,
      tag = partOfSpeech,
      examples = entry.examples,
      wordLanguage = entry.wordLanguage,
      definitionLanguage = entry.definitionLanguage,
      source = entry.source,
      token = entry.token
    )

  def buildSpanishDefinition(
      entry: DefinitionEntry,
      partOfSpeech: PartOfSpeech
  ): SpanishDefinition =
    SpanishDefinition(
      subdefinitions = entry.subdefinitions,
      ipa = entry.pronunciation,
      tag = partOfSpeech,
      examples = entry.examples,
      wordLanguage = entry.wordLanguage,
      definitionLanguage = entry.definitionLanguage,
      source = entry.source,
      token = entry.token
    )

  def toCacheable[T <: DefinitionEntry](entry: T): ElasticsearchCacheable[T] =
    ElasticsearchCacheable(
      item = entry,
      fields = Map(
        "source" -> entry.source.toString,
        "wordLanguage" -> entry.wordLanguage.toString,
        "definitionLanguage" -> entry.definitionLanguage.toString,
        "token" -> entry.token
      )
    )
}
