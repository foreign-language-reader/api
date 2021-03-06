package com.foreignlanguagereader.domain.fetcher.english

import com.foreignlanguagereader.content.types.Language.Language
import com.foreignlanguagereader.content.types.external.definition.DefinitionEntry
import com.foreignlanguagereader.content.types.external.definition.webster.WebsterLearnersDefinitionEntry
import com.foreignlanguagereader.content.types.internal.definition.EnglishDefinition
import com.foreignlanguagereader.content.types.internal.word.PartOfSpeech.PartOfSpeech
import com.foreignlanguagereader.content.types.internal.word.Word
import com.foreignlanguagereader.domain.client.MirriamWebsterClient
import com.foreignlanguagereader.domain.client.circuitbreaker.CircuitBreakerResult
import com.foreignlanguagereader.domain.fetcher.DefinitionFetcher
import com.foreignlanguagereader.domain.metrics.MetricsReporter

import javax.inject.Inject
import play.api.libs.json.{Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}

class WebsterLearnersFetcher @Inject() (
    websterClient: MirriamWebsterClient,
    val metrics: MetricsReporter
) extends DefinitionFetcher[
      WebsterLearnersDefinitionEntry,
      EnglishDefinition
    ] {
  override def fetch(
      language: Language,
      word: Word
  )(implicit
      ec: ExecutionContext
  ): Future[CircuitBreakerResult[List[WebsterLearnersDefinitionEntry]]] =
    websterClient.getLearnersDefinition(word)

  override def convertToDefinition(
      entry: WebsterLearnersDefinitionEntry,
      tag: PartOfSpeech
  ): EnglishDefinition = DefinitionEntry.buildEnglishDefinition(entry, tag)

  override implicit val reads: Reads[WebsterLearnersDefinitionEntry] =
    WebsterLearnersDefinitionEntry.reads
  override implicit val writes: Writes[WebsterLearnersDefinitionEntry] =
    WebsterLearnersDefinitionEntry.writes
}
