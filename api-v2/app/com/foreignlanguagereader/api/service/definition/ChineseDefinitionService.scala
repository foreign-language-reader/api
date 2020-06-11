package com.foreignlanguagereader.api.service.definition

import com.foreignlanguagereader.api.client.{
  ElasticsearchClient,
  LanguageServiceClient
}
import com.foreignlanguagereader.api.domain.Language
import com.foreignlanguagereader.api.domain.Language.Language
import com.foreignlanguagereader.api.domain.definition.combined.{
  ChineseDefinition,
  Definition
}
import com.foreignlanguagereader.api.domain.definition.entry.DefinitionSource.DefinitionSource
import com.foreignlanguagereader.api.domain.definition.entry.{
  CEDICTDefinitionEntry,
  DefinitionEntry,
  DefinitionSource,
  WiktionaryDefinitionEntry
}
import javax.inject.Inject
import play.api.Logger

import scala.concurrent.ExecutionContext

/**
  * Language specific handling for Chinese.
  * We have two dictionaries here, so we should combine them to produce the best possible results
  * In particular, CEDICT has a minimum level of quality, but doesn't have as many definitions.
  */
class ChineseDefinitionService @Inject()(
  val elasticsearch: ElasticsearchClient,
  val languageServiceClient: LanguageServiceClient,
  implicit val ec: ExecutionContext
) extends LanguageDefinitionService {
  override val logger: Logger = Logger(this.getClass)

  override val wordLanguage: Language = Language.CHINESE
  override val sources: Set[DefinitionSource] =
    Set(DefinitionSource.CEDICT, DefinitionSource.WIKTIONARY)
  override val webSources: Set[DefinitionSource] = Set(
    DefinitionSource.WIKTIONARY
  )

  override def enrichDefinitions(
    definitionLanguage: Language,
    word: String,
    definitions: Seq[DefinitionEntry]
  ): Option[Seq[Definition]] = {
    val (cedict, wiktionary) = partitionResultsByDictionary(definitions)
    logger.info(
      s"Enhancing results for $word using cedict with ${cedict.size} cedict results and ${wiktionary.size} wiktionary results"
    )

    (cedict, wiktionary) match {
      case (cedict, wiktionary) if cedict.isEmpty && wiktionary.isEmpty =>
        logger.info(s"No definitions found for $word")
        None
      case (cedict, wiktionary) if wiktionary.isEmpty =>
        logger.info(s"Using cedict definitions for $word")
        Some(cedict.map(_.toDefinition))
      case (cedict, wiktionary) if cedict.isEmpty =>
        logger.info(s"Using wiktionary definitions for $word")
        Some(wiktionary.map(_.toDefinition))
      // If CEDICT doesn't have subdefinitions, then we should return wiktionary data
      // We still want pronunciation and simplified/traditional mapping, so we will add cedict data
      case (cedict, wiktionary) if cedict(0).subdefinitions.isEmpty =>
        logger.info(s"Using enhanced wiktionary definitions for $word")
        Some(addCedictDataToWiktionaryResults(word, cedict(0), wiktionary))
      // If are definitions from CEDICT, they are better.
      // In that case, we only want part of speech tag and examples from wiktionary.
      // But everything else will be the single CEDICT definition
      case (cedict, wiktionary) =>
        logger.info(s"Using enhanced cedict definitions for $word")
        Some(addWiktionaryDataToCedictResults(word, cedict(0), wiktionary))
    }
  }

  private def partitionResultsByDictionary(
    definitions: Seq[DefinitionEntry]
  ): (List[CEDICTDefinitionEntry], List[WiktionaryDefinitionEntry]) = {
    definitions.foldLeft(
      (List[CEDICTDefinitionEntry](), List[WiktionaryDefinitionEntry]())
    )(
      (acc, entry) =>
        entry match {
          case c: CEDICTDefinitionEntry     => (c :: acc._1, acc._2)
          case w: WiktionaryDefinitionEntry => (acc._1, w :: acc._2)
      }
    )
  }

  private def addCedictDataToWiktionaryResults(
    word: String,
    cedict: CEDICTDefinitionEntry,
    wiktionary: Seq[WiktionaryDefinitionEntry]
  ): Seq[ChineseDefinition] = {
    wiktionary.map(
      w =>
        ChineseDefinition(
          w.subdefinitions,
          w.tag,
          w.examples,
          cedict.pinyin,
          cedict.simplified,
          cedict.traditional
      )
    )
  }

  private def addWiktionaryDataToCedictResults(
    word: String,
    cedict: CEDICTDefinitionEntry,
    wiktionary: Seq[WiktionaryDefinitionEntry]
  ): Seq[ChineseDefinition] = {
    val examples = wiktionary.foldLeft(List[String]())(
      (acc, entry: WiktionaryDefinitionEntry) => {
        acc ++ entry.examples
      }
    )
    Seq(
      ChineseDefinition(
        cedict.subdefinitions,
        wiktionary(0).tag,
        examples,
        cedict.pinyin,
        cedict.simplified,
        cedict.traditional
      )
    )
  }
}
