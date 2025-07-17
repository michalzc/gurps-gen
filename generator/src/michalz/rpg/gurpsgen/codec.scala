package michalz.rpg.gurpsgen.codec

import michalz.rpg.gurpsgen.model.Element
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Codec
import io.circe.generic.semiauto.*
import michalz.rpg.gurpsgen.model.ListElements
import michalz.rpg.gurpsgen.model.Generator
import michalz.rpg.gurpsgen.model.GeneratorElement
import io.circe.HCursor
import io.circe.Decoder.Result

val ElementRegEx = """([^\[^\]]+)\s+\[([-\d][\d]*)\]""".r

given Decoder[Element] = Decoder.decodeString.emap {
  case ElementRegEx(name, cost) => Right(Element(name, cost.toInt))
  case s                        => Left(s"$s is not proper Element")
}

given Encoder[Element] =
  Encoder.encodeString.contramap(elem => s"${elem.name} [${elem.cost}]")

given Decoder[ListElements] = deriveDecoder[ListElements]

given Decoder[Generator] = deriveDecoder[Generator]

given Decoder[GeneratorElement] = new Decoder[GeneratorElement] {

  override def apply(c: HCursor): Result[GeneratorElement] =
    c.as[Element].orElse(c.as[ListElements]).orElse(c.as[Generator])

}
