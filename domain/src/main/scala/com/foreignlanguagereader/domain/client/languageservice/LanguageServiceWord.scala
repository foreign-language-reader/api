package com.foreignlanguagereader.domain.client.languageservice

import play.api.libs.json.{Format, Json}

case class LanguageServiceWord(
    token: String,
    tag: String,
    lemma: String,
    definitions: List[String]
)

object LanguageServiceWord {
  implicit val format: Format[LanguageServiceWord] =
    Json.format[LanguageServiceWord]
}
