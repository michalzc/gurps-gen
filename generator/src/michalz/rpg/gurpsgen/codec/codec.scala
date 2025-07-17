package michalz.rpg.gurpsgen.codec

import io.circe.Decoder
import io.circe.Encoder
import io.circe.HCursor
import io.circe.generic.semiauto.*
import michalz.rpg.gurpsgen.model.{Element, Generator, GeneratorElement, ListElements, NPCGenerator}

val ElementRegEx = """([^\[^\]]+)\s+\[([-\d][\d]*)\]""".r

given Decoder[Element] = Decoder.decodeString.emap {
  case ElementRegEx(name, cost) =>
    Right(Element(name, cost.toInt))
  case s => Left(s"$s is not proper Element")
}

given Encoder[Element] =
  Encoder.encodeString.contramap(elem =>
    s"${elem.name} [${elem.cost}]"
  )

given Decoder[ListElements] = Decoder
  .decodeList[GeneratorElement]
  .map(ListElements.apply)

given Decoder[Generator] = deriveDecoder[Generator]

given Decoder[GeneratorElement] =
  (c: HCursor) => c.as[Element]
    .orElse(c.as[ListElements])
    .orElse(c.as[Generator])

given Decoder[NPCGenerator] = deriveDecoder[NPCGenerator]