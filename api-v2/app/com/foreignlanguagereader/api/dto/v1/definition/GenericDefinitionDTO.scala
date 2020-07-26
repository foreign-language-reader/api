package com.foreignlanguagereader.api.dto.v1.definition

import com.foreignlanguagereader.api.domain.word.PartOfSpeech.PartOfSpeech
import play.api.libs.json.{Format, Json}
import sangria.macros.derive.{ObjectTypeDescription, deriveObjectType}
import sangria.schema.ObjectType

case class GenericDefinitionDTO(subdefinitions: List[String],
                                tag: Option[PartOfSpeech],
                                examples: Option[List[String]])
    extends DefinitionDTO
object GenericDefinitionDTO {
  implicit val format: Format[GenericDefinitionDTO] = Json.format

  implicit val graphQlType: ObjectType[Unit, GenericDefinitionDTO] =
    deriveObjectType[Unit, GenericDefinitionDTO](
      ObjectTypeDescription("A definition for a word")
    )
}
