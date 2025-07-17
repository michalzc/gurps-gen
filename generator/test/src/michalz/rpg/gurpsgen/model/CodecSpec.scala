package michalz.rpg.gurpsgen.model

import io.circe.syntax.*
import io.circe.Json
import michalz.rpg.gurpsgen.codec.given
import michalz.rpg.gurpsgen.model.Element
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import io.circe.Codec
import org.scalatest.EitherValues
import scala.xml.Elem

class CodecSpec extends AnyFreeSpec with Matchers with EitherValues {
  "A codec for" - {
    "Element" - {
      "should deserialize proper line" in {
        val json = "ST 11 [10]".asJson
        json.as[Element].right.value shouldEqual Element("ST 11", 10)
      }

      "should deserialize line with negative cost" in {
        val json = "Code of Honor 10 [-10]".asJson
        json.as[Element].right.value shouldEqual Element("Code of Honor 10", -10)
      }
    }

    "ListElements" - {
      "should deserialize simple list of elements" in {
        val json = Json.arr("Magic Resisance 1 [2]".asJson, "Status 1 [5]".asJson, "Code of Honor 10 [-10]".asJson)
        println(json)
        json.as[ListElements].right.value shouldEqual ListElements(List(
          Element("Magic Resisance 1", 2),
          Element("Status 1", 5),
          Element("Code of Honor", -10)
        ))
      }
    }
  }
}
