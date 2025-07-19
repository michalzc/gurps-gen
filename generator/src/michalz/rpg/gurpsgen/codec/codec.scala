package michalz.rpg.gurpsgen.codec

import cats.syntax.traverse.*

import io.circe.Decoder
import io.circe.Encoder
import io.circe.HCursor
import io.circe.JsonObject
import io.circe.generic.semiauto.*

import michalz.rpg.gurpsgen.model.*

val ElementRegEx = """([^\[^\]]+)\s+\[([-\d][\d]*)\]""".r

given Decoder[TemplateTrait] = Decoder.decodeString.emap {
  case ElementRegEx(name, cost) =>
    Right(TemplateTrait(name, cost.toInt))
  case s => Left(s"$s is not a proper Element")
}

given Encoder[TemplateTrait] =
  Encoder.encodeString.contramap(elem => s"${elem.name} [${elem.cost}]")

given Decoder[TemplateGroup] = Decoder
  .decodeList[TemplateElement]
  .map(TemplateGroup.apply)

given Decoder[TemplateChoice] = deriveDecoder[TemplateChoice]

given Decoder[TemplateElement] =
  (c: HCursor) =>
    c.as[TemplateTrait]
      .orElse(c.as[TemplateGroup])
      .orElse(c.as[TemplateChoice])

given Decoder[NPCTemplate] = (c: HCursor) => {
  for {
    obj <- c.as[JsonObject]
    lst <- obj.toList
      .traverse((k, v) =>
        v.as[TemplateElement]
          .map(vv => k -> vv)
      )
  } yield NPCTemplate(lst.toMap)
}
