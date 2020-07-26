package com.foreignlanguagereader.api.contentsource.definition

import com.foreignlanguagereader.api.contentsource.definition.cedict.CEDICTDefinitionEntry
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.{
  ChineseDefinition,
  Definition,
  DefinitionSource
}
import com.foreignlanguagereader.api.domain.word.PartOfSpeech.PartOfSpeech
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

  def toDefinition(partOfSpeech: PartOfSpeech): Definition = {
    val part = tag match {
      case Some(t) => t
      case None    => partOfSpeech
    }
    wordLanguage match {
      case Language.CHINESE =>
        DefinitionEntry.buildChineseDefinition(this, part)
      case Language.CHINESE_TRADITIONAL =>
        DefinitionEntry.buildChineseDefinition(this, part)
      case _ =>
        Definition(
          subdefinitions = subdefinitions,
          ipa = pronunciation,
          tag = part,
          examples = examples,
          wordLanguage = wordLanguage,
          definitionLanguage = definitionLanguage,
          source = source,
          token = token
        )
    }
  }
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
      override def writes(o: DefinitionEntry): JsValue = o match {
        case c: CEDICTDefinitionEntry => CEDICTDefinitionEntry.format.writes(c)
        case w: WiktionaryDefinitionEntry =>
          WiktionaryDefinitionEntry.format.writes(w)
      }
    }
  implicit val readsSeq: Reads[Seq[DefinitionEntry]] =
    Reads.seq(formatDefinitionEntry)

  def buildChineseDefinition(entry: DefinitionEntry,
                             partOfSpeech: PartOfSpeech): ChineseDefinition =
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
}
